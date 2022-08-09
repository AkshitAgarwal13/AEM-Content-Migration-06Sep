package com.aem.migration.core.services.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.migration.core.aem.dto.AEMPage;
import com.aem.migration.core.aem.dto.components.AEMComponent;
import com.aem.migration.core.services.ContentProcessorService;
import com.aem.migration.core.utils.MigrationUtil;
import com.aem.migration.core.wordpress.dto.WPComponent;
import com.aem.migration.core.wordpress.dto.WPPageList;
import com.aem.migration.core.wordpress.dto.WordPressPage;
import com.day.cq.dam.api.Asset;
import com.google.gson.Gson;


/**
 * The Class ContentProcessorServiceImpl.
 */
@Component(
	service = { ContentProcessorService.class }
)
@Designate(ocd = ContentProcessorServiceImpl.Config.class)
public class ContentProcessorServiceImpl implements ContentProcessorService {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ContentProcessorServiceImpl.class);

	/** The resolver factory. */
	@Reference
	private ResourceResolverFactory resolverFactory;

	/**
	 * The Interface Config.
	 */
	@ObjectClassDefinition(
		name = "Migration - Configuration",
		description = "OSGi Service - Configuration to support migration process."
	)
	@interface Config {

		/**
		 * Source content extract file path.
		 *
		 * @return the string
		 */
		@AttributeDefinition(
			name = "Content Source File Path (in DAM)",
			description = "Path of the file having the content extract from source CMS."
		)
		String sourceContentExtractFilePath() default "/content/dam/migration/single-page-extract.json";

		/**
		 * Aem component property mapping.
		 *
		 * @return the string[]
		 */
		@AttributeDefinition(
				name = "AEM Component Name and Properties",
				description = "A mapping of AEM components and associated properties(comma separated).",
				type = AttributeType.STRING
		)
		String[] aemComponentPropertyMapping();

		/**
		 * Aem obj to JCR prop map.
		 *
		 * @return the string[]
		 */
		@AttributeDefinition(
				name = "AEM Object and JCR Property mapping",
				description = "AEM Object and JCR Property mapping. List of properties to replace in AEM page JSON.",
				type = AttributeType.STRING
		)
		String[] aemObjToJCRPropMap();

		/**
		 * Html elementsto parse list.
		 *
		 * @return the string[]
		 */
		@AttributeDefinition(
				name = "HTML Elements to parse",
				description = "List of the HTML elements to parse and their corresponding component type(in AEM). For example - <figure> element is mapped to image component in AEM. Applicable to CMSs storing content as HTML markup, ex - Wordpress"
		)
		String[] htmlElementstoParseList() default "{figure.img=image,figure.a.img,figure.table}";
		
		/**
		 * Source DAM root path.
		 *
		 * @return the string
		 */
		@AttributeDefinition(
				name = "DAM Assets Root Path (Source CMS) ",
				description = "Root Path of the DAM folder in source CMS. This will be updated with the AEM DAM path."
		)
		String sourceDAMRootPath() default "http://localhost:8080/wordpress_sample_db/wp-content";
		
		/**
		 * Aem DAM root path.
		 *
		 * @return the string
		 */
		@AttributeDefinition(
				name = "DAM Assets Root Path (AEM) ",
				description = "Root Path of the AEM DAM folder."
		)
		String aemDAMRootPath() default "/content/dam/migration";
		
		/**
		 * Aem site root path.
		 *
		 * @return the string
		 */
		@AttributeDefinition(
				name = "Site Root Path (AEM) ",
				description = "Root Path of the AEM Site. Pages will be created under this path."
		)
		String aemSiteRootPath() default "http://localhost:4502/content/migration/us/en";
		
		/**
		 * Source CMS site root path.
		 *
		 * @return the string
		 */
		@AttributeDefinition(
				name = "Site Root Path (Source CMS) ",
				description = "Root Path of the Site in Source CMS."
		)
		String sourceCMSSiteRootPath() default "http://localhost:8080/wordpress_sample_db";
	}

	/** The source content extract file path. */
	private String sourceContentExtractFilePath;

	/** The component aem property mapping. */
	private String[] aemComponentPropertyMapping;

	/** The aem obj to JCR prop map. */
	private String[] aemObjToJCRPropMap;

	/** The html elementsto parse list. */
	private String[] htmlElementstoParseList;
	
	/** The source DAM root path. */
	private String sourceDAMRootPath;

	/** The aem DAM root path. */
	private String aemDAMRootPath;
	
	/** The aem site root path. */
	private String aemSiteRootPath;
	
	/** The source CMS site root path. */
	private String sourceCMSSiteRootPath;

	/**
	 * Gets the source content extract file path.
	 *
	 * @return the source content extract file path
	 */
	public String getSourceContentExtractFilePath() {

		return sourceContentExtractFilePath;
	}

	/**
	 * Gets the component prperty mapping.
	 *
	 * @return the component prperty mapping
	 */
	public String[] getAEMComponentPropertyMapping() {

		return aemComponentPropertyMapping;
	}

	/**
	 * Gets the aem obj to JCR prop map.
	 *
	 * @return the aem obj to JCR prop map
	 */
	public String[] getAemObjToJCRPropMap() {

		return aemObjToJCRPropMap;
	}

	/**
	 * Gets the html elementsto parse list.
	 *
	 * @return the html elementsto parse list
	 */
	public String[] getHtmlElementstoParseList() {

		return htmlElementstoParseList;
	}
	
	/**
	 * Gets the source DAM root path.
	 *
	 * @return the source DAM root path
	 */
	public String getSourceDAMRootPath() {

		return sourceDAMRootPath;
	}

	/**
	 * Gets the aem DAM root path.
	 *
	 * @return the aem DAM root path
	 */
	public String getAemDAMRootPath() {

		return aemDAMRootPath;
	}
	
	/**
	 * Gets the aem site root path.
	 *
	 * @return the aem site root path
	 */
	public String getAemSiteRootPath() {

		return aemSiteRootPath;
	}
	
	/**
	 * Gets the source CMS site root path.
	 *
	 * @return the source CMS site root path
	 */
	public String getSourceCMSSiteRootPath() {

		return sourceCMSSiteRootPath;
	}

	/**
	 * Activate.
	 *
	 * @param config the config
	 */
	@Activate
	protected void activate(Config config) {

		this.sourceContentExtractFilePath = config.sourceContentExtractFilePath();
		this.aemComponentPropertyMapping = config.aemComponentPropertyMapping();
		this.aemObjToJCRPropMap = config.aemObjToJCRPropMap();
		this.htmlElementstoParseList = config.htmlElementstoParseList();
		this.sourceDAMRootPath = config.sourceDAMRootPath();
		this.aemDAMRootPath = config.aemDAMRootPath();
		this.aemSiteRootPath = config.aemSiteRootPath();
		this.sourceCMSSiteRootPath = config.sourceCMSSiteRootPath();
		log.info("File path of the source content extract is {}", this.sourceContentExtractFilePath);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	protected void deactivate() {
		log.info("ActivitiesImpl has been deactivated!");
	}

	/**
	 * Gets the source content extract.
	 *
	 * @return the source content extract
	 */
	@Override
	public BufferedReader getSourceContentExtract(String damPath) {

		/* Reading the JSON File from DAM. */
		Resource original;
		BufferedReader br = null;
		InputStream content = null;
		// Map<String, Object> param = new HashMap<>();
		// param.put(ResourceResolverFactory.SUBSERVICE, "readService"); //readService
		// is System User.
		try {
			if(StringUtils.isBlank(damPath)) {
				damPath = this.sourceContentExtractFilePath;
			}
			ResourceResolver resolver = resolverFactory.getAdministrativeResourceResolver(null); // Change this to get resolver using service user.																				
			Resource resource = resolver.getResource(damPath);
			Asset asset = resource.adaptTo(Asset.class);
			original = asset.getOriginal();
			content = original.adaptTo(InputStream.class);
			br = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));
			return br;
		} catch (LoginException exc) {

			log.info("Exception while reading the source content file", exc);
		}
		return null;
	}

	/**
	 * Gets the WP page object.
	 *
	 * @return the WP page object
	 */
	@Override
	public List<WordPressPage> getWPPagesList(String damPath) {

		BufferedReader pageJSONReader = getSourceContentExtract(damPath);
		WPPageList wpPageList = null;
		if(pageJSONReader != null) {

			wpPageList = deserializeResult(pageJSONReader, WPPageList.class);
		}
		List<WordPressPage> pageList = new ArrayList<>();
		if(wpPageList != null) {

			for(WordPressPage wpPage : wpPageList.getPageList()) {

				pageList.add(extractWPPageComponents(wpPage));
				
			}
		}
		return pageList;
	}

	/**
	 * Deserialize result.
	 *
	 * @param <T>          the generic type
	 * @param pageContent the response body
	 * @param declaredType the declared type
	 * @return the t
	 */
	public <T extends Object> T deserializeResult(final BufferedReader pageContent, final Class<T> declaredType) {

		Gson gson = new Gson();
		return gson.fromJson(pageContent, declaredType);
	}

	/**
	 * Extract WP page components.
	 *
	 * @param wpPage the wp page
	 * @return the word press page
	 */
	@Override
	public WordPressPage extractWPPageComponents(WordPressPage wpPage) {

		if(wpPage != null && wpPage.getContent() != null && StringUtils.isNotBlank(wpPage.getContent().getRendered())) {

			String pageHTML = wpPage.getContent().getRendered();
			Document html = Jsoup.parse(pageHTML);
			Elements elements = html.getAllElements();
			int counter = 0;
			List<WPComponent> componentsList = new ArrayList<>();
			Map<String, String> htmlElementsToAEMComponentMap = MigrationUtil.getHTMLElementsToAEMComponentMap(this.htmlElementstoParseList);
			for(Element element : elements) {

				WPComponent component = MigrationUtil.getComponent(element, htmlElementsToAEMComponentMap);
				if(component != null) {

					log.info("Element names are {} && {}", element.nodeName(), element.parent().nodeName());
					component.setAemDAMRootPath(this.aemDAMRootPath);
					component.setSourceCMSDAMRootPath(this.sourceDAMRootPath);
					componentsList.add(component);
					counter++;
				}
			}
			log.info("Number of components {}", counter);
			wpPage.setWpComponentsList(componentsList);
		}
		return wpPage;
	}

	/**
	 * Gets the AEM page create script.
	 *
	 * @param aemPageList the aem page list
	 * @return the AEM page create script
	 */
	@Override
	public String getAEMPageCreateScript(List<AEMPage> aemPageList) {

		StringBuilder sb = new StringBuilder();
		
		String pagePath = null;

		int counter = 1;
		for (AEMPage aemPage : aemPageList) {

			sb.append(":: ## ******************Page count " + counter + " current page title " + aemPage.getJcrContent().getJcr_title());
			sb.append("\n\n");
			
			pagePath = MigrationUtil.getPagePath(aemPage, this.aemSiteRootPath, this.sourceCMSSiteRootPath);
			
			sb.append("curl -u admin:admin -X POST -d \"jcr:primaryType=");
			sb.append(aemPage.getJcr_primaryType() + "\"");
			sb.append(" -d \"jcr:content/jcr:primaryType=");
			sb.append(aemPage.getJcrContent().getJcr_primaryType() + "\"");
			sb.append(" -d \"jcr:content/jcr:title=");
			sb.append(aemPage.getJcrContent().getJcr_title() + "\"");
			sb.append(" -d \"jcr:content/cq:template=");
			sb.append(aemPage.getJcrContent().getCq_template() + "\"");
			sb.append(" -d \"jcr:content/sling:resourceType=");
			sb.append(aemPage.getJcrContent().getSling_resourceType() + "\"");
			sb.append(" " + pagePath);
			sb.append(" -d \"jcr:content/root/layout=responsiveGrid\"");
			sb.append(" -d \"jcr:content/root/sling:resourceType=migration/components/container\"");
			sb.append(" -d \"jcr:content/root/container/layout=responsiveGrid\"");
			sb.append(" -d \"jcr:content/root/container/sling:resourceType=migration/components/container\"");
			sb.append(" -d \"jcr:content/root/container/container/sling:resourceType=migration/components/container\"");

			List<AEMComponent> components = aemPage.getJcrContent().getRootNode().getContainer().getChildContainerNode()
					.getComponentsList();
			Map<String, String[]> mapCompProp = MigrationUtil
					.getComponentJCRPropertiesMap(this.aemComponentPropertyMapping);
			if (MapUtils.isNotEmpty(mapCompProp)) {

				for (int count = 0; count < components.size(); count++) {

					AEMComponent aemComp = components.get(count);
					sb.append(MigrationUtil.getComponentJCRProperties(aemComp, mapCompProp, true));
				}
			}
			counter++;
		}

		return sb.toString();
	}

	/**
	 * Gets the AEM page JSON.
	 *
	 * @param aemPage the aem page
	 * @return the AEM page JSON
	 */
	@Override
	public String getAEMPageJSON(List<AEMPage> aemPage) {
		
		if(aemPage != null) {
			
			Gson gson = new Gson();
			return MigrationUtil.getAEMPageJSON(gson.toJson(aemPage), this.aemObjToJCRPropMap);
		}
		return null;
	}

	/**
	 * Creates the AEM page.
	 *
	 * @param aemPageList the aem page list
	 * @return the string
	 */
	@Override
	public String createAEMPage(AEMPage aemPage, String destPath) {
		
		URL url;
		if(StringUtils.isBlank(destPath)) {
			destPath = this.aemSiteRootPath;
		}
		String pagePath = MigrationUtil.getPagePath(aemPage, destPath, this.sourceCMSSiteRootPath);
		try {
			url = new URL(pagePath);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("POST");

			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			byte[] message = ("admin:admin").getBytes(StandardCharsets.UTF_8);
			String basicAuth = DatatypeConverter.printBase64Binary(message);
			httpConn.setRequestProperty("Authorization", "Basic " + basicAuth);

			httpConn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
			StringBuilder sb = new StringBuilder();
			sb.append("jcr:primaryType=" + aemPage.getJcr_primaryType()); 
			sb.append("&jcr:content/jcr:primaryType=" + aemPage.getJcrContent().getJcr_primaryType()); 
			sb.append("&jcr:content/jcr:title=" + aemPage.getJcrContent().getJcr_title()); 
			sb.append("&jcr:content/cq:template=" + aemPage.getJcrContent().getCq_template()); 
			sb.append("&jcr:content/sling:resourceType=" + aemPage.getJcrContent().getSling_resourceType()); 
			sb.append("&jcr:content/root/layout=responsiveGrid");
			sb.append("&jcr:content/root/sling:resourceType=migration/components/container");
			sb.append("&jcr:content/root/container/layout=responsiveGrid");
			sb.append("&jcr:content/root/container/sling:resourceType=migration/components/container");
			sb.append("&jcr:content/root/container/container/sling:resourceType=migration/components/container");

			List<AEMComponent> components = aemPage.getJcrContent().getRootNode().getContainer().getChildContainerNode()
					.getComponentsList();
			Map<String, String[]> mapCompProp = MigrationUtil
					.getComponentJCRPropertiesMap(this.aemComponentPropertyMapping);
			if (MapUtils.isNotEmpty(mapCompProp)) {

				for (int count = 0; count < components.size(); count++) {

					AEMComponent aemComp = components.get(count);
					sb.append(MigrationUtil.getComponentJCRProperties(aemComp, mapCompProp, false));
				}
			}
			writer.write(sb.toString());
			writer.flush();
			writer.close();
			httpConn.getOutputStream().close();

			InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream()
					: httpConn.getErrorStream();
			Scanner s = new Scanner(responseStream).useDelimiter("\\A");
			String response = s.hasNext() ? s.next() : "";
			log.info(response);
			return pagePath;

		} catch (MalformedURLException e) {
			
			log.error("MalformedURLException", e);
		} catch (IOException ioExc) {
			
			log.error("IOException", ioExc);
		}
		return null;
	}

}
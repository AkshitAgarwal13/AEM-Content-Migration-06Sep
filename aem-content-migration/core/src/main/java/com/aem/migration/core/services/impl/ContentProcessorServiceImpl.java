package com.aem.migration.core.services.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

@Component(
	service = { ContentProcessorService.class }
)
@Designate(ocd = ContentProcessorServiceImpl.Config.class)
public class ContentProcessorServiceImpl implements ContentProcessorService {

	private static final Logger log = LoggerFactory.getLogger(ContentProcessorServiceImpl.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@ObjectClassDefinition(
		name = "Migration - Configuration",
		description = "OSGi Service - Configuration to support migration process."
	)
	@interface Config {

		@AttributeDefinition(
			name = "Content Source File Path (in DAM)",
			description = "Path of the file having the content extract from source CMS."
		)
		String sourceContentExtractFilePath() default "/content/dam/migration/single-page-extract.json";

		@AttributeDefinition(
				name = "AEM Component Name and Properties",
				description = "A mapping of AEM components and associated properties(comma separated).",
				type = AttributeType.STRING
		)
		String[] aemComponentPropertyMapping();

		@AttributeDefinition(
				name = "Source CMS Component Name and Properties",
				description = "A mapping of source CMS components and associated properties(comma separated).",
				type = AttributeType.STRING
		)
		String[] sourceCMScomponentPropertyMapping();

		@AttributeDefinition(
				name = "AEM Object and JCR Property mapping",
				description = "AEM Object and JCR Property mapping. List of properties to replace in AEM page JSON.",
				type = AttributeType.STRING
		)
		String[] aemObjToJCRPropMap();

		@AttributeDefinition(
				name = "HTML Elements to parse",
				description = "List of the HTML elements to parse and their corresponding component type(in AEM). For example - <figure> element is mapped to image component in AEM. Applicable to CMSs storing content as HTML markup, ex - Wordpress"
		)
		String[] htmlElementstoParseList() default "figure=image";
		
		@AttributeDefinition(
				name = "DAM Assets Root Path (Source CMS) ",
				description = "Root Path of the DAM folder in source CMS. This will be updated with the AEM DAM path."
		)
		String sourceDAMRootPath() default "http://localhost:8080/wordpress_sample_db/wp-content";
		
		@AttributeDefinition(
				name = "DAM Assets Root Path (Target/AEM CMS) ",
				description = "Root Path of the AEM DAM folder."
		)
		String aemDAMRootPath() default "/content/dam/migration";

	}

	/** The source content extract file path. */
	private String sourceContentExtractFilePath;

	/** The component aem property mapping. */
	private String[] aemComponentPropertyMapping;

	/** The component source CMS property mapping. */
	private String[] sourceCMScomponentPropertyMapping;

	/** The aem obj to JCR prop map. */
	private String[] aemObjToJCRPropMap;

	/** The html elementsto parse list. */
	private String[] htmlElementstoParseList;
	
	/** The source DAM root path. */
	private String sourceDAMRootPath;

	/** The aem DAM root path. */
	private String aemDAMRootPath;

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
	 * Gets the source CM scomponent property mapping.
	 *
	 * @return the source CM scomponent property mapping
	 */
	public String[] getSourceCMScomponentPropertyMapping() {

		return sourceCMScomponentPropertyMapping;
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

	@Activate
	protected void activate(Config config) {

		this.sourceContentExtractFilePath = config.sourceContentExtractFilePath();
		this.aemComponentPropertyMapping = config.aemComponentPropertyMapping();
		this.aemObjToJCRPropMap = config.aemObjToJCRPropMap();
		this.htmlElementstoParseList = config.htmlElementstoParseList();
		this.sourceDAMRootPath = config.sourceDAMRootPath();
		this.aemDAMRootPath = config.aemDAMRootPath();
		log.info("File path of the source content extract is {}", this.sourceContentExtractFilePath);
	}

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
	public BufferedReader getSourceContentExtract() {

		/* Reading the JSON File from DAM. */
		Resource original;
		BufferedReader br = null;
		InputStream content = null;
		// Map<String, Object> param = new HashMap<>();
		// param.put(ResourceResolverFactory.SUBSERVICE, "readService"); //readService
		// is System User.
		try {

			ResourceResolver resolver = resolverFactory.getAdministrativeResourceResolver(null); // Change this to get resolver using service user.																				
			Resource resource = resolver.getResource(this.sourceContentExtractFilePath);
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
	public List<WordPressPage> getWPPagesList() {

		BufferedReader pageJSONReader = getSourceContentExtract();
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

				if(element != null && htmlElementsToAEMComponentMap.containsKey(element.nodeName())) {

					log.info("Element names are {} && {}", element.nodeName(), element.parent().nodeName());
					WPComponent component = MigrationUtil.getComponent(element, htmlElementsToAEMComponentMap);
					if(component != null) {

						component.setAemDAMRootPath(this.aemDAMRootPath);
						component.setSourceCMSDAMRootPath(this.sourceDAMRootPath);
						componentsList.add(component);
						counter++;
					}
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
	 * @param aemPage the aem page
	 * @return the AEM page create script
	 */
	@Override
	public String getAEMPageCreateScript(List<AEMPage> aemPageList) {

		StringBuilder sb = new StringBuilder();

		int counter = 1;
		for (AEMPage aemPage : aemPageList) {

			sb.append("curl -u admin:admin -F \"jcr:primaryType=");
			sb.append(aemPage.getJcr_primaryType() + "\"");
			sb.append(" -F \"jcr:content/jcr:primaryType=");
			sb.append(aemPage.getJcrContent().getJcr_primaryType() + "\"");
			sb.append(" -F \"jcr:content/jcr:title=");
			sb.append(aemPage.getJcrContent().getJcr_title() + "\"");
			sb.append(" -F \"jcr:content/sling:resourceType=migration/components/page\"");
			sb.append(" http://localhost:4502/content/migration/us/en/new-page-1" + counter);
			sb.append(" -F \"jcr:content/root/layout=responsiveGrid\"");
			sb.append(" -F \"jcr:content/root/sling:resourceType=migration/components/container\"");
			sb.append(" -F \"jcr:content/root/container/layout=responsiveGrid\"");
			sb.append(" -F \"jcr:content/root/container/sling:resourceType=migration/components/container\"");
			sb.append(" -F \"jcr:content/root/container/container/sling:resourceType=migration/components/container\"");

			List<AEMComponent> components = aemPage.getJcrContent().getRootNode().getContainer().getChildContainerNode()
					.getComponentsList();
			Map<String, String[]> mapCompProp = MigrationUtil
					.getComponentJCRPropertiesMap(this.aemComponentPropertyMapping);
			if (MapUtils.isNotEmpty(mapCompProp)) {

				for (int count = 0; count < components.size(); count++) {

					AEMComponent aemComp = components.get(count);
					sb.append(MigrationUtil.getComponentJCRProperties(aemComp, mapCompProp));
				}
			}
			sb.append("\n");
			sb.append(":: ##********************************************************Page count " + counter + " current page title " + aemPage.getJcrContent().getJcr_title());
			sb.append("\n");
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

}
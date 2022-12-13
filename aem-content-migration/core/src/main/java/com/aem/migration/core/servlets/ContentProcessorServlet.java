package com.aem.migration.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.migration.core.aem.dto.AEMPage;
import com.aem.migration.core.services.ContentProcessorService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The Class ContentProcessorServlet.
 */
@Component(service = { Servlet.class }, property = { "sling.servlet.methods=get",
		"sling.servlet.paths=/bin/migrate-content" })
public class ContentProcessorServlet extends SlingSafeMethodsServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The content processor. */
	@Reference
	ContentProcessorService contentProcessor;

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ContentProcessorServlet.class);

	/**
	 * Do get.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String damPath = request.getParameter("sourcePath");
		String templatePath = request.getParameter("templatePath");
		//String cmsVal = request.getParameter("cmsVal");
		String configPath = request.getParameter("configPath");
		String imagesPath = request.getParameter("imagesPath");
		//String pageName = request.getParameter("pageName");
		//List<AEMPage> aemPageList = new ArrayList<>();
		
		
		JsonArray jsonArray = contentProcessor.getWPPagesList(damPath,configPath, imagesPath);
		//String curlScript = contentProcessor.getAEMPageCreateScript(jsonObject);
		if(StringUtils.equals(request.getParameter("createPages"), "true")) {

			response.setContentType("text/html;charset=UTF-8");
			String pageURL = null;
			String aemRootNodePath = request.getParameter("destPath");
			int counterComp=0;
			Iterator<JsonElement> outputSchemaIterator = jsonArray.iterator();
			 while(outputSchemaIterator.hasNext()) {
				  JsonObject field = outputSchemaIterator.next().getAsJsonObject();
				  
				pageURL = contentProcessor.createAEMPage(field, counterComp, aemRootNodePath, templatePath);
				counterComp++;
				//out.write(". Migrating page(source)"+ "<br>");
				out.write("Page created successfully in AEM at path "
						+ aemRootNodePath+".html" + "<br>");
				out.write("with component " +pageURL + "<br>");
			 }
			
		} else if (StringUtils.equalsIgnoreCase(request.getParameter("showAEMPageJSON"), "true")) {

			response.setContentType("application/json;charset=UTF-8");
		} else {
			out.write(jsonArray.getAsString());
		}
	}
}

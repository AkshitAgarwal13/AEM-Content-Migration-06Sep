package com.aem.migration.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import com.aem.migration.core.wordpress.dto.WordPressPage;

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

		PrintWriter out = response.getWriter();
		List<WordPressPage> wordPressPageList = contentProcessor.getWPPagesList();
		List<AEMPage> aemPageList = new ArrayList<>();
		int counter = 1;
		for(WordPressPage wpPage : wordPressPageList) {
		
			log.info("Count  {} and page name {} ", counter, wpPage.getTitle());
			aemPageList.add(new AEMPage(wpPage));
			counter++;
		}
		String aemPageJSON = contentProcessor.getAEMPageJSON(aemPageList);
		String curlScript = contentProcessor.getAEMPageCreateScript(aemPageList);
		if(StringUtils.equals(request.getParameter("createPages"), "true")) {

			for(int count = 0; count < aemPageList.size(); count++ ) {

				out.write((count + 1) + ". Migrating page(source) " + aemPageList.get(count).getTempPagePath() + "\n\n");
				out.print("Page created successfully in AEM at path  " + contentProcessor.createAEMPage(aemPageList.get(count)) + "\n\n");
			}
		} else if (StringUtils.equalsIgnoreCase(request.getParameter("showAEMPageJSON"), "true")) {

			response.setContentType("application/json");
			response.getWriter().write(aemPageJSON);
		} else {
			out.write(curlScript);
		}
	}
}

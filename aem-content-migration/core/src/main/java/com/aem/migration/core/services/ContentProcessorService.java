package com.aem.migration.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.aem.migration.core.aem.dto.AEMPage;
import com.aem.migration.core.wordpress.dto.WordPressPage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The Interface ContentProcessorService.
 */
public interface ContentProcessorService {
	
	/**
	 * Gets the source content extract file path.
	 *
	 * @return the source content extract file path
	 */
	public String getSourceContentExtractFilePath();

	
	/**
	 * Gets the WP page object.
	 *
	 * @return the WP page object
	 * @throws IOException 
	 */
	public JsonArray getWPPagesList(String damPath,String configPath, String imagesPath) throws IOException;
	
	/**
	 * Extract WP page components.
	 *
	 * @param wpPage the wp page
	 * @return the word press page
	 */
	
	public String getAEMPageCreateScript(JsonObject aemPage);


	/**
	 * Gets the AEM page JSON.
	 *
	 * @param aemPage the aem page
	 * @return the AEM page JSON
	 */
	public String getAEMPageJSON(List<AEMPage> aemPage);


	/**
	 * Creates the AEM page.
	 *
	 * @param aemPage the aem page
	 * @return the string
	 */
	public String createAEMPage(JsonObject jOb, int counterComp, String destPath, String templatePath);




}

package com.aem.migration.core.services;

import java.io.BufferedReader;
import java.util.List;

import com.aem.migration.core.aem.dto.AEMPage;
import com.aem.migration.core.wordpress.dto.WordPressPage;

// TODO: Auto-generated Javadoc
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
	 * Gets the source content extract.
	 *
	 * @return the source content extract
	 */
	public BufferedReader getSourceContentExtract();
	
	/**
	 * Gets the WP page object.
	 *
	 * @return the WP page object
	 */
	public List<WordPressPage> getWPPagesList();
	
	/**
	 * Extract WP page components.
	 *
	 * @param wpPage the wp page
	 * @return the word press page
	 */
	public WordPressPage extractWPPageComponents(WordPressPage wpPage);
	
	/**
	 * Gets the AEM page create script.
	 *
	 * @param aemPage the aem page
	 * @return the AEM page create script
	 */
	public String getAEMPageCreateScript(List<AEMPage> aemPage);


	/**
	 * Gets the AEM page JSON.
	 *
	 * @param aemPage the aem page
	 * @return the AEM page JSON
	 */
	public String getAEMPageJSON(List<AEMPage> aemPage);

}

package com.aem.migration.core.aem.dto.components;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.aem.migration.core.constants.MigrationConstants;
import com.aem.migration.core.wordpress.dto.WPComponent;

/**
 * The Class Image.
 */
public class AEMComponent {

	/** The jcr primary type. */
	private String jcr_primaryType;
	
	/** The node name. */
	private String nodeName;

	/** The jcr title. */
	private String jcr_title;

	/** The file reference. */
	private String fileReference;

	/** The id. */
	private String id;
	
	/** The component type. */
	private String componentType;

	/** The alt. */
	private String alt;

	/** The link URL. */
	private String linkURL;

	/** The display popup title. */
	private String displayPopupTitle;

	/** The title value from DAM. */
	private String titleValueFromDAM;

	/** The sling resource type. */
	private String sling_resourceType;

	/** The is decorative. */
	private String isDecorative;

	/** The alt value from DAM. */
	private String altValueFromDAM;

	/** The height. */
	private String height;

	/** The width. */
	private String width;
	
	/** The table */
	private String tableData;
	
	/** The rich text */
	private String textIsRich;
	
	/** The text */
	private String text;
	
	/** The Constant FALSE. */
	private static final String FALSE = "FALSE";

	/**
	 * Gets the jcr primary type.
	 *
	 * @return the jcr primary type
	 */
	public String getJcr_primaryType() {
		return jcr_primaryType;
	}

	/**
	 * Gets the jcr title.
	 *
	 * @return the jcr title
	 */
	public String getJcr_title() {
		return jcr_title;
	}

	/**
	 * Gets the file reference.
	 *
	 * @return the file reference
	 */
	public String getFileReference() {
		return fileReference;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the alt.
	 *
	 * @return the alt
	 */
	public String getAlt() {
		return alt;
	}

	/**
	 * Gets the link URL.
	 *
	 * @return the link URL
	 */
	public String getLinkURL() {
		return linkURL;
	}

	/**
	 * Gets the display popup title.
	 *
	 * @return the display popup title
	 */
	public String getDisplayPopupTitle() {
		return displayPopupTitle;
	}

	/**
	 * Gets the title value from DAM.
	 *
	 * @return the title value from DAM
	 */
	public String getTitleValueFromDAM() {
		return titleValueFromDAM;
	}

	/**
	 * Gets the sling resource type.
	 *
	 * @return the sling resource type
	 */
	public String getSling_resourceType() {
		return sling_resourceType;
	}

	/**
	 * Gets the checks if is decorative.
	 *
	 * @return the checks if is decorative
	 */
	public String getIsDecorative() {
		return isDecorative;
	}

	/**
	 * Gets the alt value from DAM.
	 *
	 * @return the alt value from DAM
	 */
	public String getAltValueFromDAM() {
		return altValueFromDAM;
	}

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * Gets the node name.
	 *
	 * @return the node name
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * Gets the component type.
	 *
	 * @return the component type
	 */
	public String getComponentType() {
		return componentType;
	}
	

	public String getTableData() {
		return tableData;
	}

	public String getTextIsRich() {
		return textIsRich;
	}
	
	public String getText() {
		return text;
	}

	/**
	 * Instantiates a new image.
	 *
	 * @param wpImage the wp image
	 */
	public AEMComponent(WPComponent wpComponent, int componentCounter) {

		this.jcr_primaryType = MigrationConstants.NT_UNSTRUCTURED;
		this.componentType = wpComponent.getComponentType();
		this.jcr_title = wpComponent.getImgCaption();
		this.id = null;
		this.nodeName = wpComponent.getComponentType() + "_" + componentCounter;
		if(StringUtils.equalsIgnoreCase(wpComponent.getComponentType(), "image")) {
			this.displayPopupTitle = FALSE;
			this.titleValueFromDAM = FALSE;
			this.isDecorative = FALSE;
			this.altValueFromDAM = FALSE;
			this.sling_resourceType = MigrationConstants.IMAGE_SLING_RESOURCE_TYPE;
			this.linkURL = wpComponent.getSrc();
			this.fileReference = StringUtils.isNotBlank(wpComponent.getImgSrc()) ? wpComponent.getImgSrc()
					.replace(wpComponent.getSourceCMSDAMRootPath(), wpComponent.getAemDAMRootPath()) : null;
		}
		if(StringUtils.equalsIgnoreCase(wpComponent.getComponentType(), "table")) {
			this.tableData = StringEscapeUtils.unescapeHtml(wpComponent.getTableData());
			this.sling_resourceType = MigrationConstants.TABLE_SLING_RESOURCE_TYPE;
			this.textIsRich = "true";
		}		
		if(StringUtils.equalsIgnoreCase(wpComponent.getComponentType(), "text")) {

			this.text = wpComponent.getText();
		}
	}

}

package com.aem.migration.core.models;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import lombok.Getter;

@Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MultifieldTabs {
	
	@Inject
	@Getter
	public String id;

	@Inject
	@Getter
	public String label;

	@Inject
	@Getter
	public String header;

	@Inject
	@Getter
	public String description;

	@Inject
	@Getter
	public String link;
	
//	@Inject
//	@Getter
//	public String text;
	
	@Inject
	@Getter
	public String linkText;
	
	@Inject
	@Getter
	public String fileReference;
	
	@Inject
	@Getter
	public String altText;
}

package com.aem.migration.core.models;

import java.util.List;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

import lombok.Getter;

@Model(
		adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class ConsultingTabsModel {

	@Inject
	@Getter
	public String heading;

	@ChildResource
	@Getter
	public List<MultifieldTabs> consultingTabs;
	
}

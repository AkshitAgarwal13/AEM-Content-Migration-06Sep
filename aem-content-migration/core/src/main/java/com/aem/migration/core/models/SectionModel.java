package com.aem.migration.core.models;

import lombok.Getter; 
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class,defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SectionModel {

    @ValueMapValue @Getter
    private String sectionLabel;

    @ValueMapValue @Getter
    private String sectionButton;

    @ValueMapValue @Getter
    private String navigationUrl;

}


package com.aem.migration.core.models;

import lombok.Getter;  
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class,defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TextPromo {

    @ValueMapValue @Getter
    private String promoLabel;

    @ValueMapValue @Getter
    private String promoButton;

    @ValueMapValue @Getter
    private String navigationUrl;

}


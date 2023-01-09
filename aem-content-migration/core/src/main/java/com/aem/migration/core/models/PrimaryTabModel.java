package com.aem.migration.core.models;
 
import javax.annotation.PostConstruct;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.apache.sling.api.resource.Resource;
 
import org.apache.sling.api.SlingHttpServletRequest;
 
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
 
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PrimaryTabModel {
 
    private static final Logger log = LoggerFactory.getLogger(PrimaryTabModel.class);
 
    @SlingObject
    private Resource componentResource; 
 
    private List<Map<String, String>> details = new ArrayList<>();
 
    public List<Map<String, String>> getMultiDataMap() {
 
        return details;
 
    }
 
    @PostConstruct
    protected void init() {
 
        Resource supportPoints = componentResource.getChild("details");  
            for (Resource supportPoint : supportPoints.getChildren()) {
 
                Map<String, String> detailsMap = new HashMap<>();
                detailsMap.put("slNumber",
                        supportPoint.getValueMap().get("slNumber", String.class));  
                detailsMap.put("headline",
                        supportPoint.getValueMap().get("headline", String.class));
                detailsMap.put("uniqueNumber",
                        supportPoint.getValueMap().get("uniqueNumber", String.class));
                details.add(detailsMap);
            }
        }
 
    }
 

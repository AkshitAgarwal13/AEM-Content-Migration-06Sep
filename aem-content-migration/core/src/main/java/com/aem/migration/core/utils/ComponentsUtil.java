package com.aem.migration.core.utils;

import com.day.cq.dam.api.AssetManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class ComponentsUtil {

    private static final Logger log = LoggerFactory.getLogger(ComponentsUtil.class);

    public static JsonArray createImageComponent(Elements nodeName, Row row, String damPath, String imagesPath,ResourceResolverFactory resolverFactory){
        String resType = row.getCell(5).getStringCellValue();
        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
            map.put(t[0], t[1]);
        }
        JsonArray jArr = new JsonArray();
        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            for (String s : map.keySet()) {
                String source = elc.attr(s);
                if(s.equalsIgnoreCase("src")){
                    source = getDamImagePath(source,damPath, imagesPath,resolverFactory);
                }
                jObj.addProperty(map.get(s), source);
            }
            jArr.add(jObj);
        }
        return  jArr;
    }



    public static JsonArray createTextComponent(Elements nodeName, String resType,String cellProp, String cellPropFixed){
        JsonArray jArr = new JsonArray();
        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            jObj.addProperty(cellProp,elc.toString());
            jObj.addProperty("textIsRich","true");
            jArr.add(jObj);
        }
        return  jArr;
    }
    
    public static JsonArray createConsultingTabsComponent(Elements nodeName, Row row, String damPath, String imagesPath, ResourceResolverFactory resolverFactory) throws MalformedURLException{
    	
		String cellProp = row.getCell(7).getStringCellValue();
		String cellPropFixed = row.getCell(9).getStringCellValue();
		String resType = row.getCell(5).getStringCellValue();
        String childComp = row.getCell(12).getStringCellValue();
		
        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
                map.put(t[0],t[1]);
        }

        JsonArray jArr = new JsonArray();
        
        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            
            Map<String,String> properties = new HashMap<>();
            Map<String,String> multifieldProperties = new HashMap<>();
            int len = 0;
            for (Entry<String, String> s : map.entrySet()) {
            	Boolean multifieldPropertyCheck = s.getValue().toString().contains("/./");
            	if(multifieldPropertyCheck) {
            		multifieldProperties.put(s.getKey(), s.getValue());
            		len = len < elc.getElementsByClass(s.getKey().toString()).size() ? elc.getElementsByClass(s.getKey().toString()).size() : len;
            	} else {
            		properties.put(s.getKey(), s.getValue());
            	}
            }
            
            for (String s : properties.keySet()) {
                jObj.addProperty(properties.get(s), elc.getElementsByClass(s).text());
            }
 
			for(int i = 0 ; i < len ; i++) {
                for (Entry<String, String> s : multifieldProperties.entrySet()) {
               	    if(elc.getElementsByClass(s.getKey()).size() > i) {
               			URL url = new URL(damPath);
               			String host  = "https://"+url.getHost();
                   	    Element ee = elc.getElementsByClass(s.getKey()).get(i);
                       	Boolean check = elc.getElementsByClass(s.getKey()).get(i).select("img").hasAttr("src");
                    	if(check) {
                    		String src = ee.select("img[src]").attr("src");                       		
                    		src = getDamImagePath(src,damPath, imagesPath, resolverFactory);
                    		jObj.addProperty("."+ childComp + i + s.getValue(), src);  
                    	} else if(ee.select("a").hasAttr("href") && s.getValue().contains("#")) {
                    		String hrefValue = ee.attr("href").contains(host) ? ee.attr("href") : host + ee.attr("href");
                    		jObj.addProperty("."+ childComp + i + s.getValue().split("#")[1], ee.attr("href").contains("#") ? ee.attr("href").split("#")[1] : hrefValue);
                    		jObj.addProperty("."+ childComp + i + s.getValue().split("#")[0], ee.text());
                    	} else {
                    		jObj.addProperty("."+ childComp + i + s.getValue(), ee.text());
                    	}
               	    }
                }
            }
			jArr.add(jObj);
        }
        return  jArr;
    }
    
    public static JsonArray createButtonComponent(Elements nodeName, Row row){
        String resType = row.getCell(5).getStringCellValue();
        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        String textKey="";
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
            textKey=t[0];
            map.put(t[0],"");
        }
        JsonArray jArr = new JsonArray();
        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            for (String s : map.keySet()) {
                String source = elc.attr(s);
                if(s.equalsIgnoreCase(textKey)){
                    jObj.addProperty(textKey, elc.text());
                }
                jObj.addProperty(map.get(s), source);
            }
            jArr.add(jObj);
        }
        return  jArr;
    }
    public static JsonArray createTitleComponent(Elements nodeName, Row row){
        String resType = row.getCell(5).getStringCellValue();
        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        String type = convertedPropArray[0];
        String textKey=convertedPropArray[1];

        JsonArray jArr = new JsonArray();
        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            jObj.addProperty(textKey, elc.text());
            jObj.addProperty(type, elc.tagName());
            jArr.add(jObj);
        }
        return  jArr;
    }
    public static JsonArray createBannerComponent(Elements nodeName, Row row, String damPath, String imagesPath, ResourceResolverFactory resolverFactory){
        String resType = row.getCell(5).getStringCellValue();
        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
            map.put(t[0], t[1]);
        }
        JsonArray jArr = new JsonArray();
        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            for (String s : map.keySet()) {
                if(!elc.getElementsByTag(s).text().isEmpty()) {
                    String source = elc.getElementsByTag(s).text();
                    jObj.addProperty(map.get(s), source);
                }else if(!elc.getElementsByAttribute(s).attr(s).isEmpty()){
                    String source = elc.getElementsByAttribute(s).attr(s);
                    if(map.get(s).equals("backgroundImage")) {
                          jObj.addProperty(map.get(s), getDamImagePath(source,damPath, imagesPath, resolverFactory ));
                    }else{
                        jObj.addProperty(map.get(s), source);
                    }
                }
            }
            jArr.add(jObj);
        }
        return  jArr;
    }

    public  static JsonArray createTextImageComponent(Elements nodeName, Row row, ResourceResolverFactory resolverFactory){
        String resType = row.getCell(5).getStringCellValue();
        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
            map.put(t[0], t[1]);
        }
        JsonArray jArr = new JsonArray();
       Element elc = nodeName.first();
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            for (String s : map.keySet()) {
                if(!elc.getElementsByTag(s).text().isEmpty()) {
                    String source = elc.getElementsByTag(s).text();
                    jObj.addProperty(map.get(s), source);
                }else if(!elc.getElementsByAttribute(s).attr(s).isEmpty()){
                    if(s.equalsIgnoreCase("src")){
                        jObj.addProperty(map.get(s), getDamImagePath(elc.getElementsByAttribute(s).attr(s),"","/content/dam/ibm/new/",resolverFactory));
                    }
                    else{
                        String source = elc.getElementsByAttribute(s).attr(s);
                        jObj.addProperty(map.get(s), source);
                    }
                }
            }
            jArr.add(jObj);

        return jArr;

    }
    public static JsonArray createSectionComponent(Elements nodeName, Row row,ResourceResolverFactory resolverFactory) {
        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String resType = row.getCell(5).getStringCellValue();

        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
            map.put(t[0], t[1]);
        }

        JsonArray jArr = new JsonArray();

        for (Element elc : nodeName) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("sling:resourceType", resType);
            jObj.addProperty("componentContainer", cellPropFixed);
            for (String s : map.keySet()) {
                if (!elc.getElementsByTag(s).text().isEmpty()) {
                    String source = elc.getElementsByTag(s).text();
                    jObj.addProperty(map.get(s), source);
                } else if (!elc.getElementsByAttribute(s).attr(s).isEmpty()) {
                    String source = elc.getElementsByAttribute(s).attr(s);
                    jObj.addProperty(map.get(s), source);
                }

            }
            jArr.add(jObj);
        }
        return jArr;
    }
    public static JsonArray createPrimaryTabComponent(Elements nodeName, Row row, ResourceResolverFactory resolverFactory) {

        String cellProp = row.getCell(7).getStringCellValue();
        String cellPropFixed = row.getCell(9).getStringCellValue();
        String resType = row.getCell(5).getStringCellValue();
        String childComp = row.getCell(12).getStringCellValue();

        String[] convertedPropArray = cellProp.split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : convertedPropArray) {
            String[] t = s.split("=");
            map.put(t[0], t[1]);
        }

        JsonArray jArr = new JsonArray();
        JsonObject jObj = new JsonObject();
        jObj.addProperty("sling:resourceType", resType);
        jObj.addProperty("componentContainer", cellPropFixed);

        Map<String, String> multifieldProperties = new HashMap<>();
        Element ultag = nodeName.select("ul").first();
        Elements ulChildren = ultag.children();
        for (Entry<String, String> s : map.entrySet()) {
            Boolean check = s.getValue().toString().contains("/./");
            if (check) {
                multifieldProperties.put(s.getKey(), s.getValue());
            }
        }

        for (int i = 0; i < ulChildren.size(); i++) {
            for (Entry<String, String> s : multifieldProperties.entrySet()) {
                Element ul = nodeName.select("ul").first();
                Element li = ul.select("li").get(i);
                String aTag = li.select("a").text();
                String content = aTag.replaceAll("\\d", "");
                String slNo = li.select("a").select("span").text();
                String key = s.getKey();
                if(key.equals("tagHeading")) {
                    jObj.addProperty("." + childComp + i + s.getValue(), content);
                }
                else if(key.equals("tagSlNo")) {
                    jObj.addProperty("." + childComp + i + s.getValue(), slNo);
                }
            }
        }
        jArr.add(jObj);
        return jArr;
    }
    public static String getDamImagePath(String imageUrl, String damPath, String imagesPath,ResourceResolverFactory resolverFactory){
        String newFile="";
        try {
            String[] splitImageUrl = imageUrl.split("/");
            int urlLength = splitImageUrl.length;
            String imageName = splitImageUrl[urlLength - 1];
            if (imageName.contains("?")) {
                imageName = imageName.substring(0, imageName.indexOf("?"));
            }
            if(imageUrl.startsWith("//")){
                imageUrl="https:"+imageUrl;
            } else if(imageUrl.startsWith("/")){
                imageUrl=damPath+imageUrl;
            }else if(imageUrl.startsWith("https")){
                imageUrl=imageUrl;
            }
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            Map<String, Object> param = new HashMap<>();
            param.put(ResourceResolverFactory.SUBSERVICE, "migrationService");
            ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param);
            AssetManager assetMgr = resolver.adaptTo(AssetManager.class);
            newFile = imagesPath + imageName.replaceAll("[^a-zA-Z0-9.]","-");
            Resource res = resolver.getResource(newFile);
            if (res == null) {
                if (imageName.endsWith("svg")) {
                    assetMgr.createAsset(newFile, is, "image/svg+xml", true);
                } else {
                    assetMgr.createAsset(newFile, is, "image/jpeg", true);
                }
            }
        }
        catch(LoginException e){
            log.error("LoginException ",e);
        }catch(IOException e){
            log.error("IOException ",e);
        }
        return newFile;
    }
}

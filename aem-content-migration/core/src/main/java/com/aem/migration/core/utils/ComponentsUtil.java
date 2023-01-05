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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
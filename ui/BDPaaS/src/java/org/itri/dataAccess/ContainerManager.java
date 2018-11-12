/*
    Author     : MuKai Huang
    Copyright (c) 2018 ITRI
 */
package org.itri.dataAccess;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.itri.bdServlet.SimulateCreatePlatform;
import org.itri.data.Key;
import org.itri.utils.DebugLog;

/**
 *
 * @author Chris
 */
public class ContainerManager {
    public ContainerManager(){
        
    }
    
    public void createPlatform(final String servletURL, final JSONObject requestJSON){
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    DebugLog.info("======createPlatform=======");
                    DebugLog.info(servletURL);
                    JSONObject projectJSON = requestJSON.getJSONObject(Key.PROJECT);
                    String projectName = projectJSON.getString(Key.NAME);
                    DebugLog.info(requestJSON.toString());
                    DebugLog.info(projectName);
                    projectName = projectName.replaceAll("##", "-");
                    DebugLog.info(projectName);
                    projectJSON.put(Key.NAME, projectName);
                    DebugLog.info(requestJSON.toString());
                    HttpPost method = new HttpPost(servletURL);
                    method.setHeader("Accept", "application/json");
                    method.setHeader("Content-type", "application/json");
                    HttpEntity inputEntity = new ByteArrayEntity(requestJSON.toString().getBytes("UTF-8"));
                    method.setEntity(inputEntity);
                    /*MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder.create();        
                    reqEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    Iterator<String> keys = requestJSON.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String val = null;
                        try{
                             JSONObject value = requestJSON.getJSONObject(key);
                             val = value.toString();
                        }catch(Exception e){
                            try {
                                val = requestJSON.getString(key);
                            } catch (JSONException ex) {
                                Logger.getLogger(ContainerManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        reqEntityBuilder.addTextBody(key, val,  ContentType.DEFAULT_BINARY);    
                    }  */  

                    //method.setEntity(reqEntityBuilder.build());
                    HttpClient client = HttpClients.createDefault();
                    HttpResponse response = client.execute(method);
                    HttpEntity entity = response.getEntity();

                } catch (IOException ex) {
                    Logger.getLogger(SimulateCreatePlatform.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(ContainerManager.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        });
        t.start();
    }
    
    public void deletePlatform(final String servletURL, final JSONObject requestJSON){
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    DebugLog.info("======deletePlatform=======");
                    DebugLog.info(servletURL);
                    HttpPost method = new HttpPost(servletURL);
                    method.setHeader("Accept", "application/json");
                    method.setHeader("Content-type", "application/json");
                    JSONObject projectJSON = requestJSON.getJSONObject(Key.PROJECT);
                    String projectName = projectJSON.getString(Key.NAME);
                    projectName = projectName.replaceAll("##", "-");
                    projectJSON.put(Key.NAME, projectName);
                    DebugLog.info(requestJSON.toString());
                    HttpEntity inputEntity = new ByteArrayEntity(requestJSON.toString().getBytes("UTF-8"));
                    method.setEntity(inputEntity);
                    /*MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder.create();        
                    reqEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    Iterator<String> keys = requestJSON.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String val = null;
                        try{
                             JSONObject value = requestJSON.getJSONObject(key);
                             val = value.toString();
                        }catch(Exception e){
                            try {
                                val = requestJSON.getString(key);
                            } catch (JSONException ex) {
                                Logger.getLogger(ContainerManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        reqEntityBuilder.addTextBody(key, val,  ContentType.DEFAULT_BINARY);    
                    }  */  

                    //method.setEntity(reqEntityBuilder.build());
                    HttpClient client = HttpClients.createDefault();
                    HttpResponse response = client.execute(method);
                    HttpEntity entity = response.getEntity();

                } catch (IOException ex) {
                    Logger.getLogger(SimulateCreatePlatform.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(ContainerManager.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        });
        t.start();
    }
}

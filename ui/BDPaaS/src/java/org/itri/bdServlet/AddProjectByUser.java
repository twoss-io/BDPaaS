/*
    Author     : MuKai Huang
    Copyright (c) 2018 ITRI
 */
package org.itri.bdServlet;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.itri.data.Key;
import org.itri.data.ServletConfig;
import org.itri.data.entity.Platform;
import org.itri.data.entity.Status;
import org.itri.data.entity.User;
import org.itri.dataAccess.ContainerManager;
import org.itri.dataAccess.PlatformDBManager;
import org.itri.dataAccess.UserDBManager;
import org.itri.utils.DebugLog;


/**
 *
 * @author A40385
 */
@WebServlet(urlPatterns = {"/addProjectByUser"})
public class AddProjectByUser extends HttpServlet {
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, JSONObject inputJSON) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String userID = (String)request.getSession().getAttribute("userID");
        ServletOutputStream out = response.getOutputStream();
        JSONObject jsonResult = new JSONObject(); 
        DebugLog.info(inputJSON.toString());
        try {
            if(userID == null){
                jsonResult.put(Key.ERROR_CODE, Status.NOT_LOGIN);
            }
            else{
                PlatformDBManager platformDBManager = new PlatformDBManager();
                 ContainerManager containerManager = new ContainerManager();
                ArrayList<Platform> platformListOfCurrentUser = platformDBManager.getPlatformOfUser(userID);
                HashMap<String, Integer> projectNameList = new HashMap<String, Integer>();
                String xmlCreatePlatformURL = getServletContext().getInitParameter(Key.CREATE_PLATFORM_URL);
                String createPlatformURL = System.getProperty(Key.CREATE_PLATFORM_URL, xmlCreatePlatformURL);
                for(Platform currentPlatform : platformListOfCurrentUser){
                    if(currentPlatform.getType().matches(Key.SPARK) || currentPlatform.getType().matches(Key.APEX)){
                        DebugLog.info("currentPlatform.getProjectName(): " + currentPlatform.getProjectName());
                        String[] projectNameArray =  currentPlatform.getProjectName().split("##");
                        String basicProjectName = projectNameArray[0] + "##" + projectNameArray[1] + "##" + projectNameArray[2];
                        Integer currentIndex = Integer.valueOf(projectNameArray[3]);
                        if(projectNameList.get(basicProjectName)==null){
                            projectNameList.put(basicProjectName, currentIndex);
                        }
                        else{
                            int savedIndex = projectNameList.get(basicProjectName);
                            if(savedIndex < currentIndex) projectNameList.put(basicProjectName, currentIndex); 
                        }
                    }
                }
                String projectType = inputJSON.getString(Key.TYPE);
                String projectName = inputJSON.getString(Key.PROJECT_NAME);
                if(projectNameList.get(projectName)==null){
                    projectName = projectName + "##1";
                    projectNameList.put(projectName, 1);
                }
                else{
                    int currentIndex = projectNameList.get(projectName) + 1;
                    projectName = projectName + "##" + currentIndex;
                    projectNameList.put(projectName, currentIndex);
                }
                String finalProjectName = "bdpaas##" + userID + "##" + projectName;
                JSONObject createdProjectObject = new JSONObject();
                JSONObject userObject = new JSONObject();
                userObject.put(Key.NAME, userID);
                JSONArray resourceArrayObject = new JSONArray();
                resourceArrayObject.put(1);
                resourceArrayObject.put("100MB");
                userObject.put(Key.RESOURCE, resourceArrayObject);
                createdProjectObject.put(Key.USER, userObject);
                JSONObject projectObject = new JSONObject();
                projectObject.put(Key.NAME, finalProjectName);
                if(projectType.matches(Key.APEX)){
                    projectObject.put(Key.MODULE, Key.APEX);
                    platformDBManager.addTempPlatform(userID, finalProjectName, Key.APEX, "");
                }
                else if(projectType.matches(Key.SPARK)){
                    projectObject.put(Key.MODULE, Key.SPARK);
                    platformDBManager.addTempPlatform(userID, finalProjectName, Key.SPARK, "");
                }
                
                JSONArray commandArrayObject = new JSONArray();
                commandArrayObject.put(Key.CREATE);
                projectObject.put(Key.COMMAND, commandArrayObject);
                createdProjectObject.put(Key.PROJECT, projectObject);
                
               containerManager.createPlatform(createPlatformURL, createdProjectObject);     
               jsonResult.put(Key.ERROR_CODE, Status.SUCCESS);
            }

        } catch (JSONException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(AddProjectByUser.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally {     
            out.print(jsonResult.toString());
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuffer jsonBuffer = new StringBuffer();
        String line = null;
        try {
          BufferedReader reader = request.getReader();
          while ((line = reader.readLine()) != null)
            jsonBuffer.append(line);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        try {
          JSONObject jsonObject =  new JSONObject(jsonBuffer.toString());
          processRequest(request, response, jsonObject);
        } catch (JSONException e) {
          // crash and burn
          throw new IOException(e.getMessage());
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuffer jsonBuffer = new StringBuffer();
        String line = null;
        try {
          BufferedReader reader = request.getReader();
          while ((line = reader.readLine()) != null){
              jsonBuffer.append(line);
          }
            
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }

        try {
          JSONObject jsonObject =  new JSONObject(jsonBuffer.toString());
          processRequest(request, response, jsonObject);
        } catch (JSONException e) {
          // crash and burn
          throw new IOException(e.getMessage());
        }
        
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

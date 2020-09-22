/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cmcrdr.webinterface;

import java.io.File;
import java.io.FilenameFilter;
import cmcrdr.gui.AdminJavaGUI;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;  
import org.apache.commons.lang3.exception.ExceptionUtils;
import rdr.domain.Domain;
import cmcrdr.gui.UserInterface;
import static cmcrdr.main.DialogMain.defaultResponse;
import cmcrdr.mysql.DBOperation;
import static cmcrdr.mysql.DBCreation.referenceDatabaseSlotFieldsCreate;
import static cmcrdr.mysql.DBOperation.getDomainDescription;
import java.sql.ResultSet;

@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {
    private static DialogMain dialogMain = null;
    

    
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        switch (request.getParameter("mode").trim()) {
            case "getDomainList":
            {
                try {
                    String theStatus = "";
                    HashMap<String,String> rs = DBOperation.getDomainDetails();
                    for (Map.Entry<String, String> entry : rs.entrySet()) {
                        String key = entry.getKey();
                        String value = (String) entry.getValue();
                        if (key == "domainName" ) {
                            theStatus = theStatus + value + ";";
                        }
                    }
                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    //response.setContentType("text/plain; charset=UTF-8");
                    //response.getWriter().write(ex.getMessage());
                }
                break;
            }
            case "getLoadedDomain":
            {
                try {
                    String theStatus = "";
                    //Logger.info("starting getLoadedDomain");



                    if (DialogMain.getIsDomainInitialised()) {
                        theStatus = DialogMain.getDomainName();
                    }

                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    //Logger.info(theStatus);
                    //Logger.info("Finished getLoadedDomain...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
                break;
            }
            case "getActiveDomainDescription":
            {
                try {
                    String theStatus = "";
                    //Logger.info("starting getActiveDomainDescription");

                    if (DialogMain.getIsDomainInitialised())
                        theStatus = DialogMain.getDomainDescription();
                    else
                        theStatus = "No domain loaded...";


                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    //Logger.info(theStatus);
                    //Logger.info("Finished getActiveDomainDescription...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
                break;
            }
            case "getNonActiveDomainDescription":
            {
                try {
                    String theStatus = "";
                    String theSelectedDomain = request.getParameter("domain").trim();

                    //Logger.info("starting getNonActiveDomainDescription");
                    //Logger.info("selected domain is:" + theSelectedDomain);
                    if (!theSelectedDomain.isEmpty())
                        theStatus = getDomainDescription(theSelectedDomain,request.getSession().getServletContext().getRealPath("/WEB-INF"));

                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    //Logger.info(theStatus);
                    //Logger.info("Finished getNonActiveDomainDescription...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
                break;
            }
            case "getReferenceDatabaseFields":
            {
                try {
                    String theStatus = "";
                    String host = request.getParameter("host").trim();
                    String database = request.getParameter("database").trim();
                    String username = request.getParameter("username").trim();
                    String password = request.getParameter("password").trim();
                    ArrayList<String> schemaColumns = new ArrayList<>();
                    
                    if (!host.isEmpty() && !database.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                        Logger.info("Setting new reference details...");
                        HashMap<String,String> settings = new HashMap<>();
                        settings.put("databaseHost", host);
                        settings.put("databaseDatabase", database);
                        settings.put("databaseUsername", username);
                        settings.put("databasePassword", password);
                        settings.put("databaseSelectedFields","");
                        
                        //HashMap<String,String> oldSettings = null;
                        //if (DialogMain.getReferenceDatabaseDetails() != null) {
                            //oldSettings = DialogMain.getReferenceDatabaseDetails();
                       // }
                        
                        // temp use new settings but don't save settings in master db..
                        //Logger.info("Setting temporary reference database details..");
                        if (DialogMain.setReferenceDatabaseDetails(settings,false)) {
 
                            //Logger.info("Connection established to reference database..");

                            // get the names of all the columns in our reference database
                            ArrayList<String> tableNames = DBOperation.getTableNames();
                            for (String aTable: tableNames) {
                                //Logger.info("Found a table:" + aTable);
                                schemaColumns.addAll(DBOperation.getColumnNames(aTable));
                            }

                            for (String aColumn: schemaColumns) {
                                //Logger.info("Found a column:" + aColumn);
                                if (theStatus.isEmpty())
                                    theStatus = aColumn;
                                else
                                    theStatus += ";" + aColumn;                          
                            }
                        }
                        else {
                            theStatus = "no connection";
                        }
                                         
                    }

                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    //Logger.info(theStatus);
                    //Logger.info("Finished getReferenceDatabaseFields...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
                break;
            }
            
            case "initialise":
            {
                try {
                    String theStatus = "";
                    Logger.info("Loading knowledgebase...");
                    String domain = request.getParameter("domain").trim();
                    
                    String host = request.getParameter("host").trim();
                    String database = request.getParameter("database").trim();
                    String username = request.getParameter("username").trim();
                    String password = request.getParameter("password").trim();
                    
                    if (!host.isEmpty() && !database.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                        Logger.info("Setting new reference details...");
                        HashMap<String,String> settings = new HashMap<>();
                        settings.put("databaseHost", host);
                        settings.put("databaseDatabase", database);
                        settings.put("databaseUsername", username);
                        settings.put("databasePassword", password);
                        DialogMain.setReferenceDatabaseDetails(settings,true);
                    }

                    DialogMain.setDomainInitialised(DialogMain.initialiseForWeb(request.getSession().getServletContext().getRealPath("/WEB-INF"), domain, "headless"));
                    
                    
                    if (DialogMain.getIsDomainInitialised()) {
                        theStatus = "Knowledgebase successfully initialised";
                        DialogMain.setDomainName(domain);
                    }
                    else {
                        theStatus = "There was a problem initialising the knowledge base..";
                    }

                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write(theStatus);


                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("There was a problem initialising the knowledge base..");
                }
                break;
            }
            
            case "create":
            {
                try {
                    String theStatus = "";
                    Logger.info("create knowledgebase starting...");

                    String domain = request.getParameter("domain").trim();
                    String description = request.getParameter("description").trim();
                    String host = request.getParameter("host").trim();
                    String database = request.getParameter("database").trim();
                    String username = request.getParameter("username").trim();
                    String password = request.getParameter("password").trim();
                    String fieldNames = request.getParameter("fieldNames").trim();
                    
                    

                    DialogMain.initialiseSystem(request.getSession().getServletContext().getRealPath("/WEB-INF"),domain, Domain.MCRDR, description, defaultResponse);
                    DialogMain.createDomain();
                    if (!host.isEmpty() && !database.isEmpty() && !username.isEmpty() && !password.isEmpty() && !fieldNames.isEmpty()) {
                        Logger.info("Setting new reference details...");
                        HashMap<String,String> settings = new HashMap<>();
                        settings.put("databaseHost", host);
                        settings.put("databaseDatabase", database);
                        settings.put("databaseUsername", username);
                        settings.put("databasePassword", password);
                        settings.put("databaseSelectedFields",fieldNames);
                        DialogMain.setReferenceDatabaseDetails(settings,true);
                        
                        //Logger.info("Host:" + host);
                        //Logger.info("Database:" + database);
                        //Logger.info("Username:" + username);
                        //Logger.info("Password:" + password);
                        //Logger.info("Selected Fields:" + fieldNames);
                    }
                    
                    DialogMain.setDomainInitialised(DialogMain.initialiseForWeb(request.getSession().getServletContext().getRealPath("/WEB-INF"), domain, description));

                    if (DialogMain.getIsDomainInitialised()) {
                        
                        String[] fields = fieldNames.split(",");
                        ArrayList<String> slotFieldNames  =  new ArrayList<>(Arrays.asList(fields));
                        referenceDatabaseSlotFieldsCreate(slotFieldNames);
                        theStatus = "Knowledgebase successfully created and loaded";
                        DialogMain.setDomainName(domain);
                        
                    }
                    else 
                        theStatus = "There was a problem creating the knowledge base..";
                    
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write(theStatus);

                    Logger.info("Finished create knowledgebase...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    ex.printStackTrace();
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("There was a problem creating the knowledge base..");
                }
                break;
            }
            
            case "getReferenceDatabaseName":
            {
                try {
                    String theStatus = "";
                    //Logger.info("starting getReferenceDatabaseName");

                    if (DialogMain.referenceDatabaseDetails != null) {
                        theStatus = DialogMain.referenceDatabaseDetails.get("databaseDatabase");
                        if (theStatus == null)
                            theStatus = "";                                 
                    }
                    

                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write(theStatus);
                    //Logger.info(theStatus);
                    
                    //Logger.info("Finished getReferenceDatabaseName...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
                break;
            }
            
 
            
            case "showAdminGUI":
            {
                try {
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("Please load a knowledgebase first");
                    }
                    else {
                        boolean found = false;
                        for (UserInterface anInterface: DialogMain.userInterfaces)
                            if (anInterface.getInterfaceName().equals("AdminJavaGUI")) {
                                Logger.info("Found the admin user interface controller!");

                                ((javax.swing.JFrame) anInterface).setVisible(true);
                                found = true;
                                break;
                            }
                        if (!found) {
                            Logger.info("Admin user interface controller not found, creating!");
                            UserInterface adminJavaGUI = new AdminJavaGUI("AdminJavaGUI");
                            DialogMain.addUserInterface(adminJavaGUI);
                            ((javax.swing.JFrame)adminJavaGUI).setVisible(true);
                            // We need to add any users who have been present before the interface was added..
                            if (DialogMain.userInterfaceController == null) {
                                Logger.info("USER INTERFACE CONTROLLER IS NULL!");
                            }
                            else {
                                ArrayList<String> userNames = DialogMain.userInterfaceController.getListOfUsers();
                                for (String aUsername: userNames) {
                                    adminJavaGUI.addUser(aUsername);
                                }
                            }
                        }
                              
                    }
                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    Logger.error( ExceptionUtils.getStackTrace(ex));
                    //response.setContentType("text/plain; charset=UTF-8");
                    //response.getWriter().write(ex.getMessage());
                }
                break;
            }            
        }        
    }       
}

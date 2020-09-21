
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.mysql;

import java.sql.*;
import cmcrdr.logger.Logger;
import java.util.Enumeration;
import java.util.HashMap;


/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class DBConnection {
    private static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static String dbURL = "";
    private static String user = "";
    private static String password = "";
    private static Connection con = null;
    private static HashMap<String,String> referenceTableDetails = null;
    private static boolean database_used = false;

    /**
     *
     * @param settings
     * @param reconnect
     * @return
     */
    public static boolean connect(HashMap<String,String> settings, boolean reconnect ) {
        try {
            Driver driver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException s) {
            Logger.info(s.getMessage());
        }
        referenceTableDetails = new HashMap<>();
        dbURL = "jdbc:mysql://" + settings.get("databaseHost") + ":3306/" + settings.get("databaseDatabase") + "?useSSL=false&autoReconnect=true";
        //dbURL = "jdbc:mysql://" + settings.get("databaseHost") + ":3306/" + settings.get("databaseDatabase");
        user = settings.get("databaseUsername");
        password = settings.get("databasePassword");
        //referenceTableDetails.put("referenceTable", settings.get("databaseTable"));
        //referenceTableDetails.put("referencePrimaryColumn", settings.get("databasePrimaryColumn"));
        //referenceTableDetails.put("referenceDescriptionColumn", settings.get("databaseDescriptionColumn"));
        //referenceTableDetails.put("referenceContentColumn", settings.get("databaseContentColumn"));
        //Logger.info("Attempting to establish connection to :" + dbURL + " with username: " + user + " and password: " + password);

        //if (getIsDatabaseUsed() && !settings.isEmpty()) {
        if (!settings.isEmpty()) {
        
            try {
                if (con == null) {
                    Enumeration e  = DriverManager.getDrivers();
                    while (e.hasMoreElements()) {
                        Driver a = (Driver)e.nextElement();
                    }

                    Logger.info("Trying to connect to " + dbURL + " with user:" + user);
                    
                    con = DriverManager.getConnection(dbURL,user,password); 
                    Logger.info("Connection successful, setting database is used..");
                    setDatabaseIsUsed(true);
                    return true;
                }
                else if (reconnect) {
                    if (con != null) {
                        con.close();
                    } 
                    con = DriverManager.getConnection(dbURL,user,password); 
                    Logger.info("Connection successful, setting database is used..");
                    setDatabaseIsUsed(true);
                    return true;
                }
            }
            catch (SQLException s) {
                Logger.info(s.getMessage());
                setDatabaseIsUsed(false);
            }
        }
        return false;
    }
    
    public static void disconnect() {
        if (con != null) {
            try {
                con.close();
            } 
            catch (SQLException e) { /* ignored */}
        }
    }
    
    
    /**
     *
     * @return
     */
    public static Connection getConnection() {
        return con;
    }

    /**
     *
     * @param acon
     */
    public void setConnection(Connection acon) {
        con = acon;
    }
    
    /**
     *
     * @return
     */
    public static HashMap<String, String>  getReferenceTableDetails() {
        return referenceTableDetails;
    }
    
    /**
     *
     * @param onOff
     */
    public static void setDatabaseIsUsed(boolean onOff) {
        database_used = onOff;
    }
    
    /**
     *
     * @return
     */
    public static boolean getIsDatabaseUsed() {
        return database_used;
    }

}

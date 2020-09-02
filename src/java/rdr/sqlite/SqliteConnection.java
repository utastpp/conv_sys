/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.sqlite;

import java.sql.*;
import javax.servlet.ServletContext;
import rdr.logger.Logger;

/**
 *
 * @author David Chung
 */
public class SqliteConnection {

    /**
     *
     */
    public static String domainName;

    /**
     *
     */
    public static Connection connection;
    
    /**
     *
     * @param domainName
     * @param path
     */
    public static void connect(String domainName, String path)
    {
        
        SqliteConnection.domainName = domainName;
        try {
            Class.forName("org.sqlite.JDBC");
            
            //connection = (Connection) DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "/domain/"+ domainName +".db" );
            
            connection = (Connection) DriverManager.getConnection("jdbc:sqlite:" + path + "/domain/"+ domainName +".db" );
            Logger.info("connection set to: " + "jdbc:sqlite:" + path + "/domain/"+ domainName +".db" );
            if (connection == null)
                Logger.info("but connection is subsequently null..");
        } catch ( Exception e ) {
            System.err.println( "DAVE17" + e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
       // Logger.info("Opened database successfully");
    }
}

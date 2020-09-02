/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class DBOperation {
    
    //private static DBConnection dbConnection = null;
   
    private static String lastError = "";
    private static boolean wasError = false;
    
    /**
     *
     * @param query
     * @return
     */
    public static ResultSet selectQuery(String query) {
        
        
        Statement statement = null;
        ResultSet rs = null;
        
        if (DBConnection.getIsDatabaseUsed()) {
            try {
                // Try to reconnect if we have lost the remote database connection
                if (DBConnection.getConnection() == null) {
                    DBConnection.connect(DialogMain.getReferenceDatabaseDetails(), false);

                }

                if (DBConnection.getConnection() != null) {
                    statement = DBConnection.getConnection().createStatement();
                    rs = statement.executeQuery(query);            
                }


            }
            catch (SQLException s) {
                Logger.warn("DBOperation: " + s.getMessage());
                lastError = query + "\n" + s.getMessage();
                wasError = true;
            }
        }
       return rs; 
    }
    
    /**
     *
     * @param query
     * @return
     */
    public static ArrayList<String> selectQueryAsStringList(String query) {
        
        
        Statement statement = null;
        ArrayList<String> result = new ArrayList<>();
        ResultSet rs = null;
        String row;
        int numCols;
        
        if (DBConnection.getIsDatabaseUsed()) {
            try {
                // Try to reconnect if we have lost the remote database connection
                if (DBConnection.getConnection() == null) {
                    DBConnection.connect(DialogMain.getReferenceDatabaseDetails(), false);                 
                }

                if (DBConnection.getConnection() != null) {
                    statement = DBConnection.getConnection().createStatement();
                    rs = statement.executeQuery(query);            
                }
            }
            catch (SQLException s) {
                Logger.warn("DBOperation: " + s.getMessage());
                lastError = query + "\n" + s.getMessage();
                wasError = true;
            }
        }
       
        try {
            numCols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                row = "";
                for (int i = 1; i <= numCols; i++) {
                    if (i == numCols) {
                        row += rs.getString(i);
                    }
                    else {
                        row += rs.getString(i) + " ";
                    }
                }
                result.add(row);
            }
        }
        catch (SQLException e) {
             Logger.info("SQL error: " + e.getMessage());
             lastError = query + "\n" + e.getMessage();
             wasError = true;
        }
       
       return result; 
    }
     
    /**
     *
     * @param query
     * @param limit
     * @return
     */
    public static String selectQueryAsString(String query, boolean limit) {
        
        /*if (dbConnection == null) {
            dbConnection = new DBConnection("blah");
        }*/
        
        Statement statement = null;
        ResultSet rs = null;
        String result = "";
        int numCols = 0;
        
        if (DBConnection.getIsDatabaseUsed()) {
            try {
                // Try to reconnect if we have lost the remote database connection
                if (DBConnection.getConnection() == null) {
                   DBConnection.connect(DialogMain.getReferenceDatabaseDetails(), false);
                }
                statement = DBConnection.getConnection().createStatement();
                if (limit)
                    rs = statement.executeQuery(query + " LIMIT 1");
                else
                    rs = statement.executeQuery(query);

                numCols = rs.getMetaData().getColumnCount();
                
                boolean hasNext = rs.next();

                while (hasNext) {
                    for (int i = 1; i <= numCols; i++) {
                        if (i == numCols) {
                            result += rs.getString(i);
                        }
                        else {
                            result += rs.getString(i) + " ";
                        }
                        
                    }
                    hasNext = rs.next();
                    if (hasNext)
                        result += "\n";
                } 
            }
            catch (SQLException s) {
                Logger.warn("DBOperation: " + s.getMessage());
                lastError = query + "\n" + s.getMessage();
                wasError = true;
            }
        }
       
       return result; 
    }
    
    /**
     *
     * @param query
     * @param prefixes
     * @param postfixes
     * @param limit
     * @return
     */
    public static String selectQueryAsStringWithFormatting(String query, LinkedHashMap<Integer,String> prefixes, LinkedHashMap<Integer,String> postfixes, boolean limit) {
        
        /*if (dbConnection == null) {
            dbConnection = new DBConnection("blah");
        }*/
        
        Statement statement = null;
        ResultSet rs = null;
        String result = "";
        int numCols = 0;
        
        if (DBConnection.getIsDatabaseUsed()) {
            try {
                // Try to reconnect if we have lost the remote database connection
                if (DBConnection.getConnection() == null) {
                   DBConnection.connect(DialogMain.getReferenceDatabaseDetails(), false);
                }
                statement = DBConnection.getConnection().createStatement();
                if (limit)
                    rs = statement.executeQuery(query + " LIMIT 1");
                else
                    rs = statement.executeQuery(query);

                numCols = rs.getMetaData().getColumnCount();
                
                boolean hasNext = rs.next();

                while (hasNext) {
                    for (int i = 1; i <= numCols; i++) {
                        if (i == numCols) {
                            if (prefixes.containsKey(i)) {
                                result += prefixes.get(i) + rs.getString(i) + postfixes.get(i);
                            }
                            else {
                                result += rs.getString(i);
                            }
                            
                        }
                        else {
                            if (prefixes.containsKey(i)) {
                                result += prefixes.get(i) + rs.getString(i) + postfixes.get(i) + " ";
                            }
                            else {
                                result += rs.getString(i) + " ";
                            }
                        }
                        
                    }
                    hasNext = rs.next();
                    if (hasNext)
                        result += "\n";
                } 
            }
            catch (SQLException s) {
                Logger.warn("DBOperation: " + s.getMessage());
                lastError = query + "\n" + s.getMessage();
                wasError = true;
            }
        }
       
       return result; 
    }
    
    /**
     *
     * @param query
     * @return
     */
    public static String getFirstColumnItem(String query) {
        String result = "";
        ResultSet rs = null;
        
        if (DBConnection.getIsDatabaseUsed()) {
            try {
                rs = selectQuery(query);
                // Only get first row, first column, irrespective of number of rows returned (should only be one)
                if (rs.next()) {
                    result = rs.getString(1);
                }
            }
            catch (SQLException e) {
                Logger.info(e.getMessage());
                lastError = query + "\n" + e.getMessage();
                wasError = true;
            }
        }
        return result;
        
    }
    
    /**
     *
     * @return
     */
    public static String getLastDatabaseError() {
        return lastError;
    }
    
    /**
     *
     * @return
     */
    public static boolean wasDatabaseError() {
        return wasError;
    }
    
    /**
     *
     */
    public static void resetDatabaseError() {
        wasError = false;
        lastError = "";
    }
    
    public static ArrayList<String> getColumnNames(String tableName) {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = '" + tableName + "'";
        ArrayList<String> theNames = selectQueryAsStringList(query);
        ArrayList<String> fullNames = new ArrayList<>();
        for (String aName: theNames)
            fullNames.add(tableName + "." + aName);
        
        return fullNames;
    }
    
    public static ArrayList<String> getTableNames() {
        String databaseName;
        String query;
        ArrayList<String> theNames = new ArrayList<>();
        if (!DialogMain.getReferenceDatabaseDetails().isEmpty()) {
            databaseName = DialogMain.getReferenceDatabaseDetails().get("databaseDatabase");
            query = "SELECT table_name FROM information_schema.tables where table_schema='" + databaseName + "'";
            theNames = selectQueryAsStringList(query);
        }
        return theNames;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.mysql;

import cmcrdr.logger.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import cmcrdr.mysql.*;
import cmcrdr.main.DialogMain;
/**
 *
 * @author Kai Chiu Wong
 */
public class DBCreation {
    
    /**
     *
     */
    public static void initialise(){
        dicListTableCreate();
        DBOperation.insertDic("User");
        // is this used? grammar dictionary doesnt seem to be needed.. DPH
        //DBOperation.insertDic("Grammar");
        dicMatchingTermTableCreate();
        dicRepresentativeTermTableCreate();
        referenceDatabaseDetailTableCreate();
        
        commandVariableTableCreate();
        commandVariableValuesTableCreate();
        commandVariableActionsTableCreate();
        savedQueryTemplateDetailsTableCreate();
        conclusionQueryTableCreate();
        preAndPostProcessorTableCreate();
        dialogSettingsTableCreate();         
    }
    
    public static void initialise(String dbName, String path){
        Logger.info("KB DATABASES BEING CREATED FOR:" + dbName);
        Logger.info("KB DATABASES PATH:" + path);  
        DBOperation.domainDetailTableCreate();
        DBOperation.ruleStructureTableCreate();
        DBOperation.ruleConditionsTableCreate();
        DBOperation.ruleConclusionTableCreate();
        DBOperation.ruleCornerstonesTableCreate();
        DBOperation.caseStructureTableCreate();
        DBOperation.ruleCornerstoneInferenceResultTableCreate();
        DBOperation.categoricalValuesTableCreate();

    }
    
    /**
     * tb_dic_list 
     */
    public static void dicListTableCreate(){
        
        String tableName = "tb_dic_list";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `dic_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `dic_name` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    /**
     * tb_dic_representative_term
     */
    public static void dicRepresentativeTermTableCreate(){
        
        String tableName = "tb_dic_representative_term";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `representative_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `dic_id` INTEGER DEFAULT NULL, \n"
                         + " `representative_term` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, \n"
                         + " `allow_random` BOOLEAN DEFAULT 1 \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    /**
     * tb_dic_matching_term 
     */
    public static void dicMatchingTermTableCreate(){
        
        String tableName = "tb_dic_matching_term";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `term_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `representative_id` INTEGER DEFAULT NULL, \n"
                         + " `matching_term` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    
    
    /**
     *
     */
    public static void commandVariableTableCreate(){
        
        String tableName = "tb_commandvar_list";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `var_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `var_name` VARCHAR(255) DEFAULT NULL, \n"
                         + " `var_override` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    /**
     *
     */
    public static void commandVariableValuesTableCreate(){
        
        String tableName = "tb_commandvar_values";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `value_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `var_id` INTEGER DEFAULT NULL, \n"
                         + " `var_value` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
    }
    
    /**
     *
     */
    public static void commandVariableActionsTableCreate(){
        
        String tableName = "tb_commandvar_actions";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `action_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `var_id` INTEGER DEFAULT NULL, \n"
                         + " `action_target` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_fixed` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_context` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_trigger` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
    }
    
    /**
     *
     */
    public static void referenceDatabaseDetailTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_database_details`; \n" 
                         + "  CREATE TABLE IF NOT EXISTS `tb_database_details` (\n" 
                        +  "  `database_host` VARCHAR(100) NOT NULL,\n" 
                        +  "  `database_database` VARCHAR(100) NOT NULL,\n" 
                        +  "  `database_username` VARCHAR(20) NOT NULL,\n" 
                        +  "  `database_password` VARCHAR(50) NOT NULL,\n" 
                        +  "  `database_selectedfields` VARCHAR(1000) NOT NULL\n" 
 
                        +  ");";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (tb_database_details) created successfully");
    }
    
    
    public static void referenceDatabaseSlotFieldsCreate(ArrayList<String> columnFieldNames) {
        Connection c = DBConnection.getConnection();
   
        
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            
            String columnCreateString = "";
            int currentColumnCount = 0;
            for (String aColumn: columnFieldNames ) {
                if (columnFieldNames.size() > 1) {
                    if (columnCreateString.isEmpty()) {
                        columnCreateString = " `" + aColumn + "` VARCHAR(50), \n";
                        currentColumnCount++;
                    }
                    else {
                        if (currentColumnCount==columnFieldNames.size()-1)
                            columnCreateString += " `" + aColumn + "` VARCHAR(50) \n";
                        else
                            columnCreateString += " `" + aColumn + "` VARCHAR(50), \n";
                        currentColumnCount++;
                    }
                }
                else {
                    columnCreateString = " `" + aColumn + "` VARCHAR(50) \n";
                    currentColumnCount++;
                }
                
            }
            
            String sql = "DROP TABLE IF EXISTS `tb_slotfiller`; \n" 
                         + "  CREATE TABLE IF NOT EXISTS `tb_slotfiller` (\n" 
                        + "  `id` VARCHAR(100) PRIMARY KEY,\n"
                        + columnCreateString
                        +  ");";
            
            //Logger.info("CREATION STRING IS:" + sql);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (tb_slotfiller) created successfully");
    }
    
    /**
     *
     */
    public static void savedQueryTemplateDetailsTableCreate(){
        
        String tableName = "tb_saved_query_details";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `query_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `query_description` VARCHAR(255) DEFAULT NULL, \n"
                         + " `query_select_fields` VARCHAR(255) DEFAULT NULL, \n"
                         + " `query_join_fields` VARCHAR(255) DEFAULT NULL, \n"
                         + " `query_criteria_fields` VARCHAR(255) DEFAULT NULL \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    /**
     *
     */
    public static void conclusionQueryTableCreate(){
        
        String tableName = "tb_conclusion_queries";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `query_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `query_description` VARCHAR(255) DEFAULT NULL, \n"
                         + " `query_text` VARCHAR(255) DEFAULT NULL \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    public static void preAndPostProcessorTableCreate(){     
        String tableName = "tb_prepostprocessor_list";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `action_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `action_match_text` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_replace_text` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_regex` BOOLEAN DEFAULT 0, \n"
                         + " `action_word_only` BOOLEAN DEFAULT 1, \n"
                         + " `action_start_input` BOOLEAN DEFAULT 0, \n"
                         + " `action_end_input` BOOLEAN DEFAULT 0, \n"
                         + " `action_replace` BOOLEAN DEFAULT 0, \n"
                         + " `action_upper` BOOLEAN DEFAULT 0, \n"
                         + " `action_lower` BOOLEAN DEFAULT 0, \n"
                         + " `action_trim` BOOLEAN DEFAULT NULL, \n"
                         + " `action_post` BOOLEAN DEFAULT 0 \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    
    public static void dialogSettingsTableCreate(){     
        String tableName = "tb_dialog_settings";
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `" + tableName +"`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `initial_greeting` VARCHAR(255) DEFAULT NULL, \n"
                         + " `initial_short_greeting` VARCHAR(255) DEFAULT NULL \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
            
            stmt = c.createStatement();
            String sql2 = "INSERT INTO `" + tableName + "`  ( `initial_greeting`, `initial_short_greeting`) "
                            + " VALUES ('Default greeting','Default short greeting') ";
            stmt.executeUpdate(sql2);
            stmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (" + tableName +") created successfully");
      }
    

}

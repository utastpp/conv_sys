/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.sqlite;

import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.dic.DicRepTerm;
import cmcrdr.dic.DicTerm;
import cmcrdr.dic.Dictionary;
import cmcrdr.handler.OutputParser;
import cmcrdr.logger.Logger;
import cmcrdr.savedquery.ConclusionQuery;
import cmcrdr.savedquery.SavedQueryTemplate;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import cmcrdr.processor.PreAndPostProcessorAction;
import rdr.sqlite.SqliteConnection;
import cmcrdr.main.DialogMain;


public class SqliteOperation {
    

    private static Connection c; 
    

    public static int insertDic(String dicName){
        
        String tableName = "tb_dic_list";
        int result = 0;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "INSERT INTO `" + tableName +"` (`dic_name`) VALUES (?)";
            
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, dicName);
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    

    public static int insertDicRepresentativeTerm(int dicId, String representativeTerm, boolean allowRandomSynonym){
        //Logger.info("DB SAVING REPRESENTATIVE TERM " + representativeTerm + " for dictionary " + dicId);
        String tableName = "tb_dic_representative_term";
        int result = 0;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "INSERT INTO `" + tableName +"` (`dic_id`, `representative_term`, `allow_random`) VALUES (?, ?, ?)";
            
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, dicId);
            pstmt.setString(2, representativeTerm);
            pstmt.setBoolean(3, allowRandomSynonym);
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    

    public static void deleteDicRepresentativeTerm(int dicId, String representativeTerm){
        
        String tableName = "tb_dic_representative_term";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `dic_id` = ? AND `representative_term` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, dicId);
            pstmt.setString(2, representativeTerm);
                
            pstmt.execute();
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    

    public static void insertDicMatchingTerm(int representativeId, String matchingTerm){
        //Logger.info("DB SAVING MATCHING TERM " + matchingTerm + " FOR REPRESENTATIVE ID " + representativeId);
        String tableName = "tb_dic_matching_term";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `" + tableName +"` (`representative_id`, `matching_term`) VALUES (?, ?)";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, representativeId);
            pstmt.setString(2, matchingTerm);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    

    public static void exportDictionary(int dicID, File file) {
        
        String tableName1 = "tb_dic_representative_term";
        String tableName2 = "tb_dic_matching_term";
        ArrayList<String> repTermsSaved = new ArrayList<>();
        
         c = SqliteConnection.connection;
        
        Statement stmt = null;
               
        try {
            String sql = "SELECT representative_term, matching_term, allow_random FROM " + tableName1 +" AS R JOIN " + tableName2 + " AS M ";
            sql += "ON R.representative_id = M.representative_id ";
            sql += " where dic_id = '" + dicID + "' ";
            sql += "ORDER BY representative_term,matching_term;";
            stmt = c.createStatement();
            
            ResultSet rs = stmt.executeQuery(sql);
            PrintWriter writer = new PrintWriter(file.getPath());
            
            String previousRepTerm = "";
            while ( rs.next() ) {           
                String repTerm = rs.getString(1);
                String matchTerm = rs.getString(2);
                boolean allowRandom = rs.getBoolean(3);
                int allowRandomSynonym;
                if (allowRandom)
                    allowRandomSynonym = 1;
                else
                    allowRandomSynonym = 0;
                
                if (!previousRepTerm.equals(repTerm)) {
                    writer.println("# new term:" + repTerm);
                }
                writer.println(repTerm + " " + allowRandomSynonym + " " + matchTerm);
                previousRepTerm = repTerm;
                if (!repTermsSaved.contains(repTerm))
                    repTermsSaved.add(repTerm);
            }
            
            rs.close();
            stmt.close();
            
            // save any rep terms that didn't have matching terms (so were'nt included in the query above)
            for (Map.Entry aDicTermEntry: DialogMain.dictionary.getDicBase().entrySet()) {
                String repTermString = ((DicTerm)aDicTermEntry.getValue()).getRepresentativeTerm();
                if (!repTermsSaved.contains(repTermString)) {
                    writer.println("# term with no matching terms");
                    writer.println(repTermString + " ");

                }
            }
                       
            writer.close();
           
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param dicId
     */
    public static void deleteAllMatchingTermsForDictionary(int dicId) {
        String tableName = "tb_dic_matching_term";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "delete from `" + tableName +"`";
            
            pstmt = c.prepareStatement(sql);               
            pstmt.execute();           
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    
    /**
     * tb_dic_matching_term 테이블에서 matchingTerm(동의어)을 삭제.
     * @param representativeId
     */
    public static void deleteDicMatchingTermByRepresentativeId(int representativeId){
        
        String tableName = "tb_dic_matching_term";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `representative_id` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, representativeId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     * tb_dic_matching_term 테이블에서 matchingTerm(동의어)을 삭제.
     * @param representativeId
     * @param matchingTerm
     */
    public static void deleteDicMatchingTerm(int representativeId, String matchingTerm){
        
        String tableName = "tb_dic_matching_term";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `representative_id` = ? AND `matching_term` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, representativeId);
            pstmt.setString(2, matchingTerm);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    public static void updatePreAndPostProcessorList(ArrayList<PreAndPostProcessorAction> preprocessorActions){
        
        String tableName = "tb_prepostprocessor_list";
        int result = 0;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "DELETE FROM `" + tableName +"`";
            //Logger.info("Trying to do: " + sql);
            
            pstmt = c.prepareStatement(sql);      
            pstmt.execute();
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        
         try {
            String sql = "INSERT INTO `" + tableName +"` (`action_match_text`, `action_replace_text`, `action_regex`, `action_word_only`, `action_start_input`, `action_end_input`, `action_replace`, `action_upper`, `action_lower`, `action_trim`, `action_post`) VALUES (?, ?, ?, ?,?,?,?,?,?,?, ?)";
            /*

            */
            for (PreAndPostProcessorAction anAction: preprocessorActions) {
                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, anAction.getMatchText());
                pstmt.setString(2, anAction.getReplaceText());
                pstmt.setBoolean(3, anAction.getRegex());
                pstmt.setBoolean(4, anAction.getWordOnly());
                pstmt.setBoolean(5, anAction.getStartOfInput());
                pstmt.setBoolean(6, anAction.getEndOfInput());
                pstmt.setBoolean(7, anAction.getReplaceOption());
                pstmt.setBoolean(8, anAction.getUpperOption());
                pstmt.setBoolean(9, anAction.getLowerOption());
                pstmt.setBoolean(10, anAction.getTrimOption());               
                pstmt.setBoolean(11, anAction.getPostOption());               
                pstmt.execute();
            }
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }   
    }
    
    
    
    /**
     * tb_dic_list 테이블에 존재하는 dictionary의 리스트를 return.
     * @return 
     */
    public static HashMap<Integer,Dictionary> getDicList(){
        
        String tableName = "tb_dic_list";
        
        HashMap<Integer, Dictionary> result = new HashMap<>();
        c = SqliteConnection.connection;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM '" + tableName +"';" );
            
            while ( rs.next() ) {
                int dicId = rs.getInt(1);
                String dicName = rs.getString(2);
                
                Dictionary aDictionary = new Dictionary();
                aDictionary.setDicId(dicId);
                aDictionary.setDicName(dicName);
                
                result.put(dicId, aDictionary);
            }
            
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    
    /**
     * tb_dic_list 테이블에 주어진 dicName을 가진 dictionary를 return.
     * @param dicName
     * @return 
     */
    public static Dictionary getDicByName(String dicName){
        
        String tableName = "tb_dic_list";
        
        Dictionary result = new Dictionary();
        c = SqliteConnection.connection;
        
        PreparedStatement pstmt = null;
        try {
            pstmt = c.prepareStatement("SELECT * FROM '" + tableName +"' WHERE `dic_name` = ?;");
            pstmt.setString(1, dicName);
            
            ResultSet rs = pstmt.executeQuery();
            
            while ( rs.next() ) {
                int dicId = rs.getInt(1);
                
                result.setDicId(dicId);
                result.setDicName(dicName);
            }
            
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE1" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    
    /**
     * tb_dic_representative_term 테이블 내에 주어진 dicId의 representativeTerm(대표어)의 리스트를 return.
     * @param dicId
     * @return 
     */
    public static HashMap<Integer,DicRepTerm> getRepresentativeTermList(int dicId){
        String tableName = "tb_dic_representative_term";
        
        HashMap<Integer, DicRepTerm> result = new HashMap<>();
        DicRepTerm aTerm;
        c = SqliteConnection.connection;
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT * FROM '" + tableName +"' WHERE `dic_id` = ? ORDER BY `representative_term`;";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, dicId);
            
            ResultSet rs = pstmt.executeQuery( );
            
            while ( rs.next() ) {
                int representativeId = rs.getInt(1);
                aTerm = new DicRepTerm();
                String representativeTerm = rs.getString(3);
                boolean allowRandomSynonym = rs.getBoolean(5);
                aTerm.setRepresentativeTerm(representativeTerm);
                aTerm.setAllowRandomSynonym(allowRandomSynonym);
                //Logger.info("FOUND REPRESENTATIVE TERM: " + representativeTerm + " dicID: " + dicId);
                
                result.put(representativeId, aTerm);
            }
            
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE2" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    
       
    // DPH LOAD MATCHING TERMS
    /**
     * tb_dic_matching_term 테이블 내에 모든 matchingTerm(동의어)의 리스트(대표어를 포함하지 않음)를 return.
     * @return 
     */
    public static HashMap<Integer,DicTerm> getMatchingTermList(){
        
        String tableName = "tb_dic_matching_term";
        
        HashMap<Integer,DicTerm> result = new HashMap<>();
        
        //ArrayList<String> matchingTermsFromReferenceDatabase;
        //ArrayList<String> masterList;
        
        c = SqliteConnection.connection;
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT * FROM '" + tableName +"' WHERE 1 ORDER BY LENGTH(`matching_term`) DESC;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            
            while ( rs.next() ) {
                int representativeId = rs.getInt(2);
                String matchingTerm = rs.getString(3);
                //masterList = new ArrayList<>();
                
                /*
                Logger.info("Looking at term: " + matchingTerm + " for repId: " + representativeId);
                if (matchingTerm.contains(getDBMarkerString(DB_DATABASE_TYPE))) {
                    matchingTermsFromReferenceDatabase = DicManager.getDicTermsFromDatabase(matchingTerm);

                    if (!matchingTermsFromReferenceDatabase.isEmpty()) {
                        // we have a matching term list which is a reference to the reference database
                        // we don't want to save these synonyms in the system database, so don't insertDicMatchingTerm..
                        for (String aDatabaseMatchingTerm: matchingTermsFromReferenceDatabase) {
                            Logger.info("Adding the following database matching term: " + aDatabaseMatchingTerm + " to dictionary for rep term: " + representativeId);
                            masterList.add(aDatabaseMatchingTerm);
                        }                
                    }
                }*/
                

                if(!result.containsKey(representativeId)){
                    DicTerm aDicTerm = new DicTerm();
                    aDicTerm.addMatchingTerm(matchingTerm);
                    /*for (String aDatabaseMatchingTerm: masterList) {
                        Logger.info("new repId! Adding the matching term: " + aDatabaseMatchingTerm + " to aDicTerm");
                        aDicTerm.addMatchingTerm(aDatabaseMatchingTerm);
                    }*/
                    result.put(representativeId, aDicTerm);
                } else {
                    DicTerm aDicTerm = result.get(representativeId);
                    aDicTerm.addMatchingTerm(matchingTerm);
                    /*for (String aDatabaseMatchingTerm: masterList) {
                        Logger.info("existing repId! Adding the matching term: " + aDatabaseMatchingTerm + " to aDicTerm");
                        aDicTerm.addMatchingTerm(aDatabaseMatchingTerm);
                    }
                    */
                    result.replace(representativeId, aDicTerm);
                }
                
            }
                        
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE3" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    
    
    
       /**
     * tb_poi_matching_term 테이블 내에 모든 POI의 리스트를 return.
     * @return 
     */
    public static String getInitialDialogGreeting(){
        String result = "Default greeting not set...";
        String tableName = "tb_dialog_settings";
        
        c = SqliteConnection.connection;
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT initial_greeting FROM '" + tableName +"' WHERE 1;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            
            String initialGreeting;
            if ( rs.next() ) {
                initialGreeting = rs.getString(1);
                if (!initialGreeting.isEmpty())
                    result = initialGreeting;
               // Logger.info("Inital greeting is:" + result);
            }
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE5" + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    public static String getInitialShortDialogGreeting(){
        String result = "Default greeting not set...";
        String tableName = "tb_dialog_settings";
        
        c = SqliteConnection.connection;
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT initial_short_greeting FROM '" + tableName +"' WHERE 1;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            
            String initialGreeting;
            if ( rs.next() ) {
                initialGreeting = rs.getString(1);
                if (initialGreeting != null)
                    result = initialGreeting;
               // Logger.info("Inital short greeting is:" + result);
            }
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE5" + e.getClass().getName() + ": " + e.getMessage() );
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     *
     */
    public static void deleteAllContextVariables() {
        String tableName = "tb_commandvar_list";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "delete from `" + tableName +"`";
            
            pstmt = c.prepareStatement(sql);
                
            pstmt.execute();
           
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     */
    public static void deleteAllContextVariableValues() {
        String tableName = "tb_commandvar_values";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "delete from `" + tableName +"`";
            
            pstmt = c.prepareStatement(sql);
                
            pstmt.execute();
           
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     */
    public static void deleteAllContextVariableActions() {

        String tableName = "tb_commandvar_actions";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "delete from `" + tableName +"`";
            
            pstmt = c.prepareStatement(sql);
                
            pstmt.execute();
           
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param varName
     * @return
     */
    public static int insertContextVariable(String varName, String varOverride){
        
        String tableName = "tb_commandvar_list";
        int result = 0;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "INSERT INTO `" + tableName +"` (`var_name`, `var_override`) VALUES (?, ?)";
            
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, varName);
            pstmt.setString(2, varOverride);
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    /**
     *
     * @param varId
     * @param target
     * @param fixed
     * @param context
     * @param trigger
     * @return
     */
    public static int insertContextVariableAction(int varId, String target, String fixed, String context, String trigger){
        //Logger.info("I'm being called with " + varId + ", " + target + ", " + fixed + ", " + context + ", " + trigger);

        String tableName = "tb_commandvar_actions";
        int result = 0;
        /*var_id` INTEGER DEFAULT NULL, \n"
                         + " `action_target` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_fixed` VARCHAR(255) DEFAULT NULL, \n"
                         + " `action_context` VARCHAR(255) DEFAULT NULL, \n"*/
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "INSERT INTO `" + tableName +"` (`var_id`, `action_target`, `action_fixed`  , `action_context` , `action_trigger`) VALUES (?, ?, ?, ?, ?)";
            
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, varId);
            pstmt.setString(2, target);
            pstmt.setString(3, fixed);
            pstmt.setString(4, context);
            pstmt.setString(5, trigger);
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }      

    /**
     *
     * @param varId
     * @param varValue
     * @return
     */
    public static int insertContextVariableValue(int varId, String varValue){
        
        String tableName = "tb_commandvar_values";
        int result = 0;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "INSERT INTO `" + tableName +"` (`var_id`, `var_value`) VALUES (?, ?)";
            
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, varId);
            pstmt.setString(2, varValue);
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    /**
     *
     * @param varName
     */
    public static void deleteContextVariable(String varName){
        
        String tableName = "tb_commandvar_list";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `var_name` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setString(1, varName);
                
            pstmt.execute();
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param varId
     */
    public static void deleteContextVariableValueByVarId(int varId){
        
        String tableName = "tb_commandvar_values";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `var_id` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, varId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param varId
     */
    public static void deleteContextVariableActionByVarId(int varId){
        //Logger.info("I'm being called with " + varId);
        
        String tableName = "tb_commandvar_actions";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `var_id` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, varId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param varId
     * @param varValue
     */
    public static void deleteContextVariableValue(int varId, String varValue){
        
        String tableName = "tb_commandvar_values";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `var_id` = ? AND `var_value` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, varId);
            pstmt.setString(2, varValue);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param varId
     * @param target
     */
    public static void deleteContextVariableAction(int varId, String target){
        //Logger.info("I'm being called with " + varId + "," + target);
        String tableName = "tb_commandvar_actions";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `var_id` = ? AND `target` = ? ";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, varId);
            pstmt.setString(2, target);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @return
     */
    //public static LinkedHashMap<Integer,String> getContextVariableList(){
    public static LinkedHashMap<Integer,ContextVariable> getContextVariableList(){
        
        String tableName = "tb_commandvar_list";
        
        //LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
        LinkedHashMap<Integer, ContextVariable> result = new LinkedHashMap<>();
        c = SqliteConnection.connection;
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT * FROM '" + tableName +"' ORDER BY `var_name`;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            
            while ( rs.next() ) {
                ContextVariable cv = new ContextVariable();
                int varId = rs.getInt(1);
                String varName = rs.getString(2);
                String varOverride = rs.getString(3);
                cv.setVariableId(varId);
                cv.setVariableName(varName);
                cv.setVariableValueOverride(varOverride);
                //result.put(varId, varName);
                result.put(varId, cv);
            }
            
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            Logger.info( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    /**
     *
     * @return
     */
    public static LinkedHashMap<Integer,ContextVariable > getContextVariableValuesList(){
        
        String tableName = "tb_commandvar_values";
        
        //LinkedHashMap<Integer,String> value;
        
        LinkedHashMap<Integer,ContextVariable> result = new LinkedHashMap<Integer,ContextVariable>();
        
        c = SqliteConnection.connection;
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT * FROM '" + tableName +"' WHERE 1 ORDER BY LENGTH(`var_value`) DESC;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            
            while ( rs.next() ) {
                int valueId = rs.getInt(1);
                int varId = rs.getInt(2);
                String varValue = rs.getString(3);
                
                if(!result.containsKey(varId)){
                    ContextVariable aVariable = new ContextVariable();
                    aVariable.addVariableValue(varValue);
                    result.put(varId, aVariable);
                    
                } else {
                    ContextVariable aVariable = result.get(varId);
                    aVariable.addVariableValue(varValue);
                    
                    result.replace(varId, aVariable);
                
                }
                
            }
            
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE6" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    /**
     *
     * @return
     */
    public static LinkedHashMap<Integer,ContextVariable > getContextVariableActionsList(){
        
        String tableName = "tb_commandvar_actions";
        
        //LinkedHashMap<Integer,String> value;
        
        LinkedHashMap<Integer,ContextVariable> result = new LinkedHashMap<Integer,ContextVariable>();
        
        c = SqliteConnection.connection;
        
        PreparedStatement pstmt = null;
        try {
            //String sql = "SELECT * FROM '" + tableName +"' WHERE 1 ORDER BY LENGTH(`action_target`) DESC;";
            String sql = "SELECT * FROM '" + tableName +"' WHERE 1;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            
            while ( rs.next() ) {
                int actionId = rs.getInt(1);
                int varId = rs.getInt(2);
                String actionTarget = rs.getString(3); 
                String actionFixed = rs.getString(4);
                String actionContext = rs.getString(5);
                String actionTrigger = rs.getString(6);
                
                if(!result.containsKey(varId)){
                    ContextVariable aVariable = new ContextVariable();
                    if (actionFixed != null)
                        aVariable.addVariableActionFixed(actionTarget,actionFixed,actionTrigger);
                    else if (actionContext != null)
                        aVariable.addVariableActionContext(actionTarget,actionContext,actionTrigger);

                    result.put(varId, aVariable);
                    
                } else {
                    ContextVariable aVariable = result.get(varId);
                    if (actionFixed != null)
                        aVariable.addVariableActionFixed(actionTarget,actionFixed,actionTrigger);
                    else if (actionContext != null)
                        aVariable.addVariableActionContext(actionTarget,actionContext,actionTrigger);
                   
                    result.replace(varId, aVariable);                
                }               
            }
            
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE7" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    }
    
    /**
     *
     * @return
     */
    public static HashMap<String,String> getReferenceDatabaseDetails(){
        //Logger.info("I'm being asked to get reference database details..");
        HashMap<String, String> databaseDetails = new HashMap<>();
        c = SqliteConnection.connection;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM tb_database_details;" );
            
            while ( rs.next() ) {
                String databaseHost = rs.getString(1);
                String databaseDatabase = rs.getString(2);
                String databaseUsername = rs.getString(3);
                String databasePassword = rs.getString(4);
                String databaseSelectedFields = rs.getString(5);
                
                //Logger.info("Found host:" + databaseHost);
                //Logger.info("Found database:" + databaseDatabase);
                //Logger.info("Found username:" + databaseUsername);
                //Logger.info("Found password:" + databasePassword);
                //Logger.info("Found selected fields:" + databaseSelectedFields);
                
                databaseDetails.put("databaseHost", databaseHost);
                databaseDetails.put("databaseDatabase", databaseDatabase);
                databaseDetails.put("databaseUsername", databaseUsername);
                databaseDetails.put("databasePassword", databasePassword);
                databaseDetails.put("databaseSelectedFields", databaseSelectedFields);
            }
            
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE8" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return databaseDetails;
    }

    /**
     *
     * @param databaseDetails
     */
    public static void  setReferenceDatabaseDetails(HashMap<String,String> databaseDetails){
        c = SqliteConnection.connection;
        
        PreparedStatement pstmt = null;
        if (!databaseDetails.isEmpty()) {
            try {
                String sql = "INSERT INTO `tb_database_details`  ( `database_host`, `database_database`, `database_username`, `database_password`, `database_selectedfields` ) "
                            + " VALUES (?, ?, ?, ?, ?) ";

                //String sql = "UPDATE `tb_database_details` SET `database_host` = ?, `database_database` = ?, `database_username` = ?, `database_password`= ?";

                String sqldelete = "DELETE FROM `tb_database_details`";
                pstmt = c.prepareStatement(sqldelete);
                pstmt.execute();

                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, databaseDetails.get("databaseHost"));
                pstmt.setString(2, databaseDetails.get("databaseDatabase"));
                pstmt.setString(3, databaseDetails.get("databaseUsername"));
                pstmt.setString(4, databaseDetails.get("databasePassword"));              
                pstmt.setString(5, databaseDetails.get("databaseSelectedFields"));              
                pstmt.execute();

                pstmt.close();
            } catch ( Exception e ) {
                e.printStackTrace();
                Logger.error( e.getClass().getName() + ": " + e.getMessage() );
            }
        }
    }
    
    /**
     *
     * @return
     */
    public static LinkedHashMap<Integer,SavedQueryTemplate> getSavedQueryTemplateDetails(){
        LinkedHashMap<Integer, SavedQueryTemplate> savedQueryDetails = new LinkedHashMap<>();
        SavedQueryTemplate aSavedQuery;
        
        c = SqliteConnection.connection;
        Statement stmt;
        try {
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery( "SELECT * FROM tb_saved_query_details;" );
            
            while ( rs.next() ) {
                int queryId = rs.getInt(1);
                String queryDescription = rs.getString(2);
                String querySelect = rs.getString(3);
                String queryJoin = rs.getString(4);
                String queryCriteria = rs.getString(5);
                aSavedQuery = new SavedQueryTemplate();
                aSavedQuery.setId(queryId);
                aSavedQuery.setDescription(queryDescription);
                aSavedQuery.setSelect(querySelect);
                aSavedQuery.setjoin(queryJoin);
                aSavedQuery.setCriteria(queryCriteria);
                              
                savedQueryDetails.put(queryId, aSavedQuery);

            }
            
            rs.close();
            //stmt.close();
        } catch ( Exception e ) {
            Logger.error(e.getClass().getName() + ": " + e.getMessage() );
        }
        return savedQueryDetails;
    }
    
    
    public static ArrayList<PreAndPostProcessorAction > getPreAndPostProcessorActionsList(){
        
        String tableName = "tb_prepostprocessor_list";
        
        //LinkedHashMap<Integer,String> value;
        
        ArrayList<PreAndPostProcessorAction> result = new ArrayList<>();
        
        c = SqliteConnection.connection;
        
        if (c == null)
            Logger.info("DAVE - C IS NULL!");
        
         PreparedStatement pstmt = null;
        try {
            String sql = "SELECT * FROM '" + tableName +"' WHERE 1;";
            
            pstmt = c.prepareStatement(sql);
            
            ResultSet rs = pstmt.executeQuery( );
            PreAndPostProcessorAction anAction;
            
            while ( rs.next() ) {
                int actionId = rs.getInt(1);
                String matchText = rs.getString(2);
                String replaceText = rs.getString(3);
                Boolean regex = rs.getBoolean(4);
                Boolean wordOnly = rs.getBoolean(5);
                Boolean startInput = rs.getBoolean(6);
                Boolean endInput = rs.getBoolean(7);
                Boolean replace = rs.getBoolean(8);
                Boolean upper = rs.getBoolean(9);
                Boolean lower = rs.getBoolean(10);
                Boolean trim = rs.getBoolean(11);
                Boolean post = rs.getBoolean(12);
                anAction = new PreAndPostProcessorAction(matchText, replaceText, regex, wordOnly, startInput, endInput, replace, upper, lower, trim, post);
                result.add(anAction);             
            }
            
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE9" + e.getClass().getName() + ": " + e.getMessage() );
            e.printStackTrace();

        }
        return result;
    }
    
    /**
     *
     * @param savedQueryDetails
     * @return
     */
    public static int insertNewQueryTemplateDetails(SavedQueryTemplate savedQueryDetails){
        String tableName = "tb_saved_query_details";
        int result = -1;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "INSERT INTO `" + tableName +"` (`query_description`, `query_select_fields`, `query_join_fields`, `query_criteria_fields`) VALUES (?, ?, ?, ?)";
            
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, savedQueryDetails.getDescription());
            pstmt.setString(2, savedQueryDetails.getSelect());
            pstmt.setString(3, savedQueryDetails.getJoin());
            pstmt.setString(4, savedQueryDetails.getCriteria());
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    } 
    
    /**
     *
     * @param queryId
     */
    public static void deleteSavedQueryTemplate(int queryId){
        
        String tableName = "tb_saved_query_details";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "DELETE FROM `" + tableName +"` WHERE `query_id` = ?";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, queryId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /*
                         + " CREATE TABLE IF NOT EXISTS `" + tableName +"` (\n"
                         + " `query_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `query_description` VARCHAR(255) DEFAULT NULL, \n"
                         + " `query_text` VARCHAR(255) DEFAULT NULL \n"
                         + ");\n";
    */

    /**
     *
     * @param description
     * @param query
     * @return
     */

    
    public static int insertNewConclusionQuery(String description, String query){
        String tableName = "tb_conclusion_queries";
        int result = -1;
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            
            // do an insertion to get the query_id.
            String sql = "INSERT INTO `" + tableName +"` (`query_description`, `query_text`) VALUES (?, ?)";           
            pstmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, description);
            pstmt.setString(2, "placeholder"); // we want to get the query id so we can then update the field with actual content..
                
            pstmt.execute();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            pstmt.close();
            
            // now update the insertion, including the id in the query text
            sql = "update `" + tableName +"`  set `query_text`=? where query_id='" + result + "'";
            
            // wrap the appropriate tags around the query, embedding the query id in the outer tag
            query = OutputParser.getTag(result + query, OutputParser.DB_DATABASE_TYPE);
            pstmt = c.prepareStatement(sql);
            pstmt.setString(1, query);
                
            pstmt.execute();
            
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return result;
    } 
    
    /**
     *
     * @return
     */
    public static LinkedHashMap<Integer,ConclusionQuery> getConclusionQueryDetails(){
        LinkedHashMap<Integer, ConclusionQuery> conclusionQueryDetails = new LinkedHashMap<>();
        ConclusionQuery aConclusionQuery;
        
        c = SqliteConnection.connection;
        Statement stmt;
        try {
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery( "SELECT * FROM tb_conclusion_queries;" );
            
            while ( rs.next() ) {
                int queryId = rs.getInt(1);
                String queryDescription = rs.getString(2);
                String queryText = rs.getString(3);
                
                aConclusionQuery = new ConclusionQuery();
                aConclusionQuery.setId(queryId);
                aConclusionQuery.setDescription(queryDescription);
                aConclusionQuery.setQuery(queryText);
                                            
                conclusionQueryDetails.put(queryId, aConclusionQuery);

            }
            
            rs.close();
            //stmt.close();
        } catch ( Exception e ) {
            Logger.error(e.getClass().getName() + ": " + e.getMessage() );
        }
        return conclusionQueryDetails;
    }
    
    
    public static void updateInitialGreeting(String greeting){
        
        String tableName = "tb_dialog_settings";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        Logger.info("Setting initial greeting to:" + greeting);
        /*try {
            String sql = "DELETE FROM `" + tableName +"`";
            
            pstmt = c.prepareStatement(sql);      
            pstmt.execute();
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }*/
        
         /*try {
            String sql = "INSERT INTO `" + tableName +"` (`initial_greeting`) VALUES (?)";

                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, greeting);
              
                pstmt.execute();          
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }   */
         
        try {
            String sql = "UPDATE `" + tableName +"` SET `initial_greeting`= ?";

                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, greeting);
              
                pstmt.execute();          
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        } 
    }
    
    public static void updateInitialShortGreeting(String greeting){
        
        String tableName = "tb_dialog_settings";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        
        /*try {
            String sql = "DELETE FROM `" + tableName +"`";
            
            pstmt = c.prepareStatement(sql);      
            pstmt.execute();
            pstmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }*/
        
         try {
            String sql = "UPDATE `" + tableName +"` SET `initial_short_greeting`= ?";

                pstmt = c.prepareStatement(sql);
                pstmt.setString(1, greeting);
              
                pstmt.execute();          
            pstmt.close();
        } catch ( SQLException e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }   
    }
    
    
    /**
     *
     * @param domainName
     * @param path
     * @return 
     */
    public static String getDomainDescription(String domainName, String path)
    {
        Connection connection;
        String description = "";
        
        try {
            Class.forName("org.sqlite.JDBC");

            connection = (Connection) DriverManager.getConnection("jdbc:sqlite:" + path + "/domain/"+ domainName +".db" );
            if (connection != null) {
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery( "SELECT domain_desc FROM tb_domain_details;" );

                    if ( rs.next() ) {
                        description = rs.getString(1);
                    }
                    rs.close();
                    stmt.close();
                } catch ( Exception e ) {
                    System.err.println( "Problem getting domain description1:" + e.getClass().getName() + ": " + e.getMessage() );
                }
                connection.close();
            }
            
        } catch ( Exception e ) {
            System.err.println( "Problem getting domain description2:" + e.getClass().getName() + ": " + e.getMessage() );
            description = "";
        }
        
        return description;
    }
    
    public static ArrayList<String> getCurrentSlotInformation(String userID,ArrayList<String> columnFieldNames) {
        ArrayList<String> theCurrentSlots = new ArrayList<>();
                
        c = SqliteConnection.connection;
        Statement stmt;
        try {
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery( "SELECT * FROM tb_slotfiller where id='" + userID + "';" );
            
            if (rs.next()) {
                // skip the ID column
                for (int count = 2; count <= columnFieldNames.size()+1; count++) {
                    if (rs.getString(count) == null)
                        theCurrentSlots.add("");
                    else
                        theCurrentSlots.add(rs.getString(count));
                    //Logger.info("Loaded the following item:" + rs.getString(count));
                }
            }
            
            rs.close();
            //stmt.close();
        } catch ( Exception e ) {
            Logger.error(e.getClass().getName() + ": " + e.getMessage() );
        }
        return theCurrentSlots;    
    }
    
    public static void updateCurrentSlotInformation(String userID, String field, String value){
        
        String tableName = "tb_slotfiller";
        
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        
        
        try {
            String sql = "INSERT OR IGNORE INTO `" + tableName +"` (id," + "`" + field + "`" + ") VALUES ('" + userID + "','" + value + "')";
                //Logger.info("The insertion string is:" + sql);
                pstmt = c.prepareStatement(sql);
                pstmt.execute();          
            pstmt.close();
        } catch ( SQLException e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }   
        
         try {
            String sql = "UPDATE `" + tableName +"` SET `"  + field  + "`= ? where id='" + userID + "'";
            //Logger.info("The 2nd insertion string is:" + sql);
            pstmt = c.prepareStatement(sql);
            pstmt.setString(1, value);

            pstmt.execute();          
            pstmt.close();
        } catch ( SQLException e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }   
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.mysql;

import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.dic.DicRepTerm;
import cmcrdr.dic.DicTerm;
import cmcrdr.dic.Dictionary;
import cmcrdr.handler.OutputParser;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.processor.PreAndPostProcessorAction;
import cmcrdr.savedquery.ConclusionQuery;
import cmcrdr.savedquery.SavedQueryTemplate;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import rdr.apps.Main;
import rdr.cases.CaseStructure;
import rdr.model.Attribute;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;



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
    
    public static int insertDic(String dicName) {
        
        
        PreparedStatement pstmt = null;
        String tableName = "tb_dic_list";
        ResultSet rs = null;
        int result = 0;
        
        if (DBConnection.getIsDatabaseUsed()) {
            try {
                // Try to reconnect if we have lost the remote database connection
                if (DBConnection.getConnection() == null) {
                    DBConnection.connect(DialogMain.getReferenceDatabaseDetails(), false);

                }

                if (DBConnection.getConnection() != null) {
                    String sql = "INSERT INTO `" + tableName +"` (`dic_name`) VALUES (?)";

                    pstmt = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, dicName);

                    pstmt.execute();
                    rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        result = rs.getInt(1);
                    }
                    pstmt.close();   
                }


            }
            catch (Exception e) {
                Logger.error( e.getClass().getName() + ": " + e.getMessage() );
            }
        }
        return result; 
    }
    
    public static int insertDicRepresentativeTerm(int dicId, String representativeTerm, boolean allowRandomSynonym){
        //Logger.info("DB SAVING REPRESENTATIVE TERM " + representativeTerm + " for dictionary " + dicId);
        String tableName = "tb_dic_representative_term";
        int result = 0;
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
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
     * tb_dic_matching_term 
     * @param representativeId
     */
    public static void deleteDicMatchingTermByRepresentativeId(int representativeId){
        
        String tableName = "tb_dic_matching_term";
        
        Connection c = DBConnection.getConnection();
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
     * tb_dic_matching_term 
     * @param representativeId
     * @param matchingTerm
     */
    public static void deleteDicMatchingTerm(int representativeId, String matchingTerm){
        
        String tableName = "tb_dic_matching_term";
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
     * tb_dic_list 
     * @return 
     */
    public static HashMap<Integer,Dictionary> getDicList(){
        
        String tableName = "tb_dic_list";
        
        HashMap<Integer, Dictionary> result = new HashMap<>();
        Connection c = DBConnection.getConnection();
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
     * tb_dic_list 
     * @param dicName
     * @return 
     */
    public static Dictionary getDicByName(String dicName){
        
        String tableName = "tb_dic_list";
        
        Dictionary result = new Dictionary();
        Connection c = DBConnection.getConnection();
        
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
     * tb_dic_representative_term
     * @param dicId
     * @return 
     */
    public static HashMap<Integer,DicRepTerm> getRepresentativeTermList(int dicId){
        String tableName = "tb_dic_representative_term";
        
        HashMap<Integer, DicRepTerm> result = new HashMap<>();
        DicRepTerm aTerm;
        Connection c = DBConnection.getConnection();
        
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
     * tb_dic_matching_term 
     * @return 
     */
    public static HashMap<Integer,DicTerm> getMatchingTermList(){
        
        String tableName = "tb_dic_matching_term";
        
        HashMap<Integer,DicTerm> result = new HashMap<>();
        
        //ArrayList<String> matchingTermsFromReferenceDatabase;
        //ArrayList<String> masterList;
        
        Connection c = DBConnection.getConnection();
        
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
     * tb_poi_matching_term 
     * @return 
     */
    public static String getInitialDialogGreeting(){
        String result = "Default greeting not set...";
        String tableName = "tb_dialog_settings";
        
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
        
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
        Connection c = DBConnection.getConnection();
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
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
        
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
        Connection connection = DBConnection.getConnection();
        String description = "";
        
        try {
            
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
                
        Connection c = DBConnection.getConnection();
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
        
        Connection c = DBConnection.getConnection();
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
    
    
    /**
     *
     * @param sql
     */
    public static synchronized void executeQuery(String sql){
        
        Connection c = DBConnection.getConnection();
        
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        rdr.logger.Logger.info("Query (" + sql + ") executed successfully");
    }    
    
    /**
     *
     */
    public static void domainDetailTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_domain_details`; \n" 
                         + "  CREATE TABLE IF NOT EXISTS `tb_domain_details` (\n" 
                        +  "  `domain_name` VARCHAR(50) NOT NULL,\n" 
                        +  "  `domain_desc` VARCHAR(255) NOT NULL,\n" 
                        +  "  `domain_reasoner` VARCHAR(5) NOT NULL,\n" 
                        +  "  `domain_created` DATETIME NOT NULL,\n" 
                        +  "  `domain_modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\n" 
                        +  ");";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        rdr.logger.Logger.info("Table (tb_domain_details) created successfully");
    }
    
    /**
     *
     */
    public static void caseStructureTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_case_structure`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_case_structure` (\n" 
                         + " `attribute_id` INTEGER NOT NULL, \n"
                         + " `value_type_id` INTEGER NOT NULL, \n"
                         + " `attribute_name` VARCHAR(255) NULL \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        rdr.logger.Logger.info("Table (tb_case_structure) created successfully");
    }
    
    /**
     *
     */
    public static void categoricalValuesTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_categorical_value`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_categorical_value` (\n" 
                         + " `categorical_value_id` INTEGER PRIMARY KEY   AUTOINCREMENT, \n"
                         + " `attribute_id` INTEGER NOT NULL, \n"
                         + " `value_name` VARCHAR(255) NULL \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        rdr.logger.Logger.info("Table (tb_categorical_value) created successfully");
    }
    
    /**
     *
     */
    public static void ruleConclusionTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_rule_conclusion`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_rule_conclusion` (\n"
                         + " `conclusion_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `value_type_id` INTEGER NOT NULL, \n"
                         + " `conclusion_name` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        rdr.logger.Logger.info("Table (tb_rule_conclusion) created successfully");
    }
    
    /**
     *
     */
    public static void ruleStructureTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_rule_structure`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_rule_structure` (\n"
                         + " `rule_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `parentrule_id` INTEGER NOT NULL, \n"
                         + " `conclusion_id` INTEGER NOT NULL, \n"
                         + " `stopping_rule` INTEGER NOT NULL, \n"
                         + " `do_not_stack` INTEGER NOT NULL, \n"
                    
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        //Logger.info("Table (tb_rule_structure) created successfully");
      }
    
    /**
     *
     */
    public static void ruleConditionsTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_rule_conditions`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_rule_conditions` (\n"
                         + " `condition_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `rule_id` INTEGER NOT NULL, \n"
                         + " `attribute_id` INTEGER NOT NULL, \n"
                         + " `operator_id` INTEGER NOT NULL, \n"
                         + " `condition_value` VARCHAR(255) DEFAULT NULL, \n"
                         + " `creation_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        //Logger.info("Table (tb_rule_conditions) created successfully");
      }
    
    /**
     *
     */
    public static void ruleCornerstonesTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_rule_cornerstones`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_rule_cornerstones` (\n"
                         + " `rule_id` INTEGER DEFAULT NULL, \n"           
                         + " `case_id` INTEGER DEFAULT NULL, \n"              
                         + " `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        //Logger.info("Table (tb_rule_cornerstones) created successfully");
      }
    
    /**
     *
     */
    public static void ruleCornerstoneInferenceResultTableCreate(){
        
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS `tb_rule_cornerstone_inference_result`; \n" 
                         + " CREATE TABLE IF NOT EXISTS `tb_rule_cornerstone_inference_result` (\n"                    
                         + " `instance_id` INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                         + " `case_id` INTEGER DEFAULT NULL, \n"           
                         + " `rule_id` INTEGER DEFAULT NULL, \n"              
                         + " `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP \n"
                         + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
       // Logger.info("Table (tb_rule_cornerstone_inference_result) created successfully");
      }
    
    /**
     *
     * @param tableName
     * @param attributes
     * @param values
     * @param lastId
     * @return
     */
    public static synchronized int insertQuery(String tableName, String[] attributes, String[] values, boolean lastId){
        int inserted_id = 0;
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        int attributeAmount = attributes.length;
        String attrSql = "";
        String valSql = "";    
        for(int i=0; i<attributeAmount; i++) {
            if(i==0){
                attrSql += " `" + attributes[i] + "`";
                valSql += " '" + values[i] + "'";
            } else {
                attrSql += ", `" + attributes[i] + "`";
                valSql += ", '" + values[i] + "'";
            }
        }
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO `" + tableName + "` " 
                       + " ( " + attrSql + ") "
                       + "VALUES ( " + valSql + ") ";      
            if(lastId){
                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()){
                    inserted_id=rs.getInt(1);
                }
                rs.close();
            } else {
                stmt.executeUpdate(sql);
            }
            stmt.close();
            
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        return inserted_id;
      }
    
    /**
     *
     * @param tableName
     * @param columnName
     * @param value
     */
    public static synchronized void deleteQuery(String tableName, String columnName, int value){
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
       
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `" + tableName + "` " 
                       + " WHERE `" + columnName 
                       + "` = " + value; 
            
            stmt.executeUpdate(sql);
            stmt.close();
            
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
      }
    
    /**
     *
     * @param conclusionId
     * @param valueTypeId
     * @param conclusionName
     */
    public static void insertRuleConclusion(int conclusionId, int valueTypeId, String conclusionName){
        Connection c = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_conclusion` (`conclusion_id`, `value_type_id`, `conclusion_name`) VALUES (?, ?, ?)";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, conclusionId);
            pstmt.setInt(2, valueTypeId);
            pstmt.setString(3, conclusionName);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param parentId
     * @param conclusionId
     */
    public static void insertRuleStructure(int ruleId, int parentId, int conclusionId, boolean stoppingRule, boolean doNotStack){
        Connection c = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_structure` (`rule_id`, `parentrule_id`, `conclusion_id`, `stopping_rule`, `do_not_stack`) VALUES (?, ?, ?, ?,?)";
            
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, ruleId);
            pstmt.setInt(2, parentId);
            pstmt.setInt(3, conclusionId);
            if (stoppingRule)
                pstmt.setInt(4, 1);
            else
                pstmt.setInt(4, 0);
            if (doNotStack)
                pstmt.setInt(5, 1);
            else
                pstmt.setInt(5, 0);

            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param attrId
     * @param operId
     * @param conditionVal
     */
    public static void insertRuleCondition(int ruleId, int attrId, int operId, String conditionVal){
        Connection c = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_conditions`  (  `rule_id`, `attribute_id`, `operator_id`, `condition_value`) VALUES (?, ?, ?, ?)";

            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, ruleId);
            pstmt.setInt(2, attrId);
            pstmt.setInt(3, operId);
            pstmt.setString(4, conditionVal);
            

                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param caseId
     */
    public static void insertCornerstoneCases(int ruleId, int caseId){
        Connection c = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_cornerstones`  (  `rule_id`, `case_id` ) VALUES (?, ?)";

            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, ruleId);
            pstmt.setInt(2, caseId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param caseId
     */
    public static void insertRuleCornerstone(int ruleId, int caseId){
        Connection c = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_cornerstones`  (  `rule_id`, `case_id` ) VALUES (?, ?)";

            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, ruleId);
            pstmt.setInt(2, caseId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param caseId
     * @param ruleId
     */
    public static void insertRuleCornerstoneInferenceResult(int caseId, int ruleId){
        Connection c = DBConnection.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_cornerstone_inference_result`  ( `case_id`, `rule_id` )  \n"
                        + " VALUES (?, ?) \n";
         
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, caseId);
            pstmt.setInt(2, ruleId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            rdr.logger.Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @return
     */
    public static HashMap<String,String> getDomainDetails(){
        HashMap<String, String> domainDetails = new HashMap<>();
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM tb_domain_details;" );
            
            while ( rs.next() ) {
                String domainName = rs.getString(1);
                String domainDesc = rs.getString(2);
                String domainReasoner = rs.getString(3);
                domainDetails.put("domainName", domainName);
                domainDetails.put("domainDesc", domainDesc);
                domainDetails.put("domainReasoner", domainReasoner);
            }
            
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE10" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return domainDetails;
    }
    
    /**
     *
     * @return
     */
    public static CaseStructure getCaseStructure(){
        CaseStructure caseStructure = new CaseStructure();
        Connection c = DBConnection.getConnection();
        Statement stmt = null;
        Statement stmt2 = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM tb_case_structure;" );
            //Logger.info("Case structure loading...");
            while ( rs.next() ) {
                int attributeId = rs.getInt(1);
                int valueTypeId = rs.getInt(2);
                String  attrName = rs.getString(3);
//                System.out.println(attributeId + "," + valueTypeId + "," + attrName );
                
                IAttribute attr = AttributeFactory.createAttribute(valueTypeId);
                attr.setAttributeId(attributeId);
                attr.setAttributeType(Attribute.CASE_TYPE);
                attr.setName(attrName);
                attr.setValueType(new ValueType(valueTypeId));
                if(attr.isThisType("CATEGORICAL")){
                    stmt2 = c.createStatement();
                    ResultSet rs2 = stmt2.executeQuery( "SELECT * FROM tb_categorical_value WHERE attribute_id = " + attributeId + ";" );
                    while (rs2.next()){
                        String catVal = rs2.getString(3);
                        attr.addCategoricalValue(catVal);
                    }
                }
                caseStructure.addAttribute(attr);
                
                //Logger.info("Case structure atrribute added: " + attr.toString());
            }
            
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( "DAVE11" + e.getClass().getName() + ": " + e.getMessage() );
        }
        return caseStructure;
    }
    
    /**
     *
     * @param conditionHashMap
     * @param conclusionSet
     * @return
     */
    public static RuleSet getRuleStructureSet(HashMap<Integer, ConditionSet> conditionHashMap, ConclusionSet conclusionSet) {
        RuleSet kb = new RuleSet();
        
        Connection c = DBConnection.getConnection();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            rs = stmt.executeQuery( "SELECT * FROM `tb_rule_structure` ORDER BY rule_id ASC ");
            //Logger.info("Rule loading...");
            while ( rs.next() ) {
                Rule aRule = new Rule();
                
                int ruleId = rs.getInt(1);
                int parentId = rs.getInt(2);                
                int conclusionId = rs.getInt(3);
                int stoppingRule = rs.getInt(4);
                int doNotStack = rs.getInt(5);
                
                if(ruleId==0){
                    if(conclusionId==0){
                        Conclusion aConclusion = conclusionSet.getConclusionById(conclusionId);
                        aRule.setRuleId(ruleId);
                        aRule.setConclusion(aConclusion);
                    } else {
                        aRule = RuleBuilder.buildRootRule();
                    }
                    kb.addRule(aRule);
                    kb.setRootRule(aRule);      
                    
                } else {     
                    Conclusion aConclusion = conclusionSet.getConclusionById(conclusionId);
                    aRule.setRuleId(ruleId);
                    aRule.setConclusion(aConclusion);
                    aRule.setParent(kb.getRuleById(parentId)); 
                    if (stoppingRule == 0)
                        aRule.setIsStoppingRule(false);
                    else
                        aRule.setIsStoppingRule(true);
                    if (doNotStack == 0)
                        aRule.setDoNotStack(false);
                    else
                        aRule.setDoNotStack(true);
                    
                    kb.getRuleById(parentId).addChildRule(aRule);
                    ConditionSet aConditionSet = new ConditionSet();
                    aConditionSet = conditionHashMap.get(ruleId);
                    aRule.setConditionSet(aConditionSet);
                    kb.addRule(aRule);
                }
                //Logger.info("Rule loaded: " + aRule.toString());
                
            }
        } catch ( Exception e ) {
            System.err.println( "DAVE12" + e.getClass().getName() + ": " + e.getMessage() );
        } finally {
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }
        return kb;
    }
    
    /**
     *
     * @return
     */
    public static HashMap<Integer, ArrayList<Integer>> getCornerstoneCaseIdsHashMap() {
        HashMap<Integer, ArrayList<Integer>> caseIdHashMap = new HashMap<>();
        Connection c = DBConnection.getConnection();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            
            rs = stmt.executeQuery( "SELECT * FROM tb_rule_cornerstones " );
           // Logger.info("Rule cornerstone cases loading...");
            while(rs.next()) {
                int ruleId = rs.getInt(1);
                int caseId = rs.getInt(2);           
                if(caseIdHashMap.containsKey(ruleId)){
                    caseIdHashMap.get(ruleId).add(caseId);
                } else {
                    ArrayList<Integer> caseIdList = new ArrayList();
                    caseIdList.add(caseId);
                    caseIdHashMap.put(ruleId, caseIdList);
                }
            }
        } catch ( Exception e ) {
            System.err.println( "DAVE13" + e.getClass().getName() + ": " + e.getMessage() );
        } finally {
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }
        return caseIdHashMap;
    }
    
    /**
     *
     * @return
     */
    public static HashMap<Integer, ArrayList<Integer>> getCornerstoneCaseInferenceResultHashMap() {
        HashMap<Integer, ArrayList<Integer>> inferenceResultHashMap = new HashMap<>();
        Connection c = DBConnection.getConnection();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            
            rs = stmt.executeQuery( "SELECT * FROM `tb_rule_cornerstone_inference_result` " );
            //Logger.info("Rule cornerstone cases loading...");
            while(rs.next()) {
                int caseId = rs.getInt(2);
                int ruleId = rs.getInt(3);           
                if(inferenceResultHashMap.containsKey(caseId)){
                    inferenceResultHashMap.get(caseId).add(ruleId);
                } else {
                    ArrayList<Integer> ruleIdList = new ArrayList();
                    ruleIdList.add(ruleId);
                    inferenceResultHashMap.put(caseId, ruleIdList);
                }
            }
        } catch ( Exception e ) {
            System.err.println( "DAVE14" + e.getClass().getName() + ": " + e.getMessage() );
        } finally {
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }
        return inferenceResultHashMap;
    }
    
    /**
     *
     * @return
     */
    public static HashMap<Integer, ConditionSet> getConditionHashMap() {
        HashMap<Integer, ConditionSet> conditionHashMap = new HashMap<>();
        Connection c = DBConnection.getConnection();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            
            rs = stmt.executeQuery( "SELECT * FROM tb_rule_conditions " );
            //Logger.info("Rule conditions loading...");
            while(rs.next()) {
                int conditionId = rs.getInt(1);
                int ruleId = rs.getInt(2);
                int attributeId = rs.getInt(3);
                int operatorId = rs.getInt(4);                
                String conditionValue = rs.getString(5);
//                System.out.println( conditionId + "," + ruleId + "," + attributeId + "," + operatorId + "," + conditionValue);                
                IAttribute attr = Main.domain.getCaseStructure().getAttributeByAttrId(attributeId);      

                Condition aCondition = new Condition(attr, operatorId, new Value(Main.domain.getCaseStructure().getAttributeByAttrId(attributeId).getValueType(), conditionValue));
//                Logger.info("Condition loaded: " + aCondition.toString());
                if(conditionHashMap.containsKey(ruleId)){
                    conditionHashMap.get(ruleId).addCondition(aCondition);
                } else {
                    ConditionSet tempConditionSet = new ConditionSet();
                    tempConditionSet.addCondition(aCondition);
                    conditionHashMap.put(ruleId, tempConditionSet);
                }
            }
        } catch ( Exception e ) {
            System.err.println( "DAVE15" + e.getClass().getName() + ": " + e.getMessage() );
        } finally {
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }
        return conditionHashMap;
    }
    
    /**
     *
     * @return
     */
    public static ConclusionSet getConclusionSet() {
        ConclusionSet conclusionSet = new ConclusionSet();    
        Connection c = DBConnection.getConnection();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            
            rs = stmt.executeQuery( "SELECT * FROM tb_rule_conclusion;" );
            //Logger.info("Rule conclusions loading...");
            while ( rs.next() ) {
                
                int conclusionId = rs.getInt(1);
                int valueTypeId = rs.getInt(2);
                String conclusionName = rs.getString(3);
//                System.out.println(conclusionId + "," + valueTypeId + "," + conclusionName );
                
                Conclusion aConclusion = new Conclusion();
                aConclusion.setConclusionId(conclusionId);
                aConclusion.setConclusionValue(new Value(valueTypeId, conclusionName));
                
//                Logger.info("Conclusion loaded: " + aConclusion.toString());
                
                conclusionSet.addConclusion(aConclusion);
            }
        } catch ( Exception e ) {
            System.err.println( "DAVE16" + e.getClass().getName() + ": " + e.getMessage() );
        } finally {
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }
        return conclusionSet;
    }
 
}

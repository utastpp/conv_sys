/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import rdr.apps.Main;
import rdr.cases.CaseStructure;
import rdr.logger.Logger;
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
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class SqliteBasicOperation {
    private static Connection c; 
    
    /**
     *
     * @param sql
     */
    public static synchronized void executeQuery(String sql){
        
        c = SqliteConnection.connection;
        
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Query (" + sql + ") executed successfully");
    }    
    
    /**
     *
     */
    public static void domainDetailTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (tb_domain_details) created successfully");
    }
    
    /**
     *
     */
    public static void caseStructureTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (tb_case_structure) created successfully");
    }
    
    /**
     *
     */
    public static void categoricalValuesTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (tb_categorical_value) created successfully");
    }
    
    /**
     *
     */
    public static void ruleConclusionTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        Logger.info("Table (tb_rule_conclusion) created successfully");
    }
    
    /**
     *
     */
    public static void ruleStructureTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        //Logger.info("Table (tb_rule_structure) created successfully");
      }
    
    /**
     *
     */
    public static void ruleConditionsTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        //Logger.info("Table (tb_rule_conditions) created successfully");
      }
    
    /**
     *
     */
    public static void ruleCornerstonesTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
        //Logger.info("Table (tb_rule_cornerstones) created successfully");
      }
    
    /**
     *
     */
    public static void ruleCornerstoneInferenceResultTableCreate(){
        
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
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
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
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
        c = SqliteConnection.connection;
        Statement stmt = null;
       
        try {
            stmt = c.createStatement();
            String sql = "DELETE FROM `" + tableName + "` " 
                       + " WHERE `" + columnName 
                       + "` = " + value; 
            
            stmt.executeUpdate(sql);
            stmt.close();
            
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
      }
    
    /**
     *
     * @param conclusionId
     * @param valueTypeId
     * @param conclusionName
     */
    public static void insertRuleConclusion(int conclusionId, int valueTypeId, String conclusionName){
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param parentId
     * @param conclusionId
     */
    public static void insertRuleStructure(int ruleId, int parentId, int conclusionId, boolean stoppingRule, boolean doNotStack){
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
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
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param caseId
     */
    public static void insertCornerstoneCases(int ruleId, int caseId){
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_cornerstones`  (  `rule_id`, `case_id` ) VALUES (?, ?)";

            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, ruleId);
            pstmt.setInt(2, caseId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param ruleId
     * @param caseId
     */
    public static void insertRuleCornerstone(int ruleId, int caseId){
        c = SqliteConnection.connection;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO `tb_rule_cornerstones`  (  `rule_id`, `case_id` ) VALUES (?, ?)";

            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, ruleId);
            pstmt.setInt(2, caseId);
                
            pstmt.execute();
            
            pstmt.close();
        } catch ( Exception e ) {
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @param caseId
     * @param ruleId
     */
    public static void insertRuleCornerstoneInferenceResult(int caseId, int ruleId){
        c = SqliteConnection.connection;
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
            Logger.error( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     *
     * @return
     */
    public static HashMap<String,String> getDomainDetails(){
        HashMap<String, String> domainDetails = new HashMap<>();
        c = SqliteConnection.connection;
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
        c = SqliteConnection.connection;
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
        
        c = SqliteConnection.connection;
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
        c = SqliteConnection.connection;
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
        c = SqliteConnection.connection;
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
        c = SqliteConnection.connection;
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
        c = SqliteConnection.connection;
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
    


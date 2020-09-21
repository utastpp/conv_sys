/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import rdr.apps.Main;
import rdr.cases.Case;
import rdr.cases.CornerstoneCase;
import rdr.cases.CornerstoneCaseSet;
import rdr.logger.Logger;
import cmcrdr.mysql.DBOperation;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class RuleLoader {
    private static CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
    
    /**
     *
     */
    public static void deleteDefaultRule(){
        DBOperation.deleteQuery("tb_rule_structure", "rule_id", 0);
    }

    /**
     *
     * @param newRuleId
     * @param newRule
     * @param conclusionId
     */
    public static void insertRule(int newRuleId, Rule newRule, int conclusionId){
        insertRuleStructure(newRule, conclusionId);
        insertRuleConditions(newRuleId, newRule);
        insertRuleCornerstones(newRuleId, newRule);        
        
        Logger.info("Rule " + newRule.toString() + " added.");

    }
    
    /**
     *
     * @param newConclusionId
     * @param newConclusion
     */
    public static void insertRuleConclusions(int newConclusionId, Conclusion newConclusion){
//        attributes[0] = "conclusion_id";
//        attributes[1] = "value_type_id";
//        attributes[2] = "conclusion_name";
        
        DBOperation.insertRuleConclusion(newConclusionId, newConclusion.getConclusionValue().getValueType().getTypeCode(), newConclusion.getConclusionName());
        
    }
    
    /**
     *
     * @param newRule
     * @param conclusionId
     */
    public static void insertRuleStructure(Rule newRule, int conclusionId){
//        attributes[0] = "rule_id";
//        attributes[1] = "parentrule_id";
//        attributes[2] = "conclusion_id";
         
        int parentId = -1;
        if(newRule.getRuleId()==0){
            parentId = -1;
        } else {
            parentId = newRule.getParent().getRuleId();
        }
        
        DBOperation.insertRuleStructure(newRule.getRuleId(), parentId, conclusionId, newRule.getIsStoppingRule(), newRule.getDoNotStack());
    }
    
    /**
     *
     * @param newRuleId
     * @param newRule
     */
    public static void insertRuleConditions(int newRuleId, Rule newRule){
//        attributes[0] = "rule_id";
//        attributes[1] = "attribute_id";
//        attributes[2] = "operator_id";
//        attributes[3] = "condition_value";
        
        for (Condition aCondition : newRule.getConditionSet().getBase()) {
            
            DBOperation.insertRuleCondition(newRuleId, aCondition.getAttribute().getAttributeId(), aCondition.getOperator().getOperator(), aCondition.getValue().toString());
        }
    }
        
    /**
     *
     * @param newRuleId
     * @param newRule
     */
    public static void insertRuleCornerstones(int newRuleId, Rule newRule){
//        attributes[0] = "rule_id";
//        attributes[1] = "case_id";
        
        Set cases = newRule.getCornerstoneCaseSet().getBase().entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();

        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            int caseId = (int) me.getKey();
            CornerstoneCase aCase = (CornerstoneCase) me.getValue();
            
            DBOperation.insertRuleCornerstone(newRuleId, caseId);
           
            RuleSet inferenceResult = aCase.getWrongRuleSet();
            
            Set rules = inferenceResult.getBase().entrySet();
            // Get an iterator
            Iterator ruleIterator = rules.iterator();

            while (ruleIterator.hasNext()) {
                Map.Entry me2 = (Map.Entry) ruleIterator.next();
                Rule aRule = (Rule) me2.getValue();
                
                DBOperation.insertRuleCornerstoneInferenceResult(caseId, aRule.getRuleId());            
            }
        }
    }
        
    /**
     *
     */
    public static void setRules(){
        HashMap<Integer, ConditionSet> conditionHashMap = DBOperation.getConditionHashMap();
        ConclusionSet conclusionSet = DBOperation.getConclusionSet();
        Main.KB = DBOperation.getRuleStructureSet(conditionHashMap,  conclusionSet);
        Main.KB.setRootRuleTree();
        cornerstoneCaseSet = RuleLoader.getCornerstoneCaseSet();
        RuleLoader.setCornerstoneCase();
    }
    
    private static CornerstoneCaseSet getCornerstoneCaseSet() {
        //case id , list of rule ids
        HashMap<Integer, ArrayList<Integer>> cornerstoneCaseInferenceResultsHashMap = DBOperation.getCornerstoneCaseInferenceResultHashMap();
      
        Set cases = cornerstoneCaseInferenceResultsHashMap.entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();

        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            int caseId = (int) me.getKey();
            ArrayList<Integer> ruleIds = (ArrayList<Integer>) me.getValue();
            CornerstoneCase aCornerstoneCase = new CornerstoneCase(Main.allCaseSet.getCaseById(caseId));
            for(int i=0; i<ruleIds.size(); i++){
                Rule aRule = Main.KB.getRuleById(ruleIds.get(i));
                if(aRule.isParentExist()){
                    aCornerstoneCase.replaceRuleWithExistingWrongRule(aRule.getParent(), aRule);
                } else {
                    aCornerstoneCase.addRuleToWrongRuleSet(aRule);                    
                }
            }
            cornerstoneCaseSet.addCornerstoneCase(aCornerstoneCase);
        }
        return cornerstoneCaseSet;
    }   
    
    private static void setCornerstoneCase(){
        //rule id , list of case ids
        HashMap<Integer, ArrayList<Integer>> cornerstoneCaseIdsHashMap = DBOperation.getCornerstoneCaseIdsHashMap();

        Set rules = Main.KB.getBase().entrySet();
        // Get an iterator
        Iterator ruleIterator = rules.iterator();

        while (ruleIterator.hasNext()) {
            Map.Entry me = (Map.Entry) ruleIterator.next();
            int ruleId = (int) me.getKey();
            Rule aRule = (Rule) me.getValue();

            if(cornerstoneCaseIdsHashMap.containsKey(ruleId)){
                ArrayList<Integer> caseIdList = cornerstoneCaseIdsHashMap.get(ruleId);
                for(int i=0; i<caseIdList.size(); i++){
                    int caseId = caseIdList.get(i);
                    if(cornerstoneCaseSet.isCaseIdExist(caseId)){
                        CornerstoneCase aCase = (CornerstoneCase) cornerstoneCaseSet.getCornerstoneCaseById(caseId);                    
                        aRule.addCornerstoneCase(aCase);

                    } else {
                        Case aCase = Main.allCaseSet.getCaseById(caseId);
                        //Logger.info("Trying to add case:" + caseId + " for rule:" + ruleId);
                        aRule.addCornerstoneCase(new CornerstoneCase (aCase));
                    }
                }
            }
        }
    }
}

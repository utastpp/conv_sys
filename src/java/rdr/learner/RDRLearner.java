/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.learner;

import rdr.workbench.Workbench;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rdr.cases.Case;
import rdr.cases.CaseSet;
import rdr.rules.Conclusion;

import rdr.rules.Condition;
import rdr.rules.Rule;
import rdr.logger.Logger;
import rdr.model.Value;
import rdr.rules.ConclusionSet;

/**
 *
 * @author yangsokk
 * @author (modified by) David Chung
 */
public class RDRLearner {
    /**
     * This 
     */
    public static final int CONTYPE_WRONG_RULE = 1; 

    /**
     *
     */
    public static final int CONTYPE_CHILD_OF_WRONG_RULE = 2;

    /**
     *
     */
    public static final int CONTYPE_ALT_RULE = 3;

    /**
     *
     */
    public static final int CONTYPE_CHILD_OF_ALT_RULE = 4;
    
    /**
     * Working memory
     */
    protected Workbench workbench = new Workbench();

    /**
     * Wrong conclusion
     */
    protected CaseSet wrongConclusioSet;

    /**
     * Constructor
     */
    public RDRLearner() {
        this.workbench = new Workbench();
        this.wrongConclusioSet = null;
    }

    /**
     * Constructor
     * @param workbench 
     */
    public RDRLearner(Workbench workbench) {
        this.workbench = workbench;
    }

    /**
     * Constructor
     * @param workbench
     * @param set 
     */
    public RDRLearner(Workbench workbench, CaseSet set) {
        this.workbench = workbench;
        this.wrongConclusioSet = set;
    }

    /**
     * Set new rule
     * @param rule 
     */
    public void setNewRule(Rule rule) {
        this.workbench.setNewRule(rule);
    }

    /**
     * Get new rule
     * @return 
     */
    public Rule getNewRule() {
        return this.workbench.getNewRule();
    }

    /**
     * Find cornerstone sets related to the new rule.
     */
//    public void findCornerstones() {
//        Set inferencedResultSet = this.workbench.getInferenceResult().getBaseSet().entrySet();
//        Iterator iterator1 = inferencedResultSet.iterator();
//        while (iterator1.hasNext()) {
//            Map.Entry me = (Map.Entry) iterator1.next();
//            Rule inferencedRule = (Rule) me.getValue();
//            
//            CaseSet cornerstoneCaseSet2 = inferencedRule.getCornerstoneCaseSet();
//            Set cornerstoneCaseSetBase2 = cornerstoneCaseSet2.getCaseSetBase().entrySet();
//            Iterator iterator2 = cornerstoneCaseSetBase2.iterator();
//            while (iterator2.hasNext()) {
//                Map.Entry me2 = (Map.Entry) iterator2.next();
//                Case caseInstance2 = (Case) me2.getValue();
//                if (inferencedRule.getConclusion().equals(this.getWrongConclusionSet())) {
//                    //Collect cornerstone cases of wrong rule
//                    caseInstance2.setConerstoneCaseType(RDRLearner.CONTYPE_WRONG_RULE);
//                    this.workbench.addCornerstoneCase(caseInstance2);
//                    //Collect cornerstone cases of wrong rule's child rule 
//                    HashMap<Integer, Rule> childRules
//                            = this.workbench.getRuleSet().getChildDecisionListRules(inferencedRule);
//                    if (!childRules.isEmpty()) {
//                        Set set3 = childRules.entrySet();
//                        Iterator iterator3 = set3.iterator();
//                        while (iterator3.hasNext()) {
//                            Map.Entry me3 = (Map.Entry) iterator3.next();
//                            Rule rule3 = (Rule) me.getValue();
//                            CaseSet cornerstoneCaseSet4 = rule3.getCornerstoneCaseSet();
//                            Set cornerstoneCaseSetBase4 = cornerstoneCaseSet4.getCaseSetBase().entrySet();
//                            Iterator iterator4 = cornerstoneCaseSetBase4.iterator();
//                            while (iterator4.hasNext()) {
//                                Map.Entry me4 = (Map.Entry) iterator4.next();
//                                Case caseInstance4 = (Case) me4.getValue();
//                                caseInstance4.setConerstoneCaseType(RDRLearner.CONTYPE_CHILD_OF_WRONG_RULE);
//                                this.workbench.addCornerstoneCase(caseInstance4);
//                            }
//                        }
//                    }
//                } else {
//                    //Collect cornerstone cases of alternative rules
//                    caseInstance2.setConerstoneCaseType(RDRLearner.CONTYPE_ALT_RULE);
//                    this.workbench.addCornerstoneCase(caseInstance2);
//                    //Collect cornerstone cases of alternative rules's child rules
//                    HashMap<Integer, Rule> childRules
//                            = this.workbench.getRuleSet().getChildDecisionListRules(inferencedRule);
//                    if (!childRules.isEmpty()) {
//                        Set set3 = childRules.entrySet();
//                        Iterator iterator3 = set3.iterator();
//                        while (iterator3.hasNext()) {
//                            Map.Entry me3 = (Map.Entry) iterator3.next();
//                            Rule rule3 = (Rule) me.getValue();
//                            CaseSet cornerstoneCaseSet4 = rule3.getCornerstoneCaseSet();
//                            Set cornerstoneCaseSetBase4 = cornerstoneCaseSet4.getCaseSetBase().entrySet();
//                            Iterator iterator4 = cornerstoneCaseSetBase4.iterator();
//                            while (iterator4.hasNext()) {
//                                Map.Entry me4 = (Map.Entry) iterator4.next();
//                                Case caseInstance4 = (Case) me4.getValue();
//                                caseInstance4.setConerstoneCaseType(RDRLearner.CONTYPE_CHILD_OF_ALT_RULE);
//                                this.workbench.addCornerstoneCase(caseInstance4);
//                            }
//
//                        }
//                    }
//                }           
//            }
//        }
//    }

    /**
     * Find different attributes of the current case by comparing cornerstone cases
     * of the wrong rule.
     */
//    public void findDifferenceAttributes() {
//        //String attributeName, attribute
//        HashMap<String, Value> current = this.workbench.getCurrentCase().getValues();
//        Set set1 = current.entrySet();
//        Iterator iterator1 = set1.iterator();
//        while (iterator1.hasNext()) {
//            Map.Entry me = (Map.Entry) iterator1.next();
//            String attributeName = (String) me.getKey();
//            Value currentCaseVlaue = (Value) me.getValue();
//            Set set2 = this.workbench.getCornerstoneCaseSet().getCaseSetBase().entrySet();
//            Iterator iterator2 = set2.iterator();
//            boolean isSame = false;
//            HashMap<Integer, Value> comparingCaseVlaues = new HashMap<>();
//            while (iterator2.hasNext()) {
//                Map.Entry me2 = (Map.Entry) iterator2.next();
//                //Get a conerstone case
//                Case comparingCase = (Case) me2.getValue();
//                if (comparingCase.getCornerstoneCaseType() == RDRLearner.CONTYPE_WRONG_RULE) {
//                    Value comparingCaseVlaue = comparingCase.getValues().get(attributeName);
//                    if (currentCaseVlaue.equals(comparingCaseVlaue)) {
//                        isSame = true;
//                        break;
//                    } else {
//                        comparingCaseVlaues.put(comparingCase.getCaseId(), currentCaseVlaue);
//                    }
//                }
//            }
//
//            if (!isSame) {
//                addDifferentAttribute(attributeName, currentCaseVlaue);
//            }
//        }
//    }

    /**
     * Add an attribute value
     *
     * @param attributeName
     * @param currentCaseVlaue
     * @return
     */
//    protected boolean addDifferentAttribute(String attributeName, Value currentCaseVlaue) {
//        if (this.workbench.getDifferenceList().containsKey(attributeName)) {
//            Logger.error("Cannot add this case, since it is already in the differenc list");
//            return false;
//        } else {
//            this.workbench.addDifferenceList(attributeName, currentCaseVlaue);
//        }
//        return true;
//    }

    /**
     * Set conclusion to new rule
     * @param conclusion 
     */
    public void setConclusionToNewRule(Conclusion conclusion) {
        this.workbench.getNewRule().setConclusion(conclusion);
    }

    /**
     * Delete conclusion from new rule
     */
    public void deleteConclusionFromNewRule() {
        this.workbench.getNewRule().deleteConclusion();
    }
    
    /**
     * Add condition to new rule
     * @param condition 
     */
    public void addCondtionToNewRule(Condition condition) {
        this.workbench.getNewRule().getConditionSet().addCondition(condition);
    }

    /**
     * Delete condition from new rule
     * @param condition 
     */
    public void deleteConditionFromNewRule(Condition condition) {
        this.workbench.getNewRule().getConditionSet().deleteCondition(condition);
    }

    /**
     * Get wrong conclusion set
     * @return 
     */
    public CaseSet getWrongConclusionSet() {
        return this.wrongConclusioSet;
    }

    /**
     * Set wrong conclusion set
     * @param conclusionSet 
     */
    public void setWrongConclusionSet(CaseSet conclusionSet) {
        this.wrongConclusioSet = conclusionSet;
    }

//    /**
//     * Add new rule to rule set
//     */
//    public void addNewRule() {
//        //For each conclusion change HashMap<Integer, Rule>
//        Set set1 = workbench.getInferenceResult().getBaseSet().entrySet();
//        Iterator caseIterator = set1.iterator();
//        while (caseIterator.hasNext()) {
//            Map.Entry me1 = (Map.Entry) caseIterator.next();
//            Rule rule = (Rule) me1.getValue();
//
//            //Set the inference result as parent rule
//            workbench.getNewRule().setParent(rule);
//
//            //Register new rule
//            int newRuleId = getNewRuleId();
//            workbench.getNewRule().setRuleId(newRuleId);
//
//            //Register new rule
//            workbench.getRuleSet().addRule(workbench.getNewRule());
//
//            //Upadate rule links
//            
//            if (wrongConclusioSet == null) {
//                throw new IllegalArgumentException("argument is null");
//            }
//            
//            if (rule.getConclusion().equals(wrongConclusioSet)) {
//               
//            }
//        }
//
//    }

    /**
     * Get new rule id
     *
     * @return
     */
    private int getNewRuleId() {
        int newRuleId = 0;
        //HashMap<Integer, Rule>
        if (workbench.getRuleSet().getBase().isEmpty()) {
            newRuleId = 1;
        } else {
            Set set1 = workbench.getRuleSet().getBase().entrySet();
            Iterator caseIterator = set1.iterator();
            while (caseIterator.hasNext()) {
                Map.Entry me1 = (Map.Entry) caseIterator.next();
                if ((int) me1.getKey() > newRuleId) {
                    newRuleId = (int) me1.getKey();
                }
            }
            newRuleId = newRuleId + 1;
        }
        return newRuleId;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.learner;

import java.util.HashMap;
import rdr.cases.Case;
import rdr.cases.CaseSet;
import rdr.cases.CornerstoneCase;
import rdr.cases.CornerstoneCaseSet;
import rdr.rules.Conclusion;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleSet;

/**
 * This interface is for knowledge acquisition process
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public interface ILearner {

    /**
     *
     * @param aCase
     */
    public void setCurrentCornerstoneCaseByCase(Case aCase);

    /**
     *
     * @param aCase
     */
    public void setCurrentCornerstoneCase(CornerstoneCase aCase);

    /**
     *
     * @return
     */
    public CornerstoneCase getCurrentCornerstoneCase();

    /**
     *
     * @param ruleSet
     */
    public void setRuleSet(RuleSet ruleSet);

    /**
     *
     * @return
     */
    public RuleSet getRuleSet();

    /**
     *
     * @return
     */
    public HashMap<String, DiffElement> getFullDifferenceList();

    /**
     *
     * @return
     */
    public HashMap<String, DiffElement> getPartialDifferenceList();

    /**
     *
     * @param rule
     */
    public void setInferenceResult(Rule rule);

    /**
     *
     * @param ruleSet
     */
    public void setInferenceResult(RuleSet ruleSet);

    /**
     *
     * @return
     */
    public Object getInferenceResult();

    /**
     *
     * @param wrongConclusion
     */
    public void setWrongConclusion(Conclusion wrongConclusion);

    /**
     *
     */
    public void deleteWrongConclusion();

    /**
     *
     * @return
     */
    public Conclusion getWrongConclusion();

    /**
     *
     * @return
     */
    public boolean isWrongConclusionExist();

    /**
     *
     * @param wrongRule
     */
    public void setWrongRule(Rule wrongRule);

    /**
     *
     * @return
     */
    public Rule getWrongRule();

    /**
     *
     * @param wrongRuleSet
     */
    public void setWrongRuleSet(RuleSet wrongRuleSet);

    /**
     *
     * @return
     */
    public RuleSet getWrongRuleSet();    

    /**
     *
     * @param rule
     */
    public void setNewRule(Rule rule);

    /**
     *
     * @return
     */
    public Rule getNewRule();

    /**
     *
     * @param condition
     * @return
     */
    public boolean addConditionToNewRule(Condition condition);

    /**
     *
     * @param conditionSet
     */
    public void setConditionSetToNewRule(ConditionSet conditionSet);

    /**
     *
     * @param condition
     * @return
     */
    public boolean deleteConditionFromNewRule(Condition condition);

    /**
     *
     */
    public void deleteAllConditionFromNewRule();

    /**
     *
     * @return
     */
    public ConditionSet getConditionSetFromNewRule();

    /**
     *
     * @return
     */
    public int getConditionAmountFromNewRule();

    /**
     *
     * @param conclusion
     */
    public void setConclusionToNewRule(Conclusion conclusion);

    /**
     *
     * @return
     */
    public Conclusion getConclusionFromNewRule();

    /**
     *
     * @return
     */
    public String getNewRuleType();

    /**
     *
     * @return
     */
    public boolean addNewRule(boolean doNotStack);

    /**
     *
     * @return
     */
    public boolean addAlternativeRule(boolean doNotStack);

    /**
     *
     * @return
     */
    public boolean addExceptionRule(boolean doNotStack);

    /**
     *
     * @return
     */
    public boolean addTempNewRule();

    /**
     *
     * @return
     */
    public boolean addTempAlternativeRule();

    /**
     *
     * @return
     */
    public boolean addTempExceptionRule();

    /**
     *
     * @param newCase
     * @return
     */
    public boolean addCornerstoneCase(Case newCase);

    /**
     *
     * @param newCase
     * @return
     */
    public boolean deleteCornerstoneCase(Case newCase);

    /**
     *
     */
    public void retrieveValidatingCaseSet();

    /**
     *
     * @return
     */
    public CornerstoneCaseSet getValidatingCaseSet();

    /**
     *
     */
    public void generateFullDifferenceList();

    /**
     *
     * @param subsetOfValidatingCaseSet
     */
    public void generatePartialDifferenceList(HashMap<Integer, Case> subsetOfValidatingCaseSet);    

    /**
     *
     */
    public void executeAddingRule(boolean doNotStack);
    
    public void executeAddingStopRule();

    /**
     *
     */
    public void executeAddingRuleForValidation();

    

}

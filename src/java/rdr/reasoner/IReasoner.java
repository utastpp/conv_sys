/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.reasoner;

import rdr.cases.Case;
import rdr.rules.Rule;
import rdr.rules.RuleSet;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public interface IReasoner {

    /**
     *
     * @return
     */
    public String getReasonerMethod();

    /**
     *
     * @return
     */
    public Case getCurrentCase();

    /**
     *
     * @param currentCase
     */
    public void setCurrentCase(Case currentCase);

    /**
     *
     * @return
     */
    public RuleSet getRuleSet();

    /**
     *
     * @param ruleSet
     */
    public void setRuleSet(RuleSet ruleSet);

    /**
     *
     * @return
     */
    public Rule getStartingRule();

    /**
     *
     * @param aRule
     */
    public void setStartingRule(Rule aRule);

    /**
     *
     */
    public void clearStartingRule();

    /**
     *
     * @param rule
     * @return
     */
    public Object inference(Rule rule);

    /**
     *
     * @param rule
     * @return
     */
    public Object inferenceWithStartingRule(Rule rule);

    /**
     *
     * @param rule
     * @return
     */
    public boolean addRuleToFiredRules(Rule rule);

    /**
     *
     * @param firedRules
     */
    public void setFiredRules(RuleSet firedRules);

    /**
     *
     * @return
     */
    public RuleSet getFiredRules();

    /**
     *
     */
    public void clearFiredRules();

    /**
     *
     * @param rule
     * @return
     */
    public boolean addRuleToInferenceResult(Rule rule);

    /**
     *
     * @param rule
     * @return
     */
    public boolean deleteRuleFromInferenceResult(Rule rule);

    /**
     *
     */
    public void clearInferenceResult();

    /**
     *
     * @param inferenceResult
     */
    public void setInferenceResult(Rule inferenceResult);

    /**
     *
     * @param inferenceResult
     */
    public void setInferenceResult(RuleSet inferenceResult);

    /**
     *
     * @return
     */
    public Object getInferenceResult();
}

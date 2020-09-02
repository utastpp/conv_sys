/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.reasoner;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import rdr.cases.Case;
import rdr.logger.Logger;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;
import rdr.workbench.Workbench;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */

/*
Reasoner is the inference engine.. DPH
*/
public class Reasoner implements IReasoner {
    
    /**
     * Reasoner method
     */
    protected String method;
    
    /**
     * Rule set
     */
    protected RuleSet ruleSet = null;
    
    /**
     * Current case
     */
    protected Case currentCase = null;
    
    /**
     * Inference starting rule
     */
    protected Rule startingRule = null;

    /**
     * Fired rules
     */
    protected RuleSet firedRules= new RuleSet();
    
    /**
     * Constructor. 
     */
    public Reasoner(){
        this.firedRules = new RuleSet();
    }

    /**
     * Constructor.
     * @param ruleSet
     * @param currentCase 
     */
    public Reasoner(RuleSet ruleSet, Case currentCase){
       this.firedRules = new RuleSet();
       this.ruleSet = ruleSet;
       this.currentCase = currentCase;
    }
    
    
    /**
     * Perform inference
     * @param rule 
     * @return  
     */
    @Override
    public Object inference(Rule rule) {
        return null;
    }
    
    
    /**
     * Perform inference with starting rule
     * @param rule 
     * @return  
     */
    @Override
    public Object inferenceWithStartingRule(Rule rule) {
        return null;
    }
    
    
    
    /**
     * Get current case
     * @return 
     */
    @Override
    public Case getCurrentCase() {
        return this.currentCase;
    }
    
    /**
     * Set current case
     * @param currentCase 
     */
    @Override
    public void setCurrentCase(Case currentCase){
        //clear added conclusions
        currentCase.clearConclusionSet();
        this.currentCase = currentCase;
    }
    
    /**
     * Get rule set
     * @return 
     */
    @Override
    public RuleSet getRuleSet(){
        return this.ruleSet;
    }
    
    /**
     * Set rule set
     * @param ruleSet 
     */
    @Override
    public void setRuleSet(RuleSet ruleSet){
        this.ruleSet = ruleSet;
    }
    
    /**
     * Get starting rule 
     * @return 
     */
    @Override
    public Rule getStartingRule(){
        return this.startingRule;
    }
    
    /**
     * Set starting rule 
     * @param aRule 
     */
    @Override
    public void setStartingRule(Rule aRule){
        this.startingRule = aRule;
    }
    
    /**
     * Clear starting rule 
     */
    @Override
    public void clearStartingRule(){
        this.startingRule = new Rule();
        this.startingRule.setRuleId(Rule.NULL_RULE_ID);
    }
    
    /**
     * Get inference result
     * @return 
     */
    @Override
    public Object getInferenceResult(){
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     * Set inference result for SCRDR
     * @param inferenceResult 
     */
    @Override
    public void setInferenceResult(Rule inferenceResult){
        throw new UnsupportedOperationException("Only support in SCRDR."); 
    }
    
    /**
     * Set inference result for MCRDR
     * @param inferenceResult 
     */
    @Override
    public void setInferenceResult(RuleSet inferenceResult){
        throw new UnsupportedOperationException("Only support in MCRDR."); 
    }
    
    /**
     * Clear inference result
     */
    @Override
    public void clearInferenceResult(){
        throw new UnsupportedOperationException("Only support in MCRDR."); 
    }
    
    
    /**
     * Add a rule to inference result
     *
     * @param rule
     * @return
     */
    @Override
    public boolean addRuleToInferenceResult(Rule rule) {
        throw new UnsupportedOperationException("Only support in MCRDR."); 
    }

    /**
     * Delete a rule from the fired rules list
     *
     * @param rule
     * @return
     */
    @Override
    public boolean deleteRuleFromInferenceResult(Rule rule) {
        throw new UnsupportedOperationException("Only support in MCRDR."); 
    }  
    
    
    /**
     * Add new rule to the fired rules list
     *
     * @param rule
     * @return
     */
    @Override
    public boolean addRuleToFiredRules(Rule rule) {
        
        Rule addingRule = RuleBuilder.copyRule(rule);   
        //Logger.info("copied rule is:" +  addingRule.toString());
        if(rule.isParentExist()){
            Iterator iter = this.firedRules.getBase().entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry me = (Map.Entry) iter.next();
                Rule aRule = (Rule)me.getValue();
            }
            Rule parentRule = this.firedRules.getRuleById(rule.getParent().getRuleId());
            
            parentRule.addChildRule(addingRule);            
            this.firedRules.deleteRuleByRuleId(parentRule.getRuleId());
            this.firedRules.addRule(parentRule);
        }        
        if(this.firedRules.addRule(addingRule)){
            this.firedRules.getRuleById(addingRule.getRuleId()).clearChildRuleList();
            return true;
        } else {
        }
        return false;
    }
    
    /**
     * Set fired rules
     * @param firedRules
     */
    @Override
    public void setFiredRules(RuleSet firedRules){
        this.firedRules = firedRules;
    }
    
    /**
     * Get fired rules
     * @return 
     */
    @Override
    public RuleSet getFiredRules(){
        return this.firedRules;
    }
    
    /**
     * Clear  fired rules
     */
    @Override
    public void clearFiredRules(){
        this.firedRules = new RuleSet();
    }

    /**
     *
     * @return
     */
    @Override
    public String getReasonerMethod() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.learner;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import rdr.apps.Main;
import rdr.cases.Case;
import rdr.cases.CaseSet;
import rdr.cases.CornerstoneCase;
import rdr.cases.CornerstoneCaseSet;
import rdr.rules.Conclusion;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleLoader;
import rdr.rules.RuleSet;

/**
 * This class is used to define SCRDR method knowledge acquisition process
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class SCRDRLearner extends Learner {
    
    /**
     *
     */
    protected Rule inferenceResult;
    
    /**
     * A rule set that suggest wrong conclusion
     */
    protected Rule wrongRule = new Rule(); 
    
    
    /**
     * Constructor
     */
    public SCRDRLearner() {
        super();
    }

    /**
     * Constructor
     * @param ruleSet
     * @param currentCase 
     * @param wrongConclusion
     * @param rule 
     */
    public SCRDRLearner(RuleSet ruleSet, Case currentCase, 
            Conclusion wrongConclusion, Rule rule) {
        super(ruleSet, currentCase, wrongConclusion, rule);
    }
    
    /**
     * Get new rule type
     * @return i.e. new, exception
     */
    @Override
    public String getNewRuleType() {
        String mode = "";
        if(this.isWrongConclusionExist()){
            mode = "exception";
        } else {
            mode = "new";
        }
        return mode;
    }
    
    
    /**
     * Set inference result
     * @param rule 
     */
    @Override
    public void setInferenceResult(Rule rule) {
        this.inferenceResult = rule;
    }
    
    /**
     * Get inference result
     * @return 
     */
    @Override
    public Rule getInferenceResult() {
        return this.inferenceResult;
    }
    
    /**
     * Add exception rule
     * @return 
     */
    @Override
    public boolean addExceptionRule(boolean doNotStack) {
        this.newRule.setRuleId(Main.KB.getNewRuleId());
        int conclusionId = this.newRule.getConclusion().getConclusionId();
        if(this.ruleSet.isNewConclusion(this.newRule.getConclusion())){
            conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
            RuleLoader.insertRuleConclusions(conclusionId, this.newRule.getConclusion());
            this.newRule.getConclusion().setConclusionId(conclusionId);
        }
        this.newRule.setParent(this.wrongRule);
        this.ruleSet.getRuleById(this.wrongRule.getRuleId()).addChildRule(this.newRule);
            
        RuleLoader.insertRule(this.newRule.getRuleId(), this.newRule, conclusionId);
            
        this.ruleSet.setRootRuleTree();
        
        return this.ruleSet.addRule(this.newRule);
    }   
    
    /**
     * Add exception rule temporarily
     * @return 
     */
    @Override
    public boolean addTempExceptionRule() {
        this.newRule.setRuleId(Main.KB.getNewRuleId());
        int conclusionId = this.newRule.getConclusion().getConclusionId();
        if(this.ruleSet.isNewConclusion(this.newRule.getConclusion())){
            conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
            this.newRule.getConclusion().setConclusionId(conclusionId);
        }
        this.newRule.setParent(this.wrongRule);
        this.ruleSet.getRuleById(this.wrongRule.getRuleId()).addChildRule(this.newRule);
            
        this.ruleSet.setRootRuleTree();
        
        return this.ruleSet.addRule(this.newRule);
    }   
    
    
    /**
     * Delete wrong conclusion.
     */
    @Override
    public void deleteWrongConclusion() {
        this.wrongConclusion = null;
        this.wrongRule = null;
    }
    
    
    
    /**
     * Set wrong conclusion and set wrong rule
     * @param wrongConclusion
     */
    @Override
    public void setWrongConclusion(Conclusion wrongConclusion) {
        this.wrongConclusion = wrongConclusion;    
        this.wrongRule = this.inferenceResult;
        //this.currentCornerstoneCase
    }
    
    /**
     * Set wrong rule
     * @param wrongRule
     */
    @Override
    public void setWrongRule(Rule wrongRule) {
        this.wrongRule = wrongRule;
    }

    /**
     * Get wrong conclusion 
     * @return 
     */
    @Override
    public Conclusion getWrongConclusion() {
        return this.wrongConclusion;
    }    
    
    /**
     * Retrieve validation cornerstone cases
     */
    @Override
    public void retrieveValidatingCaseSet() {
        this.validatingCaseSet = new CornerstoneCaseSet();
        if(this.wrongConclusion!=null) {
            //when refining rule...
            //Current fired rules' cornerstones
            this.validatingCaseSet.putCornerstoneCaseSet(this.wrongRule.getCornerstoneCaseSet());        
            if(this.wrongRule.isParentExist()){
                Rule parentWrongRule = this.wrongRule.getParent();
                Vector<Rule> aChildRuleList = parentWrongRule.getChildRuleList();
                for(int i=0; i < aChildRuleList.size(); i++){
                    Rule aRule = aChildRuleList.get(i);
                    this.validatingCaseSet.putCornerstoneCaseSet(aRule.getCornerstoneCaseSet());
                }       
            }
        } else {
            //when altering rule...
            if(this.inferenceResult.isParentExist()){
                Rule parentWrongRule = this.inferenceResult.getParent();
                Vector<Rule> aChildRuleList = parentWrongRule.getChildRuleList();
                for(int i=0; i < aChildRuleList.size(); i++){
                    Rule aRule = aChildRuleList.get(i);
                    this.validatingCaseSet.putCornerstoneCaseSet(aRule.getCornerstoneCaseSet());
                }       
            }
        }
    }          
    
    /**
     *
     */
    @Override
    public void executeAddingRule(boolean doNotStack){
        switch (this.getNewRuleType()) {
            case "new":
                this.addNewRule(doNotStack);
                break;
            case "exception":
                this.addExceptionRule(doNotStack);
                break;
        }        
        this.getRuleSet().setRootRuleTree();
    }
    
    /**
     *
     */
    @Override
    public void executeAddingRuleForValidation(){
        switch (this.getNewRuleType()) {
            case "new":
                this.addTempNewRule();
                break;
            case "exception":
                this.addTempExceptionRule();
                break;
        }        
        this.getRuleSet().setRootRuleTree();
    }
}

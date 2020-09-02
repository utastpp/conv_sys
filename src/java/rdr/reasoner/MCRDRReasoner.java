/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.reasoner;

import rdr.cases.Case;
import rdr.logger.Logger;
import rdr.rules.Rule;
import rdr.rules.RuleSet;


public class MCRDRReasoner extends Reasoner{
    
    private RuleSet inferenceResult;
    
    
    /**
     * Constructor
     */
    public MCRDRReasoner() {
        super();
        this.inferenceResult = new RuleSet();
    }
    
    /**
     * Constructor 
     * @param ruleSet
     * @param currentCase
     */
    public MCRDRReasoner(RuleSet ruleSet,Case currentCase){
        super(ruleSet, currentCase);
        this.inferenceResult = new RuleSet();
    } 

    /**
     *
     * @return
     */
    @Override
    public String getReasonerMethod() {
        return "MCRDR";
    }
    
    /**
     * Set inference result
     * @param inferenceResult 
     */
    @Override
    public void setInferenceResult(RuleSet inferenceResult){
        this.inferenceResult = inferenceResult;
    }
    
    /**
     * Clear inference result
     */
    @Override
    public void clearInferenceResult(){
        this.inferenceResult = new RuleSet();
    }
    
    
    /**
     * Add a rule to inference result
     *
     * @param rule
     * @return
     */
    @Override
    public boolean addRuleToInferenceResult(Rule rule) {
        return this.inferenceResult.addRule(rule);
    }

    /**
     * Delete a rule from the fired rules list
     *
     * @param rule
     * @return
     */
    @Override
    public boolean deleteRuleFromInferenceResult(Rule rule) {
        return this.inferenceResult.deleteRuleByRuleId(rule.getRuleId());
    }  
    
    /**
     * Get inference result
     * @return 
     */
    @Override
    public RuleSet getInferenceResult(){
        return this.inferenceResult;
    }
    
    /**
     * Perform inference with starting rule for multiple classifications
     * @param currentRule
     * @return 
     */
    @Override
    public RuleSet inferenceWithStartingRule(Rule currentRule) {   
        //check whether the current rule is fired
        
        //Logger.info("#### inferenceWithStartingRule with rule:" + currentRule.getRuleId());
        
        if (currentRule.getIsStopped()) {
            //Logger.info("#### Current rule " + currentRule.getRuleId() + " is stopped!");
            // dear me add more here!
        }
        else  if(currentRule.isSatisfied(this.currentCase) || currentRule.getRuleId()==startingRule.getRuleId()) { 
            
            //if(currentRule.isSatisfied(this.currentCase)) {
                //Logger.info("###### The current rule," + currentRule.getRuleId() + ", satisfies the case..");
                //if (currentRule.getRuleId()==startingRule.getRuleId())
                    //Logger.info("###### but it is our starting rule..");
            //}
            
            if( currentRule.getRuleId()!=startingRule.getRuleId()){  // if this is true, rule must satisfy the case!
                //add current rule to fired rule list
                //Logger.info("###### adding rule " + currentRule.getRuleId() + " to fired list");              
                this.addRuleToFiredRules(currentRule);
            }
            
            // get the number of rules in the decision list of the current rule
            int childCount = currentRule.getChildRuleCount();

            //if there is no rule in the decision list, set the current rule as conclusion
            if(childCount == 0) {
                if( currentRule.getRuleId()!=startingRule.getRuleId()){     
                    //this.addRuleToFiredRules(currentRule);
                    this.addRuleToInferenceResult(currentRule); 
                   // Logger.info("###### no children, so adding rule " + currentRule.getRuleId() + " to stack frame");              

                }
            //if there rule in the decision list, test the child rules
            } 
            else {

                // to check whether all the rules in the decision list are not valid
                boolean isAllChildRulesNotValid = true;

                //check all child rules
                for (int i = 0; i < childCount; i++) {
                    // get child rule (rule in the decision list) of the current rule
                    Rule childRule = currentRule.getChildAt(i);

                    //check whether the child rule is fired
                    if (childRule.isSatisfied(this.currentCase) && !childRule.getIsStopped()) {

                        //add child rule to fired rule list
                        // DAVE HERE DPH 25/9/18 should we add child rule to fired list if it has a stopping child?
                        this.addRuleToFiredRules(childRule);
                        // confirm there is a valid rule in the siblings
                        isAllChildRulesNotValid = false;

                        // if the child rule has its child rules (grand child of the current rule), then inference with the child rule
                        if (childRule.getChildRuleCount()> 0) {
                           // Logger.info("#### Child rule " + childRule.getRuleId() +" satisfies case, but it has children, so calling inference on it..");
                            this.inferenceWithStartingRule(childRule);
                        // if the child rule has no child rules (grand child of the current rule), then set the child rule as conclusion 
                        } else { 
                            // DAVE HERE DPH 25/9/18 added following line as a test..
                            //this.addRuleToFiredRules(childRule);
                            this.deleteRuleFromInferenceResult(currentRule);
                            this.addRuleToInferenceResult(childRule);
                        }
                    } 
                }

                //if there is no valid child rules, set the current rule as conclusion 
                if (isAllChildRulesNotValid == true) {
                    if( currentRule.getRuleId()!=startingRule.getRuleId()){
                        this.addRuleToInferenceResult(currentRule);
                    }
                }        
                
            }
        }
        return this.inferenceResult;
        
    }
    

    /**
     * Perform inference for multiple classifications
     * @param currentRule
     * @return 
     */
    @Override
    public RuleSet inference(Rule currentRule) {   
        //check whether the current rule is fired
        //Logger.info("#### inference - considering rule: " + currentRule.getRuleId());
        
        if (currentRule.getIsStopped()) {
            //Logger.info("#### Current rule " + currentRule.getRuleId() + " is stopped!");
            // dear me add more here!
        }
        else if(currentRule.isSatisfied(this.currentCase)) {        

            //Logger.info("Current rule is satisfied by case:" + currentRule.getRuleId());
            
             // get the number of rules in the decision list of the current rule
            
            
            //Logger.info("This rule has " + childCount + " children" );

            //boolean stoppingChildFound = false;
            
            // check for a next level child stopping rule (which invalidates the branch)
            // DPH 25/9/18
            
            //for (int i = 0; i < childCount; i++) {
                    // get child rule (rule in the decision list) of the current rule
                   // Rule childRule = currentRule.getChildAt(i);
                    //Logger.info("Lookin at childRule:" + childRule.toString());
                   // if (childRule.getIsStoppingRule()){
                        //stoppingChildFound = true;
                       // Logger.info("##### While looking at rule " +  currentRule.getRuleId() + ", we found a child rule that is a stopping rule!");
                    //}
            //}
            
            // we don't want to ever invalidate the root rule! DPH
            //if (currentRule.getRuleId() == 0) {
                //if (stoppingChildFound) {
                    //Logger.info("A stopping child found, but as we're considering the root rule it is not a valid stop!");
                //}
               //stoppingChildFound = false;                
            //}
            
            //if (stoppingChildFound) {
                    //Logger.info("A stopping child found, so rule " +  currentRule.getRuleId() + " will be invalidated ");
                    //this.deleteRuleFromInferenceResult(currentRule);
            //}
                                 
            //if (!stoppingChildFound) {
                //Logger.info("No stopping children found at this level, so adding rule " + currentRule.getRuleId() + " to fired rules");
                //add current rule to fired rule list
            this.addRuleToFiredRules(currentRule);

            int childCount = currentRule.getChildRuleCount();
            
            //if there is no rule in the decision list, set the current rule as conclusion
            if(childCount == 0) {
                //Logger.info("#### no children so rule is conclusion!");
                this.addRuleToInferenceResult(currentRule);
            //if there rule in the decision list, test the child rules
            } 
            else {
                // to check whether all the rules in the decision list are not valid
                boolean isAllChildRulesNotValid = true;
                //Logger.info("Considering child rules");
                //check all child rules
                for (int i = 0; i < childCount; i++) {
                    // get child rule (rule in the decision list) of the current rule
                    Rule childRule = currentRule.getChildAt(i);

                    //check whether the child rule is fired
                    if (childRule.isSatisfied(this.currentCase) && !childRule.getIsStopped()) {
                        //add child rule to fired rule list
                        this.addRuleToFiredRules(childRule);  
                        // confirm there is a valid rule in the siblings
                        //Logger.info("Setting isAllChildRulesNotValid to false");
                        isAllChildRulesNotValid = false;

                        // if the child rule has its child rules (grand child of the current rule), then inference with the child rule
                        if (childRule.getChildRuleCount()> 0) {
                            Logger.info("#### Child rule " + childRule.getRuleId() +" satisfies case, but it has children, so calling inference on it..");
                            this.inference(childRule);
                        // if the child rule has no child rules (grand child of the current rule), then set the child rule as conclusion 
                        } 
                        else {   
                            //Logger.info("Child rule " + childRule.getRuleId() + " has no children, so it is the inference result - removing rule " + currentRule.getRuleId() + " from inference result");
                            //this.addRuleToFiredRules(childRule);  //DAVE march 2019 REMINDER TEST ADD HERE

                            this.deleteRuleFromInferenceResult(currentRule);
                            this.addRuleToInferenceResult(childRule);
                        }
                    } 
                }

                //if there is no valid child rules, set the current rule as conclusion 
                if (isAllChildRulesNotValid == true) {
                    //Logger.info("#### no children satisfy case, so rule " + currentRule.getRuleId() + " is conclusion");
                    this.addRuleToInferenceResult(currentRule);
                }        
            }
            //}
        }
        //else {
           // Logger.info("##### rule does not satisfy case so ignoring..");
        //}
        return this.inferenceResult;
        
    }
}

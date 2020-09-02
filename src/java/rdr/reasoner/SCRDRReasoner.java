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
public class SCRDRReasoner extends Reasoner {
    
    private Rule inferenceResult;
    
    
    /**
     * Constructor
     */
    public SCRDRReasoner() {
        super();
    }
    
    /**
     * Constructor 
     * @param ruleSet
     * @param currentCase
     */
    public SCRDRReasoner(RuleSet ruleSet,Case currentCase){
        super(ruleSet, currentCase);
    }    

    /**
     *
     * @return
     */
    @Override
    public String getReasonerMethod() {
        return "SCRDR";
    }
    
    /**
     * Set inference result
     * @param inferenceResult 
     */
    @Override
    public void setInferenceResult(Rule inferenceResult){
        this.inferenceResult = inferenceResult;
    }
    
    /**
     * Get inference result
     * @return 
     */
    @Override
    public Rule getInferenceResult(){
        return this.inferenceResult;
    }
    
    /**
     * Perform inference for single classifications
     * @param currentRule
     * @return 
     */
    @Override
    public Rule inference(Rule currentRule) {   
        //check whether the current rule is fired
        if(currentRule.isSatisfied(this.currentCase)){
            //add current rule to fired rule list
            this.addRuleToFiredRules(currentRule);
            
            // get the number of rules in the decision list of the current rule
            int childCount = currentRule.getChildRuleCount();
        
            //if there is no rule in the decision list, set the current rule as conclusion
            if(childCount == 0) {                
                this.inferenceResult = currentRule;
            //if there rule in the decision list, test the child rules
            } else {
                // to check whether all the rules in the decision list are not valid
                boolean isAllChildRulesNotValid = true;

                //check all child rules
                for (int i = 0; i < childCount; i++) {
                    // get child rule (rule in the decision list) of the current rule
                    Rule childRule = currentRule.getChildAt(i);

                    //check whether the child rule is fired
                    if (childRule.isSatisfied(this.currentCase)) {
                        //add child rule to fired rule list
                        this.addRuleToFiredRules(childRule);
                        // confirm there is a valid rule in the siblings
                        isAllChildRulesNotValid = false;

                        // if the child rule has its child rules (grand child of the current rule), then inference with the child rule
                        if (childRule.getChildRuleCount()> 0) {                            
                            //inference the child rule
                            this.inference(childRule);
                            //break the for, so the other siblings will not be validated
                            break;
                        // if the child rule has no child rules (grand child of the current rule), then set the child rule as conclusion 
                        } else { 
                            this.inferenceResult = childRule;
                            //break the for, so the other siblings will not be validated
                            break;
                        }
                    } 
                }

                //if there is no valid child rules, set the current rule as conclusion 
                if (isAllChildRulesNotValid == true) {
                    this.inferenceResult = currentRule;
                }        
            }
        }
        return this.inferenceResult;
    }
    
}
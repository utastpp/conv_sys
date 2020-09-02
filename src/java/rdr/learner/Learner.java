/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.learner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import rdr.cases.Case;
import rdr.cases.CornerstoneCase;
import rdr.cases.CornerstoneCaseSet;
import rdr.model.Value;
import rdr.rules.Conclusion;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleLoader;
import rdr.rules.RuleSet;
import cmcrdr.logger.Logger;

/**
 * This class is used to define knowledge acquisition process
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Learner implements ILearner{
    
    /**
     * Rule set
     */
    protected RuleSet ruleSet = new RuleSet();
    
    /**
     * wrong conclusion 
     */
    protected Conclusion wrongConclusion;
    
    /**
     * Current Cornerstone case
     */
    protected CornerstoneCase currentCornerstoneCase;
    
    /**
     * New rule
     */
    protected Rule newRule;
    
    /**
     * Cornerstone case set 
     */
    protected CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
    
    /**
     * Validating case set for new rule
     */
    protected CornerstoneCaseSet validatingCaseSet = new CornerstoneCaseSet();
    
    /**
     * Full difference list
     */
    protected HashMap<String, DiffElement> fullDifferenceList;
    
    /**
     * Partial difference list
     */
    protected HashMap<String, DiffElement> partialDifferenceList;
    /**
     * Constructor
     */
    public Learner() {
        this.ruleSet = null;
        this.currentCornerstoneCase = null;
        this.wrongConclusion = null;
        this.newRule = new Rule();
        this.cornerstoneCaseSet = new CornerstoneCaseSet();
        
    }

    /**
     * Constructor
     * @param ruleSet
     * @param aCase 
     * @param wrongConclusion 
     * @param rule 
     */
    public Learner(RuleSet ruleSet, Case aCase, 
            Conclusion wrongConclusion, Rule rule) {
        this.ruleSet = ruleSet;
        this.currentCornerstoneCase = new CornerstoneCase (aCase);
        this.wrongConclusion = wrongConclusion;
        this.newRule = rule;
        this.cornerstoneCaseSet = new CornerstoneCaseSet();
    }    
    
    /**
     * Set cornerstone case by case that is used for knowledge acquisition
     * @param aCase 
     */
    @Override
    public void setCurrentCornerstoneCaseByCase(Case aCase) {
        this.currentCornerstoneCase =new CornerstoneCase (aCase);
    }
    
    /**
     * Set cornerstone case that is used for knowledge acquisition
     * @param aCornerstoneCase 
     */
    @Override
    public void setCurrentCornerstoneCase(CornerstoneCase aCornerstoneCase) {
        this.currentCornerstoneCase = aCornerstoneCase;
    }
    
    /**
     * Get current case that is used for knowledge acquisition
     * @return 
     */
    @Override
    public CornerstoneCase getCurrentCornerstoneCase() {
        return this.currentCornerstoneCase;
    }
    
    /**
     * Set new rule that will be acquired
     * @param rule 
     */
    @Override
    public void setNewRule(Rule rule) {
        this.newRule = rule;
    }
    
    /**
     * Get current new rule
     * @return 
     */
    @Override
    public Rule getNewRule() {
        return this.newRule;
    }
    
    
    /**
     * Get new rule type
     * @return 
     */
    @Override
    public String getNewRuleType(){
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     * Set new rule that will be acquired 
     * @param rule
     */
    @Override
    public void setInferenceResult(Rule rule) {
        throw new UnsupportedOperationException("Only supported in SCRDR."); 
    }
    
    /**
     * Set new rule that will be acquired
     * @param ruleSet 
     */
    @Override
    public void setInferenceResult(RuleSet ruleSet) {
        throw new UnsupportedOperationException("Only supported in MCRDR."); 
    }
    
    /**
     * Get current new rule
     * @return 
     */
    @Override
    public Object getInferenceResult() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     * Set new rule condition set
     * @param conditionSet 
     * 
     */
    @Override
    public void setConditionSetToNewRule(ConditionSet conditionSet) {
        this.newRule.setConditionSet(conditionSet);
    }

    /**
     * Add new condition
     * @param condition 
     * 
     * @return  
     */
    @Override
    public boolean addConditionToNewRule(Condition condition) {
        return this.newRule.getConditionSet().addCondition(condition);        
    }

    /**
     * Delete a condition
     * @param condition 
     * 
     * @return  
     */
    @Override
    public boolean deleteConditionFromNewRule(Condition condition) {
        return this.newRule.getConditionSet().deleteCondition(condition);
    }

    /**
     * Delete all conditions of new rule
     */
    @Override
    public void deleteAllConditionFromNewRule() {
        this.newRule.getConditionSet().deleteAllCondition();
    }    

    /**
     * Get the number of conditions in new rule
     * @return 
     */
    @Override
    public int getConditionAmountFromNewRule() {
        return this.newRule.getConditionSet().getConditionAmount();
    }

    /**
     * Get the number of conditions in new rule
     * @return 
     */
    @Override
    public ConditionSet getConditionSetFromNewRule() {
        return this.newRule.getConditionSet();
    }
    
    
    /**
     * Set new conclusion
     * @param conclusion 
     */
    @Override
    public void setConclusionToNewRule(Conclusion conclusion) {
        this.newRule.setConclusion(conclusion);
    }

    /**
     * Get new conclusion
     * @return 
     */
    @Override
    public Conclusion getConclusionFromNewRule() {
        return this.newRule.getConclusion();
    }

    /**
     * Add new rule
     * @return 
     */
    @Override
    public boolean addNewRule(boolean doNotStack) {
        if(!this.ruleSet.getRuleById(0).isRuleChild(this.newRule)){        
            int newRuleId = this.ruleSet.getNewRuleId();
            this.newRule.setRuleId(newRuleId);
            this.newRule.setDoNotStack(doNotStack);
            
            int conclusionId = this.newRule.getConclusion().getConclusionId();
            Logger.info("***LEARNER: new ruleId is " + newRuleId);
            Logger.info("***LEARNER: initial conclusionId is " + conclusionId);
            
            if(this.ruleSet.isNewConclusion(this.newRule.getConclusion())){
                conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                Logger.info("***LEARNER2: new conclusionId is now " + conclusionId);

                RuleLoader.insertRuleConclusions(conclusionId, this.newRule.getConclusion());
                this.newRule.getConclusion().setConclusionId(conclusionId);
            }
            this.newRule.setParent(this.ruleSet.getRuleById(0));
            this.ruleSet.getRuleById(0).addChildRule(this.newRule);
            Logger.info("***LEARNER: inserting new rule: " + newRuleId + " with conclusionId: " + conclusionId);

            RuleLoader.insertRule(newRuleId, this.newRule, conclusionId);
                           
            return this.ruleSet.addRule(this.newRule);
        } else {
            return false;
        }
    }


    /**
     * Add new rule temporarily (without inserting)
     * @return 
     */
    @Override
    public boolean addTempNewRule() {
        if(!this.ruleSet.getRuleById(0).isRuleChild(this.newRule)){        
            int newRuleId = this.ruleSet.getNewRuleId();
            this.newRule.setRuleId(newRuleId);
            
            int conclusionId = this.newRule.getConclusion().getConclusionId();
            
            if(this.ruleSet.isNewConclusion(this.newRule.getConclusion())){
                conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                this.newRule.getConclusion().setConclusionId(conclusionId);
            }
            this.newRule.setParent(this.ruleSet.getRuleById(0));
            this.ruleSet.getRuleById(0).addChildRule(this.newRule);
                           
            return this.ruleSet.addRule(this.newRule);
        } else {
            return false;
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean addAlternativeRule(boolean doNotStack) {
        throw new UnsupportedOperationException("Only supported for MCRDR Learner."); 
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean addTempAlternativeRule(){
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     *
     * @return
     */
    @Override
    public boolean addExceptionRule(boolean doNotStack) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean addTempExceptionRule(){
        throw new UnsupportedOperationException("Not supported yet."); 
        
    }

    /**
     * Return rule set
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
     * Set wrong conclusion 
     * @param wrongConclusion
     */
    @Override
    public void setWrongConclusion(Conclusion wrongConclusion) {
        this.wrongConclusion = wrongConclusion;
    }
    
    /**
     * Delete wrong conclusion 
     */
    @Override
    public void deleteWrongConclusion() {
        this.wrongConclusion = null;
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
     * Returns true if wrong conclusion exists
     * @return 
     */
    @Override
    public boolean isWrongConclusionExist() {
        return this.wrongConclusion!=null;
    }    
    
    /**
     * Set wrong rule 
     * @param wrongRule
     */
    @Override
    public void setWrongRule(Rule wrongRule) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     * Get wrong rule 
     * @return 
     */
    @Override
    public Rule getWrongRule() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }    
    
    /**
     * Set wrong rule set
     * @param wrongRuleSet 
     */
    @Override
    public void setWrongRuleSet(RuleSet wrongRuleSet) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     * Get wrong rule set
     * @return 
     */
    @Override
    public RuleSet getWrongRuleSet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     * Add cornerstone case to new rule
     * @param newCase
     * @return 
     */
    @Override
    public boolean addCornerstoneCase(Case newCase) {
        return this.cornerstoneCaseSet.addCase(new CornerstoneCase(newCase));
    }

    /**
     * Delete cornerstone cases
     * @param newCase
     * @return 
     */
    @Override
    public boolean deleteCornerstoneCase(Case newCase) {
        return this.cornerstoneCaseSet.deleteCornerstoneCase(new CornerstoneCase(newCase));
    }
    
    /**
     * Retrieve validation case set
     */
    @Override
    public void retrieveValidatingCaseSet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }      
    
    /**
     * Get validation case set
     * @return 
     */
    @Override
    public CornerstoneCaseSet getValidatingCaseSet() {
        return this.validatingCaseSet;
    }      
    
    /**
     * Generate full difference list - differences between the current case and
     * all validation cases
     */
    @Override
    public void generateFullDifferenceList() {
        this.fullDifferenceList = new HashMap<>();
        HashMap<String, Value> currentCaseValues = this.currentCornerstoneCase.getValues();
        Set valueSet = currentCaseValues.entrySet();
        Iterator resultIterator = valueSet.iterator();

        while (resultIterator.hasNext()) {
            Map.Entry me = (Map.Entry) resultIterator.next();
            String attributeName = (String) me.getKey();
            Value currentCaseValue = (Value) me.getValue();

            //System.out.println("Case amount = " + this.validatingCaseSet.getCaseAmount());
            Set valSet = this.validatingCaseSet.getBase().entrySet();
            Iterator resultIterator2 = valSet.iterator();
            boolean isPositiveDifferentAtrribute = true;
            boolean isNegativeDifferentAtrribute = true;

            HashMap<Integer, Value> validationCaseValue = new HashMap<>();

            Value previousValidationCaseValue = null;
            while (resultIterator2.hasNext()) {
                Map.Entry me2 = (Map.Entry) resultIterator2.next();
                Case valCase = (Case) me2.getValue();
                validationCaseValue.put(valCase.getCaseId(), (Value) valCase.getValue(attributeName));
                if (valCase.getValue(attributeName).equals(currentCaseValue)) {
                    isPositiveDifferentAtrribute = false;
                } else {
                    if (previousValidationCaseValue == null) {
                        previousValidationCaseValue = (Value) valCase.getValue(attributeName);
                    } else {
                        if (!valCase.getValue(attributeName).equals(previousValidationCaseValue)) {
                            isNegativeDifferentAtrribute = false;
                        }
                    }
                }
            }

            //Register 
            if (isPositiveDifferentAtrribute) {
                this.fullDifferenceList.put(attributeName,
                        new DiffElement(attributeName,
                                currentCaseValue, validationCaseValue, DiffElement.POSITIVE_FULL_DIFF));
            }
            
            //Register negative different attribute
            if (isNegativeDifferentAtrribute) {
                this.fullDifferenceList.put(attributeName,
                        new DiffElement(attributeName,
                                previousValidationCaseValue,
                                validationCaseValue, DiffElement.NEGATIVE_FULL_DIFF));
            }
        }

    }

    /**
     * Generate partial difference list - given the selected validation cases
     * the system finds attributes that have difference values between the 
     * current case and the subset of the validation cases
     * 
     * @param subsetOfValidatingCaseSet 
     */
    @Override
    public void generatePartialDifferenceList(HashMap<Integer, Case> subsetOfValidatingCaseSet) {
                this.partialDifferenceList = new HashMap<>();
        HashMap<String, Value> currentCaseValues = this.currentCornerstoneCase.getValues();
        Set valueSet = currentCaseValues.entrySet();
        Iterator resultIterator = valueSet.iterator();

        while (resultIterator.hasNext()) {
            Map.Entry me = (Map.Entry) resultIterator.next();
            String attributeName = (String) me.getKey();
            Value currentCaseValue = (Value) me.getValue();

            Set valSet = subsetOfValidatingCaseSet.entrySet();
            Iterator resultIterator2 = valSet.iterator();
            boolean isPositiveDifferentAtrribute = true;
            boolean isNegativeDifferentAtrribute = true;

            HashMap<Integer, Value> validationCaseValue = new HashMap<>();

            Value previousValidationCaseValue = null;
            while (resultIterator2.hasNext()) {
                Map.Entry me2 = (Map.Entry) resultIterator.next();
                Case valCase = (Case) me2.getValue();
                validationCaseValue.put(valCase.getCaseId(),
                        valCase.getValue(attributeName));
                if (valCase.getValue(attributeName).equals(currentCaseValue)) {
                    isPositiveDifferentAtrribute = false;
                } else {
                    if (previousValidationCaseValue == null) {
                        previousValidationCaseValue = valCase.getValue(attributeName);
                    } else {
                        if (!valCase.getValue(attributeName).equals(previousValidationCaseValue)) {
                            isNegativeDifferentAtrribute = false;
                        }
                    }
                }
            }

            //Register 
            if (isPositiveDifferentAtrribute) {
                this.partialDifferenceList.put(attributeName,
                        new DiffElement(attributeName,
                                currentCaseValue, validationCaseValue, DiffElement.POSITIVE_FULL_DIFF));
            }
            
            //Register negative different attribute
            if (isNegativeDifferentAtrribute) {
                this.partialDifferenceList.put(attributeName,
                        new DiffElement(attributeName,
                                previousValidationCaseValue,
                                validationCaseValue, DiffElement.NEGATIVE_FULL_DIFF));
            }
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public HashMap<String, DiffElement> getPartialDifferenceList(){
        return partialDifferenceList;
    }
    
    /**
     *
     * @return
     */
    @Override
    public HashMap<String, DiffElement> getFullDifferenceList(){
        return fullDifferenceList;
    }
    
    /**
     *
     */
    @Override
    public void executeAddingRule(boolean doNotStack){
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    @Override
    public void executeAddingStopRule(){
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     *
     */
    @Override
    public void executeAddingRuleForValidation(){
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}

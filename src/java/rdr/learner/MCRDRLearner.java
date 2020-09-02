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
import rdr.cases.Case;
import rdr.cases.CornerstoneCaseSet;
import rdr.rules.Conclusion;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleLoader;
import rdr.rules.RuleSet;
import cmcrdr.logger.Logger;

public class MCRDRLearner extends Learner{
    
    protected RuleSet inferenceResult = new RuleSet();   
    protected RuleSet wrongRuleSet = new RuleSet(); 
    public boolean isStoppingRule = false;
    
    
    /**
     * Constructor
     */
    public MCRDRLearner() {
        super();
    }

    /**
     * Constructor
     * @param ruleSet
     * @param currentCase 
     * @param wrongConclusion
     * @param rule 
     */
    public MCRDRLearner(RuleSet ruleSet, Case currentCase, 
            Conclusion wrongConclusion, Rule rule) {
        super(ruleSet, currentCase, wrongConclusion, rule);
    } 
    
    /**
     * Get new rule type
     * @return i.e. new, alter, exception
     */
    @Override
    public String getNewRuleType() {
        String mode = "";
        if(this.isWrongConclusionExist()){            
            mode = "exception";
        } else {
            // if there is no root rule in inference result, this means some rules are fired, so the rule type is alter
            if(this.inferenceResult.getRuleById(0)==null){
                mode = "alter";
            } else {
                mode = "new";
            }
        }
        
        // testing total hack... DAVE JUNE 2019
        //if (!mode.equals("new"))
           // mode = "exception";
        
        Logger.info("getNewRuleType set rule mode to " + mode);

        return mode;
    }
    
    
    /**
     * Set new rule that will be acquired
     * @param ruleSet 
     */
    @Override
    public void setInferenceResult(RuleSet ruleSet) {
        this.inferenceResult = ruleSet;
    }
    
    /**
     * Get current new rule
     * @return 
     */
    @Override
    public RuleSet getInferenceResult() {
        return this.inferenceResult;
    }
    
    
    /**
     * Add alternative rule
     * @return 
     */
    @Override
    public boolean addAlternativeRule(boolean doNotStack) {
        Logger.info("addAlternativeRule - doNotStack is:" + doNotStack);
        Logger.info("addAlternativeRule - this.newRule is: " + this.newRule.toString());

        // get wrong rule set
        Set valueSet = this.inferenceResult.getBase().entrySet();
        Logger.info("addAlternativeRule - iterating over inference results:");

        Iterator resultIterator = valueSet.iterator();
        while (resultIterator.hasNext()) {
            Map.Entry me = (Map.Entry) resultIterator.next();
            //get alter rule
            Rule alterRule = (Rule) me.getValue();
            Logger.info("   addAlternativeRule - current alterRule is: [" + alterRule.getRuleId() +"]");

            //add this rule only if there is no rule in the same level (siblings)            
            if(!this.ruleSet.getRule(alterRule.getParent()).isRuleAddedInChildRuleList(this.newRule)){
                Logger.info("   addAlternativeRule - alterRule is not sibling to newRule!");

                int newRuleId = this.ruleSet.getNewRuleId();
                Rule addingRule = RuleBuilder.copyRule(this.newRule);
                addingRule.setRuleId(newRuleId);
                addingRule.setDoNotStack(doNotStack);
                // if this is new conclusion, add it into db
                int conclusionId = addingRule.getConclusion().getConclusionId();

                if(this.ruleSet.isNewConclusion(addingRule.getConclusion())){
                    conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                    RuleLoader.insertRuleConclusions(conclusionId, addingRule.getConclusion());
                    addingRule.getConclusion().setConclusionId(conclusionId);
                }
                //set parent rule of the new rule as the parent rule of the alternative rule (retrieved from the wrong rule set).
                addingRule.setParent(this.ruleSet.getRuleById(alterRule.getParent().getRuleId()));
                Logger.info("   addAlternativeRule - addingRule parent set to:" + this.ruleSet.getRuleById(alterRule.getParent().getRuleId()));


                //set the parent rule of the alternative rule (retrieved from the wrong rule set) as the new rule.
                this.ruleSet.getRuleById(alterRule.getParent().getRuleId()).addChildRule(addingRule);
                Logger.info("   addAlternativeRule - addingRule parent [" + alterRule.getParent().getRuleId() + "] set child as " + addingRule.getRuleId()); ;
   
                RuleLoader.insertRule(newRuleId, addingRule, conclusionId);                
                this.ruleSet.addRule(addingRule);
            }
            else {
                 Logger.info("   addAlternativeRule - alterRule [" + alterRule.getRuleId() + "] *is* sibling to newRule!");

            }
        }
        this.ruleSet.setRootRuleTree();
        return true;
    }
    
    
    /**
     * Add alternative rule temporarily
     * @return 
     */
    @Override
    public boolean addTempAlternativeRule() {
        // get wrong rule set
        Set valueSet = this.inferenceResult.getBase().entrySet();
        Iterator resultIterator = valueSet.iterator();

        while (resultIterator.hasNext()) {
            
            Map.Entry me = (Map.Entry) resultIterator.next();
            //get alter rule
            Rule alterRule = (Rule) me.getValue();
            
            //add this rule only if there is no rule in the same level (siblings)
            if(!this.ruleSet.getRuleById(alterRule.getParent().getRuleId()).getChildRuleList().contains(this.newRule)){
                int newRuleId = this.ruleSet.getNewRuleId();
                Rule addingRule = RuleBuilder.copyRule(this.newRule);
                addingRule.setRuleId(newRuleId);
                // if this is new conclusion, add it into db
                int conclusionId = addingRule.getConclusion().getConclusionId();

                if(this.ruleSet.isNewConclusion(addingRule.getConclusion())){
                    conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                    addingRule.getConclusion().setConclusionId(conclusionId);
                }
                //set parent rule of the new rule as the parent rule of the alternative rule (retrieved from the wrong rule set).
                addingRule.setParent(this.ruleSet.getRuleById(alterRule.getParent().getRuleId()));

                //set the parent rule of the alternative rule (retrieved from the wrong rule set) as the new rule.
                this.ruleSet.getRuleById(alterRule.getParent().getRuleId()).addChildRule(addingRule);      
                
                this.ruleSet.addRule(addingRule);
            } 
        }
        this.ruleSet.setRootRuleTree();
        return true;
    }
    
    
    /**
     * Add exception rule
     * @return 
     */
    @Override
    public boolean addExceptionRule(boolean doNotStack) {  
         Logger.info("doNotStack is:" + doNotStack);
        // get wrong rule base set
        Set valueSet = this.wrongRuleSet.getBase().entrySet();
        Iterator wrongRuleSetIterator = valueSet.iterator();
        Logger.info("addExceptionRule - iterating over wrongRuleSet:");
        if (valueSet.isEmpty())
            Logger.info("    addExceptionRule - no values in wrongRuleSet!");
        while (wrongRuleSetIterator.hasNext()) {
            int newRuleId = this.ruleSet.getNewRuleId();
             

            Rule addingRule = RuleBuilder.copyRule(this.newRule);
            addingRule.setRuleId(newRuleId);
            addingRule.setDoNotStack(doNotStack);

            Map.Entry me = (Map.Entry) wrongRuleSetIterator.next();           
            Rule parentRule = (Rule) me.getValue();     
            Logger.info("    addExceptionRule - wrong rule parent:" + parentRule.toString());

            
            // if this is new conclusion, add it into db
            int conclusionId = addingRule.getConclusion().getConclusionId();
            
            if(this.ruleSet.isNewConclusion(addingRule.getConclusion())){
                conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                RuleLoader.insertRuleConclusions(conclusionId, addingRule.getConclusion());
                addingRule.getConclusion().setConclusionId(conclusionId);
            }
            //set parent rule of the new rule as the parent rule (retrieved from the wrong rule set)
            addingRule.setParent(this.ruleSet.getRuleById(parentRule.getRuleId()));
            
            //set exception rule of the parent rule (retrieved from the wrong rule set) as the new rule.
            this.ruleSet.getRuleById(parentRule.getRuleId()).addChildRule(addingRule);          
            
            // insert new rule into db
            RuleLoader.insertRule(newRuleId, addingRule, conclusionId);
              
            this.ruleSet.addRule(addingRule);
        }
        this.ruleSet.setRootRuleTree();
        return true;
    }
    

    public boolean addExceptionStopRule() {        
        // get wrong rule base set
        Set valueSet = this.wrongRuleSet.getBase().entrySet();
        Iterator wrongRuleSetIterator = valueSet.iterator();
        
        while (wrongRuleSetIterator.hasNext()) {
            int newRuleId = this.ruleSet.getNewRuleId();
            Rule addingRule = RuleBuilder.copyRule(this.newRule);
            addingRule.setRuleId(newRuleId);

            Map.Entry me = (Map.Entry) wrongRuleSetIterator.next();
            Rule parentRule = (Rule) me.getValue();     
            Logger.info("Wrong rule parent:" + parentRule.toString());

            
            // if this is new conclusion, add it into db
            int conclusionId = addingRule.getConclusion().getConclusionId();
            
            if(this.ruleSet.isNewConclusion(addingRule.getConclusion())){
                conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                RuleLoader.insertRuleConclusions(conclusionId, addingRule.getConclusion());
                addingRule.getConclusion().setConclusionId(conclusionId);
            }
            //set parent rule of the new rule as the parent rule (retrieved from the wrong rule set)
            addingRule.setParent(this.ruleSet.getRuleById(parentRule.getRuleId()));
            
            //set exception rule of the parent rule (retrieved from the wrong rule set) as the new rule.
            this.ruleSet.getRuleById(parentRule.getRuleId()).addChildRule(addingRule);          
            
            // insert new rule into db
            RuleLoader.insertRule(newRuleId, addingRule, conclusionId);
              
            this.ruleSet.addRule(addingRule);
        }
        this.ruleSet.setRootRuleTree();
        return true;
    }
    
    
    /**
     * Add exception rule temporarily
     * @return 
     */
    @Override
    public boolean addTempExceptionRule() {        
        
        // get wrong rule base set
        Set wrongRules = this.wrongRuleSet.getBase().entrySet();
        Iterator wrongRuleSetIterator = wrongRules.iterator();
        
        while (wrongRuleSetIterator.hasNext()) {
            
            
            int newRuleId = this.ruleSet.getNewRuleId();
            Rule addingRule = RuleBuilder.copyRule(this.newRule);
            addingRule.setRuleId(newRuleId);

            Map.Entry me = (Map.Entry) wrongRuleSetIterator.next();
            Rule parentRule = (Rule) me.getValue();  
            
            // if this is new conclusion, add it into db
            int conclusionId = addingRule.getConclusion().getConclusionId();
            
            if(this.ruleSet.isNewConclusion(addingRule.getConclusion())){
                conclusionId = this.ruleSet.getConclusionSet().getNewConclusionId();
                addingRule.getConclusion().setConclusionId(conclusionId);
            }
            //set parent rule of the new rule as the parent rule (retrieved from the wrong rule set)
            addingRule.setParent(this.ruleSet.getRuleById(parentRule.getRuleId()));
            
            //set exception rule of the parent rule (retrieved from the wrong rule set) as the new rule.
            this.ruleSet.getRuleById(parentRule.getRuleId()).addChildRule(addingRule);          
              
            this.ruleSet.addRule(addingRule);
            
        }
        this.ruleSet.setRootRuleTree();
        return true;
    }
    
    
    /**
     * Delete wrong conclusion.
     */
    @Override
    public void deleteWrongConclusion() {
        this.wrongConclusion = null;
        this.wrongRuleSet = new RuleSet();
    }
    
    
    /**
     * Set wrong conclusion and set wrong rule set
     * @param wrongConclusion
     */
    @Override
    public void setWrongConclusion(Conclusion wrongConclusion) {
        this.wrongConclusion = wrongConclusion;        
        this.wrongRuleSet = this.inferenceResult.getRuleSetbyConclusion(wrongConclusion);
        this.currentCornerstoneCase.setWrongRuleSet(this.wrongRuleSet);
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
     * Set wrong rule set
     * @param wrongRuleSet 
     */
    @Override
    public void setWrongRuleSet(RuleSet wrongRuleSet) {
        this.wrongRuleSet = wrongRuleSet;
    }

    /**
     * Get wrong rule set
     * @return 
     */
    @Override
    public RuleSet getWrongRuleSet() {
        return this.wrongRuleSet;
    }
    
    /**
     * Retrieve validation cornerstone cases
     */
    @Override
    public void retrieveValidatingCaseSet() {
        this.validatingCaseSet = new CornerstoneCaseSet();      
        
        if(this.wrongConclusion!=null) {
            //when refining rule...
            
            this.wrongRuleSet  = this.inferenceResult.getRuleSetbyConclusion(this.wrongConclusion);
            //Current fired rules' cornerstones

            this.wrongRuleSet.retrieveCornerstoneCaseSet();                       
            CornerstoneCaseSet aCornerstoneCaseSet = this.wrongRuleSet.getCornerstoneCaseSet();            
                  
            
            this.validatingCaseSet.putCornerstoneCaseSet(aCornerstoneCaseSet);
            
            Set wrongRules = this.wrongRuleSet.getBase().entrySet();
            Iterator wrongRuleSetIterator = wrongRules.iterator();

            while (wrongRuleSetIterator.hasNext()) {
                Map.Entry me = (Map.Entry) wrongRuleSetIterator.next();
                Rule aRule = (Rule) me.getValue();      

                Vector<Rule> aAlternativeRuleList = aRule.getChildRuleList();
                for (Rule aAlternativeRule : aAlternativeRuleList) {
                    this.validatingCaseSet.putCornerstoneCaseSet(aAlternativeRule.getCornerstoneCaseSet());
                }            
            }
        } else {
            //when altering rule...
            Set inferenceRules = this.inferenceResult.getBase().entrySet();
            Iterator ruleSetIterator = inferenceRules.iterator();

            while (ruleSetIterator.hasNext()) {
                Map.Entry me = (Map.Entry) ruleSetIterator.next();
                Rule aRule = (Rule) me.getValue();      

                if(aRule.isParentExist()){
                    Rule aParentRule = aRule.getParent();
                    Vector<Rule> aChildRuleList = aParentRule.getChildRuleList();
                    for(int i=0; i < aChildRuleList.size(); i++){
                        Rule aChildRule = aChildRuleList.get(i);
                        this.validatingCaseSet.putCornerstoneCaseSet(aChildRule.getCornerstoneCaseSet());
                    }       
                }
            }
        }
    }    
    
    /**
     *
     */
    @Override
    public void executeAddingRule(boolean doNotStack){
        Logger.info("executeAddingRule doNotStack is:" + doNotStack);
        switch (this.getNewRuleType()) {
            case "new":
                Logger.info("Add addNewRule");
                this.addNewRule(doNotStack);
                break;

            case "alter":
                Logger.info("Add addAlternativeRule");
                this.addAlternativeRule(doNotStack);
                break;

            case "exception":
                Logger.info("Add addExceptionRule");

                this.addExceptionRule(doNotStack);
                break;
        }
        this.getRuleSet().setRootRuleTree();
    }
    

    @Override
    public void executeAddingStopRule(){
        switch (this.getNewRuleType()) {
            case "new":
                Logger.info("Add new stopping rule");
                this.addNewRule(false);
                break;

            case "alter":
                Logger.info("Add alter stopping rule");

                this.addAlternativeRule(false);
                break;

            case "exception":
                Logger.info("Add exception stopping rule");

                this.addExceptionStopRule();
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

            case "alter":
                this.addTempAlternativeRule();
                break;

            case "exception":
                this.addTempExceptionRule();
                break;
        }
        this.getRuleSet().setRootRuleTree();
    }
}

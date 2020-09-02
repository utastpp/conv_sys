/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.workbench;

import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import rdr.apps.Main;
import rdr.rules.RuleSet;
import rdr.cases.Case;
import rdr.learner.ILearner;
import rdr.learner.LearnerFactory;
import rdr.rules.Rule;
import rdr.reasoner.IReasoner;
import rdr.reasoner.MCRDRStackResultInstance;
import rdr.reasoner.MCRDRStackResultSet;
import rdr.reasoner.ReasonerFactory;
import rdr.rules.Conclusion;

/**
 * This class is used to maintain resources for inference and knowledge
 * acquisition
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Workbench {

    /**
     * Inference Method i.e.SCRDR, MCRDR
     */
    protected String method;

    /**
     * Rule set
     */
    public RuleSet ruleSet = null;

    /**
     * Temporary Rule set
     */
    protected RuleSet tempRuleSet = null;

    /**
     * Temporary Reasoner
     */
    protected IReasoner tempReasoner = null;

    /**
     * Temporary Learner
     */
    protected ILearner tempLearner = null;
    
    /**
     * temp inference result
     */
    private Object tempInferenceResult;
    
    /**
     * Current case
     */
    protected Case currentCase = null;
    
    /**
     * Rule 
     */
    protected Rule newRule = null;

    /**
     * Reasoner
     */
    protected IReasoner reasoner;
    
    /**
     * initial inference result for ka or inference
     */
    private Object initialInferenceResult;
    
    /**
     * inference result
     */
    private Object inferenceResult;
    
    /**
     * stacked inference result
     */
    //private MCRDRStackResultSet stackedInferenceResult = new MCRDRStackResultSet();
    //private ArrayList<MCRDRStackResultSet> stackedInferenceResultList = new ArrayList<>();
    //private int currentStackedInferenceResultIndex = -1;
    
    //private MCRDRStackResultSet currentStackedInferenceResult;
    
    /**
     * stacked inference result for temporary
     */
    //private MCRDRStackResultSet tmpStackedInferenceResult = new MCRDRStackResultSet();

    /**
     * fired rules
     */
    protected RuleSet firedRules = new RuleSet();
    
    /**
     * Learner
     */
    protected ILearner learner;
    
    /**
     * KA mode
     */
    protected String kaMode;
    
    /**
     * Wrong conclusion
     */
    protected Conclusion wrongConclusion = null;
    
    
    
    /**
     * 
     * Default Constructor.
     */
    public Workbench() {
        this.ruleSet = null;
        this.tempRuleSet = null;
        this.inferenceResult = null;
        this.firedRules = new RuleSet();
        this.method = "SCRDR";        
        this.reasoner = (IReasoner) ReasonerFactory.createReasoner("SCRDR");
        this.learner = (ILearner) LearnerFactory.createLearner("SCRDR");
        this.initialInferenceResult = null;
        // DPH 
        //this.stackedInferenceResult = new MCRDRStackResultSet();
    }

    /**
     * Constructor inference engine
     *
     * @param method
     */
    public Workbench(String method) {
        this.ruleSet = null;
        this.tempRuleSet = null;
        this.reasoner = (IReasoner) ReasonerFactory.createReasoner(method);
        this.learner = (ILearner) LearnerFactory.createLearner(method);
        this.reasoner.setRuleSet(ruleSet);
        this.firedRules = new RuleSet();
        switch (method) {
            case "SCRDR":
                this.method = "SCRDR";
                this.inferenceResult = null;
                this.initialInferenceResult = null;
                // DPH
                //this.stackedInferenceResult = new MCRDRStackResultSet();
                break;
            case "MCRDR":
                this.method = "MCRDR";
                this.inferenceResult = new RuleSet();
                this.initialInferenceResult = null;
                // DPH
                //this.stackedInferenceResult = new MCRDRStackResultSet();
                break;
        }
    }
    
    
    
    

    /**
     * Set reasoner
     * @param method 
     */
    public void setReasoner(String method) {
        this.reasoner = (IReasoner) ReasonerFactory.createReasoner(method);
    }
        
    /**
     * Get reasoner
     * @return 
     */
    public IReasoner getReasoner() {
        return this.reasoner;
    }
        
    /**
     * Get reasoner type i.e. SCRDR, MCRDR
     * @return 
     */
    public String getReasonerType() {
        return this.method;
    }

    /**
     * Set learner
     * @param method 
     */
    public void setLearner(String method) {
        this.learner = (ILearner) LearnerFactory.createLearner(method);
    }
        
    /**
     * Get learner
     * @return 
     */
    public ILearner getLearner() {
        return this.learner;
    }

    /**
     * Set current case
     * @param currentCase 
     */
    public void setCurrentCase(Case currentCase) {
        this.currentCase = currentCase;        
        this.reasoner.setCurrentCase(currentCase);
        this.learner.setCurrentCornerstoneCaseByCase(currentCase);
    }
        
    /**
     * Get current case in the work bench
     * @return 
     */
    public Case getCurrentCase() {
        return this.currentCase;
    }
    
    /**
     * Set rule in this work bench
     * @param ruleSet 
     */
    public void setRuleSet(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
        this.reasoner.setRuleSet(ruleSet);
        this.learner.setRuleSet(ruleSet);
        this.initialInferenceResult = ruleSet.getRootRule();
        
    }
    
   
    
    /**
     * Get ruleset in this work bench
     * @return 
     */
    public RuleSet getRuleSet() {
        return this.ruleSet;
    }
    
    /**
     * Inference for validation (temporarily add new rule)
     */
    public void inferenceForValidation(){
        //Set temporary new rule set for validation
        this.tempRuleSet =  this.ruleSet.cloneRuleSet();
                
        //copy current learner to temporary learner
        this.tempLearner = LearnerFactory.createLearner(this.method);        
        this.tempLearner.setCurrentCornerstoneCaseByCase(this.learner.getCurrentCornerstoneCase());
        switch (this.method) {
            case "SCRDR":
                this.tempLearner.setInferenceResult((Rule) this.learner.getInferenceResult());
                
                break;
            case "MCRDR":
                this.tempLearner.setInferenceResult((RuleSet) this.learner.getInferenceResult());
                break;
        }
        this.tempLearner.setNewRule(this.learner.getNewRule());
        
        //set rule set         
        this.tempLearner.setRuleSet(this.tempRuleSet);
        this.tempLearner.setWrongConclusion(this.learner.getWrongConclusion());
        
        
        // add new rule to temporary rule set by using temporary learner
        this.tempLearner.executeAddingRuleForValidation();
        
        this.tempRuleSet.setRuleSet(this.tempLearner.getRuleSet());
        
        //create temporary reasoner
        this.tempReasoner = ReasonerFactory.createReasoner(this.method);
        this.tempReasoner.setCurrentCase(this.learner.getCurrentCornerstoneCase());
        
        //set temporary rule set into the temporary reasoner
        this.tempReasoner.setRuleSet(this.tempRuleSet);
        
        Rule aRule = this.tempRuleSet.getRootRule();
        
        switch (this.method) {
            case "SCRDR":
                this.tempReasoner.inference(aRule);
                this.tempInferenceResult = ((Rule) this.tempReasoner.getInferenceResult());
                Rule tmpInferenceRule = (Rule) this.tempInferenceResult;
                if(tmpInferenceRule.getConclusion()!=null){
                    this.currentCase.addConclusion(tmpInferenceRule.getConclusion());
                }
                break;
            case "MCRDR":
                this.tempReasoner.clearInferenceResult();
                this.tempReasoner.inference(aRule);
                this.tempInferenceResult = ((RuleSet) this.tempReasoner.getInferenceResult());
                RuleSet tmpInferenceRuleSet = (RuleSet) this.tempInferenceResult;
                if(!tmpInferenceRuleSet.isEmpty()){
                    this.currentCase.setConclusionSet(tmpInferenceRuleSet.getConclusionSet());
                }
                break;
        }
    }
    
    /**
     * Inference
     */
    public void inference(){
        //Logger.info("Starting inference");
        // testing removal of clearing fired rules.. DPH Jun 9 2016
        this.reasoner.clearFiredRules();
        Rule aRule = this.ruleSet.getRootRule();

        this.reasoner.clearStartingRule();     

        RuleSet addingInferenceResultForStack = new RuleSet();

        switch (this.method) {
            case "SCRDR":
                this.inferenceResult=null;
                this.reasoner.inference(aRule);
                this.inferenceResult = ((Rule) this.reasoner.getInferenceResult());
                this.learner.setInferenceResult((Rule) this.reasoner.getInferenceResult());
                Rule tmpInferenceRule = (Rule) this.inferenceResult;
                if(tmpInferenceRule.getConclusion()!=null){
                    this.currentCase.addConclusion(tmpInferenceRule.getConclusion());
                }
                
                addingInferenceResultForStack.addRule(tmpInferenceRule);
                
                break;
            case "MCRDR":
                //Logger.info("%%%%%%%%%%%%%%%%%%%%%%% Inference without initial stack..");

                this.reasoner.clearInferenceResult();
                this.reasoner.inference(aRule);
                this.inferenceResult = ((RuleSet) this.reasoner.getInferenceResult());
                this.learner.setInferenceResult((RuleSet) this.reasoner.getInferenceResult());

                RuleSet tmpInferenceRuleSet = (RuleSet) this.inferenceResult;
                if(!tmpInferenceRuleSet.isEmpty()){
                    this.currentCase.setConclusionSet(tmpInferenceRuleSet.getConclusionSet());
                }

                addingInferenceResultForStack = tmpInferenceRuleSet;
                
                break;
        }
            
        this.firedRules = this.reasoner.getFiredRules();
        this.firedRules.setRootRuleTree();
        this.initialInferenceResult = this.inferenceResult;
               
        
        //Logger.info("#### after initial inference, the temp stack ruleset contains:");
        //for (Rule theRule: addingInferenceResultForStack.getBase().values()) {
            //Logger.info("##### Rule: " + theRule.getRuleId());
        //}
        //Logger.info("#### temp stack ruleset contains keys:");
        //for (int someKey: addingInferenceResultForStack.getBase().keySet()) {
            //Logger.info("#####  Found key: " + someKey);
        //}

       //Logger.info("####1 current inference result size is : " + DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize());
        
       
        int startingStackSize = DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize()-1;  // used to report which stack frame we start from..
        
        //Logger.info("##### considering whether to create stack frames for all fired rules.. ");
        // for each rule in our fire rule set, add a separate stacked result.. we do this as each rule was actually satisfied and future inference requests should consider them 
        // independently (ie separate stack frames) to consider as starting rule points
        for (Map.Entry me: reasoner.getFiredRules().getBase().entrySet()) {
             Rule theRule = (Rule)me.getValue();             
             
             //if (theRule.getDoNotStack()) {
                //Logger.info("Rule:" + theRule + " is set to do not stack!");
             //}
             
            // if (theRule.getRuleId() != addingInferenceResultForStack.getLastRule().getRuleId() && theRule.getRuleId() != 0) {
             if (!addingInferenceResultForStack.getBase().keySet().contains(theRule.getRuleId())&& theRule.getRuleId() != 0) {
                 //Logger.info("The rule " + theRule.getRuleId() + " was not in the temporary stack ruleset so adding it to a new stack");

                RuleSet theRuleSet = new RuleSet(theRule);
                 
                this.addStackedInferenceResult(this.currentCase.getCaseId(), theRuleSet,-1);
                //Logger.info("####2 stack size is now : : " + DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize());

             }
        } 
       
        if (DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize() == 1) {// we only have initial stack with root rule as inference result
            // No additional stack frames were created, so adding temporary stack frame to stack with root as parent           
            this.addStackedInferenceResult(this.currentCase.getCaseId(), addingInferenceResultForStack,0);  // stack result was satisfied by root

        }
        else {            
            //Logger.info("**** Now adding the temporary frame to the stack");
            //this.addStackedInferenceResult(this.currentCase.getCaseId(), addingInferenceResultForStack,DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize());
            this.addStackedInferenceResult(this.currentCase.getCaseId(), addingInferenceResultForStack,startingStackSize); // seeing if this fixes issue with reporting which starting stack satisfied rule
            //Logger.info("####5 stack size is now : : " + DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize());

        }
        // save the fired ruleset for the current user..
        DialogMain.getDialogUserList().getCurrentDialogUser().setFiredRules(reasoner.getFiredRules());

    }
   
    
    /**
     * Inference with initialResult
     */
    public void inferenceWithInitialResult(){   
        //Logger.info("##### Inference from starting rules");

        // load the current fired ruleset
        this.reasoner.clearFiredRules();
        this.reasoner.setFiredRules(DialogMain.getDialogUserList().getCurrentDialogUser().getFiredRules());
 
        RuleSet addingInferenceResultForStack = new RuleSet();
        Set inferenceResults = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getBaseSet().keySet();
        int stackSize = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getBaseSet().size();
        
        int lastValidStackWithInferenceResult = 0;
        int count = 0;
        //Logger.info("##### starting iterating back through stacks..");
        // Get a list of iterator for backward iterating
        ListIterator<Integer> iterator = new ArrayList(inferenceResults).listIterator(stackSize);
        while (iterator.hasPrevious()){ 

            Integer key = iterator.previous();
            RuleSet applyingInitialInferenceResult = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(key).getInferenceResult();
            
            //Logger.info("##### Iteration: " + count + ", Looking at stack " + key);
            
            //for (Rule aRule: applyingInitialInferenceResult.getBase().values()) {
                //Logger.info("###### Stack " + key + " contains rule: " + aRule.getRuleId());
            //}
            
            // skip root rule stack
            if(applyingInitialInferenceResult.getLastRule().getRuleId()!=0 || key == 0){
                switch (this.method) {
                    case "SCRDR":           
                        // set target rule
                        Rule startingRule = this.ruleSet.getRule((Rule) applyingInitialInferenceResult.getLastRule());
                        this.reasoner.clearInferenceResult();
                        if(startingRule.getRuleId()!=0){
                            this.reasoner.setStartingRule(startingRule);
                        } else {
                            this.reasoner.clearStartingRule();
                        }
                        this.reasoner.inferenceWithStartingRule(startingRule);
                        Rule tmpInferenceResultRule = (Rule) this.reasoner.getInferenceResult();

                        if(tmpInferenceResultRule.isRootRule(newRule)){
                            this.reasoner.inferenceWithStartingRule(this.ruleSet.getRootRule());
                            tmpInferenceResultRule = (Rule) this.reasoner.getInferenceResult();
                        }
                        this.inferenceResult = tmpInferenceResultRule;
                        this.reasoner.setInferenceResult((Rule) this.inferenceResult);
                        this.learner.setInferenceResult(tmpInferenceResultRule);

                        addingInferenceResultForStack.addRule(tmpInferenceResultRule);
                        break;
                    case "MCRDR":                         
                        RuleSet initInferResult = (RuleSet) applyingInitialInferenceResult;
                        RuleSet finalInferenceResultRuleSet = new RuleSet();
                        
                        // set target rule
                        for (Rule aRule : initInferResult.getBase().values()) {
                            this.reasoner.clearInferenceResult();
                            startingRule = this.ruleSet.getRule(aRule);
                            
                           // Logger.info("###### Starting inference with rule: " +  startingRule.getRuleId());

                            // DPH 9/10/18 only infer if rule set to not be doNotStack
                            if (!checkForParentWithDoNotStackSet(startingRule)) {

                                if(startingRule.getRuleId()==0){
                                    this.reasoner.clearStartingRule();
                                } else {
                                    this.reasoner.setStartingRule(startingRule);
                                }

                                this.reasoner.inferenceWithStartingRule(startingRule);
                                RuleSet tmpInferenceResultRuleSet = (RuleSet) this.reasoner.getInferenceResult();
                                //if (tmpInferenceResultRuleSet.getBase().values().isEmpty())
                                    //Logger.info("##### inference starting with rule [" + startingRule.getRuleId() + "] did not find any satisfied rules: ");
                                //else
                                    //Logger.info("##### inference starting with rule [" + startingRule.getRuleId() + "] found the following satisfied rules: ");
                                
                                //for (Rule someRule: tmpInferenceResultRuleSet.getBase().values()) {
                                    //Logger.info("###### Rule: " + someRule.getRuleId());
                                //}
                                
                                for (int ruleId: tmpInferenceResultRuleSet.getBase().keySet()) {
                                    //if (tmpInferenceResultRuleSet.getBase().get(ruleId).getDoNotStack())
                                        //Logger.info("###### Adding rule [" + tmpInferenceResultRuleSet.getBase().get(ruleId) + "] to final inference result (& it has doNotStack set)");
                                    //else
                                        //Logger.info("###### Adding rule [" + tmpInferenceResultRuleSet.getBase().get(ruleId) + "] to final inference result");

                                    finalInferenceResultRuleSet.addRule(tmpInferenceResultRuleSet.getBase().get(ruleId));
                                }
                            }
                            //else {
                                //Logger.info("###### Starting rule [" + startingRule.getRuleId() + "] inherits doNotStack from parent so skipping inference start!");
                           // }

                            
                        }
                        this.inferenceResult = finalInferenceResultRuleSet;
                        this.reasoner.setInferenceResult((RuleSet) this.inferenceResult);
                        this.learner.setInferenceResult((RuleSet) this.reasoner.getInferenceResult());

                        addingInferenceResultForStack = finalInferenceResultRuleSet;
                        
                        break;    
                    
                }
                
                if(addingInferenceResultForStack.isEmpty()){
                    //Logger.info("  ##### inference with the current stack frame (" + key + ") found no satisfied rules..");
                } 
                else {
                    if(addingInferenceResultForStack.getLastRule().getRuleId()==0){
                        //Logger.info("##### inference with the current stack frame only satisfied the root rule.");
                        lastValidStackWithInferenceResult = 0;
                        //inferenceResultsIterator.remove();
                    } 
                    else {
                        //DPH 2016 this.tmpStackedInferenceResult.clearSet();
                        // last rule inferenced was not root, so we have a valid result, exiting loop
                        lastValidStackWithInferenceResult = key;                       
                        break;
                    }
                }
            }
        }


        this.firedRules = this.reasoner.getFiredRules();
        this.firedRules.setRootRuleTree();
        
        int startingStackSize = DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize()-1;  // used to report which stack frame we start from..
        //Logger.info("#### startingStackSize is: " + startingStackSize);

        //Logger.info("##### considering whether to create stack frames for all fired rules.. ");

        // for each rule in our fire rule set, add a stacked result if we don't have a stack entry already containing that rule..
        for (Map.Entry me: reasoner.getFiredRules().getBase().entrySet()) {
            Rule theRule = (Rule)me.getValue();
            //Logger.info("#### considering fired rule " + theRule.getRuleId() + " to have its own stack frame");
            //if (theRule.getDoNotStack()) {
                //Logger.info("Rule:" + theRule + " is set to do not stack!");
            //}
            
             boolean found = false;   // dear me - should check over all rules in addingInferenceResultForStack instead of just the last rule..
             if (!addingInferenceResultForStack.getBase().keySet().contains(theRule.getRuleId()) && theRule.getRuleId() != 0) {
                 //Logger.info("The rule " + theRule.getRuleId() + " was not in the temporary stack ruleset so adding it to a new stack");

                 RuleSet theRuleSet = new RuleSet(theRule);
                 // check to see if we have a context that already has this rule..
                 for (MCRDRStackResultInstance instance : DialogMain.getDialogUserList().getCurrentStackInferenceResult().getBaseSet().values()) {
                     //if (instance.getInferenceResult().getLastRule().getRuleId() == theRule.getRuleId()) {
                     for (Rule aRule: instance.getInferenceResult().getBase().values()) {
                        if (aRule.getRuleId() == theRule.getRuleId()) {
                            found = true;
                            //Logger.info("##### But rule " + aRule.getRuleId() + " already has its own stack frame, so not adding another");
                            break;
                        }
                     }
                     if (found)
                        break;
                 }
                 
                 if (!found) {
                    this.addStackedInferenceResult(this.currentCase.getCaseId(), theRuleSet,-1);
                    //Logger.info("#### stack size is now : : " + DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize());

                 }
             }
        } 

        //Logger.info("#### We might have added some interim stack frames - " +  (DialogMain.getDialogUserList().getCurrentStackedMCRDRInferenceResultSize() - startingStackSize) + " frames were created"  );
        //Logger.info("**** Now adding the temporary frame to the stack");
        //this.addStackedInferenceResult(this.currentCase.getCaseId(), addingInferenceResultForStack, lastValidStackWithInferenceResult);
        //Logger.info("######## jsut before adding stack frame, startingStackSize is " + startingStackSize + " and lastValidStackWithInferenceResult is " + lastValidStackWithInferenceResult);
        //this.addStackedInferenceResult(this.currentCase.getCaseId(), addingInferenceResultForStack, startingStackSize);
        this.addStackedInferenceResult(this.currentCase.getCaseId(), addingInferenceResultForStack, lastValidStackWithInferenceResult);
        
        // seems I forgot to do this! March 2019
        DialogMain.getDialogUserList().getCurrentDialogUser().setFiredRules(reasoner.getFiredRules());

    }
    

    
    private static boolean checkForParentWithDoNotStackSet(Rule aRule) {
        boolean found = false;
        
        //rdr.logger.Logger.info("Checking rule: " + aRule.getRuleId());
        
        if (aRule.getDoNotStack()) {
            //Logger.info("Rule has doNotStack set");
            return true;
        }
        
        if (aRule.getRuleId() == 0)
            return false;
      
        found = found || checkForParentWithDoNotStackSet(aRule.getParent());

        return found;
    }
    
    
    /**
     * Set rule in this work bench
     * @param rootRule 
     */
    public void setRootRule(Rule rootRule) {        
        this.ruleSet.setRootRule(rootRule);
        this.firedRules.setRootRule(rootRule);
    }
    
    /**
     * Set initial inferenced results
     * @param inferenceResult 
     */
    public void setInitialInferenceResult(Rule inferenceResult) {        
        this.initialInferenceResult = inferenceResult;
    }
    
    
    /**
     * Set initial inferenced results
     * @param inferenceResult
     */
    public void setInitialInferenceResult(RuleSet inferenceResult) {        
        this.initialInferenceResult = inferenceResult;
    }
    
    /**
     * Get initial inferenced results
     * @return 
     */
    public Object getInitialInferenceResult() {        
        return this.initialInferenceResult;
    }
    
    
    /**
     * Set inferenced results
     * @param inferenceResult
     */
    public void setInferenceResult(Rule inferenceResult) {        
        this.inferenceResult = inferenceResult;
        this.reasoner.setInferenceResult(inferenceResult);
        this.learner.setInferenceResult(inferenceResult);
    }
    
    
    /**
     * Set inferenced results
     * @param inferenceResult
     */
    public void setInferenceResult(RuleSet inferenceResult) {        
        this.inferenceResult = inferenceResult;
        this.reasoner.setInferenceResult(inferenceResult);
        this.learner.setInferenceResult(inferenceResult);
    }
    
    
    /**
     * Get inference results
     * @return 
     */
    public Object getInferenceResult() {
        switch (this.method) {
            case "SCRDR":
                return (Rule) this.inferenceResult;                
            case "MCRDR":
                return (RuleSet) this.inferenceResult;
        }
        throw new UnsupportedOperationException("No inference result."); 
    }
    
    /**
     * Get stacked inference result 
     * @param caseId
     * @param inferenceResultInstance
     * @param lastValidStackKey
     */
    // lastValidStackKey is used purely for reporting (which last stack instance gives a valid inference result)
    // it's used by IDSAdminGUI context viewer..
    public void addStackedInferenceResult(int caseId, RuleSet inferenceResultInstance, int lastValidStackKey) {
        //Logger.info("lastValidStackKey is " + lastValidStackKey);
        MCRDRStackResultInstance aMCRDRStackResultInstance = new MCRDRStackResultInstance();
        aMCRDRStackResultInstance.setCaseId(caseId);
        MCRDRStackResultSet currentStack = DialogMain.getDialogUserList().getCurrentStackInferenceResult();
        aMCRDRStackResultInstance.setStackId(currentStack.getSize());
        aMCRDRStackResultInstance.setInferenceResult(inferenceResultInstance);
        DialogMain.getDialogUserList().setCurrentSatisfiedBystackedMCRDRInferenceResultStackId(aMCRDRStackResultInstance.getStackId(), lastValidStackKey);
        
       // if(inferenceResultInstance.getLastRule().getRuleId()!=0){
       // DPH 11/10/18 - should not add inference result for a stopped rule!
        if (inferenceResultInstance.getLastRule().getRuleId()!=0 && !inferenceResultInstance.getLastRule().getIsStopped()){
            aMCRDRStackResultInstance.setIsRuleFired(true); 
            currentStack.addMCRDRStackResultInstance(aMCRDRStackResultInstance);
        }
        else {
            
            //Logger.info("DAVE******* - Adding an initial stack result with only root rule..");
            // DAVE - for some reason I had commented out to a frame only containing root rule (And added it anyway)
            // so next line should be guarded by if statement as below
            //currentStack.addMCRDRStackResultInstance(aMCRDRStackResultInstance);
            if (currentStack.getSize() == 0) {  // only add inference result with root rule as last rule if the current stack is empty..
                //Logger.info("Adding an initial stack result with only root rule..");
                currentStack.addMCRDRStackResultInstance(aMCRDRStackResultInstance);
            }
            else {
                //Logger.info("... but not actually adding stack result as it only contains the root rule in inference results..");

            }
        }      
    }
    
    /**
     *
     */
    public void generateMultipleStackedInferenceResults() {
        
    }
    
    /**
     * Get stacked inference result 
     * @return 
     */
    public MCRDRStackResultSet getStackedInferenceResult() {
        // DPH
        //return this.stackedInferenceResult;
        //Logger.info("Getting stacked inference result for " + DialogMain.getDialogUserList().getCurrentUsername());
        return DialogMain.getDialogUserList().getCurrentStackInferenceResult();
    }
    
    /**
     * Get stacked inference result 
     * @return 
     */
    public MCRDRStackResultInstance getLastMCRDRStackResultInstance() {
        // DPH
        //return this.stackedInferenceResult.getLastMCRDRStackResultInstance();
        return DialogMain.getDialogUserList().getCurrentStackInferenceResult().getLastMCRDRStackResultInstance();
    }
    
    
    
    
    /**
     * Get previous MCRDRStackResultInstance
     * @param stackKey
     * @return 
     */
    public MCRDRStackResultInstance getPreviousStackedResultInstance(int stackKey) {
        int curId = stackKey;
        curId--;
        // DPH
        //while(!this.stackedInferenceResult.getMCRDRStackResultInstanceById(curId).getIsRuleFired()){            
        while(!DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId).getIsRuleFired()){            
            curId--;
        }
        // DPH
        //return this.stackedInferenceResult.getMCRDRStackResultInstanceById(curId);
        return DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId);
    }
    
    /**
     * Get previous stacked inference result key id
     * @param stackKey
     * @return 
     */
    public int getPreviousStackedInferenceResultKeyId(int stackKey) {
        int curId = stackKey;
        
        //DPH May 2019
        if (stackKey > 1)
            curId--;
        // DPH
        //while(!this.stackedInferenceResult.getMCRDRStackResultInstanceById(curId).getIsRuleFired()){ 
        
        /*
        if (DialogMain.getDialogUserList().getCurrentStackInferenceResult() == null)
            Logger.info("******* getPreviousStackedInferenceResultKeyId: getCurrentStackedInferenceResult() is null");
        else if (DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId) == null) {
            Logger.info("******* getPreviousStackedInferenceResultKeyId: getMCRDRStackResultInstanceById(curId) is null");
        }
        else 
            Logger.info("******* getPreviousStackedInferenceResultKeyId: getMCRDRStackResultInstanceById(curId):" + DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId).toString());
       
        */
        /*while(this.getCurrentStackedInferenceResult().getMCRDRStackResultInstanceById(curId) != null){
            if (!this.getCurrentStackedInferenceResult().getMCRDRStackResultInstanceById(curId).getIsRuleFired()) {
                System.out.println("decrementing curID - it is now: " + (curId -1));
                curId--;
            }
        }*/
        
        
        while(curId >= 0 && !DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId).getIsRuleFired() ){
            //System.out.println("decrementing curID - it is now: " + (curId -1));
            curId--;
        }
        
        return curId;
    }
    
    /**
     * Get previous stacked inference result 
     * @param stackKey
     * @return 
     */
    public RuleSet getPreviousStackedInferenceResult(int stackKey) {
        int curId = stackKey;
        //Logger.info("SANITY: stackKey is " + stackKey);
  
        
        while(!DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId).getIsRuleFired()){  
            // DEAR ME - REVERSED THE CODE BELOW - if curID was zero it was being decremented to -1!
            
            //Logger.info("stack " + curId + " did not have fired rules!");
            if(curId==0){
                return new RuleSet(Main.KB.getRootRule());
            }
            curId--;
            
        }
        
        // DPH
        //return this.stackedInferenceResult.getMCRDRStackResultInstanceById(curId).getInferenceResult(); 
        return DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(curId).getInferenceResult(); 
    }
    
    
    /**
     * Get fired rules
     * @return 
     */
    public RuleSet getFiredRules() {
        return this.firedRules;
    }

    /**
     * Delete wrong conclusion for the knowledge acquisition
     */
    public void deleteWrongConclusion() {
        this.wrongConclusion = null;
        this.learner.deleteWrongConclusion();
    }

    /**
     * Set wrong conclusion for the knowledge acquisition
     * @param conclusion 
     */
    public void setWrongConclusion(Conclusion conclusion) {
        this.wrongConclusion = conclusion;
        this.learner.setWrongConclusion(conclusion);
    }
        
    /**
     * Get wrong conclusion for the knowledge acquisition
     * @return 
     */
    public Conclusion getWrongConclusion() {
        return this.wrongConclusion;
    }

    
    /**
     * Set new rule conclusion for the knowledge acquisition
     * @param conclusion 
     *
     */
    public void setNewRuleConclusion(Conclusion conclusion) {
        this.newRule.setConclusion(conclusion);
        this.learner.setNewRule(this.newRule);
    }

    
    /**
     * Set new rule for the knowledge acquisition
     * @param rule 
     *
     */
    public void setNewRule(Rule rule) {
        //Logger.info("Creating a new rule..");
        this.newRule = rule;
        this.learner.setNewRule(rule);
    }
    
    
    /**
     * Get rule for the knowledge acquisition
     * @return 
     * 
     */
    public Rule getNewRule() {
        return this.newRule;
    }
    
    
    /**
     * Returns true if new rule is ready for the knowledge acquisition
     * @return Returns true if new rule is ready for the knowledge acquisition
     * 
     */
    public boolean isNewRuleReady() {
        if(this.newRule.getConclusion().isSet()){
            return this.newRule.getConditionSet().getConditionAmount()>0;
        } else {
            return false;
        }
    }

    
    /**
     * Add exception rule under inference result.
     * @param targetConclusion
     */
//    public void addExceptionRuleUnderInferenceResult(Conclusion targetConclusion) {
//        this.setWrongConclusion(targetConclusion);
//        this.learner.addExceptionRule();
//        this.learner.getRuleSet().setRootRuleTree();
//        this.ruleSet.setRuleSet(this.learner.getRuleSet());
//    }
    
    /**
     * Execute adding rule.
     */
    public void executeAddingRule(boolean doNotStack){
        this.learner.executeAddingRule(doNotStack);
        this.ruleSet.setRuleSet(this.learner.getRuleSet());
        this.reasoner.setRuleSet(this.learner.getRuleSet());
    }
    
    public void executeAddingStopRule(){
        this.learner.executeAddingStopRule();
        this.ruleSet.setRuleSet(this.learner.getRuleSet());
        this.reasoner.setRuleSet(this.learner.getRuleSet());
    }
    
    /*
    public void setCurrentStackedInferenceResultIndex(int i) {
        currentStackedInferenceResultIndex = i;
    }
    
    public int getCurrentStackedInferenceResultIndex() {
        return currentStackedInferenceResultIndex;
    }
    */
    
    /*
    public MCRDRStackResultSet getCurrentStackedInferenceResult() {
        if (!stackedInferenceResultList.isEmpty())
            return stackedInferenceResultList.get(currentStackedInferenceResultIndex);
        else
            return null;
    }
*/
    /*
    public ArrayList<MCRDRStackResultSet> getStackedInferenceResultList() {
        return stackedInferenceResultList;
    }
    
    public void addStackedInferenceResult() {
        stackedInferenceResultList.add(new MCRDRStackResultSet());
    }
    
    public void removeStackedInferenceResult(int i) {
        if (stackedInferenceResultList.size() > i) {
            stackedInferenceResultList.remove(i);
            if (stackedInferenceResultList.isEmpty()) {
                setCurrentStackedInferenceResultIndex(-1);
            }
        }
    }
*/
    
    
    //public boolean checkIfInferenceWithInitialResult(RuleSet firedRules, Rule startingRule){   

    /**
     *
     * @param startingRule
     * @return
     */
    public boolean checkIfInferenceWithInitialResult(Rule startingRule){   
        
        // load the current fired ruleset
        this.reasoner.clearFiredRules();
        ArrayList<Rule> ruleList = new ArrayList<>();
        
        //Logger.info("I am being called to check inference for starting rule:" + startingRule.getRuleId() + " " + startingRule.getConditionSet().toString());
        
        //this.reasoner.addRuleToFiredRules(startingRule);
        
        Rule[]  rulePathArray = startingRule.getPath();
        Collections.reverse(Arrays.asList(rulePathArray));
 
        //Logger.info("Clearing fired rules...");
        for (Rule aRule: rulePathArray) {
            //Logger.info("Fired rule list now includes " + aRule.getRuleId() + " " + aRule.getConditionSet().toString() );
            this.reasoner.addRuleToFiredRules(aRule);

        }
  
       
        
        //for (Case aCase: Main.allCaseSet.getBase().values()) {
        //    Logger.info("Global case list: case " + aCase.getCaseId() + " value: " + aCase);
        //}
        //this.reasoner.setFiredRules(firedRules);
        boolean result = false;
  
        switch (this.method) {
            case "SCRDR":           
                // set target rule              
                this.reasoner.clearInferenceResult();
                if(startingRule.getRuleId()!=0){
                    this.reasoner.setStartingRule(startingRule);
                } else {
                    this.reasoner.clearStartingRule();
                }
                this.reasoner.inferenceWithStartingRule(startingRule);
                Rule tmpInferenceResultRule = (Rule) this.reasoner.getInferenceResult();

                if(tmpInferenceResultRule.isRootRule(newRule)){
                    this.reasoner.inferenceWithStartingRule(this.ruleSet.getRootRule());
                    tmpInferenceResultRule = (Rule) this.reasoner.getInferenceResult();
                }
                
                if (tmpInferenceResultRule.getRuleId() != 0)
                    result = true;

                break;
            case "MCRDR": 
                    this.reasoner.clearInferenceResult();

                    if(startingRule.getRuleId()==0){
                        this.reasoner.clearStartingRule();
                    } else {
                        //Logger.info("MCRDR - setting starting rule to: " + startingRule.getRuleId());
                        this.reasoner.setStartingRule(startingRule);

                    }

                    this.reasoner.inferenceWithStartingRule(startingRule);
                    RuleSet tmpInferenceResultRuleSet = (RuleSet) this.reasoner.getInferenceResult();
                    for (Rule aRule: tmpInferenceResultRuleSet.getBase().values()) {
                        //Logger.info("Test Inference results: " + aRule.getRuleId());
                    }

                    if (tmpInferenceResultRuleSet.getLastRule().getRuleId() != 0)
                        result = true;

                                    
                break;   
        }
        
        return result;
    }
                    
}

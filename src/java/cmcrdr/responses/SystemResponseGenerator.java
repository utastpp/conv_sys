/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.responses;

import cmcrdr.cases.DialogCase;
import cmcrdr.command.CommandFactory;
import cmcrdr.command.ICommandInstance;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.dialog.DialogArchiveModule;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.SystemDialogInstance;
import cmcrdr.dic.DicConverter;
import static cmcrdr.handler.OutputParser.METAHELP;
import static cmcrdr.handler.OutputParser.METANOHELP;
import static cmcrdr.handler.OutputParser.METAWHERE;
import cmcrdr.main.DialogMain;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import rdr.apps.Main;
import rdr.logger.Logger;
import rdr.reasoner.MCRDRStackResultInstance;
import rdr.reasoner.MCRDRStackResultSet;
import rdr.rules.Condition;
import rdr.rules.Rule;
import rdr.rules.RuleSet;
import rdr.workbench.Workbench;
import static cmcrdr.handler.OutputParser.META_CONTEXT_VAR_CONDITION;

/**
 * SystemResponse
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class SystemResponseGenerator extends Workbench{
    
    //private Object tempInferenceResult;
    //private Object initialInferenceResult;
    //private Object inferenceResult;
    //private HashMap<Integer, RuleSet> stackedInferenceResult;
    
    //private ProcessMapMatcher processMapMatcher;
    private DicConverter dicConverter;
    
    /**
     *
     */
    public SystemResponseGenerator() {
        super();
    }
    
    /**
     *
     * @param aCase
     * @return
     */
    public static SystemResponse generateResponse(DialogCase aCase) {
        SystemResponse aResponse = new SystemResponse();
        MCRDRStackResultSet currentStackedInferenceResult = null;
        
        //Logger.info("Generating a response for case: " + aCase.getCaseStatusString());
        
        Main.workbench.setCurrentCase(aCase);       
        currentStackedInferenceResult = Main.workbench.getStackedInferenceResult();

        if(currentStackedInferenceResult != null){
            if (currentStackedInferenceResult.getDoesValidStackExist()) {               
                Main.workbench.inferenceWithInitialResult();
            }
            else {  
                Main.workbench.setRuleSet(Main.KB);
                //Main.workbench.addStackedInferenceResult();
                DialogMain.getDialogUserList().getCurrentDialogUser().addInitialMCRDRStackResultInstance();
                //Main.workbench.setCurrentStackInferenceResult(DialogMain.getCurrentDialogUser().getMCRDRStackResultSet());
               // Main.workbench.setRuleSetForNewUser(Main.workbench.getCurrentStackedInferenceResultIndex(),Main.KB);       
                Main.workbench.inference();
            }
        } else {   
            Main.workbench.setRuleSet(Main.KB);
            DialogMain.getDialogUserList().getCurrentDialogUser().addInitialMCRDRStackResultInstance();

            //Main.workbench.addStackedInferenceResult();
            //DialogMain.getCurrentDialogUser().addInitialMCRDRStackResultInstance();
            //Main.workbench.setCurrentStackInferenceResult(DialogMain.getCurrentDialogUser().getMCRDRStackResultSet());


            //Main.workbench.setRuleSetForNewUser(Main.workbench.getCurrentStackedInferenceResultIndex(),Main.KB);       
            //Logger.info("Requesting inference for " + aCase);
            Main.workbench.inference();
        }
        
        RuleSet inferenceResult = (RuleSet)Main.workbench.getInferenceResult();
        
        //Logger.info("Inference Result conclusion is: " + inferenceResult.toStringOnlyConclusion());
        aResponse.setInferenceResult(inferenceResult);
        
        // DAVE March 2019 - testing returning stackId 0 if no fire rules..
        // (previously stackId was set via statement in else clause)
        int stackId;
        if (inferenceResult.getLastRule().getRuleId() == 0) {
            //Logger.info("DAVE **** Setting stackId to zero!");
            stackId = 0;
        }
        else {

            stackId = Main.workbench.getStackedInferenceResult().getLastMCRDRStackResultInstance().getStackId();
            //Logger.info("**** Setting stackId to last valid stack id: " + stackId);

        }
        

        int i = 0;
        for (MCRDRStackResultInstance set : Main.workbench.getStackedInferenceResult().getBaseSet().values())
        {
            //Logger.info("###### Current stack: " + i );
            for (Rule aRule: set.getInferenceResult().getBase().values()) {
                //Logger.info("###### Rule: " + aRule.getRuleId());
            }
            i++;
        }
        
        
        String responseDialog = "";
        String responseDevice = "";
        String responseAction = "";
        
        // Look at all the fired rules in the inference result and generate replies..
        for (Rule inferencedRule : inferenceResult.getBase().values()) {      

            if (!responseDialog.isEmpty())
                responseDialog += "\\n";
            
            if (!responseDevice.isEmpty())
                responseDevice += "\\n";

            if (!responseAction.isEmpty())
                responseAction += "\\n";

            String conclusionName = inferencedRule.getConclusion().getConclusionName();

            if(conclusionName.contains("^")){
                String[] conclusionArray = conclusionName.split("\\^");
                responseDialog += conclusionArray[0];     
                if(conclusionArray.length>1){
                    responseDevice +=conclusionArray[1];
                    if(conclusionArray.length>2){
                        responseAction += conclusionArray[2];
                    }
                    else {
                        responseAction += " ";  // want to ensure action array matches device array in terms of element index..
                    }
                }


                if(!DialogMain.dicConverter.isRecentListEmpty()){
                    responseDialog = DialogMain.dicConverter.replaceFromRepresentativeToRecentMatchingTerm(responseDialog);
                }     
            } else {
                responseDialog += conclusionName;
                if(responseDialog.equals("") && aCase.getInputDialogInstance().getDialogTypeCode()==DialogInstance.USER_TYPE){
                    responseDialog = "I'm sorry, I don't understand..";
                } else {

                    if(!DialogMain.dicConverter.isRecentListEmpty()){
                        responseDialog = DialogMain.dicConverter.replaceFromRepresentativeToRecentMatchingTerm(responseDialog);
                    }     
                }
            }
        }
        
        SystemDialogInstance systemDialogInstance = DialogArchiveModule.archiveSystemDialogInstance(aCase, aCase.getCaseId(), responseDialog);
        
        systemDialogInstance.setStackId(stackId);
        

        
        if(!DialogMain.dicConverter.isRecentListEmpty()){
            responseAction = DialogMain.dicConverter.replaceFromRepresentativeToRecentMatchingTerm(responseAction);
        }        

        if(responseDevice.equals("")){
            ICommandInstance aCommandInstance = CommandFactory.createCommandInstance("");
            aCommandInstance.setCommand(responseDevice, responseAction);
            // DPH May 2019
            aResponse.addResponseCommandInstance(aCommandInstance);
            //aResponse.setResponseCommandInstance(aCommandInstance);
        } else {
            Logger.info("responseDevice is: " + responseDevice);
            
            // DPH May 2019 - need to iterate over a possible number of commands due to MCRDR in the overall response
            String[] responseDevices = responseDevice.split("\\\\n");
            String[] responseActions = responseAction.split("\\\\n");

            int count = 0;
            for (String aDevice : responseDevices) {
                Logger.info("Creating a device found from split: " + aDevice);
                ICommandInstance aCommandInstance = CommandFactory.createCommandInstance(aDevice);
                if (aCommandInstance == null)
                    Logger.info("aCommandInstance is: null");

                aCommandInstance.setCommand(aDevice, responseActions[count]);
                aResponse.addResponseCommandInstance(aCommandInstance);
                count++;
            }
            
            /*ICommandInstance aCommandInstance = CommandFactory.createCommandInstance(responseDevice);
            if (aCommandInstance == null)
                            Logger.info("aCommandInstance is: null");

            aCommandInstance.setCommand(responseDevice, responseAction);
            aResponse.setResponseCommandInstance(aCommandInstance);*/
        }
        
        aResponse.setResponseDialogInstance(systemDialogInstance);
        
        DialogMain.getDialogUserList().getCurrentDialogUser().setPreviousInputDialogString(DialogMain.getDialogUserList().getCurrentDialogUser().getCurrentInputDialogString());
        DialogMain.getDialogUserList().getCurrentDialogUser().setPreviousInferenceSuccess(DialogMain.getDialogUserList().getCurrentDialogUser().getCurrentInferenceSuccess());
        DialogMain.getDialogUserList().getCurrentDialogUser().setCurrentInputDialogString(aCase.getInputDialogInstance().getDialogStr());
        
        if (aResponse.getInferenceResult().getLastRule().getRuleId() == 0) {
            //Logger.info("No inference result, so storing user recent input for later: " + aCase.getInputDialogInstance().getDialogStr());
            DialogMain.getDialogUserList().getCurrentDialogUser().setCurrentInferenceSuccess(false);
            String potentialReplies = promptUserWithPotentialValidRulesInLastContext(false);
            //String testChildInferenceReplies = promptUserWithPotentialValidRulesInChildContexts(false);
            SystemDialogInstance systemReply = aResponse.getResponseDialogInstance();
            
            //if (!testChildInferenceReplies.isEmpty()) {
               // systemReply.setDialogStr("I'm sorry, I don't understand.  Try specifying one of the following first eg." + testChildInferenceReplies);
            //}
            //else
            
              
                
            //Logger.info("Current user is:" +  DialogMain.getDialogUserList().getCurrentUsername());
            //Logger.info("Calling getCurrentContextVariables1...");
            ContextVariableUser userPrompting = DialogMain.getDialogUserList().getCurrentContextVariables().get("@prompting");
            //ContextVariableUser var = DialogMain.getDialogUserList().getCurrentContextVariables().get(userVarName);
            //Logger.info("Calling getCurrentContextVariables2...");
            for (ContextVariableUser aContextVar : DialogMain.getDialogUserList().getCurrentContextVariables().values()) {
                Logger.info("User context vars found: " + aContextVar.getVariableName() + " with value: " + aContextVar.getValue());             
            }
            //for (ContextVariable aContextVar : DialogMain.globalContextVariableManager.getContextVariables().values()) {
            //    Logger.info("global context vars: " + aContextVar.getVariableName() + " with value(s): ");
            //    for (String aValue: aContextVar.getValuesBase().values()) {
            //        Logger.info("                        " + aValue);
            //    }                          
            //}
           // ContextVariable userPrompting = DialogMain.globalContextVariableManager.getContextVariables().get("@prompting");
            ContextVariable systemPrompting = DialogMain.globalContextVariableManager.getSystemContextVariables().get("@SYSTEMprompting");
            boolean usePrompting = false;
            if (userPrompting != null) { // use user value... 
                usePrompting = ("true".equals(userPrompting.getValue()));
                //Logger.info("prompting value is " + userPrompting.getValue());
            }
            else if (systemPrompting != null) {
                usePrompting = ("true".equals(systemPrompting.getSingleVariableValue()));
                //Logger.info("@SYSTEMprompting value is " + systemPrompting.getSingleVariableValue());
            }
            //Logger.info("Prompting is " + usePrompting);
            
            
            if (potentialReplies.isEmpty()) {
                systemReply.setDialogStr("I'm sorry, I don't understand.");
            }
            else if (usePrompting) {
                systemReply.setDialogStr("I'm sorry, I don't understand.  You could try - e.g. " + potentialReplies);
            }
            //aResponse.setResponseDialogInstance(new SystemDialogInstance());
        }
        else {
            DialogMain.getDialogUserList().getCurrentDialogUser().setCurrentInferenceSuccess(true);
        }
        
        // DPH 9/10/18
        // Remove rules in the last stack result that have doNotStack set (or a parent rule with it set)
//        MCRDRStackResultInstance lastStackFrame = Main.workbench.getStackedInferenceResult().getLastMCRDRStackResultInstance();
//        if (lastStackFrame != null) {
//            RuleSet rules = lastStackFrame.getInferenceResult();
//            for (Rule inferencedRule : rules.getBase().values()) {     
//               if (checkForParentWithDoNotStackSet(inferencedRule)) {
//                   Logger.info("Removing rule: " + inferencedRule + " from last stacked result");
//                   rules.getBase().remove(inferencedRule.getRuleId());
//               }
//            }
//        }
//        // should test to see if the stack result set is now empty?
//        RuleSet rules = lastStackFrame.getInferenceResult();
//        if (rules.isEmpty()) {
//            Logger.info("No more rules!");
//            // remove the last stacked result
//            Logger.info("Current stack frames:");
//            for (MCRDRStackResultInstance aStackFrame: Main.workbench.getStackedInferenceResult().getBaseSet().values()) {
//                Logger.info("Stack: " + aStackFrame.getStackId());
//            }
//            Main.workbench.getStackedInferenceResult().getBaseSet().remove(stackId);
//            stackId = Main.workbench.getStackedInferenceResult().getLastMCRDRStackResultInstance().getStackId();
//            systemDialogInstance.setStackId(stackId);
//            Logger.info("Post deletion stack frames:");
//            for (MCRDRStackResultInstance aStackFrame: Main.workbench.getStackedInferenceResult().getBaseSet().values()) {
//                Logger.info("Stack: " + aStackFrame.getStackId());
//            }
//        }
//        else
//        for (Rule inferencedRule : rules.getBase().values()) {  
//            Logger.info("Remaining Rules after deletion:" + inferencedRule.getRuleId());
//        }
        
        
        return aResponse;
    }
    
    
    
    /**
     *
     * @param numOfItems
     * @param maxNumber
     * @return
     */
    public static Set getRandomSet(int numOfItems, int maxNumber) {
        Random random = new Random();
        Set set = new HashSet<>(numOfItems);
        while(set.size()< numOfItems) {
            int theNumber = random.nextInt(maxNumber);
            set.add(theNumber);
           // Logger.info("Try to add " + theNumber + " to the set..");
        }
        return set;
    }
    
        // If the user gets no inference results, then suggest conditions from the last successful inference result..

    /**
     *
     * @param allRules
     * @return
     */
 
    public static String promptUserWithPotentialValidRulesInLastContext(boolean allRules) {
        String result = "";
        MCRDRStackResultInstance lastStackResult = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getLastMCRDRStackResultInstance();

        int numOfSuggestions;
        int numOfChildren;
        Set set;
        int i;
        
        RuleSet theRuleSet;
        Rule theLastRule;
        int stackId = 0;
        
        if (lastStackResult != null) {    
            // get the last stack result that didn't have a "META" conclusion (help, where, or the nohelp flag)
            theRuleSet = lastStackResult.getInferenceResult();
            theLastRule = theRuleSet.getLastRule();

            stackId = lastStackResult.getStackId();
            while ((stackId != 0) && (theLastRule.getRuleId() == 0  || theLastRule.getConclusion().getConclusionValue().toString().contains(METAHELP) 
                    || theLastRule.getConclusion().getConclusionValue().toString().contains(METAWHERE)
                    || theLastRule.getConclusion().getConclusionValue().toString().contains(METANOHELP))) {
                stackId--;
                lastStackResult = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(stackId);
                theRuleSet = lastStackResult.getInferenceResult();
                theLastRule = theRuleSet.getLastRule();               
            }
        }
        //}
        
        if (lastStackResult == null) {
            // we're dealing with the root rule..
            numOfChildren = Main.KB.getRootRule().getChildRuleCount();
            if (allRules)
                numOfSuggestions = numOfChildren;
            else
                numOfSuggestions = Integer.min(5, numOfChildren);

            set = getRandomSet(numOfSuggestions, numOfChildren);
            //i = 0;
            for (Object childIndex:  set.toArray()) {
                Rule aChild = Main.KB.getRootRule().getChildAt((int)childIndex);
                String conditionResult = "";
                if (!aChild.getConclusion().getConclusionValue().toString().contains(METANOHELP) && !aChild.getDoNotStack()) {
                    for (Condition aCondition : aChild.getConditionSet().getBase()) {
                        
                        String conditionTerm = aCondition.getValue().toString();                                
                        // replaced getFirstMatchForRepresentationTerm with getRandomMatchForRepresentationTerm
                        if (!aCondition.getAttribute().getName().contains(META_CONTEXT_VAR_CONDITION)) {  // don't offer suggestion for contextvar condition
                            if (conditionResult.isEmpty())
                                conditionResult += "\"" + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);
                            else
                                conditionResult += " " + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);
                        }
                    }
                    if (!conditionResult.isEmpty()) 
                        conditionResult += "\"";
                    
                    if (result.isEmpty()) {
                        if (!result.contains(conditionResult))  // don't add duplicate suggestions..
                            result += conditionResult;
                    }
                    else {
                        if (!conditionResult.isEmpty()) {
                            if (!result.contains(conditionResult)) // don't add duplicate suggestions..
                                result += ", " + conditionResult;
                        }
                    }
                }
                    
            }
        }
        else {
            
            for (Rule aRule : lastStackResult.getInferenceResult().getBase().values()) {
                Rule currentRule = aRule;
                numOfChildren = currentRule.getChildRuleCount();
                if (numOfChildren == 0) {
                    currentRule = aRule.getParent();
                    if (currentRule != null)
                        numOfChildren = currentRule.getChildRuleCount();
                    // if the current rule has no children, get its parent and then look at those children.
                }
                
                if (currentRule != null) {
                    if (allRules)    
                        numOfSuggestions = numOfChildren;
                    else
                        numOfSuggestions = Integer.min(5, numOfChildren);
                    
                    set = getRandomSet(numOfSuggestions, numOfChildren);
                    //i = 0;
                    for (Object childIndex: set.toArray()) {
                        Rule aChild = currentRule.getChildAt((int)childIndex);
                        String conditionResult = "";
                        if (!aChild.getConclusion().getConclusionValue().toString().contains(METANOHELP) && !aChild.getDoNotStack()) {
                            for (Condition aCondition : aChild.getConditionSet().getBase()) {
                                String conditionTerm = aCondition.getValue().toString();
                                if (!aCondition.getAttribute().getName().contains(META_CONTEXT_VAR_CONDITION)) {  // don't offer suggestion for contextvar condition

                                    if (conditionResult.isEmpty())
                                        //conditionResult += "\"" + DialogMain.dicConverter.getFirstMatchForRepresentationTerm(conditionTerm);
                                        conditionResult += "\"" + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);
                                    else
                                        //conditionResult += " " + DialogMain.dicConverter.getFirstMatchForRepresentationTerm(conditionTerm);
                                        conditionResult += " " + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);
                                }
                            }
                            
                            if (!conditionResult.isEmpty())
                                conditionResult += "\"";

                            if (result.isEmpty()) {
                                if (!result.contains(conditionResult)) // don't add duplicate suggestions..
                                    result += conditionResult;
                            }
                            else {
                                if (!conditionResult.isEmpty()) {
                                    if (!result.contains(conditionResult)) // don't add duplicate suggestions..
                                        result += ", " + conditionResult;
                                }
                            }
                        }
                        //if (i != numOfSuggestions - 1)
                            //result += ", ";
                        //i++;
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     *
     * @return
     */
    public static String promptUserWithPotentialValidRulesInChildContexts(boolean allRules) {   // not used??
        String result = "";
        MCRDRStackResultInstance lastStackResult = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getLastMCRDRStackResultInstance();

        int numOfSuggestions;
        int numOfChildren;
        Set set;
        int i;
        
        if (lastStackResult == null) {
            // we're dealing with the root rule..
            //numOfSuggestions = Integer.min(5, numOfChildren);
            i = 0;
            // for each child, pretend it's fired and then check inference results for grandchildren
            for (Rule aChild:  Main.KB.getRootRule().getChildRuleList()) {
                RuleSet firedRuleList = new RuleSet();
                firedRuleList.addRule(aChild);
                //Logger.info("Adding rule " + aChild.getRuleId() + " to fired list..");
                
                    if (Main.workbench.checkIfInferenceWithInitialResult(aChild)) {
                        // We would have an inference result from a grandchild if the child rule was satisified, so suggest the child rule's conditions as a prompt..
                        //Logger.info("inference suceeded for " + aChild.getRuleId());

                        String conditionResult = "";
                        for (Condition aCondition : aChild.getConditionSet().getBase()) {
                            String conditionTerm = aCondition.getValue().toString();

                            // replaced getFirstMatchForRepresentationTerm with getRandomMatchForRepresentationTerm

                            if (conditionResult.isEmpty())
                                conditionResult += "\"" + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);
                            else
                                conditionResult += " " + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);

                        }
                        conditionResult += "\"";
                        result += conditionResult;
                        //if (i != numOfSuggestions - 1)
                            result += " ";
                        i++;
                    }
                
            }
        }
        else {
            // get the last inference result that didn't have root as the last rule.. otherwise just use the root rule if nothing else is found
            int stackId = lastStackResult.getStackId();
            while (lastStackResult.getInferenceResult().getLastRule().getRuleId() == 0 && stackId != 0) {
                stackId--;
                lastStackResult = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getMCRDRStackResultInstanceById(stackId);
            }
            
            
            for (Rule aRule : lastStackResult.getInferenceResult().getBase().values()) {
                Rule currentRule = aRule;
                //numOfChildren = currentRule.getChildRuleCount();
                //if (numOfChildren == 0) {
                //    currentRule = aRule.getParent();
                //    if (currentRule != null)
                //        numOfChildren = currentRule.getChildRuleCount();
                    // if the current rule has no children, get its parent and then look at those children.
                //}
                    
                //numOfSuggestions = Integer.min(5, numOfChildren);
                //set = getRandomSet(numOfSuggestions, numOfChildren);
                i = 0;              
                // for each child, pretend it's fired and then check inference results for grandchildren
                if (currentRule != null) {
                    for (Rule aChild:  currentRule.getChildRuleList()) {
                        RuleSet firedRuleList = new RuleSet();
                        firedRuleList.addRule(aChild);
                        //Logger.info("Adding rule " + aChild.getRuleId() + " to fired list..");

                        //for (Rule aGrandchild: aChild.getChildRuleList()) {
                            //Logger.info("Checking inference for child rule " + aChild.getRuleId() + " " + aChild.getConclusion().toString());

                            if (Main.workbench.checkIfInferenceWithInitialResult(aChild)) {
                                // We would have an inference result from a grandchild if the child rule was satisified, so suggest the child rule's conditions as a prompt..
                                //Logger.info("inference suceeded for " + aChild.getRuleId());

                                String conditionResult = "";
                                for (Condition aCondition : aChild.getConditionSet().getBase()) {
                                    String conditionTerm = aCondition.getValue().toString();
                                    
                                   // replaced getFirstMatchForRepresentationTerm with getRandomMatchForRepresentationTerm


                                    if (conditionResult.isEmpty())
                                        conditionResult += "\"" + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);
                                    else
                                        conditionResult += " " + DialogMain.dicConverter.getRandomMatchForRepresentationTerm(conditionTerm);

                                }
                                conditionResult += "\"";
                                result += conditionResult;
                                //if (i != numOfSuggestions - 1)
                                    result += " ";
                                i++;
                            }
                        //}
                    }  
                }
            }
        }
        return result;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.user;

import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.dialog.DialogSet;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.handler.InputHandler;
import cmcrdr.handler.OutputHandler;
import cmcrdr.main.DialogMain;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import rdr.apps.Main;
import rdr.logger.Logger;
import rdr.reasoner.MCRDRStackResultInstance;
import rdr.reasoner.MCRDRStackResultSet;
import rdr.rules.Rule;
import rdr.rules.RuleSet;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class DialogUser {

    /**
     *
     */
    public static enum UserSourceType { 
        MANUAL,
        REMOTEPOST,
        WEB};
    
    private String userName;
    private int userId;
    private UserSourceType userType;   
    private DialogSet dialogRepository;
    private InputHandler inputHandler;
    private OutputHandler outputHandler;
    private String converstationHistory;
    private LinkedHashMap<Integer,LinkedHashMap<String,ContextVariableUser>> contextVariables;
    private LinkedHashMap<String,ContextVariableUser> recentContextVariables;
    private Date userTimeout;
    private MCRDRStackResultSet stackedMCRDRInferenceResult;
    private RuleSet firedRules = new RuleSet();
    private RuleSet KAapplyingInferenceResult;
    
    private String currentInputDialogString;
    private boolean currentInferenceSucceeded;
    private String previousInputDialogString;
    private boolean previousInferenceSucceeded;
    
    private LinkedHashMap<Integer,Integer> satisfiedBystackedMCRDRInferenceResultStackId = new LinkedHashMap<>();
 
    /**
     *
     * @param username
     * @param userId
     * @param type
     * @param accessTime
     */
    public DialogUser(String username, int userId, UserSourceType type, Date accessTime) {
        this.userName = username;
        this.userId = userId;
        this.userType = type;
        this.userTimeout = accessTime;
        dialogRepository = new DialogSet();
        inputHandler = new InputHandler();
        outputHandler = new OutputHandler();
        //contextVariables = new LinkedHashMap<>();
        contextVariables = new LinkedHashMap<>();
        converstationHistory = "";
        currentInputDialogString = "";
        currentInferenceSucceeded = true;        
        previousInputDialogString = "";
        previousInferenceSucceeded = true;       
        stackedMCRDRInferenceResult = new MCRDRStackResultSet();
        //addInitialMCRDRStackResultInstance();
    }
    
    public void resetUser() {
        Logger.info("Resetting user " + this.userName);
        dialogRepository = new DialogSet();
        inputHandler = new InputHandler();
        outputHandler = new OutputHandler();
        //contextVariables = new LinkedHashMap<>();
        contextVariables = new LinkedHashMap<>();
        converstationHistory = "";
        currentInputDialogString = "";
        currentInferenceSucceeded = true;        
        previousInputDialogString = "";
        previousInferenceSucceeded = true;       
        stackedMCRDRInferenceResult = new MCRDRStackResultSet();
    }
    
    /**
     *
     */
    public void resetTimeout() {
        Date theTime = Calendar.getInstance().getTime();
        this.userTimeout = theTime;
    }
    
    /**
     *
     * @return
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     *
     * @return
     */
    public String getUsername() {
        return userName;
    }
    
    /**
     *
     * @return
     */
    public MCRDRStackResultSet getMCRDRStackResultSet() {
        return stackedMCRDRInferenceResult;
    }
    
    /**
     *
     * @param stack
     */
    public void setMCRDRStackResultSet(MCRDRStackResultSet stack) {
        stackedMCRDRInferenceResult = stack;
    }    
    
    /**
     *
     */
    public void clearMCRDRStackResultSet() {
        stackedMCRDRInferenceResult.clearSet();
    }
    
    /**
     *
     */
    public void addInitialMCRDRStackResultInstance() {
        MCRDRStackResultInstance aMCRDRStackResultInstance = new MCRDRStackResultInstance();
        aMCRDRStackResultInstance.setCaseId(0);
        aMCRDRStackResultInstance.setStackId(0);
        aMCRDRStackResultInstance.setInferenceResult(new RuleSet(Main.workbench.ruleSet.getRootRule()));
        //Logger.info("adding a new stack instance..");
        stackedMCRDRInferenceResult.addMCRDRStackResultInstance(aMCRDRStackResultInstance);
    }
    
    /**
     *
     * @return
     */
    public int getStackedMCRDRInferenceResultSize() {
        return stackedMCRDRInferenceResult.getSize();
    }
    
    /**
     *
     * @param ruleSet
     */
    public void setRuleSet(RuleSet ruleSet) {
        
        MCRDRStackResultInstance aMCRDRStackResultInstance = new MCRDRStackResultInstance();
        aMCRDRStackResultInstance.setCaseId(0);
        aMCRDRStackResultInstance.setStackId(0);
        aMCRDRStackResultInstance.setInferenceResult(ruleSet);
        //Logger.info("adding a new stack instance..");
        stackedMCRDRInferenceResult.addMCRDRStackResultInstance(aMCRDRStackResultInstance);
       // }
    }
    
    /**
     *
     * @return
     */
    public RuleSet getFiredRules() {
        return firedRules;
    }
    
    /**
     *
     * @param firedRules
     */
    public void setFiredRules(RuleSet firedRules) {
        this.firedRules = firedRules;
    }
    
    /**
     *
     * @return
     */
    public UserSourceType getUserType() {
        return userType;
    }
    
    /**
     *
     * @return
     */
    public DialogSet getDialogRepository() {
        return dialogRepository;
    }
    
    /**
     *
     * @return
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    /**
     *
     * @return
     */
    public OutputHandler getOutputHandler() {
        return outputHandler;
    }
    
    /**
     *
     * @return
     */
    public LinkedHashMap<String,ContextVariableUser> getRecentContextVariables() {
        return recentContextVariables;
    }
    
    /**
     *
     * @param recent
     */
    public void setRecentContextVariables(LinkedHashMap<String,ContextVariableUser> recent) {
        recentContextVariables = recent;
    }
    
    /**
     *
     * @return
     */
    public LinkedHashMap<Integer, LinkedHashMap<String,ContextVariableUser>> getContextVariablesList() {
        return contextVariables;
    }
    
    /**
     *
     * @return
     */
    public int getCurrentStackLevel() {
        int stackId = 0;
        
        IDialogInstance recentSystemDialogInstance = getDialogRepository().getSystemDialogRepository().getMostRecentSystemDialogInstance();
        if (recentSystemDialogInstance != null) 
            stackId = recentSystemDialogInstance.getStackId();
        
        return stackId;
    }
        
    /**
     *
     * @param currentStackLevel
     * @return
     */
    public int getPreviousStackLevel(int currentStackLevel) {
        int stackId = -1;
        
        //Logger.info("I am being called with currentStackLevel of " + currentStackLevel);
        
        if (currentStackLevel != 0) {
            int previous = 0;
                  
            for (int aStackId :  satisfiedBystackedMCRDRInferenceResultStackId.keySet()) {
                Logger.info("aStackId from  satisfiedBystackedMCRDRInferenceResultStackId is " + aStackId);

                if (aStackId == currentStackLevel) {
                    stackId = previous;
                    break;
                }
                //Logger.info("previous being set to aStackId:" + aStackId);
                       
                previous = aStackId;                
            }
        }
        //Logger.info("returning stackId of: " + stackId);

        return stackId;
    }
      
    /**
     *
     * @return
     */
    public int getLastInferencedRuleId() {
        int ruleId = 0;
        int processId;
        
        RuleSet applyingInferenceResult;
        Rule inferencedRule;
        RuleSet currentContextRuleSet = null;
        if (getDialogRepository().getSystemDialogRepository().getMostRecentSystemDialogInstance() != null) {
            processId = getDialogRepository().getSystemDialogRepository().getMostRecentSystemDialogInstance().getStackId();
            //Logger.info("ProcessId is: " + processId);
            applyingInferenceResult = getMCRDRStackResultSet().getMCRDRStackResultInstanceById(processId).getInferenceResult();
            inferencedRule = applyingInferenceResult.getLastRule();
            currentContextRuleSet = inferencedRule.getPathRuleSet();
            ruleId = currentContextRuleSet.getFirstRule().getRuleId();
        }
        return ruleId;        
    }
    

    
    public Rule getLastInferencedRule() {
        Rule result = null;
        int processId;
        
        RuleSet applyingInferenceResult;
        Rule inferencedRule;
        RuleSet currentContextRuleSet = null;
        if (getDialogRepository().getSystemDialogRepository().getMostRecentSystemDialogInstance() != null) {
            processId = getDialogRepository().getSystemDialogRepository().getMostRecentSystemDialogInstance().getStackId();
            applyingInferenceResult = getMCRDRStackResultSet().getMCRDRStackResultInstanceById(processId).getInferenceResult();
            inferencedRule = applyingInferenceResult.getLastRule();
            currentContextRuleSet = inferencedRule.getPathRuleSet();
            result = currentContextRuleSet.getFirstRule();
        }
        else {
            result = Main.workbench.getRuleSet().getRootRule();
        }
        
        return result;        
    }

    
    // this returns a new list consisting of variables at the current stackID.
    //public LinkedHashMap<String,ContextVariableUser> getContextVariables(boolean useOtherContext ) { 

    /**
     *
     * @return
     */
    public LinkedHashMap<String,ContextVariableUser> getContextVariables() { 
        //Logger.info("Looking to see if any contextual stackID variables");
        
        LinkedHashMap<String,ContextVariableUser> result = new LinkedHashMap<>();        
        LinkedHashMap<String,ContextVariableUser> currentContextLevelContextVariables;
        
        //int lastInferencedRuleId;
        //Rule lastInferencedRule;
        
        int stackID = getCurrentStackLevel();
        //Logger.info("Current stack level is " + stackID);
        
        //if (!useOtherContext) {
            // we need to get variables from root stackID down..
        //    lastInferencedRuleId = getLastInferencedRuleId();
        //    lastInferencedRule = getLastInferencedRule();
        //}
        //else {
           // Logger.info("Not getting user stackID variables, getting KA stackID location variables");
            //lastInferencedRuleId = KAapplyingInferenceResult.getLastRule().getRuleId();
           // lastInferencedRule = KAapplyingInferenceResult.getLastRule();
        //}
        
        
        currentContextLevelContextVariables = this.contextVariables.get(stackID);
        if (currentContextLevelContextVariables != null && currentContextLevelContextVariables.entrySet().size() >= 1) {
            Logger.info("   This stackID level (" + stackID + ") has the following variables set:");
            for (Map.Entry me: currentContextLevelContextVariables.entrySet()) {
                Logger.info("      Found variable: " + (String)me.getKey() + " with value: " + ((ContextVariableUser)me.getValue()).getValue());
                result.put((String)me.getKey(), ((ContextVariableUser)me.getValue()));
            }
        }
        
        int parentStackLevel = getPreviousStackLevel(stackID);
        while (parentStackLevel >= 0) {
            currentContextLevelContextVariables = this.contextVariables.get(parentStackLevel);
            if (currentContextLevelContextVariables != null) {
                for (Map.Entry me: currentContextLevelContextVariables.entrySet()) {
                    String parentKey = (String)me.getKey();
                    ContextVariableUser parentContextVariable = (ContextVariableUser)me.getValue();
                    String parentContextVariableValue =  parentContextVariable.getValue();
                                       
                    if (!result.containsKey(parentKey)) {
                            result.put(parentKey, parentContextVariable);
                            //Logger.info("   New variable found - Parent stackID level (" + parentStackLevel + ") with value: " + parentContextVariableValue);
                        }
                        else {
                            ContextVariableUser currentContextVariable = (ContextVariableUser)result.get(parentKey);
                            String currentContextVariableValue = currentContextVariable.getValue();
                            
                            if (!parentContextVariableValue.equals(currentContextVariableValue)) {
                                me.setValue(currentContextVariable);
                                //Logger.info("   My variable - " + parentKey + " with value: " + currentContextVariableValue + " overrides parent's value: " + parentContextVariableValue);

                            }
                        }
                }
            }
            parentStackLevel = getPreviousStackLevel(parentStackLevel);
        }
        
        /*if (lastInferencedRule != null) {
            Rule currentRule = lastInferencedRule;
            Rule parentRule = null;
            int parentRuleId;
            while (currentRule.isParentExist()) {
                parentRule = currentRule.getParent();
                parentRuleId = parentRule.getRuleId();
                Logger.info("Found a parent rule with id: " + parentRuleId);
                
                currentContextLevelContextVariables = this.contextVariables.get(parentRuleId);
                if (currentContextLevelContextVariables != null) {
                    for (Map.Entry me: currentContextLevelContextVariables.entrySet()) {
                        Logger.info("Found parent variable: " + (String)me.getKey() + " with value: " + ((ContextVariableUser)me.getValue()).getValue());
                        if (!result.containsKey((String)me.getKey())) {
                            result.put((String)me.getKey(), ((ContextVariableUser)me.getValue()));
                            Logger.info("Parent had a variable (" + (String)me.getKey() +") that this rule (" + lastInferencedRuleId + ") didn't, so copying..");
                        }
                        else {
                            Logger.info("Parent had a variable (" + (String)me.getKey() +") that this rule (" + lastInferencedRuleId + ") already had, so ignoring it..");
                        }
                    }
                }
                else {
                    Logger.info("   This parent (" + parentRuleId + ") has no stackID variables");
                }
                currentRule = parentRule;

            }
        } */
        
        return result;
    } 
    
    /**
     *
     * @param stackID
     * @return
     */
    public LinkedHashMap<String,ContextVariableUser> getContextVariables(int stackID) { 
       // Logger.info("Looking to see if any contextual stackID variables");
        
        LinkedHashMap<String,ContextVariableUser> result = new LinkedHashMap<>();
        
        LinkedHashMap<String,ContextVariableUser> currentContextLevelContextVariables;
        


        currentContextLevelContextVariables = this.contextVariables.get(stackID);
        if (currentContextLevelContextVariables != null && currentContextLevelContextVariables.entrySet().size() >= 1) {
            Logger.info("This stackID level (" + stackID + ") has the following variables set:");
            for (Map.Entry me: currentContextLevelContextVariables.entrySet()) {
                Logger.info("   Found variable: " + (String)me.getKey() + " with value: " + ((ContextVariableUser)me.getValue()).getValue());
                result.put((String)me.getKey(), ((ContextVariableUser)me.getValue()));
            }
        }
        
        int parentContext = getPreviousStackLevel(stackID);
        while (parentContext >= 0) {
            currentContextLevelContextVariables = this.contextVariables.get(parentContext);
            if (currentContextLevelContextVariables != null) {
                for (Map.Entry me: currentContextLevelContextVariables.entrySet()) {
                    String parentKey = (String)me.getKey();
                    ContextVariableUser parentContextVariable = (ContextVariableUser)me.getValue();
                    String parentContextVariableValue =  parentContextVariable.getValue();
                                       
                    if (!result.containsKey(parentKey)) {
                            result.put(parentKey, parentContextVariable);
                            Logger.info("   New variable found " +  parentKey + " - Parent stackID level (" + parentContext + ") with value: " + parentContextVariableValue);
                        }
                    else {
                            ContextVariableUser currentContextVariable = (ContextVariableUser)result.get(parentKey);
                            String currentContextVariableValue = currentContextVariable.getValue();
                            
                            if (!parentContextVariableValue.equals(currentContextVariableValue)) {
                                me.setValue(currentContextVariable);
                                Logger.info("   My variable - " + parentKey + " with value: " + currentContextVariableValue + " overrides parent's value: " + parentContextVariableValue);

                            }
                    }
                }
            }
            Logger.info("I'm calling parentStackLevel with " + parentContext);
            parentContext = getPreviousStackLevel(parentContext);
        }      
        return result;
    } 
    
    /**
     *
     * @return
     */
    public Date getUserTimeout() {
        return userTimeout;
    }
    
    /**
     *
     * @return
     */
    public String getConversationHistory() {
        return converstationHistory;
    }
    
    /**
     *
     * @param append
     */
    public void appendToConversationHistory(String append) {
        converstationHistory += append;
    }
    
    /**
     *
     */
    public void clearConversationHistory() {
        converstationHistory = "";
    }
    
    /**
     *
     * @param context
     * @return
     */
    public Object[][] getContextVariablesForList(int context) {
        //LinkedHashMap<String,ContextVariableUser> list = DialogMain.getDialogUserList().getCurrentContextVariables();
        
        // new test
        //LinkedHashMap<String,ContextVariableUser> currentContextVariables = getContextVariables(false);
        LinkedHashMap<String,ContextVariableUser> currentContextVariables = getContextVariables(context);
        //Object[][] data = new Object[contextVariables.size()][];
        Object[][] data = new Object[currentContextVariables.size()][];
        //Iterator iter = contextVariables.entrySet().iterator();
        Iterator iter = currentContextVariables.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            String varName = (String)me.getKey();
            String varValue = ((ContextVariableUser)me.getValue()).getValue();
            int originalContext = ((ContextVariableUser)me.getValue()).getContext();
            String [] row = {Integer.toString(originalContext), varName,varValue};
            data[i] = row;
            i++;
        }
     
        return data;
    }
    
    /**
     *
     * @param stackId
     * @return
     */
    public Object[][] getSystemContextVariablesForList(int stackId) {
        //LinkedHashMap<String,ContextVariableUser> list = DialogMain.getDialogUserList().getCurrentContextVariables();
        
        Rule lastInferencedRuleForContext = getMCRDRStackResultSet().getMCRDRStackResultInstanceById(stackId).getInferenceResult().getLastRule();
        int ruleIdForOverride = lastInferencedRuleForContext.getRuleId();
        // new test
        //LinkedHashMap<String,ContextVariableUser> currentContextVariables = getContextVariables(false);
        LinkedHashMap<String,ContextVariable> currentSystemContextVariables = DialogMain.globalContextVariableManager.getSystemContextVariables();
        //Object[][] data = new Object[contextVariables.size()][];
        Object[][] data = new Object[currentSystemContextVariables.size()][];
        //Iterator iter = contextVariables.entrySet().iterator();
        int i = 0;
        
        for (String key: currentSystemContextVariables.keySet()) {

            String varName = key;
            String defaultVarValue = currentSystemContextVariables.get(key).getSingleVariableValue();
            String varValue = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(varName, ruleIdForOverride).getSingleVariableValue();
            int ruleId;
            if (defaultVarValue.equals(varName)) {
                ruleId = 0;
            }
            else {
                ruleId = DialogMain.globalContextVariableManager.getSystemContextVariableOverrideParentId(getUserId(),varName,ruleIdForOverride);
            }
            String [] row = {Integer.toString(ruleId), varName,varValue};
            data[i] = row;
            i++;
        }  
        return data;
    }
   
    /**
     *
     * @return
     */
    public RuleSet getKAInferenceResult() {
        return KAapplyingInferenceResult;
    }
    
    /**
     *
     * @param inferenceResult
     */
    public void setKAInferenceResult(RuleSet inferenceResult) {
        KAapplyingInferenceResult = inferenceResult;
    }
    
    /**
     *
     * @param currentStackId
     * @param satisfiedByStackId
     */
    public void setSatisfiedBystackedMCRDRInferenceResultProcessId(int currentStackId, int satisfiedByStackId) {
        Logger.info("I'm being asked to set  StackId: " +  currentStackId + " satisfied by: " + satisfiedByStackId);
        
        if (satisfiedBystackedMCRDRInferenceResultStackId.isEmpty()) {
            Logger.info("satisfied list was empty, so adding: " +  currentStackId + " satisfied by: " + satisfiedByStackId);
            satisfiedBystackedMCRDRInferenceResultStackId.put(currentStackId, satisfiedByStackId);
        }
        else if (!satisfiedBystackedMCRDRInferenceResultStackId.containsKey(currentStackId)) {
            Logger.info("satisfied list did not contain " +  currentStackId + " key, so adding satisfied by: " + satisfiedByStackId);
            satisfiedBystackedMCRDRInferenceResultStackId.put(currentStackId, satisfiedByStackId);
        }
        else { 
                Logger.info("satisfied list already contained the key "+  currentStackId + " so not adding satisfied by: "+ satisfiedByStackId);
        }
    }
    
    /**
     * 
     *
     * @param currentStackId
     * @return
     */
    public int getSatisfiedBystackedMCRDRInferenceResultStackId(int currentStackId) {
        //Logger.info("satisfiedBystackedMCRDRInferenceResultStackId - currentStackId is:" + currentStackId);
        // DAVE March 2019
        if (currentStackId == 0)
            return 0;
        else
            return satisfiedBystackedMCRDRInferenceResultStackId.get(currentStackId);
    }
    
    /**
     *
     * @return
     */
    public String getPreviousInputDialogString() {
        return previousInputDialogString;
    }
    
    /**
     *
     * @param lastInput
     */
    public void setPreviousInputDialogString(String lastInput) {
        if (!DialogMain.dicConverter.containsMatchForRepresentativeTerm(lastInput,"/METAHELP/") && !DialogMain.dicConverter.containsMatchForRepresentativeTerm(lastInput,"/METAWHERE/") && !DialogMain.dicConverter.containsMatchForRepresentativeTerm(lastInput,"/requesthelp/")) {
        
            previousInputDialogString = lastInput;
        }
    }
    
    /**
     *
     * @return
     */
    public String getCurrentInputDialogString() {
        return currentInputDialogString;
    }
    
    /**
     *
     * @param current
     */
    public void setCurrentInputDialogString(String current) {
        currentInputDialogString = current;
    }
    
    /**
     *
     * @return
     */
    public boolean getPreviousInferenceSuccess() {
        return previousInferenceSucceeded;
    }
    
    /**
     *
     * @param success
     */
    public void setPreviousInferenceSuccess(boolean success) {
        previousInferenceSucceeded = success;
    }
    
    /**
     *
     * @return
     */
    public boolean getCurrentInferenceSuccess() {
        return currentInferenceSucceeded;
    }
    
    /**
     *
     * @param success
     */
    public void setCurrentInferenceSuccess(boolean success) {
        currentInferenceSucceeded = success;
    }
    
    
    
    public int getMostRecentDialogID() {                                         
        int dialogID = -1;
        if (dialogRepository.getSize()!=0){
            dialogID = dialogRepository.getUserDialogRepository().getMostRecentUserDialogInstance().getDialogId();                                
        } 
        
        return dialogID;
    }
    
    public void unsetContextVariable(String varName) {
        //LinkedHashMap<Integer, LinkedHashMap<String,ContextVariableUser>> contextVariables
        
        // iterate over all stack frames (stackID, the key used in the contextVariables LinkedHashMap) and remove the stackID variable if its name matches
        Logger.info("I have been called to unset " + varName);
        contextVariables.forEach((key,value) -> {
            //Logger.info("Current Key is " + key);
            //for (String aKey : value.keySet()) {
                //Logger.info("Found stackID level key:" + aKey);
            //}
            value.keySet().removeIf(e->(e.equals(varName))); 
        });
    }
    
    
    @Override
    public String toString() {
        return "User: " + getUsername() + ", userId: " + getUserId() + ", type: " + getUserType(); 
    }
}

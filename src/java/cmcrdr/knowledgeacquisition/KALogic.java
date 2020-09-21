/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.knowledgeacquisition;

import cmcrdr.cases.DialogCase;
import cmcrdr.cases.DialogCaseArchiveModule;
import cmcrdr.cases.DialogCaseGenerator;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableAction;
import cmcrdr.contextvariable.ContextVariableManager;
import cmcrdr.contextvariable.ContextVariableSystem;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.dialog.SystemDialogInstance;
import static cmcrdr.gui.AdminJavaGUI.getDialogIDSequenceInHistory;
import static cmcrdr.gui.AdminJavaGUI.syncLock;
import cmcrdr.handler.OutputParser;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.mysql.DBOperation;
import cmcrdr.processor.PreAndPostProcessorAction;
import cmcrdr.mysql.DBOperation;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListModel;
import rdr.apps.Main;
import rdr.cases.CornerstoneCaseSet;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.Condition;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;

/**
 *
 * @author dherbert
 * Class defines static methods to be used (where possible) by knowledge-acquisition GUI interfaces
 * such as AdminJAvaGUI, AddRuleGUI, KnwoeldgeAcquisitionServlet etc
 * Most code doesn't follow MVC design pattern so a bit hard..
 */
public class KALogic {
    private static Conclusion newConclusion = new Conclusion();
    private static ConclusionSet conclusionSet = new ConclusionSet();
    private static DialogCase currentCase = null;
    private static Condition newCondition = null;
    private static boolean isWrongConclusionSet=false;
    private static String mode;
    private static int targetStackId = 0;
    public static LinkedHashMap<String, ContextVariable> contextVariables = new LinkedHashMap<>();
    public static LinkedHashMap<String, ContextVariable> systemContextVariables = new LinkedHashMap<>();
    public static LinkedHashMap<Integer,LinkedHashMap<String,ContextVariableSystem>> systemContextVariableOverrides = new LinkedHashMap<>();
    public static ContextVariableManager globalContextVariableManager = new ContextVariableManager();
    //public static  DefaultListModel preAndPostProcessActionListModel = new DefaultListModel();
    
    public static String getConclusionPreview(String text) {
        String result;
        //Logger.info("preview text before is:" + text);
        result = OutputParser.parseOtherTerms(OutputParser.replaceAllDatabaseTerms(text,true),true);

        if (DBOperation.wasDatabaseError()) {
            result = result + "\n" + "Error: " + DBOperation.getLastDatabaseError();
            DBOperation.resetDatabaseError();
        }
        
        return result;
    }
    
    public static String constructNewConclusion(String conclusionText, String actionCategory, String actionCommand, String actionVariable) {
        String result = "";
        
        String conclusionType = "TEXT";
        String actionSummary = actionCommand;
        if (actionSummary == null)
            actionSummary = "";
        if (actionCategory == null)
            actionCategory = "";
        if (actionVariable == null)
            actionVariable = "";
        
        if(!actionSummary.equals("")){
            if (!actionVariable.equals(""))
                actionSummary += "_" + actionVariable;
        }
        //SPLITAGEDDON
        String conclusionName = conclusionText+"^"+actionCategory+"^"+actionSummary;     
        //Logger.info("conclusionName: " + conclusionName);
        
        IAttribute attribute = AttributeFactory.createAttribute(conclusionType);
        Value value = new Value(new ValueType(conclusionType), conclusionName);
        newConclusion = new Conclusion(attribute, value);
        
        if(conclusionSet.isExist(newConclusion)){
            result = "Conclusion already exists1";
            //Logger.info(result);

        } 
        else {
            if(Main.workbench.getWrongConclusion()!=null) {
                if(newConclusion.getConclusionName().equals(Main.workbench.getWrongConclusion().getConclusionName())){
                    result = "Conclusion already exists2";
                    //Logger.info(result);
                } 
                else {
                    //set new conclusion for the knowledge acquisition
                    Main.workbench.setNewRuleConclusion(newConclusion);
                    conclusionSet.addConclusion(newConclusion);
                    //Logger.info("Added conclusion 1");
                }
            } else {
                //set new conclusion for the knowledge acquisition
                Main.workbench.setNewRuleConclusion(newConclusion);
                conclusionSet.addConclusion(newConclusion);
                //Logger.info("Added conclusion 2");

            }
        }
        return result;
    }
    
    public static void learnerInit(){       
        // set the possible conclusion into conclusion set
        Main.workbench.setRuleSet(Main.KB);
        conclusionSet.setConclusionSet(Main.KB.getConclusionSet());
        
       
        Rule newRule = new Rule();
        
        CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
        cornerstoneCaseSet.addCase(currentCase);
        
        newRule.setCornerstoneCaseSet(cornerstoneCaseSet);        
        Main.workbench.setNewRule(newRule);
    }
    
    public static void setWrongConclusion( String conclusionName ) {        
        Conclusion wrongConclusion = conclusionSet.getConclusionByName(conclusionName);              
        Main.workbench.setWrongConclusion(wrongConclusion);
    }
    
    public static TableResponse getInferenceResult(String username, int selectedConclusion){
        TableResponse response = new TableResponse();
        boolean conclusionSelected = selectedConclusion >= 0;
        
                
        String theMode = "";
        
        isWrongConclusionSet=false; 
        Main.workbench.deleteWrongConclusion();
        
        // a slight mis-match in terms here so altered to match older code..
        if(mode.equals("add")){
            theMode = "alter"; 
        } else if(mode.equals("modify")){
            theMode = "exception";
        }
        
        RuleSet inferenceResult = new RuleSet();
        
        // the response has rows if we need to select a wrong conclusion
        // (if there are more than one)
        response.setStatus("OK");
        response.setHeader(new String[] {"ID","Conclusion"});
        String[] rowData;
        
        //MCRDRStackResultSet selectedUserInferenceResults = DialogMain.getDialogUserList().getDialogUser(username).getMCRDRStackResultSet();
        
        switch (Main.domain.getReasonerType()) {            
            case "MCRDR":
                //DPH June 2019 added this instead..
                inferenceResult = DialogMain.getDialogUserList().getCurrentKAInferenceResult();        
                        
                /*if (selectedUserInferenceResults.getMCRDRStackResultInstanceById(targetStackId) != null)
                    inferenceResult = (RuleSet) selectedUserInferenceResults.getMCRDRStackResultInstanceById(targetStackId).getInferenceResult();
                else {
                    inferenceResult = new RuleSet();
                    inferenceResult.addRule(Main.KB.getRootRule());
                }*/
                
                Main.workbench.setInferenceResult(inferenceResult); 
                Logger.info("Inferenced result contains the following rules:");
                for (Rule aRule: inferenceResult.getBase().values()) {
                    Logger.info("   rule" + aRule.getRuleId());
                }
                
                Logger.info("Inferenced result contains the following conclusions:");
                for (Conclusion aConclusion: inferenceResult.getConclusionSet().getBase().values()) {
                    Logger.info("   conclusion string" + aConclusion.toString());
                    Logger.info("   conclusion name" + aConclusion.getConclusionName());
                    
                }
                for (Rule aRule: inferenceResult.getBase().values()) {
                    Logger.info("   rule" + aRule.getRuleId());
                    Logger.info("   rule conclusion" + aRule.getConclusion().toString());                    
                }
                
                if (theMode.equals("exception")){
                    Logger.info("in exception section..");
                    String[] wrongConclusionArray = inferenceResult.getConclusionSet().toStringArrayForGUIWithoutAddConclusion();
                    String[] conclusionSections;
                    String conclusionCategory;
                    String conclusionAction;
                    if (wrongConclusionArray.length > 1) {
                        if (conclusionSelected) {
                            //Rule inferencedRule = inferenceResult.getLastRule();
                            //setWrongConclusion(inferencedRule.getConclusion().getConclusionName());
                            setWrongConclusion(wrongConclusionArray[selectedConclusion]);
                        }
                        else { // we have to go back to the user to select which conclusion is being targeted
                            int i = 0;
                           
                            for (String aConclusion: wrongConclusionArray) {
                                conclusionSections = aConclusion.split("\\^");
                                conclusionCategory = "";
                                conclusionAction = "";
                                
                                if (conclusionSections.length == 3) {
                                    conclusionCategory = " Category:" + conclusionSections[1];
                                    conclusionAction = " Action:" + conclusionSections[2];
                                }
                                
                                response.addRow(new String[] {""+i,conclusionSections[0] + conclusionCategory + conclusionCategory});
                                i++;
                            }
                            Logger.info("WARNING WARNING WARNING - multiple conclusions in inference result, need to select target rule for KA");
                            return response;
                        }
                    }
                    else if (wrongConclusionArray.length == 1) {
                        Rule inferencedRule = inferenceResult.getLastRule();
                        Logger.info("Setting wrong conclusion to " + inferencedRule.getConclusion().getConclusionName());
                        setWrongConclusion(inferencedRule.getConclusion().getConclusionName());
                    }   
                }   
                break;
            case "SCRDR":
                Rule inferenceRule = (Rule)Main.workbench.getInferenceResult();
                inferenceResult.addRule(inferenceRule);
                if(theMode.equals("exception")){
                    Main.workbench.setWrongConclusion(inferenceRule.getConclusion());
                }   
                break;
        }
        
        return response;
    }
    
    public static void submitRule(boolean stoppingRule, boolean doNotStack) {                                              
//        validateRule();
        if (stoppingRule) {
            Main.workbench.getNewRule().setIsStoppingRule(true);
            Main.workbench.getLearner().deleteAllConditionFromNewRule();
            constructNewCondition("","","","true");      
            Main.workbench.getLearner().addConditionToNewRule(newCondition);
            
            // this is here as adding stop rule was not an initial attibute, it was addded later..
            IAttribute attribute = AttributeFactory.createAttribute("Text");
            Value value = new Value(new ValueType("Text"), "STOPRULE^^");
            newConclusion  = new Conclusion(attribute, value);
            if (!conclusionSet.isExist(newConclusion)){
                conclusionSet.addConclusion(newConclusion);
            }
            
            Main.workbench.setNewRuleConclusion(newConclusion);
            
            addStopRule();
        }
        else {
            addRule(doNotStack);
        }
    }   
    
    public static boolean constructNewCondition(String attribute, String operator, String value, String stopped) {        
        boolean isValid = false;
        String newConAttr = attribute;
        String newConOper = operator;
        String newConVal = value;
        
        //Logger.info("Stopped is: " + stopped);
        
        if (stopped.equals("true")) {
            newCondition = RuleBuilder.buildRuleCondition(currentCase.getCaseStructure(), "STOP", "==", "true");
            return true;
        }
        
        newCondition = RuleBuilder.buildRuleCondition(currentCase.getCaseStructure(), newConAttr, newConOper, newConVal);
        
        // check whether the new condition is valid for this case
        isValid = newCondition.isSatisfied(currentCase);        
        return isValid;
    }
    
    public static String[] getAttributeOperators(String attribute) {
        //Logger.info("getAttributeOperators - attribute is:" + attribute);
        if (currentCase == null) {
            Logger.info("CURRENT CASE IS NULL!");
        }
        
        IAttribute selectedAttr = currentCase.getCaseStructure().getAttributeByName(attribute);
        
        //get potential operators
        String[] operatorsList = selectedAttr.getPotentialOperators();       
        return operatorsList;
    }
    
    public static  void addStopRule() {    
        Main.workbench.getNewRule().setIsStoppingRule(true);
        Main.workbench.executeAddingStopRule();
        Main.KB.setRuleSet(Main.workbench.getRuleSet());
        Main.KB.setRootRuleTree();
        Main.KB.retrieveCornerstoneCaseSet();
        
    }
    
    public static void addRule(boolean doNotStack) {
            Main.workbench.executeAddingRule(doNotStack);
            Main.KB.setRuleSet(Main.workbench.getRuleSet());
            Main.KB.setRootRuleTree();
            Main.KB.retrieveCornerstoneCaseSet();
    }
    
    
    
    public static void constructConclusion(String conclusion, String actionCategory, String action) {      
            //SPLITAGEDDON
            String selectedConclusionName = conclusion + "^" + actionCategory + "^" + action;
            //Logger.info("selectedConclusionName is: " + selectedConclusionName);
            newConclusion = conclusionSet.getConclusionByName(selectedConclusionName);
            //Logger.info("Trying to set conclusion  to:" + newConclusion.toString());

            Main.workbench.setNewRuleConclusion(newConclusion);
    }
    
    // determine if there was a valid system response found in the past preceeding the current user dialog
    public static boolean getIsValidPastSystemResponse(String sessionID) {                                         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(sessionID);
        boolean msg = false;
               
           
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
            //IDialogInstance prevSystemDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(KADialogInstance.getDialogId());


            if(DialogMain.getDialogUserList().getCurrentDialogRepository().getSize()!=0){

                Logger.info("We have a current repo!");
                SystemDialogInstance systemDialogInstance = (SystemDialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getMostRecentDialogInstance();
                int caseId = systemDialogInstance.getDerivedCaseId();
                IDialogInstance inputDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogInstanceByGeneratedCaseId(caseId);
                
                // user dialog
                //     valid system dialog  <--- this (or earlier) is previous valid dialog (we get the first, most recent valid one)
                // user dialog
                //     a system dialog (may or may not be valid)
                Logger.info("Dialog sequence is " + getDialogIDSequenceInHistory(inputDialogInstance) );
                
                IDialogInstance prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(inputDialogInstance.getDialogId());
                if (prevDialogInstance != null)
                    Logger.info("There would be a prev dialog instance! ID is " + prevDialogInstance.getDialogId());
                if (getDialogIDSequenceInHistory(inputDialogInstance) >= 2) {  //there will only be a previous valid system dialog if we've had at least 2 user dialogs
                //if(inputDialogInstance.getDialogId() >= 2){
                     prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(inputDialogInstance.getDialogId());
                    if (prevDialogInstance!=null && Main.workbench.getStackedInferenceResult().getDoesValidStackExist()){
                        msg = true;  
                        Logger.info("Found previous valid response:" + msg);
                    }
                }
            }                   
        }
        Logger.info("Returning: " + msg);
        return msg;
    } 
    
    
    // get the system response string for the first valid system response found in the past preceeding the current user dialog
    public static String getPastSystemResponseString(String sessionID) {                                         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(sessionID);
        String msg = "";
               
           
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
            //IDialogInstance prevSystemDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(KADialogInstance.getDialogId());


            if(DialogMain.getDialogUserList().getCurrentDialogRepository().getSize()!=0){

                SystemDialogInstance systemDialogInstance = (SystemDialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getMostRecentDialogInstance();
                int caseId = systemDialogInstance.getDerivedCaseId();
                IDialogInstance inputDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogInstanceByGeneratedCaseId(caseId);
                
                // user dialog
                //     valid system dialog  <--- this (or earlier) is previous valid dialog (we get the first, most recent valid one)
                // user dialog
                //     a system dialog (may or may not be valid)
                
                if (getDialogIDSequenceInHistory(inputDialogInstance) >= 2) {  //there will only be a previous valid system dialog if we've had at least 2 user dialogs
                //if(inputDialogInstance.getDialogId() >= 2){
                    IDialogInstance prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(inputDialogInstance.getDialogId());
                    if (prevDialogInstance!=null && Main.workbench.getStackedInferenceResult().getDoesValidStackExist()){
                        msg = ((SystemDialogInstance)prevDialogInstance).getParsedDialogStr();  
                        Logger.info("Found *previous* valid response:" + msg);
                    }
                }
            }                   
        }                  
        return msg;
    }     
    
    // get the response string from the most recent system reply to the user's dialog
    public static String getMostRecentSystemResponseString(String sessionID) {                                         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(sessionID);
        String msg = "";
        
        //Logger.info("Lookign at username:" + sessionID);
           
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);

            if(DialogMain.getDialogUserList().getCurrentDialogRepository().getSize()!=0){
                SystemDialogInstance systemDialogInstance = (SystemDialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getMostRecentDialogInstance();
                int caseId = systemDialogInstance.getDerivedCaseId();
                IDialogInstance inputDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogInstanceByGeneratedCaseId(caseId);
                if(inputDialogInstance.getDialogTypeCode()==DialogInstance.USER_TYPE){
                    //if (systemDialogInstance.getIsRuleFired()) {
                        msg = systemDialogInstance.getParsedDialogStr();                        
                    //}
                }                   
            }           
        }       
        return msg;
    }     

    // determine if the most recent system response had non-root rules fired in reply to the most recent user dialog
    public static boolean getIsValidMostRecentSystemResponse(String sessionID) {                                         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(sessionID);
           boolean msg = false;
           
        synchronized (syncLock) {
            Logger.info("Start of Web KA..");
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);

            if(DialogMain.getDialogUserList().getCurrentDialogRepository().getSize()!=0){

                // get the last system response
                SystemDialogInstance systemDialogInstance = (SystemDialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getMostRecentDialogInstance();
                int caseId = systemDialogInstance.getDerivedCaseId();
                
                IDialogInstance inputDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogInstanceByGeneratedCaseId(caseId);
                if(inputDialogInstance.getDialogTypeCode()==DialogInstance.USER_TYPE){ 
                    // if the last system response had rules fired return true..
                    msg = systemDialogInstance.getIsRuleFired();                    
                }                   
            }           
        }
        
        return msg;
    }

    public static DialogCase generateCaseForKnowledgeAcquisition(IDialogInstance selectedDialog, String theMode){
        DialogCase theCase;
        IDialogInstance recentDialog;
        boolean isRuleFired;
        int caseId;
        int stackId;
                
        // inference result
        RuleSet applyingInferenceResult = new RuleSet();
        
        // root rule 
        Rule rootRule;
        
        // setting recent Dialog, caseId and stackId
        caseId = selectedDialog.getGeneratedCaseId();

        Logger.info("caseId is:" + caseId);
        synchronized (syncLock) {
            SystemDialogInstance aSystemDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId);
            Logger.info("Trying to retrieve a previous system dialog - starting ID I have is " + aSystemDialogInstance.getDialogId());
            IDialogInstance prevSystemDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(selectedDialog.getDialogId());
            if (prevSystemDialogInstance != null) {
                Logger.info("Found a previous system dialog - its ID  is " + prevSystemDialogInstance.getDialogId());
                Logger.info("The previous system dialog isRuleFired is: " + prevSystemDialogInstance.getIsRuleFired());
            }

            isRuleFired = aSystemDialogInstance.getIsRuleFired();
            stackId = aSystemDialogInstance.getStackId(); 
            Logger.info("Initial stack ID is " + stackId);
            recentDialog = selectedDialog;

            Logger.info("Mode is:" + theMode);
            switch(theMode){
                
                case "stop":              
                    if(isRuleFired){
                        // if processing is valid (if there is inference result)
                        applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                    } else {
                        // if processing is not valid there is no inference result
                        rootRule = Main.KB.getRootRule();
                        applyingInferenceResult.addRule(rootRule);
                    }      
                    break;

                case "continue":
                    if (prevSystemDialogInstance != null) {
                        stackId = prevSystemDialogInstance.getStackId(); 
                        Logger.info("continue: prevDialogInstance was non-null, so stackId is now:" + stackId);                 
                    }
                    else {
                        Logger.info("continue: prevDialogInstance was null [should not happen!], so stack ID unchange(" + stackId + ")!");
                    }
                    // if there is no previous result, getPreviousStackedInferenceResult returns a new ruleSet with root rule
                    applyingInferenceResult = Main.workbench.getPreviousStackedInferenceResult(stackId);
                    Logger.info("last rule in applyingInferenceResult is:" + applyingInferenceResult.getLastRule().getRuleId());
                    break;
                    
                case "immediate":
                    // we're adding a new rule to correct the last immediate (and valid) system response
                    applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                    Logger.info("immediate: last rule in applyingInferenceResult is:" + applyingInferenceResult.getLastRule().getRuleId());
                    break;    

                case "new":             
                    rootRule = Main.KB.getRootRule();
                    applyingInferenceResult.addRule(rootRule);

                    break;
            }

            //set inference result for ka
            Main.workbench.setInferenceResult(applyingInferenceResult);

            // Case generation
            String historyInputStr;
            String recentInputStr = "";
            String eventType = "";
            String eventValue = "";

            DialogCase selectedCase = (DialogCase) Main.allCaseSet.getCaseById(caseId);

            LinkedHashMap<String, Value> values = selectedCase.getValues();

            DialogMain.dicConverter.setDictionary(DialogMain.dictionary);


            switch(recentDialog.getDialogTypeCode()){
                case DialogInstance.USER_TYPE:
                    recentInputStr=recentDialog.getDialogStr();
                    recentInputStr = DialogMain.dicConverter.convertTermFromDic(recentInputStr,true);
                    break;

                case DialogInstance.EVENT_TYPE:
                    eventType = recentDialog.getEventInstance().getEventType();
                    eventValue = recentDialog.getEventInstance().getEventValue();
                    break;
            }
            historyInputStr = "";

            // put them in the values array
            values.replace("History", new Value(ValueType.TEXT, historyInputStr));
            values.replace("Recent", new Value(ValueType.TEXT, recentInputStr));
            values.replace("EventType", new Value(ValueType.TEXT, eventType));
            values.replace("EventValue", new Value(ValueType.TEXT, eventValue));

            // generate new case by cloning case
            DialogCase newCase = DialogCaseGenerator.generateCase(selectedCase.getInputDialogInstance(), selectedCase.getValues(),false);

            newCase.setInputDialogInstance(recentDialog);

            //insert new case
            try {
                Logger.info("I'm calling insertCase: " + newCase.getCaseId() + " value: " + newCase);
                DialogCaseArchiveModule.insertCase(newCase);

            } catch (FileNotFoundException ex) {
                Logger.info("File not found");
            }


            Logger.info("Context chosen for KA is " + applyingInferenceResult.getLastRule().getRuleId());
            DialogMain.getDialogUserList().setCurrentKAInferenceResult(applyingInferenceResult);
            //AddRuleGUI.execute("modify", newCase, stackId);

            //Logger.info("Would call AddRuleGUI...");
            theCase = newCase;
            
            //msg = addRule(mode,newCase,stackId);
            //msg = getCurrentCaseDialogHistory(selectedDialog,prevSystemDialogInstance,newCase);
            targetStackId = stackId; 
            //mode = theMode;
            mode = "modify"; // DPH June 2019 - this set to match AdminJavaGUI line 519
        }
        
        return theCase;
        
    }
    
    public static TableResponse getPreprocessorPreview(String matchText, String replaceText, String previewText,
                                                        Boolean regex, Boolean wordOnly,Boolean startInput,
                                                        Boolean endInput, Boolean replace, Boolean upper,
                                                        Boolean lower, Boolean trim) {
        TableResponse theResponse = new TableResponse();
        String result;
        
        theResponse.setHeader(new String[] {"Preview"});
        theResponse.setStatus("OK");
        
        Logger.info("Match text is: " + matchText);
        Logger.info("Replace text is: " + replaceText);
        Logger.info("Preview text is: " + previewText);
        Logger.info("regex text is: " + regex);
        Logger.info("wordOnly text is: " + wordOnly);
        Logger.info("startInput text is: " + startInput);
        Logger.info("endInput text is: " + endInput);
        Logger.info("replace text is: " + replace);
        Logger.info("upper text is: " + upper);
        Logger.info("lower text is: " + lower);
        Logger.info("trim text is: " + trim);
        
        
        try {
            result = DialogMain.userInterfaceController.preAndPostProcessInput(true, previewText, matchText, replaceText, regex, wordOnly, startInput, endInput, replace, upper, lower, trim, false);
            theResponse.addRow(new String[] {result});
        }
        catch (PatternSyntaxException | IndexOutOfBoundsException p) {
            theResponse.addRow(new String[] {"#EXCEPTION#"});
            theResponse.addRow(new String[] {p.getMessage()});
        }
        return theResponse;
    }
    
    public static StatusResponse addPreprocessorAction(String matchText, String replaceText,
                                                        Boolean regex, Boolean wordOnly,Boolean startInput,
                                                        Boolean endInput, Boolean replace, Boolean upper,
                                                        Boolean lower, Boolean trim) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        
        String[] row;
        
        DialogMain.userInterfaceController.preAndPostProcessActionListModel.addElement(new PreAndPostProcessorAction(matchText,replaceText,regex, wordOnly, startInput, endInput, replace, upper, lower, trim,false));
        
        return theResponse;
    }
    
    
    public static StatusResponse modifyPreprocessorAction(String matchText, String replaceText,
                                                        Boolean regex, Boolean wordOnly,Boolean startInput,
                                                        Boolean endInput, Boolean replace, Boolean upper,
                                                        Boolean lower, Boolean trim, String actionID) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        int actionIDInt = -1;
        
        try {
            actionIDInt = Integer.parseInt(actionID);
        }
        catch (NumberFormatException e) {
            theResponse.setStatus("Invalid action ID!");
        }
        
        if (actionIDInt != -1 && DialogMain.userInterfaceController.preAndPostProcessActionListModel.size()-1 >= actionIDInt) {
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.remove(actionIDInt);
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.add(actionIDInt,new PreAndPostProcessorAction(matchText,replaceText,regex, wordOnly, startInput, endInput, replace, upper, lower, trim,false));
        }
        
        return theResponse;
    }
    
    public static TableResponse getPreprocessorActions() {
        TableResponse theResponse = new TableResponse();
        theResponse.setHeader(new String[] {"Preprocessor Actions"});
        theResponse.setStatus("OK");
        
        String[] row;
                
        for (int i = 0; i < DialogMain.userInterfaceController.preAndPostProcessActionListModel.getSize(); i++) {
            PreAndPostProcessorAction anAction = (PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.getElementAt(i);
            row = new String[] {anAction.toString()};
                theResponse.addRow(row);
        }
        return theResponse;
    }
    
    public static TableResponse getPreprocessorActionDetails(String actionID) {
        TableResponse theResponse = new TableResponse();
        theResponse.setHeader(new String[] {"Action Details"});
        theResponse.setStatus("OK");
        int actionIDInt = -1;
        String[] row;
        
        try {
            actionIDInt = Integer.parseInt(actionID);
        }
        catch (NumberFormatException e) {
            theResponse.setStatus("Invalid action ID!");
        }
        
        if (actionIDInt != -1 && DialogMain.userInterfaceController.preAndPostProcessActionListModel.size()-1 >= actionIDInt) {
            PreAndPostProcessorAction anAction = (PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.getElementAt(actionIDInt); 
            row = new String[] {anAction.getMatchText()};
                theResponse.addRow(row);
            row = new String[] {anAction.getRegex()==true?"True":"False"};
                theResponse.addRow(row);  
            row = new String[] {anAction.getWordOnly()==true?"True":"False"};
                theResponse.addRow(row);      
            row = new String[] {anAction.getStartOfInput()==true?"True":"False"};
                theResponse.addRow(row);   
            row = new String[] {anAction.getEndOfInput()==true?"True":"False"};
                theResponse.addRow(row); 
            row = new String[] {anAction.getReplaceText()};
                theResponse.addRow(row);                
            row = new String[] {anAction.getReplaceOption()==true?"True":"False"};
                theResponse.addRow(row); 
            row = new String[] {anAction.getUpperOption()==true?"True":"False"};
                theResponse.addRow(row);     
            row = new String[] {anAction.getLowerOption()==true?"True":"False"};
                theResponse.addRow(row);     
            row = new String[] {anAction.getTrimOption()==true?"True":"False"};
                theResponse.addRow(row); 
        }

        return theResponse;
    }
    
    public static StatusResponse moveUpPreprocessorAction(String actionID) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        int actionIDInt = -1;
        
        try {
            actionIDInt = Integer.parseInt(actionID);
        }
        catch (NumberFormatException e) {
            theResponse.setStatus("Invalid action ID!");
        }
        
        if (actionIDInt > 0 && actionIDInt < DialogMain.userInterfaceController.preAndPostProcessActionListModel.size()) {
            PreAndPostProcessorAction anActionSelected = (PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.getElementAt(actionIDInt);
            PreAndPostProcessorAction anActionBefore = (PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.getElementAt(actionIDInt-1);
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.set(actionIDInt-1, anActionSelected);
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.set(actionIDInt, anActionBefore);
        }
        else {
            theResponse.setStatus("You can't move beyond the start!!");
        }

        return theResponse;        
    }
    public static StatusResponse moveDownPreprocessorAction(String actionID) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        int actionIDInt = -1;
        
        try {
            actionIDInt = Integer.parseInt(actionID);
        }
        catch (NumberFormatException e) {
            theResponse.setStatus("Invalid action ID!");
        }
        
        if (actionIDInt >= 0 && actionIDInt < DialogMain.userInterfaceController.preAndPostProcessActionListModel.size()-1) {
            PreAndPostProcessorAction anActionSelected = (PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.getElementAt(actionIDInt);
            PreAndPostProcessorAction anActionAfter = (PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.getElementAt(actionIDInt+1);
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.set(actionIDInt+1, anActionSelected);
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.set(actionIDInt, anActionAfter);
        }
        else {
            theResponse.setStatus("You can't move beyond the end!!");
        }

        return theResponse;        
    }
    public static StatusResponse deletePreprocessorAction(String actionID) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        int actionIDInt = -1;
        
        try {
            actionIDInt = Integer.parseInt(actionID);
        }
        catch (NumberFormatException e) {
            theResponse.setStatus("Invalid action ID!");
        }
        
        if (actionIDInt >= 0 && actionIDInt <= DialogMain.userInterfaceController.preAndPostProcessActionListModel.size()-1) {
            DialogMain.userInterfaceController.preAndPostProcessActionListModel.removeElementAt(actionIDInt);
        }
        else {
            theResponse.setStatus("You have not selected an action to delete!");
        }

        return theResponse;        
    }
    public static StatusResponse savePreprocessorActions() {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        
        DialogMain.processorList = new ArrayList<>();
        for (int anActionIndex = 0; anActionIndex < DialogMain.userInterfaceController.preAndPostProcessActionListModel.getSize(); anActionIndex++) {
            PreAndPostProcessorAction anAction = ((PreAndPostProcessorAction) DialogMain.userInterfaceController.preAndPostProcessActionListModel.get(anActionIndex)).copy();
            DialogMain.processorList.add(anAction);
            Logger.info("Adding preprocessor rule: " + anAction.getMatchText());
        }
        DBOperation.updatePreAndPostProcessorList(DialogMain.processorList);   

        return theResponse;        
    }
    
    public static StatusResponse initialiseActionList() {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        
        DialogMain.userInterfaceController.preAndPostProcessActionListModel.clear();
        
        for (int anActionIndex = 0; anActionIndex < DialogMain.processorList.size(); anActionIndex++) {
          DialogMain.userInterfaceController.preAndPostProcessActionListModel.addElement((PreAndPostProcessorAction)DialogMain.processorList.get(anActionIndex).copy());
        }
        
        return theResponse;    
    }
    
    
    public static TableResponse getExistingContextVariableActions(String definingVariable) {
        TableResponse theResponse = new TableResponse();
        
        theResponse.setHeader(new String[] {"Variable Actions"});
        theResponse.setStatus("OK");
        String[] row;
        
        ContextVariable definingVar = contextVariables.get(definingVariable);
        if (definingVar == null) {
            theResponse.setStatus("The context variable does not exist!");
        }
        else {
            for (ContextVariableAction anAction: definingVar.getVariableActions().values()) {
                row = new String[] {"Target:" + anAction.getTarget() + " Trigger:" + anAction.getTrigger() + " Value:" + anAction.getValue()};
                theResponse.addRow(row);
            }
        }
                   
        
        return theResponse;
    }
    
    public static TableResponse getAllExistingContextVariableActions() {
        TableResponse theResponse = new TableResponse();
        theResponse.setHeader(new String[] {"Defining Variable","Target","Trigger","Value"});
        theResponse.setStatus("OK");
        String[] row;
                       
        for (ContextVariable aVariable : DialogMain.globalContextVariableManager.getContextVariables().values()) {
            for (ContextVariableAction anAction: aVariable.getVariableActions().values()) {
                row = new String[] {aVariable.getVariableName(),anAction.getTarget(),anAction.getTrigger(),anAction.getValue()};
                theResponse.addRow(row);
            }
        }
        return theResponse;
    }
    
   
    
    public static StatusResponse deleteContextVariableAction(String definingVariable,String trigger, String targetVariableAction, String targetVariableValue) {
        StatusResponse result = new StatusResponse();
        result.setStatus("OK");
        result.setResult("");
        
        ContextVariable definingVar = contextVariables.get(definingVariable);
        if (definingVar != null ) {
            if (!definingVar.deleteVariableAction(trigger, targetVariableAction,targetVariableValue))
                result.setStatus("Failed to delete the variable action!");
        }
        else {
            result.setStatus("The defining variable does not exist, or the action to delete does not exist!");
        }
        
        return result;
    }
    
    public static StatusResponse addContextVariableAction(String definingVariable, String triggerCondition, String targetVariable, String targetValue) {
        StatusResponse result = new StatusResponse();
        result.setStatus("OK");
        result.setResult("");
        boolean actionOK = true;
        
        ContextVariable definingVar = contextVariables.get(definingVariable);
        ContextVariable targetVar = contextVariables.get(targetVariable);
        
        if (definingVar == null || targetVar == null) {
            result.setStatus("The defining variable or the target variable for the action does not exist or the name specified was empty!");
            actionOK = false;
        }
        
        if (triggerCondition.startsWith("@") || triggerCondition.trim().isEmpty()) { // the action is triggered based on the value contained in another variable
            if (contextVariables.get(triggerCondition) == null) {
                result.setStatus("The trigger condition variable for the action does not exist or the name specified was empty!");
                actionOK = false;
            }
        }
        
        if (targetValue.startsWith("@")) {
            if (contextVariables.get(targetValue) == null) {
                result.setStatus("The target value variable for the action does not exist!");
                actionOK = false;
            }
        }
        else if (targetValue.trim().isEmpty()) {
            result.setStatus("The target literal value for the action was empty!");
            actionOK = false;
        }

        
        if (actionOK) {
            if (targetValue.startsWith("@")) { // target value is from another context variable
                if (!contextVariables.get(definingVariable).addVariableActionContext(targetVariable, targetValue, triggerCondition))
                    result.setStatus("Another action targetting " + targetVariable + " already exists and has the same trigger condition (" + triggerCondition + ") for this defining variable (" + definingVariable + ")");
            }
            else { // target value is literal
                if (!contextVariables.get(definingVariable).addVariableActionFixed(targetVariable, targetValue, triggerCondition))
                    result.setStatus("Another action targetting " + targetVariable + " already exists and has the same trigger condition (" + triggerCondition + ") for this defining variable (" + definingVariable + ")");
            }
        }               
        return result;
    }
    
    public static StatusResponse getContextVariableOverride(String variableName) {
        StatusResponse result = new StatusResponse();
        String aVariableOverride;

        result.setStatus("OK");
        result.setResult("");
        
        if (contextVariables.get(variableName) != null) { 
            Logger.info("Override: Looking at variable: " + variableName + ":");
            aVariableOverride = contextVariables.get(variableName).getVariableValueOverride();
            if (!aVariableOverride.equals("")) {
                Logger.info("   Found override: " + aVariableOverride);
                result.setResult(aVariableOverride);
            }
            
        }
        
        return result;
    }
    
    public static TableResponse getContextVariableCriteria(String variableName, String username) {
        
        TableResponse result = new TableResponse();
        String[] row;
        String aVariableCriteria;
        ContextVariableUser userContextVariable;
        
        result.setHeader(new String[] {"VariableCiteria"});
        result.setStatus("OK");
        
        HashMap<Integer,String> contextCriteriaValues;
        if (contextVariables.get(variableName) != null) {
            Logger.info("Looking at variable: " + variableName + ":");
            contextCriteriaValues = contextVariables.get(variableName).getVariableValues();
            for (String aValue : contextCriteriaValues.values()) {
                Logger.info("   Found criteria: " + aValue);

                row = new String[] {aValue};
                result.addRow(row);  
            }
        }
        
        // I think I am confusing global definitions of variables below verses user instances with values.  So for now, don't include the user context vars
        // in the criteria list..
        
//        if (!username.equals("")) {
//            synchronized (syncLock) { 
//                int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(username);
//                DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
//                if (DialogMain.getDialogUserList() != null) {
//                    if (DialogMain.getDialogUserList().getCurrentContextVariables() != null) {
//                        userContextVariable = DialogMain.getDialogUserList().getCurrentContextVariables().get(variableName);
//                        if (userContextVariable != null) {
//                            aVariableCriteria = userContextVariable.getValue();
//                            Logger.info("   Found user set criteria: " + aVariableCriteria);
//                            row = new String[] {aVariableCriteria};
//                            result.addRow(row);  
//                        }
//                    }
//                    
//                }
//            }
//        }
        
        return result;

    }
    
    public static StatusResponse getSystemContextVariableValue(String variableName) {
        
        StatusResponse result = new StatusResponse();
        ContextVariable aSystemContextVariable;
        result.setStatus("OK");
        
        aSystemContextVariable = systemContextVariables.get(variableName);
        
        if (aSystemContextVariable != null) {
            Logger.info("Found variable: " + variableName + " with value:" + aSystemContextVariable.getSingleVariableValue());
            result.setResult(aSystemContextVariable.getSingleVariableValue());
        }       
        return result;
    }
    
    public static TableResponse getSystemContextVariableOverrides(String variableName) {
        
        
        TableResponse result = new TableResponse();
        String[] row;
        
        result.setHeader(new String[] {"RuleOverride","VariableValue"});
        result.setStatus("OK");
        
        for (int key: systemContextVariableOverrides.keySet()) {
            LinkedHashMap<String,ContextVariableSystem> thisRuleOverrideContext = systemContextVariableOverrides.get(key);
            for (String varName: thisRuleOverrideContext.keySet()) {
                if (varName.equals(variableName)) {
                    row = new String [] {"Rule:"+key,"Value:" + thisRuleOverrideContext.get(varName).getValue()};
                    result.addRow(row);   
                }
            }
        }        
        return result;
    }
    
    public static StatusResponse addNewSystemContextVariable(String variableName, String variableValue) {
        StatusResponse result = new StatusResponse();
        result.setStatus("OK");
        String newVariableName = variableName;
        if (!newVariableName.startsWith("@SYSTEM"))
            newVariableName = "@SYSTEM" + newVariableName;
        
        ContextVariable newVariable = new ContextVariable();
        newVariable.setVariableName(newVariableName);
        newVariable.addVariableValue(variableValue);
        
        if (!systemContextVariables.containsKey(newVariableName)) {
            Logger.info("Adding NEW system variable: " + newVariableName + " with value: " + variableValue);
            systemContextVariables.put(newVariableName, newVariable);
        }
        else {
            Logger.info("Modifying existing variable: " + newVariableName + " with value: " + variableValue);
            systemContextVariables.replace(newVariableName, newVariable);
        }
        
        return result;
    }
    
    
    public static StatusResponse addSystemContextVariableOverride(String variableName,String ruleNumber, String overrideValue) {
        StatusResponse result = new StatusResponse();
        result.setStatus("OK");
        int ruleNumberInt;
        try {
            ruleNumberInt = Integer.parseInt(ruleNumber);
        }
        catch (NumberFormatException e) {
            ruleNumberInt = 0;
        }
        
        LinkedHashMap<String,ContextVariableSystem> ruleContextOverrides = systemContextVariableOverrides.get(ruleNumberInt);
        if (ruleContextOverrides == null) {
            Logger.info("Adding new rule context as list didn't contain this context before..");
            ruleContextOverrides = new LinkedHashMap<>();
            ContextVariableSystem newCV = new ContextVariableSystem(variableName, overrideValue, ruleNumberInt);
            Logger.info("Creating new CVS variable, name:" + variableName + ", override value: " + overrideValue + " for rule location:" + ruleNumber);
            ruleContextOverrides.put(variableName, newCV);
            systemContextVariableOverrides.put(ruleNumberInt, ruleContextOverrides);
            Logger.info("Putting new CVS in local source list");
        }
        else {
            if (!ruleContextOverrides.containsKey(variableName)) {
                Logger.info("Rule context found, but this variable " + variableName + " didn't exist, adding..");

                ContextVariableSystem newCV = new ContextVariableSystem(variableName, overrideValue, ruleNumberInt);
                ruleContextOverrides.put(variableName, newCV);
            }
            else {
                Logger.info("Rule context found, this variable " + variableName + " existed, replacing..");
                ContextVariableSystem newCV = new ContextVariableSystem(variableName, overrideValue, ruleNumberInt);
                ruleContextOverrides.replace(variableName, newCV);
            }
        }
        
        return result;
    } 
    
    public static StatusResponse deleteSystemContextVariableOverride(String variableName,String ruleNumber) {
        StatusResponse result = new StatusResponse();
        result.setStatus("OK");
        int ruleNumberInt;
        try {
            ruleNumberInt = Integer.parseInt(ruleNumber);
        }
        catch (NumberFormatException e) {
            ruleNumberInt = 0;
        }
        
        LinkedHashMap<String,ContextVariableSystem> theContextList = systemContextVariableOverrides.get(ruleNumberInt);
        if (theContextList != null) {
            theContextList.remove(variableName);
        }
        
        return result;
    }
    
    public static TableResponse getContextVariables(String username) {
                
        TableResponse result = new TableResponse();
        String[] row;
        
        result.setHeader(new String[] {"VariableName"});
        result.setStatus("OK");
        
        ArrayList<String> contextList = new ArrayList<>();
        for (ContextVariable cv : DialogMain.globalContextVariableManager.getContextVariables().values()) {
            row = new String[] {cv.getVariableName()};
            result.addRow(row);           
            contextList.add(cv.getVariableName());
        }
        
        if (!username.equals("")) {
            synchronized (syncLock) { 
                int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(username);
                DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
                for (ContextVariableUser aCV: DialogMain.getDialogUserList().getCurrentContextVariables().values()) {
                    if (!contextList.contains(aCV.getVariableName())) {
                        row = new String[] {aCV.getVariableName()};
                        result.addRow(row);
                        //contextList.add(aCV.getVariableName());
                    }       
                }
            }
        }
        
        //String[] result = contextList.toArray(new String[0]);
        
        return result;

    }
    
    // we might be doing context variable maintenance - so fetch our local copy of variables instead of the 
    // main list..
    public static TableResponse getContextVariablesForModification(String username) {
        
        TableResponse result = new TableResponse();
        String[] row;
        
        result.setHeader(new String[] {"VariableName"});
        result.setStatus("OK");

        
        ArrayList<String> contextList = new ArrayList<>();
        for (ContextVariable cv : contextVariables.values()) {  // <---- these are the local copies..
            row = new String[] {cv.getVariableName()};
            //Logger.info("Adding " + cv.getVariableName() + " to backup copy of context variables..");
            if (!cv.getVariableName().startsWith("@SYSTEM"))  // only add user variable to result
                result.addRow(row);           
            contextList.add(cv.getVariableName());
        }
        
        if (!username.equals("")) {
            synchronized (syncLock) { 
                int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(username);
                DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
                if (DialogMain.getDialogUserList() != null && DialogMain.getDialogUserList().getCurrentContextVariables() != null) {
                    for (ContextVariableUser aCV: DialogMain.getDialogUserList().getCurrentContextVariables().values()) {
                        if (!contextList.contains(aCV.getVariableName())) {
                            row = new String[] {aCV.getVariableName()};
                            result.addRow(row);
                            //contextList.add(aCV.getVariableName());
                        }       
                    }
                }
            }
        }
        
        //String[] result = contextList.toArray(new String[0]);
        
        return result;
    }
    
    public static TableResponse getSystemContextVariablesForModification() {
        
        TableResponse result = new TableResponse();
        String[] row;
        
        result.setHeader(new String[] {"VariableName"});
        result.setStatus("OK");

        
        ArrayList<String> contextList = new ArrayList<>();
        for (ContextVariable cv : systemContextVariables.values()) {  // <---- these are the local copies..
            row = new String[] {cv.getVariableName()};
            //Logger.info("Adding " + cv.getVariableName() + " to backup copy of context variables..");
            if (cv.getVariableName().startsWith("@SYSTEM"))  // only add system variable to result
                result.addRow(row);           
            contextList.add(cv.getVariableName());
        }       
        
        return result;
    }
       
    
    public static TableResponse getContextVariablesAndValues(String username, int dialogID) {
         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(username);
        int stackID;
        TableResponse theResponse = new TableResponse();
        String[] columns = {" "," "," "," "};
        String[] row;

        synchronized (syncLock) {         
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
                if (dialogID >= 0) {
                    stackID = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getDialoginstanceById(dialogID).getStackId();

                    theResponse.setHeader(columns);
                    theResponse.setStatus("OK");
                    
                    theResponse.addRow(new String[] {".subheader1","User variables",""});
                    theResponse.addRow(new String[] {".subheader2","Variable Name","Current Value"});
                    for (Object[] userVarListRow : DialogMain.getDialogUserList().getCurrentDialogUser().getContextVariablesForList(stackID)) {
                        if (!((String)userVarListRow[1]).startsWith("@SYSTEM")) {
                            row = Arrays.copyOf(userVarListRow, userVarListRow.length, String[].class);
                            theResponse.addRow(row); 
                        }
                    }
                    


                    theResponse.addRow(new String[] {".subheader1","System variables",""});
                    theResponse.addRow(new String[] {".subheader2","Variable Name","Current Value"});
                    for (Object[] systemVarListRow : DialogMain.getDialogUserList().getCurrentDialogUser().getSystemContextVariablesForList(stackID)) {
                        row = Arrays.copyOf(systemVarListRow, systemVarListRow.length, String[].class);
                        theResponse.addRow(row); 
                    }
                    
                   
                    theResponse.addRow(new String[] {".subheader1","Variable definitions",""});
                    theResponse.addRow(new String[] {".subheader2","Variable Name","Matching criteria"});
                    for (ContextVariable cv : DialogMain.globalContextVariableManager.getContextVariables().values()) {
                        row = new String[3]; // stack,varName, varValue
                        row[0] = "-";
                        row[1] = cv.getVariableName();
                        if (cv.getVariableValuesAmount() == 1)
                            row[2] = cv.getSingleVariableValue();
                        else {
                            row[2] = "";
                            int count = 1;
                            for (String aValue: cv.getValuesBase().values()) {
                                if (!row[2].isEmpty())
                                    row[2] += "#br#" + count + ". " + aValue;
                                else
                                    row[2] += count + ". " + aValue;
                                count++;
                            }
                        }
                        if (!cv.getVariableName().startsWith("@SYSTEM"))
                            theResponse.addRow(row);
                    }
                }
        }   
        return theResponse;

    }
    
    public static TableResponse getContextVariablesAndValuesFromStack(String username, int stackID) {
         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(username);
        TableResponse theResponse = new TableResponse();
        String[] columns = {" "," "," "," "};
        String[] row;

        synchronized (syncLock) {         
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
                if (stackID >= 0) {
                    theResponse.setHeader(columns);
                    theResponse.setStatus("OK");

//                    for (ContextVariable cv : DialogMain.globalContextVariableManager.getContextVariables().values()) {
//                        row = new String[3]; // stack,varName, varValue
//                        row[0] = "All";
//                        row[1] = cv.getVariableName();
//                        row[2] = cv.getSingleVariableValue();
//                        theResponse.addRow(row);
//                    }


                    theResponse.addRow(new String[] {".subheader1","User variables",""});
                    theResponse.addRow(new String[] {".subheader2","Variable Name","Current Value"});
                    for (Object[] userVarListRow : DialogMain.getDialogUserList().getCurrentDialogUser().getContextVariablesForList(stackID)) {
                        row = Arrays.copyOf(userVarListRow, userVarListRow.length+1, String[].class);
                        theResponse.addRow(row); 
                    }
                    
                    theResponse.addRow(new String[] {".subheader1","System variables",""});
                    theResponse.addRow(new String[] {".subheader2","Variable Name","Current Value"});
                    
                    for (Object[] systemVarListRow : DialogMain.getDialogUserList().getCurrentDialogUser().getSystemContextVariablesForList(stackID)) {
                        row = Arrays.copyOf(systemVarListRow, systemVarListRow.length+1, String[].class);
                        theResponse.addRow(row); 
                    }
                }
            theResponse.addRow(new String[] {".subheader1","Variable definitions",""});
            theResponse.addRow(new String[] {".subheader2","Variable Name","Matching criteria"});
            for (ContextVariable cv : DialogMain.globalContextVariableManager.getContextVariables().values()) {
                row = new String[3]; // stack,varName, varValue
                row[0] = "-";
                row[1] = cv.getVariableName();
                if (cv.getVariableValuesAmount() == 1)
                    row[2] = cv.getSingleVariableValue();
                else {
                    row[2] = "";
                    int count = 1;
                    for (String aValue: cv.getValuesBase().values()) {
                        if (!row[2].isEmpty())
                            row[2] += "#br#" + count + ". " + aValue;
                        else
                            row[2] += count + ". " + aValue;
                        count++;
                    }
                }
                if (!cv.getVariableName().startsWith("@SYSTEM"))
                    theResponse.addRow(row);
            }
        }   
        return theResponse;

    }
    
    public static void populateContextVariables() {
        for (Map.Entry me : DialogMain.globalContextVariableManager.getContextVariables().entrySet()) {
            String varName = (String) me.getKey();
            ContextVariable aVariable = (ContextVariable) me.getValue();
            
            if (varName.startsWith("@SYSTEM")) {
                systemContextVariables.put(varName, aVariable);  // hide away and store the system context variables for now..
            }
            else if (!varName.startsWith("@MOD")) {  // ignore the overridden system variables as well..               
                contextVariables.put(varName, aVariable);
            }
            else { //varName.startsWith("@MOD")
                Logger.info("Overridden variable found!");
                String contextVariableValue = aVariable.getSingleVariableValue();
                String contextStringLabel = varName.substring(varName.indexOf("[")+1,varName.indexOf("]"));
                String contextVariableNameSimplified = varName.substring(varName.indexOf("]")+1);
                int ruleContextFound = Integer.parseInt(contextStringLabel);
                Logger.info("Modified system variable - original name: " + varName + ", simplified name: " + contextVariableNameSimplified  + ", rule override detected: " + ruleContextFound);                
                addOverrideVariableToRuleOverrideList(contextVariableNameSimplified, contextVariableValue, ruleContextFound);
            }           
        }       
    }

    private static void addOverrideVariableToRuleOverrideList(String variableName, String variableValue, int currentRuleContextOverride) {
        LinkedHashMap<String,ContextVariableSystem> ruleContextOverrides = systemContextVariableOverrides.get(currentRuleContextOverride);
        if (ruleContextOverrides == null) {
            Logger.info("Adding new context as list didn't contain this context before..");
            ruleContextOverrides = new LinkedHashMap<>();
            ContextVariableSystem newCV = new ContextVariableSystem(variableName, variableValue, currentRuleContextOverride);
            Logger.info("Creating new CVS variable, name:" + variableName + ", value: " + variableValue + " for currentRuleContextOverride:" + currentRuleContextOverride);
            ruleContextOverrides.put(variableName, newCV);
            systemContextVariableOverrides.put(currentRuleContextOverride, ruleContextOverrides);
            Logger.info("Putting new CVS in local source list");
        }
        else {
            if (!ruleContextOverrides.containsKey(variableName)) {
                Logger.info("Rule overrides found, but this variable " + variableName + " didn't exist, adding..");

                ContextVariableSystem newCV = new ContextVariableSystem(variableName, variableValue, currentRuleContextOverride);
                ruleContextOverrides.put(variableName, newCV);
            }
            else {
                Logger.info("Rule overrides found, and this variable " + variableName + " already existed, so replacing..");
                ContextVariableSystem newCV = new ContextVariableSystem(variableName, variableValue, currentRuleContextOverride);
                ruleContextOverrides.replace(variableName, newCV);
            }
        }
    }
    
    public static StatusResponse saveContextVariableChanges() {
        
        LinkedHashMap<String, ContextVariable> contextVariableList = new LinkedHashMap<>();
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        
        for (Map.Entry me: contextVariables.entrySet()) {
            String varName = (String)me.getKey();
            ContextVariable cv = (ContextVariable)me.getValue();
            contextVariableList.put(varName, cv);
        }
        
        for (Map.Entry me : systemContextVariables.entrySet()) {
            String varName = (String)me.getKey();
            ContextVariable cv = (ContextVariable)me.getValue();
            contextVariableList.put(varName, cv);
        }
        
        for (Map.Entry me : systemContextVariableOverrides.entrySet()) {
            for (Map.Entry overrideEntry : ((LinkedHashMap<String, ContextVariableSystem>)me.getValue()).entrySet()) {
                int ruleContext = (int)me.getKey();
                String variableName = (String)overrideEntry.getKey();
                String variableValue = (String) ((ContextVariableSystem)overrideEntry.getValue()).getValue();
                ContextVariable newCV = new ContextVariable();
                newCV.setVariableName("@MOD[" + ruleContext + "]" + variableName);
                newCV.addVariableValue(variableValue);
                contextVariableList.put("@MOD[" + ruleContext + "]" + variableName, newCV);
                Logger.info("newContextVariableList - Adding system mod variable:" + newCV.getVariableName() + " with value: " + variableValue);

            }
        }

        // Add the system context variables back in the list before we write them back to dialogmain..
        globalContextVariableManager.setContextVariables(contextVariableList);
        DialogMain.globalContextVariableManager = globalContextVariableManager;
        
        return theResponse;
    }
    

    
    
    
    
    
    
    
    
    
    
    
    /* internal state getters and setters */
    public static Conclusion getNewConclusion() {
        return newConclusion;
    }
    public static void setNewConclusion(Conclusion aNewConclusion) {
        newConclusion = aNewConclusion;
    }
    
    public static ConclusionSet getConclusionSet() {
        return conclusionSet;
    }
    public static void setConclusionSet(ConclusionSet aConclusionSet) {
       conclusionSet = aConclusionSet;
    }
    
    public static DialogCase getCurrentCase() {
        return currentCase;
    }  
    public static void setCurrentCase(DialogCase aCurrentCase) {
        currentCase = aCurrentCase;
    }
    
    public static Condition getNewCondition() {
        return newCondition;
    }  
    public static void setNewCondition(Condition aNewCondition) {
       newCondition = aNewCondition;
    }
    
    public static String getMode() {
        return mode;
    }
    public static void setMode(String theMode) {
        mode = theMode;
    }
    
    
    public static boolean getIsWrongConclusionSet() {
        return isWrongConclusionSet;
    }
    public static void setIsWrongConclusionSet(boolean isWrong) {
        isWrongConclusionSet = isWrong;
    }
    
    public static int getTargetStackID() {
        return targetStackId;
    }
    public static void setTargetStackID(int targetStackID) {
        targetStackId = targetStackID;
    }
    
    
}

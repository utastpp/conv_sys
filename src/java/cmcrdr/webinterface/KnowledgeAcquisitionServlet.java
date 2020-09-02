/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cmcrdr.webinterface;

import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rdr.apps.Main;
import rdr.rules.Rule;
import rdr.rules.RuleSet;
import cmcrdr.cases.DialogCaseArchiveModule;
import cmcrdr.dialog.IDialogInstance;
import static cmcrdr.gui.AdminJavaGUI.syncLock;
import com.google.gson.Gson;
import java.util.Iterator;
import java.util.Map;
import rdr.cases.CaseLoader;
import rdr.domain.DomainLoader;
import rdr.reasoner.MCRDRStackResultInstance;
import rdr.reasoner.MCRDRStackResultSet;
import rdr.rules.Conclusion;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.RuleLoader;
import rdr.sqlite.DBManager;
import cmcrdr.command.CommandFactory;
import cmcrdr.command.CommandInstance;
import cmcrdr.dialog.DialogSet;
import cmcrdr.handler.OutputParser;
import cmcrdr.mysql.DBOperation;
import cmcrdr.command.ICommandInstance;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.dialog.SystemDialogInstance;
import cmcrdr.gui.DatabaseQueryBuilderGUI;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.JOINTYPE;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.ORDERTYPE;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.SELECTTYPE;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.getQueryModelResult;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.getReferenceTableNames;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.getselectedTableRefId;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.setReferenceTableNames;
import static cmcrdr.gui.DatabaseQueryBuilderGUI.setSelectedTableRefId;
import static cmcrdr.handler.OutputParser.containsTableAndFieldTag;
import static cmcrdr.main.DialogMain.contextPath;
import cmcrdr.savedquery.ConclusionQuery;
import cmcrdr.savedquery.SavedQueryTemplate;
import cmcrdr.sqlite.SqliteOperation;
import cmcrdr.user.DialogUser;
import cmcrdr.knowledgeacquisition.KALogic;
import static cmcrdr.knowledgeacquisition.KALogic.addContextVariableAction;
import static cmcrdr.knowledgeacquisition.KALogic.addNewSystemContextVariable;
import static cmcrdr.knowledgeacquisition.KALogic.addSystemContextVariableOverride;
import static cmcrdr.knowledgeacquisition.KALogic.contextVariables;
import static cmcrdr.knowledgeacquisition.KALogic.deleteContextVariableAction;
import static cmcrdr.knowledgeacquisition.KALogic.deleteSystemContextVariableOverride;
import static cmcrdr.knowledgeacquisition.KALogic.getAllExistingContextVariableActions;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariableCriteria;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariableOverride;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariables;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariablesAndValues;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariablesAndValuesFromStack;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariablesForModification;
import static cmcrdr.knowledgeacquisition.KALogic.getExistingContextVariableActions;
import static cmcrdr.knowledgeacquisition.KALogic.getPreprocessorPreview;
import static cmcrdr.knowledgeacquisition.KALogic.addPreprocessorAction;
import static cmcrdr.knowledgeacquisition.KALogic.deletePreprocessorAction;
import static cmcrdr.knowledgeacquisition.KALogic.getPreprocessorActionDetails;
import static cmcrdr.knowledgeacquisition.KALogic.getPreprocessorActions;
import static cmcrdr.knowledgeacquisition.KALogic.getSystemContextVariableOverrides;
import static cmcrdr.knowledgeacquisition.KALogic.getSystemContextVariableValue;
import static cmcrdr.knowledgeacquisition.KALogic.getSystemContextVariablesForModification;
import static cmcrdr.knowledgeacquisition.KALogic.initialiseActionList;
import static cmcrdr.knowledgeacquisition.KALogic.modifyPreprocessorAction;
import static cmcrdr.knowledgeacquisition.KALogic.moveDownPreprocessorAction;
import static cmcrdr.knowledgeacquisition.KALogic.moveUpPreprocessorAction;
import static cmcrdr.knowledgeacquisition.KALogic.populateContextVariables;
import static cmcrdr.knowledgeacquisition.KALogic.saveContextVariableChanges;
import static cmcrdr.knowledgeacquisition.KALogic.savePreprocessorActions;
import cmcrdr.knowledgeacquisition.StatusResponse;
import cmcrdr.knowledgeacquisition.TableResponse;


@WebServlet("/KnowledgeAcquisitionServlet")
public class KnowledgeAcquisitionServlet extends HttpServlet {

    private static DialogMain dialogMain = null;
    //private static DialogCase currentCase = null;
    //private static Condition newCondition;   
    
    //private Conclusion newConclusion = new Conclusion();
    //private ConclusionSet conclusionSet = new ConclusionSet();
    //private static boolean isWrongConclusionSet=false;
    //private static int targetStackId = 0;
    //private static String mode;
    //private static DatabaseQueryBuilderGUI referenceDatabaseMethods = null;
    
    //private static KALogic KA = DialogMain.cmcrdrKA;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        switch (request.getParameter("mode").trim()) {
            case "getIsDomainInitialised":
            {
                String sendData = "false";
                if (DialogMain.getIsDomainInitialised()) { 
                     sendData = "true";
                }
                //Logger.info("getIsDomainInitialised: " + sendData);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "getIsValidMostRecentSystemResponse":
            {
                String username = request.getParameter("username");
                String sendData = "false";

                if (DialogMain.getIsDomainInitialised()) {                   
                    if (KALogic.getIsValidMostRecentSystemResponse(username))
                        sendData = "true";
                }

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }   
            case "getMostRecentSystemResponseString":
            {
                String username = request.getParameter("username");
                String sendData = "";

                if (DialogMain.getIsDomainInitialised()) {                   
                    sendData = KALogic.getMostRecentSystemResponseString(username);
                }

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }   
            case "getMostRecentUserDialogID":
            {
                String sessionId = request.getSession().getId();
                String username = request.getParameter("username");
                String sendData = "";

                if (DialogMain.getIsDomainInitialised()) {                   
                    sendData = "" + getMostRecentUserDialogID(username);
                }

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            } 
            case "getMostRecentSystemDialogID":
            {
                String sessionId = request.getSession().getId();
                String username = request.getParameter("username");
                String sendData = "";

                if (DialogMain.getIsDomainInitialised()) {                   
                    sendData = "" + getMostRecentSystemDialogID(username);
                }

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            } 
            case "getKAConclusionList":
            {            
                String json = new Gson().toJson(getKAConclusionList());
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);

                break;
            } 
            case "getIsValidPastSystemResponse":
            {            
                String username = request.getParameter("username");
                String sendData = "false";
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    if (KALogic.getIsValidPastSystemResponse(username))
                        sendData = "true";
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }        
            case "getPastSystemResponseString":
            {            
                String username = request.getParameter("username");
                String sendData = "";
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    sendData = KALogic.getPastSystemResponseString(username);
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "generateCaseForKnowledgeAcquisition" :
            {
                String sendData = "false";
                
                String caseMode = request.getParameter("caseMode");
                String dialogID = request.getParameter("dialogID");
                String username = request.getParameter("username");
                IDialogInstance selectedDialog = getSelectedDialog(username,dialogID);
                KALogic.setCurrentCase(KALogic.generateCaseForKnowledgeAcquisition(selectedDialog, caseMode));
                
                if (KALogic.getCurrentCase() != null)
                    sendData = "true";
                
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "getCaseAttributes":
            {                           
                String json = new Gson().toJson(KALogic.getCurrentCase());
                //Logger.info("CASE JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getAttributeOperators" :
            {
                String attribute = request.getParameter("attribute");
               //Logger.info("The attribute is " + attribute);
                String [] operators = KALogic.getAttributeOperators(attribute);
                String json = new Gson().toJson(operators);
                //Logger.info("operators JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "constructNewCondition" :
            {
                String attribute = request.getParameter("attribute");
                String operator = request.getParameter("operator");
                String value = request.getParameter("value");
                String initial = request.getParameter("initial");
                //String stopped = request.getParameter("stopped");
                
                String sendData = "false";
                boolean isValid;
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    isValid = KALogic.constructNewCondition( attribute,  operator,  value, "false");
                    if (isValid) {
                        if (initial.equals("true")) {
                            // remove all conditions (if we've partially added some already then reset the GUI)..
                            Main.workbench.getLearner().deleteAllConditionFromNewRule();
                        }
                        
                        if(Main.workbench.getLearner().addConditionToNewRule(KALogic.getNewCondition()))
                            sendData = "true";
                        else
                            sendData = "exists";
                    }
                        
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "constructConclusion" :
            {
                String conclusion = request.getParameter("conclusion");
                String actionCategory = request.getParameter("actionCategory");
                String action = request.getParameter("action");           
                
                String sendData = "false";
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    KALogic.constructConclusion(conclusion, actionCategory, action);
                    sendData = "true";
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "getInferenceResult":
            {
                String json = "";
                Gson gson = new Gson();
                TableResponse theResponse = new TableResponse();
                theResponse.setStatus("The domain is not initialised");
                
                String username = request.getParameter("username");  
                String selectedConclusionStr = request.getParameter("selectedConclusion");  
                int selectedConclusion;
                
                try {
                    selectedConclusion = Integer.parseInt(selectedConclusionStr);
                }
                catch (NumberFormatException e) {
                    selectedConclusion = -1;
                }
    
                if (DialogMain.getIsDomainInitialised()) {
                    
                    theResponse = KALogic.getInferenceResult(username,selectedConclusion);
                    json = new Gson().toJson(theResponse);  
                    
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            
            case "prepareLearner" :
            {               
                String sendData = "false";
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    KALogic.learnerInit();
                    sendData = "true";

                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "submitRule" :
            {
                String sendData = "false";
                String isStoppingRuleStr = request.getParameter("isStoppingRule");
                String doNotStackStr = request.getParameter("doNotStack");
                boolean isStoppingRule = isStoppingRuleStr.equals("true");
                boolean doNotStack = doNotStackStr.equals("true");
                
                if (DialogMain.getIsDomainInitialised()) { 
                    KALogic.submitRule(isStoppingRule,doNotStack);
                    sendData = "true";
                }
                
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "getConclusionPreview" :
            {
                String sendData = "false";
                String text = request.getParameter("text");
                             
                if (DialogMain.getIsDomainInitialised()) { 
                    sendData = KALogic.getConclusionPreview(text);
                }
                
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "constructNewConclusion" :
            {
                String sendData = "";
                String text = request.getParameter("text");
                String category = request.getParameter("category");
                String command = request.getParameter("command");
                String variable = request.getParameter("variable");
                
                //Logger.info("text: " + text);
                //Logger.info("category: " + category);
                //Logger.info("command: " + command);
                //Logger.info("variable: " + variable);
                             
                if (DialogMain.getIsDomainInitialised()) { 
                    sendData = KALogic.constructNewConclusion(text,category,command,variable);
                }
                //Logger.info("sendData: '" + sendData + "'");

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "getCommandCategoryList": {
                String sendData = "";
                String json = "";
                if (DialogMain.getIsDomainInitialised()) { 

                    String [] categories = getCommandCategoryList();
                    json = new Gson().toJson(categories);
                    //Logger.info("categories JSON is: " + json);
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getCommandActionList": {
                String json = "";
                String category = request.getParameter("category");
                
                //Logger.info("category is " + category);
    
                if (DialogMain.getIsDomainInitialised()) { 
                    if (!category.equals("")) {
                        String [] actions = getCommandActionList(category);
                        json = new Gson().toJson(actions);
                        //Logger.info("actions JSON is: " + json);
                    }
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getContextVariables": {
                String json = "";
                TableResponse variables = new TableResponse();
                variables.setStatus("");
                
                String username = request.getParameter("username");
                if (DialogMain.getIsDomainInitialised()) { 
                    
                    //String [] variables = getContextVariables();
                   variables = getContextVariables(username);
                   json = new Gson().toJson(variables);
                   // Logger.info("variables JSON is: " + json);
                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getContextVariablesForModification": {
                String json = "";
                TableResponse variables = new TableResponse();
                variables.setStatus("");
                //Logger.info("getContextVariablesForModification called..");
                
                String username = request.getParameter("username");
    
                if (DialogMain.getIsDomainInitialised()) { 
                    
                   variables = getContextVariablesForModification(username);
                   json = new Gson().toJson(variables);
                   // Logger.info("variables JSON is: " + json);
                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getSystemContextVariablesForModification": {
                String json = "";
                TableResponse variables = new TableResponse();
                variables.setStatus("");                
    
                if (DialogMain.getIsDomainInitialised()) { 
                    
                   variables = getSystemContextVariablesForModification();
                   json = new Gson().toJson(variables);                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getSystemContextVariableValue": {
                String json = "";
                StatusResponse status = new StatusResponse();
                status.setStatus("");                
                String variableName = request.getParameter("variableName");
    
                if (DialogMain.getIsDomainInitialised()) { 
                    
                   status = getSystemContextVariableValue(variableName);
                   json = new Gson().toJson(status);                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getSystemContextVariableOverrides": {
                String json = "";
                TableResponse variables = new TableResponse();
                variables.setStatus("");   
                
                String variableName = request.getParameter("variableName");
   
                if (DialogMain.getIsDomainInitialised()) { 
                    
                   variables = getSystemContextVariableOverrides(variableName);
                   json = new Gson().toJson(variables);                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "addNewSystemContextVariable": {
                String json = "";
                StatusResponse status = new StatusResponse();
                status.setStatus("");                
                String variableName = request.getParameter("variableName");
                String variableValue = request.getParameter("variableValue");
    
                if (DialogMain.getIsDomainInitialised()) { 
                    
                   status = addNewSystemContextVariable(variableName,variableValue);
                   json = new Gson().toJson(status);                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "insertContextVariable": {
                String sendData = "";
                
                String contextVariable = request.getParameter("contextVariable");
                String convertToInt = request.getParameter("convertToInt");
    
                if (DialogMain.getIsDomainInitialised()) {                    
                    sendData = insertContextVariable(contextVariable, convertToInt);
                }
                //Logger.info("insertContextVariable  is: " + sendData);                   

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            
            }
            
            case "getInsertQueryMarkup": 
            {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String queryID = request.getParameter("queryID");
                String queryDescription = request.getParameter("queryDescription");

                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getQueryInsertMarkup(queryID, queryDescription);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getInsertQueryContent": 
            {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String queryID = request.getParameter("queryID");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getQueryInsertContent(queryID);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "insertReplaceEmpty": 
            {
                
                String sendData = "false";
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    sendData = insertReplaceEmpty();
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "resetKnowledgebase" :
            {
                String sendData = "false";
                
                if (DialogMain.getIsDomainInitialised()) {                   
                    sendData = resetKnowledgebase();
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "getIsStackFrameNotStacked" :
            {
                String sendData = "IGNORE";
                int frameID;
                
                if (DialogMain.getIsDomainInitialised()) {   
                    String username = request.getParameter("username");
                    String frameIDstr = request.getParameter("frameID");
                    
                    try {
                        frameID = Integer.parseInt(frameIDstr);
                        sendData = getIsStackFrameNotStacked(username,frameID);
                    }
                    catch (NumberFormatException e) {
                        
                    }
                     
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "resetUser" :
            {
                String sendData = "IGNORE";
                
                if (DialogMain.getIsDomainInitialised()) {   
                    String username = request.getParameter("username");

                    sendData = resetUser(username);                         
                }
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "saveUser" :
            {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {   
                    String username = request.getParameter("username");
                    theResponse = saveUser(username);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getIsKnowledgeAcquisitionContextAtRoot" : {
                 
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String username = request.getParameter("username");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getIsKnowledgeAcquisitionContextAtRoot(username);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getContextVariablesAndValues" : {
                 
                TableResponse theResponse = new TableResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String username = request.getParameter("username");
                String dialogIDstr = request.getParameter("dialogID");
                int dialogID;
                
                try {
                    dialogID = Integer.parseInt(dialogIDstr);
                }
                catch (NumberFormatException e) {
                    dialogID = -1;
                }
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getContextVariablesAndValues(username,dialogID);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getContextVariablesAndValuesFromStack" : {
                 
                TableResponse theResponse = new TableResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String username = request.getParameter("username");
                String stackIDstr = request.getParameter("stackID");
                int stackID;
                
                try {
                    stackID = Integer.parseInt(stackIDstr);
                }
                catch (NumberFormatException e) {
                    stackID = -1;
                }
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getContextVariablesAndValuesFromStack(username,stackID);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getUserDialogFromSystemResponse" : {
                 
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String username = request.getParameter("username");
                String dialogIDstr = request.getParameter("dialogID");
                int dialogID;
                
                try {
                    dialogID = Integer.parseInt(dialogIDstr);
                }
                catch (NumberFormatException e) {
                    dialogID = 0;
                }
                
                if (DialogMain.getIsDomainInitialised()) {
                    if (dialogID == 0) {
                        theResponse.setResult("");
                        theResponse.setStatus("OK");
                    }
                    else {
                        theResponse = getUserDialogFromSystemResponse(username,dialogID);
                    }
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "findMatchingDictionaryTerm" : {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
             
                String text = request.getParameter("highlightedText");
                
                if (DialogMain.getIsDomainInitialised()) {                
                    theResponse = findMatchingDictionaryTerm(text);                
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getRepresentativeTermList":
            {          
                TableResponse theResponse = new TableResponse();
                String json = "";
                
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse.setHeader(new String[] {"Terms"});
                    theResponse.setSingleColumnRows(DialogMain.dicConverter.getDictionary().getRepresentativeTermsForWeb());
                    theResponse.setStatus("OK");
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "populateContextVariables": {  /// these are 'local' copies of vars to allow modification
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                Logger.info("populateContextVariables has been called!");
                
                theResponse.setStatus("OK");

                
                if (DialogMain.getIsDomainInitialised()) {                
                    populateContextVariables();                
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "addContextVariableMatchingCriteria": {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
                
                String variableName = request.getParameter("variableName");
                String criteria = request.getParameter("criteria");
                String override = request.getParameter("override");
                
                if (DialogMain.getIsDomainInitialised()) {                
                    theResponse = addContextVariableMatchingCriteria(variableName,criteria,override);                
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "saveContextVariableChanges": {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                
                theResponse.setStatus("");
                
                
                if (DialogMain.getIsDomainInitialised()) {                
                    theResponse = saveContextVariableChanges();                
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getContextVariableCriteria": {
                TableResponse theResponse = new TableResponse();
                String json = "";
                String variableName = request.getParameter("variableName");
                String username = request.getParameter("username");
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getContextVariableCriteria(variableName,username);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getContextVariableOverride": {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                String variableName = request.getParameter("variableName");
                //String username = request.getParameter("username");
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getContextVariableOverride(variableName);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "deleteContextVariableCriteria" : {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                String variableName = request.getParameter("variableName");
                String criteria = request.getParameter("criteria");

                //String username = request.getParameter("username");
                theResponse.setStatus("");
                
                Logger.info("About to try to delete " + criteria + " from " + variableName);
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = deleteContextVariableCriteria(variableName, criteria);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "addContextVariableAction" : {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                String definingVariable = request.getParameter("definingVariable");
                String triggerCondition = request.getParameter("triggerCondition");
                String targetVariable = request.getParameter("targetVariable");
                String targetValue = request.getParameter("targetValue");              

                Logger.info("Adding to variable " + definingVariable + ": " + triggerCondition + " "  + targetVariable + " " + targetValue);
                //String username = request.getParameter("username");
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = addContextVariableAction(definingVariable,triggerCondition,targetVariable,targetValue);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "deleteContextVariableAction" : {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                String definingVariable = request.getParameter("definingVariable");
                String trigger = request.getParameter("triggerCondition");
                String targetVariable = request.getParameter("targetVariable");
                String targetVariableValue = request.getParameter("targetVariableValue");
                Logger.info("Calling deleteContextVariableAction..");
                //String username = request.getParameter("username");
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = deleteContextVariableAction(definingVariable,trigger,targetVariable,targetVariableValue);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getExistingContextVariableActions" : {
                TableResponse theResponse = new TableResponse();
                String json = "";
                String variableName = request.getParameter("variableName");
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getExistingContextVariableActions(variableName);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getAllExistingContextVariableActions" : {
                TableResponse theResponse = new TableResponse();
                String json = "";
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getAllExistingContextVariableActions();
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            } 
            
            case "addSystemContextVariableOverride" : {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                String variableName = request.getParameter("variableName");
                String override = request.getParameter("override");
                String ruleNumber = request.getParameter("ruleNumber");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = addSystemContextVariableOverride(variableName,ruleNumber,override);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            } 
            case "deleteSystemContextVariableOverride" : {
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                String variableName = request.getParameter("variableName");
                String ruleNumber = request.getParameter("ruleNumber");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = deleteSystemContextVariableOverride(variableName,ruleNumber);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            } 
            case "getPreprocessorPreview" : {
                TableResponse theResponse = new TableResponse();
                String json = "";
                theResponse.setStatus("");
                
                String previewText = request.getParameter("previewText");
                String matchText = request.getParameter("matchText");
                String replaceText = request.getParameter("replaceText");
                Boolean regex = request.getParameter("regex").equals("true");
                Boolean wordOnly = request.getParameter("wordOnly").equals("true");
                Boolean startInput = request.getParameter("startInput").equals("true");
                Boolean endInput = request.getParameter("endInput").equals("true");
                Boolean replace = request.getParameter("replace").equals("true");
                Boolean upper = request.getParameter("upper").equals("true");
                Boolean lower = request.getParameter("lower").equals("true");
                Boolean trim = request.getParameter("trim").equals("true");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getPreprocessorPreview(matchText,replaceText,previewText,
                                                        regex,wordOnly,startInput,endInput,
                                                        replace,upper,lower,trim);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            } 
            case "addPreprocessorAction" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                String matchText = request.getParameter("matchText");
                String replaceText = request.getParameter("replaceText");
                Boolean regex = request.getParameter("regex").equals("true");
                Boolean wordOnly = request.getParameter("wordOnly").equals("true");
                Boolean startInput = request.getParameter("startInput").equals("true");
                Boolean endInput = request.getParameter("endInput").equals("true");
                Boolean replace = request.getParameter("replace").equals("true");
                Boolean upper = request.getParameter("upper").equals("true");
                Boolean lower = request.getParameter("lower").equals("true");
                Boolean trim = request.getParameter("trim").equals("true");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = addPreprocessorAction(matchText,replaceText,
                                                        regex, wordOnly, startInput,
                                                        endInput, replace, upper,
                                                        lower, trim);
                    
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "modifyPreprocessorAction" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                String matchText = request.getParameter("matchText");
                String replaceText = request.getParameter("replaceText");
                Boolean regex = request.getParameter("regex").equals("true");
                Boolean wordOnly = request.getParameter("wordOnly").equals("true");
                Boolean startInput = request.getParameter("startInput").equals("true");
                Boolean endInput = request.getParameter("endInput").equals("true");
                Boolean replace = request.getParameter("replace").equals("true");
                Boolean upper = request.getParameter("upper").equals("true");
                Boolean lower = request.getParameter("lower").equals("true");
                Boolean trim = request.getParameter("trim").equals("true");
                String actionID = request.getParameter("actionID");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = modifyPreprocessorAction(matchText,replaceText,
                                                        regex, wordOnly, startInput,
                                                        endInput, replace, upper,
                                                        lower, trim, actionID);
                    
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getPreprocessorActions" : {
                
                TableResponse theResponse = new TableResponse();
                String json = "";
                theResponse.setStatus("");
                
               

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getPreprocessorActions();
                    
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getPreprocessorActionDetails" : {
                TableResponse theResponse = new TableResponse();
                String json = "";
                theResponse.setStatus("");
                
                String actionID = request.getParameter("actionID");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getPreprocessorActionDetails(actionID);
                    
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            
            case "moveUpPreprocessorAction" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                String actionID = request.getParameter("actionID");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = moveUpPreprocessorAction(actionID);                  
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "moveDownPreprocessorAction" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                String actionID = request.getParameter("actionID");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = moveDownPreprocessorAction(actionID);                  
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            
            
            case "deletePreprocessorAction" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                String actionID = request.getParameter("actionID");

                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = deletePreprocessorAction(actionID);                  
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "savePreprocessorActions" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = savePreprocessorActions();                  
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "initialiseActionList" : {
                
                StatusResponse theResponse = new StatusResponse();
                String json = "";
                theResponse.setStatus("");
                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = initialiseActionList();                  
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
 /********************************************************************************************************************************/           
            
            
            
            /**************** queryBuilder calls ****************/
            
            case "getQueryTableNames": {
                String sendData = "";
                String json = "";
                
    
                if (DialogMain.getIsDomainInitialised()) { 
                    if (getReferenceTableNames() == null) {
                        setReferenceTableNames();
                    }
                    
                    Object[] [] variables = getQueryTableNames();
                    json = new Gson().toJson(variables);
                    //Logger.info("variables JSON is: " + json);
                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "setSelectedTableID": {
                String tableID = request.getParameter("tableID");
                
                if (DialogMain.getIsDomainInitialised()) { 
                    try {
                        int theTableID = Integer.parseInt(tableID);
                        
                        // avoid javascript attempts to index table names outside the allowable ones..
                        if (theTableID <= getReferenceTableNames().length)
                            setQuerySelectedTableID(theTableID);
                    }
                    catch (NumberFormatException e) {
                        Logger.info(e.getMessage());
                    }                  
                }

                break;
            }
            case "getQueryFieldNames": {
                String sendData = "";
                String json = "";
                
    
                if (DialogMain.getIsDomainInitialised()) {                    
                    
                    Object[] [] fieldNames = getQueryFieldNames();
                    json = new Gson().toJson(fieldNames);
                    //Logger.info("fieldNames JSON is: " + json);
                    
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            
            case "getQueryMarkup": {
                String sendData = "";
                //Logger.info("getQueryMarkup starting..");
                               
                String queryMode = request.getParameter("queryMode");
                int queryModeInt;
                
                try {                   
                    queryModeInt = Integer.parseInt(queryMode);
                }
                catch (NumberFormatException e) {
                    queryModeInt = -1;
                    Logger.info("Error with queryModeInt!");
                }
                
                String json ="";
                Gson gson = new Gson();
                StatusResponse theResponse;
                
    
                if (DialogMain.getIsDomainInitialised()) {                    
                    String[] fieldList;
                    boolean countSelected;
                    String currentMarkup;
                    String fieldNameListJSON;
                    boolean isFormatMarkup;
                    String prefixData = "";
                    String postfixData = "";
                    boolean isLeftJoin;
                    boolean multivalued;
                    
                    switch (queryModeInt) {
                        
                        case ORDERTYPE:    
                        case SELECTTYPE: 
                        {
                            //Logger.info("mode is " + queryModeInt);
                            fieldNameListJSON = request.getParameter("fieldList");
                            countSelected = request.getParameter("countSelected").equals("true");
                            currentMarkup = request.getParameter("currentMarkup");
                            isFormatMarkup = request.getParameter("isFormatMarkup").equals("true");
                            if (isFormatMarkup) {
                                prefixData = request.getParameter("prefixData");
                                postfixData = request.getParameter("postfixData");
                            }

                            fieldList = gson.fromJson(fieldNameListJSON, String[].class);
                            
                            theResponse = getQueryMarkup(queryModeInt,fieldList,countSelected,currentMarkup,isFormatMarkup,prefixData,postfixData,false,false);
                            json = new Gson().toJson(theResponse); 
                            break;
                        }
                        case JOINTYPE: {
                            //Logger.info("mode is " + queryModeInt);
                            fieldNameListJSON = request.getParameter("fieldList");
                            countSelected = request.getParameter("countSelected").equals("true");
                            currentMarkup = request.getParameter("currentMarkup");
                            isFormatMarkup = request.getParameter("isFormatMarkup").equals("true");
                            if (isFormatMarkup) {
                                prefixData = request.getParameter("prefixData");
                                postfixData = request.getParameter("postfixData");
                            }
                            isLeftJoin = request.getParameter("isLeftJoin").equals("true");
                            multivalued = request.getParameter("multivalued").equals("true");


                            fieldList = gson.fromJson(fieldNameListJSON, String[].class);
                            
                            theResponse = getQueryMarkup(queryModeInt,fieldList,countSelected,currentMarkup,isFormatMarkup,prefixData,postfixData,isLeftJoin,multivalued);
                            json = new Gson().toJson(theResponse); 
                            break;
                        }
                       
                        
                    }
                                                           
                }
                
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            
            case "getQueryContextVarSelectorMarkup": 
            {
                
                StatusResponse theResponse;
                String json = "";
             
                String contextVar = request.getParameter("contextVar");
                String currentMarkup = request.getParameter("currentMarkup");

                
                if (DialogMain.getIsDomainInitialised()) {
                    theResponse = getQueryContextVarSelectorMarkup(contextVar, currentMarkup);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }
            case "getQueryCriterionMarkup":
            {
                StatusResponse theResponse;
                String json = "";
             
                String contextVar = request.getParameter("contextVar");
                String fieldNameListJSON = request.getParameter("fieldList");
                String[] fieldList;
                String fixedValue = request.getParameter("fixedValue");
                boolean partialMatch = request.getParameter("partialMatch").equals("true");
                String radioSelectionStr = request.getParameter("radioSelection");
                int radioSelection;
                Gson gson = new Gson();
                
                try {
                    radioSelection = Integer.parseInt(radioSelectionStr);
                }
                catch (NumberFormatException e) {
                    radioSelection = -1;
                }

                
                if (DialogMain.getIsDomainInitialised()) {
                    fieldList = gson.fromJson(fieldNameListJSON, String[].class);
                    theResponse = getQueryCriterionMarkup(fieldList, fixedValue,contextVar,partialMatch,radioSelection);
                    json = new Gson().toJson(theResponse); 
                }
                
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;
            }

            case "getQueryPreviewText": {
                String sendData = "";
                //Logger.info("getQueryPreviewText starting..");
                               
                String queryText = request.getParameter("queryText");


                
                String json ="";
                Gson gson = new Gson();
                StatusResponse theResponse = new StatusResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {                                       
                    theResponse = getQueryPreviewText(queryText);
                    json = new Gson().toJson(theResponse);                                       
                }                               
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            case "getQuerySpecificOuterMarkup": {
                String sendData = "";
                //Logger.info("getQuerySpecificOuterMarkup starting..");
                               
                String queryText = request.getParameter("queryText");
                String queryType = request.getParameter("queryType");


                String json = "";
                Gson gson = new Gson();
                StatusResponse theResponse = new StatusResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {                                       
                    theResponse = getQuerySpecificOuterMarkup(queryType, queryText);
                    json = new Gson().toJson(theResponse);                                       
                }                               
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            case "setSavedQuery": {
              
                String sendData = "";
                //Logger.info("getQuerySpecificOuterMarkup starting..");
                               
                String queryText = request.getParameter("queryText");
                String queryDescription = request.getParameter("queryDescription");


                String json = "";
                Gson gson = new Gson();
                StatusResponse theResponse = new StatusResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {                                       
                    theResponse = setSavedQuery(queryText, queryDescription);
                    json = new Gson().toJson(theResponse);                                       
                }                               
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            case "getSavedQueryList": {
              
                String sendData = "";
                //Logger.info("getSavedQueryList starting..");

                String json = "";
                Gson gson = new Gson();
                TableResponse theResponse = new TableResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {                                       
                    theResponse = getSavedQueryList();
                    json = new Gson().toJson(theResponse);                                       
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            case "getSavedQuerySnippetList": {
              
                String sendData = "";
                //Logger.info("getSavedQuerySnippetList starting..");

                String json = "";
                Gson gson = new Gson();
                TableResponse theResponse = new TableResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {                                       
                    theResponse = getSavedQuerySnippetList();
                    json = new Gson().toJson(theResponse);                                       
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            
            case "deleteQuerySnippet": {
              
                String sendData = "";
                //Logger.info("deleteQuerySnippet starting..");
                
                String queryIDstr = request.getParameter("queryID");
                int queryID; 
                
                try {
                    queryID = Integer.parseInt(queryIDstr);
                }
                catch (NumberFormatException e) {
                    queryID = -1;
                }

                String json = "";
                Gson gson = new Gson();
                StatusResponse theResponse = new StatusResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) { 
                    if (queryID != -1) { 
                        theResponse = deleteQuerySnippet(queryID);
                        json = new Gson().toJson(theResponse);     
                    }
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            
            case "saveQuerySnippet": {
              
                String sendData = "";
                //Logger.info("deleteQuerySnippet starting..");
                
                String queryDescription = request.getParameter("queryDescription");
                String queryFields = request.getParameter("queryFields");
                String queryJoins = request.getParameter("queryJoins");
                String queryCriteria = request.getParameter("queryCriteria");

                String json = "";
                Gson gson = new Gson();
                StatusResponse theResponse = new StatusResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {        
                    theResponse = saveQuerySnippet(queryDescription, queryFields, queryJoins, queryCriteria);
                    json = new Gson().toJson(theResponse);                         
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            case "insertQuerySnippet": {
              
                String sendData = "";
                //Logger.info("insertQuerySnippet starting..");
                
                String queryIDstr = request.getParameter("queryID");
                int queryID; 
                
                try {
                    queryID = Integer.parseInt(queryIDstr);
                }
                catch (NumberFormatException e) {
                    queryID = -1;
                }


                String json = "";
                Gson gson = new Gson();
                TableResponse theResponse = new TableResponse();
                theResponse.setStatus("The domain is not initialised");
                
    
                if (DialogMain.getIsDomainInitialised()) {
                    if (queryID != -1) {
                        theResponse = insertQuerySnippet(queryID);
                        json = new Gson().toJson(theResponse);  
                    }
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break; 
            }
            
            
            
            
            
            
            
            
            case "getDialogHistory":
            {
                String userName = request.getParameter("username");
                String json = new Gson().toJson(getDialogHistory(userName));
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                //Logger.info(json);
                break;
            }
            case "getRulebase":
            {
                //Logger.info("getting rulebase");
                
                String sendData = ruleTreeHTMLRenderer(Main.KB.getRootRule());
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "getRulebaseJSON":
            {
                StatusResponse theResponse = new StatusResponse();
                theResponse.setStatus("The domain is not initialised");
                String json = "";
                
                if (DialogMain.getIsDomainInitialised()) {  
                    theResponse.setStatus("OK");
                    theResponse.setResult(ruleTreeHTMLRenderer(Main.KB.getRootRule()));
                    json = new Gson().toJson(theResponse);                         
                }
                //Logger.info("JSON is: " + json);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                break;               
            }
            case "getRuleDetails":
            {
                //Logger.info("getting rule details");
                String ruleID = request.getParameter("ruleID");
                String json = new Gson().toJson(getRuleDetails(ruleID));
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(json);
                //Logger.info(json);
                break;
            }
            case "getFiredRuleList":
            {
                String username = request.getParameter("username");

                String dialogID = request.getParameter("dialogID");
                Logger.info("getting fired rule details username: " + username + "dialogID: " + dialogID);

                String sendData = new Gson().toJson(getFiredRulesForGUI(username,dialogID));
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "getUsers":
            {
               
                //Logger.info("getting user list");
                String sessionId = request.getSession().getId();
                String sendData = DialogMain.getDialogUserList().toStringCommaList();
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            } 
            
            case "getSessionID":
            {
                String sessionId = request.getSession().getId();
                response.setContentType("text/plain; charset=UTF-8");
                //Logger.info("Session ID is " + sessionId);
                response.getWriter().write(sessionId);
                break;
            }
            case "getMaxStackID":
            {
                String userName = request.getParameter("username");
                String userId = request.getSession().getId();
                String sendData = getMaxStackID(userName);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            case "getDialogsAndRulesFromStackFrame":
            {
                String userName = request.getParameter("username");
                String stackFrameID = request.getParameter("stackFrameID");
                //String userId = request.getSession().getId();
                String sendData = new Gson().toJson(getDialogAndRulesFromStackFrame(userName,stackFrameID));
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }       
            case "getDialogsAndStackFramesFromRule":
            {
                String userName = request.getParameter("username");
                String ruleID = request.getParameter("ruleID");
                //String userId = request.getSession().getId();
                String sendData = new Gson().toJson(getDialogsAndStackFramesFromRule(userName,ruleID));
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
        }         
    }
    
    private StatusResponse deleteContextVariableCriteria(String variableName, String criteria) {
        StatusResponse theResponse = new StatusResponse();
        
        theResponse.setStatus("OK");
        
        if (!variableName.equals("") && !criteria.equals("")) {
            Logger.info("deleting criteria:" + criteria + " from variable: " + variableName);
            contextVariables.get(variableName).deleteVariableValue(criteria);
        }
        
        return theResponse;
    }
    
    private StatusResponse addContextVariableMatchingCriteria(String varName, String criteria, String override) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        
        // create the variable if it doesn't already exist..
        if (contextVariables.get(varName) == null) {
            ContextVariable aVariable = new ContextVariable();
            aVariable.setVariableName(varName);
            contextVariables.put(varName, aVariable);
        }
        
        // we might just be updating the variable's override value, and sometimes as criteria already exist,
        // the current submitted criteria might be empty.. (don't add empty criteria!)
        if (!criteria.equals("")) {
            if (!contextVariables.get(varName).isVariableValueExist(criteria)) {       
                contextVariables.get(varName).addVariableValue(criteria);
            }
        }
    
        // if the override is blank, we in essence remove it. Otherwise it has a non-empty value.
        contextVariables.get(varName).setVariableValueOverride(override);           
        
        return theResponse;
    }
    
    
    
    
    
    
    
    
    private StatusResponse findMatchingDictionaryTerm(String highlightedText) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("OK");
        theResponse.setResult("");
        
        String dictionaryTerm = DialogMain.dicConverter.getFirstMatchingTermFromDic(highlightedText,true);
        if (dictionaryTerm != null) {
            if (!dictionaryTerm.equals(highlightedText)) {
                theResponse.setResult(dictionaryTerm);
                Logger.info("found term: " + dictionaryTerm + " in text: " + highlightedText);
            }
        }
        
        return theResponse;
    }
    
    private StatusResponse getUserDialogFromSystemResponse(String username,int dialogID) {
        Logger.info("The dialogID for user " + username + " is " + dialogID);
        StatusResponse theResponse = new StatusResponse();
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
        synchronized (syncLock) {   
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            for (IDialogInstance aSystemDialogInstance : DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getBaseSet().values()) {
                Logger.info("System dialog found is: [" + aSystemDialogInstance.getDialogId() + "] " + aSystemDialogInstance.getDialogStr());
            }
            SystemDialogInstance systemDialogInstance = (SystemDialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getDialoginstanceById(dialogID);
            String userDialog = systemDialogInstance.getDerivedCaseRecentData();
            theResponse.setResult(userDialog);
            theResponse.setStatus("OK");
        }  
        return theResponse;
    }
    
    private StatusResponse getIsKnowledgeAcquisitionContextAtRoot(String username) {
        StatusResponse theResponse = new StatusResponse();
        
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
        synchronized (syncLock) {   
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            RuleSet kbInferenceResult = DialogMain.getDialogUserList().getCurrentKAInferenceResult();
            theResponse.setResult("" + (kbInferenceResult.getLastRule().getRuleId() == 0));
            theResponse.setStatus("OK");
        }  
        
        return theResponse;
    }
    
    private String resetUser(String username) {
        Logger.info("Clearing history for user: " + username);
        String response = "IGNORE";
    
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
       
        synchronized (syncLock) {   
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            DialogMain.getDialogUserList().getCurrentDialogUser().resetUser();   
            response = "OK";
        }  
        
        return response;
    }
    
    private StatusResponse saveUser(String username) {
        Logger.info("Saving history for user: " + username);
        StatusResponse response = new StatusResponse();
    
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
       
        synchronized (syncLock) {   
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            response.setResult(DialogMain.getDialogUserList().getCurrentDialogUser().getDialogRepository().getAllDialogStringWithNewLine());   
            response.setStatus("OK");
        }  
        
        return response;
    }
    
    
    private String getIsStackFrameNotStacked(String username, int frameID) {
        String response = "IGNORE";
                       
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
       
        synchronized (syncLock) {  
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            RuleSet ruleSet  = DialogMain.getDialogUserList().getCurrentStackInferenceResult().getBaseSet().get(frameID).getInferenceResult();
            for (Rule aRule: ruleSet.getBase().values()) {
                if (aRule.getDoNotStack() || aRule.getIsParentDoNotStack()) {
                    response = "OK";
                    break;
                }
            }
        } 
           
        return response;
    }
    
    private String resetKnowledgebase() {
        String theResponse = "OK";
        
        synchronized (syncLock) {
            
            for (Map.Entry me : DialogMain.getDialogUserList().getBaseSet().entrySet()) {
                DialogUser aDialogUser = (DialogUser) me.getValue();
                //Logger.info("Clearing dialog history for: " + aDialogUser.getUsername());
                aDialogUser.clearConversationHistory();
                aDialogUser.getDialogRepository().clearRepository();
            }
                     
            DBManager.initialise(Main.domain.getDomainName(), contextPath);

            try {
                DialogCaseArchiveModule.createTextFileWithCaseStructure(contextPath);
            } catch (Exception ex) {
                Logger.error(ex.getMessage());
            }

            DialogMain.initialiseSystem(contextPath,Main.domain.getDomainName(), Main.domain.getReasonerType(), Main.domain.getDescription(), DialogMain.defaultResponse);

            DomainLoader.inserDomainDetails(Main.domain.getDomainName(), Main.domain.getDescription(), Main.domain.getReasonerType());
            CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());

            RuleLoader.insertRuleConclusions(0, Main.KB.getRootRule().getConclusion());
            RuleLoader.insertRule(0, Main.KB.getRootRule(), 0);
            // generate initial stack inference instances for existing users in the interface list..

    
            for (Map.Entry me : DialogMain.getDialogUserList().getBaseSet().entrySet()) {
                DialogUser aDialogUser = (DialogUser)me.getValue();
                aDialogUser.clearMCRDRStackResultSet();
                aDialogUser.addInitialMCRDRStackResultInstance();
            }
        }
        
        return theResponse;
    }
    
    /*****************************************************/
    /*******************   QUERY calls *******************/
    /*****************************************************/
    private String insertReplaceEmpty() {
        String theResponse = "";
        
        theResponse = OutputParser.META_REPLACE_START + 
                    " query-goes-here " +
                    OutputParser.META_REPLACE_EMPTY + " no-result-text-goes-here " +
                    OutputParser.META_REPLACE_TEXT + " override-text-goes-here  " +
                    OutputParser.META_REPLACE_END;
        
        return theResponse;
    }
    
    private TableResponse insertQuerySnippet(int queryID) {
        TableResponse theResponse = new TableResponse();
        String[] columns = {"ID"};
        String[] row;
        theResponse.setHeader(columns);
        theResponse.setStatus("query ID not found!");
        
        SavedQueryTemplate theQuery = DialogMain.savedQueryTemplateList.get(queryID);

        if (theQuery != null){     
            row = new String[] {theQuery.getSelect()};
            theResponse.addRow(row);
            row = new String[] {theQuery.getJoin()};
            theResponse.addRow(row);    
            row = new String[] {theQuery.getCriteria()};
            theResponse.addRow(row);    

            theResponse.setStatus("OK");
        }
        return theResponse;
    }
    
    private StatusResponse saveQuerySnippet(String description, String fields, String joins, String criteria) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("Error");
        
        SavedQueryTemplate aSavedQuery = new SavedQueryTemplate();
            aSavedQuery.setDescription(description);
            aSavedQuery.setSelect(fields);
            aSavedQuery.setjoin(joins);
            aSavedQuery.setCriteria(criteria);
            int id = SqliteOperation.insertNewQueryTemplateDetails(aSavedQuery);
            if (id != -1) {
                aSavedQuery.setId(id);
                DialogMain.savedQueryTemplateList.put(id, aSavedQuery);
                theResponse.setStatus("OK");
            }
            
        return theResponse;    
    }
    
    private StatusResponse deleteQuerySnippet(int queryID) {
        StatusResponse theResponse = new StatusResponse();
        
        DialogMain.savedQueryTemplateList.remove(queryID);
        SqliteOperation.deleteSavedQueryTemplate(queryID);
            
        theResponse.setStatus("OK");
        return theResponse;
    }
    
    private TableResponse getSavedQuerySnippetList() {
        TableResponse theResponse = new TableResponse();
        String[] row;
        String[] columns = {"ID","Description","Content"};
        theResponse.setHeader(columns);
        
        for (Map.Entry me : DialogMain.savedQueryTemplateList.entrySet()) {
            SavedQueryTemplate aSavedQuery = (SavedQueryTemplate)me.getValue();
            row = new String[3];
            row[0] = "" + aSavedQuery.getId();
            row[1] = aSavedQuery.getDescription();
            row[2] = "Fields: " + aSavedQuery.getSelect() + " \nJoins:" + aSavedQuery.getJoin() + " \nCriteria:" + aSavedQuery.getCriteria();
            theResponse.addRow(row);
            //Logger.info("Added " + row[0] + " " + row[1] + " to snippet list!");
        }
        

        theResponse.setStatus("OK");
        return theResponse;
    }
    
    private TableResponse getSavedQueryList() {
        TableResponse theResponse = new TableResponse();
        String[] columns = {"ID","Description","Query"};
        String[] row;
        //Logger.info("getSavedQueryList");
        theResponse.setHeader(columns);
        
        for (Map.Entry me : DialogMain.conclusionQueryList.entrySet()) {
            int id = (int) me.getKey();
            ConclusionQuery aConclusionQuery = (ConclusionQuery)me.getValue();
            row = new String[3];
            row[0] = "" + id;
            row[1] = aConclusionQuery.getDescription();
            row[2] = aConclusionQuery.getQuery();
            //Logger.info("Adding items to row:" + row[0] +" " +  row[1] + " " +  row[2]);
            theResponse.addRow(row);
        }
        theResponse.setStatus("OK");
        return theResponse;
    }
    
    private StatusResponse setSavedQuery(String queryText, String description) {
        StatusResponse theResponse = new StatusResponse();
        
        int id = SqliteOperation.insertNewConclusionQuery(description, queryText);
        String query = OutputParser.getTag(id + queryText,OutputParser.DB_DATABASE_TYPE);
        String descriptionTag = OutputParser.getTag(description, OutputParser.DB_DESCRIPTION_TYPE);
        //String queryPlaceholder = OutputParser.getTag(Integer.toString(id) + descriptionTag, OutputParser.DB_PLACEHOLDER_TYPE);

        ConclusionQuery theConclusionQuery = new ConclusionQuery();
        theConclusionQuery.setId(id);
        theConclusionQuery.setDescription(description);
        theConclusionQuery.setQuery(query);
        DialogMain.conclusionQueryList.put(id, theConclusionQuery);
        
        theResponse.setResult("Query saved - query ID is " + id);
        theResponse.setStatus("OK");
        
        return theResponse;
    }
    
    private StatusResponse getQueryPreviewText(String queryText) {
        StatusResponse theResponse = new StatusResponse();
        String queryToPreview = OutputParser.getTag(queryText,OutputParser.DB_DATABASE_TYPE);
        String dbresult = OutputParser.parseOtherTerms(OutputParser.replaceSingleDatabaseQuery("",queryToPreview,true),true);
        
        theResponse.setResult(dbresult);
        
        if (DBOperation.wasDatabaseError()) {
                theResponse.setStatus("Error: " + DBOperation.getLastDatabaseError());
                DBOperation.resetDatabaseError();
            }
        else {
            theResponse.setStatus("OK");
        }
        return theResponse;
    }
    
    private StatusResponse getQueryContextVarSelectorMarkup(String contextVar, String currentMarkup) {
        StatusResponse theResponse = new StatusResponse();
        String contextVarMarkup = OutputParser.getTag(contextVar,OutputParser.DB_SELECTOR_TYPE);
        theResponse.setResult(contextVarMarkup);
        theResponse.setStatus("OK");
        
        if (currentMarkup.contains(contextVarMarkup)) {
            theResponse.setStatus("Cannot add the same context variable value as a field selector more than once!");
        }
        
        return theResponse;
    }
    
    private StatusResponse getQueryCriterionMarkup(String[] fieldNames, String fixedValue, String contextVar, boolean partialMatch, int radioSelection) {
        StatusResponse theResponse = new StatusResponse();
        
        Object[][] tableNames = getReferenceTableNames();
        int selectedTableRow = getselectedTableRefId();
        String selectedTable = (String) tableNames[selectedTableRow][0];
        
        String theField =  fieldNames[0];
        String fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
        String tableTag = OutputParser.getTag(selectedTable + fieldTag, OutputParser.DB_ORDER_TYPE);
        

        String criterionValue = "";
        String criterionTag;
        String containsTag = "";
        String contextTag;
        boolean okToGo = true;
        
        if (partialMatch) {
            containsTag = OutputParser.getTag("Y",OutputParser.DB_PARTIALMATCH_TYPE);          
        }
        
        switch (radioSelection) {
            case 1: // fixed value
                criterionValue = OutputParser.getTag(fixedValue + containsTag, OutputParser.DB_CRITERION_VALUE_FIXED_TYPE);
                break;
            case 2: // fixed value with context var
                contextTag = OutputParser.getTag(contextVar, OutputParser.DB_CRITERION_VALUE_CONTEXT_TYPE);
                criterionValue = OutputParser.getTag(fixedValue + contextTag + containsTag, OutputParser.DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE);
                break;
            case 3: // fixed value with numeric from context var
                contextTag = OutputParser.getTag(contextVar, OutputParser.DB_CRITERION_VALUE_NUMBER_TYPE);
                criterionValue = OutputParser.getTag(fixedValue + contextTag + containsTag, OutputParser.DB_CRITERION_VALUE_FIXED_NUMBER_TYPE);
                break;
            case 4: // context var
                criterionValue = OutputParser.getTag(contextVar + containsTag, OutputParser.DB_CRITERION_VALUE_CONTEXT_TYPE);
                break;
            case 5: // numeric from context var
                criterionValue = OutputParser.getTag(contextVar + containsTag, OutputParser.DB_CRITERION_VALUE_NUMBER_TYPE);
                break;    
        }

        criterionTag = OutputParser.getTag(selectedTable + fieldTag + criterionValue, OutputParser.DB_CRITERION_TYPE);
            
        theResponse.setResult(criterionTag);
        theResponse.setStatus("OK");
        
        return theResponse;
    }
    
    // some XML result has an outer wrapper, for instance joins consist of two inner <JVA>
    // but surrounded by a <JOI> tag. This function returns the inner tags surrounded by the outer.
    private StatusResponse getQuerySpecificOuterMarkup(String type, String innerMarkup) {
        StatusResponse theResponse = new StatusResponse();
        theResponse.setStatus("A database XML tag was requested that does not exist");
                
        
        switch (type) {
            case "JOIN" :
                String joinTag = OutputParser.getTag(innerMarkup, OutputParser.DB_JOIN_TYPE);
                theResponse.setResult(joinTag);
                theResponse.setStatus("OK");
                break;
        }
        
        return theResponse;
    }
    
    private StatusResponse getQueryMarkup(int mode, String[] fieldNames, boolean countboxSelected, String currentMarkup, boolean isFormatMarkup, String prefixData, String postfixData, boolean isLeftJoin, boolean multivalued) {

        String tableTag;
        StatusResponse theResponse = new StatusResponse();
        String theField;
        String fieldTag;
        String countError = "When using the COUNT option, only one field can be in the selection text.";

        Object[][] tableNames = getReferenceTableNames();
        int selectedTableRow = getselectedTableRefId();
        String selectedTable = (String) tableNames[selectedTableRow][0];

        switch (mode) {
            case JOINTYPE:
                // JOIN FIELDS
                // we should only have one row selected as the selection mode was set to single selection..
                //Logger.info("In join code..");
                
                theField =  fieldNames[0];
                fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                String joinValueTag;
                
                if (multivalued && !isLeftJoin) {
                    //Logger.info("This value is multivalued!");
                    joinValueTag = OutputParser.getTag(selectedTable + fieldTag,OutputParser.DB_MULTIV_JOIN_TYPE);
                    //joinValueTag = OutputParser.getTag(Integer.toString(getselectedTableRefId()) + fieldTag,OutputParser.DB_MULTIV_JOIN_TYPE);
                }
                else  {
                    joinValueTag = OutputParser.getTag(selectedTable + fieldTag,OutputParser.DB_JVALUE_TYPE); 
                    //joinValueTag = OutputParser.getTag(Integer.toString(getselectedTableRefId()) + fieldTag,OutputParser.DB_JVALUE_TYPE); 
                }
                theResponse.setResult(joinValueTag);
                theResponse.setStatus("OK");

                    
    
                break;
            case ORDERTYPE:
            {                
                // ORDER BY FIELD
                theField =  fieldNames[0];
                fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                tableTag = OutputParser.getTag(selectedTable + fieldTag, OutputParser.DB_ORDER_TYPE);
                theResponse.setResult(tableTag);
                
                //Logger.info("ORDERBY: Looking at table: " +selectedTable + " and field:" +  theField);
                if (currentMarkup.contains(OutputParser.getStartTag(OutputParser.DB_ORDER_TYPE))) {
                    theResponse.setStatus("Only one order-by field is currently supported!");
                }
                else if (!containsTableAndFieldTag(currentMarkup,selectedTable,theField)) {
                    theResponse.setStatus("You cannot order-by on a field that is not selected");
                }
                else
                    theResponse.setStatus("OK");
                break;
            }
            case SELECTTYPE: 
            {
                if (countboxSelected) {
                    if (currentMarkup.equals("")) {
                        if (fieldNames.length == 1) {
                            DatabaseQueryBuilderGUI.currentMarkerType = OutputParser.DB_COUNT_TYPE;

                            theField = fieldNames[0];
                            fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                            tableTag = OutputParser.getTag(selectedTable + fieldTag, OutputParser.DB_COUNT_TYPE);
                            theResponse.setResult(tableTag);
                            theResponse.setStatus("OK");
                        }
                        else {
                            theResponse.setStatus(countError);                
                        }                      
                    }
                    else {
                        theResponse.setStatus(countError); 
                    }
                }  // ADD FOR FORMAT WHEN READY..
                else if(isFormatMarkup) {
                    theField = fieldNames[0];
                    fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                    String prefixTag = OutputParser.getTag(prefixData, OutputParser.DB_PREFIX_TYPE);
                    String postfixTag = OutputParser.getTag(postfixData, OutputParser.DB_POSTFIX_TYPE);
                    String tableValue = selectedTable + fieldTag + prefixTag + postfixTag;
                    String formatTag = OutputParser.getTag(tableValue, OutputParser.DB_FORMAT_TYPE);
                    theResponse.setResult(formatTag);
                    theResponse.setStatus("OK");                   
                }
                else {
                    // can't add another selection field if we're already doing an aggregate count
                    if (currentMarkup.contains(OutputParser.getStartTag(OutputParser.DB_COUNT_TYPE))) {
                        theResponse.setStatus(countError); 
                    }
                    else {
                        String temp = "";
                        boolean repeatFound = false;
                        String statusError = "<ul style='margin-left:20px;'>";
                        for (String aField: fieldNames) {
                            theField = aField;
                            fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                            tableTag = OutputParser.getTag(selectedTable + fieldTag, OutputParser.DB_TABLE_TYPE);
                            if (currentMarkup.contains(tableTag)) {
                                repeatFound = true;
                                statusError += "<li>table:" + selectedTable + ", field:" + theField + "</li>";
                            }
                            temp += tableTag;
                            
                        }
                        statusError += "</ul>";
                        theResponse.setResult(temp);
                        if (repeatFound) {
                            theResponse.setStatus("You cannot add duplicate fields for selection - the following already exist:" + statusError);
                        }
                        else {
                            theResponse.setStatus("OK");
                        }
                    }
                }
            }
        }
        return theResponse;
    }  
    
    private void setQuerySelectedTableID(int tableID) {
        //Logger.info("Setting the reference Table ID to " + tableID);
        setSelectedTableRefId(tableID);
    }
        
    private Object[][] getQueryTableNames() {
        Object[][] response;
        
        String query = "select table_name from information_schema.tables where table_schema='" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' order by table_name";

        response = getQueryModelResult(query);
       
        return response;
    }
    
    private Object[][] getQueryFieldNames() {
        Object[][] response;
        
        Object[][] tableNames = getReferenceTableNames();
        int selectedTableRow = getselectedTableRefId();
        String selectedTableName = (String) tableNames[selectedTableRow][0];
        //Logger.info("Selected table name is:" + selectedTableName);
            
        String query = "select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'";
        
        response = getQueryModelResult(query);
       
        return response;
    }
    
    /*****************************************************/
    /************ new conclusion calls *******************/
    /*****************************************************/

    private StatusResponse getQueryInsertMarkup(String queryID, String queryDescription) {
        StatusResponse theResponse = new StatusResponse();
        
        
        String descriptionTag = OutputParser.getTag(queryDescription, OutputParser.DB_DESCRIPTION_TYPE);
        String queryPlaceholder = OutputParser.getTag(queryID + descriptionTag, OutputParser.DB_PLACEHOLDER_TYPE);
        theResponse.setResult(queryPlaceholder);
        theResponse.setStatus("OK");
        return theResponse;
        
    }
    
    private StatusResponse getQueryInsertContent(String queryIDstr) {
        StatusResponse theResponse = new StatusResponse();
        int queryID;
        try {
            queryID = Integer.parseInt(queryIDstr);
        }
        catch (NumberFormatException e) {
            queryID = -1;
        }
        String queryContent =  DialogMain.conclusionQueryList.get(queryID).getQuery();
        theResponse.setResult(queryContent);
        theResponse.setStatus("OK");
        return theResponse;
        
    }
    
    private String insertContextVariable(String contextVariable, String convertToInt) {
        
        String conclusionTag = "";
        
        if (contextVariable != null) {
            if (convertToInt.equals("true")) {
                conclusionTag = OutputParser.getTag(contextVariable, OutputParser.CONTEXT_NUMBER_TYPE);  
            }
            else {
                conclusionTag = OutputParser.getTag(contextVariable, OutputParser.CONTEXT_LITERAL_TYPE);
            }
            
        }
        return conclusionTag;
    }
    
    
    
    private String[] getCommandCategoryList() {
        String[] result = new String[CommandInstance.devicelist.length];
        
        int i=0;
        for (String aCategory: CommandInstance.devicelist) {
            result[i] = aCategory;
            i++;
        }
        return result;
    }
    
    private String[] getCommandActionList(String category) {
        String[] result = new String[]{""};  // default value
        
        
        if (!category.equals("")) {
            ICommandInstance aCommandInstance = CommandFactory.createCommandInstance(category);
            result = new String[aCommandInstance.getDeviceActionList().length];
            int i = 0;
            for (String anAction: aCommandInstance.getDeviceActionList()) {
                result[i] = anAction;
                i++;
            }
        }
        
        return result;
    }
     

    public int getMostRecentUserDialogID(String sessionID) {                                         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(sessionID);
        int msg = -1;
                 
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
            msg = DialogMain.getDialogUserList().getCurrentMostRecentDialogID();
        }           

        return msg;
    }
    
    public int getMostRecentSystemDialogID(String sessionID) {                                         
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(sessionID);
        int msg = -1;
                 
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
            msg  = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getMostRecentSystemDialogInstance().getDialogId();           
        }           

        return msg;
    }
    
    public IDialogInstance getSelectedDialog(String username, String dialogID) {
        IDialogInstance theDialog;
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(username);
        int dialogIDInt = Integer.parseInt(dialogID);
        
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);
            theDialog = DialogMain.getDialogUserList().getCurrentDialogRepository().getUserDialogRepository().getDialoginstanceById(dialogIDInt);
        }
        
        return theDialog;
    }
    
    public JSONDialogAndRuleAndStackDetails getDialogsAndStackFramesFromRule(String username, String ruleId) {
        JSONDialogAndRuleAndStackDetails details = new JSONDialogAndRuleAndStackDetails();
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
        DialogSet systemDialogHistory;
        synchronized (syncLock) {  
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            systemDialogHistory = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository();
            for (IDialogInstance aDialog: systemDialogHistory.getBaseSet().values()) {
                //Logger.info("###### Rule: " + ruleId + ", looking at dialog:" + aDialog.getDialogId());
                //Logger.info("######## stack frame" + aDialog.getStackId());
                RuleSet ruleset = DialogMain.getDialogUserList().getCurrentDialogUser().getMCRDRStackResultSet().getMCRDRStackResultInstanceById(aDialog.getStackId()).getInferenceResult();
            
                for (Rule aRule: ruleset.getBase().values()) {
                    if (aRule.getRuleId() == Integer.parseInt(ruleId)) {
                        //Logger.info("Rule " + aRule.getRuleId() + " matches our target Id, so dialog " + aDialog.getDialogId() + " is added to the list.."); 
                        details.addDialogId(aDialog.getDialogId());
                        details.addStackFrameId(aDialog.getStackId());
                    }
                }
            }
            // DEAR ME - we might have stack frames with rules that aren't associated directly with dailogs... (frames created automatically due to intermediate rules satisfied)
            MCRDRStackResultSet stackedResults = DialogMain.getDialogUserList().getCurrentDialogUser().getMCRDRStackResultSet();
            for (int key: stackedResults.getBaseSet().keySet()) {
                MCRDRStackResultInstance aStackFrame = stackedResults.getBaseSet().get(key);
                RuleSet ruleset = aStackFrame.getInferenceResult();
                for (Rule aRule: ruleset.getBase().values()) { 
                  if (aRule.getRuleId() == Integer.parseInt(ruleId)) { 
                      details.addStackFrameId(key); // we're adding a stack frame that contains the requested rule, but there's no direct dialog (rule was satisfied in pass through)
                  }
                }
            }
 
        }
        return details;
    }

    public DialogSet getDialogHistory(String username) {
       int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
       //Logger.info("Getting dialog history for user: " + currentSelectedUserIdIndex + "(" + username + ")");
       DialogSet userDialogHistory;
       synchronized (syncLock) {  
           DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
           userDialogHistory = DialogMain.getDialogUserList().getCurrentDialogRepository();
           //Logger.info("History is:" + userDialogHistory);
       } 
       return userDialogHistory;
    }    

    private String ruleTreeHTMLRenderer(Rule rootRule) {
        String renderedHTML;
        Rule currentRule = rootRule;
        int childRuleCount = rootRule.getChildRuleCount();
        
        renderedHTML = renderRuleHTML(rootRule);
        
        if (childRuleCount !=0)
            renderedHTML += "<div class=nesting>";
        for (int i = 0; i < childRuleCount; i++) {
            Rule childRule = currentRule.getChildAt(i);
            renderedHTML += ruleTreeHTMLRenderer(childRule);
        }
        if (childRuleCount !=0)
            renderedHTML += "</div>";
        
        return renderedHTML;
    }
    
    private String renderRuleHTML(Rule aRule) {
        String details;
        String extraStyles = "";
        String extraText = "";
        
        if (aRule.getIsStoppingRule()) {
            //extraText = "[STOPPING RULE]";
            extraStyles +=  " isStoppingRule";
        }
        if (aRule.getIsStopped() && !aRule.getIsStoppingRule()) {
            //extraText = "[STOPPED]";
            extraStyles +=  " isStopped";
        }
        if (aRule.getDoNotStack()) {
             extraStyles +=  " isNotStacked";
        }
        
   
        ConditionSet conditionSet = aRule.getConditionSet();
        // Get an iterator
        Iterator caseIterator = conditionSet.getBase().iterator();
        int cnt=0;
        String condStr="";
        while (caseIterator.hasNext()) {
            if (cnt!=0){
                condStr += " & ";
            }
            Condition condition = (Condition)caseIterator.next();
            condStr += condition.toString();
            cnt++;
        }

        Conclusion conclusion = aRule.getConclusion();
        String conclusionStr = conclusion.getConclusionName();
        
        String[] conclusionSections = conclusionStr.split("\\^");
        String category = "";
        String action = "";
        
        if (conclusionSections.length == 3) {
            category = " category:" + conclusionSections[1];
            action = " action:" + conclusionSections[2];
        }
        
        if (aRule.getRuleId() == 0)
            condStr = "true (ROOT)";
        String theRule = "[" + aRule.getRuleId() + "] IF " + condStr + " THEN " + conclusionSections[0] + category + action;
        
          
        //details = "<div class=rule id=rule" + aRule.getRuleId() + ">" + aRule.toString().replace("^^","") + "</div>";
        //details = "<div class='rule" + extraStyles + "' id=rule" + aRule.getRuleId() + ">" + extraText + " " + aRule.toString().replace("^^",""). replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")+ "</div>";
        details = "<div class='rule" + extraStyles + "' id=rule" + aRule.getRuleId() + ">" + extraText + " " + theRule.replace("^^",""). replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")+ "</div>";
        return details;
    }
    
    private JSONRuleDetails getRuleDetails(String stringRuleID) {

        int ruleId = Integer.parseInt(stringRuleID);
        Rule aRule = Main.KB.getRuleById(ruleId);
        
        JSONRuleDetails ruleDetails = new JSONRuleDetails();
   
        //get condition set 
        ConditionSet conditionSet = aRule.getConditionSet();
        // Get an iterator
        Iterator caseIterator = conditionSet.getBase().iterator();
        int cnt=0;
        String condStr="";
        while (caseIterator.hasNext()) {
            if (cnt!=0){
                condStr += " & ";
            }
            Condition condition = (Condition)caseIterator.next();
            condStr += condition.toString();
            cnt++;
        }        
       
        String csCaseIdStr = aRule.getCornerstoneCaseSet().toStringOnlyId();
        
        //for (CornerstoneCase aCase : aRule.getCornerstoneCaseSet().getBase().values()) {
            //Logger.info("**************** case:" + aCase.getCaseId() + " value is: " + aCase.toString());

        //}
        
        Conclusion conclusion = aRule.getConclusion();
        String conclusionStr = conclusion.getConclusionName();
        
        ruleDetails.setRuleID(stringRuleID);
        ruleDetails.setRuleCondition(condStr);
        ruleDetails.setRuleCornerstoneCaseID(csCaseIdStr);
        String[] conclusionSections = conclusionStr.split("\\^");
        //ruleDetails.setRuleConclusion(conclusionStr.replace("^^",""));
        ruleDetails.setRuleConclusion(conclusionSections[0]);
        
        if (conclusionSections.length == 3) {
            ruleDetails.setRuleConclusionCategory(conclusionSections[1]);
            ruleDetails.setRuleConclusionAction(conclusionSections[2]);
        }

        
        if (aRule.getDoNotStack())
            ruleDetails.setRuleDoNotStack("True");
        else if (aRule.getIsParentDoNotStack())
            ruleDetails.setRuleDoNotStack("True  (via parent)");
        else
            ruleDetails.setRuleDoNotStack("False");
        
        if (aRule.getIsStopped())
           ruleDetails.setRuleStopped("True");
        else
           ruleDetails.setRuleStopped("False");
        
        return ruleDetails;
    }
    
    private String getMaxStackID(String username) {
        int stackIdInt = 0;
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
       
        synchronized (syncLock) {  
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);
            stackIdInt = DialogMain.getDialogUserList().getCurrentDialogRepository().getMaxSystemDialogStackId();
        } 
           
        return "" + stackIdInt;
    }
    

    /* -------------------- JSON associated functions */
    
    private JSONDialogAndRuleAndStackDetails getDialogAndRulesFromStackFrame(String username, String stackFrame) {
        
        JSONDialogAndRuleAndStackDetails details = new JSONDialogAndRuleAndStackDetails();
        int stackFrameInt;
        RuleSet ruleset;
        
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
        stackFrameInt = Integer.parseInt(stackFrame);
       
        synchronized (syncLock) {  
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);  
            DialogSet systemDialogHistory = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository();

            ruleset = DialogMain.getDialogUserList().getCurrentDialogUser().getMCRDRStackResultSet().getMCRDRStackResultInstanceById(stackFrameInt).getInferenceResult();
            
            
            for (Rule aRule: ruleset.getBase().values()) {
                details.addRuleId(aRule.getRuleId());              
            }
            
            for (IDialogInstance aDialog: systemDialogHistory.getBaseSet().values()) {
                if (aDialog.getStackId() == Integer.parseInt(stackFrame)) {
                    details.addDialogId(aDialog.getDialogId());              
                }
            }
        } 
           
        return details;
    }
    
    private JSONFiredRules getFiredRulesForGUI(String username, String dialogID){
        int dialogIDInt = Integer.parseInt(dialogID);
        //Logger.info("dialogID as int is: " + dialogIDInt);
        
        JSONFiredRules firedRules = new JSONFiredRules();
        int currentSelectedUserIdIndex = DialogMain.getDialogUserList().getIndexFromUsername(username);
        
        synchronized (syncLock) {
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserIdIndex);  
            IDialogInstance selectedDialog = DialogMain.getDialogUserList().getCurrentDialogRepository().getDialoginstanceById(dialogIDInt);

            //int caseId = selectedDialog.getGeneratedCaseId();

           // Logger.info("caseID is: " + caseId);

            //int stackId = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId).getStackId();
            int stackId;
            int satisfiedByStackId;
            int previousStackId;
            
            stackId = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getDialoginstanceById(dialogIDInt).getStackId();
            firedRules.setStackId(stackId);
            
            satisfiedByStackId = DialogMain.getDialogUserList().getCurrentDialogUser().getSatisfiedBystackedMCRDRInferenceResultStackId(stackId);
            firedRules.setSatisfiedByStackId(satisfiedByStackId);

            RuleSet selectedInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
            
            //getSatisfiedBystackedMCRDRInferenceResultStackd

            for (Rule aRule : selectedInferenceResult.getBase().values()) {
                //Logger.info("Rule to add to fired list for reporting:" + aRule.toSimpleString());
                firedRules.addRule(aRule);

            }
            
            //getPreviousStackLevel so we can notify of the starting rules for current inference
            //previousStackId = DialogMain.getDialogUserList().getCurrentDialogUser().getPreviousStackLevel(stackId);
            previousStackId = DialogMain.getDialogUserList().getCurrentDialogUser().getSatisfiedBystackedMCRDRInferenceResultStackId(stackId);

            //Logger.info("###### previouStackId is " + previousStackId);
            
            if (previousStackId > 0) {
  
                RuleSet previousValidInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(previousStackId).getInferenceResult();

                for (Rule aRule : previousValidInferenceResult.getBase().values()) {
                    Logger.info("##### Rule reporting:" + aRule.toSimpleString());
                    firedRules.addStartingRule(aRule);
                }
            }
            else {
                 firedRules.addStartingRule(Main.KB.getRootRule());       
            }
            
        }
        return firedRules;
    }
   
    

    private JSONConclusionList[] getKAConclusionList() {
 
        JSONConclusionList[] theJSONConclusionListArray= new JSONConclusionList[KALogic.getConclusionSet().getBase().size()];
        //JSONConclusionList[] theJSONConclusionListArray= new JSONConclusionList[Main.KB.getConclusionSet().getBase().size()];
        
        JSONConclusionList aConclusionList;
        
        //ConclusionSet conclusionSet = new ConclusionSet();
        //conclusionSet.setConclusionSet(Main.KB.getConclusionSet());
        
        String[] conclusionList = KALogic.getConclusionSet().toStringArrayForGUIWithoutAddConclusion();
        for(int i=0; i<  KALogic.getConclusionSet().getSize(); i++){
            String[] aConclusionLine = conclusionList[i].split("\\^");
            aConclusionList = new JSONConclusionList();
            
            if (aConclusionLine.length == 3) {              
                aConclusionList.setConclusion(aConclusionLine[0]);
                aConclusionList.setActionCategory(aConclusionLine[1]);
                aConclusionList.setAction(aConclusionLine[2]);
            }
            else {
                aConclusionList.setConclusion(aConclusionLine[0]);
                aConclusionList.setActionCategory("");
                aConclusionList.setAction("");
            }
            theJSONConclusionListArray[i] = aConclusionList;
        }
        return theJSONConclusionListArray;
    }


    /* ******************* private classes used purley for JSON transfers *******************  */


    private class JSONConclusionList {
        private String aConclusion;
        private String anActionCategory;
        private String anAction;
        
        public void setConclusion(String conclusion) {
            aConclusion = conclusion;
        }
        
        public void setActionCategory(String category) {
            anActionCategory = category;
        }
        
        public void setAction(String action) {
            anAction = action;
        }
              
    }
    
    private class JSONFiredRules {
        private String ruleList = "";
        private int stackId;
        private String  startingRuleList = "";
        private int satisfiedByStackId;
        
        
        public void addRule(Rule aRule) {
            if (ruleList.isEmpty())
                ruleList = "" + aRule.getRuleId();
            else
                ruleList += "," + aRule.getRuleId();
        }
        
        public void addStartingRule(Rule aRule) {
            if (startingRuleList.isEmpty())
                startingRuleList = "" + aRule.getRuleId();
            else
                startingRuleList += "," + aRule.getRuleId();
        }
        
        public void setStackId(int stackId) {
            this.stackId = stackId;
        }
        
        public void setSatisfiedByStackId(int stackId) {
            this.satisfiedByStackId = stackId;
        }
    }
       
    public class JSONDialogAndRuleAndStackDetails {
        private String dialogList = "";
        private String stackFrameList = "";
        private String ruleList = "";
        
        public void addDialogId(int id) {
            if (dialogList.isEmpty())
                dialogList += id;
            else
                dialogList += "," + id;
        }
        
        public void addStackFrameId(int id) {
            if (stackFrameList.isEmpty())
                stackFrameList += id;
            else
                stackFrameList += "," + id;
        }
        
        public void addRuleId(int id) {
            if (ruleList.isEmpty())
                ruleList += id;
            else
                ruleList += "," + id;
        }
    }
    
    public class JSONRuleDetails {
        private String ruleID;
        private String ruleCondition;
        private String ruleCornerstoneCaseID;
        private String ruleConclusion;
        private String ruleConclusionCategory = "";
        private String ruleConclusionAction = "";     
        private String ruleStopped;
        private String ruleDoNotStack;
        
        public void setRuleID(String ruleID) {
            this.ruleID = ruleID;
        }
        
        public void setRuleCondition(String ruleCondition) {
            this.ruleCondition = ruleCondition;
        }
        
        public void setRuleCornerstoneCaseID(String ruleCornerstoneCaseID) {
            this.ruleCornerstoneCaseID = ruleCornerstoneCaseID;
        }
        
        public void setRuleConclusion(String ruleConclusion) {
            this.ruleConclusion = ruleConclusion;
        }
        
        public void setRuleConclusionCategory(String ruleConclusionCategory) {
            this.ruleConclusionCategory = ruleConclusionCategory;
        }
        
        public void setRuleConclusionAction(String ruleConclusionAction) {
            this.ruleConclusionAction = ruleConclusionAction;
        }
        
        public void setRuleStopped(String ruleStopped) {
            this.ruleStopped = ruleStopped;
        }  
        
        public void setRuleDoNotStack(String ruleDoNotStack) {
            this.ruleDoNotStack = ruleDoNotStack;
        } 
   }
    
//    public class StatusResponse {
//        String result;
//        String status;
//        
//        public void setResult(String theMarkup) {
//            result = theMarkup;
//        }
//        
//        public void setStatus(String theStatus) {
//            status = theStatus;
//        }
//    }
    
//    public class TableResponse {
//        String status;
//        ArrayList<String> header = new ArrayList<>();
//        ArrayList<ArrayList<String>> rows = new ArrayList<>();
//      
//        public void setHeader(String[] theHeader) {
//            for (String aHeaderItem: theHeader) {
//                header.add(aHeaderItem);               
//            }
//        }
//        
//        /*public String[] getHeader() {
//            return header.toArray(new String[header.size()]);
//        }*/
//        
//        public void addRow(String[] aRow) {
//            ArrayList<String> row = new ArrayList<>();
//            if (aRow.length <= header.size()) {
//                for (String aRowItem: aRow) {
//                    //Logger.info("TableResponse: Adding row item:" + aRowItem);
//                    row.add(aRowItem);
//                }
//                rows.add(row);
//            }
//        }
//        
//        public void setStatus(String theStatus) {
//            status = theStatus;
//        }
//    }

} 


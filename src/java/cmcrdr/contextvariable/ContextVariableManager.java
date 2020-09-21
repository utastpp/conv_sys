/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.contextvariable;

import cmcrdr.dic.Dictionary;
import cmcrdr.main.DialogMain;
import cmcrdr.mysql.DBOperation;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import cmcrdr.logger.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import rdr.rules.Rule;

/**
 *
 * @author dherbert
 */
public class ContextVariableManager {

    // This is the list of all context variable definitions.
    private LinkedHashMap<String, ContextVariable> contextVariables = new LinkedHashMap<>();

    /**
     * Get all context variables in the base list
     * @return a linked list of all context variables
     */
    public LinkedHashMap<String, ContextVariable> getContextVariables() {
        return this.contextVariables;
    }
    
    // get a list of all system context variables

    /**
     * Get all context variables in the base list with names starting with "@SYSTEM"
     * @return a linked list of all system context variables
     */
    public LinkedHashMap<String, ContextVariable> getSystemContextVariables() {
         LinkedHashMap<String, ContextVariable> result = new LinkedHashMap<>();
         
         for (String key : this.contextVariables.keySet()) {
             if (key.startsWith("@SYSTEM")) {
                 result.put(key, this.contextVariables.get(key));
             }
         }
         
         return result;
    }

    /**
     * Get a context variable but replace the individual instance with a rule-local modified version if it exists.
     * @param varName the name of the system variable to fetch
     * @param ruleOverride rule number to determine if there's a specific override
     * @return the requested context variable
     */
    public ContextVariable getSystemContextVariableOverride(String varName, int ruleOverride) {
        ContextVariable result = this.contextVariables.get(varName);  // default to the @SYSTEM version of the variable..

        
        if (result != null) {
            String overrideName = "@MOD[" + ruleOverride + "]" + varName;          

            // should be the same valueCriteria as the context formal parameter above..
            //int currentContext = DialogMain.getDialogUserList().getCurrentStackLevel();
            // currentRule should refer to the rule with the same ruleId as the formal parameter ruleOverride
            Rule currentRule = DialogMain.getDialogUserList().getCurrentLastInferencedRule();
            Rule parent = currentRule.getParent();
            int ruleId;

            if (this.contextVariables.containsKey(overrideName)) {
                result = this.contextVariables.get(overrideName);
            }

           else { // we need to see if any of this rule's parents have an override..
                while (parent != null) {
                    ruleId = parent.getRuleId();
                    overrideName = "@MOD[" + ruleId + "]" + varName;
                    //Logger.info(" mod match not found, so looking at parent to see if " + overrideName + " exists..");
                    if (this.contextVariables.containsKey(overrideName)) {
                        result = this.contextVariables.get(overrideName);
                        //Logger.info("Found a parent override - " + overrideName + " with valueCriteria: " + result.getSingleVariableValue());
                        break;
                    }
                    parent = parent.getParent();
                }
            }         
        }

        return result;
    }
    
    /**
     * Determine if there's a rule-specific override for the requested system context variable
     * @param userId the current user id
     * @param varName the name of the system context variable
     * @param ruleOverride the starting rule to determine if there's an override. If not, keep checking parent rules
     * @return the rule id where the override occurs, 0 if no override occurred
     */
    public int getSystemContextVariableOverrideParentId(int userId, String varName, int ruleOverride) {
        int resultRuleId = 0;
        ContextVariable cv = this.contextVariables.get(varName);  // default to the @SYSTEM version of the variable..
        
        if (cv != null) {
            String overrideName = "@MOD[" + ruleOverride + "]" + varName;
 
            Rule currentRule = DialogMain.getDialogUserList().getDialogUser(userId).getLastInferencedRule();
            Rule parent = currentRule.getParent();
            int ruleId;

            if (this.contextVariables.containsKey(overrideName)) {
                resultRuleId = ruleOverride;
            }
            
            else { // we need to see if any of this rule's parents have an override..
                while (parent != null) {
                    ruleId = parent.getRuleId();
                    overrideName = "@MOD[" + ruleId + "]" + varName;
                    if (this.contextVariables.containsKey(overrideName)) {
                        resultRuleId = ruleId;
                        break;
                    }
                    parent = parent.getParent();
                }
            }          
        }
        return resultRuleId;
    }

    /**
     * Set the base list of context variables for this manager instance
     * @param theContextVariables the list of context variables
     */
    public void setContextVariables(LinkedHashMap<String, ContextVariable> theContextVariables) {

        deleteAllContextVariables();
        // we need to iterate over all context variables in order to save each item to database
        for (Map.Entry me : theContextVariables.entrySet()) {
            String varName = (String) me.getKey();
            ContextVariable var = (ContextVariable) me.getValue();
            //Logger.info("Found context variable:" + varName);
            //Logger.info("Override value is:" + var.getVariableValueOverride());
            if (!var.getValuesBase().isEmpty()) {
                //Logger.info("has value based so added..");
                this.addContextVariable(var);
            }
        }
    }

    /**
     * Load context variables from database
     */
    public void setCurrentContextVariablesFromDB() {

        //LinkedHashMap<Integer, String> variablesHashMap = DBOperation.getContextVariableList();
        LinkedHashMap<Integer, ContextVariable> variablesHashMap = DBOperation.getContextVariableList();
        LinkedHashMap<Integer, ContextVariable> variableValuesList = DBOperation.getContextVariableValuesList();
        LinkedHashMap<Integer, ContextVariable> variableActionsList = DBOperation.getContextVariableActionsList();

        Set variableSet = variablesHashMap.entrySet();

        Iterator variableIterator = variableSet.iterator();

        while (variableIterator.hasNext()) {
            
            Map.Entry me = (Map.Entry) variableIterator.next();

            int variableId = (int) me.getKey();
            ContextVariable cv = (ContextVariable) me.getValue();
            String variableName = cv.getVariableName();
            String variableOverride = cv.getVariableValueOverride();
            //String variableName = (String) me.getValue();
            //Logger.info("Loading context variable:" + variableName + " with override:" + variableOverride);

            ContextVariable aVariable = variableValuesList.get(variableId);
            ContextVariable actionVariable = variableActionsList.get(variableId);
            if (aVariable != null) {
                aVariable.setVariableName(variableName);
                aVariable.setVariableId(variableId);
                aVariable.setVariableValueOverride(variableOverride);
                if (actionVariable != null) {
                    aVariable.setVariableActions(actionVariable.getVariableActions());
                }
                //Logger.info("Variable had values so added to main list..");
                this.addToContextVariableList(aVariable);
            } else {
                DBOperation.deleteContextVariable(variableName);
            }
        }
    }

    /**
     * Delete all current context variables
     */
    public void deleteAllContextVariables() {
        DBOperation.deleteAllContextVariableActions();
        DBOperation.deleteAllContextVariableValues();
        DBOperation.deleteAllContextVariables();
        this.contextVariables.clear();
    }

    /**
     * Add a context variable to the list, saving into database also in order to determine next sequence number (id) from db
     * @param aVariable the variable to add the list
     * @return the context variable added (with updated id)
     */
    public ContextVariable addContextVariable(ContextVariable aVariable) {
        int varId = DBOperation.insertContextVariable(aVariable.getVariableName(), aVariable.getVariableValueOverride());

        aVariable.setVariableId(varId);

        Set values = aVariable.getValuesBase().entrySet();
        Set actions = aVariable.getActionsBase().entrySet();
        // Get an iterator
        Iterator valueIterator = values.iterator();
        while (valueIterator.hasNext()) {
            Map.Entry me = (Map.Entry) valueIterator.next();
            String value = (String) me.getValue();

            DBOperation.insertContextVariableValue(varId, value);
        }

        Iterator actionIterator = actions.iterator();
        while (actionIterator.hasNext()) {
            Map.Entry me = (Map.Entry) actionIterator.next();
            ContextVariableAction anAction = (ContextVariableAction) me.getValue();

            DBOperation.insertContextVariableAction(varId, anAction.getTarget(), anAction.getFixed(), anAction.getContext(), anAction.getTrigger());
        }

        this.addToContextVariableList(aVariable);

        return aVariable;
    }

    /**
     * Add a new default context variable to the list
     * @param varName the name of the context variable to create
     * @return the created context variable
     */
    public ContextVariable addContextVariableUsingString(String varName) {
        ContextVariable aVariable = new ContextVariable();
        aVariable.setVariableName(varName);

        return this.addContextVariable(aVariable);
    }

    

    /**
     * Delete specified context variable
     * @param varName the name of the variable to delete
     */

    public void deleteContextVariable(String varName) {
        ContextVariable aVariable = this.contextVariables.get(varName);
        if (aVariable != null) {
            int varId = aVariable.getVariableId();

            DBOperation.deleteContextVariable(varName);

            DBOperation.deleteContextVariableValueByVarId(varId);

            DBOperation.deleteContextVariableActionByVarId(varId);

            this.contextVariables.remove(varName);
        }
    }

    /**
     * Add a context variable to the base list
     * @param aVariable the variable to add
     * @return true if addition successful, false if variable already exists in the list
     */
    public boolean addToContextVariableList(ContextVariable aVariable) {
        //Logger.info("Trying to add context variable to local list: " + aVariable.getVariableName());
        boolean result = false;

        if (!this.contextVariables.containsKey(aVariable.getVariableName())) {
            this.contextVariables.put(aVariable.getVariableName(), aVariable);
            //Logger.info("Added context variable to local list: " + aVariable.getVariableName());

            result = true;
        }

        return result;
    }
    
    public boolean variableExists(ContextVariable aVariable) {
        return this.contextVariables.containsKey(aVariable.getVariableName());
    }
    
    
    public ArrayList<String> stringCrossProduct(ArrayList<String> list1, ArrayList<String> list2) {
        ArrayList<String> newList = new ArrayList<>();
        
        // "a","b" x "c","d"  gives "ac"  "ac" "bc" "bd"
        
        for (String element1: list1) {
            for (String element2: list2) {
               newList.add(element1 + " " + element2);
            }
        }
        
        return newList;
    }
    
    public ArrayList<ArrayList<String>> listCrossProduct(ArrayList<ArrayList<String>> list1, ArrayList<String> list2) {
        ArrayList<ArrayList<String>> resultList = new ArrayList<>();
        
        for (ArrayList<String> element1List: list1) {
            for (String element2: list2) {
                ArrayList<String> tempList = new ArrayList(element1List);
                tempList.add(element2);
                resultList.add(tempList);
            }
        }
        
        return resultList;
        
    }

    /**
     * Determine if recentInput contains a synonym match for a dictionary term, or a static valueCriteria, that has been 
 associated with a context variable
     * @param varName the context variable we are scanning the input for
     * @param recentInput the recent input possibly containing valueCriteria match for a context variable
     * @return the valueCriteria of the requested context variable found in the recent input
     */
    
    
    //public ContextVariableTokenisedResults getVariableMatchValueFromInput(String varName, String recentInput, int startIndex) {
    public ContextVariableTokenisedResults getVariableMatchValueFromInput(String varName, String recentInput) {
    //public String[] getVariableMatchValueFromInput(String varName, String recentInput, int startIndex) {
        Dictionary dic = DialogMain.dictionary;
        int[] matchingCriteriaLocation;
        //String[] returnValue  = {"","0"};
        ContextVariableTokenisedResults returnValue  = new ContextVariableTokenisedResults(null,null,"","");
        String processingUserInput = recentInput;
        
        //Logger.info("startIndex is:" + startIndex);

        ContextVariable cv = this.contextVariables.get(varName);

        if (cv != null && !cv.getVariableName().startsWith("@SYSTEM") && !cv.getVariableName().startsWith("@MOD")) {

            for (Map.Entry me : cv.getValuesBase().entrySet()) {
                int id = (int) me.getKey();
                String valueCriteria = (String) me.getValue();
                //Logger.info("criteria matching value is: " + valueCriteria);
                
                ArrayList<String> valueCriteriaTokens = getTokenisedTerms(valueCriteria); // split by spaces..
                ArrayList<String> results = new ArrayList<>();
                
                for (String aValueCriteriaToken: valueCriteriaTokens) {
                    String temp = "";
                    //Logger.info("Looking at criteria token: " + aValueCriteriaToken);

                    if (dic.isDicTermExist(aValueCriteriaToken)) { // the token is a dictionary term!

                        boolean moreMatchesToFind = true;
                        //ArrayList<String> synonymMatches = new ArrayList<>();
                        temp = "";
                        
                        while (moreMatchesToFind) {
                            //Logger.info(aValueCriteriaToken + " is a dictionary term");
                            // we have a matching dictionary representative term..
                            //matchingCriteriaLocation = DialogMain.dicConverter.getValidMatchingTermLocation(aValueCriteriaToken, recentInput,index);
                            matchingCriteriaLocation = DialogMain.dicConverter.getValidMatchingTermLocation(aValueCriteriaToken, processingUserInput);
                            if (matchingCriteriaLocation[0] != -1) { // we have found a matching term in our input string!
                                //Logger.info("input string match:[" + matchingCriteriaLocation[0] + " to " + matchingCriteriaLocation[1] + "]");
                                String currentSubString = processingUserInput.substring(matchingCriteriaLocation[0], matchingCriteriaLocation[1]);
                                if (temp.isEmpty())
                                    temp = currentSubString;
                                else
                                    temp += ";" + currentSubString;
                                
                                
                                //Logger.info("Adding: '" + temp + "' to the synonym matches results");
                                
                                // progressively replace matched terms with #'s so we don't match them again this round
                                char[] charArray = new char[currentSubString.length()];
                                Arrays.fill(charArray, '#');
                                String replaceString = new String(charArray);

                                processingUserInput = processingUserInput.substring(0,matchingCriteriaLocation[0]) + replaceString + processingUserInput.substring(matchingCriteriaLocation[1]);
                                //Logger.info("New processingUserInput after replacing synonym match is:" + processingUserInput);
                                //index = matchingCriteriaLocation[1];
                            }
                            else {
                                //Logger.info("No more matches for token:" + aValueCriteriaToken + " found");
                                moreMatchesToFind = false;
                            }
                        }
                        if (!temp.isEmpty()) {
                            results.add(temp);
                        }
                    } 
                    else {
                        //Logger.info(aValueCriteriaToken + " is NOT a dictionary term");
                        //matchingCriteriaLocation = DialogMain.dicConverter.getValidMatchingPhraseLocation(aValueCriteriaToken, recentInput,index);
                        matchingCriteriaLocation = DialogMain.dicConverter.getValidMatchingPhraseLocation(aValueCriteriaToken, recentInput);
                        if (matchingCriteriaLocation[0] != -1) { // we have found a matching term in our input string!
                            temp = processingUserInput.substring(matchingCriteriaLocation[0], matchingCriteriaLocation[1]); 
                            results.add(temp);
                            //Logger.info("input string match:[" + matchingCriteriaLocation[0] + " to " + matchingCriteriaLocation[1] + "]");                           
                            //Logger.info("Adding: " + temp + " to the results");
                            
                            // progressively replace matched tokens with #'s so we don't match them again this round
                            char[] charArray = new char[temp.length()];
                            Arrays.fill(charArray, '#');
                            String replaceString = new String(charArray);
                            
                            processingUserInput = processingUserInput.substring(0,matchingCriteriaLocation[0]) + replaceString + processingUserInput.substring(matchingCriteriaLocation[1]);
                            //Logger.info("New processingUserInput after replaceing synonym match is:" + processingUserInput);
                        }
                    }              
                }
                

                // let's work out how many potential matching strings we have, one of them should map to the input string
                // DAVE THIS IS TRICKY.. 
                /*int count = 1;
                int tokenCount = 0;
                for (String aResult: results) {
                    tokenCount = 0;
                    if (aResult.contains(";"))
                        tokenCount = aResult.length() - aResult.replace(";","").length() + 1;
                    else 
                        tokenCount = 1;
                    
                    count = count * tokenCount;
                }*/
                
                // build up the final list of valueCriteriaTokens, replacing criteria that matched multiple values 
                // (separated with ";") with separate lists each containing one of the matched values
                ArrayList<ArrayList<String>> tokenResultsList = new ArrayList<>();

                for (String aResult: results) {
                    ArrayList<String> tempList = new ArrayList<>();
                    
                    if (aResult.contains(";")) {
                        tempList.addAll(Arrays.asList(aResult.split(";")));
                        Logger.info("Found split data for tempList:");
                        for (String anItem: tempList) {
                           Logger.info("\tSplit data item is:" + anItem);
                        }
                    }           
                    else {
                        tempList.add(aResult);
                        Logger.info("Found a non-split item for tempList:" + aResult);
                    }
                    
                    tokenResultsList.add(tempList);
                }
                
                // generate all possible strings comprising of matched synonyms in our context variable critera
                // start with the first list..  this is used we can find a final match to the user input later
                ArrayList<String> sentenceList = new ArrayList<>();
                if (!tokenResultsList.isEmpty()) {
                    sentenceList = tokenResultsList.get(0);
                }
                int i = 0;
                
                for (ArrayList<String> synonymList : tokenResultsList) {
                    if (i !=0) {
                        sentenceList = stringCrossProduct(sentenceList,synonymList);
                    }
                    else {
                        i++;
                    }                       
                }
                
                // debugging..
                if (!sentenceList.isEmpty())
                    Logger.info("GENERATED SENTENCES:");
                for (String debugSentence : sentenceList) {                 
                    Logger.info("\t" + debugSentence);
                }
                
                // generate all possible lists comprising of matched synonyms in our context variable critera
                // start with the first list..  this is similar to the sentence list, but instead of a list of individual
                // sentence strings, we have a list (of lists) of the original tokens that produced the corresponding sentence
                // This is used later so we can override a token with its matched value
                // In essence we have a list of result lists, so we can pick the right one that produced the matching sentence
                ArrayList<ArrayList<String>> resultsList = new ArrayList<>();
                if (!tokenResultsList.isEmpty()) {
                    ArrayList<String> list1 =  tokenResultsList.get(0);
                    {
                        // Create new separate lists for each token in the first element of tokenResultsList
                        for (String element: list1) {
                            ArrayList<String> newList = new ArrayList<>();
                            newList.add(element);
                            resultsList.add(newList);
                        }                        
                    }
                }
                int j = 0;
                
                // Now successively build up our list of lists 
                for (ArrayList<String> synonymList : tokenResultsList) {
                    if (j !=0) {
                        resultsList = listCrossProduct(resultsList,synonymList);
                    }
                    else {
                        j++;
                    }                       
                }
                
                
                // NOTE - the number of lists in resultsList should equal the number of sentences generated (sentenceList.size)
                int sentenceIndex = 0;
                for (String resultString : sentenceList) {
                    Logger.info("The list of resultString are:" + resultString);
                    Logger.info("Sentence index is: " + sentenceIndex);
                    
                    ArrayList<String> currentResultList = new ArrayList<>();
                    if (!resultsList.isEmpty()) {
                        currentResultList = resultsList.get(sentenceIndex);
                        
                        Logger.info("Current result tokens corresponding to sentence index are:");
                        for (String resultTokens: currentResultList) {
                            Logger.info("\t" + resultTokens);
                        }
                    }


                    //if (results.size() == valueCriteriaTokens.size() && !resultString.isEmpty() && recentInput.replaceAll(" +"," ").contains(resultString)) {
                    if (currentResultList.size() == valueCriteriaTokens.size() && !resultString.isEmpty() && recentInput.replaceAll(" +"," ").contains(resultString)) {
                        Logger.info("User input match found for "+ valueCriteria + ": " + resultString);
                        //returnValue.setResultTokens(results);
                        returnValue.setResultTokens(resultsList.get(sentenceIndex));
                        returnValue.setSourceTokens(valueCriteriaTokens);
                        returnValue.setProcessedInputString(processingUserInput);
                        returnValue.setResultString(resultString);
                        return returnValue;
                    }
                    else
                        sentenceIndex++;
                }
                
            }
        }

        return returnValue;
    }
    
    public ArrayList<String> getTokenisedTerms(String value) {
        ArrayList<String> result;
        result = new ArrayList(Arrays.asList(value.split(" ")));
        return result;
    } 

    /**
     * Get a list of all context variable names as a string array list
     * @return an array list of all context variable names
     */
    public ArrayList<String> getContextVariablesStringArrayList() {
        ArrayList<String> theList = new ArrayList<>();

        for (Map.Entry me : this.contextVariables.entrySet()) {
            String varName = (String) me.getKey();
            // if (varName.startsWith("@SYSTEM") || varName.startsWith("@MOD"))
            if (varName.startsWith("@SYSTEM") || (!varName.startsWith("@SYSTEM") && !varName.startsWith("@MOD"))) {
                theList.add(varName);
            }
        }

        /*
        // only add context variables at the current context (or its parents)
        //for (Map.Entry me : DialogMain.getDialogUserList().getCurrentContextVariables().entrySet()) {
        //Logger.info("DATABASE BUILDER - CURRENT KA CONTEXT is : " + DialogMain.getDialogUserList().getCurrentKAInferenceResult().getLastRule().getRuleId());
        for (Map.Entry me : DialogMain.getDialogUserList().getCurrentKAContextVariables().entrySet()) {
            String varName = (String) me.getKey();
            theList.add(varName);
        }
        */
        return theList;
    }

    //public void setContextVariablesForSpecificContext(LinkedHashMap<String, ContextVariableUser> contextVariablesFoundinRecentInput, RuleSet applyingInferenceResult) {

    /**
     * Set one or more context variables associated with a specific context (a stacked inference result)
     * @param contextVariablesFoundinRecentInput all context variables that have values matched in the most recent user dialog string
     * @param context the key used for stacked inference results
     */
    public void setContextVariablesForSpecificContext(LinkedHashMap<String, ContextVariableUser> contextVariablesFoundinRecentInput, int context) {
        LinkedHashMap<String, ContextVariableUser> currentUserContextVariablesForThisContext;
        LinkedHashMap<String, ContextVariableUser> contextVariablesForCurrentContext = new LinkedHashMap<>();
        //Rule inferencedRule;
        //RuleSet currentContextRuleSet = null;
        //int currentContextLocaton;

        //inferencedRule = applyingInferenceResult.getLastRule();
        //currentContextRuleSet = inferencedRule.getPathRuleSet();
        //currentContextLocaton = currentContextRuleSet.getFirstRule().getRuleId();

        //currentUserContextVariablesForThisContext = DialogMain.getDialogUserList().getCurrentContextVariablesList().get(currentContextLocaton);
        currentUserContextVariablesForThisContext = DialogMain.getDialogUserList().getCurrentContextVariablesList().get(context);
        if (currentUserContextVariablesForThisContext != null) { // need to merge what we have now with what was there before..
            //Logger.info("Some context variables exist for this context (" + context + ")");
            // first, copy all the existing values
            for (Map.Entry sourceUserContextVariablesEntry : currentUserContextVariablesForThisContext.entrySet()) {
                contextVariablesForCurrentContext.put((String) sourceUserContextVariablesEntry.getKey(), (ContextVariableUser) sourceUserContextVariablesEntry.getValue());
                //Logger.info("Copying: " + (String) sourceUserContextVariablesEntry.getKey());

            }
            // now replace the redefined ones..
            for (Map.Entry testUserContextVariablesEntry : contextVariablesFoundinRecentInput.entrySet()) {

                if (contextVariablesForCurrentContext.containsKey((String) testUserContextVariablesEntry.getKey())) {
                    contextVariablesForCurrentContext.replace((String) testUserContextVariablesEntry.getKey(), (ContextVariableUser) testUserContextVariablesEntry.getValue());
                    //Logger.info("Variable " + (String) testUserContextVariablesEntry.getKey() + " is being overwritten locally");
                } else {
                    contextVariablesForCurrentContext.put((String) testUserContextVariablesEntry.getKey(), (ContextVariableUser) testUserContextVariablesEntry.getValue());
                    //Logger.info("Variable " + (String) testUserContextVariablesEntry.getKey() + " not seen before, so adding");
                }

            }
            //Logger.info("a variable set existed for this context (" + context + ") before, so replacing source");
            DialogMain.getDialogUserList().getCurrentContextVariablesList().replace(context, contextVariablesForCurrentContext);
        } else {
            for (Map.Entry testUserContextVariablesEntry : contextVariablesFoundinRecentInput.entrySet()) {

                if (contextVariablesForCurrentContext.containsKey((String) testUserContextVariablesEntry.getKey())) {
                    contextVariablesForCurrentContext.replace((String) testUserContextVariablesEntry.getKey(), (ContextVariableUser) testUserContextVariablesEntry.getValue());
                    //Logger.info("Variable " + (String) testUserContextVariablesEntry.getKey() + " is being overwritten locally");
                } else {
                    contextVariablesForCurrentContext.put((String) testUserContextVariablesEntry.getKey(), (ContextVariableUser) testUserContextVariablesEntry.getValue());
                    //Logger.info("Variable " + (String) testUserContextVariablesEntry.getKey() + " not seen before, so adding");
                }
            }

            DialogMain.getDialogUserList().getCurrentContextVariablesList().put(context, contextVariablesForCurrentContext);
            //Logger.info("a variable set did not exist for this context (" + context + ") before, so adding");
        }


    }
    

    /**
     * Scan user recent dialog input and determine if it contains any valueCriteria matches for any defined context variable.
     * @param userInput the recent user dialog input
     * @return true if any context variables were found in the input string
     */
    public boolean registerUserContextVariables(String userInput) {
        Dictionary dic = DialogMain.dictionary;
        String processingUserInput = userInput;
        
        LinkedHashMap<String, ContextVariableUser> contextVariablesFoundinRecentInput = new LinkedHashMap<>();

        int userContext = DialogMain.getDialogUserList().getCurrentStackLevel();

        boolean foundAVariable = false;
        // look through all pre-defined context variables for matches with user input..
        for (Map.Entry me : this.contextVariables.entrySet()) {
            String contextVariableName = (String) me.getKey();
            //int startIndex = 0;
            String matchedValue = "";
            ArrayList<String> resultTokens = new ArrayList<>();
            ArrayList<String> sourceTokens = new ArrayList<>();
            
            //Logger.info("Processing variable: " + contextVariableName);
            //String[] matchedValueData = getVariableMatchValueFromInput(contextVariableName, userInput,startIndex);
            //ContextVariableTokenisedResults matchedValueData = getVariableMatchValueFromInput(contextVariableName, userInput,startIndex);
            ContextVariableTokenisedResults matchedValueData = getVariableMatchValueFromInput(contextVariableName, userInput);
           
            
            
            //while (!matchedValueData[0].equals("")) {
            while (matchedValueData.getResultTokens() != null) {
                foundAVariable = true;
                String temp = "";
                for (String aResult: matchedValueData.getResultTokens()) {
                    if (temp.isEmpty())
                        temp += aResult;
                    else
                        temp += " " + aResult;
                    }
                
                
                if (matchedValue.isEmpty()) {                
                    matchedValue += temp;
                }
                else
                    matchedValue += "," + temp;
                
                //startIndex = Integer.parseInt(matchedValueData[1]);
               // startIndex = matchedValueData.getLastIndex();
                //Logger.info("Found data:" + matchedValue);
                //Logger.info("But new fixed returned data should be:" + matchedValueData.getResultString());
                matchedValue = matchedValueData.getResultString();
                
                resultTokens.addAll(matchedValueData.getResultTokens());
                sourceTokens.addAll(matchedValueData.getSourceTokens());
                //matchedValueData = getVariableMatchValueFromInput(contextVariableName, userInput,startIndex);
                processingUserInput = matchedValueData.getProcessedInputString();
                matchedValueData = getVariableMatchValueFromInput(contextVariableName, processingUserInput);
            }
            
            if (!matchedValue.isEmpty()) {
                Logger.info("#### Found a context variable in the input - adding:" + contextVariableName + " with value: " + matchedValue + " to list for processing");
                ContextVariable cv = this.contextVariables.get(contextVariableName);
                if (!cv.getVariableValueOverride().equals("")) {
                    Logger.info("The variable " + contextVariableName + " has an override:" + cv.getVariableValueOverride());
                    
                    String overrideResultString = "";
                    
                    // tokenise our override string in order to look for dictionary terms..
                    ArrayList<String> overrideTokens = getTokenisedTerms(cv.getVariableValueOverride());
                    for (String anOverrideToken: overrideTokens) {
                        int i = 0;
                        boolean dicTermFound = false;
                        String tempResultString = "";
                        
                        for (String aCriteriaToken: sourceTokens) {
                            if (dic.isDicTermExist(aCriteriaToken) && aCriteriaToken.equals(anOverrideToken)) {
                                // our override token is actually a dictionary term, so we find the resulting term matching from our results
                                dicTermFound = true;
                                tempResultString = resultTokens.get(i);   
                                break;
                            }
                            i++;
                        }
                        // if we didn't find an overriding dictionary term, use the literal token we already have
                        if (!dicTermFound) {
                            tempResultString = anOverrideToken;
                        }
                        
                        if (overrideResultString.equals(""))
                            overrideResultString += tempResultString;
                        else
                            overrideResultString += " " + tempResultString;
                        
                    }
                    ContextVariableUser aNewUserVariable = new ContextVariableUser(contextVariableName, overrideResultString, userContext);
                    Logger.info("Setting " + contextVariableName + " variableMatchedValue to " + matchedValue);
                    aNewUserVariable.setVariableMatchedValue(matchedValue); // need to retain this for possible action conditions..
                    aNewUserVariable.arm();
                    contextVariablesFoundinRecentInput.put(contextVariableName, aNewUserVariable);
                }
                else {
                    ContextVariableUser aNewUserVariable =  new ContextVariableUser(contextVariableName, matchedValue, userContext);
                    Logger.info("Setting " + contextVariableName + " variableMatchedValue to " + matchedValue);
                    aNewUserVariable.setVariableMatchedValue(matchedValue); // need to retain this for possible action conditions..
                    aNewUserVariable.arm();
                    contextVariablesFoundinRecentInput.put(contextVariableName, aNewUserVariable);
                }
                
            }
        }

        // need for KA acquisition to register variables if a different context is chosen based on the conclusion of the current dialog
        DialogMain.getDialogUserList().setCurrentRecentContextVariables(contextVariablesFoundinRecentInput);

        if (contextVariablesFoundinRecentInput.size() > 0) {
            int context = DialogMain.getDialogUserList().getCurrentStackLevel();
            
           

            //setContextVariablesForSpecificContext(contextVariablesFoundinRecentInput, applyingInferenceResult);
            setContextVariablesForSpecificContext(contextVariablesFoundinRecentInput, context);
            // debug let's see what we got as a test..  should remove this later..
            DialogMain.getDialogUserList().getCurrentContextVariables();
        }

        return foundAVariable;
    }

    /**
     * Determine if there are any actions associated with context variables that are triggered.
     * The trigger can either be forced (trigger is true), or true if the valueCriteria of the current context variable 
 contains the valueCriteria of another variable. If the trigger is true, the target variable's valueCriteria is set
 (which can either be a literal valueCriteria, or the valueCriteria of another source context variable).
 Once triggered, the action is only executed once.
     */
    public void executeUserContextVariableActions() {
        // ok, now see if any of the user context variables had an associated action - if so, execute the action and then delete the action-instigating variable from the user's list..
        LinkedHashMap<String, ContextVariableUser> userVariables = DialogMain.getDialogUserList().getCurrentContextVariables();
        LinkedHashMap<String, ContextVariableUser> newUserVariables = new LinkedHashMap<>(); // temp list as we can't modify userVariables while iterating over it.
        ArrayList<String> variablesToDelete = new ArrayList<>();

       int currentUserLastInferenceRuleId = DialogMain.getDialogUserList().getCurrentLastInferencedRuleId();
       int processId = DialogMain.getDialogUserList().getCurrentStackLevel();
       //Logger.info("Current last inference Rule ID is:" + currentUserLastInferenceRuleId);
       //Logger.info("Current stack context:" + processId);
        // now, add any new user context variables to the original user context variable list..

        for (Map.Entry me : userVariables.entrySet()) {
            String varName = (String) me.getKey();
            ContextVariableUser varValue = (ContextVariableUser) me.getValue();
            Logger.info("User has a defined variable called " + varName);
            ContextVariable globalContextVariable = DialogMain.globalContextVariableManager.getContextVariables().get(varName);
            //if (globalContextVariable == null) {
                //Logger.info("The defined variable value fetch failed!");
            //}
            LinkedHashMap<Integer, ContextVariableAction> theActions = null;
            if (globalContextVariable != null)
                theActions = globalContextVariable.getVariableActions();
            else    
                theActions = null;
            
            //if (theActions != null) {
            if (theActions != null && varValue.isArmed()) {
                Iterator actionIterator = theActions.entrySet().iterator();
                while (actionIterator.hasNext()) {
                    Logger.info("Found an action associated with the user variable " + varName);
                    Map.Entry actionEntry = (Map.Entry) actionIterator.next();
                    ContextVariableAction anAction = (ContextVariableAction) actionEntry.getValue();
                    String theTarget = anAction.getTarget();
                    String theTriggerSource = anAction.getTrigger();
                    boolean actionIsTriggered = false;
                    String theValue;
                    
                    Logger.info("The action target is " + theTarget + " and the trigger is " + theTriggerSource);

                    if (!theTriggerSource.equals("TRUE")) {  // we need to fetch the defined variable and see if our action variable string matches.
                        //String triggerSourceValue = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(theTriggerSource, currentUserLastInferenceRuleId).getSingleVariableValue().toLowerCase();
                        String triggerSourceValue = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(theTriggerSource, processId).getSingleVariableValue().toLowerCase();
                        if (triggerSourceValue != null && (varValue.getValue().toLowerCase().contains(triggerSourceValue) || varValue.getVariableMatchedValue().toLowerCase().contains(triggerSourceValue))) {
                            Logger.info("ACTION WILL BE TRIGGERED AS '" + varValue.getValue() + "' or '" + varValue.getVariableMatchedValue() + "' contains " + triggerSourceValue);
                            actionIsTriggered = true;
                        }
                        else {
                            actionIsTriggered = false;
                            Logger.info("ACTION WILL NOT BE TRIGGERED AS '" + varValue.getValue() + "' or '" + varValue.getVariableMatchedValue() + "' does not contain " + triggerSourceValue);                         
                        }                  
                    }
                    else {
                        actionIsTriggered = true;
                        //Logger.info("ACTION WILL BE TRIGGERED AS TRIGGER SET TO ALWAYS EXECUTE");

                    }

                    if (actionIsTriggered) {
                        if (anAction.contextUsed()) {// the source for the target value is a context variable..
                            Logger.info("The ACTION uses a context variable for the target value:" + anAction.getValue());
                            // if the action source is a system variable, we need to get the overriden version (if it exists)
                            if (anAction.getValue().startsWith("@SYSTEM")) {
                                //Logger.info("ACTION VARIABLE ACTION IS A SYSTEM VARIABLE - " + anAction.getValue());
                                //theValue = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(anAction.getValue(), currentUserLastInferenceRuleId).getSingleVariableValue();
                                theValue = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(anAction.getValue(), processId).getSingleVariableValue();
                            } else {
                                // this should be the value of the user's context variable, not the criteria used in the global definition..? DPH 1 August 2019
                                //theValue = DialogMain.globalContextVariableManager.getContextVariables().get(anAction.getValue()).getSingleVariableValue();
                                ContextVariableUser targetContextVariableValue =  userVariables.get(anAction.getValue());
                                if (targetContextVariableValue != null) {
                                    theValue = userVariables.get(anAction.getValue()).getValue();
                                    Logger.info("The source context variable used for the target value had a value of: " + theValue);

                                }
                                else {
                                    Logger.info("The source context variable used for the target value did not exist..");
                                    theValue = "";
                                }
                            }
                            //Logger.info("The action is to set " + theTarget + " with " + anAction.getValue() + "'s value, which is " + theValue);

                        } else {
                            theValue = anAction.getValue();  // this is a literal value..
                            //Logger.info("The action is to set " + theTarget + " with value " + theValue);

                        }
                        //ContextVariableUser newUserVariable = new ContextVariableUser(theTarget, theValue, currentUserLastInferenceRuleId);
                        ContextVariableUser newUserVariable = new ContextVariableUser(theTarget, theValue, processId);
                        
                        newUserVariables.put(theTarget, newUserVariable);
                        
                        
                    }
                    //Logger.info(varName + " is set to be deleted.. but I'm not going to do that for now.. DPH August 2019!");
                    //variablesToDelete.add(varName);
                }
                //Logger.info("Disarming " + varValue.getVariableName());
                varValue.disarm();  // stop the action firing again until the defining variable's value changes..
            }
            
        }

        
        Iterator iter = newUserVariables.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            ContextVariableUser aVariable = (ContextVariableUser) me.getValue();
            ContextVariable masterVersion;

            String theKey = (String) me.getKey();
            String theTarget = aVariable.getVariableName(); // should be the same as the key..
            if (userVariables.containsKey(theKey)) {

                userVariables.replace(theKey, aVariable);
                //Logger.info("Replacing original variable " + theKey + " with variable " + aVariable.getVariableName() + " which has value " + aVariable.getValue());
                //Logger.info("VERIFY: replaced:" + userVariables.get(theKey).getVariableName() + " with value:" + userVariables.get(theKey).getValue() );
            } else {
                userVariables.put(theKey, aVariable);
                if (!DialogMain.globalContextVariableManager.getContextVariables().containsKey(theKey)) {
                    // need to make a master copy!
                    masterVersion = new ContextVariable();
                    masterVersion.setVariableName(theKey);
                    masterVersion.addVariableValue(aVariable.getValue());
                    DialogMain.globalContextVariableManager.addContextVariable(masterVersion);
                }
                //Logger.info("Adding new variable " + theKey + " with variable " + aVariable.getVariableName() + " which has value " + aVariable.getValue());

            }
        }

        // and finally, delete any variables that were detected to have an action as we only want to do the action once!
        for (String var : variablesToDelete) {
            userVariables.remove(var);
            //Logger.info(var + " has been deleted!");
        }
        // finally, replace the resultant variables in our current context level list as all we have at the moment is a copy, not the original..
        //if (DialogMain.getDialogUserList().getCurrentContextVariablesList().containsKey(currentUserLastInferenceRuleId))
        if (DialogMain.getDialogUserList().getCurrentContextVariablesList().containsKey(processId))
            //DialogMain.getDialogUserList().getCurrentContextVariablesList().replace(currentUserLastInferenceRuleId, userVariables);
            DialogMain.getDialogUserList().getCurrentContextVariablesList().replace(processId, userVariables);
        else
            //DialogMain.getDialogUserList().getCurrentContextVariablesList().put(currentUserLastInferenceRuleId, userVariables);
            DialogMain.getDialogUserList().getCurrentContextVariablesList().put(processId, userVariables);

        //for (Map.Entry me : userVariables.entrySet()) {
            //Logger.info("DAVE User Variable : " + (String) me.getKey() + " has value: " + ((ContextVariableUser) me.getValue()).getValue());
        //}
        
        // let's see what's now in our list
        //for  (Map.Entry me : DialogMain.getDialogUserList().getCurrentContextVariablesList().get(currentUserLastInferenceRuleId).entrySet()) {
        //for  (Map.Entry me : DialogMain.getDialogUserList().getCurrentContextVariablesList().get(processId).entrySet()) {
            //Logger.info("DAVE2 global User Variable list now : CONTEXT:" + currentUserLastInferenceRuleId + " VARIABLE:" + (String) me.getKey() + " VALUE:" + ((ContextVariableUser)me.getValue()).getValue());
            //Logger.info("DAVE2 global User Variable list now : CONTEXT:" + processId + " VARIABLE:" + (String) me.getKey() + " VALUE:" + ((ContextVariableUser)me.getValue()).getValue());
        //}
        

        
        /*
        // sanity test
        //Logger.info("SANITY CHECK ****************************************  After actions results, user variables contains");
        Logger.info("My current context is: " + currentUserContextLevel);
        for (Map.Entry me : userVariables.entrySet()) {
            Logger.info("   Variable : " + (String) me.getKey() + " has valueCriteria: " + ((ContextVariableUser) me.getValue()).getValue());
        }
        DialogMain.getDialogUserList().getCurrentContextVariablesList().replace(currentUserContextLevel, userVariables);
        Logger.info("Just to verify the change has been made, original source values:");
        for (Map.Entry me : DialogMain.getDialogUserList().getCurrentContextVariables().entrySet()) {
            Logger.info("   XXXX   Variable : " + (String) me.getKey() + " has valueCriteria: " + ((ContextVariableUser) me.getValue()).getValue());
        }
        */
    }
    
    public String getContextVariablesForAttributeString() {
        LinkedHashMap<String,ContextVariableUser> theList = DialogMain.getDialogUserList().getCurrentContextVariables();
        String theAttributeString = "";
        if (theList == null)
            return "";
        else {
            for (Map.Entry<String,ContextVariableUser> entry : theList.entrySet()) {
                String key = entry.getKey();
                ContextVariableUser userVar = (ContextVariableUser)theList.get(key);
                theAttributeString += userVar.getVariableName() + "=" + userVar.getValue() + ";";
            }
        }
        return theAttributeString;
    }
    
    public boolean isUserVariableSet(String varname) {
        LinkedHashMap<String,ContextVariableUser> theList = DialogMain.getDialogUserList().getCurrentContextVariables();
        //Logger.info("Varname is '" + varname + "'");
        
       /*if (theList != null)
            theList.forEach((k,v) -> {
                Logger.info("Found variable " + "'" + k + "'");
                Logger.info("Confirming value: " + v.getValue());
            });
       else {
           Logger.info("The list was null..");
       }*/
        
        if (theList == null) {
            //Logger.info("Returning false, list was null");
            return false; 
        }
        else if (theList.get(varname) == null) {
            //Logger.info("Returning false, var not found");

            return false;
        }
        
        //Logger.info("Returning not value is empty");

        return (!theList.get(varname).getValue().isEmpty());
    }
}

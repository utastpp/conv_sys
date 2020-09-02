/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.contextvariable;

import cmcrdr.logger.Logger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dherbert
 */
public class ContextVariable {
    
    private int variableId;
    
    private String variableName;

    private LinkedHashMap<Integer,String> variableValues = new LinkedHashMap<>();
    private LinkedHashMap<Integer,ContextVariableAction> variableActions = new LinkedHashMap<>();
    private String variableValueOverride = "";

    
    
    /**
     * Constructor.
     */
    public ContextVariable() {
        
    }
    
    /**
     * get the number of values associated with this context variable
     * @return the number of values
     */
    public int getVariableValuesAmount(){
        return this.variableValues.size();
    }
    
    /**
     * Get the number of actions associated with this context variable
     * @return the number of actions
     */
    public int getVariableActionsAmount(){
        return this.variableActions.size();
    }

    /**
     * Set the current context variable id
     * @param variableId the context variable id to use
     */
    public void setVariableId(int variableId){
        this.variableId = variableId;
    }
    
    /**
     * Get the current context variable id
     * @return the id of the current context variable
     */
    public int getVariableId(){
        return this.variableId;
    }
    
    /**
     * Set the current context variable name
     * @param variableName the name to use for this context variable
     */
    public void setVariableName(String variableName){
        this.variableName = variableName;
    }
    
    /**
     * Get the current context variable name
     * @return the name of the current context variable
     */
    public String getVariableName(){
        return this.variableName;
    }
    
    /**
     * Get the base set of all values for the current context variable instance
     * @return the base set of all values
     */
    public LinkedHashMap<Integer,String> getValuesBase(){
        return this.variableValues;
    }
    
    /**
     * Get the base set of all actions associated with the current context variable instance
     * @return the base set of all actions
     */
    public LinkedHashMap<Integer,ContextVariableAction> getActionsBase(){
        return this.variableActions;
    }
    
    /**
     * Add a new value for the current context variable instance
     * @param variableValue the value to add
     * @return true if addition successful, false if value already in base set
     */
    public boolean addVariableValue(String variableValue) {
       
        if(variableValues.containsValue(variableValue)){
            return false;
        } else {
            this.variableValues.put(variableValues.size(), variableValue);
            return true;
        }                     
    }
    
    /**
     * Add a new action to the base set of actions for the current context variable instance
     * @param target the context variable that will be the target of the action
     * @param context name of the context variable to be used as source data for the target
     * @param trigger the condition necessary for the action to occur
     * @return true if action addition succeeds, false if this context variable instance already targets the target with the same trigger
     */
    public boolean addVariableActionContext(String target, String context, String trigger) {
       
        Iterator iter = variableActions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            ContextVariableAction action = (ContextVariableAction)me.getValue();
            String theTarget = action.getTarget();
            String theTargetValue = action.getContext();
            String theTrigger = action.getTrigger();
            //if (theTarget.equals(target)) { DPH August 2019 - can target same var but not with same trigger!
            if (theTarget.equals(target) && theTrigger.equals(trigger)) {
                return false;
            }
        }
        
        
            ContextVariableAction anAction = new ContextVariableAction();
            anAction.setContextValue(context);
            anAction.setTarget(target);
            anAction.setTrigger(trigger);
            this.variableActions.put(variableActions.size(), anAction);
            return true;

    }
    
    /**
     *
     * Add a new action to the base set of actions for the current context variable instance
     * @param target the context variable that will be the target of the action
     * @param fixed literal value used as source data for the target
     * @param trigger the condition necessary for the action to occur
     * @return true if action addition succeeds, false if this context variable instance already targets the target with the same trigger
     */
    public boolean addVariableActionFixed(String target, String fixed, String trigger) {
        //Logger.info("Sanity check: I'm being called..");    
        Iterator iter = variableActions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            ContextVariableAction action = (ContextVariableAction)me.getValue();
            String theTarget = action.getTarget();
            String theTargetValue = action.getValue();
            String theTrigger = action.getTrigger();
            // if (theTarget.equals(target)) { DPH August 2019 must also match trigger here!
            if (theTarget.equals(target) && theTrigger.equals(trigger)) {
                return false;
            }
        }
        
            ContextVariableAction anAction = new ContextVariableAction();
            anAction.setFixedValue(fixed);
            anAction.setTarget(target);
            anAction.setTrigger(trigger);
            //anAction.setContextValue(this.variableName);
            this.variableActions.put(variableActions.size(), anAction);
                                
            return true;
    }
    
    /**
     * Delete a value from the base set of values
     * @param value the value to delete
     * @return true if deletion successful, false if value did not exist
     */
    public boolean deleteVariableValue(String value) {
        if(!this.variableValues.containsValue(value)){
            return false;
        } else {
            Set values = this.variableValues.entrySet();
            // Get an iterator
            Iterator valuesIterator = values.iterator();
            while (valuesIterator.hasNext()) {
                Map.Entry me = (Map.Entry) valuesIterator.next();
                int valuesId = (int) me.getKey();          
                String valueName = (String) me.getValue();          
                if(valueName.equals(value)){
                    this.variableValues.remove(valuesId);
                    break;
                }            
            }
            return true;
        }
    }
    
    /**
     * Delete an action associated with the current context variable instance
     * @param targetAndValue space-delimited string containing target variable name and value
     * @return true if action targeting the target existed and has been deleted
     */
    public boolean deleteVariableAction(String targetAndValue) {
        
        String target = targetAndValue.split(" ")[0];
        
        Iterator actionIterator = this.variableActions.entrySet().iterator();
        while (actionIterator.hasNext()) {
            Map.Entry me = (Map.Entry) actionIterator.next();
            int id = (int) me.getKey();
            ContextVariableAction action = (ContextVariableAction) me.getValue();
            if (action.getTarget().equals(target)) {
                this.variableActions.remove(id);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean deleteVariableAction(String trigger, String targetVariable, String targetValue) {      
        
        for (Map.Entry me : this.variableActions.entrySet()) {
            int id = (int) me.getKey();
            ContextVariableAction anAction = (ContextVariableAction) me.getValue();
            if (anAction.getTarget().equals(targetVariable) && anAction.getTrigger().equals(trigger) && anAction.getValue().equals(targetValue)) {
                this.variableActions.remove(id);
                Logger.info("Deleted action:" + trigger + " on " + targetVariable + " with value:" + targetValue);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Specify the base set of all variable actions for the current context variable instance
     * @param actions the set of actions to add
     */
    public void setVariableActions(LinkedHashMap<Integer,ContextVariableAction> actions){
        this.variableActions = actions;
    }
    
    /**
     * Get the set of actions associated with the current context variable instance
     * @return
     */
    public LinkedHashMap<Integer,ContextVariableAction> getVariableActions(){
        return this.variableActions;
    }
    
    /**
     * Determine if there is an action associated with the target context variable
     * @param target the name of the context variable
     * @return true if an action with target exists, false if the action base set does not contain the target
     */
    public boolean isVariableActionExist(String target){
        Iterator iter = variableActions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            ContextVariableAction action = (ContextVariableAction)me.getValue();
            String theTarget = action.getTarget();
            if (theTarget.equals(target)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the action associated with the specified context variable target
     * @param target the name of the context variable that is the target of the action
     * @return the action if it exists, null otherwise
     */
    public ContextVariableAction getVariableAction(String target){
        Iterator iter = variableActions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            ContextVariableAction action = (ContextVariableAction)me.getValue();
            String theTarget = action.getTarget();
            if (theTarget.equals(target)) {
                return action;
            }
        }
        return null;
    }
    
    /**
     * Set the base set of values for the current context variable instance
     * @param values the values to be used as the base set of values
     */
    public void setVariableValues(LinkedHashMap<Integer,String> values){
        this.variableValues = values;
    }
    
    /**
     * Get a set of all the values associated with the current context variable instance
     * @return
     */
    public HashMap<Integer,String> getVariableValues(){
        return this.variableValues;
    }
    
    /**
     *
     * @param value
     * @return
     */
    public boolean isVariableValueExist(String value){
        return this.variableValues.containsValue(value);
    }

    /**
     * Get an array of string representing all values for the current context variable instance
     * @return an array of single-element string arrays, used typically in a JTable
     */
    public String[][] toVariableValuesStringArrayForGUI() {
        
        int wordCount = this.getVariableValuesAmount();
        // new object for gui
        String[][] newObject = new String[wordCount][1];

        Set values = this.variableValues.entrySet();
        // Get an iterator
        Iterator valueIterator = values.iterator();
        int cnt = 0;
        while (valueIterator.hasNext()) {
            Map.Entry me = (Map.Entry) valueIterator.next();
            String value = (String) me.getValue();            
            
           
            newObject[cnt][0] = value;

            cnt++;
        }       
        
        return newObject;
    }
    
    /**
     * Get the value of the current context variable instance, assuming there is only one value
     * @return string value associated with the current context variable instance
     */
    public String getSingleVariableValue() {
        String result = "";
        if (getVariableValuesAmount() == 1) {
            result =  (String) this.variableValues.entrySet().iterator().next().getValue();
        }
        
        return result;
    }
    
    /**
     * Get an array of string representing all values for the current context variable instance
     * @return an array of single-element string arrays, used typically as data source for JTable
     */
    public String[][] toVariableActionsStringArrayForGUI() {
        
        int wordCount = this.getVariableActionsAmount();
        // new object for gui
        String[][] newObject = new String[wordCount][1];

        Set values = this.variableActions.entrySet();
        // Get an iterator
        Iterator valueIterator = values.iterator();
        int cnt = 0;
        while (valueIterator.hasNext()) {
            Map.Entry me = (Map.Entry) valueIterator.next();
            ContextVariableAction context = (ContextVariableAction) me.getValue();            
            String target = context.getTarget();
            String value = context.getValue();
           
            newObject[cnt][0] = target + " " + value;

            cnt++;
        }       
        
        return newObject;
    }
    
    public void setVariableValueOverride(String override) {
        variableValueOverride = override;
    }
    
    public String getVariableValueOverride() {
        return variableValueOverride;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.contextvariable;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class ContextVariableAction {
    
    private String target;
    private String contextValue;
    private String fixedValue;
    private String trigger;
    private boolean useContextVariableValue;
    
    /**
     * Set the target variable name for this action
     * @param target name of the target variable
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * Set the name of the variable whose value is used in the action
     * @param context name of the source variable whose value is used when action triggered
     */
    public void setContextValue(String context) {
        contextValue = context;
        useContextVariableValue = true;
    }
    
    /**
     * Set the literal value to use when action is triggered
     * @param fixed a string to be used to set the target variable's value
     */
    public void setFixedValue(String fixed) {
        fixedValue = fixed;
        useContextVariableValue = false;
    }
    
    /**
     * Get the value (variable name) or literal value associated with this action
     * @return string containing a literal value or variable name which is the data source for this action
     */
    public String getValue() {
        String result = "";
        
        if (useContextVariableValue) {
            if (contextValue != null) {
                result = contextValue;
            }
        }
        else {
            result = fixedValue;
        }
        
        return result;
    }
    
    /**
     * Get the name of the variable to be used as the data source for this action
     * @return context variable name to be used as data source for the action
     */
    public String getContext() {
        return contextValue;
    }
    
    /**
     * Get the literal value used as the data source for this action
     * @return a string which is the value to be used as the data source for this action
     */
    public String getFixed() {
        return fixedValue;
    }
    
    /**
     * Determine if the data source for the action is another variable 
     * @return True if a context variable should be used as the data source
     */
    public boolean contextUsed() {
        return useContextVariableValue;
    }
    
    /**
     * Get the target variable name for this action
     * @return the name of the target
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * Get the trigger condition string to be used to determine whether the action is fired
     * @return the trigger value
     */
    public String getTrigger() {
        return trigger;
    }
    
    /**
     * Set the trigger value which is a simple string to be used as a conditional 
     * @param triggerString the string to be tested for trigger action (condition is equality)
     */
    public void setTrigger(String triggerString) {
        trigger = triggerString;
    }
    
    
    
}

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

public class ContextVariableUser {
    
    private String variableName;
    
    private String variableValue = "";
    
    private String variableMatchedValue = ""; // if a variable value is overridden, this is the original value
    
    private int context;
    
    private boolean armed = false;
    
    /**
     * Constructor.
     * @param varName name of the context variable to create.
     * @param criteria
     * @param value the value to assign to this context variable
     * @param theContext the stack level (of stacked inference results) where this variable was created.
     */
    public ContextVariableUser(String varName,String value, int theContext) {
        variableName = varName;
        variableValue = value;
        context = theContext;
    }
    
    /**
     * Constructor
     */
    public ContextVariableUser() {

    }
    
    /**
     * Set the name of this context variable instance
     * @param variableName the name for this context variable instance
     */
    public void setVariableName(String variableName){
        this.variableName = variableName;
    }
    
    /**
     * Get the name of this context variable instance
     * @return the name of this context variable instance
     */
    public String getVariableName(){
        return this.variableName;
    }
    
    /**
     * Get the value of this context variable instance
     * @return the value of this context variable instance
     */
    public String getValue(){
        return this.variableValue;
    }
    
    /**
     * Set the value of this context variable instance
     * @param variableValue the value to assign to this context variable instance
     */
    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;                
    }
    
   
        
    /**
     * Determine if this value of this context variable is a non-empty string
     * @return true if this context variable has a non-empty string value
     */
    public boolean isVariableValueExist() {
        return !this.variableValue.equals("");
    }
    
    public void setVariableMatchedValue(String value) {
        variableMatchedValue = value;
    }
    
    public String getVariableMatchedValue() {
        return variableMatchedValue;
    }
    
    /**
     * Get the stack level context where this variable was created (key for the list of inference results)
     * @return the stack level where this context variable was created
     */
    public int getContext() {
        return context;
    }
    
    /**
     * Set the stack level context where this context variable was defined
     * @param theContext the stack level where this context variable is defined
     */
    public void setContext(int theContext) {
        context = theContext;
    }
    
        
    public boolean isArmed() {
        return armed;
    }
    
    public void arm() {
        armed = true;
    }
    
    public void disarm() {
        armed = false;
    }
}

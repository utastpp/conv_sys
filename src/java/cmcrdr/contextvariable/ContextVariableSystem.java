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
public class ContextVariableSystem {
    //private int variableId;
    
    private String variableName;

    private String variableValue = "";
    
    private int variableContext;
    
    
    /**
     * Constructor.
     * @param varName name of the context variable to create
     * @param value value to assign to the new context variable
     * @param context which stack context was this variable created it?
     */
    public ContextVariableSystem(String varName, String value, int context) {
        //variableId = id;
        variableName = varName;
        variableValue = value;
        variableContext = context;
    }
    
    /**
     * Set the name of this context variable instance
     * @param variableName
     */
    public void setVariableName(String variableName){
        this.variableName = variableName;
    }
    
    /**
     * Get the name of this context variable instance
     * @return the name of the context variable
     */
    public String getVariableName(){
        return this.variableName;
    }
    
    /**
     * Get the value assigned to this context variable
     * @return the value of this context variable instance
     */
    public String getValue(){
        return this.variableValue;
    }
    
    /**
     * Set the value assigned to this context variable
     * @param variableValue the value to be assigned to this context variable instance
     */
    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;                
    }
    
    /**
     * Get the stack context for this variable (which stacked inference result defined this variable?)
     * (the stack context is merely the key to the linked list of inference results)
     * @return the stack context for this variable
     */
    public int getContext() {
        return variableContext;
    }
    
    /**
     * Set the stack context for this variable (which stacked inference result defined this variable?)
     * @param context the stack context
     */
    public void setContext(int context) {
        variableContext = context;
    }
        
    /**
     * Determine if this context variable instance has an assigned (non-empty string) value
     * @return true if this context variable has an assigned non-empty string value.
     */
    public boolean isVariableValueExist() {
        return !this.variableValue.equals("");
    }
}

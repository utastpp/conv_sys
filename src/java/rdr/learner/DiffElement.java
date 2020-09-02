/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.learner;

import java.util.HashMap;

import rdr.model.Value;

/**
 * This class is used to define difference element
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DiffElement {

    /**
     *
     */
    public static final int POSITIVE_FULL_DIFF = 1;

    /**
     *
     */
    public static final int POSITIVE_PARTIAL_DIFF = 2;

    /**
     *
     */
    public static final int NEGATIVE_FULL_DIFF = 3;

    /**
     *
     */
    public static final int NEGATIVE_PARTIAL_DIFF = 4;
    
    private String attributeName;
    private Value currentCaseValue;
    private HashMap<Integer, Value> validationCaseValue;
    
    private int diffType = DiffElement.POSITIVE_FULL_DIFF;  
      
    /**
     * Constructor
     */
    public DiffElement(){
        this.attributeName = null;
        this.currentCaseValue = null;
        this.validationCaseValue = null;
        this.diffType = DiffElement.POSITIVE_FULL_DIFF;
    }
    
    /**
     * Constructor
     * @param attributeName
     * @param currentCaseValue
     * @param validationCaseValue
     * @param diffType 
     */
    public DiffElement(String attributeName, Value currentCaseValue, 
            HashMap<Integer, Value> validationCaseValue, int diffType){
        this.attributeName = attributeName;
        this.currentCaseValue = currentCaseValue;
        this.validationCaseValue = validationCaseValue;
        this.diffType = diffType;
    }
    
    /**
     * Get attribute name
     * @return 
     */
    public String getAttributeName(){
        return this.attributeName;
    }
    
    /**
     * Set attribute name
     * @param name 
     */
    public void setAttributeName(String name) {
        this.attributeName = name;
    }
    
    /**
     * 
     * @return 
     */
    public Value getCurrentCaseValue(){
        return this.currentCaseValue;
    }
    
    /**
     * 
     * @param currentCaseValue
     */
    public void setCurrentCaseValue(Value currentCaseValue) {
        this.currentCaseValue = currentCaseValue;
    }
    
    /**
     * 
     * @return 
     */
    public HashMap<Integer, Value> getValidationCaseValue(){
        return this.validationCaseValue;
    }
    
    /**
     * 
     * @param validationCaseValue
     */
    public void setValidationCaseValue(HashMap<Integer, Value>
            validationCaseValue) {
        this.validationCaseValue = validationCaseValue;
    }    
    
    /**
     * Get difference type
     * @return 
     */
    public int getDiffType(){
        return this.diffType;
    }
    
    /**
     * Set difference type
     * @param type 
     */
    public void setDiffType(int type){
        this.diffType = type;
    }
    
}

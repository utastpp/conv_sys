/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.model;

import java.util.ArrayList;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DateAttribute extends Attribute {

    /**
     *
     */
    protected DateAttribute maxValue;

    /**
     *
     */
    protected DateAttribute minValue;
    
    /**
     *
     */
    public DateAttribute(){
        super(ValueType.DATE);
    }
    
    /**
     *
     * @param attributeType
     * @param name
     * @param valueType
     */
    public DateAttribute(String attributeType, 
            String name, ValueType valueType){
        super(attributeType, name, valueType);
    }
    
    /**
     *
     * @param attributeType
     * @param name
     * @param value
     */
    public DateAttribute(String attributeType, String name, Value value){
        super(attributeType, name, value);
    } 
    
    /**
     *
     * @param attributeType
     * @param names
     * @param valueType
     */
    public DateAttribute(String attributeType, ArrayList<String> names, 
            ValueType valueType){
        super(attributeType, names, valueType);
        this.isBasic = false;
    }    
    
    /**
     * Get maximum value
     * @return 
     */
    public DateAttribute getMax(){
        return this.maxValue;
    }
    
    /**
     * Set maximum value
     * @param max 
     */
    public void setMax(DateAttribute max){
        this.maxValue = max;
    }
    
    /**
     * Get minimum value
     * @return 
     */
    public DateAttribute getMin() {
        return this.minValue;
    }
    
    /**
     * Set max minimum value
     * @param min 
     */
    public void setMin(DateAttribute min) {
        this.minValue = min;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String[] getPotentialOperators() {
        String[] operators = {"Before", "Before or Equals", "After", "After or Equals"};
        return operators;
    } 
}

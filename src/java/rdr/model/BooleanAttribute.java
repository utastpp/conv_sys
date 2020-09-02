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
public class BooleanAttribute extends Attribute {
    
    /**
     *
     */
    public BooleanAttribute(){
        super(ValueType.BOOLEAN);
    }
    
    /**
     *
     * @param attributeType
     * @param name
     * @param valueType
     */
    public BooleanAttribute(String attributeType, 
            String name, ValueType valueType){
        super(attributeType, name, valueType);
    }
    
    /**
     *
     * @param attributeType
     * @param name
     * @param value
     */
    public BooleanAttribute(String attributeType, String name, Value value){
        super(attributeType, name, value);
    } 
    
    /**
     *
     * @param attributeType
     * @param names
     * @param valueType
     */
    public BooleanAttribute(String attributeType, ArrayList<String> names, 
            ValueType valueType){
        super(attributeType, names, valueType);
        this.isBasic = false;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String[] getPotentialOperators() {
        String[] operators = {"=="};
        return operators;
    }    

    
    
}

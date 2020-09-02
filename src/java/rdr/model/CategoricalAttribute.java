/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.model;

import java.util.ArrayList;
import rdr.logger.Logger;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class CategoricalAttribute extends Attribute {
    
    /**
     * Allowed value list when the attribute is nominal
     */
    protected ArrayList<String> categoricalValues = new ArrayList<>();
    
    /**
     *
     */
    public CategoricalAttribute(){
        super(ValueType.CATEGORICAL);
    }
    
    /**
     *
     * @param attributeType
     * @param name
     * @param valueType
     */
    public CategoricalAttribute(String attributeType, 
            String name, ValueType valueType){
        super(attributeType, name, valueType);
    }
    
    /**
     *
     * @param attributeType
     * @param name
     * @param valueTypeCode
     */
    public CategoricalAttribute(String attributeType, String name, int valueTypeCode){
        super(attributeType, name, valueTypeCode);
    } 
    
    /**
     *
     * @param attributeType
     * @param name
     * @param value
     */
    public CategoricalAttribute(String attributeType, String name, Value value){
        super(attributeType, name, value);
    } 
    
    /**
     *
     * @param attributeType
     * @param names
     * @param valueType
     */
    public CategoricalAttribute(String attributeType, ArrayList<String> names, 
            ValueType valueType){
        super(attributeType, names, valueType);
        this.isBasic = false;
    }    
    
    /**
     *
     * @param attributeType
     * @param names
     * @param valueTypeCode
     */
    public CategoricalAttribute(String attributeType, ArrayList<String> names, 
            int valueTypeCode){
        super(attributeType, names, valueTypeCode);
        this.isBasic = false;
    }    
    
    
    /**
     * Get value list for nominal attributes
     *
     * @return
     */
    @Override
    public ArrayList<String> getCategoricalValues() {
        return this.categoricalValues;
    }

    /**
     * Set allowable values
     *
     * @param values
     */
    @Override
    public void setCategoricalValues(ArrayList<String> values) {
        this.categoricalValues = values;
    }

    /**
     * Add Value
     *
     * @param value
     * @return 
     */
    @Override
    public boolean addCategoricalValue(String value) {
        if (this.categoricalValues.contains(value)) {
            return false;
        } else {
            this.categoricalValues.add(value);
        }
        return true;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String[] getPotentialOperators() {
        String[] operators = {"==", "!="};
        return operators;
    }    
}

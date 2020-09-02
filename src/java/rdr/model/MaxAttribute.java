/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Basic condition a1 is higher than 10 
 * 
 * Condition -- Max(a1, a2, a3) is higher than 10
 * @author yangsokk
 */
public class MaxAttribute extends ContinuousAttribute{
    
    /**
     *
     */
    public MaxAttribute(){
        super();
        this.isBasic = false;
    }
    
    /**
     *
     * @param attributeType
     * @param names
     * @param valueType
     */
    public MaxAttribute(String attributeType, ArrayList<String> names, 
            ValueType valueType){
        super(attributeType, names, valueType);
    }
    
    /**
     *
     * @param attributeValues
     * @return
     */
    @Override
    public Value getDerivedValue(HashMap<String, Value> attributeValues){
        double max = (double)attributeValues.get(attributeList.get(0)).getActualValue();
        for(int i=1; i<attributeList.size(); i++) {
            if((double)attributeValues.get(attributeList.get(0)).getActualValue()>max) {
                max = (double)attributeValues.get(attributeList.get(0)).getActualValue();
            }
        }
        return new Value(ValueType.CONTINUOUS, max);
    }
}

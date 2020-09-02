/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.cases.Case;
import rdr.logger.Logger;
import rdr.model.Value;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class ConditionSet {
    
    /**
     * condition set <condition> 
     */
    private HashSet<Condition> conditionSet = new HashSet<>();
    
    /**
     * Constructor
     */
    public ConditionSet(){
        this.conditionSet = new HashSet<>();
    }
    
    /**
     * Constructor with condition set
     * @param conditionSet 
     */
    public ConditionSet(HashSet<Condition> conditionSet) {
        this.conditionSet = conditionSet;
    }
    
    /**
     * Get condition amount
     * @return 
     */
    public int getConditionAmount() {
        return this.conditionSet.size();
    }
    
    /**
     * Get condition set
     * @return 
     */
    public HashSet<Condition> getBase() {
        return this.conditionSet;
    }

    /**
     * Set condition set
     * @param conditionSet 
     */
    public void setConditionSet(HashSet<Condition> conditionSet){
        this.conditionSet = conditionSet;
    }
    
    
    /**
     * Add new condition to condition set
     * @param condition 
     * @return  
     */
    public boolean addCondition(Condition condition) {
        if(!this.conditionSet.contains(condition)){
            this.conditionSet.add(condition);
        } else {
            Logger.error("Condition already exists.");
            return false;
        }
        return true;
    }
    
    
    /**
     * Add new condition to condition set
     * @param condition 
     * @return  
     */
    public boolean deleteCondition(Condition condition) {
        if(!this.conditionSet.contains(condition)){
            Logger.error("Cannot delete condition.");
            return false;
        } else {
            this.conditionSet.remove(condition);
        }
        return true;
    }
    
    
    /**
     * Delete all condition
     */
    public void deleteAllCondition() {
        this.conditionSet = new HashSet<>();
    }
    
   /**
     * Get condition as a object for GUI
     * @param conditionAmount
     * @return 
     */
    
    public Object[][] toObjectForGUI(int conditionAmount) {
        // new object for gui
        Object[][] newObject = new Object[conditionAmount][3];
        
        // Get an iterator
        Iterator conditionIterator = this.conditionSet.iterator();
        
        //count for condition
        int conditionCnt = 0;
        while (conditionIterator.hasNext()) {
            Condition aCondition = (Condition) conditionIterator.next();
            
            newObject[conditionCnt][0] = aCondition.getAttribute().getName();
            newObject[conditionCnt][1] = aCondition.getOperator().toString();
            newObject[conditionCnt][2] = aCondition.getValue().toString();
                    
            conditionCnt++;
        }        
        
        
        return newObject;
    }
    
    /**
     *
     * @return
     */
    public String toObjectforGUI() {
        // Get an iterator
        Iterator conditionIterator = this.conditionSet.iterator();
        // Display elements
        String strConditionSet =" " ;
        int cnt=0;
        while (conditionIterator.hasNext()) {
            if(cnt!=0){
                strConditionSet+= " & ";
            }
            Condition aCondition = (Condition) conditionIterator.next();
            
            strConditionSet += aCondition.toString();
            cnt++;
        }
        return strConditionSet;
    }
   
    /**
     * Get condition as a string
     * @return 
     */
    @Override
    public String toString() {
        String strConditionSet =" ";
        if(this.conditionSet.size() > 0 && this.conditionSet!=null){
            // Get an iterator
            Iterator conditionIterator = this.conditionSet.iterator();
            // Display elements
            int cnt=0;
            while (conditionIterator.hasNext()) {
                if(cnt!=0){
                    strConditionSet+= " & ";
                }
                Condition aCondition = (Condition) conditionIterator.next();

                strConditionSet += aCondition.toString();
                cnt++;
            }
        } else {
            strConditionSet += "null";
        }
        return strConditionSet;
    }
    
    
}

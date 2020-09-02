/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.cases;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.model.Attribute;
import rdr.model.AttributeSet;
import rdr.model.IAttribute;
import rdr.model.ValueType;

/**
 * This class is used to define a structure of case
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public final class CaseStructure  {
    /**
     * Case Structure.
     */
    private LinkedHashMap<String, IAttribute> caseStructure;
    
    /**
     * Attribute Name for Case Structure.
     */
    private String[] caseStructureAttrNameArray;
    
    /**
     * Constructor.
     */
    public CaseStructure(){
        caseStructure = new LinkedHashMap<>();
        
    }
    
    /**
     * Constructor with an Case Structure
     * @param attributeSet the set of attributes to be used as the structure for the case
     */
    public CaseStructure(AttributeSet attributeSet) {
        this.caseStructure = attributeSet.getBase();        
        caseStructureAttrNameArray = attributeSet.getAttributeNameArray();
    }
    
    
    /**
     * Get Case Structure
     * @return the set of attributes that form a basis for all cases in the CaseSet
     */
    public LinkedHashMap<String, IAttribute>  getStructureBase(){
        return this.caseStructure;
    }
    
    /**
     * Get Attribute Amount
     * @return how many attributes are associated with each case
     */
    public int getAttrAmount(){
        return this.caseStructure.size();
    }
    
    /**
     *
     * @param hashMapName
     * @param hashMapType
     * @return
     */
    public LinkedHashMap<String, Attribute> constructStructure(HashMap<Integer, String> hashMapName, HashMap<Integer, String> hashMapType){
        
        LinkedHashMap<String, Attribute> newCaseStructure = new LinkedHashMap<>();
        int hashMapSize = hashMapName.size();
        caseStructureAttrNameArray = new String[hashMapSize];
        for(int i=0;i<hashMapSize;i++){
            String attributeName = hashMapName.get(i);
            String attributeType = hashMapType.get(i);
            ValueType valueType = new ValueType(attributeType);
            Attribute attribute = new Attribute("Case Attribute", attributeName, valueType);
            
            caseStructureAttrNameArray[i] = attributeName;
            newCaseStructure.put(attributeName, attribute);
        }
        return newCaseStructure;
    }
    
    
    /**
     * Get attribute by name
     * @param attributeName the name of the attribute to retrieve
     * @return the requested attribute
     */
    public IAttribute getAttributeByName(String attributeName){
        return this.caseStructure.get(attributeName);
    }    
    
    
    /**
     * Get attribute by attribute id
     * @param attributeId id of attribute to retrieve
     * @return the requested attribute, null if id is not found
     */
    public IAttribute getAttributeByAttrId(int attributeId){
        IAttribute attr = null;
        Set set = this.caseStructure.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            attr = (IAttribute) me.getValue();
            if(attr.getAttributeId()==attributeId){
                break;
            }
        }
        return attr;
    }    
    
    
    /**
     * Add an attribute to attribute set
     * @param attribute the attribute to be added to the case structure
     * @return true if successful, false if attribute name already exists
     */
    public boolean addAttribute(IAttribute attribute) {
        if(this.caseStructure.containsKey(attribute.getName())){
            return false;
        } else {
            attribute.setAttributeId(this.caseStructure.size());
            this.caseStructure.put(attribute.getName(), attribute);
        }
        return true;
    }
    
    /**
     * Delete single attribute
     * @param attribute the attribute to be deleted from the case structure
     * @return true if deletion successful, false if attribute does not exist
     */
    public boolean deleteAttribute(IAttribute attribute) {
        if(!this.caseStructure.containsKey(attribute.getName())){
            return false;
        } else {
            this.caseStructure.remove(attribute.getName());
        }
        return true;
    }
    
    /**
     * Delete single attribute by name
     * @param attrName name of attribute to be deleted
     * @return true if deletion successful, false if attribute does not exist
     */
    public boolean deleteAttributeByName(String attrName) {
        if(!this.caseStructure.containsKey(attrName)){
            return false;
        } else {
            this.caseStructure.remove(attrName);
        }
        return true;
    }
    
    /**
     * Get string array of attribute name
     * @return a string array containing all attribute names and their associated type
     */
    public String[] getAttributeNameArrayWithCaseId() {
        String[] attrArray = new String[this.caseStructure.size()+1];
        Set set = this.caseStructure.entrySet();
        attrArray[0] = "Case ID";
        Iterator i = set.iterator();
        int idx = 1;
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            IAttribute attr = (IAttribute) me.getValue();
            attrArray[idx] = attr.getName() + " [" + attr.getValueType().getTypeName() + "]";
            idx++;
        }        
        return attrArray;
    } 
    
    /**
     * Get string array of attribute name
     * @return a string array containing all attribute names
     */
    public String[] getAttributeNameArray() {
        String[] attrArray = new String[this.caseStructure.size()];
        Set set = this.caseStructure.entrySet();
        
        Iterator i = set.iterator();
        int idx = 0;
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            IAttribute attr = (IAttribute) me.getValue();
            attrArray[idx] = attr.getName();
            idx++;
        }        
        return attrArray;
    } 
    
    public String toString(){
        String str = "";
        Set set = this.caseStructure.entrySet();
        
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            IAttribute attr = (IAttribute) me.getValue();
            str += attr.getName() + "[" + attr.getValueType().getTypeName() + "] \n";
        }        
        
        
        return str;
    }
    
}

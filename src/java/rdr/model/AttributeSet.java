/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is a container for a set of attribute
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class AttributeSet {
    /**
     * Attribute set.
     */
    private LinkedHashMap<String, IAttribute> attriuteSet;
    
    /**
     * Constructor.
     */
    public AttributeSet(){
        attriuteSet = new LinkedHashMap<>();
        
    }
    
    /**
     * Constructor with an attribute set
     * @param attriuteSet 
     */
    public AttributeSet(LinkedHashMap<String, IAttribute> attriuteSet) {
        this.attriuteSet = attriuteSet;
    }
    
    /**
     * Get attribute set
     * @return 
     */
    public LinkedHashMap<String, IAttribute>  getBase(){
        return this.attriuteSet;
    }
    
    /**
     * Set an attribute set
     * @return 
     */
    @Override
    public AttributeSet clone()  {
        return new AttributeSet((LinkedHashMap<String, IAttribute>) this.attriuteSet.clone());
    }
    
    /**
     * Set an attribute set
     * @param attriuteSet 
     */
    public void setAttributeSet(LinkedHashMap<String, IAttribute> attriuteSet) {
        this.attriuteSet = attriuteSet; 
    }
    
    /**
     * Get attribute by name
     * @param attributeName
     * @return 
     */
    public IAttribute  getAttributeByName(String attributeName){
        return this.attriuteSet.get(attributeName);
    }    
    
    
    /**
     * Add an attribute to attribute set
     * @param attribute
     * @return 
     */
    public boolean addAttribute(IAttribute attribute) {
        if(this.attriuteSet.containsKey(attribute.getName())){
            return false;
        } else {
            this.attriuteSet.put(attribute.getName(), attribute);
        }
        return true;
    }
    
    /**
     * Delete single attribute
     * @param attribute
     * @return 
     */
    public boolean deleteAttribute(IAttribute attribute) {
        if(this.attriuteSet.containsKey(attribute.getName())){
            return false;
        } else {
            this.attriuteSet.remove(attribute.getName());
        }
        return true;
    }
    
    /**
     * Get string array of attribute name
     * @return 
     */
    public String[] getAttributeNameArray() {
        String[] attrArray = new String[this.attriuteSet.size()];
        Set set = this.attriuteSet.entrySet();
        Iterator i = set.iterator();
        int idx = 0;
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            attrArray[idx] = (String) me.getKey();
            idx++;
        }        
        return attrArray;
    } 
}

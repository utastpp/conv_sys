/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.rules;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.logger.Logger;


/**
 * This class is used to store conclusion
 * @author David Chung
 */
public class ConclusionSet {
    
    /**
     * Conclusion set <conclusionName, conclusion> 
     */
    private LinkedHashMap<String, Conclusion> conclusionSet = new LinkedHashMap<>();
    
    /**
     * Constructor
     */
    public ConclusionSet(){
        this.conclusionSet = new LinkedHashMap();        
    }
    
    /**
     * Constructor with conclusion set
     * @param conclusionSet 
     */
    public ConclusionSet(LinkedHashMap<String, Conclusion> conclusionSet) {
        this.conclusionSet = conclusionSet;
    }
    
    /**
     * Get conclusion set size
     * @return  returns the number of conclusions in conclusion set
     */
    public int getSize() {
        return this.conclusionSet.size();
    }
    
    
    /**
     * Get a new conclusion id
     * @return 
     */
    public int getNewConclusionId() {
        if(this.conclusionSet.size()>0){
            Set conclusions = this.conclusionSet.entrySet();
            Iterator iterator = conclusions.iterator();
            int maxId = 0;
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                Conclusion aConclusion = (Conclusion)me.getValue();
                maxId = Math.max(maxId, aConclusion.getConclusionId());
            }
            return maxId+1;
        } else {
            return 1;
        }
    }
    
    /**
     * Returns true if there is conclusion in the conclusion set
     * @param aConclusion
     * @return  returns true if there is conclusion in the conclusion set
     */
    public boolean isExist(Conclusion aConclusion) {
        //Logger.info("Looking to see if " + aConclusion.getConclusionName() + " exists already..");
        //for (Conclusion currentConclusion : this.conclusionSet.values()) {
            //Logger.info("Current conclusion: ID:" + currentConclusion.getConclusionId() + "name:" +  currentConclusion.getConclusionName() + " value:" + currentConclusion.getConclusionValue());
            //if (currentConclusion.getConclusionName() == aConclusion.getConclusionName())
                //Logger.info("MATCH FOUND!");
       //}
        return this.conclusionSet.containsKey(aConclusion.getConclusionName());
    }
    
    /**
     * Get conclusion set
     * @return 
     */
    public LinkedHashMap<String, Conclusion> getBase() {
        return this.conclusionSet;
    }

    /**
     * Set conclusion set
     * @param conclusionSet 
     */
    public void setConclusionSet(ConclusionSet conclusionSet){
        this.conclusionSet = conclusionSet.getBase();
    }
    
    /**
     * Get a conclusion set by conclusion name
     * @param name
     * @return 
     */
    public Conclusion getConclusionByName(String name) {        
        return this.conclusionSet.get(name);
    }
    
    /**
     * Get a conclusion set by conclusion id
     * @param conclusionId
     * @return 
     */
    public Conclusion getConclusionById(int conclusionId) {      
        Conclusion conclusion = new Conclusion();
        Set conclusions = this.conclusionSet.entrySet();
        // Get an iterator
        Iterator caseIterator = conclusions.iterator();
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            conclusion = (Conclusion)me.getValue();
            if(conclusion.getConclusionId()==conclusionId){
                break;
            }
        }
        return conclusion;
    }
    
    /**
     * Add new conclusion to conclusion set
     * @param conclusion 
     * @return  
     */
    public boolean addConclusion(Conclusion conclusion) {
        if(!this.conclusionSet.containsKey(conclusion.getConclusionName())){
            this.conclusionSet.put(conclusion.getConclusionName(), conclusion);
        } else {
            return false;
        }
        return true;
    }
    
    
    /**
     * Add new conclusion to conclusion set
     * @param conclusion 
     * @return  
     */
    public boolean deleteConclusion(Conclusion conclusion) {
        if(!this.conclusionSet.containsKey(conclusion.getConclusionName())){
            Logger.error("This conclusion " + conclusion.getConclusionName() + " does not exist.");
            return false;
        } else {
            this.conclusionSet.remove(conclusion.getConclusionName());
        }
        return true;
    }
    
    /**
     * Add new conclusion to conclusion set
     * @param name
     * @return  
     */
    public boolean deleteConclusionByName(String name) {
        if(!this.conclusionSet.containsKey(name)){
            Logger.error("This conclusion " + name + " does not exist.");
            return false;
        } else {
            this.conclusionSet.remove(name);
        }
        return true;
    }    
    
    /**
     * Delete all conclusion
     */
    public void deleteAllConclusion() {
        this.conclusionSet = new LinkedHashMap<>();
    }
    
    
    /**
     * Get conclusion set as string array
     * @return 
     */
    public String[] toStringArrayForGUI() {
        Set conclusions = this.conclusionSet.entrySet();
        // Get an iterator
        Iterator caseIterator = conclusions.iterator();
        // Display elements
        String[] strConclusionArray = new String[conclusions.size()+1];
        int i=0;
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            Conclusion conclusion = (Conclusion)me.getValue();
            strConclusionArray[i] = conclusion.getConclusionName();
            i++;
        }
        if(i==0){
            strConclusionArray = new String[2];
            strConclusionArray[0] = "There is no conclusion";
            i++;
        }
        strConclusionArray[i] = "[Add Conclusion]";
        return strConclusionArray;
    }
    
    /**
     * Get conclusion set as string array
     * @return 
     */
    public String[] toStringArrayForGUIWithoutAddConclusion() {
        Set conclusions = this.conclusionSet.entrySet();
        // Get an iterator
        Iterator caseIterator = conclusions.iterator();
        // Display elements
        String[] strConclusionArray = new String[conclusions.size()];
        int i=0;
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            Conclusion conclusion = (Conclusion)me.getValue();
            strConclusionArray[i] = conclusion.toString();
            i++;
        }
        if(i==0){            
            strConclusionArray = new String[1];
            strConclusionArray[0] = "There is no conclusion";            
        }        
        return strConclusionArray;
    }
   
    /**
     * Get conclusion as a string
     * @return 
     */
    @Override
    public String toString() {
        Set conclusions = this.conclusionSet.entrySet();
        // Get an iterator
        Iterator conclusionIterator = conclusions.iterator();
        // Display elements
        String strConclusion ="Rule id " ;
        while (conclusionIterator.hasNext()) {
            Map.Entry me = (Map.Entry) conclusionIterator.next();            
            strConclusion += me.getValue().toString()+"\n";
            
        }
        strConclusion = strConclusion+"\n";
        return strConclusion;
    }
}
 
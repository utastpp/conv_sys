/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.cases;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import rdr.logger.Logger;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.RuleSet;

/**
 * This class is used to define a case used in RDR
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Case {
    
    /**
     * Processed case identifier
     */
    public static final int Processed = 1;  
    
    /**
     * NOT Processed case identifier
     */
    public static final int NotProcessed = 0;  
    
    /**
     * Case identifier
     */
    private int caseId;
    
    /**
     * case structure
     */
    private CaseStructure caseStructure;    
    
    /**
     * Value set for each attribute
     */
    private LinkedHashMap<String, Value> attributeValues = new LinkedHashMap<String, Value>(); 
    
    /**
     * Case status - processed (1) or not (0)
     */
    private int caseStatus;
    
    /**
     * Inference Result
     */
    private RuleSet inferenceResult = new RuleSet();
    
    /**
     * conclusion set of inference result
     */
    private ConclusionSet conclusionSet = new ConclusionSet();
    
    /**
     * Constructor.
     */
    public Case(){
        this.caseStructure = new CaseStructure();  
        this.attributeValues = new LinkedHashMap<>();
    }
    
    /**
     * Constructor.
     * @param aCase a case to be duplicated by the constructor
     */
    public Case(Case aCase){
        if (aCase == null)
            Logger.info("Case is null..");
        else {
        this.caseStructure = aCase.caseStructure;
        this.attributeValues = (LinkedHashMap<String, Value>) aCase.attributeValues.clone();
        this.caseId = aCase.caseId;
        this.caseStatus = aCase.caseStatus;
        this.conclusionSet = aCase.conclusionSet;
        }
    }
    
    /**
     * Constructs a case object
     *
     * @param caseStructure CaseStructure to be used as the basis of a new case
     */
    public Case(CaseStructure caseStructure) {
        this.caseStructure = caseStructure;
        this.attributeValues = new LinkedHashMap<>();
        this.caseStatus = Case.NotProcessed;
    }
    
    /**
     * Constructs a case object
     *
     * @param values linked list of named attribute values
     * @param caseStructure the type of case structure used by this constructor
     */
    public Case(CaseStructure caseStructure, LinkedHashMap<String, Value> values) {
        this.caseStructure = caseStructure;
        this.attributeValues = values;
        this.caseStatus = Case.NotProcessed;
    }    
    /**
     * Constructs a case object
     *
     * @param id the case id
     * @param caseStructure the type of case structure used by this constructor
     * @param values linked list of named attribute values
     */
    public Case(int id, CaseStructure caseStructure, LinkedHashMap<String, Value> values) {
        this.caseId = id;
        this.attributeValues = values;
        this.caseStatus = Case.NotProcessed;
    }

    /**
     * Get case id
     * @return the case id
     */
    public int getCaseId() {
        return this.caseId;
    }    
    
    /**
     * Set case id
     * @param id Unique id for this case
     */
    public void setCaseId(int id) {
        this.caseId = id;
    }
    
    /**
     * Get case values
     * @return LinkedHas
     */
    public LinkedHashMap<String, Value> getValues() {
        return this.attributeValues;
    }
    
    
    /**
     * Set case values
     * @param values LinkedHashMap of named attributes
     * @return True if named attribute exists
     */
    public boolean setValues(LinkedHashMap<String, Value> values) {
        Set set = values.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            String attributeName = (String) me.getKey();
            Value value = (Value) me.getValue();
            if(this.caseStructure.getAttributeByName(attributeName).getValueType()
                    != value.getValueType()) {
                return false;
            } else {
                this.addValue(attributeName, value);
            }
        }
        return true;
    }

    /**
     * Set a case attribute value specified by attribute name and value
     * @param name the name of the attribute
     * @param value the attribute Value, which consists of a type ("CONTINUOUS", "CATEGORICAL", "TEXT", "DATE", "BOOLEAN") and value
     */
    public void setValue(String name, Value value) {
        if(this.attributeValues.containsKey(name)){
            this.attributeValues.replace(name, value);
        } else {
            this.attributeValues.put(name, value);
        }
    }

    /**
     * Get a case attribute value specified by attribute name
     * @param name get the named attribute
     * @return the attribute Value, or null if attribute does not exist
     */
    public Value getValue(String name) {
        if(this.attributeValues.containsKey(name)){
            return this.attributeValues.get(name);
        } else {
            return null;
        }
    }
 
    /**
     * Get a case attribute value specified by attribute name
     * @param attribute a generic attribute
     * @return attribute Value
     */
    public Value getValue(IAttribute attribute) {
        if(attribute.getIsBasic()){
            return attributeValues.get(attribute.getName());
        } else {
            return attribute.getDerivedValue(attributeValues);
        }
    }
    
    /**
     * Add new value
     * @param name an attribute name    
     * @param value an attribute Value
     * @return false if named attribute value is not the same type as provided value
     */
    public boolean addValue(String name, Value value) {
        if(this.caseStructure.getAttributeByName(name).getValueType() 
                == value.getValueType()) {
            this.attributeValues.put(name, value);
        } else {
            return false;
        }
        return true;
    }
    
    /**
     * Set case status (currently unused)
     * @param status current case status
     */
    public void setCaseStatus(int status) {
        this.caseStatus = status;
    }
    
    /**
     * Get case status (currently unused)
     * @return current case status
     */
    public int getCaseStatus() {
        return this.caseStatus;
    }
    
    /**
     * Get case structure
     * @return returns a structure of the case
     */
    public CaseStructure getCaseStructure() {
        return this.caseStructure;
    }
    
    /**
     * Get case status as String
     * @return string representing current case status (Processed or Not Processed) (currently unused)
     */
    public String getCaseStatusString() {
        String result = "";
        if (caseStatus == 0) {
            result = "Not Processed";
        } else if (caseStatus == 1) {
            result = "Processed";
        }
        return result;
    }
    /**
     * Add conclusion
     * @param conclusion a new conclusion to be added to the case
     */
    public void addConclusion(Conclusion conclusion){
        this.conclusionSet.addConclusion(conclusion);
    }
    
    
    /**
     * Delete conclusion
     * @param conclusion conclusion to be deleted from this case
     */
    public void deleteConclusion(Conclusion conclusion){
        this.conclusionSet.deleteConclusion(conclusion);
    }
    
    
    /**
     * Clear conclusion set.
     */
    public void clearConclusionSet(){
        this.conclusionSet = new ConclusionSet();
    }
    
    
    /**
     * Get conclusion set
     * @return  the current conclusion set
     */
    public ConclusionSet getConclusionSet(){
        return this.conclusionSet;
    }
    
    
    /**
     * Set conclusion set
     * @param conclusionSet a set of conclusions to be associated with this case
     */
    public void setConclusionSet(ConclusionSet conclusionSet){
        this.conclusionSet = conclusionSet;
    }
    
    
    /**
     * Get the case values for setting up a JTable with the data
     *
     * @return an array of strings which represent this case's data
     */
    public Object[] getValuesArray() {
        Object[] caseValuesArray = new String[this.attributeValues.size() + 1];
        //build the array full of case values
        caseValuesArray[0] = this.caseId;
        // Get a set of the entries
        Set set = this.attributeValues.entrySet();
        // Get an iterator
        Iterator i = set.iterator();
        // Display elements
        int idx = 1;
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            Value current = (Value) me.getValue();
            String currentString = current.toString();
            caseValuesArray[idx] = currentString;
            idx++;
        }
        return caseValuesArray;
    }
    
    /**
     * Check whether this instance equals with another instance
     * @param other an object to be tested for equality (a case)
     * @return  true if other case attributes are the same
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Case)) {
            return false;
        } else {
            Case c = (Case) other;
            if (c.getValues().size() != this.getValues().size()) {
                return false;	//sizes are different, definitely not equal.
            }
            Set set = this.attributeValues.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                Value source = (Value) me.getValue(); 
                Value target = (Value) c.getValue((String)me.getKey()); 
                if(!source.equals(target)){
                    return false;
                } 
            }
        }
        return true;	//found no differences, so equal.
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.attributeValues);
        return hash;
    }
    
    /**
     * Convert the case into an easily readable text representation
     * @return the text representation of the case
     */
    @Override
    public String toString() {
        String str = new String();        // Get a set of the entries
        Set set = this.attributeValues.entrySet();
        // Get an iterator
        Iterator i = set.iterator();
        str = "Case (" + this.caseId + "): ";
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            String attributeName = (String) me.getKey();
            Value attributeValue = (Value) me.getValue();
            str += attributeName +":"+ attributeValue.toString()+"\t";
        }        
        return str;
    }    
    
    /**
     *
     * @param attributeAmount
     * @return
     */
    public Object[][] toObjectOnlyValue(int attributeAmount) {
        
        // new object for gui
        Object[][] newObject = new Object[attributeAmount][1];
        
        LinkedHashMap<String, Value> caseValues = this.attributeValues;
        Set values = caseValues.entrySet();
        // Get an iterator
        Iterator valIterator = values.iterator();
            
        //count for attribute
        int attrCnt=0;
        while (valIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) valIterator.next();
            newObject[attrCnt][1] = me2.getValue().toString();

            attrCnt++;
        }
        return newObject;
    }
    
    /**
     *
     * @param attributeAmount
     * @return
     */
    public Object[][] toObjectOnlyValueWithCaseId(int attributeAmount) {
        
        // new object for gui
        Object[][] newObject = new Object[1][attributeAmount+1];
        
        LinkedHashMap<String, Value> caseValues = this.attributeValues;
        Set values = caseValues.entrySet();
        // Get an iterator
        Iterator valIterator = values.iterator();
            
        //count for attribute
        int attrCnt=1;
        newObject[0][0] = this.caseId;
        while (valIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) valIterator.next();
            newObject[0][attrCnt] = me2.getValue().toString();

            attrCnt++;
        }
        return newObject;
    }
    
    /**
     *
     * @param attributeAmount
     * @return
     */
    public Object[][] toObjectForGUIRow(int attributeAmount) {
        
        // new object for gui
        Object[][] newObject = new Object[attributeAmount][2];
        
        LinkedHashMap<String, Value> caseValues = this.attributeValues;
        Set values = caseValues.entrySet();
        // Get an iterator
        Iterator valIterator = values.iterator();
            
        //count for attribute
        int attrCnt=0;
        while (valIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) valIterator.next();
            newObject[attrCnt][0] = me2.getKey();
            newObject[attrCnt][1] = me2.getValue().toString();

            attrCnt++;
        }
        return newObject;
    }
    
    /**
     *
     * @param attributeAmount
     * @return
     */
    public Object[][] toObjectForGUIRowWithType(int attributeAmount) {
        
        // new object for gui
        Object[][] newObject = new Object[attributeAmount][3];
        
        LinkedHashMap<String, Value> caseValues = this.attributeValues;
        Set values = caseValues.entrySet();
        // Get an iterator
        Iterator valIterator = values.iterator();
            
        //count for attribute
        int attrCnt=0;
        while (valIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) valIterator.next();
            //attribute name
            newObject[attrCnt][0] = me2.getKey();
            //attribute value type
            Value aValue = (Value) me2.getValue();
            newObject[attrCnt][1] = aValue.getValueType().getTypeName();
            //attribute value
            newObject[attrCnt][2] = me2.getValue().toString();
            //Logger.info("" + attrCnt);
            attrCnt++;
        }
        return newObject;
    }
    
    /**
     *
     * @param attributeAmount
     * @return
     */
    public Object[][] toObjectForGUIRowSortedWithType(int attributeAmount) {
        
        // new object for gui
        Object[][] newObject = new Object[attributeAmount][3];
        
        LinkedHashMap<String, Value> caseValues = this.attributeValues;
        
        Map<String, Value> map = new TreeMap<String, Value>(caseValues); 
        
        Set values = map.entrySet();
        // Get an iterator
        Iterator valIterator = values.iterator();
            
        //count for attribute
        int attrCnt=0;
        while (valIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) valIterator.next();
            //attribute name
            newObject[attrCnt][0] = me2.getKey();
            //attribute value type
            Value aValue = (Value) me2.getValue();
            newObject[attrCnt][1] = aValue.getValueType().getTypeName();
            //attribute value
            newObject[attrCnt][2] = me2.getValue().toString();
            //Logger.info("" + attrCnt);
            attrCnt++;
        }
        return newObject;
    }

    /**
     *
     * @param attributeAmount
     * @return
     */
    public Object[][] toObjectForGUICol(int attributeAmount) {
        
        // new object for gui
        Object[][] newObject = new Object[attributeAmount][2];
        
        LinkedHashMap<String, Value> caseValues = this.attributeValues;
        Set values = caseValues.entrySet();
        // Get an iterator
        Iterator valIterator = values.iterator();
            
        //count for attribute
        int attrCnt=0;
        while (valIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) valIterator.next();
            newObject[attrCnt][0] = me2.getKey();
            newObject[attrCnt][1] = me2.getValue().toString();

            attrCnt++;
        }
        return newObject;
    }
}

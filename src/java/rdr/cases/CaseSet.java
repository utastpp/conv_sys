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
import java.util.TreeMap;
import rdr.logger.Logger;
import rdr.model.Value;

/**
 * This class is a container for cases
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class CaseSet {
    
    /**
     * case set
     */
    private HashMap<Integer, Case> caseSet = new HashMap<>();
    
    /**
     * case structure
     */
    private CaseStructure caseStructure;    
    
    
    /**
     * Constructor.
     */
    public CaseSet(){
        caseSet = new HashMap<>();
        caseStructure = new CaseStructure();
    }
    
    /**
     * Constructor with case structure.
     * @param caseStructure the case structure (named attribute list) that form the basis of this CaseSet
     */
    public CaseSet(CaseStructure caseStructure) {
        this.caseStructure = caseStructure;
    }
    
    /**
     * Constructor.
     * @param caseSet set of cases to form the basis of this CaseSet
     */
    public CaseSet(CaseSet caseSet) {
        this.caseSet = caseSet.getBase();
        this.caseStructure = caseSet.caseStructure;
    }
    
    /**
     * Get case base
     * @return hash map of cases
     */
    public HashMap<Integer, Case> getBase() {
        return this.caseSet;
    }

    /**
     * Set case base
     * @param caseSet the HashMap set of cases to be associated with this CaseSet
     */
    public void setCaseBase(HashMap<Integer, Case> caseSet){
        this.caseSet = caseSet;
    }
    
    /**
     * Get number of cases in set
     * @return number of cases in the CaseSet
     */
    public int getCaseAmount() {
        return this.caseSet.size();
    }
    
    /**
     * Get new case id
     * @return a new id which is one greater than the highest value already present in the set
     */
    public int getNewCaseId() {
        if(this.caseSet.size()>0){
            Set dialogs = this.caseSet.entrySet();
            Iterator iterator = dialogs.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                Case aCase = (Case)me.getValue();
                maxId = Math.max(maxId, aCase.getCaseId());
            }
            return this.caseSet.get(maxId).getCaseId()+1;
        } else {
            return 1;
        }
    }
    
    /**
     * Check whether case exist in case set
     * @param caseId the id to test for existence 
     * @return true if the case exists
     */
    public boolean isCaseExist(int caseId) {
        return this.caseSet.containsKey(caseId);
    }
    
    
    /**
     * Get case by Id
     * @param caseId the id of the case to fetch
     * @return the case being fetched
     */
    public Case getCaseById(int caseId) {
        return this.caseSet.get(caseId);
    }
    
    
    
    /**
     * Replace case 
     * @param aCase case data to replace existing (matching) case key
     * @return the old case data that was replaced
     */
    public Case replaceCase(Case aCase) {
        return this.caseSet.replace(aCase.getCaseId(), aCase);
    }
    
    
    /**
     * Add case
     * @param aCase the case to add to the set
     * @return  true if the case was added successfully (false if the key mapping already exists)
     */
    public boolean addCase(Case aCase) {
        if(this.caseSet.containsKey(aCase.getCaseId())){
            Logger.error("Cannot add this case since it is already in the case set");
            return false;
        } else {
            this.caseSet.put(aCase.getCaseId(), aCase);
        }
        return true;
    }
    
    /**
     * Put case set together
     * @param aCaseSet set of cases to add to the current CaseSet
     */
    public void putCaseSet(CaseSet aCaseSet) {
        caseSet.putAll(aCaseSet.getBase());
    }
    
    /**
     * delete case
     * @param aCase case to be deleted
     * @return true if deleted successfully, false if case did not exist in the CaseSet 
     */
    public boolean deleteCase(Case aCase) {
        if(this.caseSet.containsValue(aCase)){
            this.caseSet.remove(aCase.getCaseId());
        } else {
            Logger.error("This case does not exist in the case set");
            return false;
        }
        return true;
    }
    
    /**
     * delete case case id
     * @param caseId the id of the case to be deleted from the CaseSet
     * @return  true if deleted successfully, false if case did not exist in the CaseSet 
     */
    public boolean deleteCaseById(int caseId) {
        if(this.caseSet.containsKey(caseId)){
            this.caseSet.remove(caseId);
        } else {
            Logger.error("This case does not exist in the case set");
            return false;
        }
        return true;
    }
    
    /**
     * Get cornerstone case set
     * @return copy of set of all cases in this CaseSet
     */
    public CornerstoneCaseSet getCornerstoneCaseSet() {
        CornerstoneCaseSet returnCornerstoneCaseSet = new CornerstoneCaseSet();
         Set cases = this.caseSet.entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            Case aCase = (Case) me.getValue();
            returnCornerstoneCaseSet.addCase(aCase);
        }
        return returnCornerstoneCaseSet;
    }
    
    
    @Override
    public String toString() {
        Set cases = this.caseSet.entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();
        // Display elements
        String strCaseBase = "\r\n";
        strCaseBase = strCaseBase+"Cases in Casebase \r\n";
        strCaseBase = strCaseBase+"================= \r\n";
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            strCaseBase = strCaseBase + "Case "+me.getKey()+": "+me.getValue().toString()+"\r\n";
        }
        strCaseBase = strCaseBase+"================= \r\n";
        return strCaseBase;
    }
    
    /**
     *
     * @return
     */
    public String toStringSorted() {
        Map<Integer, Case> map = new TreeMap<Integer, Case>(this.caseSet); 
        Set set2 = map.entrySet();
        Iterator iterator2 = set2.iterator();
        
        String strCaseBase = "\r\n";
        strCaseBase = strCaseBase+"Cases in Casebase \r\n";
        strCaseBase = strCaseBase+"================= \r\n";    
        
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            strCaseBase = strCaseBase + "Case "+me2.getKey()+": "+me2.getValue().toString()+"\r\n";
        }
       
        strCaseBase = strCaseBase+"================= \r\n";
        return strCaseBase;
    }
    
    /**
     *
     * @return
     */
    public String toStringOnlyId() {
        Set cases = this.caseSet.entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();
        // Display elements
        String strCaseBase = "";        
        int cnt=0;
        while (caseIterator.hasNext()) {
            if(cnt!=0){
                strCaseBase+=", ";
            }
            Map.Entry me = (Map.Entry) caseIterator.next();
            strCaseBase = strCaseBase + me.getKey();
            cnt++;
        }        
        return strCaseBase;
    }
    
    /**
     *
     * @return
     */
    public String toStringOnlyIdSorted() {
        Map<Integer, Case> map = new TreeMap<Integer, Case>(this.caseSet); 
        Set set2 = map.entrySet();
        Iterator iterator2 = set2.iterator();
        
        String strCaseBase = "";        
        int cnt=0;
        
        while(iterator2.hasNext()) {
            if(cnt!=0){
               strCaseBase+=", ";
            }
            Map.Entry me2 = (Map.Entry)iterator2.next();
            strCaseBase = strCaseBase + me2.getKey();
            cnt++;
        }
       
        return strCaseBase;
    }
    
    /**
     *
     * @param caseAmount
     * @param colAmount
     * @return
     */
    public Object[][] toObjectForGUI(int caseAmount, int colAmount) {
        // new object for gui
        Object[][] newObject = new Object[caseAmount][colAmount];
        
        if(caseAmount!=0){

            Set cases = this.caseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();

            //count for case
            int caseCnt = 0;
            while (caseIterator.hasNext()) {
                Map.Entry me = (Map.Entry) caseIterator.next();
                int caseId = (int) me.getKey();
                Case aCase = (Case) me.getValue();

                LinkedHashMap<String, Value> caseValues = aCase.getValues();
                Set values = caseValues.entrySet();
                // Get an iterator
                Iterator valIterator = values.iterator();

                //store case id in first column
                newObject[caseCnt][0] = caseId;

                //count for attribute
                int attrCnt=1;
                while (valIterator.hasNext()) {
                    Map.Entry me2 = (Map.Entry) valIterator.next();
                    newObject[caseCnt][attrCnt] = me2.getValue().toString();

                    attrCnt++;
                }
                caseCnt++;
            }        
        }
        
        return newObject;
    }
}

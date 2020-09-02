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
import rdr.logger.Logger;
import rdr.model.Value;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class CornerstoneCaseSet {
    
    /**
     * case set
     */
    private HashMap<Integer, CornerstoneCase> cornerstoneCaseSet = new HashMap<>();
    
    /**
     * case structure
     */
    private CaseStructure caseStructure;    
    
    /**
     * Constructor.
     */
    public CornerstoneCaseSet(){
        cornerstoneCaseSet = new HashMap<>();
        caseStructure = new CaseStructure();
    }
    
    /**
     * Constructor with cornerstone case structure.
     * @param caseStructure set the case structure for cases in the cornerstone case set
     */
    public CornerstoneCaseSet(CaseStructure caseStructure) {
        this.caseStructure = caseStructure;
    }
    
    /**
     * Constructor.
     * @param cornerstoneCaseSet the case set to be used as the basis for this cornerstone case set
     */
    public CornerstoneCaseSet(CornerstoneCaseSet cornerstoneCaseSet) {
        this.cornerstoneCaseSet = cornerstoneCaseSet.getBase();
        this.caseStructure = cornerstoneCaseSet.caseStructure;
    }
    
    /**
     * Get cornerstone case base
     * @return list of all cases associated with the cornerstone case set
     */
    public HashMap<Integer, CornerstoneCase> getBase() {
        return this.cornerstoneCaseSet;
    }

    /**
     * Set case base
     * @param cornerstoneCaseSet the set of cases to be used as a basis for this cornerstone case set
     */
    public void setCornerstoneCaseBase(HashMap<Integer, CornerstoneCase> cornerstoneCaseSet){
        this.cornerstoneCaseSet = cornerstoneCaseSet;
    }
    
    /**
     * Get first cornerstone case in the set
     * @return the first case in the cornerstone case set
     */
    public CornerstoneCase getFirstCornerstoneCase() {
        if(this.cornerstoneCaseSet.size()>0){
            Set cases = this.cornerstoneCaseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();
            if(caseIterator.hasNext()){
                Map.Entry me = (Map.Entry) caseIterator.next();
                CornerstoneCase aCase = (CornerstoneCase) me.getValue();
                return aCase;
            }
        } else {
            return null;
        }
        return null;
    }
    
    /**
     * Get next case of the given cornerstone case
     * @param aCase the case that precedes the case we want to retrieve
     * @return the case following the specified case
     */
    public CornerstoneCase getNextCornerstoneCase(CornerstoneCase aCase) {
        if(this.cornerstoneCaseSet.size()>0){
            Set cases = this.cornerstoneCaseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();
            while(caseIterator.hasNext()){
                Map.Entry me = (Map.Entry) caseIterator.next();
                CornerstoneCase currentCase = (CornerstoneCase) me.getValue();
                if(currentCase.equals(aCase)){
                    Map.Entry me2 = (Map.Entry) caseIterator.next();
                    CornerstoneCase resultCase = (CornerstoneCase) me2.getValue();
                    return resultCase;
                }
            }
        } else {
            return null;
        }
        return null;
    }
    
    /**
     * Get previously added case of the given cornerstone case
     * @param aCase the case that follows the case we want to retrieve
     * @return the case that precedes the specified case
     */
    public CornerstoneCase getPreviousCornerstoneCase(Case aCase) {
        if(this.cornerstoneCaseSet.size()>0){
            Set cases = this.cornerstoneCaseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();
            CornerstoneCase previousCase = new CornerstoneCase();
            while(caseIterator.hasNext()){
                Map.Entry me = (Map.Entry) caseIterator.next();
                CornerstoneCase currentCase = (CornerstoneCase) me.getValue();
                if(previousCase.equals(aCase)){
                    return previousCase;
                }
                previousCase = currentCase;
            }
        } else {
            return null;
        }
        return null;
    }
    
    /**
     * Returns true if there is next added cornerstone case
     * @param aCase case used to test if this set has a successor
     * @return true if there is a successor to the specified case
     */
    public boolean hasNextCornerstoneCase(Case aCase) {
        if(this.cornerstoneCaseSet.size()>0){
            Set cases = this.cornerstoneCaseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();
            while(caseIterator.hasNext()){
                Map.Entry me = (Map.Entry) caseIterator.next();
                CornerstoneCase currentCase = (CornerstoneCase) me.getValue();
                if(currentCase.equals(aCase)){
                    return caseIterator.hasNext();
                }
            }
        } 
        return false;  
    }
    
    /**
     * Returns true if this first added cornerstone case
     * @param aCase the case to test
     * @return true if the specified case is the first case in the cornerstone case set
     */
    public boolean isFirstCornerstoneCase(CornerstoneCase aCase) {
        if(this.cornerstoneCaseSet.size()>0){
            Set cases = this.cornerstoneCaseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();
            if(caseIterator.hasNext()){
                Map.Entry me = (Map.Entry) caseIterator.next();
                CornerstoneCase currentCase = (CornerstoneCase) me.getValue();
                if(currentCase.equals(aCase)){
                    return true;
                }
            }
        } 
        return false;  
    }
    
    /**
     * return true if cornerstone case exists in case set
     * @param aCornerstoneCase the cornerstone case to test
     * @return  true if the cornerstone case set includes the specified case
     */
    public boolean isCornerstonCaseExist(CornerstoneCase aCornerstoneCase) {
        return this.cornerstoneCaseSet.containsKey(aCornerstoneCase.getCaseId());            
    }
    
    /**
     * return true if case exists as cornerstone case in case set
     * @param aCase the case to test
     * @return  true if the cornerstone case set includes the specified case
     */
    public boolean isCaseExist(Case aCase) {
        return this.cornerstoneCaseSet.containsKey(aCase.getCaseId());            
    }
    
    /**
     * return true if case id assigned in case set
     * @param caseId the id to verify is present as a key in the cornerstone case set
     * @return  true if the cornerstone case set includes the specified case id
     */
    public boolean isCaseIdExist(int caseId) {
        return this.cornerstoneCaseSet.containsKey(caseId);            
    }
    
    /**
     * Get number of cornerstone cases in set
     * @return the number of cases in the cornerstone case set
     */
    public int getCaseAmount() {
        return this.cornerstoneCaseSet.size();
    }
    
    /**
     * Get new cornerstone case id
     * @return an id which is one greater than the highest id already present in the cornerstone case set
     */
    public int getNewCornerstoneCaseId() {
        if(this.cornerstoneCaseSet.size()>0){
            Set cases = this.cornerstoneCaseSet.entrySet();
            Iterator iterator = cases.iterator();
            int maxId = 0;
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                CornerstoneCase aCornerstoneCase = (CornerstoneCase)me.getValue();
                maxId = Math.max(maxId, aCornerstoneCase.getCaseId());
            }
            return maxId+1;
        } else {
            return 1;
        }
    }
    
    /**
     * Get cornerstone case by Id
     * @param caseId the case id to fetch
     * @return the cornerstone case with specified id
     */
    public CornerstoneCase getCornerstoneCaseById(int caseId) {
        return this.cornerstoneCaseSet.get(caseId);
    }
    
    /**
     * Add cornerstone case
     * @param aCase the case to add to the cornerstone case set
     * @return  true if the case was added successfully, false if the case is already in the set
     */
    public boolean addCase(Case aCase) {
        CornerstoneCase aCornerstoneCase = new CornerstoneCase(aCase);
        if(this.cornerstoneCaseSet.containsKey(aCornerstoneCase.getCaseId())){
            Logger.error("Cannot add this case since it is already in the case set");
            return false;
        } else {
            this.cornerstoneCaseSet.put(aCornerstoneCase.getCaseId(), aCornerstoneCase);
        }
        return true;
    }
    
    /**
     * Add cornerstone case
     * @param aCornerstoneCase the case to add to the cornerstone case set
     * @return  true if the case was added successfully, false if the case is already in the set
     */
    public boolean addCornerstoneCase(CornerstoneCase aCornerstoneCase) {
        if(this.cornerstoneCaseSet.containsKey(aCornerstoneCase.getCaseId())){
            Logger.error("Cannot add this case since it is already in the case set");
            return false;
        } else {
            this.cornerstoneCaseSet.put(aCornerstoneCase.getCaseId(), aCornerstoneCase);
        }
        return true;
    }
    
    /**
     * Put cornerstone case set together
     * @param aCornerstoneCaseSet cases to add to the cornerstone case set
     */
    public void putCornerstoneCaseSet(CornerstoneCaseSet aCornerstoneCaseSet) {
        cornerstoneCaseSet.putAll(aCornerstoneCaseSet.getBase());
    }
    
    /**
     * delete cornerstone case 
     * @param aCornerstoneCase case to be removed from the cornerstone case set
     * @return  true if deletion was successful, false if case not present in set
     */
    public boolean deleteCornerstoneCase(CornerstoneCase aCornerstoneCase) {
        if(this.cornerstoneCaseSet.containsValue(aCornerstoneCase)){
            this.cornerstoneCaseSet.remove(aCornerstoneCase.getCaseId());
        } else {
            Logger.error("This case does not exist in the case set");
            return false;
        }
        return true;
    }
    
    /**
     * delete case case id
     * @param caseId the id of the case to be deleted
     * @return  true if the case was successfully deleted, false if the case was not present in the set
     */
    public boolean deleteCaseById(int caseId) {
        if(this.cornerstoneCaseSet.containsKey(caseId)){
            this.cornerstoneCaseSet.remove(caseId);
        } else {
            Logger.error("This case does not exist in the case set");
            return false;
        }
        return true;
    }
    
    /**
     * Get cornerstone case set
     * @return a copy of all cases present in the current cornerstone case set
     */
    public CornerstoneCaseSet getCornerstoneCaseSet() {
        CornerstoneCaseSet returnCornerstoneCaseSet = new CornerstoneCaseSet();
        Set cases = this.cornerstoneCaseSet.entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            int caseId = (int) me.getKey();
            CornerstoneCase aCase = (CornerstoneCase) me.getValue();
            returnCornerstoneCaseSet.addCornerstoneCase(aCase);
        }
        return returnCornerstoneCaseSet;
    }
    
    
    @Override
    public String toString() {
        Set cases = this.cornerstoneCaseSet.entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();
        // Display elements
        String strCaseBase = "\n";
        strCaseBase = strCaseBase+"Cases in Casebase \n";
        strCaseBase = strCaseBase+"================= \n";
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            int caseId = (int) me.getKey();
            CornerstoneCase aCornerstoneCase = (CornerstoneCase) me.getValue();
            strCaseBase = strCaseBase + "Case " + caseId + ": "+ aCornerstoneCase.toString() + "\n";
            strCaseBase = strCaseBase + "wrong rule set " + aCornerstoneCase.getWrongRuleSet().toString();
        }
        strCaseBase = strCaseBase+"================= \n";
        return strCaseBase;
    }
    
    /**
     *
     * @return
     */
    public String toStringOnlyId() {
        Set cases = this.cornerstoneCaseSet.entrySet();
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
     * @param caseAmount
     * @param colAmount
     * @return
     */
    public Object[][] toObjectForGUI(int caseAmount, int colAmount) {
        // new object for gui
        Object[][] newObject = new Object[caseAmount][colAmount];
        
        if(caseAmount!=0){

            Set cases = this.cornerstoneCaseSet.entrySet();
            // Get an iterator
            Iterator caseIterator = cases.iterator();

            //count for case
            int caseCnt = 0;
            while (caseIterator.hasNext()) {
                Map.Entry me = (Map.Entry) caseIterator.next();
                int caseId = (int) me.getKey();
                CornerstoneCase aCase = (CornerstoneCase) me.getValue();

                LinkedHashMap<String, Value> caseValues = aCase.getValues();
                Set values = caseValues.entrySet();
                // Get an iterator
                Iterator valIterator = values.iterator();

                //store case id in first column
                newObject[caseCnt][0] = Integer.toString(caseId);

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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dialog;

import cmcrdr.logger.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DialogSet {


    /**
     * Dialog Repository Name
     */
    protected String dialogRepositoryName;
    
    /**
     * Dialog Repository Identifier
     */
    protected int repositoryId;
    
    /**
     *
     */
    protected static int maxId = -1;
   
    /**
     * Dialog Repository
     */
    protected LinkedHashMap<Integer, IDialogInstance> dialogRepository = new LinkedHashMap<>();
   
    /**
     * Constructor
     */
    public DialogSet() {
        this.dialogRepositoryName = null;
        this.repositoryId = 0;
        this.dialogRepository = new LinkedHashMap<>();
    }
    
    /**
     * Constructor
     * @param dialogRepository the set to use as the basis of the new repository
     */
    public DialogSet(DialogSet dialogRepository ){
        this.dialogRepositoryName = dialogRepository.dialogRepositoryName;
        this.repositoryId = dialogRepository.repositoryId;
        this.dialogRepository = dialogRepository.dialogRepository;
        
    }
    
    /**
     * Get base set
     * @return list of dialogs in the current base set
     */
    public LinkedHashMap<Integer, IDialogInstance> getBaseSet(){
        return this.dialogRepository;
    }    
    
    /**
     * Get size of repository
     * @return number of dialog instances in the base set
     */
    public int getSize(){
        if(this.dialogRepository.isEmpty()){
            return 0;
        } else {
            return this.dialogRepository.size();
        }
        
    }    
    
    /**
     * Get size of repository
     * @return an id which is one more than the highest id already present in the set
     */
    public int getNewId(){
        if(this.dialogRepository.size()>0){
            Set dialogs = this.dialogRepository.entrySet();
            Iterator iterator = dialogs.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                IDialogInstance aDialogInstance = (IDialogInstance)me.getValue();
                maxId = Math.max(maxId, aDialogInstance.getDialogId());
            }
            return this.dialogRepository.get(maxId).getDialogId()+1;
        } else {
            return 1;
        }
    } 
   
    
    
    /**
     * Clear the dialog repository set.     
     */
    public void clearRepository(){
        this.dialogRepository.clear();
    }    
    
    /**
     * Add dialog instance and returns true if success
     * @param dialogInstance a dialog instance to be added to the base set
     * @return true if the dialog can be added, false if it already exists in the set
     */
    public boolean addDialogInstance(IDialogInstance dialogInstance){
        //Logger.info("Adding new dialog to dialog set: Type: " + dialogInstance.getDialogType() + " value: " + dialogInstance.getDialogStr());
        if(!this.dialogRepository.containsKey(dialogInstance.getDialogId())){
            this.dialogRepository.put(dialogInstance.getDialogId(), dialogInstance);
            // DPH 2016
            if (dialogInstance.getDialogId() >= maxId)
                maxId = dialogInstance.getDialogId();
            return true;
        } else {
            return false;
        }
    }    
    
    /**
     * Delete dialog instance and returns true if success
     * @param dialogInstance the dialog instance to be deleted from the set
     * @return true if deletion successful, false if dialog not present in the base set
     */
    public boolean deleteDialogInstance(IDialogInstance dialogInstance){
        if(!this.dialogRepository.containsKey(dialogInstance.getDialogId())){
            return false;
        } else {
            this.dialogRepository.remove(dialogInstance.getDialogId());
            return true;
        }
    }    
    
    /**
     * Get dialog instance by id
     * @param dialogInstanceId the id of the dialog instance to fetch
     * @return the requested dialog instance
     */
    public IDialogInstance getDialoginstanceById(int dialogInstanceId){
        if(this.dialogRepository.containsKey(dialogInstanceId)){
            return this.dialogRepository.get(dialogInstanceId);
        } else {
            return null;
        }
    }    
    
    /**
     * Get dialog instance
     * @param dialogInstance the dialog instance to fetch from the base set (based on id)
     * @return the requested dialog instance, or null if it does not exist in the base set
     */
    public IDialogInstance getDialogInstance(IDialogInstance dialogInstance){
        if(this.dialogRepository.containsKey(dialogInstance.getDialogId())){
            return this.dialogRepository.get(dialogInstance.getDialogId());
        } else {
            return null;
        }
    }    
    
    /**
     * Get previous dialog instance by id
     * @param dialogInstanceId the id of the successor dialog instance to the predecessor dialog we want to fatch
     * @return the predecessor dialog instance or null if there is no predecessor
     */
    public IDialogInstance getPreviousDialoginstanceById(int dialogInstanceId){
        IDialogInstance aDialog = null;
         Set dialogs = this.dialogRepository.keySet();
        int repositorySize = this.dialogRepository.size();
        
        // Get a liat of iterator for backward iterating
        ListIterator<Integer> iterator = new ArrayList(dialogs).listIterator(repositorySize);
        while (iterator.hasPrevious()){ 
            Integer key = iterator.previous();
            if(key < dialogInstanceId){
                aDialog = this.dialogRepository.get(key);
                break;
            }
            
        }       
        return aDialog;        
    }    
    
    /**
     * Get previous rule fired dialog instance by id
     * @param dialogInstanceId the starting dialog instance id
     * @return the first predecessor dialog instance which has a rule fired (inference result)
     */
    public IDialogInstance getPreviousRuleFiredDialoginstanceById(int dialogInstanceId){
        IDialogInstance aDialog = null;
        IDialogInstance returnDialog = null;   // DPH June 2019 - and yes, this is a very late bug fix!
         Set dialogs = this.dialogRepository.keySet();
        int repositorySize = this.dialogRepository.size();
        
        // Get a list of iterator for backward iterating
        ListIterator<Integer> iterator = new ArrayList(dialogs).listIterator(repositorySize);
        while (iterator.hasPrevious()){                         
            Integer key = iterator.previous();
            if(key <= dialogInstanceId){                
                aDialog = this.dialogRepository.get(key);
                if(aDialog.getIsRuleFired()&&aDialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE){
                    returnDialog = aDialog; // DPH June 2019 
                    break;
                }
            }
            
        }
        return returnDialog;
    }    
    
    /**
     * Get most recent dialog instance
     * @return the most recent dialog instance added to the base set
     */
    public IDialogInstance getMostRecentDialogInstance(){
        if(this.dialogRepository.size()>0){
            Set dialogs = this.dialogRepository.entrySet();
            Iterator iterator = dialogs.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                IDialogInstance dialog = (IDialogInstance)me.getValue();
                maxId = Math.max(maxId, dialog.getDialogId());
            }
            return this.dialogRepository.get(maxId);
        } else {
            return null;
        }
    }  
    
    /**
     * Get the most recent dialog instance that is a user dialog
     * @return the most recent user type dialog instance
     */
    public IDialogInstance getMostRecentUserDialogInstance(){
        if(this.dialogRepository.size()>0){
            Set dialogs = this.dialogRepository.entrySet();
            Iterator iterator = dialogs.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                IDialogInstance dialog = (IDialogInstance)me.getValue();
                if (dialog.getDialogTypeCode() == DialogInstance.USER_TYPE) {
                    maxId = Math.max(maxId, dialog.getDialogId());
                }
            }
            return this.dialogRepository.get(maxId);
        } else {
            return null;
        }
    }
    
    /**
     * Get the most recent dialog instance that is a system dialog
     * @return the most recent system type dialog instance
     */
    public IDialogInstance getMostRecentSystemDialogInstance(){
        if(this.dialogRepository.size()>0){
            Set dialogs = this.dialogRepository.entrySet();
            Iterator iterator = dialogs.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                IDialogInstance dialog = (IDialogInstance)me.getValue();
                if (dialog.getDialogTypeCode() == DialogInstance.SYSTEM_TYPE) {
                    maxId = Math.max(maxId, dialog.getDialogId());
                }
            }
            return this.dialogRepository.get(maxId);
        } else {
            return null;
        }
    }
    
    /**
     * Get the most recent dialog instance that is a event dialog
     * @return the most recent event type dialog instance
     */
    public IDialogInstance getMostRecentEventDialogInstance(){
        if(this.dialogRepository.size()>0){
            Set dialogs = this.dialogRepository.entrySet();
            Iterator iterator = dialogs.iterator();
            int maxId = -1;

            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                IDialogInstance dialog = (IDialogInstance)me.getValue();
                if(dialog.getDialogTypeCode()==DialogInstance.EVENT_TYPE) {
                    maxId = Math.max(maxId, dialog.getDialogId());
                }
            }
            if (maxId > -1)
                return this.dialogRepository.get(maxId);
            else 
                return null; // no info dialog instances found..
        } else {
            return null;
        }
    }  
    
    /**
     * Get all dialog instance dialog strings concatenated as a string, include new line between instances
     * @return string of all dialog instances
     */
    public String getAllDialogStringWithNewLine(){
        String historyDialog = "";
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int cnt=1;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            IDialogInstance dialog = (IDialogInstance)me.getValue();
            if(dialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE && dialog.getDialogStr().equals("")){

            } 
            else if(dialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE && dialog.getDialogStr().equals("")){
                historyDialog += dialog.getDialogTag() + "I'm sorry, I don't understand.";
                if(cnt!=this.dialogRepository.size()){
                    historyDialog+="\n";
                }
                
            } 
            else {               

                historyDialog += dialog.toString();              

                if(cnt!=this.dialogRepository.size()){
                    historyDialog+="\n";
                }
            }
            cnt++;
        }
        return historyDialog;
    }
    
    
    /**
     * Get all dialog instance strings up to the specified dialog instance
     * @param aDialogInstance the last dialog instance (that is not included in the return)
     * @return a string of requested dialog instances
     */
    public String getAllDialogStringUntilGivenDialogWithNewLine(IDialogInstance aDialogInstance){
        String historyDialog = "";
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int cnt=1;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            IDialogInstance dialog = (IDialogInstance)me.getValue();
            if(aDialogInstance.getDialogId()==dialog.getDialogId()){
                break;
            }
            if(dialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE && dialog.getDialogStr().equals("")){
                
            } else if(dialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE && dialog.getDialogStr().equals("")){
                historyDialog += dialog.getDialogTag() + "I'm sorry, I don't understand.";
                if(cnt!=this.dialogRepository.size()){
                    historyDialog+="\n";
                }
            } else {
                historyDialog += dialog.toString();
                if(cnt!=this.dialogRepository.size()){
                    historyDialog+="\n";
                }
            }
            cnt++;
        }
        return historyDialog;
    }
    
    /**
     * Get all dialog instances as a set up to (but not including) the requested instance
     * @param aDialogInstance the last (exclusive) dialog instance in the set
     * @return all dialog instances up to the specified dialog instance
     */
    public DialogSet getAllDialogsUntilGivenDialog(IDialogInstance aDialogInstance){
        DialogSet result = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int cnt=1;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            IDialogInstance dialog = (IDialogInstance)me.getValue();
            if(aDialogInstance.getDialogId()==dialog.getDialogId()){
                break;
            }
            result.addDialogInstance(dialog);
        }
        return result;
    }
    
    /**
     * Get all dialog instances in the base set
     * @return all dialog instances in the base set
     */
    public DialogSet getAllDialogs(){
        DialogSet result = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int cnt=1;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            IDialogInstance dialog = (IDialogInstance)me.getValue();
            result.addDialogInstance(dialog);
        }
        return result;
    }
    
    /**
     * Get all dialog instances, but in reverse order (reverse sorted based on key)
     * @return all (reverse) sorted dialog instances in the base set
     */
    public DialogSet getAllDialogsReverseOrder(){
        DialogSet result = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        ArrayList<Integer> reverseOrderKeys = new ArrayList<Integer>(this.dialogRepository.keySet());
        Collections.reverse(reverseOrderKeys);
        for (int key: reverseOrderKeys) {
            result.addDialogInstance(this.dialogRepository.get(key));
        }

        return result;
    }
    
    // DPH - needed when current dialog isn't used, but we need the one before it (e.g. KA when replacing context)

    /**
     * Get the predecessor dialog to the one specified
     * @param aDialogInstance the successor dialog
     * @return the specified dialog's predecessor, null if one doesn't exist
     */
    public IDialogInstance getDialogBeforeGivenDialog(IDialogInstance aDialogInstance){
        IDialogInstance result = null;
        Set dialogs = this.dialogRepository.entrySet();
        if (this.dialogRepository.size() == 1)
            return result;
        else {
            Iterator iterator = dialogs.iterator();
            int cnt=1;
            Map.Entry current  = null;
            Map.Entry next = null;
            IDialogInstance currentDialog = null;
            IDialogInstance nextDialog = null;
            
            if (iterator.hasNext()) {
                current = (Map.Entry) iterator.next();
                currentDialog = (IDialogInstance)current.getValue();
            }
            
            while (iterator.hasNext()) {
                next = (Map.Entry) iterator.next();
                nextDialog = (IDialogInstance)next.getValue();
                
                if(aDialogInstance.getDialogId()==nextDialog.getDialogId()){
                    result = currentDialog;
                    break;
                }
                
                current =  next;
                currentDialog = nextDialog;
                
            }
            return result;
        }
    }
    
    /**
     * Get all dialog strings, but separate by slash instead of new line
     * @return all dialog strings, separated by slashes
     */
    public String getAllDialogStringWithSlash(){
        String historyDialog = "";
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int cnt=1;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            IDialogInstance dialog = (IDialogInstance)me.getValue();            
            historyDialog += dialog.getDialogStr();
            if(cnt!=this.dialogRepository.size()){
                historyDialog+="\\";
            }
            cnt++;
        }
        return historyDialog;
    }
    
    /**
     * Get all dialog strings, but separate by ' @!!@  ' instead of new line
     * (this is more human readable)
     * @return all dialog strings, separated by ' @!!@ '
     */
    public String getAllDialogStringWithSeparator(){
        String historyDialog = "";
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int cnt=1;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            IDialogInstance dialog = (IDialogInstance)me.getValue();            
            historyDialog += dialog.getDialogStr();
            if(cnt!=this.dialogRepository.size()){
                historyDialog+=" @!!@ ";
            }
            cnt++;
        }
        return historyDialog;
    }
    
    /**
     * Get all user dialog 
     * @return a set of all dialogs that are of user type
     */
    public DialogSet getUserDialogRepository(){
        DialogSet userDialogRepository = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.USER_TYPE) {
                userDialogRepository.addDialogInstance(dialogInstance);
            }
        }
        return userDialogRepository;
    }    
    
    /**
     * Get all system dialog 
     * @return a set of all dialogs that are of system type
     */
    public DialogSet getSystemDialogRepository(){
        DialogSet systemDialogRepository = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE) {
                systemDialogRepository.addDialogInstance(dialogInstance);
            }
        }
        return systemDialogRepository;
    }    
    
    /**
     * Get all event dialog 
     * @return a set of all dialogs that are of event type
     */
    public DialogSet getEventLogRepository(){
        DialogSet eventLogRepository = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.EVENT_TYPE) {
                eventLogRepository.addDialogInstance(dialogInstance);
            }
        }
        return eventLogRepository;
    }    
    
   
    
    /**
     * Get all user and event dialog 
     * @return a set of all dialogs that are of user and/or event type
     */
    public DialogSet getInputDialogRepository(){
        DialogSet inputDialogRepository = new DialogSet();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.USER_TYPE || dialogInstance.getDialogTypeCode()==DialogInstance.EVENT_TYPE) {
                inputDialogRepository.addDialogInstance(dialogInstance);
            }
        }
        return inputDialogRepository;
    }    
    
    /**
     * Get SystemDialogInstance that has the given derived case id
     * @param caseId the case id to use to find the associated system dialog instance
     * @return the system dialog instance that was caused by the inference result of the specified case
     */
    public SystemDialogInstance getSystemDialogInstanceByDerivedCaseId(int caseId){
        SystemDialogInstance aSystemDialogInstance = new SystemDialogInstance();
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE) {
                if(dialogInstance.getDerivedCaseId() == caseId){
                    aSystemDialogInstance = (SystemDialogInstance) dialogInstance;

                }
            }
        }
        return aSystemDialogInstance;
    }        
    
    
     /**
     * Get DialogInstance that has the given generated case id (EventLogInstance or UserDialogInstance)
     * @param caseId the case id to use to find the associated user or event dialog that raised the case
     * @return the user or event dialog instance that raised the specified case 
     */
    public IDialogInstance getDialogInstanceByGeneratedCaseId(int caseId){
        IDialogInstance aDialogInstance = null;
                
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.USER_TYPE || dialogInstance.getDialogTypeCode()==DialogInstance.EVENT_TYPE) {
                if(dialogInstance.getGeneratedCaseId()== caseId){
                    aDialogInstance = dialogInstance;
                } 
            }
        }
        return aDialogInstance;
    }  
    
    public int getMaxSystemDialogStackId() {
        int max = 0;
        
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();
            if(dialogInstance.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE) {
                if(dialogInstance.getStackId() > max){
                    max = dialogInstance.getStackId();
                } 
            }
        }
       
        return max;
    }
    
    /**
     * to String
     * @return string representation of all dialogs in the base set
     */
    @Override
    public String toString(){        
        String result = "";
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();            
            result += dialogInstance.getDialogId() + ": " + dialogInstance.getDialogTag() + dialogInstance.getDialogStr();
        }
        return result;
    }    
    
    
    /**
     * convert repository into array
     * @return array of dialog instances
     */
    public IDialogInstance[] toArray(){        
        IDialogInstance[] result = new IDialogInstance[this.dialogRepository.size()];
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int i=0;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();            
            result[i] = dialogInstance;
            i++;
        }
        return result;
    }    
    
    /**
     * convert repository into string array
     * @return string array of string representations of each dialog instance
     */
    public String[] toStringArray(){        
        String[] result = new String[this.dialogRepository.size()];
        Set dialogs = this.dialogRepository.entrySet();
        Iterator iterator = dialogs.iterator();
        int i=0;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            IDialogInstance dialogInstance = (IDialogInstance)me.getValue();            
            result[i] = dialogInstance.getDialogStr();
            i++;
        }
        return result;
    }    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.user;

import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.dialog.DialogSet;
import cmcrdr.handler.InputHandler;
import cmcrdr.handler.OutputHandler;
import cmcrdr.logger.Logger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.apps.Main;
import rdr.reasoner.MCRDRStackResultInstance;
import rdr.reasoner.MCRDRStackResultSet;
import rdr.rules.Rule;
import rdr.rules.RuleSet;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class DialogUserList {
    
    //private LinkedHashMap<Integer,DialogUser> dialogUserList;
    private LinkedHashMap<Integer,DialogUser> dialogUserList;
    private int currentIndex;
    
    /**
     *
     */
    public DialogUserList() {
        dialogUserList = new LinkedHashMap<>();
    }
    
    /**
     *
     * @return
     */
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    /**
     *
     * @param index
     */
    public void setCurrentIndex(int index) {
        currentIndex = index;
    }
    
    /**
     *
     * @return
     */
    public LinkedHashMap<Integer,DialogUser> getBaseSet() {
        return dialogUserList;
    }
    
    /**
     *
     * @param username
     * @param sourceType
     */
    public void addUser(String username, DialogUser.UserSourceType sourceType) {
        Date theTime = Calendar.getInstance().getTime();
        int newId = getNewId();
        DialogUser aDialogUser = new DialogUser(username, newId, sourceType, theTime);
        //Logger.info("about to add user to list.. :" + aDialogUser.toString());
        dialogUserList.put(newId, aDialogUser);
    }
    
    /**
     *
     * @param userId
     */
    public void removeUser(int userId) {
        if (dialogUserList.containsKey(userId))
            dialogUserList.remove(userId);
        if (this.isEmpty())
            setCurrentIndex(-1);
    }
    
    /**
     *
     * @param aDialogUser
     */
    public void removeUser(DialogUser aDialogUser) {
        if (dialogUserList.containsKey(aDialogUser.getUserId()))
            dialogUserList.remove(aDialogUser.getUserId());
        if (this.isEmpty())
            setCurrentIndex(-1);
    }
    
    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return dialogUserList.isEmpty();
    }
    
    /**
     *
     * @return
     */
    public DialogUser getFirstDialogUser() {
        if (!isEmpty()) {
            return dialogUserList.entrySet().iterator().next().getValue();
        }
        return null;
    }
    
    /**
     *
     * @param username
     * @return
     */
    public DialogUser getDialogUser(String username) {
        
        Iterator iter = dialogUserList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            if (aDialogUser.getUsername().equals(username))
                return aDialogUser;
        }
        
        return null;
    }
    
    /**
     *
     * @param userId
     * @return
     */
    public DialogUser getDialogUser(int userId) {
        
        if (dialogUserList.containsKey(userId))
            return dialogUserList.get(userId);
        else     
            return null;
    }
    
    /**
     *
     * @param username
     * @return
     */
    public boolean existsUser(String username) {
        Iterator iter = dialogUserList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            if (aDialogUser.getUsername().equals(username))
                return true;
        }    
        return false;
    }
    
    /**
     *
     * @param userId
     * @return
     */
    public boolean existsUser(int userId) {
        return dialogUserList.containsKey(userId);
    }
    
    /**
     *
     * @return
     */
    public int getNewId(){
        if(this.dialogUserList.size()>0){
            Set userSet = this.dialogUserList.entrySet();
            Iterator iterator = userSet.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                DialogUser aDialogUser = (DialogUser)me.getValue();
                maxId = Math.max(maxId, aDialogUser.getUserId());
            }
            return maxId+1;
        } else {
            //return 1;
            return 0;
        }
    } 
    
    /**
     *
     * @return
     */
    public ArrayList<DialogSet> getAllDialogRepositories() {
        ArrayList<DialogSet> result = new ArrayList<>();
        
        for (Map.Entry me : this.getBaseSet().entrySet()) {
            DialogUser aDialogUser = (DialogUser)me.getValue();
            result.add(aDialogUser.getDialogRepository());
        }
        
        return result;
    }
    
    /**
     *
     * @return
     */
    public  DialogUser getCurrentDialogUser() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex);
        else
            return null; 
    }
    
    /**
     *
     * @return
     */
    public  DialogSet getCurrentDialogRepository() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getDialogRepository();
        else
            return null;
    }

    /**
     *
     * @return
     */
    public  InputHandler getCurrentDialogInputHandler() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getInputHandler();
        else
            return null;
    }
    
    /**
     *
     * @return
     */
    public  OutputHandler getCurrentDialogOutputHandler() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getOutputHandler();
        else
            return null;
    }
    
    /**
     *
     * @return
     */
    public  MCRDRStackResultSet getCurrentStackInferenceResult() {
        //Logger.info("current user index is: " + currentIndex);
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getMCRDRStackResultSet();
        }
        else
            return null;
    }
    
    /**
     *
     * @return
     */
    public int getCurrentLastInferencedRuleId() {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getLastInferencedRuleId();
        }
        else
            return -1;
    }
    
    /*
    public int getCurrentLastKAInferencedRuleId() {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getLastKAInferencedRuleId();
        }
        else
            return -1;
    }
    */

    /**
     *
     * @return
     */

    
    public Rule getCurrentLastInferencedRule() {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getLastInferencedRule();
        }
        else
            return null;
    }
    
    /**
     *
     * @return
     */
    public int getCurrentStackLevel() {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getCurrentStackLevel();
        }
        else
            return -1;
    }
    
    /**
     *
     * @param context
     * @return
     */
    public int getCurrentPreviousStackLevel(int context) {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getPreviousStackLevel(context);
        }
        else
            return -1;
    }
    
    /**
     *
     * @return
     */
    public LinkedHashMap<String,ContextVariableUser> getCurrentRecentContextVariables() {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getRecentContextVariables();
        }
        else
            return null;
    }
    
    /**
     *
     * @return
     */
    public RuleSet getCurrentKAInferenceResult() {
        if (currentIndex >=0) {
            return dialogUserList.get(currentIndex).getKAInferenceResult();
        }
        else
            return null;
    }
    
    /**
     *
     * @param inferenceResult
     */
    public void setCurrentKAInferenceResult(RuleSet inferenceResult) {
        if (currentIndex >=0) {
            dialogUserList.get(currentIndex).setKAInferenceResult(inferenceResult);
        }
    }
    
    /**
     *
     * @param recent
     */
    public void setCurrentRecentContextVariables(LinkedHashMap<String,ContextVariableUser> recent) {
        if (currentIndex >=0) {
            dialogUserList.get(currentIndex).setRecentContextVariables(recent);
        }
    }
    
    /**
     *
     */
    public void clearCurrentStackedInferenceResult() {
        // DPH
        //this.stackedInferenceResult = new MCRDRStackResultSet();
        if (getCurrentStackInferenceResult() != null) {
            getCurrentDialogUser().clearMCRDRStackResultSet();

            MCRDRStackResultInstance aMCRDRStackResultInstance = new MCRDRStackResultInstance();
            aMCRDRStackResultInstance.setCaseId(0);
            aMCRDRStackResultInstance.setStackId(0);
            aMCRDRStackResultInstance.setInferenceResult(new RuleSet(Main.workbench.ruleSet.getRootRule()));
            // DPH
            //this.stackedInferenceResult.addMCRDRStackResultInstance(aMCRDRStackResultInstance);
            Logger.info("adding a new stack instance..");
            getCurrentStackInferenceResult().addMCRDRStackResultInstance(aMCRDRStackResultInstance);
        }
        
    }
    
    /**
     *
     */
    public  void clearAllStackedInferenceResult() {
        Logger.info("Resetting stacked inference results");
        
        Iterator iter = dialogUserList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            aDialogUser.clearMCRDRStackResultSet();
            
            MCRDRStackResultInstance aMCRDRStackResultInstance = new MCRDRStackResultInstance();
            aMCRDRStackResultInstance.setCaseId(0);
            aMCRDRStackResultInstance.setStackId(0);
            aMCRDRStackResultInstance.setInferenceResult(new RuleSet(Main.workbench.ruleSet.getRootRule()));
            Logger.info("adding a new stack instance..");
            aDialogUser.getMCRDRStackResultSet().addMCRDRStackResultInstance(aMCRDRStackResultInstance);
        }
    }
      
    /**
     *
     * @param username
     */
    public  void setCurrentUserIndexFromUsername(String username) {
        /*currentDialogRepository = current;
        currentUserContextVariables = current;
        currentUser = theUser;
        Main.workbench.setCurrentStackedInferenceResultIndex(current);*/
        
        Iterator iter = dialogUserList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            if (aDialogUser.getUsername().equals(username)) {
                currentIndex = aDialogUser.getUserId();
                break;
            }
        }
        //Main.workbench.setCurrentStackInferenceResult(getCurrentDialogUser().getMCRDRStackResultSet());
       // Main.workbench.setCurrentStackedInferenceResultIndex(currentIndex);

        //Main.setCurrentWorkbench(current);
    }
    
    /**
     *
     * @param username
     * @return
     */
    public  int getIndexFromUsername(String username) {
        Iterator iter = dialogUserList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            if (aDialogUser.getUsername().equals(username)) {
                return aDialogUser.getUserId();
            }
        }
        return -1;
    }
    
    /**
     *
     * @return
     */
    public  String getCurrentUsername() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getUsername();
        else
            return "";
    }
    
    /**
     *
     * @return
     */
    public  LinkedHashMap<String,ContextVariableUser> getCurrentContextVariables() {
        //Logger.info("Calling getContextVariables for index: " + currentIndex);
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getContextVariables();
        else
            return null;
    }
    
    /*
    public  LinkedHashMap<String,ContextVariableUser> getCurrentKAContextVariables() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getContextVariables(true);
        else
            return null;
    }
*/

    /**
     *
     * @return
     */

    
    public  LinkedHashMap<Integer, LinkedHashMap<String,ContextVariableUser>> getCurrentContextVariablesList() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getContextVariablesList();
        else
            return null;
    }
    
    /**
     *
     * @return
     */
    public int  getCurrentStackedMCRDRInferenceResultSize() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getStackedMCRDRInferenceResultSize();
        else
            return -1;
    }
    
    /**
     *
     * @param currentStackId
     * @param satisfiedByStackId
     */
    public void setCurrentSatisfiedBystackedMCRDRInferenceResultStackId(int currentStackId, int satisfiedByStackId) {
        if (currentIndex >= 0) 
            dialogUserList.get(currentIndex).setSatisfiedBystackedMCRDRInferenceResultProcessId(currentStackId,satisfiedByStackId);
    }
    
    /**
     *
     * @param currentStackId
     * @return
     */
    public int getCurrentSatisfiedBystackedMCRDRInferenceResultStackId(int currentStackId) {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getSatisfiedBystackedMCRDRInferenceResultStackId(currentStackId);
        else
            return -1;
    }
    
    /**
     *
     * @return
     */
    public int getNewIdFromAllDialogRepositories(){
        
        int max = 0;
        
        Iterator iter = dialogUserList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            if (aDialogUser.getDialogRepository().getNewId() > max)
                max = aDialogUser.getDialogRepository().getNewId();
        }
        return max;
    }
    
    /**
     *
     * @return
     */
    public String getCurrentPreviousInputDialogString() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getPreviousInputDialogString();
        else
            return "";
    }
    
    /**
     *
     * @param lastInput
     */
    public void setCurrentPreviousInputDialogString(String lastInput) {
        if (currentIndex >= 0) {
            dialogUserList.get(currentIndex).setPreviousInputDialogString(lastInput);
        }
    }
    
    /**
     *
     * @return
     */
    public String getCurrentCurrentInputDialogString() {
        if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getCurrentInputDialogString();
        else
            return "";
    }
    
    /**
     *
     * @param current
     */
    public void setCurrentCurrentInputDialogString(String current) {
        if (currentIndex >= 0) {
            dialogUserList.get(currentIndex).setCurrentInputDialogString(current);
        }
    }
    
    /**
     *
     * @return
     */
    public boolean getCurrentPreviousInferenceSuccess() {
        if (currentIndex >= 0) {
            return dialogUserList.get(currentIndex).getPreviousInferenceSuccess();
        }
        else 
            return false;
    }
    
    /**
     *
     * @param success
     */
    public void setCurrentPreviousInferenceSuccess(boolean success) {
        if (currentIndex >= 0) 
            dialogUserList.get(currentIndex).setPreviousInferenceSuccess(success);
    }
    
    /**
     *
     * @return
     */
    public boolean getCurrentCurrentInferenceSuccess() {
        if (currentIndex >= 0) {
            return dialogUserList.get(currentIndex).getCurrentInferenceSuccess();
        }
        else 
            return false;
    }
    
    /**
     *
     * @param success
     */
    public void setCurrentCurrentInferenceSuccess(boolean success) {
        if (currentIndex >= 0) 
            dialogUserList.get(currentIndex).setCurrentInferenceSuccess(success);
    }
    
    public int getCurrentMostRecentDialogID() {
         if (currentIndex >= 0) 
            return dialogUserList.get(currentIndex).getMostRecentDialogID();
         else
             return -1;
    }
        
    /**
     *
     * @return
     */
    public String[] toStringArray() {        
        String[] result = new String[this.dialogUserList.size()];
        Set userSet = this.dialogUserList.entrySet();
        Iterator iterator = userSet.iterator();
        int i=0;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            DialogUser dialogUser = (DialogUser)me.getValue();            
            result[i] = dialogUser.toString();
            i++;
        }
        return result;
    }  
    
    public String toStringCommaList() {        
        String result = "";
        Set userSet = this.dialogUserList.entrySet();
        Iterator iterator = userSet.iterator();
        int i=0;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            DialogUser dialogUser = (DialogUser)me.getValue(); 
            
            if (result.isEmpty())
                result = dialogUser.getUsername();
            else
                result += "," + dialogUser.getUsername();

        }
        return result;
    }    
    
    
}

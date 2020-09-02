/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.handler;

import cmcrdr.cases.DialogCase;
import cmcrdr.cases.DialogCaseGenerator;
import static cmcrdr.dialog.DialogArchiveModule.archiveDialogString;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.DialogInstanceFactory;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.event.EventInstance;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.util.LinkedHashMap;
import rdr.apps.Main;
import rdr.model.Value;
import rdr.model.ValueType;

/**
 *
 * @author dherbert
 */
public class InputHandler {
    

    private String recentStringInput="";
    private String copyRecentStringInput = "";
    private String recentContextVars="";
    

    private EventInstance recentEventInput = new EventInstance();
    
    /**
     *
     */
    public InputHandler(){
        
    }
    

    public void setRecentStringInput(String recentStringInput){
        this.recentStringInput = recentStringInput;
        this.copyRecentStringInput = recentStringInput;
    }
    

    public void clearRecentStringInput(){
        this.recentStringInput = "";
    }
    

    public String getRecentStringInput(){
        return this.recentStringInput;
    }
    

    public String getCopyRecentStringInput(){
        return this.copyRecentStringInput;
    }
    

    public void setRecentEventInput(EventInstance recentEventInput){
        this.recentEventInput = recentEventInput;
    }
    

    public EventInstance getRecentEventInput(){
        return this.recentEventInput;
    }
    
    public void setRecentContextVars(String recentVars){
        this.recentContextVars = recentVars;
    }
    

    public String getRecentContextVars(){
        return this.recentContextVars;
    }

    /**
     *
     * @return
     */
    public DialogCase processInput() {
        
        IDialogInstance dialog = null;
        
        LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        
        //String historyDialog = DialogMain.getDialogUserList().getCurrentDialogRepository().getAllDialogStringWithSlash();
        String historyDialog = DialogMain.getDialogUserList().getCurrentDialogRepository().getAllDialogStringWithSeparator();
        
        DialogMain.dicConverter.setDictionary(DialogMain.dictionary);
        
        values.put("History", new Value(ValueType.TEXT, historyDialog));

        if(!this.recentStringInput.equals("")){
            Logger.info("Processing input: [" + this.recentStringInput + "]");
            dialog = DialogInstanceFactory.createDialogInstance("UserDialog");
            
            int newDialogID = DialogMain.getDialogUserList().getNewIdFromAllDialogRepositories();
            Logger.info("Processing input: newDialogID is " + newDialogID);

            dialog.setDialogId(newDialogID);
            dialog.setDialogTypeCode(DialogInstance.USER_TYPE);
            dialog.setDialogStr(this.recentStringInput);

            DialogMain.getDialogUserList().getCurrentDialogRepository().addDialogInstance(dialog);
            
            DialogMain.dicConverter.setDictionary(DialogMain.dictionary);
            
            
            // scan the user input to see if they've provided a match for any registered context variables
            // and if any are found, assign the variable(s) and values to the user's context.
            DialogMain.globalContextVariableManager.registerUserContextVariables(recentStringInput);          
            // Now see if any of the user's context variables have actions associated with them
            DialogMain.globalContextVariableManager.executeUserContextVariableActions();
            

            this.copyRecentStringInput = this.recentStringInput;
            
            //Logger.info("CONVERTING: from: " + this.recentStringInput);
            this.recentStringInput = DialogMain.dicConverter.convertTermFromDic(this.recentStringInput,true);
            //Logger.info("CONVERTING: to: " + this.recentStringInput);
            this.copyRecentStringInput = this.recentStringInput;
            
            try {
                //archiveDialogString(DialogMain.currentUser, DialogInstance.USER_TYPE, recentStringInput);
                archiveDialogString(DialogMain.getDialogUserList().getCurrentDialogUser().getUsername(), newDialogID, DialogInstance.USER_TYPE, recentStringInput);
            } catch (Exception ex) {
                Logger.info("Problem saving current dialog: " + ex.getMessage());
            }
          
        }
             
        // look for events..
        if(!this.recentEventInput.getEventType().equals("")){
            Logger.info("Found event input: "+ this.recentEventInput.getEventType() + ", " +  this.recentEventInput.getEventValue());

            dialog = DialogInstanceFactory.createDialogInstance("EventLog");
            //dialog.setDialogId(DialogMain.getCurrentDialogRepository().getNewId());
            dialog.setDialogId(DialogMain.getDialogUserList().getNewIdFromAllDialogRepositories());
            dialog.setDialogTypeCode(DialogInstance.EVENT_TYPE);
            dialog.setDialogStr(this.recentEventInput.toString());
            dialog.setEventInstance(this.recentEventInput);
            //Logger.info("Created new event dialog, Id: " + dialog.getDialogId());          
            DialogMain.getDialogUserList().getCurrentDialogRepository().addDialogInstance(dialog);
        }
        
        values.put("Recent", new Value(ValueType.TEXT, this.recentStringInput));
        values.put("EventType", new Value(ValueType.TEXT, this.recentEventInput.getEventType()));
        values.put("EventValue", new Value(ValueType.TEXT, this.recentEventInput.getEventValue()));
        values.put("ContextVars", new Value(ValueType.TEXT, DialogMain.globalContextVariableManager.getContextVariablesForAttributeString()));
        values.put("STOP", new Value(ValueType.TEXT, "false"));
        

        // DialogCaseGenerator
        DialogCase newCase = DialogCaseGenerator.generateCase(dialog, values,true);
        
        dialog.setGeneratedCaseId(newCase.getCaseId());

        Main.allCaseSet.addCase(newCase);
        
        return newCase;
    }
}


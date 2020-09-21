/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.cases.DialogCase;
import cmcrdr.cases.DialogCaseArchiveModule;
import cmcrdr.cases.DialogCaseGenerator;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.DialogSet;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.dialog.SystemDialogInstance;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import static cmcrdr.main.DialogMain.contextPath;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import rdr.apps.Main;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Rule;
import rdr.rules.RuleSet;
import javax.swing.filechooser.FileNameExtensionFilter;
import rdr.cases.CaseLoader;
import rdr.domain.DomainLoader;
import rdr.rules.RuleLoader;
import cmcrdr.mysql.DBConnection;
import cmcrdr.mysql.DBCreation;
import cmcrdr.mysql.DBOperation;
import cmcrdr.user.DialogUser;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.text.WordUtils;
import rdr.rules.RuleTreeModel;
import cmcrdr.processor.PreAndPostProcessorAction;
import static javax.swing.JOptionPane.showMessageDialog;


/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class AdminJavaGUI extends javax.swing.JFrame implements UserInterface  {

    private DefaultListModel usersListModel = new DefaultListModel();
    private ArrayList<ManualDialogGUI> manualUsersGUIList = new ArrayList<>();
    private DefaultListModel preAndPostProcessActionListModel = new DefaultListModel();

    public final static Object syncLock = new Object();
    private static IDialogInstance selectedContextDialog;
    private static String interfaceName;

    /**
     * Creates new form TwitterDispatcherGUI
     */
    public AdminJavaGUI(String theInterfaceName) {
        initComponents();
        
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
        interfaceName = theInterfaceName;

        for (int anActionIndex = 0; anActionIndex < DialogMain.processorList.size(); anActionIndex++) {
          preAndPostProcessActionListModel.addElement((PreAndPostProcessorAction)DialogMain.processorList.get(anActionIndex).copy());
        }
        
        databaseOnOffCheckbox.setSelected(DBConnection.getIsDatabaseUsed());
    }
    
    @Override
    public  String getInterfaceName() {
        return interfaceName;
    }

    /**
     *
     * @param user
     * @param sourceType
     */
    @Override
    public void addUser(String user) {
        int userIndex;
        
        synchronized(syncLock) {
            userIndex = DialogMain.userInterfaceController.getUserId(user);
            usersListModel.addElement(user);

            clearButton.setEnabled(true);
            KAButton.setEnabled(true);
        }
    }
    
    /**
     *
     * @param user
     */
    @Override
    public  void removeUser(String user) {
        synchronized(syncLock) {
            int userIndex = getGUIIndexofUser(user);
            if (userIndex != -1)
                usersListModel.remove(getGUIIndexofUser(user));
     
            if (DialogMain.getDialogUserList().isEmpty()) {
                clearButton.setEnabled(false);
                KAButton.setEnabled(false);
            }
        }
    }
    
    
    /**
     *
     * @param user
     * @return
     */
    public int getGUIIndexofUser(String user) {            
        if (usersListModel.contains(user))
            return usersListModel.indexOf(user);
        else       
            return -1;
    }
    
    /**
     *
     * @return
     */
    public int getGUIIndexOfSelectedUser() {
        return userListBox.getSelectedIndex();
    }
    
    /**
     *
     * @return
     */
    public String getGUIValueOfSelectedUser() {
        if (userListBox.getSelectedValue() != null)
            return userListBox.getSelectedValue();
        else
           return null;
    }
        
    // a forced selection, possibly called when operator closes a manual conversation window

    /**
     *
     * @param user
     */
    public void setSelectedGUIUser(String user) {
        //int i = getUserId(user);
        int i = 0;
        
        if (userListBox.getSelectedValue() != null)
            if (!userListBox.getSelectedValue().equals(user)) {
                for (i=0; i < userListBox.getModel().getSize(); i++) {
                    if (userListBox.getModel().getElementAt(i).equals(user)) {
                        userListBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        else
            for (i=0; i < userListBox.getModel().getSize(); i++) {
                if (userListBox.getModel().getElementAt(i).equals(user)) {
                    userListBox.setSelectedIndex(i);
                    break;
                }
            }
        
        synchronized (syncLock) {
            mainConversationTextArea.removeAll();
            mainConversationTextArea.setText(DialogMain.getDialogUserList().getCurrentDialogUser().getConversationHistory());
        }
       
    }
    
    
    /**
     *
     * @param user
     * @param message
     * @param child
     */
    public void setManualMessageGUI(String user, String message, ManualDialogGUI child) {
        
        String systemReplyString = DialogMain.userInterfaceController.setIncomingMessage(user, message, DialogUser.UserSourceType.MANUAL);
        child.setReply(systemReplyString);

            // we're guaranteed to have at least one user by now..
            // Set the initial conversation history display after we've received the first message.
        updateInterfaceContent(user);
  
    }
    
    
    
    /**
     *
     * @param user
     * @param message
     * @return 
     */
    
    // Update interface after a web message has been sent..
    @Override
    public  void updateInterfaceContent(String user) {
        if (DialogMain.userInterfaceController.getNumberOfUsers() == 1) {
                userListBox.setSelectedIndex(0);
                DialogMain.getDialogUserList().setCurrentIndex(DialogMain.getDialogUserList().getFirstDialogUser().getUserId());
                mainConversationTextArea.setText(DialogMain.getDialogUserList().getCurrentDialogUser().getConversationHistory());
            }

            // update the display if we're currently looking at the user whose message has just come in
            String currentUser  = userListBox.getSelectedValue();
            if (user.equals(currentUser)) {
                mainConversationTextArea.removeAll();
                mainConversationTextArea.setText(DialogMain.getDialogUserList().getDialogUser(user).getConversationHistory());
            }
    }
    
    /**
     *
     * @param user
     */
    @Override
    public void removeUserSessionHasExpired(String user) {
        Logger.info("Removing user " + user + " as their session has expired..");
        removeUser(user);
    }
    
    // work out where the provided dialog occurs in the user's dialog history
    public static int getDialogIDSequenceInHistory(IDialogInstance KADialogInstance) {
        int count = 1;
        
        Logger.info("Reference dialogID is " + KADialogInstance.getDialogId());
        
        DialogSet userDialogs = DialogMain.getDialogUserList().getCurrentDialogRepository().getUserDialogRepository();
        for (IDialogInstance aDialog: userDialogs.getBaseSet().values()) {
            Logger.info("Looking at dialog: " + aDialog.getDialogId());
            if (aDialog.equals(KADialogInstance)) {
                return count;
            }
            count ++;
        }
        return count;
    }
    
    
    private void determineNewRuleLocation(IDialogInstance KADialogInstance) {
        
        
         // We're going to determine the possible new rule location by setting the inference result for generateCaseForKnowledgeAcquisition
         // this method in essence is asking the user where to put it
         
        boolean rulePositionDetermined = false;
        // new June 2019
            /* ************************************************************************************************************/
            int stackId;  
            RuleSet applyingInferenceResult;
            Rule inferencedRule ;            
            RuleSet currentContextRuleSet;
            String dialogStr;
            String toStringConclusion;
            String actualConclusionShown;
            String actualRecentDialogs;

            String temp1;
            String maxTemp1;
            String temp2;
            String maxTemp2;
            String dbNoResultText = "noResponse";
            String msg;
            int confirmed;

            String[] conclusionsArray;
            
        Logger.info("determineNewRuleLocation - the user dialog (KADialogInstance) ID is " + KADialogInstance.getDialogId());
        int dialogSequence = getDialogIDSequenceInHistory(KADialogInstance);
        Logger.info("determineNewRuleLocation - The dialog's sequence number is " + dialogSequence);
        int caseId = KADialogInstance.getGeneratedCaseId();
       
      
        if(DialogMain.getDialogUserList().getCurrentDialogRepository().getSize()!=0) {
            // the system response related to the current KADialogInstance
            SystemDialogInstance aSystemDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId);

                
            IDialogInstance prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(KADialogInstance.getDialogId());
            
            if (prevDialogInstance == null)
                 Logger.info("determineNewRuleLocation - prevDialogInstance is null..");
            else {               
                Logger.info("determineNewRuleLocation - Previous ID is: " + prevDialogInstance.getDialogId());
                Logger.info("determineNewRuleLocation - and dialog is: " + prevDialogInstance.toString());
            }

            
            
            
           
                     
            if (aSystemDialogInstance.getIsRuleFired()) { // the past response has an inference result! Check if this is where KA occurs
                if (Main.workbench.getStackedInferenceResult().getDoesValidStackExist()) {
                    stackId = aSystemDialogInstance.getStackId(); 
                    applyingInferenceResult= Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                    dialogStr = aSystemDialogInstance.getDialogStr();                    
                    Logger.info("immediate: inference result dialog is: " + dialogStr);
                    conclusionsArray = getContextConclusionStrings(applyingInferenceResult,dialogStr);
                    
                    msg = "Is the new knowledge related to one or more of the most recent response(s)?\n\n"
                        + "Current Context: " + conclusionsArray[0] +  "\n\n"
                        + "Recent Answer:\n" +  conclusionsArray[1] + "\n";
                
                    confirmed = JOptionPane.showConfirmDialog(this,
                        msg, "Related to the most recent response?",
                        JOptionPane.YES_NO_CANCEL_OPTION
                    );
                
                    if (confirmed == JOptionPane.YES_OPTION) {
                        generateCaseForKnowledgeAcquisition(KADialogInstance, "immediate");
                        rulePositionDetermined = true;
                    }
                    else if (confirmed == JOptionPane.NO_OPTION) {
                        rulePositionDetermined = false;                  
                    }
                    else if (confirmed == JOptionPane.CANCEL_OPTION) {
                        return;                  
                    }
                }
            }
            
            if (prevDialogInstance!=null && !rulePositionDetermined && Main.workbench.getStackedInferenceResult().getDoesValidStackExist()) {
                stackId = prevDialogInstance.getStackId(); 
                Logger.info("continue - Stack id for KA is now: " + stackId);
                dialogStr = prevDialogInstance.getDialogStr();
                Logger.info("continue: inference result dialog is: " + dialogStr);
                applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                conclusionsArray = getContextConclusionStrings(applyingInferenceResult,dialogStr);
                
                msg = "Is the new knowledge related one of the past response(s)?\n\n"
                        + "Current Context: " + conclusionsArray[0] +  "\n\n"
                        + "Recent Answer:\n" +  conclusionsArray[1] + "\n";
               
                confirmed = JOptionPane.showConfirmDialog(this,
                        msg, "Related to a past response?",
                        JOptionPane.YES_NO_CANCEL_OPTION
                );
                
                if (confirmed == JOptionPane.YES_OPTION) {
                    generateCaseForKnowledgeAcquisition(KADialogInstance, "continue");
                    rulePositionDetermined = true;
                }
                else if (confirmed == JOptionPane.NO_OPTION) {
                    msg = "Is the recent dialog a totally new context? (if yes, rule is added to the root)";
                    confirmed = JOptionPane.showConfirmDialog(this,
                            msg, "New context?",
                            JOptionPane.YES_NO_CANCEL_OPTION
                    );

                    if (confirmed == JOptionPane.YES_OPTION) {
                        generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
                        rulePositionDetermined = true;
                    } 
                    else if (confirmed == JOptionPane.NO_OPTION) {
                        InferenceReferenceSelectionGUI.execute(KADialogInstance);  // add to rule to an arbitrary context..   
                        rulePositionDetermined = true;
                    }
                }
            }
            else if (!rulePositionDetermined) {
                generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
                rulePositionDetermined = true;
            }
            
            
            

            /* ************************************************************************************************************/
           
            // If we had a previous dialog that had a valid response, ask the user if this is the context to add the new rule
            // DPH added getDoesValidStackExist to condition
//            if (prevDialogInstance!=null && Main.workbench.getStackedInferenceResult().getDoesValidStackExist()){
//                //System.out.println("checkDialog: my previd is " + prevDialogInstance.toString());
//
//                stackId = prevDialogInstance.getStackId(); 
//                //int stackId = prevDialogInstance.getStackId(); 
//                Logger.info("Stack id for KA is:" + stackId);
//                  
//                applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
//                //RuleSet applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
//                inferencedRule = applyingInferenceResult.getLastRule();
//                //Rule inferencedRule = applyingInferenceResult.getLastRule();
//                Logger.info("inferenced Rule is:" + inferencedRule.getRuleId());
//                 currentContextRuleSet = inferencedRule.getPathRuleSet();
//                //RuleSet currentContextRuleSet = inferencedRule.getPathRuleSet();
//                 dialogStr = prevDialogInstance.getDialogStr();
//                //String dialogStr = prevDialogInstance.getDialogStr();
//                Logger.info("dialogStr is:" + dialogStr);
//
//                if(dialogStr.equals("")){
//                    dialogStr = "[N/A]";
//                }
//                toStringConclusion = currentContextRuleSet.toStringOnlyConclusion();
//                //String toStringConclusion = currentContextRuleSet.toStringOnlyConclusion();
//                toStringConclusion = toStringConclusion.replace("noResponse^^", "[N/A]");
//                
//                 actualConclusionShown = SystemDialogInstance.getParsedDialogStrForKA(toStringConclusion);
//                //String actualConclusionShown = SystemDialogInstance.getParsedDialogStrForKA(toStringConclusion);
//                 actualRecentDialogs = SystemDialogInstance.getParsedDialogStrForKA(dialogStr);
//                //String actualRecentDialogs = SystemDialogInstance.getParsedDialogStrForKA(dialogStr);
//                
//                 temp1 = WordUtils.wrap(actualConclusionShown.replace("^^", ""), 90);
//                //String temp1 = WordUtils.wrap(actualConclusionShown.replace("^^", ""), 90);
//                 maxTemp1 = temp1.substring(0, Integer.min(540, temp1.length()));
//                //String maxTemp1 = temp1.substring(0, Integer.min(540, temp1.length()));
//                 temp2 = WordUtils.wrap(actualRecentDialogs.replace("^^", ""), 90);
//                //String temp2 = WordUtils.wrap(actualRecentDialogs.replace("^^", ""), 90);
//                 maxTemp2 = temp2.substring(0, Integer.min(540, temp2.length()));
//                //String maxTemp2 = temp2.substring(0, Integer.min(540, temp2.length()));
//                
//                 
//                
//                msg = "Is the recent dialog related to the last response?\n\n"
//                        + "Current Context: " + temp1 +  "\n\n"
//                        + "Recent Answer:\n" +  temp2 + "\n";
//                
//                confirmed = JOptionPane.showConfirmDialog(this,
//                        msg, "Related to the current context?",
//                        JOptionPane.YES_NO_CANCEL_OPTION);
//                
//                //Close if user confirmed
//                if (confirmed == JOptionPane.YES_OPTION) {
//                    generateCaseForKnowledgeAcquisition(KADialogInstance, "continue");                    
//                } 
//                else if (confirmed == JOptionPane.NO_OPTION) {
//                    // The user doesn't want to add the new rule to the previous context
//                    String msg2 = "Is the recent dialog a totally new context? (if yes, rule is added to the root)";
//                    int confirmed2 = JOptionPane.showConfirmDialog(this,
//                            msg2, "New context?",
//                            JOptionPane.YES_NO_CANCEL_OPTION);
//                    
//                    //Close if user confirmed
//                    if (confirmed2 == JOptionPane.YES_OPTION) {
//                        // add the rule at the root level
//                        generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
//                        
//                    } else if (confirmed2 == JOptionPane.NO_OPTION) {
//                        // add the rule at any arbitray context (user selects).
//                        InferenceReferenceSelectionGUI.execute(KADialogInstance);                 
//                    }
//                }
//                
//                // Previous dialog's result was not understood, so rule added to root
//            } else {
//                Logger.info("Previous dialog's result was not understood, so rule added to root1");
//                generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
//            }
            
            // No rules fired from previous dialog, so add rule to root
        } else if (!rulePositionDetermined) {
            Logger.info("No rules fired from previous dialog, so add rule to root2");
            generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
        }
    }
    
    private String[] getContextConclusionStrings(RuleSet applyingInferenceResult, String dialogString) {
        String[] response = new String[2];
        String toStringConclusion;
        String dialogStr = dialogString;
        String dbNoResultText = "noResponse";
        String actualConclusionShown;
        String actualRecentDialogs;
        String temp1;
        String maxTemp1;
        String temp2;
        String maxTemp2;
        
        ContextVariable dbNoResponseVar = DialogMain.globalContextVariableManager.getSystemContextVariables().get("@SYSTEMemptyDBResult");
        if (dbNoResponseVar != null)
            dbNoResultText = dbNoResponseVar.getSingleVariableValue();
        
        Logger.info("immediate: inference result dialog is:" + dialogStr);
        toStringConclusion = "";
        for (Rule aRule: applyingInferenceResult.getBase().values()) {
            if (toStringConclusion.isEmpty())
                toStringConclusion = aRule.getConclusion().toString();
            else
                toStringConclusion += "\n" + aRule.getConclusion().toString();
        }

        if(dialogStr.equals("")){
            dialogStr = "[N/A]";
        }

        toStringConclusion = toStringConclusion.replace(dbNoResultText + "^^", "[N/A]");

        actualConclusionShown = SystemDialogInstance.getParsedDialogStrForKA(toStringConclusion);
        actualRecentDialogs = SystemDialogInstance.getParsedDialogStrForKA(dialogStr);

        temp1 = WordUtils.wrap(actualConclusionShown.replace("^^", ""), 90);
        maxTemp1 = temp1.substring(0, Integer.min(540, temp1.length()));
        temp2 = WordUtils.wrap(actualRecentDialogs.replace("^^", ""), 90);
        maxTemp2 = temp2.substring(0, Integer.min(540, temp2.length())); 
        
        response[0] = maxTemp1;
        response[1] = maxTemp2;
        
        return response;
    }
    
//    private void determineNewRuleLocation(IDialogInstance KADialogInstance) {
//        Logger.info("THE DIALOG ID IS " + KADialogInstance.getDialogId());
//        int dialogSequence = getDialogLocationInHistory(KADialogInstance);
//        Logger.info("The dialog sequence number is " + dialogSequence);
//        
//        //if(dialogSequence >= 2){
//        //if(dialogSequence >= 1){
//        //if(KADialogInstance.getDialogId() >= 2){
//        if(true){
//            IDialogInstance prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(KADialogInstance.getDialogId());
//            
//            if (prevDialogInstance == null)
//                 Logger.info("prevDialogInstance is null..");
//            else                
//                Logger.info("Previous ID is: " + prevDialogInstance.getDialogId());
//            
//            //DEAR ME - FOLLOW PATTERN IN 1654 of KnAcSe.java?
//            // TRY TO GET PREVIOUS, IF NULL THEN MODIFY ELSE 342 TO JSUT USE CURRENT SYS REPOSNSE STACK?
//            
//           
//            // If we had a previous dialog that had a valid response, ask the user if this is the context to add the new rule
//            // DPH added getDoesValidStackExist to condition
//            if (prevDialogInstance!=null && Main.workbench.getStackedInferenceResult().getDoesValidStackExist()){
//                //System.out.println("checkDialog: my previd is " + prevDialogInstance.toString());
//
//                int stackId = prevDialogInstance.getStackId(); 
//                Logger.info("Stack id for KA is:" + stackId);
//                  
//                RuleSet applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
//                Rule inferencedRule = applyingInferenceResult.getLastRule();
//                Logger.info("inferenced Rule is:" + inferencedRule.getRuleId());
//                RuleSet currentContextRuleSet = inferencedRule.getPathRuleSet();
//                String dialogStr = prevDialogInstance.getDialogStr();
//                Logger.info("dialogStr is:" + dialogStr);
//
//                if(dialogStr.equals("")){
//                    dialogStr = "[N/A]";
//                }
//                String toStringConclusion = currentContextRuleSet.toStringOnlyConclusion();
//                toStringConclusion = toStringConclusion.replace("noResponse^^", "[N/A]");
//                
//                String actualConclusionShown = SystemDialogInstance.getParsedDialogStrForKA(toStringConclusion);
//                String actualRecentDialogs = SystemDialogInstance.getParsedDialogStrForKA(dialogStr);
//                
//                String temp1 = WordUtils.wrap(actualConclusionShown.replace("^^", ""), 90);
//                String maxTemp1 = temp1.substring(0, Integer.min(540, temp1.length()));
//                String temp2 = WordUtils.wrap(actualRecentDialogs.replace("^^", ""), 90);
//                String maxTemp2 = temp2.substring(0, Integer.min(540, temp2.length()));
//                
//                 
//                
//                String msg = "Is the recent dialog related to the last response?\n\n"
//                        + "Current Context: " + temp1 +  "\n\n"
//                        + "Recent Answer:\n" +  temp2 + "\n";
//                
//                int confirmed = JOptionPane.showConfirmDialog(this,
//                        msg, "Related to the current context?",
//                        JOptionPane.YES_NO_CANCEL_OPTION);
//                
//                //Close if user confirmed
//                if (confirmed == JOptionPane.YES_OPTION) {
//                    generateCaseForKnowledgeAcquisition(KADialogInstance, "continue");                    
//                } 
//                else if (confirmed == JOptionPane.NO_OPTION) {
//                    // The user doesn't want to add the new rule to the previous context
//                    String msg2 = "Is the recent dialog a totally new context? (if yes, rule is added to the root)";
//                    int confirmed2 = JOptionPane.showConfirmDialog(this,
//                            msg2, "New context?",
//                            JOptionPane.YES_NO_CANCEL_OPTION);
//                    
//                    //Close if user confirmed
//                    if (confirmed2 == JOptionPane.YES_OPTION) {
//                        // add the rule at the root level
//                        generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
//                        
//                    } else if (confirmed2 == JOptionPane.NO_OPTION) {
//                        // add the rule at any arbitray context (user selects).
//                        InferenceReferenceSelectionGUI.execute(KADialogInstance);                 
//                    }
//                }
//                
//                // Previous dialog's result was not understood, so rule added to root
//            } else {
//                Logger.info("Previous dialog's result was not understood, so rule added to root1");
//                generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
//            }
//            
//            // No rules fired from previous dialog, so add rule to root
//        } else {
//            Logger.info("No rules fired from previous dialog, so add rule to root2");
//            generateCaseForKnowledgeAcquisition(KADialogInstance, "new");
//        }
//    }
    
   
    /**
     *
     */
    private void generateCaseForKnowledgeAcquisition(IDialogInstance selectedDialog, String mode){
        // DEAR ME, MAKE SURE TO LOOK AT processSelectedRuleSet in InferenceReferenceSelectionGUI if updating
        Logger.info("generateCaseForKnowledgeAcquisition - mode set to " + mode);
        
        IDialogInstance recentDialog = null;
        boolean isRuleFired = false;
        int caseId =0;
        int stackId = 0;
        boolean isStopping = false;
                
        // inference result
        RuleSet applyingInferenceResult = new RuleSet();
        
        // root rule for gui
        Rule rootRule;
        
        // setting recent Dialog, caseId and stackId
        caseId = selectedDialog.getGeneratedCaseId();

        Logger.info("generateCaseForKnowledgeAcquisition caseId is: " + caseId);
        
        SystemDialogInstance aSystemDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId);
        
        Logger.info("generateCaseForKnowledgeAcquisition aSystemDialogInstance is: " + aSystemDialogInstance.toString());
        isRuleFired = aSystemDialogInstance.getIsRuleFired();
        Logger.info("generateCaseForKnowledgeAcquisition isRuleFired is: " + isRuleFired);
        
        stackId = aSystemDialogInstance.getStackId();
        Logger.info("generateCaseForKnowledgeAcquisition initial stackId is: " + stackId);
        recentDialog = selectedDialog;
        
        Rule inferencedRule;
        
        switch(mode){
            // I don't use this case as far as I can tell... DPH May 2019
            /*case "refine":
                
                if(isRuleFired){
                    // if processing is valid (if there is inference result)
                    applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                    
                    Rule inferencedRule = applyingInferenceResult.getLastRule();
                    RuleSet rulePathRuleSet =inferencedRule.getPathRuleSet();
                    
                    rootRule = rulePathRuleSet.getRootRule();
                    
                } else {
                    // if processing is not valid there is no inference result
                    rootRule = Main.KB.getRootRule();
                    applyingInferenceResult.addRule(rootRule);
                }
                
                break;
            */    
            case "stop":              
                Logger.info("generateCaseForKnowledgeAcquisition - mode is: stop");

                if(isRuleFired){
                    // if processing is valid (if there is inference result)
                    applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                    
                    //Rule inferencedRule = applyingInferenceResult.getLastRule();
                    //RuleSet rulePathRuleSet =inferencedRule.getPathRuleSet();
                    
                    //rootRule = rulePathRuleSet.getRootRule();
                    
                } else {
                    // if processing is not valid there is no inference result
                    rootRule = Main.KB.getRootRule();
                    applyingInferenceResult.addRule(rootRule);
                }
                
                isStopping = true;
                
                break;
            case "immediate" :
                Logger.info("generateCaseForKnowledgeAcquisition - mode is: immediate");

                applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
                inferencedRule = applyingInferenceResult.getLastRule(); // wpuld be root if no previous
                for (Rule aRule: applyingInferenceResult.getBase().values()) {
                    Logger.info("      immediate - generateCaseForKnowledgeAcquisition - a rule in appyingInferenceResult is: " + aRule.getRuleId());
                }
                Logger.info("   immediate - generateCaseForKnowledgeAcquisition inferencedRule (last one) is:" + inferencedRule.getRuleId());
                
                break;
            case "continue":
                Logger.info("generateCaseForKnowledgeAcquisition - mode is: continue");

                //if (stackId == 0) {
                    // trying to fix a bug when the stackId shouldn't be 0!  It should refer to the last valid result with fired rules 
                    // if we're continuing!
                    //if(selectedDialog.getDialogId() >= 2) { // can't do this too early! and it's incorrect, dialogIDs are additive across all users so sequence is out
                        //IDialogInstance prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(selectedDialog.getDialogId());
                        //if (prevDialogInstance != null) {
                            //stackId = prevDialogInstance.getStackId(); 
                            //Logger.info("Stack id for continue KA is now:" + stackId);
                        //}
                    //}
                //}
                
                /* DAVE May 2019 modified all of the following code, uncomment to revert..
                applyingInferenceResult = Main.workbench.getPreviousStackedInferenceResult(stackId);
                Logger.info("stackId before:" + stackId);
                stackId = Main.workbench.getPreviousStackedInferenceResultKeyId(stackId);
                Logger.info("stackId after:" + stackId);

                Rule inferencedRule = applyingInferenceResult.getLastRule();
                Logger.info("inferencedRule is:" + inferencedRule.getRuleId());
                RuleSet rulePathRuleSet =inferencedRule.getPathRuleSet();              
                rootRule = rulePathRuleSet.getRootRule();
                
                break;
                */
                
                // get the last dialog that had fired rules as an inference result
                IDialogInstance prevDialogInstance =  DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getPreviousRuleFiredDialoginstanceById(selectedDialog.getDialogId());
                if (prevDialogInstance != null) {
                    Logger.info("   continue - generateCaseForKnowledgeAcquisition prevDialogInstance is non-null");
                    Logger.info("   continue  - generateCaseForKnowledgeAcquisition - dialog is " + prevDialogInstance.toString());
                    stackId = prevDialogInstance.getStackId(); 
                    Logger.info("   continue - generateCaseForKnowledgeAcquisition Stack id for continue KA is now:" + stackId);                 
                }
                applyingInferenceResult = Main.workbench.getPreviousStackedInferenceResult(stackId);  // this returns a new root-centred ruleSet if there was no previous..
                inferencedRule = applyingInferenceResult.getLastRule(); // wpuld be root if no previous
                for (Rule aRule: applyingInferenceResult.getBase().values()) {
                    Logger.info("      continue - generateCaseForKnowledgeAcquisition - a rule in appyingInferenceResult is: " + aRule.getRuleId());
                }
                Logger.info("   continue - generateCaseForKnowledgeAcquisition inferencedRule (last one) is:" + inferencedRule.getRuleId());
                break;
                
            case "new": 
                Logger.info("    generateCaseForKnowledgeAcquisition - mode is: new");

                rootRule = Main.KB.getRootRule();
                applyingInferenceResult.addRule(rootRule);
                for (Rule aRule: applyingInferenceResult.getBase().values()) {
                    Logger.info("new mode - applyingInferenceResult contains rule: " + aRule.getRuleId());
                }
                
                break;
        }
        
        //set inference result for ka
        Logger.info("Setting applyingInferenceResult in workbench");
        Main.workbench.setInferenceResult(applyingInferenceResult);
        for (Rule aRule: ((RuleSet)Main.workbench.getInferenceResult()).getBase().values()) {
             Logger.info("generateCaseForKnowledgeAcquisition VERIFY workbench inference - applyingInferenceResult contains rule: " + aRule.getRuleId());
        }
        
        // Case generation
        String historyInputStr = "";
        String recentInputStr = "";
        String eventType = "";
        String eventValue = "";
        
        DialogCase selectedCase = (DialogCase) Main.allCaseSet.getCaseById(caseId);
        
        LinkedHashMap<String, Value> values = selectedCase.getValues();
        
        DialogMain.dicConverter.setDictionary(DialogMain.dictionary);
        
        
        switch(recentDialog.getDialogTypeCode()){
            case DialogInstance.USER_TYPE:
                
                recentInputStr=recentDialog.getDialogStr();
                Logger.info("Recent String is: " + recentInputStr);
                
                recentInputStr = DialogMain.dicConverter.convertTermFromDic(recentInputStr,true);
                Logger.info("DIC CONVERTED: to: " + recentInputStr);

                
                break;
                
            case DialogInstance.EVENT_TYPE:
                
                eventType = recentDialog.getEventInstance().getEventType();
                eventValue = recentDialog.getEventInstance().getEventValue();
                
                break;
        }
        historyInputStr = "";
        
        // put them in the values array
        values.replace("History", new Value(ValueType.TEXT, historyInputStr));
        values.replace("Recent", new Value(ValueType.TEXT, recentInputStr));
        values.replace("EventType", new Value(ValueType.TEXT, eventType));
        values.replace("EventValue", new Value(ValueType.TEXT, eventValue));
        
        // generate new case by cloning case
        DialogCase newCase = DialogCaseGenerator.generateCase(selectedCase.getInputDialogInstance(), selectedCase.getValues(),false);
        
        newCase.setInputDialogInstance(recentDialog);

        //insert new case
        try {
            Logger.info("I'm calling insertCase: " + newCase.getCaseId() + " value: " + newCase);
            DialogCaseArchiveModule.insertCase(newCase);

        } catch (FileNotFoundException ex) {
            Logger.info("File not found");
        }
        
        
        Logger.info("Context (rule) chosen for KA is " + applyingInferenceResult.getLastRule().getRuleId());
        DialogMain.getDialogUserList().setCurrentKAInferenceResult(applyingInferenceResult);
        
       // AddRuleGUI.execute(mode, newCase, stackId);
        // DPH MAY 2019
        Logger.info("Calling AddRuleGUI execute..");
        AddRuleGUI.execute("modify", newCase, stackId);  // this one..
    }
    
    /**
     *
     */
    public void knowledgeAcquisition() {                                         
        String currentSelectedUsername = userListBox.getSelectedValue();
        int currentSelectedUserId = DialogMain.getDialogUserList().getIndexFromUsername(currentSelectedUsername);

        synchronized (syncLock) {
            Logger.info("Start of KA..");
            DialogMain.getDialogUserList().setCurrentIndex(currentSelectedUserId);

            if(DialogMain.getDialogUserList().getCurrentDialogRepository().getSize()!=0){

                SystemDialogInstance systemDialogInstance = (SystemDialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogRepository().getMostRecentDialogInstance();
                int caseId = systemDialogInstance.getDerivedCaseId();
                IDialogInstance inputDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogInstanceByGeneratedCaseId(caseId);
                if(inputDialogInstance.getDialogTypeCode()==DialogInstance.USER_TYPE){

                    String msg = "";
                    if(systemDialogInstance.getIsRuleFired()){
                        msg = "Do you want to add new knowledge?";
                        int confirmed = JOptionPane.showConfirmDialog(this,
                            msg, "New Knowledge Acquisition?",
                            JOptionPane.YES_NO_OPTION);

                        //Close if user confirmed
                        if (confirmed == JOptionPane.YES_OPTION)
                        {
                            determineNewRuleLocation(inputDialogInstance);
                        }
                    } else {
                        msg = "The system didn't understand the previous dialog. Do you want to add new knowledge?";
                        int confirmed = JOptionPane.showConfirmDialog(this,
                            msg, "New Knowledge Acquisition?",
                            JOptionPane.YES_NO_OPTION);

                        //Close if user confirmed
                        if (confirmed == JOptionPane.YES_OPTION)
                        {
                            determineNewRuleLocation(inputDialogInstance);
                        }
                    }
                } 
            } else {
                showMessageDialog(null, "You haven't say anything. Please say something before you add new knowledge.");
            }
        Logger.info("End of KA..");

        }

    } 
    
    private void updateChooseContextDialogList(){
        String user = userListBox.getSelectedValue();
        if (user != null && !user.isEmpty()) {
                   
            showConversationHistoryUserLabel.setText(user + " conversation history");

            IDialogInstance[] dialogHistoryArray = DialogMain.getDialogUserList().getDialogUser(user).getDialogRepository().getAllDialogsReverseOrder().toArray();
            DefaultListModel currentStackListModel = new DefaultListModel();
            int caseId;
            int stackId;
            ArrayList<Integer> stackIndex = new ArrayList<>();
            ArrayList<String> ruleNumbers = new ArrayList<>();


            currentContextStackList.setModel(currentStackListModel);
            
            // get the whole number of stacked results..
            
            for (int stackKey: DialogMain.getDialogUserList().getDialogUser(user).getMCRDRStackResultSet().getBaseSet().keySet()) {
                //int lastRuleId = DialogMain.getDialogUserList().getDialogUser(user).getMCRDRStackResultSet().getBaseSet().get(stackKey).getInferenceResult().getLastRule().getRuleId();
                String inferencedRuleList = "";
                for (Rule aRule : DialogMain.getDialogUserList().getDialogUser(user).getMCRDRStackResultSet().getBaseSet().get(stackKey).getInferenceResult().getBase().values()) {
                    if (!inferencedRuleList.isEmpty())
                        inferencedRuleList += ",";                  
                    inferencedRuleList += "R" + aRule.getRuleId();
                }
                    
                stackIndex.add(stackKey);
                ruleNumbers.add(inferencedRuleList);
                
                if (inferencedRuleList.equals("R0"))
                    currentStackListModel.add(0,stackKey + " (R0) (S0)");
                else {
                    currentStackListModel.add(0,stackKey + " (" + inferencedRuleList + ") (*)");  // mark all stack entries as sourced from unknown for now..
                }

            }
            Collections.reverse(stackIndex);
            Collections.reverse(ruleNumbers);

            
            // update the currentStackListModel entries with more information (last rule, stack location where this particular context started from)
            for (IDialogInstance aDialogInstance : dialogHistoryArray)
            {
                if (aDialogInstance.getDialogTypeCode() == DialogInstance.SYSTEM_TYPE) {
                    //caseId = aDialogInstance.getDerivedCaseId();
                    stackId = aDialogInstance.getStackId();
                    RuleSet lastRuleSet = getLastInferencedRuleSet(stackId, user);
                    if(lastRuleSet!=null){
                        //Rule inferencedRule = lastRuleSet.getLastRule();
                        String inferencedRuleList = "";
                        for (Rule aRule : lastRuleSet.getBase().values()) {
                            if (!inferencedRuleList.isEmpty())
                                inferencedRuleList += ",";                  
                            inferencedRuleList += "R" + aRule.getRuleId();
                        }
                        int stackKeyLocation = stackIndex.indexOf(stackId);
                        if (stackKeyLocation >= 0) {
                            currentStackListModel.remove(stackKeyLocation);
                            currentStackListModel.add(stackKeyLocation,stackId + " (" + inferencedRuleList + ") (S" + DialogMain.getDialogUserList().getDialogUser(user).getSatisfiedBystackedMCRDRInferenceResultStackId(stackId) + ")");
                        }
                    }
                }
            }


            chooseContextDialogList.setModel(new javax.swing.AbstractListModel() {
                    IDialogInstance[] dialogs = dialogHistoryArray;
                    @Override
                    public int getSize() { return dialogs.length; }
                    @Override
                    public Object getElementAt(int i) { return dialogs[i]; }
            });                    
        }
        
        if (chooseContextDialogList.getModel().getSize() > 0)
            chooseContextDialogList.setSelectedIndex(0);
        
    }
    
    private RuleSet getLastInferencedRuleSet(int stackId, String user) {
        RuleSet selectedInferenceResult = null;
    
        if (!user.isEmpty()) {       
            selectedInferenceResult = DialogMain.getDialogUserList().getDialogUser(user).getMCRDRStackResultSet().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
        }
        return selectedInferenceResult;
    }
    
    private void updateContextVariableGUI(int stackId, String user) {
        String [] columns = {"Stack","Variable","Value"};
        String [] systemColumns = {"RuleId", "Variable","Value"};
        //userContextVariablesTable.setModel(new DefaultTableModel(DialogMain.globalContextVariableManager.getUserContextVariablesList(), columns));
        if (user != null && !user.isEmpty()) {
            int userIndex = DialogMain.userInterfaceController.getUserId(user);
            userContextVariablesTable.setModel(new DefaultTableModel(DialogMain.getDialogUserList().getDialogUser(userIndex).getContextVariablesForList(stackId), columns));
            userContextVariablesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            userContextVariablesTable.getTableHeader().setReorderingAllowed(false);
            if(userContextVariablesTable.getColumnModel() != null) {
                userContextVariablesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
                userContextVariablesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
                userContextVariablesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
            }
            
            int ruleOverrideId = DialogMain.getDialogUserList().getDialogUser(userIndex).getLastInferencedRuleId();
            systemContextVariablesTable.setModel(new DefaultTableModel(DialogMain.getDialogUserList().getDialogUser(userIndex).getSystemContextVariablesForList(stackId), systemColumns));
            systemContextVariablesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            systemContextVariablesTable.getTableHeader().setReorderingAllowed(false);
            if(systemContextVariablesTable.getColumnModel() != null) {
                systemContextVariablesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
                systemContextVariablesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
                systemContextVariablesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
            }
        }
    }
    private void updateChooseContextRuleTree(int stackId, String user) {
        
        if (!user.isEmpty()) {
        
            RuleSet selectedInferenceResult = getLastInferencedRuleSet(stackId, user);

            Rule rootRule = Main.KB.getRootRule();
            
            RuleSet totalInferredRuleSet = null;

            if(selectedInferenceResult!=null){
                for (Rule aRule: selectedInferenceResult.getBase().values()) {
                    if (totalInferredRuleSet == null)
                        totalInferredRuleSet = aRule.getPathRuleSet();
                    else
                        totalInferredRuleSet.combineRuleSet(aRule.getPathRuleSet());
                    
                    
                }
                rootRule = totalInferredRuleSet.getRootRule();

            }
            RuleTreeModel aModel = new RuleTreeModel(rootRule);

            showContextRuleTree.setModel(aModel);

            for (int i = 0; i < showContextRuleTree.getRowCount(); i++) {
                showContextRuleTree.expandRow(i);
            }
        }
    }  


    String preAndPostProcessInput(boolean preview, String input, String matchText, String replaceText, boolean regex, boolean wordOnly, boolean startInput, boolean endInput, boolean replace, boolean upper, boolean lower, boolean trim, boolean post) {
        String result = input;
        
        try {
            result = DialogMain.userInterfaceController.preAndPostProcessInput(preview, input, matchText, replaceText, regex, wordOnly, startInput, endInput, replace, upper, lower, trim, post);
        }

        catch (PatternSyntaxException p) {
            if (preview) {
                errorTextArea.setText(p.getDescription());
            }
        }
        catch (IndexOutOfBoundsException i) {
            if (preview) {
                errorTextArea.setText(i.getMessage());
            }
        }
        
        return result;
    }
    
   

    public static void execute() {   
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AdminJavaGUI gui = new AdminJavaGUI("AdminJavaGUI");
                DialogMain.addUserInterface(gui);

                gui.setVisible(false);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser = new javax.swing.JFileChooser();
        showContextFrame = new javax.swing.JFrame();
        jScrollPane4 = new javax.swing.JScrollPane();
        chooseContextDialogList = new javax.swing.JList();
        showContextCloseButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        showContextRuleTree = new javax.swing.JTree();
        showConversationHistoryUserLabel = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        showContextRefreshButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        currentContextStackList = new javax.swing.JList<>();
        jScrollPane7 = new javax.swing.JScrollPane();
        userContextVariablesTable = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        systemContextVariablesTable = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jMenuBar2 = new javax.swing.JMenuBar();
        windowsMenu1 = new javax.swing.JMenu();
        jLabel3 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        speechProcessorFrame = new javax.swing.JFrame();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        currentActionsList = new javax.swing.JList<>();
        deleteActionButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        wordMatchOnlyCheckBox = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        matchTextField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        previewTextField = new javax.swing.JTextField();
        previewButton = new javax.swing.JButton();
        addActionButton = new javax.swing.JButton();
        startMatchCheckBox = new javax.swing.JCheckBox();
        endMatchCheckBox = new javax.swing.JCheckBox();
        regexCheckBox = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        replaceRadioButton = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        replaceTextField = new javax.swing.JTextField();
        upperRadioButton = new javax.swing.JRadioButton();
        lowerRadioButton = new javax.swing.JRadioButton();
        trimRadioButton = new javax.swing.JRadioButton();
        jPanel9 = new javax.swing.JPanel();
        preprocessRadioButton = new javax.swing.JRadioButton();
        jLabel22 = new javax.swing.JLabel();
        postprocessRadioButton = new javax.swing.JRadioButton();
        saveActionButton = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        modifyActionButton = new javax.swing.JButton();
        cancelActionButton = new javax.swing.JButton();
        preprocessorRadioGroup = new javax.swing.ButtonGroup();
        showPreviewDialog = new javax.swing.JDialog();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        resultTextArea = new javax.swing.JTextArea();
        previewActionCloseButton = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        previewTextArea = new javax.swing.JTextArea();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        errorTextArea = new javax.swing.JTextArea();
        preOrPostButtonGroup = new javax.swing.ButtonGroup();
        setGreetingDialog = new javax.swing.JDialog();
        jPanel10 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        initialGreetingText = new javax.swing.JTextArea();
        initialGreetingSaveButton = new javax.swing.JButton();
        initialGreetingCancelButton = new javax.swing.JButton();
        setShortGreetingDialog = new javax.swing.JDialog();
        jPanel12 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        initialShortGreetingText = new javax.swing.JTextArea();
        initialShortGreetingSaveButton = new javax.swing.JButton();
        initialShortGreetingCancelButton = new javax.swing.JButton();
        KAButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userListBox = new javax.swing.JList<>();
        spawnManualUserButton = new javax.swing.JButton();
        userContextButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        mainConversationTextArea = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        databaseOnOffCheckbox = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        settingsMenu = new javax.swing.JMenu();
        viewKnowledgeBaseMenuItem = new javax.swing.JMenuItem();
        resetKnowledgeBaseMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        clearAllDialogsMenuItem = new javax.swing.JMenuItem();
        saveAllDialogsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        dictionaryManagementMenuItem = new javax.swing.JMenuItem();
        userContextVariableMenuItem = new javax.swing.JMenuItem();
        systemContextVariablesMenuItem = new javax.swing.JMenuItem();
        textProcessorMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        referenceDatabaseMenuItem = new javax.swing.JMenuItem();
        setInitialDialogResponseMenuItem = new javax.swing.JMenuItem();
        setInitialShortDialogResponseMenuItem = new javax.swing.JMenuItem();
        windowsMenu = new javax.swing.JMenu();

        jFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooser.setCurrentDirectory(null);
        jFileChooser.setDialogTitle("Save chat history");

        showContextFrame.setTitle("show inference contexts");
        showContextFrame.setMinimumSize(new java.awt.Dimension(1345, 625));
        showContextFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                showContextFrameformWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                showContextFrameWindowClosed(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                showContextFrameformWindowActivated(evt);
            }
        });

        chooseContextDialogList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        chooseContextDialogList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                chooseContextDialogListValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(chooseContextDialogList);

        showContextCloseButton.setText("close");
        showContextCloseButton.setAutoscrolls(true);
        showContextCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showContextCloseButtonActionPerformed(evt);
            }
        });

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Root");
        showContextRuleTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane5.setViewportView(showContextRuleTree);

        showConversationHistoryUserLabel.setText("Conversation History");

        jLabel16.setText("Inference - Last rule fired");

        showContextRefreshButton.setText("refresh");
        showContextRefreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showContextRefreshButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Current Stack");

        currentContextStackList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        currentContextStackList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                currentContextStackListValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(currentContextStackList);

        userContextVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Context", "Variable", "Value"
            }
        ));
        userContextVariablesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        userContextVariablesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        userContextVariablesTable.getTableHeader().setReorderingAllowed(false);
        if(userContextVariablesTable.getColumnModel() != null) {
            userContextVariablesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            userContextVariablesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            userContextVariablesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        }
        jScrollPane7.setViewportView(userContextVariablesTable);

        jLabel9.setText("Variables");

        systemContextVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        systemContextVariablesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        systemContextVariablesTable.getTableHeader().setReorderingAllowed(false);
        if(systemContextVariablesTable.getColumnModel() != null) {
            systemContextVariablesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            systemContextVariablesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            systemContextVariablesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        }
        jScrollPane3.setViewportView(systemContextVariablesTable);

        jLabel10.setText("System variables");

        jLabel12.setText("User variables");

        windowsMenu1.setText("Windows");
        jMenuBar2.add(windowsMenu1);

        showContextFrame.setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout showContextFrameLayout = new javax.swing.GroupLayout(showContextFrame.getContentPane());
        showContextFrame.getContentPane().setLayout(showContextFrameLayout);
        showContextFrameLayout.setHorizontalGroup(
            showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(showContextFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(showContextFrameLayout.createSequentialGroup()
                        .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(showContextCloseButton)
                            .addComponent(showConversationHistoryUserLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(showContextRefreshButton)
                    .addGroup(showContextFrameLayout.createSequentialGroup()
                        .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane7)
                            .addComponent(jScrollPane3)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12)
                            .addComponent(jLabel9))))
                .addContainerGap())
        );
        showContextFrameLayout.setVerticalGroup(
            showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, showContextFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showConversationHistoryUserLabel)
                    .addComponent(jLabel9)
                    .addComponent(jLabel16)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5)
                    .addComponent(jScrollPane6)
                    .addComponent(jScrollPane4)
                    .addGroup(showContextFrameLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(showContextFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showContextCloseButton)
                    .addComponent(showContextRefreshButton))
                .addContainerGap())
        );

        jLabel3.setText("jLabel3");

        jLabel11.setText("jLabel11");

        speechProcessorFrame.setMinimumSize(new java.awt.Dimension(670, 490));

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        currentActionsList.setModel(preAndPostProcessActionListModel);
        currentActionsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                currentActionsListValueChanged(evt);
            }
        });
        jScrollPane8.setViewportView(currentActionsList);

        deleteActionButton.setText("Delete Action");
        deleteActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionButtonActionPerformed(evt);
            }
        });

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        wordMatchOnlyCheckBox.setSelected(true);
        wordMatchOnlyCheckBox.setText("(w) word match only");
        wordMatchOnlyCheckBox.setToolTipText("Only match whole words, not substrings");
        wordMatchOnlyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordMatchOnlyCheckBoxActionPerformed(evt);
            }
        });

        jLabel13.setText("Match Text:");

        matchTextField.setToolTipText("If not using 'regex' option, the following characters need to be \"escaped\" (prefix with \\) so they are not interpreted as special meaning:  \\ ^ [ ] . $ { } * ( ) \\ + | ? < > ");

        jLabel17.setText("Preview text:");

        previewButton.setText("Preview Action");
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });

        addActionButton.setText("Add Action");
        addActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionButtonActionPerformed(evt);
            }
        });

        startMatchCheckBox.setText("(s) must be at start");
        startMatchCheckBox.setToolTipText("Match text must start at the beginning of the input");

        endMatchCheckBox.setText("(e) must be at end");
        endMatchCheckBox.setToolTipText("Match text must be at the end of the input");

        regexCheckBox.setText("(R) regex");
        regexCheckBox.setToolTipText("Only select this option if you are familiar with regular expressions and are typing a raw regular expression...");

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel14.setText("Actions:");

        preprocessorRadioGroup.add(replaceRadioButton);
        replaceRadioButton.setSelected(true);
        replaceRadioButton.setText("(r) Replace matched text");
        replaceRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceRadioButtonActionPerformed(evt);
            }
        });

        jLabel15.setText("Replace text:");

        preprocessorRadioGroup.add(upperRadioButton);
        upperRadioButton.setText("(u) UPPER case");

        preprocessorRadioGroup.add(lowerRadioButton);
        lowerRadioButton.setText("(l) lower case");
        lowerRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowerRadioButtonActionPerformed(evt);
            }
        });

        preprocessorRadioGroup.add(trimRadioButton);
        trimRadioButton.setText("(t) Trim");
        trimRadioButton.setToolTipText("Remove leading and trailing whitespace");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(replaceRadioButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(replaceTextField))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(upperRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lowerRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(trimRadioButton)))
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(replaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upperRadioButton)
                    .addComponent(lowerRadioButton)
                    .addComponent(trimRadioButton)))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        preOrPostButtonGroup.add(preprocessRadioButton);
        preprocessRadioButton.setSelected(true);
        preprocessRadioButton.setText("pre-process (speech input)");
        preprocessRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preprocessRadioButtonActionPerformed(evt);
            }
        });

        jLabel22.setText("Pre or Post processing");

        preOrPostButtonGroup.add(postprocessRadioButton);
        postprocessRadioButton.setText("post-process (speech output)");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(preprocessRadioButton)
                    .addComponent(postprocessRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preprocessRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(postprocessRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(previewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addActionButton))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(matchTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(1, 1, 1))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(regexCheckBox)
                            .addComponent(startMatchCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(endMatchCheckBox)
                            .addComponent(wordMatchOnlyCheckBox))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wordMatchOnlyCheckBox)
                    .addComponent(regexCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startMatchCheckBox)
                    .addComponent(endMatchCheckBox))
                .addGap(8, 8, 8)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(previewTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewButton)
                    .addComponent(addActionButton))
                .addContainerGap())
        );

        saveActionButton.setText("Save");
        saveActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionButtonActionPerformed(evt);
            }
        });

        jLabel18.setText("Current Actions:");

        modifyActionButton.setText("Modify Action");
        modifyActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyActionButtonActionPerformed(evt);
            }
        });

        cancelActionButton.setText("Cancel");
        cancelActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 10, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(deleteActionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifyActionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelActionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveActionButton)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveActionButton)
                    .addComponent(cancelActionButton)
                    .addComponent(deleteActionButton)
                    .addComponent(modifyActionButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout speechProcessorFrameLayout = new javax.swing.GroupLayout(speechProcessorFrame.getContentPane());
        speechProcessorFrame.getContentPane().setLayout(speechProcessorFrameLayout);
        speechProcessorFrameLayout.setHorizontalGroup(
            speechProcessorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speechProcessorFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        speechProcessorFrameLayout.setVerticalGroup(
            speechProcessorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speechProcessorFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        showPreviewDialog.setMinimumSize(new java.awt.Dimension(400, 450));

        jLabel19.setText("Preview Text:");

        jLabel20.setText("Result Text:");

        resultTextArea.setEditable(false);
        resultTextArea.setColumns(20);
        resultTextArea.setRows(5);
        jScrollPane9.setViewportView(resultTextArea);

        previewActionCloseButton.setText("Close");
        previewActionCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewActionCloseButtonActionPerformed(evt);
            }
        });

        previewTextArea.setEditable(false);
        previewTextArea.setColumns(20);
        previewTextArea.setRows(5);
        jScrollPane10.setViewportView(previewTextArea);

        jLabel21.setText("Regular expression errors:");

        errorTextArea.setColumns(20);
        errorTextArea.setRows(5);
        jScrollPane11.setViewportView(errorTextArea);

        javax.swing.GroupLayout showPreviewDialogLayout = new javax.swing.GroupLayout(showPreviewDialog.getContentPane());
        showPreviewDialog.getContentPane().setLayout(showPreviewDialogLayout);
        showPreviewDialogLayout.setHorizontalGroup(
            showPreviewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(showPreviewDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(showPreviewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addGroup(showPreviewDialogLayout.createSequentialGroup()
                        .addGroup(showPreviewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, showPreviewDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(previewActionCloseButton))
                    .addComponent(jScrollPane9)
                    .addComponent(jScrollPane11))
                .addContainerGap())
        );
        showPreviewDialogLayout.setVerticalGroup(
            showPreviewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(showPreviewDialogLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(previewActionCloseButton)
                .addContainerGap())
        );

        setGreetingDialog.setMinimumSize(new java.awt.Dimension(400, 300));

        jLabel23.setText("Specify initial dialog greeting");

        initialGreetingText.setColumns(20);
        initialGreetingText.setRows(5);
        jScrollPane12.setViewportView(initialGreetingText);

        initialGreetingSaveButton.setText("Save");
        initialGreetingSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialGreetingSaveButtonActionPerformed(evt);
            }
        });

        initialGreetingCancelButton.setText("Cancel");
        initialGreetingCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialGreetingCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(initialGreetingCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initialGreetingSaveButton)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialGreetingSaveButton)
                    .addComponent(initialGreetingCancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout setGreetingDialogLayout = new javax.swing.GroupLayout(setGreetingDialog.getContentPane());
        setGreetingDialog.getContentPane().setLayout(setGreetingDialogLayout);
        setGreetingDialogLayout.setHorizontalGroup(
            setGreetingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setGreetingDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        setGreetingDialogLayout.setVerticalGroup(
            setGreetingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setGreetingDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setShortGreetingDialog.setMinimumSize(new java.awt.Dimension(400, 300));

        jLabel25.setText("Specify initial dialog greeting");

        initialShortGreetingText.setColumns(20);
        initialShortGreetingText.setRows(5);
        jScrollPane13.setViewportView(initialShortGreetingText);

        initialShortGreetingSaveButton.setText("Save");
        initialShortGreetingSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialShortGreetingSaveButtonActionPerformed(evt);
            }
        });

        initialShortGreetingCancelButton.setText("Cancel");
        initialShortGreetingCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initialShortGreetingCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(initialShortGreetingCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initialShortGreetingSaveButton)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialShortGreetingSaveButton)
                    .addComponent(initialShortGreetingCancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout setShortGreetingDialogLayout = new javax.swing.GroupLayout(setShortGreetingDialog.getContentPane());
        setShortGreetingDialog.getContentPane().setLayout(setShortGreetingDialogLayout);
        setShortGreetingDialogLayout.setHorizontalGroup(
            setShortGreetingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setShortGreetingDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        setShortGreetingDialogLayout.setVerticalGroup(
            setShortGreetingDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setShortGreetingDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setTitle("Main Window");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        KAButton.setText("Knowledge Acquisition");
        KAButton.setEnabled(false);
        KAButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KAButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Users");

        userListBox.setModel(usersListModel);
        userListBox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        userListBox.setPreferredSize(new java.awt.Dimension(140, 0));
        userListBox.setSelectedIndex(0);
        userListBox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListBoxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userListBox);

        spawnManualUserButton.setText("New manual user");
        spawnManualUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spawnManualUserButtonActionPerformed(evt);
            }
        });

        userContextButton.setText("show user context");
        userContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userContextButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spawnManualUserButton)
                    .addComponent(userContextButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {spawnManualUserButton, userContextButton});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(3, 3, 3)
                .addComponent(spawnManualUserButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userContextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("User conversation history");

        clearButton.setText("Clear selected user history");
        clearButton.setEnabled(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        mainConversationTextArea.setColumns(20);
        mainConversationTextArea.setLineWrap(true);
        mainConversationTextArea.setRows(5);
        mainConversationTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(mainConversationTextArea);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(clearButton)
                        .addGap(0, 84, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(clearButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        databaseOnOffCheckbox.setSelected(true);
        databaseOnOffCheckbox.setText("database on/off");
        databaseOnOffCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseOnOffCheckboxActionPerformed(evt);
            }
        });

        jLabel7.setText("Reference Database");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(databaseOnOffCheckbox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databaseOnOffCheckbox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        settingsMenu.setText("Settings");

        viewKnowledgeBaseMenuItem.setText("View Knowledge Base");
        viewKnowledgeBaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewKnowledgeBaseMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(viewKnowledgeBaseMenuItem);

        resetKnowledgeBaseMenuItem.setText("Reset Knowledge Base");
        resetKnowledgeBaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetKBOptionActionPerformed(evt);
            }
        });
        settingsMenu.add(resetKnowledgeBaseMenuItem);
        settingsMenu.add(jSeparator1);

        clearAllDialogsMenuItem.setText("Clear All Dialogs");
        clearAllDialogsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllDialogsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(clearAllDialogsMenuItem);

        saveAllDialogsMenuItem.setText("Save All Dialogs");
        saveAllDialogsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllDialogsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(saveAllDialogsMenuItem);
        settingsMenu.add(jSeparator2);

        dictionaryManagementMenuItem.setText("Dictionary Management");
        dictionaryManagementMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dictionaryManagementMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(dictionaryManagementMenuItem);

        userContextVariableMenuItem.setText("User Context Variable Management ");
        userContextVariableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userContextVariableMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(userContextVariableMenuItem);

        systemContextVariablesMenuItem.setText("System Context Variable Management");
        systemContextVariablesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemContextVariablesMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(systemContextVariablesMenuItem);

        textProcessorMenuItem.setText("Speech Processor actions");
        textProcessorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textProcessorMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(textProcessorMenuItem);
        settingsMenu.add(jSeparator3);

        referenceDatabaseMenuItem.setText("Reference Database Settings");
        referenceDatabaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                referenceDatabaseMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(referenceDatabaseMenuItem);

        setInitialDialogResponseMenuItem.setText("Set Initial Dialog Response");
        setInitialDialogResponseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setInitialDialogResponseMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(setInitialDialogResponseMenuItem);

        setInitialShortDialogResponseMenuItem.setText("Set Initial Short Dialog Response");
        setInitialShortDialogResponseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setInitialShortDialogResponseMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(setInitialShortDialogResponseMenuItem);

        jMenuBar1.add(settingsMenu);

        windowsMenu.setText("Windows");
        jMenuBar1.add(windowsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(311, 311, 311)
                        .addComponent(KAButton))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(KAButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void KAButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KAButtonActionPerformed

        int selected = getGUIIndexOfSelectedUser();
        knowledgeAcquisition();  
    }//GEN-LAST:event_KAButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        if (userListBox.getSelectedValue() != null) {
            String user  = userListBox.getSelectedValue().toString();
            mainConversationTextArea.removeAll();
            mainConversationTextArea.setText("");
            DialogMain.getDialogUserList().getDialogUser(user).clearConversationHistory();
            DialogMain.getDialogUserList().setCurrentIndex(DialogMain.getDialogUserList().getIndexFromUsername(user));
            DialogMain.getDialogUserList().getCurrentDialogRepository().clearRepository();
            DialogMain.getDialogUserList().getCurrentDialogUser().getMCRDRStackResultSet().clearSet();
            //Main.workbench.setCurrentStackInferenceResult(DialogMain.getCurrentDialogUser().getMCRDRStackResultSet());
        }
    }//GEN-LAST:event_clearButtonActionPerformed

    private void spawnManualUserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spawnManualUserButtonActionPerformed
        ManualDialogGUI newManualUser = new ManualDialogGUI(this);
        manualUsersGUIList.add(newManualUser);
        newManualUser.setVisible(true);
    }//GEN-LAST:event_spawnManualUserButtonActionPerformed

    private int getSpawnedUserIndex(ManualDialogGUI user) {
        return manualUsersGUIList.indexOf(user);
    }
    
    /**
     *
     * @param user
     */
    public void removeSpawnedUser(ManualDialogGUI user){
        
        //String i = DialogMain.getIndexFromUsername(user.getUsername());
        String currentlySelectedUsername = getGUIValueOfSelectedUser();
        
        DialogMain.userInterfaceController.removeUser(user.getUsername());
        
        if (currentlySelectedUsername != null)
            if (currentlySelectedUsername.equals(user.getUsername())) {
                // we have to change the GUI display to select the (arbitrarily) first user
                // but only if we have a user as the current user is the one we have just deleted
                if (!DialogMain.getDialogUserList().isEmpty()) {
                    setSelectedGUIUser(DialogMain.getDialogUserList().getFirstDialogUser().getUsername());
                }

            }
    
        manualUsersGUIList.remove(getSpawnedUserIndex(user));
    }
    
    private void viewKnowledgeBaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewKnowledgeBaseMenuItemActionPerformed
                RuleViewerFrame.execute();
    }//GEN-LAST:event_viewKnowledgeBaseMenuItemActionPerformed

    private void resetKBOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetKBOptionActionPerformed
             
        int confirmed = JOptionPane.showConfirmDialog(this,
                "This will delete all rules and current dialogs. Do you want to initialise knowledge base? ", "Initialise knowledge base?",
                JOptionPane.YES_NO_CANCEL_OPTION);
        
        //Close if user confirmed
        if (confirmed == JOptionPane.YES_OPTION)
        {
            mainConversationTextArea.removeAll();
            mainConversationTextArea.setText("");
            
            Iterator iter = DialogMain.getDialogUserList().getBaseSet().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry me = (Map.Entry)iter.next();
                DialogUser aDialogUser = (DialogUser) me.getValue();
                aDialogUser.clearConversationHistory();
            }
            /*
            for (int i=0; i < conversationHistories.size(); i++) {
                conversationHistories.set(i, "");
            }   
            */
            DialogMain.userInterfaceController.clearAllDialogResults();
            
            DBCreation.initialise(Main.domain.getDomainName(), contextPath);
            
            try {
                DialogCaseArchiveModule.createTextFileWithCaseStructure(contextPath);
            } catch (Exception ex) {
                Logger.error(ex.getMessage());
            }
            
            DialogMain.initialiseSystem(contextPath,Main.domain.getDomainName(), Main.domain.getReasonerType(), Main.domain.getDescription(), DialogMain.defaultResponse);
            
            DomainLoader.inserDomainDetails(Main.domain.getDomainName(), Main.domain.getDescription(), Main.domain.getReasonerType());
            CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());
            
            RuleLoader.insertRuleConclusions(0, Main.KB.getRootRule().getConclusion());
            RuleLoader.insertRule(0, Main.KB.getRootRule(), 0);
            

            
            // generate initial stack inference instances for existing users in the interface list..
            iter = DialogMain.getDialogUserList().getBaseSet().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry me = (Map.Entry) iter.next();
                DialogUser aDialogUser = (DialogUser)me.getValue();
                aDialogUser.clearMCRDRStackResultSet();
                aDialogUser.addInitialMCRDRStackResultInstance();
                        
                //Main.workbench.addStackedInferenceResult();
            }
            /*
            for (String user: userList) {
                Main.workbench.addStackedInferenceResult();
            }
            */
                       
            
        }
    }//GEN-LAST:event_resetKBOptionActionPerformed

    private void clearAllDialogsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllDialogsMenuItemActionPerformed
        mainConversationTextArea.removeAll();
        mainConversationTextArea.setText("");
        Iterator iter = DialogMain.getDialogUserList().getBaseSet().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            DialogUser aDialogUser = (DialogUser) me.getValue();
            aDialogUser.clearConversationHistory();
        }
        /*
        for (int i=0; i < conversationHistories.size(); i++) {
            conversationHistories.set(i, "");
        }   
        */
        DialogMain.userInterfaceController.clearAllDialogResults();
    }//GEN-LAST:event_clearAllDialogsMenuItemActionPerformed

    private void saveAllDialogsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllDialogsMenuItemActionPerformed
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", new String[] {"txt"});
        jFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/domain/cases/"));
        jFileChooser.addChoosableFileFilter(filter);
        jFileChooser.setFileFilter(filter);
        int returnVal = jFileChooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                DialogCaseArchiveModule.createNewTextFileWithAllDialogRepositories(jFileChooser.getSelectedFile()+".txt");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(AdminJavaGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_saveAllDialogsMenuItemActionPerformed

    private void dictionaryManagementMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dictionaryManagementMenuItemActionPerformed
         DicManagerGUI.execute();
    }//GEN-LAST:event_dictionaryManagementMenuItemActionPerformed

    private void userListBoxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListBoxValueChanged
        if (userListBox.getSelectedValue() != null) {
            String user  = userListBox.getSelectedValue().toString();
            mainConversationTextArea.removeAll();
            mainConversationTextArea.setText(DialogMain.getDialogUserList().getDialogUser(user).getConversationHistory());
            DialogMain.getDialogUserList().setCurrentIndex(DialogMain.getDialogUserList().getIndexFromUsername(user));
            //Main.workbench.setCurrentStackInferenceResult(DialogMain.getCurrentDialogUser().getMCRDRStackResultSet());
        }
        else {
            mainConversationTextArea.removeAll();
            mainConversationTextArea.setText("");
        }

    }//GEN-LAST:event_userListBoxValueChanged

    private void referenceDatabaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_referenceDatabaseMenuItemActionPerformed
        ReferenceDatabaseSettingsGUI referenceDatabaseSettings = new ReferenceDatabaseSettingsGUI(this);
        referenceDatabaseSettings.setVisible(true);
    }//GEN-LAST:event_referenceDatabaseMenuItemActionPerformed

    private void databaseOnOffCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseOnOffCheckboxActionPerformed
        if (databaseOnOffCheckbox.isSelected()) {
            DBConnection.setDatabaseIsUsed(true);
            if (!DBConnection.connect(DialogMain.referenceDatabaseDetails, true)) {
                JOptionPane.showMessageDialog(this,"Reference database connection failed - check settings..");
                databaseOnOffCheckbox.setSelected(false);
                DBConnection.setDatabaseIsUsed(false);

            }
            else {
                JOptionPane.showMessageDialog(this,"Reference database connection succeeded.");
                // re-load matching terms in case we have matching terms sourced from the reference database.
                DBOperation.getMatchingTermList();
            }
        }
        else {
           Logger.info("databaseOnOffCheckbox was not selected..");
           DBConnection.setDatabaseIsUsed(false);
        }
    }//GEN-LAST:event_databaseOnOffCheckboxActionPerformed

    private void userContextVariableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userContextVariableMenuItemActionPerformed
        
        ContextVariableGUI context = new ContextVariableGUI("");
        context.parent = null;
        context.setVisible(true);
    }//GEN-LAST:event_userContextVariableMenuItemActionPerformed

    private void systemContextVariablesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemContextVariablesMenuItemActionPerformed
        SystemContextVariableGUI manager = new SystemContextVariableGUI();
        manager.setVisible(true);
    }//GEN-LAST:event_systemContextVariablesMenuItemActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated

        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    private void chooseContextDialogListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_chooseContextDialogListValueChanged
        IDialogInstance selectedDialog = (IDialogInstance) chooseContextDialogList.getSelectedValue();
        int caseId =0;
        boolean found = false;
        
        String user = userListBox.getSelectedValue();
     
        
        if (selectedDialog != null && !user.isEmpty()) {
            if(selectedDialog.getDialogTypeCode()==DialogInstance.USER_TYPE ||selectedDialog.getDialogTypeCode()==DialogInstance.EVENT_TYPE ){
                caseId = selectedDialog.getGeneratedCaseId();
            } else if(selectedDialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE ){
                caseId = selectedDialog.getDerivedCaseId();
            }
            

            int stackId = DialogMain.getDialogUserList().getDialogUser(user).getDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId).getStackId();

            DefaultListModel theListModel = (DefaultListModel)currentContextStackList.getModel();

            for (int count = 0; count < theListModel.size(); count++) {
                if ((Integer.parseInt(((String)theListModel.get(count)).split("\\s")[0])) == stackId) {
                    currentContextStackList.setSelectedIndex(count);
                    found = true;
                    break;
                }
            }
            if (!found)
                currentContextStackList.clearSelection();
           
            updateChooseContextRuleTree(stackId, user);
            updateContextVariableGUI(stackId, user);
        }
    }//GEN-LAST:event_chooseContextDialogListValueChanged

    private void showContextCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showContextCloseButtonActionPerformed

        showContextFrame.setVisible(false);
    }//GEN-LAST:event_showContextCloseButtonActionPerformed

    private void showContextFrameformWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_showContextFrameformWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_showContextFrameformWindowClosing

    private void showContextFrameformWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_showContextFrameformWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_showContextFrameformWindowActivated

    private void userContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userContextButtonActionPerformed
        updateChooseContextDialogList();
        showContextFrame.setVisible(true);
    }//GEN-LAST:event_userContextButtonActionPerformed

    private void showContextRefreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showContextRefreshButtonActionPerformed
       updateChooseContextDialogList();
    }//GEN-LAST:event_showContextRefreshButtonActionPerformed

    private void currentContextStackListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_currentContextStackListValueChanged
        String selectedStack = currentContextStackList.getSelectedValue();
        String user = userListBox.getSelectedValue();
        int stackId = 0;
        if (selectedStack != null && user != null) {
            stackId= Integer.parseInt(selectedStack.split("\\s")[0]);         
            updateChooseContextRuleTree(stackId, user); 
            updateContextVariableGUI(stackId, user);
        }
        
        boolean found = false;
        for (int i = 0; i < chooseContextDialogList.getModel().getSize(); i++) {
            IDialogInstance selectedDialog = (IDialogInstance) chooseContextDialogList.getModel().getElementAt(i);
        
        
            int caseId =0;
            if (selectedDialog != null && user != null) {
                if(selectedDialog.getDialogTypeCode()==DialogInstance.USER_TYPE ||selectedDialog.getDialogTypeCode()==DialogInstance.EVENT_TYPE ){
                    caseId = selectedDialog.getGeneratedCaseId();
                } else if(selectedDialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE ){
                    caseId = selectedDialog.getDerivedCaseId();
                }

                int dialogStackId = DialogMain.getDialogUserList().getDialogUser(user).getDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId).getStackId();
                if (dialogStackId == stackId) {
                    chooseContextDialogList.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
        }
        if (!found)
            chooseContextDialogList.clearSelection();
        
    }//GEN-LAST:event_currentContextStackListValueChanged

    private void replaceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_replaceRadioButtonActionPerformed

    private void wordMatchOnlyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordMatchOnlyCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_wordMatchOnlyCheckBoxActionPerformed

    private void lowerRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowerRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lowerRadioButtonActionPerformed

    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonActionPerformed
        
        String matchText = matchTextField.getText();
        String previewText = previewTextField.getText();
        boolean regex = regexCheckBox.isSelected();
        boolean wordOnly = wordMatchOnlyCheckBox.isSelected();
        String replaceText = replaceTextField.getText();
        boolean startOfInput = startMatchCheckBox.isSelected();
        boolean endOfInput = endMatchCheckBox.isSelected();
        boolean replaceOption = replaceRadioButton.isSelected();
        boolean upperOption = upperRadioButton.isSelected();
        boolean lowerOption = lowerRadioButton.isSelected();
        boolean trimOption = trimRadioButton.isSelected();
        boolean postOption = postprocessRadioButton.isSelected();
        previewTextArea.setText(previewText);
        
        errorTextArea.setText("");
        resultTextArea.setText(preAndPostProcessInput(true,previewText, matchText, replaceText, regex, wordOnly, startOfInput, endOfInput, replaceOption, upperOption, lowerOption,trimOption, postOption));
        
        showPreviewDialog.setVisible(true);
    }//GEN-LAST:event_previewButtonActionPerformed

    private void textProcessorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textProcessorMenuItemActionPerformed
        preAndPostProcessActionListModel = new DefaultListModel();
        currentActionsList.setModel(preAndPostProcessActionListModel);
        
        for (int anActionIndex = 0; anActionIndex < DialogMain.processorList.size(); anActionIndex++) {
            preAndPostProcessActionListModel.addElement(DialogMain.processorList.get(anActionIndex).copy());
        }
        
        speechProcessorFrame.setVisible(true);
    }//GEN-LAST:event_textProcessorMenuItemActionPerformed

    private void previewActionCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewActionCloseButtonActionPerformed
        showPreviewDialog.setVisible(false);
    }//GEN-LAST:event_previewActionCloseButtonActionPerformed

    private void addActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionButtonActionPerformed
        String matchText = matchTextField.getText();
        String previewText = previewTextField.getText();
        boolean regex = regexCheckBox.isSelected();
        boolean wordOnly = wordMatchOnlyCheckBox.isSelected();
        String replaceText = replaceTextField.getText();
        boolean startOfInput = startMatchCheckBox.isSelected();
        boolean endOfInput = endMatchCheckBox.isSelected();
        boolean replaceOption = replaceRadioButton.isSelected();
        boolean upperOption = upperRadioButton.isSelected();
        boolean lowerOption = lowerRadioButton.isSelected();
        boolean trimOption = trimRadioButton.isSelected();
        boolean isPostProcessorOption = postprocessRadioButton.isSelected();
        preAndPostProcessActionListModel.addElement(new PreAndPostProcessorAction(matchText,replaceText,regex, wordOnly, startOfInput, endOfInput, replaceOption, upperOption, lowerOption, trimOption,isPostProcessorOption));
    }//GEN-LAST:event_addActionButtonActionPerformed

    private void currentActionsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_currentActionsListValueChanged
        int selectedAction = currentActionsList.getSelectedIndex();
        if (selectedAction >= 0) {
            PreAndPostProcessorAction theAction = (PreAndPostProcessorAction) preAndPostProcessActionListModel.elementAt(selectedAction);
            matchTextField.setText(theAction.getMatchText());
            regexCheckBox.setSelected(theAction.getRegex());
            wordMatchOnlyCheckBox.setSelected(theAction.getWordOnly());
            replaceTextField.setText(theAction.getReplaceText());
            startMatchCheckBox.setSelected(theAction.getStartOfInput());
            endMatchCheckBox.setSelected(theAction.getEndOfInput());
            replaceRadioButton.setSelected(theAction.getReplaceOption());
            upperRadioButton.setSelected(theAction.getUpperOption());
            lowerRadioButton.setSelected(theAction.getLowerOption());
            trimRadioButton.setSelected(theAction.getTrimOption());
            postprocessRadioButton.setSelected(theAction.getPostOption());
            preprocessRadioButton.setSelected(!theAction.getPostOption());

        }
    }//GEN-LAST:event_currentActionsListValueChanged

    private void modifyActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyActionButtonActionPerformed
        int selectedAction = currentActionsList.getSelectedIndex();
        if (selectedAction >= 0) {
            PreAndPostProcessorAction theAction = (PreAndPostProcessorAction) preAndPostProcessActionListModel.elementAt(selectedAction);
            theAction.setMatchText(matchTextField.getText());
            theAction.setReplaceText(replaceTextField.getText());
            theAction.setRegex(regexCheckBox.isSelected());
            theAction.setWordOnly(wordMatchOnlyCheckBox.isSelected());
            theAction.setStartOfInput(startMatchCheckBox.isSelected());
            theAction.setEndOfInput(endMatchCheckBox.isSelected());
            theAction.setReplaceOption(replaceRadioButton.isSelected());
            theAction.setUpperOption(upperRadioButton.isSelected());
            theAction.setLowerOption(lowerRadioButton.isSelected());
            theAction.setTrimOption(trimRadioButton.isSelected());
            theAction.setPostOption(postprocessRadioButton.isSelected());

            /* update list display....*/
            preAndPostProcessActionListModel.setElementAt(theAction, selectedAction);
        }
        else {
            showMessageDialog(this, "Please select an action before trying to modify it..");
        }
    }//GEN-LAST:event_modifyActionButtonActionPerformed

    private void deleteActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionButtonActionPerformed
        int selectedAction = currentActionsList.getSelectedIndex();
        if (selectedAction >= 0) {
            preAndPostProcessActionListModel.removeElementAt(selectedAction);
        }
        else {
            showMessageDialog(this, "Please select an action before trying to delete it..");
        }
    }//GEN-LAST:event_deleteActionButtonActionPerformed

    private void saveActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionButtonActionPerformed
        
        DialogMain.processorList = new ArrayList<>();
        for (int anActionIndex = 0; anActionIndex < preAndPostProcessActionListModel.getSize(); anActionIndex++) {
            PreAndPostProcessorAction anAction = ((PreAndPostProcessorAction) preAndPostProcessActionListModel.get(anActionIndex)).copy();
            DialogMain.processorList.add(anAction);
            Logger.info("Adding preprocessor rule: " + anAction.getMatchText());
        }
        DBOperation.updatePreAndPostProcessorList(DialogMain.processorList);
        speechProcessorFrame.setVisible(false);
    }//GEN-LAST:event_saveActionButtonActionPerformed

    private void cancelActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionButtonActionPerformed
        preAndPostProcessActionListModel = new DefaultListModel();
        for (int anActionIndex = 0; anActionIndex < DialogMain.processorList.size(); anActionIndex++) {
            PreAndPostProcessorAction anAction = (PreAndPostProcessorAction) DialogMain.processorList.get(anActionIndex).copy();
            preAndPostProcessActionListModel.addElement(anAction);
        }
        speechProcessorFrame.setVisible(false);
    }//GEN-LAST:event_cancelActionButtonActionPerformed

    private void preprocessRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preprocessRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_preprocessRadioButtonActionPerformed

    private void initialGreetingSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialGreetingSaveButtonActionPerformed
        DialogMain.updateInitialGreeting(initialGreetingText.getText());
        setGreetingDialog.setVisible(false);
    }//GEN-LAST:event_initialGreetingSaveButtonActionPerformed

    private void initialGreetingCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialGreetingCancelButtonActionPerformed
        setGreetingDialog.setVisible(false);
    }//GEN-LAST:event_initialGreetingCancelButtonActionPerformed

    private void setInitialDialogResponseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setInitialDialogResponseMenuItemActionPerformed
        initialGreetingText.setText(DialogMain.initialGreeting);
        setGreetingDialog.setVisible(true);
        
    }//GEN-LAST:event_setInitialDialogResponseMenuItemActionPerformed

    private void showContextFrameWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_showContextFrameWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_showContextFrameWindowClosed

    private void initialShortGreetingSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialShortGreetingSaveButtonActionPerformed
        DialogMain.updateInitialShortGreeting(initialShortGreetingText.getText());
        setShortGreetingDialog.setVisible(false);
    }//GEN-LAST:event_initialShortGreetingSaveButtonActionPerformed

    private void initialShortGreetingCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initialShortGreetingCancelButtonActionPerformed
        setShortGreetingDialog.setVisible(false);
    }//GEN-LAST:event_initialShortGreetingCancelButtonActionPerformed

    private void setInitialShortDialogResponseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setInitialShortDialogResponseMenuItemActionPerformed
        initialShortGreetingText.setText(DialogMain.initialShortGreeting);
        setShortGreetingDialog.setVisible(true);
    }//GEN-LAST:event_setInitialShortDialogResponseMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminJavaGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminJavaGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminJavaGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminJavaGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AdminJavaGUI gui = new AdminJavaGUI("AdminJavaGUI");
                DialogMain.addUserInterface(gui);
                gui.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton KAButton;
    private javax.swing.JButton addActionButton;
    private javax.swing.JButton cancelActionButton;
    private javax.swing.JList chooseContextDialogList;
    private javax.swing.JMenuItem clearAllDialogsMenuItem;
    private javax.swing.JButton clearButton;
    private javax.swing.JList<String> currentActionsList;
    private javax.swing.JList<String> currentContextStackList;
    private javax.swing.JCheckBox databaseOnOffCheckbox;
    private javax.swing.JButton deleteActionButton;
    private javax.swing.JMenuItem dictionaryManagementMenuItem;
    private javax.swing.JCheckBox endMatchCheckBox;
    private javax.swing.JTextArea errorTextArea;
    private javax.swing.JButton initialGreetingCancelButton;
    private javax.swing.JButton initialGreetingSaveButton;
    private javax.swing.JTextArea initialGreetingText;
    private javax.swing.JButton initialShortGreetingCancelButton;
    private javax.swing.JButton initialShortGreetingSaveButton;
    private javax.swing.JTextArea initialShortGreetingText;
    private javax.swing.JFileChooser jFileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JRadioButton lowerRadioButton;
    private javax.swing.JTextArea mainConversationTextArea;
    private javax.swing.JTextField matchTextField;
    private javax.swing.JButton modifyActionButton;
    private javax.swing.JRadioButton postprocessRadioButton;
    private javax.swing.ButtonGroup preOrPostButtonGroup;
    private javax.swing.JRadioButton preprocessRadioButton;
    private javax.swing.ButtonGroup preprocessorRadioGroup;
    private javax.swing.JButton previewActionCloseButton;
    private javax.swing.JButton previewButton;
    private javax.swing.JTextArea previewTextArea;
    private javax.swing.JTextField previewTextField;
    private javax.swing.JMenuItem referenceDatabaseMenuItem;
    private javax.swing.JCheckBox regexCheckBox;
    private javax.swing.JRadioButton replaceRadioButton;
    private javax.swing.JTextField replaceTextField;
    private javax.swing.JMenuItem resetKnowledgeBaseMenuItem;
    private javax.swing.JTextArea resultTextArea;
    private javax.swing.JButton saveActionButton;
    private javax.swing.JMenuItem saveAllDialogsMenuItem;
    private javax.swing.JDialog setGreetingDialog;
    private javax.swing.JMenuItem setInitialDialogResponseMenuItem;
    private javax.swing.JMenuItem setInitialShortDialogResponseMenuItem;
    private javax.swing.JDialog setShortGreetingDialog;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JButton showContextCloseButton;
    private javax.swing.JFrame showContextFrame;
    private javax.swing.JButton showContextRefreshButton;
    private javax.swing.JTree showContextRuleTree;
    private javax.swing.JLabel showConversationHistoryUserLabel;
    private javax.swing.JDialog showPreviewDialog;
    private javax.swing.JButton spawnManualUserButton;
    private javax.swing.JFrame speechProcessorFrame;
    private javax.swing.JCheckBox startMatchCheckBox;
    private javax.swing.JMenuItem systemContextVariablesMenuItem;
    private javax.swing.JTable systemContextVariablesTable;
    private javax.swing.JMenuItem textProcessorMenuItem;
    private javax.swing.JRadioButton trimRadioButton;
    private javax.swing.JRadioButton upperRadioButton;
    private javax.swing.JButton userContextButton;
    private javax.swing.JMenuItem userContextVariableMenuItem;
    private javax.swing.JTable userContextVariablesTable;
    private javax.swing.JList<String> userListBox;
    private javax.swing.JMenuItem viewKnowledgeBaseMenuItem;
    private javax.swing.JMenu windowsMenu;
    private javax.swing.JMenu windowsMenu1;
    private javax.swing.JCheckBox wordMatchOnlyCheckBox;
    // End of variables declaration//GEN-END:variables
}

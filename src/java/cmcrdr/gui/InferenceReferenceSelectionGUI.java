/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.cases.DialogCase;
import cmcrdr.cases.DialogCaseArchiveModule;
import cmcrdr.cases.DialogCaseGenerator;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.DialogSet;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.dialog.SystemDialogInstance;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import rdr.apps.Main;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Rule;
import rdr.rules.RuleSet;
import rdr.rules.RuleTreeModel;

/**
 *
 * @author hchung
 */
public class InferenceReferenceSelectionGUI extends javax.swing.JFrame {
    
     private DialogSet storedDialogInputSet = new DialogSet();
     private static IDialogInstance selectedDialog;
    
    /**
     * Creates new form InferenceReferenceSelectionGUI
     */
    public InferenceReferenceSelectionGUI() {
        initComponents();
        updateChooseContextDialogList();
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
    }
    
    
    
    /**
     * 
     */
    
    private void updateChooseContextDialogList(){
        IDialogInstance[] dialogHistoryArray = DialogMain.getDialogUserList().getCurrentDialogRepository().getAllDialogsUntilGivenDialog(selectedDialog).toArray();
        
        chooseContextDialogList.setModel(new javax.swing.AbstractListModel() {
                IDialogInstance[] dialogs = dialogHistoryArray;
                @Override
                public int getSize() { return dialogs.length; }
                @Override
                public Object getElementAt(int i) { return dialogs[i]; }
        });
        chooseContextDialogList.setCellRenderer(new MyListCellRenderer());
        chooseContextDialogList.setSelectedValue(DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogBeforeGivenDialog(selectedDialog),true);
        Logger.info(DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogBeforeGivenDialog(selectedDialog).toString());
//        chooseContextDialogList.setSelectedIndex(selectedIndex);
    }
    
    
    
    /**
     * 
     * @param stackId 
     */
    private void updateChooseContextRuleTree(int stackId){
        RuleSet selectedInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
        
        // TODO it gets the last rule
         Rule rootRule = Main.KB.getRootRule();
         
        if(selectedInferenceResult!=null){
            Rule inferencedRule = selectedInferenceResult.getLastRule();
            RuleSet rulePathRuleSet =inferencedRule.getPathRuleSet();

            rootRule = rulePathRuleSet.getRootRule();
        }
        RuleTreeModel aModel = new RuleTreeModel(rootRule);
        chooseContextRuleTree.setModel(aModel);
        
        for (int i = 0; i < chooseContextRuleTree.getRowCount(); i++) {
            chooseContextRuleTree.expandRow(i);
        }
    }
    
    
    private void processSelectedRuleSet(IDialogInstance selectedReferDialog){
        int caseId = selectedDialog.getGeneratedCaseId();

        SystemDialogInstance aSystemDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId);

        int stackId = aSystemDialogInstance.getStackId();

        IDialogInstance recentDialog = selectedDialog;
        
        
        RuleSet applyingInferenceResult  = new RuleSet();
        
        stackId = selectedReferDialog.getStackId();

        applyingInferenceResult = Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult();

        Rule inferencedRule = applyingInferenceResult.getLastRule();
        RuleSet rulePathRuleSet =inferencedRule.getPathRuleSet();

        Rule rootRule = rulePathRuleSet.getRootRule();
        
        //set inference result for ka
        Main.workbench.setInferenceResult(applyingInferenceResult);

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

                recentInputStr = DialogMain.dicConverter.convertTermFromDic(recentInputStr,true);
                Logger.info("CONVERTING: to: " + recentInputStr);


                break;
                
            case DialogInstance.EVENT_TYPE:
                
                eventType = recentDialog.getEventInstance().getEventType();
                eventValue = recentDialog.getEventInstance().getEventValue();
                
                break;
        } 
        //historyInputStr = storedDialogInputSet.getAllDialogStringWithSlash();
        historyInputStr = storedDialogInputSet.getAllDialogStringWithSeparator();

        // put them in the values array
        values.replace("History", new Value(ValueType.TEXT, historyInputStr));
        values.replace("Recent", new Value(ValueType.TEXT, recentInputStr));
        values.replace("EventType", new Value(ValueType.TEXT, eventType));
        values.replace("EventValue", new Value(ValueType.TEXT, eventValue));

        // generate new case by cloning case
        DialogCase newCase = DialogCaseGenerator.generateCase(selectedCase.getInputDialogInstance(), selectedCase.getValues(),false);
        
        //set new case id
//        recentDialog.setGeneratedCaseId(newCase.getCaseId());
            
        newCase.setInputDialogInstance(recentDialog);
        

        //insert new case
        try {
            DialogCaseArchiveModule.insertCase(newCase);

        } catch (FileNotFoundException ex) {
            Logger.info("File not found");
        }
        

        Logger.info("Context chosen for KA is " + applyingInferenceResult.getLastRule().getRuleId());
        DialogMain.getDialogUserList().setCurrentKAInferenceResult(applyingInferenceResult);
        // need to set context variables if we're doing KA and the selected context isn't the same as the dialog's actual context
        //DialogMain.globalContextVariableManager.setContextVariablesForSpecificContext(DialogMain.getDialogUserList().getCurrentRecentContextVariables(), applyingInferenceResult);
        
        AddRuleGUI.execute("modify", newCase, stackId);
        
        
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        chooseContextDialogList = new javax.swing.JList();
        chooseContext = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        chooseContextRuleTree = new javax.swing.JTree();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Inference context selection");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        chooseContextDialogList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        chooseContextDialogList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                chooseContextDialogListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(chooseContextDialogList);

        chooseContext.setText("Choose context");
        chooseContext.setAutoscrolls(true);
        chooseContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseContextActionPerformed(evt);
            }
        });

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Root");
        chooseContextRuleTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane4.setViewportView(chooseContextRuleTree);

        jLabel15.setText("Conversation History");

        jLabel16.setText("Selected conversation context");

        windowsMenu.setText("Windows");
        jMenuBar1.add(windowsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(chooseContext))
                    .addComponent(jScrollPane4)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(0, 173, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chooseContext))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        jLabel15.getAccessibleContext().setAccessibleName("Conversation history");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chooseContextDialogListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_chooseContextDialogListValueChanged
        IDialogInstance selectedDialog = (IDialogInstance) chooseContextDialogList.getSelectedValue();
        int caseId =0;
        
        if (selectedDialog == null)
            Logger.info("selectedDialog is null..");
        
        if (selectedDialog != null) {
            Logger.info("selectedDialog value: " + selectedDialog.getDialogStr());
            Logger.info("selectedDialog tag: " + selectedDialog.getDialogTag());
            Logger.info("selectedDialog type: " + selectedDialog.getDialogType());
        }
        
        if(selectedDialog.getDialogTypeCode()==DialogInstance.USER_TYPE ||selectedDialog.getDialogTypeCode()==DialogInstance.EVENT_TYPE ){
            caseId = selectedDialog.getGeneratedCaseId();
            Logger.info("USER or EVENT - generated case ID is: " + caseId);
        } else if(selectedDialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE ){
            caseId = selectedDialog.getDerivedCaseId();
            Logger.info("SYSTEM - generated case ID is: " + caseId);
        }

        int stackId = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(caseId).getStackId();
        Logger.info("Processing Id is: " + stackId);
        
        if(selectedDialog.getDialogTypeCode()==DialogInstance.SYSTEM_TYPE){
            if(selectedDialog.getIsLastRuleNode()){
                Logger.info("SYSTEM - last rule node is true. Context button enabled!");
                chooseContext.setEnabled(true);
            } else {
                Logger.info("SYSTEM - last rule node is false. Context button would be disabled..!");
                chooseContext.setEnabled(true);
                //chooseContext.setEnabled(false);
            }
        } else {
            Logger.info("USER or EVENT - Context button disabled!");
            chooseContext.setEnabled(false);
        }
        updateChooseContextRuleTree(stackId);
    }//GEN-LAST:event_chooseContextDialogListValueChanged

    private void chooseContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseContextActionPerformed
     
        IDialogInstance selectedReferDialog = (IDialogInstance) chooseContextDialogList.getSelectedValue();
        Logger.info("chooseContextDialogList getSelectedvalue is " + selectedReferDialog.toString() );
        processSelectedRuleSet(selectedReferDialog);
        this.dispose();
    }//GEN-LAST:event_chooseContextActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    /**
     *
     * @param aDialog
     */
    public static void execute(IDialogInstance aDialog) {
        selectedDialog = aDialog;
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InferenceReferenceSelectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InferenceReferenceSelectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InferenceReferenceSelectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InferenceReferenceSelectionGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InferenceReferenceSelectionGUI().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseContext;
    private javax.swing.JList chooseContextDialogList;
    private javax.swing.JTree chooseContextRuleTree;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JMenu windowsMenu;
    // End of variables declaration//GEN-END:variables
}

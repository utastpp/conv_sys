/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import java.awt.Color;
import java.awt.Component;
import cmcrdr.main.DialogMain;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import rdr.apps.Main;
import rdr.rules.Conclusion;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleTreeModel;
import cmcrdr.logger.Logger;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class RuleViewerFrame extends javax.swing.JFrame {

    /**
     * Creates new form RuleViewerFrame
     */
    public RuleViewerFrame() {
        initComponents();
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
        setRuleTree(Main.KB.getRootRule());
    }

    
    private void setRuleTree(Rule rootRule) {
        RuleTreeModel aModel = new RuleTreeModel(rootRule);
        ruleTree.setModel(aModel);
        for (int i = 0; i < ruleTree.getRowCount(); i++) {
            ruleTree.expandRow(i);
        }
        
        
        ruleTree.setCellRenderer(new DefaultTreeCellRenderer()
        {
            @Override
            public Component getTreeCellRendererComponent(JTree pTree,
                     Object pValue, boolean pIsSelected, boolean pIsExpanded,
                     boolean pIsLeaf, int pRow, boolean pHasFocus)
                 {
                Rule node = (Rule)pValue;
                
                Color VERY_LIGHT_RED = new Color(200,0,0);
               
                boolean notStacked = true;
                
                if (node.getIsParentDoNotStack()) {
                   setBackgroundSelectionColor(Color.red);
                   setTextSelectionColor(Color.white);
                   setTextNonSelectionColor(Color.red);
                   setBackgroundNonSelectionColor(Color.white);
                   Logger.info("doNotStack rule:" + node.getRuleId() + " " + node.toString());
                   notStacked = true;

                }
                else {                   
                   setBackgroundSelectionColor(Color.blue);
                   setTextSelectionColor(Color.white);
                   setTextNonSelectionColor(Color.black);
                   setBackgroundNonSelectionColor(Color.white);
                   Logger.info("rule:" + node.getRuleId() + " " + node.toString());
                   notStacked = false;
                }
                
                String html = node.toString().replace("^^","");
                //if (node.getIsStoppingRule()) {
                if (node.getIsStopped()) {
                    Color theColor = Color.gray;
                    if (notStacked)
                        theColor = VERY_LIGHT_RED;
                    setTextSelectionColor(theColor);
                    setTextNonSelectionColor(theColor); 
                    if (!node.getIsStoppingRule())
                       html = "<html><i>[" + node.getRuleId() + "] (STOPPED) <strike>" + node.toSimpleString() +  "</strike></i></html>";                    
                    else  
                       html = "<html><i>[" + node.getRuleId() + "] (STOP RULE)</i></html>";                    

                }
                 super.getTreeCellRendererComponent(pTree, html, pIsSelected,
                         pIsExpanded, pIsLeaf, pRow, pHasFocus);
                   //setBackgroundSelectionColor(Color.blue);
                return (this);    
            }
        });
    }
    
    private void updateRuleDetails(Rule aRule) {
        /**
         *  Getting rule Id
         */
        int ruleId = aRule.getRuleId();
        // convert to string
        String ruleIdStr = Integer.toString(ruleId);
        
        /**
         *  Getting rule conditions
         */
        //get condition set 
        ConditionSet conditionSet = aRule.getConditionSet();
        // Get an iterator
        Iterator caseIterator = conditionSet.getBase().iterator();
        int cnt=0;
        String condStr="";
        while (caseIterator.hasNext()) {
            if (cnt!=0){
                condStr += " & ";
            }
            Condition condition = (Condition)caseIterator.next();
            condStr += condition.toString();
            cnt++;
        }        
        
        /**
         *  Getting cornerstone case id
         */
        //get cornerstone case ids        
        String csCaseIdStr = aRule.getCornerstoneCaseSet().toStringOnlyId();
        
        /**
         *  Getting conclusion
         */
        //get conclusion  
        Conclusion conclusion = aRule.getConclusion();
        // conver to String
        String conclusionStr = conclusion.getConclusionName();
        
        /**
         *  Updating rule details fields
         */        
        // update rule id field
        ruleIdField.setText(ruleIdStr);
        // update condition field
        conditionField.setText(condStr);
        // update cornerstone case id field
        csCaseField.setText(csCaseIdStr);
        // update conclusion field
        conclusionField.setText(conclusionStr.replace("^^",""));
        if (aRule.getDoNotStack())
            doNotStackField.setText("True");
        else if (aRule.getIsParentDoNotStack())
            doNotStackField.setText("True  (via parent)");
        else
            doNotStackField.setText("False");
        
        if (aRule.getIsStopped())
           stoppedField.setText("True");
        else
           stoppedField.setText("False");

    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ruleTree = new javax.swing.JTree();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        ruleIdField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        conditionField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        csCaseField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        conclusionField = new javax.swing.JTextField();
        doNotStackField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        stoppedField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Rule Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        ruleTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                ruleTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(ruleTree);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel3.setText("Rule Details");

        jLabel6.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel6.setText("Rule ID");

        ruleIdField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel7.setText("Condition");

        conditionField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conditionFieldActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel8.setText("Cornerstone Case ID");

        csCaseField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel12.setText("Conclusion");

        jLabel13.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel13.setText("Do Not Stack");

        jLabel14.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel14.setText("Stopped");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ruleIdField, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                            .addComponent(csCaseField)
                            .addComponent(conclusionField)
                            .addComponent(conditionField)
                            .addComponent(doNotStackField)
                            .addComponent(stoppedField))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(ruleIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(conditionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(csCaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(conclusionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stoppedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doNotStackField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)))
        );

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

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
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(refreshButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refreshButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void conditionFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conditionFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_conditionFieldActionPerformed

    private void ruleTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_ruleTreeValueChanged
        if(!ruleTree.isSelectionEmpty()){
            Rule selectedRule = (Rule) ruleTree.getLastSelectedPathComponent();
            updateRuleDetails(selectedRule);
       }
    }//GEN-LAST:event_ruleTreeValueChanged

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        setRuleTree(Main.KB.getRootRule());
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    /**
     *
     */
    public static void execute() {
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
            java.util.logging.Logger.getLogger(RuleViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RuleViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RuleViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RuleViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RuleViewerFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField conclusionField;
    private javax.swing.JTextField conditionField;
    private javax.swing.JTextField csCaseField;
    private javax.swing.JTextField doNotStackField;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton refreshButton;
    private javax.swing.JTextField ruleIdField;
    private javax.swing.JTree ruleTree;
    private javax.swing.JTextField stoppedField;
    private javax.swing.JMenu windowsMenu;
    // End of variables declaration//GEN-END:variables
}

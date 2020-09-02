/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableSystem;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import rdr.apps.Main;
import rdr.rules.Conclusion;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleTreeModel;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class SystemContextVariableGUI extends javax.swing.JFrame {

    private LinkedHashMap<Integer,LinkedHashMap<String,ContextVariableSystem>> systemContextVariableOverrides = new LinkedHashMap<>();
    /**
     * Creates new form SystemContextVariableGUI
     */
    public SystemContextVariableGUI() {
        initComponents();
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
        
        setRuleTree(Main.KB.getRootRule());
                
        String [] columnNames = {"Name","Value"};      
        DefaultTableModel theModel = new DefaultTableModel(columnNames, 0);
        int count = 0;
        
        newSystemContextVariableName.setText("");
        newSystemContextVariableValue.setText("");
        
        editSystemContextVariableName.setText("");
        editSystemContextVariableValue.setText("");
        
        // context-overide versions
        String [] contextColumnNames = {"Name","Value","Rule Context Override"}; 
        DefaultTableModel ruleContextOverrideModel = new DefaultTableModel(contextColumnNames, 0);
        
        Iterator iter = DialogMain.globalContextVariableManager.getContextVariables().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            String contextVariableName = (String) me.getKey();
            
            Logger.info("LOADING VARIABLES - current variable:" + contextVariableName);
            
            // store all @MOD entries in our local list, add to table model later
            if (contextVariableName.startsWith("@MOD")) {
                Logger.info("Overridden variable found!");
                String contextVariableValue = ((ContextVariable)me.getValue()).getSingleVariableValue();
                String contextStringLabel = contextVariableName.substring(contextVariableName.indexOf("[")+1,contextVariableName.indexOf("]"));
                String contextVariableNameSimplified = contextVariableName.substring(contextVariableName.indexOf("]")+1);
                int ruleContextFound = Integer.parseInt(contextStringLabel);
                Logger.info("Modified system variable - original name: " + contextVariableName + ", simplified name: " + contextVariableNameSimplified  + ", rule override detected: " + ruleContextFound);

                ContextVariableSystem overrideContext = new ContextVariableSystem(contextVariableNameSimplified, contextVariableValue, ruleContextFound);
                
                addOverrideVariableToRuleOverrideList(contextVariableNameSimplified, contextVariableValue, ruleContextFound);
            }
            // We can add these entries directly to our table model
            else if (contextVariableName.startsWith("@SYSTEM")) {
                Logger.info("System variable found!");

                String contextVariableValue = ((ContextVariable)me.getValue()).getSingleVariableValue();
                ArrayList row = new ArrayList<>();
                row.add(contextVariableName);
                row.add(contextVariableValue);
                Logger.info("Adding system variable: " + contextVariableName +", with value: " + contextVariableValue +" to list");
                theModel.addRow(row.toArray());
            }
        }
        
        // If we've invoke this GUI as part of a KA, get the current context of the selected user..
        //int userContext = DialogMain.getDialogUserList().getCurrentContext();
        //if (userContext >= 0) {
            //Logger.info("DETECTED CURRENT USER CONTEXT AS :" + userContext);
            //updateContextDefaultTableModel(ruleContextOverrideModel,userContext);
            //ruleOverrideContext.setText(Integer.toString(userContext));
        //}
    
        ruleOverrideEditSystemContextVariableName.setText("");
        ruleOverrideEditSystemContextVariableValue.setText("");    
                     
        systemContextVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = systemContextVariablesTable.getSelectedRow();
                if (row >= 0) {
                    editSystemContextVariableName.setText((String)systemContextVariablesTable.getValueAt(row, 0));
                    editSystemContextVariableValue.setText((String)systemContextVariablesTable.getValueAt(row, 1));
                    updateRuleOverrideContextDefaultTableModel(ruleContextOverrideModel,(String)systemContextVariablesTable.getValueAt(row, 0));
                }
            }
        });
        systemContextVariablesTable.setModel(theModel);
        
        ruleOverrideSystemContextVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = ruleOverrideSystemContextVariablesTable.getSelectedRow();
                if (row >= 0) {
                    ruleOverrideEditSystemContextVariableName.setText((String)ruleOverrideSystemContextVariablesTable.getValueAt(row, 0));
                    ruleOverrideEditSystemContextVariableValue.setText((String)ruleOverrideSystemContextVariablesTable.getValueAt(row, 1));
                    selectedRuleOverrideContext.setText(Integer.toString((int)ruleOverrideSystemContextVariablesTable.getValueAt(row, 2)));
                }
            }
        });
        ruleOverrideSystemContextVariablesTable.setModel(ruleContextOverrideModel);

    }
    
    private void updateRuleOverrideContextDefaultTableModel(DefaultTableModel theModel,String variableName) {
        theModel.getDataVector().clear();
        
        
        for (int key: systemContextVariableOverrides.keySet()) {
            LinkedHashMap<String,ContextVariableSystem> thisRuleOverrideContext = systemContextVariableOverrides.get(key);
            for (String varName: thisRuleOverrideContext.keySet()) {
                if (varName.equals(variableName)) {
                    ArrayList row = new ArrayList<>();
                    row.add(varName);
                    row.add(thisRuleOverrideContext.get(varName).getValue());
                    row.add(key);
                    theModel.addRow(row.toArray());
                }
            }
        }
        theModel.fireTableDataChanged();
    }
    
    /*
    private void updateContextDefaultTableModel(DefaultTableModel theModel, int context) {
        theModel.getDataVector().clear();
        
        Logger.info("I am being called for context " + context);
        
        LinkedHashMap<String,ContextVariableSystem> thisContext = systemContextVariableOverrides.get(context);
        if (thisContext != null) {
            Iterator iter = thisContext.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry me = (Map.Entry) iter.next();
                String contextVariableName = (String) me.getKey();    
                String contextVariableValue = ((ContextVariableSystem)me.getValue()).getValue();
                ArrayList row = new ArrayList<>();
                row.add(contextVariableName);
                row.add(contextVariableValue);
                row.add(context);
                Logger.info("Adding variable: " + contextVariableName + " with value: " + contextVariableValue);

                theModel.addRow(row.toArray());
            }                          
        }
        else {
            Logger.info("thisContext is null");
        }
    }
    */
    
    
    private void setRuleTree(Rule rootRule) {
        RuleTreeModel aModel = new RuleTreeModel(rootRule);
        ruleTree.setModel(aModel);
        for (int i = 0; i < ruleTree.getRowCount(); i++) {
            ruleTree.expandRow(i);
        }
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
        
        conclusionField.setText(conclusionStr);
        
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ruleContextSelectorFrame = new javax.swing.JFrame();
        jScrollPane2 = new javax.swing.JScrollPane();
        ruleTree = new javax.swing.JTree();
        jPanel8 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        ruleIdField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        conclusionField = new javax.swing.JTextField();
        contextSelectButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        windowsMenu1 = new javax.swing.JMenu();
        jPanel6 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        systemContextVariablesTable = new javax.swing.JTable();
        systemContextVariablesEditButton = new javax.swing.JButton();
        systemContextVariablesDeleteButton = new javax.swing.JButton();
        editSystemContextVariableValue = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        editSystemContextVariableName = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        newSystemContextVariableName = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        newSystemContextVariableValue = new javax.swing.JTextField();
        newSystemContextVariableAddButton = new javax.swing.JButton();
        systemContextVariablesCancelButton = new javax.swing.JButton();
        systemContextVariablesSaveButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ruleOverrideSystemContextVariablesTable = new javax.swing.JTable();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        ruleOverrideEditSystemContextVariableName = new javax.swing.JTextField();
        ruleOverrideEditSystemContextVariableValue = new javax.swing.JTextField();
        contextSystemContextVariablesDeleteButton = new javax.swing.JButton();
        contextSystemContextVariablesEditButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        selectedRuleOverrideContext = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        addContextOverrideButton = new javax.swing.JButton();
        ruleOverrideContext = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        selectRuleOverrideContextButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        ruleContextSelectorFrame.setTitle("Select Rule Context");
        ruleContextSelectorFrame.setMinimumSize(new java.awt.Dimension(595, 550));
        ruleContextSelectorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                ruleContextSelectorFrameformWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                ruleContextSelectorFrameformWindowActivated(evt);
            }
        });

        ruleTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                ruleTreeValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(ruleTree);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel3.setText("Context Details");

        jLabel6.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel6.setText("Rule ID (context)");

        ruleIdField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel12.setText("Conclusion");

        contextSelectButton.setText("Select");
        contextSelectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                contextSelectButtonMousePressed(evt);
            }
        });
        contextSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextSelectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ruleIdField, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                            .addComponent(conclusionField)))
                    .addComponent(jLabel3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(contextSelectButton)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(ruleIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(conclusionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(contextSelectButton)
                .addContainerGap())
        );

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        windowsMenu1.setText("Windows");
        jMenuBar2.add(windowsMenu1);

        ruleContextSelectorFrame.setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout ruleContextSelectorFrameLayout = new javax.swing.GroupLayout(ruleContextSelectorFrame.getContentPane());
        ruleContextSelectorFrame.getContentPane().setLayout(ruleContextSelectorFrameLayout);
        ruleContextSelectorFrameLayout.setHorizontalGroup(
            ruleContextSelectorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ruleContextSelectorFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ruleContextSelectorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ruleContextSelectorFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(refreshButton)))
                .addContainerGap())
        );
        ruleContextSelectorFrameLayout.setVerticalGroup(
            ruleContextSelectorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ruleContextSelectorFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refreshButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("System Context Variables");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("System Context Variables (global)");

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
        systemContextVariablesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane13.setViewportView(systemContextVariablesTable);

        systemContextVariablesEditButton.setText("Save modifications");
        systemContextVariablesEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemContextVariablesEditButtonActionPerformed(evt);
            }
        });

        systemContextVariablesDeleteButton.setText("Delete selected");
        systemContextVariablesDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemContextVariablesDeleteButtonActionPerformed(evt);
            }
        });

        jLabel22.setText("Name:");

        editSystemContextVariableName.setEditable(false);

        jLabel23.setText("Value:");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editSystemContextVariableValue)
                        .addContainerGap())
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editSystemContextVariableName)))
                        .addGap(6, 6, 6))))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(systemContextVariablesDeleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(systemContextVariablesEditButton))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 6, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(editSystemContextVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(editSystemContextVariableValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(systemContextVariablesEditButton)
                    .addComponent(systemContextVariablesDeleteButton))
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setText("Add New Context Variable");

        jLabel11.setText("Name:");

        jLabel20.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(204, 204, 204));
        jLabel20.setText("@SYSTEM");

        jLabel21.setText("Value:");

        newSystemContextVariableAddButton.setText("Add");
        newSystemContextVariableAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSystemContextVariableAddButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newSystemContextVariableName))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 277, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newSystemContextVariableValue))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(newSystemContextVariableAddButton)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel20)
                    .addComponent(newSystemContextVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(newSystemContextVariableValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(newSystemContextVariableAddButton))
        );

        systemContextVariablesCancelButton.setText("Cancel");
        systemContextVariablesCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemContextVariablesCancelButtonActionPerformed(evt);
            }
        });

        systemContextVariablesSaveButton.setText("Save All Changes");
        systemContextVariablesSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemContextVariablesSaveButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("System Context Variables (rule context override)");

        ruleOverrideSystemContextVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(ruleOverrideSystemContextVariablesTable);

        jLabel24.setText("Name:");

        jLabel25.setText("Value:");

        ruleOverrideEditSystemContextVariableName.setEditable(false);
        ruleOverrideEditSystemContextVariableName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ruleOverrideEditSystemContextVariableNameActionPerformed(evt);
            }
        });

        contextSystemContextVariablesDeleteButton.setText("Delete selected override");
        contextSystemContextVariablesDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextSystemContextVariablesDeleteButtonActionPerformed(evt);
            }
        });

        contextSystemContextVariablesEditButton.setText("Save modifications");
        contextSystemContextVariablesEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextSystemContextVariablesEditButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Rule:");

        selectedRuleOverrideContext.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24)
                                    .addComponent(jLabel25)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(selectedRuleOverrideContext, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ruleOverrideEditSystemContextVariableName, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                                    .addComponent(ruleOverrideEditSystemContextVariableValue)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(contextSystemContextVariablesDeleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(contextSystemContextVariablesEditButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(5, 5, 5)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(ruleOverrideEditSystemContextVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ruleOverrideEditSystemContextVariableValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(selectedRuleOverrideContext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextSystemContextVariablesDeleteButton)
                    .addComponent(contextSystemContextVariablesEditButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        addContextOverrideButton.setText("Add rule context override >>");
        addContextOverrideButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addContextOverrideButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Context");

        selectRuleOverrideContextButton.setText("select rule context");
        selectRuleOverrideContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRuleOverrideContextButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(ruleOverrideContext, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(addContextOverrideButton, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(selectRuleOverrideContextButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addContextOverrideButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ruleOverrideContext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(selectRuleOverrideContextButton))
        );

        windowsMenu.setText("Windows");
        jMenuBar1.add(windowsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(systemContextVariablesCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemContextVariablesSaveButton)))
                .addGap(0, 8, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(systemContextVariablesCancelButton)
                        .addComponent(systemContextVariablesSaveButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void systemContextVariablesEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemContextVariablesEditButtonActionPerformed
        DefaultTableModel theModel = (DefaultTableModel) systemContextVariablesTable.getModel();

        if (systemContextVariablesTable.getSelectedRow() >= 0) {
            if (!editSystemContextVariableValue.getText().equals("")) {
                String theVarName = editSystemContextVariableName.getText();  // not needed here..
                String theVarValue = editSystemContextVariableValue.getText();
                int rowNumber = systemContextVariablesTable.getSelectedRow();
                theModel.setValueAt(theVarValue, rowNumber, 1);
            }
            else {
                showMessageDialog(null,"You cannot set an empty value..");
            }
        }
        else {
            showMessageDialog(null,"You must select an existing variable to modify...");
        }
    }//GEN-LAST:event_systemContextVariablesEditButtonActionPerformed

    private void systemContextVariablesDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemContextVariablesDeleteButtonActionPerformed
        int theRow = systemContextVariablesTable.getSelectedRow();
        if (theRow >= 0) {
            String variableName = (String) systemContextVariablesTable.getValueAt(theRow, 0);
            DefaultTableModel theModel = (DefaultTableModel) systemContextVariablesTable.getModel();
            theModel.removeRow(theRow);
            editSystemContextVariableName.setText("");
            editSystemContextVariableValue.setText("");
        }
        else {
            showMessageDialog(null,"You must select a variable to delete..");
        }
    }//GEN-LAST:event_systemContextVariablesDeleteButtonActionPerformed

    private void newSystemContextVariableAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSystemContextVariableAddButtonActionPerformed
        if (!newSystemContextVariableName.getText().equals("") && !newSystemContextVariableValue.getText().equals("")) {
            DefaultTableModel theModel = (DefaultTableModel) systemContextVariablesTable.getModel();
            String [] aRow = {"@SYSTEM" + newSystemContextVariableName.getText(),newSystemContextVariableValue.getText()};
            boolean alreadyExists = false;

            for (int i = 0; i < theModel.getRowCount(); i++) {
                if (((String)theModel.getValueAt(i, 0)).equals("@SYSTEM" + newSystemContextVariableName.getText())) {
                    alreadyExists = true;
                    break;
                }

            }
            if (!alreadyExists)
                theModel.addRow(aRow);
            else
                showMessageDialog(null,"A variable with this name alreay exists...");

        }
        else {
            showMessageDialog(null,"A variable name and value is required!");
        }
    }//GEN-LAST:event_newSystemContextVariableAddButtonActionPerformed

    private void systemContextVariablesCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemContextVariablesCancelButtonActionPerformed
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_systemContextVariablesCancelButtonActionPerformed

    private void systemContextVariablesSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemContextVariablesSaveButtonActionPerformed

        LinkedHashMap<String,ContextVariable> newContextVariableList = new LinkedHashMap<>();
        // ok, build up a new list containing all non-system context variables

        for (Map.Entry me : DialogMain.globalContextVariableManager.getContextVariables().entrySet()) {
            String varName = (String)me.getKey();
            ContextVariable cv = (ContextVariable)me.getValue();
            if (!varName.startsWith("@SYSTEM") && !varName.startsWith("@MOD")) {
                newContextVariableList.put(varName, cv);
                Logger.info("newContextVariableList - Adding user variable:" + varName + " with value: " + cv.getSingleVariableValue());
            }
        }

        // now add the system ones..
        DefaultTableModel theModel = (DefaultTableModel)systemContextVariablesTable.getModel();
        for (int i=0; i < theModel.getRowCount(); i++) {
            ContextVariable newCV = new ContextVariable();
            newCV.setVariableName((String)theModel.getValueAt(i, 0));
            newCV.addVariableValue((String)theModel.getValueAt(i, 1));
            newContextVariableList.put((String)theModel.getValueAt(i, 0),newCV);
            Logger.info("newContextVariableList - Adding system variable:" + (String)theModel.getValueAt(i, 0) + " with value: " + (String)theModel.getValueAt(i, 1));

        }
        
        // now add the context overriden system ones..
        for (Map.Entry contextEntry : systemContextVariableOverrides.entrySet()) {
            for (Map.Entry overrideVarEntry : ((LinkedHashMap<String, ContextVariableSystem>)contextEntry.getValue()).entrySet()) {
                int context = (int)contextEntry.getKey();
                String variableName = (String)overrideVarEntry.getKey();
                String variableValue = (String) ((ContextVariableSystem)overrideVarEntry.getValue()).getValue();
                ContextVariable newCV = new ContextVariable();
                newCV.setVariableName("@MOD[" + context + "]" + variableName);
                newCV.addVariableValue(variableValue);
                newContextVariableList.put("@MOD[" + context + "]" + variableName, newCV);
                Logger.info("newContextVariableList - Adding system mod variable:" + newCV.getVariableName() + " with value: " + variableValue);

            }
        }
        // now reset the global list with our new list..
        DialogMain.globalContextVariableManager.setContextVariables(newContextVariableList);
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_systemContextVariablesSaveButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    private void ruleOverrideEditSystemContextVariableNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ruleOverrideEditSystemContextVariableNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ruleOverrideEditSystemContextVariableNameActionPerformed

    private void addContextOverrideButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addContextOverrideButtonActionPerformed
        DefaultTableModel theModel = (DefaultTableModel) ruleOverrideSystemContextVariablesTable.getModel();

        if (!ruleOverrideContext.getText().isEmpty()) {
            if (systemContextVariablesTable.getSelectedRow() >= 0) {
                if (!editSystemContextVariableValue.getText().equals("")) {
                    String theVarName = editSystemContextVariableName.getText();
                    String theVarValue = editSystemContextVariableValue.getText();
                    int theRuleOverride = Integer.parseInt(ruleOverrideContext.getText());
                    int rowNumber = systemContextVariablesTable.getSelectedRow();
                    ArrayList row = new ArrayList<>();
                    row.add(theVarName);
                    row.add(theVarValue);
                    row.add(theRuleOverride);
                    
                    boolean alreadyExists = false;

                    for (int i = 0; i < theModel.getRowCount(); i++) {
                        if (((String)theModel.getValueAt(i, 0)).equals(theVarName) && (int)theModel.getValueAt(i, 2) == theRuleOverride) {
                            alreadyExists = true;
                            break;
                        }

                    }
                    if (!alreadyExists) {
                        theModel.addRow(row.toArray());
                        addOverrideVariableToRuleOverrideList(theVarName,theVarValue,theRuleOverride);
                    }
                    else {
                        showMessageDialog(null,"This variable is already overridden for this rule..");
                        
                    }
                }
                else {
                    showMessageDialog(null,"You cannot set an empty value..");
                }
            }
            else {
                showMessageDialog(null,"You must select an existing variable to override...");
            }
        }
        else {
            showMessageDialog(null,"Please select a rule first (choose 'select rule context')");

        }
        
        
        
    }//GEN-LAST:event_addContextOverrideButtonActionPerformed

    private void ruleTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_ruleTreeValueChanged
        if(!ruleTree.isSelectionEmpty()){
            Rule selectedRule = (Rule) ruleTree.getLastSelectedPathComponent();
            updateRuleDetails(selectedRule);
        }
    }//GEN-LAST:event_ruleTreeValueChanged

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        setRuleTree(Main.KB.getRootRule());
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void ruleContextSelectorFrameformWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_ruleContextSelectorFrameformWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_ruleContextSelectorFrameformWindowClosing

    private void ruleContextSelectorFrameformWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_ruleContextSelectorFrameformWindowActivated
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_ruleContextSelectorFrameformWindowActivated

    private void contextSelectButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contextSelectButtonMousePressed
        if (!ruleIdField.getText().isEmpty()) {
            
        }
        else {
             showMessageDialog(null,"You must select a context.");           
        }
    }//GEN-LAST:event_contextSelectButtonMousePressed

    private void contextSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contextSelectButtonActionPerformed
        if (!ruleIdField.getText().isEmpty()) {
            ruleOverrideContext.setText(ruleIdField.getText());
            DefaultTableModel theModel = (DefaultTableModel)ruleOverrideSystemContextVariablesTable.getModel();
            //updateContextDefaultTableModel(theModel,Integer.parseInt(ruleOverrideContext.getText()));
            
            ruleContextSelectorFrame.setVisible(false);
        }
        else {
           showMessageDialog(null,"You must select a context.");  
        }
    }//GEN-LAST:event_contextSelectButtonActionPerformed

    private void contextSystemContextVariablesEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contextSystemContextVariablesEditButtonActionPerformed
        DefaultTableModel theModel = (DefaultTableModel) ruleOverrideSystemContextVariablesTable.getModel();

        if (ruleOverrideSystemContextVariablesTable.getSelectedRow() >= 0) {
            if (!ruleOverrideEditSystemContextVariableValue.getText().equals("")) {
                String theVarName = ruleOverrideEditSystemContextVariableName.getText();  // not needed here..
                String theVarValue = ruleOverrideEditSystemContextVariableValue.getText();
                int ruleOverride = Integer.parseInt(selectedRuleOverrideContext.getText());
                
                int rowNumber = ruleOverrideSystemContextVariablesTable.getSelectedRow();
                theModel.setValueAt(theVarValue, rowNumber, 1);
                
                LinkedHashMap<String,ContextVariableSystem> theContext = systemContextVariableOverrides.get(ruleOverride);
                if (theContext != null) {
                    ContextVariableSystem varBeingModified = theContext.get(theVarName);
                    if (varBeingModified != null) {
                        varBeingModified.setVariableValue(theVarValue);
                    }
                }
            }
            else {
                showMessageDialog(null,"You cannot set an empty value..");
            }
        }
        else {
            showMessageDialog(null,"You must select an existing variable to modify...");
        }
    }//GEN-LAST:event_contextSystemContextVariablesEditButtonActionPerformed

    private void contextSystemContextVariablesDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contextSystemContextVariablesDeleteButtonActionPerformed
        int theRow = ruleOverrideSystemContextVariablesTable.getSelectedRow();
        if (theRow >= 0) {
            String variableName = (String) ruleOverrideSystemContextVariablesTable.getValueAt(theRow, 0);
            DefaultTableModel theModel = (DefaultTableModel) ruleOverrideSystemContextVariablesTable.getModel();
            int ruleOverride = (int)ruleOverrideSystemContextVariablesTable.getValueAt(theRow, 2);
            theModel.removeRow(theRow);
            ruleOverrideEditSystemContextVariableName.setText("");
            ruleOverrideEditSystemContextVariableValue.setText("");
            deleteOverrideVariableFromContextList(variableName,ruleOverride);         
        }
        else {
            showMessageDialog(null,"You must select a variable to delete..");
        }
    }//GEN-LAST:event_contextSystemContextVariablesDeleteButtonActionPerformed

    private void selectRuleOverrideContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRuleOverrideContextButtonActionPerformed
        ruleContextSelectorFrame.setVisible(true);
    }//GEN-LAST:event_selectRuleOverrideContextButtonActionPerformed

    private void deleteOverrideVariableFromContextList(String variableName, int context) {
        LinkedHashMap<String,ContextVariableSystem> theContextList = systemContextVariableOverrides.get(context);
        if (theContextList != null) {
            theContextList.remove(variableName);
        }
    }
    
    private void addOverrideVariableToRuleOverrideList(String variableName, String variableValue, int currentRuleContextOverride) {
        LinkedHashMap<String,ContextVariableSystem> ruleContextOverrides = systemContextVariableOverrides.get(currentRuleContextOverride);
        if (ruleContextOverrides == null) {
            Logger.info("Adding new context as list didn't contain this context before..");
            ruleContextOverrides = new LinkedHashMap<>();
            ContextVariableSystem newCV = new ContextVariableSystem(variableName, variableValue, currentRuleContextOverride);
            Logger.info("Creating new CVS variable, name:" + variableName + ", value: " + variableValue + " for currentRuleContextOverride:" + currentRuleContextOverride);
            ruleContextOverrides.put(variableName, newCV);
            systemContextVariableOverrides.put(currentRuleContextOverride, ruleContextOverrides);
            Logger.info("Putting new CVS in local source list");
        }
        else {
            if (!ruleContextOverrides.containsKey(variableName)) {
                Logger.info("Context found, but this variable " + variableName + " didn't exist, adding..");

                ContextVariableSystem newCV = new ContextVariableSystem(variableName, variableValue, currentRuleContextOverride);
                ruleContextOverrides.put(variableName, newCV);
            }
            else {
                Logger.info("Context found, this variable " + variableName + " existed, replacing..");
                ContextVariableSystem newCV = new ContextVariableSystem(variableName, variableValue, currentRuleContextOverride);
                ruleContextOverrides.replace(variableName, newCV);
            }
        }
    } 
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
            java.util.logging.Logger.getLogger(SystemContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SystemContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SystemContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SystemContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SystemContextVariableGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addContextOverrideButton;
    private javax.swing.JTextField conclusionField;
    private javax.swing.JButton contextSelectButton;
    private javax.swing.JButton contextSystemContextVariablesDeleteButton;
    private javax.swing.JButton contextSystemContextVariablesEditButton;
    private javax.swing.JTextField editSystemContextVariableName;
    private javax.swing.JTextField editSystemContextVariableValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton newSystemContextVariableAddButton;
    private javax.swing.JTextField newSystemContextVariableName;
    private javax.swing.JTextField newSystemContextVariableValue;
    private javax.swing.JButton refreshButton;
    private javax.swing.JFrame ruleContextSelectorFrame;
    private javax.swing.JTextField ruleIdField;
    private javax.swing.JTextField ruleOverrideContext;
    private javax.swing.JTextField ruleOverrideEditSystemContextVariableName;
    private javax.swing.JTextField ruleOverrideEditSystemContextVariableValue;
    private javax.swing.JTable ruleOverrideSystemContextVariablesTable;
    private javax.swing.JTree ruleTree;
    private javax.swing.JButton selectRuleOverrideContextButton;
    private javax.swing.JTextField selectedRuleOverrideContext;
    private javax.swing.JButton systemContextVariablesCancelButton;
    private javax.swing.JButton systemContextVariablesDeleteButton;
    private javax.swing.JButton systemContextVariablesEditButton;
    private javax.swing.JButton systemContextVariablesSaveButton;
    private javax.swing.JTable systemContextVariablesTable;
    private javax.swing.JMenu windowsMenu;
    private javax.swing.JMenu windowsMenu1;
    // End of variables declaration//GEN-END:variables
}

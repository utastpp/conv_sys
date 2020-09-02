/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableAction;
import cmcrdr.contextvariable.ContextVariableManager;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class ContextVariableGUI extends javax.swing.JFrame {

    private DefaultListModel recentInputTokenisedListModel = new DefaultListModel();
    private DefaultListModel variableNameListModel = new DefaultListModel();
    private DefaultListModel variableValueListModel = new DefaultListModel();
    private DefaultListModel variableActionListModel = new DefaultListModel();
    private LinkedHashMap<String, ContextVariable> contextVariables = new LinkedHashMap<>();
    private LinkedHashMap<String, ContextVariable> systemContextVariables = new LinkedHashMap<>();
    private ContextVariableManager globalContextVariableManager = new ContextVariableManager();

    /**
     *
     */
    public Object parent = null;
    
    /**
     * Creates new form ContextVariableGUI
     * @param recentInput
     */
    public ContextVariableGUI(String recentInput) {
        initComponents();
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
        
        String delims = " ";
        StringTokenizer st = new StringTokenizer(recentInput,delims);
        
        // skip the first two elements as they are the dialog number and replyer e.g. 1 [user] 
        if (st.hasMoreElements())
            st.nextElement();
        if (st.hasMoreElements())
            st.nextElement();
        
        while (st.hasMoreElements()) {
            recentInputTokenisedListModel.addElement(st.nextElement());
        }  
        
        rawRecentInput.setText(recentInput);
        
        
        for (Map.Entry me : DialogMain.globalContextVariableManager.getContextVariables().entrySet()) {
            String varName = (String) me.getKey();
            ContextVariable aVariable = (ContextVariable) me.getValue();
            if (varName.startsWith("@SYSTEM")) {
                this.systemContextVariables.put(varName, aVariable);  // hide away and store the system context variables for now..
                targetActionValueVariableListComboBox.addItem(varName);  // need to include system context vars here..
                targetActionTriggerVariableListComboBox.addItem(varName);
            }
            else if (!varName.startsWith("@MOD")) {  // ignore the overridden system variables as well..
                variableNameListModel.addElement(varName);
                this.contextVariables.put(varName, aVariable);
                variableListComboBox.addItem(varName);
                
                actionVariableListComboBox.addItem(varName);
                targetActionValueVariableListComboBox.addItem(varName);
                targetActionVariableListComboBox.addItem(varName);
                targetActionTriggerVariableListComboBox.addItem(varName);
                
                /*
                Iterator actionIterator = aVariable.getActionsBase().entrySet().iterator();
                while (actionIterator.hasNext()) {
                me = (Map.Entry) actionIterator.next();
                ContextVariableAction anAction = (ContextVariableAction) me.getValue();
                variableActionListModel.addElement(anAction.getTarget() + " " + anAction.getValue());
                }
                */
            }          
        }
        
        if (variableNameListModel.isEmpty()) {
            deleteSelectedVariableButton.setEnabled(false);
        }
        
        if (variableActionListModel.isEmpty()) {
            deleteSelectedActionButton.setEnabled(false);
        }
        
        if (variableListComboBox.getItemCount() == 0) {
            variableListComboBox.setEnabled(false);
            addInputToVariableButton.setEnabled(false);
        }
        else {
            variableListComboBox.setSelectedIndex(0);
        }
        
        
    }
    
    private void getContextVariableOverrideValueForTextField(String selectedVarName) {
        ContextVariable cv = this.contextVariables.get(selectedVarName);
        if (cv != null) 
        {
            variableOverrideValue.setText(cv.getVariableValueOverride());
        }
    }
    
    private void getContextVariableValuesForList(String selectedVarName) {
        
        //ArrayList<String> values = this.contextVariables.get(selectedVarName);
        ContextVariable varValues = this.contextVariables.get(selectedVarName);
        if (varValues != null) {
            variableValueListModel.clear();
            
            LinkedHashMap values = varValues.getValuesBase();
            
            Iterator valueIterator = values.entrySet().iterator();
            while (valueIterator.hasNext()) {
                Map.Entry me = (Map.Entry) valueIterator.next();
                int id = (int)me.getKey();
                String value = (String)me.getValue();
                variableValueListModel.addElement(value);
            }
           

            if (variableValueListModel.isEmpty()) {
                deleteSelectedValueButton.setEnabled(false);
            }
            else {
                deleteSelectedValueButton.setEnabled(true);
            }
            
            if (!varValues.getVariableValueOverride().equals("")) {
                variableOverrideValue.setText(varValues.getVariableValueOverride());
            }
        }
    }
    
    private void getContextVariableActionsForList(String selectedVarName) {
        
        //ArrayList<String> values = this.contextVariables.get(selectedVarName);
        ContextVariable varActions = this.contextVariables.get(selectedVarName);
        if (varActions != null) {
            variableActionListModel.clear();
            
            
            Iterator actionIterator = varActions.getActionsBase().entrySet().iterator();
            while (actionIterator.hasNext()) {
                Map.Entry me = (Map.Entry) actionIterator.next();
                int id = (int)me.getKey();
                ContextVariableAction action = (ContextVariableAction)me.getValue();
                variableActionListModel.addElement(action.getTarget() + " " + action.getValue() + " " + action.getTrigger());
            }
           

            if (variableActionListModel.isEmpty()) {
                deleteSelectedActionButton.setEnabled(false);
            }
            else {
                deleteSelectedActionButton.setEnabled(true);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectedTermButtonGroup = new javax.swing.ButtonGroup();
        selectedActionButtonGroup = new javax.swing.ButtonGroup();
        triggerButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        recentInputTokenisedList = new javax.swing.JList<>();
        jLabel5 = new javax.swing.JLabel();
        matchingDicTerm = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        rawRecentInputRadioButton = new javax.swing.JRadioButton();
        addInputTermRadioButton = new javax.swing.JRadioButton();
        addDicTermRadioButton = new javax.swing.JRadioButton();
        jLabel13 = new javax.swing.JLabel();
        findHighlightedDictionaryMatchButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        rawRecentInput = new javax.swing.JTextArea();
        jLabel19 = new javax.swing.JLabel();
        otherText = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        findOtherInDictButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        variableListComboBox = new javax.swing.JComboBox<>();
        addInputToVariableButton = new javax.swing.JButton();
        manualCriteriaRadioButton = new javax.swing.JRadioButton();
        manualCriteriaInput = new javax.swing.JTextField();
        manualValueInput = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        variableNameList = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        variableValueList = new javax.swing.JList<>();
        deleteSelectedVariableButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        deleteSelectedValueButton = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        variableActionList = new javax.swing.JList<>();
        deleteSelectedActionButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        newVariableName = new javax.swing.JTextField();
        addNewVariableButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        targetActionVariableListComboBox = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        fixedValueField = new javax.swing.JTextField();
        fixedValueRadiobutton = new javax.swing.JRadioButton();
        contextValueRadiobutton = new javax.swing.JRadioButton();
        targetActionValueVariableListComboBox = new javax.swing.JComboBox<>();
        addActionButton = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        actionVariableListComboBox = new javax.swing.JComboBox<>();
        jLabel21 = new javax.swing.JLabel();
        triggerTrueRadioButton = new javax.swing.JRadioButton();
        triggerContainsRadioButton = new javax.swing.JRadioButton();
        targetActionTriggerVariableListComboBox = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        variableOverrideValue = new javax.swing.JTextField();
        deleteVariableOverrideValueButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("User Context Variables");
        setMinimumSize(new java.awt.Dimension(1035, 525));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Edit Global User Context Variable Definitions");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Select matching criteria");

        jLabel4.setText("Recent input");

        recentInputTokenisedList.setModel(recentInputTokenisedListModel);
        recentInputTokenisedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        recentInputTokenisedList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                recentInputTokenisedListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(recentInputTokenisedList);

        jLabel5.setText("Dictionary");

        matchingDicTerm.setEnabled(false);

        jLabel10.setText("Recent input:");

        jLabel11.setText("(tokenised):");

        jLabel12.setText(" matching term found:");

        selectedTermButtonGroup.add(rawRecentInputRadioButton);

        selectedTermButtonGroup.add(addInputTermRadioButton);

        selectedTermButtonGroup.add(addDicTermRadioButton);
        addDicTermRadioButton.setSelected(true);

        jLabel13.setText("(highlight selection)");

        findHighlightedDictionaryMatchButton.setText("find in dict");
        findHighlightedDictionaryMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findHighlightedDictionaryMatchButtonActionPerformed(evt);
            }
        });

        rawRecentInput.setColumns(20);
        rawRecentInput.setLineWrap(true);
        rawRecentInput.setRows(5);
        rawRecentInput.setWrapStyleWord(true);
        jScrollPane4.setViewportView(rawRecentInput);

        jLabel19.setText("Variable matching criteria");

        jLabel20.setText("Other (not in input)");

        findOtherInDictButton.setText("find in dict");
        findOtherInDictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findOtherInDictButtonActionPerformed(evt);
            }
        });

        jLabel9.setText("Variable name:");

        addInputToVariableButton.setText("Add matching criteria");
        addInputToVariableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInputToVariableButtonActionPerformed(evt);
            }
        });

        selectedTermButtonGroup.add(manualCriteriaRadioButton);
        manualCriteriaRadioButton.setText("Manual raw criteria");

        manualCriteriaInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                manualCriteriaInputFocusGained(evt);
            }
        });

        manualValueInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                manualValueInputFocusGained(evt);
            }
        });
        manualValueInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualValueInputActionPerformed(evt);
            }
        });

        jLabel22.setText("Manual value override:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(otherText, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findOtherInDictButton))
                    .addComponent(jLabel19)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(variableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addInputToVariableButton))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(rawRecentInputRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addGap(26, 26, 26))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel22)
                                            .addComponent(jLabel13))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel6))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(addInputTermRadioButton)
                                            .addComponent(addDicTermRadioButton))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel11)
                                            .addComponent(jLabel5)))
                                    .addComponent(jLabel12)
                                    .addComponent(manualCriteriaRadioButton))
                                .addGap(55, 55, 55)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(manualCriteriaInput)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(matchingDicTerm, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(findHighlightedDictionaryMatchButton)))
                                .addGap(0, 22, Short.MAX_VALUE))
                            .addComponent(manualValueInput))))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane3, matchingDicTerm});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(variableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addInputToVariableButton))
                .addGap(1, 1, 1)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel10)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(rawRecentInputRadioButton)))
                    .addComponent(findHighlightedDictionaryMatchButton))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(addInputTermRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addDicTermRadioButton))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(matchingDicTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manualCriteriaRadioButton)
                    .addComponent(manualCriteriaInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manualValueInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(otherText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(findOtherInDictButton)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Variables:");

        variableNameList.setModel(variableNameListModel);
        variableNameList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        variableNameList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                variableNameListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(variableNameList);

        jLabel3.setText("Matching Criteria:");

        variableValueList.setModel(variableValueListModel);
        variableValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(variableValueList);

        deleteSelectedVariableButton.setText("Delete");
        deleteSelectedVariableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedVariableButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Variable maintenance");

        deleteSelectedValueButton.setText("Delete ");
        deleteSelectedValueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedValueButtonActionPerformed(evt);
            }
        });

        jLabel18.setText("Actions:");

        variableActionList.setModel(variableActionListModel);
        variableActionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(variableActionList);

        deleteSelectedActionButton.setText("Delete");
        deleteSelectedActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedActionButtonActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setText("New variable name:");

        newVariableName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newVariableNameActionPerformed(evt);
            }
        });

        addNewVariableButton.setText("Add new variable");
        addNewVariableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewVariableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(newVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewVariableButton))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewVariableButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(deleteSelectedVariableButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(deleteSelectedValueButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deleteSelectedActionButton)
                            .addComponent(jLabel18)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane1, jScrollPane2, jScrollPane5});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteSelectedVariableButton)
                    .addComponent(deleteSelectedValueButton)
                    .addComponent(deleteSelectedActionButton))
                .addGap(58, 58, 58))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel14.setText("Variable action");

        jLabel15.setText("Target variable to be set by action:");

        jLabel16.setText("Value for target variable:");

        selectedActionButtonGroup.add(fixedValueRadiobutton);
        fixedValueRadiobutton.setSelected(true);
        fixedValueRadiobutton.setText("Fixed value:");

        selectedActionButtonGroup.add(contextValueRadiobutton);
        contextValueRadiobutton.setText("Value from other context variable:");

        addActionButton.setText("Add Action");
        addActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionButtonActionPerformed(evt);
            }
        });

        jLabel17.setText("Add action to variable:");

        actionVariableListComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionVariableListComboBoxActionPerformed(evt);
            }
        });

        jLabel21.setText("Trigger:");

        triggerButtonGroup.add(triggerTrueRadioButton);
        triggerTrueRadioButton.setSelected(true);
        triggerTrueRadioButton.setText("TRUE");

        triggerButtonGroup.add(triggerContainsRadioButton);
        triggerContainsRadioButton.setText("Contains");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(actionVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(triggerTrueRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(triggerContainsRadioButton)))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(targetActionTriggerVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(targetActionVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(10, 10, 10))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel15)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(contextValueRadiobutton)
                            .addComponent(fixedValueRadiobutton))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(targetActionValueVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(fixedValueField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addActionButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(30, 30, 30))
                            .addComponent(actionVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(targetActionVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(triggerTrueRadioButton)
                    .addComponent(triggerContainsRadioButton)
                    .addComponent(targetActionTriggerVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addActionButton))
                .addGap(98, 98, 98))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fixedValueRadiobutton)
                    .addComponent(fixedValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetActionValueVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contextValueRadiobutton))
                .addGap(120, 120, 120))
        );

        jLabel23.setText("Manual override value:");

        variableOverrideValue.setEditable(false);

        deleteVariableOverrideValueButton.setText("Delete");
        deleteVariableOverrideValueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteVariableOverrideValueButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(variableOverrideValue, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteVariableOverrideValueButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(47, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(variableOverrideValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteVariableOverrideValueButton))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        windowsMenu.setText("Windows");
        jMenuBar1.add(windowsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addGap(33, 33, 33)
                .addComponent(okButton)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void recentInputTokenisedListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_recentInputTokenisedListValueChanged
        String selectedValue = recentInputTokenisedList.getSelectedValue().toLowerCase();
        
        String dictionaryTerm = DialogMain.dicConverter.convertTermFromDic(selectedValue,true);
        if (!dictionaryTerm.equals(selectedValue))
            matchingDicTerm.setText(dictionaryTerm);
        else
            matchingDicTerm.setText("");
        
        addInputTermRadioButton.setSelected(true);
    }//GEN-LAST:event_recentInputTokenisedListValueChanged

    private void addInputToVariableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInputToVariableButtonActionPerformed
        String selectedVariable = (String)variableListComboBox.getSelectedItem();
        String dictionaryTerm = matchingDicTerm.getText();
        String selectedValue = recentInputTokenisedList.getSelectedValue();
        String rawInputHighlighted = rawRecentInput.getSelectedText();
        String manualCriteria = manualCriteriaInput.getText();
        String manualValueOverride = manualValueInput.getText();
        
        if (selectedVariable != null) {
        
            if (addInputTermRadioButton.isSelected()) {
                if (!contextVariables.get(selectedVariable).isVariableValueExist(selectedValue))
                    contextVariables.get(selectedVariable).addVariableValue(selectedValue);
            }
            else if (rawRecentInputRadioButton.isSelected()) {
                if (rawInputHighlighted.equals("")) {
                    showMessageDialog(this, "Cannot add highlighted text to variable " + selectedVariable + " as there is no highlighted text!");
                }
                else if (!contextVariables.get(selectedVariable).isVariableValueExist(rawInputHighlighted)) {
                    contextVariables.get(selectedVariable).addVariableValue(rawInputHighlighted);
                }
            }
            else if (addDicTermRadioButton.isSelected()) {
                if (dictionaryTerm.equals("")) {
                    showMessageDialog(this, "Cannot add dictionary term to variable " + selectedVariable + " as there is no dictionary term.");
                }
                else if (!contextVariables.get(selectedVariable).isVariableValueExist(dictionaryTerm)) {
                    contextVariables.get(selectedVariable).addVariableValue(dictionaryTerm);
                }
            }
            else if (manualCriteriaRadioButton.isSelected()) {
                if (manualCriteria.equals("")) {
                    showMessageDialog(this, "Cannot add empty criteria to variable " + selectedVariable + " as there is no dictionary term.");
                }
                else if (!contextVariables.get(selectedVariable).isVariableValueExist(manualCriteria)) {
                    contextVariables.get(selectedVariable).addVariableValue(manualCriteria);
                    if (!manualValueOverride.equals("")) {
                        contextVariables.get(selectedVariable).setVariableValueOverride(manualValueOverride);
                    }
                }
            }
            // we may have updated the currently viewed variable so show its values..
            getContextVariableValuesForList(variableNameList.getSelectedValue());
        }
        else {
            showMessageDialog(this, "No variable is selected..");
        }
    }//GEN-LAST:event_addInputToVariableButtonActionPerformed

    private void findHighlightedDictionaryMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findHighlightedDictionaryMatchButtonActionPerformed
        String highlightedText = rawRecentInput.getSelectedText();
        
        if (highlightedText != null && !highlightedText.equals("")) {
            highlightedText = highlightedText.toLowerCase();
            String dictionaryTerm = DialogMain.dicConverter.getFirstMatchingTermFromDic(highlightedText,true);
            if (dictionaryTerm != null) {
                if (!dictionaryTerm.equals(highlightedText))
                    matchingDicTerm.setText(dictionaryTerm);
                else
                    matchingDicTerm.setText("");
            } 
            else
                matchingDicTerm.setText("");
        }
    }//GEN-LAST:event_findHighlightedDictionaryMatchButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    
        for (Map.Entry me : this.systemContextVariables.entrySet()) {
            String varName = (String)me.getKey();
            ContextVariable cv = (ContextVariable)me.getValue();
            this.contextVariables.put(varName, cv);
        }
// Add the system context variables back in the list before we write them back to dialogmain..
        globalContextVariableManager.setContextVariables(this.contextVariables);
        DialogMain.globalContextVariableManager = globalContextVariableManager;

        if (parent != null) {
            ((AddRuleGUI)parent).updateContextVariableComboBox();
        }
        
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void deleteSelectedValueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedValueButtonActionPerformed
        String variableName = variableNameList.getSelectedValue();
        String variableValue = variableValueList.getSelectedValue();

        if (variableName != null && variableValue != null) {
            variableValueListModel.removeElement(variableValue);
            contextVariables.get(variableName).deleteVariableValue(variableValue);
        }

        if (variableValueListModel.isEmpty()) {
            deleteSelectedValueButton.setEnabled(false);
        }
        else {
            deleteSelectedValueButton.setEnabled(true);
        }
    }//GEN-LAST:event_deleteSelectedValueButtonActionPerformed

    private void addNewVariableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewVariableButtonActionPerformed
        String newVariable = newVariableName.getText();
        if (!newVariable.startsWith("@"))
            newVariable = "@" + newVariable;
        if (!newVariable.equals("")) {
            if (!variableNameListModel.contains(newVariable)) {
                variableNameListModel.addElement(newVariable);
                variableListComboBox.addItem(newVariable);
                variableListComboBox.setEnabled(true);
                
                actionVariableListComboBox.addItem(newVariable);
                targetActionVariableListComboBox.addItem(newVariable);
                targetActionTriggerVariableListComboBox.addItem(newVariable);
                targetActionValueVariableListComboBox.addItem(newVariable);
                
                addInputToVariableButton.setEnabled(true);
                ContextVariable aVariable = new ContextVariable();
                aVariable.setVariableName(newVariable);
                this.contextVariables.put(newVariable, aVariable);
                newVariableName.setText("");
                variableNameList.setSelectedValue(newVariable, true);
            }
        }
        else
        showMessageDialog(this,"No variable name specified to add..");

        if (!variableNameListModel.isEmpty())
        deleteSelectedVariableButton.setEnabled(true);
    }//GEN-LAST:event_addNewVariableButtonActionPerformed

    private void deleteSelectedVariableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedVariableButtonActionPerformed
        String variableName = variableNameList.getSelectedValue();

        if (variableName != null) {
            variableNameListModel.removeElement(variableName);
            variableListComboBox.removeItem(variableName);
            variableValueListModel.clear();
            contextVariables.remove(variableName);
            if (variableListComboBox.getItemCount() == 0) {
                variableListComboBox.setEnabled(false);
                addInputToVariableButton.setEnabled(false);
            }
        }
        if (variableNameListModel.isEmpty()) {
            deleteSelectedVariableButton.setEnabled(false);
            deleteSelectedValueButton.setEnabled(false);
        }
    }//GEN-LAST:event_deleteSelectedVariableButtonActionPerformed

    private void newVariableNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newVariableNameActionPerformed
        addNewVariableButtonActionPerformed(evt);
    }//GEN-LAST:event_newVariableNameActionPerformed

    private void variableNameListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_variableNameListValueChanged
        String selectedVariable = variableNameList.getSelectedValue();

        getContextVariableValuesForList(selectedVariable);
        getContextVariableActionsForList(selectedVariable);
        getContextVariableOverrideValueForTextField(selectedVariable);
    }//GEN-LAST:event_variableNameListValueChanged

    private void deleteSelectedActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedActionButtonActionPerformed
        String variableName = variableNameList.getSelectedValue();
        String variableAction = variableActionList.getSelectedValue();

        if (variableName != null && variableAction != null) {
            variableActionListModel.removeElement(variableAction);
            contextVariables.get(variableName).deleteVariableAction(variableAction);
        }

        if (variableActionListModel.isEmpty()) {
            deleteSelectedActionButton.setEnabled(false);
        }
        else {
            deleteSelectedActionButton.setEnabled(true);
        }
    }//GEN-LAST:event_deleteSelectedActionButtonActionPerformed

    private void findOtherInDictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findOtherInDictButtonActionPerformed
        String otherText = this.otherText.getText();
        
        if (otherText != null && !otherText.equals("")) {
            otherText = otherText.toLowerCase();
            String dictionaryTerm = DialogMain.dicConverter.getFirstMatchingTermFromDic(otherText,true);
            if (dictionaryTerm != null) {
                if (!dictionaryTerm.equals(otherText))
                    matchingDicTerm.setText(dictionaryTerm);
                else
                    matchingDicTerm.setText("");
            } 
            else
                matchingDicTerm.setText("");
        }
    }//GEN-LAST:event_findOtherInDictButtonActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void actionVariableListComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionVariableListComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_actionVariableListComboBoxActionPerformed

    private void addActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionButtonActionPerformed
        String fixedValueString = fixedValueField.getText();
        String contextSource = (String) actionVariableListComboBox.getSelectedItem();
        String contextTarget = (String) targetActionVariableListComboBox.getSelectedItem();
        String contextValue = (String) targetActionValueVariableListComboBox.getSelectedItem();
        String trigger;
        if (triggerTrueRadioButton.isSelected())
            trigger = "TRUE";
        else
            trigger = (String)targetActionTriggerVariableListComboBox.getSelectedItem();
        
        ContextVariableAction action;
        boolean okToGo = true;

        if (contextSource.equals("")) {
            showMessageDialog(this,"No variable was chosen to add an action to..");
            okToGo = false;
        }
        else if (contextTarget.equals("")) {
            showMessageDialog(this,"No variable target was chosen in the action..");
            okToGo = false;
        }

        if (okToGo) {
            if (fixedValueRadiobutton.isSelected()) {
                Logger.info("fixed selected..");
                if (!fixedValueString.equals("")) {
                    ((ContextVariable)contextVariables.get(contextSource)).addVariableActionFixed(contextTarget, fixedValueString, trigger);
                    Logger.info("Adding trigger:" + trigger + " to: " + contextSource + " for target:" + contextTarget + " with fixed value:" + fixedValueString);

                }
                else {
                    showMessageDialog(this,"No fixed value was specified..");
                }
            }
            else if (contextValueRadiobutton.isSelected()) {
                Logger.info("context selected..");

                if (!contextValue.equals("")) {
      
                    //look in the system context variables for a match first, otherwise try the user context variables..
                    //ContextVariable contextVariableValue = systemContextVariables.get(contextValue);
                    //if (contextVariableValue == null)
                    //contextVariableValue = contextVariables.get(contextValue);

                    contextVariables.get(contextSource).addVariableActionContext(contextTarget, contextValue, trigger);
                }
                else {
                    showMessageDialog(this,"Select a context value for the action..");
                }
            }
        }

        getContextVariableActionsForList(variableNameList.getSelectedValue());

    }//GEN-LAST:event_addActionButtonActionPerformed

    private void manualValueInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualValueInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_manualValueInputActionPerformed

    private void deleteVariableOverrideValueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteVariableOverrideValueButtonActionPerformed
        String variableName = variableNameList.getSelectedValue();

        if (variableName != null ) {
            contextVariables.get(variableName).setVariableValueOverride("");
            variableOverrideValue.setText("");
        }
    }//GEN-LAST:event_deleteVariableOverrideValueButtonActionPerformed

    private void manualValueInputFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_manualValueInputFocusGained
        manualCriteriaRadioButton.setSelected(true);
    }//GEN-LAST:event_manualValueInputFocusGained

    private void manualCriteriaInputFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_manualCriteriaInputFocusGained
        manualCriteriaRadioButton.setSelected(true);
    }//GEN-LAST:event_manualCriteriaInputFocusGained

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
            java.util.logging.Logger.getLogger(ContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ContextVariableGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ContextVariableGUI(args[0]).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> actionVariableListComboBox;
    private javax.swing.JButton addActionButton;
    private javax.swing.JRadioButton addDicTermRadioButton;
    private javax.swing.JRadioButton addInputTermRadioButton;
    private javax.swing.JButton addInputToVariableButton;
    private javax.swing.JButton addNewVariableButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton contextValueRadiobutton;
    private javax.swing.JButton deleteSelectedActionButton;
    private javax.swing.JButton deleteSelectedValueButton;
    private javax.swing.JButton deleteSelectedVariableButton;
    private javax.swing.JButton deleteVariableOverrideValueButton;
    private javax.swing.JButton findHighlightedDictionaryMatchButton;
    private javax.swing.JButton findOtherInDictButton;
    private javax.swing.JTextField fixedValueField;
    private javax.swing.JRadioButton fixedValueRadiobutton;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextField manualCriteriaInput;
    private javax.swing.JRadioButton manualCriteriaRadioButton;
    private javax.swing.JTextField manualValueInput;
    private javax.swing.JTextField matchingDicTerm;
    private javax.swing.JTextField newVariableName;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField otherText;
    private javax.swing.JTextArea rawRecentInput;
    private javax.swing.JRadioButton rawRecentInputRadioButton;
    private javax.swing.JList<String> recentInputTokenisedList;
    private javax.swing.ButtonGroup selectedActionButtonGroup;
    private javax.swing.ButtonGroup selectedTermButtonGroup;
    private javax.swing.JComboBox<String> targetActionTriggerVariableListComboBox;
    private javax.swing.JComboBox<String> targetActionValueVariableListComboBox;
    private javax.swing.JComboBox<String> targetActionVariableListComboBox;
    private javax.swing.ButtonGroup triggerButtonGroup;
    private javax.swing.JRadioButton triggerContainsRadioButton;
    private javax.swing.JRadioButton triggerTrueRadioButton;
    private javax.swing.JList<String> variableActionList;
    private javax.swing.JComboBox<String> variableListComboBox;
    private javax.swing.JList<String> variableNameList;
    private javax.swing.JTextField variableOverrideValue;
    private javax.swing.JList<String> variableValueList;
    private javax.swing.JMenu windowsMenu;
    // End of variables declaration//GEN-END:variables
}

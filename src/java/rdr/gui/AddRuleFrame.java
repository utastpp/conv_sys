/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.gui;

import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import rdr.apps.Main;
import rdr.cases.Case;
import rdr.cases.CornerstoneCase;
import rdr.cases.CornerstoneCaseSet;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.rules.RuleTreeModel;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.Condition;
import rdr.rules.Operator;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;

/**
 * This class is used to present GUI for adding a new rule
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class AddRuleFrame extends javax.swing.JFrame {
    private boolean isWrongConclusionSet=false;
    private static Case currentCase;
    private CornerstoneCase validatingCornerstoneCase;
    
    private static String mode;
    private Condition newCondition;
    private Conclusion newConclusion = new Conclusion();
    private ConclusionSet conclusionSet = new ConclusionSet();
      
    /**
     * Creates new form AddRuleFrame
     */
    public AddRuleFrame() {
        initComponents();
        
        getInferenceResult();
        learnerInit();
        updateCurrentCaseTable();
        updateAttrComboBox();
        updateRuleTreeGUI();
        valModeSetEnabled(false);
        updateConclusionComboBox();
    }
    
    private void getInferenceResult(){
        isWrongConclusionSet=false; 
        Main.workbench.deleteWrongConclusion();
        
        RuleSet inferenceResult = new RuleSet();
        switch (Main.domain.getReasonerType()) {            
            case "MCRDR":
                inferenceResult = (RuleSet) Main.workbench.getInferenceResult();
                if(mode.equals("exception")){
                    String[] wrongConclusionArray = inferenceResult.getConclusionSet().toStringArrayForGUIWithoutAddConclusion();
                    this.setEnabled(false);
                    wrongConclusionFrame.setVisible(true);
                    wrongConclusionList.setModel(new javax.swing.AbstractListModel() {
                        String[] strings = wrongConclusionArray;
                        @Override
                        public int getSize() { return strings.length; }
                        @Override
                        public Object getElementAt(int i) { return strings[i]; }
                    });
                    wrongConclusionList.setSelectedIndex(0);
                }   
                break;
            case "SCRDR":
                Rule inferenceRule = (Rule)Main.workbench.getInferenceResult();
                inferenceResult.addRule(inferenceRule);
                if(mode.equals("exception")){
                    Main.workbench.setWrongConclusion(inferenceRule.getConclusion());
                }   
                break;
        }
    }
    
    private void learnerInit(){       
        // set the possible conclusion into conclusion set
        conclusionSet.setConclusionSet(Main.KB.getConclusionSet());
        
        Rule newRule = new Rule();
        
        CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
        cornerstoneCaseSet.addCase(currentCase);
        
        newRule.setCornerstoneCaseSet(cornerstoneCaseSet);
        
        Main.workbench.setNewRule(newRule);
    }
    
    private void updateConclusionComboBox() {
        String[] conclusionList = conclusionSet.toStringArrayForGUI();
        
        conclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel(conclusionList));
        
        if(conclusionSet.getSize() > 0){
            String selectedConclusionName = (String) conclusionComboBox.getSelectedItem();
            newConclusion = conclusionSet.getConclusionByName(selectedConclusionName);
            Main.workbench.setNewRuleConclusion(newConclusion);
        } else {
            newConclusion = new Conclusion();
        }
    }
    
    private void updateCurrentCaseTable() {
        int attributeAmount = currentCase.getCaseStructure().getAttrAmount();
        
        String[] columnNames = new String[]{"Attribute", "Type", "Value"};
        
        
        Object[][] tempArray = currentCase.toObjectForGUIRowSortedWithType(attributeAmount);
        
        TableModel newModel = new DefaultTableModel(
                tempArray,columnNames
         ){
            boolean[] canEdit = new boolean []{false, false, false};    
            
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];                
            }
         };
        
        currentCaseTable.setModel(newModel);
        currentCaseTable.getTableHeader().setReorderingAllowed(false);
        
//        currentCaseTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        currentCaseTable.getTableHeader().setReorderingAllowed(false);
        
        if (currentCaseTable.getColumnModel().getColumnCount() > 0) {
            currentCaseTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            currentCaseTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            currentCaseTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        }
        
    }
    
    private void addConclusion() {
        String conclusionType = (String) conclusionTypeComboBox.getSelectedItem();
        String conclusionName = conclusionValueField.getText();
        
        IAttribute attribute = AttributeFactory.createAttribute(conclusionType);
        Value value = new Value(new ValueType(conclusionType), conclusionName);
        newConclusion = new Conclusion(attribute, value);
        
        if(conclusionSet.isExist(newConclusion)){
            showMessageDialog(null, "This conclusion name is already used.");
            
        } else {
            if(Main.workbench.getWrongConclusion()!=null) {
                if(newConclusion.getConclusionName().equals(Main.workbench.getWrongConclusion().getConclusionName())){                    
                    showMessageDialog(null, "This conclusion name is what you have chosen to modify.");
                } else {
                    //set new conclusion for the knowledge acquisition
                    Main.workbench.setNewRuleConclusion(newConclusion);

                    conclusionSet.addConclusion(newConclusion);

                    String[] newModelStr = new String[]{newConclusion.getConclusionName()};
                    conclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel(newModelStr));

                    newConclusionFrame.dispose();
                }
            } else {
                //set new conclusion for the knowledge acquisition
                Main.workbench.setNewRuleConclusion(newConclusion);

                conclusionSet.addConclusion(newConclusion);

                String[] newModelStr = new String[]{newConclusion.getConclusionName()};
                conclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel(newModelStr));

                newConclusionFrame.dispose();
            }
        }
    }
    
    
    private void updateConditionFields(String attrName) {
        IAttribute selectedAttr = currentCase.getCaseStructure().getAttributeByName(attrName);
        //set potential operators
        String[] operatorsList = selectedAttr.getPotentialOperators();

        conAttrComboBox.setSelectedItem(attrName);
        conOperComboBox.setModel(new javax.swing.DefaultComboBoxModel(operatorsList));
        
        if(selectedAttr.getValueType().getTypeName().equals("CATEGORICAL")){
            conValField.setText(currentCase.getValue(attrName).toString());
            conValField.setEditable(false);
        } else {
            conValField.setText(currentCase.getValue(attrName).toString());
            conValField.setEditable(true);
        }
    }
    
    
    private boolean constructNewCondition() {        
        boolean isValid = false;
        String newConAttr = (String) conAttrComboBox.getSelectedItem();
        String newConOper = (String) conOperComboBox.getSelectedItem();
        String newConVal = conValField.getText();
        
        newCondition = RuleBuilder.buildRuleCondition(currentCase.getCaseStructure(), newConAttr, newConOper, newConVal);
        
        // check whether the new condition is valid for this case
        isValid = newCondition.isSatisfied(currentCase);
         
        // TODO
//        Set rules = inferenceResult.getBaseSet().entrySet();
//        Iterator ruleIterator = rules.iterator();
//        while (ruleIterator.hasNext()) {
//            Map.Entry me = (Map.Entry) ruleIterator.next();
//            Rule rule = (Rule)me.getValue();
//            
//        }
//        
        
        return isValid;
    }
    
    private void addCondition() {
        //check whether the new condition value field is empty
        String newConVal = conValField.getText();
        if(!newConVal.equals("") && newConVal!=null){
            
            // construct new condition and check whether the condition is valid for this case            
            if(constructNewCondition()) {
                
                // add condition and check whether there is duplicating one
                if(Main.workbench.getLearner().addConditionToNewRule(newCondition)){
                    // if the condition is valid to add, then update condition table
                    updateConditionListTable();                
                } else {                    
                    showMessageDialog(null, "This condition is already added.");
                }
            } else {
                showMessageDialog(null, "Condition does not satisfy with the current case.");
            }
            
        } else {
            showMessageDialog(null, "Please enter condition value");
        }
        
    }
    
    private void deleteCondition() {
        int[] selectedRows = conditionListTable.getSelectedRows();
        int selectedAmount = conditionListTable.getSelectedRowCount();
        for (int i=0; i<selectedAmount; i++){
            Condition deletingCondition = new Condition();
            String conditionAttrName = (String) conditionListTable.getValueAt(selectedRows[i], 0);
            String conditionOper = (String) conditionListTable.getValueAt(selectedRows[i], 1);
            String conditionVal = (String) conditionListTable.getValueAt(selectedRows[i], 2);
                        
            IAttribute attr = currentCase.getCaseStructure().getAttributeByName(conditionAttrName);            
            Operator oper = Operator.stringToOperator(conditionOper);            
            Value val = new Value(attr.getValueType(), conditionVal);
            
            deletingCondition.setAttribute(attr);
            deletingCondition.setOperator(oper);
            deletingCondition.setValue(val);
            
            if(!Main.workbench.getLearner().deleteConditionFromNewRule(deletingCondition)){
                showMessageDialog(null, "This condition cannot be deleted.");
            } 
            
        }        
        updateConditionListTable();
    }
    
    private void updateConditionListTable() {        
        int conditionAmount = Main.workbench.getLearner().getConditionAmountFromNewRule();
        String[] columnNames = new String[]{"Attribute", "Operator", "Value"};
        
        
        Object[][] tempArray = Main.workbench.getLearner().getConditionSetFromNewRule().toObjectForGUI(conditionAmount);
        
        TableModel newModel = new DefaultTableModel(
                tempArray,columnNames
         ){
            boolean[] canEdit = new boolean []{false, false, false};    
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];                
            }
         };
        
        conditionListTable.setModel(newModel);
        conditionListTable.getTableHeader().setReorderingAllowed(false);
        
        conditionListTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        conditionListTable.getTableHeader().setReorderingAllowed(false);
        
        if (conditionListTable.getColumnModel().getColumnCount() > 0) {
            conditionListTable.getColumnModel().getColumn(0).setPreferredWidth(90);
            conditionListTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            conditionListTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        }
        if(conditionListTable.getRowCount()==0){
            validationButton.setEnabled(false);
        } else {
            validationButton.setEnabled(true);
        }
    }
    
    
    private void validateRule() {
        // clear cornerstonce case set (but the current case must be remained)
        CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
        cornerstoneCaseSet.addCase(currentCase);
        Main.workbench.getNewRule().setCornerstoneCaseSet(cornerstoneCaseSet);

        Main.workbench.getLearner().retrieveValidatingCaseSet();            
        CornerstoneCaseSet cornerstoneCases = Main.workbench.getLearner().getValidatingCaseSet();            
        if(cornerstoneCases.getCaseAmount()==0){
            addRule();
        } else {
            updateValCaseSetTable(cornerstoneCases);
            validatingCornerstoneCase = cornerstoneCases.getFirstCornerstoneCase();
            validateCornerstoneCase();

            updateValCaseTable(validatingCornerstoneCase);
            updateValConclusionList(validatingCornerstoneCase.getConclusionSet());

            validationAcceptButton.setEnabled(true);
//                generateDiffButton.setEnabled(true);
            if(cornerstoneCases.getCaseAmount()>1){;
                valCasePrevButton.setEnabled(false);
                valCaseNextButton.setEnabled(true);
            }

        }        
    }
    
    private void validateCornerstoneCase() {
        Main.workbench.setCurrentCase(validatingCornerstoneCase);
        Main.workbench.inferenceForValidation();

        switch (Main.domain.getReasonerType()) {            
            case "SCRDR":
                validatingCornerstoneCase.addRuleToWrongRuleSet((Rule) Main.workbench.getInferenceResult());
                break;
            case "MCRDR":                       
                validatingCornerstoneCase.setWrongRuleSet((RuleSet) Main.workbench.getInferenceResult());
                break;
        }
    }
    
    
    private void updateValCaseSetTable(CornerstoneCaseSet caseSet){
                
        int attributeAmount = currentCase.getCaseStructure().getAttrAmount();
        
        // +1 for case id
        int columnCount = attributeAmount+1;
        
        String[] columnNames = currentCase.getCaseStructure().getAttributeNameArrayWithCaseId();
        
        // for table cell editable stting
        boolean[] tempCanEdit = new boolean [columnCount];
        
        
        for(int i=0;i<columnCount;i++){
            tempCanEdit[i] = false;
        }
        
        // case amount
        int caseAmount = caseSet.getCaseAmount();
        
        // case id plus attribute amount 
        int colAmount = currentCase.getCaseStructure().getAttrAmount() + 1;
        
        Object[][] tempArray = caseSet.toObjectForGUI(caseAmount, colAmount);
        
        
        TableModel newModel = new DefaultTableModel(
                tempArray,columnNames
         ){
            boolean[] canEdit = tempCanEdit;
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
         };
        
        valCaseListTable.setModel(newModel);
        
        if(valCaseListTable.getColumnCount()>6){
            valCaseListTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        }
    }
    
    
    
    private void updateValCaseTable(Case aCase){
        int attributeAmount = aCase.getCaseStructure().getAttrAmount();
        
        String[] columnNames = new String[]{"Attribute", "Type", "Value"};
        
        
        Object[][] tempArray = aCase.toObjectForGUIRowWithType(attributeAmount);
        
        TableModel newModel = new DefaultTableModel(
                tempArray,columnNames
         ){
            boolean[] canEdit = new boolean []{false, false, false};    
            
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];                
            }
         };
        
        valCaseTable.setModel(newModel);
        valCaseTable.getTableHeader().setReorderingAllowed(false);
        
//        currentCaseTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        valCaseTable.getTableHeader().setReorderingAllowed(false);
        
        if (valCaseTable.getColumnModel().getColumnCount() > 0) {
            valCaseTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            valCaseTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            valCaseTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        }
    }
        
    private void updateValConclusionList(ConclusionSet aConclusionSet){
        valConclusionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = aConclusionSet.toStringArrayForGUIWithoutAddConclusion();
            @Override
            public int getSize() { return strings.length; }
            @Override
            public Object getElementAt(int i) { return strings[i]; }
        });
    }
        
    private void updateRuleTreeGUI(){
        Rule rootRule = Main.workbench.getFiredRules().getRootRule();
        
        RuleTreeModel aModel = new RuleTreeModel(rootRule);
        firedRuleTree.setModel(aModel);
        
        for (int i = 0; i < firedRuleTree.getRowCount(); i++) {
            firedRuleTree.expandRow(i);
        }
        
    }
    
    private void updateAttrComboBox() {
        String[] attrNames = currentCase.getCaseStructure().getAttributeNameArray();
        conAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(attrNames));
        
    }

    private void valModeSetEnabled(boolean bool){
        valCaseNextButton.setEnabled(bool);
        valCasePrevButton.setEnabled(bool);
        valCaseTable.setEnabled(bool);
        validationAcceptButton.setEnabled(bool);
        generateDiffButton.setEnabled(bool);
    }
    
    private void checkValidationCaseInterface(){
        CornerstoneCaseSet cornerstoneCases = Main.workbench.getLearner().getValidatingCaseSet();
        validationAcceptButton.setEnabled(true);
//        generateDiffButton.setEnabled(true);
        
        if(cornerstoneCases.hasNextCornerstoneCase(validatingCornerstoneCase)){
            valCaseNextButton.setEnabled(true);
        } else {
            valCaseNextButton.setEnabled(false);
        }
        if(!cornerstoneCases.isFirstCornerstoneCase(validatingCornerstoneCase)){
            valCasePrevButton.setEnabled(true);
        } else {
            valCasePrevButton.setEnabled(false);
        }
    }
    
    private void setWrongConclusion() {        
        String conclusionName = (String) wrongConclusionList.getSelectedValue();
        Conclusion wrongConclusion = conclusionSet.getConclusionByName(conclusionName);                
        Main.workbench.setWrongConclusion(wrongConclusion);
        
        wrongConclusionFrame.dispose();        
        this.setEnabled(true);
        this.requestFocus();
        
        learnerInit();
        updateConclusionComboBox();
    }
    
    
    private void addRule(){
        int confirmed = JOptionPane.showConfirmDialog(this,
                "There is no validation cases. Do you want to add the rule?", "Confirm Add Rule",
                JOptionPane.YES_NO_OPTION);
        //Close if user confirmed
        if (confirmed == JOptionPane.YES_OPTION)
        {
            Main.workbench.executeAddingRule(false);
            Main.KB.setRuleSet(Main.workbench.getRuleSet());
            Main.KB.setRootRuleTree();
            Main.KB.retrieveCornerstoneCaseSet();
            
            //dispose addRuleFrame
            this.dispose();
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

        newConclusionFrame = new javax.swing.JFrame();
        conclusionTypeComboBox = new javax.swing.JComboBox();
        conclusionValueField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        addConclusionButton = new javax.swing.JButton();
        conditionControlPopup = new javax.swing.JPopupMenu();
        deleteSelectedItem = new javax.swing.JMenuItem();
        wrongConclusionFrame = new javax.swing.JFrame();
        jScrollPane6 = new javax.swing.JScrollPane();
        wrongConclusionList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        wrongConclusionSelectButton = new javax.swing.JButton();
        jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        currentCaseTable = new javax.swing.JTable();
        currentCaseLabel = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        conclusionComboBox = new javax.swing.JComboBox();
        conclusionLabel = new javax.swing.JLabel();
        jSplitPane5 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        firedRuleTree = new javax.swing.JTree();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        conAttrComboBox = new javax.swing.JComboBox();
        conValField = new javax.swing.JTextField();
        conAddButton = new javax.swing.JButton();
        conOperComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        conditionListTable = new javax.swing.JTable();
        conditionLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        valCaseListTable = new javax.swing.JTable();
        validationButton = new javax.swing.JButton();
        valCaseListLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        validationAcceptButton = new javax.swing.JButton();
        valCasePrevButton = new javax.swing.JButton();
        valCaseNextButton = new javax.swing.JButton();
        generateDiffButton = new javax.swing.JButton();
        jSplitPane4 = new javax.swing.JSplitPane();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        valCaseTable = new javax.swing.JTable();
        valCaseLabel = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        valConclusionList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();

        newConclusionFrame.setTitle("New Conclusion");
        newConclusionFrame.setLocationByPlatform(true);
        newConclusionFrame.setMinimumSize(new java.awt.Dimension(410, 120));
        newConclusionFrame.setResizable(false);

        conclusionTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        conclusionValueField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                conclusionValueFieldKeyPressed(evt);
            }
        });

        jLabel1.setText("Conclusion Value Type");

        jLabel2.setText("Conclusion Value");

        addConclusionButton.setText("Add");
        addConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addConclusionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout newConclusionFrameLayout = new javax.swing.GroupLayout(newConclusionFrame.getContentPane());
        newConclusionFrame.getContentPane().setLayout(newConclusionFrameLayout);
        newConclusionFrameLayout.setHorizontalGroup(
            newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(newConclusionFrameLayout.createSequentialGroup()
                        .addGroup(newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(conclusionTypeComboBox, 0, 238, Short.MAX_VALUE)
                            .addComponent(conclusionValueField)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, newConclusionFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addConclusionButton)))
                .addContainerGap())
        );
        newConclusionFrameLayout.setVerticalGroup(
            newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(conclusionTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conclusionValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addConclusionButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        deleteSelectedItem.setText("Delete Selected");
        deleteSelectedItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedItemActionPerformed(evt);
            }
        });
        conditionControlPopup.add(deleteSelectedItem);

        wrongConclusionFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        wrongConclusionFrame.setTitle("Choose Wrong Conclusion");
        wrongConclusionFrame.setAlwaysOnTop(true);
        wrongConclusionFrame.setMinimumSize(new java.awt.Dimension(500, 300));
        wrongConclusionFrame.setResizable(false);
        wrongConclusionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                wrongConclusionFrameWindowClosed(evt);
            }
        });

        wrongConclusionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane6.setViewportView(wrongConclusionList);

        jLabel3.setText("Wrong Conclusions");

        jLabel4.setText("Select the conclusion that you want to modify");

        wrongConclusionSelectButton.setText("Select");
        wrongConclusionSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrongConclusionSelectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout wrongConclusionFrameLayout = new javax.swing.GroupLayout(wrongConclusionFrame.getContentPane());
        wrongConclusionFrame.getContentPane().setLayout(wrongConclusionFrameLayout);
        wrongConclusionFrameLayout.setHorizontalGroup(
            wrongConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wrongConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(wrongConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addGroup(wrongConclusionFrameLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(wrongConclusionFrameLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 151, Short.MAX_VALUE)
                        .addComponent(wrongConclusionSelectButton)))
                .addContainerGap())
        );
        wrongConclusionFrameLayout.setVerticalGroup(
            wrongConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wrongConclusionFrameLayout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(wrongConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(wrongConclusionSelectButton))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Rule");
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(840, 500));

        jSplitPane3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSplitPane3.setResizeWeight(0.9);

        jSplitPane2.setBorder(null);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.75);

        jSplitPane1.setBorder(null);
        jSplitPane1.setResizeWeight(0.4);

        currentCaseTable.setAutoCreateRowSorter(true);
        currentCaseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Attribute", "Type", "Value"
            }
        ));
        currentCaseTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        currentCaseTable.getTableHeader().setReorderingAllowed(false);
        currentCaseTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                currentCaseTableMouseReleased(evt);
            }
        });
        currentCaseTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                currentCaseTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(currentCaseTable);
        if (currentCaseTable.getColumnModel().getColumnCount() > 0) {
            currentCaseTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            currentCaseTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            currentCaseTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        }

        currentCaseLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        currentCaseLabel.setText("Current Case");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(currentCaseLabel)
                        .addGap(0, 127, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(currentCaseLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel6);

        conclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Conclusion" }));
        conclusionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conclusionComboBoxActionPerformed(evt);
            }
        });

        conclusionLabel.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        conclusionLabel.setText("Select Conclusion");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conclusionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conclusionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conclusionLabel)
                    .addComponent(conclusionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jSplitPane5.setBorder(null);
        jSplitPane5.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Rule A");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Rule B");
        treeNode1.add(treeNode2);
        firedRuleTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane4.setViewportView(firedRuleTree);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setText("Fired Rules");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane5.setLeftComponent(jPanel3);

        conAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute" }));
        conAttrComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conAttrComboBoxActionPerformed(evt);
            }
        });

        conAddButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        conAddButton.setText("Add");
        conAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conAddButtonActionPerformed(evt);
            }
        });

        conOperComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "==", "!=", ">=", "<=" }));

        conditionListTable.setAutoCreateRowSorter(true);
        conditionListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Attribute", "Operator", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        conditionListTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                conditionListTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(conditionListTable);

        conditionLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        conditionLabel.setText("New Rule Condition");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(conditionLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(conAttrComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(conOperComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(conValField, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(conAddButton)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conditionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conAttrComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(conOperComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(conValField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(conAddButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane5.setRightComponent(jPanel4);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane5)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane5))
        );

        jSplitPane1.setRightComponent(jPanel7);

        jSplitPane2.setLeftComponent(jSplitPane1);

        jScrollPane3.setAutoscrolls(true);

        valCaseListTable.setAutoCreateRowSorter(true);
        valCaseListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Case ID", "Attr 1", "Attr 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        valCaseListTable.setColumnSelectionAllowed(true);
        jScrollPane3.setViewportView(valCaseListTable);
        valCaseListTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        validationButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        validationButton.setText("Validate");
        validationButton.setEnabled(false);
        validationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationButtonActionPerformed(evt);
            }
        });

        valCaseListLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        valCaseListLabel.setText("Validation Case List");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(valCaseListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(validationButton))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valCaseListLabel)
                    .addComponent(validationButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setRightComponent(jPanel5);

        jSplitPane3.setLeftComponent(jSplitPane2);

        validationAcceptButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        validationAcceptButton.setText("Accept");
        validationAcceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationAcceptButtonActionPerformed(evt);
            }
        });

        valCasePrevButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        valCasePrevButton.setText("<<");
        valCasePrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valCasePrevButtonActionPerformed(evt);
            }
        });

        valCaseNextButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        valCaseNextButton.setText(">>");
        valCaseNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valCaseNextButtonActionPerformed(evt);
            }
        });

        generateDiffButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        generateDiffButton.setText("Generate Diff. List");
        generateDiffButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateDiffButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(valCasePrevButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(generateDiffButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(validationAcceptButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valCaseNextButton))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(valCasePrevButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(validationAcceptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(generateDiffButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(valCaseNextButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jSplitPane4.setBorder(null);
        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane4.setResizeWeight(0.5);

        valCaseTable.setAutoCreateRowSorter(true);
        valCaseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Attribute", "Type", "Value"
            }
        ));
        jScrollPane5.setViewportView(valCaseTable);

        valCaseLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        valCaseLabel.setText("Validating Case");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(valCaseLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(valCaseLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane4.setLeftComponent(jPanel9);

        jScrollPane7.setViewportView(valConclusionList);

        jLabel6.setText("Validating Case Conclusion");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
        );

        jSplitPane4.setRightComponent(jPanel10);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSplitPane4))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane3.setRightComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane3)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane3)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void conAttrComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conAttrComboBoxActionPerformed
        String attrName = conAttrComboBox.getSelectedItem().toString();
        updateConditionFields(attrName);
        
    }//GEN-LAST:event_conAttrComboBoxActionPerformed

    private void conAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conAddButtonActionPerformed
        if(newConclusion!=null && newConclusion.isSet()){
            addCondition();
        } else {
            showMessageDialog(null, "Please select the conclusion.");
        }
    }//GEN-LAST:event_conAddButtonActionPerformed

    private void currentCaseTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_currentCaseTableMouseReleased
        int selectedRow = currentCaseTable.getSelectedRow();
        if(selectedRow>-1){
            String attrName = (String) currentCaseTable.getValueAt(selectedRow, 0);
            updateConditionFields(attrName);
        }
    }//GEN-LAST:event_currentCaseTableMouseReleased

    private void currentCaseTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_currentCaseTableKeyReleased
        int selectedRow = currentCaseTable.getSelectedRow();
        if(selectedRow>-1){
            String attrName = (String) currentCaseTable.getValueAt(selectedRow, 0);
            updateConditionFields(attrName);
        }
    }//GEN-LAST:event_currentCaseTableKeyReleased

    private void conclusionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conclusionComboBoxActionPerformed
        if(conclusionComboBox.getItemCount()==conclusionComboBox.getSelectedIndex()+1){
            // new conclusion frame
            newConclusionFrame.setVisible(true);
            conclusionValueField.setText("");
            conclusionTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"TEXT"}));
            conclusionTypeComboBox.setEnabled(false);
            conclusionValueField.requestFocus();
            conclusionTypeComboBox.setSelectedItem("TEXT");
        } else {
            String selectedConclusionName = (String) conclusionComboBox.getSelectedItem();
            newConclusion = conclusionSet.getConclusionByName(selectedConclusionName);
            Main.workbench.setNewRuleConclusion(newConclusion);
        }
    }//GEN-LAST:event_conclusionComboBoxActionPerformed

    private void validationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationButtonActionPerformed
        validateRule();
    }//GEN-LAST:event_validationButtonActionPerformed

    private void conditionListTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_conditionListTableMouseReleased
         if(conditionListTable.getSelectedRowCount()>0){
            if (SwingUtilities.isRightMouseButton(evt) && evt.getClickCount() == 1) {
                if(evt.isPopupTrigger()){
                    conditionControlPopup.show(evt.getComponent(),evt.getX(),evt.getY());
                }

            }
        }
        
    }//GEN-LAST:event_conditionListTableMouseReleased

    private void deleteSelectedItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedItemActionPerformed
        deleteCondition();
    }//GEN-LAST:event_deleteSelectedItemActionPerformed

    private void addConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConclusionButtonActionPerformed
        addConclusion();
    }//GEN-LAST:event_addConclusionButtonActionPerformed

    private void conclusionValueFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_conclusionValueFieldKeyPressed
        int key=evt.getKeyCode();
        if(key==KeyEvent.VK_ENTER)
        {     
            addConclusion();
        }
    }//GEN-LAST:event_conclusionValueFieldKeyPressed

    private void wrongConclusionSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrongConclusionSelectButtonActionPerformed
        if(wrongConclusionList.getSelectedIndex()>-1){
            isWrongConclusionSet = true;
            setWrongConclusion();
        }
            //System.out.println(wrongConclusionList.getSelectedIndex());
    }//GEN-LAST:event_wrongConclusionSelectButtonActionPerformed

    private void wrongConclusionFrameWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_wrongConclusionFrameWindowClosed
        if(!isWrongConclusionSet){
            //System.out.println(isWrongConclusionSet);
            this.dispose();
        }
    }//GEN-LAST:event_wrongConclusionFrameWindowClosed

    private void validationAcceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationAcceptButtonActionPerformed
        Main.workbench.getNewRule().addCornerstoneCase(validatingCornerstoneCase);
        
        Main.workbench.getLearner().getValidatingCaseSet().deleteCornerstoneCase(validatingCornerstoneCase);
        CornerstoneCaseSet cornerstoneCases = Main.workbench.getLearner().getValidatingCaseSet();
        if(cornerstoneCases.getCaseAmount()==0){
            addRule();
        } else {
            validatingCornerstoneCase = Main.workbench.getLearner().getValidatingCaseSet().getFirstCornerstoneCase();
            updateValCaseSetTable(cornerstoneCases);
            validateCornerstoneCase();
            updateValCaseTable(validatingCornerstoneCase);
            checkValidationCaseInterface();
        }
    }//GEN-LAST:event_validationAcceptButtonActionPerformed

    private void valCasePrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valCasePrevButtonActionPerformed
        validatingCornerstoneCase = Main.workbench.getLearner().getValidatingCaseSet().getPreviousCornerstoneCase(validatingCornerstoneCase);
        
        CornerstoneCaseSet cornerstoneCases = Main.workbench.getLearner().getValidatingCaseSet();
        updateValCaseSetTable(cornerstoneCases);
        validateCornerstoneCase();
        updateValCaseTable(validatingCornerstoneCase);
        checkValidationCaseInterface();
    }//GEN-LAST:event_valCasePrevButtonActionPerformed

    private void valCaseNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valCaseNextButtonActionPerformed
        validatingCornerstoneCase = Main.workbench.getLearner().getValidatingCaseSet().getNextCornerstoneCase(validatingCornerstoneCase);
        CornerstoneCaseSet cornerstoneCases = Main.workbench.getLearner().getValidatingCaseSet();
        updateValCaseSetTable(cornerstoneCases);
        validateCornerstoneCase();
        updateValCaseTable(validatingCornerstoneCase);
        checkValidationCaseInterface();
    }//GEN-LAST:event_valCaseNextButtonActionPerformed

    private void generateDiffButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateDiffButtonActionPerformed
        // generate full difference list
        Main.workbench.getLearner().generateFullDifferenceList();
        if(Main.workbench.getLearner().getFullDifferenceList().size()>0){
            //use full list
        } else {
            // full list is empty (cannot be generated), generate partial difference list
            
        }
        
        
        
    }//GEN-LAST:event_generateDiffButtonActionPerformed

    /**
     *
     * @param aMode
     * @param aCase
     */
    public static void execute(String aMode, Case aCase) {
        currentCase = aCase;
        if(aMode.equals("add")){
            mode = "alter"; 
        } else {
            mode = "exception";
        }
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Windows is not available, stay with the default look and feel.
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
            java.util.logging.Logger.getLogger(AddRuleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddRuleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddRuleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddRuleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AddRuleFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addConclusionButton;
    private javax.swing.JButton conAddButton;
    private javax.swing.JComboBox conAttrComboBox;
    private javax.swing.JComboBox conOperComboBox;
    private javax.swing.JTextField conValField;
    private javax.swing.JComboBox conclusionComboBox;
    private javax.swing.JLabel conclusionLabel;
    private javax.swing.JComboBox conclusionTypeComboBox;
    private javax.swing.JTextField conclusionValueField;
    private javax.swing.JPopupMenu conditionControlPopup;
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JTable conditionListTable;
    private javax.swing.JLabel currentCaseLabel;
    private javax.swing.JTable currentCaseTable;
    private javax.swing.JMenuItem deleteSelectedItem;
    private javax.swing.JTree firedRuleTree;
    private javax.swing.JButton generateDiffButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JSplitPane jSplitPane5;
    private javax.swing.JFrame newConclusionFrame;
    private javax.swing.JLabel valCaseLabel;
    private javax.swing.JLabel valCaseListLabel;
    private javax.swing.JTable valCaseListTable;
    private javax.swing.JButton valCaseNextButton;
    private javax.swing.JButton valCasePrevButton;
    private javax.swing.JTable valCaseTable;
    private javax.swing.JList valConclusionList;
    private javax.swing.JButton validationAcceptButton;
    private javax.swing.JButton validationButton;
    private javax.swing.JFrame wrongConclusionFrame;
    private javax.swing.JList wrongConclusionList;
    private javax.swing.JButton wrongConclusionSelectButton;
    // End of variables declaration//GEN-END:variables
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.gui;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import rdr.apps.Main;
import rdr.cases.Case;
import rdr.cases.CaseLoader;
import rdr.cases.CaseSet;
import rdr.cases.CaseStructure;
import rdr.domain.DomainLoader;
import rdr.inductrdr.InductRDR;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.rules.RuleTreeModel;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleLoader;
import rdr.rules.RuleSet;
import rdr.sqlite.inductSqliteOperation;

/**
 * This class is used to present GUI for main frame for this program
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class MainFrame extends javax.swing.JFrame {
    private final CaseStructure caseStructure;
    private int selectedCaseId;
    private static String domainName;
    private boolean isDefaultSet=false;
    private static boolean isInductExecuted;
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        domainNameLabel.setText(domainName);
        //reasonerLabel.setText(Main.workbench.getReasoner().getReasonerMethod());    DPHJun9   
        caseStructure = Main.domain.getCaseStructure();    
       
        initRuleBase();
        setRuleTree(Main.KB.getRootRule());
        updateFiredRuleTree(Main.workbench.getFiredRules().getRootRule());
        setCaseTable();
        setConclusionComboBox();
        if(MainFrame.isInductExecuted){
            getInductResult();
        }
    }
    
    private void initRuleBase(){
        Main.workbench.setRuleSet(Main.KB);
        
        switch (Main.workbench.getReasonerType()) {
            case "MCRDR":
                {
                    RuleSet inferenceResult = new RuleSet();
                    inferenceResult.setRootRule(Main.KB.getRootRule());
                    Main.workbench.setInferenceResult(inferenceResult);
                    break;
                }
            case "SCRDR":
                {
                    Main.workbench.setInferenceResult(Main.KB.getRootRule());
                    break;
                }
        }
    }
    
    private void openDefaultConclusionFrame(){
        if(!Main.KB.getRootRule().isRuleValid()) {
            if(Main.KB.getRootRule().getConclusion().getConclusionName().equals("")){
                
                int confirmed = JOptionPane.showConfirmDialog(this, 
                   "Would you like to add default conclusion?", "Add Default Conclusion?", 
                   JOptionPane.YES_NO_OPTION); 

                //Close if user confirmed 
                if (confirmed == JOptionPane.YES_OPTION) {
                   this.setEnabled(false);
                   conclusionValueField.setText("");
                   conclusionTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"TEXT"}));
                   conclusionTypeComboBox.setEnabled(false);
                   conclusionValueField.requestFocus();
                   conclusionTypeComboBox.setSelectedItem("TEXT");
                   defaultConclusionFrame.setVisible(true);
                   this.setEnabled(false);
                } else {
                    RuleLoader.deleteDefaultRule();
                    RuleLoader.insertRule(0, Main.KB.getRootRule(), -1);
                }
            }
        }
    }
        
    /**
     *
     * @param rootRule
     */
    public static void setRuleTree(Rule rootRule) {
        RuleTreeModel aModel = new RuleTreeModel(rootRule);
        ruleTree.setModel(aModel);
        for (int i = 0; i < ruleTree.getRowCount(); i++) {
            ruleTree.expandRow(i);
        }
    }
    
    /**
     *
     */
    public static void setConclusionComboBox() {
        ConclusionSet conclusionSet = Main.KB.getConclusionSet();
        String[] conclusionList = conclusionSet.toStringArrayForGUIWithoutAddConclusion();
        
        conclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel(conclusionList));
//        if(conclusionSet.getSize()==0){
//            displayCasesButton.setEnabled(false);
//        } else {
//            displayCasesButton.setEnabled(true);
//        }
    }
    
    private void setCaseTable() {
        
        int attributeAmount = caseStructure.getAttrAmount();
        
        // +1 for case id
        int columnCount = attributeAmount+1;
        
        String[] columnNames = caseStructure.getAttributeNameArrayWithCaseId();
        
        // for table cell editable stting
        boolean[] tempCanEdit = new boolean [columnCount];
        
        
        for(int i=0;i<columnCount;i++){
            tempCanEdit[i] = false;
        }
        
        // case amount
        int caseAmount = Main.allCaseSet.getCaseAmount();
        
        // case id plus attribute amount 
        int colAmount = caseStructure.getAttrAmount() + 1;
        
        Object[][] tempArray = Main.allCaseSet.toObjectForGUI(caseAmount, colAmount);
        
        //TODO!!  to get value as its value type
//        String[] attrNames = caseStructure.getAttributeNameArray();
//        
//        Class[] types = new Class [] {
//                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
//            };
        
        Class[] types = new Class [colAmount];
        
        Set set = Main.domain.getCaseStructure().getStructureBase().entrySet();
        
        int i=0;
        types[i] = java.lang.Integer.class; 
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            IAttribute attr = (IAttribute) me.getValue();
            i++;
            types[i] = attr.getClass();
        }
        
        
        TableModel newModel = new DefaultTableModel(
                tempArray,columnNames
         ){
            boolean[] canEdit = tempCanEdit;
            
            //TODO
            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
         };
        
        caseTable.setModel(newModel);
        
        if(caseTable.getColumnCount()>6){
            caseTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        } 
    }
    
    
    
    
    private void setCaseEditorTable(String mode, int selectedCaseId) {
        int thisCaseId = 0;
        switch (mode) {
            case "add":
                thisCaseId = Main.allCaseSet.getNewCaseId();
                break;
            case "edit":
                thisCaseId = selectedCaseId;
                break;
        }
        int attributeAmount = caseStructure.getAttrAmount();
        
        // +1 for case id
        int columnCount = attributeAmount+1;
        
        String[] attrNames = caseStructure.getAttributeNameArray();
        String[] columnNames = caseStructure.getAttributeNameArrayWithCaseId();
        
        // for table cell editable stting
        boolean[] tempCanEdit = new boolean [columnCount];
        
        // categorical attribute column index 
        ArrayList<Integer> categoricalColumnIndex = new ArrayList<>();
        
        Object[][] tempArray = new Object[1][columnCount];
        
        for(int i=0; i<columnCount; i++){
            // if i equals 0 cell editable and set integer, otherwise not editable and get attribute classs
            if (i==0){
                tempCanEdit[i] = false;
            } else {                
                tempCanEdit[i] = true;
                
                IAttribute currAttr = caseStructure.getAttributeByName(attrNames[i-1]);
                if(currAttr.isThisType("CATEGORICAL")){
                    categoricalColumnIndex.add(i-1);
                }
            }
        }
        
        //if this is add mode, set case id only
        tempArray[0][0] = thisCaseId;       
        
        if(mode.equals("edit")){
            Case aCase = Main.allCaseSet.getCaseById(thisCaseId);
            tempArray = aCase.toObjectOnlyValueWithCaseId(attributeAmount);
        }
        
        TableModel newModel = new DefaultTableModel(
                tempArray,columnNames
         ){
            boolean[] canEdit = tempCanEdit;            
            
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];                
            }
         };
        
        caseEditorTable.setModel(newModel);
        caseEditorTable.getTableHeader().setReorderingAllowed(false);
        
        for (Integer categoricalColumnIndexInstance : categoricalColumnIndex) {
            setComboBoxForCategorical(categoricalColumnIndexInstance);
        }
        
        if(caseEditorTable.getColumnCount()>6){
            caseEditorTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        } 
        
    }
    
    private void setComboBoxForCategorical(int targetColumnIndex) {
        JComboBox comboBox = new JComboBox();
        
        String[] attrNames = caseStructure.getAttributeNameArray();
        IAttribute currAttr = caseStructure.getAttributeByName(attrNames[targetColumnIndex]);
        
        ArrayList <String> categoryList = currAttr.getCategoricalValues();
        
        for (String categoryList1 : categoryList) {
            comboBox.addItem(categoryList1);
        }
        
        TableColumn targetColumn = caseEditorTable.getColumnModel().getColumn(targetColumnIndex+1);
        
        targetColumn.setCellEditor(new DefaultCellEditor(comboBox));
        
        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer(); 
        targetColumn.setCellRenderer(renderer);
    }
    
    private void updateUnloadedData(Case selectedCase){
            try {
                    selectedCase = CaseLoader.caseLoad(selectedCase.getCaseId());
                    Main.allCaseSet.replaceCase(selectedCase);
                    int selectedRow = caseTable.getSelectedRow();
                    ListSelectionModel aModel = caseTable.getSelectionModel();
                    setCaseTable();
                    caseTable.setSelectionModel(aModel);
                    caseTable.setRowSelectionInterval(selectedRow, selectedRow);
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void inferenceCase(){
        Case selectedCase = Main.allCaseSet.getCaseById(selectedCaseId);
        
        if(selectedCase.getValues().size()==0){
            updateUnloadedData(selectedCase);
            selectedCase = Main.allCaseSet.getCaseById(selectedCaseId);
        } 
        
        Main.workbench.setCurrentCase(selectedCase);
        Main.workbench.inference();

//        Main.workbench.setInitialInferenceResult((RuleSet) Main.workbench.getInferenceResult());
//        
//        if(Main.workbench.getStackedInferenceResult().getIsValidStackExist()){
//            Main.workbench.inferenceWithInitialResult();
//        } else {            
//            Main.workbench.inference();
//        }
        
        boolean onlyRootRuleFired = true;
        
        ConclusionSet inferencedConclusionSet = new ConclusionSet();
        switch (Main.workbench.getReasonerType()) {
            case "MCRDR":
                {
                    // get inference result
                    RuleSet inferenceResult = (RuleSet) Main.workbench.getInferenceResult();
                    
                    // if there is no root rule in inference result, this means other rules are fired
                    if(inferenceResult.getRuleById(0) == null) {
                        onlyRootRuleFired = false;
                    }
                    
                    //set inference conclusion set
                    inferencedConclusionSet = inferenceResult.getConclusionSet();
                    break;
                }
            case "SCRDR":
                {
                    // get inference result
                    Rule inferenceResult = (Rule) Main.workbench.getInferenceResult();
                    
                    // if inference result rule id is not 0, this means other rules are fired
                    if(inferenceResult.getRuleId()!=0){
                        onlyRootRuleFired = false;
                        addConclusionButton.setEnabled(false);
                    } else {
                        addConclusionButton.setEnabled(true);
                    }
                    
                    //set inference conclusion set
                    inferencedConclusionSet.addConclusion(inferenceResult.getConclusion());
                    break;
                }
        }
        updateInferencedConclusionList(inferencedConclusionSet);
        updateFiredRuleTree(Main.workbench.getFiredRules().getRootRule());
        
     
        // if there is firedRules, disables edit case button and enables modify conclusion button
        if(!Main.workbench.getFiredRules().isEmpty()){            
            ruleTreeTabPanel.setSelectedIndex(1);
            editCaseButton.setEnabled(false);
            if(onlyRootRuleFired){
                modifyConclusionButton.setEnabled(false);
            } else {
                modifyConclusionButton.setEnabled(true);                
            }
        // Otherwise, opposite way
        } else {
            editCaseButton.setEnabled(true);
            modifyConclusionButton.setEnabled(false);
        }
    }
    
    private void updateInferencedConclusionList(ConclusionSet conclusionSet){
        inferencedConclusionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = conclusionSet.toStringArrayForGUIWithoutAddConclusion();
            @Override
            public int getSize() { return strings.length; }
            @Override
            public Object getElementAt(int i) { return strings[i]; }
        });
    }
    
    
    private void updateFiredRuleTree(Rule rootRule) {
        RuleTreeModel aModel = new RuleTreeModel(rootRule);
        firedRuleTree.setModel(aModel);
        for (int i = 0; i < firedRuleTree.getRowCount(); i++) {
            firedRuleTree.expandRow(i);
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
        
        String condStr=conditionSet.toString();
//        if(conditionSet.getConditionAmount() > 0){
//            // Get an iterator
//            Iterator caseIterator = conditionSet.getBase().iterator();
//            int cnt=0;
//            while (caseIterator.hasNext()) {
//                if (cnt!=0){
//                    condStr += " & ";
//                }
//                Condition condition = (Condition)caseIterator.next();
//                condStr += condition.toString();
//                cnt++;
//            }       
//        }
        
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
        conclusionField.setText(conclusionStr);
        
        
    }
    
    
    
    private void addDefaultConclusion() {
        String conclusionType = (String) conclusionTypeComboBox.getSelectedItem();
        String conclusionName = conclusionValueField.getText();
        
        IAttribute attribute = AttributeFactory.createAttribute(conclusionType);
        Value value = new Value(new ValueType(conclusionType), conclusionName);
        Conclusion defaultConclusion = new Conclusion(attribute, value);
        
        Main.KB.getRuleById(0).setConclusion(defaultConclusion);
        Main.KB.setRootRule(Main.KB.getRuleById(0));
        
        //insert default conclusion
        RuleLoader.insertRuleConclusions(0, defaultConclusion);
        
        //insert rule into db
        RuleLoader.deleteDefaultRule();
        RuleLoader.insertRule(0, Main.KB.getRuleById(0), 0);
        
        isDefaultSet = true;
        
        setRuleTree(Main.KB.getRootRule());
        
        defaultConclusionFrame.dispose();
        
        
        this.setEnabled(true);
        this.requestFocus();
    }
    
    
    private void displayAllCasesOfConclusion(){
        
    }    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        reasonerChangerFrame = new javax.swing.JFrame();
        reasonerComboBox = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        reasonerChangeButton = new javax.swing.JButton();
        caseEditorFrame = new javax.swing.JFrame();
        jScrollPane4 = new javax.swing.JScrollPane();
        caseEditorTable = new javax.swing.JTable();
        caseEditorFrameAddButton = new javax.swing.JButton();
        importFileChooser = new javax.swing.JFileChooser();
        defaultConclusionFrame = new javax.swing.JFrame();
        conclusionTypeComboBox = new javax.swing.JComboBox();
        conclusionValueField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        addConclusionButton1 = new javax.swing.JButton();
        testingArffChooser = new javax.swing.JFileChooser();
        jLabel9 = new javax.swing.JLabel();
        domainNameLabel = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        reasonerLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        ruleTreeTabPanel = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        ruleTree = new javax.swing.JTree();
        jScrollPane5 = new javax.swing.JScrollPane();
        firedRuleTree = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        conclusionComboBox = new javax.swing.JComboBox();
        displayCasesButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        caseTable = new javax.swing.JTable();
        addCaseButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        selectedCaseField = new javax.swing.JTextField();
        modifyConclusionButton = new javax.swing.JButton();
        addConclusionButton = new javax.swing.JButton();
        editCaseButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
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
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        inferencedConclusionList = new javax.swing.JList();
        jLabel13 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel15 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newDomainOption = new javax.swing.JMenuItem();
        editCaseStructureMenuItem = new javax.swing.JMenuItem();
        loadDomainOption = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitOption = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        inductRDRExecuteOption = new javax.swing.JMenuItem();
        testWithNewCaseSetOption = new javax.swing.JMenuItem();
        calculatePerformanceOption = new javax.swing.JMenuItem();

        reasonerChangerFrame.setTitle("Change Reasoner Type");
        reasonerChangerFrame.setMinimumSize(new java.awt.Dimension(320, 90));
        reasonerChangerFrame.setResizable(false);

        reasonerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SCRDR", "MCRDR" }));

        jLabel11.setText("Reasoner Type");

        reasonerChangeButton.setText("Change");

        javax.swing.GroupLayout reasonerChangerFrameLayout = new javax.swing.GroupLayout(reasonerChangerFrame.getContentPane());
        reasonerChangerFrame.getContentPane().setLayout(reasonerChangerFrameLayout);
        reasonerChangerFrameLayout.setHorizontalGroup(
            reasonerChangerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reasonerChangerFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reasonerChangerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reasonerChangerFrameLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(reasonerChangerFrameLayout.createSequentialGroup()
                        .addComponent(reasonerComboBox, 0, 192, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reasonerChangeButton)))
                .addContainerGap())
        );
        reasonerChangerFrameLayout.setVerticalGroup(
            reasonerChangerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reasonerChangerFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(reasonerChangerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reasonerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reasonerChangeButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        caseEditorFrame.setTitle("Case Editor");
        caseEditorFrame.setMinimumSize(new java.awt.Dimension(650, 170));

        caseEditorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Attribute", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        caseEditorTable.setColumnSelectionAllowed(true);
        caseEditorTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                caseEditorTablePropertyChange(evt);
            }
        });
        jScrollPane4.setViewportView(caseEditorTable);
        caseEditorTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        caseEditorFrameAddButton.setText("Add Case");
        caseEditorFrameAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                caseEditorFrameAddButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout caseEditorFrameLayout = new javax.swing.GroupLayout(caseEditorFrame.getContentPane());
        caseEditorFrame.getContentPane().setLayout(caseEditorFrameLayout);
        caseEditorFrameLayout.setHorizontalGroup(
            caseEditorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(caseEditorFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(caseEditorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, caseEditorFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(caseEditorFrameAddButton)))
                .addContainerGap())
        );
        caseEditorFrameLayout.setVerticalGroup(
            caseEditorFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(caseEditorFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(caseEditorFrameAddButton)
                .addContainerGap())
        );

        defaultConclusionFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        defaultConclusionFrame.setTitle("New Conclusion");
        defaultConclusionFrame.setAlwaysOnTop(true);
        defaultConclusionFrame.setMinimumSize(new java.awt.Dimension(410, 120));
        defaultConclusionFrame.setResizable(false);
        defaultConclusionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                defaultConclusionFrameWindowClosed(evt);
            }
        });

        conclusionTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        conclusionValueField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                conclusionValueFieldKeyPressed(evt);
            }
        });

        jLabel1.setText("Conclusion Value Type");

        jLabel14.setText("Conclusion Value");

        addConclusionButton1.setText("Add");
        addConclusionButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addConclusionButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout defaultConclusionFrameLayout = new javax.swing.GroupLayout(defaultConclusionFrame.getContentPane());
        defaultConclusionFrame.getContentPane().setLayout(defaultConclusionFrameLayout);
        defaultConclusionFrameLayout.setHorizontalGroup(
            defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defaultConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(defaultConclusionFrameLayout.createSequentialGroup()
                        .addGroup(defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(conclusionTypeComboBox, 0, 238, Short.MAX_VALUE)
                            .addComponent(conclusionValueField)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, defaultConclusionFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(addConclusionButton1)))
                .addContainerGap())
        );
        defaultConclusionFrameLayout.setVerticalGroup(
            defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defaultConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(conclusionTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(defaultConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conclusionValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addConclusionButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("RDR Engine");
        setAutoRequestFocus(false);
        setLocationByPlatform(true);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel9.setText("Domain: ");

        domainNameLabel.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
        domainNameLabel.setText("n/a");

        jLabel10.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel10.setText("Current Reasoner: ");

        reasonerLabel.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        reasonerLabel.setText("_____");

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSplitPane1.setResizeWeight(0.2);

        jScrollPane3.setMinimumSize(new java.awt.Dimension(70, 23));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Root");
        ruleTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        ruleTree.setAutoscrolls(true);
        ruleTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                ruleTreeValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(ruleTree);

        ruleTreeTabPanel.addTab("KB Rules", jScrollPane3);

        treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Root");
        firedRuleTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        firedRuleTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                firedRuleTreeValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(firedRuleTree);

        ruleTreeTabPanel.addTab("Fired Rules", jScrollPane5);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ruleTreeTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ruleTreeTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel2.setText("Conclusion List");

        conclusionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "There is no conclusion" }));
        conclusionComboBox.setEnabled(false);

        displayCasesButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        displayCasesButton.setText("Display only this");
        displayCasesButton.setEnabled(false);
        displayCasesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayCasesButtonActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel4.setText("Case Browser");

        caseTable.setAutoCreateRowSorter(true);
        caseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Case ID", "Attr 1", "Attr 2", "Attr 3"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        caseTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        caseTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                caseTableMouseReleased(evt);
            }
        });
        caseTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                caseTableComponentMoved(evt);
            }
        });
        caseTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                caseTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(caseTable);

        addCaseButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        addCaseButton.setText("Add New Case");
        addCaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCaseButtonActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setText("Selected Case ID");

        selectedCaseField.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        modifyConclusionButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        modifyConclusionButton.setText("Modify Conlusion");
        modifyConclusionButton.setEnabled(false);
        modifyConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyConclusionButtonActionPerformed(evt);
            }
        });

        addConclusionButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        addConclusionButton.setText("Add Conlusion");
        addConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addConclusionButtonActionPerformed(evt);
            }
        });

        editCaseButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        editCaseButton.setText("Edit Case");
        editCaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCaseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editCaseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addCaseButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(conclusionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayCasesButton))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(selectedCaseField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifyConclusionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addConclusionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conclusionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(displayCasesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(addCaseButton)
                    .addComponent(editCaseButton))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(selectedCaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modifyConclusionButton)
                    .addComponent(addConclusionButton))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jSplitPane1)
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jSplitPane1))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ruleIdField)
                            .addComponent(csCaseField)
                            .addComponent(conclusionField)
                            .addComponent(conditionField)))
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
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
                .addGap(6, 6, 6)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(conclusionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        inferencedConclusionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Case Not Selected" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(inferencedConclusionList);

        jLabel13.setText("Inferenced Conclusion(s)");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGap(0, 144, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel15.setText("ver 1.1");

        fileMenu.setText("File");
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        newDomainOption.setText("New Domain Model");
        newDomainOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDomainOptionActionPerformed(evt);
            }
        });
        fileMenu.add(newDomainOption);

        editCaseStructureMenuItem.setText("Edit Case Structure");
        editCaseStructureMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCaseStructureMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(editCaseStructureMenuItem);

        loadDomainOption.setText("Load Domain Model");
        fileMenu.add(loadDomainOption);
        fileMenu.add(jSeparator2);

        exitOption.setText("Exit");
        exitOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitOptionActionPerformed(evt);
            }
        });
        fileMenu.add(exitOption);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuActionPerformed(evt);
            }
        });

        inductRDRExecuteOption.setText("Create rule tree based on dataset (Induct RDR)");
        inductRDRExecuteOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inductRDRExecuteOptionActionPerformed(evt);
            }
        });
        editMenu.add(inductRDRExecuteOption);

        testWithNewCaseSetOption.setText("Test with New Case Set");
        testWithNewCaseSetOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testWithNewCaseSetOptionActionPerformed(evt);
            }
        });
        editMenu.add(testWithNewCaseSetOption);

        calculatePerformanceOption.setText("Calculate Performance");
        calculatePerformanceOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculatePerformanceOptionActionPerformed(evt);
            }
        });
        editMenu.add(calculatePerformanceOption);

        jMenuBar1.add(editMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addComponent(domainNameLabel)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reasonerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel15))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel9)
                    .addContainerGap(751, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainNameLabel)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reasonerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel9)
                    .addContainerGap(554, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // when the rule node of rule tree is clicked
    private void ruleTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_ruleTreeValueChanged
        if(!ruleTree.isSelectionEmpty()){
            Rule selectedRule = (Rule) ruleTree.getLastSelectedPathComponent();
            updateRuleDetails(selectedRule);
       }
    }//GEN-LAST:event_ruleTreeValueChanged

    private void conditionFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conditionFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_conditionFieldActionPerformed

    private void newDomainOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDomainOptionActionPerformed
        if(!DomainEditorFrame.isOpened()){
            DomainEditorFrame.execute("Add");
        } else {
            showMessageDialog(null, "Domain editor frame is already opened.");
        }
    }//GEN-LAST:event_newDomainOptionActionPerformed

    private void addConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConclusionButtonActionPerformed
        //check whether case is clicked
        if(selectedCaseId!=0){
            Case selectedCase = Main.allCaseSet.getCaseById(selectedCaseId);
            AddRuleFrame.execute("add", selectedCase);
        } else {
            showMessageDialog(null, "Please select the case.");
        }
    }//GEN-LAST:event_addConclusionButtonActionPerformed

    private void modifyConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyConclusionButtonActionPerformed
        //check whether case is clicked
        if(selectedCaseId!=0){
            Case selectedCase = Main.allCaseSet.getCaseById(selectedCaseId);
            AddRuleFrame.execute("modify", selectedCase);
        } else {
            showMessageDialog(null, "Please select the case.");
        }
    }//GEN-LAST:event_modifyConclusionButtonActionPerformed

    private void addCaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCaseButtonActionPerformed
        setCaseEditorTable("add", 0);
        caseEditorFrame.setVisible(true);
        
        Point p = MouseInfo.getPointerInfo().getLocation();
        int x = (int) p.getX();
        int y = (int) p.getY();
        caseEditorFrame.setLocation(x-600,y-70);
    }//GEN-LAST:event_addCaseButtonActionPerformed

    private void caseEditorFrameAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_caseEditorFrameAddButtonActionPerformed

        caseEditorTable.getCellEditor(caseEditorTable.getSelectedRow(), caseEditorTable.getSelectedColumn()).stopCellEditing();
        
        //check whether there is any empty field
        int colAmount = caseStructure.getAttrAmount()+1;
        String[] attrNames = caseStructure.getAttributeNameArray();
        
        LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        
        boolean isAllFilled = true;
        boolean isAllValid = true;
        
        for(int i=1; i<colAmount; i++){
            String attrName = attrNames[i-1];
            String valString = (String) caseEditorTable.getValueAt(0, i);
            ValueType valType = caseStructure.getAttributeByName(attrName).getValueType();
            if(valString==null){
                isAllFilled=false;
                break;
            }            
            Value val = new Value(valType, valString);
            
            //TODO
            if(val.isValidValue(valString)){
                values.put(attrName, val);
            } else {
                //TODO
                // point out which value is not valid
                isAllValid=false;
            }
        }        
        if(isAllFilled){
            if(isAllValid) {
                try {
                    int newCaseId = (int) caseEditorTable.getValueAt(0,0);
                    //construct case
                    Case newCase = new Case(caseStructure, values);
                    newCase.setCaseId(newCaseId);
                    
                    //add case into allCaseSet
                    Main.allCaseSet.addCase(newCase);
                    
                    //add case into arff file
                    CaseLoader.insertCase(newCase);
                    
                    //dispose frame
                    caseEditorFrame.dispose();
                    
                    //update case table
                    setCaseTable();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                showMessageDialog(null, "Please enter valid value.");
            }
            
        } else {
            showMessageDialog(null, "Please complete all fields.");
        }
    }//GEN-LAST:event_caseEditorFrameAddButtonActionPerformed

    private void editCaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCaseButtonActionPerformed
        setCaseEditorTable("edit", selectedCaseId);
        caseEditorFrame.setVisible(true);
    }//GEN-LAST:event_editCaseButtonActionPerformed

    private void caseEditorTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_caseEditorTablePropertyChange
        if(!caseEditorTable.isEditing()){
            int selectedColumn = caseEditorTable.getSelectedColumn();
            if(selectedColumn>0){
                String[] attrNames = caseStructure.getAttributeNameArray();
                IAttribute attr = caseStructure.getAttributeByName(attrNames[selectedColumn-1]);
                String typedString = (String) caseEditorTable.getValueAt(0, selectedColumn);
                if(!attr.isAcceptableValue(typedString)) {
                    if(attr.getValueType().getTypeName().equals("DATE")){
                        caseEditorTable.setValueAt("2001-01-01 00:00:00.0", 0, selectedColumn);    
                    } else {
                        caseEditorTable.setValueAt("", 0, selectedColumn);
                        showMessageDialog(null, "This value is not acceptable.");
                    }
                } else {
                    if(attr.getValueType().getTypeName().equals("BOOLEAN")){
                        if(typedString.equals("FALSE")  || typedString.equals("0")){
                            caseEditorTable.setValueAt("FALSE", 0, selectedColumn);    
                        } else {
                            caseEditorTable.setValueAt("TRUE", 0, selectedColumn);    
                        }
                    } 
                }
            }
        } 
    }//GEN-LAST:event_caseEditorTablePropertyChange

    private void caseTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_caseTableKeyReleased
        int selectedRow = caseTable.getSelectedRow();
        if(selectedRow>-1){
            selectedCaseId = (int) caseTable.getValueAt(selectedRow, 0);
            selectedCaseField.setText(Integer.toString(selectedCaseId));
            inferenceCase();
        }
    }//GEN-LAST:event_caseTableKeyReleased

    private void caseTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_caseTableMouseReleased
        int selectedRow = caseTable.getSelectedRow();
        if(selectedRow>-1){
            selectedCaseId = (int) caseTable.getValueAt(selectedRow, 0);
            selectedCaseField.setText(Integer.toString(selectedCaseId));
            inferenceCase();
        }
    }//GEN-LAST:event_caseTableMouseReleased

    private void testWithNewCaseSetOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testWithNewCaseSetOptionActionPerformed
        FileNameExtensionFilter filter = new FileNameExtensionFilter("rdr case set", new String[] {"arff"});
            
            importFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            importFileChooser.addChoosableFileFilter(filter);
            importFileChooser.setFileFilter(filter);
            int returnVal = importFileChooser.showSaveDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = importFileChooser.getSelectedFile();                
                File dest =new File(System.getProperty("user.dir") + "/domain/cases/" + domainName + "_testing.arff") {};
                Main.loadedFile = dest;
                try {
                    //copy file
                    Files.copy(selectedFile.toPath(), dest.toPath(),REPLACE_EXISTING);
                    
                    showMessageDialog(null, "Case Loading...");
                    
                    CaseLoader.caseImportForTesting();
                    
                    
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(StartupFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(DomainEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }   
    }//GEN-LAST:event_testWithNewCaseSetOptionActionPerformed

    private void editMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_editMenuActionPerformed

    private void conclusionValueFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_conclusionValueFieldKeyPressed
        int key=evt.getKeyCode();
        if(key==KeyEvent.VK_ENTER)
        {
            addDefaultConclusion();
        }
    }//GEN-LAST:event_conclusionValueFieldKeyPressed

    private void addConclusionButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConclusionButton1ActionPerformed
        addDefaultConclusion();
    }//GEN-LAST:event_addConclusionButton1ActionPerformed

    private void defaultConclusionFrameWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_defaultConclusionFrameWindowClosed
        if(!isDefaultSet){
            RuleLoader.deleteDefaultRule();
            RuleLoader.insertRule(0, Main.KB.getRootRule(), -1);
        }
        Main.workbench.setRuleSet(Main.KB);
        setRuleTree(Main.KB.getRootRule());
        updateFiredRuleTree(Main.workbench.getFiredRules().getRootRule());
        setCaseTable();
        setConclusionComboBox();
        this.setEnabled(true);
    }//GEN-LAST:event_defaultConclusionFrameWindowClosed

    private void firedRuleTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_firedRuleTreeValueChanged
        if(!firedRuleTree.isSelectionEmpty()){
            Rule selectedRule = (Rule) firedRuleTree.getLastSelectedPathComponent();
            updateRuleDetails(selectedRule);
        }
    }//GEN-LAST:event_firedRuleTreeValueChanged

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        openDefaultConclusionFrame();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        setRuleTree(Main.KB.getRootRule());
        setConclusionComboBox();
    }//GEN-LAST:event_formWindowGainedFocus

    private void exitOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitOptionActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitOptionActionPerformed

    private void inductRDRExecuteOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inductRDRExecuteOptionActionPerformed

        int confirmed = JOptionPane.showConfirmDialog(this, 
           "Would you like too create rule tree based on dataset (Induct RDR)? (This will delete all the rules in the current knolwedge base.", "Executing Induct RDR?", 
           JOptionPane.YES_NO_OPTION); 

        //Close if user confirmed 
        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                String fileName = System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff";
                inductSqliteOperation.init();
                
                CaseLoader.caseStructureImport();
                
                int CaseAttrAmount = Main.domain.getCaseStructure().getAttrAmount();
                InductRDR.createRules(fileName, CaseAttrAmount-1);
                try {
                    DomainLoader.reloadDomainFile(Main.domain.getDomainName(),"");
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                initRuleBase();
                setRuleTree(Main.KB.getRootRule());
                updateFiredRuleTree(Main.workbench.getFiredRules().getRootRule());
                setCaseTable();
                setConclusionComboBox();
            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
    }//GEN-LAST:event_inductRDRExecuteOptionActionPerformed

    private void caseTableComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_caseTableComponentMoved
        
        
    }//GEN-LAST:event_caseTableComponentMoved

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
        CaseStructureFrame.execute("edit");
    }//GEN-LAST:event_fileMenuActionPerformed

    private void editCaseStructureMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCaseStructureMenuItemActionPerformed
        CaseStructureFrame.execute("edit");
    }//GEN-LAST:event_editCaseStructureMenuItemActionPerformed

    private void displayCasesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayCasesButtonActionPerformed
        displayAllCasesOfConclusion();
    }//GEN-LAST:event_displayCasesButtonActionPerformed

    private void calculatePerformanceOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculatePerformanceOptionActionPerformed
        getInductResult();
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("arff", new String[] {"arff","ARFF"});
//            
//            testingArffChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/domain/cases/"));
//            testingArffChooser.addChoosableFileFilter(filter);
//            testingArffChooser.setFileFilter(filter);
//            int returnVal = testingArffChooser.showOpenDialog(this);
//            if(returnVal == JFileChooser.APPROVE_OPTION) {
//                File selectedFile = testingArffChooser.getSelectedFile();        
//                String fileName = selectedFile.getName();
//                File dest =new File(System.getProperty("user.dir") + "/domain/cases/" + fileName) {};
//                Main.loadedFile = dest;
//                try {
//                    //copy file
//                    Files.copy(selectedFile.toPath(), dest.toPath(),REPLACE_EXISTING);
//                    
//                    // dispose confirm dialog
//                    confirmImportDataset.dispose();
//                    
//                    //if inductRDROnly
//                    if(inductRDROnlyCheckBox.isSelected()){
//                        CaseLoader.caseStructureImport();
//                        
//                        String fileName = System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff";
//                        inductSqliteOperation.init();
//                        
//                        int CaseAttrAmount = Main.domain.getCaseStructure().getAttrAmount();
//                        InductRDR.createRules(fileName, CaseAttrAmount-1);
//                        try {
//                            DomainLoader.reloadDomainFile(Main.domain.getDomainName());
//                        } catch (Exception ex) {
//                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        
//                        CaseLoader.caseImportWithCaseStructure();     
//                        
//                        InductRDR.getCornerstonCases();
//                        
//                        //execute main frame 
//                        MainFrame.execute(domainName,reasoner);
//                        
//                    } else {
//                        CaseLoader.caseImportWithCaseStructure();     
//
//                        //insert case structure into db                    
//                        CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());
//                        
//                        //execute main frame 
//                        MainFrame.execute(domainName,reasoner);
//                    }
//                    
//                    //dispose domain Editor
//                    this.dispose();
//                    
//                } catch (IOException ex) {
//                    Logger.getLogger(StartupFrame.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (Exception ex) {
//                    Logger.getLogger(DomainEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                
//            }   
        
        
    }//GEN-LAST:event_calculatePerformanceOptionActionPerformed

    /**
     *
     */
    public void getInductResult(){
        
        CaseSet incorrectCaseSet = new CaseSet();
        CaseSet correctCaseSet = new CaseSet();
        
        Set cases = Main.allCaseSet.getBase().entrySet();
        // Get an iterator
        Iterator caseIterator = cases.iterator();
        // Display elements
        while (caseIterator.hasNext()) {
            Map.Entry me = (Map.Entry) caseIterator.next();
            Case aCase = (Case) me.getValue();

            Case currentCase = Main.allCaseSet.getCaseById(aCase.getCaseId());

            if(currentCase.getValues().size()==0){
                  try {
                    currentCase = CaseLoader.caseLoad(currentCase.getCaseId());
                    Main.allCaseSet.replaceCase(currentCase);
                   
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentCase = Main.allCaseSet.getCaseById(aCase.getCaseId());
            } 

            Main.workbench.setCurrentCase(currentCase);
            Main.workbench.inference();
            Rule inferenceResult = (Rule) Main.workbench.getInferenceResult();
            
            Value groundTruth = currentCase.getValue("T");
            
            if(inferenceResult.getConclusion().getConclusionValue().equals(groundTruth)){                
                //System.out.println("true");
                correctCaseSet.addCase(currentCase);
            } else {
                incorrectCaseSet.addCase(currentCase);
                //System.out.println("false");
            }
        }
        
        writeInductResult(correctCaseSet, incorrectCaseSet);
        
        setCaseTable();
    }
    
    /**
     *
     * @param correctCaseSet
     * @param incorrectCaseSet
     */
    public void writeInductResult(CaseSet correctCaseSet, CaseSet incorrectCaseSet){
        BufferedWriter writer = null;
        try {
            //create a temporary file
            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File logFile = new File(System.getProperty("user.dir") + "/inductResult/" + Main.domain.getDomainName() + "_" + timeLog + ".txt");

            // This will output the full path where the file will be written to...
            //System.out.println(logFile.getCanonicalPath());

            int totalCaseAmount = Main.allCaseSet.getCaseAmount();

            double performance = (double) correctCaseSet.getCaseAmount()/ (double) totalCaseAmount;
            
            String output = "Domain Name: " + Main.domain.getDomainName() + "\r\n\r\n\r\n"
                        + "Total Number of cases: " + totalCaseAmount + "\r\n"
                        + "Number of correct cases: " + correctCaseSet.getCaseAmount() + "\r\n"
                        + "Number of incorrect cases: " + incorrectCaseSet.getCaseAmount() + "\r\n"
                        + "Accuracy: " + correctCaseSet.getCaseAmount() + "/" + totalCaseAmount + "(" + performance + ")\r\n\r\n\r\n"       
                        + "====================================================================================================" + "\r\n\r\n"
                        + "Correct cases: " + correctCaseSet.toStringSorted()  + "\r\n\r\n"
                        + "Incorrect cases: " + incorrectCaseSet.toStringSorted();
            
            System.out.println(output);
            
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(output);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
        
    }
    
    /**
     *
     * @param isInductExecuted
     * @param domainName
     * @param reasoner
     */
    public static void execute(boolean isInductExecuted, String domainName, String reasoner) {       
        MainFrame.isInductExecuted = isInductExecuted;
        MainFrame.domainName = domainName;
        
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCaseButton;
    private javax.swing.JButton addConclusionButton;
    private javax.swing.JButton addConclusionButton1;
    private javax.swing.JMenuItem calculatePerformanceOption;
    private javax.swing.JFrame caseEditorFrame;
    private javax.swing.JButton caseEditorFrameAddButton;
    private javax.swing.JTable caseEditorTable;
    private javax.swing.JTable caseTable;
    public static javax.swing.JComboBox conclusionComboBox;
    private javax.swing.JTextField conclusionField;
    private javax.swing.JComboBox conclusionTypeComboBox;
    private javax.swing.JTextField conclusionValueField;
    private javax.swing.JTextField conditionField;
    private javax.swing.JTextField csCaseField;
    private javax.swing.JFrame defaultConclusionFrame;
    public static javax.swing.JButton displayCasesButton;
    private javax.swing.JLabel domainNameLabel;
    private javax.swing.JButton editCaseButton;
    private javax.swing.JMenuItem editCaseStructureMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitOption;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTree firedRuleTree;
    private javax.swing.JFileChooser importFileChooser;
    private javax.swing.JMenuItem inductRDRExecuteOption;
    private javax.swing.JList inferencedConclusionList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuItem loadDomainOption;
    private javax.swing.JButton modifyConclusionButton;
    private javax.swing.JMenuItem newDomainOption;
    private javax.swing.JButton reasonerChangeButton;
    private javax.swing.JFrame reasonerChangerFrame;
    private javax.swing.JComboBox reasonerComboBox;
    private javax.swing.JLabel reasonerLabel;
    private javax.swing.JTextField ruleIdField;
    public static javax.swing.JTree ruleTree;
    private javax.swing.JTabbedPane ruleTreeTabPanel;
    private javax.swing.JTextField selectedCaseField;
    private javax.swing.JMenuItem testWithNewCaseSetOption;
    private javax.swing.JFileChooser testingArffChooser;
    // End of variables declaration//GEN-END:variables
   
    
}

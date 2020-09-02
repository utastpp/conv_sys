/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

//import com.sun.media.jfxmedia.logging.Logger;
import cmcrdr.cases.DialogCase;
import cmcrdr.command.CommandFactory;
import cmcrdr.command.CommandInstance;
import cmcrdr.command.ICommandInstance;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.handler.OutputParser;
import cmcrdr.main.DialogMain;
import cmcrdr.mysql.DBOperation;
import cmcrdr.logger.Logger;
import cmcrdr.mysql.DBConnection;
import cmcrdr.savedquery.ConclusionQuery;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import rdr.apps.Main;
import rdr.cases.Case;
import rdr.cases.CornerstoneCase;
import rdr.cases.CornerstoneCaseSet;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.Condition;
import rdr.rules.Operator;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;
import static javax.swing.JOptionPane.showMessageDialog;
import cmcrdr.command.ContextVariableCommandInstance;
import cmcrdr.command.DatabaseItemCommandInstance;
import static cmcrdr.knowledgeacquisition.KALogic.getContextVariables;



/**
 * This class is used to present GUI for adding a new rule
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class AddRuleGUI extends javax.swing.JFrame {
    private boolean isWrongConclusionSet=false;
    private static DialogCase currentCase;
    private static IDialogInstance newInputDialog=null;
    private static IDialogInstance prevDialog = null;
    private static int targetStackId;
    
    private CornerstoneCase validatingCornerstoneCase;
    
    private static String mode;
    private static int wizardStep = 1;
    private Condition newCondition;
    private Conclusion newConclusion = new Conclusion();
    private ConclusionSet conclusionSet = new ConclusionSet();

    private boolean isConclusionSet = false;
    private boolean isConditionSet = false;
    private boolean isValidated = false;
    


    
    /**
     * Creates new form AddRuleFrame
     */
    public AddRuleGUI() {
        Logger.info("Mode is set to:" + mode);
        initComponents();
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);
        
        Logger.info("About to call learnerInit");
        learnerInit();
        getInferenceResult();
        updateCurrentCaseTable();
        updateCurrentCaseDialogHistory();
        updateAttrComboBox();
        valModeSetEnabled(false);
        updateConclusionTable();
        initialiseGUI();           
    }
    
    private void initialiseGUI(){
        
        updateConditionFields("Recent");
        selectConditionPanel.setVisible(false);
        validationStepPanel.setVisible(false);
        selectConclusionPanel.setVisible(true);
        
        selectedCommandCategoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(CommandInstance.devicelist));
        conclusionCommandCategoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(CommandInstance.devicelist));
        conclusionCommandActionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{""}));
        
        
        selectConclusionButton.setVisible(true);
        
        //String[] contextVariableList = DialogMain.globalContextVariableManager.getContextVariablesStringArrayList().toArray(new String[0]);
        String[] contextVariableList = getContextVariables("").getRowsAsStringArray();
        for (String aVariable :  contextVariableList) {
            Logger.info("Initialise: Found context var in list: " + aVariable);
        }
        
        contextVariableListComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        
        if (contextVariableListComboBox.getModel().getSize() == 0) {
            contextVariableListComboBox.setEnabled(false);     
        }
        else {
            contextVariableListComboBox.setEnabled(true); 
        }
        
        saveDBContextValueComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (saveDBContextValueComboBox.getModel().getSize() == 0) {
            saveDBContextValueComboBox.setEnabled(false);     
        }
        else {
            saveDBContextValueComboBox.setEnabled(true); 
        }
        
        setContextVarListComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (setContextVarListComboBox.getModel().getSize() == 0) {
            setContextVarListComboBox.setEnabled(false);     
        }
        else {
            setContextVarListComboBox.setEnabled(true); 
        }
        
        setContextVarValueListComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (setContextVarValueListComboBox.getModel().getSize() == 0) {
            setContextVarValueListComboBox.setEnabled(false);     
        }
        else {
            setContextVarValueListComboBox.setEnabled(true); 
        }
        
        schemaItemsComboBox.setModel(new DefaultComboBoxModel(DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray()));
        if (schemaItemsComboBox.getModel().getSize() == 0) {
            schemaItemsComboBox.setEnabled(false);     
        }
        else {
            schemaItemsComboBox.setEnabled(true); 
        }
        
        if (!DBConnection.getIsDatabaseUsed()) {
            showDatabaseQueryBuilderButton.setEnabled(false);
        }
        
       // deselectConclusionButton.setVisible(false);
    }
    
    /**
     *
     * @param text
     * @param databaseText
     */
    public void addToConclusionFieldText(String text, String databaseText) {  
        conclusionDialogTextArea.setText(conclusionDialogTextArea.getText() + text);
        databaseQueries.setText(databaseQueries.getText() + databaseText);
    }
    
    private void getInferenceResult(){
        Logger.info("getInferenceResult called");
        
        isWrongConclusionSet=false; 
        Main.workbench.deleteWrongConclusion();
        
        RuleSet inferenceResult = new RuleSet();
        switch (Main.domain.getReasonerType()) {            
            case "MCRDR":  
                // DPH trying the following..
                inferenceResult = DialogMain.getDialogUserList().getCurrentKAInferenceResult();
                // DPH May 2019 commented out the following
                /*if (Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(targetStackId) != null)
                    inferenceResult = (RuleSet) Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(targetStackId).getInferenceResult();
                else {
                    inferenceResult = new RuleSet();
                    inferenceResult.addRule(Main.KB.getRootRule());
                }*/
                
                Main.workbench.setInferenceResult(inferenceResult);
                Logger.info("Mode is:" + mode);
                
                Logger.info("Inferenced result contains the following rules:");
                for (Rule aRule: inferenceResult.getBase().values()) {
                    Logger.info("   rule" + aRule.getRuleId());
                }
                
                Logger.info("Inferenced result contains the following conclusions:");
                for (Conclusion aConclusion: inferenceResult.getConclusionSet().getBase().values()) {
                    Logger.info("   conclusion" + aConclusion.toString());
                }
                
                
                // looks like inference result is wrong..... test1 test2
                
                if (mode.equals("exception")){
                    Logger.info("in exception section..");
                    String[] wrongConclusionArray = inferenceResult.getConclusionSet().toStringArrayForGUIWithoutAddConclusion();
                    if(wrongConclusionArray.length>1) {

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
                    } else if(wrongConclusionArray.length == 1) {

                        Rule inferencedRule = inferenceResult.getLastRule();
                        Logger.info("Setting wrong conclusion to " + inferencedRule.getConclusion().getConclusionName());
                        setWrongConclusion(inferencedRule.getConclusion().getConclusionName());
                    } else {
                        
                    }
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
        Logger.info("learnerInit called!");
        // set the possible conclusion into conclusion set
        Main.workbench.setRuleSet(Main.KB);
        conclusionSet.setConclusionSet(Main.KB.getConclusionSet());
            
        Rule newRule = new Rule();
        
        CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
        cornerstoneCaseSet.addCase(currentCase);
        
        newRule.setCornerstoneCaseSet(cornerstoneCaseSet);
        
        Logger.info("About to call setNewRule..");
        Main.workbench.setNewRule(newRule);
    }
    
    private void updateConclusionTable() {
        
        String[] conclusionList = conclusionSet.toStringArrayForGUIWithoutAddConclusion();
        Object[][] conclusionObjectArray = new Object[conclusionList.length][3];
        
        for(int i=0; i<conclusionSet.getSize(); i++){
            String aConclusion = conclusionList[i];
            if(!aConclusion.equals("")){
                // SPLITAGEDDON
                String[] aConclusionArray = aConclusion.split("\\^");

                conclusionObjectArray[i][0] = aConclusionArray[0];
                conclusionObjectArray[i][1] = "";
                conclusionObjectArray[i][2] = "";

                if(aConclusionArray.length>1){
                    conclusionObjectArray[i][1] = aConclusionArray[1];
                    if(aConclusionArray.length>2){
                    conclusionObjectArray[i][2] = aConclusionArray[2];
                    }
                } else {
                }
            } else {
                conclusionObjectArray[i][0] = "";
                conclusionObjectArray[i][1] = "";
                conclusionObjectArray[i][2] = "";
                
            }
        }
        
        String[] columnNames = new String[]{"Conclusion", "Action Category", "Action"};
        
        TableModel newModel = new DefaultTableModel(
                conclusionObjectArray,columnNames
         ){
            boolean[] canEdit = new boolean []{false, false, false};    
            
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];                
            }
         };
        
        conclusionTable.setModel(newModel);
            newConclusion = new Conclusion();
    }
    
    private void updateCurrentCaseTable() {
        int attributeAmount = currentCase.getCaseStructure().getAttrAmount();
        
        String[] columnNames = new String[]{"Attribute", "Type", "Value", "Variable"};
        
        
        Object[][] tempArray = currentCase.toObjectForGUIRowWithType(attributeAmount);
        
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
        
        currentCaseTable.getTableHeader().setReorderingAllowed(false);
        
        if (currentCaseTable.getColumnModel().getColumnCount() > 0) {
            currentCaseTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            currentCaseTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            currentCaseTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        }
        
    }
    
    private void updateCurrentCaseDialogHistory() {
        String historyDialog = DialogMain.getDialogUserList().getCurrentDialogRepository().getAllDialogStringUntilGivenDialogWithNewLine(currentCase.getInputDialogInstance());
        
        
        if(newInputDialog!=null){
            historyDialog = DialogMain.getDialogUserList().getCurrentDialogRepository().getAllDialogStringUntilGivenDialogWithNewLine(prevDialog);
        } 
        
        MyHighlighter hilite = new MyHighlighter();
        dialogHistoryTextArea.setHighlighter(hilite);
        dialogHistoryTextArea.setText(historyDialog);
        
        String highlightInputStr = "";
        
        int start = historyDialog.length();                   
        int end = start + highlightInputStr.length() ;
            
        if(newInputDialog==null){
            
            highlightInputStr  = currentCase.getInputDialogInstance().toString();
            dialogHistoryTextArea.append(highlightInputStr);
            
            start = historyDialog.length();                   
            end = start + highlightInputStr.length() ;
            
        } else {
            
            String recentOfCaseInputStr  = currentCase.getInputDialogInstance().toString();
            dialogHistoryTextArea.append("\n" + recentOfCaseInputStr);
            
            String recentOfCaseOutputStr  = currentCase.getSystemResponse().getResponseDialogInstance().toString();
            dialogHistoryTextArea.append("\n" + recentOfCaseOutputStr);
            
            highlightInputStr  = newInputDialog.toString();
            dialogHistoryTextArea.append("\n" + highlightInputStr);
            
            start = historyDialog.length() + recentOfCaseInputStr.length() + recentOfCaseOutputStr.length() + 3;                   
            end = start + highlightInputStr.length();
            
        }
        
        DefaultHighlightPainter yellowPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

        try {
            boolean even = true;

            //look for newline char, and then toggle between white and gray painters.
            hilite.addHighlight(start, end+1, yellowPainter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
    }
    
    private void addConclusion() {
        String conclusionType = "TEXT";
        
        //String dialogStr = conclusionDialogTextArea.getText() + databaseQueries.getText();
        String dialogStr = conclusionDialogTextArea.getText();
        
        /*
        int dbConclusionRowSelected = dbConclusionReferenceTables.getSelectedRow();
        String dbConclusionID;
        if (dbConclusionRowSelected > -1) 
            dbConclusionID = (String)dbConclusionReferenceTables.getValueAt(dbConclusionRowSelected, 0);
        */
        
        String commandDevice = (String) conclusionCommandCategoryComboBox.getSelectedItem();
        String commandAction = (String) conclusionCommandActionComboBox.getSelectedItem();
        String commandActionVariable = deviceActionVariableTextField.getText();
        if(commandAction==null){
            commandAction = "";
        }
        if(!commandActionVariable.equals("")){
            commandAction += commandActionVariable;
        }
        //SPLITAGEDDON
        String conclusionName = dialogStr+"^"+commandDevice+"^"+commandAction;
        
        //showMessageDialog(null,"ConclusionID selected is: " + dbConclusionID);
        
        IAttribute attribute = AttributeFactory.createAttribute(conclusionType);
        Value value = new Value(new ValueType(conclusionType), conclusionName);
        newConclusion = new Conclusion(attribute, value);
        
        if(conclusionSet.isExist(newConclusion)){
            showMessageDialog(null, "Conclusion already exists.");
            
        } else {
            if(Main.workbench.getWrongConclusion()!=null) {
                if(newConclusion.getConclusionName().equals(Main.workbench.getWrongConclusion().getConclusionName())){
                    showMessageDialog(null, "Conclusion already exists.");
                } else {
                    //set new conclusion for the knowledge acquisition
                    Main.workbench.setNewRuleConclusion(newConclusion);
                    conclusionSet.addConclusion(newConclusion);
                    
                    updateConclusionTable();

                    newConclusionFrame.dispose();
                }
            } else {
                //set new conclusion for the knowledge acquisition
                Main.workbench.setNewRuleConclusion(newConclusion);
                conclusionSet.addConclusion(newConclusion);

                updateConclusionTable();
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
        
        if (addStopRuleCheckbox.isSelected()) {
            newCondition = RuleBuilder.buildRuleCondition(currentCase.getCaseStructure(), "STOP", "==", "true");
            return true;
        }
        
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
                    isConditionSet=true;                    
                    /**
                     * Without Validation
                     **/
                    addRuleButton.setEnabled(true);
                    
                    /**
                     * With Validation
                     **/
//                    nextButton.setEnabled(true);
                    
                    updateConditionListTable();
                } else {                    
                    showMessageDialog(null, "Condition already exists.");
                }
            } else {
                showMessageDialog(null, "Condition is not valid for this case.");
            }
            
        } else {
            showMessageDialog(null, "Please enter condition.");
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
        
        // disable next button
        if(conditionAmount==0){            
            isConditionSet=false;
            nextButton.setEnabled(false);
        } else {            
            isConditionSet=true;
            nextButton.setEnabled(true);
        }
        
        conditionListTable.setModel(newModel);
        conditionListTable.getTableHeader().setReorderingAllowed(false);
        
        conditionListTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        conditionListTable.getTableHeader().setReorderingAllowed(false);
        
        if (conditionListTable.getColumnModel().getColumnCount() > 0) {
            conditionListTable.getColumnModel().getColumn(0).setPreferredWidth(90);
            conditionListTable.getColumnModel().getColumn(1).setPreferredWidth(90);
            conditionListTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        }
        if(conditionListTable.getRowCount()==0){
            addRuleButton.setEnabled(false);
        } else {
            addRuleButton.setEnabled(true);
        }
    }
    
    
    
    
    private void validateRule() {
        // clear cornerstonce case set (but the current case must be remained)
        CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
        cornerstoneCaseSet.addCase(currentCase);
        Main.workbench.getNewRule().setCornerstoneCaseSet(cornerstoneCaseSet);

        //Main.workbench.getLearner().reteriveValidatingCaseSet();            
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
      
    private void updateAttrComboBox() {
        String[] attrNames = currentCase.getCaseStructure().getAttributeNameArray();
        conAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(attrNames));
        
    }

    private void valModeSetEnabled(boolean bool){
        valCaseNextButton.setEnabled(bool);
        valCasePrevButton.setEnabled(bool);
        valCaseTable.setEnabled(bool);
        validationAcceptButton.setEnabled(bool);
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
    
    private void setWrongConclusion( String conclusionName ) {        
        Conclusion wrongConclusion = conclusionSet.getConclusionByName(conclusionName);        
        
        Main.workbench.setWrongConclusion(wrongConclusion);
        
        wrongConclusionFrame.dispose();        
        this.setEnabled(true);
        this.requestFocus();
        
        learnerInit();
    }
    
    
    private void addRule(){
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Do you want to execute the current knowledge acquisition?", "Knowledge Acquisition?",
                JOptionPane.YES_NO_OPTION);
        //Close if user confirmed
        if (confirmed == JOptionPane.YES_OPTION)
        {
            //Logger.info("addRule - getRuleSet                :" + Main.workbench.getLearner().getRuleSet());
            Logger.info("addRule - getConclusionFromNewRule  :" + Main.workbench.getLearner().getConclusionFromNewRule());
            Logger.info("addRule - getConditionSetFromNewRule:" + Main.workbench.getLearner().getConditionSetFromNewRule());
            //Logger.info("addRule - getCurrentCornerstoneCase :" + Main.workbench.getLearner().getCurrentCornerstoneCase());
            Logger.info("addRule - getWrongConclusion        :" + Main.workbench.getLearner().getWrongConclusion());
            Logger.info("addRule - getWrongRuleSet           :" + Main.workbench.getLearner().getWrongRuleSet());
            Logger.info("addRule - getNewRuleType            :" + Main.workbench.getLearner().getNewRuleType());
            
            
            Main.workbench.executeAddingRule(this.doNotStackCheckbox.isSelected());
            Main.KB.setRuleSet(Main.workbench.getRuleSet());
            Main.KB.setRootRuleTree();
            Main.KB.retrieveCornerstoneCaseSet();
            
            //dispose addRuleFrame
            this.dispose();
        }
    }
    
    private void addStopRule(){
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Do you want to execute the current knowledge acquisition?", "Knowledge Acquisition?",
                JOptionPane.YES_NO_OPTION);
        //Close if user confirmed
        if (confirmed == JOptionPane.YES_OPTION)
        {
            /*System.out.println("getRuleSet" + Main.workbench.getLearner().getRuleSet());
            System.out.println("getConclusionFromNewRule" + Main.workbench.getLearner().getConclusionFromNewRule());
            System.out.println("getConditionSetFromNewRule" + Main.workbench.getLearner().getConditionSetFromNewRule());
            System.out.println("getCurrentCornerstoneCase" + Main.workbench.getLearner().getCurrentCornerstoneCase());
            System.out.println("getWrongConclusion" + Main.workbench.getLearner().getWrongConclusion());
            System.out.println("getWrongRuleSet" + Main.workbench.getLearner().getWrongRuleSet());
            System.out.println("getNewRuleType" + Main.workbench.getLearner().getNewRuleType());
            */
            Main.workbench.getNewRule().setIsStoppingRule(true);
            Main.workbench.executeAddingStopRule();
            Main.KB.setRuleSet(Main.workbench.getRuleSet());
            Main.KB.setRootRuleTree();
            Main.KB.retrieveCornerstoneCaseSet();
            
            //dispose addRuleFrame
            this.dispose();
        }
    }
    
    private void updateWizard(){
        
        if(wizardStep==1){
            selectConclusionPanel.setVisible(true);
            selectConditionPanel.setVisible(false);            
            validationStepPanel.setVisible(false);         
            backButton.setEnabled(false);
            if(isConclusionSet){
                nextButton.setEnabled(true);
            } else {
                nextButton.setEnabled(false);
            }
            
            nextButton.setVisible(true);
            addRuleButton.setVisible(false);
        } else if(wizardStep==2){
            
            showMessageDialog(null, "If you want to use representative term, click 'Add New Representative Terms' button.");

            selectConclusionPanel.setVisible(false);
            selectConditionPanel.setVisible(true);            
            validationStepPanel.setVisible(false);        
            backButton.setEnabled(true);  
            
            /**
             * With Validation
             **/
            nextButton.setVisible(false);
            addRuleButton.setVisible(true);
            
            /**
             * Without Validation
             **/
//            nextButton.setVisible(true);
//            addRuleButton.setVisible(false);
        } else if(wizardStep==3){
            selectConclusionPanel.setVisible(false);
            selectConditionPanel.setVisible(false);            
            validationStepPanel.setVisible(true);      
            backButton.setEnabled(true); 
            nextButton.setVisible(false);
            addRuleButton.setVisible(true);
            
            
        }
        
    }
    
    private void updateSelectedConclusionFields(){
        if(conclusionTable.getSelectedRowCount()>0){
            int selectedRow = conclusionTable.getSelectedRow();
            String selectedDialogStr = (String) conclusionTable.getValueAt(selectedRow, 0);
            String selectedCommandDevice = (String) conclusionTable.getValueAt(selectedRow, 1);
            String selectedCommandAction = (String) conclusionTable.getValueAt(selectedRow, 2);
            
            selectedDialogStrField.setText(selectedDialogStr);
            selectedCommandCategoryComboBox.setSelectedItem(selectedCommandDevice);
            selectedCommandActionField.setText(selectedCommandAction);
        }
    }
    
    private void selectConclusion(){
        if(conclusionTable.getSelectedRowCount()>0){
            int selectedRow = conclusionTable.getSelectedRow();
            String selectedDialogStr = (String) conclusionTable.getValueAt(selectedRow, 0);
            String selectedCommandDevice = (String) conclusionTable.getValueAt(selectedRow, 1);
            String selectedCommandAction = (String) conclusionTable.getValueAt(selectedRow, 2);
            
            selectedDialogStrField.setText(selectedDialogStr);
            selectedCommandCategoryComboBox.setSelectedItem(selectedCommandDevice);
            selectedCommandActionField.setText(selectedCommandAction);
            //SPLITAGEDDON
            String selectedConclusionName = selectedDialogStr + "^" + selectedCommandDevice + "^" + selectedCommandAction;
            newConclusion = conclusionSet.getConclusionByName(selectedConclusionName);
            
            Main.workbench.setNewRuleConclusion(newConclusion);
            
        
            conclusionTable.setEnabled(false);
            conclusionTable.setBackground(Color.LIGHT_GRAY);
            addNewConclusionButton.setEnabled(false);
            selectConclusionButton.setVisible(false);
           // deselectConclusionButton.setVisible(true);
            
            wizardStep = 2;
            isConclusionSet=true;
            updateWizard();
            
        } else {
            showMessageDialog(null, "Please select conclusion 1.");
        }
    }
    
    
    private void deselectConclusion(){
        if(conclusionTable.getSelectedRowCount()>0){
            
            conclusionTable.setEnabled(true);
            conclusionTable.setBackground(Color.WHITE);
            addNewConclusionButton.setEnabled(true);
            selectConclusionButton.setVisible(true);
            //deselectConclusionButton.setVisible(false);
            
            wizardStep = 1;
            isConclusionSet=false;
            updateWizard();
            
        } else {
            showMessageDialog(null, "Please select conclusion 2.");
        }
    }
    
    private void updateActionComboBox(String deviceName) {
        if(!deviceName.equals("")){
            ICommandInstance aCommandInstance = CommandFactory.createCommandInstance(deviceName);
            conclusionCommandActionComboBox.setModel(new javax.swing.DefaultComboBoxModel(aCommandInstance.getDeviceActionList()));
        } else {
            conclusionCommandActionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{""}));
        }
    }
    
    /**
     *
     */
    public void updateContextVariableComboBox() {
        String[] contextVariableList = getContextVariables("").getRowsAsStringArray();
        for (String aVariable :  contextVariableList) {
            Logger.info("Found context var in list: " + aVariable);
        }
        
        contextVariableListComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (contextVariableListComboBox.getModel().getSize() == 0) {
            contextVariableListComboBox.setEnabled(false);     
        }
        else {
            contextVariableListComboBox.setEnabled(true); 
        }
        
        saveDBContextValueComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (saveDBContextValueComboBox.getModel().getSize() == 0) {
            saveDBContextValueComboBox.setEnabled(false);     
        }
        else {
            saveDBContextValueComboBox.setEnabled(true); 
        }
        
        setContextVarListComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (setContextVarListComboBox.getModel().getSize() == 0) {
            setContextVarListComboBox.setEnabled(false);     
        }
        else {
            setContextVarListComboBox.setEnabled(true); 
        }
        
        setContextVarValueListComboBox.setModel(new DefaultComboBoxModel(contextVariableList));
        if (setContextVarValueListComboBox.getModel().getSize() == 0) {
            setContextVarValueListComboBox.setEnabled(false);     
        }
        else {
            setContextVarValueListComboBox.setEnabled(true); 
        }
        
        schemaItemsComboBox.setModel(new DefaultComboBoxModel(DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray()));
        if (schemaItemsComboBox.getModel().getSize() == 0) {
            schemaItemsComboBox.setEnabled(false);     
        }
        else {
            schemaItemsComboBox.setEnabled(true); 
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
        jPanel4 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        conclusionCommandCategoryComboBox = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        conclusionCommandActionComboBox = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        actionVariableCheckBox = new javax.swing.JCheckBox();
        deviceActionVariableTextField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        schemaItemsComboBox = new javax.swing.JComboBox<>();
        insertSchemaItemButton = new javax.swing.JButton();
        saveDBContextValueComboBox = new javax.swing.JComboBox<>();
        insertSaveDBContextValueButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        saveDBLiteralValueTextField = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        insertSaveDBLiteralButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        setContextVarListComboBox = new javax.swing.JComboBox<>();
        insertSetContextVarButton = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        setContextVarValueListComboBox = new javax.swing.JComboBox<>();
        insertSetContextVarValueButton = new javax.swing.JButton();
        setContextVarLiteralTextField = new javax.swing.JTextField();
        insertSetContextVarLiteralButton = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        actionDataClearButton = new javax.swing.JButton();
        addConclusionButton = new javax.swing.JButton();
        previewConclusionButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        conclusionDialogTextArea = new javax.swing.JTextArea();
        jScrollPane12 = new javax.swing.JScrollPane();
        databaseQueries = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        contextVariableListComboBox = new javax.swing.JComboBox<>();
        intValueCheckbox = new javax.swing.JCheckBox();
        insertContextVariableButton = new javax.swing.JButton();
        databaseQueriesClearButton = new javax.swing.JButton();
        insertDatabaseQueryButton = new javax.swing.JButton();
        noHelpCheckBox = new javax.swing.JCheckBox();
        replaceEmptyCheckbox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        showDatabaseQueryBuilderButton = new javax.swing.JButton();
        dictionaryManagerButton = new javax.swing.JButton();
        userContextVariableManager = new javax.swing.JButton();
        systemContextVariableManager = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu2 = new javax.swing.JMenu();
        conditionControlPopup = new javax.swing.JPopupMenu();
        deleteSelectedItem = new javax.swing.JMenuItem();
        wrongConclusionFrame = new javax.swing.JFrame();
        jScrollPane6 = new javax.swing.JScrollPane();
        wrongConclusionList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        wrongConclusionSelectButton = new javax.swing.JButton();
        previewConclusionDialog = new javax.swing.JDialog();
        previewConclusionCloseButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        previewTextArea = new javax.swing.JTextArea();
        existingConclusionQueries = new javax.swing.JDialog();
        jScrollPane13 = new javax.swing.JScrollPane();
        conclusionQueryTable = new javax.swing.JTable();
        existingConclusionQueriesSelectButton = new javax.swing.JButton();
        existingConclusionQueriesCancelButton = new javax.swing.JButton();
        contextVariableList2ComboBox1 = new javax.swing.JComboBox<>();
        insertContextVariable2Button1 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        CaseViewerPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        dialogHistoryTextArea = new javax.swing.JTextArea();
        wizardPanel = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        selectConclusionPanel = new javax.swing.JPanel();
        conclusionLabel = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        conclusionTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        selectedCommandActionField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        selectedCommandCategoryComboBox = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        selectedDialogStrField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLayeredPane3 = new javax.swing.JLayeredPane();
        deselectConclusionButton = new javax.swing.JButton();
        selectConclusionButton = new javax.swing.JButton();
        addNewConclusionButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        selectConditionPanel = new javax.swing.JPanel();
        conAttrComboBox = new javax.swing.JComboBox();
        conValField = new javax.swing.JTextField();
        conAddButton = new javax.swing.JButton();
        conOperComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        conditionListTable = new javax.swing.JTable();
        conditionLabel = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        currentCaseTable = new javax.swing.JTable();
        currentCaseLabel = new javax.swing.JLabel();
        newDicTermApplyButton = new javax.swing.JButton();
        dicManagerOpenButton = new javax.swing.JButton();
        modifyCommandVariablesButton = new javax.swing.JButton();
        validationStepPanel = new javax.swing.JPanel();
        valCaseListLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        valCaseListTable = new javax.swing.JTable();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        valCaseTable = new javax.swing.JTable();
        valCaseLabel = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        valConclusionList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        validationAcceptButton = new javax.swing.JButton();
        valCasePrevButton = new javax.swing.JButton();
        valCaseNextButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        addRuleButton = new javax.swing.JButton();
        addStopRuleCheckbox = new javax.swing.JCheckBox();
        doNotStackCheckbox = new javax.swing.JCheckBox();
        nextButton = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        newConclusionFrame.setTitle("New Conclusion");
        newConclusionFrame.setLocationByPlatform(true);
        newConclusionFrame.setMinimumSize(new java.awt.Dimension(530, 780));
        newConclusionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                newConclusionFrameWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                newConclusionFrameWindowActivated(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel16.setText("Conclusion");

        jLabel18.setText("New Conclusion");

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N
        jLabel10.setText("Actions");

        jLabel17.setText("Category");

        conclusionCommandCategoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conclusionCommandCategoryComboBoxActionPerformed(evt);
            }
        });

        jLabel15.setText("Action");

        conclusionCommandActionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conclusionCommandActionComboBoxActionPerformed(evt);
            }
        });

        jLabel19.setText("Parameter Data");

        actionVariableCheckBox.setText("Enabled");
        actionVariableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionVariableCheckBoxActionPerformed(evt);
            }
        });

        deviceActionVariableTextField.setEditable(false);
        deviceActionVariableTextField.setEnabled(false);
        deviceActionVariableTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                deviceActionVariableTextFieldKeyTyped(evt);
            }
        });

        jPanel6.setBackground(new java.awt.Color(204, 204, 204));
        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel11.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N
        jLabel11.setText("Save schema item to database:");

        jLabel8.setText("Schema item");

        schemaItemsComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        insertSchemaItemButton.setText("Insert");
        insertSchemaItemButton.setEnabled(false);
        insertSchemaItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSchemaItemButtonActionPerformed(evt);
            }
        });

        saveDBContextValueComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        insertSaveDBContextValueButton.setText("Insert");
        insertSaveDBContextValueButton.setEnabled(false);
        insertSaveDBContextValueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSaveDBContextValueButtonActionPerformed(evt);
            }
        });

        jLabel9.setText("Context Value");

        saveDBLiteralValueTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDBLiteralValueTextFieldActionPerformed(evt);
            }
        });

        jLabel24.setText("Literal value");

        insertSaveDBLiteralButton.setText("Insert");
        insertSaveDBLiteralButton.setEnabled(false);
        insertSaveDBLiteralButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSaveDBLiteralButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(11, 11, 11)
                                .addComponent(schemaItemsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(3, 3, 3)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(insertSaveDBContextValueButton)
                                    .addComponent(saveDBContextValueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(104, 104, 104)
                                .addComponent(insertSaveDBLiteralButton))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(insertSchemaItemButton)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel24)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(saveDBLiteralValueTextField)))))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(schemaItemsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(insertSchemaItemButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveDBContextValueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(saveDBLiteralValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertSaveDBContextValueButton)
                    .addComponent(insertSaveDBLiteralButton)))
        );

        jPanel7.setBackground(new java.awt.Color(204, 204, 204));
        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel20.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N
        jLabel20.setText("Set Context variable");

        setContextVarListComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        insertSetContextVarButton.setText("Insert ");
        insertSetContextVarButton.setEnabled(false);
        insertSetContextVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSetContextVarButtonActionPerformed(evt);
            }
        });

        jLabel21.setText("dest var");

        jLabel22.setText("source var");

        setContextVarValueListComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        insertSetContextVarValueButton.setText("Insert");
        insertSetContextVarValueButton.setEnabled(false);
        insertSetContextVarValueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSetContextVarValueButtonActionPerformed(evt);
            }
        });

        setContextVarLiteralTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setContextVarLiteralTextFieldActionPerformed(evt);
            }
        });

        insertSetContextVarLiteralButton.setText("Insert");
        insertSetContextVarLiteralButton.setEnabled(false);
        insertSetContextVarLiteralButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSetContextVarLiteralButtonActionPerformed(evt);
            }
        });

        jLabel23.setText("literal value");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(setContextVarListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(insertSetContextVarButton)
                                .addGap(40, 40, 40))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(setContextVarValueListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel23))
                                    .addComponent(insertSetContextVarValueButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(insertSetContextVarLiteralButton)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(setContextVarLiteralTextField)))))
                    .addComponent(jLabel20))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(setContextVarListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(insertSetContextVarButton))
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setContextVarValueListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23)
                    .addComponent(setContextVarLiteralTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertSetContextVarLiteralButton)
                    .addComponent(insertSetContextVarValueButton))
                .addGap(15, 15, 15))
        );

        actionDataClearButton.setText("clear");
        actionDataClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDataClearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(conclusionCommandCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel15)
                                .addGap(18, 18, 18)
                                .addComponent(conclusionCommandActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actionVariableCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deviceActionVariableTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actionDataClearButton))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(conclusionCommandCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)))
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conclusionCommandActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(actionVariableCheckBox)
                    .addComponent(deviceActionVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actionDataClearButton))
                .addGap(10, 10, 10)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        addConclusionButton.setText("Add Conclusion");
        addConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addConclusionButtonActionPerformed(evt);
            }
        });

        previewConclusionButton.setText("Preview Concusion");
        previewConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewConclusionButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Last Inserted Query Value:");

        conclusionDialogTextArea.setColumns(20);
        conclusionDialogTextArea.setLineWrap(true);
        conclusionDialogTextArea.setRows(5);
        conclusionDialogTextArea.setWrapStyleWord(true);
        conclusionDialogTextArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        jScrollPane11.setViewportView(conclusionDialogTextArea);

        databaseQueries.setEditable(false);
        databaseQueries.setColumns(20);
        databaseQueries.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N
        databaseQueries.setLineWrap(true);
        databaseQueries.setRows(5);
        jScrollPane12.setViewportView(databaseQueries);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Insert context variable value");

        contextVariableListComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        intValueCheckbox.setText("int value");

        insertContextVariableButton.setText("Insert");
        insertContextVariableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertContextVariableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(contextVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(intValueCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insertContextVariableButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contextVariableListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intValueCheckbox)
                    .addComponent(insertContextVariableButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        databaseQueriesClearButton.setText("clear");
        databaseQueriesClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseQueriesClearButtonActionPerformed(evt);
            }
        });

        insertDatabaseQueryButton.setText("Insert stored database query");
        insertDatabaseQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertDatabaseQueryButtonActionPerformed(evt);
            }
        });

        noHelpCheckBox.setText("No Help Suggestion");
        noHelpCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noHelpCheckBoxActionPerformed(evt);
            }
        });

        replaceEmptyCheckbox.setText("Replace Empty Query");
        replaceEmptyCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceEmptyCheckboxActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        showDatabaseQueryBuilderButton.setText("Query builder");
        showDatabaseQueryBuilderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDatabaseQueryBuilderButtonActionPerformed(evt);
            }
        });

        dictionaryManagerButton.setText("Dictionary");
        dictionaryManagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dictionaryManagerButtonActionPerformed(evt);
            }
        });

        userContextVariableManager.setText("User variables");
        userContextVariableManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userContextVariableManagerActionPerformed(evt);
            }
        });

        systemContextVariableManager.setText("System variables");
        systemContextVariableManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemContextVariableManagerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(showDatabaseQueryBuilderButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dictionaryManagerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(userContextVariableManager)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemContextVariableManager, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showDatabaseQueryBuilderButton)
                    .addComponent(dictionaryManagerButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userContextVariableManager)
                    .addComponent(systemContextVariableManager))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane11)
                    .addComponent(jScrollPane12)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(replaceEmptyCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noHelpCheckBox))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(databaseQueriesClearButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(insertDatabaseQueryButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(previewConclusionButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addConclusionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addConclusionButton, previewConclusionButton});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(addConclusionButton)
                    .addComponent(previewConclusionButton))
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(replaceEmptyCheckbox)
                    .addComponent(noHelpCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(databaseQueriesClearButton)
                    .addComponent(insertDatabaseQueryButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        windowsMenu2.setText("Windows");
        jMenuBar1.add(windowsMenu2);

        newConclusionFrame.setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout newConclusionFrameLayout = new javax.swing.GroupLayout(newConclusionFrame.getContentPane());
        newConclusionFrame.getContentPane().setLayout(newConclusionFrameLayout);
        newConclusionFrameLayout.setHorizontalGroup(
            newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        newConclusionFrameLayout.setVerticalGroup(
            newConclusionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newConclusionFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        previewConclusionDialog.setMinimumSize(new java.awt.Dimension(410, 310));

        previewConclusionCloseButton.setText("close");
        previewConclusionCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewConclusionCloseButtonActionPerformed(evt);
            }
        });

        previewTextArea.setColumns(20);
        previewTextArea.setLineWrap(true);
        previewTextArea.setRows(5);
        previewTextArea.setWrapStyleWord(true);
        jScrollPane4.setViewportView(previewTextArea);

        javax.swing.GroupLayout previewConclusionDialogLayout = new javax.swing.GroupLayout(previewConclusionDialog.getContentPane());
        previewConclusionDialog.getContentPane().setLayout(previewConclusionDialogLayout);
        previewConclusionDialogLayout.setHorizontalGroup(
            previewConclusionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, previewConclusionDialogLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(previewConclusionCloseButton))
            .addGroup(previewConclusionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        previewConclusionDialogLayout.setVerticalGroup(
            previewConclusionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewConclusionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewConclusionCloseButton)
                .addContainerGap())
        );

        existingConclusionQueries.setTitle("Existing Conclusion Queries");
        existingConclusionQueries.setMinimumSize(new java.awt.Dimension(900, 315));

        conclusionQueryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        conclusionQueryTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        conclusionQueryTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane13.setViewportView(conclusionQueryTable);

        existingConclusionQueriesSelectButton.setText("Select");
        existingConclusionQueriesSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingConclusionQueriesSelectButtonActionPerformed(evt);
            }
        });

        existingConclusionQueriesCancelButton.setText("Cancel");
        existingConclusionQueriesCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingConclusionQueriesCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout existingConclusionQueriesLayout = new javax.swing.GroupLayout(existingConclusionQueries.getContentPane());
        existingConclusionQueries.getContentPane().setLayout(existingConclusionQueriesLayout);
        existingConclusionQueriesLayout.setHorizontalGroup(
            existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(existingConclusionQueriesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, existingConclusionQueriesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(existingConclusionQueriesCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(existingConclusionQueriesSelectButton)))
                .addContainerGap())
        );
        existingConclusionQueriesLayout.setVerticalGroup(
            existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(existingConclusionQueriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(existingConclusionQueriesSelectButton)
                    .addComponent(existingConclusionQueriesCancelButton))
                .addContainerGap())
        );

        contextVariableList2ComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        insertContextVariable2Button1.setText("Insert context var name");
        insertContextVariable2Button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertContextVariable2Button1ActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("IDS Knowledge Acquisition Process");
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(975, 660));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jLabel7.setText("Dialog History");

        dialogHistoryTextArea.setEditable(false);
        dialogHistoryTextArea.setColumns(20);
        dialogHistoryTextArea.setLineWrap(true);
        dialogHistoryTextArea.setRows(5);
        dialogHistoryTextArea.setWrapStyleWord(true);
        jScrollPane9.setViewportView(dialogHistoryTextArea);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout CaseViewerPanelLayout = new javax.swing.GroupLayout(CaseViewerPanel);
        CaseViewerPanel.setLayout(CaseViewerPanelLayout);
        CaseViewerPanelLayout.setHorizontalGroup(
            CaseViewerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CaseViewerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        CaseViewerPanelLayout.setVerticalGroup(
            CaseViewerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CaseViewerPanelLayout.createSequentialGroup()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(CaseViewerPanel);

        jLayeredPane1.setPreferredSize(new java.awt.Dimension(800, 600));

        conclusionLabel.setFont(new java.awt.Font("굴림", 1, 14)); // NOI18N
        conclusionLabel.setText("Please choose conclusion");

        conclusionTable.setAutoCreateRowSorter(true);
        conclusionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Conclusion", "Action Category", "Action"
            }
        ));
        conclusionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                conclusionTableMouseReleased(evt);
            }
        });
        conclusionTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                conclusionTableKeyReleased(evt);
            }
        });
        jScrollPane10.setViewportView(conclusionTable);

        selectedCommandActionField.setEditable(false);

        jLabel13.setText("Selected Action Category");

        selectedCommandCategoryComboBox.setEnabled(false);

        jLabel12.setText("Selected conclusion");

        selectedDialogStrField.setEditable(false);

        jLabel14.setText("Selected Action");

        deselectConclusionButton.setText("deselect");
        deselectConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectConclusionButtonActionPerformed(evt);
            }
        });

        jLayeredPane3.setLayer(deselectConclusionButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane3Layout = new javax.swing.GroupLayout(jLayeredPane3);
        jLayeredPane3.setLayout(jLayeredPane3Layout);
        jLayeredPane3Layout.setHorizontalGroup(
            jLayeredPane3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane3Layout.createSequentialGroup()
                .addContainerGap(279, Short.MAX_VALUE)
                .addComponent(deselectConclusionButton)
                .addContainerGap())
        );
        jLayeredPane3Layout.setVerticalGroup(
            jLayeredPane3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane3Layout.createSequentialGroup()
                .addComponent(deselectConclusionButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        selectConclusionButton.setText("Set");
        selectConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectConclusionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectedDialogStrField, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedCommandCategoryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(selectedCommandActionField)))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLayeredPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectConclusionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLayeredPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(selectConclusionButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedDialogStrField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(selectedCommandCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(selectedCommandActionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        addNewConclusionButton.setText("Add new conclusion");
        addNewConclusionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewConclusionButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Existing conclusions");

        javax.swing.GroupLayout selectConclusionPanelLayout = new javax.swing.GroupLayout(selectConclusionPanel);
        selectConclusionPanel.setLayout(selectConclusionPanelLayout);
        selectConclusionPanelLayout.setHorizontalGroup(
            selectConclusionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(selectConclusionPanelLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addNewConclusionButton))
            .addGroup(selectConclusionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conclusionLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        selectConclusionPanelLayout.setVerticalGroup(
            selectConclusionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectConclusionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conclusionLabel)
                .addGap(18, 18, 18)
                .addGroup(selectConclusionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addNewConclusionButton)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        conAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute" }));
        conAttrComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conAttrComboBoxActionPerformed(evt);
            }
        });

        conValField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conValFieldActionPerformed(evt);
            }
        });

        conAddButton.setText("Add");
        conAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conAddButtonActionPerformed(evt);
            }
        });

        conOperComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "==", "!=", ">=", "<=" }));
        conOperComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conOperComboBoxActionPerformed(evt);
            }
        });

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

        conditionLabel.setFont(new java.awt.Font("굴림", 1, 14)); // NOI18N
        conditionLabel.setText("Add Conditions");

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

        currentCaseLabel.setText("Conditions");

        newDicTermApplyButton.setText("Apply representative term");
        newDicTermApplyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDicTermApplyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(currentCaseLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newDicTermApplyButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentCaseLabel)
                    .addComponent(newDicTermApplyButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
        );

        dicManagerOpenButton.setText("Add New Representative Terms");
        dicManagerOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dicManagerOpenButtonActionPerformed(evt);
            }
        });

        modifyCommandVariablesButton.setText("Modify command variables");
        modifyCommandVariablesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyCommandVariablesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout selectConditionPanelLayout = new javax.swing.GroupLayout(selectConditionPanel);
        selectConditionPanel.setLayout(selectConditionPanelLayout);
        selectConditionPanelLayout.setHorizontalGroup(
            selectConditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
            .addGroup(selectConditionPanelLayout.createSequentialGroup()
                .addComponent(conAttrComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conOperComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conValField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conAddButton))
            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, selectConditionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conditionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(selectConditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modifyCommandVariablesButton)
                    .addComponent(dicManagerOpenButton)))
        );
        selectConditionPanelLayout.setVerticalGroup(
            selectConditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectConditionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectConditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conditionLabel)
                    .addComponent(dicManagerOpenButton))
                .addGap(1, 1, 1)
                .addComponent(modifyCommandVariablesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(selectConditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(conAttrComboBox)
                    .addGroup(selectConditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(conOperComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(conValField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(conAddButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                .addContainerGap())
        );

        valCaseListLabel.setText("Rule Validation");

        jScrollPane3.setAutoscrolls(true);

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

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane8.setViewportView(jTextArea1);

        valCaseTable.setAutoCreateRowSorter(true);
        valCaseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Attribute", "Type", "Value"
            }
        ));
        jScrollPane5.setViewportView(valCaseTable);

        valCaseLabel.setText("Validating Case");

        jScrollPane7.setViewportView(valConclusionList);

        jLabel6.setText("Validating Case Conclusion");

        validationAcceptButton.setText("Accept");
        validationAcceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationAcceptButtonActionPerformed(evt);
            }
        });

        valCasePrevButton.setText("<<");
        valCasePrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valCasePrevButtonActionPerformed(evt);
            }
        });

        valCaseNextButton.setText(">>");
        valCaseNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valCaseNextButtonActionPerformed(evt);
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
                .addComponent(validationAcceptButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valCaseNextButton))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(valCasePrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(validationAcceptButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(valCaseNextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout validationStepPanelLayout = new javax.swing.GroupLayout(validationStepPanel);
        validationStepPanel.setLayout(validationStepPanelLayout);
        validationStepPanelLayout.setHorizontalGroup(
            validationStepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
            .addComponent(jScrollPane8)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(validationStepPanelLayout.createSequentialGroup()
                .addGroup(validationStepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(valCaseListLabel)
                    .addComponent(jLabel6)
                    .addComponent(valCaseLabel))
                .addGap(0, 616, Short.MAX_VALUE))
        );
        validationStepPanelLayout.setVerticalGroup(
            validationStepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validationStepPanelLayout.createSequentialGroup()
                .addComponent(valCaseListLabel)
                .addGap(26, 26, 26)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valCaseLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLayeredPane1.setLayer(selectConclusionPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(selectConditionPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(validationStepPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(validationStepPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(selectConclusionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(selectConditionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(validationStepPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(selectConclusionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(selectConditionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        backButton.setText("Previous");
        backButton.setEnabled(false);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        addRuleButton.setText("Submit");
        addRuleButton.setEnabled(false);
        addRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRuleButtonActionPerformed(evt);
            }
        });

        addStopRuleCheckbox.setText("Add STOP rule");
        addStopRuleCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStopRuleCheckboxActionPerformed(evt);
            }
        });

        doNotStackCheckbox.setText("Do not stack");

        jLayeredPane2.setLayer(addRuleButton, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(addStopRuleCheckbox, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(doNotStackCheckbox, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addComponent(addStopRuleCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doNotStackCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addRuleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addRuleButton)
                    .addComponent(addStopRuleCheckbox)
                    .addComponent(doNotStackCheckbox))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        nextButton.setText("Next");
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout wizardPanelLayout = new javax.swing.GroupLayout(wizardPanel);
        wizardPanel.setLayout(wizardPanelLayout);
        wizardPanelLayout.setHorizontalGroup(
            wizardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wizardPanelLayout.createSequentialGroup()
                .addContainerGap(140, Short.MAX_VALUE)
                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, wizardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE))
        );
        wizardPanelLayout.setVerticalGroup(
            wizardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wizardPanelLayout.createSequentialGroup()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(wizardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(wizardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(resetButton)
                        .addComponent(backButton)
                        .addComponent(nextButton))
                    .addComponent(jLayeredPane2))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(wizardPanel);

        windowsMenu.setText("Windows");
        jMenuBar2.add(windowsMenu);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane1)
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
            showMessageDialog(null, "Please select conclusion 3.");
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

    private void addRuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRuleButtonActionPerformed
//        validateRule();
        if (addStopRuleCheckbox.isSelected()) {
            Main.workbench.getNewRule().setIsStoppingRule(true);
            // condition
            constructNewCondition();
            isConditionSet=true; 
            Main.workbench.getLearner().addConditionToNewRule(newCondition);
            
            //conclusion
            IAttribute attribute = AttributeFactory.createAttribute("Text");
            Value value = new Value(new ValueType("Text"), "STOPRULE^^");
            newConclusion = new Conclusion(attribute, value);
            if (!conclusionSet.isExist(newConclusion)){
                conclusionSet.addConclusion(newConclusion);
            }
            
            Main.workbench.setNewRuleConclusion(newConclusion);
            
            addStopRule();
        }
        else {
            Logger.info("addRuleButtonActionPerformed - Calling addRule...");
            addRule();
        }
    }//GEN-LAST:event_addRuleButtonActionPerformed

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

    private void wrongConclusionSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrongConclusionSelectButtonActionPerformed
        if(wrongConclusionList.getSelectedIndex()>-1){
            isWrongConclusionSet = true;
            String conclusionName = (String) wrongConclusionList.getSelectedValue();
            setWrongConclusion(conclusionName);
        }
    }//GEN-LAST:event_wrongConclusionSelectButtonActionPerformed

    private void wrongConclusionFrameWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_wrongConclusionFrameWindowClosed
        if(!isWrongConclusionSet){
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

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        //Display confirm dialog 
        int confirmed = JOptionPane.showConfirmDialog(this, 
                "Do you want to reset knowledge acquisition process?", "Confirm Quit", 
                JOptionPane.YES_NO_OPTION); 
        
        //Close if user confirmed 
        if (confirmed == JOptionPane.YES_OPTION) 
        {       
            this.dispose();
            
            AddRuleGUI.execute(mode,currentCase,targetStackId);
        }
        
    }//GEN-LAST:event_resetButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        wizardStep -= 1;
        updateWizard();
    }//GEN-LAST:event_backButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        wizardStep += 1;
        updateWizard();
    }//GEN-LAST:event_nextButtonActionPerformed

    private void addNewConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewConclusionButtonActionPerformed
        newConclusionFrame.setVisible(true);
        DialogMain.addToWindowsList(newConclusionFrame);
    }//GEN-LAST:event_addNewConclusionButtonActionPerformed

    private void conclusionTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_conclusionTableKeyReleased
        updateSelectedConclusionFields();
    }//GEN-LAST:event_conclusionTableKeyReleased

    private void conclusionTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_conclusionTableMouseReleased
        updateSelectedConclusionFields();
    }//GEN-LAST:event_conclusionTableMouseReleased

    private void selectConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectConclusionButtonActionPerformed
        selectConclusion();
        
    }//GEN-LAST:event_selectConclusionButtonActionPerformed

    private void dicManagerOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dicManagerOpenButtonActionPerformed
        newDicTermApplyButton.setEnabled(true);
        
        showMessageDialog(null, "Please click 'Apply representative term' button after registration.");
        
        DicManagerGUI.execute();
    }//GEN-LAST:event_dicManagerOpenButtonActionPerformed

    private void newDicTermApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDicTermApplyButtonActionPerformed
        
        DialogMain.dicConverter.setDictionary(DialogMain.dictionary);
        String recentDialog = currentCase.getValue("Recent").toString();
        
        cmcrdr.logger.Logger.info("CONVERTING: from: " + recentDialog);
        recentDialog = DialogMain.dicConverter.convertTermFromDic(recentDialog,true);
        cmcrdr.logger.Logger.info("CONVERTING: to: " + recentDialog);

        
        currentCase.getValue("Recent").setValue(recentDialog);
        updateCurrentCaseTable();
        
    }//GEN-LAST:event_newDicTermApplyButtonActionPerformed

    private void actionVariableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionVariableCheckBoxActionPerformed
        if(actionVariableCheckBox.isSelected()){
            String selectedCommandAction =  (String) conclusionCommandActionComboBox.getSelectedItem();
            if(!selectedCommandAction.equals("")){
                deviceActionVariableTextField.setEnabled(true);
                actionDataClearButton.setEnabled(true);
                
                String action = (String) conclusionCommandActionComboBox.getSelectedItem();
                if (!action.isEmpty()) {
                    if (action.equals(DatabaseItemCommandInstance.saveSchemaItem) || action.equals(DatabaseItemCommandInstance.loadSchemaItem))
                        insertSchemaItemButton.setEnabled(true);
                    else if (action.equals(ContextVariableCommandInstance.SetContextVariable))
                         insertSetContextVarButton.setEnabled(true);
                    else if (action.equals(ContextVariableCommandInstance.UnsetContextVariable))
                         insertSetContextVarButton.setEnabled(true);
                    else if (action.equals(ContextVariableCommandInstance.SetParsedDateContextVariable))
                         insertSetContextVarButton.setEnabled(true);
                }
              
               

            } else {
                showMessageDialog(null, "Select Device.");
                actionVariableCheckBox.setSelected(false);
                deviceActionVariableTextField.setEnabled(false);
                actionDataClearButton.setEnabled(false);
            }
        } else {
            deviceActionVariableTextField.setEnabled(false);
            insertSchemaItemButton.setEnabled(false);
            insertSetContextVarButton.setEnabled(false);
            actionDataClearButton.setEnabled(false);
        }
    }//GEN-LAST:event_actionVariableCheckBoxActionPerformed

    private void deviceActionVariableTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deviceActionVariableTextFieldKeyTyped
        String deviceActionVariable = deviceActionVariableTextField.getText();
        if(!deviceActionVariable.equals("")){
            if(!deviceActionVariable.startsWith("_")) {
                showMessageDialog(null, "You must begin with the underscore character (_).");
            }
        }
    }//GEN-LAST:event_deviceActionVariableTextFieldKeyTyped

    private void addConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConclusionButtonActionPerformed
        if (conclusionDialogTextArea.getText().equals("")) {
            showMessageDialog(null,"Cannot add conclusion if IDS Answer is empty!");
        }
        else
            addConclusion();

    }//GEN-LAST:event_addConclusionButtonActionPerformed

    private void conclusionCommandCategoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conclusionCommandCategoryComboBoxActionPerformed
        String selectedCommandCategory = (String) conclusionCommandCategoryComboBox.getSelectedItem();
        if(conclusionCommandCategoryComboBox.getSelectedIndex()==0){
            updateActionComboBox(selectedCommandCategory);
        } else {
            updateActionComboBox(selectedCommandCategory);
        }

    }//GEN-LAST:event_conclusionCommandCategoryComboBoxActionPerformed

    private void showDatabaseQueryBuilderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDatabaseQueryBuilderButtonActionPerformed
        DatabaseQueryBuilderGUI dbBuilder = new DatabaseQueryBuilderGUI(this);
        dbBuilder.setVisible(true);
        
    }//GEN-LAST:event_showDatabaseQueryBuilderButtonActionPerformed

    private void deselectConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectConclusionButtonActionPerformed
        deselectConclusion();
    }//GEN-LAST:event_deselectConclusionButtonActionPerformed

    private void modifyCommandVariablesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyCommandVariablesButtonActionPerformed
        String recentOfCaseInputStr  = currentCase.getInputDialogInstance().toString();
        ContextVariableGUI context = new ContextVariableGUI(recentOfCaseInputStr);
        context.setVisible(true);
    }//GEN-LAST:event_modifyCommandVariablesButtonActionPerformed

    private void conclusionCommandActionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conclusionCommandActionComboBoxActionPerformed
        String selectedAction = (String)conclusionCommandActionComboBox.getSelectedItem();
        
        if (selectedAction != null) {
            if (selectedAction.equals(DatabaseItemCommandInstance.saveSchemaItem) || selectedAction.equals(DatabaseItemCommandInstance.loadSchemaItem)) {
                insertSchemaItemButton.setEnabled(actionVariableCheckBox.isEnabled());
            }
            else {
                insertSchemaItemButton.setEnabled(false);
            }
            
            if (selectedAction.equals(ContextVariableCommandInstance.SetContextVariable) || selectedAction.equals(ContextVariableCommandInstance.SetParsedDateContextVariable)
                    || selectedAction.equals(ContextVariableCommandInstance.UnsetContextVariable)) {
                insertSetContextVarButton.setEnabled(actionVariableCheckBox.isEnabled());
            }
            else {
                insertSetContextVarButton.setEnabled(false);
            }
        }
        

    }//GEN-LAST:event_conclusionCommandActionComboBoxActionPerformed

    private void userContextVariableManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userContextVariableManagerActionPerformed
        String recentOfCaseInputStr  = currentCase.getInputDialogInstance().toString();
        ContextVariableGUI context = new ContextVariableGUI(recentOfCaseInputStr);
        context.parent = this;
        context.setVisible(true);
    }//GEN-LAST:event_userContextVariableManagerActionPerformed

    private void dictionaryManagerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dictionaryManagerButtonActionPerformed
        //newDicTermApplyButton.setEnabled(true);
        
        //showMessageDialog(null, "Please click 'Apply representative term' button after registration.");
        
        DicManagerGUI.execute();
    }//GEN-LAST:event_dictionaryManagerButtonActionPerformed

    private void insertContextVariableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertContextVariableButtonActionPerformed
        String contextVariable = (String)contextVariableListComboBox.getSelectedItem();
        String conclusionTag;
        
        if (contextVariable != null) {
            if (intValueCheckbox.isSelected()) {
                conclusionTag = OutputParser.getTag(contextVariable, OutputParser.CONTEXT_NUMBER_TYPE);  
            }
            else {
                conclusionTag = OutputParser.getTag(contextVariable, OutputParser.CONTEXT_LITERAL_TYPE);
            }
            conclusionDialogTextArea.setText(conclusionDialogTextArea.getText() + conclusionTag);
        }
    }//GEN-LAST:event_insertContextVariableButtonActionPerformed

    private void previewConclusionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewConclusionButtonActionPerformed
        
        String conclusionToPreview;
        previewTextArea.setText("");
        
        
        //String [] separateQueries = databaseQueries.getText().split(OutputParser.getStartTag(OutputParser.DB_DATABASE_TYPE));
        //conclusionToPreview = OutputParser.getTag(conclusionDialogTextArea.getText() + databaseQueries.getText(),OutputParser.DB_DATABASE_TYPE);
        conclusionToPreview = conclusionDialogTextArea.getText();
        previewTextArea.setText(OutputParser.parseOtherTerms(OutputParser.replaceAllDatabaseTerms(conclusionToPreview,true),true));

        if (DBOperation.wasDatabaseError()) {
            previewTextArea.append("\n" + "Error: " + DBOperation.getLastDatabaseError());
            DBOperation.resetDatabaseError();
        }
        
       
      
        previewConclusionDialog.setVisible(true);
    }//GEN-LAST:event_previewConclusionButtonActionPerformed

    private void previewConclusionCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewConclusionCloseButtonActionPerformed
        previewConclusionDialog.setVisible(false);
    }//GEN-LAST:event_previewConclusionCloseButtonActionPerformed

    private void systemContextVariableManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemContextVariableManagerActionPerformed
        SystemContextVariableGUI systemManager = new SystemContextVariableGUI();
        systemManager.setVisible(true);
        
    }//GEN-LAST:event_systemContextVariableManagerActionPerformed

    private void databaseQueriesClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseQueriesClearButtonActionPerformed
        databaseQueries.setText("");
    }//GEN-LAST:event_databaseQueriesClearButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    private void newConclusionFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_newConclusionFrameWindowClosing
        DialogMain.removeFromWindowsList(newConclusionFrame);
    }//GEN-LAST:event_newConclusionFrameWindowClosing

    private void newConclusionFrameWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_newConclusionFrameWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu2);
    }//GEN-LAST:event_newConclusionFrameWindowActivated

    private void existingConclusionQueriesSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingConclusionQueriesSelectButtonActionPerformed
        int row = conclusionQueryTable.getSelectedRow();
        
        if (row >= 0) {
            int id = (int)conclusionQueryTable.getValueAt(row, 0);
            String description = (String) conclusionQueryTable.getValueAt(row, 1);
            String descriptionTag = OutputParser.getTag(description, OutputParser.DB_DESCRIPTION_TYPE);
            String queryPlaceholder = OutputParser.getTag(id + descriptionTag, OutputParser.DB_PLACEHOLDER_TYPE);
            conclusionDialogTextArea.setText(conclusionDialogTextArea.getText() + queryPlaceholder);
            databaseQueries.setText((String) conclusionQueryTable.getValueAt(row, 2));
            existingConclusionQueries.setVisible(false);
        }
        else {
            showMessageDialog(this, "You must select a row before choosing the select button!");
        }       
        
    }//GEN-LAST:event_existingConclusionQueriesSelectButtonActionPerformed

    private void insertDatabaseQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertDatabaseQueryButtonActionPerformed
        String [] columns = {"ID","Description","Query"};
        ArrayList<ArrayList<String>> conclusionQueriesArrayList = new ArrayList<>();
        DefaultTableModel theModel = new DefaultTableModel(columns,0);
        conclusionQueryTable.setModel(theModel);
        conclusionQueryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        conclusionQueryTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        conclusionQueryTable.getColumnModel().getColumn(2).setPreferredWidth(600);

        int count = 0;
              
        Iterator iter = DialogMain.conclusionQueryList.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            int id = (int) me.getKey();
            ConclusionQuery aConclusionQuery = (ConclusionQuery)me.getValue();
            ArrayList row = new ArrayList<>();
            row.add(id);
            row.add(aConclusionQuery.getDescription());
            row.add(aConclusionQuery.getQuery());
            conclusionQueriesArrayList.add(row);
            theModel.addRow(conclusionQueriesArrayList.get(count).toArray());
            count++;
        }
        
        conclusionQueryTable.setModel(theModel);
        conclusionQueryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        existingConclusionQueries.setVisible(true);
    }//GEN-LAST:event_insertDatabaseQueryButtonActionPerformed

    private void existingConclusionQueriesCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingConclusionQueriesCancelButtonActionPerformed
        existingConclusionQueries.setVisible(false);
    }//GEN-LAST:event_existingConclusionQueriesCancelButtonActionPerformed

    private void noHelpCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noHelpCheckBoxActionPerformed
        if (noHelpCheckBox.isSelected()) {
            conclusionDialogTextArea.setText(conclusionDialogTextArea.getText() + " " + OutputParser.METANOHELP);
        }
        else {
            String temp = conclusionDialogTextArea.getText();
            temp = temp.replaceAll(" " + OutputParser.METANOHELP, "");
            conclusionDialogTextArea.setText(temp);
        }
    }//GEN-LAST:event_noHelpCheckBoxActionPerformed

    private void replaceEmptyCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceEmptyCheckboxActionPerformed
        if (replaceEmptyCheckbox.isSelected()) {
            String existingText = conclusionDialogTextArea.getText();
            if (!existingText.isEmpty())
                existingText += " ";
            conclusionDialogTextArea.setText(existingText + 
                    OutputParser.META_REPLACE_START + 
                    " query-goes-here " +
                    OutputParser.META_REPLACE_EMPTY + " no-result-text-goes-here " +
                    OutputParser.META_REPLACE_TEXT + " override-text-goes-here  " +
                    OutputParser.META_REPLACE_END);
        }
        else {
            String temp = conclusionDialogTextArea.getText();
            if (temp.contains(OutputParser.META_REPLACE_EMPTY)) {
                temp = temp.substring(0,temp.indexOf(OutputParser.META_REPLACE_EMPTY)-1);
                conclusionDialogTextArea.setText(temp);
            }

        }
    }//GEN-LAST:event_replaceEmptyCheckboxActionPerformed

    private void insertSaveDBContextValueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSaveDBContextValueButtonActionPerformed
        String contextVariable = (String)saveDBContextValueComboBox.getSelectedItem();
        
        if (contextVariable != null) {
            if(actionVariableCheckBox.isSelected()){
                deviceActionVariableTextField.setText(deviceActionVariableTextField.getText() + "_@" + contextVariable);
                
                insertSchemaItemButton.setEnabled(false);
                insertSaveDBContextValueButton.setEnabled(false);
                insertSaveDBLiteralButton.setEnabled(false);
                
            }
            
        }
    }//GEN-LAST:event_insertSaveDBContextValueButtonActionPerformed

    private void insertSchemaItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSchemaItemButtonActionPerformed
        String schemaItem = (String)schemaItemsComboBox.getSelectedItem();
        
        if (schemaItem != null) {
            if(actionVariableCheckBox.isSelected()){
                deviceActionVariableTextField.setText("_" + schemaItem);
               
               insertSchemaItemButton.setEnabled(false);
               insertSaveDBContextValueButton.setEnabled(true);
               insertSaveDBLiteralButton.setEnabled(true);
            }

            
        }
    }//GEN-LAST:event_insertSchemaItemButtonActionPerformed

    private void insertSetContextVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSetContextVarButtonActionPerformed
        String contextVariable = (String)setContextVarListComboBox.getSelectedItem();
        String action = (String) conclusionCommandActionComboBox.getSelectedItem();
        
        if (contextVariable != null) {
            if(actionVariableCheckBox.isSelected()){
                deviceActionVariableTextField.setText(deviceActionVariableTextField.getText() + "_" + contextVariable);
                
                insertSetContextVarButton.setEnabled(false);
                if (action.equals(ContextVariableCommandInstance.SetContextVariable) || action.equals(ContextVariableCommandInstance.SetParsedDateContextVariable)) {
                    insertSetContextVarValueButton.setEnabled(true);
                    if (action.equals(ContextVariableCommandInstance.SetContextVariable))
                        insertSetContextVarLiteralButton.setEnabled(true);
                    else 
                        insertSetContextVarLiteralButton.setEnabled(false);
                }
            }
            
        }
    }//GEN-LAST:event_insertSetContextVarButtonActionPerformed

    private void insertSetContextVarValueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSetContextVarValueButtonActionPerformed
        String contextVariable = (String)setContextVarValueListComboBox.getSelectedItem();
        
        if (contextVariable != null) {
            if(actionVariableCheckBox.isSelected()){
                deviceActionVariableTextField.setText(deviceActionVariableTextField.getText() + "_@" + contextVariable);
                
                insertSetContextVarButton.setEnabled(false);
                insertSetContextVarValueButton.setEnabled(false);
                insertSetContextVarLiteralButton.setEnabled(false);
            }
            
        }
    }//GEN-LAST:event_insertSetContextVarValueButtonActionPerformed

    private void insertSetContextVarLiteralButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSetContextVarLiteralButtonActionPerformed
        String literalValue = setContextVarLiteralTextField.getText();
        
        if (literalValue != null) {
            if(actionVariableCheckBox.isSelected()){
                deviceActionVariableTextField.setText(deviceActionVariableTextField.getText() + "_" + literalValue);
                
                insertSetContextVarButton.setEnabled(false);
                insertSetContextVarValueButton.setEnabled(false);
                insertSetContextVarLiteralButton.setEnabled(false);
            }
            
        }
    }//GEN-LAST:event_insertSetContextVarLiteralButtonActionPerformed

    private void insertContextVariable2Button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertContextVariable2Button1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_insertContextVariable2Button1ActionPerformed

    private void setContextVarLiteralTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setContextVarLiteralTextFieldActionPerformed
        String enteredText = deviceActionVariableTextField.getText();
        if (!enteredText.isEmpty()) {
            if (!setContextVarLiteralTextField.getText().isEmpty())
                insertSetContextVarLiteralButton.setEnabled(true);
            else
                insertSetContextVarLiteralButton.setEnabled(false);     
        }
        else
            insertSetContextVarLiteralButton.setEnabled(false);


        
    }//GEN-LAST:event_setContextVarLiteralTextFieldActionPerformed

    private void actionDataClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionDataClearButtonActionPerformed
        deviceActionVariableTextField.setText("");
        
        String action = (String) conclusionCommandActionComboBox.getSelectedItem();
        if (!action.isEmpty()) {
            if (action.equals(DatabaseItemCommandInstance.saveSchemaItem))
                insertSchemaItemButton.setEnabled(actionVariableCheckBox.isEnabled());
            else if (action.equals(ContextVariableCommandInstance.SetContextVariable))
                 insertSetContextVarButton.setEnabled(actionVariableCheckBox.isEnabled());
        }
    }//GEN-LAST:event_actionDataClearButtonActionPerformed

    private void saveDBLiteralValueTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDBLiteralValueTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveDBLiteralValueTextFieldActionPerformed

    private void insertSaveDBLiteralButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSaveDBLiteralButtonActionPerformed
        String literalValue = (String)saveDBLiteralValueTextField.getText();
        
        if (!literalValue.isEmpty()) {
            if(actionVariableCheckBox.isSelected()){
                deviceActionVariableTextField.setText(deviceActionVariableTextField.getText() + "_" + literalValue);
                insertSchemaItemButton.setEnabled(false);
                insertSaveDBContextValueButton.setEnabled(false);
                insertSaveDBLiteralButton.setEnabled(false);
            }
            
        }
        
    }//GEN-LAST:event_insertSaveDBLiteralButtonActionPerformed

    private void conValFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conValFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_conValFieldActionPerformed

    private void conOperComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conOperComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_conOperComboBoxActionPerformed

    private void addStopRuleCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStopRuleCheckboxActionPerformed
        // TODO add your handling code here:
        if (addStopRuleCheckbox.isSelected()) {
            addRuleButton.setEnabled(true);
        }
    }//GEN-LAST:event_addStopRuleCheckboxActionPerformed

    /**
     *
     * @param aMode
     * @param aCase
     * @param pDialog
     * @param aInputDialog
     * @param stackId
     */
    public static void execute(String aMode, DialogCase aCase, IDialogInstance pDialog, IDialogInstance aInputDialog, int stackId) {
        Logger.info("execute1: aMode is set to " + aMode);
        
        currentCase = aCase;
        newInputDialog = aInputDialog;
        prevDialog = pDialog;
        targetStackId = stackId;
        
        if(aMode.equals("add")){
            Logger.info("execute1: !!!!!!!!!!!!!!!!!!!!! this will never happen - mode set to alter!!!!!!!!!!!!!!!!");
        currentCase = aCase;
            mode = "alter"; 
        } else {
            mode = "exception";
            Logger.info("execute1: mode is set to " + mode);
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
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            public void run() {
                new AddRuleGUI().setVisible(true);
            }
        });
    }
    
    /**
     *
     * @param aMode
     * @param aCase
     * @param stackId
     */
    public static void execute(String aMode, DialogCase aCase, int stackId) {
        Logger.info("execute2: mode is " + aMode);
        Logger.info("execute2: case is " + aCase.toString());
        Logger.info("execute2: stackId is " + stackId);

        
        if (Main.workbench.getStackedInferenceResult() != null) {
            if (Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId) != null) {
                Logger.info("Applying RuleSet: " + Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).getInferenceResult());
            }
            else {
                Logger.info("Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(" + stackId + ") is null");
            }           
        }
        else {
            Logger.info("Main.workbench.getStackedInferenceResult() is null");
        }
        
        Logger.info("Here's were I screwed up: dialog user's KA result should be:");
        RuleSet applyingRuleset = DialogMain.getDialogUserList().getCurrentKAInferenceResult();
        for (Rule aRule: applyingRuleset.getBase().values()) {
            Logger.info("Dialog users' starting KA inference rule found: " + aRule);
        }
        
        currentCase = aCase;
        newInputDialog = null;
        targetStackId = stackId;
        
        if(aMode.equals("add")){
            Logger.info("Setting mode to alter");
            mode = "alter"; 
        } else if(aMode.equals("modify")){
            Logger.info("Setting mode to exception");
            mode = "exception";
        } else if (aMode.equals("new")) {// DPH added May 2019
            Logger.info("Setting mode to new");
            mode = "new";
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
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddRuleGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            public void run() {
                new AddRuleGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CaseViewerPanel;
    private javax.swing.JButton actionDataClearButton;
    private javax.swing.JCheckBox actionVariableCheckBox;
    private javax.swing.JButton addConclusionButton;
    private javax.swing.JButton addNewConclusionButton;
    private javax.swing.JButton addRuleButton;
    private javax.swing.JCheckBox addStopRuleCheckbox;
    private javax.swing.JButton backButton;
    private javax.swing.JButton conAddButton;
    private javax.swing.JComboBox conAttrComboBox;
    private javax.swing.JComboBox conOperComboBox;
    private javax.swing.JTextField conValField;
    private javax.swing.JComboBox conclusionCommandActionComboBox;
    private javax.swing.JComboBox conclusionCommandCategoryComboBox;
    private javax.swing.JTextArea conclusionDialogTextArea;
    private javax.swing.JLabel conclusionLabel;
    private javax.swing.JTable conclusionQueryTable;
    private javax.swing.JTable conclusionTable;
    private javax.swing.JPopupMenu conditionControlPopup;
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JTable conditionListTable;
    private javax.swing.JComboBox<String> contextVariableList2ComboBox1;
    private javax.swing.JComboBox<String> contextVariableListComboBox;
    private javax.swing.JLabel currentCaseLabel;
    private javax.swing.JTable currentCaseTable;
    private javax.swing.JTextArea databaseQueries;
    private javax.swing.JButton databaseQueriesClearButton;
    private javax.swing.JMenuItem deleteSelectedItem;
    private javax.swing.JButton deselectConclusionButton;
    private javax.swing.JTextField deviceActionVariableTextField;
    private javax.swing.JTextArea dialogHistoryTextArea;
    private javax.swing.JButton dicManagerOpenButton;
    private javax.swing.JButton dictionaryManagerButton;
    private javax.swing.JCheckBox doNotStackCheckbox;
    private javax.swing.JDialog existingConclusionQueries;
    private javax.swing.JButton existingConclusionQueriesCancelButton;
    private javax.swing.JButton existingConclusionQueriesSelectButton;
    private javax.swing.JButton insertContextVariable2Button1;
    private javax.swing.JButton insertContextVariableButton;
    private javax.swing.JButton insertDatabaseQueryButton;
    private javax.swing.JButton insertSaveDBContextValueButton;
    private javax.swing.JButton insertSaveDBLiteralButton;
    private javax.swing.JButton insertSchemaItemButton;
    private javax.swing.JButton insertSetContextVarButton;
    private javax.swing.JButton insertSetContextVarLiteralButton;
    private javax.swing.JButton insertSetContextVarValueButton;
    private javax.swing.JCheckBox intValueCheckbox;
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
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JLayeredPane jLayeredPane3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
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
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton modifyCommandVariablesButton;
    private javax.swing.JFrame newConclusionFrame;
    private javax.swing.JButton newDicTermApplyButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JCheckBox noHelpCheckBox;
    private javax.swing.JButton previewConclusionButton;
    private javax.swing.JButton previewConclusionCloseButton;
    private javax.swing.JDialog previewConclusionDialog;
    private javax.swing.JTextArea previewTextArea;
    private javax.swing.JCheckBox replaceEmptyCheckbox;
    private javax.swing.JButton resetButton;
    private javax.swing.JComboBox<String> saveDBContextValueComboBox;
    private javax.swing.JTextField saveDBLiteralValueTextField;
    private javax.swing.JComboBox<String> schemaItemsComboBox;
    private javax.swing.JButton selectConclusionButton;
    private javax.swing.JPanel selectConclusionPanel;
    private javax.swing.JPanel selectConditionPanel;
    private javax.swing.JTextField selectedCommandActionField;
    private javax.swing.JComboBox selectedCommandCategoryComboBox;
    private javax.swing.JTextField selectedDialogStrField;
    private javax.swing.JComboBox<String> setContextVarListComboBox;
    private javax.swing.JTextField setContextVarLiteralTextField;
    private javax.swing.JComboBox<String> setContextVarValueListComboBox;
    private javax.swing.JButton showDatabaseQueryBuilderButton;
    private javax.swing.JButton systemContextVariableManager;
    private javax.swing.JButton userContextVariableManager;
    private javax.swing.JLabel valCaseLabel;
    private javax.swing.JLabel valCaseListLabel;
    private javax.swing.JTable valCaseListTable;
    private javax.swing.JButton valCaseNextButton;
    private javax.swing.JButton valCasePrevButton;
    private javax.swing.JTable valCaseTable;
    private javax.swing.JList valConclusionList;
    private javax.swing.JButton validationAcceptButton;
    private javax.swing.JPanel validationStepPanel;
    private javax.swing.JMenu windowsMenu;
    private javax.swing.JMenu windowsMenu2;
    private javax.swing.JPanel wizardPanel;
    private javax.swing.JFrame wrongConclusionFrame;
    private javax.swing.JList wrongConclusionList;
    private javax.swing.JButton wrongConclusionSelectButton;
    // End of variables declaration//GEN-END:variables
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.handler.OutputParser;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import static cmcrdr.mysql.DBConnection.getIsDatabaseUsed;
import cmcrdr.mysql.DBOperation;
import cmcrdr.savedquery.ConclusionQuery;
import cmcrdr.savedquery.SavedQueryTemplate;
import cmcrdr.mysql.DBOperation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class DatabaseQueryBuilderGUI extends javax.swing.JFrame {

    Object theParentCallback = null;
    private String tableViewerQuery = "";
    private static int selectedTableRefId;
    private javax.swing.JTextField tableViewerTextFieldTarget;

    private int tableViewerType;
    public static final int JOINTYPE = 0;
    public static final int SELECTTYPE = 1;
    public static final int CRITERIONTYPE = 2;
    public static final int ORDERTYPE = 3;
    
    
    public static int currentMarkerType;
    
    private static int queryNumber = 0;
    private static Object [][] tableNames = null;
    
    
    /**
     * Creates new form databaseQueryBuilderGUI
     * @param parentCaller
     */
    public DatabaseQueryBuilderGUI(Object parentCaller) {
        initComponents();
        theParentCallback = parentCaller;
        criterionButtonGroup.add(fixedValueRadioButton);
        criterionButtonGroup.add(contextValueRadioButton);
        contextVariableComboBox.setModel(new DefaultComboBoxModel(DialogMain.globalContextVariableManager.getContextVariablesStringArrayList().toArray()));
        contextVariableComboBox1.setModel(new DefaultComboBoxModel(DialogMain.globalContextVariableManager.getContextVariablesStringArrayList().toArray()));

        if (contextVariableComboBox.getModel().getSize() == 0) {
            contextVariableComboBox.setEnabled(false);
        }
        DialogMain.addToWindowsList(this);
        DialogMain.populateWindowsMenu(windowsMenu);

    }
    
    public DatabaseQueryBuilderGUI() {       
        
    }
    
    //private Vector<Vector<String>> getDataModelFromDB() {
    public static ArrayList<ArrayList<String>> getDataModelFromDB(String query) {
      
        HashMap <String,String> databaseSettings;

        databaseSettings= DialogMain.getReferenceDatabaseDetails();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        
        
        //ResultSet  rs = DBOperation.selectQuery("select " + databaseSettings.get("databasePrimaryColumn") + "," + databaseSettings.get("databaseDescriptionColumn") + " from " + databaseSettings.get("databaseTable"));
        ResultSet  rs = DBOperation.selectQuery(query);
        //System.out.println("QUERY IS: select " + databaseSettings.get("databasePrimaryColumn") + "," + databaseSettings.get("databaseDescriptionColumn") + " from " + databaseSettings.get("databaseTable"));
        //Logger.info("QUERY IS: " + query);
        
       // Vector<Vector<String>> result = new Vector<>();
        
        if (rs != null) {
            try {
                int numCols = rs.getMetaData().getColumnCount();
                
                // Add the column names to the first row
                ArrayList<String> row = new ArrayList<>(numCols);
                for (int i = 1; i <= numCols; i++) {
                        row.add(rs.getMetaData().getColumnLabel(i));
                        //Logger.info("Adding column name to first row: " + rs.getMetaData().getColumnLabel(i));
                }
                result.add(row);
                
                // Add the rest of the fetched data 
                while (rs.next()) {
                    row = new ArrayList<>(numCols);
                    for (int i = 1; i <= numCols; i++) {
                        row.add(rs.getString(i));
                        //Logger.info("Adding the following row data:"  + rs.getString(i));
                    }
                    result.add(row);
                }
            }
            catch (SQLException e) {
                Logger.info(e.getMessage());
            }
        }
        
        //Logger.info("result size is: " +  result.size());
        return result;

    }
    
    /**
     *
     * @param query
     * @return
     */
    public static TableModel getTableModel(String query) {
        
        TableModel model = new DefaultTableModel();
        
        if (getIsDatabaseUsed()) {
        // Get the actual field names from the currently referenced database..

            HashMap<String,String> databaseSettings;
            databaseSettings= DialogMain.getReferenceDatabaseDetails();


            //ArrayList<ArrayList<String>> list = getDataModelFromDB("select table_name,table_comment from information_schema.tables where table_schema='" + databaseSettings.get("databaseDatabase") + "'");
            ArrayList<ArrayList<String>> list = getDataModelFromDB(query);

            String idColumn = databaseSettings.get("databasePrimaryColumn");        
            String descColumn = databaseSettings.get("databaseDescriptionColumn");
           // String [] columnNames = new String[] {idColumn, descColumn};
            ArrayList<String> columnLabelsRow = list.get(0);

            String [] columnNames = columnLabelsRow.toArray(new String[columnLabelsRow.size()]);

            // Convert ArrayList<ArrayList<String>> to Object[][] for DefaultTableModel call..

            Object[][] modelList = new Object[list.size()-1][];
            for (int i = 1; i < list.size(); i++) {
                ArrayList<String> row = list.get(i);
                modelList[i-1] = row.toArray(new String[row.size()]);
            }

            model = new DefaultTableModel(modelList,columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
           
        }
        
        //model.get
    
        return model;
    }
    
    // used by  callers not using JDialog TableModel etc
    public static Object[][] getQueryModelResult(String query) {
        Object[][] response = null;
        
        if (getIsDatabaseUsed()) {
        // Get the actual field names from the currently referenced database..
            
            
            HashMap<String,String> databaseSettings;
            databaseSettings= DialogMain.getReferenceDatabaseDetails();
            
            ArrayList<ArrayList<String>> list = DatabaseQueryBuilderGUI.getDataModelFromDB(query);

            String idColumn = databaseSettings.get("databasePrimaryColumn");        
            String descColumn = databaseSettings.get("databaseDescriptionColumn");

            ArrayList<String> columnLabelsRow = list.get(0);

            Object[][] modelList = new Object[list.size()-1][];
            for (int i = 1; i < list.size(); i++) {
                ArrayList<String> row = list.get(i);
                modelList[i-1] = row.toArray(new String[row.size()]);
            }
            
            response = modelList;
           
        }
        return response;
    }
    
    public static void setReferenceTableNames() {
        String query = "select table_name from information_schema.tables where table_schema='" + cmcrdr.main.DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' order by table_name";
        tableNames = getQueryModelResult(query);
    }
    
    public static Object[][] getReferenceTableNames() {
        return tableNames;
    }
    
    /**
     *
     * @param query
     */
    public void setTableViewerQuery(String query) {
        tableViewerQuery = query;
    }
    
    /**
     *
     * @return
     */
    public String getTableViewerQuery() {
        return tableViewerQuery;
    }
    
    /**
     *
     * @return
     */
    public static int getselectedTableRefId() {
        return selectedTableRefId;
    }
    
    /**
     *
     * @param selected
     */
    public static void setSelectedTableRefId(int selected) {
        selectedTableRefId = selected;
    }
    
    /**
     *
     * @return
     */
    public static ArrayList<String> getAllReferenceDatabaseTableNames() {
        ArrayList<String> result = new ArrayList<>();
        ResultSet rs = null;

        rs = DBOperation.selectQuery("select table_name from information_schema.tables where table_schema='" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' order by table_name");

        
        if (rs != null) {
            try {
                while (rs.next()) {
                    String table = rs.getString("table_name");
                    result.add(table);
                }
            }
            catch (SQLException e) {
                Logger.info(e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     *
     * @param table
     * @return
     */
    public static ArrayList<String> getAllReferenceDatabaseFieldNames(String table) {
        ArrayList<String> result = new ArrayList<>();

        ResultSet rs = null;

        rs = DBOperation.selectQuery("select column_name from information_schema.columns where table_schema='" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + table + "'");

        try {
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                result.add(columnName);
            }
        }
        catch (SQLException e) {
            Logger.info(e.getMessage());
        }
        
        return result;
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableViewerDialog = new javax.swing.JDialog();
        jScrollPane11 = new javax.swing.JScrollPane();
        tableViewer = new javax.swing.JTable();
        selectButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        tableName = new javax.swing.JLabel();
        selectFormatButton = new javax.swing.JButton();
        criterionButtonGroup = new javax.swing.ButtonGroup();
        formatDialog = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        formatField = new javax.swing.JTextField();
        formatOKButton = new javax.swing.JButton();
        formatCancelButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        prefixText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        postfixText = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        formatTable = new javax.swing.JTextField();
        criterionDialog = new javax.swing.JDialog();
        jPanel10 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        criterionFixedValue = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        contextVariableComboBox = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        partialMatchCheckbox = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        fixedAndNumericRadioButton = new javax.swing.JRadioButton();
        contextValueRadioButton = new javax.swing.JRadioButton();
        fixedValueRadioButton = new javax.swing.JRadioButton();
        fixedAndContextRadioButton = new javax.swing.JRadioButton();
        contextValueNumberRadioButton = new javax.swing.JRadioButton();
        criterionField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        criterionTable = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        criterionInsertButton = new javax.swing.JButton();
        criterionCancelButton = new javax.swing.JButton();
        joinDialog = new javax.swing.JDialog();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        joinTableLeft = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        joinTableRight = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        selectLeftButton = new javax.swing.JButton();
        selectRightButton = new javax.swing.JButton();
        multivaluedCheckBox = new javax.swing.JCheckBox();
        joinConditionsCancelButton = new javax.swing.JButton();
        joinConditionsOKButton = new javax.swing.JButton();
        loadSaveQueryTemplateDialog = new javax.swing.JDialog();
        jPanel4 = new javax.swing.JPanel();
        saveQueryCloseButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        savedQueryItemsJList = new javax.swing.JList<>();
        loadQueryButton = new javax.swing.JButton();
        deleteQueryButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        newQueryName = new javax.swing.JTextField();
        saveQueryButton = new javax.swing.JButton();
        previewConclusionDialog = new javax.swing.JDialog();
        previewConclusionCloseButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        previewTextArea = new javax.swing.JTextArea();
        existingConclusionQueries = new javax.swing.JDialog();
        jScrollPane2 = new javax.swing.JScrollPane();
        conclusionQueryTable = new javax.swing.JTable();
        existingConclusionQueriesCloseButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        javax.swing.JButton saveButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        dbConclusionReferenceTables = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        joinFields = new javax.swing.JTextField();
        chooseJoinFieldsButton = new javax.swing.JButton();
        joinClearButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        criteriaField = new javax.swing.JTextField();
        chooseCriterionButton = new javax.swing.JButton();
        criterionClearButton = new javax.swing.JButton();
        builderCancelButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        selectedFields = new javax.swing.JTextField();
        chooseSelectedFieldsButton = new javax.swing.JButton();
        selectClearButton = new javax.swing.JButton();
        countCheckBox = new javax.swing.JCheckBox();
        chooseOrderByButton = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        contextVariableComboBox1 = new javax.swing.JComboBox<>();
        insertContextVariableSelectorButton = new javax.swing.JButton();
        databaseReferenceBuilderPreviewButton = new javax.swing.JButton();
        loadSaveQueryButton = new javax.swing.JButton();
        conclusionQueryDescription = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        showExistingQueries = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        tableViewerDialog.setMinimumSize(new java.awt.Dimension(360, 325));

        tableViewer.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane11.setViewportView(tableViewer);

        selectButton.setText("Select");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        tableName.setText("placeholder");

        selectFormatButton.setText("Select with FORMAT");
        selectFormatButton.setMinimumSize(new java.awt.Dimension(365, 310));
        selectFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFormatButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tableViewerDialogLayout = new javax.swing.GroupLayout(tableViewerDialog.getContentPane());
        tableViewerDialog.getContentPane().setLayout(tableViewerDialogLayout);
        tableViewerDialogLayout.setHorizontalGroup(
            tableViewerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableViewerDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tableViewerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tableViewerDialogLayout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(selectFormatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectButton))
                    .addGroup(tableViewerDialogLayout.createSequentialGroup()
                        .addComponent(tableName, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        tableViewerDialogLayout.setVerticalGroup(
            tableViewerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableViewerDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(tableViewerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectButton)
                    .addComponent(cancelButton)
                    .addComponent(selectFormatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        formatDialog.setMinimumSize(new java.awt.Dimension(415, 180));
        formatDialog.setModal(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Format field");

        formatField.setEnabled(false);

        formatOKButton.setText("OK");
        formatOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatOKButtonActionPerformed(evt);
            }
        });

        formatCancelButton.setText("Cancel");
        formatCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatCancelButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Prefix text");

        jLabel4.setText("Postfix text");

        jLabel7.setText("Format table");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(formatCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(formatOKButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(postfixText)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(formatTable, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(formatField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(prefixText))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(formatField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(formatTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prefixText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(postfixText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formatOKButton)
                    .addComponent(formatCancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout formatDialogLayout = new javax.swing.GroupLayout(formatDialog.getContentPane());
        formatDialog.getContentPane().setLayout(formatDialogLayout);
        formatDialogLayout.setHorizontalGroup(
            formatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        formatDialogLayout.setVerticalGroup(
            formatDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formatDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        criterionDialog.setMinimumSize(new java.awt.Dimension(650, 320));

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel22.setText("Fixed value:");

        jLabel12.setText("Context variable");

        contextVariableComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextVariableComboBoxActionPerformed(evt);
            }
        });

        jLabel10.setText("Criterion matching value");

        partialMatchCheckbox.setSelected(true);
        partialMatchCheckbox.setText("allow partial match (e.g. robot is matched in robotic)");

        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        criterionButtonGroup.add(fixedAndNumericRadioButton);
        fixedAndNumericRadioButton.setText("fixed value with number from context variable");

        criterionButtonGroup.add(contextValueRadioButton);
        contextValueRadioButton.setText("context variable");

        criterionButtonGroup.add(fixedValueRadioButton);
        fixedValueRadioButton.setSelected(true);
        fixedValueRadioButton.setText("fixed value");

        criterionButtonGroup.add(fixedAndContextRadioButton);
        fixedAndContextRadioButton.setText("fixed value with context variable");
        fixedAndContextRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixedAndContextRadioButtonActionPerformed(evt);
            }
        });

        criterionButtonGroup.add(contextValueNumberRadioButton);
        contextValueNumberRadioButton.setText("number from context variable");
        contextValueNumberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contextValueNumberRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fixedAndContextRadioButton)
                    .addComponent(fixedValueRadioButton)
                    .addComponent(fixedAndNumericRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contextValueNumberRadioButton)
                    .addComponent(contextValueRadioButton))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fixedValueRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fixedAndContextRadioButton)
                    .addComponent(contextValueRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fixedAndNumericRadioButton)
                    .addComponent(contextValueNumberRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(criterionFixedValue, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(contextVariableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(partialMatchCheckbox))
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(partialMatchCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(criterionFixedValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(contextVariableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        criterionField.setEditable(false);

        jLabel13.setText("Table");

        criterionTable.setEditable(false);

        jLabel14.setText("Field");

        jLabel15.setText("Criterion creation");

        criterionInsertButton.setText("Insert");
        criterionInsertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                criterionInsertButtonActionPerformed(evt);
            }
        });

        criterionCancelButton.setText("Cancel");
        criterionCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                criterionCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout criterionDialogLayout = new javax.swing.GroupLayout(criterionDialog.getContentPane());
        criterionDialog.getContentPane().setLayout(criterionDialogLayout);
        criterionDialogLayout.setHorizontalGroup(
            criterionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(criterionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(criterionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, criterionDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(criterionCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(criterionInsertButton))
                    .addGroup(criterionDialogLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(criterionDialogLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(criterionTable, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(criterionField)))
                .addContainerGap())
        );
        criterionDialogLayout.setVerticalGroup(
            criterionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(criterionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addGap(26, 26, 26)
                .addGroup(criterionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(criterionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(criterionTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(criterionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(criterionCancelButton)
                    .addComponent(criterionInsertButton))
                .addContainerGap())
        );

        joinDialog.setMinimumSize(new java.awt.Dimension(835, 210));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Join conditions");

        jLabel11.setText("Left table and field");

        joinTableLeft.setEditable(false);

        jLabel16.setText("Right table and field");

        joinTableRight.setEditable(false);
        joinTableRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinTableRightActionPerformed(evt);
            }
        });

        jLabel17.setText("=");

        selectLeftButton.setText("Select left value");
        selectLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectLeftButtonActionPerformed(evt);
            }
        });

        selectRightButton.setText("Select right value");
        selectRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRightButtonActionPerformed(evt);
            }
        });

        multivaluedCheckBox.setText("Multivalued");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel11)))
                            .addComponent(selectLeftButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 392, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(multivaluedCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectRightButton))
                            .addComponent(jLabel16)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(joinTableLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(joinTableRight, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {joinTableLeft, joinTableRight});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(joinTableLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(joinTableRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectLeftButton)
                    .addComponent(selectRightButton)
                    .addComponent(multivaluedCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        joinConditionsCancelButton.setText("Cancel");
        joinConditionsCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinConditionsCancelButtonActionPerformed(evt);
            }
        });

        joinConditionsOKButton.setText("OK");
        joinConditionsOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinConditionsOKButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout joinDialogLayout = new javax.swing.GroupLayout(joinDialog.getContentPane());
        joinDialog.getContentPane().setLayout(joinDialogLayout);
        joinDialogLayout.setHorizontalGroup(
            joinDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(joinDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(joinDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, joinDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(joinConditionsCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(joinConditionsOKButton)))
                .addContainerGap())
        );
        joinDialogLayout.setVerticalGroup(
            joinDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(joinDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(joinDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(joinConditionsCancelButton)
                    .addComponent(joinConditionsOKButton))
                .addContainerGap())
        );

        loadSaveQueryTemplateDialog.setMinimumSize(new java.awt.Dimension(300, 375));

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        saveQueryCloseButton.setText("Close");
        saveQueryCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveQueryCloseButtonActionPerformed(evt);
            }
        });

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel18.setText("Saved query snippets");

        savedQueryItemsJList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(savedQueryItemsJList);

        loadQueryButton.setText("Load query");
        loadQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadQueryButtonActionPerformed(evt);
            }
        });

        deleteQueryButton.setText("Delete snippet");
        deleteQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteQueryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(deleteQueryButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(loadQueryButton)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadQueryButton)
                    .addComponent(deleteQueryButton))
                .addContainerGap())
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel19.setText("New query snippet description");

        saveQueryButton.setText("Save current query template");
        saveQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveQueryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newQueryName)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveQueryButton)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newQueryName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveQueryButton)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveQueryCloseButton)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveQueryCloseButton))
        );

        javax.swing.GroupLayout loadSaveQueryTemplateDialogLayout = new javax.swing.GroupLayout(loadSaveQueryTemplateDialog.getContentPane());
        loadSaveQueryTemplateDialog.getContentPane().setLayout(loadSaveQueryTemplateDialogLayout);
        loadSaveQueryTemplateDialogLayout.setHorizontalGroup(
            loadSaveQueryTemplateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadSaveQueryTemplateDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        loadSaveQueryTemplateDialogLayout.setVerticalGroup(
            loadSaveQueryTemplateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadSaveQueryTemplateDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        previewTextArea.setRows(5);
        jScrollPane5.setViewportView(previewTextArea);

        javax.swing.GroupLayout previewConclusionDialogLayout = new javax.swing.GroupLayout(previewConclusionDialog.getContentPane());
        previewConclusionDialog.getContentPane().setLayout(previewConclusionDialogLayout);
        previewConclusionDialogLayout.setHorizontalGroup(
            previewConclusionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, previewConclusionDialogLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(previewConclusionCloseButton))
            .addGroup(previewConclusionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        previewConclusionDialogLayout.setVerticalGroup(
            previewConclusionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewConclusionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewConclusionCloseButton)
                .addContainerGap())
        );

        existingConclusionQueries.setTitle("Existing Conclusion Queries");
        existingConclusionQueries.setMinimumSize(new java.awt.Dimension(900, 315));

        conclusionQueryTable.setModel(new javax.swing.table.DefaultTableModel(
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
        conclusionQueryTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane2.setViewportView(conclusionQueryTable);

        existingConclusionQueriesCloseButton.setText("Close");
        existingConclusionQueriesCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingConclusionQueriesCloseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout existingConclusionQueriesLayout = new javax.swing.GroupLayout(existingConclusionQueries.getContentPane());
        existingConclusionQueries.getContentPane().setLayout(existingConclusionQueriesLayout);
        existingConclusionQueriesLayout.setHorizontalGroup(
            existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(existingConclusionQueriesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, existingConclusionQueriesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(existingConclusionQueriesCloseButton)))
                .addContainerGap())
        );
        existingConclusionQueriesLayout.setVerticalGroup(
            existingConclusionQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(existingConclusionQueriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(existingConclusionQueriesCloseButton)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Database Query Builder");
        setMinimumSize(new java.awt.Dimension(795, 500));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        saveButton.setText("Save ");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jScrollPane4.setViewportBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane4.setAutoscrolls(true);
        jScrollPane4.setPreferredSize(new java.awt.Dimension(100, 300));

        dbConclusionReferenceTables.setModel(getTableModel("select table_name from information_schema.tables where table_schema='" + cmcrdr.main.DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' order by table_name"));
        dbConclusionReferenceTables.getTableHeader().setReorderingAllowed(false);
        dbConclusionReferenceTables.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                //conclusionDialogStrField.setText("@DBTABLE@" + dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0) + "@");
            }
        });

        dbConclusionReferenceTables.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        dbConclusionReferenceTables.getTableHeader().setReorderingAllowed(false);
        if(dbConclusionReferenceTables.getColumnModel() != null) {
            dbConclusionReferenceTables.getColumnModel().getColumn(0).setPreferredWidth(250);
        }
        //dbConclusionReferenceTables.getColumnModel().getColumn(1).setPreferredWidth(250);
        jScrollPane4.setViewportView(dbConclusionReferenceTables);

        jLabel2.setText("Available tables:");

        jLabel9.setText("Database reference builder");

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Join conditions:");

        joinFields.setEditable(false);
        joinFields.setToolTipText("");
        joinFields.setEnabled(false);

        chooseJoinFieldsButton.setText("Choose join fields");
        chooseJoinFieldsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseJoinFieldsButtonActionPerformed(evt);
            }
        });

        joinClearButton.setText("clear entry");
        joinClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinClearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(joinFields))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(chooseJoinFieldsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 284, Short.MAX_VALUE)
                        .addComponent(joinClearButton)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(joinFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseJoinFieldsButton)
                    .addComponent(joinClearButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel20.setText("Criteria:");

        criteriaField.setEditable(false);

        chooseCriterionButton.setText("Choose criterion value");
        chooseCriterionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseCriterionButtonActionPerformed(evt);
            }
        });

        criterionClearButton.setText("clear entry");
        criterionClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                criterionClearButtonActionPerformed(evt);
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
                        .addComponent(jLabel20)
                        .addGap(18, 18, 18)
                        .addComponent(criteriaField))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(chooseCriterionButton)
                        .addGap(263, 263, 263)
                        .addComponent(criterionClearButton)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(criteriaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseCriterionButton)
                    .addComponent(criterionClearButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        builderCancelButton.setText("Cancel");
        builderCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                builderCancelButtonActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Selected fields:");

        selectedFields.setEditable(false);
        selectedFields.setEnabled(false);

        chooseSelectedFieldsButton.setText("Choose selected fields");
        chooseSelectedFieldsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseSelectedFieldsButtonActionPerformed(evt);
            }
        });

        selectClearButton.setText("clear entry");
        selectClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectClearButtonActionPerformed(evt);
            }
        });

        countCheckBox.setText("COUNT");

        chooseOrderByButton.setText("Choose order by");
        chooseOrderByButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseOrderByButtonActionPerformed(evt);
            }
        });

        jLabel23.setText("Context variable");

        insertContextVariableSelectorButton.setText("Insert context variable");
        insertContextVariableSelectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertContextVariableSelectorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                        .addComponent(selectedFields, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(chooseSelectedFieldsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(countCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(chooseOrderByButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(selectClearButton))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(contextVariableComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(insertContextVariableSelectorButton)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(selectedFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chooseSelectedFieldsButton)
                    .addComponent(selectClearButton)
                    .addComponent(countCheckBox)
                    .addComponent(chooseOrderByButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextVariableComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(insertContextVariableSelectorButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        databaseReferenceBuilderPreviewButton.setText("Preview Query");
        databaseReferenceBuilderPreviewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseReferenceBuilderPreviewButtonActionPerformed(evt);
            }
        });

        loadSaveQueryButton.setText("Load/Save query snippet");
        loadSaveQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSaveQueryButtonActionPerformed(evt);
            }
        });

        jLabel21.setText("Description");

        showExistingQueries.setText("Show Existing Conclusion Queries");
        showExistingQueries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showExistingQueriesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(databaseReferenceBuilderPreviewButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(loadSaveQueryButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(showExistingQueries))
                                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(builderCancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conclusionQueryDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(databaseReferenceBuilderPreviewButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(loadSaveQueryButton))
                    .addComponent(showExistingQueries, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(conclusionQueryDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(builderCancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        String contextString = "";
        String criterionValue = "";
        boolean okToGo = true;

        if (criteriaField.getText().equals("")) {
            JOptionPane.showMessageDialog(this,"You need to specify a criterion field to limit results!");
            okToGo = false;
        }
        
        if (selectedFields.getText().equals("")) {
            JOptionPane.showMessageDialog(this,"You need to specify field(s) to use in the query!");
            okToGo = false;
        }
        
        if (criteriaField.getText().equals("")) {
            okToGo = false;
            JOptionPane.showMessageDialog(this,"You need to specify some criteria to limit the results from the query!");

        }
        
        if (conclusionQueryDescription.getText().equals("")) {
            okToGo = false;
            JOptionPane.showMessageDialog(this,"You need to specify a description for the query before it is saved..!");

        }
            
        
        if (okToGo) { 
            
            
            // insertNewConclusionQuery adds database tag to query along with embedded id number, so no need to include it here next..
            String query = selectedFields.getText() + joinFields.getText() + criteriaField.getText();

            int id = DBOperation.insertNewConclusionQuery(conclusionQueryDescription.getText(), query);
            query = OutputParser.getTag(id + selectedFields.getText() + joinFields.getText() + criteriaField.getText(),OutputParser.DB_DATABASE_TYPE);
            String descriptionTag = OutputParser.getTag(conclusionQueryDescription.getText(), OutputParser.DB_DESCRIPTION_TYPE);
            String queryPlaceholder = OutputParser.getTag(Integer.toString(id) + descriptionTag, OutputParser.DB_PLACEHOLDER_TYPE);

            ConclusionQuery theConclusionQuery = new ConclusionQuery();
            theConclusionQuery.setId(id);
            theConclusionQuery.setDescription(conclusionQueryDescription.getText());
            theConclusionQuery.setQuery(query);
            DialogMain.conclusionQueryList.put(id, theConclusionQuery);
            

            //Logger.info("DabaseQuery builder: adding query: " + query);
            // current old one - ((AddRuleGUI) theParentCallback).addToConclusionFieldText(OutputParser.getDBMarkerString(OutputParser.DB_DATABASE_TYPE) + selectedFields.getText() + joinFields.getText() + criterionField.getText());
            ((AddRuleGUI) theParentCallback).addToConclusionFieldText(queryPlaceholder,query);
            //conclusionDialogStrField.setText(conclusionDialogStrField.getText() + ICSAdminGUI.DATABASE_MARKER + selectedFields.getText() + joinFields.getText() + criterionField.getText() + contextString + criterionValue);
            this.dispose();
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void chooseJoinFieldsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseJoinFieldsButtonActionPerformed
        joinTableLeft.setText("");
        joinTableRight.setText("");
        joinDialog.setVisible(true);
        
        /*
        String selectedTableName = "Nothing Selected";
        selectFormatButton.setEnabled(false);
        if (dbConclusionReferenceTables.getSelectedRowCount() != 0) {
            selectedTableName = dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0).toString();
            setSelectedTableRefId(dbConclusionReferenceTables.getSelectedRow());
            setTableViewerQuery("select column_name from information_schema.columns where table_schema = '" + ICSAdminGUI.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'");
            tableViewerTextFieldTarget = joinFields;
            currentMarkerType = OutputParser.DB_JOIN_TYPE;
            tableViewer.setModel(getTableModel(getTableViewerQuery()));
            tableViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableViewerType = JOINTYPE;
            tableName.setText(selectedTableName);
            tableViewerDialog.setTitle("Select table fields to include in conclusion..");
            tableViewerDialog.setVisible(true);
        }
        */
    }//GEN-LAST:event_chooseJoinFieldsButtonActionPerformed

    private void joinClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinClearButtonActionPerformed
        joinFields.setText("");
    }//GEN-LAST:event_joinClearButtonActionPerformed

    private void chooseCriterionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseCriterionButtonActionPerformed
        String selectedTableName = "Nothing Selected";
        if (dbConclusionReferenceTables.getSelectedRowCount() != 0) {
            selectedTableName = dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0).toString();
            setSelectedTableRefId(dbConclusionReferenceTables.getSelectedRow());
            setTableViewerQuery("select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'");
            tableViewerTextFieldTarget = criterionField;
            currentMarkerType = OutputParser.DB_CRITERION_TYPE;
            tableViewer.setModel(getTableModel(getTableViewerQuery()));
            tableViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableViewerType = CRITERIONTYPE;
            tableName.setText(selectedTableName);
            tableViewerDialog.setTitle("Select table field to limit result (criterion)");
            selectFormatButton.setEnabled(false);
            tableViewerDialog.setVisible(true);
        }
        else {
            JOptionPane.showMessageDialog(this, "You need to select a table first..");
        }
        
        
        
    }//GEN-LAST:event_chooseCriterionButtonActionPerformed

    private void criterionClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_criterionClearButtonActionPerformed
        criteriaField.setText("");
    }//GEN-LAST:event_criterionClearButtonActionPerformed

    private void builderCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_builderCancelButtonActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_builderCancelButtonActionPerformed

    private void chooseSelectedFieldsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseSelectedFieldsButtonActionPerformed
        String selectedTableName = "Nothing Selected";
        selectFormatButton.setEnabled(true);
        if (dbConclusionReferenceTables.getSelectedRowCount() != 0) {
            selectedTableName = dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0).toString();
            setSelectedTableRefId(dbConclusionReferenceTables.getSelectedRow());
            setTableViewerQuery("select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'");
            tableViewerTextFieldTarget = selectedFields;
            currentMarkerType = OutputParser.DB_TABLE_TYPE;
            tableViewer.setModel(getTableModel(getTableViewerQuery()));
            tableViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            tableViewerType = SELECTTYPE;
            tableName.setText(selectedTableName);
            tableViewerDialog.setTitle("Select table fields to include in conclusion..");
            tableViewerDialog.setVisible(true);
        }
    }//GEN-LAST:event_chooseSelectedFieldsButtonActionPerformed

    private void selectClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectClearButtonActionPerformed
        selectedFields.setText("");
    }//GEN-LAST:event_selectClearButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        selectButtonsAction(evt,"","","","");
    }//GEN-LAST:event_selectButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        tableViewerDialog.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void selectFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFormatButtonActionPerformed
        int[] rows = tableViewer.getSelectedRows();
        String fieldString = (String) tableViewer.getValueAt(rows[0], 0);
        String theTable = (String) dbConclusionReferenceTables.getValueAt(getselectedTableRefId(), 0);
        if (rows.length > 1) {
            JOptionPane.showMessageDialog(this, "You can only specify the format for one field at time");
        }
        else if (rows.length == 0) {  
            JOptionPane.showMessageDialog(this, "You must specify a field to format");

        }
        else {
            currentMarkerType = OutputParser.DB_FORMAT_TYPE;
            String fieldTag = OutputParser.getTag(fieldString,OutputParser.DB_FIELD_TYPE);
            //String fieldTag = OutputParser.getTag(Integer.toString(rows[0]),OutputParser.DB_FIELD_TYPE);
            formatField.setText(fieldTag);
            formatTable.setText(theTable);
            //formatTable.setText(Integer.toString(getselectedTableRefId()));
            prefixText.setText("");
            postfixText.setText("");
            formatDialog.setVisible(true);
        }

    }//GEN-LAST:event_selectFormatButtonActionPerformed

    private void formatCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatCancelButtonActionPerformed
        formatDialog.setVisible(false);
    }//GEN-LAST:event_formatCancelButtonActionPerformed

    private void formatOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatOKButtonActionPerformed
        formatDialog.setVisible(false);
        selectButtonsAction(evt,formatTable.getText(),formatField.getText(),prefixText.getText(),postfixText.getText());
    }//GEN-LAST:event_formatOKButtonActionPerformed

    private void chooseOrderByButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseOrderByButtonActionPerformed
        String selectedTableName = "Nothing Selected";
        selectFormatButton.setEnabled(false);
        if (dbConclusionReferenceTables.getSelectedRowCount() != 0) {
            selectedTableName = dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0).toString();
            setSelectedTableRefId(dbConclusionReferenceTables.getSelectedRow());
            setTableViewerQuery("select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'");
            tableViewerTextFieldTarget = selectedFields;
            currentMarkerType = OutputParser.DB_ORDER_TYPE;
            tableViewer.setModel(getTableModel(getTableViewerQuery()));
            tableViewer.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            tableViewerType = ORDERTYPE;
            tableName.setText(selectedTableName);
            tableViewerDialog.setTitle("Select order by field to include in conclusion..");
            tableViewerDialog.setVisible(true);
        }
    }//GEN-LAST:event_chooseOrderByButtonActionPerformed

    private void contextValueNumberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contextValueNumberRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_contextValueNumberRadioButtonActionPerformed

    private void criterionInsertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_criterionInsertButtonActionPerformed
        //if (criteriaField.getText().equals("")) {
           //JOptionPane.showMessageDialog(this,"You need to specify a criterion field before setting its value!");
       // }
        String fieldTag = criterionField.getText();
        String table = criterionTable.getText();
        String criterionValue = "";
        String criterionTag;
        String containsTag = "";
        boolean okToGo = true;
        if (partialMatchCheckbox.isSelected()) {
            containsTag = OutputParser.getTag("Y",OutputParser.DB_PARTIALMATCH_TYPE);
            partialMatchCheckbox.setSelected(false);
            
        }
        
        if (fixedValueRadioButton.isSelected()) {
            if (!criterionFixedValue.getText().equals(""))
                criterionValue = OutputParser.getTag(criterionFixedValue.getText() + containsTag, OutputParser.DB_CRITERION_VALUE_FIXED_TYPE);
            else {
                JOptionPane.showMessageDialog(this,"No fixed value for criterion has been specified..");
                okToGo = false;
            }
        }
        else if (contextValueRadioButton.isSelected()) {  // use context variable to get value
            criterionValue = OutputParser.getTag(contextVariableComboBox.getSelectedItem().toString() + containsTag, OutputParser.DB_CRITERION_VALUE_CONTEXT_TYPE);
        }
        else if (contextValueNumberRadioButton.isSelected()) { // use context variable's numeric value e.g. item12 is replaced by 12
            criterionValue = OutputParser.getTag(contextVariableComboBox.getSelectedItem().toString() + containsTag, OutputParser.DB_CRITERION_VALUE_NUMBER_TYPE);
        }
        else if (fixedAndContextRadioButton.isSelected()) { // use combination of fixed value and context variable
            String contextTag = OutputParser.getTag(contextVariableComboBox.getSelectedItem().toString(), OutputParser.DB_CRITERION_VALUE_CONTEXT_TYPE);
            criterionValue = OutputParser.getTag(criterionFixedValue.getText()+contextTag + containsTag, OutputParser.DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE);
        }
        else if (fixedAndNumericRadioButton.isSelected()) { // use combination of fixed value and context variable's numeric only value
            String contextTag = OutputParser.getTag(contextVariableComboBox.getSelectedItem().toString(), OutputParser.DB_CRITERION_VALUE_NUMBER_TYPE);
            criterionValue = OutputParser.getTag(criterionFixedValue.getText()+contextTag + containsTag, OutputParser.DB_CRITERION_VALUE_FIXED_NUMBER_TYPE);
        }
        
        
        if (okToGo) {
            criterionTag = OutputParser.getTag(table + fieldTag + criterionValue, OutputParser.DB_CRITERION_TYPE);
            criteriaField.setText(criteriaField.getText() + criterionTag);
            criterionDialog.setVisible(false);
        }
    }//GEN-LAST:event_criterionInsertButtonActionPerformed

    private void criterionCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_criterionCancelButtonActionPerformed
        criterionDialog.setVisible(false);
    }//GEN-LAST:event_criterionCancelButtonActionPerformed

    private void joinTableRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinTableRightActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_joinTableRightActionPerformed

    private void joinConditionsCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinConditionsCancelButtonActionPerformed
        joinDialog.setVisible(false);
    }//GEN-LAST:event_joinConditionsCancelButtonActionPerformed

    private void selectLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectLeftButtonActionPerformed
        String selectedTableName = "Nothing Selected";
        selectFormatButton.setEnabled(false);
        if (dbConclusionReferenceTables.getSelectedRowCount() != 0) {
            selectedTableName = dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0).toString();
            setSelectedTableRefId(dbConclusionReferenceTables.getSelectedRow());
            setTableViewerQuery("select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'");
            tableViewerTextFieldTarget = joinTableLeft;
            currentMarkerType = OutputParser.DB_JVALUE_TYPE;
            tableViewer.setModel(getTableModel(getTableViewerQuery()));
            tableViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableViewerType = JOINTYPE;
            tableName.setText(selectedTableName);
            tableViewerDialog.setTitle("Select table fields to include in conclusion..");
            tableViewerDialog.setVisible(true);
        }
    }//GEN-LAST:event_selectLeftButtonActionPerformed

    private void selectRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRightButtonActionPerformed
        String selectedTableName = "Nothing Selected";
        selectFormatButton.setEnabled(false);
        if (dbConclusionReferenceTables.getSelectedRowCount() != 0) {
            selectedTableName = dbConclusionReferenceTables.getValueAt(dbConclusionReferenceTables.getSelectedRow(), 0).toString();
            setSelectedTableRefId(dbConclusionReferenceTables.getSelectedRow());
            setTableViewerQuery("select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selectedTableName + "'");
            tableViewerTextFieldTarget = joinTableRight;
            currentMarkerType = OutputParser.DB_JVALUE_TYPE;
            tableViewer.setModel(getTableModel(getTableViewerQuery()));
            tableViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableViewerType = JOINTYPE;
            tableName.setText(selectedTableName);
            tableViewerDialog.setTitle("Select table fields to include in conclusion..");
            tableViewerDialog.setVisible(true);
        }// TODO add your handling code here:
    }//GEN-LAST:event_selectRightButtonActionPerformed

    private void joinConditionsOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinConditionsOKButtonActionPerformed
        String joinTag;
        String leftJoinTag;
        String rightJoinTag;
        
        leftJoinTag = joinTableLeft.getText();
        rightJoinTag = joinTableRight.getText();
        if (leftJoinTag.equals("") || leftJoinTag.equals("")) {
            JOptionPane.showMessageDialog(null, "Both left and right join fields must have values..");
        }
        else {
            joinTag = OutputParser.getTag(leftJoinTag + rightJoinTag, OutputParser.DB_JOIN_TYPE);
            joinFields.setText(joinFields.getText() + joinTag);
            joinDialog.setVisible(false);
        }

    }//GEN-LAST:event_joinConditionsOKButtonActionPerformed

    private void saveQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveQueryButtonActionPerformed
        if(!newQueryName.getText().equals("")) {
            SavedQueryTemplate aSavedQuery = new SavedQueryTemplate();
            aSavedQuery.setDescription(newQueryName.getText());
            aSavedQuery.setSelect(selectedFields.getText());
            aSavedQuery.setjoin(joinFields.getText());
            aSavedQuery.setCriteria(criteriaField.getText());
            int id = DBOperation.insertNewQueryTemplateDetails(aSavedQuery);
            if (id != -1) {
                aSavedQuery.setId(id);
                DialogMain.savedQueryTemplateList.put(id, aSavedQuery);
                JOptionPane.showMessageDialog(null, "Query successfully saved..");
                DefaultListModel theModel = (DefaultListModel)savedQueryItemsJList.getModel();
                theModel.addElement(aSavedQuery.getId() + " " + aSavedQuery.getDescription());
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "You must specify a description for the query..");

        }
    }//GEN-LAST:event_saveQueryButtonActionPerformed

    private void databaseReferenceBuilderPreviewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseReferenceBuilderPreviewButtonActionPerformed
        previewTextArea.setText("");
        
        Logger.info("here1");
        
        if (! selectedFields.getText().isEmpty()) {
            String queryToPreview = OutputParser.getTag(selectedFields.getText() + joinFields.getText() + criteriaField.getText(),OutputParser.DB_DATABASE_TYPE);
        Logger.info("here2");

           // previewTextArea.setText(OutputParser.parseOtherTerms(OutputParser.replaceAllDatabaseTerms(queryToPreview)));
            previewTextArea.setText(OutputParser.parseOtherTerms(OutputParser.replaceSingleDatabaseQuery("",queryToPreview,true),true));
            if (DBOperation.wasDatabaseError()) {
                previewTextArea.append("\n" + "Error: " + DBOperation.getLastDatabaseError());
                DBOperation.resetDatabaseError();
            }

            previewConclusionDialog.setVisible(true);
        }
        else {
            JOptionPane.showMessageDialog(null, "You must select from fields to retrieve before attempting to preview..");
        }
    }//GEN-LAST:event_databaseReferenceBuilderPreviewButtonActionPerformed

    private void previewConclusionCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewConclusionCloseButtonActionPerformed
        previewConclusionDialog.setVisible(false);
    }//GEN-LAST:event_previewConclusionCloseButtonActionPerformed

    private void loadSaveQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSaveQueryButtonActionPerformed
        DefaultListModel theModel = new DefaultListModel();
        Iterator iter = DialogMain.savedQueryTemplateList.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            SavedQueryTemplate aSavedQuery = (SavedQueryTemplate)me.getValue();
            theModel.addElement(aSavedQuery.getId() + " " + aSavedQuery.getDescription());
        }

        savedQueryItemsJList.setModel(theModel);
        savedQueryItemsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadSaveQueryTemplateDialog.setVisible(true);
    }//GEN-LAST:event_loadSaveQueryButtonActionPerformed

    private void saveQueryCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveQueryCloseButtonActionPerformed
        loadSaveQueryTemplateDialog.setVisible(false);
    }//GEN-LAST:event_saveQueryCloseButtonActionPerformed

    private void loadQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadQueryButtonActionPerformed
        if (savedQueryItemsJList.getSelectedIndex() >= 0) {
            int queryId = Integer.parseInt(savedQueryItemsJList.getSelectedValue().split(" ")[0]);
            SavedQueryTemplate theQuery = DialogMain.savedQueryTemplateList.get(queryId);
            selectedFields.setText(selectedFields.getText() + theQuery.getSelect());
            joinFields.setText(joinFields.getText() + theQuery.getJoin());
            criteriaField.setText(criteriaField.getText() + theQuery.getCriteria());
        }
        else {
            JOptionPane.showMessageDialog(null, "You must select a query to load..");

        }
    }//GEN-LAST:event_loadQueryButtonActionPerformed

    private void deleteQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteQueryButtonActionPerformed
        if (savedQueryItemsJList.getSelectedIndex() >= 0) {
            int queryId = Integer.parseInt(savedQueryItemsJList.getSelectedValue().split(" ")[0]);
            DialogMain.savedQueryTemplateList.remove(queryId);
            DBOperation.deleteSavedQueryTemplate(queryId);
            DefaultListModel theModel = (DefaultListModel)savedQueryItemsJList.getModel();
            theModel.remove(savedQueryItemsJList.getSelectedIndex());
        }
        else {
            JOptionPane.showMessageDialog(null, "You must select a query to load..");

        }
    }//GEN-LAST:event_deleteQueryButtonActionPerformed

    private void fixedAndContextRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixedAndContextRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fixedAndContextRadioButtonActionPerformed

    private void contextVariableComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contextVariableComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_contextVariableComboBoxActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    private void showExistingQueriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showExistingQueriesActionPerformed
        String [] columns = {"ID","Description","Query"};
        ArrayList<ArrayList<String>> conclusionQueriesArrayList = new ArrayList<>();
        DefaultTableModel theModel = new DefaultTableModel(columns,0);
        conclusionQueryTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        conclusionQueryTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        conclusionQueryTable.getColumnModel().getColumn(2).setPreferredWidth(400);

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
    }//GEN-LAST:event_showExistingQueriesActionPerformed

    private void existingConclusionQueriesCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingConclusionQueriesCloseButtonActionPerformed
        existingConclusionQueries.setVisible(false);
    }//GEN-LAST:event_existingConclusionQueriesCloseButtonActionPerformed

    private void insertContextVariableSelectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertContextVariableSelectorButtonActionPerformed
        if (contextVariableComboBox1.getSelectedIndex() >= 0) {
            selectedFields.setText(selectedFields.getText() + OutputParser.getTag((String)contextVariableComboBox1.getSelectedItem(),OutputParser.DB_SELECTOR_TYPE));
        }
    }//GEN-LAST:event_insertContextVariableSelectorButtonActionPerformed

    private void selectButtonsAction(java.awt.event.ActionEvent evt, String formatTableName, String formatVariableName, String formatPrefix, String formatPostfix) {
        int[] rows = tableViewer.getSelectedRows();
        String fieldTag;
        String selectedTable = (String) dbConclusionReferenceTables.getValueAt(getselectedTableRefId(),0);
        
        //if (!formatVariableName.equals("")) {
            //Logger.info("Would format " + formatVariableName + " - value becomes: " + formatPrefix + formatVariableName + formatPostfix);
        //}

        switch (tableViewerType) {
            case JOINTYPE:
                // JOIN FIELDS
                // we should only have one row selected as the selection mode was set to single selection..
                //Logger.info("In join code..");
                int theRow = rows[0];
                String theField = (String)tableViewer.getValueAt(theRow, 0);
                fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                //fieldTag = OutputParser.getTag(Integer.toString(theRow), OutputParser.DB_FIELD_TYPE);
                String joinValueTag;
                
                if (multivaluedCheckBox.isSelected() && tableViewerTextFieldTarget == joinTableRight) {
                    //Logger.info("This value is multivalued!");
                    joinValueTag = OutputParser.getTag(selectedTable + fieldTag,OutputParser.DB_MULTIV_JOIN_TYPE);
                    //joinValueTag = OutputParser.getTag(Integer.toString(getselectedTableRefId()) + fieldTag,OutputParser.DB_MULTIV_JOIN_TYPE);
                }
                else {
                    joinValueTag = OutputParser.getTag(selectedTable + fieldTag,OutputParser.DB_JVALUE_TYPE); 
                    //joinValueTag = OutputParser.getTag(Integer.toString(getselectedTableRefId()) + fieldTag,OutputParser.DB_JVALUE_TYPE); 
                }
                
                tableViewerTextFieldTarget.setText(joinValueTag);
                    
    
                break;
            case SELECTTYPE:
                // SELECTION FIELDS
                String tableTag;
                
                if (countCheckBox.isSelected()) {
                    if (tableViewerTextFieldTarget.getText().equals("")) {
                        if (rows.length == 1) {
                            currentMarkerType = OutputParser.DB_COUNT_TYPE;
                            //fieldTag = OutputParser.getTag(Integer.toString(rows[0]), OutputParser.DB_FIELD_TYPE);
                            //tableTag = OutputParser.getTag(Integer.toString(getselectedTableRefId()) + fieldTag, OutputParser.DB_COUNT_TYPE);
                            theField = (String)tableViewer.getValueAt(rows[0], 0);
                            fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                            tableTag = OutputParser.getTag(selectedTable + fieldTag, OutputParser.DB_COUNT_TYPE);
                            tableViewerTextFieldTarget.setText(tableTag);
                        }
                        else {
                            JOptionPane.showMessageDialog(this,"Only one field can be selected if using COUNT option..");                
                        }                      
                    }
                    else {
                        JOptionPane.showMessageDialog(this,"Only one field can be selected if using COUNT option..");
                    }
                }
                else if(!formatVariableName.equals("")) {
                    String prefixTag = OutputParser.getTag(prefixText.getText(), OutputParser.DB_PREFIX_TYPE);
                    String postfixTag = OutputParser.getTag(postfixText.getText(), OutputParser.DB_POSTFIX_TYPE);
                    String tableValue = formatTable.getText() + formatField.getText() + prefixTag + postfixTag;
                    String formatTag = OutputParser.getTag(tableValue, OutputParser.DB_FORMAT_TYPE);
                    tableViewerTextFieldTarget.setText(tableViewerTextFieldTarget.getText() + formatTag);
 
                }
                else {
                    // can't add another selection field if we're already doing an aggregate count
                    if (tableViewerTextFieldTarget.getText().contains(OutputParser.getStartTag(OutputParser.DB_COUNT_TYPE))) {
                        JOptionPane.showMessageDialog(this,"Only one field can be selected if using COUNT option..");
                    }
                    else {
                        for (int aRow: rows) {
                            theField = (String)tableViewer.getValueAt(aRow, 0);
                            fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                            tableTag = OutputParser.getTag(dbConclusionReferenceTables.getValueAt(getselectedTableRefId(),0) + fieldTag, OutputParser.DB_TABLE_TYPE);
                            //fieldTag = OutputParser.getTag(Integer.toString(aRow), OutputParser.DB_FIELD_TYPE);
                            //tableTag = OutputParser.getTag(Integer.toString(getselectedTableRefId()) + fieldTag, OutputParser.DB_TABLE_TYPE);
                            tableViewerTextFieldTarget.setText(tableViewerTextFieldTarget.getText() + tableTag);
                        }
                    }
                }   break;
            case CRITERIONTYPE:
                for (int aRow: rows) {
                    theField = (String)tableViewer.getValueAt(aRow, 0);
                    fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE); 
                    //fieldTag = OutputParser.getTag(Integer.toString(aRow), OutputParser.DB_FIELD_TYPE); 
                    criterionField.setText(fieldTag);
                    //String table = Integer.toString(getselectedTableRefId());
                    criterionFixedValue.setText("");
                    //criterionTable.setText(table);
                    criterionTable.setText(selectedTable);
                    partialMatchCheckbox.setSelected(true);
                    criterionDialog.setVisible(true);
                    //tableViewerTextFieldTarget.setText(tableViewerTextFieldTarget.getText() + OutputParser.wrapVariableInMarker(Integer.toString(getselectedTableRefId()),currentMarkerType) + OutputParser.wrapVariableInMarker(Integer.toString(aRow),OutputParser.DB_FIELD_TYPE));
                }   break;
            case ORDERTYPE:
                // ORDER BY FIELD
                theField =  (String)tableViewer.getValueAt(rows[0], 0);
                fieldTag = OutputParser.getTag(theField, OutputParser.DB_FIELD_TYPE);
                tableTag = OutputParser.getTag(selectedTable + fieldTag, OutputParser.DB_ORDER_TYPE);
                //fieldTag = OutputParser.getTag(Integer.toString(rows[0]), OutputParser.DB_FIELD_TYPE);
                //tableTag = OutputParser.getTag(getselectedTableRefId() + fieldTag, OutputParser.DB_ORDER_TYPE);
                tableViewerTextFieldTarget.setText(tableViewerTextFieldTarget.getText() + tableTag);
                break;
            default:
                break;
        }
        tableViewerDialog.dispose();
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
            java.util.logging.Logger.getLogger(DatabaseQueryBuilderGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DatabaseQueryBuilderGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DatabaseQueryBuilderGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DatabaseQueryBuilderGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
       /* java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DatabaseQueryBuilderGUI().setVisible(true);
            }
        });
*/
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton builderCancelButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton chooseCriterionButton;
    private javax.swing.JButton chooseJoinFieldsButton;
    private javax.swing.JButton chooseOrderByButton;
    private javax.swing.JButton chooseSelectedFieldsButton;
    private javax.swing.JTextField conclusionQueryDescription;
    private javax.swing.JTable conclusionQueryTable;
    private javax.swing.JRadioButton contextValueNumberRadioButton;
    private javax.swing.JRadioButton contextValueRadioButton;
    private javax.swing.JComboBox<String> contextVariableComboBox;
    private javax.swing.JComboBox<String> contextVariableComboBox1;
    private javax.swing.JCheckBox countCheckBox;
    private javax.swing.JTextField criteriaField;
    private javax.swing.ButtonGroup criterionButtonGroup;
    private javax.swing.JButton criterionCancelButton;
    private javax.swing.JButton criterionClearButton;
    private javax.swing.JDialog criterionDialog;
    private javax.swing.JTextField criterionField;
    private javax.swing.JTextField criterionFixedValue;
    private javax.swing.JButton criterionInsertButton;
    private javax.swing.JTextField criterionTable;
    private javax.swing.JButton databaseReferenceBuilderPreviewButton;
    private javax.swing.JTable dbConclusionReferenceTables;
    private javax.swing.JButton deleteQueryButton;
    private javax.swing.JDialog existingConclusionQueries;
    private javax.swing.JButton existingConclusionQueriesCloseButton;
    private javax.swing.JRadioButton fixedAndContextRadioButton;
    private javax.swing.JRadioButton fixedAndNumericRadioButton;
    private javax.swing.JRadioButton fixedValueRadioButton;
    private javax.swing.JButton formatCancelButton;
    private javax.swing.JDialog formatDialog;
    private javax.swing.JTextField formatField;
    private javax.swing.JButton formatOKButton;
    private javax.swing.JTextField formatTable;
    private javax.swing.JButton insertContextVariableSelectorButton;
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
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton joinClearButton;
    private javax.swing.JButton joinConditionsCancelButton;
    private javax.swing.JButton joinConditionsOKButton;
    private javax.swing.JDialog joinDialog;
    private javax.swing.JTextField joinFields;
    private javax.swing.JTextField joinTableLeft;
    private javax.swing.JTextField joinTableRight;
    private javax.swing.JButton loadQueryButton;
    private javax.swing.JButton loadSaveQueryButton;
    private javax.swing.JDialog loadSaveQueryTemplateDialog;
    private javax.swing.JCheckBox multivaluedCheckBox;
    private javax.swing.JTextField newQueryName;
    private javax.swing.JCheckBox partialMatchCheckbox;
    private javax.swing.JTextField postfixText;
    private javax.swing.JTextField prefixText;
    private javax.swing.JButton previewConclusionCloseButton;
    private javax.swing.JDialog previewConclusionDialog;
    private javax.swing.JTextArea previewTextArea;
    private javax.swing.JButton saveQueryButton;
    private javax.swing.JButton saveQueryCloseButton;
    private javax.swing.JList<String> savedQueryItemsJList;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton selectClearButton;
    private javax.swing.JButton selectFormatButton;
    private javax.swing.JButton selectLeftButton;
    private javax.swing.JButton selectRightButton;
    private javax.swing.JTextField selectedFields;
    private javax.swing.JButton showExistingQueries;
    private javax.swing.JLabel tableName;
    private javax.swing.JTable tableViewer;
    private javax.swing.JDialog tableViewerDialog;
    private javax.swing.JMenu windowsMenu;
    // End of variables declaration//GEN-END:variables
}

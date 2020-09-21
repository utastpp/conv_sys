/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.gui;

import cmcrdr.cases.DialogCase;
import cmcrdr.dic.DicManager;
import cmcrdr.dic.DicTerm;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.mysql.DBOperation;
import cmcrdr.mysql.DBOperation;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import static javax.swing.JOptionPane.showMessageDialog;


/**
 * This class is used for defining DicManagerGUI 
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DicManagerGUI extends javax.swing.JFrame {   

    private static DialogCase selectedCase=null;
    private DicTerm tempDicTerm;
    
    /**
     * Creates new form DicMangerGUI
     */
    public DicManagerGUI() {
        initComponents();
        updateRepresentativeTermTable("");
        
        updateUserInputField();
        
    }

    private void updateUserInputField(){
        if(selectedCase!=null){
            String userRecentInput = selectedCase.getValue("Recent").toString();
            userInputTextField.setText(userRecentInput);
        } else {
        }
    }
    
    private void updateRepresentativeTermTable(String selected){
        
        
        String[][] stringArray = DialogMain.dictionary.toRepresentativeTermStringArrayForGUI();
        String[] columnName = new String[1];
        columnName[0] = "Representative Term";
        
        TableModel newModel = new DefaultTableModel(
                stringArray, columnName
         );
        
        representativeTermTable.setModel(newModel);
        
        // leave the last item selected when updating the GUI
        if (!selected.equals("")) {
            for (int row = 0; row < representativeTermTable.getModel().getRowCount(); row++) {
                if (representativeTermTable.getValueAt(row, 0).equals(selected)) {
                    representativeTermTable.setRowSelectionInterval(row, row);
                    break;
                }
            }          
        }
    }
    
    private void updateReferenceDatabaseSynonymList(String selected){
        if (selected!=null) {
            DefaultListModel model = (DefaultListModel) referenceDatabaseSynonymFieldList.getModel();
            model.removeAllElements();
            ArrayList<String> selectedTableFieldList = DBOperation.selectQueryAsStringList("select column_name from information_schema.columns where table_schema = '" + DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' and table_name = '" + selected + "'");
            for (String item: selectedTableFieldList) {
                model.addElement(item);
            }
        }
    }
    
    private void updateMatchingTermTable(String representativeTerm){
        
        DicTerm aDicTerm = DialogMain.dictionary.getDicTermByRepresentative(representativeTerm);
        
        
        
        String[][] stringArray = aDicTerm.toMatchingTermStringArrayForGUI();
        String[] columnName = new String[1];
        columnName[0] = "Synonyms";
        
        TableModel newModel = new DefaultTableModel(
                stringArray, columnName
         );
        
        matchingTermTable.setModel(newModel);
        
    }
    
    private void updateRepresentativeTermField(String representativeTerm){
        if(!representativeTerm.equals("")){
            representativeTermField.setText(representativeTerm);
        }
        
    }
    
    private void updateNewMatchingTermTable(DicTerm aDicTerm){
        if(aDicTerm != null){
            String[][] stringArray = aDicTerm.toMatchingTermStringArrayForGUI();
            String[] columnName = new String[1];
            columnName[0] = "Synonyms";

            TableModel newModel = new DefaultTableModel(
                    stringArray, columnName
             );

            newMatchingTermTable.setModel(newModel);
            matchingTermField.setText("");
        } else {
            String[][] stringArray = new String[][]{};
            String[] columnName = new String[1];
            columnName[0] = "Synonyms";

            TableModel newModel = new DefaultTableModel(
                    stringArray, columnName
             );

            newMatchingTermTable.setModel(newModel);
            matchingTermField.setText("");
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

        fetchSynonymsDialog = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        referenceDatabaseSynonymTable = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        referenceDatabaseSynonymFieldList = new javax.swing.JList<>();
        jLabel12 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        fetchSynonymsButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        fetchCriteria1 = new javax.swing.JTextField();
        setCriteriaFieldButton1 = new javax.swing.JButton();
        fetchCriteriaFieldTextField1 = new javax.swing.JTextField();
        setCriteriaFieldButton2 = new javax.swing.JButton();
        fetchCriteriaFieldTextField2 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        fetchCriteria2 = new javax.swing.JTextField();
        fetchSelectFieldButton = new javax.swing.JButton();
        fetchSelectField = new javax.swing.JTextField();
        importexportFileChooser = new JFileChooser() {
            @Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                        case JOptionPane.NO_OPTION:
                        return;
                        case JOptionPane.CLOSED_OPTION:
                        return;
                        case JOptionPane.CANCEL_OPTION:
                        cancelSelection();
                        return;
                    }
                }
                super.approveSelection();
            }
        };
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jLabel14 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        userInputTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        exportDictionaryButton = new javax.swing.JButton();
        importDictionaryButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        representativeTermTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        matchingTermTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        representativeTermField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        matchingTermField = new javax.swing.JTextField();
        addMatchingTermButton = new javax.swing.JButton();
        addTermButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        setRepresentativeTermButton = new javax.swing.JToggleButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        newMatchingTermTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        deleteTempMatchingTermButton = new javax.swing.JButton();
        fetchButton = new javax.swing.JButton();
        allowRandomSynonymCheckbox = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        windowsMenu = new javax.swing.JMenu();

        fetchSynonymsDialog.setMinimumSize(new java.awt.Dimension(475, 630));
        fetchSynonymsDialog.setModal(true);
        fetchSynonymsDialog.setResizable(false);

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        referenceDatabaseSynonymTable.setModel(DatabaseQueryBuilderGUI.getTableModel("select table_name from information_schema.tables where table_schema='" + cmcrdr.main.DialogMain.getReferenceDatabaseDetails().get("databaseDatabase") + "' order by table_name"));
        referenceDatabaseSynonymTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        referenceDatabaseSynonymTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                Logger.info("Sending: " + (String)referenceDatabaseSynonymTable.getValueAt(referenceDatabaseSynonymTable.getSelectedRow(), 0));
                updateReferenceDatabaseSynonymList((String)referenceDatabaseSynonymTable.getValueAt(referenceDatabaseSynonymTable.getSelectedRow(), 0));
            }
        });

        referenceDatabaseSynonymTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        referenceDatabaseSynonymTable.getTableHeader().setReorderingAllowed(false);
        if(referenceDatabaseSynonymTable.getColumnModel() != null) {
            if (referenceDatabaseSynonymTable.getColumnModel().getColumnCount() > 0)
            referenceDatabaseSynonymTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        }
        //dbConclusionReferenceTables.getColumnModel().getColumn(1).setPreferredWidth(250);
        jScrollPane2.setViewportView(referenceDatabaseSynonymTable);

        jScrollPane1.setViewportView(jScrollPane2);

        jLabel10.setText("Tables");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(0, 163, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel11.setText("Fetch synonyms from reference database");

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        referenceDatabaseSynonymFieldList.setModel(new DefaultListModel()
        );
        referenceDatabaseSynonymFieldList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(referenceDatabaseSynonymFieldList);

        jLabel12.setText("Fields");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        fetchSynonymsButton.setText("Fetch");
        fetchSynonymsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchSynonymsButtonActionPerformed(evt);
            }
        });

        jLabel13.setText("criteria text to match 1:");

        setCriteriaFieldButton1.setText("Set Criteria Field 1");
        setCriteriaFieldButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCriteriaFieldButton1ActionPerformed(evt);
            }
        });

        fetchCriteriaFieldTextField1.setEditable(false);

        setCriteriaFieldButton2.setText("Set Criteria Field 2");
        setCriteriaFieldButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCriteriaFieldButton2ActionPerformed(evt);
            }
        });

        fetchCriteriaFieldTextField2.setEditable(false);

        jLabel15.setText("criteria text to match 2:");

        fetchSelectFieldButton.setText("Set value to fetch");
        fetchSelectFieldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchSelectFieldButtonActionPerformed(evt);
            }
        });

        fetchSelectField.setEditable(false);

        javax.swing.GroupLayout fetchSynonymsDialogLayout = new javax.swing.GroupLayout(fetchSynonymsDialog.getContentPane());
        fetchSynonymsDialog.getContentPane().setLayout(fetchSynonymsDialogLayout);
        fetchSynonymsDialogLayout.setHorizontalGroup(
            fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fetchSynonymsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fetchSynonymsDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(fetchSynonymsDialogLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fetchSynonymsDialogLayout.createSequentialGroup()
                                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fetchSynonymsDialogLayout.createSequentialGroup()
                                        .addComponent(cancelButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fetchSynonymsButton))))
                            .addGroup(fetchSynonymsDialogLayout.createSequentialGroup()
                                .addComponent(setCriteriaFieldButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(fetchCriteriaFieldTextField1))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fetchSynonymsDialogLayout.createSequentialGroup()
                        .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(fetchSelectFieldButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fetchSelectField)
                            .addComponent(fetchCriteria2, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)))
                    .addGroup(fetchSynonymsDialogLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fetchCriteria1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(fetchSynonymsDialogLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(fetchSynonymsDialogLayout.createSequentialGroup()
                        .addComponent(setCriteriaFieldButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fetchCriteriaFieldTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        fetchSynonymsDialogLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel6, jPanel7});

        fetchSynonymsDialogLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {fetchCriteria1, fetchCriteria2, fetchCriteriaFieldTextField1, fetchCriteriaFieldTextField2});

        fetchSynonymsDialogLayout.setVerticalGroup(
            fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fetchSynonymsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setCriteriaFieldButton1)
                    .addComponent(fetchCriteriaFieldTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(fetchCriteria1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setCriteriaFieldButton2)
                    .addComponent(fetchCriteriaFieldTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(fetchCriteria2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fetchSelectFieldButton)
                    .addComponent(fetchSelectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(fetchSynonymsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(fetchSynonymsButton))
                .addContainerGap())
        );

        importexportFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        importexportFileChooser.setDialogTitle("Choose export location");

        jLabel14.setText("jLabel14");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Dictionary Manager");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
        jLabel5.setText("Recent Dialog");

        userInputTextField.setEditable(false);

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N
        jLabel9.setText("Representative terms are applied in the order they appear in the Registered Representative list below");

        exportDictionaryButton.setText("Export Dictionary");
        exportDictionaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDictionaryButtonActionPerformed(evt);
            }
        });

        importDictionaryButton.setText("Import Dictionary");
        importDictionaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDictionaryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userInputTextField))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportDictionaryButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importDictionaryButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(userInputTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(exportDictionaryButton)
                    .addComponent(importDictionaryButton))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        representativeTermTable.setAutoCreateRowSorter(true);
        representativeTermTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Representative Term"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        representativeTermTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                representativeTermTableMouseReleased(evt);
            }
        });
        representativeTermTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                representativeTermTableKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(representativeTermTable);

        jLabel1.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
        jLabel1.setText("Registered Representative");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 142, Short.MAX_VALUE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        matchingTermTable.setAutoCreateRowSorter(true);
        matchingTermTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Synonyms"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(matchingTermTable);

        jLabel2.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
        jLabel2.setText("Synonyms");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 308, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Synonyms");

        jLabel3.setText("Repre.");

        matchingTermField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                matchingTermFieldKeyPressed(evt);
            }
        });

        addMatchingTermButton.setText("Add");
        addMatchingTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMatchingTermButtonActionPerformed(evt);
            }
        });

        addTermButton.setText("Register");
        addTermButton.setEnabled(false);
        addTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTermButtonActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
        jLabel6.setText("Register New Term");

        setRepresentativeTermButton.setText("Set");
        setRepresentativeTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setRepresentativeTermButtonActionPerformed(evt);
            }
        });

        newMatchingTermTable.setAutoCreateRowSorter(true);
        newMatchingTermTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Synonyms"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(newMatchingTermTable);

        jLabel7.setFont(new java.awt.Font("굴림", 2, 12)); // NOI18N
        jLabel7.setText("Representative Term format: /*/ ");

        jLabel8.setFont(new java.awt.Font("굴림", 2, 12)); // NOI18N
        jLabel8.setText("i.e.) /Search/, /Open/, /Navigate/");

        deleteTempMatchingTermButton.setText("Delete Selected");
        deleteTempMatchingTermButton.setEnabled(false);
        deleteTempMatchingTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTempMatchingTermButtonActionPerformed(evt);
            }
        });

        fetchButton.setText("Fetch..");
        fetchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchButtonActionPerformed(evt);
            }
        });

        allowRandomSynonymCheckbox.setSelected(true);
        allowRandomSynonymCheckbox.setText("allow random synonym");
        allowRandomSynonymCheckbox.setToolTipText("Allow any of the synonyms for this term to be returned when giving a user honts for potential input.\nIf not selected, only the first synonym will be used as a hint.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(deleteTempMatchingTermButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addTermButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(matchingTermField)
                            .addComponent(representativeTermField))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMatchingTermButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(setRepresentativeTermButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(allowRandomSynonymCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fetchButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(representativeTermField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setRepresentativeTermButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(matchingTermField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMatchingTermButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(fetchButton)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(allowRandomSynonymCheckbox)
                        .addGap(4, 4, 4)))
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteTempMatchingTermButton)
                    .addComponent(addTermButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void representativeTermTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_representativeTermTableKeyReleased
if(representativeTermTable.getSelectedRow()>-1){
            int selectedRow = representativeTermTable.getSelectedRow();
            String selectedTerm = (String) representativeTermTable.getValueAt(selectedRow, 0);
            updateMatchingTermTable(selectedTerm);
            if(!setRepresentativeTermButton.isSelected()){
                updateRepresentativeTermField(selectedTerm);
            }
        }
        
        
    }//GEN-LAST:event_representativeTermTableKeyReleased

    private void representativeTermTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_representativeTermTableMouseReleased
        if(representativeTermTable.getSelectedRow()>-1){
            int selectedRow = representativeTermTable.getSelectedRow();
            String selectedTerm = (String) representativeTermTable.getValueAt(selectedRow, 0);
            updateMatchingTermTable(selectedTerm);
            if(!setRepresentativeTermButton.isSelected()){
                updateRepresentativeTermField(selectedTerm);
            }
        }
    }//GEN-LAST:event_representativeTermTableMouseReleased

    private void setRepresentativeTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setRepresentativeTermButtonActionPerformed
        if(setRepresentativeTermButton.isSelected()){      
            DicManager aDicManager = new DicManager();
            if(!representativeTermField.getText().equals("")){    
                if(aDicManager.isValidRepresentative(representativeTermField.getText())){
                    deleteTempMatchingTermButton.setEnabled(true);
                    addTermButton.setEnabled(true);
                    fetchButton.setEnabled(true);
                    if(DialogMain.dictionary.isDicTermExist(representativeTermField.getText())){
                        DicTerm aDicTerm = DialogMain.dictionary.getDicTermByRepresentative(representativeTermField.getText());
                        tempDicTerm = aDicTerm.cloneDicTerm();

                        updateNewMatchingTermTable(aDicTerm);
                        representativeTermField.setEnabled(false);
                        allowRandomSynonymCheckbox.setSelected(aDicTerm.getAllowRandomSynonym());
                    } else {
                        if(representativeTermField.getText().equals("/POI/")){
                            // alert message that the /POI/ cannot be added here
                            showMessageDialog(null, "Please use POI Manager.");

                        } else {
                            //add new representative
                            aDicManager.setCurrentDic(DialogMain.dictionary);

                            DicTerm aDicTerm = aDicManager.addRepresentativeTermUsingString(representativeTermField.getText(),allowRandomSynonymCheckbox.isSelected());

                            updateRepresentativeTermTable("");

                            tempDicTerm = aDicTerm.cloneDicTerm();

                            updateNewMatchingTermTable(aDicTerm);
                            representativeTermField.setEnabled(false);
                        }
                    }
                } else {
                    showMessageDialog(null, "Please follow the appropriate format: /Representative/");
                    
                    setRepresentativeTermButton.setSelected(false);
                    deleteTempMatchingTermButton.setEnabled(false);
                    addTermButton.setEnabled(false);
                    fetchButton.setEnabled(false);
                }
            } else {
                showMessageDialog(null, "Please set representative term.");

                setRepresentativeTermButton.setSelected(false);
                deleteTempMatchingTermButton.setEnabled(false);
                addTermButton.setEnabled(false);
                fetchButton.setEnabled(false);
            }
        } else {           
            deleteTempMatchingTermButton.setEnabled(false);
            addTermButton.setEnabled(false);
            fetchButton.setEnabled(false);
            updateNewMatchingTermTable(null);
            representativeTermField.setEnabled(true);
        }
    }//GEN-LAST:event_setRepresentativeTermButtonActionPerformed

    
    private void addMatchingTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMatchingTermButtonActionPerformed
        if (setRepresentativeTermButton.isSelected()) {
            if(!matchingTermField.getText().equals("")){
                String newMatchingTerm = matchingTermField.getText().toLowerCase();
                boolean validTerm = true;
                Logger.info("Trying to add new matching term: " + newMatchingTerm);
                if (newMatchingTerm.toLowerCase().startsWith("/re:")) {
                    try {
                        Pattern p = Pattern.compile(newMatchingTerm.substring(4));
                        
                    }
                    catch (PatternSyntaxException p) {
                        showMessageDialog(null, "The synonym " + newMatchingTerm + " does not contain a valid regular expression.");
                        validTerm = false;
                    }
                }
                
                if (validTerm)
                    if(tempDicTerm.addMatchingTerm(newMatchingTerm)){
                        updateNewMatchingTermTable(tempDicTerm);
                    } else {
                        // alert message that the given matching term eixsts
                        showMessageDialog(null, "Synonym already exists.");
                    }
            }
        }
        else {
            showMessageDialog(null, "Please set a representative term first..");

        }
    }//GEN-LAST:event_addMatchingTermButtonActionPerformed

    private void matchingTermFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_matchingTermFieldKeyPressed
        int key=evt.getKeyCode();
        if(key==KeyEvent.VK_ENTER)
        {   
            if(matchingTermField.isFocusOwner()){
                if(!matchingTermField.getText().equals("")){
                    String newMatchingTerm = matchingTermField.getText().toLowerCase();
                    if(tempDicTerm.addMatchingTerm(newMatchingTerm)){
                        updateNewMatchingTermTable(tempDicTerm);
                    } else {
                        // alert message that the given matching term exists
                        showMessageDialog(null, "Synonyms already exists.");
                    }
                }
            }
        }
    }//GEN-LAST:event_matchingTermFieldKeyPressed

    private void deleteTempMatchingTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTempMatchingTermButtonActionPerformed
        if(newMatchingTermTable.getSelectedRow()>-1){
            int selectedRow = newMatchingTermTable.getSelectedRow();
            String selectedTerm = (String) newMatchingTermTable.getValueAt(selectedRow, 0);
            tempDicTerm.deleteMatchingTerm(selectedTerm);
            updateNewMatchingTermTable(tempDicTerm);
            if (newMatchingTermTable.getModel().getRowCount() > 0) {
                newMatchingTermTable.setRowSelectionInterval(0, 0);
            }
        } else {
            // alert message that there is no row selected
            showMessageDialog(null, "Please select any synonym.");
        }
    }//GEN-LAST:event_deleteTempMatchingTermButtonActionPerformed

    private void addTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTermButtonActionPerformed
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Do you want to register representative term with synonyms?", "Confirm Save Representative Term",
                JOptionPane.YES_NO_OPTION);
        //Close if user confirmed
        if (confirmed == JOptionPane.YES_OPTION)
        {
            String selectedRepresentativeTerm = representativeTermField.getText();
            DicManager aDicManager = new DicManager();
            aDicManager.setCurrentDic(DialogMain.dictionary);
            aDicManager.deleteRepresentativeTerm(selectedRepresentativeTerm);
            tempDicTerm.setAllowRandomSynonym(allowRandomSynonymCheckbox.isSelected());
            aDicManager.addRepresentativeTerm(tempDicTerm);

            // alert message that there is no row selected
            showMessageDialog(null, "New representative term is registered.");


            //update GUI
            updateRepresentativeTermTable("");
            updateUserInputField();
            updateMatchingTermTable(tempDicTerm.getRepresentativeTerm());
            
    //        this.dispose();
        }
    }//GEN-LAST:event_addTermButtonActionPerformed

    private void fetchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fetchButtonActionPerformed
        if (setRepresentativeTermButton.isSelected()) {
            if (representativeTermField.getText() != null && !representativeTermField.getText().equals("")) {
                fetchSynonymsDialog.setVisible(true);
            }
            else {
                fetchSynonymsDialog.setVisible(false);
                JOptionPane.showMessageDialog(this,"You need to specify the representative term to which you are adding synonyms..");
            }
        }
        else {
            showMessageDialog(this, "Please set a representative term first..");
        }
    }//GEN-LAST:event_fetchButtonActionPerformed

    private void fetchSynonymsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fetchSynonymsButtonActionPerformed
        if (referenceDatabaseSynonymFieldList.getSelectedValue() != null) {
            if (!fetchSelectField.getText().isEmpty()) {
                
                String representativeTerm = representativeTermField.getText();
                String criteriaString1 = fetchCriteria1.getText();
                String criteriaString2 = fetchCriteria2.getText();
                String criteriaTableAndField1 = fetchCriteriaFieldTextField1.getText();
                String criteriaTableAndField2 = fetchCriteriaFieldTextField2.getText();
                String [] tableAndField;
                String overallCriteriaString = "";
                
                if (!criteriaTableAndField1.isEmpty()) {
                    tableAndField = criteriaTableAndField1.split("\\.");
                    overallCriteriaString += " " + tableAndField[1] + " like '%" + criteriaString1 + "%'"; 
                }
                
                if (!criteriaTableAndField2.isEmpty()) {
                    tableAndField = criteriaTableAndField2.split("\\.");
                    if (!criteriaTableAndField1.isEmpty())
                        overallCriteriaString += " AND ";

                    overallCriteriaString += " " + tableAndField[1] + " like '%" + criteriaString2 + "%'"; 

                }

                String query;
                ArrayList<String> selectedFieldData;
                tableAndField = fetchSelectField.getText().split("\\.");
                
                query = "select " + tableAndField[1] + " from " + tableAndField[0] + " where " + overallCriteriaString;
                selectedFieldData = DBOperation.selectQueryAsStringList(query);
                Logger.info("Fetch query is: " + query);
                
                String alreadyAddedItems = "";

                int count = 1;
                for (String item: selectedFieldData) {
                    if(tempDicTerm.addMatchingTerm(item)){
                        updateNewMatchingTermTable(tempDicTerm);
                    } 
                    else {
                        if (count != 1)
                            alreadyAddedItems += ", " + item;
                        else
                            alreadyAddedItems = item;

                        if (count%10 == 0)
                            alreadyAddedItems += "\n";

                    }
                    count ++;
                }
                    // alert message that the given matching term eixsts
                if (!alreadyAddedItems.equals("")) {
                    showMessageDialog(this,"The following synonyms already existed: \n" + alreadyAddedItems);
                    alreadyAddedItems = "";

                }
            }
            else {
                JOptionPane.showMessageDialog(this,"You need to select a database field to be used as the synonym source..");
            }
            
          
        }
        else {
            JOptionPane.showMessageDialog(this,"You need to select a database field to be used as the synonym source..");
        }
    }//GEN-LAST:event_fetchSynonymsButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        fetchSynonymsDialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DialogMain.populateWindowsMenu(windowsMenu);
    }//GEN-LAST:event_formWindowActivated

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DialogMain.removeFromWindowsList(this);
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void exportDictionaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDictionaryButtonActionPerformed
        importexportFileChooser.setSelectedFile(new File("mydictionary.ids"));
        importexportFileChooser.setFileFilter(new FileNameExtensionFilter("ids file","ids"));
        
        int returnVal = importexportFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = importexportFileChooser.getSelectedFile();
            //This is where a real application would open the file.
            Logger.info("Saving to: " + file.getName());
            DBOperation.exportDictionary(DialogMain.dictionary.getDicId(), file);
        }
    }//GEN-LAST:event_exportDictionaryButtonActionPerformed

    private void importDictionaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDictionaryButtonActionPerformed
        //importexportFileChooser.setSelectedFile(new File("mydictionary.ids"));
        importexportFileChooser.setFileFilter(new FileNameExtensionFilter("ids file","ids"));
        int confirmed = JOptionPane.showConfirmDialog(this,
                        "Warning - this will overwrite the current dictionary!  Do you want to continue?", "Overwrite dictionary",
                        JOptionPane.YES_NO_CANCEL_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            int returnVal = importexportFileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = importexportFileChooser.getSelectedFile();
                             
                // delete all the current matching terms.
                // Don't delete the representative terms however, naughty, they're used in conclusions!
                DicManager aDicManager = new DicManager();
                aDicManager.setCurrentDic(DialogMain.dictionary);
                for (Map.Entry aDicTermEntry : DialogMain.dictionary.getDicBase().entrySet()) {
                    String repString = ((DicTerm)aDicTermEntry.getValue()).getRepresentativeTerm();
                    aDicManager.deleteMatchingTermsForRepresentativeTerm(repString);
                }
              
                try {
                    BufferedReader reader = Files.newBufferedReader(file.toPath(), Charset.defaultCharset());
                    
                    String line;
                    String [] repTermAndMatch;
                    String repTerm;
                    int allowRandom;
                    String matchTerm;

                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("#")) { // ignore comment lines
                            repTermAndMatch = line.split("\\s");
                            repTerm = repTermAndMatch[0];
                            if (repTermAndMatch.length >= 2) {

                                allowRandom = Integer.parseInt(line.substring(line.indexOf(" ")+1,line.indexOf(" ")+2));
                                matchTerm = line.substring(line.indexOf(" ")+3);
                                aDicManager.addMatchingTerm(repTerm, matchTerm,(allowRandom==1));                              
                            }
                            else {                            
                                aDicManager.addRepresentativeTermUsingString(repTerm,true);
                            }
                        }
                    }  
                }
                catch ( Exception e ) {
                    Logger.error( e.getClass().getName() + ": " + e.getMessage() );
                }
                updateRepresentativeTermTable(aDicManager.getCurrentDic().getDicBase().entrySet().iterator().next().getValue().getRepresentativeTerm());
                updateUserInputField();
                updateMatchingTermTable(aDicManager.getCurrentDic().getDicBase().entrySet().iterator().next().getValue().getRepresentativeTerm());
            }
        }
    }//GEN-LAST:event_importDictionaryButtonActionPerformed

    private void setCriteriaFieldButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setCriteriaFieldButton1ActionPerformed
        if (referenceDatabaseSynonymTable.getSelectedRow() >= 0) {
            String table = (String)referenceDatabaseSynonymTable.getValueAt(referenceDatabaseSynonymTable.getSelectedRow(), 0);
            String tableField = referenceDatabaseSynonymFieldList.getSelectedValue();
            fetchCriteriaFieldTextField1.setText(table + "." + tableField);
        }
    }//GEN-LAST:event_setCriteriaFieldButton1ActionPerformed

    private void setCriteriaFieldButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setCriteriaFieldButton2ActionPerformed
        if (referenceDatabaseSynonymTable.getSelectedRow() >= 0) {
            String table = (String)referenceDatabaseSynonymTable.getValueAt(referenceDatabaseSynonymTable.getSelectedRow(), 0);
            String tableField = referenceDatabaseSynonymFieldList.getSelectedValue();
            fetchCriteriaFieldTextField2.setText(table + "." + tableField);
        }
    }//GEN-LAST:event_setCriteriaFieldButton2ActionPerformed

    private void fetchSelectFieldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fetchSelectFieldButtonActionPerformed
        if (referenceDatabaseSynonymTable.getSelectedRow() >= 0) {
            String table = (String)referenceDatabaseSynonymTable.getValueAt(referenceDatabaseSynonymTable.getSelectedRow(), 0);
            String tableField = referenceDatabaseSynonymFieldList.getSelectedValue();
            fetchSelectField.setText(table + "." + tableField);
        }
    }//GEN-LAST:event_fetchSelectFieldButtonActionPerformed

    /**
     * 
     * 
     */
    public static void execute() {
        /* Set the Windows look and feel */
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
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DicManagerGUI().setVisible(true);
            }
        });
    }
    
    
    /**
     * 
     * @param aCase
     */
    public static void execute(DialogCase aCase) {
        selectedCase = aCase;
        /* Set the Windows look and feel */
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
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DicManagerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DicManagerGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMatchingTermButton;
    private javax.swing.JButton addTermButton;
    private javax.swing.JCheckBox allowRandomSynonymCheckbox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton deleteTempMatchingTermButton;
    private javax.swing.JButton exportDictionaryButton;
    private javax.swing.JButton fetchButton;
    private javax.swing.JTextField fetchCriteria1;
    private javax.swing.JTextField fetchCriteria2;
    private javax.swing.JTextField fetchCriteriaFieldTextField1;
    private javax.swing.JTextField fetchCriteriaFieldTextField2;
    private javax.swing.JTextField fetchSelectField;
    private javax.swing.JButton fetchSelectFieldButton;
    private javax.swing.JButton fetchSynonymsButton;
    private javax.swing.JDialog fetchSynonymsDialog;
    private javax.swing.JButton importDictionaryButton;
    private javax.swing.JFileChooser importexportFileChooser;
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
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextField matchingTermField;
    private javax.swing.JTable matchingTermTable;
    private javax.swing.JTable newMatchingTermTable;
    private javax.swing.JList<String> referenceDatabaseSynonymFieldList;
    private javax.swing.JTable referenceDatabaseSynonymTable;
    private javax.swing.JTextField representativeTermField;
    private javax.swing.JTable representativeTermTable;
    private javax.swing.JButton setCriteriaFieldButton1;
    private javax.swing.JButton setCriteriaFieldButton2;
    private javax.swing.JToggleButton setRepresentativeTermButton;
    private javax.swing.JTextField userInputTextField;
    private javax.swing.JMenu windowsMenu;
    // End of variables declaration//GEN-END:variables

/*
private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        if(representativeTermTable.getSelectedRow()>-1){
            int selectedRow = representativeTermTable.getSelectedRow();
            String selectedTerm = (String) representativeTermTable.getValueAt(selectedRow, 0);
            DialogMain.dictionary.moveTermUpInList(selectedTerm);
            updateRepresentativeTermTable(selectedTerm);
            

        }
            
    }                                            

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        if(representativeTermTable.getSelectedRow()>-1){
            int selectedRow = representativeTermTable.getSelectedRow();
            String selectedTerm = (String) representativeTermTable.getValueAt(selectedRow, 0);
            DialogMain.dictionary.moveTermDownInList(selectedTerm);
            updateRepresentativeTermTable(selectedTerm);
            

        }    // TODO add your handling code here:
    } 
*/


}

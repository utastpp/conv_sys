/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.gui;

import java.util.ArrayList;
import java.util.logging.Level;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import rdr.apps.Main;
import rdr.cases.CaseLoader;
import rdr.cases.CaseStructure;
import rdr.logger.Logger;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.model.ValueType;

/**
 * This class is used to present GUI for constructing a structure of case
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class CaseStructureFrame extends javax.swing.JFrame {

    /**
     *
     */
    public static String mode = "";

    /**
     *
     */
    protected ArrayList<String> tempCategoricalValues = new ArrayList<>();
    /**
     * Creates new form CaseStructureFrame
     */
    public CaseStructureFrame() {
        initComponents();
        if(mode.equals("edit")){
            deleteButton.setEnabled(false);
            updateTable();
        }
        
    }
    
    private void updateTable(){
        
        //get row count
        int existTableRows = Main.domain.getCaseStructure().getAttrAmount();

        //create temp object
        Object[][] tempObject = new Object[existTableRows+1][3];
        
        String[] attrNameArray = Main.domain.getCaseStructure().getAttributeNameArray();
        
        tempObject[0][0]= "Case ID";
        tempObject[0][1]= "Continuous";
        tempObject[0][2]= "1";
                
        for(int i=0; i<attrNameArray.length; i++){
            String attrName = attrNameArray[i];
            
            ValueType attrValueType = Main.domain.getCaseStructure().getAttributeByName(attrName).getValueType();
            
            String attrType = attrValueType.getTypeName();            
            String attrSampleVal = attrValueType.getSampleValue().toString();
            
            tempObject[i+1][0] = attrName;
            tempObject[i+1][1] = attrType;
            tempObject[i+1][2] = attrSampleVal;
        }
        
        //update table
        attributeTable.setModel(new DefaultTableModel(
                tempObject,
                new String [] {
                    "Attribute Name", "Attribute Type", "Sample Value"
                }
         ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });

    }
    
    private CaseStructure addAttribute(String newAttrName, String newAttrType) {
        //get existing table
        TableModel existTableModel = attributeTable.getModel();

        //get row count
        int existTableRows = existTableModel.getRowCount();

        //create temp object
        Object[][] tempObject = new Object[existTableRows+1][3];

        // boolean for checking duplicating condition
        boolean existBool = false;

        //checking duplicating condition  &&  storing existing table into temp object
        for(int i=0;i<existTableRows;i++){

            //checking existence condition with new condition
            String currentAttrName = existTableModel.getValueAt(i, 0).toString();
            if(newAttrName.equals(currentAttrName)){
                // new condition same as existing condition
                existBool = true;
            }
            //storing existing table
            for(int j=0;j<3;j++){                
                tempObject[i][j]=existTableModel.getValueAt(i, j);
            }
        }

        // if there is no duplicating condition update table with new condition
        if(existBool==false){
            ValueType sampleValue = new ValueType(newAttrType);

            tempObject[existTableRows][0]=newAttrName;
            tempObject[existTableRows][1]=newAttrType;
            tempObject[existTableRows][2]=sampleValue.getSampleValue().toString();

            //update table
            attributeTable.setModel(new DefaultTableModel(
                    tempObject,
                    new String [] {
                        "Attribute Name", "Attribute Type", "Sample Value"
                    }
             ) {
                boolean[] canEdit = new boolean [] {
                    false, false, false
                };
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });

        } else {
            // alert message that there is duplicating condition
            showMessageDialog(null, "Same Attribute exists.");
            return null;

        }
        // empty attribute name field
        attributeNameField.setText("");

        // set focus on attribute name field
        attributeNameField.requestFocus();

        //get current case structure
        CaseStructure tempCaseStructure = Main.domain.getCaseStructure();

        //create attribute
        IAttribute attr = AttributeFactory.createAttribute(newAttrType);

        //set categorical values if attribute type is categorical
        if(newAttrType.equals("Categorical")){
            attr.setCategoricalValues(tempCategoricalValues);
        }
        
        //set attribute name
        attr.setName(newAttrName);
        attr.setAttributeType("Case Attribute");

        attr.setValueType(new ValueType(newAttrType));

        //add attribute in case structure
        tempCaseStructure.addAttribute(attr);
        Logger.info("Attribute (" + newAttrName  +" - " + newAttrType + ") added");
        
        return tempCaseStructure;
    }

    /**
     * This method is called from within the constructor to initialize the form.w
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categoryFrame = new javax.swing.JFrame();
        jScrollPane2 = new javax.swing.JScrollPane();
        categoryTable = new javax.swing.JTable();
        categoryTextField = new javax.swing.JTextField();
        addCategoryButton = new javax.swing.JButton();
        deleteCategoryButton = new javax.swing.JButton();
        confirmCategoryButton = new javax.swing.JButton();
        attrNameLabel = new javax.swing.JLabel();
        attrLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        attributeTable = new javax.swing.JTable();
        attributeTypeComboBox = new javax.swing.JComboBox();
        attributeNameField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        submitButton = new javax.swing.JButton();

        categoryFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        categoryFrame.setTitle("Categorical Options");
        categoryFrame.setMinimumSize(new java.awt.Dimension(320, 270));
        categoryFrame.setResizable(false);
        categoryFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                categoryFrameWindowClosed(evt);
            }
        });

        categoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Category"
            }
        ));
        jScrollPane2.setViewportView(categoryTable);

        addCategoryButton.setText("Add");
        addCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCategoryButtonActionPerformed(evt);
            }
        });

        deleteCategoryButton.setText("Delete Selected");
        deleteCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCategoryButtonActionPerformed(evt);
            }
        });

        confirmCategoryButton.setText("Confirm");
        confirmCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmCategoryButtonActionPerformed(evt);
            }
        });

        attrNameLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        attrNameLabel.setText("jLabel1");

        attrLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        attrLabel.setText("Attribute Name:");

        javax.swing.GroupLayout categoryFrameLayout = new javax.swing.GroupLayout(categoryFrame.getContentPane());
        categoryFrame.getContentPane().setLayout(categoryFrameLayout);
        categoryFrameLayout.setHorizontalGroup(
            categoryFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(categoryFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(categoryFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(categoryFrameLayout.createSequentialGroup()
                        .addComponent(deleteCategoryButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                        .addComponent(confirmCategoryButton))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(categoryFrameLayout.createSequentialGroup()
                        .addComponent(categoryTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addCategoryButton))
                    .addGroup(categoryFrameLayout.createSequentialGroup()
                        .addComponent(attrLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attrNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        categoryFrameLayout.setVerticalGroup(
            categoryFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, categoryFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(categoryFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attrNameLabel)
                    .addComponent(attrLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(categoryFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addCategoryButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(categoryFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteCategoryButton)
                    .addComponent(confirmCategoryButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Case Structure");
        setLocationByPlatform(true);
        setResizable(false);

        attributeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Case ID", "Continuous", "1"}
            },
            new String [] {
                "Attribute Name", "Attribute Type", "Sample Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        attributeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        attributeTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(attributeTable);
        if (attributeTable.getColumnModel().getColumnCount() > 0) {
            attributeTable.getColumnModel().getColumn(0).setResizable(false);
            attributeTable.getColumnModel().getColumn(1).setResizable(false);
            attributeTable.getColumnModel().getColumn(2).setResizable(false);
        }

        attributeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Continuous", "Categorical", "Text", "Date", "Boolean" }));

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete selected");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(attributeNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(attributeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(submitButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attributeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteButton)
                    .addComponent(submitButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        
        String newAttrName = (String)attributeNameField.getText();
        String newAttrType = (String)attributeTypeComboBox.getSelectedItem();


        if(!newAttrName.equals("")){
            if(newAttrType.equals("Categorical")){
                Logger.info("Creating categorical attribute");
                //open categorical frame
                attrNameLabel.setText(newAttrName);
                
                //reset category table and container
                categoryTable.setModel(new javax.swing.table.DefaultTableModel(
                    new Object [][] {

                    },
                    new String [] {
                        "Category"
                    }
                ));
                tempCategoricalValues = new ArrayList<>();
                
                categoryFrame.setVisible(true);
                this.setEnabled(false);
                this.setAlwaysOnTop(false);
            } else {
                CaseStructure tempCaseStructure = addAttribute(newAttrName, newAttrType);
                if(tempCaseStructure!=null) {                
                    //set case structure
                    Main.domain.setCaseStructure(tempCaseStructure);
                }
            }
        } else {
            // alert message that field is empty
            showMessageDialog(null, "Please name the attribute.");
        }
            
    }//GEN-LAST:event_addButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        
        TableModel previousModel = attributeTable.getModel();
        int amountRows=previousModel.getRowCount();
        int amountColumns=previousModel.getColumnCount();
        
        int selectedRow = attributeTable.getSelectedRow();
        
        if(selectedRow >-1 ) {
            Object[][] tempArray = new Object[amountRows-1][amountColumns];
            //get selected attribute name
            String selectedAttrName = (String) attributeTable.getValueAt(selectedRow, 0);

            if(selectedRow!=0){
                int passed = 0;
                for(int i =0; i<amountRows; i++){
                    if(i==selectedRow){
                        passed=1;
                    } else {
                        for(int j=0; j<amountColumns; j++) {                
                            if(passed!=1){
                                tempArray[i][j]=(String) previousModel.getValueAt(i, j);
                            } else{
                                tempArray[i-1][j]=(String) previousModel.getValueAt(i, j);
                            }
                        }
                    }

                }
                TableModel newModel = new DefaultTableModel(
                        tempArray,
                            new String [] {
                                "Attribute Name", "Attribute Type", "Sample Value"
                            }
                 ) {
                        boolean[] canEdit = new boolean [] {
                            false, false, false
                        };
                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                            return canEdit [columnIndex];
                        }
                };        
                attributeTable.setModel(newModel);

                attributeTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);

                 //get current case structure
                CaseStructure tempCaseStructure = Main.domain.getCaseStructure();

                //add attribute in case structure
                tempCaseStructure.deleteAttributeByName(selectedAttrName);

                //set case structure
                Main.domain.setCaseStructure(tempCaseStructure);

            } else {
                showMessageDialog(null, "You cannot delete Case ID.");
            }
        } else {
            showMessageDialog(null, "Please select the attribute.");
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        int attributeAmount = attributeTable.getRowCount();
        if(attributeAmount!=1){       
            
            //insert case structure into db
            CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());
            
            try {
                CaseLoader.createArffFileWithCaseStructure();
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(CaseStructureFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // dispose case structure frame
            this.dispose();

            // open main frame
            MainFrame.execute(false, Main.domain.getDomainName(), Main.domain.getReasonerType());
        } else{
            showMessageDialog(null, "You need at least one attribute.");
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void categoryFrameWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_categoryFrameWindowClosed

        this.setEnabled(true);
        this.setAlwaysOnTop(true);
    }//GEN-LAST:event_categoryFrameWindowClosed

    private void confirmCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmCategoryButtonActionPerformed
        // confirm category
        String newAttrName = attributeNameField.getText();
        String newAttrType = (String)attributeTypeComboBox.getSelectedItem();
        
        CaseStructure tempCaseStructure = addAttribute(newAttrName, newAttrType);
        if(tempCaseStructure!=null) {                
            //set case structure
            Main.domain.setCaseStructure(tempCaseStructure);
            categoryFrame.dispose();
        }
    }//GEN-LAST:event_confirmCategoryButtonActionPerformed

    private void deleteCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCategoryButtonActionPerformed
        // delete selected category
        TableModel previousModel = categoryTable.getModel();
        int amountRows=previousModel.getRowCount();
        
        int selectedRow = categoryTable.getSelectedRow();
        if(selectedRow>-1){
            Object[][] tempArray = new Object[amountRows-1][1];
            
            //get selected attribute name
            String selectedCatText = (String) categoryTable.getValueAt(selectedRow, 0);

            int passed = 0;
            for(int i =0; i<amountRows; i++){
                if(i==selectedRow){
                    passed=1;
                } else {           
                    if(passed!=1){
                        tempArray[i][0]=(String) previousModel.getValueAt(i, 0);
                    } else{
                        tempArray[i-1][0]=(String) previousModel.getValueAt(i-1, 0);
                    }
                }

            }
            TableModel newModel = new DefaultTableModel(
                    tempArray,
                        new String [] {
                            "Category"
                        }
             ) {
                    boolean[] canEdit = new boolean [] {
                        false
                    };
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit [columnIndex];
                    }
            };        
            categoryTable.setModel(newModel);
            categoryTable.setRowSelectionInterval(selectedRow-1, selectedRow-1);
            //delete attribute in case structure
            tempCategoricalValues.remove(selectedCatText);     
        } else {
            showMessageDialog(null, "Please select the category.");
        }

    }//GEN-LAST:event_deleteCategoryButtonActionPerformed

    private void addCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCategoryButtonActionPerformed
        // add category
        String newCatText = categoryTextField.getText();

         if(!newCatText.equals("")){
             //get existing table
            TableModel existTableModel = categoryTable.getModel();

            //get row count
            int existTableRows = categoryTable.getRowCount();

            //create temp object
            Object[][] tempObject = new Object[existTableRows+1][1];

            // boolean for checking duplicating category
            boolean existBool = false;

            //checking duplicating category  &&  storing existing table into temp object
            for(int i=0;i<existTableRows;i++){
                //checking existence category with new category
                String currentCatText = existTableModel.getValueAt(i, 0).toString();
                if(newCatText.equals(currentCatText)){
                    // new category exists 
                    existBool = true;
                }
                //storing existing table
                tempObject[i][0]=existTableModel.getValueAt(i,0);
            }
            // if there is no duplicating condition update table with new category
            if(existBool==false){
                // add category into category storage and table Object
                tempCategoricalValues.add(newCatText);                
                tempObject[existTableRows][0]=newCatText; 

                //update table
                categoryTable.setModel(new DefaultTableModel(
                    tempObject,
                    new String [] {
                        "Category"
                    }
                ) {
                    boolean[] canEdit = new boolean [] {
                        false
                    };
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit [columnIndex];
                    }
                });
                // empty category name field
                categoryTextField.setText("");

                // set focus on category name field
                categoryTextField.requestFocus();
            } else {
                // alert message that there is duplicating category
                showMessageDialog(null, "Same category exists.");
            }
        } else {
            // alert message that field is empty
            showMessageDialog(null, "Please name the category.");
        }
         
        

    }//GEN-LAST:event_addCategoryButtonActionPerformed

    /**
     *
     * @param mode
     */
    public static void execute(String mode) {
        CaseStructureFrame.mode = mode;
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
            java.util.logging.Logger.getLogger(CaseStructureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CaseStructureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CaseStructureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CaseStructureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CaseStructureFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addCategoryButton;
    private javax.swing.JLabel attrLabel;
    private javax.swing.JLabel attrNameLabel;
    private javax.swing.JTextField attributeNameField;
    private javax.swing.JTable attributeTable;
    private javax.swing.JComboBox attributeTypeComboBox;
    private javax.swing.JFrame categoryFrame;
    private javax.swing.JTable categoryTable;
    private javax.swing.JTextField categoryTextField;
    private javax.swing.JButton confirmCategoryButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteCategoryButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton submitButton;
    // End of variables declaration//GEN-END:variables
}

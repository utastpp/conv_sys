/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.mysql.DBOperation;

/**
 *
 * @author dherbert
 */
public class DatabaseItemCommandInstance  extends CommandInstance {
    public static final String saveSchemaItem = "SaveSchemaItem";
    public static final String loadAllSchemaItems = "LoadAllSchemaItems";
    public static final String loadSchemaItem = "LoadSchemaItem";
    private final String[] deviceActionList = new String[]{saveSchemaItem,loadSchemaItem,loadAllSchemaItems};
    
    public DatabaseItemCommandInstance(){
        super(DATABASE_ITEM_ID);
    }
    
    public void SaveSchemaItem() {
        
        Logger.info("SaveSchemaItem has been called");
        
        String contextVarName = "";
        String schemaItem = "";
        String primaryID = "";
        String finalDataValue = "";
        
        String[] variableDataList= this.getDeviceActionVariableList();
        
        
        for (String aVariableData: variableDataList) {
           Logger.info("Found variable data:" + aVariableData);
        }
        
        schemaItem = variableDataList[0];
        Logger.info("Trying to save scema item:" + schemaItem);


        ContextVariableUser primaryIDcv = DialogMain.getDialogUserList().getCurrentContextVariables().get("primaryID");
        if (primaryIDcv == null) {
            primaryID = DialogMain.getDialogUserList().getCurrentUsername();
        }
        else {
            primaryID = primaryIDcv.getValue();
        }
        
        if (variableDataList.length > 1) {


            // determine if we have a context variable or literal data
            if (variableDataList[1].startsWith("@")) {
                contextVarName = variableDataList[1].substring(1);
                if (DialogMain.globalContextVariableManager.isUserVariableSet(contextVarName))
                {
                    ContextVariableUser cv = DialogMain.getDialogUserList().getCurrentContextVariables().get(contextVarName);
                    finalDataValue = cv.getValue();
                }
            }
            else { // literal value
                    finalDataValue = variableDataList[1];
            }
            
            Logger.info("Save schema item: " + schemaItem + " with set value: " + finalDataValue + ", using primary ID of :" + primaryID);


            String originalSchemaColumnName = DialogMain.userInterfaceController.getSchemaOriginalColumnName(schemaItem);
            if (!originalSchemaColumnName.isEmpty() && !finalDataValue.isEmpty())
                DBOperation.updateCurrentSlotInformation(primaryID, originalSchemaColumnName, finalDataValue);

        }
        else {
            // we're just saving the current value of the schema item itself (i.e. evaluate it)
            contextVarName = variableDataList[0];

            if (DialogMain.globalContextVariableManager.isUserVariableSet(contextVarName)) {

                ContextVariableUser cv = DialogMain.getDialogUserList().getCurrentContextVariables().get(contextVarName);
                finalDataValue = cv.getValue();
                
                Logger.info("Save schema item: " + schemaItem + " with implicit value: " + finalDataValue + ", using primary ID of :" + primaryID);

                String originalSchemaColumnName = DialogMain.userInterfaceController.getSchemaOriginalColumnName(contextVarName);
                if (!originalSchemaColumnName.isEmpty() && !finalDataValue.isEmpty())
                    DBOperation.updateCurrentSlotInformation(primaryID, originalSchemaColumnName, finalDataValue);
            }
        }
   } 
   
    public void LoadAllSchemaItems() {
        String primaryID = "";       
        Logger.info("LoadAllSchemaItems has been called!");

        
        ContextVariableUser primaryIDcv = DialogMain.getDialogUserList().getCurrentContextVariables().get("primaryID");
        if (primaryIDcv == null) {
            primaryID = DialogMain.getDialogUserList().getCurrentUsername();
        }
        else {
            primaryID = primaryIDcv.getValue();
        }
        
        Logger.info("I have been called to load all saved schema items, using primary ID of :" + primaryID);
        int context = DialogMain.getDialogUserList().getCurrentStackLevel();
        ArrayList<String> storedSchemaValues = DBOperation.getCurrentSlotInformation(primaryID,DialogMain.userInterfaceController.getSelectedSchemaColumnNames());
        
        int index = 0;  // index 0 is the primaryID
        
        //for (String schemaItem: DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray() )
            //Logger.info("SCHEMA items to look for:" + schemaItem);
        
        
       
        if (!storedSchemaValues.isEmpty() && storedSchemaValues.size()==DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray().length) {
            for (String aContextVariableName : DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray()) {
                String variableValue = storedSchemaValues.get(index);
                if (!variableValue.isEmpty()) {
                    Logger.info("Loading variable from database: " + aContextVariableName + " with value " + variableValue);
                    if (!DialogMain.globalContextVariableManager.isUserVariableSet(aContextVariableName)) {
                        ContextVariableUser cvu = new ContextVariableUser(aContextVariableName,variableValue, context);
                        LinkedHashMap<String, ContextVariableUser> newVariableList = new LinkedHashMap<>();
                        newVariableList.put(aContextVariableName, cvu);
                        DialogMain.globalContextVariableManager.setContextVariablesForSpecificContext(newVariableList,context);
                    }
                    else {
                        ContextVariableUser cvu = DialogMain.getDialogUserList().getCurrentContextVariables().get(aContextVariableName);
                        cvu.setVariableValue(variableValue);
                        cvu.setContext(context);
                    }
                }
                index++;
            }
        }
   } 
    
    public void LoadSchemaItem() {
        String primaryID = "";       
        Logger.info("LoadSchemaItem has been called!");
        String schemaItem = "";
        
        String[] variableDataList= this.getDeviceActionVariableList();
        
        if (variableDataList.length == 1) {            
            schemaItem = variableDataList[0];
        
            ContextVariableUser primaryIDcv = DialogMain.getDialogUserList().getCurrentContextVariables().get("primaryID");
            if (primaryIDcv == null) {
                primaryID = DialogMain.getDialogUserList().getCurrentUsername();
            }
            else {
                primaryID = primaryIDcv.getValue();
            }

            int context = DialogMain.getDialogUserList().getCurrentStackLevel();
            ArrayList<String> storedSchemaValues = DBOperation.getCurrentSlotInformation(primaryID,DialogMain.userInterfaceController.getSelectedSchemaColumnNames());

            int index = 0;  // index 0 is the primaryID

            //for (String schemaItem: DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray() )
                //Logger.info("SCHEMA items to look for:" + schemaItem);



            if (!storedSchemaValues.isEmpty() && storedSchemaValues.size()==DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray().length) {
                for (String aContextVariableName : DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray()) {
                    String variableValue = storedSchemaValues.get(index);
                    if (!variableValue.isEmpty() && aContextVariableName.equals(schemaItem)) {
                        Logger.info("Loading variable from database: " + aContextVariableName + " with value " + variableValue);
                        if (!DialogMain.globalContextVariableManager.isUserVariableSet(aContextVariableName)) {
                            ContextVariableUser cvu = new ContextVariableUser(aContextVariableName,variableValue, context);
                            LinkedHashMap<String, ContextVariableUser> newVariableList = new LinkedHashMap<>();
                            newVariableList.put(aContextVariableName, cvu);
                            DialogMain.globalContextVariableManager.setContextVariablesForSpecificContext(newVariableList,context);
                        }
                        else {
                            ContextVariableUser cvu = DialogMain.getDialogUserList().getCurrentContextVariables().get(aContextVariableName);
                            cvu.setVariableValue(variableValue);
                            cvu.setContext(context);
                        }
                        break;  // we're only loading one varaible..
                    }
                    index++;
                }
            }
        }
   } 
    
    @Override
    public String[] getDeviceActionList(){
        return this.deviceActionList;
    }
}

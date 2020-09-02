/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.cases;

import cmcrdr.dialog.DialogSet;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.main.DialogMain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.apps.Main;
import rdr.cases.Case;
import cmcrdr.logger.Logger;
import rdr.model.Value;
import rdr.model.ValueType;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DialogCaseArchiveModule {
    
    IDialogInstance dialogInstance;
    DialogCase dialogCase;
    
    /**
     * create txt file
     * 
     * @throws java.lang.Exception
     */
    public static void createTextFileWithCaseStructure(String path) throws Exception {
        try (PrintWriter writer = new PrintWriter(path + "/domain/cases/" + Main.domain.getDomainName() + ".txt")) {
            writer.print("");
            writer.close();
        }
    }
    
    /**
     * save current dialog repository into txt file
     * 
     * @param savePath
     * @throws java.lang.Exception
     */
    public static void createNewTextFileWithCurrentDialogRepository(String savePath) throws Exception {
        try (PrintWriter writer = new PrintWriter(savePath)) {
            
            writer.print(DialogMain.getDialogUserList().getCurrentDialogRepository().getAllDialogStringWithNewLine());
            writer.close();
        }
    }
    
    /**
     *
     * @param savePath
     * @throws Exception
     */
    public static void createNewTextFileWithAllDialogRepositories(String savePath) throws Exception {
        try (PrintWriter writer = new PrintWriter(savePath)) {
            for (DialogSet dialog: DialogMain.getDialogUserList().getAllDialogRepositories()) {
                writer.print(dialog.getAllDialogStringWithNewLine());
                writer.print("\n");
            }
            writer.close();
        }
    }
    
    /**
     * insert case into txt file
     * 
     * @param aCase
     * @throws java.io.FileNotFoundException
     */
    public static void insertCase(DialogCase aCase) throws FileNotFoundException{
        //Logger.info("I'm being called to write case: " + aCase.getCaseId() + " value: " + aCase);
        PrintWriter writer = new PrintWriter(new FileOutputStream(
             //new File(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".txt"), 
             new File(DialogMain.contextPath + "/domain/cases/" + Main.domain.getDomainName() + ".txt"), 
                true /* append = true */)
        ); 
        
        String caseValueString = "";
        
        LinkedHashMap<String, Value> valHashMap = aCase.getValues();       
        Set set = valHashMap.entrySet();
        Iterator iter = set.iterator();
        int cnt = 0;
        while (iter.hasNext()) {
            
            Map.Entry me = (Map.Entry) iter.next();
            String id = (String)me.getKey();
            Value value = (Value) me.getValue();
            String caseValue = value.getActualValue().toString();  
            

            if(caseValue.equals("")){
                caseValue="/null/";
            }
            caseValueString += caseValue;      
            cnt++;
            if(cnt!=valHashMap.size()){
                caseValueString += "/tabComma/";
            }
        }

        writer.println(caseValueString);
        writer.close();
    }
    
    /**
     *
     * @throws Exception
     */
    public static void caseImport(String path) throws Exception {
        BufferedReader br = null;
        String sCurrentLine;

        //br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".txt"));
        br = new BufferedReader(new FileReader(path + "/domain/cases/" + Main.domain.getDomainName() + ".txt"));
        int caseId = 1;
        
        //LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        
        while ((sCurrentLine = br.readLine()) != null) {
            
            String[] currentLineArr = sCurrentLine.split("/tabComma/");
            LinkedHashMap<String, Value> values = new LinkedHashMap<>();
           // Logger.info("**CURRENTLINE:" + sCurrentLine);
            //if (currentLineArr.length < 4)
                //Logger.info("Bad line is:" + caseId);
                       
            
            String recent = currentLineArr[0];
            String history = currentLineArr[1];
            String eventType = currentLineArr[2];
            String eventValue = currentLineArr[3];
            
            
            Value recentVal = new Value(ValueType.TEXT, recent);
            values.put("Recent", recentVal);
            
            Value historyVal = new Value(ValueType.TEXT, history);
            values.put("History", historyVal);
            
            Value eventTypeVal = new Value(ValueType.TEXT, eventType);
            values.put("EventType", eventTypeVal);
            
            Value eventValueVal = new Value(ValueType.TEXT, eventValue);
            values.put("EventValue", eventValueVal);
            
            Case rdrCase = new Case(Main.domain.getCaseStructure(), values);
            rdrCase.setCaseId(caseId);
            
            //add case into allCaseSet
            //Logger.info("Adding case:" + caseId + " which has recent:" + recent);
            Main.allCaseSet.addCase(rdrCase);
            caseId++;
        }

            
                        
            
        }
}

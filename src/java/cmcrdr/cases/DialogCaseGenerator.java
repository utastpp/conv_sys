/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.cases;

import cmcrdr.dialog.IDialogInstance;
import cmcrdr.logger.Logger;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import rdr.apps.Main;
import rdr.model.Value;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DialogCaseGenerator {
    
    /**
     *
     * @param aDialogInstance
     * @param values
     * @param archive
     * @return
     */
    public static DialogCase generateCase(IDialogInstance aDialogInstance, LinkedHashMap<String, Value> values, boolean archive) {

        DialogCase newCase = new DialogCase(Main.domain.getCaseStructure(), values);
        newCase.setCaseId(Main.allCaseSet.getNewCaseId());
        
        // input으로 쓰인 DialogInstance를 등록. UserDialogInstance 또는 EventLogInstance가 등록 가능.
        newCase.setInputDialogInstance(aDialogInstance);

        //insert new case into arff
        if (archive) {
            try {
                //Logger.info("I'm calling insertCase: " + newCase.getCaseId() + " value: " + newCase);
                DialogCaseArchiveModule.insertCase(newCase);


            } catch (FileNotFoundException ex) {
                Logger.info("File not found");
            }
        }
        return newCase;
    }
    
}

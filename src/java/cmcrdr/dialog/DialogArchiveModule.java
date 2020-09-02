/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dialog;

import cmcrdr.cases.DialogCase;
import cmcrdr.main.DialogMain;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import rdr.apps.Main;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DialogArchiveModule {
    
    /**
     *
     * @param aCase
     * @param derivedCaseId
     * @param systemResponse
     * @return
     */
    public static SystemDialogInstance archiveSystemDialogInstance(DialogCase aCase, int derivedCaseId, String systemResponse) {
        // archive system response
        SystemDialogInstance dialog = new SystemDialogInstance();
        
       // dialog.setDialogId(DialogMain.getCurrentDialogRepository().getNewId());
        dialog.setDialogId(DialogMain.getDialogUserList().getNewIdFromAllDialogRepositories());
        dialog.setDialogTypeCode(DialogInstance.SYSTEM_TYPE);
        dialog.setDialogStr(systemResponse);        
        dialog.setIsRuleFired(false);
        dialog.setDerivedCaseId(derivedCaseId);
        dialog.setDerivedCaseRecentData(aCase.getInputDialogInstance().getDialogStr());
        //Logger.info("Adding dialog to SYSTEM set :" + dialog.toString());
        DialogMain.getDialogUserList().getCurrentDialogRepository().addDialogInstance(dialog);
        
        return dialog;
    }
    
    /**
     *
     * @param user
     * @throws Exception
     */
    public static void createTextFileForDialogHistory(String user) throws Exception {
        //try (FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
        try (FileWriter writer = new FileWriter(DialogMain.contextPath + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw);
                ){
            out.print("");
            out.close();
        }
    }
    
    /**
     *
     * @param user
     * @param dialogType
     * @param dialogString
     * @throws Exception
     */
    public static void archiveDialogString(String user, int dialogID, int dialogType, String dialogString) throws Exception {
        //try (FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
        try (FileWriter writer = new FileWriter(DialogMain.contextPath + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw);
                )
        { 
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String header = dateFormat.format(cal.getTime());
        
        if (dialogType == DialogInstance.USER_TYPE)
            header  += "\tUSER:[" + dialogID + "]\t";
        else if (dialogType == DialogInstance.SYSTEM_TYPE)
            header  += "\tSYSTEM:[" + dialogID + "]\t";

        out.println(header + dialogString);
        out.close();
        }
    }
    
    public static void archiveFeedbackString(String user, String dialogID, String userDialogString, String systemDialogString, String feedback) throws Exception {
        //try (FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
        try (FileWriter writer = new FileWriter(DialogMain.contextPath + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + "-feedback.txt", true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw);
                )
        { 
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String header = dateFormat.format(cal.getTime());
        
        int actualDialogID = Integer.parseInt(dialogID);
        
        out.println(header + "\tUSER:[" + (actualDialogID-1) + "]\t" + userDialogString);
        out.println(header + "\tSYSTEM:[" + actualDialogID + "]\t" + systemDialogString + " " + "\tFEEDBACK:[" + feedback + "]");
        out.close();
        }
    }
    
    public static void archiveSystemFeedbackString(String user, String rank, String comment) throws Exception {
        //try (FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
        try (FileWriter writer = new FileWriter(DialogMain.contextPath + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + "-systemfeedback.txt", true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw);
                )
        { 
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String header = dateFormat.format(cal.getTime());
               
        out.println(header + "\tRANK:[" + rank + "]\t" + "COMMENT:[" + comment + "]");
        out.close();
        }
    }
    
    public static void archiveStatusString(String user, String status) throws Exception {
        //try (FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + ".txt", true);
        try (FileWriter writer = new FileWriter(DialogMain.contextPath + "/domain/dialogHistory/" + Main.domain.getDomainName() + "_" + user + "-feedback.txt", true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw);
                )
        { 
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String header = dateFormat.format(cal.getTime());
               
        out.println(header + "\tSTATUS:[" + status + "]");
        out.close();
        }
    }
}

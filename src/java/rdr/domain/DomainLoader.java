/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.domain;

import cmcrdr.logger.Logger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import rdr.apps.Main;
import rdr.cases.CaseLoader;
import rdr.rules.RuleLoader;
import cmcrdr.mysql.DBOperation;


/**
 * This class is used to load and save domain 
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DomainLoader {
    
    /**
     *
     * @param domainName
     * @param isCaseImport
     * @throws Exception
     */
    public static void openDomainFile(String domainName, String path, boolean isCaseImport) throws Exception {
        DomainLoader.setDomainDetails(domainName);

        CaseLoader.setCaseStructure();
        if(isCaseImport){
            CaseLoader.caseImport();
            RuleLoader.setRules();
        }
    }
    
    /**
     *
     * @param domainName
     * @throws Exception
     */
    public static void reloadDomainFile(String domainName, String path) throws Exception {
        Logger.info("I too am calling connect with " + domainName);
        Logger.info("my path is " + path);
        DomainLoader.setDomainDetails(domainName);

        CaseLoader.setCaseStructure();
        RuleLoader.setRules();
    }
    
    /**
     *
     * @param domainName
     * @param domainDesc
     * @param domainReasoner
     */
    public static void inserDomainDetails(String domainName, String domainDesc,String domainReasoner){
        String[] attributes = new String[5];
        String[] values = new String[5];
        
        attributes[0] = "domain_name";
        attributes[1] = "domain_desc";
        attributes[2] = "domain_reasoner";
        attributes[3] = "domain_created";
        attributes[4] = "domain_modified";
         
        String cur_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        values[0] = domainName;
        values[1] = domainDesc;
        values[2] = domainReasoner;
        values[3] = cur_time;
        values[4] = cur_time;
        DBOperation.insertQuery("tb_domain_details", attributes, values, false);
        
    }
    
    /**
     *
     * @param domainName
     */
    public static void setDomainDetails(String domainName){
        HashMap<String,String> domainDetails = DBOperation.getDomainDetails();
        if(domainDetails.size()>0){
            Main.domain.setDomainName(domainDetails.get("domainName"));
            Main.domain.setDescription(domainDetails.get("domainDesc"));
            Main.domain.setReasonerType(domainDetails.get("domainReasoner"));
            // DPH 2016
            //Main.workbench = new Workbench(domainDetails.get("domainReasoner"));
            //Main.addWorkbench(domainDetails.get("domainReasoner"));
        } else {
            Main.domain.setDomainName(domainName);
            Main.domain.setDescription(domainName);
            Main.domain.setReasonerType("SCRDR");
            // DPH 2016
            //Main.workbench = new Workbench("SCRDR");
            //Main.addWorkbench("SCRDR");
        }
    }
    
            
        
    
}

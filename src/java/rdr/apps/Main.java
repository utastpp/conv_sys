/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.apps;

import java.io.File;
import java.util.ArrayList;
import org.apache.log4j.PropertyConfigurator;
import rdr.cases.CaseSet;
import rdr.gui.StartupFrame;
import rdr.domain.Domain;
import rdr.logger.Logger;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;
import rdr.workbench.Workbench;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Main {

    /**
     *
     */
    public static Domain domain;

    /**
     *
     */
    public static File loadedFile;

    /**
     *
     */
    public static Workbench workbench;

    // DPH 2016
    //public static ArrayList<Workbench> workbenchList;
    //public static int currentWorkbench = -1;
    
    /**
     *
     */
    public static CaseSet allCaseSet;       

    /**
     *
     */
    public static CaseSet testingCaseSet;

    /**
     *
     */
    public static RuleSet KB;        
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Welcome. This is Dave RDR engine ver1.1");
                
        String log4jConfPath = "./log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        //Define doamin
        String domainName = "";
        String methodType = Domain.MCRDR;
        String dbName = "";
        String description = "";
                    
        domain = new Domain (domainName, methodType, dbName, description);
        Logger.info("IS THIS CALLED?????");
        allCaseSet = new CaseSet();
        testingCaseSet = new CaseSet();
        KB = new RuleSet();
        Rule rootRule = RuleBuilder.buildRootRule();
        KB.setRootRule(rootRule);
        
        StartupFrame.execute();
    }
    /*
    public static void setCurrentWorkbench(int i) {
        currentWorkbench = i;
    }
    
    public static Workbench getCurrentWorkbench() {
        return workbenchList.get(currentWorkbench);
    }
    
    public static ArrayList<Workbench> getWorkbenchList() {
        return workbenchList;
    }
    
    public static void addWorkbench(String methodType) {
        workbenchList.add(new Workbench(methodType));
    }
    
    public static void removeWorkbench(int bench) {
        workbenchList.remove(bench);
    }*/
}

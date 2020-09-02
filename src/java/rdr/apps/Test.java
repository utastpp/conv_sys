/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.apps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.PropertyConfigurator;
import rdr.cases.Case;
import rdr.cases.CaseLoader;
import rdr.cases.CaseSet;
import rdr.cases.CaseStructure;
import rdr.domain.Domain;
import rdr.gui.DomainEditorFrame;
import rdr.gui.MainFrame;
import rdr.gui.StartupFrame;
import rdr.learner.RDRLearner;
import rdr.model.Attribute;
import rdr.model.AttributeFactory;
import rdr.model.CategoricalAttribute;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.reasoner.MCRDRReasoner;
import rdr.reasoner.SCRDRReasoner;
import rdr.rules.Conclusion;
import rdr.rules.ConclusionSet;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Operator;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;
import rdr.workbench.Workbench;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Test {
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
       
    }
    /**
     * Initialise the IDS (Intelligent Dialog System)
     * 
     * @param domainName
     * @param methodType
     * @param domainDesc
     * @param defaultConclusion
     */
    private static void intialiseSystem(String domainName, String methodType, String domainDesc, String defaultConclusion){
        String log4jConfPath = "./log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        
        CaseStructure caseStructure = new CaseStructure();
        IAttribute attr = AttributeFactory.createAttribute("Text");
        attr.setAttributeId(0);
        attr.setName("Recent");
        caseStructure.addAttribute(attr);
        
        IAttribute attr2 = AttributeFactory.createAttribute("Text");
        attr2.setAttributeId(1);
        attr2.setName("History");
        caseStructure.addAttribute(attr2);
        
        Main.domain = new Domain (domainName, methodType, domainName, domainDesc);
        
        Main.domain.setCaseStructure(caseStructure);
        
        Main.allCaseSet = new CaseSet();
        Main.KB = new RuleSet();
        RuleBuilder.setDefaultConclusion(defaultConclusion);
        Rule rootRule = RuleBuilder.buildRootRule();
        Main.KB.setRootRule(rootRule);
        
        // set domainName and methodType
        Main.domain.setDomainName(domainName);
        Main.domain.setReasonerType(methodType);

        // DPH 2016
        Main.workbench = new Workbench(methodType);
        Main.workbench.setRuleSet(Main.KB);
        //Main.addWorkbench(methodType); 
        //Main.workbench.setRuleSet(Main.KB);
       
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.main;

import cmcrdr.cases.DialogCaseArchiveModule;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableManager;
import cmcrdr.logger.Logger;
import cmcrdr.dic.DicConverter;
import cmcrdr.dic.DicManager;
import cmcrdr.dic.Dictionary;
import cmcrdr.external.ExternalExecutor;
import cmcrdr.savedquery.ConclusionQuery;
import cmcrdr.savedquery.SavedQueryTemplate;
import cmcrdr.sqlite.SqliteOperation;
import cmcrdr.user.DialogUser;
import cmcrdr.user.DialogUserList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import cmcrdr.processor.PreAndPostProcessorAction;
import rdr.apps.Main;
import rdr.cases.CaseSet;
import rdr.cases.CaseStructure;
import rdr.domain.Domain;
import rdr.domain.DomainLoader;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleLoader;
import rdr.rules.RuleSet;
import rdr.workbench.Workbench;
import static cmcrdr.sqlite.SqliteOperation.getPreAndPostProcessorActionsList;
import rdr.cases.Case;
import rdr.cases.CaseLoader;
import rdr.cases.CornerstoneCaseSet;
import rdr.sqlite.DBManager;
import cmcrdr.gui.UserInterface;
import cmcrdr.mysql.DBConnection;
import cmcrdr.sqlite.SqliteDBCreation;
import cmcrdr.webinterface.UserInterfaceController;
import cmcrdr.xml.Transrotations;
import static cmcrdr.xml.TransrotationsBuilder.getTransrotationCapabilities;


/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 * Modified by Dave Herbert
 */
public class DialogMain {
    
    // DPH 2016
    private static DialogUserList dialogUserList = new DialogUserList();


    public static ContextVariableManager globalContextVariableManager;
    //public static ContextVariableManager systemContextVariableManager;


    public static Dictionary dictionary = new Dictionary();


    public static DicConverter dicConverter = new DicConverter();
    // DPH Jun 2016
    //public static Dictionary grammarDictionary = new Dictionary();
    

    public static LinkedHashMap<Integer,SavedQueryTemplate> savedQueryTemplateList = new LinkedHashMap<>();


    public static LinkedHashMap<Integer,ConclusionQuery> conclusionQueryList = new LinkedHashMap<>();
    
    
    public static ExternalExecutor externalExecutor = new ExternalExecutor();
    
    //public static KALogic cmcrdrKA = new KALogic();


    //public static SelfLearningModule selfLearningModule = new SelfLearningModule();
    
    //public static ApiReturnDataInstance apiReturnDataInstance = new ApiReturnDataInstance();
 
    // set default response when the system does not understand.
    


    public static String defaultResponse = "";
    
    public static String workbenchMethodType;
    
    public static LinkedHashMap<String,JFrame> windowList = new LinkedHashMap<>();
        
    public static UserInterfaceController userInterfaceController = null; 
    
    public static ArrayList<UserInterface> userInterfaces = new ArrayList<>();
    //public static ICSAdminGUI icsAdminGUI = null;
    
    public static String contextPath;
    
    public static ArrayList<PreAndPostProcessorAction> processorList = new ArrayList<>();
    
    public static String initialGreeting;
    public static String initialShortGreeting;
    public static String STARTSESSION = "STARTSESSION";

    
    //public static boolean headless = false;
    
    public static HashMap<String,String> referenceDatabaseDetails = null; 

    private static String domainName = "";
    private static boolean domainInitialised = false;
    
    private static Transrotations robotCapabilities = null;

    
    public static void setDomainInitialised(boolean initialisation) {
        domainInitialised = initialisation;
    }
    
    public static boolean getIsDomainInitialised() {
        return domainInitialised;
    }
    
    public static void setDomainName(String name) {
        domainName = name;
    }
    
    public static String getDomainName() {
        return domainName;
    }
    
    
    public static String getDomainDescription() {
        return Main.domain.getDescription();
    }
    
    
    public static void initialiseSystem(String path, String domainName, String methodType, String domainDesc, String defaultResponse){
        
        workbenchMethodType = methodType;
        //String log4jConfPath = "./log4j.properties";
        //PropertyConfigurator.configure(log4jConfPath);
        
        contextPath = path;
        
        CaseStructure caseStructure = new CaseStructure();
        IAttribute attr = AttributeFactory.createAttribute("Text");
        attr.setAttributeId(0);
        attr.setName("Recent");
        caseStructure.addAttribute(attr);
        
        IAttribute attr2 = AttributeFactory.createAttribute("Text");
        attr2.setAttributeId(1);
        attr2.setName("History");
        caseStructure.addAttribute(attr2);
        
        IAttribute attr3 = AttributeFactory.createAttribute("Text");
        attr3.setAttributeId(2);
        attr3.setName("EventType");
        caseStructure.addAttribute(attr3);
        
        IAttribute attr4 = AttributeFactory.createAttribute("Text");
        attr4.setAttributeId(3);
        attr4.setName("EventValue");
        caseStructure.addAttribute(attr4);
        
        IAttribute attr5 = AttributeFactory.createAttribute("Text");
        attr5.setAttributeId(4);
        attr5.setName("ContextVars");
        caseStructure.addAttribute(attr5);
        
        IAttribute attr6 = AttributeFactory.createAttribute("Text");
        attr6.setAttributeId(5);
        attr6.setName("STOP");
        caseStructure.addAttribute(attr6);
        
        Main.domain = new Domain (domainName, methodType, domainName, domainDesc);
        
        Main.domain.setCaseStructure(caseStructure);
        
        Main.allCaseSet = new CaseSet();
        Main.KB = new RuleSet();
        RuleBuilder.setDefaultConclusion(defaultResponse);
        Rule rootRule = RuleBuilder.buildRootRule();
        Main.KB.setRootRule(rootRule);
        
        // set domainName and methodType
        Main.domain.setDomainName(domainName);
        Main.domain.setReasonerType(methodType);

        
        Main.workbench = new Workbench(methodType);
        Main.workbench.setRuleSet(Main.KB);
        
        dialogUserList.setCurrentIndex(-1);             
        globalContextVariableManager = new ContextVariableManager();
        
        dicConverter.setDictionary(dictionary);
        
        
        
        // DPH add GUI component to enable this.. 25/9/18
        //loadRobotCapabilities(path);
        
            
    }
    
    public static void createDomain() {
        // assumption is initialiseSystem has already been called..
        
        // create domain database
        Logger.info("About to create databases..");
        DBManager.initialise(Main.domain.getDomainName(), contextPath);       

        try {
            DialogCaseArchiveModule.createTextFileWithCaseStructure(contextPath);
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }

        DomainLoader.inserDomainDetails(Main.domain.getDomainName(), Main.domain.getDescription(), Main.domain.getReasonerType());
        CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());
        RuleLoader.insertRuleConclusions(0, Main.KB.getRootRule().getConclusion());
        RuleLoader.insertRule(0, Main.KB.getRootRule(), 0);      

        // create chat features databases
        SqliteDBCreation.initialise();      
        
        // DPH JUNE 2016 test creating new dicmanager..
        DicManager dicManager = new DicManager();
        dicManager.setCurrentDicByNameFromDB("User");
        
        dictionary = dicManager.getCurrentDic();
        DialogMain.dicConverter.setDictionary(dictionary); // added 2 June 2016
    }
    

    public static boolean initialiseForWeb(String path, String theDomainName, String theDescription) {
        //Define doamin
        //String domainName = "ids-unit-outline";
        String myDomainName = theDomainName;
        String methodType = Domain.MCRDR;
        String domainDesc = theDescription;
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        contextPath = path;
        Logger.info("Initialising, contextPath is set to be: " + contextPath);
        boolean success = false;
        
                
        initialiseSystem(path, myDomainName, methodType, domainDesc, defaultResponse);

        try {
            DomainLoader.openDomainFile(myDomainName,contextPath, false);
            DicManager dicManager = new DicManager();
            dicManager.setCurrentDicByNameFromDB("User");
            dictionary = dicManager.getCurrentDic();
            DialogMain.dicConverter.setDictionary(dictionary);  // added June 2 2016
            globalContextVariableManager.setCurrentContextVariablesFromDB();
            ContextVariable prompting = globalContextVariableManager.addContextVariableUsingString("@SYSTEMprompting");
            prompting.addVariableValue("true");
            ContextVariable emptyDbResult = globalContextVariableManager.addContextVariableUsingString("@SYSTEMemptyDBResult");
            emptyDbResult.addVariableValue("No database result found for query!");
            
            DialogCaseArchiveModule.caseImport(contextPath);
            RuleLoader.setRules();

            for (Rule aRule :  Main.KB.getBase().values()) {
                CornerstoneCaseSet cases = aRule.getCornerstoneCaseSet();
                for (Case aCase: cases.getBase().values()) {
                    Logger.info("Rule: " + aRule.getRuleId() + " " + aRule.toString() + "STOP" + aRule.getIsStoppingRule());
                }
            }
            
            Main.workbench.setRuleSet(Main.KB);
            
            dialogUserList.setCurrentIndex(-1);
            processorList = getPreAndPostProcessorActionsList();
            initialGreeting = SqliteOperation.getInitialDialogGreeting();
            initialShortGreeting = SqliteOperation.getInitialShortDialogGreeting();
            referenceDatabaseDetails = cmcrdr.sqlite.SqliteOperation.getReferenceDatabaseDetails();
            

            //Logger.info("CREATING USER INTERFACE CONTROLLER");
            UserInterfaceController.execute();
            success = true;

            
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            ex.printStackTrace();
            success = false;
        }
        
        return success;
    }
    
    /**
     *
     */
    public  void reset() {      
        //DPH 2016
       // for (Workbench bench: Main.getWorkbenchList()) {
         //   bench.clearStackedInferenceResult();
        //}
        
        dialogUserList.clearAllStackedInferenceResult();
        
        //DPH 2016 clear all user dialogSet repositories
        Iterator iter = dialogUserList.getBaseSet().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry)iter.next();
            int userId = (int)me.getKey();
            DialogUser aDialogUser = (DialogUser)me.getValue();
            aDialogUser.getDialogRepository().clearRepository();
            }
        
        this.dicConverter.clearConverter();
    }
    

    public static DialogUserList getDialogUserList() {
        return dialogUserList;
    }
    

    public static void setDialogUserList(DialogUserList list) {
        dialogUserList = list;
    }
    

    public static void setGlobalContextVariables(LinkedHashMap<String,ContextVariable> vars) {
        globalContextVariableManager.setContextVariables(vars);
    }
    

    public static void populateWindowsMenu(JMenu theMenu) {
        
        theMenu.removeAll();
        
        Iterator iter = windowList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            String theWindowName = (String)me.getKey();
            JFrame theWindow = (JFrame)me.getValue();
            
            JMenuItem windowItem = new JMenuItem(theWindowName);
            windowItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JFrame)windowList.get(((JMenuItem)e.getSource()).getText())).toFront();
                    ((JFrame)windowList.get(((JMenuItem)e.getSource()).getText())).requestFocus();

                }
            });
            
            theMenu.add(windowItem);
        }     
    }
    

    public static void addToWindowsList(JFrame aFrame) {
        String title = aFrame.getTitle();
        if (!windowList.containsKey(title))
            windowList.put(title, aFrame);
    }
    

    public static void removeFromWindowsList(JFrame aFrame) {
        String title = aFrame.getTitle();
        if (windowList.containsKey(title))
            windowList.remove(title);
    }
    
    public static void updateInitialGreeting(String greeting) {
        initialGreeting = greeting;
        SqliteOperation.updateInitialGreeting(greeting);
    }
    
    public static void updateInitialShortGreeting(String greeting) {
        initialShortGreeting = greeting;
        SqliteOperation.updateInitialShortGreeting(greeting);
    }
    
    public static String getInitialGreeting() {
        return initialGreeting;
    }
    
    public static String getInitialShortGreeting() {
        return initialShortGreeting;
    }
    
    public static HashMap<String,String> getReferenceDatabaseDetails() {
        return DialogMain.referenceDatabaseDetails;
    }

    public static boolean setReferenceDatabaseDetails(HashMap<String,String> settings, boolean save) {
        //Logger.info("Setting reference database details..");
        DialogMain.referenceDatabaseDetails = settings;
        
        //for (Map.Entry me : DialogMain.referenceDatabaseDetails.entrySet()) {
            //String item = (String)me.getKey();
            //String value = (String)me.getValue();
            //Logger.info("Found:" + item + " with value: " + value);
        //}
        
        if (save)
            cmcrdr.sqlite.SqliteOperation.setReferenceDatabaseDetails(DialogMain.referenceDatabaseDetails);
        
        return DBConnection.connect(DialogMain.referenceDatabaseDetails, true);
    }
    
    public static void addUserInterface(UserInterface anInterface) {
        Logger.info("Adding a user interface");
        userInterfaces.add(anInterface);
    }
    
    public static void removeUserInterface(UserInterface anInterface) {
        if (userInterfaces.contains(anInterface))
        userInterfaces.remove(anInterface);
    }
    
    public static void updateInterfacesContent(String user) {
        for (UserInterface anInterface: userInterfaces) {
            anInterface.updateInterfaceContent(user);
        }
    }
    
    public static void loadRobotCapabilities(String path) {
        robotCapabilities = getTransrotationCapabilities(path);
    }
    
    public static Transrotations getRobotCapabilities() {
        return robotCapabilities;
    }
}

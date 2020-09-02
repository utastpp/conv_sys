/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.webinterface;
import cmcrdr.gui.UserInterface;
import cmcrdr.gui.ManualDialogGUI;
import cmcrdr.cases.DialogCase;
import cmcrdr.dialog.DialogArchiveModule;
import static cmcrdr.dialog.DialogArchiveModule.archiveDialogString;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.dialog.IDialogInstance;
import cmcrdr.dialog.SystemDialogInstance;
import cmcrdr.dialog.UserDialogInstance;
import cmcrdr.event.EventInstance;
import cmcrdr.external.ExternalExecutor;
import cmcrdr.inference.InferenceExecutor;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.main.DialogMainTimer;
import cmcrdr.mysql.DBConnection;
import cmcrdr.processor.PreAndPostProcessorAction;
import cmcrdr.responses.SystemResponse;
import cmcrdr.sqlite.SqliteOperation;
import cmcrdr.user.DialogUser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListModel;
import rdr.apps.Main;
import rdr.rules.RuleSet;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.dialog.DialogSet;
import cmcrdr.dic.DicManager;
import java.util.Arrays;
import java.util.Map;
import cmcrdr.command.ICommandInstance;
import cmcrdr.handler.OutputParser;
import cmcrdr.user.DialogUserList;

/**
 *
 * @author David Herbert, david.herbert@utas.edu.au
 */
public class UserInterfaceController  {
   
    private DefaultListModel usersListModel = new DefaultListModel();
    private DialogCase newCase;
    private ArrayList<ManualDialogGUI> manualUsersGUIList = new ArrayList<>();
    public DefaultListModel preAndPostProcessActionListModel = new DefaultListModel();

    private ArrayList<String> schemaDictionaryTermNames = new ArrayList<>();
    private ArrayList<String> selectedSchemaColumnNames = new ArrayList<>();
    private ArrayList<String> convertedSchemaColumnNames = new ArrayList<>();
    private HashMap<String,String> schemaColumnNameMappings = new HashMap<>();

    

    /**
     *
     */
    public final static Object syncLock = new Object();
    private static IDialogInstance selectedContextDialog;
    

    public UserInterfaceController() {
                
        //Logger.info("Setting KB ruleset");
        Main.workbench.setRuleSet(Main.KB);
        HashMap <String,String> settings = cmcrdr.sqlite.SqliteOperation.getReferenceDatabaseDetails();
        DialogMain.setReferenceDatabaseDetails(settings,true);
        //Logger.info("Setting referential database details:");
        //for (String aSetting: settings.values()) {
            //Logger.info("Setting:" + aSetting);
        //}

        //Logger.info("Setting saved query and conclusion details..");
        DialogMain.savedQueryTemplateList = SqliteOperation.getSavedQueryTemplateDetails();
        DialogMain.conclusionQueryList = SqliteOperation.getConclusionQueryDetails();
        
        // Check for user inactivity and remove them if they are idle for 60 mins or more..
        TimerTask timerTask = new DialogMainTimer(this);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0,60000);

        // See if there are speech pre/post actions
        for (int anActionIndex = 0; anActionIndex < DialogMain.processorList.size(); anActionIndex++) {
          preAndPostProcessActionListModel.addElement((PreAndPostProcessorAction)DialogMain.processorList.get(anActionIndex).copy());
        }
        
        String[] fields = DialogMain.getReferenceDatabaseDetails().get("databaseSelectedFields").split(",");
        selectedSchemaColumnNames =  new ArrayList<>(Arrays.asList(fields));
      
        //Logger.info("Checking reference database (if used)..");
        if (DBConnection.getIsDatabaseUsed()) {
            if (DBConnection.connect(DialogMain.referenceDatabaseDetails, true)) {
                Logger.info("Connection established to reference database..");
               
                
                for (String aColumn: selectedSchemaColumnNames) {                   
                    String convertedName = getSchemaCamelCaseName(aColumn);
                    convertedSchemaColumnNames.add(convertedName);
                    schemaColumnNameMappings.put(convertedName,aColumn);
                    addSchemaGeneratedDictionaryTerm(convertedName, "REPLACE-ME");
                    addSchemaGeneratedContextVariable("@" + convertedName);                    
                    schemaDictionaryTermNames.add(convertedName);
                    //if (!DialogMain.globalContextVariableManager.isUserVariableSet(convertedName)) {
                        //Logger.info("The user variable: " + convertedName + " is not currently set!");
                    //}
                }
                
                // we want to create associated dictionary representative terms for each camelCase column name
                //for (String aTerm: schemaDictionaryTermNames) {                    
                    //Logger.info("Found the following camelCase term:" + aTerm);
                //}
                
                
            }
        }
        else
            Logger.info("Reference database not used..");

        
        
        // a test insertion to the internal slotFiller database..
        //SqliteOperation.updateCurrentSlotInformation("david.herbert@utas.edu.au", "booking.Destination City", "Melbourne");
        
        
       //Logger.info("Getting dictionary values..");

        SqliteOperation.getMatchingTermList();
    }
    
    public String[] getConvertedSchemaColumnNamesAsStringArray() {
        String[] theList = new String[convertedSchemaColumnNames.size()];
        theList = convertedSchemaColumnNames.toArray(theList);
        return theList;
    }
    
    private String getSchemaCamelCaseName(String aSentence) {
        //Logger.info("Found the following reference column:" + aSentence);
        String aCamelCaseTerm = "";
        int count = 0;
        for (String aWord: aSentence.split(" ")) {
            if (count == 0)
                aCamelCaseTerm = "schema" + aWord.substring(0,1).toUpperCase() + aWord.substring(1);
            else
                aCamelCaseTerm += aWord.substring(0,1).toUpperCase() + aWord.substring(1);
            count++;
        } 
        return aCamelCaseTerm;
    }
    
    private void addSchemaGeneratedDictionaryTerm(String representativeString, String matchingTerm) {
        DicManager aDicManager = new DicManager();
        aDicManager.setCurrentDic(DialogMain.dictionary);
        String dictionaryRepTerm = "/" + representativeString + "/";

        if(!representativeString.equals("")){    
            if(aDicManager.isValidRepresentative(dictionaryRepTerm)){
                if (!DialogMain.dictionary.isDicTermExist(dictionaryRepTerm)) {
                        aDicManager.addMatchingTerm(dictionaryRepTerm,matchingTerm.toLowerCase(),true);
                        //Logger.info("Dictionary matching term: '" + matchingTerm.toLowerCase() + "' added for term: '" + dictionaryRepTerm + "'");
                }                
            }
        }
    }
    
    private void addSchemaGeneratedContextVariable(String representativeString) {
        ContextVariable aVariable = new ContextVariable();
        Logger.info("Generating schema-backed variable :" + representativeString);
        aVariable.setVariableName(representativeString);
        aVariable.addVariableValue("/" + representativeString + "/");
        if (!DialogMain.globalContextVariableManager.variableExists(aVariable)) {
            DialogMain.globalContextVariableManager.addContextVariable(aVariable);
        }
        
    }
    
    public String getSchemaOriginalColumnName(String convertedName) {
        String value = "";
        if (schemaColumnNameMappings.containsKey(convertedName))
            value = schemaColumnNameMappings.get(convertedName);
        
        return value;
    }
    
    
    public void setSelectedSchemaColumnNames(ArrayList<String> names) {
        selectedSchemaColumnNames = names;
    }
    
    public ArrayList<String> getSelectedSchemaColumnNames() {
        return selectedSchemaColumnNames;
    }
    
    private void registerGlobalAdminInterface() {
        DialogMain.userInterfaceController = this;
    }
    

    /**
     *
     * @return
     */
    public int getNumberOfUsers() {
        return DialogMain.getDialogUserList().getBaseSet().size();
    }
    
    public ArrayList<String> getListOfUsers() {
        ArrayList<String> userNameList = new ArrayList<>();
        DialogUserList dialogUserList = DialogMain.getDialogUserList();
        for (Map.Entry me: dialogUserList.getBaseSet().entrySet()) {
            DialogUser aDialogUser = (DialogUser)me.getValue();
            userNameList.add(aDialogUser.getUsername());
        }
        return userNameList;
    }
    
    /**
     *
     * @param user
     * @return
     */
    public boolean userExists(String user) {
        //return userList.contains(user);
        return DialogMain.getDialogUserList().existsUser(user);
    }
    
    /**
     *
     * @param user
     * @param sourceType
     */
    public void addUser(String user,DialogUser.UserSourceType sourceType) {
        int userIndex;
        
        synchronized(syncLock) {
        
            if (!userExists(user))
            {
                //userList.add(user);
                DialogMain.getDialogUserList().addUser(user, sourceType);

                userIndex = getUserId(user);
                
                // If we have any other user interfaces that require user addition, do it here..
                for (UserInterface aUserInterface: DialogMain.userInterfaces) {
                    aUserInterface.addUser(user);
                }

                try {
                    DialogArchiveModule.createTextFileForDialogHistory(user);
                } catch (Exception ex) {
                    Logger.info("User dialog history: " + ex.getMessage());
                }

            }
        }
    }
    
    /**
     *
     * @param user
     */
    public  void removeUser(String user) {
        synchronized(syncLock) {
            if (userExists(user))
            {
               int dialogUserIndex = getUserId(user);
               DialogMain.getDialogUserList().removeUser(dialogUserIndex);
               
                // If we have any other user interfaces that require user addition, do it here..
                for (UserInterface aUserInterface: DialogMain.userInterfaces) {
                    aUserInterface.removeUser(user);
                }
            }                    
        }
    }
    
    /**
     *
     * @param user
     * @return
     */
    public int getUserId(String user) {
        return DialogMain.getDialogUserList().getIndexFromUsername(user); 
    }
    

    
    
    
    /**
     *
     * @param user
     * @param message
     * @param messageSource
     * @param systemQuery
     * @return 
     */
    public String setIncomingMessage(String user, String message, DialogUser.UserSourceType messageSource) {
        
        message = message.replace("?", "");
        
        int i = 0;
        if (userExists(user)) {
            i = DialogMain.getDialogUserList().getIndexFromUsername(user);                 
        }
        else {
            addUser(user,messageSource);
            i = DialogMain.getDialogUserList().getIndexFromUsername(user);
        } 
        String systemReplyString = "";
        
        synchronized (syncLock) {
        
            DialogMain.getDialogUserList().setCurrentIndex(i);
            DialogMain.getDialogUserList().getDialogUser(i).resetTimeout();

            // *********************
            // Main inference request occurs here
            SystemResponse systemResponse = processInput(message);
            processOutput(systemResponse);
            // *********************
                    
            SystemDialogInstance systemReply = (SystemDialogInstance)DialogMain.getDialogUserList().getCurrentDialogRepository().getMostRecentDialogInstance();    
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Calendar cal = Calendar.getInstance(); 
            
            int dialogID = systemReply.getDialogId();

            systemReplyString = systemReply.getParsedDialogStr();
            systemReplyString = OutputParser.parseAutonomousSystemTerms(systemReplyString, false);


            try {
                //archiveDialogString(DialogMain.currentUser, DialogInstance.SYSTEM_TYPE, systemReplyString);
                archiveDialogString(DialogMain.getDialogUserList().getDialogUser(i).getUsername(), dialogID, DialogInstance.SYSTEM_TYPE, systemReplyString);
            } catch (Exception ex) {
                Logger.info("Problem saving current dialog: " + ex.getMessage());
            }

                     
            DialogMain.getDialogUserList().getCurrentDialogUser().appendToConversationHistory(((UserDialogInstance)DialogMain.getDialogUserList().getCurrentDialogRepository().getMostRecentUserDialogInstance()).toConsoleString());
            DialogMain.getDialogUserList().getCurrentDialogUser().appendToConversationHistory(((SystemDialogInstance)DialogMain.getDialogUserList().getCurrentDialogRepository().getMostRecentSystemDialogInstance()).getParsedDialogStr()+"\n");

            // we're guaranteed to have at least one user by now..
            // Set the initial conversation history display after we've received the first message.
            if (getNumberOfUsers() == 1) {
                DialogMain.getDialogUserList().setCurrentIndex(DialogMain.getDialogUserList().getFirstDialogUser().getUserId());
            }
        }
        
        DialogMain.updateInterfacesContent(user);
        
        return systemReplyString;
    }
    

    
    public String getDialogHistory(String user) {
        
        int i = 0;
        if (userExists(user)) {
            i = DialogMain.getDialogUserList().getIndexFromUsername(user);                 
        }
        else {
            addUser(user,DialogUser.UserSourceType.WEB);
            i = DialogMain.getDialogUserList().getIndexFromUsername(user);
        } 
        String systemReplyString = "";
        
        synchronized (syncLock) {
        
            DialogMain.getDialogUserList().setCurrentIndex(i);
            DialogMain.getDialogUserList().getDialogUser(i).resetTimeout();

            return DialogMain.getDialogUserList().getCurrentDialogUser().getConversationHistory();    
        }   
    }
    

    
    /**
     *
     * @param user
     */
    public void removeUserSessionHasExpired(String user) {
        Logger.info("Removing user " + user + " as their session has expired..");
        removeUser(user);
    }
    
    public void clearAllDialogResults() {
        DialogMain.getDialogUserList().clearAllStackedInferenceResult();

        for (DialogSet dialogRepo: DialogMain.getDialogUserList().getAllDialogRepositories()) {
            dialogRepo.clearRepository();
        }
        DialogMain.dicConverter.clearConverter();
    }
    

    
     private SystemResponse processInputEvent(String eventType, String eventValue) {
        Logger.info("Processing event: " + eventType.toString() + " with value " + eventValue.toString());
        DialogMain.getDialogUserList().getCurrentDialogInputHandler().setRecentStringInput("");
        EventInstance e = new EventInstance();
        e.setEventType(eventType);
        e.setEventValue(eventValue);
        DialogMain.getDialogUserList().getCurrentDialogInputHandler().setRecentEventInput(e);
        
        newCase = DialogMain.getDialogUserList().getCurrentDialogInputHandler().processInput();
        
        SystemResponse systemResponse = InferenceExecutor.requestResponse(newCase);
        
        return systemResponse;              
    }
    
    /**
     *
     * @param recentUserInputString
     * @return
     */
    public SystemResponse processInput(String recentUserInputString) {
        String userInput = recentUserInputString;
        // Step through pre-processing actions..
        //Logger.info("Raw input:" + userInput);
       // for (PreAndPostProcessorAction aPreprocessorAction: DialogMain.processorList) {
           // Logger.info("Preprocess input:" + userInput + ", Match:[" + aPreprocessorAction.getMatchText() + "], Replace:[" + aPreprocessorAction.getReplaceText() +"]" );
           // userInput = preAndPostProcessInput(userInput, aPreprocessorAction);
        //}
       // Logger.info("Post processing:" + userInput);
        //recentUserInputString = recentUserInputString.toLowerCase();           
        
        // String taggerModel =  System.getProperty("user.dir") + "/taggers/english-left3words-distsim.tagger";
        //String tagger =  System.getProperty("user.dir") + "/taggers/english-bidirectional-distsim.tagger";      
        //MaxentTagger tagger = new MaxentTagger(taggerModel);      
        //String tagged = tagger.tagTokenizedString(recentUserInputString);
        
        //Logger.info("Currently processing inference request for user:" + DialogMain.getDialogUserList().getCurrentUsername());

        DialogMain.getDialogUserList().getCurrentDialogInputHandler().setRecentStringInput(userInput);
        // DPH get rid of any previous events..
        DialogMain.getDialogUserList().getCurrentDialogInputHandler().setRecentEventInput(new EventInstance());

        //DialogMain.inputHandler.setRecentStringInput(tagged);
        newCase = DialogMain.getDialogUserList().getCurrentDialogInputHandler().processInput();
        //Logger.info("Inference request for case: " + newCase.toString());

        SystemResponse systemResponse = InferenceExecutor.requestResponse(newCase);
        
        
         
        //Logger.info("Inference response: " + systemResponse.getResponseDialogInstance().getDialogStr());       
        return systemResponse;
        
    }
    
    /**
     *
     * @param systemResponse
     */
    public void processOutput(SystemResponse systemResponse){
        //Logger.info("Output handler case is: " + newCase.toString());

        DialogMain.getDialogUserList().getCurrentDialogOutputHandler().setDialogCase(newCase);

        
        // See if there are any command actions to execute
        // DPH May 2019 we need to iterate here as in MCRDR there may be multiple events firing included in the one all-encompassing system response..
        
        ArrayList<ICommandInstance> responseCommandInstances = systemResponse.getResponseCommandInstanceList();
        for (ICommandInstance aCommand : responseCommandInstances) {
            DialogMain.externalExecutor.setOutputMode(ExternalExecutor.JAVA_OUTPUT);
            DialogMain.externalExecutor.setCommand(aCommand);
            EventInstance resultEvent = null;
            if (aCommand.isSet()) {
                Logger.info("Firing event action: " + aCommand.toString());
                Logger.info("About to execute...");
                resultEvent = DialogMain.externalExecutor.execute();
                Logger.info("Finished execute...");
            }
            
            if (resultEvent != null)
            {
                String eventResultString = resultEvent.getEventValue();
                SystemDialogInstance sdi = systemResponse.getResponseDialogInstance();

                String currentSystemReply = sdi.getDialogStr();
                sdi.setDialogStr(currentSystemReply + " " + eventResultString );
            }
        }
        
        /*DialogMain.externalExecutor.setOutputMode(ExternalExecutor.JAVA_OUTPUT);
        DialogMain.externalExecutor.setCommand(systemResponse.getResponseCommandInstance());
        EventInstance resultEvent = null;
        if (systemResponse.getResponseCommandInstance().isSet()) {
            Logger.info("Firing event action: " + systemResponse.getResponseCommandInstance().toString());
            Logger.info("About to execute...");
            resultEvent = DialogMain.externalExecutor.execute();
            Logger.info("Finished execute...");
        }
        */
  
        // Re-interpreting original event types here - response from a command instance might simply be a system reply to be included 
        // in response, so let's use it.
        
        /*
        if (resultEvent != null)
        {
            String eventResultString = resultEvent.getEventValue();
            SystemDialogInstance sdi = systemResponse.getResponseDialogInstance();
            
            String currentSystemReply = sdi.getDialogStr();
            sdi.setDialogStr(currentSystemReply + " " + eventResultString );
        }
        */
        
        DialogMain.getDialogUserList().getCurrentDialogOutputHandler().setSystemResponse(systemResponse);
        String[] output = DialogMain.getDialogUserList().getCurrentDialogOutputHandler().processOutput();
        
        /* This is the proper event code - present a new case for inference, ignoring for "flightBooking" system. Nov 2017
        
        if(resultEvent != null){
            DialogMain.getDialogUserList().getCurrentDialogInputHandler().clearRecentStringInput();
            DialogMain.getDialogUserList().getCurrentDialogInputHandler().setRecentEventInput(resultEvent);
            newCase = DialogMain.getDialogUserList().getCurrentDialogInputHandler().processInput();
  
            SystemResponse newSystemResponse = InferenceExecutor.requestResponse(newCase);
            processOutput(newSystemResponse);
    
        }*/
        
        
        
    }
       
    
    private RuleSet getLastInferencedRuleSet(int stackId, String user) {
        RuleSet selectedInferenceResult = null;
    
        if (!user.isEmpty()) {       
            selectedInferenceResult = DialogMain.getDialogUserList().getDialogUser(user).getMCRDRStackResultSet().getMCRDRStackResultInstanceById(stackId).getInferenceResult();
        }
        return selectedInferenceResult;
    }
    

    public String preProcessInputAction(String input, PreAndPostProcessorAction aPreprocessAction) {
        if (!aPreprocessAction.getPostOption()) {
            String result = preAndPostProcessInput(false, input, aPreprocessAction.getMatchText(), aPreprocessAction.getReplaceText(), aPreprocessAction.getRegex(), aPreprocessAction.getWordOnly(), 
                    aPreprocessAction.getStartOfInput(), aPreprocessAction.getEndOfInput(), aPreprocessAction.getReplaceOption(), aPreprocessAction.getUpperOption(), 
                    aPreprocessAction.getLowerOption(), aPreprocessAction.getTrimOption(), aPreprocessAction.getPostOption());
            return result;
        }
        return input;
    }
    
    public String postProcessInputAction(String input, PreAndPostProcessorAction aPreprocessAction) {
        if (aPreprocessAction.getPostOption()) {
            String result = preAndPostProcessInput(false, input, aPreprocessAction.getMatchText(), aPreprocessAction.getReplaceText(), aPreprocessAction.getRegex(), aPreprocessAction.getWordOnly(), 
                    aPreprocessAction.getStartOfInput(), aPreprocessAction.getEndOfInput(), aPreprocessAction.getReplaceOption(), aPreprocessAction.getUpperOption(), 
                    aPreprocessAction.getLowerOption(), aPreprocessAction.getTrimOption(), aPreprocessAction.getPostOption());
            return result;
        }
        return input;
    }
    
    public String preAndPostProcessInput(boolean preview, String input, String matchText, String replaceText, boolean regex, 
                                    boolean wordOnly, boolean startInput, 
                                    boolean endInput, boolean replace, boolean upper, 
                                    boolean lower, boolean trim, boolean post) throws PatternSyntaxException,  IndexOutOfBoundsException {
        String result = input;
        String regularExpression = matchText;
        String prefix = "";
        String postfix = "";
        
        
            if (!regex) {
                regularExpression = Pattern.quote(regularExpression);
            }           
            
            if (wordOnly) {
                prefix = "\\b";
                postfix = "\\b";
            }
            
            if (startInput)
                prefix = "^" + prefix;
            
            if (endInput)
                postfix += "$";
            
            if (upper)
                result = result.toUpperCase();
            
            if (lower)
                result = result.toLowerCase();
            
            if (trim)
                result = result.trim();
            
            if (replace)
                //try {
                    result = result.replaceAll(prefix + regularExpression + postfix, replaceText);
                //}
                //catch (PatternSyntaxException | IndexOutOfBoundsException p) {
                    
                //}
        
        return result;
    }
    
   

    /**
     *
     */
    public static void execute() {       
        /* Create and display the form */
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Logger.info("DEBUG about to create user interface controller...");

                UserInterfaceController controller = new UserInterfaceController();
                //Logger.info("Setting dialogMain headless");
               // Logger.info("DEBUG Finished creating user interface controller...");
                DialogMain.userInterfaceController = controller;
            }
        });
    }
    
    private int getSpawnedUserIndex(ManualDialogGUI user) {
        return manualUsersGUIList.indexOf(user);
    }
    
    public String initiateKnowledgeAcquisitionFromWeb(String sessionID) {
        return "OK";
    }

  
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                UserInterfaceController controller = new UserInterfaceController();
                DialogMain.userInterfaceController = controller;
            }
        });
    }
}

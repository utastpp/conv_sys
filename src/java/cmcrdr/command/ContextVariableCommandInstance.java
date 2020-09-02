/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;

/**
 *
 * @author dherbert
 */
public class ContextVariableCommandInstance  extends CommandInstance {
    public static final String SetContextVariable = "SetContextVariable";
    public final static String UnsetContextVariable = "UnsetContextVariable";
    public final static String SetParsedDateContextVariable = "SetParsedDateContextVariable";
    public final static String SetGlobalContextVariable = "SetGlobalContextVariable";
    
    private final String[] deviceActionList = new String[]{SetContextVariable,SetGlobalContextVariable,SetParsedDateContextVariable,UnsetContextVariable};
    
    public ContextVariableCommandInstance(){
        super(CONTEXT_VARIABLE_ID);
    }
    
    public void SetContextVariable() {
        String contextVarName = "";
        String valueContextVarName = "";
        String finalValue = "";
        String sourceForContextVarValue = "";
        boolean variableValueDefined = false;
        
        String[] variableDataList= this.getDeviceActionVariableList();
        
        
        //for (String aVariableData: variableDataList) {
          // Logger.info("Found variable data:" + aVariableData);
        //}
        
        if (variableDataList.length == 2) {
            contextVarName = variableDataList[0];
            sourceForContextVarValue = variableDataList[1];
            
            Logger.info("I have been called to set variable " + contextVarName + " with value " + sourceForContextVarValue);
            
            if (sourceForContextVarValue.startsWith("@")) {
                //valueContextVarName = sourceForContextVarValue.substring(1);
                valueContextVarName = sourceForContextVarValue;
                //Logger.info("Looking for the stackID variable " + valueContextVarName + " in order to set " + contextVarName);
                if (DialogMain.globalContextVariableManager.isUserVariableSet(valueContextVarName))
                {
                    // setting the var's value to the value of another stackID variable
                    ContextVariableUser cv = DialogMain.getDialogUserList().getCurrentContextVariables().get(valueContextVarName);
                    finalValue = cv.getValue();
                    variableValueDefined = true;
                    //Logger.info("The variable " + valueContextVarName + " has been found with value " + finalValue);
                }
                else { // this shouldn't happen..
                    finalValue = "ERROR";
                    variableValueDefined = false;
                    Logger.info("The variable " + valueContextVarName + " could not be found!");

                }
            }
            else { // setting the var's value to the literal value
                finalValue = sourceForContextVarValue;
                variableValueDefined = true;
            }
            
            if (variableValueDefined) {
                if (!DialogMain.globalContextVariableManager.isUserVariableSet(contextVarName)) {
                    Logger.info("User variable '" + contextVarName + "' is not currently set!");
                    int stackID = DialogMain.getDialogUserList().getCurrentStackLevel();

                    ContextVariableUser cvu = new ContextVariableUser(contextVarName, finalValue, stackID);

                    LinkedHashMap<String, ContextVariableUser> newVariableList = new LinkedHashMap<>();
                    newVariableList.put(contextVarName, cvu);

                    DialogMain.globalContextVariableManager.setContextVariablesForSpecificContext(newVariableList,stackID);
                }
                else {
                    int context = DialogMain.getDialogUserList().getCurrentStackLevel();
                    ContextVariableUser cvu = DialogMain.getDialogUserList().getCurrentContextVariables().get(contextVarName);
                    cvu.setVariableValue(finalValue);
                    cvu.setContext(context);
                } 
            }
            else {
                Logger.info("Tried to set a variable - " + contextVarName + " but its value was undefined..");
            }
        }
   } 
   
    public void UnsetContextVariable() {
        String contextVarName = "";
    
        String[] variableDataList= this.getDeviceActionVariableList();
        
        //for (String aVariableData: variableDataList) {
            //Logger.info("Found variable data:" + aVariableData);
        //}
        
        if (variableDataList.length == 1) {
            contextVarName = variableDataList[0];
            if (DialogMain.globalContextVariableManager.isUserVariableSet(contextVarName)) {
                DialogMain.getDialogUserList().getCurrentDialogUser().unsetContextVariable(contextVarName);
                Logger.info("I have unset variable " + contextVarName);
            }
        }
        
   } 
    
    // variable is defined to exist from root stack frame, so all frames inherit
    public void SetGlobalContextVariable() {
        String contextVarName = "";
        String valueContextVarName = "";
        String finalValue = "";
        String sourceForContextVarValue = "";
        boolean variableValueDefined = false;
        
        String[] variableDataList= this.getDeviceActionVariableList();
        
        
        //for (String aVariableData: variableDataList) {
          // Logger.info("Found variable data:" + aVariableData);
        //}
        
        if (variableDataList.length == 2) {
            contextVarName = variableDataList[0];
            sourceForContextVarValue = variableDataList[1];
            
            Logger.info("I have been called to set global variable " + contextVarName + " with value " + sourceForContextVarValue);
            
            if (sourceForContextVarValue.startsWith("@")) {
                //valueContextVarName = sourceForContextVarValue.substring(1);
                valueContextVarName = sourceForContextVarValue;
                //Logger.info("Looking for the stackID variable " + valueContextVarName + " in order to set " + contextVarName);
                if (DialogMain.globalContextVariableManager.isUserVariableSet(valueContextVarName))
                {
                    // setting the var's value to the value of another stackID variable
                    ContextVariableUser cv = DialogMain.getDialogUserList().getCurrentContextVariables().get(valueContextVarName);
                    finalValue = cv.getValue();
                    variableValueDefined = true;
                    //Logger.info("The variable " + valueContextVarName + " has been found with value " + finalValue);
                }
                else { // this shouldn't happen..
                    finalValue = "ERROR";
                    variableValueDefined = false;
                    Logger.info("The variable " + valueContextVarName + " cound not be found!");

                }
            }
            else { // setting the var's value to the literal value
                finalValue = sourceForContextVarValue;
                variableValueDefined = true;
            }
            
            if (variableValueDefined) {
                if (!DialogMain.globalContextVariableManager.isUserVariableSet(contextVarName)) {
                    //Logger.info("User variable '" + contextVarName + "' is not currently set!");
                    int stackID = 0;  // we are defining for root..

                    ContextVariableUser cvu = new ContextVariableUser(contextVarName, finalValue, stackID);

                    LinkedHashMap<String, ContextVariableUser> newVariableList = new LinkedHashMap<>();
                    newVariableList.put(contextVarName, cvu);

                    DialogMain.globalContextVariableManager.setContextVariablesForSpecificContext(newVariableList,stackID);
                }
                else {
                    int context = 0; // we are defining for root..
                    ContextVariableUser cvu = DialogMain.getDialogUserList().getCurrentContextVariables().get(contextVarName);
                    cvu.setVariableValue(finalValue);
                    cvu.setContext(context);
                } 
            }
            else {
                Logger.info("Tried to set a variable - " + contextVarName + " but its value was undefined..");
            }
        }
   } 
    
    
   public void SetParsedDateContextVariable() {
        String[] variableDataList= this.getDeviceActionVariableList();
        String contextVarName = "";
        String sourceForContextVarValue = "";
        String valueContextVarName = "";
        String valueToParse = "";
        String parsedResult = "";
        boolean variableValueDefined = false;
        
        
        if (variableDataList.length == 2) {
            contextVarName = variableDataList[0];
            sourceForContextVarValue = variableDataList[1];
            if (sourceForContextVarValue.startsWith("@")) {
                //valueContextVarName = sourceForContextVarValue.substring(1);
                valueContextVarName = sourceForContextVarValue;
                if (DialogMain.globalContextVariableManager.isUserVariableSet(valueContextVarName))
                {
                    // setting the var's value to the value of another stackID variable
                    ContextVariableUser cv = DialogMain.getDialogUserList().getCurrentContextVariables().get(valueContextVarName);
                    valueToParse = cv.getValue();
                    parsedResult = parseDateValue(valueToParse);
                    variableValueDefined = !parsedResult.isEmpty();
                }
            }
            else {
                // we're dealing with a literal value instead of a variable
                
                // setting the var's value to the value of the literal
                valueToParse = sourceForContextVarValue;
                parsedResult = parseDateValue(valueToParse);
                variableValueDefined = !parsedResult.isEmpty();               
            }
            
            
            if (variableValueDefined) {
                if (!DialogMain.globalContextVariableManager.isUserVariableSet(contextVarName)) {
                    //Logger.info("User variable '" + contextVarName + "' is not currently set!");
                    int context = DialogMain.getDialogUserList().getCurrentStackLevel();

                    ContextVariableUser cvu = new ContextVariableUser(contextVarName, parsedResult, context);

                    LinkedHashMap<String, ContextVariableUser> newVariableList = new LinkedHashMap<>();
                    newVariableList.put(contextVarName, cvu);

                    DialogMain.globalContextVariableManager.setContextVariablesForSpecificContext(newVariableList,context);
                }
                else {
                    int context = DialogMain.getDialogUserList().getCurrentStackLevel();
                    ContextVariableUser cvu = DialogMain.getDialogUserList().getCurrentContextVariables().get(contextVarName);
                    cvu.setVariableValue(parsedResult);
                    cvu.setContext(context);
                } 
            }
        }      
   }
    
    private String parseDateValue(String theSource) {
        String result = "";
       
        LocalDate today = LocalDate.now();
        TemporalAdjuster adj = null;
        LocalDate nextDate = null;
        Month theMonth = null;
        boolean monthFound = false;
        boolean dayFound = false;
        boolean yearFound = false;
        
        
        // theSource should be a dictionary term for a date
        
        // test for some standard phrases to express a date from the current date
        if (theSource.toLowerCase().contains("today"))
            adj = TemporalAdjusters.ofDateAdjuster(date -> date.plusDays(0)); 
        else if (theSource.toLowerCase().contains("tomorrow"))
            adj = TemporalAdjusters.ofDateAdjuster(date -> date.plusDays(1)); 
        else if (theSource.toLowerCase().contains("the day after tomorrow"))
            adj = TemporalAdjusters.ofDateAdjuster(date -> date.plusDays(2));
        else if (theSource.toLowerCase().contains("next mon") || theSource.toLowerCase().contains("monday next week") || theSource.toLowerCase().equals("monday"))
            adj = TemporalAdjusters.next(DayOfWeek.MONDAY);
        else if (theSource.toLowerCase().contains("next tues") || theSource.toLowerCase().contains("tuesday next week") || theSource.toLowerCase().equals("tuesday"))
            adj = TemporalAdjusters.next(DayOfWeek.TUESDAY);
        else if (theSource.toLowerCase().contains("next wed") || theSource.toLowerCase().contains("wednesday next week") || theSource.toLowerCase().equals("wednesday"))
            adj = TemporalAdjusters.next(DayOfWeek.WEDNESDAY);
        else if (theSource.toLowerCase().contains("next thurs") || theSource.toLowerCase().contains("thursday next week") || theSource.toLowerCase().equals("thursday"))
            adj = TemporalAdjusters.next(DayOfWeek.THURSDAY);
        else if (theSource.toLowerCase().contains("next fri") || theSource.toLowerCase().contains("friday next week") || theSource.toLowerCase().equals("friday"))
            adj = TemporalAdjusters.next(DayOfWeek.FRIDAY);
        else if (theSource.toLowerCase().contains("next sat") || theSource.toLowerCase().contains("saturday next week") || theSource.toLowerCase().equals("saturday"))
            adj = TemporalAdjusters.next(DayOfWeek.SATURDAY);
        else if (theSource.toLowerCase().contains("next sun") || theSource.toLowerCase().contains("sunday next week") || theSource.toLowerCase().equals("sunday"))
            adj = TemporalAdjusters.next(DayOfWeek.SUNDAY);
        
        if (theSource.toLowerCase().contains("next week")) 
            adj = TemporalAdjusters.ofDateAdjuster(date -> date.plusDays(7));
        
        if (adj != null) {
            nextDate = today.with(adj);
            Logger.info("Found a phrased date, the parsed date is " + nextDate);
        }
        else { // see if the date is expressed as dd month [year] or month dd [year] . []=optional
            String [] dateTokens = theSource.toLowerCase().split(" ");
            if (dateTokens.length > 1) {
                String token1 = dateTokens[0];
                String token2 = dateTokens[1];
                
                int testMonth1 = getMonth(token1);
                int testMonth2 = getMonth(token2);
                int day = 0;
                int year = 0;
                
                if (dateTokens.length == 3) {
                    String token3 = dateTokens[2];             
                    year = getYear(token3);
                    yearFound = true;
                }
                else {
                    year = LocalDate.now().getYear();
                }
               
                
                    
                
                
                if (testMonth1 != 0) {
                    theMonth = Month.of(testMonth1);
                    day = getDay(token2,theMonth,year);
                    if (day > 0) {
                        dayFound = true;
                        if (LocalDate.of(year,theMonth, day).compareTo(today) < 0 && !yearFound){
                            year++;
                        }
                    }
                    monthFound = true;
                }
                else if (testMonth2 != 0) {
                    theMonth = Month.of(testMonth2);
                    day = getDay(token1,theMonth,year);
                    if (day > 0) {
                        dayFound = true;
                        if (LocalDate.of(year,theMonth, day).compareTo(today) < 0 && !yearFound){
                            year++;
                        }
                    }
                    monthFound = true;
                }
                
                if (monthFound && dayFound) {
                    nextDate = LocalDate.of(year, theMonth, day);
                    Logger.info("Found a simple positional date, the parsed date is " + nextDate);
                }
                
            }
            else {  // try standard date format.. yyyy-mm-dd
                try {
                    Logger.info("Trying to parse a proper date format:" +theSource);
                            
                    nextDate = LocalDate.parse(theSource);
                    Logger.info("Found a natural date, the parsed date is " + nextDate);
                }
                catch (DateTimeParseException d) {
                    Logger.info("Invalid date from: " + theSource);
                }
            }
            
        }
        
        if (nextDate != null)
            result = nextDate.toString();
        
        return result;
    }
    
    private int getMonth(String month) {
        int theMonth = 0;
        if (month.toLowerCase().contains("jan"))
            theMonth = 1;
        else if (month.toLowerCase().contains("feb"))
            theMonth = 2;
        else if (month.toLowerCase().contains("mar"))
            theMonth = 3;
        else if (month.toLowerCase().contains("apr"))
            theMonth = 4;
        else if (month.toLowerCase().contains("may"))
            theMonth = 5;
        else if (month.toLowerCase().contains("jun"))
            theMonth = 6;
        else if (month.toLowerCase().contains("jul"))
            theMonth = 7;
        else if (month.toLowerCase().contains("aug"))
            theMonth = 8;
        else if (month.toLowerCase().contains("sep"))
            theMonth = 9;
        else if (month.toLowerCase().contains("oct"))
            theMonth = 10;
        else if (month.toLowerCase().contains("nov"))
            theMonth = 11;
        else if (month.toLowerCase().contains("dec"))
            theMonth = 12;
        return theMonth;
    }
       
    private int getDay(String day, Month month, int year) {
        int theDay;
        
        try {
            theDay = Integer.parseInt(day);
            LocalDate.of(year, month, theDay);
        }
        catch (NumberFormatException | DateTimeException p) {
            theDay = 0;
        }
        
        return theDay;
    }
    
    private int getYear(String year) {
        int theYear;
        
        try {
            theYear = Integer.parseInt(year);
            if (!(theYear >= LocalDate.now().getYear() && theYear <=LocalDate.now().plusYears(4).getYear()))
                theYear = 0;
        }
        catch (NumberFormatException p) {
            theYear = 0;
        }
        
        return theYear;
    }
    
    
    @Override
    public String[] getDeviceActionList(){
        return this.deviceActionList;
    }
}

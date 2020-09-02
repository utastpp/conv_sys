/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.handler;

import java.io.IOException;
import cmcrdr.contextvariable.ContextVariable;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.dialog.DialogInstance;
import cmcrdr.gui.DatabaseQueryBuilderGUI;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.mysql.DBConnection;
import cmcrdr.mysql.DBOperation;
import static cmcrdr.responses.SystemResponseGenerator.promptUserWithPotentialValidRulesInLastContext;
import cmcrdr.savedquery.ConclusionQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.htmlcleaner.HtmlCleaner;
import rdr.apps.Main;
import rdr.reasoner.MCRDRStackResultInstance;
import rdr.reasoner.MCRDRStackResultSet;
import rdr.rules.Condition;
import rdr.rules.ConditionSet;
import rdr.rules.Rule;
import rdr.rules.RuleSet;
import cmcrdr.xml.Transrotation;
import cmcrdr.xml.Transrotations;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class OutputParser {

    public static final String META_CONTEXT_VAR_CONDITION = "ContextVars";

    public static final String METAHELP = "<METAHELP>";
    public static final String METAWHERE = "<METAWHERE>";
    public static final String METANOHELP = "<METANOHELP>";
    public static final String META_REPLACE_START = "<REPLACE>";
    public static final String META_REPLACE_EMPTY = "<REPLACE-EMPTY>";
    public static final String META_REPLACE_TEXT = "<REPLACE-OVERRIDE>";
    public static final String META_REPLACE_END = "</REPLACE>";
    public static final String META_NO_SPEAK = "#NOSPEAK";
    public static final String META_NO_SPEAK_END = "#ENDNOSPEAK";

//    public static final String ROS_FORWARD = "<ROS_FORWARD>";
//    public static final String ROS_BACKWARD = "<ROS_BACKWARD>";
//    public static final String ROS_LEFT = "<ROS_LEFT>";
//    public static final String ROS_RIGHT = "<ROS_RIGHT>";
//    
    public static int databaseResponseInstance = 0;

    /**
     *
     */
    public static final String PARSER_START_MARKER = "<";

    /**
     *
     */
    public static final String PARSER_END_MARKER = "/>";

    /**
     *
     */
    public static final String DB_REF_START_MARKER = "<";

    /**
     *
     */
    public static final String DB_REF_END_MARKER = "/>";

    /**
     *
     */
    public static final String[] OPEN_TAG = {"<", ">"};

    /**
     *
     */
    public static final String[] CLOSE_TAG = {"</", ">"};

    /**
     *
     */
    public static final String[] TAG_LIST = {"QREF", "QUERY", "TBL", "FLD", "JOI", "JVA", "MVA", "CRT", "CFI", "CCX", "CNU", "CFC", "CFN", "CNT", "FMT", "PRX", "POX", "ORD", "LIT", "LNU", "PAR", "DESC", "SEL"};

    /**
     *
     */
    public static final int DB_PLACEHOLDER_TYPE = 0;

    /**
     *
     */
    public static final int DB_DATABASE_TYPE = 1;

    /**
     *
     */
    public static final int DB_TABLE_TYPE = 2;

    /**
     *
     */
    public static final int DB_FIELD_TYPE = 3;

    /**
     *
     */
    public static final int DB_JOIN_TYPE = 4;

    /**
     *
     */
    public static final int DB_JVALUE_TYPE = 5;

    /**
     *
     */
    public static final int DB_MULTIV_JOIN_TYPE = 6;

    /**
     *
     */
    public static final int DB_CRITERION_TYPE = 7;

    /**
     *
     */
    public static final int DB_CRITERION_VALUE_FIXED_TYPE = 8;

    /**
     *
     */
    public static final int DB_CRITERION_VALUE_CONTEXT_TYPE = 9;

    /**
     *
     */
    public static final int DB_CRITERION_VALUE_NUMBER_TYPE = 10;

    /**
     *
     */
    public static final int DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE = 11;

    /**
     *
     */
    public static final int DB_CRITERION_VALUE_FIXED_NUMBER_TYPE = 12;

    /**
     *
     */
    public static final int DB_COUNT_TYPE = 13;

    /**
     *
     */
    public static final int DB_FORMAT_TYPE = 14;

    /**
     *
     */
    public static final int DB_PREFIX_TYPE = 15;

    /**
     *
     */
    public static final int DB_POSTFIX_TYPE = 16;

    /**
     *
     */
    public static final int DB_ORDER_TYPE = 17;

    /**
     *
     */
    public static final int CONTEXT_LITERAL_TYPE = 18;

    /**
     *
     */
    public static final int CONTEXT_NUMBER_TYPE = 19;

    /**
     *
     */
    public static final int DB_PARTIALMATCH_TYPE = 20;

    /**
     *
     */
    public static final int DB_DESCRIPTION_TYPE = 21;

    /**
     *
     */
    public static final int DB_SELECTOR_TYPE = 22;

    /*
    public static final String CONTEXT_LITERAL_MARKER       = "LITERAL";
    public static final String CONTEXT_LITERAL_INT_MARKER   = "LIT_INT";
     */
    /**
     *
     * @param type
     * @return
     */
    public static String getStartTag(int type) {
        return OPEN_TAG[0] + getTagKeyword(type) + OPEN_TAG[1];
    }

    /**
     *
     * @param type
     * @return
     */
    public static String getEndTag(int type) {
        return CLOSE_TAG[0] + getTagKeyword(type) + CLOSE_TAG[1];
    }

    /**
     *
     * @param value
     * @param type
     * @return
     */
    public static String getTag(String value, int type) {
        return getStartTag(type) + value + getEndTag(type);
    }

    /**
     *
     * @param type
     * @return
     */
    public static String getTagKeyword(int type) {
        return TAG_LIST[type];
    }

    /**
     *
     * @param tag
     * @param type
     * @return
     */
    public static String getTagValue(String tag, int type) {
        int startOfValue;
        int endOfValue;
        String result = "";

        //Logger.info("Tag is: " + tag + " and type is "+ getStartTag(type));
        if (!tag.equals("")) {
            startOfValue = tag.indexOf(getStartTag(type)) + getStartTag(type).length();
            endOfValue = tag.indexOf(getEndTag(type));
            //Logger.info("Tag: " + tag + " start is : " + startOfValue + " and end is: " + endOfValue);
            result = tag.substring(startOfValue, endOfValue);
        }

        return result;
    }

    // converts a comma-separated string to a list of values
    /**
     *
     * @param theItems
     * @return
     */
    public static ArrayList<String> convertMultivalueStringToList(String theItems) {
        ArrayList<String> result;
        String[] items = theItems.split(",");
        result = new ArrayList<String>(Arrays.asList(items));
        return result;
    }

    /**
     *
     * @param reference
     * @param findString
     * @return
     */
    public static int countOccurrencesOfString(String reference, String findString) {
        int lastIndex = 0;
        int count = 0;

        //Logger.info("Reference string is: " + reference);
        //Logger.info("findString is: " + findString);
        while (lastIndex != -1) {
            lastIndex = reference.indexOf(findString, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findString.length();
            }
        }
        //Logger.info("count is: " + count);

        return count;
    }

    /*
    public static String getContextTagString(int type) {
        
        switch (type) {
            case LITERAL_TYPE:
                return PARSER_START_MARKER + CONTEXT_LITERAL_MARKER;
            case LITERAL_INT_TYPE:
                return PARSER_START_MARKER + CONTEXT_LITERAL_INT_MARKER;
            default:
                return "ERROR"; // no other types supported yet..
        }
    }
     */
    /**
     *
     * @param inputString
     * @return
     */
    static public ArrayList<String> getMainDatabaseTagList(String inputString) {
        ArrayList<String> result = new ArrayList<>();
        String input = inputString;
        int find = 0;
        int next = 0;
        int start = 0;

        if (input.contains(getStartTag(DB_DATABASE_TYPE))) {
            find = input.indexOf(getStartTag(DB_DATABASE_TYPE));
            //Logger.info("Adding reference string:" + input.substring(0,find));
            result.add(input.substring(0, find));  // this is the reference string with text and placeholders..
            while (next != -1) {
                next = input.indexOf(getStartTag(DB_DATABASE_TYPE), find + 1);
                if (next != -1) {
                    result.add(input.substring(find, next));
                    //Logger.info("Adding database string:" + input.substring(find, next));
                } else {
                    result.add(input.substring(find, input.length()));
                    //Logger.info("No more: Adding database string:" + input.substring(find, input.length()));

                }
                find = next;
                //start = next;
            }
        } else {
            result.add(inputString);
        }

        return result;
    }

    /**
     *
     * @param inputString
     * @return
     */
    static public ArrayList<String> getDatabaseReferences(String inputString) {
        ArrayList<String> result = new ArrayList<>();
        String referenceString = inputString;
        result.add(inputString);  // this is the conclusion text with placeholders.

        Iterator iter = DialogMain.conclusionQueryList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            int id = (int) me.getKey();
            ConclusionQuery q = (ConclusionQuery) me.getValue();
            //Logger.info("Found stored query: " + id + " with query value:'" + q.getQuery());
        }

        // get the database queries referred to in order to parse later and add to the list
        while (referenceString.contains(getStartTag(DB_PLACEHOLDER_TYPE))) {
            //Logger.info("referenceString is:" + referenceString);

            String placeholderTag = getNextTag(referenceString, DB_PLACEHOLDER_TYPE);
            String tempPlaceholderTag = placeholderTag;
            String descriptionTag = getNextTag(tempPlaceholderTag, DB_DESCRIPTION_TYPE);
            tempPlaceholderTag = removeTag(tempPlaceholderTag, descriptionTag);
            int queryId = Integer.parseInt(getTagValue(tempPlaceholderTag, DB_PLACEHOLDER_TYPE));
            //Logger.info("placeholderTag is: " + placeholderTag);
            //Logger.info("descriptionTag is: " + descriptionTag);
            //Logger.info("Trying to fetch query: " + queryId);
            result.add(DialogMain.conclusionQueryList.get(queryId).getQuery());
            referenceString = removeTag(referenceString, placeholderTag);
            //Logger.info("ReferenceString after tag removal is:" + referenceString);
        }
        return result;
    }

    /**
     *
     * @param inputString
     * @param preview
     * @return
     */
    static public String replaceAllDatabaseTerms(String inputString, boolean preview) {
        //ArrayList<String> textListToParse = OutputParser.getMainDatabaseTagList(inputString);
        ArrayList<String> textListToParse = getDatabaseReferences(inputString);
        String referenceString = textListToParse.get(0);  // bulk of the conclusion
        String queryResult;
        int count = 0;
        for (String item : textListToParse) { // database queries
            if (count != 0) { // skip over first item, it's the reference string to have placeholders replaced..
                //Logger.info("PLACEHOLDER TEXT: " + referenceString + " WITH DATABASE TERMS:" + item);
                queryResult = OutputParser.replaceSingleDatabaseQuery(referenceString, item, preview);
                // Logger.info("PLACEHOLDER RESULT IS: " + queryResult);
                referenceString = queryResult;
                //result += queryResult;
            }
            count++;
        }
        //Logger.info("Returning the following: " + referenceString);
        return referenceString;
    }

    // if using a reference database, do the database lookup using referenced marker terms, and replace DB_PLACEHOLDER_TYPE with the database result.
    /**
     *
     * @param referenceString
     * @param databaseString
     * @param preview
     * @return
     */
    static public String replaceSingleDatabaseQuery(String referenceString, String databaseString, boolean preview) {
        String result = databaseString;

        Logger.info("MY REFERENCE STRING IS: " + referenceString + " AND QUERY IS: " + databaseString);

        if (DBConnection.getIsDatabaseUsed()) {

            String criterionValueFromContext = "";
            String criterionTag;
            String criteriaString = "";
            String criterionField = "";
            String criterionValueTag;
            String criterionValue = "";
            String joinMarkerLeft = "";
            String joinMarkerRight = "";
            String joinMarkerRightMV = "";
            String contextValue;
            String fixedValue;
            String contextTag;

            if (result.contains(getStartTag(DB_DATABASE_TYPE))) {
                ArrayList<String> tableNames = DatabaseQueryBuilderGUI.getAllReferenceDatabaseTableNames();
                //ArrayList<ArrayList<String>> fieldNames = new ArrayList<>();
                //for (String table: tableNames) {             
                // fieldNames.add(DatabaseQueryBuilderGUI.getAllReferenceDatabaseFieldNames(table));
                //}
                // the main database tag is going to be used as the final marker to be replaced by database output..

                // *********************** TABLES *******************************************************************
                // Find referenced tables and fields
                ArrayList<String> selectedTables = new ArrayList<>();
                ArrayList<String> selectedFields = new ArrayList<>();
                int numOfSelectFields = 0;
                int nextType;
                String currentTag;
                String[] currentTableAndFieldIndex;
                LinkedHashMap<Integer, String> prefixStringHashMap = new LinkedHashMap<>();
                LinkedHashMap<Integer, String> postfixStringHashMap = new LinkedHashMap<>();
                boolean invalidSelector = false;

                while (result.contains(getStartTag(DB_TABLE_TYPE)) || result.contains(getStartTag(DB_COUNT_TYPE)) || result.contains(getStartTag(DB_FORMAT_TYPE)) || result.contains(getStartTag(DB_SELECTOR_TYPE))) {
                    numOfSelectFields++;

                    nextType = getNextSelectTagType(result);
                    switch (nextType) {
                        case DB_TABLE_TYPE:
                            currentTag = getNextSelectionTag(result);
                            currentTableAndFieldIndex = getDatabaseTableAndFieldFromTag(currentTag, DB_TABLE_TYPE);
                            selectedTables.add(currentTableAndFieldIndex[0]);
                            selectedFields.add(currentTableAndFieldIndex[0] + "." + currentTableAndFieldIndex[1]);
                            //selectedFields.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(currentTableAndFieldIndex[0])).get(Integer.parseInt(currentTableAndFieldIndex[1])));
                            //selectedTables.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0]))); 

                            result = removeTag(result, currentTag);
                            break;
                        case DB_SELECTOR_TYPE: // we're dealing with a context variable that contains the table and field to fetch..
                            currentTag = getNextSelectionTag(result);
                            //Logger.info("Found the following selector tag:" + currentTag);
                            // fetch the value of the variable..
                            String variableName = getTagValue(currentTag, DB_SELECTOR_TYPE);
                            // Logger.info("The embedded variable name is: " + variableName);
                            String tableAndField = getCurrentContextVariableValue(variableName, DialogMain.getDialogUserList().getCurrentContextVariables());
                            //Logger.info("The value of the variable selected is: " + tableAndField);
                            currentTableAndFieldIndex = getDatabaseTableAndFieldFromTag(tableAndField, DB_TABLE_TYPE);
                            selectedTables.add(currentTableAndFieldIndex[0]);
                            selectedFields.add(currentTableAndFieldIndex[0] + "." + currentTableAndFieldIndex[1]);
                            //selectedFields.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(currentTableAndFieldIndex[0])).get(Integer.parseInt(currentTableAndFieldIndex[1])));
                            //selectedTables.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0]))); 
                            invalidSelector = tableAndField.isEmpty();
                            result = removeTag(result, currentTag);
                            break;
                        case DB_COUNT_TYPE:
                            currentTag = getNextSelectionTag(result);
                            currentTableAndFieldIndex = getDatabaseTableAndFieldFromTag(currentTag, DB_COUNT_TYPE);
                            selectedTables.add(currentTableAndFieldIndex[0]);
                            selectedFields.add("COUNT(" + currentTableAndFieldIndex[0] + "." + currentTableAndFieldIndex[1] + ")");
                            //selectedTables.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0]))); 
                            //selectedFields.add("COUNT(" + tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(currentTableAndFieldIndex[0])).get(Integer.parseInt(currentTableAndFieldIndex[1]))  + ")");
                            result = removeTag(result, currentTag);
                            break;
                        case DB_FORMAT_TYPE:
                            String formatTagString = getNextSelectionTag(result);
                            //Logger.info("Initial format string is: " + formatTagString);
                            String tempFomatString = formatTagString;
                            currentTag = getNextTag(tempFomatString, DB_PREFIX_TYPE);
                            String prefix = getTagValue(currentTag, DB_PREFIX_TYPE);
                            tempFomatString = removeTag(tempFomatString, currentTag);
                            //Logger.info("after removal of prefix: " + formatTagString);

                            currentTag = getNextTag(tempFomatString, DB_POSTFIX_TYPE);
                            String postfix = getTagValue(currentTag, DB_POSTFIX_TYPE);
                            tempFomatString = removeTag(tempFomatString, currentTag);
                            //Logger.info("after removal of postfix: " + formatTagString);

                            currentTableAndFieldIndex = getDatabaseTableAndFieldFromTag(tempFomatString, DB_FORMAT_TYPE);

                            prefixStringHashMap.put(numOfSelectFields, prefix);
                            postfixStringHashMap.put(numOfSelectFields, postfix);
                            selectedTables.add(currentTableAndFieldIndex[0]);
                            selectedFields.add(currentTableAndFieldIndex[0] + "." + currentTableAndFieldIndex[1]);
                            //selectedTables.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0]))); 
                            //selectedFields.add(tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(currentTableAndFieldIndex[0])).get(Integer.parseInt(currentTableAndFieldIndex[1])));
                            result = removeTag(result, formatTagString);
                            break;
                    }
                }

                String output = "select ";
                int count = 1;
                for (String field : selectedFields) {
                    if (count == selectedFields.size()) {
                        output += field;
                    } else {
                        output += field + ",";
                    }
                    count++;

                }

                // build up the criteria details..
                //while (result.contains(getDBMarkerString(DB_CRITERION_TYPE))) {
                while (result.contains(getStartTag(DB_CRITERION_TYPE))) {

                    if (criteriaString.equals("")) {
                        criteriaString = " where ";
                    } else {
                        criteriaString += " and ";
                    }

                    String partialMatchTag = "";

                    //Logger.info("Looking for criteria: result before removal is:" + result);
                    criterionTag = getNextTag(result, DB_CRITERION_TYPE);
                    //Logger.info("CRITERION TAG IS: " + criterionTag);
                    result = removeTag(result, criterionTag);

                    //Logger.info("Result after removal is: " + result);
                    String[] criterionTableAndFieldIndex = getDatabaseTableAndFieldFromTag(criterionTag, DB_CRITERION_TYPE);
                    //criterionField = tableNames.get(Integer.parseInt(criterionTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(criterionTableAndFieldIndex[0])).get(Integer.parseInt(criterionTableAndFieldIndex[1]));
                    criterionField = criterionTableAndFieldIndex[0] + "." + criterionTableAndFieldIndex[1];
                    selectedTables.add(criterionTableAndFieldIndex[0]);
                    //selectedTables.add(tableNames.get(Integer.parseInt(criterionTableAndFieldIndex[0])));
                    //criteriaString += criterionField + " like ";

                    // criterion value
                    nextType = getNextCriterionValueTagType(criterionTag);
                    criterionValueTag = getNextCriterionValueTag(criterionTag);
                    switch (nextType) {
                        case DB_CRITERION_VALUE_FIXED_TYPE:
                            //Logger.info("DB_CRITERION_VALUE_FIXED_TYPE - tag found is: " + criterionValueTag);

                            //criterionValueMarker  = getNextSimpleMarker(result,getDBMarkerString(DB_CRITERIONVALUE_TYPE));
                            //result = removeMarker(result,criterionValueMarker);
                            criterionValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_FIXED_TYPE);

                            partialMatchTag = getNextTag(criterionValue, DB_PARTIALMATCH_TYPE);
                            if (!partialMatchTag.equals("")) {
                                criterionValue = removeTag(criterionValue, partialMatchTag);
                                criteriaString += criterionField + " LIKE '%" + criterionValue + "%'";
                            } else {
                                criteriaString += criterionField + " LIKE '" + criterionValue + "'";
                            }

                            break;
                        case DB_CRITERION_VALUE_CONTEXT_TYPE:
                            //Logger.info("DB_CRITERION_VALUE_CONTEXT_TYPE - tag found is: " + criterionValueTag);
                            criterionValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_CONTEXT_TYPE);

                            partialMatchTag = getNextTag(criterionValue, DB_PARTIALMATCH_TYPE);
                            if (!partialMatchTag.equals("")) {
                                criterionValue = removeTag(criterionValue, partialMatchTag);
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(criterionValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(criterionValue,DialogMain.getDialogUserList().getCurrentKAContextVariables());
                                criteriaString += criterionField + " LIKE '%" + criterionValueFromContext + "%'";
                            } else {
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(criterionValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(criterionValue,DialogMain.getDialogUserList().getCurrentKAContextVariables());

                                criteriaString += criterionField + " LIKE '" + criterionValueFromContext + "'";
                            }

                            break;
                        case DB_CRITERION_VALUE_NUMBER_TYPE:
                            //Logger.info("DB_CRITERION_VALUE_NUMBER_TYPE - tag found is: " + criterionValueTag);

                            //result = removeMarker(result,criterionValueMarker);
                            criterionValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_NUMBER_TYPE);

                            partialMatchTag = getNextTag(criterionValue, DB_PARTIALMATCH_TYPE);
                            if (!partialMatchTag.equals("")) {
                                criterionValue = removeTag(criterionValue, partialMatchTag);
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(criterionValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(criterionValue,DialogMain.getDialogUserList().getCurrentKAContextVariables());
                                criterionValueFromContext = criterionValueFromContext.replaceAll("[\\D]", "");
                                criteriaString += criterionField + " LIKE '%" + criterionValueFromContext + "%'";
                            } else {
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(criterionValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(criterionValue,DialogMain.getDialogUserList().getCurrentKAContextVariables());
                                criterionValueFromContext = criterionValueFromContext.replaceAll("[\\D]", "");
                                criteriaString += criterionField + " LIKE '" + criterionValueFromContext + "'";
                            }
                            break;
                        case DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE:
                            //Logger.info("DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE - tag found is: " + criterionValueTag);

                            // e.g. start with <CFC>x<CCX>y</CCX></CFC><CON>Y</CON>
                            // contextTag is <CCX>y</CCX>
                            //fixedAndContextTag = getTagValue(criterionValueTag,DB_CRITERION_VALUE_FIXED_NUMBER_TYPE);
                            //Logger.info("criterionValueTag is: " + criterionValueTag);
                            partialMatchTag = getNextTag(criterionValueTag, DB_PARTIALMATCH_TYPE);
                            if (!partialMatchTag.equals("")) {
                                criterionValueTag = removeTag(criterionValueTag, partialMatchTag);
                                //Logger.info("criterionValueTag after contains removal is: " + criterionValueTag);

                                contextTag = getNextTag(criterionValueTag, DB_CRITERION_VALUE_CONTEXT_TYPE);
                                //Logger.info("contextTag is: " + contextTag);
                                // context value is y
                                contextValue = getTagValue(contextTag, DB_CRITERION_VALUE_CONTEXT_TYPE);
                                //Logger.info("contextValue is: " + contextValue);

                                //criteronValue becomes <CFC>x</CFC>
                                criterionValueTag = removeTag(criterionValueTag, contextTag);
                                //Logger.info("criterionValueTag after contextTag removal is: " + criterionValueTag);

                                //Logger.info("********************  value of fixedAndContextTag after removing contextTag is:" + fixedAndContextTag);
                                // fixedValue becomes x
                                fixedValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE);
                                //Logger.info("fixedValue is: " + fixedValue);
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(contextValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(contextValue,DialogMain.getDialogUserList().getCurrentKAContextVariables());
                                //Logger.info("******************* context variable for fixed number - context var looked up is:" + contextValue + "and its lookup value is:"+ criterionValueFromContext + "and the fixed value is: " + fixedValue);
                                criteriaString += criterionField + " LIKE '%" + fixedValue + criterionValueFromContext + "%'";
                            } else {
                                contextTag = getNextTag(criterionValueTag, DB_CRITERION_VALUE_CONTEXT_TYPE);
                                // context value is y
                                contextValue = getTagValue(contextTag, DB_CRITERION_VALUE_CONTEXT_TYPE);
                                //criteronValue becomes <CFC>x</CFC>
                                criterionValueTag = removeTag(criterionValueTag, contextTag);
                                // fixedValue becomes x
                                fixedValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE);
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(contextValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(contextValue,DialogMain.getDialogUserList().getCurrentKAContextVariables()); 
                                criteriaString += criterionField + " LIKE '" + fixedValue + criterionValueFromContext + "'";

                            }

                            break;
                        case DB_CRITERION_VALUE_FIXED_NUMBER_TYPE:
                            //Logger.info("DB_CRITERION_VALUE_FIXED_NUMBER_TYPE - tag found is: " + criterionValueTag);

                            // e.g. start with <CFC>x<CCX>y</CCX></CFC><CON>Y</CON>
                            // contextTag is <CCX>y</CCX>
                            //fixedAndContextTag = getTagValue(criterionValueTag,DB_CRITERION_VALUE_FIXED_NUMBER_TYPE);
                            //Logger.info("criterionValueTag is: " + criterionValueTag);
                            partialMatchTag = getNextTag(criterionValueTag, DB_PARTIALMATCH_TYPE);
                            if (!partialMatchTag.equals("")) {
                                criterionValueTag = removeTag(criterionValueTag, partialMatchTag);
                                //Logger.info("criterionValueTag after contains removal is: " + criterionValueTag);

                                contextTag = getNextTag(criterionValueTag, DB_CRITERION_VALUE_NUMBER_TYPE);
                                //Logger.info("contextTag is: " + contextTag);
                                // context value is y
                                contextValue = getTagValue(contextTag, DB_CRITERION_VALUE_NUMBER_TYPE);
                                //Logger.info("contextValue is: " + contextValue);

                                //criteronValue becomes <CFC>x</CFC>
                                criterionValueTag = removeTag(criterionValueTag, contextTag);
                                //Logger.info("criterionValueTag after contextTag removal is: " + criterionValueTag);

                                //Logger.info("********************  value of fixedAndContextTag after removing contextTag is:" + fixedAndContextTag);
                                // fixedValue becomes x
                                fixedValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_FIXED_NUMBER_TYPE);
                                //Logger.info("fixedValue is: " + fixedValue);
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(contextValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(contextValue,DialogMain.getDialogUserList().getCurrentKAContextVariables());
                                //Logger.info("******************* context variable for fixed number - context var looked up is:" + contextValue + "and its lookup value is:"+ criterionValueFromContext + "and the fixed value is: " + fixedValue);
                                criterionValueFromContext = criterionValueFromContext.replaceAll("[\\D]", "");
                                criteriaString += criterionField + " LIKE '%" + fixedValue + criterionValueFromContext + "%'";
                            } else {
                                contextTag = getNextTag(criterionValueTag, DB_CRITERION_VALUE_NUMBER_TYPE);
                                // context value is y
                                contextValue = getTagValue(contextTag, DB_CRITERION_VALUE_NUMBER_TYPE);
                                //criteronValue becomes <CFC>x</CFC>
                                criterionValueTag = removeTag(criterionValueTag, contextTag);
                                // fixedValue becomes x
                                fixedValue = getTagValue(criterionValueTag, DB_CRITERION_VALUE_FIXED_NUMBER_TYPE);
                                //if (!preview)
                                criterionValueFromContext = getCurrentContextVariableValue(contextValue, DialogMain.getDialogUserList().getCurrentContextVariables());
                                //else
                                //criterionValueFromContext = getCurrentContextVariableValue(contextValue,DialogMain.getDialogUserList().getCurrentKAContextVariables()); 
                                criterionValueFromContext = criterionValueFromContext.replaceAll("[\\D]", "");
                                criteriaString += criterionField + " LIKE '" + fixedValue + criterionValueFromContext + "'";

                            }

                            break;
                    }
                }

                String tempOutput = "";
                boolean impreciseQuery = false;
                if (!criteriaString.equals("")) {

                    impreciseQuery = false;

                    tempOutput = criteriaString;
                    // Determine how to join tables..

                    ArrayList<String> selectedJoinFields = new ArrayList<>();
                    while (result.contains(getStartTag(DB_JOIN_TYPE))) {
                        String originalCurrentTag = getNextTag(result, DB_JOIN_TYPE);
                        currentTag = originalCurrentTag;

                        joinMarkerLeft = getNextTag(currentTag, DB_JVALUE_TYPE);
                        currentTag = removeTag(currentTag, joinMarkerLeft);

                        //result = removeTag(result,joinMarkerLeft+"=");
                        joinMarkerRight = getNextTag(currentTag, DB_JVALUE_TYPE);
                        currentTag = removeTag(currentTag, joinMarkerRight);

                        joinMarkerRightMV = getNextTag(currentTag, DB_MULTIV_JOIN_TYPE);
                        // Logger.info("joinMarkerRightMV is: " + joinMarkerRightMV);
                        result = removeTag(result, originalCurrentTag);

                        String[] rightTableAndFieldIndex;
                        String[] leftTableAndFieldIndex;
                        //Logger.info("LOOKING AT JOIN LEFT FIELD:" + joinMarkerLeft);
                        //leftTableAndFieldIndex = getDatabaseTableAndFieldFromTag(joinMarkerLeft,DB_JOIN_TYPE);
                        leftTableAndFieldIndex = getDatabaseTableAndFieldFromTag(joinMarkerLeft, DB_JVALUE_TYPE);
                        if (joinMarkerRightMV.equals("")) {
                            //rightTableAndFieldIndex = getDatabaseTableAndFieldFromTag(joinMarkerRight,DB_JOIN_TYPE);
                            rightTableAndFieldIndex = getDatabaseTableAndFieldFromTag(joinMarkerRight, DB_JVALUE_TYPE);
                        } else {
                            rightTableAndFieldIndex = getDatabaseTableAndFieldFromTag(joinMarkerRightMV, DB_MULTIV_JOIN_TYPE);
                        }

                        //Logger.info("Result after join removal is: [" + result + "]");
                        tempOutput += " and " + leftTableAndFieldIndex[0] + "." + leftTableAndFieldIndex[1];
                        //tempOutput += " and " + tableNames.get(Integer.parseInt(leftTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(leftTableAndFieldIndex[0])).get(Integer.parseInt(leftTableAndFieldIndex[1]));

                        if (joinMarkerRightMV.equals("")) {
                            tempOutput += " = " + rightTableAndFieldIndex[0] + "." + rightTableAndFieldIndex[1];
                            //tempOutput += " = " + tableNames.get(Integer.parseInt(rightTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(rightTableAndFieldIndex[0])).get(Integer.parseInt(rightTableAndFieldIndex[1]));
                        } else {
                            // we're fetching the values of a multi-value field to be used as a list in an SQL IN ('a','b',...) statement..
                            String mvQuery = "select " + rightTableAndFieldIndex[0] + "." + rightTableAndFieldIndex[1];
                            mvQuery += " from " + rightTableAndFieldIndex[0];
                            //String mvQuery  = "select " + tableNames.get(Integer.parseInt(rightTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(rightTableAndFieldIndex[0])).get(Integer.parseInt(rightTableAndFieldIndex[1]));
                            //mvQuery += " from " + tableNames.get(Integer.parseInt(rightTableAndFieldIndex[0]));

                            mvQuery += criteriaString;

                            //mvQuery += " where ";

                            /*
                            if (!criterionValue.equals("")) {
                                mvQuery += criterionField + " like ";
                                mvQuery += "'" + criterionValue + "'";
                            }
                            else if (!criterionValueFromContext.equals("")) {
                                mvQuery += criterionField + " like ";
                                mvQuery += "'" + criterionValueFromContext + "'";
                            }*/
                            //Logger.info("mvQuery is: " + mvQuery);
                            String mvQueryResult = DBOperation.selectQueryAsString(mvQuery, false);
                            // Logger.info("mvQuery is: " + mvQuery);
                            tempOutput += convertMultivalueListToInclusionString(convertMultivalueStringToList(mvQueryResult));
                        }

                        selectedTables.add(rightTableAndFieldIndex[0]);
                        selectedTables.add(leftTableAndFieldIndex[0]);
                        //selectedTables.add(tableNames.get(Integer.parseInt(rightTableAndFieldIndex[0])));
                        //selectedTables.add(tableNames.get(Integer.parseInt(leftTableAndFieldIndex[0])));
                    }

                    currentTag = getNextTag(result, DB_ORDER_TYPE);
                    currentTableAndFieldIndex = getDatabaseTableAndFieldFromTag(currentTag, DB_ORDER_TYPE);
                    result = removeTag(result, currentTag);

                    if (!currentTableAndFieldIndex[0].equals("")) {
                        tempOutput += " order by " + currentTableAndFieldIndex[0] + "." + currentTableAndFieldIndex[1];
                        //tempOutput += " order by " + tableNames.get(Integer.parseInt(currentTableAndFieldIndex[0])) + "." + fieldNames.get(Integer.parseInt(currentTableAndFieldIndex[0])).get(Integer.parseInt(currentTableAndFieldIndex[1]));
                    } else {
                        tempOutput += " order by " + criterionField;
                    }
                }

                output += " from ";
                HashSet<String> uniqueValues = new HashSet<>(selectedTables);
                count = 1;
                for (String table : uniqueValues) {
                    if (count == uniqueValues.size()) {
                        output += table;
                    } else {
                        output += table + ",";
                    }
                    count++;
                }

                output += tempOutput;

                //Logger.info("query is: " + output);
                if (!impreciseQuery) {
                    Logger.info("************************************************** query is: " + output);
                    //String dbResult = DBOperation.selectQueryAsString(output,false);
                    String dbResult = "";
                    if (!invalidSelector)
                        dbResult = DBOperation.selectQueryAsStringWithFormatting(output, prefixStringHashMap, postfixStringHashMap, false);
                    
                    
                    if (dbResult.toLowerCase().contains("<table")) {
                        dbResult = META_NO_SPEAK + "<div id=\"db" + databaseResponseInstance + "\" style=\"display:none;\">" + dbResult + "</div>"
                                + "<p><a href=\"#db" + databaseResponseInstance + "\" rel=\"modal:open\">click me</a></p>" + META_NO_SPEAK_END
                                + "click above to show formatted data";
                        databaseResponseInstance++;
                    } else {
                        dbResult = new HtmlCleaner().clean(dbResult).getText().toString();
                    }

                    //Logger.info("database result is: " + dbResult);
                    // if we have a multiline database output, put the first line on a line of its own.
                    if (dbResult.contains("\n")) {
                        dbResult = "\n" + dbResult;
                    }

                    if (dbResult.isEmpty()) {
                        String defaultEmptyDBRresult;
                        ContextVariable emptyResultVariable;
                        //if (!preview)
                        emptyResultVariable = DialogMain.globalContextVariableManager.getSystemContextVariableOverride("@SYSTEMemptyDBResult", DialogMain.getDialogUserList().getCurrentLastInferencedRuleId());
                        //emptyResultVariable = DialogMain.globalContextVariableManager.getSystemContextVariableOverride("@SYSTEMemptyDBResult", DialogMain.getDialogUserList().getCurrentStackLevel());
                        //else
                        //emptyResultVariable = DialogMain.globalContextVariableManager.getSystemContextVariableOverride("@SYSTEMemptyDBResult", DialogMain.getDialogUserList().getCurrentLastKAInferencedRuleId());

                        if (emptyResultVariable != null) {
                            defaultEmptyDBRresult = emptyResultVariable.getSingleVariableValue();
                        } else {
                            defaultEmptyDBRresult = "Query produced no results";
                            if (invalidSelector)
                                defaultEmptyDBRresult += " - invalid/empty selector used";
                        }

                        dbResult = defaultEmptyDBRresult;
                    }

                    //Logger.info("************* RAW QUERY RESULT IS: " + dbResult);
                    String databaseTag = getNextTag(result, DB_DATABASE_TYPE);
                    String databaseTagInstance = getTagValue(databaseTag, DB_DATABASE_TYPE);

                    String placeholderTag;
                    String placeholderDescriptionTag;
                    String placeholderDescription;
                    String tempPlaceholderTag;
                    String placeholderTagInstance;

                    // escape all explict two-character newline sequences with an additional escape "\n" becomes "\\n" so a regex replace maintains two char sequence "\n" after the replacement.
                    // this will then (later) be replaced by the one-character newline after parsing.
                    dbResult = dbResult.replace("\\n", "\\\n");

                    if (!preview) {
                        placeholderTag = getNextTag(referenceString, DB_PLACEHOLDER_TYPE);
                        placeholderDescriptionTag = getNextTag(placeholderTag, DB_DESCRIPTION_TYPE);
                        placeholderDescription = getTagValue(placeholderDescriptionTag, DB_DESCRIPTION_TYPE);
                        tempPlaceholderTag = removeTag(placeholderTag, placeholderDescriptionTag);
                        placeholderTagInstance = getTagValue(tempPlaceholderTag, DB_PLACEHOLDER_TYPE);
                        result = removeTag(result, databaseTag);

                        /*
                        Logger.info("databaseTag is: " + databaseTag);
                        Logger.info("databaseTagInstance is: " + databaseTagInstance);
                        Logger.info("placeholderTag is: " + placeholderTag);
                        Logger.info("placeholder Description is: " + placeholderDescription);
                        Logger.info("placeholderTagInstance is: " + placeholderTagInstance);
                         */
                        // Look for placeholders that match this specific query and replace them with the database query result
                        if (placeholderTagInstance.equals(databaseTagInstance)) {
                            //Logger.info("ReferenceString before placeholder replacement:" + referenceString);
                            //Logger.info("About to replace all placeholder instances of: " + placeholderTag);
                            //Logger.info("DBRESULT BEFORE REPLACEALL:" + dbResult);

                            result = referenceString.replaceAll(Pattern.quote(placeholderTag), dbResult);  // ignore any regular expression chars in placeholderTag
                            //Logger.info("REPLACED TAGS AFTER :" + result);

                            //Logger.info("Result after placeholder replacement:" + result);  
                        }

                        //result = result.replace(getNextTag(result,DB_DATABASE_TYPE),dbResult);
                        //result = result.replace("\\n", "\n");
                        //result = result.replace("\\t", "\t");
                    } else { //previewing a query before saving it..
                        //Logger.info(result);
                        placeholderTag = getNextTag(referenceString, DB_PLACEHOLDER_TYPE);
                        if (!placeholderTag.isEmpty()) {
                            placeholderDescriptionTag = getNextTag(placeholderTag, DB_DESCRIPTION_TYPE);
                            placeholderDescription = getTagValue(placeholderDescriptionTag, DB_DESCRIPTION_TYPE);
                            tempPlaceholderTag = removeTag(placeholderTag, placeholderDescriptionTag);
                            placeholderTagInstance = getTagValue(tempPlaceholderTag, DB_PLACEHOLDER_TYPE);
                            result = removeTag(result, databaseTag);

                            if (placeholderTagInstance.equals(databaseTagInstance)) {
                                //Logger.info("ReferenceString before placeholder replacement:" + referenceString);
                                //Logger.info("About to replace all placeholder instances of: " + placeholderTag);
                                result = referenceString.replaceAll(Pattern.quote(placeholderTag), dbResult);  // ignore any regular expression chars in placeholderTag
                                //Logger.info("Result after placeholder replacement:" + result);  
                            }

                        } else //result = dbResult.replace("\\n", "\n").replace("\\t", "\t");                       
                        {
                            result = referenceString.replaceAll(Pattern.quote(""), dbResult);  // ignore any regular expression chars in placeholderTag
                        }
                    }
                } else {
                    result = result.replace(getNextTag(result, DB_DATABASE_TYPE), "[I do not have enough context to produce a result..]");
                }
                //Logger.info("result is: " + result);

            } else {
                return result;
            }
        } // no database at the moment, so replace database keywords in conclusion with a suitable message..
        else {

            if (result.contains(getStartTag(DB_DATABASE_TYPE))) {

                result = "Unfortunately my response relies on a database which is not currently accessible..";
            }

        }
        return result;
    }

    // converts a list of values to an SQL parenthesis enclosed quoted list
    /**
     *
     * @param theList
     * @return
     */
    public static String convertMultivalueListToInclusionString(ArrayList<String> theList) {
        String result = " IN (";
        int count = 1;

        for (String item : theList) {
            result += "'" + item + "'";
            if (count != theList.size()) {
                result += ",";
            }
            count++;
        }
        result += ")";

        return result;
    }

    /**
     *
     * @param field
     * @param prefixString
     * @param postFixString
     * @return
     */
    public String formatDatabaseField(String field, String prefixString, String postFixString) {
        return prefixString + field + postFixString;
    }

    /**
     *
     * @param tableTag
     * @param type
     * @return
     */
    public static String[] getDatabaseTableAndFieldFromTag(String tableTag, int type) {
        String result[] = new String[]{"", ""};
        String argument1;
        String argument2;
        String valueTag;
        String valueValue;
        String fieldTag;
        String fieldValue;
        String tableValue;

        //Logger.info("The marker I am looking at is: " + marker + " and the type is: " +  tableMarker);
        if (!tableTag.equals("")) {
            //Logger.info("Initial tableTag is: " + tableTag);
            fieldTag = getNextTag(tableTag, DB_FIELD_TYPE);
            fieldValue = getTagValue(fieldTag, DB_FIELD_TYPE);
            //Logger.info("fieldTag is : " + fieldTag + " and fieldValue is : " + fieldValue);
            // why did I put this here?
            valueTag = getNextCriterionValueTag(tableTag);  // ignore this for now, will be retrieved later..
            //Logger.info("value tag is: " + valueTag);
            tableTag = removeTag(tableTag, fieldTag);
            //Logger.info("tableTag after fieldTag removal is: " + tableTag);
            tableTag = removeTag(tableTag, valueTag);
            //Logger.info("tableTag after valueTag removal is: " + tableTag);

            //Logger.info("Now tryting to get value of tableTag: " + tableTag);
            tableValue = getTagValue(tableTag, type);
            //Logger.info("tableTag is : " + tableTag + " and tableValue is : " + tableValue);

            result[0] = tableValue;
            result[1] = fieldValue;
        }

        return result;
    }

    /**
     *
     * @param reference
     * @return
     */
    public static String getNextSelectionTag(String reference) {
        String result = "";
        int start;
        int end;
        int type;

        //Logger.info("Ref is: " + reference);
        type = getNextSelectTagType(reference);
        if (type != -1) {
            start = reference.indexOf(getStartTag(type));
            end = reference.indexOf(getEndTag(type)) + getEndTag(type).length();
            //Logger.info("start is: " + start);
            //Logger.info("end is: " + end);
            result = reference.substring(start, end);
        } else {
            //Logger.info("No more selection tags found..");
            result = "";
        }
        return result;
    }

    /**
     *
     * @param reference
     * @return
     */
    public static String getNextCriterionValueTag(String reference) {
        String result = "";
        int start;
        int end;
        int type;

        //Logger.info("Ref is: " + reference);
        type = getNextCriterionValueTagType(reference);
        if (type != -1) {
            start = reference.indexOf(getStartTag(type));
            end = reference.indexOf(getEndTag(type)) + getEndTag(type).length();
            //Logger.info("start is: " + start);
            //Logger.info("end is: " + end);
            result = reference.substring(start, end);
        } else {
            result = "";
        }

        return result;
    }

    /**
     *
     * @param reference
     * @param type
     * @return
     */
    public static String getNextTag(String reference, int type) {
        String result = "";
        int endOfMarker;
        int startOfMarker;

        if (reference.contains(getStartTag(type))) {
            startOfMarker = reference.indexOf(getStartTag(type));
            endOfMarker = reference.indexOf(getEndTag(type)) + getEndTag(type).length();

            result = reference.substring(startOfMarker, endOfMarker);
        }
        return result;
    }

    /**
     *
     * @param reference
     * @return
     */
    public static int getNextSelectTagType(String reference) {
        int result = -1;
        int find = 0;
        int next = Integer.MAX_VALUE;

        if (reference.contains(getStartTag(DB_TABLE_TYPE)) || reference.contains(getStartTag(DB_COUNT_TYPE)) || reference.contains(getStartTag(DB_FORMAT_TYPE)) || reference.contains(getStartTag(DB_SELECTOR_TYPE))) {

            find = reference.indexOf(getStartTag(DB_TABLE_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_TABLE_TYPE;
            }
            find = reference.indexOf(getStartTag(DB_COUNT_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_COUNT_TYPE;
            }
            find = reference.indexOf(getStartTag(DB_FORMAT_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_FORMAT_TYPE;
            }

            find = reference.indexOf(getStartTag(DB_SELECTOR_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_SELECTOR_TYPE;
            }
        }

        return result;
    }

    /**
     *
     * @param reference
     * @return
     */
    public static int getNextCriterionValueTagType(String reference) {
        int result = -1;
        int next = Integer.MAX_VALUE;
        int find = 0;

        if (reference.contains(getStartTag(DB_CRITERION_VALUE_NUMBER_TYPE)) || reference.contains(getStartTag(DB_CRITERION_VALUE_CONTEXT_TYPE)) || reference.contains(getStartTag(DB_CRITERION_VALUE_FIXED_TYPE)) || reference.contains(getStartTag(DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE)) || reference.contains(getStartTag(DB_CRITERION_VALUE_FIXED_NUMBER_TYPE))) {

            find = reference.indexOf(getStartTag(DB_CRITERION_VALUE_NUMBER_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_CRITERION_VALUE_NUMBER_TYPE;
            }

            find = reference.indexOf(getStartTag(DB_CRITERION_VALUE_CONTEXT_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_CRITERION_VALUE_CONTEXT_TYPE;
            }

            find = reference.indexOf(getStartTag(DB_CRITERION_VALUE_FIXED_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_CRITERION_VALUE_FIXED_TYPE;
            }

            find = reference.indexOf(getStartTag(DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_CRITERION_VALUE_FIXED_CONTEXT_TYPE;
            }

            find = reference.indexOf(getStartTag(DB_CRITERION_VALUE_FIXED_NUMBER_TYPE));
            if (find >= 0 && find < next) {
                next = find;
                result = DB_CRITERION_VALUE_FIXED_NUMBER_TYPE;
            }
        }
        return result;
    }

    /**
     *
     * @param reference
     * @param theTag
     * @return
     */
    public static String removeTag(String reference, String theTag) {
        String result = reference;
        if (reference.contains(theTag)) {
            result = result.replace(theTag, "");
        }
        return result;
    }

    public static boolean containsTableAndFieldTag(String reference, String table, String field) {
        boolean result = false;
        String fieldTag;
        String tableTag;
        String fieldValue;
        String tableValue;
        String originalTag;

        tableTag = getNextTag(reference, DB_TABLE_TYPE);
        originalTag = tableTag;
        
        Logger.info("** reference table: " + table + " and field :" + field);
        Logger.info("** Starting table tag: " + tableTag);

        while (!tableTag.equals("")) {
            fieldTag = getNextTag(tableTag, DB_FIELD_TYPE);
            fieldValue = getTagValue(fieldTag, DB_FIELD_TYPE);

            tableTag = removeTag(tableTag, fieldTag);
            tableValue = getTagValue(tableTag, DB_TABLE_TYPE);

            Logger.info("** current table value: " + tableValue + " and field value :" + fieldValue);
            if (tableValue.equals(table) && fieldValue.equals(field)) {
                Logger.info("Found match! ");
                return true;
            } else {
                Logger.info("No match! ");
                reference = removeTag(reference, originalTag);

                tableTag = getNextTag(reference, DB_TABLE_TYPE);
                originalTag = tableTag;
            }
        }

        return result;
    }

    /**
     *
     * @param reference
     * @param theTag
     * @param replaceValue
     * @return
     */
    public static String replaceTag(String reference, String theTag, String replaceValue) {
        String result = reference;

        if (reference.contains(theTag)) {
            result = result.replace(theTag, replaceValue);
        }
        return result;
    }

    /**
     *
     * @param varName
     * @param list
     * @return
     */
    public static String getCurrentContextVariableValue(String varName, LinkedHashMap<String, ContextVariableUser> list) {
        String result = "";

        //Logger.info("BEING CALLED TO REPLACE CONTEXT VARIABLE " + varName);
        // check to see if the user's context variable overrides a system version..
        //String userVarName = varName.replace("@SYSTEM", "");
        String userVarName = varName.replace("@SYSTEM", "@");

        ContextVariableUser var = list.get(userVarName);
        ContextVariable sysVar = null;

        if (var != null) {
            result = var.getValue();
            //Logger.info("USER HAD OVERRIDE: " + result);
        } else { // we're looking for a system variable...
            if (varName.contains("@SYSTEM")) {
                int context = DialogMain.getDialogUserList().getCurrentStackLevel();
                int ruleId = DialogMain.getDialogUserList().getCurrentLastInferencedRuleId();
                //Logger.info("OUTPUTPARSER - user ruleId is: " + ruleId);
                sysVar = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(varName, ruleId);

                if (sysVar != null) {
                    result = sysVar.getSingleVariableValue();
                    //Logger.info("System context variable: current value is:" + result);
                }
            }
        }

        return result;
    }

    public static String getMetaWhere(String input) {

        String result = input;
        String metaWhereResult = "";

        MCRDRStackResultSet currentStackedInferenceResults = DialogMain.getDialogUserList().getCurrentStackInferenceResult();
        MCRDRStackResultInstance previousInferenceRuleSet;
        String lastUserInput = DialogMain.getDialogUserList().getCurrentPreviousInputDialogString();

        int lastStackId = currentStackedInferenceResults.getSize() - 1;
        RuleSet theRuleSet;
        Rule theLastRule;

        previousInferenceRuleSet = currentStackedInferenceResults.getMCRDRStackResultInstanceById(lastStackId);
        theRuleSet = previousInferenceRuleSet.getInferenceResult();
        theLastRule = theRuleSet.getLastRule();
        while (theLastRule != Main.KB.getRootRule() && lastStackId != 0) {
            if (theLastRule == null) {
                Logger.info("theLastRule is null!");
            }
            if (theLastRule.getConclusion() == null) {
                Logger.info("theLastRule.getConclusion is null!");
            }

            if (theLastRule.getConclusion() != null) {
                if (!theLastRule.getConclusion().getConclusionValue().toString().contains(METAHELP) && !theLastRule.getConclusion().getConclusionValue().toString().contains(METAWHERE)
                        && !theLastRule.getConclusion().getConclusionValue().toString().contains(METANOHELP)) {
                    break;
                }
            }
            lastStackId--;
            previousInferenceRuleSet = currentStackedInferenceResults.getMCRDRStackResultInstanceById(lastStackId);
            theRuleSet = previousInferenceRuleSet.getInferenceResult();
            theLastRule = theRuleSet.getLastRule();
        }

        int caseID = previousInferenceRuleSet.getCaseId();
        DialogInstance dialogInstance = (DialogInstance) DialogMain.getDialogUserList().getCurrentDialogRepository().getDialogInstanceByGeneratedCaseId(caseID);
        if (dialogInstance != null) {
            lastUserInput = dialogInstance.getDialogStr();
        } else {
            lastUserInput = "";
        }

        // Logger.info("The matching user input was:" + lastUserInput);
        String matchingTermLookup;

        for (Rule aRule : theRuleSet.getBase().values()) {
            ConditionSet conditions = aRule.getConditionSet();
            String intermediateString = "";
            for (Condition aCondition : conditions.getBase()) {
                if (!intermediateString.isEmpty()) {
                    intermediateString += ", ";
                }

                matchingTermLookup = DialogMain.dicConverter.getOriginalInputForRepresentative(lastUserInput, aCondition.recentToRawString());
                if (!matchingTermLookup.isEmpty()) {
                    intermediateString += matchingTermLookup;
                } else {
                    intermediateString += aCondition.recentToRawString();
                }

            }
            if (!metaWhereResult.isEmpty()) {
                metaWhereResult += ", ";
            }

            metaWhereResult += intermediateString;
        }

        return metaWhereResult;
    }

    public static String parseMetaTerms(String input) {
        String result = input;

        boolean metaHelp = result.contains(METAHELP);
        boolean metaWhere = result.contains(METAWHERE);
        boolean metaNoHelp = result.contains(METANOHELP);

        String metaWhereResult = getMetaWhere(input);

        if (metaHelp) {

            MCRDRStackResultSet currentStackedInferenceResults = DialogMain.getDialogUserList().getCurrentStackInferenceResult();
            MCRDRStackResultInstance previousInferenceRuleSet;
            String lastUserInput = DialogMain.getDialogUserList().getCurrentPreviousInputDialogString();

            int lastStackId = currentStackedInferenceResults.getSize() - 1;
            RuleSet theRuleSet;
            Rule theLastRule;

            previousInferenceRuleSet = currentStackedInferenceResults.getMCRDRStackResultInstanceById(lastStackId);
            theRuleSet = previousInferenceRuleSet.getInferenceResult();
            theLastRule = theRuleSet.getLastRule();
            while (theLastRule != Main.KB.getRootRule() && lastStackId != 0) {
                if (!theLastRule.getConclusion().getConclusionValue().toString().contains(METAHELP) && !theLastRule.getConclusion().getConclusionValue().toString().contains(METAWHERE)
                        && !theLastRule.getConclusion().getConclusionValue().toString().contains(METANOHELP)) {
                    break;
                }
                lastStackId--;
                previousInferenceRuleSet = currentStackedInferenceResults.getMCRDRStackResultInstanceById(lastStackId);
                theRuleSet = previousInferenceRuleSet.getInferenceResult();
                theLastRule = theRuleSet.getLastRule();
            }

            //Logger.info("No inference result, so storing user recent input for later: " + aCase.getInputDialogInstance().getDialogStr());
            String potentialReplies = promptUserWithPotentialValidRulesInLastContext(true);
            //String testChildInferenceReplies = promptUserWithPotentialValidRulesInChildContexts(true);

            //if (!testChildInferenceReplies.isEmpty()) {
            //Logger.info("We have children.." + testChildInferenceReplies);
            // result = result.replace(METAHELP,"For our current topic of conversation, you can be more specific by asking me questions including the following topics - " + testChildInferenceReplies + ".");
            //}
            //else 
            if (potentialReplies.isEmpty()) {
                result = result.replace(METAHELP, "I'm sorry, I don't have any help for this topic.");
            } else {
                //result = result.replace(METAHELP, "You can ask me questions about any of the following topics - e.g." + potentialReplies + ".");
                if (!metaWhereResult.isEmpty()) {
                    result = result.replace(METAHELP, "We're currently discussing the topics '" + metaWhereResult + "'. From here you can ask me about (for example) - " + potentialReplies + ".");
                } else {
                    result = result.replace(METAHELP, "You can ask me about the following topics (for example) - " + potentialReplies + ".");
                }

            }

        } else if (metaWhere) {
            if (!metaWhereResult.isEmpty()) {
                result = result.replace(METAWHERE, "We are currently talking about the topic(s) - '" + metaWhereResult + "'");
            } else {
                result = result.replace(METAWHERE, "We haven't started talking about any topics yet..");
            }

        }

        result = result.replace(METANOHELP, "");

        //if (result.contains(META_REPLACE_EMPTY)) {      
        if (result.contains(META_REPLACE_START)) {      
            Logger.info("REPLACE CHECK: initial:" + result);
            ContextVariable emptyResultVariable = DialogMain.globalContextVariableManager.getSystemContextVariableOverride("@SYSTEMemptyDBResult", DialogMain.getDialogUserList().getCurrentLastInferencedRuleId());
            if (emptyResultVariable != null) {
                
                while (result.contains(META_REPLACE_START)) {
                    String dbNoResultText = emptyResultVariable.getSingleVariableValue();

                    int replaceStartMarker = result.indexOf(META_REPLACE_START);
                    int replaceEmptyMarker = result.indexOf(META_REPLACE_EMPTY);
                    int replaceTextMarker = result.indexOf(META_REPLACE_TEXT);
                    int replaceTextEndMarker = result.indexOf(META_REPLACE_END);

                    Logger.info("dbNoResultText:" + dbNoResultText + " replaceEmptyMarker:" + replaceEmptyMarker + " replaceTextMarker:" + replaceTextMarker);

                    String replaceEmptyText = result.substring(replaceEmptyMarker + META_REPLACE_EMPTY.length(), replaceTextMarker - 1);
                    String replaceTextText = result.substring(replaceTextMarker + META_REPLACE_TEXT.length(), replaceTextEndMarker - 1);
                    //String queryText = result.substring(replaceStartMarker+META_REPLACE_START.length(),replaceEmptyMarker-1);
                    String queryText = result.substring(replaceStartMarker + META_REPLACE_START.length(), replaceEmptyMarker);
                    Logger.info("result: " + result);
                    Logger.info("queryText: " + queryText);

    //REPLACESTART Formal Examination REPLACEEMPTY As far as I can tell, there is no formal exam.  REPLACETEXT Yes REPLACEEND 
                    Logger.info("replaceEmptyText:" + replaceEmptyText + " replaceTextText:" + replaceTextText);
                    if (queryText.contains(dbNoResultText)) {
                        Logger.info("contains dbNoResultText!");
                        result = result.substring(0, Integer.max(0, replaceStartMarker - 1))
                                + replaceEmptyText + result.substring(replaceTextEndMarker + META_REPLACE_END.length());
                    } else {
                        Logger.info("doesn't contain dbNoResultText!");

                        if (!replaceTextText.trim().isEmpty()) {
                            Logger.info("replaceTextText wasn't empty");
                            result = result.substring(0, Integer.max(0, replaceStartMarker - 1))
                                    + replaceTextText + result.substring(replaceTextEndMarker + META_REPLACE_END.length());
                        } else {  // no replace text so use original database result.
                            Logger.info("replaceTextText was empty");

                            result = result.substring(0, Integer.max(0, replaceStartMarker - 1))
                                    + result.substring(replaceStartMarker + META_REPLACE_START.length(), replaceEmptyMarker - 1)
                                    + result.substring(replaceTextEndMarker + META_REPLACE_END.length());

                        }
                    }
                }
            }
        }

        return result;
    }

    // only context variable literals to date..
    /**
     *
     * @param input
     * @param preview
     * @return
     */
    public static String parseOtherTerms(String input, boolean preview) {
        String result = input;
        // check to see if the user's context variable overrides a system version..
        String userVarName;
        ContextVariable sysVar = null;
        int context;
        int ruleId = DialogMain.getDialogUserList().getCurrentLastInferencedRuleId();

        //Logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  My raw input is : " + input);
        while (result.contains(getStartTag(CONTEXT_LITERAL_TYPE)) || result.contains(getStartTag(CONTEXT_NUMBER_TYPE))) {
            if (result.contains(getStartTag(CONTEXT_LITERAL_TYPE))) {

                String currentMarker = getNextTag(result, CONTEXT_LITERAL_TYPE);
                String contextVariableName = getTagValue(currentMarker, CONTEXT_LITERAL_TYPE);
                userVarName = contextVariableName.replace("@SYSTEM", "@");
               // userVarName = contextVariableName.replace("@SYSTEM", "");
                ContextVariableUser var;
                
                Logger.info("Calling getCurrentContextVariables...");
                var = DialogMain.getDialogUserList().getCurrentContextVariables().get(userVarName);
                if (var != null)
                    Logger.info("Detected a context variable:" + contextVariableName + "(userVar is:" + userVarName+") with value: " + var.getValue());
                
                Logger.info("Current user is: " + DialogMain.getDialogUserList().getCurrentDialogUser().getUsername());
//                for (ContextVariableUser aCV : DialogMain.getDialogUserList().getCurrentContextVariables().values()) {
//                    Logger.info("All user variables - found: " + aCV.getVariableName() + " with value: " + aCV.getValue());
//                }

                //else    
                //var = DialogMain.getDialogUserList().getCurrentKAContextVariables().get(userVarName);
                String contextVariableValue = "";
                if (var != null) {
                    contextVariableValue = var.getValue();
                } else {
                    if (contextVariableName.contains("@SYSTEM")) {
                        //if (!preview)
                        context = DialogMain.getDialogUserList().getCurrentStackLevel();
                        //else 
                        //context = DialogMain.getDialogUserList().getCurrentLastKAInferencedRuleId();
                        //Logger.info("Calling override with " + contextVariableName + " and ruleId " + ruleId);
                        sysVar = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(contextVariableName, ruleId);
                        Logger.info("Looking for system context variable:" + contextVariableName);

                        //sysVar = DialogMain.globalContextVariableManager.getContextVariables().get(contextVariableName);
                        if (sysVar != null) {
                            contextVariableValue = sysVar.getSingleVariableValue();
                            Logger.info("System context variable: current value is:" + contextVariableValue);

                        }
                    }
                }
                result = replaceTag(result, currentMarker, contextVariableValue);
            } else if (result.contains(getStartTag(CONTEXT_NUMBER_TYPE))) {

                String currentMarker = getNextTag(result, CONTEXT_NUMBER_TYPE);
                String contextVariableName = getTagValue(currentMarker, CONTEXT_NUMBER_TYPE);
                userVarName = contextVariableName.replace("@SYSTEM", "@");
                //userVarName = contextVariableName.replace("@SYSTEM", "");

                ContextVariableUser var;
                //if (!preview)
                var = DialogMain.getDialogUserList().getCurrentContextVariables().get(userVarName);
                //else 
                //var = DialogMain.getDialogUserList().getCurrentKAContextVariables().get(userVarName);

                String contextVariableValue = "";
                if (var != null) {
                    contextVariableValue = var.getValue();
                } else {
                    if (contextVariableName.contains("@SYSTEM")) {

                        //if (!preview)
                        context = DialogMain.getDialogUserList().getCurrentStackLevel();
                        //else 
                        //context = DialogMain.getDialogUserList().getCurrentLastKAInferencedRuleId();
                        //  Logger.info("OUTPUTPARSER - user ruleId is: " + ruleId);
                        sysVar = DialogMain.globalContextVariableManager.getSystemContextVariableOverride(contextVariableName, ruleId);

                        if (sysVar != null) {
                            contextVariableValue = sysVar.getSingleVariableValue();
                        }
                    }
                }
                // now get the integer value appended to the end of the context variable value..

                result = replaceTag(result, currentMarker, contextVariableValue.replaceAll("[\\D]", ""));
            }
        }

        result = result.replace("\\n", "\n").replace("\\t", "\t");
        //Logger.info("Result is: "  + result);
        return result;
    }

    public static String parseAutonomousSystemTerms(String input, boolean preview) {
        String result = input;
        Transrotations robotCapabilities = DialogMain.getRobotCapabilities();

        if (robotCapabilities != null) {
            for (Transrotation transrotation : robotCapabilities.getTransrotations()) {
                if (result.contains(transrotation.getName())) {
                    Logger.info("About to send data to AS.. pre-filtered data is: " + result);
                    result = removeTag(result, transrotation.getName());
                    Logger.info(transrotation.getName() + " tag found and removed..");
                    try {
                        result = result + AutonomousSystemHandler.sendAndReceive(transrotation.getCommand());
                        Logger.info("AS returned:" + result);
                    } catch (IOException e) {
                        Logger.info("Problem with calling autonomous system send and receive..." + e.toString());
                    }
                }
            }
        }

//        if (result.contains(ROS_FORWARD)) {
//           Logger.info("About to send data to AS.. data is: " + result);
//
//            result = removeTag(result,ROS_FORWARD);
//            Logger.info("ROS_FORWARD tag found and removed..");
//
//            try {
//                result = result + AutonomousSystemHandler.sendAndReceive("x");
//                Logger.info("AS returned:" + result);
//
//            }
//            catch (IOException e) {
//                Logger.info("Problem with calling autonomous system send and receive..." + e.toString());
//            }
//        }
        return result;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
     
package rdr.cases;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.apps.Main;
import rdr.logger.Logger;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;
import cmcrdr.mysql.DBOperation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class CaseLoader {

    /**
     * insert case structure details into sqlite db
     * 
     * @param caseStructure in essence a wrapped linked list of named attributes
     */
    public static void inserCaseStructure(CaseStructure caseStructure){
        String[] columns = new String[3];
        String[] values = new String[3];
        
        // rule_id
        columns[0] = "attribute_id";
        columns[1] = "value_type_id";
        columns[2] = "attribute_name";
        // creation_date
        
        String[] attributeNames = caseStructure.getAttributeNameArray();
        for(int i=0; i< attributeNames.length; i++) {
            IAttribute cur_attr = caseStructure.getAttributeByName(attributeNames[i]);
            values[0] = Integer.toString(cur_attr.getAttributeId());
            values[1] = Integer.toString(cur_attr.getValueType().getTypeCode());
            values[2] = cur_attr.getName();
            DBOperation.insertQuery("tb_case_structure", columns, values, false);
            
            //if the attribute is categorical, add categorical valuse
            if(cur_attr.isThisType("CATEGORICAL")){
                inserCategoricalValues(cur_attr.getAttributeId(), cur_attr.getCategoricalValues());
            }
             
        }
    }
    
    
    
    /**
     * insert case structure details into sqlite db
     * 
     */
    public static void setCaseStructure() {
        CaseStructure caseStructure = DBOperation.getCaseStructure();
        Main.domain.setCaseStructure(caseStructure);
    }
    
    
    /**
     * insert categorical value for the case structure 
     * 
     * @param attribute_id the attribute id
     * @param catValues list of categorical values 
     */
    public static void inserCategoricalValues(int attribute_id, ArrayList<String> catValues){
        String[] columns = new String[2];
        String[] values = new String[2];
        
        columns[0] = "attribute_id";
        columns[1] = "value_name";
        
        for(int i=0; i< catValues.size(); i++) {
            values[0] = Integer.toString(attribute_id);
            values[1] = catValues.get(i);
            DBOperation.insertQuery("tb_categorical_value", columns, values, false);
        }
    }
    
    /**
     * create arff file
     * 
     * @throws java.lang.Exception FileNotFoundException
     */
    public static void createArffFileWithCaseStructure() throws Exception {
        try (PrintWriter writer = new PrintWriter(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff")) {
            writer.println("% DO NOT CHANGE CASE STRUCTURE.");
            writer.println("% CHAGING CASE STRUCTURE WILL CAUSE FAILURE IN RETRIEVING KNOWLEDGE BASE.");
            writer.println("@relation	" +  Main.domain.getDomainName());
            CaseStructure caseStructure = Main.domain.getCaseStructure();
            String[] attrNameArray = caseStructure.getAttributeNameArray();
            
            for (String attrName : attrNameArray) {
                IAttribute attr = caseStructure.getAttributeByName(attrName);
                writer.print("@attribute	" + attr.getName());
                writer.println("	" + convertAttributeStringFromRDRAttrToArff(attr));
            }
            writer.println("");
            writer.println("");
            writer.print("@data");
            
            writer.close();
        }
    }
    
    /**
     * insert case into arff
     * 
     * @param aCase case to be inserted into file
     * @throws java.io.FileNotFoundException thrown if file not found
     */
    public static void insertCase(Case aCase) throws FileNotFoundException{
        PrintWriter writer = new PrintWriter(new FileOutputStream(
             new File(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff"), 
                true /* append = true */)
        ); 
        //give a newline
        writer.append("\n");
        
        
        String caseValueString = "";
        
        LinkedHashMap<String, Value> valHashMap = aCase.getValues();       
        Set set = valHashMap.entrySet();
        Iterator iter = set.iterator();
        int cnt = 0;
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            Value value = (Value) me.getValue();
            String caseValue = value.getActualValue().toString();      
            if(caseValue.equals("")){
                caseValue="/null/";
            }
            caseValueString += caseValue;      
            cnt++;
            if(cnt!=valHashMap.size()){
                caseValueString += ",";
            }
        }
        writer.append(caseValueString);
        writer.close();
    }
    
    
    /**
     * import arff dataset into rdr case set
     * 
     * @throws java.lang.Exception FileNotFoundException thrown if file not found
     */
    public static void caseImportWithCaseStructureForInductCornerstoneCases() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff");
        if(source.getDataSet()!=null){
            Instances data = source.getDataSet();
            // setting class attribute if the data format does not provide this information
            // For example, the XRFF format saves the class attribute information as well
            if (data.classIndex() == -1){
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Construct case structure
            CaseStructure caseStructure = new CaseStructure();

            int attrAmount = source.getStructure().numAttributes();
            for(int i=0; i<attrAmount; i++) {
                Attribute arffAttr = source.getStructure().attribute(i);

                IAttribute convertedAttr = convertAttributeFromArffToRDRAttr(arffAttr);
                caseStructure.addAttribute(convertedAttr);
            }

            //set case structure
            Main.domain.setCaseStructure(caseStructure);


            // Add cases
            int caseAmount = source.getDataSet().numInstances();
            for(int i=0; i<caseAmount; i++) {

                Case rdrCase = new Case(caseStructure);

                // if case amount is bigger than 100, skip case loading and will load when user clicks the case
                if(i<=1){
                    Instance arffCase = source.getDataSet().instance(i);
                    String[] attrNames = caseStructure.getAttributeNameArray();


                    for(int j=0; j<attrAmount; j++) {
                        String attrName = attrNames[j];
                        String attrType = caseStructure.getAttributeByName(attrName).getValueType().getTypeName();
                        String valStr = "";
                        switch (attrType) {
                            case "CONTINUOUS":
                                double dVal = arffCase.value(j);
                                valStr = String.valueOf(dVal);
                                break;
                            case "CATEGORICAL":
                                valStr = arffCase.stringValue(j);
                                break;
                            default:
                                valStr = arffCase.stringValue(j);
                                break;
                        }
                        ValueType valType = caseStructure.getAttributeByName(attrName).getValueType();
                        Value val = new Value(valType, valStr);
                        rdrCase.getValues().put(attrName, val);
                    }
                }
                int caseId = i+1;
                rdrCase.setCaseId(caseId);

                //add case into allCaseSet
                Main.allCaseSet.addCase(rdrCase);
                
//                Main.workbench.setRuleSet(Main.KB);
//        
//                switch (Main.workbench.getReasonerType()) {
//                    case "MCRDR":
//                        {
//                            RuleSet inferenceResult = new RuleSet();
//                            inferenceResult.setRootRule(Main.KB.getRootRule());
//                            Main.workbench.setInferenceResult(inferenceResult);
//
//                            Main.workbench.setCurrentCase(rdrCase);
//                            Main.workbench.inference();
//                            inferenceResult = (RuleSet) Main.workbench.getInferenceResult();
//                            
//                            break;
//                        }
//                    case "SCRDR":
//                        {
//                            Main.workbench.setInferenceResult(Main.KB.getRootRule());
//                            
//                            Main.workbench.setCurrentCase(rdrCase);
//                            Main.workbench.inference();
//                            Rule inferenceResult = (Rule) Main.workbench.getInferenceResult();
//                            
//                            break;
//                        }
//                }
                        
                Logger.info("Case ("+ caseId + ") loading...");
            }
            if(caseAmount==0){
                Logger.info("There is no case can be imported");
            } else if(caseAmount==1){
                Logger.info("Total 1 case imported");
            } else {
                Logger.info("Total " + caseAmount + " cases imported");
            }
        } else {
            
            // Construct case structure
            CaseStructure caseStructure = new CaseStructure();

            int attrAmount = source.getStructure().numAttributes();
            for(int i=0; i<attrAmount; i++) {
                Attribute arffAttr = source.getStructure().attribute(i);

                IAttribute convertedAttr = convertAttributeFromArffToRDRAttr(arffAttr);
                caseStructure.addAttribute(convertedAttr);
            }

            //set case structure
            Main.domain.setCaseStructure(caseStructure);
        }
        
    }
    
    
    /**
     * import arff dataset into rdr case set
     * 
     * @throws java.lang.Exception FileNotFoundException thrown if file not found
     */
    public static void caseImportWithCaseStructure() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff");
        if(source.getDataSet()!=null){
            Instances data = source.getDataSet();
            // setting class attribute if the data format does not provide this information
            // For example, the XRFF format saves the class attribute information as well
            if (data.classIndex() == -1){
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Construct case structure
            CaseStructure caseStructure = new CaseStructure();

            int attrAmount = source.getStructure().numAttributes();
            for(int i=0; i<attrAmount; i++) {
                Attribute arffAttr = source.getStructure().attribute(i);

                IAttribute convertedAttr = convertAttributeFromArffToRDRAttr(arffAttr);
                caseStructure.addAttribute(convertedAttr);
            }

            //set case structure
            Main.domain.setCaseStructure(caseStructure);


            // Add cases
            int caseAmount = source.getDataSet().numInstances();
            for(int i=0; i<caseAmount; i++) {

                Case rdrCase = new Case(caseStructure);

                // if case amount is bigger than 100, skip case loading and will load when user clicks the case
                if(i<=1){
                    Instance arffCase = source.getDataSet().instance(i);
                    String[] attrNames = caseStructure.getAttributeNameArray();


                    for(int j=0; j<attrAmount; j++) {
                        String attrName = attrNames[j];
                        String attrType = caseStructure.getAttributeByName(attrName).getValueType().getTypeName();
                        String valStr = "";
                        switch (attrType) {
                            case "CONTINUOUS":
                                double dVal = arffCase.value(j);
                                valStr = String.valueOf(dVal);
                                break;
                            case "CATEGORICAL":
                                valStr = arffCase.stringValue(j);
                                break;
                            default:
                                valStr = arffCase.stringValue(j);
                                break;
                        }
                        ValueType valType = caseStructure.getAttributeByName(attrName).getValueType();
                        Value val = new Value(valType, valStr);
                        rdrCase.getValues().put(attrName, val);
                    }
                }
                int caseId = i+1;
                rdrCase.setCaseId(caseId);

                //add case into allCaseSet
                Main.allCaseSet.addCase(rdrCase);

                Logger.info("Case ("+ caseId + ") loading...");
            }
            if(caseAmount==0){
                Logger.info("There is no case can be imported");
            } else if(caseAmount==1){
                Logger.info("Total 1 case imported");
            } else {
                Logger.info("Total " + caseAmount + " cases imported");
            }
        } else {
            
            // Construct case structure
            CaseStructure caseStructure = new CaseStructure();

            int attrAmount = source.getStructure().numAttributes();
            for(int i=0; i<attrAmount; i++) {
                Attribute arffAttr = source.getStructure().attribute(i);

                IAttribute convertedAttr = convertAttributeFromArffToRDRAttr(arffAttr);
                caseStructure.addAttribute(convertedAttr);
            }

            //set case structure
            Main.domain.setCaseStructure(caseStructure);
        }
        
    }
    
    /**
     * import arff dataset into rdr case set
     * 
     * @throws java.lang.Exception FileNotFoundException thrown if file not found
     */
    public static void caseImport() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff");
        if(source.getDataSet()!=null){
            Instances data = source.getDataSet();
            // setting class attribute if the data format does not provide this information
            // For example, the XRFF format saves the class attribute information as well
            if (data.classIndex() == -1){
                data.setClassIndex(data.numAttributes() - 1);
            }
            // Getting case structure
            CaseStructure caseStructure = Main.domain.getCaseStructure();
            int attrAmount = Main.domain.getCaseStructure().getAttrAmount();

            // Add cases
            int caseAmount = source.getDataSet().numInstances();
            for(int i=0; i<caseAmount; i++) {
                Case rdrCase = new Case(caseStructure);

                // if case amount is bigger than 100, skip case loading and will load when user clicks the case
                if(i<=1){
                    Instance arffCase = source.getDataSet().instance(i);
                    String[] attrNames = caseStructure.getAttributeNameArray();


                    for(int j=0; j<attrAmount; j++) {
                        String attrName = arffCase.attribute(j).name();
                        if(caseStructure.getAttributeByName(attrName)!=null){
                            String attrType = caseStructure.getAttributeByName(attrName).getValueType().getTypeName();
                            String valStr = "";
                            switch (attrType) {
                                case "CONTINUOUS":
                                    double dVal = arffCase.value(j);
                                    valStr = String.valueOf(dVal);
                                    break;
                                case "CATEGORICAL":
                                    valStr = arffCase.stringValue(j);
                                    break;
                                default:
                                    valStr = arffCase.stringValue(j);
                                    break;
                            }
                            ValueType valType = caseStructure.getAttributeByName(attrName).getValueType();
                            Value val = new Value(valType, valStr);
                            rdrCase.getValues().put(attrName, val);
                        } else {
                            ValueType valType = new ValueType(ValueType.TEXT);
                            Value val = new Value(valType, "");
                            rdrCase.getValues().put(attrName, val);
                        }
                    }
                }
                int caseId = i+1;
                rdrCase.setCaseId(caseId);

                //add case into allCaseSet
                Main.allCaseSet.addCase(rdrCase);

                Logger.info("Case ("+ caseId + ") loading...");
            }
            if(caseAmount==0){
                Logger.info("There is no case can be imported");
            } else if(caseAmount==1){
                Logger.info("Total 1 case imported");
            } else {
                Logger.info("Total " + caseAmount + " cases imported");
            }
        } else {
            
            // Construct case structure
            CaseStructure caseStructure = new CaseStructure();

            int attrAmount = source.getStructure().numAttributes();
            for(int i=0; i<attrAmount; i++) {
                Attribute arffAttr = source.getStructure().attribute(i);

                IAttribute convertedAttr = convertAttributeFromArffToRDRAttr(arffAttr);
                caseStructure.addAttribute(convertedAttr);
            }

            //set case structure
            Main.domain.setCaseStructure(caseStructure);
        }
        
    }
    
    
    /**
     * load case using case id from arff dataset 
     * 
     * @param targetCaseId the case id to be loaded from arff file
     * @return  case loaded from arff file
     * @throws java.lang.Exception FileNotFoundException thrown if file not found
     */
    public static Case caseLoad(int targetCaseId) throws Exception {
        int arffCaseId = targetCaseId-1;
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff");
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1){
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        // Construct case structure
        CaseStructure caseStructure = Main.domain.getCaseStructure();
        
        int attrAmount = source.getStructure().numAttributes();
        
        //get arffCase
        Instance arffCase = source.getDataSet().instance(arffCaseId);
        
        String[] attrNames = caseStructure.getAttributeNameArray();

        Case rdrCase = new Case(caseStructure);

        for(int j=0; j<attrAmount; j++) {
            String attrName = arffCase.attribute(j).name();
            if(caseStructure.getAttributeByName(attrName)!=null){
                String attrType = caseStructure.getAttributeByName(attrName).getValueType().getTypeName();
                String valStr = "";
                switch (attrType) {
                    case "CONTINUOUS":
                        double dVal = arffCase.value(j);
                        valStr = String.valueOf(dVal);
                        break;
                    case "CATEGORICAL":
                        valStr = arffCase.stringValue(j);
                        break;
                    default:
                        valStr = arffCase.stringValue(j);
                        break;
                }
                ValueType valType = caseStructure.getAttributeByName(attrName).getValueType();
                Value val = new Value(valType, valStr);
                rdrCase.getValues().put(attrName, val);
            } else {
                ValueType valType = new ValueType(ValueType.TEXT);
                Value val = new Value(valType, "");
                rdrCase.getValues().put(attrName, val);
            }
        }

        rdrCase.setCaseId(targetCaseId);

        return rdrCase;
       
    }
    
    
    
    /**
     * import arff dataset into rdr case set (currently unused)
     * 
     * @return number of cases loaded from arff file
     * @throws java.lang.Exception FileNotFoundException thrown if file not found
     */
    public static int getCaseAmount() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff");
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1){
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        // Add cases
        int caseAmount = source.getDataSet().numInstances();
        
        return caseAmount;
    }
    
    /**
     * import arff case structure
     * @throws Exception FileNotFoundException thrown if file not found
     */
    public static void caseStructureImport() throws Exception{
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + ".arff");
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1){
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        // Construct case structure
        CaseStructure caseStructure = new CaseStructure();
        
        int attrAmount = source.getStructure().numAttributes();
        for(int i=0; i<attrAmount; i++) {
            Attribute arffAttr = source.getStructure().attribute(i);
            
            IAttribute convertedAttr = convertAttributeFromArffToRDRAttr(arffAttr);
            caseStructure.addAttribute(convertedAttr);
        }
        
        //set case structure
        Main.domain.setCaseStructure(caseStructure);
        Logger.info("Case structure imported successfully");
    }
    
    /**
     * imports cases from arff for testing purposes
     * @throws Exception FileNotFoundException thrown if file not found
     */
    public static void caseImportForTesting() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.getProperty("user.dir") + "/domain/cases/" + Main.domain.getDomainName() + "_testing.arff");
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1){
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        // Get case structure
        CaseStructure caseStructure = Main.domain.getCaseStructure();
        
        // Add cases
        int caseAmount = source.getDataSet().numInstances();
        for(int i=0; i<caseAmount; i++) {
            Instance arffCase = source.getDataSet().instance(i);
            String[] attrNames = caseStructure.getAttributeNameArray();
            
            LinkedHashMap<String, Value> values = new LinkedHashMap<>();
              
            for(int j=0; j<caseStructure.getAttrAmount(); j++) {
                String attrName = attrNames[j];
                String attrType = caseStructure.getAttributeByName(attrName).getValueType().getTypeName();
                String valStr = "";
                switch (attrType) {
                    case "CONTINUOUS":
                        double dVal = arffCase.value(j);
                        valStr = String.valueOf(dVal);
                        break;
                    case "CATEGORICAL":
                        valStr = arffCase.stringValue(j);
                        break;
                    default:
                        valStr = arffCase.stringValue(j);
                        break;
                }
                ValueType valType = caseStructure.getAttributeByName(attrName).getValueType();
                Value val = new Value(valType, valStr);
                values.put(attrName, val);
            }
            
            Case rdrCase = new Case(caseStructure, values);
            rdrCase.setCaseId(i+1);
            
            //add case into allCaseSet
            Main.testingCaseSet.addCase(rdrCase);
        }
    }
    
    /**
     * converts arff attribute into rdr attribute
     * 
     * @param arffAttr
     * @return 
     */
    private static IAttribute convertAttributeFromArffToRDRAttr(Attribute arffAttr){        
        String RDRAttrType = "";
        
        // check the attribute type of arff dataset
        if(arffAttr.isDate()){
            RDRAttrType = "Date";
        } else if(arffAttr.isNumeric()){
            RDRAttrType = "Continuous";
        } else if(arffAttr.isString()){
            RDRAttrType = "Text";
        } else if(arffAttr.isNominal()){
            RDRAttrType = "Categorical";            
        } 
        IAttribute convertedAttr = AttributeFactory.createAttribute(RDRAttrType);
        
        //if nominal type, add categories
        if(arffAttr.isNominal()){
            int valueAmount = arffAttr.numValues();
            for (int i=0; i<valueAmount; i++){
                // get single value of nominal values
                String valueInstance = arffAttr.value(i);
                // add categorical value
                convertedAttr.addCategoricalValue(valueInstance);
            }      
            if(convertedAttr.getCategoricalValues().size()==2){
                if(convertedAttr.getCategoricalValues().contains("true") && convertedAttr.getCategoricalValues().contains("false") ){
                    RDRAttrType = "Boolean";
                    convertedAttr = AttributeFactory.createAttribute(RDRAttrType);
                }
            }
        } 
        
        convertedAttr.setName(arffAttr.name());
        convertedAttr.setAttributeType("Case Attribute");
        convertedAttr.setValueType(new ValueType(RDRAttrType));
        
        
        return convertedAttr;
    }
    
    
    /**
     * converts rdr attribute into arff attribute string
     * 
     * @param arffAttr
     * @return 
     */
    private static String convertAttributeStringFromRDRAttrToArff(IAttribute RDRAttr){        
        String arffAttrType = "";
        
        if(RDRAttr.isThisType(ValueType.DATE)){
            arffAttrType =  "DATE \"yyyy-mm-dd hh:mm:ss.S\"";
        } else if(RDRAttr.isThisType(ValueType.CONTINUOUS)){
            arffAttrType =  "NUMERIC";
        } else if(RDRAttr.isThisType(ValueType.TEXT)){
            arffAttrType =  "STRING";
        } else if(RDRAttr.isThisType(ValueType.CATEGORICAL)){            
            arffAttrType =  "{";
            ArrayList<String> arrayList =RDRAttr.getCategoricalValues();
            for(int i=0; i<arrayList.size(); i++){
                String eachCat = arrayList.get(i);
                arffAttrType += eachCat;
                if( i < arrayList.size()-1) {
                    arffAttrType += ", ";
                }
            }
            arffAttrType += "}";
        } else if(RDRAttr.isThisType(ValueType.BOOLEAN)){
            arffAttrType =  "{true, false}";
        }
        return arffAttrType;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.apps;

import java.util.HashMap;
import java.util.LinkedHashMap;
import rdr.cases.Case;
import rdr.cases.CaseSet;
import rdr.cases.CaseStructure;
import rdr.model.Attribute;
import rdr.model.CategoricalAttribute;
import rdr.model.ValueType;
import rdr.model.Value;
import rdr.domain.Domain;

/**
 *
 * @author yangsokk
 * @author (modified by) David Chung
 */
public class SCRDRApplication {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        
        //Define doamin
        String domainName = "Census";
        String methodType = Domain.SCRDR;
        String dbName = "census";
        String description = "Predict whether or not personal income "
                + "is >50K or <=50K based on personal attribute";
                    
        Domain domain = new Domain (domainName, methodType, dbName, description);
        
        //Define attribute container
        CaseStructure caseStructure = new CaseStructure();
        
        //Create attribute and add
        Attribute age = new Attribute("age", "case", new ValueType(ValueType.CONTINUOUS));
        caseStructure.addAttribute(age);
        
        CategoricalAttribute edu = new CategoricalAttribute("education", "case", new ValueType(ValueType.CATEGORICAL));
        edu.addCategoricalValue("11th");
        edu.addCategoricalValue("HS-grad");
        edu.addCategoricalValue("Some-college");
        edu.addCategoricalValue("Assoc-acdm");
        edu.addCategoricalValue("Bachelors");
        edu.addCategoricalValue("Masters");
        caseStructure.addAttribute(edu);
        
        CategoricalAttribute meritalStatus = new CategoricalAttribute("maritalstatus", "case", new ValueType(ValueType.CATEGORICAL));
        meritalStatus.addCategoricalValue( "Married-civ-spouse");
        meritalStatus.addCategoricalValue("Never-married");
        meritalStatus.addCategoricalValue("Separated");        
        caseStructure.addAttribute(meritalStatus);
        
        CategoricalAttribute occupation = new CategoricalAttribute("occupation", "case", new ValueType(ValueType.CATEGORICAL));
        occupation.addCategoricalValue("Handlers-cleaners");
        occupation.addCategoricalValue("Exec-managerial");
        occupation.addCategoricalValue("Sales");   
        occupation.addCategoricalValue("Prof-specialty");
        occupation.addCategoricalValue("Adm-clerical");
        occupation.addCategoricalValue("Other-service");
        caseStructure.addAttribute(occupation);
        
        
        Attribute capitalGain = new Attribute("capitalgain", "case", new ValueType(ValueType.CONTINUOUS));
        caseStructure.addAttribute(capitalGain);
        Attribute hourPerWork = new Attribute("hourperwork", "case", new ValueType(ValueType.CONTINUOUS));
        caseStructure.addAttribute(hourPerWork);
        CategoricalAttribute income = new CategoricalAttribute("income", "class", new ValueType(ValueType.CATEGORICAL)); 
        income.addCategoricalValue("50>=");
        income.addCategoricalValue("50<");        
        caseStructure.addAttribute(income);
        
        //Create an instance for case set
        CaseSet caseSet = new CaseSet();
        
        //age, education, maritalstatus, occupation, capitalgain, hourperwork, income
        LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        values.put("age", new Value(new ValueType(ValueType.CONTINUOUS), 53));
        values.put("education", new Value(new ValueType(ValueType.CATEGORICAL), "11th"));
        values.put("maritalstatus", new Value(new ValueType(ValueType.CATEGORICAL), 
                "Married-civ-spouse"));
        values.put("occupation", new Value(new ValueType(ValueType.CATEGORICAL), 
                "Handlers-cleaners"));
        values.put("capitalgain", new Value(new ValueType(ValueType.CONTINUOUS), 0));
        values.put("hourperwork", new Value(new ValueType(ValueType.CONTINUOUS), 40));
        values.put("income", new Value(new ValueType(ValueType.CATEGORICAL), "<=50K"));
        //Create case
        Case newCase = new Case(caseStructure, values);
        //Add new case
        caseSet.addCase(newCase);
        
//        
//        //Initialise workbench
//        Workbench workbench = new Workbench();
//        //Initialise rule set
//        RuleSet ruleSet = new RuleSet();
//        //set rule set into workbench
//        workbench.setRuleSet(ruleSet);
//        //Set a case
//        workbench.setCurrentCase(newCase);
//        
//        SCRDRReasoner engine = new SCRDRReasoner(workbench);
//        engine.inference();
//        
//        //Get wrongly fired rule 
//        Rule currentFiredRule  = workbench.getInferenceResult().get(2);
//        
//        //Instantiate conclusion set
//        ConclusionSet conclusionSet = new ConclusionSet();
//        //Add nee conclusion
//        CategoricalAttribute conclusionAttribute = new CategoricalAttribute("income", "class", new ValueType("CATEGORICAL"));
//        Value value = new Value(new ValueType(ValueType.CATEGORICAL), "<=50K");
//        conclusionSet.addConclusion(new Conclusion(conclusionAttribute, value));
//        //Add new rule with the conclusion set
//        workbench.setNewRule(new Rule(conclusionSet));
//        //Get a bottom rule
//        Rule bottomRule  = workbench.getRuleSet().getBottomRuleOfChildDecisionList(currentFiredRule);
//        //Locate new rule to the wrong conclusion
//        workbench.getNewRule().setParent(currentFiredRule);
//        currentFiredRule.setException(workbench.getNewRule().getRuleId());
//
//        //Retrieve conflict cases based on condition
//        RDRLearner rulebuilder = new RDRLearner(workbench);
//        //Retrieve cornerstones
//        rulebuilder.findCornerstones();
//        //Find differenc attributes based on cornerstone cases
//        rulebuilder.findDifferenceAttributes();
//
//        //Add condition
//        ConditionSet conditionSet = new ConditionSet();
//        Attribute conditionAttribute = new Attribute("age", "case", new ValueType("CONTINUOUS"));
//        Value conditionValue = new Value(new ValueType(ValueType.CATEGORICAL), "<=50K");
//        conditionSet.addCondition(new Condition(conditionAttribute, Operator.GREATER_THAN, conditionValue));
//        //Set condition set
//        workbench.getNewRule().setConditionSet(conditionSet);
//        
    }
}

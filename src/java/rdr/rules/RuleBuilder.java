/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.rules;

import rdr.cases.CaseStructure;
import rdr.model.Attribute;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class RuleBuilder {
    
    private static String defaultConclusion = "";
    private static boolean isDefaultConclusionSet = false;
    
    /**
     *
     * @param defaultConclusion
     */
    public static void setDefaultConclusion(String defaultConclusion){
        RuleBuilder.defaultConclusion = defaultConclusion;
        RuleBuilder.isDefaultConclusionSet= true;
    }
        
    /**
     * build root rule     
     * @return  
     */
    public static Rule buildRootRule(){
        Rule rootRule = new Rule();
                
        //root conclusion
        if(RuleBuilder.isDefaultConclusionSet){
            Attribute rootAttribute = new Attribute(Attribute.CLASS_TYPE, "Root Rule", ValueType.TEXT);   
            Value rootValue = new Value(ValueType.TEXT, RuleBuilder.defaultConclusion);
            Conclusion rootConclusion = new Conclusion(rootAttribute,rootValue);
            
            ConditionSet rootConditionSet = new ConditionSet();    

            rootRule.setRuleId(0);
            rootRule.setConditionSet(rootConditionSet);
            rootRule.setConclusion(rootConclusion);
            
        } else {
            Attribute rootAttribute = new Attribute(Attribute.CLASS_TYPE, "Root Rule", ValueType.NULL_TYPE);   
            Value rootValue = new Value(ValueType.NULL_TYPE, "");
            Conclusion rootConclusion = new Conclusion(rootAttribute,rootValue);
            
            ConditionSet rootConditionSet = new ConditionSet();    

            rootRule.setRuleId(0);
            rootRule.setConditionSet(rootConditionSet);
            rootRule.setConclusion(rootConclusion);
        }
        return rootRule;
    }
    
    /**
     * build root condition     
     * @param caseStructure
     * @param condOperStr
     * @param condAttrName
     * @param condValStr
     * @return  
     */
    public static Condition buildRuleCondition(CaseStructure caseStructure, String condAttrName, String condOperStr, String condValStr){
        
        IAttribute newAttr = caseStructure.getAttributeByName(condAttrName);
        
        Value newValue = new Value(newAttr.getValueType(), condValStr);
        Operator newOper = Operator.stringToOperator(condOperStr);
        
        Condition newCondition = new Condition(newAttr, newOper, newValue);
        
        return newCondition;
    }
    

    
    /**
     * build root rule     
     * @param originRule
     * @return  
     */
    public static Rule copyRule(Rule originRule){
        Rule newRule = new Rule();
        
        newRule.setRuleId(originRule.getRuleId());
        newRule.setConditionSet(originRule.getConditionSet());
        newRule.setConclusion(originRule.getConclusion());
        newRule.setChildRuleList(originRule.getChildRuleList());
        newRule.setCornerstoneCaseSet(originRule.getCornerstoneCaseSet());
        if(originRule.isParentExist()){
            newRule.setParent(originRule.getParent());
        } 
        newRule.setIsStoppingRule(originRule.getIsStoppingRule());
        newRule.setDoNotStack(originRule.getDoNotStack());
        
        return newRule;
    }
    
    
}

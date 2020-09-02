/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.ka;

import cmcrdr.cases.DialogCase;
import java.util.LinkedHashMap;
import rdr.apps.Main;
import rdr.cases.CornerstoneCaseSet;
import rdr.model.AttributeFactory;
import rdr.model.IAttribute;
import rdr.model.Value;
import rdr.model.ValueType;
import rdr.rules.Conclusion;
import rdr.rules.Condition;
import rdr.rules.Rule;
import rdr.rules.RuleBuilder;
import rdr.rules.RuleSet;


public class SelfLearningModule {    
    

    private String conditionStr;
    

    private String conclusionStr;
    

    private RuleSet inferenceResult;
    

    //private int checkedStackId = 0;
    
    

    public void initialise (){
        //this.checkedStackId = 0;
        this.conditionStr = null;
        this.conclusionStr = null;
        this.inferenceResult = null;
    }
    

    public void setConditionStr (String conditionStr){
        this.conditionStr = conditionStr;
    }
    

    public String getConditionStr (){
        return this.conditionStr;
    }
    

    public void setConclusionStr (String conclusionStr){
        this.conclusionStr = conclusionStr;
    }
    

    public String getConclusionStr (){
        return this.conclusionStr;
    }
    

    public void setInferenceResult (RuleSet inferenceResult){
        this.inferenceResult = inferenceResult;
    }
    

    public RuleSet getInferenceResult (){
        return this.inferenceResult;
    }
    
    
    public void executeKA(){
        CornerstoneCaseSet cornerstoneCaseSet = new CornerstoneCaseSet();
        Conclusion newConclusion;
        Conclusion wrongConclusion;
        Condition newCondition;
        Rule newRule = new Rule();
        
        wrongConclusion = this.inferenceResult.getLastRule().getConclusion();
        
        
        LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        values.put("Recent",new Value(new ValueType("TEXT"), conditionStr));
        
        DialogCase currentCase = new DialogCase(Main.domain.getCaseStructure(),values);
        currentCase.setCaseId(Main.allCaseSet.getNewCaseId());
        
        newCondition = RuleBuilder.buildRuleCondition(currentCase.getCaseStructure(), "Recent", "CONTAIN", conditionStr);
        
        
        IAttribute attribute = AttributeFactory.createAttribute("Text");
        Value value = new Value(new ValueType("TEXT"), conclusionStr);
        newConclusion = new Conclusion(attribute, value);
        
        Main.workbench.setLearner("MCRDR");
        Main.workbench.setRuleSet(Main.KB);
        Main.workbench.setCurrentCase(currentCase);
        Main.workbench.setInferenceResult(inferenceResult);
        
        cornerstoneCaseSet.addCase(currentCase);
        
        Main.workbench.setNewRule(newRule);
        Main.workbench.getNewRule().setCornerstoneCaseSet(cornerstoneCaseSet);
        
        Main.workbench.setWrongConclusion(wrongConclusion);
        Main.workbench.setNewRuleConclusion(newConclusion);     
        Main.workbench.getLearner().addConditionToNewRule(newCondition);      
        Main.workbench.executeAddingRule(false);
        
        Main.KB.setRuleSet(Main.workbench.getRuleSet());
        Main.KB.setRootRuleTree();
        Main.KB.retrieveCornerstoneCaseSet();
    }
    
    
}

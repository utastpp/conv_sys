/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.cases;

import rdr.rules.Rule;
import rdr.rules.RuleSet;

/**
 * This class is used to define a cornerstone case used in RDR
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class CornerstoneCase extends Case{
    
    /**
     * Wrong Rule Set
     */
    private RuleSet wrongRuleSet = new RuleSet();
    
    /**
     * Constructor.
     */
    public CornerstoneCase(){
        super();
    }
    
    /**
     * Constructs a case 
     *
     * @param aCase case to be used as the basis for this cornerstone case
     */
    public CornerstoneCase(Case aCase) {
        super(aCase);
    }
    
    /**
     * Set wrong rule set
     * @param wrongRuleSet the RuleSet to be associated with the wrong rule set
     */
    public void setWrongRuleSet(RuleSet wrongRuleSet){
        this.wrongRuleSet = wrongRuleSet;
    }
   
    /**
     * Get wrong rule set
     * @return the set of rules associated with the wrong rule set
     */
    public RuleSet getWrongRuleSet(){
        return this.wrongRuleSet;
    }
    
    /**
     * Add rule into wrong rule set
     * @param aRule rule to be added to the wrong rule set
     * @return  true if rule successfully added
     */
    public boolean addRuleToWrongRuleSet(Rule aRule){
        return this.wrongRuleSet.addRule(aRule);        
    }
    
    /**
     * Replace rule with existing rule in the wrong rule set
     * @param oldRule rule to be replaced
     * @param newRule rule being used as replace value
     * @return  true if replacement successful, false if old rule did not exist
     */
    public boolean replaceRuleWithExistingWrongRule(Rule oldRule, Rule newRule){
        if(this.wrongRuleSet.isRuleExist(oldRule)){
            return this.wrongRuleSet.addRule(newRule);
        } else {
            return false;
        }
    }
    
    /**
     * Clear wrong rule set.
     */
    public void clearWrongRuleSet(){
        this.wrongRuleSet = new RuleSet();
    }
    
}

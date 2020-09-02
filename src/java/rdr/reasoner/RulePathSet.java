/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.reasoner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.rules.Rule;
import rdr.rules.RuleSet;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class RulePathSet {
    
    private LinkedHashMap<Integer, RuleSet> rulePathSet = new LinkedHashMap<>();
    
    
    /**
     * Get clone MCRDRStackResultSet
     * @return 
     */
    public RulePathSet cloneMCRDRStackResultSet(){
        RulePathSet newRulePathSet = new RulePathSet();
        newRulePathSet.rulePathSet = (LinkedHashMap<Integer, RuleSet>) this.rulePathSet.clone();
        
        return newRulePathSet;
    }    
    
    /**
     * Get base set
     * @return 
     */
    public LinkedHashMap<Integer, RuleSet> getBaseSet(){
        return this.rulePathSet;
    }    
    
    /**
     * Get size of repository
     * @return 
     */
    public int getSize(){
        if(this.rulePathSet.isEmpty()){
            return 0;
        } else {
            return this.rulePathSet.size();
        }
        
    }    
    
    /**
     * clear rule path set.
     */
    public void clearSet(){
        this.rulePathSet.clear();
    }    
    
    /**
     * Add RuleSet 
     * @param aRuleSet
     */
    public void addRuleSet(RuleSet aRuleSet){
        boolean isNewRuleSet = true;
        Set rulePaths = this.rulePathSet.entrySet();
        Iterator iterator = rulePaths.iterator();
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            RuleSet existingRuleSet = (RuleSet)me.getValue();
            if(aRuleSet.getLastRule().isRuleAncestor(existingRuleSet.getLastRule())){
                isNewRuleSet = false;
                existingRuleSet.combineRuleSet(aRuleSet);
            }
        }
        if(isNewRuleSet){
            this.rulePathSet.put(this.rulePathSet.size(), aRuleSet);
        }
    }    
    
    
    /**
     * Get Last Node of rulepaths in the set
     * @return 
     */
    public Rule[] getLastNodesOfEachRulePath(){
        Rule[] lastNodesOfEachRulePath = new Rule[this.rulePathSet.size()];
        
        Set rulePaths = this.rulePathSet.entrySet();
        Iterator iterator = rulePaths.iterator();
        int i=0;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();    
            RuleSet aRuleSet = (RuleSet)me.getValue();
            
            lastNodesOfEachRulePath[i] = aRuleSet.getLastRule();
            i++;
        }
            
        return lastNodesOfEachRulePath;
    }    
    
}

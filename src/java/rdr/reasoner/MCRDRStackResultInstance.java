/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.reasoner;

import rdr.rules.RuleSet;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class MCRDRStackResultInstance {
    private int stackId;
    private int caseId;
    private RuleSet inferenceResult;
    private boolean isRuleFired = false;
    
    /**
     *
     * @param stackId
     */
    public void setStackId(int stackId){
        this.stackId = stackId;
    }
    
    /**
     *
     * @return
     */
    public int getStackId(){
        return this.stackId;
    }
    
    /**
     *
     * @param caseId
     */
    public void setCaseId(int caseId){
        this.caseId = caseId;
    }
    
    /**
     *
     * @return
     */
    public int getCaseId(){
        return this.caseId;
    }
    
    /**
     *
     * @param inferenceResult
     */
    public void setInferenceResult(RuleSet inferenceResult){
        this.inferenceResult = inferenceResult;
    }
    
    /**
     *
     * @return
     */
    public RuleSet getInferenceResult(){
        return this.inferenceResult;
    }
    
    /**
     *
     * @param isRuleFired
     */
    public void setIsRuleFired(boolean isRuleFired){
        this.isRuleFired = isRuleFired;
    }
    
    /**
     *
     * @return
     */
    public boolean getIsRuleFired(){
        return this.isRuleFired;
    }
    
}

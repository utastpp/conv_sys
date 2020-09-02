/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dialog;


import cmcrdr.handler.OutputParser;
import cmcrdr.logger.Logger;


/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class SystemDialogInstance extends DialogInstance {
    
    /**
     * Case ID that is used for construct this dialog
     */
    private int derivedCaseId;
    
    private String derivedCaseRecentData;
    /**
     * processing ID 
     */
    private int stackId;
    
    
    /**
     * true if there is inference result (rule is fired)
     */
    private boolean isRuleFired=false;
    
    
    
    /**
     * Dialog LastRuleNode?
     */
    private boolean isLastRuleNode = false;
    
    
    
    /**
     * Constructor
     */
    public SystemDialogInstance() {
        super(DialogInstance.SYSTEM_TYPE);
    }
     /**
     * Constructor
     * @param aDialog the dialog instance to be used as the basis of the new instance
     */
    public SystemDialogInstance(SystemDialogInstance aDialog) {
        super(aDialog);
        this.derivedCaseId = aDialog.derivedCaseId;
        
    }

    /**
     *
     * @param caseId
     */
    @Override
    public void setDerivedCaseId(int caseId){
        this.derivedCaseId = caseId;
    }

    /**
     *
     * @return
     */
    @Override
    public int getDerivedCaseId(){
        return this.derivedCaseId;
    }
    
    // DPH

    /**
     *
     * @param data
     */
    public void setDerivedCaseRecentData(String data){
        this.derivedCaseRecentData = data;
    }
    
    // DPH

    /**
     *
     * @return
     */
    public String getDerivedCaseRecentData(){
        return this.derivedCaseRecentData;
    }

    /**
     *
     * @param stackId
     */
    @Override
    public void setStackId(int stackId){
        this.stackId = stackId;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStackId(){
        return this.stackId;
    }
    
    /**
     *
     * @param isRuleFired
     */
    @Override
    public void setIsRuleFired(boolean isRuleFired){
        this.isRuleFired = isRuleFired;
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean getIsRuleFired(){
        return this.isRuleFired;
    }
    
    /**
     *
     * @param isLastRuleNode
     */
    @Override
    public void setIsLastRuleNode(boolean isLastRuleNode){
        this.isLastRuleNode = isLastRuleNode;
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean getIsLastRuleNode(){
        return this.isLastRuleNode;
    }
    
    /**
     *
     * @return
     */
    public String getDatabaseDialogStr(){
        if(!this.dialogStr.equals("noResponse")){
            return this.dialogStr;
        } else {
            return "";
        }
    }
    
    /**
     *
     * @return
     */
    public String toConsoleString(){  // used by conversationHistory in IDSAdminGUI et al
        String result = this.getDialogId() + " ";
        
        if(this.dialogType==DialogInstance.USER_TYPE && this.dialogStr==""){
            result += "I'm sorry, I don't understand..";
        } else if(this.dialogType==DialogInstance.SYSTEM_TYPE && this.dialogStr=="noResponse"){            
            result += "";
        } else if(this.dialogType==DialogInstance.SYSTEM_TYPE && this.dialogStr==""){
            result += "";
        } else {           
            result += this.getDialogTag()+this.dialogStr + "\n";          
        }
        
        return result;
    }
 
    /**
     *
     * @return
     */
    public String getParsedDialogStr(){  // spawned child uses this in IDSAdminGUI 
        String result = ""; 
        
        if(!this.dialogStr.equals("noResponse")) {
            result = this.dialogStr;
            result = OutputParser.replaceAllDatabaseTerms(result,false);
            //Logger.info("Calling parseOtherTerms..");
            result = OutputParser.parseOtherTerms(result,false);
            result = OutputParser.parseMetaTerms(result);  
            result = result.trim();
            //result = result.replaceAll("^\\.\\</PRE\\>", "</PRE>");
        } 
        
        return result;
    }
    
    public static String getParsedDialogStrForKA(String inputString){  // spawned child uses this in IDSAdminGUI 
        String result = inputString; 
        result = OutputParser.replaceAllDatabaseTerms(result,false);
        Logger.info("Calling parseOtherTerms..");
        result = OutputParser.parseOtherTerms(result,false);
        result = OutputParser.parseMetaTerms(result);           
        result = result.trim();
        
        return result;
    }
    
}

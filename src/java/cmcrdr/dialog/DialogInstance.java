/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dialog;

import cmcrdr.event.EventInstance;


public class DialogInstance implements IDialogInstance {
    
    /**
     * USER dialog type
     */
    public static final int USER_TYPE = 1;
    
    /**
     * SYSTEM dialog type
     */
    public static final int SYSTEM_TYPE = 2;
    
    /**
     * EVENT log type
     */
    public static final int EVENT_TYPE = 3;
    
    /**
     * INFO log type
     */
    public static final int INFO_TYPE = 4;
    
    

    /**
     * Dialog Identifier
     */
    private int dialogId;    
    
    /**
     *
     */
    public static int maxId = 0;
    
    /**
     * Dialog type 1=USER_TYPE, 2=SYSTEM_TYPE, 3=EVENT_TYPE, 4=INFO_TYPE
     */
    protected int dialogType;
    
    /**
     * Dialog String
     */
    protected String dialogStr = "";
    

    
    /**
     * Constructor
     * @param dialogType partial creation of a new dialog instance - types are 1=USER_TYPE, 2=SYSTEM_TYPE, 3=EVENT_TYPE
     */
    public DialogInstance(int dialogType) {
        this.dialogId = -1;
        this.dialogType = dialogType;
        this.dialogStr = "";
    }
    
    
    /**
     * Constructor
     * @param aDialog the dialog to be used as the basis of the new dialog
     */
    public DialogInstance(DialogInstance aDialog) {
        this.dialogId = aDialog.dialogId;
        this.dialogType = aDialog.dialogType;
        this.dialogStr = aDialog.dialogStr;
        if (this.dialogId >= maxId)
            maxId = this.dialogId;
    }
    
    /**
     * Create a clone of the current DialogInstance 
     * @return the copy of the current DialogInstance
     */
    @Override
    public IDialogInstance cloneDialogInstance(){
        String prefix = "";
        switch (this.dialogType) {
            case DialogInstance.USER_TYPE:
                prefix = "UserDialog";
                break;
            case DialogInstance.SYSTEM_TYPE:
                prefix = "SystemDialog";
                break;
            case DialogInstance.EVENT_TYPE:
                prefix = "EventLog";
                break;
            case DialogInstance.INFO_TYPE:
                prefix = "InfoLog";
                break;
            default:
                break;
        }
        IDialogInstance aDialogInstance = DialogInstanceFactory.createDialogInstance(prefix);
        aDialogInstance.setDialogId(this.dialogId);
        aDialogInstance.setDialogTypeCode(this.dialogType);
        aDialogInstance.setDialogStr(this.dialogStr);   
        
        switch (this.dialogType) {
            case DialogInstance.USER_TYPE:
                aDialogInstance.setGeneratedCaseId(this.getGeneratedCaseId());
                break;
            case DialogInstance.SYSTEM_TYPE:
                aDialogInstance.setDerivedCaseId(this.getDerivedCaseId());
                aDialogInstance.setStackId(this.getStackId());
                aDialogInstance.setIsRuleFired(this.getIsRuleFired());
                break;
            case DialogInstance.EVENT_TYPE:
                aDialogInstance.setGeneratedCaseId(this.getGeneratedCaseId());
                break;
            case DialogInstance.INFO_TYPE:
                aDialogInstance.setGeneratedCaseId(this.getGeneratedCaseId());
                break;
            default:
                break;
        }
        
        
        return aDialogInstance;
    }
    
    /**
     *
     * @param dialogId
     */
    @Override
    public void setDialogId(int dialogId){
        this.dialogId = dialogId;
        if (dialogId >= maxId)
            maxId = dialogId;
    }
    
    /**
     *
     * @return
     */
    @Override
    public int getDialogId(){
        return this.dialogId;
    }
    
    /**
     *
     * @return
     */
    public int getMaxId() {
        return maxId;
    }
    
    /**
     *
     * @param dialogType
     */
    @Override
    public void setDialogType(String dialogType){
        switch (dialogType.toUpperCase()) {
            case "USER_TYPE":
                // 1
                this.dialogType = DialogInstance.USER_TYPE;
                break;
            case "SYSTEM_TYPE":
                // 2
                this.dialogType = DialogInstance.SYSTEM_TYPE;
                break;
            case "EVENT_TYPE":
                // 3
                this.dialogType = DialogInstance.EVENT_TYPE;
                break;
            case "INFO_TYPE":
                // 4
                this.dialogType = DialogInstance.INFO_TYPE;
                break;
            default:
                break;
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getDialogType(){
        switch (this.dialogType) {
            case DialogInstance.USER_TYPE:
                return "USER_TYPE";
            case DialogInstance.SYSTEM_TYPE:
                return "SYSTEM_TYPE";
            case DialogInstance.EVENT_TYPE:
                return "EVENT_TYPE";
            case DialogInstance.INFO_TYPE:
                return "INFO_TYPE";
            default:
                break;
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getDialogTag(){
        switch (this.dialogType) {
            case DialogInstance.USER_TYPE:
                return "[User]: ";
            case DialogInstance.SYSTEM_TYPE:
                return "[IDS ]: ";
            case DialogInstance.EVENT_TYPE:
                return "[Event] ";
            case DialogInstance.INFO_TYPE:
                return "[INFO] "; 
            default:
                break;
        }
        return null;
    }
    
    /**
     *
     * @param dialogTypeCode
     */
    @Override
    public void setDialogTypeCode(int dialogTypeCode){
        this.dialogType = dialogTypeCode;
    }
    
    /**
     *
     * @return
     */
    @Override
    public int getDialogTypeCode(){
        return this.dialogType;
    }
    
    /**
     *
     * @param dialogStr
     */
    @Override
    public void setDialogStr(String dialogStr){
        this.dialogStr = dialogStr;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getDialogStr(){
        if(!this.dialogStr.equals("noResponse")){
           return this.dialogStr;
        } else {
            return "";
        }
    }

    /**
     *
     * @param caseId
     */
    @Override
    public void setGeneratedCaseId(int caseId){
        throw new UnsupportedOperationException("Only supported in UserDialogInstance or EventDialogInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public int getGeneratedCaseId(){
        throw new UnsupportedOperationException("Only supported in UserDialogInstance or EventDialogInstance."); 
    }

    /**
     *
     * @param caseId
     */
    @Override
    public void setDerivedCaseId(int caseId){
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public int getDerivedCaseId(){
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }

    /**
     *
     * @param stackId
     */
    @Override
    public void setStackId(int stackId) {
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public int getStackId() {
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }

    /**
     *
     * @param isProcessingNull
     */
    @Override
    public void setIsRuleFired(boolean isProcessingNull){
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getIsRuleFired(){
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }
    
    /**
     *
     * @param isLastRuleNode
     */
    @Override
    public void setIsLastRuleNode(boolean isLastRuleNode){
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean getIsLastRuleNode(){
        throw new UnsupportedOperationException("Only supported in SystemDialogInstance."); 
    }

   
    
    @Override
    public String toString(){
        String result = this.getDialogId() + " ";
        
        if(this.dialogType==DialogInstance.USER_TYPE && this.dialogStr==""){
            result += "I'm sorry, I don't understand..";
        } else if(this.dialogType==DialogInstance.SYSTEM_TYPE && this.dialogStr=="noResponse"){            
            result += "";
        } else if(this.dialogType==DialogInstance.SYSTEM_TYPE && this.dialogStr==""){
            result += "";
        } else {
             result += this.getDialogTag()+this.dialogStr;
        }
        
        return result;
    }

    /**
     *
     * @param eventInstance
     */
    @Override
    public void setEventInstance(EventInstance eventInstance) {
        throw new UnsupportedOperationException("Only supported in EventLogInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public EventInstance getEventInstance() {
        throw new UnsupportedOperationException("Only supported in EventLogInstance."); 
    }
    
}

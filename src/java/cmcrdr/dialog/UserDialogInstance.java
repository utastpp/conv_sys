/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dialog;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class UserDialogInstance extends DialogInstance {
    
    /**
     * Case ID that is generated by this dialog
     */
    private int generatedCaseId;
    
    /**
     * Constructor
     */
    public UserDialogInstance() {
        super(DialogInstance.USER_TYPE);
    }
     /**
     * Constructor
     * @param aDialog the dialog instance to be used as the basis of the new instance
     */
    public UserDialogInstance(UserDialogInstance aDialog) {
        super(aDialog);
    }
        
    /**
     *
     * @param caseId
     */
    @Override
    public void setGeneratedCaseId(int caseId){
        this.generatedCaseId = caseId;
    }

    /**
     *
     * @return
     */
    @Override
    public int getGeneratedCaseId(){
        return this.generatedCaseId;
    }
    
    /**
     *
     * @return
     */
    public String toConsoleString(){  // used by conversationHistory in IDSAdminGUI et al
        String result = this.getDialogId() + " ";
        
        if(this.dialogType==DialogInstance.USER_TYPE && this.dialogStr==""){
            result += "I'm sorry, I don't understand..";
        } else {           
            result += this.getDialogTag()+this.dialogStr + "\n";          
        }
        
        return result;
    }
}

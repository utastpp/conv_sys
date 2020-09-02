/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.responses;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import cmcrdr.command.ICommandInstance;
import cmcrdr.dialog.SystemDialogInstance;
import rdr.rules.RuleSet;


public class SystemResponse {
    
    //private int myId;
    
    //private int conclusionId;

    private RuleSet inferenceResult = new RuleSet();
  
    private SystemDialogInstance responseDialogInstance;
    
    private SystemDialogInstance dbReponseDialogInstance = null;
    
    // DPH May 2019
    private ArrayList<ICommandInstance> responseCommandInstances = new ArrayList<>();
    //private ICommandInstance responseCommandInstance;

    public SystemResponse(){
        super();        
    }
    
    // DEAR ME - need to make responseCommandInstance a list as in MCRDR we might have several of them, not just one!
    
    public void setInferenceResult(RuleSet inferenceResult){
        this.inferenceResult = inferenceResult;
    }
    

    public RuleSet getInferenceResult(){
        return this.inferenceResult;
    }
    

    public void setResponseDialogInstance(SystemDialogInstance responseDialogInstance){
        this.responseDialogInstance = responseDialogInstance;
    }
    
    public void setDBResponseDialogInstance(SystemDialogInstance responseDialogInstance){
        this.dbReponseDialogInstance = responseDialogInstance;
    }
    
    public SystemDialogInstance getResponseDialogInstance(){
        return this.responseDialogInstance;
    }
    
    public SystemDialogInstance getDBResponseDialogInstance(){
        return this.dbReponseDialogInstance;
    }
    
    public void addResponseCommandInstance(ICommandInstance responseCommandInstance){
        
        this.responseCommandInstances.add(responseCommandInstance);
    }
    
    // DPH May 2019
    public void setResponseCommandInstanceList(ArrayList<ICommandInstance> responseCommandInstance){
        this.responseCommandInstances = responseCommandInstance;
    }
    
    //DPH May 2019
    public ArrayList<ICommandInstance> getResponseCommandInstanceList(){
        return this.responseCommandInstances;
    }
    
    
    
    
    @Override
    public String toString() {
        String result = "";
        //result = "[" + this.myId + "] " + this.inferenceResult.getLastRule().toString() + "\n"
        result = this.inferenceResult.getLastRule().toString() + "\n"
                + this.responseDialogInstance + "\n" 
                + this.responseDialogInstance;
        
        return result;
    }
    
    
    
}

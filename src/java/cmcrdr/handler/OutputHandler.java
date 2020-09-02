/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.handler;

import cmcrdr.cases.DialogCase;
import cmcrdr.dialog.SystemDialogInstance;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import cmcrdr.responses.SystemResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import rdr.apps.Main;
import rdr.reasoner.MCRDRStackResultInstance;
import cmcrdr.command.ICommandInstance;


public class OutputHandler {
    

    private DialogCase aDialogCase;     
    

    private SystemResponse systemResponse;    


    public void setDialogCase(DialogCase aDialogCase){
        this.aDialogCase = aDialogCase;
    }
    

    public DialogCase getDialogCase(){
        return this.aDialogCase;
    }
    

    public void setSystemResponse(SystemResponse systemResponse){
        this.systemResponse = systemResponse;
    }
    

    public SystemResponse getSystemResponse(){
        return this.systemResponse;
    }
    
    

    public String[] generateOutput() {
       String[] output = new String[3];
       
       // send the user dialog the db output (if any) but leave the actual conclusion as is
       SystemDialogInstance response = this.systemResponse.getDBResponseDialogInstance();
        if (response != null) {
            output[0] = response.getDialogStr();
            Logger.info("Reference database output: " + output[0]);
        }
        else {
            output[0] = this.systemResponse.getResponseDialogInstance().getDialogStr();

        }
        
        // DPH May 2019 - for some reason I never use the output here..
        // but need to iterate anyway for possible multiple actions occuring in the one system response
        output[1] = "";
        output[2] = "";
        for (ICommandInstance aCommand: this.systemResponse.getResponseCommandInstanceList()) {
            if (!output[1].equals(""))
               output[1] += ";";
            if (!output[2].equals(""))
               output[2] += ";";
            output[1] += aCommand.getTargetDeviceName();
            output[2] += aCommand.getDeviceAction(); 
        }
        
        //output[1] = this.systemResponse.getResponseCommandInstance().getTargetDeviceName();
        //output[2] = this.systemResponse.getResponseCommandInstance().getDeviceAction(); 
        
        return output;
    }
    


    public String[] processOutput() {

        String[] output = generateOutput();
                
        this.aDialogCase.setSystemResponse(this.systemResponse);
        SystemDialogInstance systemDialogInstance = this.systemResponse.getResponseDialogInstance();
        
        int stackId = systemDialogInstance.getStackId();
        
        if(this.systemResponse.getInferenceResult().getLastRule().getRuleId() == 0){
            Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).setIsRuleFired(false);
            systemDialogInstance.setIsRuleFired(false);
        } else {
            Main.workbench.getStackedInferenceResult().getMCRDRStackResultInstanceById(stackId).setIsRuleFired(true);
            systemDialogInstance.setIsRuleFired(true);
        }
                
        if(this.systemResponse.getInferenceResult().getLastRule().isParentExist()){
                        
            Set stackResultSet = Main.workbench.getStackedInferenceResult().getBaseSet().entrySet();
            //Logger.info("system response has a parent:" + this.systemResponse.getInferenceResult().getLastRule().toString());
            //Logger.info("\tThe parent:" + this.systemResponse.getInferenceResult().getLastRule().getParent().toString());

            Iterator iterator = stackResultSet.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();    
                MCRDRStackResultInstance aMCRDRStackResultInstance = (MCRDRStackResultInstance)me.getValue();
                //Logger.info("Stack key:" + (int)me.getKey());
                //Logger.info("Examining :" + aMCRDRStackResultInstance.getInferenceResult().getLastRule().toString());
                if(this.systemResponse.getInferenceResult().getLastRule().isParentExist()){
                    if(this.systemResponse.getInferenceResult().getLastRule().getParent().getRuleId() == aMCRDRStackResultInstance.getInferenceResult().getLastRule().getRuleId()){
                        if(aMCRDRStackResultInstance.getInferenceResult().getLastRule().getRuleId()!=0){
                            int prevValidCaseId = aMCRDRStackResultInstance.getCaseId();
                            SystemDialogInstance aSystemDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(prevValidCaseId);
                            aSystemDialogInstance.setIsLastRuleNode(false);
                            this.systemResponse.getResponseDialogInstance().setIsLastRuleNode(true);
                            //Logger.info("\t1 Setting NOT LAST user's system dialog instance - " + aSystemDialogInstance.getDialogStr() );
                            //Logger.info("\t1 Setting LAST system response - " + this.systemResponse.getResponseDialogInstance().getDialogStr());

                        }

                    } else {
                        if(aMCRDRStackResultInstance.getInferenceResult().getLastRule().getRuleId()!=0){
                            int prevValidCaseId = aMCRDRStackResultInstance.getCaseId();
                            SystemDialogInstance aSystemDialogInstance = DialogMain.getDialogUserList().getCurrentDialogRepository().getSystemDialogInstanceByDerivedCaseId(prevValidCaseId);
                            aSystemDialogInstance.setIsLastRuleNode(true);
                            this.systemResponse.getResponseDialogInstance().setIsLastRuleNode(true);
                            //Logger.info("\t2 Setting LAST user's system dialog instance - " + aSystemDialogInstance.getDialogStr());
                            //Logger.info("\t2 Setting LAST system response - " + this.systemResponse.getResponseDialogInstance().getDialogStr());
                        }
                    }
                }
            }
        }
        
        //Logger.info("final value of output is :" + output[0]);
        return output;        
    }
}

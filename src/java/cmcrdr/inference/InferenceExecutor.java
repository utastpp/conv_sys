/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.inference;

import cmcrdr.cases.DialogCase;
import cmcrdr.responses.SystemResponse;
import cmcrdr.responses.SystemResponseGenerator;


/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class InferenceExecutor  {
    
    /**
     *
     * @param aCase
     * @return
     */
    public static SystemResponse requestResponse(DialogCase aCase) {

        SystemResponse systemResponse = SystemResponseGenerator.generateResponse(aCase);
        
        //Logger.info(systemResponse.toString());
        
        return systemResponse;
    }
    
}

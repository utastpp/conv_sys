/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.knowledgeacquisition;

/**
 *
 * @author dherbert
 */
public class StatusResponse {
    String result;
    String status;
        
    public void setResult(String theResult) {
        result = theResult;
    }

    public void setStatus(String theStatus) {
        status = theStatus;
    }
}

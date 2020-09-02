/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.contextvariable;

import java.util.ArrayList;

/**
 *
 * @author dherbert
 */
public class ContextVariableTokenisedResults {
    
    private ArrayList<String> sourceTokens;
    private ArrayList<String> resultTokens;
    private String processedInput;
    private String resultString;

    public ContextVariableTokenisedResults(ArrayList<String> sourceTokens, ArrayList<String> resultTokens, String processedInputString, String resultString) {
        this.sourceTokens = sourceTokens;
        this.resultTokens = resultTokens;
        this.processedInput = processedInputString;
        this.resultString = resultString;
    }
    
    public ArrayList<String> getSourceTokens() {
        return sourceTokens;
    }
    
    public ArrayList<String> getResultTokens() {
        return resultTokens;
    }
    
    public String getProcessedInputString() {
        return processedInput;
    }
    
    public String getResultString() {
        return resultString;
    }
    
    public void setSourceTokens(ArrayList<String> theSource) {
        sourceTokens = theSource;
    }
    
    public void setResultTokens(ArrayList<String> theResults) {
        resultTokens = theResults;
    }   
    
    public void setProcessedInputString(String processedInputString) {
        processedInput = processedInputString;
    }
    
    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
    
}

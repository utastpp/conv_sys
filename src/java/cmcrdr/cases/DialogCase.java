/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.cases;

import cmcrdr.dialog.IDialogInstance;
import cmcrdr.responses.SystemResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import rdr.cases.Case;
import rdr.cases.CaseStructure;
import rdr.model.Value;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DialogCase extends Case{
    
    /**
     * DialogCase를 생성하기 위해 사용되어진 DialogInstance - EventLogInstance 또는 UserDialogInstance.
     */
    IDialogInstance inputDialogInstance;
    
    /**
     * DialogCase가 추론 결과에 의해 생성된 SystemResponse.
     */
    SystemResponse aSystemResponse;
    

    
    
    /**
     * Constructor.
     */
    public DialogCase(){
        super();
    }
    
    /**
     * Constructor.
     * @param caseStructure
     * @param values
     */
    public DialogCase(CaseStructure caseStructure, LinkedHashMap<String, Value> values){    
        super(caseStructure, values);
    }
    
    /**
     *
     * @return
     */
    public DialogCase cloneCase(){
        DialogCase aCase = new DialogCase(this.getCaseStructure(), this.getValues());
        aCase.setInputDialogInstance(this.inputDialogInstance);
        aCase.setSystemResponse(this.aSystemResponse);
        return aCase;
    }
    
    /**
     * IDialogInstance set
     * @param inputDialogInstance 
     */
    public void setInputDialogInstance(IDialogInstance inputDialogInstance){
        this.inputDialogInstance = inputDialogInstance;
    }
    
    /**
     * IDialogInstance return
     * @return 
     */
    public IDialogInstance getInputDialogInstance(){
        return this.inputDialogInstance;
    }
    
    /**
     * SystemResponse set
     * @param aSystemResponse 
     */
    public void setSystemResponse(SystemResponse aSystemResponse){
        this.aSystemResponse = aSystemResponse;
    }
    
    /**
     * SystemResponse return
     * @return 
     */
    public SystemResponse getSystemResponse(){
        return this.aSystemResponse;
    }
    
    
    @Override
    public String toString(){
        String str = new String();        // Get a set of the entries
        Set set = this.getValues().entrySet();
        // Get an iterator
        Iterator i = set.iterator();
        str = "Case (" + this.getCaseId() + "): [";
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();            
            String attributeName = (String) me.getKey();
            Value attributeValue = (Value) me.getValue();
            if(!attributeName.equals("History")){
                str += attributeName +":"+ attributeValue.toString()+";";
            }
        }        
        str +="]";
        return str;
    }

}

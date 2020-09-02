/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.external;

import cmcrdr.command.ICommandInstance;
import cmcrdr.event.EventInstance;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class ExternalExecutor {
    
    /**
     * ExternalExecutor가 동작 하는 모드 (Java GUI) ID.
     */
    public static final int JAVA_OUTPUT = 1001;
    
    /**
     * ExternalExecutor가 동작 하는 모드 (Web GUI) ID.
     */
    public static final int WEB_OUTPUT = 1002;
    
    private int outputMode = 1001;
    
    private ICommandInstance commandInstance;
    
    /**
     *
     */
    public ExternalExecutor() {
        
    }
    
    /**
     *
     * @param outputMode
     */
    public void setOutputMode(int outputMode){
        this.outputMode = outputMode;
    }
    
    /**
     *
     * @return
     */
    public int getOutputMode(){
        return this.outputMode;
    }
    
    /**
     *
     * @param commandInstance
     */
    public void setCommand(ICommandInstance commandInstance){
        this.commandInstance = commandInstance;
    }
    
    /**
     *
     * @return
     */
    public ICommandInstance getCommand(){
        return this.commandInstance;
    }
    
    /**
     *
     * @return
     */
    public EventInstance execute(){
        EventInstance result = null;
        
        result = this.commandInstance.executeDeviceAction(this.outputMode);
        
        if(result!=null) {
            result.setIsGeneratedByRule(true);
        }
        
        return result;
        
    } 
    
}

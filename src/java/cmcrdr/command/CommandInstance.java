/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.command;

import cmcrdr.event.EventInstance;
import cmcrdr.external.ExternalExecutor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandInstance implements ICommandInstance{
    
    /**
     * ExternalExecutor Device (Knowledge Acquisition) ID .
     */
    public static final int KA_ID = 1;
    public static final int DATABASE_ITEM_ID = 2;
    public static final int CONTEXT_VARIABLE_ID = 3;
    //public static final int FLIGHT_QUERY_ID = 4;

    

    /**
     * ExternalExecutor Device List - refer eventInstance.eventList. 
     */
    public static final String[] devicelist = new String[]{
        "",
        //"KA", future work
        "DatabaseItem",
        "ContextVariable"
        //"FlightQuery"
    };
    
    /**
     * CommandInstance Command Device Identifier.
     */
    private int targetDeviceId;
    
    /**
     * CommandInstance Command Device Name.
     */
    private String targetDeviceName;
    
    /**
     * CommandInstance Command Device Command.
     */
    private String deviceAction;
    
    /**
     * CommandInstance Command Device possible Command List.
     */
    private String[] deviceActionList;
    
    // DPH
    private String[] deviceActionListParameterData;
    private String deviceActionResponse;
    
    /**
     * CommandInstance Command Device Command result type is JAVA or WEB.
     */
    private int actionOutputMode;
    
    /**
     * CommandInstance Command Device Command Variable List - verify value is set.
     */
    private boolean isDeviceActionVariableListSet=false;
    
    /**
     * CommandInstance Command Device Command Variable List.
     */
    private String[] deviceActionVariableList;
    
    /**
     * CommandInstance Command .
     */
    private boolean deviceSet=false;
    
    
    /**
     * Constructor.
     */
    public CommandInstance (){
        
    }
    
    /**
     * Constructor.
     * @param targetDeviceId
     */
    public CommandInstance (int targetDeviceId){
        this.targetDeviceId = targetDeviceId;
    }
    
    
    /**
     * CommandInstance Command Device Name set
     * @return 
     */
    @Override
    public boolean isSet(){
        return this.deviceSet;
    }
    
    
    /**
     * CommandInstance Command Device Name set
     * @return 
     */
    @Override
    public boolean isEtcCommandInstance(){
        return this.targetDeviceId > devicelist.length;
    }
    
    
    /**
     * CommandInstance Command set
     * @param targetDevice 
     * @param deviceAction 
     */
    @Override
    public void setCommand(String targetDevice, String deviceAction){
        this.deviceAction = deviceAction;
        this.targetDeviceName = targetDevice;
        if(!targetDevice.equals("")){
            this.deviceSet = true;
        }
    }
    
    /**
     * CommandInstance Identifier set
     * @param targetDeviceId 
     */
    @Override
    public void setTargetDeviceId(int targetDeviceId){
        this.targetDeviceId = targetDeviceId;
    }
    
    /**
     * CommandInstance Identifier return
     * @return 
     */
    @Override
    public int getTargetDeviceId(){
        return this.targetDeviceId;
    }    
    
    /**
     * CommandInstance Command Device Name set
     * @param targetDeviceName 
     */
    @Override
    public void setTargetDeviceName(String targetDeviceName){
        this.targetDeviceName = targetDeviceName;
    }
    
    /**
     * CommandInstance Command Device Name return
     * @return 
     */
    @Override
    public String getTargetDeviceName(){
        return this.targetDeviceName;
    }    
    
    /**
     * CommandInstance Command Device Command set
     * @param deviceAction 
     */
    @Override
    public void setDeviceAction(String deviceAction){
        this.deviceAction = deviceAction;
    }
    
    /**
     * CommandInstance Command Device Command return
     * @return 
     */
    @Override
    public String getDeviceAction(){
        return this.deviceAction;
    }
    
    /**
     * CommandInstance Command 결과 모드 set
     * @param actionOutputMode 
     */
    @Override
    public void setActionOutputMode(int actionOutputMode){
        this.actionOutputMode = actionOutputMode;
    }
    
    /**
     * CommandInstance Command 결과 모드  return
     * @return 
     */
    @Override
    public int getActionOutputMode(){
        return this.actionOutputMode;
    }
    
    /**
     * CommandInstance Command Device Command set
     * @param deviceActionVariableList 
     */
    @Override
    public void setDeviceActionVariableList(String[] deviceActionVariableList){
        this.deviceActionVariableList = deviceActionVariableList;
    }
    
    /**
     * CommandInstance Command Device Command return
     * @return 
     */
    @Override
    public String[] getDeviceActionVariableList(){
        return this.deviceActionVariableList;
    }
    
    /**
     * CommandInstance Command Device Command Variable값 List가 지정되었는지 set
     * @param isDeviceActionVariableListSet 
     */
    @Override
    public void setIsDeviceActionVariableListSet(boolean isDeviceActionVariableListSet){
        this.isDeviceActionVariableListSet = isDeviceActionVariableListSet;
    }
    
    /**
     * CommandInstance Command Device Command Variable값 List가 지정되었는지 return
     * @return 
     */
    @Override
    public boolean getIsDeviceActionVariableListSet(){
        return this.isDeviceActionVariableListSet;
    }
    
    /**
     * CommandInstance Command Device 실행 메소드 리턴.
     * @param outputMode
     * @return 
     */
    @Override
    public EventInstance executeDeviceAction(int outputMode){

        if(outputMode == ExternalExecutor.JAVA_OUTPUT){
           this.actionOutputMode = ExternalExecutor.JAVA_OUTPUT;
        } else if(outputMode == ExternalExecutor.WEB_OUTPUT){
           this.actionOutputMode = ExternalExecutor.WEB_OUTPUT;
        }
        
        EventInstance result = null;
        String[] methodArray = this.getDeviceAction().split("_");
        String methodName = methodArray[0];
        
        if(methodArray.length>1){
            // return array?
            int newLength = methodArray.length-1;
            this.deviceActionVariableList = new String[newLength];

            for(int i=1; i<methodArray.length; i++){
                this.deviceActionVariableList[i-1] = methodArray[i];
            }
            this.isDeviceActionVariableListSet = true;
        }
        if(this.isEtcCommandInstance()){
            result = (EventInstance) this.etcAction();
        } else {

            try {
                Method method = getClass().getDeclaredMethod(methodName);
                result = (EventInstance) method.invoke(this);

            } catch (SecurityException | NoSuchMethodException e) {
              // ...
            } catch (IllegalAccessException ex) {
            Logger.getLogger(CommandInstance.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(CommandInstance.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(CommandInstance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    /**
     *
     * @param eventType
     */
    @Override
    public void setEventType(String eventType) {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public String getEventType() {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @param eventValue
     */
    @Override
    public void setEventValue(String eventValue) {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public String getEventValue() {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @param eventTypeForWeb
     */
    @Override
    public void setEventTypeForWeb(String eventTypeForWeb) {
       throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public String getEventTypeForWeb() {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @param eventValueForWeb
     */
    @Override
    public void setEventValueForWeb(String eventValueForWeb) {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public String getEventValueForWeb() {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @return
     */
    @Override
    public EventInstance etcAction() {
        throw new UnsupportedOperationException("Only supported in EtcCommandInstance."); 
    }

    /**
     *
     * @param deviceActionList
     */
    @Override
    public void setDeviceActionList(String[] deviceActionList) {
        this.deviceActionList = deviceActionList;
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getDeviceActionList() {
        return this.deviceActionList;
    }
    




    @Override
    public String toString() {
        if(this.isSet()){
            String result = "";
            result = this.targetDeviceName + " : " + this.deviceAction;

            return result;
        } else {
            return "";
        }
    }
}

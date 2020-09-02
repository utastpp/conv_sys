/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.command;

import cmcrdr.event.EventInstance;


/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public interface ICommandInstance {

    /**
     *
     * @return
     */
    public boolean isSet();

    /**
     *
     * @return
     */
    public boolean isEtcCommandInstance();

    /**
     *
     * @param deviceActionList
     */
    public void setDeviceActionList(String[] deviceActionList);

    /**
     *
     * @return
     */
    public String[] getDeviceActionList();

    /**
     *
     * @param targetDevice
     * @param deviceAction
     */
    public void setCommand(String targetDevice, String deviceAction);

    /**
     *
     * @param targetDeviceId
     */
    public void setTargetDeviceId(int targetDeviceId);

    /**
     *
     * @return
     */
    public int getTargetDeviceId();

    /**
     *
     * @param targetDeviceName
     */
    public void setTargetDeviceName(String targetDeviceName);

    /**
     *
     * @return
     */
    public String getTargetDeviceName();

    /**
     *
     * @param deviceAction
     */
    public void setDeviceAction(String deviceAction);

    /**
     *
     * @return
     */
    public String getDeviceAction();

    /**
     *
     * @param actionOutputMode
     */
    public void setActionOutputMode(int actionOutputMode);

    /**
     *
     * @return
     */
    public int getActionOutputMode();

    /**
     *
     * @param outputMode
     * @return
     */
    public EventInstance executeDeviceAction(int outputMode);

    /**
     *
     * @param deviceActionVariableList
     */
    public void setDeviceActionVariableList(String[] deviceActionVariableList);

    /**
     *
     * @return
     */
    public String[] getDeviceActionVariableList();

    /**
     *
     * @return
     */
    public boolean getIsDeviceActionVariableListSet();

    /**
     *
     * @param isDeviceActionVariableListSet
     */
    public void setIsDeviceActionVariableListSet(boolean isDeviceActionVariableListSet);

    /**
     *
     * @param eventType
     */
    public void setEventType(String eventType);

    /**
     *
     * @return
     */
    public String getEventType();

    /**
     *
     * @param eventValue
     */
    public void setEventValue(String eventValue);

    /**
     *
     * @return
     */
    public String getEventValue();

    /**
     *
     * @param eventTypeForWeb
     */
    public void setEventTypeForWeb(String eventTypeForWeb);

    /**
     *
     * @return
     */
    public String getEventTypeForWeb();

    /**
     *
     * @param eventValueForWeb
     */
    public void setEventValueForWeb(String eventValueForWeb);

    /**
     *
     * @return
     */
    public String getEventValueForWeb();

    /**
     *
     * @return
     */
    public EventInstance etcAction();

    
}

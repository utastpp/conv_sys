/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.event;


public class EventInstance {
    

    /*public static final String[] eventList = new String[]{
        "",
        "IDS",
        "Car",
        "GPS",
        "Scheduler", 
        "Navigation",
        "SMS", 
        "Radio", 
        "Windows",
        "Time",
        "Temperature",
        "Smartphone",
        "Highway",
        "Music",
        "A/C",
        "Heater"};
    */
    
    public static final String[] eventList = new String[]{
        "",
        "FlightQueryResponse"
    };
    
    public static final int FlightQuery_ID = 1;

    
    /*
    public static final int Scheduler_ID = 1;

    public static final int Navigation_ID = 2;
    
    public static final int SMS_ID = 3;

    public static final int Radio_ID = 4;

    public static final int Window_ID = 5;

    public static final int Time_ID = 6;

    public static final int Temperature_ID = 7;

    public static final int GPS_ID = 8;
    
    public static final int Car_ID = 9;

    public static final int IDS_ID = 10;

    public static final int Traffic_ID = 11;

    public static final int Smartphone_ID = 12;

    public static final int Highway_ID = 13;

    public static final int Music_ID = 14;

    public static final int AC_ID = 15;
    */
    

    private String eventType;
    
    private String eventValue;
    
    private boolean isGeneratedByRule = false;
    
    public EventInstance (){
        this.eventType="";
        this.eventValue="";        
    }
    
    public void setEventType(String eventType){
        this.eventType = eventType;
    }

    public String getEventType(){
        return this.eventType;
    }    
    
    public void setEventValue(String eventValue){
        this.eventValue = eventValue;
    }
    

    public String getEventValue(){
        return this.eventValue;
    }

    public void setIsGeneratedByRule(boolean isGeneratedByRule){
        this.isGeneratedByRule = isGeneratedByRule;
    }

    public boolean getIsGeneratedByRule(){
        return this.isGeneratedByRule;
    }
    

    @Override
    public String toString(){
        return "[" + this.eventType + " " + this.eventValue + "]";
    }
    
}

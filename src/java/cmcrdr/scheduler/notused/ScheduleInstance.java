/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.scheduler.notused;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;



/**
 * ScheduleInstance 객체를 정의하기 위한 클래스
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class ScheduleInstance {
    
    /**
     * ScheduleInstance의 Identifier.
     */
    private int scheduleId;
    
    /**
     * ScheduleInstance의 날짜.
     */
    private ScheduleDateTimeInstant scheduleCalendar;
    
    /**
     * ScheduleInstance의 장소.
     */
    private String schedulePlace;   
    
    /**
     * ScheduleInstance의 출발장소.
     */
    private String scheduleDepartPlace;
    
    /**
     * ScheduleInstance의 제목. 
     */
    private String scheduleTitle;
    
    /**
     * ScheduleInstance의 설명. 
     */
    private String scheduleDescription;
    
    /**
     *
     */
    public ScheduleInstance(){
    }
    
    
    /**
     * ScheduleInstance의 Identifier 값 set
     * @param scheduleId 
     */
    public void setScheduleId(int scheduleId){
        this.scheduleId = scheduleId;
    }
    
    /**
     * ScheduleInstance의 Identifier 값 return
     * @return 
     */
    public int getScheduleId(){
        return this.scheduleId;
    }
    
    /**
     * ScheduleInstance의 Calendar 값 set
     * @param scheduleCalendar 
     */
    public void setScheduleCalendar(ScheduleDateTimeInstant scheduleCalendar){
        this.scheduleCalendar = scheduleCalendar;
    }
    
    /**
     * ScheduleInstance의 Calendar 값 return
     * @return 
     */
    public ScheduleDateTimeInstant getScheduleCalendar(){
        return this.scheduleCalendar;
    }
    
    
    /**
     * ScheduleInstance의 제목 set
     * @param scheduleTitle 
     */
    public void setScheduleTitle(String scheduleTitle){
        this.scheduleTitle = scheduleTitle;
    }
    
    /**
     * ScheduleInstance의 제목 return
     * @return 
     */
    public String getScheduleTitle(){
        return this.scheduleTitle;
    }
    
    
    /**
     * ScheduleInstance의 설명 set
     * @param scheduleDescription 
     */
    public void setScheduleDescription(String scheduleDescription){
        this.scheduleDescription = scheduleDescription;
    }
    
    /**
     * ScheduleInstance의 설명 return
     * @return 
     */
    public String getScheduleDescription(){
        return this.scheduleDescription;
    }
    
    
    /**
     * ScheduleInstance의 출발 장소 set
     * @param scheduleDepartPlace 
     */
    public void setScheduleDepartPlace(String scheduleDepartPlace){
        this.scheduleDepartPlace = scheduleDepartPlace;
    }
    
    /**
     * ScheduleInstance의 출발 장소 return
     * @return 
     */
    public String getScheduleDepartPlace(){
        return this.scheduleDepartPlace;
    }
    
    
    /**
     * ScheduleInstance의 약속 장소 set
     * @param schedulePlace 
     */
    public void setSchedulePlace(String schedulePlace){
        this.schedulePlace = schedulePlace;
    }
    
    /**
     * ScheduleInstance의 약속 장소 return
     * @return 
     */
    public String getSchedulePlace(){
        return this.schedulePlace;
    }
    
    
    /**
     * ScheduleSet 내에 있는 각각의 ScheduleInstance를 toString
     * @return
     */
    @Override
    public String toString() {
        String result = "["+this.scheduleId+"] "+ this.scheduleTitle + "\n ";
        if(!this.scheduleDescription.equals(""))
        {
            result += "description: " + this.scheduleDescription + "\n ";
        }
        if(this.scheduleCalendar!=null)
        {
             result += "datetime: " + this.scheduleCalendar.toString() + "\n ";
        }
        if(!this.scheduleDepartPlace.equals(""))
        {
            result += "depart from: " + this.scheduleDepartPlace + "\n ";
        }
        if(!this.schedulePlace.equals(""))
        {
            result += "scheduled at: " + this.schedulePlace;
        }
        return result;
    }
}

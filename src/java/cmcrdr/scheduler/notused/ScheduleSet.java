/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.scheduler.notused;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * ScheduleInstance 객체를 저장하는 클래스
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class ScheduleSet {
    
    /**
     * ScheduleSet의 ScheduleInstance 저장 HashMap.
     */
    private LinkedHashMap<Integer, ScheduleInstance> scheduleSet = new LinkedHashMap<>();
        
    /**
     * ScheduleSet 내에 존재하는 ScheduleInstance의 갯수 return
     * @return 
     */
    public int getScheduleInstanceAmount(){
        return this.scheduleSet.size();
    }
    
    /**
     * ScheduleSet에 새롭게 생성될 ScheduleInstance id 값 return
     * @return 
     */
    public int getNewScheduleId(){
        if(this.scheduleSet.size()>0){
            Set rules = this.scheduleSet.entrySet();
            Iterator iterator = rules.iterator();
            int maxId = 0;
            
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                ScheduleInstance aScheduleInstance = (ScheduleInstance)me.getValue();
                maxId = Math.max(maxId, aScheduleInstance.getScheduleId());
            }
            return maxId+1;
        } else {
            return 1;
        }
    }
    
    
    /**
     * ScheduleSet의 ScheduleInstance 저장 HashMap return
     * @return
     */
    public LinkedHashMap<Integer, ScheduleInstance> getScheduleSetBase(){
        return this.scheduleSet;
    }
    
    /**
     * ScheduleSet 내에 새로운 ScheduleInstance을 추가
     * @param scheduleInstance
     * @return 
     */
    public boolean addScheduleInstance(ScheduleInstance scheduleInstance) {
        if(this.scheduleSet.containsKey(scheduleInstance.getScheduleId())){
            return false;
        } else {
            this.scheduleSet.put(scheduleInstance.getScheduleId(), scheduleInstance);
            return true;
        }
    }
    
    /**
     * ScheduleSet 내에 있는 ScheduleInstance을 삭제
     * @param scheduleInstance
     * @return 
     */
    public boolean deleteScheduleInstance(ScheduleInstance scheduleInstance) {
        if(!this.scheduleSet.containsKey(scheduleInstance.getScheduleId())){
            return false;
        } else {
            this.scheduleSet.remove(scheduleInstance.getScheduleId());
            return true;
        }
    }
    
    /**
     * ScheduleSet 내에 주어진 ScheduleInstance의 ID로 등록된 ScheduleInstance가 존재하는지 return
     * @param scheduleInstance
     * @return
     */
    public boolean isScheduleInstanceExist(ScheduleInstance scheduleInstance){
        return this.scheduleSet.containsKey(scheduleInstance.getScheduleId());
    }
    
    
    
    /**
     * ScheduleSet 내에 오늘의 날짜에 등록된 ScheduleInstance를 ScheduleSet 형태로 return
     * @return
     */
    public ScheduleSet getTodaySchedule(){
        ScheduleSet todayScheduleList = new ScheduleSet();
        
        Set schedules = this.scheduleSet.entrySet();
        // Get an iterator
        Iterator termIterator = schedules.iterator();
        
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            ScheduleInstance aScheduleInstance = (ScheduleInstance) me.getValue();            
            
            GregorianCalendar today = new GregorianCalendar();
            
            Date todayDate = new Date();
            todayDate.setTime(System.currentTimeMillis());
            today.setTime(todayDate);
            
            if(aScheduleInstance.getScheduleCalendar().get(Calendar.DATE) == today.get(Calendar.DATE) 
                    && aScheduleInstance.getScheduleCalendar().get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && aScheduleInstance.getScheduleCalendar().get(Calendar.YEAR) == today.get(Calendar.YEAR)){
                //store calendar instance of today
                todayScheduleList.addScheduleInstance(aScheduleInstance);
            }
        }       
        return todayScheduleList;
    }
    
    
    /**
     * ScheduleSet 내에 내일 날짜에 등록된 ScheduleInstance를 ScheduleSet 형태로 return
     * @return
     */
    public ScheduleSet getTomorrowSchedule(){
        ScheduleSet todayScheduleList = new ScheduleSet();
        
        Set schedules = this.scheduleSet.entrySet();
        // Get an iterator
        Iterator termIterator = schedules.iterator();
        
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            ScheduleInstance aScheduleInstance = (ScheduleInstance) me.getValue();            
            
            GregorianCalendar today = new GregorianCalendar();
            
            Date todayDate = new Date();
            todayDate.setTime(System.currentTimeMillis());
            today.setTime(todayDate);
            
            if(aScheduleInstance.getScheduleCalendar().get(Calendar.DATE) == today.get(Calendar.DATE)+1 
                    && aScheduleInstance.getScheduleCalendar().get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && aScheduleInstance.getScheduleCalendar().get(Calendar.YEAR) == today.get(Calendar.YEAR)){
                //store calendar instance of today
                todayScheduleList.addScheduleInstance(aScheduleInstance);
            }
        }       
        return todayScheduleList;
    }
    
    /**
     * ScheduleSet 내에 있는 ScheduleInstance를 GUI표현을 위해 Object[][] Array 형태로 return
     * @return
     */
    public Object[][] toObjectArrayForGUI() {
        
        int scheduleCount = this.getScheduleInstanceAmount();
        // new object for gui
        Object[][] newObject = new Object[scheduleCount][6];

        Set schedules = this.scheduleSet.entrySet();
        
        // Get an iterator
        Iterator termIterator = schedules.iterator();
        
        int cnt = 0;
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            ScheduleInstance aScheduleInstance = (ScheduleInstance) me.getValue();            
            
            //store case id in first column
            newObject[cnt][0] = aScheduleInstance.getScheduleId();
            newObject[cnt][1] = aScheduleInstance.getScheduleTitle();
            newObject[cnt][2] = aScheduleInstance.getScheduleCalendar().toString();
            newObject[cnt][3] = aScheduleInstance.getSchedulePlace();
            newObject[cnt][4] = aScheduleInstance.getScheduleDepartPlace();
            newObject[cnt][5] = aScheduleInstance.getScheduleDescription();

            cnt++;
        }       
        
        return newObject;
    }
    
    
    /**
     * ScheduleSet 내에 있는 각각의 ScheduleInstance를 toString
     * @return
     */
    @Override
    public String toString() {
        String result = "";

        Set schedules = this.scheduleSet.entrySet();
        
        // Get an iterator
        Iterator termIterator = schedules.iterator();
        
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            ScheduleInstance aScheduleInstance = (ScheduleInstance) me.getValue();            
            result += aScheduleInstance.toString()+"\n";
        }       
        
        return result;
    }
}

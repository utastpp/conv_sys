/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.scheduler.notused;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * ScheduleDateTimeInstant 객체를 정의하는 클래스 (GregorianCalendar 상속)
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class ScheduleDateTimeInstant extends GregorianCalendar {
    
    /**
     *
     */
    public ScheduleDateTimeInstant(){
        super();
    }
    
    @Override
    public String toString(){
        int year = this.get(Calendar.YEAR);
        String yearStr = Integer.toString(year);
        
        int month = this.get(Calendar.MONTH);
        String monthStr = Integer.toString(month);
        if(month<10) {
            monthStr = "0" + monthStr;
        }
        
        int date = this.get(Calendar.DATE);
        String dateStr = Integer.toString(date);
        if(date<10) {
            dateStr = "0" + dateStr;
        }
        
        String am_pm = "";
        int amPm = this.get(Calendar.AM_PM);
        if(amPm==1){
            am_pm = "pm";
        } else if(amPm==0){
            am_pm = "am";
        }
        
        
        int hour = this.get(Calendar.HOUR);
        String hourStr = Integer.toString(hour);
        if(hour<10) {
            hourStr = "0" + hourStr;
        }
        
        int minute = this.get(Calendar.MINUTE);
        String minuteStr = Integer.toString(minute);
        if(minute<10) {
            minuteStr = "0" + minuteStr;
        }
        
        return yearStr + "year " + monthStr + "month " + dateStr + "day " + am_pm + " " + hourStr + "hour " + minuteStr + "hour";
    }
    
}

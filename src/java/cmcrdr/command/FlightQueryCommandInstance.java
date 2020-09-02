/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.command;

import java.util.Arrays;
import java.util.List;
import cmcrdr.contextvariable.ContextVariableUser;
import cmcrdr.event.EventInstance;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;

/**
 *
 * @author dherbert
 */
/*public class FlightQueryCommandInstance  extends CommandInstance {
    
    // This class should send a JSON query to an external flight schedule system using a REST interface
    // It should format the response as an appropriate dialog response
    
    public static final String SendFlightQuery = "SendFlightQuery";
    public static final String eventType = "FlightQueryResponse";

    private final String[] deviceActionList = new String[]{SendFlightQuery};
    
    public FlightQueryCommandInstance(){
        super(FLIGHT_QUERY_ID);
    }
    
   */ 
    
    /********************************************************************************************/
    /********************************************************************************************/
    /********************** KNU TO DO                                    ************************/
    /********************************************************************************************/
    /********************************************************************************************/
    
   /* public EventInstance SendFlightQuery() {
        Logger.info("I have been called...");
        
        // determine list of context variables currently set that are schema-backed      
        String [] schemaVariables = DialogMain.userInterfaceController.getConvertedSchemaColumnNamesAsStringArray();
        String RESTResponse = sendRESTquery(schemaVariables);
        
        
        EventInstance theReplyEvent = new EventInstance();
        theReplyEvent.setEventType(this.eventType);
        theReplyEvent.setEventValue(RESTResponse);
        theReplyEvent.setIsGeneratedByRule(true);
        
        return theReplyEvent;
    }
    */
    /********************************************************************************************/
    /********************************************************************************************/
    /********************** KNU TO DO                                    ************************/
    /********************************************************************************************/
    /********************************************************************************************/
    
    // This method needs to be written (the method interface can be modifed as needed)
    // (and actually send JSON data to an external REST interface..)
    /*private String sendRESTquery(String [] schemaVariables) {
        String myReply = "";
        
        for (String aSchemaVariable: schemaVariables) {
            if (DialogMain.globalContextVariableManager.isUserVariableSet(aSchemaVariable)) {
                ContextVariableUser cv = DialogMain.getDialogUserList().getCurrentContextVariables().get(aSchemaVariable);
                Logger.info("The flight context variable " + aSchemaVariable + " is set and has value:" + cv.getValue());
            
                if (myReply.isEmpty())
                    myReply = "The following variables were sent for REST query: " + aSchemaVariable + " with value:" + cv.getValue();
                else
                    myReply += ", and " + aSchemaVariable + " with value:" + cv.getValue();
                // KNU:
                // collate all the set variables then send via JSON....
                // ..... to do :-)
                

            }
        }
        return myReply;
    }
    */
    
    /*@Override
    public String[] getDeviceActionList(){
        return this.deviceActionList;
    }
    */
//}

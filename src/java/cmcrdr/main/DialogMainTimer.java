/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.main;

import cmcrdr.gui.AdminJavaGUI;
import cmcrdr.user.DialogUser;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import rdr.logger.Logger;
import cmcrdr.webinterface.UserInterfaceController;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class DialogMainTimer extends TimerTask {

    private int TIMEOUT_VALUE = 120 * 60 * 1000;
    private Object parentCaller;

    /**
     *
     * @param parent
     */
    public DialogMainTimer(Object parent) {
        super();
        parentCaller = parent;
    }
    
    @Override
    public void run() {
        Date currentTime = Calendar.getInstance().getTime();
        
        int currentIndex = 0;
        ArrayList<String> usersToRemove = new ArrayList<>();
              
        synchronized(((UserInterfaceController)parentCaller).syncLock) {

            for (Map.Entry me : DialogMain.getDialogUserList().getBaseSet().entrySet()) {
                DialogUser aDialogUser = (DialogUser) me.getValue();
                Date userTimeoutValue = aDialogUser.getUserTimeout();
                Date isTimeout = new Date(userTimeoutValue.getTime() + TIMEOUT_VALUE);
                if (isTimeout.getTime() < currentTime.getTime()) {
                    //String username = DialogMain.getDialogUserList().getDialogUser(currentIndex).getUsername();
                    String username = aDialogUser.getUsername();
                    Logger.info("User: " + username + " timeout expired..");
                    // we can't modify the source list while we're iterating over it, so we construct a new list..
                    usersToRemove.add(username);                   
                }
                currentIndex ++;
            }

            for (String user: usersToRemove) {
                ((UserInterfaceController)parentCaller).removeUserSessionHasExpired(user);
            }
        }
    }   
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.handler;

import cmcrdr.logger.Logger;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
//import weka.core.logging.Logger;
/**
 *
 * @author dherbert
 */
public class AutonomousSystemHandler {
    private static BufferedReader in = null;
    private static PrintWriter out = null;
    private static Socket s = null;

    public static Socket getSocket(boolean reset) throws IOException  {
        //String serverAddress = "rostest.duckdns.org";
        String serverAddress = "rosmaster.duckdns.org";
        int PORT = 8090;
        
        if (s == null ) {
            Logger.info("Attempting to connect to " + serverAddress + " on port " + PORT);
            s = new Socket(serverAddress, PORT);
            Logger.info("Connection complete..");
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));        
            out = new PrintWriter(s.getOutputStream(), true);
            if (in.readLine() == null) {
                Logger.info("Status OK from server.");
            }
        }
        else if (reset) {
            s.shutdownInput();
            s.shutdownOutput();
            s.close();
            Logger.info("Attempting to reconnect to " + serverAddress + " on port " + PORT);

            s = new Socket(serverAddress, PORT);
            Logger.info("Connection complete..");
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));        
            out = new PrintWriter(s.getOutputStream(), true);
            if (in.readLine() == null) {
                Logger.info("Status OK from server.");
            }
        }
        
        return s;
    }

    public static void closeSocket() throws IOException {
        s.close();
    }
    
    public static String sendAndReceive(String message) throws IOException {
        String response = "";
        Socket theSocket;
        
        Logger.info("In send and receive, getting socket..");

        theSocket = getSocket(false);
        
        Logger.info("In send and receive, about to send:'" + message +"'");

        out.print(message);
        out.flush();

        Logger.info("In send and receive, waiting to read..");
        try {
            response = in.readLine();
            if (response == null || response.equals("")) {
              throw new SocketException();
            }      
        }
        catch (SocketException e) {
            Logger.info("No response read from server, trying reconnect");
              theSocket = getSocket(true);
              out.print(message);
              out.flush();
              response = in.readLine();        
        }
        Logger.info("In send and receive, read:" + response);

         
        return response;
    }
    
}

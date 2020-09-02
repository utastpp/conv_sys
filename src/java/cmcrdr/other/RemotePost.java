/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.other;

import cmcrdr.main.DialogMain;
import cmcrdr.processor.PreAndPostProcessorAction;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cmcrdr.user.DialogUser;

/**
 *
 * @author dherbert
 */
public class RemotePost extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RemotePost</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RemotePost at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
               

        String sendData;
        //String sessionId = request.getSession().getId();
        String sessionId = request.getParameter("client").trim();
        cmcrdr.logger.Logger.info("My received session ID is " + sessionId);

        
        String recentUserInputString = request.getParameter("command").trim();
        cmcrdr.logger.Logger.info("INPUT COMMAND: " + recentUserInputString);
        
        // determine if we have a remote client connecting for the first time - send them the short greeting
        if (recentUserInputString.equals(DialogMain.STARTSESSION)) {
           sendData = DialogMain.getInitialShortGreeting();
           response.setContentType("text/plain; charset=UTF-8");
           response.getWriter().write(sendData);
        }
        else {
            // Fix ASR errors..
            for (PreAndPostProcessorAction aPreprocessorAction: DialogMain.processorList) {
                recentUserInputString = DialogMain.userInterfaceController.preProcessInputAction(recentUserInputString, aPreprocessorAction);
            }

            sendData = DialogMain.userInterfaceController.setIncomingMessage(sessionId, recentUserInputString,DialogUser.UserSourceType.REMOTEPOST) ;

            // remove non-speakable elements
            String NO_SPEAK = "#NOSPEAK";
            String END_NO_SPEAK = "#ENDNOSPEAK";

            int nospeakStartPosition = sendData.indexOf(NO_SPEAK);
            int nospeakEndPosition;

            while (nospeakStartPosition != -1) {
                nospeakEndPosition = sendData.indexOf(END_NO_SPEAK);
                sendData = sendData.substring(0,nospeakStartPosition) + sendData.substring(nospeakEndPosition + END_NO_SPEAK.length());
                sendData = sendData.replace(NO_SPEAK,"");
                sendData = sendData.replace(END_NO_SPEAK,"");
                nospeakStartPosition = sendData.indexOf(NO_SPEAK);
            }

            cmcrdr.logger.Logger.info("send data after nospeak: " + sendData);
            // fix mispronunciation errors
            for (PreAndPostProcessorAction aPreprocessorAction: DialogMain.processorList) {
                sendData = DialogMain.userInterfaceController.postProcessInputAction(sendData, aPreprocessorAction);
            }

            cmcrdr.logger.Logger.info("send data after post process: " + sendData);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write(sendData);
        }
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

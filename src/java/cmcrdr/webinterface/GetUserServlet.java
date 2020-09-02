/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cmcrdr.webinterface;

import cmcrdr.main.DialogMain;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cmcrdr.processor.PreAndPostProcessorAction;
import cmcrdr.user.DialogUser;

@WebServlet("/GetUserServlet")
public class GetUserServlet extends HttpServlet {
    //private static final long serialVersionUID = 1L;
    //public static String deviceActionName;
    //public static String[] variableList;
    private static DialogMain dialogMain = null;
    private static String status = null;
    private static boolean domainInitialised = false;
    
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        switch (request.getParameter("mode").trim()) {
            
            case "getDomainDescription":
            {
                String sendData;
                if (!DialogMain.getIsDomainInitialised()) {
                    sendData = "The knowledgebase has not been initialised yet..";
                }
                else
                    sendData = DialogMain.getDomainDescription();  
                
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }

            case "greeting":
            {
                String sessionId = request.getSession().getId();
                String sendData;
                
                if (!DialogMain.getIsDomainInitialised()) {
                    sendData = "The knowledgebase has not been initialised yet..";
                }
                else
                    sendData = DialogMain.getInitialGreeting();   
                
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
            case "shortgreeting":
            {
                String sessionId = request.getSession().getId();
                String sendData;
                
                if (!DialogMain.getIsDomainInitialised()) {
                    sendData = "The knowledgebase has not been initialised yet..";
                }
                else
                    sendData = DialogMain.getInitialShortGreeting();    
                
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
            
 
            
            case "preprocess":
            {
                String recentUserInputString = request.getParameter("stringInput").trim();

                for (PreAndPostProcessorAction aPreprocessorAction: DialogMain.processorList) {
                    recentUserInputString = DialogMain.userInterfaceController.preProcessInputAction(recentUserInputString, aPreprocessorAction);
                }

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(recentUserInputString);
                break;
            }
            
            case "postprocess":
            {
                String recentUserInputString = request.getParameter("stringInput").trim();

                for (PreAndPostProcessorAction aPreprocessorAction: DialogMain.processorList) {
                    recentUserInputString = DialogMain.userInterfaceController.postProcessInputAction(recentUserInputString, aPreprocessorAction);
                }

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(recentUserInputString);
                break;
            }
           
            case "dialog":
            {
                String recentUserInputString = request.getParameter("stringInput").trim();
                String sessionId = request.getSession().getId();
                String sendData;

                if (!DialogMain.getIsDomainInitialised()) {
                    sendData = "The knowledgebase has not been initialised yet..";
                }
                else {
                    //Logger.info("Raw input:" + recentUserInputString);
                    for (PreAndPostProcessorAction aPreprocessorAction: DialogMain.processorList) {
                        recentUserInputString = DialogMain.userInterfaceController.preProcessInputAction(recentUserInputString, aPreprocessorAction);
                    }

                    sendData = DialogMain.userInterfaceController.setIncomingMessage(sessionId, recentUserInputString,DialogUser.UserSourceType.WEB) ;
                }

                //sendData = responseStr;

                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(sendData);
                break;
            }
        }           
    }       
}

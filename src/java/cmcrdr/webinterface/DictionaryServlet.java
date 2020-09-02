/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cmcrdr.webinterface;

import com.google.gson.Gson;
import cmcrdr.logger.Logger;
import cmcrdr.main.DialogMain;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import cmcrdr.dic.DicManager;
import cmcrdr.dic.DicTerm;
import cmcrdr.savedquery.SavedQueryTemplate;

@WebServlet("/DictionaryServlet")
public class DictionaryServlet extends HttpServlet {

    private static DialogMain dialogMain = null;
    private static DicTerm tempDicTerm;

    
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        switch (request.getParameter("mode").trim()) {
            case "getRepresentativeTermList":
            {
                try {
                    String theStatus = "";
                    String json = "";
                    
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }
                    
                    //Logger.info("starting getRepresentativeTermList");
                    String[] theList = DialogMain.dicConverter.getDictionary().getRepresentativeTermsForWeb();

                    
                    json = new Gson().toJson(theList);
                    //for (int i=0; i<theList.length; i++) {
                            //theStatus = theStatus + theList[i] + ";";
                    //}
                    
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write(json);
                    
                    //Logger.info("Finished getRepresentativeTermList...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    //response.setContentType("text/plain; charset=UTF-8");
                    //response.getWriter().write(ex.getMessage());
                }
                break;
            }
            case "addRepresentativeTerm":
            {
                try {
                    String theStatus = "true";
                    Logger.info("starting addRepresentativeTerm");
                    
                    String theTerm = request.getParameter("theTerm").trim().toLowerCase();
                    String allowRandomSynonymS = request.getParameter("allowRandomSynonym").trim();
                    boolean allowRandomSynonym;
                    
                    if (allowRandomSynonymS.equals("1"))
                        allowRandomSynonym = true;
                    else
                        allowRandomSynonym = false;
                    

                    setRepresentativeTermButtonWEB(theTerm, allowRandomSynonym);

                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }

                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write(theStatus);

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
                break; 
            }
            case "addNewSynonym":
            {
                try {
                    String theStatus = "true";
                    boolean invalidPattern = false;
                    
                    Logger.info("starting addNewSynonym");
                    
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }
                    
                    String theSynonym = request.getParameter("theSynonym").trim().toLowerCase();
                    String theTerm = request.getParameter("theTerm").trim();
                    Logger.info("Term:" + theTerm);
                    Logger.info("New synonym:" + theSynonym);
                    DicTerm aDicTerm = DialogMain.dictionary.getDicTermByRepresentative(theTerm);
                    
                    if (theSynonym.startsWith("/re:")) {
                        try {
                            Pattern p = Pattern.compile(theSynonym.substring(4));

                        }
                        catch (PatternSyntaxException p) {
                            theStatus = "patternFalse";
                            Logger.info("Invalid regular expression pattern..");
                            invalidPattern = true;
                        }
                    }

                    if (!invalidPattern) {
                        if (!aDicTerm.addMatchingTerm(theSynonym)) {
                            theStatus = "addingFalse";
                        }
                    }

                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    Logger.info(theStatus);

                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break; 
            }
            case "getSynonymList":
            {
                try {
                    String theStatus = "";
                    String json = "";
                    
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }
                    
                    
                    //for (int i=0; i<theList.length; i++) {
                            //theStatus = theStatus + theList[i] + ";";
                    //}
                    
                    
                    
                    
                    String theTerm = request.getParameter("theTerm").trim();
                    Logger.info("starting getSynonymList for term: " + theTerm);

                    
                    DicTerm aDicTerm = DialogMain.dicConverter.getDictionary().getDicTermByRepresentative(theTerm);
                 
                    
                    if (aDicTerm != null) {
                        String[][] synonymArrays = aDicTerm.toMatchingTermStringArrayForGUI();
                        String[] synonymArray = new String[synonymArrays.length];
                        for (int i=0; i < aDicTerm.getMatchingTermSize(); i++) {
                            synonymArray[i] = synonymArrays[i][0];
                        }
                        json = new Gson().toJson(synonymArray);
                        //Logger.info("Synonym JSON is: " + json);
                        
                    }

                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write(json);
                    //response.setContentType("text/plain; charset=UTF-8");

                    //response.getWriter().write(theStatus);
                    Logger.info(theStatus);
                    Logger.info("Finished getSynonymList...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    ex.printStackTrace();

                    //response.setContentType("text/plain; charset=UTF-8");
                    //response.getWriter().write(ex.getMessage());
                }
                break;
            }
            case "saveChanges":
            {
                try {
                    String theStatus = "true";
                    Logger.info("starting saveChanges");
                    
                    String theTerm = request.getParameter("theTerm").trim();
                    String allowRandomSynonymStr = request.getParameter("allowRandomSynonym").trim();
                    boolean allowRandomSynonym;
                    
                    allowRandomSynonym = allowRandomSynonymStr.equals("1");

                    modifyRepresentativeTerm(theTerm, allowRandomSynonym);
                    
                    
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }

                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write(theStatus);

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
                break; 
            }
            
            case "getRandomSynonymStatusList":
            {
                Logger.info("****** getRandomSynonymStatusList");
                try {
                    String theStatus = "";
                    
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }
                   

                    Logger.info("starting getRandomSynonymStatusList");
                    String[] theList = DialogMain.dicConverter.getDictionary().getRepresentativeTermsForWeb();

                    

                    for (String theList1 : theList) {
                        Logger.info("Looking at term: " + theList1);
                        theStatus = theStatus + DialogMain.dicConverter.getDictionary().getDicTermByRepresentative(theList1).getAllowRandomSynonym() + ";";
                        Logger.info("getAllowRandomSynonym status of term is " + DialogMain.dicConverter.getDictionary().getDicTermByRepresentative(theList1).getAllowRandomSynonym());
                    }
                    

                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    Logger.info(theStatus);
                    Logger.info("Finished getRandomSynonymStatusList...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    //response.setContentType("text/plain; charset=UTF-8");
                    //response.getWriter().write(ex.getMessage());
                }
                break;
            }
             
            case "deleteSynonym":
            {
                try {
                    String theStatus = "true";
                    
                    if (!DialogMain.getIsDomainInitialised()) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().write("false");
                        break;
                    }
                    
                    String theTerm = request.getParameter("theTerm").trim();
                    String theSynonym = request.getParameter("theSynonym").trim();

                    //Logger.info("starting deleteSynonym: term:" + theTerm + " and synonym:" + theSynonym);
                    
                    DicTerm aTerm = DialogMain.dictionary.getDicTermByRepresentative(theTerm);
                    //Logger.info(" Fetched term is :" + aTerm.getRepresentativeTerm() + " and synonym:" + theSynonym);

                    if (!aTerm.deleteMatchingTerm(theSynonym)) {
                        theStatus = "deletingFalse";
                    }

                    

                    response.setContentType("text/plain; charset=UTF-8");

                    response.getWriter().write(theStatus);
                    Logger.info(theStatus);
                    Logger.info("Finished deleteSynonym...");

                    break;
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                    //response.setContentType("text/plain; charset=UTF-8");
                    //response.getWriter().write(ex.getMessage());
                }
                break;
            }  
            case "getAllSynonymsList":
            {
                try {

                    String json = "";
                    Gson gson = new Gson();
                    TableResponse theResponse = new TableResponse();
                    String theTerm = request.getParameter("theTerm").trim();
                    
                    theResponse.setStatus("The domain is not initialised");
                    
                    if (DialogMain.getIsDomainInitialised()) {                     
                        theResponse = getAllSynonymsList(theTerm);
                        json = new Gson().toJson(theResponse); 
                    }
                    
                    Logger.info("JSON is: " + json);
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write(json);
                    break; 

                } catch (IOException ex) {
                    Logger.error(ex.getMessage());
                }
                break;
            }
        } 
}  
    
    private TableResponse getAllSynonymsList(String dictionaryTerm) {
        TableResponse theResponse = new TableResponse();
        String[] columns = {dictionaryTerm};
        String[] row;
        theResponse.setHeader(columns);
        theResponse.setStatus("dictionary term not found!");
                    
        DicTerm aDicTerm = DialogMain.dicConverter.getDictionary().getDicTermByRepresentative(dictionaryTerm);
                 
                    
        if (aDicTerm != null) {
            String[][] synonymArrays = aDicTerm.toMatchingTermStringArrayForGUI();
            String[] synonymArray = new String[synonymArrays.length];
            for (int i=0; i < aDicTerm.getMatchingTermSize(); i++) {
                row = new String[] {synonymArrays[i][0]};
                theResponse.addRow(row);
            }
            theResponse.setStatus("OK");
        }
        
        return theResponse;
    }
    
private void setRepresentativeTermButtonWEB(String term, boolean allowRandom) {                                                            
     
        DicManager aDicManager = new DicManager();
        
        if(aDicManager.isValidRepresentative(term)){
            if(DialogMain.dictionary.isDicTermExist(term)){
                DicTerm aDicTerm = DialogMain.dictionary.getDicTermByRepresentative(term);
                tempDicTerm = aDicTerm.cloneDicTerm();

            } 
            else {
                //add new representative
                aDicManager.setCurrentDic(DialogMain.dictionary);

                DicTerm aDicTerm = aDicManager.addRepresentativeTermUsingString(term,allowRandom);

                //tempDicTerm = aDicTerm.cloneDicTerm(); // to allow for synonym rejection etc
            }
        } 
    } 
       


private void modifyRepresentativeTerm(String term, boolean allowRandom) {                                                            
     
        DicManager aDicManager = new DicManager();
        aDicManager.setCurrentDic(DialogMain.dictionary);
        
        if(aDicManager.isValidRepresentative(term)){
            if(DialogMain.dictionary.isDicTermExist(term)){
                DicTerm aDicTerm = DialogMain.dictionary.getDicTermByRepresentative(term);
                DicTerm tempTerm = aDicTerm.cloneDicTerm();
                aDicManager.deleteRepresentativeTerm(term);
                tempTerm.setAllowRandomSynonym(allowRandom);
                aDicManager.addRepresentativeTerm(tempTerm);

                Logger.info("Term " + term + " has had allowRandom set to " + allowRandom);
            } 
        } 
    } 

    public class TableResponse {
            String status;
            ArrayList<String> header = new ArrayList<>();
            ArrayList<ArrayList<String>> rows = new ArrayList<>();

            public void setHeader(String[] theHeader) {
                for (String aHeaderItem: theHeader) {
                    header.add(aHeaderItem);               
                }
            }

            /*public String[] getHeader() {
                return header.toArray(new String[header.size()]);
            }*/

            public void addRow(String[] aRow) {
                ArrayList<String> row = new ArrayList<>();
                if (aRow.length <= header.size()) {
                    for (String aRowItem: aRow) {
                        //Logger.info("TableResponse: Adding row item:" + aRowItem);
                        row.add(aRowItem);
                    }
                    rows.add(row);
                }
            }

            public void setStatus(String theStatus) {
                status = theStatus;
            }
    }
} 

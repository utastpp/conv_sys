/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dic;

import cmcrdr.sqlite.SqliteOperation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import rdr.logger.Logger;


public class DicManager {
    private Dictionary currentDic = new Dictionary();
    

    public static HashMap<Integer,Dictionary> getAllDictionaryFromDB() {
        HashMap<Integer,Dictionary> dicList = SqliteOperation.getDicList();
        
        return dicList;
    }
    

    public void setCurrentDicByNameFromDB(String dicName) {
        Dictionary aDic = SqliteOperation.getDicByName(dicName);
        int dicId = aDic.getDicId();
        
        
        HashMap<Integer,DicRepTerm> representativeTermHashMap = SqliteOperation.getRepresentativeTermList(dicId);

        HashMap<Integer,DicTerm> matchingTermList = SqliteOperation.getMatchingTermList();
        
        // Get a set
         Set representativeSet = representativeTermHashMap.entrySet();
         //Logger.info(representativeSet.size() + "size");
        // Get an iterator
        Iterator representativeIterator = representativeSet.iterator();

        
        while (representativeIterator.hasNext()) {
            Map.Entry me = (Map.Entry) representativeIterator.next();     
            
            int representativeId = (int) me.getKey();
            DicRepTerm aDicRepTerm = (DicRepTerm)me.getValue();
            
            //String representativeTerm = (String) me.getValue();
            String representativeTerm = aDicRepTerm.getRepresentativeTerm();
            
            DicTerm aTerm = matchingTermList.get(representativeId);
            if(aTerm!=null){
                aTerm.setRepresentativeTerm(representativeTerm);
                aTerm.setRepresentativeTermId(representativeId);
                aTerm.setAllowRandomSynonym(aDicRepTerm.getAllowRandomSynonym());
                aDic.addDicTerm(aTerm);
            }
            else { // we have a representative term, but no matching terms, so add rep term anyway.
                aTerm = new DicTerm();
                aTerm.setRepresentativeTermId(representativeId);
                aTerm.setRepresentativeTerm(representativeTerm);
                aTerm.setAllowRandomSynonym(aDicRepTerm.getAllowRandomSynonym());

                aDic.addDicTerm(aTerm);
            }
            //else {
            //    Logger.info("DELETING REPRESENTATIVE TERM: " + representativeTerm + " as it has no matching terms..");
            //    SqliteOperation.deleteDicRepresentativeTerm(dicId, representativeTerm);
            //}
        }
        this.currentDic= aDic;
    }
    

    public void createNewDic(String dicName) {
        int dicId = SqliteOperation.insertDic(dicName);
        
        this.currentDic.setDicId(dicId);
        this.currentDic.setDicName(dicName);
    }
    

    public void setCurrentDic(Dictionary aDic) {
        this.currentDic = aDic;
    }    
    

    public Dictionary getCurrentDic() {
        return this.currentDic;
    }   
    

    public DicTerm addRepresentativeTerm(DicTerm aDicTerm){
        Logger.info("About to save rep term " + aDicTerm.getRepresentativeTerm());
        int representativeId = SqliteOperation.insertDicRepresentativeTerm(this.currentDic.getDicId(), aDicTerm.getRepresentativeTerm(), aDicTerm.getAllowRandomSynonym());

        //ArrayList<String> matchingTermsFromDatabase;
        //ArrayList<String> masterList = new ArrayList<>();
        
        aDicTerm.setRepresentativeTermId(representativeId);
        
        Set terms = aDicTerm.getTermBase().entrySet();
        
        // Get an iterator
        Iterator termIterator = terms.iterator();
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();            
            String matchingTerm = (String) me.getValue();  
                   
 
            // we have a direct matching term, so add it to the system database
            SqliteOperation.insertDicMatchingTerm(representativeId, matchingTerm);
            
        }

        this.currentDic.addDicTerm(aDicTerm);

        return aDicTerm;
    }
    

    public DicTerm addRepresentativeTermUsingString(String representativeTerm, boolean allowRandomSynonym){
        DicTerm aDicTerm = new DicTerm();
        int representativeId = SqliteOperation.insertDicRepresentativeTerm(this.currentDic.getDicId(), representativeTerm, allowRandomSynonym);

        aDicTerm.setRepresentativeTerm(representativeTerm);
        aDicTerm.setRepresentativeTermId(representativeId);
        aDicTerm.setAllowRandomSynonym(allowRandomSynonym);

        this.currentDic.addDicTerm(aDicTerm);

        return aDicTerm;
    }
    

    public void addMatchingTerm(String representativeTerm, String matchingTerm, boolean allowRandomSynonyms) {
        if(this.currentDic.isDicTermExist(representativeTerm)){
            
            DicTerm aDicTerm = this.currentDic.getDicTermByRepresentative(representativeTerm);
            aDicTerm.addMatchingTerm(matchingTerm);     

            SqliteOperation.insertDicMatchingTerm(aDicTerm.getRepresentativeTermId(), matchingTerm);
            
            this.currentDic.addDicTerm(aDicTerm);       
            
        } else {    
            
            DicTerm aDicTerm = this.addRepresentativeTermUsingString(representativeTerm,allowRandomSynonyms);

            SqliteOperation.insertDicMatchingTerm(aDicTerm.getRepresentativeTermId(), matchingTerm);
            aDicTerm.addMatchingTerm(matchingTerm);         
            this.currentDic.addDicTerm(aDicTerm);
        }
        
    }

    public void deleteRepresentativeTerm(String representativeTerm){
        DicTerm aDicTerm = this.currentDic.getDicTermByRepresentative(representativeTerm);
        int representativeId = aDicTerm.getRepresentativeTermId();
        
        SqliteOperation.deleteDicRepresentativeTerm(this.currentDic.getDicId(), representativeTerm);
        
        SqliteOperation.deleteDicMatchingTermByRepresentativeId(representativeId);
        
        this.currentDic.deleteDicTerm(aDicTerm);
    }
    

    public void deleteMatchingTermsForRepresentativeTerm(String representativeTerm){
        DicTerm aDicTerm = this.currentDic.getDicTermByRepresentative(representativeTerm);
        if (aDicTerm != null) {
            int representativeId = aDicTerm.getRepresentativeTermId();
            // delete from the list..
            aDicTerm.getMatchingTerm().clear();
            // delete from the database..
            SqliteOperation.deleteDicMatchingTermByRepresentativeId(representativeId);   
        }
    }
    

    public boolean isValidRepresentative(String representativeTerm){
        boolean result= false;
        String tmpRepresentativeTerm = representativeTerm;
        int cnt = 0;
        if(representativeTerm.startsWith("/")){
            if(representativeTerm.endsWith("/")){
                tmpRepresentativeTerm = representativeTerm.substring(1, representativeTerm.length()-1);
                if(!tmpRepresentativeTerm.contains("/")){
                    result = true;
                }
            }
        }
        
        return result;
    }
    

    public void initialiseUserDictionaryFromFile() {
        /*
        this.addMatchingTerm("/hello/", "hello");
        this.addMatchingTerm("/hello/", "hi");
        this.addMatchingTerm("/hello/", "good day");
        this.addMatchingTerm("/hello/", "hullo");
        this.addMatchingTerm("/hello/", "hi");
        */
    }
    
//    
//    public static void initialiseUserDictionary() {
//        DicManager aDicManager = new DicManager();
//        aDicManager.createNewDic("User");
//        
//        aDicManager.addMatchingTerm("/test/", "a dictionary term");
//        
//    }
    
}

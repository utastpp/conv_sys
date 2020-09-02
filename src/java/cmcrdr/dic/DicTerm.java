/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import cmcrdr.logger.Logger;

public class DicTerm {
    
    private int representativeTermId;
    

    private String representativeTerm;
    
    private boolean allowRandomSynonym;
    
    private HashMap<Integer,String> matchingTermSet = new HashMap<>();
    
    

    public DicTerm() {
        
    }
    
    
    public DicTerm cloneDicTerm(){
        DicTerm aDicTerm = new DicTerm();
        aDicTerm.setRepresentativeTerm(this.representativeTerm);
        aDicTerm.setRepresentativeTermId(this.representativeTermId);
        aDicTerm.setMatchingTerm(this.matchingTermSet);
        aDicTerm.setAllowRandomSynonym(this.allowRandomSynonym);
        
        return aDicTerm;
    }
    
    
    
    public int getMatchingTermSize(){
        return this.matchingTermSet.size();
    }
    

    public void setRepresentativeTermId(int representativeTermId){
        this.representativeTermId = representativeTermId;
    }
    

    public int getRepresentativeTermId(){
        return this.representativeTermId;
    }
    

    public void setRepresentativeTerm(String representativeTerm){
        this.representativeTerm = representativeTerm;
    }
    

    public String getRepresentativeTerm(){
        return this.representativeTerm;
    }
    
    public void setAllowRandomSynonym(boolean allowRandom){
        this.allowRandomSynonym = allowRandom;
    }
    

    public boolean getAllowRandomSynonym(){
        return this.allowRandomSynonym;
    }
    

    public HashMap<Integer,String> getTermBase(){
        return this.matchingTermSet;
    }
    

    public boolean addMatchingTerm(String term) {
        //Logger.info("Trying to add synonym: " + term + " to dictionary for representative term: " + this.getRepresentativeTerm());
        // prevent adding synonyms if any term (or sub-term) equals the term we're trying to add.
        Iterator it = matchingTermSet.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry)it.next();
            int matchingTermId = (int) me.getKey();          
            String matchingTerm = (String) me.getValue();
            
            //Logger.info("Current existing term we're looking at is: " + matchingTerm );
            
            /*  commented out testing sub terms, just match whole phrase.. DPH 30/6/216 
            Remove the next statement if this stuffs up matching terms.. */
            
            if (matchingTerm.toLowerCase().equals(term.toLowerCase()))
                return false;
            
            /*  commented out testing sub terms, just match whole phrase as above.. DPH 30/6/216
            
            String []items = matchingTerm.split(" ");
            for (int i = 0; i < items.length; i++) {
                //Logger.info("1 - Looking at " + matchingTerm + " sub-item " + items[i] );

                // look for the term in each existing dictionary term (if a term has spaces,
                // split it up and match each word)
                if (items[i].equals(term)) {
                    //Logger.info("Cannot add - found a matching term for " + term + " in " + matchingTerm);
                    return false;
                }
                
                // for each existing dictionary term, see if any match to sub-words in the term we're 
                // trying to add..
                String []termItems = term.split(" ");
                for (int j = 0; j < termItems.length; j++) {
                    //Logger.info("2 - Comparing " + term + " sub-item " + termItems[j] + " against " + items[i]);

                    if (items[i].equals(termItems[j])) {
                        //Logger.info("Cannot add - found a matching term for " + termItems[j] + " in " + matchingTerm);
                        return false;
                    }
                }
            }
        */   
            
        }
            
            
            
        //Logger.info("Attempting to add: " + term + " into matchingTermSet, which has size: " + matchingTermSet.size());
        //for (String item: this.matchingTermSet.values()) {
            //Logger.info("BEFORE LAST CALL - item is: " + item);
       // }
        
        // in case of deletions, we need to actually get a unique ID, not just the set size..
        int maxId = 0;
        for (int i : this.matchingTermSet.keySet()) {
            maxId = Integer.max(maxId, i);
        }
        maxId++;
        
        this.matchingTermSet.put(maxId, term);
       
        
        //for (String item: this.matchingTermSet.values()) {
            //Logger.info("LAST CALL - item is: " + item);
        //}
        return true;
        
        /*
        if(matchingTermSet.containsValue(term)){
            return false;
        } else {
            this.matchingTermSet.put(matchingTermSet.size(), term);
            return true;
        }
        */
        
    }
    

    public boolean deleteMatchingTerm(String term) {

        if(!this.matchingTermSet.containsValue(term)){
            return false;
        } else {
            Set terms = this.matchingTermSet.entrySet();
            // Get an iterator
            Iterator termIterator = terms.iterator();
            while (termIterator.hasNext()) {
                Map.Entry me = (Map.Entry) termIterator.next();
                int matchingTermId = (int) me.getKey();          
                String matchingTerm = (String) me.getValue();          
                if(matchingTerm.equals(term)){
                    this.matchingTermSet.remove(matchingTermId);
                    break;
                }            
            }
            return true;
        }
    }
    

    public void setMatchingTerm(HashMap<Integer,String> matchingTermSet){
        this.matchingTermSet = matchingTermSet;
    }
    

    public HashMap<Integer,String> getMatchingTerm(){
        return this.matchingTermSet;
    }
    
    
    public boolean isMatchingTermExist(String matchingTerm){
        return this.matchingTermSet.containsValue(matchingTerm);
    }
    

    public String[][] toMatchingTermStringArrayForGUI() {
        
        int wordCount = this.getMatchingTermSize();
        // new object for gui
        String[][] newObject = new String[wordCount][1];

        Set terms = this.matchingTermSet.entrySet();
        // Get an iterator
        Iterator termIterator = terms.iterator();
        int cnt = 0;
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            String matchingTerm = (String) me.getValue();            
            
            //store case id in first column
            newObject[cnt][0] = matchingTerm;

            cnt++;
        }       
        
        return newObject;
    }
}

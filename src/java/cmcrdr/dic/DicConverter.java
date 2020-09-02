/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dic;

import cmcrdr.logger.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DicConverter {
    private Dictionary currentDic;
    private HashMap<Integer,DicTermMap> recentUsedDicTermMap = new HashMap<Integer,DicTermMap>();
    private LinkedHashMap<Integer,DicTermMap> pendingDicTermMap;
    
    public static String REGULAR_EXPRESSION = "/re:";
    
    /**
     *
     */
    public void clearConverter() {
        this.currentDic = null;
        this.recentUsedDicTermMap = new HashMap<Integer,DicTermMap>();
    }
    
    /**
     *
     * @param dictionary
     */
    public void setDictionary (Dictionary dictionary){
        this.currentDic = dictionary;
    }
    
    /**
     *
     * @return
     */
    public Dictionary getDictionary (){
        return this.currentDic;
    }
    
    /**
     *
     * @param aDicTermMap
     * @return
     */
    public int addDicTermMapToRecent(DicTermMap aDicTermMap){
        int newId = recentUsedDicTermMap.size();
        
        this.recentUsedDicTermMap.put(newId, aDicTermMap);
        
        return newId;
    }
    
    /**
     *
     * @param aDicTermMap
     * @return
     */
    public int addDicTermMapToPending(DicTermMap aDicTermMap){
        int newId = pendingDicTermMap.size();
        
        this.pendingDicTermMap.put(newId, aDicTermMap);
        
        return newId;
    }
    
    /**
     *
     * @param aDicTerm
     * @param matchingTerm
     * @return
     */
    public int addDicTermMapToRecent(DicTerm aDicTerm, String matchingTerm){
        int newId = recentUsedDicTermMap.size();
        DicTermMap dicTermMap = new DicTermMap();
        
        dicTermMap.addMap(matchingTerm, aDicTerm);
        
        this.recentUsedDicTermMap.put(newId, dicTermMap);
        
        return newId;
    }
    
    /**
     *
     * @param aDicTerm
     * @param matchingTerm
     * @return
     */
    public int addDicTermMapToPending(DicTerm aDicTerm, String matchingTerm){
        int newId = pendingDicTermMap.size();
        DicTermMap dicTermMap = new DicTermMap();
        
        dicTermMap.addMap(matchingTerm, aDicTerm);
        
        this.pendingDicTermMap.put(newId, dicTermMap);
        
        return newId;
    }
    
    /**
     *
     * @param id
     * @return
     */
    public DicTermMap getDicTermMapFromRecentById(int id){
        if(this.recentUsedDicTermMap.containsKey(id)){
            return this.recentUsedDicTermMap.get(id);
        } else {
            throw new UnsupportedOperationException("The given id is not in the recent list."); 
        }
    }
    
    /**
     *
     * @return
     */
    public DicTermMap getRecentDicTermMap(){
        if(!this.recentUsedDicTermMap.isEmpty()){
            return this.recentUsedDicTermMap.get(recentUsedDicTermMap.size()-1);
        } else {
            throw new UnsupportedOperationException("There is no recent DicTermMap."); 
        }
    }
    
    /**
     *
     * @param representativeTerm
     * @return
     */
    public DicTermMap getRecentDicTermMapByRepresentative(String representativeTerm){
        if(!this.recentUsedDicTermMap.isEmpty()){
            for(int i=recentUsedDicTermMap.size(); i>0; i-- ){
                DicTermMap aDicTermMap = this.recentUsedDicTermMap.get(i);
                if(aDicTermMap.getDicTerm().getRepresentativeTerm().equals(representativeTerm)){
                    return this.recentUsedDicTermMap.get(recentUsedDicTermMap.size()-1);
                }
            }
            throw new UnsupportedOperationException("There is no recent DicTermMap of the given representative term.");
        } else {
            throw new UnsupportedOperationException("There is no recent DicTermMap."); 
        }
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String replaceFromRepresentativeToRecentMatchingTerm(String input){        
        String output = input;
        HashSet<String> usedRepresentativeTerm = new HashSet<String>();
        if(!this.recentUsedDicTermMap.isEmpty()){
            for(int i=recentUsedDicTermMap.size()-1; i>=0; i-- ){
                DicTermMap aDicTermMap = this.recentUsedDicTermMap.get(i);
                
                String aRepresentativeTerm = aDicTermMap.getDicTerm().getRepresentativeTerm();
                
                if(!usedRepresentativeTerm.contains(aRepresentativeTerm)){
                    if(isContainExactTerm(input, aRepresentativeTerm)){
                        //output = replaceExactTerm(output, aRepresentativeTerm, aDicTermMap.getMatchingTerm());                        
                        output = replaceExactPhrase(output, aRepresentativeTerm, aDicTermMap.getMatchingTerm());                        
                        usedRepresentativeTerm.add(aRepresentativeTerm);
                    }
                }
            }
            return output;
        } else {
            throw new UnsupportedOperationException("There is no recent DicTermMap."); 
        }
    }
    
    /**
     *
     * @return
     */
    public boolean isRecentListEmpty(){
        return this.recentUsedDicTermMap.isEmpty();
    }
    
    
    
    public boolean containsMatchForRepresentativeTerm(String input, String representativeTerm) {
        DicTerm aDicTerm = this.currentDic.getDicTermByRepresentative(representativeTerm);
        
        if (aDicTerm != null) {
            for (String aMatchingTerm : aDicTerm.getMatchingTerm().values()) {
                //if (isContainExactPhrase(input.toLowerCase(), aMatchingTerm.toLowerCase(),0))
                if (isContainExactPhrase(input.toLowerCase(), aMatchingTerm.toLowerCase()))
                    return true;             
            }
        }
        
        return false;
    }
    
    /**
     *
     * @param input
     * @param phrase
     * @return
     */
    public String getFirstMatchingTermFromDic(String input, boolean phrase){
    
        String output = input;
        String result = null;
        
               
        Set category = this.currentDic.getDicBase().entrySet();
        // Get an iterator
        Iterator categoryIterator = category.iterator();
        while (categoryIterator.hasNext()) {
            Map.Entry me = (Map.Entry) categoryIterator.next();
            String representative = (String) me.getKey();
            
            DicTerm dicTermInstance = (DicTerm) me.getValue();
            
            Set terms = dicTermInstance.getTermBase().entrySet();
            // Get an iterator
            Iterator termsIterator = terms.iterator();
            while (termsIterator.hasNext()) {
                Map.Entry me2 = (Map.Entry) termsIterator.next();
                String termInstance = (String) me2.getValue();  
                               
                if (phrase) {
                    //if (isContainExactPhrase(output, termInstance,0)){
                    if (isContainExactPhrase(output, termInstance)){
                        output = replaceExactPhrase(output, termInstance, representative);   
                        result = representative;
                        DicTermMap aDicTermMap = new DicTermMap();
                        aDicTermMap.addMap(termInstance, dicTermInstance);
                        this.addDicTermMapToRecent(aDicTermMap);

                        break;
                    }
                }
                else {  // testing all matches with phrase only (hence repetition of code from above)
                    if (isContainExactTerm(output, termInstance)){
                    //if (isContainExactPhrase(output, termInstance,0)){
                        //output = replaceExactTerm(output, termInstance, representative);   
                        output = replaceExactPhrase(output, termInstance, representative);   
                        result = representative;
                        DicTermMap aDicTermMap = new DicTermMap();
                        aDicTermMap.addMap(termInstance, dicTermInstance);
                        this.addDicTermMapToRecent(aDicTermMap);
                        break;
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     *
     * @param pendingList
     * @return
     */
    public LinkedHashMap<Integer,DicTermMap> getSortedByMatchingTermWordCount(LinkedHashMap<Integer,DicTermMap> pendingList) {
        LinkedHashMap<Integer,DicTermMap> result = new LinkedHashMap<>();
        
        ArrayList<DicTermMap> entries = new ArrayList<>(pendingList.values());
              
        Collections.sort(entries, new Comparator<DicTermMap>() {
            @Override
            public int compare(DicTermMap o1, DicTermMap o2) {
                if (!o2.getMatchingTerm().startsWith(REGULAR_EXPRESSION) && !o1.getMatchingTerm().startsWith(REGULAR_EXPRESSION)) 
                    return o2.getMatchingTermWordCount() > o1.getMatchingTermWordCount() ? 1 : -1 ;
                else if (o2.getMatchingTerm().startsWith(REGULAR_EXPRESSION) && !o1.getMatchingTerm().startsWith(REGULAR_EXPRESSION)) 
                    return  -1 ;
                else if (!o2.getMatchingTerm().startsWith(REGULAR_EXPRESSION) && o1.getMatchingTerm().startsWith(REGULAR_EXPRESSION)) 
                    return 1;
                
                return o2.getMatchingTermWordCount() > o1.getMatchingTermWordCount() ? 1 : -1 ;

            }
        });
        
        for(DicTermMap aDicTermMap: entries) {
            result.put(result.size(), aDicTermMap);
        }
        
        return result;
    }
    
    
    // DAVE HERE!!!!

    /**
     *
     * @param input
     * @param phrase
     * @return
     */
    public String convertTermFromDic(String input, boolean phrase){
        String output = input;
        
        this.pendingDicTermMap = new LinkedHashMap<>();
        
        Set category = this.currentDic.getDicBase().entrySet();
        // Get an iterator
        
        //Logger.info("Converting : " + input + " from dictionary...");
        Iterator categoryIterator = category.iterator();
        boolean added;
        while (categoryIterator.hasNext()) {
            Map.Entry me = (Map.Entry) categoryIterator.next();
            String representative = (String) me.getKey();
            
            
            
            //Logger.info("Looking at representative: " + representative);
            
            DicTerm dicTermInstance = (DicTerm) me.getValue();
            
            Set terms = dicTermInstance.getTermBase().entrySet();
            // Get an iterator
            Iterator termsIterator = terms.iterator();
            while (termsIterator.hasNext()) {
                Map.Entry me2 = (Map.Entry) termsIterator.next();
                String termInstance = (String) me2.getValue();
                boolean isRegularExpression = termInstance.startsWith(REGULAR_EXPRESSION);
                added = false;
                
                if (phrase) {
                    //if (isContainExactPhrase(output, termInstance,0)){
                    if (isContainExactPhrase(output, termInstance)){
                        //Logger.info("Found an exact phrase - " + termInstance + " in the input string: " + output);
                        
                        // uncomment me to fix!
                        //output = replaceExactPhrase(output, termInstance, representative); 
                        
                        //Logger.info("output string is now: " + output);
                        DicTermMap aDicTermMap = new DicTermMap();
                        aDicTermMap.addMap(termInstance, dicTermInstance);
                        
                        this.addDicTermMapToPending(aDicTermMap);
                        added = true;
                        
                        // uncomment me to fix!
                        //this.addDicTermMapToRecent(aDicTermMap);
                    }
                }
                else {
                    if (isContainExactTerm(output, termInstance)){
                    //if (isContainExactPhrase(output, termInstance,0)){
                        //Logger.info("Found an exact non-phrase - " + termInstance + " in the input string: " + output);

                        //output = replaceExactTerm(output, termInstance, representative);   
                        output = replaceExactPhrase(output, termInstance, representative); 

                        DicTermMap aDicTermMap = new DicTermMap();
                        aDicTermMap.addMap(termInstance, dicTermInstance);

                        this.addDicTermMapToRecent(aDicTermMap);
                        added = true;

                    }
                }
               
                
                if (isRegularExpression) {
                    //Logger.info("Found a regular expression..");
                    String regEx = termInstance.substring(4);
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(output);
                    //Logger.info("We're looking at a regular expression");
                    if (m.matches()) {
                        if (!added) {
                            DicTermMap aDicTermMap = new DicTermMap();
                            aDicTermMap.addMap(termInstance, dicTermInstance);
                            this.addDicTermMapToPending(aDicTermMap);
                            //Logger.info("Found a synonym match with a regular expression" + termInstance);
                        }
                    }
                    //else {
                        //Logger.info("Regular expression doesn't match input..");
                    //}
                }
                    
                    
                
            }
        }

        // ok, so now we've collected all the matching terms, sorted them based on word count, now go and replace
        // matching terms in the output string in the order of terms with the greatest word count first
        // e.g. matching "a fine day" in "a fine day today" take precendence over matching "fine"
        LinkedHashMap<Integer,DicTermMap> sortedList = this.getSortedByMatchingTermWordCount(this.pendingDicTermMap);
        
        for (Map.Entry<Integer, DicTermMap> aDictTermMapEntry :  sortedList.entrySet()) {
            
            String theTermInstance = aDictTermMapEntry.getValue().getMatchingTerm();
            String theRepresentativeString = aDictTermMapEntry.getValue().getDicTerm().getRepresentativeTerm();
            
            if (theTermInstance.startsWith(REGULAR_EXPRESSION)) {
                
                String regEx = theTermInstance.substring(4);
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(output);
                //Logger.info("We're looking at a regular expression");
                if (m.matches()) {
                    int start = m.start();
                    int end = m.end();
                    //Logger.info("Found a synonym match with a regular expression" + theTermInstance);
                    output = output.substring(0,start) + theRepresentativeString + output.substring(start+end);
                    
                    //Logger.info("new output: " + output);
                }
                

            }
            //else if (isContainExactPhrase(output, theTermInstance,0)){
            else if (isContainExactPhrase(output, theTermInstance)){
                output = replaceExactPhrase(output, theTermInstance, theRepresentativeString); 
                // HEY ME, WORK FROM HERE..
                this.addDicTermMapToRecent(aDictTermMapEntry.getValue());
            }
        }
        return output;
    }
    
    /**
     *
     * @param input
     * @param representative
     * @return
     */
    public String convertTermFromDicUsingRepresentative(String input, String representative){
        String output = input;
        
        DicTerm dicTermInstance = this.currentDic.getDicTermByRepresentative(representative);
        
        Set terms = dicTermInstance.getTermBase().entrySet();
        // Get an iterator
        Iterator termsIterator = terms.iterator();
        while (termsIterator.hasNext()) {
            Map.Entry me2 = (Map.Entry) termsIterator.next();
            String termInstance = (String) me2.getValue();

            if(isContainExactTerm(output, termInstance)){
            //if(isContainExactPhrase(output, termInstance,0)){
                //output = replaceExactTerm(output, termInstance, representative);   
                output = replaceExactPhrase(output, termInstance, representative);   
            }
        }
        return output;
    }
    
    // similar to isContaintExactTerm but allows terms with spaces in them..

    /**
     *
     * @param caseValue
     * @param condValue
     * @return
     */
    //public boolean isContainExactPhrase(String caseValue, String condValue, int startIndex){
    public boolean isContainExactPhrase(String caseValue, String condValue){
        if (!caseValue.toLowerCase().contains(condValue.toLowerCase())) {
            //Logger.info("input: " + caseValue + " does not contain '" +condValue + "'");
            return false;
        }
        else {

            String pattern = "\\b" + condValue + "\\b";
            //Logger.info("Compiling :" + pattern);
            Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
           // Matcher m = p.matcher(caseValue.substring(startIndex));
            Matcher m = p.matcher(caseValue);

            boolean found = m.find();
            //if (!found)
                //Logger.info("Match not found!");
            
            // test to see if what we have matched is a literal representative term itself that contains the matching term
            // e.g. /abc/ matches abc, so test to see if preceeding character is a backslash
            // This is an issue as then we replace one slash with two i.e. /abc/ later would become //abc//
            // We should really restrict representative terms from not containing any of its synonyms in its name..
            if (found) {
                int location  = m.start();
                if (location != 0) {
                    if (caseValue.charAt(location-1) == '/' )
                        return false;
                }
            }
            
            return found;
        }
    }
    
    /**
     *
     * @param caseValue
     * @param condValue
     * @param replaceValue
     * @return
     */
    public String replaceExactPhrase(String caseValue, String condValue, String replaceValue){
         String result;
         //result = caseValue.replaceAll("\\b" + condValue + "\\b", replaceValue);
         result = caseValue.replaceAll("\\b" + "(?i)" + Pattern.quote(condValue) + "\\b", replaceValue);
         
         return result;
    }
    
    /**
     *
     * @param caseValue
     * @param condValue
     * @return
     */
    public boolean isContainExactTerm(String caseValue, String condValue){
         String[] caseValueArray = caseValue.split(" ");
        for (String caseValueArray1 : caseValueArray) {
            if (caseValueArray1.toLowerCase().equals(condValue.toLowerCase())) {
                return true;
            }
        }
         return false;
    }
    
    /**
     *
     * @param caseValue
     * @param condValue
     * @param replaceValue
     * @return
     */
    public String replaceExactTerm(String caseValue, String condValue, String replaceValue){
         String result = "";
         String[] caseValueArray = caseValue.split(" ");
         for(int i=0; i<caseValueArray.length; i++){
             if(caseValueArray[i].toLowerCase().equals(condValue.toLowerCase())){
                 if(i!=0){
                  result += " ";   
                 }
                 caseValueArray[i] = replaceValue;
                 result += caseValueArray[i];
                 
             } else {
                 if(i!=0){
                  result += " ";   
                 }
                 result += caseValueArray[i];
             }
         }
         
         return result;
    }
    
    /**
     *
     * @param term
     * @param input
     * @return
     */
    //public int[] getValidMatchingTermLocation(String term, String input, int startIndex) {
    public int[] getValidMatchingTermLocation(String term, String input) {
        int [] index = {-1,-1};
        
        if (this.currentDic != null && this.currentDic.isDicTermExist(term)) {
            DicTerm representativeTerm = this.currentDic.getDicTermByRepresentative(term);
            HashMap synonymsList = representativeTerm.getMatchingTerm();
            Iterator synonymIterator = synonymsList.entrySet().iterator();
            
            while (synonymIterator.hasNext()) {
                Map.Entry me = (Map.Entry)synonymIterator.next();
                int synonymId = (int)me.getKey();
                String synonymValue = (String)me.getValue();
                
                // REGULAR EXPRESSION MATCHING LOCATION
                if (synonymValue.startsWith(REGULAR_EXPRESSION)) {
                    // we need to determine the match location for the regular expression..
                    String regEx = synonymValue.substring(4);
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(input);
                    if (m.matches()) {
                        index[0] = m.start();
                        index[1] = m.end();
                        return index;
                    }
                }
               // if (isContainExactPhrase(input,synonymValue,startIndex)) {
                if (isContainExactPhrase(input,synonymValue)) {
                    //Logger.info("Input is: [" + input + "] and synonym is: [" + synonymValue + "]");
                    //index[0] = input.toLowerCase().indexOf(synonymValue.toLowerCase(),startIndex);
                    index[0] = input.toLowerCase().indexOf(synonymValue.toLowerCase());
                    //Logger.info("index[0] is " + index[0]);

                    index[1] = index[0] + synonymValue.length();
                    return index;
                }
            }
        }     
        return index;
    }
    
    /**
     *
     * @param phrase
     * @param input
     * @return
     */
    //public int[] getValidMatchingPhraseLocation(String phrase, String input, int start) {
    public int[] getValidMatchingPhraseLocation(String phrase, String input) {
        int [] index = {-1,-1};
        //if (isContainExactPhrase(input,phrase,start)) {
        if (isContainExactPhrase(input,phrase)) {
            //index[0] = input.toLowerCase().indexOf(phrase.toLowerCase(),start);
            index[0] = input.toLowerCase().indexOf(phrase.toLowerCase());
            index[1] = index[0] + phrase.length();
        }
        return index;
    }

    
    /**
     *
     * @param representativeTerm
     * @return
     */
    public String getFirstMatchForRepresentationTerm(String representativeTerm) {
        String result = representativeTerm;
    
        if (this.currentDic != null && this.currentDic.isDicTermExist(representativeTerm)) {
            DicTerm repDicTerm = this.currentDic.getDicTermByRepresentative(representativeTerm);
            HashMap synonymsList = repDicTerm.getMatchingTerm();

            Iterator iter  = synonymsList.entrySet().iterator();
            if (iter.hasNext()) {
                Map.Entry me = (Map.Entry)iter.next();
                
                result = (String)me.getValue();
            }
        }   
        
        return result;
    }
    
    /**
     *
     * @param representativeTerm
     * @return
     */
    public String getRandomMatchForRepresentationTerm(String representativeTerm) {
        String temp = "";
        String result = representativeTerm;
        
        Random generator = new Random();
        
       // Logger.info("I'm being asked to find matching term for " + representativeTerm);
        String [] repTerms = representativeTerm.split(" ");
        
        
        for (String aRepTerm: repTerms) {
          //  Logger.info("   The rep term expands to the following terms:" + aRepTerm);
        
            if (this.currentDic != null && this.currentDic.isDicTermExist(aRepTerm)) {
                DicTerm repDicTerm = this.currentDic.getDicTermByRepresentative(aRepTerm);
                HashMap synonymsList = repDicTerm.getMatchingTerm();
                int synonymChosen = generator.nextInt(synonymsList.size());
                int count = 0; 
                Iterator iter  = synonymsList.entrySet().iterator();

                if (repDicTerm.getAllowRandomSynonym()) {           
                    while (iter.hasNext()) {
                        Map.Entry me = (Map.Entry)iter.next();               
                        if (count == synonymChosen) {
                            // we don't want to offer a synonym that's a regular expression..
                            if (!((String)me.getValue()).startsWith(REGULAR_EXPRESSION)) {
                                if (temp.isEmpty())
                                    temp = (String)me.getValue();
                                else    
                                    temp += " " + (String)me.getValue();
                                break;
                            }
                        }
                        else
                            count ++;
                    }
                }
                else {
                    // We're only going to offer the first (non) regular expression
                    while (iter.hasNext()) {
                        Map.Entry me = (Map.Entry)iter.next();
                        // we don't want to offer a synonym that's a regular expression..
                        if (!((String)me.getValue()).startsWith(REGULAR_EXPRESSION)) {
                            if (temp.isEmpty())
                                temp = (String)me.getValue();
                            else    
                                temp += " " + (String)me.getValue();
                            
                            break;
                        }                       
                    }
                }
            }  
        }
        
        if (temp.isEmpty())
            return result;
        else
            return temp;
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String getFirstMatchForAllRepresentationTerms(String input) {
        String [] repTerms = input.split("/");
        String result = "";
        
        for (String aRepTerm: repTerms) {
            
            
            if (this.currentDic != null && this.currentDic.isDicTermExist("/" + aRepTerm + "/")) {
               // Logger.info("Found term: /" + aRepTerm + "/");
                DicTerm repDicTerm = this.currentDic.getDicTermByRepresentative("/" + aRepTerm + "/");
                HashMap synonymsList = repDicTerm.getMatchingTerm();

                Iterator iter  = synonymsList.entrySet().iterator();
                if (iter.hasNext()) {
                    Map.Entry me = (Map.Entry)iter.next();
                    if (!result.isEmpty())
                        result += " " + (String)me.getValue();
                    else
                        result += (String)me.getValue();

                }
            }   
        }

        return result;
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String replaceFromRepresentativeToFirstMatchingTerm(String input){        
        String output = input;
        HashSet<String> usedRepresentativeTerm = new HashSet<String>();
        if(!this.recentUsedDicTermMap.isEmpty()){
            for(int i=recentUsedDicTermMap.size()-1; i>=0; i-- ){
                DicTermMap aDicTermMap = this.recentUsedDicTermMap.get(i);
                
                String aRepresentativeTerm = aDicTermMap.getDicTerm().getRepresentativeTerm();
                
                if(!usedRepresentativeTerm.contains(aRepresentativeTerm)){
                   if(isContainExactTerm(input, aRepresentativeTerm)){
                   // if(isContainExactPhrase(input, aRepresentativeTerm,0)){
                        //output = replaceExactTerm(output, aRepresentativeTerm, aDicTermMap.getMatchingTerm());                        
                        output = replaceExactPhrase(output, aRepresentativeTerm, aDicTermMap.getMatchingTerm());                        
                        usedRepresentativeTerm.add(aRepresentativeTerm);
                    }
                }
            }
            return output;
        } else {
            throw new UnsupportedOperationException("There is no recent DicTermMap."); 
        }
    }
    
    
    public String getOriginalInputForRepresentative(String input, String representativeTerm){        
        String output = "";
        
       // Logger.info("Looking at representative:" + representativeTerm);
        String [] allRepTerms = representativeTerm.split(" ");
        
        
        for (String aRepTerm: allRepTerms) {
            DicTerm aDicTerm = currentDic.getDicTermByRepresentative(aRepTerm);

            if (aDicTerm != null) {
                for (String matchingTerm: aDicTerm.getMatchingTerm().values()) {
                   // Logger.info("Looking to see if " + matchingTerm + " matches " + input);
                    //if(isContainExactPhrase(input, matchingTerm,0)) {
                    if(isContainExactPhrase(input, matchingTerm)) {
                        if (output.isEmpty())
                            output =  matchingTerm;
                        else   
                            output += " " + matchingTerm;
                        break;
                    }
                }

            }
        }
        return output;
    }
}

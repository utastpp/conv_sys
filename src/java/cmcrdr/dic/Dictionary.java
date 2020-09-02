/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dic;

import cmcrdr.logger.Logger;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class Dictionary {
    

    private int dicId;
    
    private String dicName;

    private LinkedHashMap<String, DicTerm> dic = new LinkedHashMap<>();

    public Dictionary(){
        this.dic = new LinkedHashMap<>();
    }
    
    public void setDicId(int dicId){
        this.dicId = dicId;
    }
    
    public int getDicId(){
        return this.dicId;
    }
    
    public void setDicName(String dicName){
        this.dicName = dicName;
    }
    

    public String getDicName(){
        return this.dicName;
    }
    

    public int getDicTermAmount(){
        return this.dic.size();
    }
    

    public LinkedHashMap<String, DicTerm> getDicBase(){
        return this.dic;
    }
    

    public void setDicBase( LinkedHashMap<String, DicTerm> aDic){
        this.dic = aDic;
    }
    

    public boolean addDicTerm(DicTerm dicTerm) {
        if(this.dic.containsKey(dicTerm.getRepresentativeTerm())){
            return false;
        } else {
            this.dic.put(dicTerm.getRepresentativeTerm(), dicTerm);
            return true;
        }
    }
    

    public boolean deleteDicTerm(DicTerm dicTerm) {
        if(!this.dic.containsKey(dicTerm.getRepresentativeTerm())){
            return false;
        } else {
            this.dic.remove(dicTerm.getRepresentativeTerm());
            return true;
        }
    }
    

    public boolean isDicTermExist(String representativeTerm){
        return this.dic.containsKey(representativeTerm);
    }
    

    public DicTerm getDicTermByRepresentative(String representativeTerm){
        return this.dic.get(representativeTerm);
    }
    
    public String[][] toRepresentativeTermStringArrayForGUI() {
        
        int wordCount = this.getDicTermAmount();
        // new object for gui
        String[][] newObject = new String[wordCount][1];

        Set terms = this.dic.entrySet();
        // Get an iterator
        Iterator termIterator = terms.iterator();
        int cnt = 0;
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            String representativeTerm = (String) me.getKey();            
            
            newObject[cnt][0] = representativeTerm;

            cnt++;
        }       
        
        return newObject;
    }

    public String[] getRepresentativeTermsForWeb() {
        
        int wordCount = this.getDicTermAmount();
        // new object for gui
        String[] newObject = new String[wordCount];

        Set terms = this.dic.entrySet();
        // Get an iterator
        Iterator termIterator = terms.iterator();
        int cnt = 0;
        while (termIterator.hasNext()) {
            Map.Entry me = (Map.Entry) termIterator.next();
            String representativeTerm = (String) me.getKey();            
            
            newObject[cnt] = representativeTerm;

            cnt++;
        }       
        
        return newObject;
    }

    public void moveTermUpInList(String term) {
        LinkedHashMap <String, DicTerm> remappedDic = new LinkedHashMap<>();
        
        Iterator termIterator = this.dic.entrySet().iterator();
        
        Map.Entry current = null;
        Map.Entry previous = null;
        String representativeTerm;
        
        if (termIterator.hasNext()) {
            previous = (Map.Entry) termIterator.next();
            Logger.info("Orginal term order: " + previous.getKey());
            
            if (!termIterator.hasNext()) {
                remappedDic.put((String)previous.getKey(), (DicTerm)previous.getValue());
            }
        }
        
        if (previous != null) {
            while (termIterator.hasNext()) {
                current = (Map.Entry) termIterator.next();
                Logger.info("Orginal term order: " + current.getKey());
                representativeTerm = (String) current.getKey();
                if (term.equals(representativeTerm)) {
                    remappedDic.put((String)current.getKey(), (DicTerm)current.getValue());
                }
                else {
                    remappedDic.put((String)previous.getKey(), (DicTerm)previous.getValue());
                    previous = current;

                }

            }
            remappedDic.put((String)previous.getKey(), (DicTerm)previous.getValue());
        }
        
        // let's make sure we've reordered..
        termIterator = remappedDic.entrySet().iterator();
        while (termIterator.hasNext()) {
            current = (Map.Entry) termIterator.next();
            Logger.info("New term order: " + current.getKey());
        }
        this.setDicBase(remappedDic);
    }
    

    public void moveTermDownInList(String term) {
        LinkedHashMap <String, DicTerm> remappedDic = new LinkedHashMap<>();
        
        Iterator termIterator = this.dic.entrySet().iterator();
        
        Map.Entry current = null;
        Map.Entry next = null;
        Map.Entry previous = null;
        String representativeTerm;
        
        if (termIterator.hasNext()) {
            current = (Map.Entry) termIterator.next();
            Logger.info("Orginal term order: " + current.getKey());
            
            if (!termIterator.hasNext()) {
                remappedDic.put((String)current.getKey(), (DicTerm)current.getValue());
            }
        }
        
        if (current != null) {
            while (termIterator.hasNext()) {
                
                if (previous != null) {
                    Logger.info("Inserting previous value: " + previous.getKey());

                    remappedDic.put((String)previous.getKey(), (DicTerm)previous.getValue());
                    previous = null;
                }
                
                next = (Map.Entry) termIterator.next();
                Logger.info("Orginal term order: " + next.getKey());
                if (term.equals((String)current.getKey())) {
                    Logger.info("Found matching key: " + current.getKey() + ", so adding next value to list: " + next.getKey());

                    remappedDic.put((String)next.getKey(), (DicTerm)next.getValue());
                    previous = current;
                    current = next;
                }
                else {
                    Logger.info("No match, so adding: " + current.getKey());
                    remappedDic.put((String)current.getKey(), (DicTerm)current.getValue());
                    current = next;

                }

            }
            Logger.info("adding penultimate: " + current.getKey());
            remappedDic.put((String)current.getKey(), (DicTerm)current.getValue());
            
            if (previous != null) {
                Logger.info("Adding last match value:" + previous.getKey());
                remappedDic.put((String)previous.getKey(), (DicTerm)previous.getValue());
            }
        }
        
        // let's make sure we've reordered..
        termIterator = remappedDic.entrySet().iterator();
        while (termIterator.hasNext()) {
            current = (Map.Entry) termIterator.next();
            Logger.info("New term order: " + current.getKey());
        }
        this.setDicBase(remappedDic);
    }
}

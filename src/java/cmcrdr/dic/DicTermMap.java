/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dic;


/**
 *
 * @author hchung
 */
public class DicTermMap {
    private String matchingTerm;
    private DicTerm aDicTerm;
    
    /**
     *
     * @param matchingTerm
     * @param aDicTerm
     */
    public void addMap(String matchingTerm, DicTerm aDicTerm){
        this.matchingTerm = matchingTerm;
        this.aDicTerm = aDicTerm;
    }
    
    /**
     *
     * @param aDicTerm
     */
    public void setDicTerm(DicTerm aDicTerm){
        this.aDicTerm = aDicTerm;
    }
    
    /**
     *
     * @return
     */
    public DicTerm getDicTerm(){
        return this.aDicTerm;
    }
    
    /**
     *
     * @param matchingTerm
     */
    public void setMatchingTerm(String matchingTerm){
        this.matchingTerm = matchingTerm;
    }
    
    /**
     *
     * @return
     */
    public String getMatchingTerm(){
        return this.matchingTerm;
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String replaceFromMatchingToRepresentative(String input){
        String output = input.replace(this.matchingTerm, this.aDicTerm.getRepresentativeTerm());
        
        return output;
    }
    
    /**
     *
     * @param input
     * @return
     */
    public String replaceFromRepresentativeToMatching(String input){
        String output = input.replaceAll(this.aDicTerm.getRepresentativeTerm(), this.matchingTerm);
        
        return output;
    }
    
    /**
     *
     * @return
     */
    public int getMatchingTermWordCount() {      
        return matchingTerm.split("\\s").length;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.dic;

/**
 *
 * @author David Herbert, david.herbert@utas.edu.au
 */
public class DicRepTerm {
    private String representativeTerm;
    private boolean allowRandomSynonym;
    
    public DicRepTerm() {

    }
    
    public String getRepresentativeTerm() {
        return representativeTerm;
    }
    
    public boolean getAllowRandomSynonym() {
        return allowRandomSynonym;
    }
    
    public void setRepresentativeTerm(String repString) {
        representativeTerm = repString;
    }
    
    public void setAllowRandomSynonym(boolean allow) {
        allowRandomSynonym = allow;
    }
            
}

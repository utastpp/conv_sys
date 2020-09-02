/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.processor;

/**
 *
 * @author David Herbert, david.herbert@utas.edu.au
 */
public class PreAndPostProcessorAction {
    private String matchText;
    private String replaceText;
    private Boolean regex;
    private Boolean wordOnly;
    private Boolean startOfInput;
    private Boolean endOfInput;
    private Boolean replaceOption;
    private Boolean upperOption;
    private Boolean lowerOption;
    private Boolean trimOption;
    private Boolean postOption;
    
    
    public PreAndPostProcessorAction(String matchText, String replaceText, Boolean regex, Boolean wordOnly, Boolean startOfInput, Boolean endOfInput, Boolean replaceOption, Boolean upperOption, Boolean lowerOption,Boolean trimOption, Boolean postOption) {
        this.matchText = matchText;
        this.replaceText = replaceText;
        this.regex = regex;
        this.wordOnly = wordOnly;
        this.startOfInput = startOfInput;
        this.endOfInput = endOfInput;
        this.replaceOption = replaceOption;
        this.upperOption = upperOption;
        this.lowerOption = lowerOption;
        this.trimOption = trimOption;
        this.postOption = postOption;
    }
    
    @Override
    public String toString() {
        String option = "";
        if (regex)
            option = "R";
        if (wordOnly)
            option += "w";
        if (startOfInput)
            option += "s";
        if (endOfInput)
            option += "e";

        
        if (replaceOption) {
            option += "r";
            
        }
        else if (upperOption) 
            option += "u";
        else if (lowerOption) 
            option += "l";
        else if (trimOption)
            option += "t";
        
        if (postOption)
            option += "-POST-";
        else
            option += "-PRE-";

        return this.matchText + " [" + option + "] " + this.replaceText;
    }
    
    public  String getMatchText() {
        return matchText;
    }

    public Boolean getRegex() {
        return regex;
    }

    public Boolean getWordOnly() {
        return wordOnly;
    }

    
    public Boolean getStartOfInput() {
        return startOfInput;
    }

    public Boolean getEndOfInput() {
        return endOfInput;
    }

    public Boolean getReplaceOption() {
        return replaceOption;
    }

    public Boolean getUpperOption() {
        return upperOption;
    }

    public Boolean getLowerOption() {
        return lowerOption;
    }

    public Boolean getTrimOption() {
        return trimOption;
    }
    
    public String getReplaceText() {
        return replaceText;
    }
    
    public Boolean getPostOption() {
        return postOption;
    }
    
    public  void setMatchText(String text) {
        matchText = text;
    }

    public void setRegex(Boolean value) {
        regex = value;
    }

    public void setWordOnly(Boolean value) {
        wordOnly = value;
    }

    
    public void setStartOfInput(Boolean value) {
        startOfInput = value;
    }

    public void setEndOfInput(Boolean value) {
        endOfInput = value;
    }

    public void setReplaceOption(Boolean value) {
        replaceOption = value;
    }

    public void setUpperOption(Boolean value) {
        upperOption = value;
    }

    public void setLowerOption(Boolean value) {
        lowerOption = value;
    }

    public void setTrimOption(Boolean value) {
        trimOption = value;
    }
    
    public void setReplaceText(String text) {
        replaceText = text;
    }
    
    public void setPostOption(Boolean value) {
        postOption = value;
    }
    
    public PreAndPostProcessorAction copy() {
        PreAndPostProcessorAction aCopy = new PreAndPostProcessorAction(matchText, replaceText, regex, wordOnly, startOfInput, endOfInput, replaceOption, upperOption, lowerOption, trimOption, postOption);
        return aCopy;
    }

}

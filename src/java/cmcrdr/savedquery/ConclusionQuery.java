/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.savedquery;

/**
 *
 * @author David Herbert david.herbert@utas.edu.au
 */
public class ConclusionQuery {
    private int queryId = -1;
    private String descriptionString;
    private String queryTextString;
    
    /**
     *
     * @param id
     */
    public void setId(int id) {
        queryId = id;
    }
    
    /**
     *
     * @return
     */
    public int getId() {
        return queryId;
    }
    
    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        descriptionString = description;
    }
    
    /**
     *
     * @return
     */
    public String getDescription() {
        return descriptionString;
    }    
    
    /**
     *
     * @param queryText
     */
    public void setQuery(String queryText) {
    
        queryTextString = queryText;
    }
    
    /**
     *
     * @return
     */
    public String getQuery() {
        return queryTextString;
    }
}

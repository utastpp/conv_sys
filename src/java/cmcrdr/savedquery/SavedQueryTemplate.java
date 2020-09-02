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
public class SavedQueryTemplate {
    private int queryId = -1;
    private String descriptionString;
    private String selectString;
    private String joinString;
    private String criteriaString;
    
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
     * @param select
     */
    public void setSelect(String select) {
        selectString = select;
    }
    
    /**
     *
     * @return
     */
    public String getSelect() {
        return selectString;
    }
    
    /**
     *
     * @param join
     */
    public void setjoin(String join) {
        joinString = join;
    }
    
    /**
     *
     * @return
     */
    public String getJoin() {
        return joinString;
    }
    
    /**
     *
     * @param criteria
     */
    public void setCriteria(String criteria) {
        criteriaString = criteria;
    }
    
    /**
     *
     * @return
     */
    public String getCriteria() {
        return criteriaString;
    }    
}

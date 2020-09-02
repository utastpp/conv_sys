/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.domain;

import rdr.cases.CaseStructure;
import rdr.rules.RuleSet;

/**
 * This class is used to define a domain used in RDR
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Domain {
    /**
     * Default identifier
     */
    public final int DEFAULT_DOMAIN_ID = -1;
    
    /**
     * method name for SCRDR
     */
    public final static String SCRDR = "SCRDR";
    
    /**
     * method name for MCRDR
     */
    public final static String MCRDR = "MCRDR";
    
    /**
     * Project identifier
     */
    private int domainId = -1;
    
    /**
     * Domain name
     */
    private String domainName; 
    
    /**
     * RDR method type
     */
    private String reasonerType = "SCRDR";

    /**
     * case structure
     */
    private CaseStructure caseStructure;
    
    /**
     * problem description
     */
    private String problemDescription = "";
    
    /**
     * knowledge base
     */
    private RuleSet kb;
    
    /**
     * Constructor.
     */
    public Domain(){
        
    }
    
    /**
     * Constructor
     * @param domainId
     * @param domainName
     * @param reasonerType
     * @param dbName
     * @param description 
     */
    public Domain(int domainId, String domainName, String reasonerType, 
            String dbName, String description){
        this.domainId = domainId;
        this.domainName = domainName;
        this.reasonerType = reasonerType;
        this.problemDescription = description;
        this.caseStructure = new CaseStructure();
        this.kb = new RuleSet();
    }
    
    /**
     * Constructor
     * @param domainName
     * @param reasonerType
     * @param dbName
     * @param description 
     */
    public Domain(String domainName, String reasonerType, 
            String dbName, String description){
        this.domainName = domainName;
        this.reasonerType = reasonerType;
        this.problemDescription = description;
        this.caseStructure = new CaseStructure();
        this.kb = new RuleSet();
    }
    
    /**
     * Get domain identifier
     * @return 
     */
    public int getDomainId(){
        return this.domainId;
    }
    
    /**
     * Set domain id
     * @param id 
     */
    public void setDomainId(int id){
        this.domainId = id;
    }
    
    /**
     * Get domain name
     * @return 
     */
    public String getDomainName(){
        return this.domainName;
    }
    
    /**
     * Set domain name
     * @param name 
     */
    public void setDomainName(String name) {
        this.domainName = name;
    }
    
    
    /**
     * Get problem description
     * @return 
     */
    public String getDescription(){
        return this.problemDescription;
    }
    
    /**
     * Set problem description
     * @param desc 
     */
    public void setDescription(String desc){
        this.problemDescription = desc;
    }
    
    /**
     * Get reasoner type
     * @return 
     */
    public String getReasonerType(){
        return this.reasonerType;
    }
    
    /**
     * Set reasoner type
     * @param reasonerType 
     */
    public void setReasonerType(String reasonerType){
        this.reasonerType = reasonerType;
    }  
    
    /**
     * Returns true if MCRDR.
     * @return 
     */
    public boolean isMCRDR(){
        return this.reasonerType.equals(Domain.MCRDR);
    }  
    
    /**
     * Returns true if SCRDR.
     * @return 
     */
    public boolean isSCRDR(){
        return this.reasonerType.equals(Domain.SCRDR);
    }  
    
    /**
     * Get case structure
     * @return 
     */
    public CaseStructure getCaseStructure(){
        return this.caseStructure;
    }
    
    /**
     * Set case structure
     * @param caseStructure 
     */
    public void setCaseStructure(CaseStructure caseStructure){
        this.caseStructure = caseStructure;
    }  
    
    
    @Override
    public String toString() {
        return "Domain data: id -> "+this.domainId +", name-> "+this.domainName + ", RDR method->" +
                this.reasonerType + ", desc -> "+this.problemDescription;
    }
}

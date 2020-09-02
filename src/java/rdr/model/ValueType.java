/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.model;

/**
 * This class is used to define attribute type such as continuous, 
 * categorical, text, date and boolean types
 * 
 * @author David Chung
 */
public class ValueType {
    
    /**
     * Numeric type identifier
     */
    public static final int CONTINUOUS = 0;  
    
    /**
     * Nominal type identifier
     */
    public static final int CATEGORICAL = 1;  
    
    /**
     * String type identifier
     */
    public static final int TEXT = 2; //
    
    /**
     * Date type identifier
     */
    public static final int DATE = 3;
   
    /**
     * Date type identifier
     */
    public static final int BOOLEAN = 4;    

    /**
     * Null type
     */
    public static final int NULL_TYPE = 5;

    /**
     * Attribute type string array
     */
    public static final String[] names 
            = {"CONTINUOUS", "CATEGORICAL", "TEXT", "DATE", "BOOLEAN"};

    /**
     * type identifier
     */
    private int typeCode = CONTINUOUS;

    /**
     * Construct a type with a default type code.
     */
    public ValueType() {

    }

    /**
     * Construct a Type
     *
     * @param typeCode the type code
     */
    public ValueType(int typeCode) {
        this.typeCode = typeCode;
    }
    
    /**
     * Construct a Type
     *
     * @param typeName the type name
     */
    public ValueType(String typeName) {                
        this.typeCode = getTypeCodeByName(typeName);
    }
    
    /**
     * Get the type code
     *
     * @return the type code
     */
    public int getTypeCode() {
        return this.typeCode;
    }

    /**
     * Get the type code by Name
     *
     * @param typeName the type name
     * @return the type code
     */
    public final int getTypeCodeByName(String typeName) {
        int returnCode=0;
        typeName = typeName.toUpperCase();
        for(int i=0; i<names.length; i++){
            if(typeName.equals(names[i])){
                returnCode=i;
            }
        }
        return returnCode;
    }

    
    /**
     * Get the type "name" i.e. "CONTINUOUS", "CATEGORICAL", "TEXT", "DATE", "BOOLEAN"
     *
     * @return the type name
     */
    public String getTypeName() {
        if (this.typeCode == -1) {
            return "NULL_TYPE";
        }
        return names[this.typeCode];
    }

    /**
     * Set type
     *
     * @param typeCode the type code
     */
    public void setType(int typeCode) {
        this.typeCode = typeCode;
    }

   
    
    /**
     * Get the sample value "continuous" i.e. "1"
     *
     * @return the sample value
     */
    public Object getSampleValue() {
        if (this.typeCode == 0) {
            return 12.00;
        } else if (this.typeCode == 1) {
            return "Classifications";
        } else if (this.typeCode == 2) {
            return "Text";
        } else if (this.typeCode == 3) {
            return "2001-01-01 00:00:00.0";
        } else if (this.typeCode == 4) {
            return "TRUE";
        } else if (this.typeCode == 5) {
            return "NULL";
        }
        return "NULL";
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValueType)) {
            return false;
        } else {
            ValueType c = (ValueType) other;
            if(typeCode != c.getTypeCode()){
                return false;
            }
        }
        return true;	//found no differences, so equal.
    }    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.typeCode;
        return hash;
    }
    
    
    /**
     * Convert the type to readable text.
     * @return
     */
    @Override
    public String toString() {
        return getTypeName();
    }
}

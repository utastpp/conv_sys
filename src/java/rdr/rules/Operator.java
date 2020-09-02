/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.rules;

/**
 * Operator 
 * @author yangsokk
 */
public class Operator {
    /**
     * Operator identifier for NULL Operator
     */
    public final static int NULL_OP = 0;
    /**
     * Operator identifier for EQUAL Operator
     */    
    public final static int EQUALS = 1;
    /**
     * Operator identifier for NOT EQUAL Operator
     */
    public final static int NOT_EQUALS = 2;
    /**
     * Operator identifier for LESS THAN Operator
     */    
    public final static int LESS_THAN = 3;
    /**
     * Operator identifier for GREATER THAN Operator
     */    
    public final static int GREATER_THAN = 4;
    
    /**
     * Operator identifier for LESS THAN EQUAL Operator
     */    
    public final static int LESS_THAN_EQUALS = 5;
    /**
     * Operator identifier for GREATER THAN EQUAL Operator
     */    
    public final static int GREATER_THAN_EQUALS = 6;
    /**
     * Operator identifier for CONTAIN Operator
     */    
    public final static int CONTAIN = 7;
    /**
     * Operator identifier for NOT_CONTAIN Operator
     */    
    public final static int NOT_CONTAIN = 8;    
    /**
     * Operator identifier for IS TRUE Operator
     */    
    public final static int IS_TRUE = 9;
    /**
     * Operator identifier for IS FALSE Operator
     */    
    public final static int IS_FALSE = 10;
    
    /**
     * Operator identifier for IS TRUE Operator
     */    
    public final static int BEFORE = 11;
    /**
     * Operator identifier for IS TRUE Operator
     */    
    public final static int BEFORE_EQUALS = 12;    
    /**
     * Operator identifier for IS FALSE Operator
     */    
    public final static int AFTER = 13;   
    /**
     * Operator identifier for IS FALSE Operator
     */    
    public final static int AFTER_EQUALS = 14;   
    /**
     * Operator identifier for CONTAIN_EXACT_TERM Operator
     */    
    public final static int CONTAIN_EXACT_TERM = 15;
    /**
     * Operator identifier for NOT_CONTAIN_EXACT_TERM Operator
     */    
    public final static int NOT_CONTAIN_EXACT_TERM = 16;    
    
    /**
     * Operator name string array
     */
    public final static String[] operatorNames = {"NULL_OP", "==", "!=", 
        "<", ">", "<=", ">=", "Contain", "Not Contain",  "True", "False", 
        "Before", "Before or Equals", "After", "After or Equals", "Contain Exact Term", "Not Contain Exact Term"};
   
    /**
     * Operator
     */
    private int operator = 1;

    /**
     * Constructor.
     */
    public Operator() {
        this.operator = 1;
    }
    
    /**
     * Constructor.
     *
     * @param op the operator code
     */
    public Operator(int op) {
        this.operator = op;
    }
    
    /**
     * Get the operator code
     *
     * @return the operator code
     */
    public int getOperator() {
        return this.operator;
    }

    /**
     * Set the operator code
     *
     * @param op the operator code to use
     */
    public void setOperator(int op) {
        this.operator = op;
    }    
    
    /**
     * Get the "name" of the operator i.e. Operator.EQUALS is named "=="
     *
     * @return name
     */
    public String getOperatorName() {
        return Operator.operatorNames[this.operator];
    }

    /**
     * Get the operator code for the opposite operator of this one i.e. ==
     * returns !=, &lt;= returns &gt; etc.
     *
     * @return operator code
     */
    public int getOpposite() {
        switch (operator) {
            case EQUALS:
                return NOT_EQUALS;
            case NOT_EQUALS:
                return EQUALS;
            case LESS_THAN:
                return GREATER_THAN_EQUALS;
            case GREATER_THAN:
                return LESS_THAN_EQUALS;
            case LESS_THAN_EQUALS:
                return GREATER_THAN;
            case GREATER_THAN_EQUALS:
                return LESS_THAN;
            case IS_TRUE:
                return IS_FALSE;
            case IS_FALSE:
                return IS_TRUE;
            case CONTAIN:
                return NOT_CONTAIN;
            case NOT_CONTAIN:
                return CONTAIN;
            case BEFORE:
                return AFTER;
            case AFTER:
                return BEFORE;                
            case BEFORE_EQUALS:
                return AFTER_EQUALS;
            case AFTER_EQUALS:
                return BEFORE_EQUALS;      
            case CONTAIN_EXACT_TERM:
                return NOT_CONTAIN_EXACT_TERM;
            case NOT_CONTAIN_EXACT_TERM:
                return CONTAIN_EXACT_TERM;              
            default:
                return NULL_OP;
        }
    }

    /**
     * Returns an Operator object in response to a string representation of one.
     *
     * @param str the string representation
     * @return the correct Operator object
     */
    public static Operator stringToOperator(String str) {
        str = str.toUpperCase();
        
        if (str.equals("==") || str.equals("=")) {
            return new Operator(EQUALS);
        }
        if (str.equals("!=")) {
            return new Operator(NOT_EQUALS);
        }
        if (str.equals("<")) {
            return new Operator(LESS_THAN);
        }
        if (str.equals(">")) {
            return new Operator(GREATER_THAN);
        }
        if (str.equals("<=")) {
            return new Operator(LESS_THAN_EQUALS);
        }
        if (str.equals(">=")) {
            return new Operator(GREATER_THAN_EQUALS);
        }
        if (str.equals("CONTAIN")) {
            return new Operator(CONTAIN);
        }
        if (str.equals("NOT CONTAIN")) {
            return new Operator(NOT_CONTAIN);
        }        
        if (str.equals("TRUE")) {
            return new Operator(IS_TRUE);
        }
        if (str.equals("FALSE")) {
            return new Operator(IS_FALSE);
        }        
        if (str.equals("BEFORE")) {
            return new Operator(BEFORE);
        }
        if (str.equals("AFTER")) {
            return new Operator(AFTER);
        }        
        if (str.equals("BEFORE EQUAL")) {
            return new Operator(BEFORE_EQUALS);
        }
        if (str.equals("AFTER EQUAL")) {
            return new Operator(AFTER_EQUALS);
        } 
        if (str.equals("CONTAIN EXACT TERM")) {
            return new Operator(CONTAIN_EXACT_TERM);
        }
        if (str.equals("NOT CONTAIN EXACT TERM")) {
            return new Operator(NOT_CONTAIN_EXACT_TERM);
        }
        
        return new Operator(NULL_OP);
    } 
    
    /**
     * Converts this operator into a readable string.
     * @return 
     */
    @Override
    public String toString() {
        return operatorNames[getOperator()];
    }    
}

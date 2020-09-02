/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import rdr.logger.Logger;

/**
 * A Value class is used to define an attribute value and contains value, 
 * an instance of ValueType, and value, an instance of Object. 
 * A Value class provides methods for defining and obtaining value.
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Value {

    /**
     * ValueType instance
     */
    private ValueType type;

    /**
     * Attribute value object
     */
    private Object value;

    /**
     * Constructor.
     */
    public Value() {
        this.type = null;
        this.value = null;
    }

    /**
     * Constructor.
     *
     * @param type the type
     * @param value the actual value object
     */
    public Value(ValueType type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param typeCode the type code
     * @param value the actual value object
     */
    public Value(int typeCode, Object value) {
        this.type = new ValueType(typeCode);
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param typeCode the type code
     * @param value the actual value object
     */
    public Value(int typeCode, String value) {
        this.type = new ValueType(typeCode);
        this.value = convertValueFromString(value);
    }

    /**
     * Constructor.
     *
     * @param ty the type
     * @param value the value as a string
     */
    public Value(ValueType ty, String value) {
        this.type = ty;
        this.value = convertValueFromString(value);
    }

    /**
     *
     * @param valStr
     * @return returns true if the given value (string) is valid for this value type
     */
    public boolean isValidValue(String valStr) {
        // TODO!! 
        return true;
    }
    
    /**
     *
     * @return the type object
     */
    public ValueType getValueType() {
        return type;
    }

    /**
     * set the type
     *
     * @param valueType a type code to set
     */
    public void setValueType(int valueType) {
        this.type = new ValueType(valueType);
    }

    /**
     * get the value object
     *
     * @return value object
     */
    public Object getValueObject() {
        return value;
    }

    /**
     * set the value object
     *
     * @param value the value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Compare this value with other value
     *
     * @param other
     * @return true if the value v is equivalent to this value, false otherwise.
     */


    /**
     * Get the actual value of a value, as in, if it's an CONTINUOUS return an
     * double, if it's a CATEGORICAL return a string etc. *
     *
     * @return actual value
     */
    public Object getActualValue() {
        Object actualValue = null; 
        switch (this.type.getTypeCode()) {
            case ValueType.NULL_TYPE:
                actualValue = (String) this.value;
                break;
            case ValueType.CONTINUOUS:
                actualValue = (Double) this.value;
                //round down to 6 decimal places.
                actualValue = Math.round((Double) actualValue * 1000000.0) / 1000000.0;
                break;
            case ValueType.CATEGORICAL:
                actualValue = (String) this.value;
                break;
            case ValueType.TEXT:
                actualValue = (String) this.value;
                break;
            case ValueType.DATE:
                actualValue = (Date) this.value;
                break;
            case ValueType.BOOLEAN:
                actualValue = (Boolean) this.value;
                break;
            default:
                actualValue = this.value;
                break;
        }
        return actualValue;
    }

    /**
     * Converts a string into a value of the correct type - if possible
     *
     * @param str the string to convert
     * @return Object the object resulting from conversion, or the original
     * string as an object if not able to convert
     */
    public Object convertValueFromString(String str) {
        Object o = null;
        try {
            switch (this.type.getTypeCode()) {
                case ValueType.NULL_TYPE:
                    o = (String) str;
                    break;
                case ValueType.CONTINUOUS:
                    o = (Double) Double.parseDouble(str);
                    break;
                case ValueType.CATEGORICAL:
                    o = (String) str;
                    break;
                case ValueType.TEXT:
                    o = (String) str;
                    break;
                case ValueType.DATE:
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
                    Date d;
                    try {
                        d = df.parse(str);
                        o = d;
                    } catch (ParseException e) {
                        Logger.info("Could not parse the date element "
                                + "- set to default (2001-01-01 00:00:00.000)!");
                        o = df.parse("2001-01-01 00:00:00.000");
                    }

                    break;
                case ValueType.BOOLEAN:
                    str=str.toUpperCase();
                    if(str.equals("FALSE") || str.equals("0")){
                        o = false;
                    } else {
                        o = true;
                    }
                    break;
                default:
                    o = str;
                    break;
            }
        } catch (NumberFormatException | ParseException e) {
            Logger.info("Could not parse value "
                    + "[" + str + "] into correct type.");
            Logger.info(e.getMessage());
        }
        return o;
    }



    
        @Override
    public boolean equals(Object other) {
        if (!(other instanceof Value)) {
            return false;
        } else {
            Value v = (Value) other;
            if (v.getValueType().getTypeCode() != this.getValueType().getTypeCode()) {
                return false;	//wrong types, so not equal.
            }
            if (!v.getActualValue().toString().equals(this.getActualValue().toString())) {
                return false;	//they're not the same.
            }
        }
        return true;	//found no problems, so equal.
    }

    /**
     * hashCode method. Allows this object to be used in HashMaps etc.
     *
     * Constraints: If o1.equals(o2) then o1 should have same hashCode as o2.
     * therefore: hashCode should be calculated based on same parameters as are
     * used to determine equality.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return (41 * (41 + getActualValue().toString().hashCode() + getValueType().getTypeCode()));
    }
    
        /**
     * Convert the AttributeValue to readable text.
     *
     * @return
     */
    @Override
    public String toString() {
        if (this.getValueType() == new ValueType(ValueType.DATE)) {
            Date d = (Date) getActualValue();
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.S");
            return df.format(d);
        } else if (this.getValueType() == new ValueType(ValueType.BOOLEAN)) {
            this.getValueObject();
        }
        return getActualValue().toString();
    }
}

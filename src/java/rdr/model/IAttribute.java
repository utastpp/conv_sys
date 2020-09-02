package rdr.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public interface IAttribute {

    /**
     *
     * @return
     */
    public int getAttributeId();

    /**
     *
     * @param attributeId
     */
    public void setAttributeId(int attributeId);

    /**
     *
     * @return
     */
    public String getAttributeType();

    /**
     *
     * @param attributeType
     */
    public void setAttributeType(String attributeType);

    /**
     *
     * @return
     */
    public Value getValue(); 

    /**
     *
     * @param value
     */
    public void setValue(Value value);

    /**
     *
     * @return
     */
    public String getName();

    /**
     *
     * @param name
     */
    public void setName(String name);

    /**
     *
     * @return
     */
    public ValueType getValueType();

    /**
     *
     * @param type
     */
    public void setValueType(ValueType type);

    /**
     *
     * @return
     */
    public String[] getPotentialOperators(); 

    /**
     *
     * @return
     */
    public boolean getIsBasic();

    /**
     *
     * @param isBasic
     */
    public void setIsBasic(boolean isBasic);

    /**
     *
     * @param typeName
     * @return
     */
    public boolean isThisType(String typeName);

    /**
     *
     * @param typeCode
     * @return
     */
    public boolean isThisType(int typeCode);

    /**
     *
     * @param value
     * @return
     */
    public boolean isAcceptableValue(String value);

    /**
     *
     * @return
     */
    public ArrayList<String> getAttributeList();

    /**
     *
     * @param attributeList
     */
    public void setAttributeList(ArrayList<String> attributeList);

    /**
     *
     * @param attributeValues
     * @return
     */
    public Value getDerivedValue(HashMap<String, Value> attributeValues);

    /**
     *
     * @param values
     */
    public void setCategoricalValues(ArrayList<String> values);

    /**
     *
     * @param value
     * @return
     */
    public boolean addCategoricalValue(String value);

    /**
     *
     * @return
     */
    public ArrayList<String> getCategoricalValues();

    /**
     *
     * @return
     */
    @Override
    public String toString();
}

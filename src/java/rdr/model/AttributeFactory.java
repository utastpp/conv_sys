package rdr.model;

/**
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class AttributeFactory {

    /**
     *
     */
    protected static String classPath = "rdr.model.";

    
    /**
     * Create attribute 
     * @param attrTypeName i.e.Boolean, Categorical, Continuous, Date, Text
     * @return 
     */
    public static IAttribute createAttribute(String attrTypeName) {
        attrTypeName = attrTypeName.substring(0, 1).toUpperCase() + attrTypeName.substring(1).toLowerCase();
        IAttribute attribute = null;
        try {
            attribute = (IAttribute) Class.forName(classPath + attrTypeName +"Attribute").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
        }
        return attribute;
    }
    
    /**
     * Create attribute with value
     * @param attrTypeName i.e.Boolean, Categorical, Continuous, Date, Text
     * @param valueStr
     * @return 
     */
    public static IAttribute createAttribute(String attrTypeName, String valueStr) {
        attrTypeName = attrTypeName.substring(0, 1).toUpperCase() + attrTypeName.substring(1).toLowerCase();
        IAttribute attribute = null;
        try {
            attribute = (IAttribute) Class.forName(classPath + attrTypeName +"Attribute").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
        }
        Value value = (Value) attribute.getValue().convertValueFromString(valueStr);
        attribute.setValue(value);
        return attribute;
    }
    
    /**
     *
     * @param valueTypeCode
     * @return
     */
    public static IAttribute createAttribute(int valueTypeCode) {
        ValueType valueType = new ValueType(valueTypeCode);
        String name = valueType.getTypeName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        IAttribute attribute = null;
        try {
            attribute = (IAttribute) Class.forName(classPath + name +"Attribute").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
        }
        return attribute;
    }
    
}

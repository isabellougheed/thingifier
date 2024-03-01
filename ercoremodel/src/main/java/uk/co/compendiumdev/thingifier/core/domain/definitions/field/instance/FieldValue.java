package uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance;

import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;

public final class FieldValue {

    //TODO: Field Value should have the definition and getValue would return default if not set
    // this would allow field values to compare against each other and simplify other code
    private final String fieldName; // should this be name or should it be a Field reference?
    private final String valueOfField;
    private final Field forField; // the related field
    private InstanceFields objectValue;
    // todo: list of strings for an array
    // todo: list of InstanceFields for an array of objects

    @Deprecated
    private FieldValue(String fieldName, String fieldValue) {
        this.forField = null;
        this.fieldName = fieldName;
        this.valueOfField = fieldValue;
        this.objectValue = null;
    }

    public FieldValue(Field forField, String fieldValue) {
        this.forField = forField;
        this.fieldName = forField.getName();
        this.valueOfField = fieldValue;
        this.objectValue = null;
    }

    @Override
    public String toString() {
        String string =  "FieldValue{" +
                "fieldName='" + fieldName + "'" +
                ", fieldValue='" + valueOfField + "'";
        if(objectValue!=null){
            string = string + ",{ " + objectValue.toString() + " }";
        }
        string = string + "}";

        return string;
    }

    // TODO: currently moving to field for all field values
    public static FieldValue is(Field forField, String fieldValue) {
        return new FieldValue(forField, fieldValue);
    }

    public static FieldValue is(Field forField, InstanceFields objectValue) {
        final FieldValue value = new FieldValue(forField, "");
        value.setValue(objectValue);
        return value;
    }

    private FieldValue setValue(final InstanceFields objectValue) {
        this.objectValue = objectValue;
        return this;
    }

    public String getName() {
        return fieldName;
    }


    public FieldValue cloned() {
        if(objectValue!=null){
            return FieldValue.is(forField, objectValue.cloned());
        }else{
            return FieldValue.is(forField, valueOfField);
        }
    }



    public String asString() {
        return valueOfField;
    }

    public InstanceFields asObject() {
        return objectValue;
    }

    public float asFloat() {
        return Float.valueOf(valueOfField);
    }

    public boolean asBoolean() {
        if (valueOfField.toLowerCase().contentEquals("true")){
            return true;
        }
        if(valueOfField.toLowerCase().contentEquals("false")) {
            return false;
        }

        throw new IllegalArgumentException(valueOfField + " is not boolean");
    }

    public int asInteger() {
        return Integer.valueOf(valueOfField);
    }

    public String asJsonValue() {
        switch(forField.getType()) {
            case BOOLEAN:
            case FLOAT:
            case AUTO_INCREMENT:
            case INTEGER:
                return valueOfField;
            case AUTO_GUID:
            case DATE:
            case ENUM:
            case STRING:
                return quoted(valueOfField);
            default:
                return quoted(valueOfField);
        }
    }

    private String quoted(String aString){
        return "\"" + aString.replaceAll("\"", "\\\"") + "\"";
    }
}

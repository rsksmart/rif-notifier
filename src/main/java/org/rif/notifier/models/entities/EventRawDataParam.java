package org.rif.notifier.models.entities;

public class EventRawDataParam{
    private String value;
    private String typeAsString;
    public EventRawDataParam(){}

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTypeAsString() {
        return typeAsString;
    }

    public void setTypeAsString(String typeAsString) {
        this.typeAsString = typeAsString;
    }

    @Override
    public String toString() {
        return "{" +
                "\"value\":\"" + value + "\"" +
                ",\"typeAsString\":\"" + typeAsString + "\"" +
                "}";
    }
}

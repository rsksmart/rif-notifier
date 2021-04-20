package org.rif.notifier.models.entities;

import org.rif.notifier.util.JsonUtil;

import java.util.HashMap;

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
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("value", value);
        map.put("typeAsString", typeAsString);
        return JsonUtil.writeValueAsString(map);
    }
}

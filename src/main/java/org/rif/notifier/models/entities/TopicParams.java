package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.util.JsonUtil;

import javax.persistence.*;
import java.util.HashMap;

@Entity
@Table(name = "topic_params")
@ApiModel(description="Defines topic parameters for a topic. This is only required for CONTRACT_EVENT")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicParams {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(optional = false, fetch=FetchType.EAGER)
    @JoinColumn(name="id_topic")
    private Topic topic;

    @ApiModelProperty(notes="Topic type to listen to.", example="CONTRACT_ADDRESS", allowableValues="EVENT_PARAM,EVENT_NAME,CONTRACT_ADDRESS")
    @Enumerated(EnumType.STRING)
    @Column(name = "param_type")
    private TopicParamTypes type;

    private String value;

    @Column(name = "param_order")
    private int order;

    @Column(name = "value_type")
    private String valueType;

    @Column(name = "is_indexed")
    private boolean indexed;

    private String filter;

    public TopicParams(){}

    public TopicParams(Topic topic, TopicParamTypes type, String value, int order, String valueType, boolean indexed, String filter){
        this.topic = topic;
        this.type = type;
        this.value = value;
        this.order = order;
        this.valueType = valueType;
        this.indexed = indexed;
        this.filter = filter;
    }

    public TopicParams(TopicParams params)  {
       this(params.getTopic(), params.getType(), params.getValue(), params.getOrder(),
               params.getValueType(), params.getIndexed(), params.getFilter());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TopicParamTypes getType() {
        return type;
    }

    public void setType(TopicParamTypes type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public boolean getIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type.toString())
                .append(value)
                .append(order)
                .append(valueType)
                .append(indexed)
                .append(filter)
                .toHashCode();
    }

    protected HashMap<String, Object> fieldMap()  {
        HashMap<String, Object> map = new HashMap<>(6);
        map.put("type", type);
        map.put("value", value);
        map.put("order", order);
        map.put("valueType", valueType);
        map.put("indexed", indexed);
        map.put("filter", filter);
        return map;
    }

    @Override
    public String toString() {
        HashMap<String, Object> map = fieldMap();
        map.put("id", id);
        map.put("topic", topic);
        return JsonUtil.writeValueAsString(map);
    }

    public String toStringInfo() {
       return JsonUtil.writeValueAsString(fieldMap());
    }
}

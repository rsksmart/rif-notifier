package org.rif.notifier.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.util.JsonUtil;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class Topic {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private TopicTypes type;

    private String hash;

    @JsonIgnore
    @ManyToMany()
    @JoinTable(name = "user_topic",
            joinColumns = @JoinColumn(name = "id_topic", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_subscription", referencedColumnName = "id"))
    private Set<Subscription> subscriptions;

    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "topic")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<TopicParams> topicParams;

    public Topic(){}

    public Topic(TopicTypes type, List<TopicParams> topicParams) {
        this.type = type;
        this.topicParams = topicParams == null ? new ArrayList<>() : topicParams;
    }

    public Topic(TopicTypes type, String hash, Subscription sub){
        this.type = type;
        this.hash = hash;
        this.subscriptions = Stream.of(sub).collect(Collectors.toSet());
        this.subscriptions.forEach(x -> x.getTopics().add(this));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TopicTypes getType() {
        return type;
    }

    public void setType(TopicTypes type) {
        this.type = type;
    }

    public List<TopicParams> getTopicParams() {
        return topicParams;
    }

    public void setTopicParams(List<TopicParams> topicParams) {
        this.topicParams = topicParams;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void addSubscription(Subscription sub){
        if(this.subscriptions == null)
            this.subscriptions = new HashSet<>();
        this.subscriptions.add(sub);
        sub.getTopics().add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return type == topic.type && Objects.equals(topicParams, topic.topicParams);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type.toString())
                .append(topicParams != null ? topicParams.stream().collect(Collectors.toList()) : null)
                .toHashCode();
    }

    @Override
    public String toString() {
        HashMap<String, Object> map = new HashMap<>(5);
        map.put("id", id);
        map.put("type", type);
        map.put("hash", hash);
        map.put("subscriptions", subscriptions);
        map.put("topicParams", topicParams);
        return JsonUtil.writeValueAsString(map);
    }

    public String toStringInfo() {
        return JsonUtil.writeValueAsString(fieldMap()) ;
    }

    protected Map fieldMap() {
        List<Map> params = new ArrayList<>();
        for(TopicParams param : topicParams){
            params.add(param.fieldMap());
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("type", type);
        map.put("params", new ImmutablePair<String, Object>("topicParams", params));
        return map;
    }
}

package org.rif.notifier.models.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.TopicParams;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stroes information about topic, topic params and the notification preferences for
 * the given topic as part of subscriptionbatchcontroller
 */
@ApiModel(description="Defines the topic to listen to and the list of notification preferences for the topic")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicDTO {
    public TopicDTO()   {
        this(true);
    }
    public TopicDTO(boolean detailedPreferences)   {
        this.detailedPreferences = detailedPreferences;
    }

    @JsonIgnore
    private boolean detailedPreferences;

    @ApiModelProperty(notes="Notification Preferences for the subscription and topic.", required=true)
    @NotEmpty @Valid
    private List<NotificationPreference> notificationPreferences = new ArrayList<>();
    @ApiModelProperty(notes="The type of topic to listen to.",
                        required=true, example="NEW_BLOCK", allowableValues= "NEW_BLOCK,NEW_TRANSACTIONS,CONTRACT_EVENT")
    @NotNull
    private TopicTypes type;
    @ApiModelProperty(notes="Topic parameters applicable for CONTRACT_EVENT", required=false)
    private List<TopicParams> topicParams;

    @JsonIgnore
    public List<NotificationPreference> getNotificationPreferencesList()  {
       return notificationPreferences;
    }

    public Object getNotificationPreferences()  {
        return detailedPreferences ? notificationPreferences : notificationPreferences.stream().map(pref->pref.getNotificationService().name()).collect(Collectors.joining(","));
    }

    public void setNotificationPreferences(List<NotificationPreference> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TopicDTO topicDTO = (TopicDTO) o;

        return new EqualsBuilder()
                .append(type, topicDTO.type)
                .append(topicParams, topicDTO.topicParams)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(topicParams)
                .toHashCode();
    }
}

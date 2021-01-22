package org.rif.notifier.models.DTO;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.rif.notifier.constants.TopicTypes;
import org.rif.notifier.models.entities.NotificationPreference;
import org.rif.notifier.models.entities.TopicParams;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Stroes information about topic, topic params and the notification preferences for
 * the given topic as part of subscriptionbatchcontroller
 */
public class TopicDTO {
    @NotEmpty @Valid
    private List<NotificationPreference> notificationPreferences;
    @NotNull
    private TopicTypes type;
    private List<TopicParams> topicParams;

    public List<NotificationPreference> getNotificationPreferences() {
        return notificationPreferences;
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

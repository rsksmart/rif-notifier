package org.rif.notifier.models.DTO;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SubscriptionResponse {
    public static final SubscriptionResponse INVALID = new SubscriptionResponse(-1);

    private Integer topicId;

    public SubscriptionResponse(Integer topicId) {
        this.topicId = topicId;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionResponse that = (SubscriptionResponse) o;

        return new EqualsBuilder()
                .append(topicId, that.topicId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(topicId)
                .toHashCode();
    }
}

package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.constants.TopicParamTypes;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.entities.TopicParams;
import org.rif.notifier.repositories.TopicParamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicParamsManager {
    @Autowired
    private TopicParamsRepository topicParamsRepository;

    public TopicParams insert(Topic topic, TopicParamTypes type, String value, int order, String valueType, boolean indexed, String filter){
        TopicParams tp = new TopicParams(topic, type, value, order, valueType, indexed, filter);
        return topicParamsRepository.save(tp);
    }
}

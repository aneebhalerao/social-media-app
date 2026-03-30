package com.meet5.interactionservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class ActionEventPublisher {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private  static final String TOPIC = "user.action";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public ActionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishVisit(UUID visitorID) {
        publish(visitorID,"visit");
    }

    public void publishLike(UUID likerId) {
        publish(likerId,"like");
    }

    public void publish(UUID userId, String actionType) {
        Map<String, Object> event = Map.of("userId", userId, "actionType", actionType);
        kafkaTemplate.send(TOPIC, userId.toString(), event);
        LOGGER.debug("Published user.action: userId={} type={}", userId, actionType);
    }
}

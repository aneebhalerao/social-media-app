package com.meet5.fraudservice.kafka;

import com.meet5.fraudservice.dto.ActionEvent;
import com.meet5.fraudservice.dto.FraudMarkedEvent;
import com.meet5.fraudservice.service.FraudDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for user action events published by interaction-service.
 * Each visit or like triggers a fraud evaluation.
 *
 * Why Kafka here instead of direct HTTP call?
 * interaction-service does not call fraud-service synchronously.
 * It publishes an event and continues — fraud evaluation happens
 * asynchronously. This means:
 *   - No latency added to the visit/like API response
 *   - fraud-service can be down without affecting interactions
 *   - Events are replayed automatically if fraud-service restarts
 */
@Component
public class ActionEventConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final FraudDetectionService fraudDetectionService;

    public ActionEventConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @KafkaListener(
            topics = "user.action",
            groupId = "fraud-service-group")
    public void onUserAction(ActionEvent actionEvent) {
        try {
            LOGGER.debug("Received action event: userId={} type={}", actionEvent.userId(), actionEvent.actionType());
            fraudDetectionService.evaluateUser(actionEvent.userId(), actionEvent.actionType());
        } catch (Exception ex) {
            LOGGER.error("Failed to process action event for userId={}: {}", actionEvent.userId(), ex.getMessage(), ex);
        }
    }

}

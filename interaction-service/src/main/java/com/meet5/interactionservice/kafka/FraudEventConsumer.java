package com.meet5.interactionservice.kafka;


import com.meet5.interactionservice.repository.BlockUserRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class FraudEventConsumer {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final BlockUserRespository blockUserRespository;

    public FraudEventConsumer(BlockUserRespository blockUserRespository) {
        this.blockUserRespository = blockUserRespository;
    }

    @KafkaListener(
            topics = "fraud.user.marked",
            groupId = "interaction-service-group"
    )
    public void onFraudUSerMarked(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString((String) event.get("userId"));
            blockUserRespository.blockUser(userId);
            LOGGER.warn("User {} marked as fraud — blocked in interaction-service", userId);
        } catch (Exception e) {
            LOGGER.error("Failed to process mark user fraud event: {}", e.getMessage(), e);
        }
    }
}

package com.meet5.userservice.kafka;

import com.meet5.userservice.domain.UserStatus;
import com.meet5.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class FraudEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudEventConsumer.class);

    private final UserRepository userRepository;

    public FraudEventConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(
            topics = "fraud.user.marked",
            groupId = "user-service-group"
    )
    public void onFraudUserMarked(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString(event.get("userId").toString());
            userRepository.updateStatus(userId, UserStatus.FRAUD);

            LOGGER.info("User status updated to FRAUD: userId={}", userId);
        } catch (Exception e) {
            LOGGER.error("Failed to process fraud event in user-service: {}", e.getMessage(), e);
        }
    }
}

package com.meet5.fraudservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic fraudUSerMarkedTopic() {
        return TopicBuilder.name("fraud.user.marked")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userActionTopic() {
        return TopicBuilder.name("user.action")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

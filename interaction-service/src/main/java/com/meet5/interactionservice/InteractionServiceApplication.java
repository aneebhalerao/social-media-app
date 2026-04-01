package com.meet5.interactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class InteractionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InteractionServiceApplication.class, args);
	}

}

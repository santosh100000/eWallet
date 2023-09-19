package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class KafkaNotifyConsumerConfig {
    private static Logger LOGGER= LoggerFactory.getLogger(KafkaNotifyConsumerConfig.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "USERS-CREATED", groupId = "email")
    public void consumeFromUserCreatedTopic(ConsumerRecord payload) throws JsonProcessingException {
        UserCreatedPayload userCreatedPayload = objectMapper.readValue(payload.value().toString(), UserCreatedPayload.class);
        MDC.put("requestId",  userCreatedPayload.getRequestId());
        LOGGER.info("Getting payload from kafka: {} ", payload);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("santoshkharel9684@gmail.com");
        simpleMailMessage.setSubject("Welcome "+userCreatedPayload.getUserName() + "!");
        simpleMailMessage.setTo(userCreatedPayload.getUserEmail());
        simpleMailMessage.setText("Hi "+userCreatedPayload.getUserName()+"Welcome to our E-Wallet");
        simpleMailMessage.setCc("admin54@gmail.com");
        javaMailSender.send(simpleMailMessage);

    }
}

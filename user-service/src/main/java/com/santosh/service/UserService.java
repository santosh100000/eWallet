package com.santosh.service;

import com.example.UserCreatedPayload;
import com.santosh.dto.UserDto;
import com.santosh.entity.User;
import com.santosh.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static String USER_CREATED_TOPIC = "USERS-CREATED";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;

    public Long createUser(UserDto userDto)  {
        User user= User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .address(userDto.getAddress())
                .phone(userDto.getPhone())
                .kycId(userDto.getKycId()).build();
        userRepository.save(user);
        UserCreatedPayload userCreatedPayload = new UserCreatedPayload
                (user.getId(), user.getName(), user.getEmail(), MDC.get("requestId"));
       CompletableFuture<SendResult<String, Object>> future =
    kafkaTemplate.send(USER_CREATED_TOPIC, String.valueOf(user.getId()), userCreatedPayload);

        try {
            LOGGER.info("pushed userCreatedPayload to kafka: {}", future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return user.getId();

    }
}

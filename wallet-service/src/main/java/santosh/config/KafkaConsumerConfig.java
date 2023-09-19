package santosh.config;

import com.example.UserCreatedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import santosh.entity.Wallet;
import santosh.repo.WalletRepository;

@Configuration
public class KafkaConsumerConfig {

    @Autowired
    private WalletRepository walletRepository;

    private static Logger LOGGER= LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "USERS-CREATED", groupId = "walletapp")
    public void consumeFromUserCreatedTopic(ConsumerRecord payload) throws JsonProcessingException {
        UserCreatedPayload userCreatedPayload = objectMapper.readValue(payload.value().toString(), UserCreatedPayload.class);
        MDC.put("requestId",  userCreatedPayload.getRequestId());
        LOGGER.info("Getting payload from kafka: {} ", payload);
        Wallet wallet = Wallet.builder()
                .userId(userCreatedPayload.getUserId())
                .balance(100.00)
                .build();

        walletRepository.save(wallet);
        MDC.clear();
    }
}

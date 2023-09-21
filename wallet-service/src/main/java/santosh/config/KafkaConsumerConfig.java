package santosh.config;

import com.example.TransactionInitPayload;
import com.example.TxnCompletedPayload;
import com.example.UserCreatedPayload;
import com.example.WalletUpdatedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import santosh.entity.Wallet;
import santosh.exception.InsufficientBalance;
import santosh.repo.WalletRepository;
import santosh.service.WalletService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Configuration
public class KafkaConsumerConfig {
    private static String TXN_COMPLETED_TOPIC = "TXN-COMPLETED";



    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

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
                .email(userCreatedPayload.getUserEmail())
                .name(userCreatedPayload.getUserName())
                .build();

        walletRepository.save(wallet);
        MDC.clear();
    }

    @KafkaListener(topics = "TXN-INIT", groupId = "txnapp")
    public void consumeFromTransactionCreatedTopic(ConsumerRecord payload) throws JsonProcessingException, ExecutionException, InterruptedException {
        TransactionInitPayload transactionInitPayload = objectMapper.readValue(payload.value().toString(), TransactionInitPayload.class);
        MDC.put("requestId",  transactionInitPayload.getRequestId());
        LOGGER.info("Getting payload from kafka: {} ", payload);
        TxnCompletedPayload txnCompletedPayload = new TxnCompletedPayload();
        txnCompletedPayload.setRequestId(transactionInitPayload.getRequestId());
        txnCompletedPayload.setId(transactionInitPayload.getId());
        try {
            walletService.doWalletTxn(transactionInitPayload);
            txnCompletedPayload.setSuccess(Boolean.TRUE);
            txnCompletedPayload.setReason("Transaction completed!!");


        } catch (InsufficientBalance e) {
            e.printStackTrace();
            txnCompletedPayload.setSuccess(Boolean.FALSE);
            txnCompletedPayload.setReason("Transaction couldn't be completed because of insufficient balance");

                    }catch (Exception e){
            txnCompletedPayload.setSuccess(Boolean.FALSE);
            txnCompletedPayload.setReason("Server Error please try again later");
        }
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TXN_COMPLETED_TOPIC , String.valueOf(transactionInitPayload.getFromUserId()), txnCompletedPayload);
        LOGGER.info("Pushed txnCompletedPayload from kafka: {} ", future.get());
        MDC.clear();
    }
}

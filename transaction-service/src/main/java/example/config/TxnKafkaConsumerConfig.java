package example.config;

import com.example.TxnCompletedPayload;
import com.example.UserCreatedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.entity.TransactionEntity;
import example.entity.TransactionStatusEnum;
import example.repo.TransactionRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class TxnKafkaConsumerConfig {

    @Autowired
    private TransactionRepository transactionRepository;

    private static Logger LOGGER = LoggerFactory.getLogger(TxnKafkaConsumerConfig.class);

    private static  ObjectMapper objectMapper = new ObjectMapper();
    @KafkaListener(topics = "TXN-COMPLETED", groupId = "txnapp")
    public void consumeFromTxnCompeletedTopic(ConsumerRecord payload) throws JsonProcessingException {
        TxnCompletedPayload txnCompletedPayload = objectMapper.readValue(payload.value().toString(), TxnCompletedPayload.class);
        MDC.put("requestId",  txnCompletedPayload.getRequestId());
        LOGGER.info("Getting payload from kafka: {} ", payload);
        TransactionEntity transaction = transactionRepository.findById(txnCompletedPayload.getId()).get();
       if(txnCompletedPayload.getSuccess()){
           transaction.setStatus(TransactionStatusEnum.SUCCESS);
       }else {
           transaction.setStatus(TransactionStatusEnum.FAILED);
       }
       transaction.setReason(txnCompletedPayload.getReason());
       transactionRepository.save(transaction);
        MDC.clear();
    }

}

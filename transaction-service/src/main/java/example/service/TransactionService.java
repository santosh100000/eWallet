package example.service;

import com.example.TransactionInitPayload;
import example.dto.TransactionRequest;
import example.dto.TxnStatusDTO;
import example.entity.TransactionEntity;
import example.entity.TransactionStatusEnum;
import example.repo.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TransactionService {

    private static String TXN_INIT_TOPIC = "TXN-INIT";
    private static Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public String doTransaction(TransactionRequest transactionRequest) throws ExecutionException, InterruptedException {
        TransactionEntity transaction = TransactionEntity.builder()
                .fromUserId(transactionRequest.getFromUserId())
                .toUserId(transactionRequest.getToUserId())
                .amount(transactionRequest.getAmount())
                .remark(transactionRequest.getRemark())
                .status(TransactionStatusEnum.PENDING)
                .txnId(UUID.randomUUID().toString()). build();
        transactionRepository.save(transaction);
        TransactionInitPayload tip = TransactionInitPayload.builder()
                .fromUserId(transactionRequest.getFromUserId())
                .toUserId(transactionRequest.getToUserId())
                .amount(transactionRequest.getAmount())
                .remark(transactionRequest.getRemark())
                .requestId(MDC.get("requestId"))
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TXN_INIT_TOPIC, String.valueOf(transaction.getFromUserId()), tip);


            LOGGER.info("pushed userCreatedPayload to kafka: {}", future.get());

            return transaction.getTxnId();
    }

    public TxnStatusDTO getStatus(String txnId){
        TransactionEntity transaction = transactionRepository.findByTxnId(txnId);
        TxnStatusDTO txnStatusDTO = TxnStatusDTO.builder()
                        .reason(transaction.getReason())
                       .status(transaction.getStatus().name())
                       .build();
        return txnStatusDTO;
    }
}

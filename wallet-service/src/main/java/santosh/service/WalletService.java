package santosh.service;

import com.example.TransactionInitPayload;
import com.example.WalletUpdatedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import santosh.entity.Wallet;
import santosh.exception.InsufficientBalance;
import santosh.repo.WalletRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class WalletService {

    private static Logger LOGGER= LoggerFactory.getLogger(WalletService.class);

    private static String WALLET_UPDATED_TOPIC = "WALLET-UPDATED";

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;



    @Transactional
    public void doWalletTxn(TransactionInitPayload transactionInitPayload) throws InsufficientBalance, ExecutionException, InterruptedException {
        Wallet fromWallet = walletRepository.findByUserId(transactionInitPayload.getFromUserId());
        if(fromWallet.getBalance() >= transactionInitPayload.getAmount()){
            Wallet toWallet = walletRepository.findByUserId(transactionInitPayload.getToUserId());
            fromWallet.setBalance(fromWallet.getBalance() - transactionInitPayload.getAmount());
            toWallet.setBalance(toWallet.getBalance() + transactionInitPayload.getAmount());

            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);

            WalletUpdatedPayload wup = WalletUpdatedPayload.builder()
                    .userName(fromWallet.getName())
                    .userEmail(fromWallet.getEmail())
                            .balance(fromWallet.getBalance())
                                    .requestId(MDC.get("requestId"))
                    .build();
            CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send("UPDATE-WALLET" , String.valueOf(transactionInitPayload.getFromUserId()), wup);
            LOGGER.info("Pushed WalletUpdatedPayload from kafka: {} ", future.get());

            WalletUpdatedPayload wup2 = WalletUpdatedPayload.builder()
                    .userName(toWallet.getName())
                    .userEmail(toWallet.getEmail())
                    .balance(toWallet.getBalance())
                    .requestId(MDC.get("requestId"))
                    .build();

            CompletableFuture<SendResult<String, Object>> future2 =
                    kafkaTemplate.send( "UPDATE-WALLET", String.valueOf(transactionInitPayload.getToUserId()), wup2);
            LOGGER.info("Pushed WalletUpdatedPayload from kafka: {} ", future2.get());

        }else{
           throw  new InsufficientBalance("not sufficient Balance");
        }

    }
}

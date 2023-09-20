package santosh.service;

import com.example.TransactionInitPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import santosh.entity.Wallet;
import santosh.exception.InsufficientBalance;
import santosh.repo.WalletRepository;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;



    @Transactional
    public void doWalletTxn(TransactionInitPayload transactionInitPayload) throws InsufficientBalance {
        Wallet fromWallet = walletRepository.findByUserId(transactionInitPayload.getFromUserId());
        if(fromWallet.getBalance() >= transactionInitPayload.getAmount()){
            Wallet toWallet = walletRepository.findByUserId(transactionInitPayload.getToUserId());
            fromWallet.setBalance(fromWallet.getBalance() - transactionInitPayload.getAmount());
            toWallet.setBalance(toWallet.getBalance() + transactionInitPayload.getAmount());
            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);

        }else{
           throw  new InsufficientBalance("not sufficient Balance");
        }

    }
}

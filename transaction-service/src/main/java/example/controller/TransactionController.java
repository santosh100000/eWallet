package example.controller;

import example.dto.TransactionRequest;
import example.dto.TxnStatusDTO;
import example.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/transaction-service")
public class TransactionController {

    private static Logger LOGGER= LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/doTransaction")
    ResponseEntity<String> doTransaction(@RequestBody TransactionRequest transactionRequest) throws ExecutionException, InterruptedException {
        LOGGER.info("initiating txn {}", transactionRequest);
       String txnId = transactionService.doTransaction(transactionRequest);

        return ResponseEntity.ok(txnId);
    }

    @GetMapping("/status/{txnId}")
    ResponseEntity<TxnStatusDTO> getStatus(@PathVariable String txnId){
        LOGGER.info("Fetching txn status for txnId {}", txnId);
        return ResponseEntity.ok(transactionService.getStatus(txnId));
    }
}

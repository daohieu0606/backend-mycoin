package com.example.mycoin.controller;

import com.example.mycoin.dto.TransactionResponse;
import com.example.mycoin.dto.WalletResponse;
import com.example.mycoin.entity.StringUtil;
import com.example.mycoin.entity.Transaction;
import com.example.mycoin.entity.Wallet;
import com.example.mycoin.miner.MinerManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {
    private final ModelMapper modelMapper;
    private final MinerManager minerManager;

    @PostMapping("/wallets")
    public ResponseEntity<?> createWallet() {
        var wallet = new Wallet();
        minerManager.getWallets().add(wallet);

        WalletResponse response = new WalletResponse();
        response.setPrivateKey(wallet.getPrivateKeyStr());
        response.setPublicKey(wallet.getPublicKeyStr());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallets")
    public ResponseEntity<?> getAllWallets() {
        var wallets = minerManager.getWallets();

        if(wallets == null || wallets.size() == 0)
            return ResponseEntity.noContent().build();

        var walletResponses = wallets.stream().map(wallet -> {
            WalletResponse walletResponse = new WalletResponse();

            walletResponse.setPrivateKey(wallet.getPrivateKeyStr());
            walletResponse.setPublicKey(wallet.getPublicKeyStr());
            walletResponse.setBalance(wallet.getBalance());

            return walletResponse;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(walletResponses);
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getWallet(@RequestParam() String privateKey) {
        //TODO: because postman has error
        privateKey = privateKey.replace(' ', '+');

        var wallet = minerManager.getWalletFromPrivateKey(privateKey);

        if(wallet == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not found");

        var result = modelMapper.map(wallet, WalletResponse.class);
        result.setBalance(wallet.getBalance());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/wallets/send-coin")
    public ResponseEntity<?> sendCoin(@RequestParam String senderPrivateKey,
                                      @RequestParam String receiverPublicKey,
                                      @RequestParam float coin) {
        senderPrivateKey = senderPrivateKey.replace(' ', '+');
        receiverPublicKey = receiverPublicKey.replace(' ', '+');
        Wallet sender = minerManager.getWalletFromPrivateKey(senderPrivateKey);
        Wallet receiver = minerManager.getWalletFromPublicKey(receiverPublicKey);


        if(sender == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("sender address is not valid");
        }

        if(sender.getPublicKeyStr().equals(receiverPublicKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("receiver address must difficult to sender address");
        }

        if(receiver == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("receiver address is not valid");
        }

        var transaction = sender.createAndSignTransaction(receiver.getPublicKey(), coin);
        if(transaction == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("can not create a transaction. maybe you have not enough funds to send");
        }

        //TODO:
        var transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        var newBlock = minerManager.createBlock(transactions);

        if(newBlock == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("can not create a block. please contact admin");
        }

        minerManager.getBlockchain().add(newBlock);

        for(var item: transactions){
            minerManager.getTransactions().add(item);
        }

        //TODO: duplicate code
        var model = new TransactionResponse();

        model.setSender(StringUtil.getStringFromKey(transaction.sender));
        model.setReceiver(StringUtil.getStringFromKey(transaction.recipient));
        model.setValue(transaction.value);
        model.setId(transaction.transactionId);
        model.setTimeStamp(transaction.timeStamp);

        return ResponseEntity.ok(model);
    }

    @GetMapping("/wallets/history")
    public ResponseEntity<?> getHistoryTransactions(@RequestParam String privateKey) {
        //TODO: because postman has error
        privateKey = privateKey.replace(' ', '+');

        var wallet = minerManager.getWalletFromPrivateKey(privateKey);

        if(wallet == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("private key is invalid");

        List<Transaction> transactions =  minerManager.getHistoryTransactions(wallet.getPublicKey());

        if(transactions == null || transactions.size() == 0)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("not found any transaction");

        var transactionModels = transactions.stream()
                        .map(item -> {
                            var model = new TransactionResponse();

                            model.setSender(StringUtil.getStringFromKey(item.sender));
                            model.setReceiver(StringUtil.getStringFromKey(item.recipient));
                            model.setValue(item.value);
                            model.setId(item.transactionId);
                            model.setTimeStamp(item.timeStamp);

                            return model;
                        })
                        .collect(Collectors.toList());

        return ResponseEntity.ok(transactionModels);
    }
}

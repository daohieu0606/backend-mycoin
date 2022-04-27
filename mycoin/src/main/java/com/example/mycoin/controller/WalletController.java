package com.example.mycoin.controller;

import com.example.mycoin.dto.WalletResponse;
import com.example.mycoin.entity.Transaction;
import com.example.mycoin.entity.Wallet;
import com.example.mycoin.miner.MinerManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {
    private final ModelMapper modelMapper;
    private MinerManager minerManager = MinerManager.getInstance();

    @PostMapping("/wallets")
    public ResponseEntity<?> createWallet() {
        var wallet = new Wallet();
        minerManager.getWallets().add(wallet);

        WalletResponse response = new WalletResponse();
        response.setPrivateKey(wallet.getPrivateKeyStr());
        response.setPublicKey(wallet.getPublicKeyStr());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wallets/send-coin")
    public ResponseEntity<?> sendCoin(@RequestParam String senderPrivateKey,
                                      @RequestParam String receiverPublicKey,
                                      @RequestParam float coin) {
//        senderPrivateKey = senderPrivateKey.replace(' ', '+');
//        receiverPublicKey = receiverPublicKey.replace(' ', '+');
//        Wallet sender = BlockChainState.getWalletFromPrivateKey(senderPrivateKey);
//        Wallet receiver = BlockChainState.getWalletFromPublicKey(receiverPublicKey);

        //TODO:
        Wallet sender = minerManager.getWallets().get(0);
        Wallet receiver = minerManager.getWallets().get(1);


        if(sender == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var transaction = sender.createAndSignTransaction(receiver.getPublicKey(), coin);

        //TODO:
        var transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        minerManager.createBlock(transactions);

        return ResponseEntity.ok("done");
    }
}

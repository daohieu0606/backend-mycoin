package com.example.mycoin.miner;

import com.example.mycoin.entity.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class MinerManager {
    private List<Miner> miners;
    private final Block root;
    private List<Block> blockchain;
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private HashMap<String, TransactionOutput> UTXOs;
    private float minimumTransaction;
    private Transaction genesisTransaction;

    public MinerManager() {
        miners = new ArrayList<>();
        blockchain = new ArrayList<>();
        transactions = new ArrayList<>();
        wallets = new ArrayList<>();
        UTXOs = new HashMap<>();
        minimumTransaction = 1;
        root = createGenesisBlock();
    }

    private Block createGenesisBlock() {
        if(genesisTransaction == null) {
            Wallet coinbase = new Wallet();
            //Create the new wallets
            var walletA = new Wallet();
            var walletB = new Wallet();

            if(wallets == null)
                wallets = new ArrayList<>();

            wallets.add(walletA);
            wallets.add(walletB);
            wallets.add(coinbase);

            //create genesis transaction, which sends 100 NoobCoin to walletA:
            genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
            genesisTransaction.generateSignature(coinbase.getPrivateKey());
            genesisTransaction.transactionId = "0"; //manually set the transaction id

            genesisTransaction.outputs.add(new TransactionOutput(
                    genesisTransaction.recipient,
                    genesisTransaction.value,
                    genesisTransaction.transactionId)); //manually add the Transactions Output

            if(UTXOs == null)
                UTXOs = new HashMap<>();

            UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        }

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(genesisTransaction);

        Block genesis = new Block(0, null, transactions, 6, null);
        genesis.setHash("ROOT");
        return genesis;
    }

    public Wallet getWalletFromPrivateKey(String privateKeyStr) {
        for(var wallet: wallets) {
            if(wallet.getPrivateKeyStr().equals(privateKeyStr))
                return wallet;
        }
        return null;
    }

    public Miner addMiner(int port) {
        if(existMinerAtPort(port)) {
            return null;
        }

        Miner a = new Miner("localhost", port, root, miners);
        a.startHost();
        miners.add(a);
        return a;
    }

    private boolean existMinerAtPort(int port) {
        if(miners == null || miners.size() == 0)
            return false;

        for (var miner: miners)
            if(miner.getPort() == port)
                return true;

        return false;
    }

    public Miner getMiner(String name) {
        for (Miner a : miners) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public List<Miner> getAllMiner() {
        return miners;
    }

    public void deleteMiner(String name) {
        final Miner a = getMiner(name);
        if (a != null) {
            a.stopHost();
            miners.remove(a);
        }
    }

    public List<Block> getMinerBlockchain(String name) {
        final Miner miner = getMiner(name);
        if (miner != null) {
            return miner.getBlockchain();
        }
        return null;
    }

    public void deleteAllMiners() {
        for (Miner a : miners) {
            a.stopHost();
        }
        miners.clear();
    }

    public Block createBlock(List<Transaction> transactions) {
//        final Miner miner = getMiner(name);
        //TODO: remove hardcode
        final Miner miner = miners.get(0);
        if (miner != null) {
            return miner.createAndMineBlock(transactions);
        }
        return null;
    }

    public List<Transaction> getHistoryTransactions(PublicKey publicKey) {
        if(transactions == null || transactions.size() == 0)
            return null;

        var result = new ArrayList<Transaction>();
        for (var transaction: transactions) {
            if(transaction.sender.equals(publicKey) || transaction.recipient.equals(publicKey))
                result.add(transaction);
        }

        return result;
    }

    public Wallet getWalletFromPublicKey(String publicKey) {
        for (var wallet: wallets)
            if(wallet.getPublicKeyStr().equals(publicKey))
                return wallet;

        return null;
    }
}

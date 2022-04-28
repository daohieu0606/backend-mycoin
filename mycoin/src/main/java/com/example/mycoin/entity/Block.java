package com.example.mycoin.entity;


import com.example.mycoin.miner.Miner;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
public class Block implements Serializable {
    private static final long serialVersionUID = 1L;

    private int index;
    private String hash;
    private String previousHash;
    private long timeStamp;
    private List<Transaction> transactions = new ArrayList<Transaction>();
    private int difficulty;
    private int nonce;
    private Miner miner;

    public Block(int index,
                 String previousHash,
                 List<Transaction> transactions,
                 int difficulty,
                 Miner miner) {
        this.index = index;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.miner = miner;
        if(transactions != null) {
            for(var transaction: transactions) {
                if(previousHash != null) {
                    if(!transaction.processTransaction()) {
                        System.out.println("Transaction failed to process. Discarded.");
                        continue;
                    }
                }

                this.transactions.add(transaction);
                System.out.println("Transaction Successfully added to Block");
            }
        }
        this.difficulty = difficulty;
    }

    //TODO
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                  Integer.toString(index) +
                        previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce)
        );
        return calculatedhash;
    }

    public void mineBlock() {
        nonce = 0;
        while (true) {
            final var newHash = calculateHash();
            if(hashMatchesDifficulty(newHash, difficulty)) {
                this.hash = newHash;
                return;
            }

            nonce++;
        }
    }

    private boolean hashMatchesDifficulty(String hash, int dif) {
        final String hashInBinary = hexToBinary(hash);
        final String hashPrefixGoal = StringUtils.repeat("0", dif);
        return hashInBinary.startsWith(hashPrefixGoal);
    }

    private String hexToBinary(final String hash) {
        String hex = hash;
        hex = hex.replaceAll("0", "0000");
        hex = hex.replaceAll("1", "0001");
        hex = hex.replaceAll("2", "0010");
        hex = hex.replaceAll("3", "0011");
        hex = hex.replaceAll("4", "0100");
        hex = hex.replaceAll("5", "0101");
        hex = hex.replaceAll("6", "0110");
        hex = hex.replaceAll("7", "0111");
        hex = hex.replaceAll("8", "1000");
        hex = hex.replaceAll("9", "1001");
        hex = hex.replaceAll("A", "1010");
        hex = hex.replaceAll("B", "1011");
        hex = hex.replaceAll("C", "1100");
        hex = hex.replaceAll("D", "1101");
        hex = hex.replaceAll("E", "1110");
        hex = hex.replaceAll("F", "1111");
        return hex;
    }

    public float getTransactionsValue() {
        if(transactions == null || transactions.size() == 0)
            return 0.0f;

        float value = 0.0f;
        for(var transaction: transactions){
            value += transaction.value;
        }

        return value;
    }
}
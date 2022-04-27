package com.example.mycoin.entity;

import com.example.mycoin.miner.MinerManager;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
public class Wallet implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private PrivateKey privateKey;
    @Transient
    private PublicKey publicKey;

    @Column(length = 65555, nullable = false)
    private String privateKeyStr;

    @Column(length = 65555, nullable = false)
    private String publicKeyStr;

    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair() {

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); //256
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            privateKeyStr = StringUtil.getStringFromKey(privateKey);
            publicKeyStr = StringUtil.getStringFromKey(publicKey);

        }catch(Exception e) {
            throw new RuntimeException(e);
        }


//        KeyPairGenerator gen = null;
//        try {
//            gen = KeyPairGenerator.getInstance("RSA");
//            gen.initialize(2048);
//            KeyPair keyPair = gen.generateKeyPair();
//            RSAPublicKey pkey = (RSAPublicKey) keyPair.getPublic();
//            privateKey = keyPair.getPrivate();
//            publicKey = keyPair.getPublic();
//
//            privateKeyStr = StringUtil.getStringFromKey(privateKey);
//            publicKeyStr = StringUtil.getStringFromKey(publicKey);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
    }

    public Transaction createAndSignTransaction(PublicKey _recipient, float value ) {
        if(getBalance() < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }

    public float getBalance() {
        float total = 0;

        for (Map.Entry<String, TransactionOutput> item: MinerManager.getInstance().getUTXOs().entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
                total += UTXO.value ;
            }
        }
        return total;
    }
}
package com.example.mycoin.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponse {
    private String id;
    private long timeStamp;
    private String sender;
    private String receiver;
    private float value;
}

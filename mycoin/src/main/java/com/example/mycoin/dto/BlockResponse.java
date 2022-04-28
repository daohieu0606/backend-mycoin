package com.example.mycoin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockResponse {
    private int index;
    private long timeStamp;
    private String miner;
    private int transactionCount;
    private float value;
}

package com.example.mycoin.dto;

import com.example.mycoin.entity.Block;
import com.example.mycoin.miner.Miner;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MinerResponse {
    private String name;
    private String address;
    private int port;
}

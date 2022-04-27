package com.example.mycoin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletResponse {
    private String privateKey;
    private String publicKey;
}

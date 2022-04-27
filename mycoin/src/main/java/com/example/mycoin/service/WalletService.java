package com.example.mycoin.service;

import com.example.mycoin.entity.Wallet;
import com.example.mycoin.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletService {
    private final WalletRepository walletRepository;

    public void addWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }

}

package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.domain.Recharge;
import com.playtomic.tests.wallet.dto.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    Wallet createWallet(Wallet wallet);

    Wallet getWallet(String walletId);

    void rechargeWallet(String walletId, Recharge recharge);

    void chargeWallet(String walletId, BigDecimal amount);
}

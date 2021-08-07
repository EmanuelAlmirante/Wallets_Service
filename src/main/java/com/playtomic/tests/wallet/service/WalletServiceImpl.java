package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.domain.Recharge;
import com.playtomic.tests.wallet.dto.Wallet;
import com.playtomic.tests.wallet.exception.BusinessException;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.stripe.StripeService;
import com.playtomic.tests.wallet.service.stripe.StripeServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {
    private final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);

    private final WalletRepository walletRepository;
    private final StripeService stripeService;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository, StripeService stripeService) {
        this.walletRepository = walletRepository;
        this.stripeService = stripeService;
    }

    @Override
    public Wallet createWallet(Wallet wallet) {
        log.info("Create wallet method invoked");

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWallet(String walletId) {
        log.info("Getting walled with id - " + walletId);

        Wallet wallet = getWalletById(walletId);
        verifyWalletExists(wallet, walletId);

        return wallet;
    }

    @Override
    public void rechargeWallet(String walletId, Recharge recharge) {
        Wallet wallet = getWalletById(walletId);
        verifyWalletExists(wallet, walletId);

        verifyChargeIsValid(recharge);

        BigDecimal amount = recharge.getAmount();
        wallet.addAmountToCurrentBalance(amount);

        walletRepository.save(wallet);
    }

    @Override
    public void chargeWallet(String walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);
        verifyWalletExists(wallet, walletId);

        wallet.subtractAmountToCurrentBalance(amount);

        walletRepository.save(wallet);
    }

    private Wallet getWalletById(String walletId) {
        Optional<Wallet> walletOptional = walletRepository.findById(walletId);

        return walletOptional.orElse(null);
    }

    private void verifyWalletExists(Wallet wallet, String walletId) {
        if (wallet == null) {
            log.error("Wallet with id " + walletId + " does not exist.");

            throw new BusinessException("Wallet with id " + walletId + " does not exist.", walletId);
        }
    }

    private void verifyChargeIsValid(Recharge recharge) {
        String creditCardNumber = recharge.getCreditCardNumber();
        BigDecimal amount = recharge.getAmount();

        try {
            stripeService.charge(creditCardNumber, amount);
        } catch (StripeServiceException stripeServiceException) {
            log.error("Charge is not valid as per Stripe service");

            throw new BusinessException(
                    "Stripe validation failed for credit card with number " + creditCardNumber + " and amount "
                    + amount, creditCardNumber, amount.toString());
        }
    }
}

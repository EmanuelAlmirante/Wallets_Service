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
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class WalletServiceImpl implements WalletService {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
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
        log.info("Creating wallet with an initial current balance of " + wallet.getCurrentBalance());

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWallet(String walletId) {
        log.info("Getting wallet with id - " + walletId);

        readWriteLock.readLock().lock();

        try {
            return getWalletById(walletId);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void rechargeWallet(String walletId, Recharge recharge) {
        log.info("Recharging wallet with id - " + walletId + " with an amount of " + recharge.getAmount());

        readWriteLock.writeLock().lock();

        try {
            verifyRechargeIsValid(recharge);

            Wallet wallet = getWalletById(walletId);
            verifyStripeServiceChargeIsValid(recharge);

            BigDecimal amount = recharge.getAmount();
            wallet.addAmountToCurrentBalance(amount);

            walletRepository.save(wallet);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void chargeWallet(String walletId, BigDecimal amount) {
        log.info("Charging wallet with id - " + walletId + " with an amount of " + amount);

        readWriteLock.writeLock().lock();

        try {
            if (amount == null) {
                throw new BusinessException("Charge amount cannot be null");
            }

            Wallet wallet = getWalletById(walletId);
            wallet.subtractAmountToCurrentBalance(amount);

            walletRepository.save(wallet);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private Wallet getWalletById(String walletId) {
        Optional<Wallet> walletOptional = walletRepository.findById(walletId);

        return walletOptional.orElseThrow(() -> {
            log.error("Wallet with id " + walletId + " does not exist.");

            throw new BusinessException("Wallet with id " + walletId + " does not exist.", walletId);
        });
    }

    private void verifyRechargeIsValid(Recharge recharge) {
        if (recharge.getCreditCardNumber() == null) {
            throw new BusinessException("Credit card number cannot be null");
        }

        if (recharge.getAmount() == null) {
            throw new BusinessException("Amount of recharge cannot be null");
        }
    }

    private void verifyStripeServiceChargeIsValid(Recharge recharge) {
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

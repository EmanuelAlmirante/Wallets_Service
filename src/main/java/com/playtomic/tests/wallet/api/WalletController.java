package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.domain.Recharge;
import com.playtomic.tests.wallet.dto.Wallet;
import com.playtomic.tests.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
public class WalletController {
    private final Logger log = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @RequestMapping("/")
    void log() {
        log.info("Logging from /");
    }

    @PostMapping("/wallet")
    @ResponseStatus(HttpStatus.CREATED)
    public Wallet createWallet(@Valid @RequestBody Wallet wallet) {
        log.info("Creating new wallet");

        return walletService.createWallet(wallet);
    }

    @GetMapping("/wallet/{wallet_id}")
    @ResponseStatus(HttpStatus.OK)
    public Wallet getWallet(@PathVariable("wallet_id") String walletId) {
        log.info("Getting wallet with id - " + walletId);

        return walletService.getWallet(walletId);
    }

    @PatchMapping("/wallet/{wallet_id}/recharge")
    @ResponseStatus(HttpStatus.OK)
    public void rechargeWallet(@PathVariable("wallet_id") String walletId, @Valid @RequestBody Recharge recharge) {
        log.info("Recharging wallet with id " + walletId);

        walletService.rechargeWallet(walletId, recharge);
    }

    @PatchMapping("/wallet/{wallet_id}/subtract/{amount}")
    @ResponseStatus(HttpStatus.OK)
    public void chargeWallet(@PathVariable("wallet_id") String walletId, @PathVariable BigDecimal amount) {
        log.info("Charging wallet with id " + walletId + " an amount of " + amount);

        walletService.chargeWallet(walletId, amount);
    }
}

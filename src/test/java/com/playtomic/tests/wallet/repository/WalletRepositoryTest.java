package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.dto.Wallet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WalletRepositoryTest {
    private static final BigDecimal INITIAL_CURRENT_BALANCE_OF_WALLET = new BigDecimal(1000);

    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void createWalletWithInitialCurrentBalanceSuccessfully() {
        // Arrange
        Wallet walletToBeCreated = createWalletInstance();

        // Act
        saveWalletInRepository(walletToBeCreated);

        Wallet walletCreated = getWalletFromRepository();

        // Assert
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET, walletCreated.getCurrentBalance());
    }

    @Test
    public void getWalletByIdSuccessfully() {
        // Arrange
        Wallet walletToBeCreated = createWalletInstance();
        saveWalletInRepository(walletToBeCreated);
        Wallet walletCreated = getWalletFromRepository();
        String walletId = walletCreated.getId();

        // Act
        Optional<Wallet> wallet =  walletRepository.findById(walletId);

        // Assert
        Assertions.assertEquals(walletId, wallet.get().getId());
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET, wallet.get().getCurrentBalance());
    }

    @Test
    public void rechargeWalletWithAnAmountSuccessfully() {
        // Arrange
        Wallet walletToBeCreated = createWalletInstance();
        saveWalletInRepository(walletToBeCreated);
        Wallet walletCreated = getWalletFromRepository();
        String walletId = walletCreated.getId();

        // Act
        BigDecimal rechargeAmount = new BigDecimal(1000);
        walletCreated.addAmountToCurrentBalance(rechargeAmount);
        walletRepository.save(walletCreated);

        Optional<Wallet> wallet =  walletRepository.findById(walletId);

        // Assert
        Assertions.assertEquals(walletId, wallet.get().getId());
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET.add(rechargeAmount), wallet.get().getCurrentBalance());
    }

    @Test
    public void chargeWalletWithAnAmountSuccessfully() {
        // Arrange
        Wallet walletToBeCreated = createWalletInstance();
        saveWalletInRepository(walletToBeCreated);
        Wallet walletCreated = getWalletFromRepository();
        String walletId = walletCreated.getId();

        // Act
        BigDecimal rechargeAmount = new BigDecimal(500);
        walletCreated.subtractAmountToCurrentBalance(rechargeAmount);
        walletRepository.save(walletCreated);

        Optional<Wallet> wallet =  walletRepository.findById(walletId);

        // Assert
        Assertions.assertEquals(walletId, wallet.get().getId());
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET.subtract(rechargeAmount), wallet.get().getCurrentBalance());
    }

    private Wallet createWalletInstance() {
        BigDecimal currentBalance = INITIAL_CURRENT_BALANCE_OF_WALLET;
        Wallet wallet = new Wallet();
        wallet.setCurrentBalance(currentBalance);

        return wallet;
    }

    private void saveWalletInRepository(Wallet wallet) {
        walletRepository.save(wallet);
    }

    private Wallet getWalletFromRepository() {
        List<Wallet> wallets = walletRepository.findAll();

        return wallets.stream().findFirst().get();
    }
}

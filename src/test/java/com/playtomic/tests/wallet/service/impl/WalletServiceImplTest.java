package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.domain.Recharge;
import com.playtomic.tests.wallet.dto.Wallet;
import com.playtomic.tests.wallet.exception.BusinessException;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.WalletServiceImpl;
import com.playtomic.tests.wallet.service.stripe.StripeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles(profiles = "test")
@ExtendWith(MockitoExtension.class)
public class WalletServiceImplTest {
    private static final String MOCK_WALLET_ID = "e7c08fea-447b-4744-bbda-a81b6944bd74";
    private static final BigDecimal INITIAL_CURRENT_BALANCE_OF_WALLET = new BigDecimal(1000);

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private StripeService stripeService;
    @InjectMocks
    private WalletServiceImpl walletServiceImpl;


    @Test
    public void createWalletWithInitialCurrentBalanceSuccessfully() {
        // Arrange
        Wallet walletToBeCreated = createWalletInstance();

        when(walletRepository.save(any())).thenReturn(walletToBeCreated);

        // Act
        Wallet wallet = walletServiceImpl.createWallet(walletToBeCreated);

        // Assert
        Assertions.assertEquals(MOCK_WALLET_ID, wallet.getId());
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET, wallet.getCurrentBalance());
    }

    @Test
    public void getWalletByIdSuccessfully() {
        // Arrange
        Wallet walletToBeReturned = createWalletInstance();

        when(walletRepository.findById(anyString())).thenReturn(Optional.of(walletToBeReturned));

        // Act
        Wallet wallet = walletServiceImpl.getWallet(MOCK_WALLET_ID);

        // Assert
        Assertions.assertEquals(MOCK_WALLET_ID, wallet.getId());
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET, wallet.getCurrentBalance());
    }

    @Test
    public void getWalletByIdWhenWalletDoesNotExistFails() {
        // Arrange
        String nonExistingWalledId = "33af95ee-3de4-4d1f-b0f6-f71cc568665e";

        when(walletRepository.findById(nonExistingWalledId)).thenReturn(Optional.empty());

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.getWallet(nonExistingWalledId);
        }, "Wallet with id " + nonExistingWalledId + " does not exist.");
    }

    @Test
    public void rechargeWalletWithAnAmountSuccessfully() {
        // Arrange
        Recharge recharge = createRechargeInstance();
        Wallet walletToBeRecharged = createWalletInstance();

        when(walletRepository.findById(anyString())).thenReturn(Optional.of(walletToBeRecharged));

        // Act
        walletServiceImpl.rechargeWallet(MOCK_WALLET_ID, recharge);

        // Assert
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET.add(recharge.getAmount()),
                                walletToBeRecharged.getCurrentBalance());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    public void rechargeWalletWithMultipleThreadsSuccessfully() throws InterruptedException {
        // Arrange
        int numberOfThreads = 20;
        ExecutorService service = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Wallet walletToBeRecharged = createWalletInstance();
        String walletToBeRechargedId = walletToBeRecharged.getId();

        Recharge recharge = createRechargeInstance();

        when(walletRepository.findById(anyString())).thenReturn(Optional.of(walletToBeRecharged));

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                walletServiceImpl.rechargeWallet(walletToBeRechargedId, recharge);

                latch.countDown();
            });
        }

        latch.await();

        // Assert
        Assertions.assertEquals(
                INITIAL_CURRENT_BALANCE_OF_WALLET.add(recharge.getAmount().multiply(new BigDecimal(numberOfThreads))),
                walletToBeRecharged.getCurrentBalance());
    }

    @Test
    public void rechargeWalletWithANullCreditCardFails() {
        // Arrange
        Recharge recharge = createRechargeInstance();
        recharge.setCreditCardNumber(null);

        Wallet walletToBeRecharged = createWalletInstance();
        String walletToBeRechargedId = walletToBeRecharged.getId();

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.rechargeWallet(walletToBeRechargedId, recharge);
        }, "Credit card number cannot be null");
    }

    @Test
    public void rechargeWalletWithANullAmountFails() {
        // Arrange
        Recharge recharge = createRechargeInstance();
        recharge.setAmount(null);

        Wallet walletToBeRecharged = createWalletInstance();
        String walletToBeRechargedId = walletToBeRecharged.getId();

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.rechargeWallet(walletToBeRechargedId, recharge);
        }, "Amount if recharge cannot be null");
    }

    @Test
    public void rechargeWalletWithAnAmountWhenWalletDoesNotExistFails() {
        // Arrange
        String nonExistingWalledId = "33af95ee-3de4-4d1f-b0f6-f71cc568665e";
        Recharge recharge = createRechargeInstance();

        when(walletRepository.findById(nonExistingWalledId)).thenReturn(Optional.empty());

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.rechargeWallet(nonExistingWalledId, recharge);
        }, "Wallet with id " + nonExistingWalledId + " does not exist.");
    }

    @Test
    public void chargeWalletWithAnAmountSuccessfully() {
        // Arrange
        Wallet walletToBeCharged = createWalletInstance();
        String walletToBeChargedId = walletToBeCharged.getId();
        BigDecimal amountToBeCharged = new BigDecimal(500);

        when(walletRepository.findById(any())).thenReturn(Optional.of(walletToBeCharged));

        // Act
        walletServiceImpl.chargeWallet(walletToBeChargedId, amountToBeCharged);

        // Assert
        Assertions.assertEquals(INITIAL_CURRENT_BALANCE_OF_WALLET.subtract(amountToBeCharged),
                                walletToBeCharged.getCurrentBalance());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    public void chargeWalletWithMultipleThreadsSuccessfully() throws InterruptedException {
        // Arrange
        int numberOfThreads = 20;
        ExecutorService service = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Wallet walletToBeCharged = createWalletInstance();
        String walletToBeChargedId = walletToBeCharged.getId();

        BigDecimal amountToBeCharged = new BigDecimal(10);

        when(walletRepository.findById(anyString())).thenReturn(Optional.of(walletToBeCharged));

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                walletServiceImpl.chargeWallet(walletToBeChargedId, amountToBeCharged);

                latch.countDown();
            });
        }

        latch.await();

        // Assert
        Assertions.assertEquals(
                INITIAL_CURRENT_BALANCE_OF_WALLET.subtract(amountToBeCharged.multiply(new BigDecimal(numberOfThreads))),
                walletToBeCharged.getCurrentBalance());
    }

    @Test
    public void chargeWalletWithAnAmountThatIsNullFails() {
        // Arrange
        Wallet walletToBeCharged = createWalletInstance();
        String walletToBeChargedId = walletToBeCharged.getId();
        BigDecimal amountToBeCharged = null;

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.chargeWallet(walletToBeChargedId, amountToBeCharged);
        }, "Charge amount cannot be null");
    }

    @Test
    public void chargeWalletWithAnAmountWhenWalletDoesNotExistFails() {
        // Arrange
        String nonExistingWalledId = "33af95ee-3de4-4d1f-b0f6-f71cc568665e";
        BigDecimal amountToBeCharged = new BigDecimal(500);

        when(walletRepository.findById(any())).thenReturn(Optional.empty());

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.chargeWallet(nonExistingWalledId, amountToBeCharged);
        }, "Wallet with id " + nonExistingWalledId + " does not exist.");
    }

    @Test
    public void chargeWalletWithAnAmountWhenCurrentBalanceIsNotSufficientFails() {
        // Arrange
        Wallet walletToBeCharged = createWalletInstance();
        String walletToBeChargedId = walletToBeCharged.getId();
        BigDecimal amountToBeCharged = INITIAL_CURRENT_BALANCE_OF_WALLET.add(INITIAL_CURRENT_BALANCE_OF_WALLET);

        when(walletRepository.findById(any())).thenReturn(Optional.of(walletToBeCharged));

        // Act && Assert
        Assertions.assertThrows(BusinessException.class, () -> {
            walletServiceImpl.chargeWallet(walletToBeChargedId, amountToBeCharged);
        }, "There is not enough balance to charge wallet with id " + walletToBeChargedId + " the amount of "
           + amountToBeCharged);
    }

    private Wallet createWalletInstance() {
        Wallet wallet = new Wallet();
        wallet.setId(MOCK_WALLET_ID);
        wallet.setCurrentBalance(INITIAL_CURRENT_BALANCE_OF_WALLET);

        return wallet;
    }

    private Recharge createRechargeInstance() {
        Recharge recharge = new Recharge();
        recharge.setCreditCardNumber("1234567890");
        recharge.setAmount(new BigDecimal(1000));

        return recharge;
    }
}

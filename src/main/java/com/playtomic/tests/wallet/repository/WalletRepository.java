package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.dto.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
}

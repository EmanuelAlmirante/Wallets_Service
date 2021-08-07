package com.playtomic.tests.wallet.dto;

import com.playtomic.tests.wallet.exception.BusinessException;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @Column(name = "current_balance")
    private BigDecimal currentBalance;

    public void addAmountToCurrentBalance(BigDecimal amount) {
        setCurrentBalance(this.currentBalance.add(amount));
    }

    public void subtractAmountToCurrentBalance(BigDecimal amount) {
        BigDecimal newCurrentBalance = this.currentBalance.subtract(amount);

        if (newCurrentBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("There is not enough balance to charge wallet with id " + this.getId() + " the amount of " + amount);
        } else {
            setCurrentBalance(newCurrentBalance);
        }
    }
}

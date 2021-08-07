package com.playtomic.tests.wallet.service.impl.stripe;

import com.playtomic.tests.wallet.service.stripe.StripeService;
import com.playtomic.tests.wallet.service.stripe.StripeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class StripeServiceTest {

    StripeService s = new StripeService();

    @Test
    public void test_exception() {
        Assertions.assertThrows(StripeServiceException.class, () -> {
            s.charge("4242 4242 4242 4242", new BigDecimal(5));
        });
    }

    @Test
    public void test_ok() throws StripeServiceException {
        s.charge("4242 4242 4242 4242", new BigDecimal(15));
    }
}

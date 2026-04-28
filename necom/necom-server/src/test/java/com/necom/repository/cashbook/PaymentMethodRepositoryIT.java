package com.necom.repository.cashbook;

import com.necom.entity.cashbook.PaymentMethod;
import com.necom.entity.cashbook.PaymentMethodType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class PaymentMethodRepositoryIT {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Test
    void findAllByStatusShouldOnlyReturnActivePaymentMethods() {
        int initialActiveCount = paymentMethodRepository.findAllByStatus(1).size();

        PaymentMethod active = new PaymentMethod()
                .setName("Cash")
                .setCode(PaymentMethodType.CASH)
                .setStatus(1);
        PaymentMethod inactive = new PaymentMethod()
                .setName("Old Gateway")
                .setCode(PaymentMethodType.PAYPAL)
                .setStatus(0);
        paymentMethodRepository.save(active);
        paymentMethodRepository.save(inactive);

        List<PaymentMethod> activeMethods = paymentMethodRepository.findAllByStatus(1);

        assertEquals(initialActiveCount + 1, activeMethods.size());
        boolean hasSavedCashMethod = activeMethods.stream()
                .anyMatch(item -> "Cash".equals(item.getName()) && item.getCode() == PaymentMethodType.CASH);
        assertEquals(true, hasSavedCashMethod);
    }
}

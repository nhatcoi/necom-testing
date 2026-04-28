package com.necom.service.order;

import com.necom.config.payment.paypal.PayPalHttpClient;
import com.necom.entity.order.Order;
import com.necom.mapper.client.ClientOrderMapper;
import com.necom.mapper.general.NotificationMapper;
import com.necom.repository.authentication.UserRepository;
import com.necom.repository.cart.CartRepository;
import com.necom.repository.general.NotificationRepository;
import com.necom.repository.order.OrderRepository;
import com.necom.repository.promotion.PromotionRepository;
import com.necom.repository.waybill.WaybillLogRepository;
import com.necom.repository.waybill.WaybillRepository;
import com.necom.service.general.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WaybillRepository waybillRepository;
    @Mock
    private WaybillLogRepository waybillLogRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private PayPalHttpClient payPalHttpClient;
    @Mock
    private ClientOrderMapper clientOrderMapper;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void cancelOrderShouldUpdateStatusWhenOrderIsCancelable() {
        Order order = new Order();
        order.setId(10L);
        order.setCode("ORDER-001");
        order.setStatus(2);
        when(orderRepository.findByCode("ORDER-001")).thenReturn(Optional.of(order));
        when(waybillRepository.findByOrderId(10L)).thenReturn(Optional.empty());

        orderService.cancelOrder("ORDER-001");

        assertEquals(5, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrderShouldThrowWhenOrderIsNotCancelable() {
        Order order = new Order();
        order.setCode("ORDER-LOCKED");
        order.setStatus(3);
        when(orderRepository.findByCode("ORDER-LOCKED")).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.cancelOrder("ORDER-LOCKED"));

        assertEquals(
                "Order with code ORDER-LOCKED is in delivery or has been cancelled. Please check again!",
                exception.getMessage()
        );
        verify(orderRepository, never()).save(any(Order.class));
    }
}

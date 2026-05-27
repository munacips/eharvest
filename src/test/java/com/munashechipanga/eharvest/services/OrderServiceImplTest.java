package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.enums.LogisticsType;
import com.munashechipanga.eharvest.enums.OrderStatus;
import com.munashechipanga.eharvest.repositories.BuyerRepository;
import com.munashechipanga.eharvest.repositories.FarmerRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    BuyerRepository buyerRepository;

    @Mock
    FarmerRepository farmerRepository;

    @Mock
    LogisticsRepository logisticsRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    ReviewService reviewService;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    void confirmDeliveryIncrementsSuccessfulCounters() {
        Buyer buyer = buyer(1L);
        Farmer farmer = farmer(2L);
        Order order = order(10L, buyer, farmer, OrderStatus.ACCEPTED.name(), LogisticsType.BUYER_PICKUP);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.confirmDelivery(order.getId());

        assertThat(buyer.getSuccessfulBuys()).isEqualTo(1);
        assertThat(farmer.getSuccessfulSales()).isEqualTo(1);
        verify(buyerRepository).save(buyer);
        verify(farmerRepository).save(farmer);
        verify(reviewService).createPendingReviewsForOrder(order);
    }

    @Test
    void cancelOrderIncrementsUnsuccessfulCountersAndRefundsEscrow() {
        Buyer buyer = buyer(1L);
        buyer.setUsdBalance(50.0);
        Farmer farmer = farmer(2L);
        Order order = order(11L, buyer, farmer, OrderStatus.ACCEPTED.name(), LogisticsType.THIRD_PARTY);
        order.setCurrency(com.munashechipanga.eharvest.enums.Currency.USD);
        order.setEscrowAmount(20.0);
        order.setEscrowHeld(true);
        order.setEscrowReleased(false);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(order.getId(), "Buyer backed out");

        assertThat(buyer.getUnsuccessfulBuys()).isEqualTo(1);
        assertThat(farmer.getUnsuccessfulSales()).isEqualTo(1);
        assertThat(buyer.getUsdBalance()).isEqualTo(70.0);
        assertThat(order.getEscrowHeld()).isFalse();
        verify(buyerRepository).save(buyer);
        verify(farmerRepository).save(farmer);
        verify(userRepository).save(buyer);
    }

    @Test
    void updateOrderRemovesPreviousSuccessfulCountWhenStatusChangesFromDeliveredToCancelled() {
        Buyer buyer = buyer(1L);
        buyer.setSuccessfulBuys(1);
        Farmer farmer = farmer(2L);
        farmer.setSuccessfulSales(1);
        Order order = order(12L, buyer, farmer, OrderStatus.DELIVERED.name(), LogisticsType.BUYER_PICKUP);

        com.munashechipanga.eharvest.dtos.request.CreateOrderDTO dto =
                new com.munashechipanga.eharvest.dtos.request.CreateOrderDTO();
        dto.setStatus(OrderStatus.CANCELLED.name());

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrder(order.getId(), dto);

        assertThat(buyer.getSuccessfulBuys()).isZero();
        assertThat(farmer.getSuccessfulSales()).isZero();
        assertThat(buyer.getUnsuccessfulBuys()).isEqualTo(1);
        assertThat(farmer.getUnsuccessfulSales()).isEqualTo(1);
    }

    private Buyer buyer(Long id) {
        Buyer buyer = new Buyer();
        buyer.setId(id);
        buyer.setRole("BUYER");
        return buyer;
    }

    private Farmer farmer(Long id) {
        Farmer farmer = new Farmer();
        farmer.setId(id);
        farmer.setRole("FARMER");
        return farmer;
    }

    private Order order(Long id, Buyer buyer, Farmer farmer, String status, LogisticsType logisticsType) {
        Order order = new Order();
        order.setId(id);
        order.setBuyer(buyer);
        order.setFarmer(farmer);
        order.setStatus(status);
        order.setLogisticsType(logisticsType);
        order.setTotalAmount(20.0);
        return order;
    }
}

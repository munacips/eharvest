package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.dtos.request.LogisticsRequestCreateDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.LogisticsProvider;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.TransactionHistory;
import com.munashechipanga.eharvest.enums.Currency;
import com.munashechipanga.eharvest.enums.LogisticsStatus;
import com.munashechipanga.eharvest.enums.TransactionType;
import com.munashechipanga.eharvest.repositories.LogisticsProviderRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.TransactionRepository;
import com.munashechipanga.eharvest.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsServiceImplTest {

    @Mock
    LogisticsRepository logisticsRepository;

    @Mock
    LogisticsProviderRepository providerRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    LogisticsServiceImpl logisticsService;

    @Test
    void createLogisticsRequestRejectsCostAboveBuyerBalance() {
        Buyer buyer = new Buyer();
        buyer.setUsdBalance(20.0);
        Order order = new Order();
        order.setId(1L);
        order.setBuyer(buyer);
        order.setCurrency(Currency.USD);

        LogisticsRequestCreateDTO dto = new LogisticsRequestCreateDTO();
        dto.setOrder(1L);
        dto.setCost(25.0);
        dto.setPickupLocation("Farm");
        dto.setDeliveryLocation("Market");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> logisticsService.createLogisticsRequest(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Logistics cost cannot exceed buyer USD balance");
        verify(logisticsRepository, never()).save(any(LogisticsRequest.class));
    }

    @Test
    void acceptRequestUsesBuyerBalanceNotProviderBalanceForEscrow() {
        Buyer buyer = new Buyer();
        buyer.setUsdBalance(100.0);

        Order order = new Order();
        order.setId(1L);
        order.setBuyer(buyer);
        order.setCurrency(Currency.USD);

        LogisticsRequest request = new LogisticsRequest();
        request.setId(2L);
        request.setOrder(order);
        request.setCost(40.0);
        request.setStatus(LogisticsStatus.SEARCHING.name());
        request.setEscrowHeld(false);
        request.setEscrowReleased(false);

        LogisticsProvider provider = new LogisticsProvider();
        provider.setId(3L);
        provider.setUsdBalance(0.0);

        when(logisticsRepository.findById(2L)).thenReturn(Optional.of(request));
        when(providerRepository.findById(3L)).thenReturn(Optional.of(provider));
        when(logisticsRepository.save(any(LogisticsRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(TransactionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LogisticsRequestDto accepted = logisticsService.acceptRequest(2L, 3L);

        assertThat(accepted.getAssignedProvider()).isSameAs(provider);
        assertThat(accepted.getStatus()).isEqualTo(LogisticsStatus.ASSIGNED.name());
        assertThat(accepted.getEscrowHeld()).isTrue();
        assertThat(buyer.getUsdBalance()).isEqualTo(60.0);
        assertThat(provider.getUsdBalance()).isEqualTo(0.0);
    }

    @Test
    void deliveredAfterEscrowHoldCreatesSeparateTransactionsForSameOrder() {
        Buyer buyer = new Buyer();
        buyer.setUsdBalance(100.0);

        Order order = new Order();
        order.setId(2L);
        order.setBuyer(buyer);
        order.setCurrency(Currency.USD);

        LogisticsProvider provider = new LogisticsProvider();
        provider.setId(5L);
        provider.setUsdBalance(10.0);

        LogisticsRequest request = new LogisticsRequest();
        request.setId(7L);
        request.setOrder(order);
        request.setAssignedProvider(provider);
        request.setCost(40.0);
        request.setStatus(LogisticsStatus.SEARCHING.name());
        request.setEscrowHeld(false);
        request.setEscrowReleased(false);

        List<TransactionHistory> savedTransactions = new ArrayList<>();

        when(logisticsRepository.findById(7L)).thenReturn(Optional.of(request));
        when(providerRepository.findById(5L)).thenReturn(Optional.of(provider));
        when(logisticsRepository.save(any(LogisticsRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(TransactionHistory.class))).thenAnswer(invocation -> {
            TransactionHistory txn = invocation.getArgument(0);
            savedTransactions.add(txn);
            return txn;
        });

        logisticsService.acceptRequest(7L, 5L);
        logisticsService.markInTransit(7L);
        LogisticsRequestDto delivered = logisticsService.markDelivered(7L);

        assertThat(delivered.getStatus()).isEqualTo(LogisticsStatus.DELIVERED.name());
        assertThat(delivered.getEscrowHeld()).isTrue();
        assertThat(delivered.getEscrowReleased()).isTrue();
        assertThat(buyer.getUsdBalance()).isEqualTo(60.0);
        assertThat(provider.getUsdBalance()).isEqualTo(50.0);
        assertThat(savedTransactions).hasSize(2);
        assertThat(savedTransactions)
                .extracting(TransactionHistory::getOrder)
                .containsOnly(order);
        assertThat(savedTransactions)
                .extracting(TransactionHistory::getType)
                .containsExactly(TransactionType.ESCROW_HOLD, TransactionType.ESCROW_RELEASE);
        verify(transactionRepository, atLeastOnce()).save(any(TransactionHistory.class));
    }
}

package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderDTO dto);
    OrderResponseDTO updateOrder(Long id,CreateOrderDTO dto);
    void deleteOrder(Long id);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getOrders();
    List<OrderResponseDTO> getOrdersByFarmerId(Long farmerId);
    List<OrderResponseDTO> getOrdersByBuyerId(Long buyerId);

    OrderResponseDTO acceptOrder(Long id);
    OrderResponseDTO rejectOrder(Long id, String reason);
    OrderResponseDTO holdEscrow(Long id);
    OrderResponseDTO confirmDeliveryStarted(Long id);
    OrderResponseDTO confirmDelivery(Long id);
}

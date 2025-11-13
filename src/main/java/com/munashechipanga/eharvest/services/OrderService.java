package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderDTO dto);
    OrderResponseDTO updateOrder(Long id,OrderResponseDTO dto);
    void deleteOrder(Long id);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getOrders();
}

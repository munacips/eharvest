package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.OrderItemDto;
import com.munashechipanga.eharvest.dtos.response.OrderItemResponseDTO;

import java.util.List;

public interface OrderItemService {
    OrderItemResponseDTO getOrderItemById(Long id);
    List<OrderItemResponseDTO> getAllOrders();
    OrderItemResponseDTO createOrderItem(OrderItemDto orderItemDto);
    OrderItemResponseDTO updateOrderItem(Long id, OrderItemDto orderItemDto);
    void deleteOrderItem(Long id);
    List<OrderItemResponseDTO> getOrderItemsByOrderId(Long orderId);
}

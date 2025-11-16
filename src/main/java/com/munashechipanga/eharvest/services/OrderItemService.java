package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.OrderItemDto;

import java.util.List;

public interface OrderItemService {
    OrderItemDto getOrderItemById(Long id);
    List<OrderItemDto> getAllOrders();
    OrderItemDto createOrderItem(OrderItemDto orderItemDto);
    OrderItemDto updateOrderItem(Long id, OrderItemDto orderItemDto);
    void deleteOrderItem(Long id);
}

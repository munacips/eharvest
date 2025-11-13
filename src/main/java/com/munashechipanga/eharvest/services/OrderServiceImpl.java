package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Override
    public OrderResponseDTO createOrder(CreateOrderDTO dto) {

        LocalDateTime dateTime = LocalDateTime.now();

        Order order = new Order();

        order.setOrderDate(dateTime);
        order.setEscrowReleased(false);
        order.setBuyer(dto.getBuyer());
        order.setStatus("PENDING");
        order.setTotalAmount(dto.getTotalAmount());
        order.setLogisticsRequest(dto.getLogisticsRequest());

        Order savedOrder = orderRepository.save(order);

        return mapToDto(savedOrder);
    }

    @Override
    public OrderResponseDTO updateOrder(Long id, OrderResponseDTO dto) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if(dto.getStatus() != null) order.setStatus(dto.getStatus());
        if(dto.getEscrowReleased() != null) order.setEscrowReleased(dto.getEscrowReleased());
        if(dto.getBuyer() != null) order.setBuyer(dto.getBuyer());
        if(dto.getLogisticsRequest() != null) order.setLogisticsRequest(dto.getLogisticsRequest());
        if(dto.getTotalAmount() != null) order.setTotalAmount(dto.getTotalAmount());

        Order updatedOrder = orderRepository.save(order);

        return mapToDto(updatedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDto(order);
    }

    @Override
    public List<OrderResponseDTO> getOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToDto(Order order){
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setBuyer(order.getBuyer());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setLogisticsRequest(order.getLogisticsRequest());
        dto.setEscrowReleased(order.getEscrowReleased());

        return dto;
    }
}

package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.BuyerRepository;
import com.munashechipanga.eharvest.repositories.FarmerRepository;
import com.munashechipanga.eharvest.repositories.LogisticsRepository;
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

    @Autowired
    BuyerRepository buyerRepository;

    @Autowired
    FarmerRepository farmerRepository;

    @Autowired
    LogisticsRepository logisticsRepository;

    @Override
    public OrderResponseDTO createOrder(CreateOrderDTO dto) {

        LocalDateTime dateTime = LocalDateTime.now();

        Order order = new Order();

        order.setOrderDate(dateTime);
        order.setEscrowReleased(dto.getEscrowReleased() != null ? dto.getEscrowReleased() : false);
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
        order.setTotalAmount(dto.getTotalAmount());

        if (dto.getBuyerId() != null) {
            Buyer buyer = buyerRepository.findById(dto.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + dto.getBuyerId()));
            order.setBuyer(buyer);
        }

        if (dto.getFarmerId() != null) {
            Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + dto.getFarmerId()));
            order.setFarmer(farmer);
        }

        if (dto.getLogisticsRequestId() != null) {
            LogisticsRequest logisticsRequest = logisticsRepository.findById(dto.getLogisticsRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("LogisticsRequest not found with id: " + dto.getLogisticsRequestId()));
            order.setLogisticsRequest(logisticsRequest);
        }

        Order savedOrder = orderRepository.save(order);

        return mapToDto(savedOrder);
    }

    @Override
    public OrderResponseDTO updateOrder(Long id, CreateOrderDTO dto) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if(dto.getStatus() != null) order.setStatus(dto.getStatus());
        if(dto.getEscrowReleased() != null) order.setEscrowReleased(dto.getEscrowReleased());

        if(dto.getBuyerId() != null) {
            Buyer buyer = buyerRepository.findById(dto.getBuyerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + dto.getBuyerId()));
            order.setBuyer(buyer);
        }

        if(dto.getFarmerId() != null) {
            Farmer farmer = farmerRepository.findById(dto.getFarmerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + dto.getFarmerId()));
            order.setFarmer(farmer);
        }

        if (dto.getLogisticsRequestId() != null) {
            LogisticsRequest logisticsRequest = logisticsRepository.findById(dto.getLogisticsRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("LogisticsRequest not found with id: " + dto.getLogisticsRequestId()));
            order.setLogisticsRequest(logisticsRequest);
        }

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

    @Override
    public List<OrderResponseDTO> getOrdersByFarmerId(Long farmerId) {
        return orderRepository.findByFarmer_Id(farmerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByBuyerId(Long buyerId) {
        return orderRepository.findByBuyer_Id(buyerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToDto(Order order){
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setBuyer(order.getBuyer());
        dto.setFarmer(order.getFarmer());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setLogisticsRequest(order.getLogisticsRequest());
        dto.setEscrowReleased(order.getEscrowReleased());

        return dto;
    }
}

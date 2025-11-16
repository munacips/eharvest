package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.OrderItemDto;
import com.munashechipanga.eharvest.entities.OrderItem;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    OrderItemRepository repository;

    @Override
    public OrderItemDto getOrderItemById(Long id) {
        OrderItem orderItem = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order Item not found"));
        return mapToDto(orderItem);
    }

    @Override
    public List<OrderItemDto> getAllOrders() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDto createOrderItem(OrderItemDto orderItemDto) {
        OrderItem orderItem = new OrderItem();

        orderItem.setPrice(orderItemDto.getPrice());
        orderItem.setQuantity(orderItemDto.getQuantity());
        orderItem.setProduce(orderItemDto.getProduce());
        orderItem.setOrder(orderItemDto.getOrder());

        repository.save(orderItem);
        return mapToDto(orderItem);
    }

    @Override
    public OrderItemDto updateOrderItem(Long id, OrderItemDto dto) {
        OrderItem orderItem = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order Item not found"));

        if(dto.getOrder() != null) orderItem.setOrder(dto.getOrder());
        if(dto.getProduce() != null) orderItem.setProduce(dto.getProduce());
        if(dto.getQuantity() != null) orderItem.setQuantity(dto.getQuantity());
        if(dto.getPrice() != null) orderItem.setPrice(dto.getPrice());

        repository.save(orderItem);
        return mapToDto(orderItem);
    }

    @Override
    public void deleteOrderItem(Long id){
        repository.deleteById(id);
    }

    private OrderItemDto mapToDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = new OrderItemDto();

        orderItemDto.setId(orderItem.getId());
        orderItemDto.setQuantity(orderItem.getQuantity());
        orderItemDto.setPrice(orderItem.getPrice());
        orderItemDto.setProduce(orderItem.getProduce());
        orderItemDto.setOrder(orderItem.getOrder());

        return orderItemDto;
    }
}

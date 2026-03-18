package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.OrderItemDto;
import com.munashechipanga.eharvest.dtos.response.OrderItemResponseDTO;
import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.OrderItem;
import com.munashechipanga.eharvest.entities.Produce;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.OrderItemRepository;
import com.munashechipanga.eharvest.repositories.OrderRepository;
import com.munashechipanga.eharvest.repositories.ProduceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    OrderItemRepository repository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProduceRepository produceRepository;

    @Override
    public OrderItemResponseDTO getOrderItemById(Long id) {
        OrderItem orderItem = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order Item not found"));
        return mapToDto(orderItem);
    }

    @Override
    public List<OrderItemResponseDTO> getAllOrders() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemResponseDTO createOrderItem(OrderItemDto orderItemDto) {
        OrderItem orderItem = new OrderItem();

        orderItem.setPrice(orderItemDto.getPrice());
        orderItem.setQuantity(orderItemDto.getQuantity());

        if(orderItemDto.getProduce() != null){
            Produce produce = produceRepository.findById(orderItemDto.getProduce())
                    .orElseThrow(()-> new ResourceNotFoundException("Produce not found"));
            orderItem.setProduce(produce);
        }

        if(orderItemDto.getOrder() != null) {
            Order order = orderRepository.findById(orderItemDto.getOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderItemDto.getOrder()));
            orderItem.setOrder(order);
        }

        repository.save(orderItem);
        return mapToDto(orderItem);
    }

    @Override
    public OrderItemResponseDTO updateOrderItem(Long id, OrderItemDto dto) {
        OrderItem orderItem = repository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order Item not found"));

        if(dto.getOrder() != null) {
            Order order = orderRepository.findById(dto.getOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.getOrder()));
            orderItem.setOrder(order);
        };
        if(dto.getProduce() != null) {
            Produce produce = produceRepository.findById(dto.getProduce())
                    .orElseThrow(()-> new ResourceNotFoundException("Produce not found"));
            orderItem.setProduce(produce);
        };
        if(dto.getQuantity() != null) orderItem.setQuantity(dto.getQuantity());
        if(dto.getPrice() != null) orderItem.setPrice(dto.getPrice());

        repository.save(orderItem);
        return mapToDto(orderItem);
    }

    @Override
    public void deleteOrderItem(Long id){
        repository.deleteById(id);
    }

    @Override
    public List<OrderItemResponseDTO> getOrderItemsByOrderId(Long orderId) {
        return repository.findByOrder_Id(orderId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private OrderItemResponseDTO mapToDto(OrderItem orderItem) {
        //OrderItemDto orderItemDto = new OrderItemDto();
        OrderItemResponseDTO orderItemDto = new OrderItemResponseDTO();

        orderItemDto.setId(orderItem.getId());
        orderItemDto.setQuantity(orderItem.getQuantity());
        orderItemDto.setPrice(orderItem.getPrice());
        orderItemDto.setProduce(orderItem.getProduce());
        orderItemDto.setOrder(orderItem.getOrder());

        return orderItemDto;
    }
}

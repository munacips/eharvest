package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.OrderItemDto;
import com.munashechipanga.eharvest.services.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/order_items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemDto> saveOrderItem(OrderItemDto dto){
        return ResponseEntity.ok(orderItemService.createOrderItem(dto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<OrderItemDto> deleteOrderItem(@PathVariable Long id){
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<OrderItemDto> updateOrderItem(@PathVariable Long id, @RequestBody OrderItemDto dto){
        return ResponseEntity.ok(orderItemService.updateOrderItem(id, dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<OrderItemDto> getOrderItemById(@PathVariable Long id){
        return ResponseEntity.ok(orderItemService.getOrderItemById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderItemDto>> getAllOrderItems(){
        return ResponseEntity.ok(orderItemService.getAllOrders());
    }
}

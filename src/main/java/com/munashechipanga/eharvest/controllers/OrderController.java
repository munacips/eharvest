package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.request.CreateOrderDTO;
import com.munashechipanga.eharvest.dtos.response.OrderResponseDTO;
import com.munashechipanga.eharvest.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {
    @Autowired
    OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(){
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByFarmer(@PathVariable Long farmerId){
        return ResponseEntity.ok(orderService.getOrdersByFarmerId(farmerId));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByBuyer(@PathVariable Long buyerId){
        return ResponseEntity.ok(orderService.getOrdersByBuyerId(buyerId));
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody CreateOrderDTO dto){
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody CreateOrderDTO dto){
        return ResponseEntity.ok(orderService.updateOrder(id,dto));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<OrderResponseDTO> acceptOrder(@PathVariable Long id){
        return ResponseEntity.ok(orderService.acceptOrder(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderResponseDTO> rejectOrder(@PathVariable Long id, @RequestParam(required = false) String reason){
        return ResponseEntity.ok(orderService.rejectOrder(id, reason));
    }

    @PostMapping("/{id}/hold-escrow")
    public ResponseEntity<OrderResponseDTO> holdEscrow(@PathVariable Long id){
        return ResponseEntity.ok(orderService.holdEscrow(id));
    }

    @PostMapping("/{id}/delivery-started")
    public ResponseEntity<OrderResponseDTO> confirmDeliveryStarted(@PathVariable Long id){
        return ResponseEntity.ok(orderService.confirmDeliveryStarted(id));
    }

    @PostMapping("/{id}/delivery-confirmed")
    public ResponseEntity<OrderResponseDTO> confirmDelivery(@PathVariable Long id){
        return ResponseEntity.ok(orderService.confirmDelivery(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }
}

package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.services.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/buyers")
public class BuyerController {
    @Autowired
    BuyerService buyerService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createBuyer(BuyerDto dto){
        return ResponseEntity.ok(buyerService.createBuyer(dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getBuyer(@PathVariable Long id){
        return ResponseEntity.ok(buyerService.getBuyerById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllBuyers(){
        return ResponseEntity.ok(buyerService.getAllBuyers());
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponseDTO> updateBuyer(@PathVariable Long id, @RequestBody BuyerDto dto){
        return ResponseEntity.ok(buyerService.updateBuyer(id, dto));
    }

    @DeleteMapping
    public ResponseEntity<UserResponseDTO> deleteBuyer(@PathVariable Long id){
        buyerService.deleteBuyer(id);
        return ResponseEntity.ok().build();
    }
}

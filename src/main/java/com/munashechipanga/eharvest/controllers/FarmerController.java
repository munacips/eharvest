package com.munashechipanga.eharvest.controllers;


import com.munashechipanga.eharvest.dtos.FarmerDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.services.FarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/farmers")
public class FarmerController {
    @Autowired
    FarmerService farmerService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createFarmer(FarmerDto dto){
        return ResponseEntity.ok(farmerService.createFarmer(dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getFarmer(@PathVariable Long id){
        return ResponseEntity.ok(farmerService.getFarmerById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllFarmers(){
        return ResponseEntity.ok(farmerService.getAllFarmers());
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponseDTO> updateFarmer(@PathVariable Long id, @RequestBody FarmerDto dto){
        return ResponseEntity.ok(farmerService.updateFarmer(id, dto));
    }

    @DeleteMapping
    public ResponseEntity<UserResponseDTO> deleteFarmer(@PathVariable Long id){
        farmerService.deleteFarmer(id);
        return ResponseEntity.ok().build();
    }
}

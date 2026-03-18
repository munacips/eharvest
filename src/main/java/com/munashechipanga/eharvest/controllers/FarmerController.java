package com.munashechipanga.eharvest.controllers;


import com.munashechipanga.eharvest.dtos.FarmerDto;
import com.munashechipanga.eharvest.dtos.FarmerFilter;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.services.FarmerService;
import com.munashechipanga.eharvest.utils.PagingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/farmers")
public class FarmerController {
    @Autowired
    FarmerService farmerService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createFarmer(@RequestBody FarmerDto dto){
        return ResponseEntity.ok(farmerService.createFarmer(dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getFarmer(@PathVariable Long id){
        return ResponseEntity.ok(farmerService.getFarmerById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> search(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String farmName,
            @RequestParam(required = false) String farmLocation,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Integer minTrustScore,
            @RequestParam(required = false) Integer maxTrustScore,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ){
        Sort sortSpec = PagingUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        FarmerFilter filter = new FarmerFilter();
        filter.setFirstName(firstName);
        filter.setLastName(lastName);
        filter.setUsername(username);
        filter.setEmail(email);
        filter.setFarmName(farmName);
        filter.setFarmLocation(farmLocation);
        filter.setActive(active);
        filter.setVerified(verified);
        filter.setMinTrustScore(minTrustScore);
        filter.setMaxTrustScore(maxTrustScore);
        filter.setSearch(search);

        return ResponseEntity.ok(farmerService.search(filter, pageable));
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponseDTO> updateFarmer(@PathVariable Long id, @RequestBody FarmerDto dto){
        return ResponseEntity.ok(farmerService.updateFarmer(id, dto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<UserResponseDTO> deleteFarmer(@PathVariable Long id){
        farmerService.deleteFarmer(id);
        return ResponseEntity.ok().build();
    }
}

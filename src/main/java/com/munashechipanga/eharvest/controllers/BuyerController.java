package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.BuyerDto;
import com.munashechipanga.eharvest.dtos.BuyerFilter;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.services.BuyerService;
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
@RequestMapping("api/v1/buyers")
public class BuyerController {
    @Autowired
    BuyerService buyerService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createBuyer(@RequestBody BuyerDto dto){
        return ResponseEntity.ok(buyerService.createBuyer(dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getBuyer(@PathVariable Long id){
        return ResponseEntity.ok(buyerService.getBuyerById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> search(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName,
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

        BuyerFilter filter = new BuyerFilter();
        filter.setFirstName(firstName);
        filter.setLastName(lastName);
        filter.setUsername(username);
        filter.setEmail(email);
        filter.setCompanyName(companyName);
        filter.setActive(active);
        filter.setVerified(verified);
        filter.setMinTrustScore(minTrustScore);
        filter.setMaxTrustScore(maxTrustScore);
        filter.setSearch(search);

        return ResponseEntity.ok(buyerService.search(filter, pageable));
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponseDTO> updateBuyer(@PathVariable Long id, @RequestBody BuyerDto dto){
        return ResponseEntity.ok(buyerService.updateBuyer(id, dto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<UserResponseDTO> deleteBuyer(@PathVariable Long id){
        buyerService.deleteBuyer(id);
        return ResponseEntity.ok().build();
    }
}

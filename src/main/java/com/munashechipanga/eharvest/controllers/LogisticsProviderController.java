package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.LogisticsProviderDto;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.services.LogisticsProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/logistics-providers")
public class LogisticsProviderController {
    @Autowired
    LogisticsProviderService logisticsProviderService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createLogisticsProvider(@RequestBody LogisticsProviderDto dto) {
        return ResponseEntity.ok(logisticsProviderService.createLogisticsProvider(dto));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getLogisticsProvider(@PathVariable Long id) {
        return ResponseEntity.ok(logisticsProviderService.getLogisticsProviderById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllLogisticsProviders() {
        return ResponseEntity.ok(logisticsProviderService.getAllLogisticsProviders());
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponseDTO> updateLogisticsProvider(@PathVariable Long id,
            @RequestBody LogisticsProviderDto dto) {
        return ResponseEntity.ok(logisticsProviderService.updateLogisticsProvider(id, dto));
    }

    @DeleteMapping
    public ResponseEntity<UserResponseDTO> deleteLogisticsProvider(@PathVariable Long id) {
        logisticsProviderService.deleteLogisticsProvider(id);
        return ResponseEntity.ok().build();
    }
}

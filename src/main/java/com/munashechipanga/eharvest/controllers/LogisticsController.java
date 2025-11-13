package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.LogisticsRequestDto;
import com.munashechipanga.eharvest.services.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/logistics")
public class LogisticsController {

    @Autowired
    LogisticsService logisticsService;

    @GetMapping
    public ResponseEntity<List<LogisticsRequestDto>> getAllLogisticsRequests() {
        return ResponseEntity.ok(logisticsService.getAllLogisticsProviders());
    }

    @GetMapping("{id}")
    public ResponseEntity<LogisticsRequestDto> getLogisticsRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(logisticsService.getLogisticsRequestById(id));
    }

    @PostMapping
    public ResponseEntity<LogisticsRequestDto>  createLogisticsRequest(@RequestBody LogisticsRequestDto logisticsRequestDto) {
        return ResponseEntity.ok(logisticsService.createLogisticsRequest(logisticsRequestDto));
    }

    @PutMapping("{id}")
    public ResponseEntity<LogisticsRequestDto> updateLogisticsRequest(@PathVariable Long id, @RequestBody LogisticsRequestDto logisticsRequestDto) {
        return ResponseEntity.ok(logisticsService.updateLogisticsRequest(id, logisticsRequestDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<LogisticsRequestDto> deleteLogisticsRequest(@PathVariable Long id) {
        logisticsService.deleteLogisticsRequest(id);
        return ResponseEntity.ok().build();
    }
}

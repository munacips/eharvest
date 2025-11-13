package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.ProduceDto;
import com.munashechipanga.eharvest.services.ProduceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/produce")
public class ProduceController {
    @Autowired
    private ProduceService produceService;

    @GetMapping("{id}")
    public ResponseEntity<ProduceDto> getProduceById(@PathVariable long id){
        return ResponseEntity.ok(produceService.getProduce(id));
    }

    @GetMapping
    public ResponseEntity<List<ProduceDto>> getAllProduce(){
        return ResponseEntity.ok(produceService.getAllProduce());
    }

    @PostMapping
    public ResponseEntity<ProduceDto> addProduce(@RequestBody ProduceDto produceDto){
        return ResponseEntity.ok(produceService.createProduce(produceDto));
    }

    @PutMapping("{id}")
    public ResponseEntity<ProduceDto> updateProduce(@PathVariable Long id, @RequestBody ProduceDto produceDto){
        return ResponseEntity.ok(produceService.updateProduce(id, produceDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ProduceDto> deleteProduce(@PathVariable Long id){
        produceService.deleteProduce(id);
        return ResponseEntity.ok().build();
    }
}

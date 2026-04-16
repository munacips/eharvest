package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.ProduceDto;
import com.munashechipanga.eharvest.dtos.ProduceFilter;
import com.munashechipanga.eharvest.dtos.request.CreateProduceDTO;
import com.munashechipanga.eharvest.dtos.response.ProduceResponseDTO;
import com.munashechipanga.eharvest.services.ProduceService;
import com.munashechipanga.eharvest.services.FileStorageService;
import com.munashechipanga.eharvest.utils.PagingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/produce")
public class ProduceController {
    @Autowired
    private ProduceService produceService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("{id}")
    public ResponseEntity<ProduceDto> getProduceById(@PathVariable long id){
        return ResponseEntity.ok(produceService.getProduce(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProduceDto>> search(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String qualityGrade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "harvestDate,desc") String sort
    ){
        Sort sortSpec = PagingUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        ProduceFilter filter = new ProduceFilter();
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setCategory(category);
        filter.setName(name);
        filter.setQualityGrade(qualityGrade);
        filter.setHarvestFrom(harvestFrom);
        filter.setHarvestTo(harvestTo);
        filter.setSearch(search);

        return ResponseEntity.ok(produceService.search(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<ProduceDto> addProduce(@RequestBody CreateProduceDTO produceDto){
        return ResponseEntity.ok(produceService.createProduce(produceDto));
    }

    @PutMapping("{id}")
    public ResponseEntity<ProduceDto> updateProduce(@PathVariable Long id, @RequestBody CreateProduceDTO produceDto){
        return ResponseEntity.ok(produceService.updateProduce(id, produceDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ProduceDto> deleteProduce(@PathVariable Long id){
        produceService.deleteProduce(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/images")
    public ResponseEntity<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(fileStorageService.storeAll(files));
    }

    @PostMapping("{id}/images")
    public ResponseEntity<ProduceDto> uploadAndAttachImages(@PathVariable Long id,
                                                            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = fileStorageService.storeAll(files);
        return ResponseEntity.ok(produceService.addProduceImages(id, urls));
    }
}

//POST /api/v1/produce
//Content-Type: application/json
//
//{
//    "name": "Tomatoes",
//        "category": "Vegetables",
//        "description": "Fresh and ripe",
//        "qualityGrade": "A",
//        "quantity": 50,
//        "price": 30.0,
//        "harvestDate": "2026-02-01",
//        "availableFrom": "2026-02-02",
//        "imageUrls": [
//    "https://cdn.example.com/imgs/tomatoes-1.jpg",
//            "https://cdn.example.com/imgs/tomatoes-2.jpg"
//  ]
//}


//PUT /api/v1/produce/123
//Content-Type: application/json
//
//{
//    "price": 28.0,
//        "imageUrls": [
//    "https://cdn.example.com/imgs/tomatoes-1.jpg",
//            "https://cdn.example.com/imgs/tomatoes-3.jpg"
//  ]
//}

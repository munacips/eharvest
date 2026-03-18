package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.VehicleDto;
import com.munashechipanga.eharvest.dtos.VehicleFilter;
import com.munashechipanga.eharvest.services.VehicleService;
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
@RequestMapping("api/v1/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("{id}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleDto>> search(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String colour,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        Sort sortSpec = PagingUtils.parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        VehicleFilter filter = new VehicleFilter();
        filter.setType(type);
        filter.setColour(colour);
        filter.setPlateNumber(plateNumber);
        filter.setOwnerId(ownerId);
        filter.setSearch(search);

        return ResponseEntity.ok(vehicleService.search(filter, pageable));
    }

    @PutMapping("{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable Long id, @RequestBody VehicleDto dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<VehicleDto> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<VehicleDto> createVehicle(@RequestBody VehicleDto dto) {
        return ResponseEntity.ok(vehicleService.createVehicle(dto));
    }
}


//POST /api/v1/vehicles/{vehicleId}/images
//Body: { "imageUrl": "https://..." }
//
//DELETE /api/v1/vehicles/{vehicleId}/images/{imageId}
package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ProduceDto;
import com.munashechipanga.eharvest.entities.Produce;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.ProduceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProduceServiceImpl implements ProduceService {

    @Autowired
    private ProduceRepository produceRepository;

    @Override
    public ProduceDto createProduce(ProduceDto dto) {

        Produce produce = new Produce();

        produce.setCategory(dto.getCategory());
        produce.setPrice(dto.getPrice());
        produce.setQuantity(dto.getQuantity());
        produce.setDescription(dto.getDescription());
        produce.setName(dto.getName());
        produce.setFarmer(dto.getFarmer());
        produce.setHarvestDate(dto.getHarvestDate());
        produce.setAvailableFrom(produce.getHarvestDate());
        produce.setQualityGrade(dto.getQualityGrade());

        Produce newProduce = produceRepository.save(produce);

        return mapToDto(newProduce);
    }

    @Override
    public ProduceDto updateProduce(Long id, ProduceDto dto) {
        Produce produce = produceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce not found"));

        if(dto.getCategory() != null) produce.setCategory(dto.getCategory());
        if(dto.getDescription() != null) produce.setDescription(dto.getDescription());
        if(dto.getName() != null) produce.setName(dto.getName());
        if(dto.getFarmer() != null) produce.setFarmer(dto.getFarmer());
        if(dto.getHarvestDate() != null) produce.setHarvestDate(dto.getHarvestDate());
        if(dto.getAvailableFrom() != null) produce.setAvailableFrom(dto.getAvailableFrom());
        if(dto.getQualityGrade() != null) produce.setQualityGrade(dto.getQualityGrade());
        if(dto.getQuantity() != null) produce.setQuantity(dto.getQuantity());

        return null;
    }

    @Override
    public void deleteProduce(Long id) {
        produceRepository.deleteById(id);
    }

    @Override
    public ProduceDto getProduce(Long id) {
        Produce produce = produceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce not found"));
        return mapToDto(produce);
    }

    @Override
    public List<ProduceDto> getAllProduce() {
        return produceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProduceDto mapToDto(Produce produce) {
         ProduceDto dto = new ProduceDto();
         dto.setCategory(produce.getCategory());
         dto.setPrice(produce.getPrice());
         dto.setQuantity(produce.getQuantity());
         dto.setDescription(produce.getDescription());
         dto.setName(produce.getName());
         dto.setFarmer(produce.getFarmer());
         dto.setId(produce.getId());
         dto.setAvailableFrom(produce.getAvailableFrom());
         dto.setQualityGrade(produce.getQualityGrade());
         dto.setHarvestDate(produce.getHarvestDate());

         return dto;
    }
}

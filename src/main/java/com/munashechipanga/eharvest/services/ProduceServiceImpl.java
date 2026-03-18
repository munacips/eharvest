package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ProduceDto;
import com.munashechipanga.eharvest.dtos.ProduceFilter;
import com.munashechipanga.eharvest.dtos.request.CreateProduceDTO;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.entities.Produce;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.ProduceRepository;
import com.munashechipanga.eharvest.repositories.FarmerRepository;
import com.munashechipanga.eharvest.specs.ProduceSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.munashechipanga.eharvest.entities.ProduceImage;

@Service
public class ProduceServiceImpl implements ProduceService {

    @Autowired
    private ProduceRepository produceRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Override
    public ProduceDto createProduce(CreateProduceDTO dto) {

        Produce produce = new Produce();

        produce.setCategory(dto.getCategory());
        produce.setPrice(dto.getPrice());
        produce.setQuantity(dto.getQuantity());
        produce.setDescription(dto.getDescription());
        produce.setName(dto.getName());
        produce.setFarmer(getFarmerById(dto.getFarmer()));
        produce.setHarvestDate(dto.getHarvestDate());
        produce.setAvailableFrom(produce.getHarvestDate());
        produce.setQualityGrade(dto.getQualityGrade());

        // Map incoming image URLs to ProduceImage entities
        applyProduceImagesFromDto(produce, dto);

        Produce newProduce = produceRepository.save(produce);

        return mapToDto(newProduce);
    }

    @Override
    public ProduceDto updateProduce(Long id, CreateProduceDTO dto) {
        Produce produce = produceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produce not found"));

        if(dto.getCategory() != null) produce.setCategory(dto.getCategory());
        if(dto.getDescription() != null) produce.setDescription(dto.getDescription());
        if(dto.getName() != null) produce.setName(dto.getName());
        if(dto.getFarmer() != null) produce.setFarmer(getFarmerById(dto.getFarmer()));
        if(dto.getHarvestDate() != null) produce.setHarvestDate(dto.getHarvestDate());
        if(dto.getAvailableFrom() != null) produce.setAvailableFrom(dto.getAvailableFrom());
        if(dto.getQualityGrade() != null) produce.setQualityGrade(dto.getQualityGrade());
        if(dto.getQuantity() != null) produce.setQuantity(dto.getQuantity());
        if(dto.getPrice() != null) produce.setPrice(dto.getPrice());

        // If imageUrls provided, replace images accordingly; if null, leave unchanged
        applyProduceImagesFromDto(produce, dto);
        Produce saved = produceRepository.save(produce);
        return mapToDto(saved);
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

    @Override
    public Page<ProduceDto> search(ProduceFilter filter, Pageable pageable) {
        return produceRepository.findAll(ProduceSpecifications.withFilters(filter), pageable)
                .map(this::mapToDto);
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
         if (produce.getImages() != null) {
             dto.setImageUrls(produce.getImages().stream()
                     .map(ProduceImage::getImageUrl)
                     .collect(Collectors.toList()));
         }
         return dto;
    }

    private void applyProduceImagesFromDto(Produce produce, CreateProduceDTO dto) {
        if (dto.getImageUrls() == null) return;

        produce.getImages().clear();
        for (String url : dto.getImageUrls()) {
            ProduceImage pi = new ProduceImage();
            pi.setImageUrl(url);
            pi.setProduce(produce);
            produce.getImages().add(pi);
        }
    }
    
    private Farmer getFarmerById(Long id){
        return farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with id: " + id));
    }
}

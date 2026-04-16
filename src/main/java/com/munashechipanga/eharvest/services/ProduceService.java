package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ProduceDto;
import com.munashechipanga.eharvest.dtos.ProduceFilter;
import com.munashechipanga.eharvest.dtos.request.CreateProduceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProduceService {
    ProduceDto createProduce(CreateProduceDTO dto);
    ProduceDto updateProduce(Long id, CreateProduceDTO dto);
    void deleteProduce(Long id);
    ProduceDto getProduce(Long id);
    List<ProduceDto> getAllProduce();

    Page<ProduceDto> search(ProduceFilter filter, Pageable pageable);

    ProduceDto addProduceImages(Long id, List<String> imageUrls);
}

package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.ProduceDto;

import java.util.List;

public interface ProduceService {
    ProduceDto createProduce(ProduceDto dto);
    ProduceDto updateProduce(Long id, ProduceDto dto);
    void deleteProduce(Long id);
    ProduceDto getProduce(Long id);
    List<ProduceDto> getAllProduce();
}

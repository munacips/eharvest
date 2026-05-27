package com.munashechipanga.eharvest.heatmap;

import com.munashechipanga.eharvest.entities.Produce;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HeatmapService {

    private final HeatmapRepository heatmapRepository;

    public HeatmapService(HeatmapRepository heatmapRepository) {
        this.heatmapRepository = heatmapRepository;
    }

    private String normalizeCrop(String crop) {
        if (crop == null) {
            return null;
        }
        return crop.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public HeatmapResponseDto getSupplyHeatmap(String crop) {
        String normalizedCrop = normalizeCrop(crop);
        if (normalizedCrop == null || normalizedCrop.isBlank()) {
            return new HeatmapResponseDto("", Collections.emptyList());
        }

        List<Produce> produceList = heatmapRepository.findByCrop(normalizedCrop);
        List<HeatmapPointDto> points = new ArrayList<>();

        for (Produce produce : produceList) {
            Double latitude = produce.getLatitude();
            Double longitude = produce.getLongitude();

            if (latitude == null || longitude == null) {
                if (produce.getFarmer() != null && produce.getFarmer().getId() != null) {
                    List<Produce> cityFallback = heatmapRepository.findFallbackByFarmerAndCity(
                            produce.getFarmer().getId(),
                            produce.getCityTown()
                    );

                    if (!cityFallback.isEmpty()) {
                        Produce fallback = cityFallback.get(0);
                        latitude = fallback.getLatitude();
                        longitude = fallback.getLongitude();
                    }

                    if (latitude == null || longitude == null) {
                        List<Produce> cropFallback = heatmapRepository.findFallbackByFarmerAndCrop(
                                produce.getFarmer().getId(),
                                normalizedCrop
                        );

                        if (!cropFallback.isEmpty()) {
                            Produce fallback = cropFallback.get(0);
                            latitude = fallback.getLatitude();
                            longitude = fallback.getLongitude();
                        }
                    }
                }
            }

            if (latitude == null || longitude == null) {
                continue;
            }

            points.add(new HeatmapPointDto(
                    latitude,
                    longitude,
                    produce.getQuantity() == null ? 0.0 : produce.getQuantity()
            ));
        }

        return new HeatmapResponseDto(normalizedCrop, points);
    }
}
package com.munashechipanga.eharvest.heatmap;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HeatmapService {

    private final HeatmapRepository heatmapRepository;

    public HeatmapService(HeatmapRepository heatmapRepository) {
        this.heatmapRepository = heatmapRepository;
    }

    public HeatmapResponseDto getSupplyHeatmap(String crop) {
        List<HeatmapPointDto> points = heatmapRepository.findSupplyHeatmapByCrop(crop.toLowerCase());
        double maxKg = points.stream().mapToDouble(HeatmapPointDto::totalKg).max().orElse(1.0);

        List<HeatmapPointDto> normalizedPoints = points.stream()
                .map(point -> new HeatmapPointDto(
                        point.city(),
                        point.latitude(),
                        point.longitude(),
                        point.totalKg(),
                        point.listingCount(),
                        point.totalKg() == 0.0 ? 0.0 : Math.sqrt(point.totalKg() / maxKg)
                ))
                .toList();

        return new HeatmapResponseDto(crop, maxKg, normalizedPoints);
    }
}

package com.munashechipanga.eharvest.heatmap;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HeatmapService {

    private final HeatmapRepository heatmapRepository;

    public HeatmapService(HeatmapRepository heatmapRepository) {
        this.heatmapRepository = heatmapRepository;
    }

    private String normalizeCrop(String crop) {
        if (crop == null)
            return null;
        // Trim leading/trailing spaces, collapse multiple internal spaces, and
        // lowercase
        return crop.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public HeatmapResponseDto getSupplyHeatmap(String crop) {
        String normalizedCrop = normalizeCrop(crop);
        if (normalizedCrop == null || normalizedCrop.isBlank()) {
            return new HeatmapResponseDto("", 0.0, Collections.emptyList());
        }

        List<HeatmapPointDto> points = heatmapRepository.findSupplyHeatmapByCrop(normalizedCrop);
        double maxKg = points.stream().mapToDouble(HeatmapPointDto::totalKg).max().orElse(1.0);

        List<HeatmapPointDto> normalizedPoints = points.stream()
                .map(point -> new HeatmapPointDto(
                        point.city(),
                        point.latitude(),
                        point.longitude(),
                        point.totalKg(),
                        point.listingCount(),
                        point.totalKg() == 0.0 ? 0.0 : Math.sqrt(point.totalKg() / maxKg)))
                .toList();

        return new HeatmapResponseDto(normalizedCrop, maxKg, normalizedPoints);
    }
}

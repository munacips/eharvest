package com.munashechipanga.eharvest.heatmap;

import java.util.List;

public record HeatmapResponseDto(
        String crop,
        double maxKg,
        List<HeatmapPointDto> points
) {
}

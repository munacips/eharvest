package com.munashechipanga.eharvest.heatmap;

import java.util.List;

public record HeatmapResponseDto(
        String crop,
        List<HeatmapPointDto> points
) {
}
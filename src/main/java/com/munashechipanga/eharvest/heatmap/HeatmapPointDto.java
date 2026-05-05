package com.munashechipanga.eharvest.heatmap;

public record HeatmapPointDto(
        String city,
        double latitude,
        double longitude,
        double totalKg,
        int listingCount,
        double normalizedWeight
) {
}

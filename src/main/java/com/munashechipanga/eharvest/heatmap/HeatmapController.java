package com.munashechipanga.eharvest.heatmap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/heatmap")
public class HeatmapController {

    private final HeatmapService heatmapService;

    public HeatmapController(HeatmapService heatmapService) {
        this.heatmapService = heatmapService;
    }

    @GetMapping("/supply")
    public ResponseEntity<HeatmapResponseDto> getSupplyHeatmap(@RequestParam String crop) {
        return ResponseEntity.ok(heatmapService.getSupplyHeatmap(crop));
    }
}

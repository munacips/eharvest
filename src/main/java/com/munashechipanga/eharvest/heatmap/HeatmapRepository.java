package com.munashechipanga.eharvest.heatmap;

import com.munashechipanga.eharvest.market.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeatmapRepository extends JpaRepository<Market, Long> {

    @Query("""
            select new com.munashechipanga.eharvest.heatmap.HeatmapPointDto(
                m.city,
                m.latitude,
                m.longitude,
                coalesce(sum(p.quantity), 0.0),
                cast(count(p.id) as integer),
                0.0
            )
            from Market m
            left join Produce p on p.market = m and lower(p.name) = lower(:crop)
            group by m.city, m.latitude, m.longitude
            """)
    List<HeatmapPointDto> findSupplyHeatmapByCrop(@Param("crop") String crop);
}

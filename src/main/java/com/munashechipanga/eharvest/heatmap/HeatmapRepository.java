package com.munashechipanga.eharvest.heatmap;

import com.munashechipanga.eharvest.entities.Produce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeatmapRepository extends JpaRepository<Produce, Long> {

    @Query("""
            select p
            from Produce p
            where lower(p.name) = lower(:crop)
            """)
    List<Produce> findByCrop(@Param("crop") String crop);

    @Query("""
            select p
            from Produce p
            where p.farmer.id = :farmerId
              and p.cityTown = :cityTown
              and p.latitude is not null
              and p.longitude is not null
            order by p.id asc
            """)
    List<Produce> findFallbackByFarmerAndCity(
            @Param("farmerId") Long farmerId,
            @Param("cityTown") String cityTown
    );

    @Query("""
            select p
            from Produce p
            where p.farmer.id = :farmerId
              and lower(p.name) = lower(:crop)
              and p.latitude is not null
              and p.longitude is not null
            order by p.id asc
            """)
    List<Produce> findFallbackByFarmerAndCrop(
            @Param("farmerId") Long farmerId,
            @Param("crop") String crop
    );
}
package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.LogisticsRequest;
import com.munashechipanga.eharvest.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LogisticsRepository extends JpaRepository<LogisticsRequest,Long>, JpaSpecificationExecutor<LogisticsRequest> {
    Optional<LogisticsRequest> findByOrder_Id(Long orderId);

    @Query("""
            select lr
            from LogisticsRequest lr
            left join fetch lr.order o
            left join fetch lr.assignedProvider
            left join fetch o.buyer
            left join fetch o.farmer
            where o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<LogisticsRequest> findAllForReportBetween(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);

    @Query("""
            select lr
            from LogisticsRequest lr
            left join fetch lr.order o
            left join fetch lr.assignedProvider
            left join fetch o.buyer
            left join fetch o.farmer
            where o.buyer.id = :buyerId
              and o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<LogisticsRequest> findAllForBuyerReportBetween(@Param("buyerId") Long buyerId,
                                                        @Param("from") LocalDateTime from,
                                                        @Param("to") LocalDateTime to);

    @Query("""
            select lr
            from LogisticsRequest lr
            left join fetch lr.order o
            left join fetch lr.assignedProvider
            left join fetch o.buyer
            left join fetch o.farmer
            where lr.assignedProvider.id = :providerId
              and o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<LogisticsRequest> findAllForProviderReportBetween(@Param("providerId") Long providerId,
                                                           @Param("from") LocalDateTime from,
                                                           @Param("to") LocalDateTime to);
}

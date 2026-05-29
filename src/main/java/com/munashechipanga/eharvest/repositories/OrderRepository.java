package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {
    // Find all orders linked to a given farmer by their ID
    java.util.List<Order> findByFarmer_Id(Long farmerId);
    java.util.List<Order> findByBuyer_Id(Long buyerId);

    boolean existsByBuyer_IdAndFarmer_IdAndStatus(Long buyerId, Long farmerId, String status);

    @Query("""
            select count(o) > 0
            from Order o
            where o.status = :status
              and ((o.buyer.id = :userId and o.farmer.id = :otherId)
                   or (o.buyer.id = :otherId and o.farmer.id = :userId))
            """)
    boolean existsDeliveredBetweenBuyerFarmer(@Param("userId") Long userId,
                                              @Param("otherId") Long otherId,
                                              @Param("status") String status);

    @Query("""
            select count(o) > 0
            from Order o
            join o.logisticsRequest lr
            where o.status = :status
              and lr.assignedProvider.id = :providerId
              and (o.buyer.id = :userId or o.farmer.id = :userId)
            """)
    boolean existsDeliveredWithProvider(@Param("userId") Long userId,
                                        @Param("providerId") Long providerId,
                                        @Param("status") String status);

    @Query("""
            select o
            from Order o
            left join fetch o.buyer
            left join fetch o.farmer
            left join fetch o.logisticsRequest
            where o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<Order> findAllForReportBetween(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);

    @Query("""
            select o
            from Order o
            left join fetch o.buyer
            left join fetch o.farmer
            left join fetch o.logisticsRequest
            where o.buyer.id = :buyerId
              and o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<Order> findAllForBuyerReportBetween(@Param("buyerId") Long buyerId,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query("""
            select o
            from Order o
            left join fetch o.buyer
            left join fetch o.farmer
            left join fetch o.logisticsRequest
            where o.farmer.id = :farmerId
              and o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<Order> findAllForFarmerReportBetween(@Param("farmerId") Long farmerId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}

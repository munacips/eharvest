package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

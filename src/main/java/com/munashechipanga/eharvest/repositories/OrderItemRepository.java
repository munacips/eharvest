package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long>, JpaSpecificationExecutor<OrderItem> {
    java.util.List<OrderItem> findByOrder_Id(Long orderId);

    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            join fetch oi.produce p
            left join fetch p.farmer
            where o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<OrderItem> findAllForReportBetween(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            join fetch oi.produce p
            left join fetch p.farmer
            where o.buyer.id = :buyerId
              and o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<OrderItem> findAllForBuyerReportBetween(@Param("buyerId") Long buyerId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            join fetch oi.produce p
            left join fetch p.farmer
            where p.farmer.id = :farmerId
              and o.orderDate between :from and :to
            order by o.orderDate asc
            """)
    List<OrderItem> findAllForFarmerReportBetween(@Param("farmerId") Long farmerId,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);
}

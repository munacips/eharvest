package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<TransactionHistory,Long>, JpaSpecificationExecutor<TransactionHistory> {
    @Query("""
            select t
            from TransactionHistory t
            left join fetch t.order
            left join fetch t.buyer
            left join fetch t.farmer
            where t.transactionDate between :from and :to
            order by t.transactionDate asc
            """)
    List<TransactionHistory> findAllForReportBetween(@Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);

    @Query("""
            select t
            from TransactionHistory t
            left join fetch t.order
            left join fetch t.buyer
            left join fetch t.farmer
            where t.buyer.id = :buyerId
              and t.transactionDate between :from and :to
            order by t.transactionDate asc
            """)
    List<TransactionHistory> findAllForBuyerReportBetween(@Param("buyerId") Long buyerId,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);
}

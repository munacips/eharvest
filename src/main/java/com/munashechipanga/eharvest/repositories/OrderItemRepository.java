package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long>, JpaSpecificationExecutor<OrderItem> {
    java.util.List<OrderItem> findByOrder_Id(Long orderId);
}

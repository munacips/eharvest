package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
}

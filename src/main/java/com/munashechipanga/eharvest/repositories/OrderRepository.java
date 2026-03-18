package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {
    // Find all orders linked to a given farmer by their ID
    java.util.List<Order> findByFarmer_Id(Long farmerId);
    java.util.List<Order> findByBuyer_Id(Long buyerId);
}

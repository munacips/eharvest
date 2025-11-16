package com.munashechipanga.eharvest.dtos;

import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.Produce;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private Long id;
    private Double price;
    private Integer quantity;
    private Produce produce;
    private Order order;
}

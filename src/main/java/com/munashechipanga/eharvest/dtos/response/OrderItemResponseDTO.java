package com.munashechipanga.eharvest.dtos.response;

import com.munashechipanga.eharvest.entities.Order;
import com.munashechipanga.eharvest.entities.Produce;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDTO {
    private Long id;
    private Double price;
    private Integer quantity;
    private Produce produce;
    private Order order;
}

package com.munashechipanga.eharvest.dtos.response;

import com.munashechipanga.eharvest.dtos.SubscriptionItemDto;
import com.munashechipanga.eharvest.entities.Buyer;
import com.munashechipanga.eharvest.entities.Farmer;
import com.munashechipanga.eharvest.enums.Currency;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SubscriptionResponseDTO {
    private Long id;
    private Long buyerId;
    private Long farmerId;
    private String frequency;
    private String status;
    private Boolean requiresLogistics;
    private String pickupAddress;
    private Currency currency;
    private LocalDateTime startDate;
    private Buyer buyer;
    private Farmer farmer;
    private LocalDateTime nextDeliveryDate;
    private List<SubscriptionItemDto> items;
    private Double totalAmount;
}

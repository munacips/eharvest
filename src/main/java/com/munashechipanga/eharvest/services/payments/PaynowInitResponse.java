package com.munashechipanga.eharvest.services.payments;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaynowInitResponse {
    private String providerReference;
    private String redirectUrl;
    private String pollUrl;
}

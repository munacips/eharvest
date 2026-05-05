package com.munashechipanga.eharvest.market;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MarketDataSeeder implements CommandLineRunner {

    private final MarketRepository marketRepository;

    public MarketDataSeeder(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Override
    public void run(String... args) {
        seedMarket("Harare", -17.8252, 31.0335);
        seedMarket("Bulawayo", -20.1325, 28.5873);
        seedMarket("Mutare", -18.9707, 32.6709);
        seedMarket("Gweru", -19.4500, 29.8167);
        seedMarket("Masvingo", -20.0667, 30.8333);
        seedMarket("Hwange", -18.3629, 26.4997);
        seedMarket("Chinhoyi", -17.3667, 30.2000);
        seedMarket("Bindura", -17.3000, 31.3333);
        seedMarket("Marondera", -18.1833, 31.5500);
        seedMarket("Chiredzi", -21.0500, 31.6667);
    }

    private void seedMarket(String city, Double latitude, Double longitude) {
        if (marketRepository.existsByCity(city)) {
            return;
        }

        Market market = new Market();
        market.setCity(city);
        market.setLatitude(latitude);
        market.setLongitude(longitude);
        marketRepository.save(market);
    }
}

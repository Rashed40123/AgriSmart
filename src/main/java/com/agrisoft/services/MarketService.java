package com.agrisoft.services;

import java.util.HashMap;
import java.util.Map;

public class MarketService {
    private Map<String, Integer> marketPrices;

    public MarketService() {
        marketPrices = new HashMap<>();
        marketPrices.put("Rice (Aman)", 30000);
        marketPrices.put("Rice (Boro)", 35000);
        marketPrices.put("Rice (Aus)", 28000);
        marketPrices.put("Wheat", 32000);
        marketPrices.put("Jute", 40000);
        marketPrices.put("Maize", 25000);
        marketPrices.put("Potato", 20000);
        marketPrices.put("Onion", 30000);
        marketPrices.put("Garlic", 60000);
        marketPrices.put("Tomato", 25000);
        marketPrices.put("Chili", 50000);
        marketPrices.put("Banana", 20000);
        marketPrices.put("Mango", 40000);
    }

    public String getPrice(String cropName) {
        Integer price = marketPrices.get(cropName);
        return price != null ? String.format("%,d BDT/ton", price) : "Not available";
    }

    public Map<String, Integer> getAllPrices() {
        return marketPrices;
    }

    public int getPriceValue(String cropName) {
        return marketPrices.getOrDefault(cropName, 0);
    }
}
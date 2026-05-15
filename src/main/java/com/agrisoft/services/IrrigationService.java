package com.agrisoft.services;

public class IrrigationService {
    public String recommendIrrigation(String cropName, String waterAvailability) {
        if ("Low".equalsIgnoreCase(waterAvailability)) {
            return "Drip irrigation, consider mulching to conserve water";
        } else if ("Rice".equalsIgnoreCase(cropName)) {
            return "Flood irrigation, 5-10cm depth for optimal rice growth";
        } else if ("High".equalsIgnoreCase(waterAvailability)) {
            return "Sprinkler irrigation for efficient water distribution";
        } else {
            return "Furrow irrigation, every 7-10 days depending on soil type";
        }
    }
}
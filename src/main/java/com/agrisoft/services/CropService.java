package com.agrisoft.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CropService {
    private Map<String, Map<String, Object>> crops;

    public CropService() {
        loadCrops();
    }

    private void loadCrops() {
        crops = new HashMap<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/bangladesh_crops.csv")))) {
            List<String[]> lines = reader.readAll();
            // Skip header
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i);
                if (line.length >= 6) {
                    String cropName = line[0].trim();
                    Map<String, Object> cropData = new HashMap<>();
                    String season = line[1].trim();
                    String soilType = line[2].trim();
                    String suitableRegions = line[3].trim();
                    String plantingTime = line[4].trim();

                    cropData.put("season", season);
                    cropData.put("soil_type", soilType);
                    cropData.put("suitable_regions", suitableRegions);
                    cropData.put("planting_time", plantingTime);
                    try {
                        double yieldPerAcre = Double.parseDouble(line[5].trim());
                        cropData.put("yield_per_hectare", yieldPerAcre * 2.47105); // Convert to per hectare
                    } catch (NumberFormatException e) {
                        cropData.put("yield_per_hectare", 5.0);
                    }
                    cropData.put("water_need", "High".equalsIgnoreCase(season) ? "High" : "Medium");

                    crops.put(cropName, cropData);
                }
            }
            System.out.println("Loaded " + crops.size() + " crops");
        } catch (IOException | CsvException e) {
            System.err.println("Error loading crops: " + e.getMessage());
            crops = new HashMap<>();
        }
    }

    public Map<String, Object> getCropInfo(String cropName) {
        return crops.getOrDefault(cropName, new HashMap<>());
    }

    public List<String> recommendCrop(String soilType, String season) {
        String normalizedSoil = normalize(soilType);
        String normalizedSeason = normalize(season);

        Set<String> recommendations = new LinkedHashSet<>();
        for (Map.Entry<String, Map<String, Object>> entry : crops.entrySet()) {
            Map<String, Object> cropData = entry.getValue();
            String cropSoil = normalize((String) cropData.get("soil_type"));
            String cropSeason = normalize((String) cropData.get("season"));

            if (normalizedSoil.equals(cropSoil) && normalizedSeason.equals(cropSeason)) {
                recommendations.add(entry.getKey());
            }
        }

        if (recommendations.isEmpty()) {
            for (Map.Entry<String, Map<String, Object>> entry : crops.entrySet()) {
                Map<String, Object> cropData = entry.getValue();
                String cropSoil = normalize((String) cropData.get("soil_type"));
                if (normalizedSoil.equals(cropSoil)) {
                    recommendations.add(entry.getKey());
                }
            }
        }

        if (recommendations.isEmpty()) {
            for (Map.Entry<String, Map<String, Object>> entry : crops.entrySet()) {
                Map<String, Object> cropData = entry.getValue();
                String cropSeason = normalize((String) cropData.get("season"));
                if (normalizedSeason.equals(cropSeason)) {
                    recommendations.add(entry.getKey());
                }
            }
        }

        return recommendations.isEmpty()
            ? Collections.singletonList("No suitable crops")
            : new ArrayList<>(recommendations);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public Map<String, Map<String, Object>> getAllCrops() {
        return crops;
    }
}
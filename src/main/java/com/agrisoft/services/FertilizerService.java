package com.agrisoft.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class FertilizerService {
    private Map<String, Map<String, Double>> fertilizers;

    public FertilizerService() {
        loadFertilizerData();
    }

    private void loadFertilizerData() {
        fertilizers = new HashMap<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/fertilizer_data.csv")))) {
            List<String[]> lines = reader.readAll();
            // Skip header
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i);
                if (line.length >= 6) {
                    String cropName = line[0];
                    Map<String, Double> fertData = new HashMap<>();
                    try {
                        fertData.put("nitrogen", Double.parseDouble(line[1]));
                        fertData.put("phosphorus", Double.parseDouble(line[2]));
                        fertData.put("potassium", Double.parseDouble(line[3]));
                        fertData.put("organic_matter", Double.parseDouble(line[4]));
                        fertData.put("lime", Double.parseDouble(line[5]));
                    } catch (NumberFormatException e) {
                        // Use defaults if parsing fails
                        fertData.put("nitrogen", 0.0);
                        fertData.put("phosphorus", 0.0);
                        fertData.put("potassium", 0.0);
                        fertData.put("organic_matter", 0.0);
                        fertData.put("lime", 0.0);
                    }
                    fertilizers.put(cropName, fertData);
                }
            }
            System.out.println("Loaded fertilizer data for " + fertilizers.size() + " crops");
        } catch (IOException | CsvException e) {
            System.err.println("Error loading fertilizer data: " + e.getMessage());
            fertilizers = new HashMap<>();
        }
    }

    public String recommendFertilizer(String cropName) {
        Map<String, Double> fertData = fertilizers.get(cropName);
        if (fertData == null) {
            return "No fertilizer recommendation available for " + cropName;
        }

        return String.format("""
            Recommended for %s:
            • Nitrogen: %.0f kg/acre
            • Phosphorus: %.0f kg/acre
            • Potassium: %.0f kg/acre
            • Organic Matter: %.0f tons/acre
            • Lime: %.0f kg/acre
            """, cropName,
            fertData.get("nitrogen"), fertData.get("phosphorus"),
            fertData.get("potassium"), fertData.get("organic_matter"),
            fertData.get("lime"));
    }

    public Map<String, Double> getFertilizerData(String cropName) {
        return fertilizers.get(cropName);
    }

    public Map<String, Map<String, Double>> getAllFertilizerData() {
        return fertilizers;
    }
}
package com.agrisoft.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PestService {
    private List<Map<String, String>> pestData;

    public PestService() {
        loadPestData();
    }

    private void loadPestData() {
        pestData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/pest_disease_data.csv")))) {
            List<String[]> lines = reader.readAll();
            // Skip header
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i);
                if (line.length >= 8) {
                    Map<String, String> pest = new HashMap<>();
                    pest.put("crop", line[0]);
                    pest.put("pest_disease", line[1]);
                    pest.put("type", line[2]);
                    pest.put("favorable_temp", line[3]);
                    pest.put("favorable_humidity", line[4]);
                    pest.put("favorable_rainfall", line[5]);
                    pest.put("vulnerable_stage", line[6]);
                    pest.put("severity", line[7]);
                    // Add a control measure based on type
                    if ("Pest".equals(line[2])) {
                        pest.put("control_measure", "Use appropriate pesticides and integrated pest management practices");
                    } else {
                        pest.put("control_measure", "Use fungicides and follow proper crop rotation practices");
                    }
                    pestData.add(pest);
                }
            }
            System.out.println("Loaded " + pestData.size() + " pest/disease records");
        } catch (IOException | CsvException e) {
            System.err.println("Error loading pest data: " + e.getMessage());
            pestData = new ArrayList<>();
        }
    }

    public String identifyPest(String description) {
        if (description == null || description.trim().isEmpty() || pestData.isEmpty()) {
            return "Unknown";
        }

        String lowerDesc = description.toLowerCase();
        for (Map<String, String> pest : pestData) {
            if (pest.get("pest_disease").toLowerCase().contains(lowerDesc) ||
                lowerDesc.contains(pest.get("pest_disease").toLowerCase())) {
                return pest.get("pest_disease");
            }
        }
        return "Unknown";
    }

    public String getControl(String pest) {
        for (Map<String, String> pestInfo : pestData) {
            if (pest.equals(pestInfo.get("pest_disease"))) {
                return pestInfo.get("control_measure");
            }
        }
        return "No control measures available";
    }

    public List<Map<String, String>> getAllPestData() {
        return pestData;
    }
}
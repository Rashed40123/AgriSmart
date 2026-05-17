package com.agrisoft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class WeatherService {
    private static final String API_KEY = "22d7d723318a590098c4bf3ff8112b78 ";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public Map<String, String> getWeather() {
        Map<String, String> weather = new HashMap<>();

        try {
            // Default coordinates for Dhaka, Bangladesh (can be made configurable)
            double lat = 23.8103;
            double lon = 90.4125;

            String url = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                                     BASE_URL, lat, lon, API_KEY);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                // Extract weather data
                JsonNode main = root.get("main");
                JsonNode wind = root.get("wind");
                JsonNode weatherArray = root.get("weather");

                double temp = main.get("temp").asDouble();
                int humidity = main.get("humidity").asInt();
                String description = weatherArray.get(0).get("description").asText();
                double windSpeed = wind.get("speed").asDouble();

                weather.put("Temperature", String.format("%.1f°C", temp));
                weather.put("Humidity", humidity + "%");
                weather.put("Wind Speed", String.format("%.1f km/h", windSpeed * 3.6)); // Convert m/s to km/h
                weather.put("Conditions", description.substring(0, 1).toUpperCase() + description.substring(1));
                weather.put("Location", "Dhaka, Bangladesh");

                // Estimate rainfall based on conditions (simplified)
                if (description.toLowerCase().contains("rain")) {
                    weather.put("Rainfall", "Expected today");
                } else {
                    weather.put("Rainfall", "No rain expected");
                }

            } else {
                // Fallback to hardcoded data if API fails
                weather.put("Temperature", "32°C");
                weather.put("Humidity", "85%");
                weather.put("Wind Speed", "10 km/h");
                weather.put("Conditions", "Partly cloudy");
                weather.put("Rainfall", "Light showers possible");
                weather.put("Location", "Dhaka, Bangladesh");
                if (response.statusCode() == 401) {
                    weather.put("Note", "Using offline data - invalid or unauthorized API key (401)");
                } else {
                    weather.put("Note", "Using offline data - API unavailable (status " + response.statusCode() + ")");
                }
            }

        } catch (IOException | InterruptedException e) {
            // Fallback to hardcoded data if network/API issues
            weather.put("Temperature", "32°C");
            weather.put("Humidity", "85%");
            weather.put("Wind Speed", "10 km/h");
            weather.put("Conditions", "Partly cloudy");
            weather.put("Rainfall", "Light showers possible");
            weather.put("Location", "Dhaka, Bangladesh");
            weather.put("Note", "Using offline data - Network error");
        }

        return weather;
    }
}
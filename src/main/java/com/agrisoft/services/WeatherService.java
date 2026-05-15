package com.agrisoft.services;

import java.util.HashMap;
import java.util.Map;

public class WeatherService {
    public Map<String, String> getWeather() {
        Map<String, String> weather = new HashMap<>();
        weather.put("Temperature", "32°C");
        weather.put("Humidity", "85%");
        weather.put("Rainfall", "15mm");
        weather.put("Wind Speed", "10 km/h");
        weather.put("Forecast", "Heavy monsoon rains expected");
        return weather;
    }
}
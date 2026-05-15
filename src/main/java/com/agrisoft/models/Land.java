package com.agrisoft.models;

public class Land {
    private int id;
    private int userId;
    private String location;
    private double area;
    private String soilType;
    private String gpsCoords;

    public Land() {}

    public Land(int userId, String location, double area, String soilType, String gpsCoords) {
        this.userId = userId;
        this.location = location;
        this.area = area;
        this.soilType = soilType;
        this.gpsCoords = gpsCoords;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getGpsCoords() { return gpsCoords; }
    public void setGpsCoords(String gpsCoords) { this.gpsCoords = gpsCoords; }
}
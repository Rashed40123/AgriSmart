package com.agrisoft.services;

import com.agrisoft.models.Land;
import com.agrisoft.utils.DatabaseManager;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportService {
    private CropService cropService;
    private MarketService marketService;

    public ReportService(CropService cropService, MarketService marketService) {
        this.cropService = cropService;
        this.marketService = marketService;
    }

    public String generateCropReport(int userId) throws SQLException {
        String userName = DatabaseManager.getUserById(userId).getName();
        List<Land> lands = DatabaseManager.getUserLands(userId);

        StringBuilder csvData = new StringBuilder();
        csvData.append("Crop,Season,Soil Type,Yield (t/ha),Price (BDT/ton)\n");

        for (Map.Entry<String, Map<String, Object>> entry : cropService.getAllCrops().entrySet()) {
            String cropName = entry.getKey();
            Map<String, Object> cropData = entry.getValue();

            // Check if this crop is suitable for any of the user's lands
            boolean suitable = false;
            for (Land land : lands) {
                if (land.getSoilType().equalsIgnoreCase((String) cropData.get("soil_type"))) {
                    suitable = true;
                    break;
                }
            }

            if (suitable) {
                csvData.append(String.format("%s,%s,%s,%.2f,%s\n",
                    cropName,
                    cropData.get("season"),
                    cropData.get("soil_type"),
                    (Double) cropData.get("yield_per_hectare"),
                    marketService.getPrice(cropName)
                ));
            }
        }

        return csvData.toString();
    }

    public void generatePDFReport(int userId, String fileName) throws SQLException, FileNotFoundException {
        String userName = DatabaseManager.getUserById(userId).getName();
        List<Land> lands = DatabaseManager.getUserLands(userId);

        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        document.add(new Paragraph("AgriSmart - Agricultural Report")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(18));
        document.add(new Paragraph("User: " + userName));
        document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        document.add(new Paragraph("\n"));

        // Land Information
        document.add(new Paragraph("Land Information").setFontSize(14));
        Table landTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 3}));
        landTable.addHeaderCell("Location");
        landTable.addHeaderCell("Area (ha)");
        landTable.addHeaderCell("Soil Type");
        landTable.addHeaderCell("GPS Coordinates");

        for (Land land : lands) {
            landTable.addCell(land.getLocation());
            landTable.addCell(String.format("%.2f", land.getArea()));
            landTable.addCell(land.getSoilType());
            landTable.addCell(land.getGpsCoords() != null ? land.getGpsCoords() : "N/A");
        }
        document.add(landTable);
        document.add(new Paragraph("\n"));

        // Crop Recommendations
        document.add(new Paragraph("Crop Recommendations").setFontSize(14));
        Table cropTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2}));
        cropTable.addHeaderCell("Crop");
        cropTable.addHeaderCell("Season");
        cropTable.addHeaderCell("Soil Type");
        cropTable.addHeaderCell("Yield (t/ha)");
        cropTable.addHeaderCell("Price (BDT/ton)");

        for (Map.Entry<String, Map<String, Object>> entry : cropService.getAllCrops().entrySet()) {
            String cropName = entry.getKey();
            Map<String, Object> cropData = entry.getValue();

            // Check if suitable for user's lands
            boolean suitable = false;
            for (Land land : lands) {
                if (land.getSoilType().equalsIgnoreCase((String) cropData.get("soil_type"))) {
                    suitable = true;
                    break;
                }
            }

            if (suitable) {
                cropTable.addCell(cropName);
                cropTable.addCell((String) cropData.get("season"));
                cropTable.addCell((String) cropData.get("soil_type"));
                cropTable.addCell(String.format("%.2f", (Double) cropData.get("yield_per_hectare")));
                cropTable.addCell(marketService.getPrice(cropName));
            }
        }
        document.add(cropTable);

        document.close();
    }
}
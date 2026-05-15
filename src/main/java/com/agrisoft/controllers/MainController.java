package com.agrisoft.controllers;

import com.agrisoft.models.Land;
import com.agrisoft.models.User;
import com.agrisoft.services.*;
import com.agrisoft.utils.AnimationHelper;
import com.agrisoft.utils.DatabaseManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainController {
    private User currentUser;
    private Stage primaryStage;

    // Services
    private CropService cropService;
    private PestService pestService;
    private FertilizerService fertilizerService;
    private IrrigationService irrigationService;
    private MarketService marketService;
    private WeatherService weatherService;
    private ReportService reportService;

    // UI Components
    private BorderPane mainLayout;
    private ListView<String> sidebar;
    private VBox contentArea;
    private ScrollPane contentScrollPane;

    public MainController(User user) {
        this.currentUser = user;
        initializeServices();
    }

    private void initializeServices() {
        cropService = new CropService();
        pestService = new PestService();
        fertilizerService = new FertilizerService();
        irrigationService = new IrrigationService();
        marketService = new MarketService();
        weatherService = new WeatherService();
        reportService = new ReportService(cropService, marketService);
    }

    public void showMainWindow(Stage stage) {
        this.primaryStage = stage;
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setTitle("AgriSmart - Smart Agriculture Management");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        createMainLayout();
        createSidebar();
        createContentArea();

        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.setScene(scene);

        // Apply CSS
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.show();

        // Load initial content
        sidebar.getSelectionModel().select(0);
        AnimationHelper.fadeIn(mainLayout);
    }

    private void createMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");
    }

    private void createSidebar() {
        sidebar = new ListView<>();
        sidebar.getItems().addAll(
            "Land Management",
            "Crop Recommendations",
            "Fertilizer Recommendations",
            "Irrigation Advice",
            "Pest Control",
            "Market Prices",
            "Weather Info",
            "Reports"
        );

        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: rgba(0, 106, 78, 0.8); -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 10px; -fx-padding: 5px;");

        sidebar.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                changeSection(newVal.intValue());
            }
        });

        mainLayout.setLeft(sidebar);
    }

    private void createContentArea() {
        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setStyle("-fx-background: transparent; -fx-border: none;");

        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: transparent;");

        contentScrollPane.setContent(contentArea);
        mainLayout.setCenter(contentScrollPane);

        // Header
        HBox header = createHeader();
        mainLayout.setTop(header);

        // Footer
        Label footer = new Label("AgriSmart - Rooted in Heart, Soil, People, and Technology");
        footer.setStyle("-fx-background-color: linear-gradient(to right, #006a4e, #f42a4d, #006a4e); -fx-text-fill: white; -fx-padding: 15px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);
        mainLayout.setBottom(footer);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #f42a4d, #006a4e, #f42a4d); -fx-border-radius: 10px;");

        Button menuToggle = new Button("☰");
        menuToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-border: none;");
        menuToggle.setOnAction(e -> toggleSidebar());

        Label title = new Label("AgriSmart");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button profileBtn = new Button("👤 Profile");
        profileBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 15px;");
        profileBtn.setOnAction(e -> showProfileDialog());

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #f42a4d; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 15px;");
        logoutBtn.setOnAction(e -> logout());

        header.getChildren().addAll(menuToggle, title, spacer, profileBtn, logoutBtn);
        return header;
    }

    private void toggleSidebar() {
        double currentWidth = sidebar.getWidth();
        double targetWidth = (currentWidth > 50) ? 0 : 220;

        sidebar.setPrefWidth(targetWidth);
        if (targetWidth == 0) {
            sidebar.setVisible(false);
        } else {
            sidebar.setVisible(true);
        }
    }

    private void changeSection(int index) {
        contentArea.getChildren().clear();

        switch (index) {
            case 0 -> createLandSection();
            case 1 -> createCropSection();
            case 2 -> createFertilizerSection();
            case 3 -> createIrrigationSection();
            case 4 -> createPestSection();
            case 5 -> createMarketSection();
            case 6 -> createWeatherSection();
            case 7 -> createReportsSection();
        }

        AnimationHelper.fadeIn(contentArea);
    }

    
    private void createLandSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("🏡 Land Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button addLandBtn = createStyledButton("➕ Add New Land");
        Button viewLandsBtn = createStyledButton("📋 View My Lands");

        ScrollPane landScrollPane = new ScrollPane();
        landScrollPane.setFitToWidth(true);
        landScrollPane.setPrefHeight(400);
        landScrollPane.setStyle("-fx-background: transparent; -fx-border: none;");

        VBox landContainer = new VBox(10);
        landContainer.setPadding(new Insets(15));
        landScrollPane.setContent(landContainer);

        addLandBtn.setOnAction(e -> showAddLandDialog());
        viewLandsBtn.setOnAction(e -> loadLandsView(landContainer));

        section.getChildren().addAll(title, addLandBtn, viewLandsBtn, landScrollPane);
        contentArea.getChildren().add(section);
    }

    private void createCropSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("🌾 Crop Recommendations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        ComboBox<String> seasonCombo = new ComboBox<>();
        seasonCombo.getItems().addAll("Monsoon", "Winter", "Summer", "Autumn");
        seasonCombo.setValue("Monsoon");
        seasonCombo.setStyle("-fx-background-color: white; -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button recommendBtn = createStyledButton("🎯 Show Best Crops for Added Lands");

        HBox recommendBox = new HBox(10, new Label("Season:"), seasonCombo, recommendBtn);
        recommendBox.setAlignment(Pos.CENTER_LEFT);

        ScrollPane cropScrollPane = new ScrollPane();
        cropScrollPane.setFitToWidth(true);
        cropScrollPane.setPrefHeight(300);
        cropScrollPane.setStyle("-fx-background: transparent; -fx-border: none;");

        VBox cropContainer = new VBox(10);
        cropContainer.setPadding(new Insets(15));
        cropScrollPane.setContent(cropContainer);

        recommendBtn.setOnAction(e -> loadCropRecommendations(cropContainer, seasonCombo.getValue()));

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ffffff;");

        Label customTitle = new Label("🔎 Custom Recommendation");
        customTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        customTitle.setTextFill(Color.WHITE);

        ComboBox<String> customSoilCombo = new ComboBox<>();
        customSoilCombo.getItems().addAll("Loamy", "Clay", "Sandy", "Silt", "Peaty");
        customSoilCombo.setPromptText("Select soil type");
        customSoilCombo.setStyle("-fx-background-color: white; -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        ComboBox<String> customSeasonCombo = new ComboBox<>();
        customSeasonCombo.getItems().addAll("Monsoon", "Winter", "Summer", "Autumn");
        customSeasonCombo.setPromptText("Select season");
        customSeasonCombo.setStyle("-fx-background-color: white; -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button customRecommendBtn = createStyledButton("✅ Recommend Crops");

        HBox customControls = new HBox(10, customSoilCombo, customSeasonCombo, customRecommendBtn);
        customControls.setAlignment(Pos.CENTER_LEFT);

        TextArea customResultArea = new TextArea();
        customResultArea.setEditable(false);
        customResultArea.setPrefHeight(150);
        customResultArea.setStyle("-fx-background-color: rgba(0, 106, 78, 0.8); -fx-text-fill: white; -fx-border-radius: 10px; -fx-padding: 10px;");

        customRecommendBtn.setOnAction(e -> {
            String soilSelection = customSoilCombo.getValue();
            String seasonSelection = customSeasonCombo.getValue();
            if (soilSelection == null || seasonSelection == null || soilSelection.isBlank() || seasonSelection.isBlank()) {
                showAlert("Input required", "Please select both soil type and season.");
                return;
            }

            List<String> recs = cropService.recommendCrop(soilSelection, seasonSelection);
            if (recs.size() == 1 && "No suitable crops".equals(recs.get(0))) {
                customResultArea.setText("No suitable crops found for " + soilSelection + " soil in " + seasonSelection + ".\nTry a different combination or use the land recommendation button above.");
            } else {
                customResultArea.setText("Recommended crops for " + soilSelection + " soil in " + seasonSelection + ":\n- " + String.join("\n- ", recs));
            }
            AnimationHelper.fadeIn(customResultArea);
        });

        section.getChildren().addAll(title, recommendBox, cropScrollPane, separator, customTitle, customControls, customResultArea);
        contentArea.getChildren().add(section);
    }

    private void createFertilizerSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("🧪 Fertilizer Recommendations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button recommendBtn = createStyledButton("🌱 Show Fertilizer Plan");

        ScrollPane fertScrollPane = new ScrollPane();
        fertScrollPane.setFitToWidth(true);
        fertScrollPane.setPrefHeight(400);
        fertScrollPane.setStyle("-fx-background: transparent; -fx-border: none;");

        VBox fertContainer = new VBox(10);
        fertContainer.setPadding(new Insets(15));
        fertScrollPane.setContent(fertContainer);

        recommendBtn.setOnAction(e -> loadFertilizerRecommendations(fertContainer));

        section.getChildren().addAll(title, recommendBtn, fertScrollPane);
        contentArea.getChildren().add(section);
    }

    private void createIrrigationSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("💧 Irrigation Advice");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button recommendBtn = createStyledButton("🚿 Show Irrigation Plan");

        ScrollPane irrigScrollPane = new ScrollPane();
        irrigScrollPane.setFitToWidth(true);
        irrigScrollPane.setPrefHeight(400);
        irrigScrollPane.setStyle("-fx-background: transparent; -fx-border: none;");

        VBox irrigContainer = new VBox(10);
        irrigContainer.setPadding(new Insets(15));
        irrigScrollPane.setContent(irrigContainer);

        recommendBtn.setOnAction(e -> loadIrrigationRecommendations(irrigContainer));

        section.getChildren().addAll(title, recommendBtn, irrigScrollPane);
        contentArea.getChildren().add(section);
    }

    private void createPestSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("🐛 Pest & Disease Control");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        TextField pestInput = new TextField();
        pestInput.setPromptText("Describe symptoms (e.g. yellow leaves, insects on stem)");
        pestInput.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 10px; -fx-font-size: 14px;");

        Button identifyBtn = createStyledButton("🔍 Identify Pest & Get Solution");

        TextArea pestOutput = new TextArea();
        pestOutput.setEditable(false);
        pestOutput.setPrefHeight(300);
        pestOutput.setStyle("-fx-background-color: rgba(0, 106, 78, 0.8); -fx-text-fill: white; -fx-border-radius: 10px; -fx-padding: 10px;");

        identifyBtn.setOnAction(e -> {
            String description = pestInput.getText().trim();
            if (!description.isEmpty()) {
                String pest = pestService.identifyPest(description);
                String control = pestService.getControl(pest);
                pestOutput.setText("Identified Pest: " + pest + "\n\nControl Measures:\n" + control);
                AnimationHelper.fadeIn(pestOutput);
            }
        });

        section.getChildren().addAll(title, pestInput, identifyBtn, pestOutput);
        contentArea.getChildren().add(section);
    }

    private void createMarketSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("💰 Market Prices");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button showPricesBtn = createStyledButton("📊 Show Current Market Prices");

        ScrollPane marketScrollPane = new ScrollPane();
        marketScrollPane.setFitToWidth(true);
        marketScrollPane.setPrefHeight(400);
        marketScrollPane.setStyle("-fx-background: transparent; -fx-border: none;");

        VBox marketContainer = new VBox(10);
        marketContainer.setPadding(new Insets(15));
        marketScrollPane.setContent(marketContainer);

        showPricesBtn.setOnAction(e -> loadMarketPrices(marketContainer));

        section.getChildren().addAll(title, showPricesBtn, marketScrollPane);
        contentArea.getChildren().add(section);
    }

    private void createWeatherSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("🌤️ Weather Forecast");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button getWeatherBtn = createStyledButton("🌦️ Get Current Weather");

        TextArea weatherOutput = new TextArea();
        weatherOutput.setEditable(false);
        weatherOutput.setPrefHeight(300);
        weatherOutput.setStyle("-fx-background-color: rgba(0, 106, 78, 0.8); -fx-text-fill: white; -fx-border-radius: 10px; -fx-padding: 10px;");

        getWeatherBtn.setOnAction(e -> {
            Map<String, String> weather = weatherService.getWeather();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : weather.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            weatherOutput.setText(sb.toString());
            AnimationHelper.fadeIn(weatherOutput);
        });

        section.getChildren().addAll(title, getWeatherBtn, weatherOutput);
        contentArea.getChildren().add(section);
    }

    private void createReportsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));

        Label title = new Label("📄 Reports & Analytics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Button pdfReportBtn = createStyledButton("📋 Generate PDF Report");
        Button chartBtn = createStyledButton("📈 View Profit/Loss Chart");

        TextArea reportOutput = new TextArea();
        reportOutput.setEditable(false);
        reportOutput.setPrefHeight(300);
        reportOutput.setStyle("-fx-background-color: rgba(0, 106, 78, 0.8); -fx-text-fill: white; -fx-border-radius: 10px; -fx-padding: 10px;");

        pdfReportBtn.setOnAction(e -> generatePDFReport(reportOutput));
        chartBtn.setOnAction(e -> showProfitLossChart());

        section.getChildren().addAll(title, pdfReportBtn, chartBtn, reportOutput);
        contentArea.getChildren().add(section);
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: linear-gradient(to right, #006a4e, #00a86b); -fx-text-fill: white; -fx-border-radius: 8px; -fx-padding: 10px 20px; -fx-font-weight: bold; -fx-font-size: 14px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(to right, #f42a4d, #ff6b6b); -fx-text-fill: white; -fx-border-radius: 8px; -fx-padding: 10px 20px; -fx-font-weight: bold; -fx-font-size: 14px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: linear-gradient(to right, #006a4e, #00a86b); -fx-text-fill: white; -fx-border-radius: 8px; -fx-padding: 10px 20px; -fx-font-weight: bold; -fx-font-size: 14px;"));
        return button;
    }

    // Implementation methods will continue in the next part

    private void showAddLandDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Add New Land");
        dialog.setResizable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #006a4e, #f42a4d);");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        TextField locationField = new TextField();
        locationField.setPromptText("Auto-filled from map...");
        locationField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        TextField areaField = new TextField();
        areaField.setPromptText("e.g. 2.5");
        areaField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        ComboBox<String> soilTypeCombo = new ComboBox<>();
        soilTypeCombo.getItems().addAll("Loamy", "Clay", "Sandy", "Silt", "Peaty");
        soilTypeCombo.setStyle("-fx-background-color: white; -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 5px;");

        TextField gpsField = new TextField();
        gpsField.setPromptText("e.g. 23.8103, 90.4125");
        gpsField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        // Style labels
        Label locationLabel = new Label("Location:");
        locationLabel.setTextFill(Color.WHITE);
        locationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label areaLabel = new Label("Area (ha):");
        areaLabel.setTextFill(Color.WHITE);
        areaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label soilLabel = new Label("Soil Type:");
        soilLabel.setTextFill(Color.WHITE);
        soilLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label gpsLabel = new Label("GPS Coords:");
        gpsLabel.setTextFill(Color.WHITE);
        gpsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        formGrid.add(locationLabel, 0, 0);
        formGrid.add(locationField, 1, 0);
        formGrid.add(areaLabel, 0, 1);
        formGrid.add(areaField, 1, 1);
        formGrid.add(soilLabel, 0, 2);
        formGrid.add(soilTypeCombo, 1, 2);
        formGrid.add(gpsLabel, 0, 3);
        formGrid.add(gpsField, 1, 3);

        // Map section
        Label mapLabel = new Label("Click on map to select location");
        mapLabel.setTextFill(Color.WHITE);
        mapLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        mapLabel.setAlignment(Pos.CENTER);

        WebView mapView = new WebView();
        mapView.setPrefHeight(300);
        WebEngine webEngine = mapView.getEngine();

        // Load map
        try {
            java.net.URL mapUrl = getClass().getResource("/map_picker.html");
            if (mapUrl != null) {
                webEngine.load(mapUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Error loading map: " + e.getMessage());
        }

        // Java-JS bridge
        JavaBridge bridge = new JavaBridge(locationField, gpsField, soilTypeCombo);
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("bridge", bridge);
            }
        });

        Button fetchAddressBtn = new Button("Fetch address from GPS");
        fetchAddressBtn.setDisable(true);
        fetchAddressBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button useCurrentLocationBtn = new Button("Use My Current Location");
        useCurrentLocationBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px;");

        HBox buttonBox = new HBox(10, fetchAddressBtn, useCurrentLocationBtn);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("Save Land");
        saveBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 10px 20px; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f42a4d; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 10px 20px; -fx-font-weight: bold;");

        HBox actionButtons = new HBox(10, saveBtn, cancelBtn);
        actionButtons.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(formGrid, mapLabel, mapView, buttonBox, actionButtons);

        // Event handlers
        gpsField.textProperty().addListener((obs, oldText, newText) -> {
            fetchAddressBtn.setDisable(!isValidGPS(newText));
        });

        fetchAddressBtn.setOnAction(e -> fetchAddressFromGPS(gpsField.getText(), locationField, soilTypeCombo));
        useCurrentLocationBtn.setOnAction(e -> fetchCurrentLocation(locationField, gpsField, soilTypeCombo));

        saveBtn.setOnAction(e -> {
            try {
                String location = locationField.getText().trim();
                String areaText = areaField.getText().trim();
                String soilType = soilTypeCombo.getValue();
                String gps = gpsField.getText().trim();

                if (location.isEmpty()) {
                    showAlert("Error", "Location is required.");
                    return;
                }

                double area = Double.parseDouble(areaText);
                if (area <= 0) {
                    showAlert("Error", "Area must be positive.");
                    return;
                }

                if (soilType == null || soilType.isEmpty()) {
                    showAlert("Error", "Soil type is required.");
                    return;
                }

                Land land = new Land(currentUser.getId(), location, area, soilType, gps);
                if (DatabaseManager.addLand(land)) {
                    showAlert("Success", "Land added successfully!");
                    dialog.close();
                    // Refresh land view if it's currently displayed
                    changeSection(sidebar.getSelectionModel().getSelectedIndex());
                } else {
                    showAlert("Error", "Failed to add land.");
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid area value.");
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Scene scene = new Scene(layout, 600, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void loadLandsView(VBox container) {
        container.getChildren().clear();

        try {
            List<Land> lands = DatabaseManager.getUserLands(currentUser.getId());

            if (lands.isEmpty()) {
                Label noLandsLabel = new Label("No lands registered yet.");
                noLandsLabel.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic; -fx-padding: 20px;");
                noLandsLabel.setAlignment(Pos.CENTER);
                container.getChildren().add(noLandsLabel);
                return;
            }

            for (int i = 0; i < lands.size(); i++) {
                Land land = lands.get(i);
                VBox landCard = createLandCard(land, i + 1);
                container.getChildren().add(landCard);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load lands: " + e.getMessage());
        }
    }

    private VBox createLandCard(Land land, int position) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(0, 106, 78, 0.3); -fx-border-color: #00a86b; -fx-border-width: 0 0 0 5px; -fx-border-radius: 10px; -fx-padding: 15px; -fx-margin: 5px;");

        Label titleLabel = new Label(position + "st Land");
        titleLabel.setStyle("-fx-text-fill: #f42a4d; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label locationLabel = new Label("Location: " + land.getLocation());
        locationLabel.setStyle("-fx-text-fill: white;");

        Label areaLabel = new Label("Area: " + land.getArea() + " ha | Soil: " + land.getSoilType());
        areaLabel.setStyle("-fx-text-fill: white;");

        Label gpsLabel = new Label();
        if (land.getGpsCoords() != null && !land.getGpsCoords().isEmpty()) {
            gpsLabel.setText("GPS: " + land.getGpsCoords());
            gpsLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12px;");
        }

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 5px 15px; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> confirmDeleteLand(land, position));

        HBox cardHeader = new HBox(10, titleLabel);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        HBox cardFooter = new HBox(10);
        cardFooter.setAlignment(Pos.CENTER_RIGHT);
        cardFooter.getChildren().add(deleteBtn);

        card.getChildren().addAll(cardHeader, locationLabel, areaLabel);
        if (!gpsLabel.getText().isEmpty()) {
            card.getChildren().add(gpsLabel);
        }
        card.getChildren().add(cardFooter);

        return card;
    }

    private void confirmDeleteLand(Land land, int position) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + getOrdinalSuffix(position) + " Land?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Password verification
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Verify Identity");
            passwordDialog.setHeaderText("Enter your password to confirm deletion");
            passwordDialog.setContentText("Password:");

            Optional<String> passwordResult = passwordDialog.showAndWait();
            if (passwordResult.isPresent()) {
                try {
                    User user = DatabaseManager.authenticateUser(currentUser.getUsername(), passwordResult.get());
                    if (user != null) {
                        if (DatabaseManager.deleteLand(land.getId(), currentUser.getId())) {
                            showAlert("Success", getOrdinalSuffix(position) + " land has been deleted.");
                            changeSection(sidebar.getSelectionModel().getSelectedIndex());
                        } else {
                            showAlert("Error", "Failed to delete land.");
                        }
                    } else {
                        showAlert("Error", "Incorrect password.");
                    }
                } catch (SQLException e) {
                    showAlert("Error", "Database error: " + e.getMessage());
                }
            }
        }
    }

    private String getOrdinalSuffix(int number) {
        if (number % 100 >= 11 && number % 100 <= 13) {
            return number + "th";
        }
        switch (number % 10) {
            case 1: return number + "st";
            case 2: return number + "nd";
            case 3: return number + "rd";
            default: return number + "th";
        }
    }

    private void loadCropRecommendations(VBox container, String season) {
        container.getChildren().clear();

        try {
            List<Land> lands = DatabaseManager.getUserLands(currentUser.getId());

            if (lands.isEmpty()) {
                Label noLandsLabel = new Label("No land added yet. Add your land to get crop recommendations.");
                noLandsLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px; -fx-padding: 20px;");
                noLandsLabel.setAlignment(Pos.CENTER);
                container.getChildren().add(noLandsLabel);
                return;
            }

            for (Land land : lands) {
                VBox landCard = new VBox(10);
                landCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.07); -fx-border-radius: 15px; -fx-padding: 20px; -fx-border-color: #00a86b; -fx-border-width: 0 0 0 5px;");

                Label landTitle = new Label("Land — " + land.getLocation() + " (" + land.getSoilType() + ")");
                landTitle.setStyle("-fx-text-fill: #f42a4d; -fx-font-size: 16px; -fx-font-weight: bold;");

                List<String> recommendations = cropService.recommendCrop(land.getSoilType(), season);
                String bestCrop = recommendations.isEmpty() ? "Not suitable" : recommendations.get(0);

                Label bestCropLabel = new Label("Best Crop → " + bestCrop + " (" + season + ")");
                bestCropLabel.setStyle("-fx-text-fill: #00a86b; -fx-font-size: 16px; -fx-font-weight: bold;");

                String alsoGood = "";
                if (recommendations.size() > 1) {
                    alsoGood = "Also good: " + String.join(", ", recommendations.subList(1, Math.min(4, recommendations.size())));
                }

                Label alsoGoodLabel = new Label(alsoGood);
                alsoGoodLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

                landCard.getChildren().addAll(landTitle, bestCropLabel);
                if (!alsoGood.isEmpty()) {
                    landCard.getChildren().add(alsoGoodLabel);
                }

                container.getChildren().add(landCard);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load crop recommendations: " + e.getMessage());
        }
    }

    private void loadFertilizerRecommendations(VBox container) {
        container.getChildren().clear();

        try {
            List<Land> lands = DatabaseManager.getUserLands(currentUser.getId());

            if (lands.isEmpty()) {
                Label noLandsLabel = new Label("No land added yet. Add your land to get fertilizer recommendations.");
                noLandsLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px; -fx-padding: 20px;");
                noLandsLabel.setAlignment(Pos.CENTER);
                container.getChildren().add(noLandsLabel);
                return;
            }

            for (Land land : lands) {
                VBox landCard = new VBox(10);
                landCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.07); -fx-border-radius: 15px; -fx-padding: 20px; -fx-border-color: #D2691E; -fx-border-width: 0 0 0 5px;");

                Label landTitle = new Label("Land — " + land.getLocation() + " (" + land.getSoilType() + ")");
                landTitle.setStyle("-fx-text-fill: #D2691E; -fx-font-size: 16px; -fx-font-weight: bold;");

                List<String> cropRecs = cropService.recommendCrop(land.getSoilType(), "Monsoon");
                String bestCrop = cropRecs.isEmpty() ? "Rice (Aman)" : cropRecs.get(0);

                Label cropLabel = new Label("Recommended Crop: " + bestCrop);
                cropLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                Label fertPlanLabel = new Label("Fertilizer Plan:");
                fertPlanLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                TextArea fertDetails = new TextArea(fertilizerService.recommendFertilizer(bestCrop));
                fertDetails.setEditable(false);
                fertDetails.setPrefHeight(100);
                fertDetails.setStyle("-fx-background-color: rgba(0, 106, 78, 0.5); -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 5px;");

                landCard.getChildren().addAll(landTitle, cropLabel, fertPlanLabel, fertDetails);
                container.getChildren().add(landCard);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load fertilizer recommendations: " + e.getMessage());
        }
    }

    private void loadIrrigationRecommendations(VBox container) {
        container.getChildren().clear();

        try {
            List<Land> lands = DatabaseManager.getUserLands(currentUser.getId());

            if (lands.isEmpty()) {
                Label noLandsLabel = new Label("No land added yet. Add your land to get irrigation recommendations.");
                noLandsLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px; -fx-padding: 20px;");
                noLandsLabel.setAlignment(Pos.CENTER);
                container.getChildren().add(noLandsLabel);
                return;
            }

            for (Land land : lands) {
                VBox landCard = new VBox(10);
                landCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.07); -fx-border-radius: 15px; -fx-padding: 20px; -fx-border-color: #4169E1; -fx-border-width: 0 0 0 5px;");

                Label landTitle = new Label("Land — " + land.getLocation());
                landTitle.setStyle("-fx-text-fill: #4169E1; -fx-font-size: 16px; -fx-font-weight: bold;");

                Label soilLabel = new Label("Soil: " + land.getSoilType());
                soilLabel.setStyle("-fx-text-fill: white;");

                Label irrigationLabel = new Label("Irrigation Advice:");
                irrigationLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                String advice;
                if ("Clay".equals(land.getSoilType())) {
                    advice = "Alternate Wetting & Drying (AWD) – Save 30% water";
                } else if ("Sandy".equals(land.getSoilType())) {
                    advice = "Drip irrigation – Every 2-3 days";
                } else {
                    advice = "Furrow irrigation – Every 7-10 days";
                }

                Label adviceLabel = new Label(advice);
                adviceLabel.setStyle("-fx-text-fill: #00a86b; -fx-font-size: 14px;");

                landCard.getChildren().addAll(landTitle, soilLabel, irrigationLabel, adviceLabel);
                container.getChildren().add(landCard);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load irrigation recommendations: " + e.getMessage());
        }
    }

    private void loadMarketPrices(VBox container) {
        container.getChildren().clear();

        try {
            List<Land> lands = DatabaseManager.getUserLands(currentUser.getId());

            String bestCrop = "Rice (Aman)"; // Default
            if (!lands.isEmpty()) {
                // Find most common recommended crop
                Map<String, Integer> cropCount = new java.util.HashMap<>();
                for (Land land : lands) {
                    List<String> recs = cropService.recommendCrop(land.getSoilType(), "Monsoon");
                    if (!recs.isEmpty()) {
                        String crop = recs.get(0);
                        cropCount.put(crop, cropCount.getOrDefault(crop, 0) + 1);
                    }
                }

                if (!cropCount.isEmpty()) {
                    bestCrop = cropCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .get().getKey();
                }
            }

            Label bestForYouLabel = new Label("Best for You → " + bestCrop);
            bestForYouLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 20px; -fx-font-weight: bold; -fx-alignment: center;");
            bestForYouLabel.setAlignment(Pos.CENTER);

            container.getChildren().add(bestForYouLabel);

            for (Map.Entry<String, Integer> entry : marketService.getAllPrices().entrySet()) {
                String crop = entry.getKey();
                String price = marketService.getPrice(crop);
                String star = crop.equals(bestCrop) ? " ⭐ RECOMMENDED" : "";

                Label priceLabel = new Label("• " + crop + ": " + price + star);
                priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 5px 0;");
                container.getChildren().add(priceLabel);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load market prices: " + e.getMessage());
        }
    }

    private void generatePDFReport(TextArea output) {
        try {
            List<Land> lands = DatabaseManager.getUserLands(currentUser.getId());
            if (lands.isEmpty()) {
                output.setText("No lands found. Add land first.");
                return;
            }

            String fileName = currentUser.getName() + "_AgriSmart_Report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Report");
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                reportService.generatePDFReport(currentUser.getId(), file.getAbsolutePath());
                output.setText("PDF report generated: " + file.getName());
                AnimationHelper.fadeIn(output);
            }

        } catch (Exception e) {
            output.setText("Error generating PDF: " + e.getMessage());
        }
    }

    private void showProfitLossChart() {
        // This would require JavaFX Charts - simplified implementation
        showAlert("Info", "Profit/Loss chart feature would be implemented with JavaFX Charts");
    }

    private void showProfileDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Farmer Profile");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #006a4e, #f42a4d);");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        TextField usernameField = new TextField(currentUser.getUsername());
        TextField nidField = new TextField(currentUser.getNid());
        TextField nameField = new TextField(currentUser.getName());
        TextField phoneField = new TextField(currentUser.getPhone());

        // Style fields
        String fieldStyle = "-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;";
        usernameField.setStyle(fieldStyle);
        nidField.setStyle(fieldStyle);
        nameField.setStyle(fieldStyle);
        phoneField.setStyle(fieldStyle);

        // Style labels
        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label nidLabel = new Label("NID:");
        nidLabel.setTextFill(Color.WHITE);
        nidLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label nameLabel = new Label("Full Name:");
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label phoneLabel = new Label("Phone:");
        phoneLabel.setTextFill(Color.WHITE);
        phoneLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(nidLabel, 0, 1);
        formGrid.add(nidField, 1, 1);
        formGrid.add(nameLabel, 0, 2);
        formGrid.add(nameField, 1, 2);
        formGrid.add(phoneLabel, 0, 3);
        formGrid.add(phoneField, 1, 3);

        Button uploadPicBtn = new Button("Upload Profile Picture");
        uploadPicBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px;");

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 10px 20px; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f42a4d; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 10px 20px; -fx-font-weight: bold;");

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(formGrid, uploadPicBtn, buttons);

        uploadPicBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                // In a real app, you'd copy the file to a safe location
                currentUser.setProfilePic(selectedFile.getAbsolutePath());
                showAlert("Success", "Profile picture selected.");
            }
        });

        saveBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String nid = nidField.getText().trim();
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (username.isEmpty() || nid.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                showAlert("Error", "All fields are required.");
                return;
            }

            try {
                currentUser.setUsername(username);
                currentUser.setNid(nid);
                currentUser.setName(name);
                currentUser.setPhone(phone);

                if (DatabaseManager.updateUser(currentUser)) {
                    showAlert("Success", "Profile updated successfully!");
                    dialog.close();
                } else {
                    showAlert("Error", "Failed to update profile.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Scene scene = new Scene(layout, 400, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void logout() {
        primaryStage.close();
        LoginController loginController = new LoginController();
        loginController.showLoginDialog(primaryStage);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility methods for GPS and geocoding
    private boolean isValidGPS(String text) {
        return text != null && text.matches("^[-+]?\\d*\\.?\\d+,\\s*[-+]?\\d*\\.?\\d+$");
    }

    private void fetchAddressFromGPS(String gpsText, TextField locationField, ComboBox<String> soilCombo) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String[] coords = gpsText.split(",");
                double lat = Double.parseDouble(coords[0].trim());
                double lng = Double.parseDouble(coords[1].trim());

                HttpClient client = HttpClient.newHttpClient();
                String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lng);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "AgriSmart/1.0")
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(response.body());
                    String address = jsonNode.path("display_name").asText();

                    Platform.runLater(() -> {
                        locationField.setText(address);
                        locationField.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        String soilType = getSoilTypeFromCoords(lat, lng, address);
                        if (soilType != null) {
                            soilCombo.setValue(soilType);
                        }
                    });
                }
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Error", "Failed to fetch address from GPS"));
            }
        };

        new Thread(task).start();
    }

    private void fetchCurrentLocation(TextField locationField, TextField gpsField, ComboBox<String> soilCombo) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();

                // Try ipapi.co first
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ipapi.co/json/"))
                        .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(response.body());
                        double lat = jsonNode.path("latitude").asDouble();
                        double lng = jsonNode.path("longitude").asDouble();
                        String city = jsonNode.path("city").asText();
                        String region = jsonNode.path("region").asText();
                        String country = jsonNode.path("country_name").asText();

                        String address = String.format("%s, %s, %s", city, region, country).replace(", ,", ",");

                        Platform.runLater(() -> {
                            gpsField.setText(String.format("%.6f, %.6f", lat, lng));
                            locationField.setText(address);
                            locationField.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            String soilType = getSoilTypeFromCoords(lat, lng, address);
                            if (soilType != null) {
                                soilCombo.setValue(soilType);
                            }
                        });
                        return null;
                    }
                } catch (Exception e) {
                    // Try ipinfo.io as fallback
                }

                // Fallback to ipinfo.io
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipinfo.io/json"))
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(response.body());
                    String loc = jsonNode.path("loc").asText();
                    String[] coords = loc.split(",");
                    double lat = Double.parseDouble(coords[0]);
                    double lng = Double.parseDouble(coords[1]);
                    String city = jsonNode.path("city").asText();
                    String country = jsonNode.path("country").asText();

                    String address = String.format("%s, %s", city, country);

                    Platform.runLater(() -> {
                        gpsField.setText(String.format("%.6f, %.6f", lat, lng));
                        locationField.setText(address);
                        locationField.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        String soilType = getSoilTypeFromCoords(lat, lng, address);
                        if (soilType != null) {
                            soilCombo.setValue(soilType);
                        }
                    });
                }

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("Error", "Failed to fetch current location"));
            }
        };

        new Thread(task).start();
    }

    private String getSoilTypeFromCoords(double lat, double lng, String address) {
        String fromAddress = estimateSoilTypeFromAddress(address);
        if (fromAddress != null) {
            return fromAddress;
        }
        return estimateSoilTypeFromCoords(lat, lng);
    }

    private String estimateSoilTypeFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String normalized = address.toLowerCase();

        if (normalized.contains("sundarbans") || normalized.contains("delta") || normalized.contains("beel") || normalized.contains("peat")) {
            return "Peaty";
        }
        if (normalized.contains("sylhet") || normalized.contains("habiganj") || normalized.contains("moulvibazar") || normalized.contains("cox") || normalized.contains("bandarban") || normalized.contains("noakhali") || normalized.contains("feni") || normalized.contains("comilla") || normalized.contains("cumilla") || normalized.contains("brahmanbaria") || normalized.contains("chattogram")) {
            return "Clay";
        }
        if (normalized.contains("khulna") || normalized.contains("barisal") || normalized.contains("barishal") || normalized.contains("patuakhali") || normalized.contains("satkhira") || normalized.contains("bagerhat") || normalized.contains("jessore") || normalized.contains("narail")) {
            return "Clay";
        }
        if (normalized.contains("rajshahi") || normalized.contains("bogra") || normalized.contains("pabna") || normalized.contains("joypurhat") || normalized.contains("natore") || normalized.contains("dinajpur") || normalized.contains("rangpur")) {
            return "Sandy";
        }
        if (normalized.contains("dhaka") || normalized.contains("gazipur") || normalized.contains("narayanganj") || normalized.contains("tangail") || normalized.contains("manikganj") || normalized.contains("kishoreganj") || normalized.contains("gopalganj") || normalized.contains("kishoreganj")) {
            return "Loamy";
        }
        if (normalized.contains("mymensingh") || normalized.contains("jamalpur") || normalized.contains("netrokona")) {
            return "Silt";
        }
        return null;
    }

    private String estimateSoilTypeFromCoords(double lat, double lng) {
        if (lat >= 25.3) {
            return lng > 91.0 ? "Clay" : "Loamy";
        }
        if (lat <= 22.8) {
            return lng <= 90.0 ? "Sandy" : "Clay";
        }
        if (lng >= 91.0) {
            return "Clay";
        }
        if (lng <= 89.0) {
            return "Sandy";
        }
        return "Loamy";
    }

    // Java-JS Bridge class
    public static class JavaBridge {
        private TextField locationField;
        private TextField gpsField;
        private ComboBox<String> soilCombo;

        public JavaBridge(TextField locationField, TextField gpsField, ComboBox<String> soilCombo) {
            this.locationField = locationField;
            this.gpsField = gpsField;
            this.soilCombo = soilCombo;
        }

        public void receive(String message) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(message);
                double lat = jsonNode.path("lat").asDouble();
                double lng = jsonNode.path("lng").asDouble();
                String gpsText = String.format("%.6f, %.6f", lat, lng);

                Platform.runLater(() -> {
                    gpsField.setText(gpsText);
                    locationField.setText("Looking up location...");
                    locationField.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                });

                reverseGeocode(lat, lng, gpsText);
            } catch (Exception e) {
                System.err.println("Error processing map click: " + e.getMessage());
            }
        }

        private void reverseGeocode(double lat, double lng, String fallback) {
            new Thread(() -> {
                String address = null;
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lng);
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "AgriSmart/1.0")
                        .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(response.body());
                        address = jsonNode.path("display_name").asText();
                    }
                } catch (Exception ignored) {
                }

                final String finalAddress = (address == null || address.isBlank()) ? fallback : address;
                Platform.runLater(() -> {
                    locationField.setText(finalAddress);
                    locationField.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    String soilType = getSoilTypeFromCoordsStatic(lat, lng, finalAddress);
                    if (soilType != null) {
                        soilCombo.setValue(soilType);
                    }
                });
            }).start();
        }

        private static String getSoilTypeFromCoordsStatic(double lat, double lng, String address) {
            String fromAddress = estimateSoilTypeFromAddressStatic(address);
            if (fromAddress != null) {
                return fromAddress;
            }
            return estimateSoilTypeFromCoordsStatic(lat, lng);
        }

        private static String estimateSoilTypeFromAddressStatic(String address) {
            if (address == null || address.isBlank()) {
                return null;
            }
            String normalized = address.toLowerCase();

            if (normalized.contains("sundarbans") || normalized.contains("delta") || normalized.contains("beel") || normalized.contains("peat")) {
                return "Peaty";
            }
            if (normalized.contains("sylhet") || normalized.contains("habiganj") || normalized.contains("moulvibazar") || normalized.contains("cox") || normalized.contains("bandarban") || normalized.contains("noakhali") || normalized.contains("feni") || normalized.contains("comilla") || normalized.contains("cumilla") || normalized.contains("brahmanbaria") || normalized.contains("chattogram")) {
                return "Clay";
            }
            if (normalized.contains("khulna") || normalized.contains("barisal") || normalized.contains("barishal") || normalized.contains("patuakhali") || normalized.contains("satkhira") || normalized.contains("bagerhat") || normalized.contains("jessore") || normalized.contains("narail")) {
                return "Clay";
            }
            if (normalized.contains("rajshahi") || normalized.contains("bogra") || normalized.contains("pabna") || normalized.contains("joypurhat") || normalized.contains("natore") || normalized.contains("dinajpur") || normalized.contains("rangpur")) {
                return "Sandy";
            }
            if (normalized.contains("dhaka") || normalized.contains("gazipur") || normalized.contains("narayanganj") || normalized.contains("tangail") || normalized.contains("manikganj") || normalized.contains("kishoreganj") || normalized.contains("gopalganj")) {
                return "Loamy";
            }
            if (normalized.contains("mymensingh") || normalized.contains("jamalpur") || normalized.contains("netrokona")) {
                return "Silt";
            }
            return null;
        }

        private static String estimateSoilTypeFromCoordsStatic(double lat, double lng) {
            if (lat >= 25.3) {
                return lng > 91.0 ? "Clay" : "Loamy";
            }
            if (lat <= 22.8) {
                return lng <= 90.0 ? "Sandy" : "Clay";
            }
            if (lng >= 91.0) {
                return "Clay";
            }
            if (lng <= 89.0) {
                return "Sandy";
            }
            return "Loamy";
        }
    }
}
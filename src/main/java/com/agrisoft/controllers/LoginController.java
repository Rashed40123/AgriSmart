package com.agrisoft.controllers;

import com.agrisoft.models.User;
import com.agrisoft.utils.AnimationHelper;
import com.agrisoft.utils.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.SQLException;

public class LoginController {
    private Stage primaryStage;
    private User currentUser;

    public void showLoginDialog(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create login dialog
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setTitle("AgriSmart Login");
        loginStage.setResizable(false);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #006a4e, #f42a4d);");

        // Title
        Label titleLabel = new Label("🌱 Welcome to AgriSmart 🌱");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        // Particle effect placeholder (simplified)
        VBox particleBox = new VBox();
        particleBox.setPrefHeight(50);
        particleBox.setStyle("-fx-background-color: transparent;");

        // Form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.WHITE);
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(passwordLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #f42a4d; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;"));

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;");
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #f42a4d; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;"));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: #f42a4d; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px; -fx-font-weight: bold;"));

        buttonBox.getChildren().addAll(loginButton, registerButton, cancelButton);

        mainLayout.getChildren().addAll(titleLabel, particleBox, formGrid, buttonBox);

        // Event handlers
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please enter both username and password.");
                return;
            }

            try {
                User user = DatabaseManager.authenticateUser(username, password);
                if (user != null) {
                    currentUser = user;
                    loginStage.close();
                    showMainWindow();
                } else {
                    showAlert("Error", "Invalid credentials.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> showRegisterDialog(loginStage));
        cancelButton.setOnAction(e -> System.exit(0));

        // Pulse animation for title
        AnimationHelper.pulse(titleLabel);

        Scene scene = new Scene(mainLayout, 400, 350);
        loginStage.setScene(scene);
        loginStage.showAndWait();
    }

    private void showRegisterDialog(Stage parentStage) {
        Stage registerStage = new Stage();
        registerStage.initModality(Modality.APPLICATION_MODAL);
        registerStage.initOwner(parentStage);
        registerStage.setTitle("AgriSmart Registration");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #006a4e, #f42a4d);");

        Label titleLabel = new Label("Register New Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        TextField nidField = new TextField();
        nidField.setPromptText("NID");
        nidField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        phoneField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-border-color: #006a4e; -fx-border-radius: 5px; -fx-padding: 8px;");

        formGrid.addRow(0, new Label("Username:"), usernameField);
        formGrid.addRow(1, new Label("Password:"), passwordField);
        formGrid.addRow(2, new Label("NID:"), nidField);
        formGrid.addRow(3, new Label("Full Name:"), nameField);
        formGrid.addRow(4, new Label("Phone:"), phoneField);

        // Style labels
        for (javafx.scene.Node node : formGrid.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.WHITE);
                ((Label) node).setFont(Font.font("Arial", FontWeight.BOLD, 12));
            }
        }

        Button registerBtn = new Button("Register");
        registerBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #006a4e; -fx-text-fill: white; -fx-border-radius: 5px; -fx-padding: 8px 20px;");

        HBox buttonBox = new HBox(10, registerBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, formGrid, buttonBox);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String nid = nidField.getText().trim();
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || nid.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                showAlert("Error", "All fields are required.");
                return;
            }

            try {
                User newUser = new User(username, password, nid, name, phone);
                if (DatabaseManager.registerUser(newUser)) {
                    showAlert("Success", "Registration successful! Please login.");
                    registerStage.close();
                } else {
                    showAlert("Error", "Registration failed.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> registerStage.close());

        Scene scene = new Scene(layout, 400, 400);
        registerStage.setScene(scene);
        registerStage.showAndWait();
    }

    private void showMainWindow() {
        MainController mainController = new MainController(currentUser);
        mainController.showMainWindow(primaryStage);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
package com.agrisoft;

import javafx.application.Application;
import javafx.stage.Stage;
import com.agrisoft.controllers.LoginController;
import com.agrisoft.utils.DatabaseManager;

public class AgriSmart extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseManager.initDatabase();

            // Show login dialog
            LoginController loginController = new LoginController();
            loginController.showLoginDialog(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
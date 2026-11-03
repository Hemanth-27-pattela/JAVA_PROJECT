package com.hotel.desktop;

import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX Application entry class.
 * Initializes the primary stage and navigates to the initial login screen.
 */
public class MainApp extends Application {

    public static final String APP_TITLE = "Hotel Booking Management System";
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 800;
    public static final int MIN_WIDTH = 1024;
    public static final int MIN_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setWidth(DEFAULT_WIDTH);
        primaryStage.setHeight(DEFAULT_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        ViewNavigator.setPrimaryStage(primaryStage);
        ViewNavigator.showLoginView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.hotel.desktop.util;

import com.hotel.desktop.controller.MainLayoutController;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.service.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Centralized navigation manager for switching views on the primary JavaFX stage.
 * Maintains authenticated shell layout and coordinates content view updating
 * without opening separate application windows.
 */
public final class ViewNavigator {

    private static Stage primaryStage;
    private static MainLayoutController mainLayoutController;

    private static final String CSS_PATH = "/css/application.css";

    private ViewNavigator() {}

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static MainLayoutController getMainLayoutController() {
        return mainLayoutController;
    }

    public static void showLoginView() {
        mainLayoutController = null;
        loadRootView("/fxml/login.fxml", "Hotel Booking Management System - Sign In");
    }

    public static void showRegisterView() {
        mainLayoutController = null;
        loadRootView("/fxml/register.fxml", "Hotel Booking Management System - Register Guest");
    }

    public static void showMainLayout() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            showLoginView();
            return;
        }

        try {
            URL fxmlUrl = ViewNavigator.class.getResource("/fxml/main-layout.fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: /fxml/main-layout.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            mainLayoutController = loader.getController();

            applyRootScene(root, "Hotel Booking Management System - Dashboard");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load main layout shell: " + e.getMessage(), e);
        }
    }

    /**
     * Backward-compatible alias for navigating to post-authentication shell.
     */
    public static void showHomeView() {
        showMainLayout();
    }

    /**
     * Navigates to a specific NavigationItem within the main shell.
     * Checks permissions and updates center content area.
     */
    public static boolean navigateTo(NavigationItem item) {
        if (mainLayoutController == null) {
            showMainLayout();
        }
        if (mainLayoutController != null) {
            return mainLayoutController.navigateTo(item);
        }
        return false;
    }

    private static void loadRootView(String fxmlPath, String title) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage has not been initialized in ViewNavigator");
        }

        try {
            URL fxmlUrl = ViewNavigator.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            applyRootScene(root, title);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    private static void applyRootScene(Parent root, String title) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1280, 800);
            URL cssUrl = ViewNavigator.class.getResource(CSS_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
            URL cssUrl = ViewNavigator.class.getResource(CSS_PATH);
            if (cssUrl != null && !scene.getStylesheets().contains(cssUrl.toExternalForm())) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        }

        primaryStage.setTitle(title);
        primaryStage.show();
    }
}

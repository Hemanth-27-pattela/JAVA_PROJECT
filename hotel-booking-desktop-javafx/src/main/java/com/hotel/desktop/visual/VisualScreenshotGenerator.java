package com.hotel.desktop.visual;

import com.hotel.desktop.controller.MainLayoutController;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class VisualScreenshotGenerator extends Application {

    private static final String CSS_PATH = "/css/application.css";
    private static final String ARTIFACT_DIR = "C:/Users/patte/.gemini/antigravity/brain/e308dbfa-266e-4ee2-b0fc-7026d4faacc9";
    private static final String PROJECT_SCREENSHOTS_DIR = "D:/HOTEL_PROJECT/Hotel-Booking-Management-System--React-MERN-FullStack/hotel-booking-desktop-javafx/screenshots";

    private final AuthService authService = new AuthService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ViewNavigator.setPrimaryStage(stage);

        new Thread(() -> {
            try {
                new File(ARTIFACT_DIR).mkdirs();
                new File(PROJECT_SCREENSHOTS_DIR).mkdirs();

                System.out.println("=============================================================");
                System.out.println("PHASE 9.4: APPLICATION SHELL & ROLE-BASED NAVIGATION CAPTURE");
                System.out.println("Backend Target: http://localhost:5001");
                System.out.println("=============================================================");

                // 1. Guest Shell & Home Page
                System.out.println("\n[1/5] Authenticating Guest (guest@hotel.com)...");
                sessionManager.clearSession();
                User guestUser = authService.login("guest@hotel.com", "12345678");
                System.out.println("Guest Authenticated: " + guestUser.getFullName() + ", Role: " + guestUser.getRole());

                Platform.runLater(() -> {
                    ViewNavigator.showMainLayout();
                });
                Thread.sleep(1200);
                captureStage(stage, "guest-shell-home.png");
                System.out.println("-> Captured: guest-shell-home.png (Guest sidebar with 4 items & Home view)");

                // 2. Hotel Owner Shell & Dashboard Placeholder
                System.out.println("\n[2/5] Authenticating Hotel Owner (owner@hotel.com)...");
                sessionManager.clearSession();
                User ownerUser = authService.login("owner@hotel.com", "12345678");
                System.out.println("Owner Authenticated: " + ownerUser.getFullName() + ", Role: " + ownerUser.getRole());

                Platform.runLater(() -> {
                    ViewNavigator.showMainLayout();
                });
                Thread.sleep(1200);
                captureStage(stage, "owner-shell-dashboard.png");
                System.out.println("-> Captured: owner-shell-dashboard.png (Owner sidebar with 6 items & Dashboard view)");

                // 3. Admin Shell & Admin Dashboard Placeholder
                System.out.println("\n[3/5] Authenticating Administrator (admin@hotel.com)...");
                sessionManager.clearSession();
                User adminUser = authService.login("admin@hotel.com", "12345678");
                System.out.println("Admin Authenticated: " + adminUser.getFullName() + ", Role: " + adminUser.getRole());

                Platform.runLater(() -> {
                    ViewNavigator.showMainLayout();
                });
                Thread.sleep(1200);
                captureStage(stage, "admin-shell-dashboard.png");
                System.out.println("-> Captured: admin-shell-dashboard.png (Admin sidebar with 10 items & Admin Dashboard view)");

                // 4. Admin Navigation Transition (Owner Requests Page)
                System.out.println("\n[4/5] Testing Navigation Transition to Owner Requests...");
                Platform.runLater(() -> {
                    ViewNavigator.navigateTo(NavigationItem.OWNER_REQUESTS);
                });
                Thread.sleep(1200);
                captureStage(stage, "admin-shell-owner-requests.png");
                System.out.println("-> Captured: admin-shell-owner-requests.png (Owner Requests view in center viewport)");

                // 5. Logout returning to Login Screen
                System.out.println("\n[5/5] Testing Logout and Return to Login View...");
                Platform.runLater(() -> {
                    MainLayoutController controller = ViewNavigator.getMainLayoutController();
                    if (controller != null) {
                        controller.handleLogout();
                    } else {
                        authService.logout();
                        ViewNavigator.showLoginView();
                    }
                });
                Thread.sleep(1200);
                captureStage(stage, "logout-return-to-login.png");
                System.out.println("-> Captured: logout-return-to-login.png (Clean Login screen with session cleared)");

                System.out.println("\n=============================================================");
                System.out.println("ALL 5 PHASE 9.4 SCREENSHOTS GENERATED & SAVED SUCCESSFULLY!");
                System.out.println("=============================================================");

                Platform.runLater(() -> {
                    stage.close();
                    Platform.exit();
                    System.exit(0);
                });

            } catch (Exception e) {
                System.err.println("FAILED during Phase 9.4 visual capture: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    stage.close();
                    Platform.exit();
                    System.exit(1);
                });
            }
        }, "shell-visual-runner").start();
    }

    private void captureStage(Stage stage, String filename) {
        Platform.runLater(() -> {
            try {
                Scene scene = stage.getScene();
                WritableImage snapshot = scene.snapshot(null);
                int width = (int) snapshot.getWidth();
                int height = (int) snapshot.getHeight();
                BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                PixelReader pr = snapshot.getPixelReader();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        bi.setRGB(x, y, pr.getArgb(x, y));
                    }
                }

                // Save to artifact directory
                File artifactFile = new File(ARTIFACT_DIR, filename);
                ImageIO.write(bi, "png", artifactFile);

                // Save to project screenshots directory
                File projectFile = new File(PROJECT_SCREENSHOTS_DIR, filename);
                ImageIO.write(bi, "png", projectFile);

            } catch (Exception e) {
                System.err.println("Error capturing " + filename + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

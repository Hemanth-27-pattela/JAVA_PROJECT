package com.hotel.desktop.controller;

import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private Label welcomeHeading;
    @FXML private Label userEmailLabel;
    @FXML private Label roleBadgeLabel;
    @FXML private Label roleDescriptionLabel;
    @FXML private Label roleTitleLabel;
    @FXML private Label sessionUserIdLabel;
    @FXML private Label sessionEmailLabel;
    @FXML private Label sessionRoleLabel;
    @FXML private Label tokenStatusLabel;
    @FXML private Button logoutButton;
    @FXML private VBox roleFeatureBox;

    private final AuthService authService;
    private final SessionManager sessionManager;

    public HomeController() {
        this(new AuthService(), SessionManager.getInstance());
    }

    public HomeController(AuthService authService, SessionManager sessionManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        User user = sessionManager.getCurrentUser();
        UserRole role = sessionManager.getRole();

        String displayName = sessionManager.getDisplayName();
        String email = sessionManager.getEmail();
        String userId = sessionManager.getUserId();

        if (welcomeHeading != null) {
            welcomeHeading.setText("Welcome back, " + displayName + "!");
        }

        if (userEmailLabel != null) {
            userEmailLabel.setText(email != null ? email : "No active email");
        }

        if (roleBadgeLabel != null) {
            roleBadgeLabel.setText(role.getDisplayName().toUpperCase());
            applyRoleBadgeStyle(role);
        }

        if (roleTitleLabel != null) {
            roleTitleLabel.setText(getRoleTitle(role));
        }

        if (roleDescriptionLabel != null) {
            roleDescriptionLabel.setText(getRoleDescription(role));
        }

        if (sessionUserIdLabel != null) {
            sessionUserIdLabel.setText(userId != null ? userId : "N/A");
        }

        if (sessionEmailLabel != null) {
            sessionEmailLabel.setText(email != null ? email : "N/A");
        }

        if (sessionRoleLabel != null) {
            sessionRoleLabel.setText(role.name() + " (" + role.getValue() + ")");
        }

        if (tokenStatusLabel != null) {
            boolean hasToken = sessionManager.isLoggedIn();
            tokenStatusLabel.setText(hasToken ? "Active Bearer JWT in Session" : "No token");
        }
    }

    @FXML
    public void handleLogout() {
        authService.logout();
        ViewNavigator.showLoginView();
    }

    private void applyRoleBadgeStyle(UserRole role) {
        roleBadgeLabel.getStyleClass().removeAll("role-badge-guest", "role-badge-owner", "role-badge-admin");
        switch (role) {
            case ADMIN -> roleBadgeLabel.getStyleClass().add("role-badge-admin");
            case HOTEL_OWNER -> roleBadgeLabel.getStyleClass().add("role-badge-owner");
            default -> roleBadgeLabel.getStyleClass().add("role-badge-guest");
        }
    }

    private String getRoleTitle(UserRole role) {
        return switch (role) {
            case ADMIN -> "Administrator Control Center (Phase 9.3 Placeholder)";
            case HOTEL_OWNER -> "Hotel Owner Property Hub (Phase 9.3 Placeholder)";
            case USER -> "Guest Booking Portal (Phase 9.3 Placeholder)";
        };
    }

    private String getRoleDescription(UserRole role) {
        return switch (role) {
            case ADMIN -> "You have full administrative privileges. In subsequent phases, you can review hotel-owner access requests, oversee all hotels, and inspect global analytics.";
            case HOTEL_OWNER -> "You have hotel management permissions. In subsequent phases, you can create and edit your properties, monitor bookings, and access property insights.";
            case USER -> "You are signed in as a registered Guest. In subsequent phases, you can browse destination hotels, view room details, submit reviews, and book stays.";
        };
    }
}


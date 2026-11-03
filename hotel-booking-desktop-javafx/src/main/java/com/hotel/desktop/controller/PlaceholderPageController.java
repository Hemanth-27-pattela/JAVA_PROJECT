package com.hotel.desktop.controller;

import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaceholderPageController {

    @FXML private Label pageIconLabel;
    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label pageDescriptionLabel;
    @FXML private Label targetRoleBadge;
    @FXML private Label roleContextLabel;
    @FXML private Label userContextLabel;
    @FXML private Label sessionStatusLabel;
    @FXML private Label phaseNoticeLabel;

    @FXML
    public void initialize() {
        // Default initialization if loaded without parameters
        SessionManager session = SessionManager.getInstance();
        User currentUser = session.getCurrentUser();
        NavigationItem defaultItem = NavigationItem.getDefaultItemForRole(session.getRole());
        setNavigationItem(defaultItem, currentUser);
    }

    public void setNavigationItem(NavigationItem item, User currentUser) {
        if (item == null) return;

        if (pageIconLabel != null) {
            pageIconLabel.setText(item.getIcon());
        }

        if (pageTitleLabel != null) {
            pageTitleLabel.setText(item.getTitle());
        }

        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText(item.getSubtitle());
        }

        if (pageDescriptionLabel != null) {
            pageDescriptionLabel.setText(item.getDescription());
        }

        if (targetRoleBadge != null) {
            SessionManager session = SessionManager.getInstance();
            UserRole activeRole = session.getRole();
            targetRoleBadge.setText(activeRole.getDisplayName().toUpperCase());
            applyRoleBadgeStyle(activeRole);
        }

        if (roleContextLabel != null) {
            String roleList = item.getAllowedRoles().stream()
                    .map(UserRole::getDisplayName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Public");
            roleContextLabel.setText("Permitted Roles: " + roleList);
        }

        if (userContextLabel != null) {
            if (currentUser != null) {
                userContextLabel.setText("Active User: " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
            } else {
                userContextLabel.setText("Active User: Authenticated Session");
            }
        }

        if (sessionStatusLabel != null) {
            SessionManager session = SessionManager.getInstance();
            sessionStatusLabel.setText(session.isLoggedIn() ? "Valid Bearer JWT in memory" : "No active session");
        }

        if (phaseNoticeLabel != null) {
            phaseNoticeLabel.setText("Phase 9.4 Architectural Shell: Full functional implementation of " +
                    item.getTitle() + " will follow in subsequent phases.");
        }
    }

    private void applyRoleBadgeStyle(UserRole role) {
        targetRoleBadge.getStyleClass().removeAll("role-badge-guest", "role-badge-owner", "role-badge-admin");
        switch (role) {
            case ADMIN -> targetRoleBadge.getStyleClass().add("role-badge-admin");
            case HOTEL_OWNER -> targetRoleBadge.getStyleClass().add("role-badge-owner");
            default -> targetRoleBadge.getStyleClass().add("role-badge-guest");
        }
    }
}


package com.hotel.desktop.controller;

import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainLayoutController {

    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label roleBadgeLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Button logoutButton;

    @FXML private Label sidebarRoleLabel;
    @FXML private VBox navigationContainer;
    @FXML private Label sidebarFooterUserName;
    @FXML private Label sidebarFooterRole;

    @FXML private StackPane contentArea;
    @FXML private Label statusMessageLabel;
    @FXML private Label activePageLabel;

    private final AuthService authService;
    private final SessionManager sessionManager;
    private final Map<NavigationItem, Button> navButtons = new HashMap<>();
    private NavigationItem currentItem;

    public MainLayoutController() {
        this(new AuthService(), SessionManager.getInstance());
    }

    public MainLayoutController(AuthService authService, SessionManager sessionManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        if (!sessionManager.isLoggedIn()) {
            ViewNavigator.showLoginView();
            return;
        }

        User user = sessionManager.getCurrentUser();
        UserRole role = sessionManager.getRole();

        // 1. Populate Header
        if (userNameLabel != null) {
            userNameLabel.setText(sessionManager.getDisplayName());
        }
        if (userEmailLabel != null) {
            userEmailLabel.setText(sessionManager.getEmail());
        }
        if (roleBadgeLabel != null) {
            roleBadgeLabel.setText(role.getDisplayName().toUpperCase());
            applyRoleBadgeStyle(role);
        }
        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText("Backend Connected (Port 5001)");
        }

        // 2. Populate Sidebar Header & Footer
        if (sidebarRoleLabel != null) {
            sidebarRoleLabel.setText(getPortalTitle(role));
        }
        if (sidebarFooterUserName != null) {
            sidebarFooterUserName.setText(sessionManager.getDisplayName());
        }
        if (sidebarFooterRole != null) {
            sidebarFooterRole.setText(role.getDisplayName());
        }

        // 3. Build Navigation Menu dynamically strictly based on role
        buildNavigationMenu(role);

        // 4. Load initial default page
        NavigationItem defaultItem = NavigationItem.getDefaultItemForRole(role);
        navigateTo(defaultItem);
    }

    private void buildNavigationMenu(UserRole role) {
        navigationContainer.getChildren().clear();
        navButtons.clear();

        List<NavigationItem> items = NavigationItem.getItemsForRole(role);
        for (NavigationItem item : items) {
            Button btn = createNavButton(item);
            navButtons.put(item, btn);
            navigationContainer.getChildren().add(btn);
        }
    }

    private Button createNavButton(NavigationItem item) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("sidebar-nav-button");

        HBox graphicBox = new HBox(12.0);
        graphicBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(item.getIcon());
        iconLabel.getStyleClass().add("sidebar-nav-icon");

        Label textLabel = new Label(item.getTitle());
        textLabel.getStyleClass().add("sidebar-nav-text");

        graphicBox.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(graphicBox);

        btn.setOnAction(event -> navigateTo(item));
        return btn;
    }

    public boolean navigateTo(NavigationItem item) {
        if (item == null) return false;

        UserRole role = sessionManager.getRole();
        if (!item.isAccessibleBy(role)) {
            System.err.println("Access Denied: Role " + role + " is not authorized to access " + item.name());
            if (statusMessageLabel != null) {
                statusMessageLabel.setText("Access Denied: You do not have permissions for " + item.getTitle());
            }
            return false;
        }

        // Update active button state
        for (Map.Entry<NavigationItem, Button> entry : navButtons.entrySet()) {
            if (entry.getKey() == item) {
                if (!entry.getValue().getStyleClass().contains("sidebar-nav-button-active")) {
                    entry.getValue().getStyleClass().add("sidebar-nav-button-active");
                }
            } else {
                entry.getValue().getStyleClass().remove("sidebar-nav-button-active");
            }
        }

        // Load target page into central content area
        try {
            Parent pageRoot;
            if (item == NavigationItem.BROWSE_HOTELS) {
                URL fxmlUrl = getClass().getResource("/fxml/browse-hotels.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                pageRoot = loader.load();
            } else if (item == NavigationItem.MY_BOOKINGS) {
                URL fxmlUrl = getClass().getResource("/fxml/my-bookings.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                pageRoot = loader.load();
            } else if (item == NavigationItem.HOME) {
                URL fxmlUrl = getClass().getResource("/fxml/home-view.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                pageRoot = loader.load();
            } else {
                URL fxmlUrl = getClass().getResource("/fxml/placeholder-page.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                pageRoot = loader.load();

                PlaceholderPageController controller = loader.getController();
                controller.setNavigationItem(item, sessionManager.getCurrentUser());
            }

            contentArea.getChildren().setAll(pageRoot);
            currentItem = item;

            if (activePageLabel != null) {
                activePageLabel.setText("Current: " + item.getTitle());
            }
            if (statusMessageLabel != null) {
                statusMessageLabel.setText("Loaded " + item.getTitle() + " successfully.");
            }
            return true;

        } catch (IOException e) {
            System.err.println("Failed to load page for item: " + item.getTitle() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Replaces the central viewport content with an arbitrary node (e.g. HotelDetails view).
     */
    public void setContentNode(Parent node, String pageTitle) {
        if (contentArea != null && node != null) {
            contentArea.getChildren().setAll(node);
            if (activePageLabel != null) {
                activePageLabel.setText("Current: " + (pageTitle != null ? pageTitle : "Details"));
            }
            if (statusMessageLabel != null) {
                statusMessageLabel.setText("Viewing " + (pageTitle != null ? pageTitle : "Details"));
            }
        }
    }

    @FXML
    public void handleLogout() {
        authService.logout();
        contentArea.getChildren().clear();
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

    private String getPortalTitle(UserRole role) {
        return switch (role) {
            case ADMIN -> "ADMIN PORTAL";
            case HOTEL_OWNER -> "OWNER PORTAL";
            case USER -> "GUEST PORTAL";
        };
    }

    public NavigationItem getCurrentItem() {
        return currentItem;
    }

    public Map<NavigationItem, Button> getNavButtons() {
        return navButtons;
    }
}


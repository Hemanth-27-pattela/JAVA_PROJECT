package com.hotel.desktop.controller;

import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.RecommendationDto;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.HotelService;
import com.hotel.desktop.service.RecommendationService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Controller for the guest Home Recommendation Dashboard.
 * Asynchronously loads and presents Personalized, Popular, and Top-Rated hotel stays
 * powered by the Stage C recommendation REST APIs.
 */
public class HomeController {

    @FXML private Label welcomeHeading;
    @FXML private Label welcomeSubtitle;
    @FXML private Button browseHotelsButton;
    @FXML private Button myBookingsButton;
    @FXML private Button refreshAllButton;
    @FXML private ScrollPane scrollPane;

    // Section 1: Personalized Recommendations
    @FXML private Button refreshPersonalizedButton;
    @FXML private FlowPane personalizedContainer;
    @FXML private VBox personalizedLoadingBox;
    @FXML private VBox personalizedErrorBox;
    @FXML private Label personalizedErrorMessage;
    @FXML private VBox personalizedEmptyBox;

    // Section 2: Popular / Trending Hotels
    @FXML private Button refreshPopularButton;
    @FXML private FlowPane popularContainer;
    @FXML private VBox popularLoadingBox;
    @FXML private VBox popularErrorBox;
    @FXML private Label popularErrorMessage;
    @FXML private VBox popularEmptyBox;

    // Section 3: Top-Rated Stays
    @FXML private Button refreshTopRatedButton;
    @FXML private FlowPane topRatedContainer;
    @FXML private VBox topRatedLoadingBox;
    @FXML private VBox topRatedErrorBox;
    @FXML private Label topRatedErrorMessage;
    @FXML private VBox topRatedEmptyBox;

    private final RecommendationService recommendationService;
    private final SessionManager sessionManager;
    private final HotelService hotelService;

    private Task<List<RecommendationDto>> personalizedTask;
    private Task<List<RecommendationDto>> popularTask;
    private Task<List<RecommendationDto>> topRatedTask;

    public HomeController() {
        this(new RecommendationService(), SessionManager.getInstance(), new HotelService());
    }

    public HomeController(RecommendationService recommendationService, SessionManager sessionManager) {
        this(recommendationService, sessionManager, new HotelService());
    }

    public HomeController(RecommendationService recommendationService, SessionManager sessionManager, HotelService hotelService) {
        this.recommendationService = recommendationService;
        this.sessionManager = sessionManager;
        this.hotelService = hotelService;
    }

    @FXML
    public void initialize() {
        updateWelcomeHeader();
        loadAllSections();
    }

    private void updateWelcomeHeader() {
        if (welcomeHeading != null) {
            String name = sessionManager.getDisplayName();
            if (name != null && !name.isBlank()) {
                welcomeHeading.setText("Welcome back, " + name + "! ✨");
            } else {
                welcomeHeading.setText("Welcome to Your Travel Portal ✨");
            }
        }
    }

    @FXML
    public void handleRefreshAll() {
        loadAllSections();
    }

    public void loadAllSections() {
        loadPersonalizedRecommendations();
        loadPopularRecommendations();
        loadTopRatedRecommendations();
    }

    @FXML
    public void handleBrowseHotels() {
        ViewNavigator.navigateTo(NavigationItem.BROWSE_HOTELS);
    }

    @FXML
    public void handleMyBookings() {
        ViewNavigator.navigateTo(NavigationItem.MY_BOOKINGS);
    }

    @FXML
    public void handleLogout() {
        sessionManager.clearSession();
        ViewNavigator.showLoginView();
    }

    // =========================================================================
    // SECTION 1: PERSONALIZED RECOMMENDATIONS
    // =========================================================================

    @FXML
    public void loadPersonalizedRecommendations() {
        if (personalizedTask != null && personalizedTask.isRunning()) {
            personalizedTask.cancel();
        }

        showPersonalizedLoading();

        personalizedTask = new Task<>() {
            @Override
            protected List<RecommendationDto> call() throws Exception {
                return recommendationService.getPersonalizedRecommendations(6);
            }
        };

        personalizedTask.setOnSucceeded(e -> {
            List<RecommendationDto> results = personalizedTask.getValue();
            Platform.runLater(() -> renderPersonalizedResults(results));
        });

        personalizedTask.setOnFailed(e -> {
            if (personalizedTask.isCancelled()) return;
            Throwable ex = personalizedTask.getException();
            String msg = formatErrorMessage(ex);
            Platform.runLater(() -> showPersonalizedError(msg));
        });

        startDaemonThread(personalizedTask, "PersonalizedRecTask");
    }

    private void renderPersonalizedResults(List<RecommendationDto> results) {
        hidePersonalizedStates();
        if (results == null || results.isEmpty()) {
            if (personalizedEmptyBox != null) {
                personalizedEmptyBox.setVisible(true);
                personalizedEmptyBox.setManaged(true);
            }
            return;
        }

        if (personalizedContainer != null) {
            personalizedContainer.setVisible(true);
            personalizedContainer.setManaged(true);
            populateCards(personalizedContainer, results);
        }
    }

    private void showPersonalizedLoading() {
        hidePersonalizedStates();
        if (personalizedLoadingBox != null) {
            personalizedLoadingBox.setVisible(true);
            personalizedLoadingBox.setManaged(true);
        }
    }

    private void showPersonalizedError(String message) {
        hidePersonalizedStates();
        if (personalizedErrorBox != null) {
            personalizedErrorBox.setVisible(true);
            personalizedErrorBox.setManaged(true);
        }
        if (personalizedErrorMessage != null) {
            personalizedErrorMessage.setText(message);
        }
    }

    private void hidePersonalizedStates() {
        if (personalizedContainer != null) {
            personalizedContainer.setVisible(false);
            personalizedContainer.setManaged(false);
            personalizedContainer.getChildren().clear();
        }
        if (personalizedLoadingBox != null) {
            personalizedLoadingBox.setVisible(false);
            personalizedLoadingBox.setManaged(false);
        }
        if (personalizedErrorBox != null) {
            personalizedErrorBox.setVisible(false);
            personalizedErrorBox.setManaged(false);
        }
        if (personalizedEmptyBox != null) {
            personalizedEmptyBox.setVisible(false);
            personalizedEmptyBox.setManaged(false);
        }
    }

    // =========================================================================
    // SECTION 2: POPULAR RECOMMENDATIONS
    // =========================================================================

    @FXML
    public void loadPopularRecommendations() {
        if (popularTask != null && popularTask.isRunning()) {
            popularTask.cancel();
        }

        showPopularLoading();

        popularTask = new Task<>() {
            @Override
            protected List<RecommendationDto> call() throws Exception {
                return recommendationService.getPopularRecommendations(6);
            }
        };

        popularTask.setOnSucceeded(e -> {
            List<RecommendationDto> results = popularTask.getValue();
            Platform.runLater(() -> renderPopularResults(results));
        });

        popularTask.setOnFailed(e -> {
            if (popularTask.isCancelled()) return;
            Throwable ex = popularTask.getException();
            String msg = formatErrorMessage(ex);
            Platform.runLater(() -> showPopularError(msg));
        });

        startDaemonThread(popularTask, "PopularRecTask");
    }

    private void renderPopularResults(List<RecommendationDto> results) {
        hidePopularStates();
        if (results == null || results.isEmpty()) {
            if (popularEmptyBox != null) {
                popularEmptyBox.setVisible(true);
                popularEmptyBox.setManaged(true);
            }
            return;
        }

        if (popularContainer != null) {
            popularContainer.setVisible(true);
            popularContainer.setManaged(true);
            populateCards(popularContainer, results);
        }
    }

    private void showPopularLoading() {
        hidePopularStates();
        if (popularLoadingBox != null) {
            popularLoadingBox.setVisible(true);
            popularLoadingBox.setManaged(true);
        }
    }

    private void showPopularError(String message) {
        hidePopularStates();
        if (popularErrorBox != null) {
            popularErrorBox.setVisible(true);
            popularErrorBox.setManaged(true);
        }
        if (popularErrorMessage != null) {
            popularErrorMessage.setText(message);
        }
    }

    private void hidePopularStates() {
        if (popularContainer != null) {
            popularContainer.setVisible(false);
            popularContainer.setManaged(false);
            popularContainer.getChildren().clear();
        }
        if (popularLoadingBox != null) {
            popularLoadingBox.setVisible(false);
            popularLoadingBox.setManaged(false);
        }
        if (popularErrorBox != null) {
            popularErrorBox.setVisible(false);
            popularErrorBox.setManaged(false);
        }
        if (popularEmptyBox != null) {
            popularEmptyBox.setVisible(false);
            popularEmptyBox.setManaged(false);
        }
    }

    // =========================================================================
    // SECTION 3: TOP-RATED RECOMMENDATIONS
    // =========================================================================

    @FXML
    public void loadTopRatedRecommendations() {
        if (topRatedTask != null && topRatedTask.isRunning()) {
            topRatedTask.cancel();
        }

        showTopRatedLoading();

        topRatedTask = new Task<>() {
            @Override
            protected List<RecommendationDto> call() throws Exception {
                return recommendationService.getTopRatedRecommendations(6);
            }
        };

        topRatedTask.setOnSucceeded(e -> {
            List<RecommendationDto> results = topRatedTask.getValue();
            Platform.runLater(() -> renderTopRatedResults(results));
        });

        topRatedTask.setOnFailed(e -> {
            if (topRatedTask.isCancelled()) return;
            Throwable ex = topRatedTask.getException();
            String msg = formatErrorMessage(ex);
            Platform.runLater(() -> showTopRatedError(msg));
        });

        startDaemonThread(topRatedTask, "TopRatedRecTask");
    }

    private void renderTopRatedResults(List<RecommendationDto> results) {
        hideTopRatedStates();
        if (results == null || results.isEmpty()) {
            if (topRatedEmptyBox != null) {
                topRatedEmptyBox.setVisible(true);
                topRatedEmptyBox.setManaged(true);
            }
            return;
        }

        if (topRatedContainer != null) {
            topRatedContainer.setVisible(true);
            topRatedContainer.setManaged(true);
            populateCards(topRatedContainer, results);
        }
    }

    private void showTopRatedLoading() {
        hideTopRatedStates();
        if (topRatedLoadingBox != null) {
            topRatedLoadingBox.setVisible(true);
            topRatedLoadingBox.setManaged(true);
        }
    }

    private void showTopRatedError(String message) {
        hideTopRatedStates();
        if (topRatedErrorBox != null) {
            topRatedErrorBox.setVisible(true);
            topRatedErrorBox.setManaged(true);
        }
        if (topRatedErrorMessage != null) {
            topRatedErrorMessage.setText(message);
        }
    }

    private void hideTopRatedStates() {
        if (topRatedContainer != null) {
            topRatedContainer.setVisible(false);
            topRatedContainer.setManaged(false);
            topRatedContainer.getChildren().clear();
        }
        if (topRatedLoadingBox != null) {
            topRatedLoadingBox.setVisible(false);
            topRatedLoadingBox.setManaged(false);
        }
        if (topRatedErrorBox != null) {
            topRatedErrorBox.setVisible(false);
            topRatedErrorBox.setManaged(false);
        }
        if (topRatedEmptyBox != null) {
            topRatedEmptyBox.setVisible(false);
            topRatedEmptyBox.setManaged(false);
        }
    }

    // =========================================================================
    // CARD INFLATION & NAVIGATION
    // =========================================================================

    private void populateCards(FlowPane container, List<RecommendationDto> recommendations) {
        container.getChildren().clear();
        for (RecommendationDto rec : recommendations) {
            try {
                URL cardFxml = getClass().getResource("/fxml/recommendation-card.fxml");
                FXMLLoader loader = new FXMLLoader(cardFxml);
                Parent cardNode = loader.load();

                RecommendationCardController cardController = loader.getController();
                cardController.setRecommendation(rec, this::handleCardSelection);

                container.getChildren().add(cardNode);
            } catch (IOException e) {
                System.err.println("Failed to load recommendation-card.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleCardSelection(RecommendationDto rec) {
        if (rec == null) return;
        Hotel hotel = rec.toHotel();
        openHotelDetails(hotel);
    }

    private void openHotelDetails(Hotel hotel) {
        if (hotel == null) return;

        try {
            URL fxmlUrl = getClass().getResource("/fxml/hotel-details.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent detailsNode = loader.load();

            HotelDetailsController controller = loader.getController();
            controller.setHotel(hotel, () -> ViewNavigator.navigateTo(NavigationItem.HOME));

            MainLayoutController mainShell = ViewNavigator.getMainLayoutController();
            if (mainShell != null) {
                mainShell.setContentNode(detailsNode, hotel.getName());
            }
        } catch (IOException e) {
            System.err.println("Failed to load hotel details view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatErrorMessage(Throwable ex) {
        if (ex instanceof ApiException apiEx) {
            if (apiEx.getStatusCode() == 401) {
                return "Session expired or unauthorized. Please re-authenticate.";
            }
            return apiEx.getMessage();
        }
        return "Failed to communicate with recommendation service: " + ex.getMessage();
    }

    private void startDaemonThread(Runnable task, String threadName) {
        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    // Accessors for automated tests and visual verification
    public FlowPane getPersonalizedContainer() { return personalizedContainer; }
    public FlowPane getPopularContainer() { return popularContainer; }
    public FlowPane getTopRatedContainer() { return topRatedContainer; }
    public VBox getPersonalizedLoadingBox() { return personalizedLoadingBox; }
    public VBox getPersonalizedErrorBox() { return personalizedErrorBox; }
    public VBox getPersonalizedEmptyBox() { return personalizedEmptyBox; }
    public VBox getPopularLoadingBox() { return popularLoadingBox; }
    public VBox getTopRatedLoadingBox() { return topRatedLoadingBox; }
}

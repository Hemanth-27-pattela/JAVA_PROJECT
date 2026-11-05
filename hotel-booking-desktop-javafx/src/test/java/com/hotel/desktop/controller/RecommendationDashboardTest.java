package com.hotel.desktop.controller;

import com.hotel.desktop.model.RecommendationCategory;
import com.hotel.desktop.model.RecommendationDto;
import com.hotel.desktop.model.RecommendationExplanation;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.HotelService;
import com.hotel.desktop.service.RecommendationService;
import com.hotel.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationDashboardTest {

    private static final AtomicBoolean jfxStarted = new AtomicBoolean(false);

    private RecommendationService mockRecommendationService;
    private HotelService mockHotelService;
    private SessionManager sessionManager;
    private HomeController homeController;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> jfxStarted.set(true));
        } catch (IllegalStateException e) {
            // Toolkit already started
            jfxStarted.set(true);
        }
    }

    @BeforeEach
    void setUp() {
        mockRecommendationService = mock(RecommendationService.class);
        mockHotelService = mock(HotelService.class);
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();

        User guest = new User("guest-001", "guest@hotel.com", "Alice", "Smith", UserRole.USER);
        sessionManager.setSession("jwt-guest-token", guest);

        homeController = new HomeController(mockRecommendationService, sessionManager, mockHotelService);
    }

    @Test
    @DisplayName("HomeController initializes with guest greeting and dependency injection")
    void testHomeControllerInitialization() throws Exception {
        Label welcomeHeading = new Label();
        setPrivateField(homeController, "welcomeHeading", welcomeHeading);

        // Run on JavaFX thread
        runOnFxThread(() -> {
            // Reflectively invoke updateWelcomeHeader
            try {
                var method = HomeController.class.getDeclaredMethod("updateWelcomeHeader");
                method.setAccessible(true);
                method.invoke(homeController);
            } catch (Exception e) {
                fail("Failed to invoke updateWelcomeHeader: " + e.getMessage());
            }
        });

        assertEquals("Welcome back, Alice Smith! ✨", welcomeHeading.getText());
    }

    @Test
    @DisplayName("Personalized section shows empty state when service returns empty list")
    void testPersonalizedSectionEmptyState() throws Exception {
        when(mockRecommendationService.getPersonalizedRecommendations(anyInt()))
                .thenReturn(List.of());

        FlowPane container = new FlowPane();
        VBox emptyBox = new VBox();
        VBox loadingBox = new VBox();
        VBox errorBox = new VBox();

        setPrivateField(homeController, "personalizedContainer", container);
        setPrivateField(homeController, "personalizedEmptyBox", emptyBox);
        setPrivateField(homeController, "personalizedLoadingBox", loadingBox);
        setPrivateField(homeController, "personalizedErrorBox", errorBox);

        runOnFxThread(() -> {
            try {
                var method = HomeController.class.getDeclaredMethod("renderPersonalizedResults", List.class);
                method.setAccessible(true);
                method.invoke(homeController, List.of());
            } catch (Exception e) {
                fail("Failed to invoke renderPersonalizedResults: " + e.getMessage());
            }
        });

        assertTrue(emptyBox.isVisible());
        assertTrue(emptyBox.isManaged());
        assertFalse(container.isVisible());
    }

    @Test
    @DisplayName("Popular section renders cards when service returns recommendations")
    void testPopularSectionRendersCards() throws Exception {
        RecommendationDto dto = new RecommendationDto();
        dto.setId("hotel-p-1");
        dto.setName("Eiffel Elegance");
        dto.setCity("Paris");
        dto.setCountry("France");
        dto.setPricePerNight(BigDecimal.valueOf(210));
        dto.setStarRating(5);
        dto.setAverageRating(4.8);
        dto.setReviewCount(112);
        dto.setCategory(RecommendationCategory.POPULAR);
        dto.setExplanation(new RecommendationExplanation(
                "Popular choice in Paris", List.of("Central"), List.of("WiFi")
        ));

        when(mockRecommendationService.getPopularRecommendations(anyInt()))
                .thenReturn(List.of(dto));

        FlowPane popularContainer = new FlowPane();
        VBox emptyBox = new VBox();
        VBox loadingBox = new VBox();
        VBox errorBox = new VBox();

        setPrivateField(homeController, "popularContainer", popularContainer);
        setPrivateField(homeController, "popularEmptyBox", emptyBox);
        setPrivateField(homeController, "popularLoadingBox", loadingBox);
        setPrivateField(homeController, "popularErrorBox", errorBox);

        runOnFxThread(() -> {
            try {
                var method = HomeController.class.getDeclaredMethod("renderPopularResults", List.class);
                method.setAccessible(true);
                method.invoke(homeController, List.of(dto));
            } catch (Exception e) {
                fail("Failed to invoke renderPopularResults: " + e.getMessage());
            }
        });

        assertTrue(popularContainer.isVisible());
        assertFalse(emptyBox.isVisible());
        assertEquals(1, popularContainer.getChildren().size());
    }

    @Test
    @DisplayName("Top-Rated section displays error state on service failure")
    void testTopRatedSectionErrorState() throws Exception {
        FlowPane container = new FlowPane();
        VBox errorBox = new VBox();
        Label errorMsg = new Label();
        VBox loadingBox = new VBox();
        VBox emptyBox = new VBox();

        setPrivateField(homeController, "topRatedContainer", container);
        setPrivateField(homeController, "topRatedErrorBox", errorBox);
        setPrivateField(homeController, "topRatedErrorMessage", errorMsg);
        setPrivateField(homeController, "topRatedLoadingBox", loadingBox);
        setPrivateField(homeController, "topRatedEmptyBox", emptyBox);

        runOnFxThread(() -> {
            try {
                var method = HomeController.class.getDeclaredMethod("showTopRatedError", String.class);
                method.setAccessible(true);
                method.invoke(homeController, "Backend 500 error: Database unreachable");
            } catch (Exception e) {
                fail("Failed to invoke showTopRatedError: " + e.getMessage());
            }
        });

        assertTrue(errorBox.isVisible());
        assertTrue(errorBox.isManaged());
        assertEquals("Backend 500 error: Database unreachable", errorMsg.getText());
        assertFalse(container.isVisible());
    }

    @Test
    @DisplayName("Format error message correctly handles 401 session expiration")
    void testFormatErrorMessage401() throws Exception {
        ApiException authError = new ApiException(401, "JWT token expired");
        var method = HomeController.class.getDeclaredMethod("formatErrorMessage", Throwable.class);
        method.setAccessible(true);
        String msg = (String) method.invoke(homeController, authError);

        assertEquals("Session expired or unauthorized. Please re-authenticate.", msg);
    }

    private void runOnFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX thread operation timed out");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

package com.hotel.desktop.controller;

import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.RecommendationCategory;
import com.hotel.desktop.model.RecommendationDto;
import com.hotel.desktop.model.RecommendationExplanation;
import com.hotel.desktop.service.ApiException;
import com.hotel.desktop.service.BookingService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SimilarHotelsIntegrationTest {

    private static final AtomicBoolean jfxStarted = new AtomicBoolean(false);

    private BookingService mockBookingService;
    private SessionManager sessionManager;
    private RecommendationService mockRecommendationService;
    private HotelDetailsController controller;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> jfxStarted.set(true));
        } catch (IllegalStateException e) {
            jfxStarted.set(true);
        }
    }

    @BeforeEach
    void setUp() {
        mockBookingService = mock(BookingService.class);
        mockRecommendationService = mock(RecommendationService.class);
        sessionManager = SessionManager.getInstance();

        controller = new HotelDetailsController(mockBookingService, sessionManager, mockRecommendationService);
    }

    @Test
    @DisplayName("Similar hotels rendering filters out current displayed hotel")
    void testSimilarHotelsExcludesCurrentHotel() throws Exception {
        Hotel currentHotel = new Hotel("hotel-cur-1", "Current Hotel", "London", "UK", BigDecimal.valueOf(200), 5);
        setPrivateField(controller, "hotel", currentHotel);

        FlowPane container = new FlowPane();
        VBox emptyBox = new VBox();
        VBox loadingBox = new VBox();
        VBox errorBox = new VBox();

        setPrivateField(controller, "similarHotelsContainer", container);
        setPrivateField(controller, "similarEmptyBox", emptyBox);
        setPrivateField(controller, "similarLoadingBox", loadingBox);
        setPrivateField(controller, "similarErrorBox", errorBox);

        // Results contain the current hotel ID and another hotel
        RecommendationDto sameDto = createDto("hotel-cur-1", "Current Hotel");
        RecommendationDto otherDto = createDto("hotel-other-2", "Other Similar Stay");

        runOnFxThread(() -> {
            try {
                var method = HotelDetailsController.class.getDeclaredMethod("renderSimilarResults", List.class);
                method.setAccessible(true);
                method.invoke(controller, List.of(sameDto, otherDto));
            } catch (Exception e) {
                fail("Failed to invoke renderSimilarResults: " + e.getMessage());
            }
        });

        // The current hotel must be excluded, leaving only 1 card
        assertEquals(1, container.getChildren().size());
        assertTrue(container.isVisible());
        assertFalse(emptyBox.isVisible());
    }

    @Test
    @DisplayName("Similar hotels shows empty state when no recommendations returned")
    void testSimilarHotelsEmptyState() throws Exception {
        Hotel currentHotel = new Hotel("hotel-cur-1", "Current Hotel", "London", "UK", BigDecimal.valueOf(200), 5);
        setPrivateField(controller, "hotel", currentHotel);

        FlowPane container = new FlowPane();
        VBox emptyBox = new VBox();
        VBox loadingBox = new VBox();
        VBox errorBox = new VBox();

        setPrivateField(controller, "similarHotelsContainer", container);
        setPrivateField(controller, "similarEmptyBox", emptyBox);
        setPrivateField(controller, "similarLoadingBox", loadingBox);
        setPrivateField(controller, "similarErrorBox", errorBox);

        runOnFxThread(() -> {
            try {
                var method = HotelDetailsController.class.getDeclaredMethod("renderSimilarResults", List.class);
                method.setAccessible(true);
                method.invoke(controller, List.of());
            } catch (Exception e) {
                fail("Failed to invoke renderSimilarResults: " + e.getMessage());
            }
        });

        assertTrue(emptyBox.isVisible());
        assertTrue(emptyBox.isManaged());
        assertFalse(container.isVisible());
    }

    @Test
    @DisplayName("Similar hotels shows error state with message when service fails")
    void testSimilarHotelsErrorState() throws Exception {
        FlowPane container = new FlowPane();
        VBox errorBox = new VBox();
        Label errorMsg = new Label();
        VBox loadingBox = new VBox();
        VBox emptyBox = new VBox();

        setPrivateField(controller, "similarHotelsContainer", container);
        setPrivateField(controller, "similarErrorBox", errorBox);
        setPrivateField(controller, "similarErrorMessage", errorMsg);
        setPrivateField(controller, "similarLoadingBox", loadingBox);
        setPrivateField(controller, "similarEmptyBox", emptyBox);

        runOnFxThread(() -> {
            try {
                var method = HotelDetailsController.class.getDeclaredMethod("showSimilarError", String.class);
                method.setAccessible(true);
                method.invoke(controller, "Connection refused: backend offline");
            } catch (Exception e) {
                fail("Failed to invoke showSimilarError: " + e.getMessage());
            }
        });

        assertTrue(errorBox.isVisible());
        assertTrue(errorBox.isManaged());
        assertEquals("Connection refused: backend offline", errorMsg.getText());
        assertFalse(container.isVisible());
        assertFalse(loadingBox.isVisible());
    }

    @Test
    @DisplayName("loadSimilarHotels handles null or blank hotelId gracefully without error")
    void testLoadSimilarHotelsNullHotelId() throws Exception {
        VBox emptyBox = new VBox();
        setPrivateField(controller, "similarEmptyBox", emptyBox);

        // Hotel with null ID
        setPrivateField(controller, "hotel", new Hotel(null, "Test", "City", "Country", BigDecimal.ONE, 3));

        runOnFxThread(() -> controller.loadSimilarHotels());

        verifyNoInteractions(mockRecommendationService);
        assertTrue(emptyBox.isVisible());
    }

    @Test
    @DisplayName("Chained back navigation preserves original action hierarchy")
    void testChainedBackNavigationHierarchy() {
        AtomicBoolean originNavigated = new AtomicBoolean(false);
        Runnable rootBackAction = () -> originNavigated.set(true);

        Hotel hotelA = new Hotel("hotel-A", "Hotel A", "London", "UK", BigDecimal.valueOf(180), 4);
        controller.setHotel(hotelA, rootBackAction);

        assertEquals(hotelA, controller.getHotel());
        assertNotNull(controller.getBackAction());

        // Trigger backAction on hotelA directly -> must trigger rootBackAction
        controller.getBackAction().run();
        assertTrue(originNavigated.get());
    }

    private RecommendationDto createDto(String id, String name) {
        RecommendationDto dto = new RecommendationDto();
        dto.setId(id);
        dto.setName(name);
        dto.setCity("London");
        dto.setCountry("United Kingdom");
        dto.setPricePerNight(BigDecimal.valueOf(190));
        dto.setStarRating(4);
        dto.setAverageRating(4.8);
        dto.setReviewCount(75);
        dto.setCategory(RecommendationCategory.SIMILAR);
        dto.setExplanation(new RecommendationExplanation(
                "Similar luxury style and amenities in London",
                List.of("Within matching price tier £150-£220", "Features Spa and Pool"),
                List.of("Spa", "Pool", "Luxury")
        ));
        return dto;
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
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}


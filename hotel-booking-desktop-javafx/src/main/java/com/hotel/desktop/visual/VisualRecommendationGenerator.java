package com.hotel.desktop.visual;

import com.hotel.desktop.controller.HomeController;
import com.hotel.desktop.controller.MainLayoutController;
import com.hotel.desktop.controller.RecommendationCardController;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.RecommendationCategory;
import com.hotel.desktop.model.RecommendationDto;
import com.hotel.desktop.model.RecommendationExplanation;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.ApiClient;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.RecommendationService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.ViewNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Visual verification runner for Phase 9.7 Stage D: JavaFX Recommendation Dashboard.
 * Renders the Home Recommendation Dashboard with Personalized, Popular, and Top-Rated
 * sections inside the authenticated guest shell, and captures visual evidence PNGs.
 */
public class VisualRecommendationGenerator extends Application {

    private static final String CSS_PATH = "/css/application.css";
    private static final String ARTIFACT_DIR = "C:/Users/patte/.gemini/antigravity/brain/e308dbfa-266e-4ee2-b0fc-7026d4faacc9";
    private static final String PROJECT_SCREENSHOTS_DIR = "D:/HOTEL_PROJECT/Hotel-Booking-Management-System--React-MERN-FullStack/hotel-booking-desktop-javafx/screenshots";

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
                System.out.println("PHASE 9.7 STAGE D: RECOMMENDATION DASHBOARD VISUAL CAPTURE");
                System.out.println("=============================================================");

                // 1. Establish Guest Session
                sessionManager.clearSession();
                User guestUser = new User("guest-rec-001", "guest@hotel.com", "Sarah", "Connor", UserRole.USER);
                sessionManager.setSession("simulated-guest-jwt-token-9.7", guestUser);
                System.out.println("Active Session: " + guestUser.getFullName() + " (" + guestUser.getEmail() + ")");

                // 2. Load Main Shell on JavaFX Thread
                CountDownLatch shellLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        URL fxmlUrl = getClass().getResource("/fxml/main-layout.fxml");
                        FXMLLoader loader = new FXMLLoader(fxmlUrl);
                        Parent root = loader.load();

                        Scene scene = new Scene(root, 1280, 800);
                        URL cssUrl = getClass().getResource(CSS_PATH);
                        if (cssUrl != null) {
                            scene.getStylesheets().add(cssUrl.toExternalForm());
                        }

                        stage.setScene(scene);
                        stage.setTitle("Hotel Booking Management System - Curated Stays");
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        shellLatch.countDown();
                    }
                });
                shellLatch.await(5, TimeUnit.SECONDS);
                Thread.sleep(800);

                // 3. Inject Mock / Curated Recommendation Data for Visual Consistency
                CountDownLatch dataLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        MainLayoutController mainShell = ViewNavigator.getMainLayoutController();
                        if (mainShell != null) {
                            URL homeUrl = getClass().getResource("/fxml/home-view.fxml");
                            FXMLLoader homeLoader = new FXMLLoader(homeUrl);
                            Parent homeNode = homeLoader.load();
                            HomeController homeCtrl = homeLoader.getController();

                            // Populate Personalized
                            populateSection(homeCtrl.getPersonalizedContainer(), createPersonalizedSampleHotels());
                            homeCtrl.getPersonalizedContainer().setVisible(true);
                            homeCtrl.getPersonalizedContainer().setManaged(true);
                            if (homeCtrl.getPersonalizedLoadingBox() != null) {
                                homeCtrl.getPersonalizedLoadingBox().setVisible(false);
                                homeCtrl.getPersonalizedLoadingBox().setManaged(false);
                            }

                            // Populate Popular
                            populateSection(homeCtrl.getPopularContainer(), createPopularSampleHotels());
                            homeCtrl.getPopularContainer().setVisible(true);
                            homeCtrl.getPopularContainer().setManaged(true);
                            if (homeCtrl.getPopularLoadingBox() != null) {
                                homeCtrl.getPopularLoadingBox().setVisible(false);
                                homeCtrl.getPopularLoadingBox().setManaged(false);
                            }

                            // Populate Top Rated
                            populateSection(homeCtrl.getTopRatedContainer(), createTopRatedSampleHotels());
                            homeCtrl.getTopRatedContainer().setVisible(true);
                            homeCtrl.getTopRatedContainer().setManaged(true);
                            if (homeCtrl.getTopRatedLoadingBox() != null) {
                                homeCtrl.getTopRatedLoadingBox().setVisible(false);
                                homeCtrl.getTopRatedLoadingBox().setManaged(false);
                            }

                            mainShell.setContentNode(homeNode, "Home");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        dataLatch.countDown();
                    }
                });
                dataLatch.await(5, TimeUnit.SECONDS);
                Thread.sleep(1200);

                // 4. Capture Overview Dashboard
                captureStage(stage, "guest-recommendation-dashboard.png");
                System.out.println("-> Captured: guest-recommendation-dashboard.png (3 recommendation sections with badges & explanations)");
                Thread.sleep(600);

                // 5. Expand first card's explanation toggle to showcase detailed reasoning
                CountDownLatch expandLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        MainLayoutController mainShell = ViewNavigator.getMainLayoutController();
                        if (mainShell != null) {
                            // Find toggle button in first card
                            // Scene has the node
                            Parent root = stage.getScene().getRoot();
                            Button toggleBtn = (Button) root.lookup(".rec-details-toggle");
                            if (toggleBtn != null) {
                                toggleBtn.fire();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        expandLatch.countDown();
                    }
                });
                expandLatch.await(5, TimeUnit.SECONDS);
                Thread.sleep(800);

                // 6. Capture with details expanded
                captureStage(stage, "guest-recommendation-details-expanded.png");
                System.out.println("-> Captured: guest-recommendation-details-expanded.png (Card with expanded supporting reasons & features)");
                Thread.sleep(600);

                // 7. Navigate to Hotel Details with Similar Hotels Section
                CountDownLatch detailsLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        MainLayoutController mainShell = ViewNavigator.getMainLayoutController();
                        if (mainShell != null) {
                            URL detailsUrl = getClass().getResource("/fxml/hotel-details.fxml");
                            FXMLLoader detailsLoader = new FXMLLoader(detailsUrl);
                            Parent detailsNode = detailsLoader.load();
                            com.hotel.desktop.controller.HotelDetailsController detailsCtrl = detailsLoader.getController();

                            com.hotel.desktop.model.Hotel sampleHotel = new com.hotel.desktop.model.Hotel("hotel-cur-1", "Grand Westminster Palace", "London", "United Kingdom", BigDecimal.valueOf(240), 5);
                            sampleHotel.setDescription("A premier heritage palace overlooking the River Thames with luxury suites, full-service wellness spa, and fine dining.");
                            sampleHotel.setType(List.of("Luxury", "Heritage"));
                            sampleHotel.setFacilities(List.of("Spa & Wellness", "Indoor Pool", "Free WiFi", "Fine Dining"));
                            sampleHotel.setAverageRating(4.9);
                            sampleHotel.setReviewCount(168);
                            sampleHotel.setAdultCount(2);
                            sampleHotel.setChildCount(1);

                            detailsCtrl.setHotel(sampleHotel, () -> {
                                System.out.println("Returned to Home from details");
                            });

                            // Populate similar hotels
                            populateSection(detailsCtrl.getSimilarHotelsContainer(), createSimilarSampleHotels());
                            detailsCtrl.getSimilarHotelsContainer().setVisible(true);
                            detailsCtrl.getSimilarHotelsContainer().setManaged(true);
                            if (detailsCtrl.getSimilarLoadingBox() != null) {
                                detailsCtrl.getSimilarLoadingBox().setVisible(false);
                                detailsCtrl.getSimilarLoadingBox().setManaged(false);
                            }

                            mainShell.setContentNode(detailsNode, sampleHotel.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        detailsLatch.countDown();
                    }
                });
                detailsLatch.await(5, TimeUnit.SECONDS);
                Thread.sleep(1200);

                // Scroll down to Similar Hotels section to capture it clearly
                CountDownLatch scrollLatch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        Parent root = stage.getScene().getRoot();
                        javafx.scene.control.ScrollPane sp = (javafx.scene.control.ScrollPane) root.lookup(".hotel-scroll-pane");
                        if (sp != null) {
                            sp.setVvalue(1.0); // scroll to bottom where Similar Hotels is
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        scrollLatch.countDown();
                    }
                });
                scrollLatch.await(5, TimeUnit.SECONDS);
                Thread.sleep(800);

                // 8. Capture Hotel Details with Similar Hotels
                captureStage(stage, "guest-hotel-details-similar-hotels.png");
                System.out.println("-> Captured: guest-hotel-details-similar-hotels.png (Hotel Details screen with Similar Hotels section)");
                Thread.sleep(600);

                System.out.println("\nAll recommendation dashboard and similar hotel screenshots captured successfully!");
                Platform.exit();
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("Fatal error during screenshot capture: " + ex.getMessage());
                ex.printStackTrace();
                Platform.exit();
                System.exit(1);
            }
        }).start();
    }

    private void populateSection(FlowPane container, List<RecommendationDto> dtos) {
        if (container == null) return;
        container.getChildren().clear();
        for (RecommendationDto rec : dtos) {
            try {
                URL cardUrl = getClass().getResource("/fxml/recommendation-card.fxml");
                FXMLLoader loader = new FXMLLoader(cardUrl);
                Parent node = loader.load();
                RecommendationCardController ctrl = loader.getController();
                ctrl.setRecommendation(rec, hotel -> {
                    System.out.println("Selected hotel: " + hotel.getName());
                });
                container.getChildren().add(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<RecommendationDto> createPersonalizedSampleHotels() {
        List<RecommendationDto> list = new ArrayList<>();

        RecommendationDto r1 = new RecommendationDto();
        r1.setId("rec-p-1");
        r1.setName("Grand Westminster Palace");
        r1.setCity("London");
        r1.setCountry("United Kingdom");
        r1.setPricePerNight(BigDecimal.valueOf(240));
        r1.setStarRating(5);
        r1.setAverageRating(4.9);
        r1.setReviewCount(168);
        r1.setScore(0.965);
        r1.setCategory(RecommendationCategory.PERSONALIZED);
        r1.setType(List.of("Luxury", "Heritage"));
        r1.setFacilities(List.of("Spa & Wellness", "Indoor Pool", "Free WiFi"));
        r1.setExplanation(new RecommendationExplanation(
                "Matched to your stay in London with 4.9/5 guest satisfaction",
                List.of("Matches your historical budget range £180-£260", "Features requested luxury wellness facilities"),
                List.of("Spa", "Indoor Pool", "Free WiFi", "Heritage")
        ));
        list.add(r1);

        RecommendationDto r2 = new RecommendationDto();
        r2.setId("rec-p-2");
        r2.setName("Champs-Élysées Luxury Boutique");
        r2.setCity("Paris");
        r2.setCountry("France");
        r2.setPricePerNight(BigDecimal.valueOf(215));
        r2.setStarRating(5);
        r2.setAverageRating(4.85);
        r2.setReviewCount(142);
        r2.setScore(0.923);
        r2.setCategory(RecommendationCategory.PERSONALIZED);
        r2.setType(List.of("Boutique", "Romantic"));
        r2.setFacilities(List.of("Fine Dining", "Airport Shuttle", "Fitness Center"));
        r2.setExplanation(new RecommendationExplanation(
                "Similar boutique style and luxury tier to your preferred stays",
                List.of("Prime central district location", "Highly rated breakfast and concierge service"),
                List.of("Fine Dining", "Fitness Center", "Boutique")
        ));
        list.add(r2);

        RecommendationDto r3 = new RecommendationDto();
        r3.setId("rec-p-3");
        r3.setName("The Grand Canal Venetian Suite");
        r3.setCity("Venice");
        r3.setCountry("Italy");
        r3.setPricePerNight(BigDecimal.valueOf(280));
        r3.setStarRating(5);
        r3.setAverageRating(4.88);
        r3.setReviewCount(120);
        r3.setScore(0.895);
        r3.setCategory(RecommendationCategory.PERSONALIZED);
        r3.setType(List.of("Historic", "Luxury"));
        r3.setFacilities(List.of("Waterfront", "Gourmet Bar", "Room Service"));
        r3.setExplanation(new RecommendationExplanation(
                "Exclusive European destination matching your high-rating preference",
                List.of("Overlooks iconic waterways", "Exceptional 4.88/5 guest cleanliness rating"),
                List.of("Waterfront", "Historic", "Room Service")
        ));
        list.add(r3);

        return list;
    }

    private List<RecommendationDto> createPopularSampleHotels() {
        List<RecommendationDto> list = new ArrayList<>();

        RecommendationDto r1 = new RecommendationDto();
        r1.setId("rec-pop-1");
        r1.setName("Tokyo Shinjuku Central Tower");
        r1.setCity("Tokyo");
        r1.setCountry("Japan");
        r1.setPricePerNight(BigDecimal.valueOf(175));
        r1.setStarRating(4);
        r1.setAverageRating(4.7);
        r1.setReviewCount(245);
        r1.setScore(0.94);
        r1.setCategory(RecommendationCategory.POPULAR);
        r1.setType(List.of("Business", "Modern"));
        r1.setFacilities(List.of("High-Speed WiFi", "Metro Access", "Sky Lounge"));
        r1.setExplanation(new RecommendationExplanation(
                "Most frequently booked destination with 245 verified stays this season",
                List.of("Exceptional transit connectivity", "Popular among international business and leisure travelers"),
                List.of("Metro Access", "Sky Lounge", "WiFi")
        ));
        list.add(r1);

        RecommendationDto r2 = new RecommendationDto();
        r2.setId("rec-pop-2");
        r2.setName("Manhattan Midtown View Hotel");
        r2.setCity("New York");
        r2.setCountry("United States");
        r2.setPricePerNight(BigDecimal.valueOf(230));
        r2.setStarRating(4);
        r2.setAverageRating(4.65);
        r2.setReviewCount(310);
        r2.setScore(0.91);
        r2.setCategory(RecommendationCategory.POPULAR);
        r2.setType(List.of("City Center", "Family"));
        r2.setFacilities(List.of("Rooftop Bar", "Valet Parking", "Fitness Center"));
        r2.setExplanation(new RecommendationExplanation(
                "Trending choice in Midtown Manhattan with consistent guest volume",
                List.of("Within walking distance of Central Park and Broadway", "High demand during upcoming holiday dates"),
                List.of("Rooftop Bar", "Fitness Center", "Valet")
        ));
        list.add(r2);

        RecommendationDto r3 = new RecommendationDto();
        r3.setId("rec-pop-3");
        r3.setName("Marina Bay Sands View Suites");
        r3.setCity("Singapore");
        r3.setCountry("Singapore");
        r3.setPricePerNight(BigDecimal.valueOf(290));
        r3.setStarRating(5);
        r3.setAverageRating(4.82);
        r3.setReviewCount(190);
        r3.setScore(0.88);
        r3.setCategory(RecommendationCategory.POPULAR);
        r3.setType(List.of("Resort", "Luxury"));
        r3.setFacilities(List.of("Infinity Pool", "Spa", "Fine Dining"));
        r3.setExplanation(new RecommendationExplanation(
                "Top-ranking destination booked 42 times this month",
                List.of("Famous skyline view and world-class infinity pool", "Consistently rated 5-star service standard"),
                List.of("Infinity Pool", "Spa", "Fine Dining")
        ));
        list.add(r3);

        return list;
    }

    private List<RecommendationDto> createTopRatedSampleHotels() {
        List<RecommendationDto> list = new ArrayList<>();

        RecommendationDto r1 = new RecommendationDto();
        r1.setId("rec-top-1");
        r1.setName("The Kyoto Ryokan Sanctuary");
        r1.setCity("Kyoto");
        r1.setCountry("Japan");
        r1.setPricePerNight(BigDecimal.valueOf(320));
        r1.setStarRating(5);
        r1.setAverageRating(4.98);
        r1.setReviewCount(94);
        r1.setScore(0.978);
        r1.setCategory(RecommendationCategory.TOP_RATED);
        r1.setType(List.of("Traditional", "Luxury"));
        r1.setFacilities(List.of("Hot Spring Onsen", "Garden", "Kaiseki Dinner"));
        r1.setExplanation(new RecommendationExplanation(
                "Highest Bayesian rating (4.98/5.0) with unanimous guest acclaim",
                List.of("100% positive verified reviews for hospitality and tranquil gardens", "Award-winning traditional onsen experience"),
                List.of("Hot Spring Onsen", "Garden", "Kaiseki")
        ));
        list.add(r1);

        RecommendationDto r2 = new RecommendationDto();
        r2.setId("rec-top-2");
        r2.setName("Amalfi Cliffside Boutique Villa");
        r2.setCity("Positano");
        r2.setCountry("Italy");
        r2.setPricePerNight(BigDecimal.valueOf(350));
        r2.setStarRating(5);
        r2.setAverageRating(4.95);
        r2.setReviewCount(118);
        r2.setScore(0.965);
        r2.setCategory(RecommendationCategory.TOP_RATED);
        r2.setType(List.of("Boutique", "Villa"));
        r2.setFacilities(List.of("Sea View Terrace", "Private Beach", "Wine Cellar"));
        r2.setExplanation(new RecommendationExplanation(
                "Top-rated Mediterranean retreat with 4.95/5 score across 118 verified reviews",
                List.of("Spectacular private beach access and cliffside panoramas", "Flawless ratings for cleanliness and dining"),
                List.of("Sea View", "Private Beach", "Wine Cellar")
        ));
        list.add(r2);

        RecommendationDto r3 = new RecommendationDto();
        r3.setId("rec-top-3");
        r3.setName("Swiss Alpine Chalet & Spa");
        r3.setCity("Zermatt");
        r3.setCountry("Switzerland");
        r3.setPricePerNight(BigDecimal.valueOf(310));
        r3.setStarRating(5);
        r3.setAverageRating(4.92);
        r3.setReviewCount(85);
        r3.setScore(0.952);
        r3.setCategory(RecommendationCategory.TOP_RATED);
        r3.setType(List.of("Chalet", "Resort"));
        r3.setFacilities(List.of("Ski-in/Ski-out", "Heated Pool", "Matterhorn View"));
        r3.setExplanation(new RecommendationExplanation(
                "Celebrated Alpine resort boasting 4.92/5 verified rating",
                List.of("Direct ski slope access and unobstructed Matterhorn vistas", "High repeat-guest loyalty score"),
                List.of("Ski-in/Ski-out", "Heated Pool", "Spa")
        ));
        list.add(r3);

        return list;
    }

    private List<RecommendationDto> createSimilarSampleHotels() {
        List<RecommendationDto> list = new ArrayList<>();

        RecommendationDto s1 = new RecommendationDto();
        s1.setId("rec-sim-1");
        s1.setName("The Savoy Riverside Hotel");
        s1.setCity("London");
        s1.setCountry("United Kingdom");
        s1.setPricePerNight(BigDecimal.valueOf(265));
        s1.setStarRating(5);
        s1.setAverageRating(4.92);
        s1.setReviewCount(156);
        s1.setScore(0.95);
        s1.setCategory(RecommendationCategory.SIMILAR);
        s1.setType(List.of("Luxury", "Historic"));
        s1.setFacilities(List.of("Spa & Wellness", "Indoor Pool", "Fine Dining"));
        s1.setExplanation(new RecommendationExplanation(
                "Similar luxury standard and riverside location in London",
                List.of("Matches your 5-star standard and budget tier £200-£280", "High rating of 4.92/5 across 156 reviews"),
                List.of("Spa", "Indoor Pool", "Historic", "Luxury")
        ));
        list.add(s1);

        RecommendationDto s2 = new RecommendationDto();
        s2.setId("rec-sim-2");
        s2.setName("Mayfair Heritage Hotel & Suites");
        s2.setCity("London");
        s2.setCountry("United Kingdom");
        s2.setPricePerNight(BigDecimal.valueOf(250));
        s2.setStarRating(5);
        s2.setAverageRating(4.88);
        s2.setReviewCount(138);
        s2.setScore(0.93);
        s2.setCategory(RecommendationCategory.SIMILAR);
        s2.setType(List.of("Heritage", "Boutique"));
        s2.setFacilities(List.of("Concierge", "Fitness Center", "High-Speed WiFi"));
        s2.setExplanation(new RecommendationExplanation(
                "Located in premier central district with shared heritage styling",
                List.of("Within 1.5 miles of current property", "Top-tier cleanliness and service score"),
                List.of("Concierge", "Fitness Center", "Heritage")
        ));
        list.add(s2);

        RecommendationDto s3 = new RecommendationDto();
        s3.setId("rec-sim-3");
        s3.setName("Kensington Boutique Palace");
        s3.setCity("London");
        s3.setCountry("United Kingdom");
        s3.setPricePerNight(BigDecimal.valueOf(225));
        s3.setStarRating(4);
        s3.setAverageRating(4.80);
        s3.setReviewCount(98);
        s3.setScore(0.89);
        s3.setCategory(RecommendationCategory.SIMILAR);
        s3.setType(List.of("Boutique", "Romantic"));
        s3.setFacilities(List.of("Garden", "Bar", "Free WiFi"));
        s3.setExplanation(new RecommendationExplanation(
                "Popular alternative offering intimate garden luxury",
                List.of("Slightly more accessible nightly rate", "Celebrated boutique breakfast"),
                List.of("Garden", "Bar", "Boutique")
        ));
        list.add(s3);

        return list;
    }

    private void captureStage(Stage stage, String filename) {
        CountDownLatch latch = new CountDownLatch(1);
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

                File artifactFile = new File(ARTIFACT_DIR, filename);
                ImageIO.write(bi, "png", artifactFile);

                File projectFile = new File(PROJECT_SCREENSHOTS_DIR, filename);
                ImageIO.write(bi, "png", projectFile);

            } catch (Exception e) {
                System.err.println("Error capturing " + filename + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }
}

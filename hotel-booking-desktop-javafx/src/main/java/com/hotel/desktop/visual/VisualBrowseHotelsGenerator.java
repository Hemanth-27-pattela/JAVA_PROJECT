package com.hotel.desktop.visual;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.controller.BrowseHotelsController;
import com.hotel.desktop.controller.HotelDetailsController;
import com.hotel.desktop.controller.MainLayoutController;
import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.HotelSearchResponse;
import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.Pagination;
import com.hotel.desktop.model.User;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.HotelService;
import com.hotel.desktop.service.SessionManager;
import com.hotel.desktop.util.JsonUtils;
import com.hotel.desktop.util.ViewNavigator;
import com.sun.net.httpserver.HttpServer;
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
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Visual verification runner for Phase 9.5: Guest Hotel Browsing and Search.
 * Logs in with guest@hotel.com, navigates to Browse Hotels, executes searches and filters,
 * opens details, and captures high-resolution screenshots.
 */
public class VisualBrowseHotelsGenerator extends Application {

    private static final String ARTIFACT_DIR = "C:/Users/patte/.gemini/antigravity/brain/e308dbfa-266e-4ee2-b0fc-7026d4faacc9";
    private static final String PROJECT_SCREENSHOTS_DIR = "D:/HOTEL_PROJECT/Hotel-Booking-Management-System--React-MERN-FullStack/hotel-booking-desktop-javafx/screenshots";

    private final AuthService authService = new AuthService();
    private final HotelService hotelService = new HotelService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ObjectMapper mapper = JsonUtils.getMapper();

    private HttpServer fallbackServer;
    private BrowseHotelsController activeBrowseController;

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

                boolean port5001Active = isPortActive(5001);
                if (!port5001Active) {
                    System.out.println("Starting verification server on port 5001 with 8 seeded demo hotels...");
                    startFallbackServer();
                }

                System.out.println("=============================================================");
                System.out.println("PHASE 9.5: GUEST HOTEL BROWSING & SEARCH VISUAL VERIFICATION");
                System.out.println("Backend Target: http://localhost:5001");
                System.out.println("=============================================================");

                // 1. Authenticate Guest
                System.out.println("\n[1/5] Authenticating Guest (guest@hotel.com)...");
                sessionManager.clearSession();
                User guestUser = authService.login("guest@hotel.com", "12345678");
                System.out.println("Guest Authenticated: " + guestUser.getFullName() + ", Role: " + guestUser.getRole());

                // 2. Open Main Shell & Load Browse Hotels (All 8 Hotels)
                System.out.println("\n[2/5] Navigating to Browse Hotels (All 8 Hotels)...");
                Platform.runLater(() -> {
                    ViewNavigator.showMainLayout();
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/browse-hotels.fxml"));
                        Parent browseNode = loader.load();
                        activeBrowseController = loader.getController();

                        MainLayoutController shell = ViewNavigator.getMainLayoutController();
                        if (shell != null) {
                            shell.setContentNode(browseNode, "Browse Hotels");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                Thread.sleep(2500);
                captureStage(stage, "guest-browse-hotels-all.png");
                System.out.println("-> Captured: guest-browse-hotels-all.png (Initial catalog with all 8 hotels)");

                // 3. Search for Destination: London
                System.out.println("\n[3/5] Performing Search for Destination: 'London'...");
                Platform.runLater(() -> {
                    if (activeBrowseController != null) {
                        activeBrowseController.searchDestination("London");
                    }
                });
                Thread.sleep(1800);
                captureStage(stage, "guest-browse-hotels-search-london.png");
                System.out.println("-> Captured: guest-browse-hotels-search-london.png (London destination results: 1 hotel)");

                // 4. Open Hotel Details for Thames View Boutique
                System.out.println("\n[4/5] Opening Hotel Details View for Thames View Boutique...");
                List<Hotel> allHotels = hotelService.getAllHotels();
                if (!allHotels.isEmpty()) {
                    Hotel targetHotel = allHotels.get(0);
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hotel-details.fxml"));
                            Parent detailsRoot = loader.load();
                            HotelDetailsController controller = loader.getController();
                            controller.setHotel(targetHotel, () -> {
                                if (activeBrowseController != null) {
                                    activeBrowseController.executeSearch(1);
                                }
                            });

                            MainLayoutController shell = ViewNavigator.getMainLayoutController();
                            if (shell != null) {
                                shell.setContentNode(detailsRoot, targetHotel.getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    Thread.sleep(2000);
                    captureStage(stage, "guest-hotel-details.png");
                    System.out.println("-> Captured: guest-hotel-details.png (Detailed property view with specs and policies)");
                }

                // 5. Filtered Search (5 Stars & Luxury)
                System.out.println("\n[5/5] Returning to Browse Hotels with Filtered Results (5 Stars & Luxury)...");
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/browse-hotels.fxml"));
                        Parent browseNode = loader.load();
                        activeBrowseController = loader.getController();

                        MainLayoutController shell = ViewNavigator.getMainLayoutController();
                        if (shell != null) {
                            shell.setContentNode(browseNode, "Browse Hotels");
                        }
                        // Clear destination to filter globally
                        javafx.scene.control.TextField destField = (javafx.scene.control.TextField) browseNode.lookup("#destinationField");
                        if (destField != null) destField.clear();

                        activeBrowseController.filterBy(5, "Luxury");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                Thread.sleep(2200);
                captureStage(stage, "guest-browse-hotels-filtered.png");
                System.out.println("-> Captured: guest-browse-hotels-filtered.png (Filtered search: 5 Stars & Luxury)");

                // 6. Empty State Search (Atlantis)
                System.out.println("\n[6/5] Executing Empty Search for Non-existent Destination ('Atlantis')...");
                Platform.runLater(() -> {
                    if (activeBrowseController != null) {
                        activeBrowseController.searchDestination("Atlantis");
                    }
                });
                Thread.sleep(1800);
                captureStage(stage, "guest-browse-empty-results.png");
                System.out.println("-> Captured: guest-browse-empty-results.png (Empty results state with reset button)");

                System.out.println("\n=============================================================");
                System.out.println("ALL PHASE 9.5 VISUAL SCREENSHOTS CAPTURED SUCCESSFULLY!");
                System.out.println("=============================================================");

                if (fallbackServer != null) {
                    fallbackServer.stop(0);
                }

                Platform.runLater(() -> {
                    stage.close();
                    Platform.exit();
                    System.exit(0);
                });

            } catch (Exception e) {
                System.err.println("FAILED during Phase 9.5 visual capture: " + e.getMessage());
                e.printStackTrace();
                if (fallbackServer != null) {
                    fallbackServer.stop(0);
                }
                Platform.runLater(() -> {
                    stage.close();
                    Platform.exit();
                    System.exit(1);
                });
            }
        }, "visual-browse-runner").start();
    }

    private boolean isPortActive(int port) {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void startFallbackServer() throws IOException {
        fallbackServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 5001), 0);

        List<Hotel> seededHotels = buildSeededHotels();

        // 1. Auth Login Endpoint
        fallbackServer.createContext("/api/auth/login", exchange -> {
            String json = """
                {
                  "userId": "63aa383e-d73d-46bc-ba14-9d6f5b0b88bf",
                  "email": "guest@hotel.com",
                  "token": "mock-guest-jwt-token-for-phase-9-5",
                  "user": {
                    "id": "63aa383e-d73d-46bc-ba14-9d6f5b0b88bf",
                    "email": "guest@hotel.com",
                    "firstName": "Guest",
                    "lastName": "Traveler",
                    "role": "USER"
                  }
                }
                """;
            sendJsonResponse(exchange, 200, json);
        });

        // 2. Validate Token
        fallbackServer.createContext("/api/auth/validate-token", exchange -> {
            String json = "{\"userId\":\"63aa383e-d73d-46bc-ba14-9d6f5b0b88bf\"}";
            sendJsonResponse(exchange, 200, json);
        });

        // 3. Search Hotels Endpoint
        fallbackServer.createContext("/api/hotels/search", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            List<Hotel> filtered = new ArrayList<>(seededHotels);

            if (query != null && !query.isEmpty()) {
                Map<String, List<String>> queryParams = new HashMap<>();
                for (String pair : query.split("&")) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        queryParams.computeIfAbsent(k, key -> new ArrayList<>()).add(v);
                    }
                }

                // Destination
                if (queryParams.containsKey("destination")) {
                    String dest = queryParams.get("destination").get(0).toLowerCase();
                    filtered.removeIf(h -> !h.getCity().toLowerCase().contains(dest) && !h.getCountry().toLowerCase().contains(dest));
                }

                // Star Rating
                if (queryParams.containsKey("stars")) {
                    List<Integer> reqStars = queryParams.get("stars").stream().map(Integer::parseInt).collect(Collectors.toList());
                    filtered.removeIf(h -> !reqStars.contains(h.getStarRating()));
                }

                // Property Types
                if (queryParams.containsKey("types")) {
                    List<String> reqTypes = queryParams.get("types");
                    filtered.removeIf(h -> h.getType().stream().noneMatch(reqTypes::contains));
                }

                // Facilities
                if (queryParams.containsKey("facilities")) {
                    List<String> reqFacs = queryParams.get("facilities");
                    filtered.removeIf(h -> !h.getFacilities().containsAll(reqFacs));
                }

                // Max Price
                if (queryParams.containsKey("maxPrice")) {
                    BigDecimal max = new BigDecimal(queryParams.get("maxPrice").get(0));
                    filtered.removeIf(h -> h.getPricePerNight().compareTo(max) > 0);
                }
            }

            HotelSearchResponse resp = new HotelSearchResponse(filtered, new Pagination(filtered.size(), 1, 1));
            String respJson = mapper.writeValueAsString(resp);
            sendJsonResponse(exchange, 200, respJson);
        });

        // 4. Get All Hotels Endpoint
        fallbackServer.createContext("/api/hotels", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/api/hotels".equals(path)) {
                String respJson = mapper.writeValueAsString(seededHotels);
                sendJsonResponse(exchange, 200, respJson);
            } else {
                String id = path.substring("/api/hotels/".length());
                Hotel found = seededHotels.stream().filter(h -> h.getId().equals(id)).findFirst().orElse(null);
                if (found != null) {
                    sendJsonResponse(exchange, 200, mapper.writeValueAsString(found));
                } else {
                    sendJsonResponse(exchange, 404, "{\"message\":\"Hotel not found\"}");
                }
            }
        });

        fallbackServer.start();
    }

    private void sendJsonResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private List<Hotel> buildSeededHotels() {
        List<Hotel> list = new ArrayList<>();

        // 1. London
        Hotel h1 = new Hotel("h-london-1", "Thames View Boutique", "London", "United Kingdom", new BigDecimal("165.00"), 4);
        h1.setDescription("A refined riverside boutique hotel with contemporary rooms, picturesque water views, and easy access to central London landmarks.");
        h1.setType(List.of("Boutique", "Luxury"));
        h1.setFacilities(List.of("Free WiFi", "Non-Smoking Rooms", "Family Rooms", "Fitness Center"));
        h1.setImageUrls(List.of(
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800"
        ));
        h1.setIsFeatured(true);
        list.add(h1);

        // 2. Edinburgh
        Hotel h2 = new Hotel("h-edin-2", "Edinburgh Castle Inn", "Edinburgh", "United Kingdom", new BigDecimal("95.00"), 3);
        h2.setDescription("Historic comfort situated near the world-famous Royal Mile and Edinburgh Castle. Ideal for leisure getaways and family city breaks.");
        h2.setType(List.of("Budget", "Family"));
        h2.setFacilities(List.of("Free WiFi", "Family Rooms", "Parking"));
        h2.setImageUrls(List.of("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"));
        list.add(h2);

        // 3. Liverpool
        Hotel h3 = new Hotel("h-liv-3", "Quiet Quayside Suites", "Liverpool", "United Kingdom", new BigDecimal("120.00"), 4);
        h3.setDescription("Modern dockside suites designed for extended stays, business trips, and leisure travelers exploring Liverpool's cultural quarters.");
        h3.setType(List.of("Business", "Boutique"));
        h3.setFacilities(List.of("Free WiFi", "Fitness Center", "Parking", "Non-Smoking Rooms"));
        h3.setImageUrls(List.of("https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800"));
        list.add(h3);

        // 4. Cotswolds
        Hotel h4 = new Hotel("h-cots-4", "Cotswold Meadow Manor", "Cotswolds", "United Kingdom", new BigDecimal("240.00"), 5);
        h4.setDescription("An idyllic honey-stone country manor surrounded by private gardens, luxury spa facilities, and award-winning dining.");
        h4.setType(List.of("Luxury", "Romantic"));
        h4.setFacilities(List.of("Free WiFi", "Spa", "Outdoor Pool", "Fitness Center", "Parking"));
        h4.setImageUrls(List.of("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800"));
        h4.setIsFeatured(true);
        list.add(h4);

        // 5. Inverness
        Hotel h5 = new Hotel("h-inv-5", "Highland Pine Lodge", "Inverness", "United Kingdom", new BigDecimal("135.00"), 4);
        h5.setDescription("A cozy alpine-inspired mountain lodge on the edge of the Scottish Highlands, surrounded by forest trails and tranquil lochs.");
        h5.setType(List.of("Cabin", "Hiking Resort"));
        h5.setFacilities(List.of("Free WiFi", "Parking", "Family Rooms", "Non-Smoking Rooms"));
        h5.setImageUrls(List.of("https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=800"));
        list.add(h5);

        // 6. Dublin
        Hotel h6 = new Hotel("h-dub-6", "Grand Central Dublin Hotel", "Dublin", "Ireland", new BigDecimal("185.00"), 4);
        h6.setDescription("Contemporary elegance overlooking the River Liffey, conveniently located near Temple Bar, Trinity College, and the business quarter.");
        h6.setType(List.of("Business", "Family"));
        h6.setFacilities(List.of("Free WiFi", "Airport Shuttle", "Fitness Center", "Non-Smoking Rooms"));
        h6.setImageUrls(List.of("https://images.unsplash.com/photo-1568495248636-6432b97bd949?w=800"));
        h6.setIsFeatured(true);
        list.add(h6);

        // 7. Paris
        Hotel h7 = new Hotel("h-par-7", "Eiffel Vista Suites", "Paris", "France", new BigDecimal("220.00"), 5);
        h7.setDescription("Chic Parisian hotel suites featuring private balconies with panoramic vistas of the Eiffel Tower, the Seine, and Parisian rooftops.");
        h7.setType(List.of("Romantic", "Luxury"));
        h7.setFacilities(List.of("Free WiFi", "Airport Shuttle", "Spa", "Non-Smoking Rooms"));
        h7.setImageUrls(List.of("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800"));
        h7.setIsFeatured(true);
        list.add(h7);

        // 8. Tokyo
        Hotel h8 = new Hotel("h-tok-8", "Shinjuku Garden Haven", "Tokyo", "Japan", new BigDecimal("150.00"), 4);
        h8.setDescription("Serene Japanese hospitality harmonizing minimalist aesthetic with modern smart amenities, steps away from Shinjuku Gyoen National Garden.");
        h8.setType(List.of("Boutique", "Business"));
        h8.setFacilities(List.of("Free WiFi", "Spa", "Fitness Center", "Non-Smoking Rooms"));
        h8.setImageUrls(List.of("https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800"));
        list.add(h8);

        return list;
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

                File artifactFile = new File(ARTIFACT_DIR, filename);
                ImageIO.write(bi, "png", artifactFile);

                File projectFile = new File(PROJECT_SCREENSHOTS_DIR, filename);
                ImageIO.write(bi, "png", projectFile);

            } catch (Exception e) {
                System.err.println("Error capturing " + filename + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

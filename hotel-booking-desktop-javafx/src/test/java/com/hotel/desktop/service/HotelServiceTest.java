package com.hotel.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.desktop.model.Hotel;
import com.hotel.desktop.model.HotelSearchParams;
import com.hotel.desktop.model.HotelSearchResponse;
import com.hotel.desktop.model.Pagination;
import com.hotel.desktop.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelServiceTest {

    private HttpServer server;
    private String serverBaseUrl;
    private SessionManager sessionManager;
    private ObjectMapper objectMapper;
    private HttpClient realHttpClient;
    private ApiClient apiClient;
    private HotelService hotelService;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        serverBaseUrl = "http://127.0.0.1:" + port;

        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
        objectMapper = JsonUtils.getMapper();

        realHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        apiClient = new ApiClient(serverBaseUrl, realHttpClient, objectMapper, sessionManager);
        hotelService = new HotelService(apiClient);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
    }

    @Test
    @DisplayName("Hotel JSON deserializes properly with _id alias and UI helpers")
    void testHotelDeserialization() throws Exception {
        String json = """
            {
              "_id": "hotel-london-001",
              "userId": "owner-123",
              "name": "The Savoy Palace",
              "city": "London",
              "country": "United Kingdom",
              "description": "Historic landmark hotel in Westminster.",
              "type": ["Luxury", "Boutique"],
              "adultCount": 2,
              "childCount": 1,
              "facilities": ["Free WiFi", "Spa", "Fitness Center"],
              "pricePerNight": 295.50,
              "starRating": 5,
              "imageUrls": ["https://images.unsplash.com/photo-1566073771259-6a8506099945", "https://images.unsplash.com/photo-2"],
              "isFeatured": true,
              "policies": {
                "checkInTime": "15:00",
                "checkOutTime": "11:00",
                "cancellationPolicy": "Free cancellation up to 48 hours before check-in"
              },
              "contact": {
                "phone": "+44 20 7946 0991",
                "email": "concierge@savoy.com",
                "website": "https://savoy.com"
              }
            }
            """;

        Hotel hotel = objectMapper.readValue(json, Hotel.class);

        assertNotNull(hotel);
        assertEquals("hotel-london-001", hotel.getId());
        assertEquals("The Savoy Palace", hotel.getName());
        assertEquals("London, United Kingdom", hotel.getLocationDisplay());
        assertEquals("★★★★★", hotel.getStarRatingDisplay());
        assertEquals("£295.5", hotel.getFormattedPrice());
        assertEquals("https://images.unsplash.com/photo-1566073771259-6a8506099945", hotel.getFirstImageUrl());
        assertTrue(hotel.getIsFeatured());
        assertEquals("15:00", hotel.getPolicies().getCheckInTime());
        assertEquals("concierge@savoy.com", hotel.getContact().getEmail());
    }

    @Test
    @DisplayName("Successful hotel search returns expected hotels and pagination metadata")
    void testSuccessfulHotelSearch() throws Exception {
        String responseJson = """
            {
              "data": [
                {
                  "_id": "h-1",
                  "name": "London Grand",
                  "city": "London",
                  "country": "United Kingdom",
                  "pricePerNight": 180,
                  "starRating": 4,
                  "type": ["Luxury"],
                  "facilities": ["Free WiFi"]
                },
                {
                  "_id": "h-2",
                  "name": "Tokyo Tower Hotel",
                  "city": "Tokyo",
                  "country": "Japan",
                  "pricePerNight": 220,
                  "starRating": 5,
                  "type": ["Boutique"],
                  "facilities": ["Free WiFi", "Spa"]
                }
              ],
              "pagination": {
                "total": 2,
                "page": 1,
                "pages": 1
              }
            }
            """;

        server.createContext("/api/hotels/search", exchange -> {
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        HotelSearchParams params = new HotelSearchParams();
        params.setDestination("London");

        HotelSearchResponse response = hotelService.searchHotels(params);

        assertNotNull(response);
        assertEquals(2, response.getData().size());
        assertEquals("London Grand", response.getData().get(0).getName());
        assertEquals("Tokyo Tower Hotel", response.getData().get(1).getName());
        assertEquals(2, response.getPagination().getTotal());
        assertEquals(1, response.getPagination().getPage());
    }

    @Test
    @DisplayName("Search parameter builder constructs valid, URL-encoded query strings")
    void testSearchParamConstruction() {
        HotelSearchParams params = new HotelSearchParams();
        params.setDestination("New York & London");
        params.setCheckIn(LocalDate.of(2026, 12, 1));
        params.setCheckOut(LocalDate.of(2026, 12, 5));
        params.setAdultCount(2);
        params.setChildCount(1);
        params.setPage(2);
        params.setMaxPrice(new BigDecimal("350.00"));
        params.setSortOption("pricePerNightAsc");
        params.setStars(List.of(4, 5));
        params.setTypes(List.of("Luxury", "Boutique"));
        params.setFacilities(List.of("Free WiFi", "Spa"));

        String queryString = params.toQueryString();

        assertNotNull(queryString);
        assertTrue(queryString.startsWith("?"));
        assertTrue(queryString.contains("destination=New+York+%26+London"));
        assertTrue(queryString.contains("checkIn=2026-12-01"));
        assertTrue(queryString.contains("checkOut=2026-12-05"));
        assertTrue(queryString.contains("adultCount=2"));
        assertTrue(queryString.contains("childCount=1"));
        assertTrue(queryString.contains("page=2"));
        assertTrue(queryString.contains("maxPrice=350.00"));
        assertTrue(queryString.contains("sortOption=pricePerNightAsc"));
        assertTrue(queryString.contains("stars=4"));
        assertTrue(queryString.contains("stars=5"));
        assertTrue(queryString.contains("types=Luxury"));
        assertTrue(queryString.contains("types=Boutique"));
        assertTrue(queryString.contains("facilities=Free+WiFi"));
        assertTrue(queryString.contains("facilities=Spa"));
    }

    @Test
    @DisplayName("Empty search parameters produce empty query string")
    void testEmptySearchParamConstruction() {
        HotelSearchParams params = new HotelSearchParams();
        assertEquals("", params.toQueryString());
    }

    @Test
    @DisplayName("Empty results are handled cleanly without NPE")
    void testEmptyResultsHandling() throws Exception {
        String responseJson = """
            {
              "data": [],
              "pagination": {
                "total": 0,
                "page": 1,
                "pages": 0
              }
            }
            """;

        server.createContext("/api/hotels/search", exchange -> {
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        HotelSearchResponse response = hotelService.searchHotels(new HotelSearchParams());

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
        assertEquals(0, response.getPagination().getTotal());
    }

    @Test
    @DisplayName("Retrieves hotel details by ID successfully")
    void testGetHotelByIdSuccess() throws Exception {
        String responseJson = """
            {
              "_id": "hotel-paris-005",
              "name": "Le Grand Hotel Paris",
              "city": "Paris",
              "country": "France",
              "pricePerNight": 320,
              "starRating": 5,
              "description": "Elegant Parisian stay near the Opera."
            }
            """;

        server.createContext("/api/hotels/hotel-paris-005", exchange -> {
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        Hotel hotel = hotelService.getHotelById("hotel-paris-005");

        assertNotNull(hotel);
        assertEquals("hotel-paris-005", hotel.getId());
        assertEquals("Le Grand Hotel Paris", hotel.getName());
        assertEquals("Paris, France", hotel.getLocationDisplay());
        assertEquals(5, hotel.getStarRating());
    }

    @Test
    @DisplayName("Retrieval of missing hotel ID throws ApiException with 404")
    void testGetHotelByIdNotFound() {
        server.createContext("/api/hotels/invalid-id", exchange -> {
            byte[] bytes = "{\"message\":\"Hotel not found\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        ApiException exception = assertThrows(ApiException.class, () -> hotelService.getHotelById("invalid-id"));
        assertEquals(404, exception.getStatusCode());
        assertEquals("Hotel not found", exception.getMessage());
    }

    @Test
    @DisplayName("Empty or null hotel ID throws ApiException before network request")
    void testGetHotelByIdEmptyValidation() {
        ApiException ex1 = assertThrows(ApiException.class, () -> hotelService.getHotelById(null));
        assertEquals(400, ex1.getStatusCode());

        ApiException ex2 = assertThrows(ApiException.class, () -> hotelService.getHotelById("   "));
        assertEquals(400, ex2.getStatusCode());
    }

    @Test
    @DisplayName("Retrieves all active hotels as array and maps to List<Hotel>")
    void testGetAllHotelsSuccess() throws Exception {
        String responseJson = """
            [
              { "_id": "h-1", "name": "Hotel One", "city": "London", "country": "UK", "pricePerNight": 100 },
              { "_id": "h-2", "name": "Hotel Two", "city": "Tokyo", "country": "Japan", "pricePerNight": 150 }
            ]
            """;

        server.createContext("/api/hotels", exchange -> {
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        List<Hotel> hotels = hotelService.getAllHotels();

        assertNotNull(hotels);
        assertEquals(2, hotels.size());
        assertEquals("Hotel One", hotels.get(0).getName());
        assertEquals("Hotel Two", hotels.get(1).getName());
    }
}

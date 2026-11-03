package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.HotelActiveStatusRequest;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.services.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User adminUser;
    private User regularUser;
    private Hotel testHotel;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setEmail("admin_hotel@test.com");
        adminUser.setPassword("password123");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

        regularUser = new User();
        regularUser.setEmail("guest_hotel@test.com");
        regularUser.setPassword("password123");
        regularUser.setFirstName("Guest");
        regularUser.setLastName("User");
        regularUser.setRole(UserRole.USER);
        regularUser = userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateToken(regularUser.getId(), regularUser.getEmail(), UserRole.USER);

        testHotel = new Hotel();
        testHotel.setUser(adminUser);
        testHotel.setName("Public Grand Hotel");
        testHotel.setCity("London");
        testHotel.setCountry("United Kingdom");
        testHotel.setDescription("Historic grand hotel in London.");
        testHotel.setType(List.of("Luxury", "City"));
        testHotel.setFacilities(List.of("WiFi", "Bar"));
        testHotel.setPricePerNight(new BigDecimal("250.00"));
        testHotel.setStarRating(5);
        testHotel.setAdultCount(2);
        testHotel.setChildCount(1);
        testHotel.setIsActive(true);
        testHotel.setLastUpdated(Instant.now());
        testHotel = hotelRepository.save(testHotel);
    }

    @Test
    @DisplayName("GET /api/hotels/search with empty string query params succeeds (as sent by frontend URLSearchParams)")
    void testSearchWithEmptyQueryParams() throws Exception {
        mockMvc.perform(get("/api/hotels/search?destination=&checkIn=&checkOut=&adultCount=&childCount=&page=&maxPrice=&sortOption="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.pagination.total", is(1)));
    }

    @Test
    @DisplayName("GET /api/hotels returns public hotel list")
    void testGetAllHotelsPublic() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Public Grand Hotel")))
                .andExpect(jsonPath("$[0]._id", is(testHotel.getId())));
    }

    @Test
    @DisplayName("GET /api/hotels/{id} returns single hotel details")
    void testGetHotelByIdPublic() throws Exception {
        mockMvc.perform(get("/api/hotels/" + testHotel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Public Grand Hotel")))
                .andExpect(jsonPath("$.city", is("London")))
                .andExpect(jsonPath("$._id", is(testHotel.getId())));
    }

    @Test
    @DisplayName("GET /api/hotels/{id} returns 404 when hotel does not exist")
    void testGetHotelByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/hotels/non-existent-id-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Hotel not found")));
    }

    @Test
    @DisplayName("PATCH /api/hotels/{id}/active succeeds when caller is ADMIN")
    void testAdminToggleHotelActive() throws Exception {
        HotelActiveStatusRequest request = new HotelActiveStatusRequest(false);

        mockMvc.perform(patch("/api/hotels/" + testHotel.getId() + "/active")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(false)));
    }

    @Test
    @DisplayName("PATCH /api/hotels/{id}/active returns 403 when caller is not ADMIN")
    void testNonAdminToggleHotelActiveForbidden() throws Exception {
        HotelActiveStatusRequest request = new HotelActiveStatusRequest(false);

        mockMvc.perform(patch("/api/hotels/" + testHotel.getId() + "/active")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }
}

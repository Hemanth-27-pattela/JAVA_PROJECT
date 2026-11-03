package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.HotelActiveStatusRequest;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.services.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MyHotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User owner1;
    private User owner2;
    private User ordinaryUser;
    private String owner1Token;
    private String owner2Token;
    private String userToken;
    private Hotel hotel1;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        owner1 = new User();
        owner1.setEmail("owner1@test.com");
        owner1.setPassword("password123");
        owner1.setFirstName("Alice");
        owner1.setLastName("Owner");
        owner1.setRole(UserRole.HOTEL_OWNER);
        owner1 = userRepository.save(owner1);
        owner1Token = jwtTokenProvider.generateToken(owner1.getId(), owner1.getEmail(), UserRole.HOTEL_OWNER);

        owner2 = new User();
        owner2.setEmail("owner2@test.com");
        owner2.setPassword("password123");
        owner2.setFirstName("Bob");
        owner2.setLastName("Owner");
        owner2.setRole(UserRole.HOTEL_OWNER);
        owner2 = userRepository.save(owner2);
        owner2Token = jwtTokenProvider.generateToken(owner2.getId(), owner2.getEmail(), UserRole.HOTEL_OWNER);

        ordinaryUser = new User();
        ordinaryUser.setEmail("user_myhotels@test.com");
        ordinaryUser.setPassword("password123");
        ordinaryUser.setFirstName("Charlie");
        ordinaryUser.setLastName("Guest");
        ordinaryUser.setRole(UserRole.USER);
        ordinaryUser = userRepository.save(ordinaryUser);
        userToken = jwtTokenProvider.generateToken(ordinaryUser.getId(), ordinaryUser.getEmail(), UserRole.USER);

        hotel1 = new Hotel();
        hotel1.setUser(owner1);
        hotel1.setName("Alice Sunset Boutique");
        hotel1.setCity("Florence");
        hotel1.setCountry("Italy");
        hotel1.setDescription("Charming Tuscan boutique hotel.");
        hotel1.setType(List.of("Boutique", "Romantic"));
        hotel1.setFacilities(List.of("Free WiFi", "Breakfast Included"));
        hotel1.setPricePerNight(new BigDecimal("180.00"));
        hotel1.setStarRating(4);
        hotel1.setAdultCount(2);
        hotel1.setChildCount(0);
        hotel1.setImageUrls(List.of("https://res.cloudinary.com/demo/image/upload/existing1.jpg"));
        hotel1.setLastUpdated(Instant.now());
        hotel1 = hotelRepository.save(hotel1);

        // Add a booking for hotel1 to test enriched dashboard counts
        Booking booking = new Booking();
        booking.setHotel(hotel1);
        booking.setUser(ordinaryUser);
        booking.setFirstName("Charlie");
        booking.setLastName("Guest");
        booking.setEmail("guest@test.com");
        booking.setAdultCount(2);
        booking.setChildCount(0);
        booking.setCheckIn(Instant.now().plus(2, ChronoUnit.DAYS));
        booking.setCheckOut(Instant.now().plus(5, ChronoUnit.DAYS));
        booking.setTotalCost(new BigDecimal("540.00"));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(booking);

        Mockito.when(imageUploadService.uploadImages(anyList()))
                .thenReturn(List.of("https://res.cloudinary.com/demo/image/upload/newly_uploaded.jpg"));
    }

    @Test
    @DisplayName("POST /api/my-hotels creates hotel with multipart and indexed form fields")
    void testCreateHotelWithMultipart() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile(
                "imageFiles",
                "photo1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content 1".getBytes()
        );

        mockMvc.perform(multipart("/api/my-hotels")
                        .file(image1)
                        .param("name", "New Tuscany Villa")
                        .param("city", "Siena")
                        .param("country", "Italy")
                        .param("description", "Private villa in Siena hills.")
                        .param("pricePerNight", "220.00")
                        .param("starRating", "5")
                        .param("adultCount", "4")
                        .param("childCount", "2")
                        .param("type[0]", "Villa")
                        .param("type[1]", "Luxury")
                        .param("facilities[0]", "Free WiFi")
                        .param("facilities[1]", "Private Pool")
                        .param("contact.phone", "+39 0577 123456")
                        .param("contact.email", "info@tuscanyvilla.com")
                        .param("policies.checkInTime", "15:00")
                        .param("policies.checkOutTime", "11:00")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Tuscany Villa")))
                .andExpect(jsonPath("$.city", is("Siena")))
                .andExpect(jsonPath("$.type", hasSize(2)))
                .andExpect(jsonPath("$.facilities", hasSize(2)))
                .andExpect(jsonPath("$.contact.phone", is("+39 0577 123456")))
                .andExpect(jsonPath("$.policies.checkInTime", is("15:00")))
                .andExpect(jsonPath("$.imageUrls", hasSize(1)))
                .andExpect(jsonPath("$.userId", is(owner1.getId())));
    }

    @Test
    @DisplayName("POST /api/my-hotels returns 403 when user does not have HOTEL_OWNER role")
    void testCreateHotelForbiddenForOrdinaryUser() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "imageFiles",
                "photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image".getBytes()
        );

        mockMvc.perform(multipart("/api/my-hotels")
                        .file(image)
                        .param("name", "Unauthorized Hotel")
                        .param("city", "Rome")
                        .param("country", "Italy")
                        .param("description", "Should fail.")
                        .param("pricePerNight", "100.00")
                        .param("type[0]", "Hotel")
                        .param("facilities[0]", "WiFi")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    @DisplayName("POST /api/my-hotels returns 400 when required fields are missing")
    void testCreateHotelMissingRequiredField() throws Exception {
        mockMvc.perform(multipart("/api/my-hotels")
                        .param("city", "Florence")
                        .param("country", "Italy")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Name is required")));
    }

    @Test
    @DisplayName("GET /api/my-hotels returns owner's enriched hotel list")
    void testGetMyHotelsEnriched() throws Exception {
        mockMvc.perform(get("/api/my-hotels")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice Sunset Boutique")))
                .andExpect(jsonPath("$[0].upcomingBookings", is(1)))
                .andExpect(jsonPath("$[0].completedBookings", is(0)))
                .andExpect(jsonPath("$[0].cancelledBookings", is(0)));
    }

    @Test
    @DisplayName("GET /api/my-hotels/{id} returns hotel details for owner")
    void testGetMyHotelByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/my-hotels/" + hotel1.getId())
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Sunset Boutique")))
                .andExpect(jsonPath("$._id", is(hotel1.getId())));
    }

    @Test
    @DisplayName("GET /api/my-hotels/{id} returns 403 when another owner attempts access")
    void testGetMyHotelByIdForbiddenForOtherOwner() throws Exception {
        mockMvc.perform(get("/api/my-hotels/" + hotel1.getId())
                        .header("Authorization", "Bearer " + owner2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    @DisplayName("PUT /api/my-hotels/{id} updates hotel and preserves/combines images")
    void testUpdateMyHotelSuccess() throws Exception {
        MockMultipartFile newImage = new MockMultipartFile(
                "imageFiles",
                "new_pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new image data".getBytes()
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/my-hotels/" + hotel1.getId())
                        .file(newImage)
                        .param("name", "Alice Updated Boutique")
                        .param("city", "Florence")
                        .param("country", "Italy")
                        .param("description", "Updated description.")
                        .param("pricePerNight", "195.00")
                        .param("starRating", "4")
                        .param("adultCount", "2")
                        .param("childCount", "1")
                        .param("type[0]", "Boutique")
                        .param("facilities[0]", "Free WiFi")
                        .param("imageUrls[0]", "https://res.cloudinary.com/demo/image/upload/existing1.jpg")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Updated Boutique")))
                .andExpect(jsonPath("$.pricePerNight", is(195.00)))
                .andExpect(jsonPath("$.imageUrls", hasSize(2)));
    }

    @Test
    @DisplayName("PUT /api/my-hotels/{id} returns 403 when non-owner attempts update")
    void testUpdateMyHotelForbiddenForOtherOwner() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/my-hotels/" + hotel1.getId())
                        .param("name", "Hacked Hotel")
                        .param("city", "Florence")
                        .param("country", "Italy")
                        .param("description", "Should fail.")
                        .param("pricePerNight", "100.00")
                        .param("type[0]", "Boutique")
                        .param("facilities[0]", "Free WiFi")
                        .header("Authorization", "Bearer " + owner2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    @DisplayName("PATCH /api/my-hotels/{id}/active toggles active status by owner")
    void testToggleMyHotelActiveByOwner() throws Exception {
        HotelActiveStatusRequest request = new HotelActiveStatusRequest(false);

        mockMvc.perform(patch("/api/my-hotels/" + hotel1.getId() + "/active")
                        .header("Authorization", "Bearer " + owner1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(false)));
    }

    @Test
    @DisplayName("PATCH /api/my-hotels/{id}/active returns 403 when non-owner attempts toggle")
    void testToggleMyHotelActiveForbiddenForOtherOwner() throws Exception {
        HotelActiveStatusRequest request = new HotelActiveStatusRequest(false);

        mockMvc.perform(patch("/api/my-hotels/" + hotel1.getId() + "/active")
                        .header("Authorization", "Bearer " + owner2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access denied")));
    }
}

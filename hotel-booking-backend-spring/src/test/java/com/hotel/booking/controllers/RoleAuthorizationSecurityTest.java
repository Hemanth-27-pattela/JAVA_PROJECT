package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.RegisterRequest;
import com.hotel.booking.dtos.ownerrequest.CreateOwnerRequestDto;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelOwnerRequestRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import com.hotel.booking.services.HotelOwnerRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAuthorizationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelOwnerRequestRepository requestRepository;

    @Autowired
    private HotelOwnerRequestService requestService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User guestUser;
    private User owner1;
    private User owner2;
    private User adminUser;
    private String guestToken;
    private String owner1Token;
    private String owner2Token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        guestUser = new User();
        guestUser.setEmail("guest@test.com");
        guestUser.setPassword(passwordEncoder.encode("password123"));
        guestUser.setFirstName("Guest");
        guestUser.setLastName("User");
        guestUser.setRole(UserRole.USER);
        guestUser.setIsActive(true);
        guestUser = userRepository.save(guestUser);
        guestToken = tokenProvider.generateToken(guestUser.getId(), guestUser.getEmail(), guestUser.getRole());

        owner1 = new User();
        owner1.setEmail("owner1@test.com");
        owner1.setPassword(passwordEncoder.encode("password123"));
        owner1.setFirstName("Owner");
        owner1.setLastName("One");
        owner1.setRole(UserRole.HOTEL_OWNER);
        owner1.setIsActive(true);
        owner1 = userRepository.save(owner1);
        owner1Token = tokenProvider.generateToken(owner1.getId(), owner1.getEmail(), owner1.getRole());

        owner2 = new User();
        owner2.setEmail("owner2@test.com");
        owner2.setPassword(passwordEncoder.encode("password123"));
        owner2.setFirstName("Owner");
        owner2.setLastName("Two");
        owner2.setRole(UserRole.HOTEL_OWNER);
        owner2.setIsActive(true);
        owner2 = userRepository.save(owner2);
        owner2Token = tokenProvider.generateToken(owner2.getId(), owner2.getEmail(), owner2.getRole());

        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
        adminToken = tokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());
    }

    @Test
    @DisplayName("Public registration always assigns USER role, even if client injects role into payload")
    void testRegistration_DefaultsToUserRoleAndIgnoresInjectedRole() throws Exception {
        String payload = "{" +
                "\"email\":\"attacker@evil.com\"," +
                "\"password\":\"Password123!\"," +
                "\"firstName\":\"Attacker\"," +
                "\"lastName\":\"Admin\"," +
                "\"role\":\"admin\"" +
                "}";

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        User created = userRepository.findByEmail("attacker@evil.com").orElseThrow();
        assertEquals(UserRole.USER, created.getRole(), "Must strictly assign USER role");
    }

    @Test
    @DisplayName("USER role cannot create hotels via POST /api/my-hotels (403 Forbidden)")
    void testUserRole_CannotCreateHotel_Forbidden() throws Exception {
        MockMultipartFile namePart = new MockMultipartFile("name", "", "text/plain", "Test Hotel".getBytes());
        MockMultipartFile cityPart = new MockMultipartFile("city", "", "text/plain", "London".getBytes());

        mockMvc.perform(multipart("/api/my-hotels")
                        .file(namePart)
                        .file(cityPart)
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER role cannot list own hotels via GET /api/my-hotels (403 Forbidden)")
    void testUserRole_CannotListMyHotels_Forbidden() throws Exception {
        mockMvc.perform(get("/api/my-hotels")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HOTEL_OWNER can access GET /api/my-hotels")
    void testHotelOwner_CanListOwnHotels_Success() throws Exception {
        mockMvc.perform(get("/api/my-hotels")
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("HOTEL_OWNER cannot access or edit another owner's hotel (403 Forbidden)")
    void testHotelOwner_CannotAccessOtherOwnerHotel() throws Exception {
        Hotel hotelOwner2 = new Hotel();
        hotelOwner2.setName("Owner2 Hotel");
        hotelOwner2.setCity("Paris");
        hotelOwner2.setCountry("France");
        hotelOwner2.setDescription("Luxury Hotel");
        hotelOwner2.setUser(owner2);
        hotelOwner2.setPricePerNight(BigDecimal.valueOf(150));
        hotelOwner2.setStarRating(4);
        hotelOwner2.setLastUpdated(Instant.now());
        hotelOwner2 = hotelRepository.save(hotelOwner2);

        // Owner 1 tries to access Owner 2's hotel
        mockMvc.perform(get("/api/my-hotels/" + hotelOwner2.getId())
                        .header("Authorization", "Bearer " + owner1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can access any owner's hotel")
    void testAdmin_CanAccessAnyHotel() throws Exception {
        Hotel hotelOwner2 = new Hotel();
        hotelOwner2.setName("Owner2 Hotel");
        hotelOwner2.setCity("Paris");
        hotelOwner2.setCountry("France");
        hotelOwner2.setDescription("Luxury Hotel");
        hotelOwner2.setUser(owner2);
        hotelOwner2.setPricePerNight(BigDecimal.valueOf(150));
        hotelOwner2.setStarRating(4);
        hotelOwner2.setLastUpdated(Instant.now());
        hotelOwner2 = hotelRepository.save(hotelOwner2);

        mockMvc.perform(get("/api/my-hotels/" + hotelOwner2.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Owner2 Hotel")));
    }

    @Test
    @DisplayName("Token refresh endpoint reissues JWT with elevated role after owner approval")
    void testTokenRefresh_ReflectsElevatedRole() throws Exception {
        // Initially, guest token validates as USER
        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(guestUser.getId())))
                .andExpect(jsonPath("$.role", is("user")));

        // Admin elevates the guest to HOTEL_OWNER
        guestUser.setRole(UserRole.HOTEL_OWNER);
        userRepository.save(guestUser);

        // User calls POST /api/auth/refresh-token
        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", is(guestUser.getId())));

        // Validate-token now returns hotel_owner
        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("hotel_owner")));
    }

    @Test
    @DisplayName("Concurrent submissions for the same user allow at most one PENDING request")
    void testConcurrentSubmissions_HandledSafely() throws Exception {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int index = i;
            tasks.add(() -> {
                try {
                    CreateOwnerRequestDto dto = new CreateOwnerRequestDto(
                            "Concurrent Hotel " + index,
                            "hotel" + index + "@test.com",
                            "123", "Address", "Justification"
                    );
                    requestService.submitRequest(guestUser.getId(), dto);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();

        int successCount = 0;
        for (Future<Boolean> f : results) {
            if (f.get()) {
                successCount++;
            }
        }

        assertEquals(1, successCount, "Exactly one concurrent submission must succeed");
        long pendingCount = requestRepository.countByStatus(com.hotel.booking.entities.enums.RequestStatus.PENDING);
        assertEquals(1, pendingCount, "Database must store exactly one PENDING request");
    }
}


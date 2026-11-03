package com.hotel.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.dtos.ownerrequest.CreateOwnerRequestDto;
import com.hotel.booking.dtos.ownerrequest.ReviewOwnerRequestDto;
import com.hotel.booking.entities.HotelOwnerRequest;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.RequestStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelOwnerRequestRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HotelOwnerRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelOwnerRequestRepository requestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User guestUser;
    private User ownerUser;
    private User adminUser;
    private String guestToken;
    private String ownerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        userRepository.deleteAll();

        guestUser = new User();
        guestUser.setEmail("guest@example.com");
        guestUser.setPassword(passwordEncoder.encode("password123"));
        guestUser.setFirstName("John");
        guestUser.setLastName("Guest");
        guestUser.setRole(UserRole.USER);
        guestUser.setIsActive(true);
        guestUser = userRepository.save(guestUser);
        guestToken = tokenProvider.generateToken(guestUser.getId(), guestUser.getEmail(), guestUser.getRole());

        ownerUser = new User();
        ownerUser.setEmail("owner@example.com");
        ownerUser.setPassword(passwordEncoder.encode("password123"));
        ownerUser.setFirstName("Alice");
        ownerUser.setLastName("Owner");
        ownerUser.setRole(UserRole.HOTEL_OWNER);
        ownerUser.setIsActive(true);
        ownerUser = userRepository.save(ownerUser);
        ownerToken = tokenProvider.generateToken(ownerUser.getId(), ownerUser.getEmail(), ownerUser.getRole());

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setFirstName("Bob");
        adminUser.setLastName("Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
        adminToken = tokenProvider.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());
    }

    @Test
    @DisplayName("Guest can submit hotel owner request successfully")
    void testSubmitRequest_Success() throws Exception {
        CreateOwnerRequestDto dto = new CreateOwnerRequestDto(
                "Grand Palace Hotel",
                "contact@grandpalace.com",
                "+1234567890",
                "123 Ocean Drive, Miami, FL",
                "We own a 50-room luxury resort and want to list on the platform"
        );

        mockMvc.perform(post("/api/owner-requests")
                        .header("Authorization", "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id", notNullValue()))
                .andExpect(jsonPath("$.businessName", is("Grand Palace Hotel")))
                .andExpect(jsonPath("$.businessEmail", is("contact@grandpalace.com")))
                .andExpect(jsonPath("$.status", is("pending")))
                .andExpect(jsonPath("$.userId", is(guestUser.getId())));

        assertTrue(requestRepository.existsByUserIdAndStatus(guestUser.getId(), RequestStatus.PENDING));
    }

    @Test
    @DisplayName("Submitting duplicate pending request returns 409 Conflict")
    void testSubmitRequest_DuplicatePendingFails() throws Exception {
        CreateOwnerRequestDto dto = new CreateOwnerRequestDto(
                "First Request Hotel",
                "first@hotel.com",
                "123", "Address", "Justification"
        );

        // Submit first
        mockMvc.perform(post("/api/owner-requests")
                        .header("Authorization", "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Attempt duplicate while first is PENDING
        CreateOwnerRequestDto duplicateDto = new CreateOwnerRequestDto(
                "Second Request Hotel",
                "second@hotel.com",
                "456", "Address 2", "Another justification"
        );

        mockMvc.perform(post("/api/owner-requests")
                        .header("Authorization", "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("User who is already HOTEL_OWNER submitting request returns 400 Bad Request")
    void testSubmitRequest_AlreadyOwnerFails() throws Exception {
        CreateOwnerRequestDto dto = new CreateOwnerRequestDto(
                "Another Hotel", "owner2@hotel.com", "123", "Address", "Already an owner"
        );

        mockMvc.perform(post("/api/owner-requests")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Guest can view their own requests and pending status")
    void testGetMyRequests_Success() throws Exception {
        HotelOwnerRequest req = new HotelOwnerRequest();
        req.setUser(guestUser);
        req.setBusinessName("Seaside Inn");
        req.setBusinessEmail("inn@seaside.com");
        req.setStatus(RequestStatus.PENDING);
        requestRepository.save(req);

        mockMvc.perform(get("/api/owner-requests/me")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].businessName", is("Seaside Inn")))
                .andExpect(jsonPath("$[0].status", is("pending")));

        mockMvc.perform(get("/api/owner-requests/me/pending")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName", is("Seaside Inn")))
                .andExpect(jsonPath("$.status", is("pending")));
    }

    @Test
    @DisplayName("Non-admin cannot list all requests (403 Forbidden)")
    void testGetAllRequests_NonAdminForbidden() throws Exception {
        mockMvc.perform(get("/api/owner-requests")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/owner-requests")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can list all requests and filter by status")
    void testGetAllRequests_AdminAllowed() throws Exception {
        HotelOwnerRequest req1 = new HotelOwnerRequest();
        req1.setUser(guestUser);
        req1.setBusinessName("Pending Hotel");
        req1.setBusinessEmail("p@h.com");
        req1.setStatus(RequestStatus.PENDING);
        requestRepository.save(req1);

        mockMvc.perform(get("/api/owner-requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/owner-requests?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/owner-requests?status=APPROVED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Admin can approve request, automatically promoting user to HOTEL_OWNER")
    void testApproveRequest_AdminSuccess() throws Exception {
        HotelOwnerRequest req = new HotelOwnerRequest();
        req.setUser(guestUser);
        req.setBusinessName("Approved Hotel");
        req.setBusinessEmail("app@h.com");
        req.setStatus(RequestStatus.PENDING);
        req = requestRepository.save(req);

        ReviewOwnerRequestDto reviewDto = new ReviewOwnerRequestDto("Verified business license", null);

        mockMvc.perform(post("/api/owner-requests/" + req.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("approved")))
                .andExpect(jsonPath("$.adminNotes", is("Verified business license")))
                .andExpect(jsonPath("$.reviewedById", is(adminUser.getId())));

        // Verify target user's role is now HOTEL_OWNER
        User updatedGuest = userRepository.findById(guestUser.getId()).orElseThrow();
        assertEquals(UserRole.HOTEL_OWNER, updatedGuest.getRole());
    }

    @Test
    @DisplayName("Guest or Owner cannot approve requests (403 Forbidden)")
    void testApproveRequest_NonAdminForbidden() throws Exception {
        HotelOwnerRequest req = new HotelOwnerRequest();
        req.setUser(guestUser);
        req.setBusinessName("Pending Hotel");
        req.setBusinessEmail("p@h.com");
        req.setStatus(RequestStatus.PENDING);
        req = requestRepository.save(req);

        mockMvc.perform(post("/api/owner-requests/" + req.getId() + "/approve")
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/owner-requests/" + req.getId() + "/approve")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can reject request with reason; user remains USER role")
    void testRejectRequest_AdminSuccess() throws Exception {
        HotelOwnerRequest req = new HotelOwnerRequest();
        req.setUser(guestUser);
        req.setBusinessName("Rejected Hotel");
        req.setBusinessEmail("rej@h.com");
        req.setStatus(RequestStatus.PENDING);
        req = requestRepository.save(req);

        ReviewOwnerRequestDto reviewDto = new ReviewOwnerRequestDto("Needs more info", "Invalid tax registration number");

        mockMvc.perform(post("/api/owner-requests/" + req.getId() + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("rejected")))
                .andExpect(jsonPath("$.rejectionReason", is("Invalid tax registration number")));

        // Verify user remains USER
        User stillGuest = userRepository.findById(guestUser.getId()).orElseThrow();
        assertEquals(UserRole.USER, stillGuest.getRole());
    }

    @Test
    @DisplayName("Cannot re-approve or re-reject an already processed request")
    void testCannotProcessAlreadyApprovedRequest() throws Exception {
        HotelOwnerRequest req = new HotelOwnerRequest();
        req.setUser(guestUser);
        req.setBusinessName("Already Done");
        req.setBusinessEmail("d@h.com");
        req.setStatus(RequestStatus.APPROVED);
        req = requestRepository.save(req);

        mockMvc.perform(post("/api/owner-requests/" + req.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/owner-requests/" + req.getId() + "/reject")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}


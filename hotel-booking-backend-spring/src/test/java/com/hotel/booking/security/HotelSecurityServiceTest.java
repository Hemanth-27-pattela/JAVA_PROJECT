package com.hotel.booking.security;

import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class HotelSecurityServiceTest {

    @Autowired
    private HotelSecurityService hotelSecurityService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User otherUser;
    private User admin;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("owner@hotels.com", "pass", "Owner", "One");
        owner.setRole(UserRole.HOTEL_OWNER);
        owner = userRepository.save(owner);

        otherUser = new User("other@hotels.com", "pass", "Other", "Two");
        otherUser.setRole(UserRole.HOTEL_OWNER);
        otherUser = userRepository.save(otherUser);

        admin = new User("admin@hotels.com", "pass", "Super", "Admin");
        admin.setRole(UserRole.ADMIN);
        admin = userRepository.save(admin);

        hotel = new Hotel();
        hotel.setUser(owner);
        hotel.setName("Security Test Hotel");
        hotel.setCity("Rome");
        hotel.setCountry("Italy");
        hotel.setDescription("Test hotel for authorization");
        hotel.setType(List.of("Boutique"));
        hotel.setFacilities(List.of("WiFi"));
        hotel.setPricePerNight(new BigDecimal("100.00"));
        hotel.setStarRating(4);
        hotel.setLastUpdated(Instant.now());
        hotel = hotelRepository.save(hotel);
    }

    @Test
    @DisplayName("Hotel owner is authorized to manage their own hotel")
    void testOwnerCanAccessOwnHotel() {
        UserPrincipal principal = UserPrincipal.create(owner);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        assertTrue(hotelSecurityService.isOwner(hotel.getId(), auth));
    }

    @Test
    @DisplayName("Non-owner user is denied authorization to manage another's hotel")
    void testOtherUserCannotAccessDifferentOwnerHotel() {
        UserPrincipal principal = UserPrincipal.create(otherUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        assertFalse(hotelSecurityService.isOwner(hotel.getId(), auth));
    }

    @Test
    @DisplayName("Admin is authorized to manage any hotel")
    void testAdminCanAccessAnyHotel() {
        UserPrincipal principal = UserPrincipal.create(admin);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        assertTrue(hotelSecurityService.isOwner(hotel.getId(), auth));
    }

    @Test
    @DisplayName("Unauthenticated or null authentication is denied")
    void testUnauthenticatedCannotAccess() {
        assertFalse(hotelSecurityService.isOwner(hotel.getId(), null));
        assertFalse(hotelSecurityService.isOwner("non-existent-hotel", null));
    }

    @Test
    @DisplayName("Non-existent hotel ID returns false")
    void testNonExistentHotelReturnsFalse() {
        UserPrincipal principal = UserPrincipal.create(owner);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        assertFalse(hotelSecurityService.isOwner("non-existent-uuid", auth));
    }
}


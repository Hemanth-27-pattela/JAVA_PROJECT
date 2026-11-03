package com.hotel.desktop.service;

import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("Initial state is unauthenticated")
    void testInitialStateIsUnauthenticated() {
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getToken());
        assertNull(sessionManager.getCurrentUser());
        assertEquals("Guest", sessionManager.getDisplayName());
        assertEquals(UserRole.USER, sessionManager.getRole());
        assertTrue(sessionManager.isGuest());
        assertFalse(sessionManager.isAdmin());
        assertFalse(sessionManager.isHotelOwner());
    }

    @Test
    @DisplayName("setSession properly stores token and user details")
    void testSetSessionSuccess() {
        User admin = new User("admin-1", "admin@hotel.com", "Admin", "User", UserRole.ADMIN);
        sessionManager.setSession("jwt_admin_token_123", admin);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("jwt_admin_token_123", sessionManager.getToken());
        assertEquals("admin-1", sessionManager.getUserId());
        assertEquals("admin@hotel.com", sessionManager.getEmail());
        assertEquals("Admin User", sessionManager.getDisplayName());
        assertEquals(UserRole.ADMIN, sessionManager.getRole());
        assertTrue(sessionManager.isAdmin());
        assertFalse(sessionManager.isGuest());
        assertFalse(sessionManager.isHotelOwner());
    }

    @Test
    @DisplayName("Hotel owner role correctly identified")
    void testHotelOwnerRoleIdentified() {
        User owner = new User("owner-1", "owner@hotel.com", "Hotel", "Owner", UserRole.HOTEL_OWNER);
        sessionManager.setSession("jwt_owner_token_123", owner);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals(UserRole.HOTEL_OWNER, sessionManager.getRole());
        assertTrue(sessionManager.isHotelOwner());
        assertFalse(sessionManager.isAdmin());
        assertFalse(sessionManager.isGuest());
    }

    @Test
    @DisplayName("clearSession removes token and user context")
    void testClearSession() {
        User guest = new User("guest-1", "guest@hotel.com", "Guest", "User", UserRole.USER);
        sessionManager.setSession("jwt_guest_token_123", guest);
        assertTrue(sessionManager.isLoggedIn());

        sessionManager.clearSession();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getToken());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getUserId());
    }

    @Test
    @DisplayName("setSession rejects null or blank token")
    void testSetSessionRejectsBlankToken() {
        User user = new User("1", "test@hotel.com", "Test", "User", UserRole.USER);

        assertThrows(IllegalArgumentException.class, () -> sessionManager.setSession(null, user));
        assertThrows(IllegalArgumentException.class, () -> sessionManager.setSession("   ", user));
    }
}


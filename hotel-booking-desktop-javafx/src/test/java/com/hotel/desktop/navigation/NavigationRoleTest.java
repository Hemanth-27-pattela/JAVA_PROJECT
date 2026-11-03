package com.hotel.desktop.navigation;

import com.hotel.desktop.model.NavigationItem;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.service.ApiClient;
import com.hotel.desktop.service.AuthService;
import com.hotel.desktop.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NavigationRoleTest {

    private SessionManager sessionManager;
    private AuthService authService;
    private ApiClient mockApiClient;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
        mockApiClient = mock(ApiClient.class);
        authService = new AuthService(mockApiClient, sessionManager);
    }

    @Test
    @DisplayName("Guest (USER) role visibility strictly allows only 4 guest items")
    void testGuestNavigationVisibility() {
        List<NavigationItem> guestItems = NavigationItem.getItemsForRole(UserRole.USER);

        assertEquals(4, guestItems.size(), "Guest role must have exactly 4 permitted navigation items");
        assertTrue(guestItems.contains(NavigationItem.HOME));
        assertTrue(guestItems.contains(NavigationItem.BROWSE_HOTELS));
        assertTrue(guestItems.contains(NavigationItem.MY_BOOKINGS));
        assertTrue(guestItems.contains(NavigationItem.MY_PROFILE));

        // Negative checks
        assertFalse(guestItems.contains(NavigationItem.ADMIN_DASHBOARD));
        assertFalse(guestItems.contains(NavigationItem.MANAGE_USERS));
        assertFalse(guestItems.contains(NavigationItem.OWNER_REQUESTS));
        assertFalse(guestItems.contains(NavigationItem.OWNER_DASHBOARD));
        assertFalse(guestItems.contains(NavigationItem.MY_HOTELS));
        assertFalse(guestItems.contains(NavigationItem.ADD_HOTEL));
        assertFalse(guestItems.contains(NavigationItem.BUSINESS_INSIGHTS));
    }

    @Test
    @DisplayName("Hotel Owner role visibility strictly allows only 6 owner items")
    void testOwnerNavigationVisibility() {
        List<NavigationItem> ownerItems = NavigationItem.getItemsForRole(UserRole.HOTEL_OWNER);

        assertEquals(6, ownerItems.size(), "Hotel Owner must have exactly 6 permitted navigation items");
        assertTrue(ownerItems.contains(NavigationItem.OWNER_DASHBOARD));
        assertTrue(ownerItems.contains(NavigationItem.MY_HOTELS));
        assertTrue(ownerItems.contains(NavigationItem.ADD_HOTEL));
        assertTrue(ownerItems.contains(NavigationItem.OWNER_BOOKINGS));
        assertTrue(ownerItems.contains(NavigationItem.BUSINESS_INSIGHTS));
        assertTrue(ownerItems.contains(NavigationItem.MY_PROFILE));

        // Negative checks
        assertFalse(ownerItems.contains(NavigationItem.ADMIN_DASHBOARD));
        assertFalse(ownerItems.contains(NavigationItem.MANAGE_USERS));
        assertFalse(ownerItems.contains(NavigationItem.MANAGE_HOTELS));
        assertFalse(ownerItems.contains(NavigationItem.OWNER_REQUESTS));
        assertFalse(ownerItems.contains(NavigationItem.HOME));
        assertFalse(ownerItems.contains(NavigationItem.BROWSE_HOTELS));
    }

    @Test
    @DisplayName("Admin role visibility strictly allows only 10 administrative items")
    void testAdminNavigationVisibility() {
        List<NavigationItem> adminItems = NavigationItem.getItemsForRole(UserRole.ADMIN);

        assertEquals(10, adminItems.size(), "Admin must have exactly 10 permitted navigation items");
        assertTrue(adminItems.contains(NavigationItem.ADMIN_DASHBOARD));
        assertTrue(adminItems.contains(NavigationItem.MANAGE_HOTELS));
        assertTrue(adminItems.contains(NavigationItem.MANAGE_USERS));
        assertTrue(adminItems.contains(NavigationItem.MANAGE_BOOKINGS));
        assertTrue(adminItems.contains(NavigationItem.MANAGE_REVIEWS));
        assertTrue(adminItems.contains(NavigationItem.OWNER_REQUESTS));
        assertTrue(adminItems.contains(NavigationItem.BUSINESS_INSIGHTS));
        assertTrue(adminItems.contains(NavigationItem.API_DOCS));
        assertTrue(adminItems.contains(NavigationItem.API_STATUS));
        assertTrue(adminItems.contains(NavigationItem.MY_PROFILE));

        // Negative checks
        assertFalse(adminItems.contains(NavigationItem.HOME));
        assertFalse(adminItems.contains(NavigationItem.BROWSE_HOTELS));
        assertFalse(adminItems.contains(NavigationItem.ADD_HOTEL));
        assertFalse(adminItems.contains(NavigationItem.OWNER_DASHBOARD));
    }

    @Test
    @DisplayName("Unauthorized page navigation rejection")
    void testUnauthorizedNavigationRejection() {
        assertFalse(NavigationItem.ADMIN_DASHBOARD.isAccessibleBy(UserRole.USER));
        assertFalse(NavigationItem.MANAGE_USERS.isAccessibleBy(UserRole.USER));
        assertFalse(NavigationItem.BUSINESS_INSIGHTS.isAccessibleBy(UserRole.USER));
        assertFalse(NavigationItem.OWNER_REQUESTS.isAccessibleBy(UserRole.USER));

        assertFalse(NavigationItem.ADMIN_DASHBOARD.isAccessibleBy(UserRole.HOTEL_OWNER));
        assertFalse(NavigationItem.MANAGE_USERS.isAccessibleBy(UserRole.HOTEL_OWNER));
        assertFalse(NavigationItem.OWNER_REQUESTS.isAccessibleBy(UserRole.HOTEL_OWNER));

        assertFalse(NavigationItem.ADMIN_DASHBOARD.isAccessibleBy(null));
        assertFalse(NavigationItem.MY_PROFILE.isAccessibleBy(null));
    }

    @Test
    @DisplayName("Default landing page resolution by role")
    void testDefaultLandingPages() {
        assertEquals(NavigationItem.HOME, NavigationItem.getDefaultItemForRole(UserRole.USER));
        assertEquals(NavigationItem.OWNER_DASHBOARD, NavigationItem.getDefaultItemForRole(UserRole.HOTEL_OWNER));
        assertEquals(NavigationItem.ADMIN_DASHBOARD, NavigationItem.getDefaultItemForRole(UserRole.ADMIN));
    }

    @Test
    @DisplayName("Session preservation during simulated navigation")
    void testSessionPreservationDuringNavigation() {
        User guest = new User("guest-id-123", "guest@hotel.com", "Guest", "User", UserRole.USER);
        sessionManager.setSession("jwt-token-active", guest);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals(UserRole.USER, sessionManager.getRole());

        // Simulate navigating through all guest-permitted pages
        List<NavigationItem> guestItems = NavigationItem.getItemsForRole(sessionManager.getRole());
        for (NavigationItem item : guestItems) {
            assertTrue(item.isAccessibleBy(sessionManager.getRole()));
            // Session must remain intact and unchanged
            assertTrue(sessionManager.isLoggedIn());
            assertEquals("jwt-token-active", sessionManager.getToken());
            assertEquals("guest-id-123", sessionManager.getUserId());
        }
    }

    @Test
    @DisplayName("Logout clears SessionManager and user state completely")
    void testLogoutClearsSession() {
        User admin = new User("admin-id-999", "admin@hotel.com", "System", "Admin", UserRole.ADMIN);
        sessionManager.setSession("admin-jwt-token", admin);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals(UserRole.ADMIN, sessionManager.getRole());

        authService.logout();

        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getToken());
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getUserId());
        assertEquals(UserRole.USER, sessionManager.getRole()); // fallback default
    }
}


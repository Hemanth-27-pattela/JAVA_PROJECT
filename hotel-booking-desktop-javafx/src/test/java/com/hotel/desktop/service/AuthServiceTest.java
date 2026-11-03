package com.hotel.desktop.service;

import com.hotel.desktop.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private ApiClient mockApiClient;
    private SessionManager sessionManager;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mockApiClient = mock(ApiClient.class);
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
        authService = new AuthService(mockApiClient, sessionManager);
    }

    @Test
    @DisplayName("Login with invalid or blank email throws validation error before network call")
    void testLoginValidationEmail() {
        ApiException ex1 = assertThrows(ApiException.class, () -> authService.login("", "12345678"));
        assertEquals(400, ex1.getStatusCode());
        assertTrue(ex1.getMessage().contains("Please enter your email"));

        ApiException ex2 = assertThrows(ApiException.class, () -> authService.login("not-an-email", "12345678"));
        assertEquals(400, ex2.getStatusCode());
        assertTrue(ex2.getMessage().contains("valid email"));

        verifyNoInteractions(mockApiClient);
    }

    @Test
    @DisplayName("Login with blank password throws validation error before network call")
    void testLoginValidationPassword() {
        ApiException ex = assertThrows(ApiException.class, () -> authService.login("admin@hotel.com", ""));
        assertEquals(400, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Please enter your password"));

        verifyNoInteractions(mockApiClient);
    }

    @Test
    @DisplayName("Successful login sets session token, fetches full profile, and assigns role")
    void testLoginSuccess() throws Exception {
        LoginResponse loginResp = new LoginResponse();
        loginResp.setToken("valid-jwt-token-123");
        loginResp.setUserId("1");
        loginResp.setMessage("Login OK");

        User profile = new User();
        profile.setId("1");
        profile.setEmail("admin@hotel.com");
        profile.setFirstName("Admin");
        profile.setLastName("User");
        profile.setRole(UserRole.ADMIN);

        when(mockApiClient.post(eq("/api/auth/login"), any(LoginRequest.class), eq(LoginResponse.class), eq(false)))
                .thenReturn(loginResp);
        when(mockApiClient.get(eq("/api/users/me"), eq(User.class), eq(true)))
                .thenReturn(profile);

        User result = authService.login("admin@hotel.com", "12345678");

        assertNotNull(result);
        assertEquals("admin@hotel.com", result.getEmail());
        assertEquals(UserRole.ADMIN, result.getRole());

        // Verify session manager was properly populated
        assertTrue(sessionManager.isLoggedIn());
        assertEquals("valid-jwt-token-123", sessionManager.getToken());
        assertEquals(UserRole.ADMIN, sessionManager.getRole());
        assertTrue(sessionManager.isAdmin());
    }

    @Test
    @DisplayName("Login falls back to validate-token if /api/users/me endpoint is unavailable")
    void testLoginFallbackToValidateToken() throws Exception {
        LoginResponse loginResp = new LoginResponse();
        loginResp.setToken("valid-jwt-token-456");
        loginResp.setUserId("2");

        when(mockApiClient.post(eq("/api/auth/login"), any(LoginRequest.class), eq(LoginResponse.class), eq(false)))
                .thenReturn(loginResp);
        when(mockApiClient.get(eq("/api/users/me"), eq(User.class), eq(true)))
                .thenThrow(new ApiException(404, "Not found"));

        ValidateTokenResponse valResp = new ValidateTokenResponse("2", UserRole.HOTEL_OWNER);
        when(mockApiClient.get(eq("/api/auth/validate-token"), eq(ValidateTokenResponse.class), eq(true)))
                .thenReturn(valResp);

        User result = authService.login("owner@hotel.com", "12345678");

        assertNotNull(result);
        assertEquals(UserRole.HOTEL_OWNER, result.getRole());
        assertTrue(sessionManager.isHotelOwner());
    }

    @Test
    @DisplayName("Login failure propagates backend error message")
    void testLoginFailure() throws Exception {
        when(mockApiClient.post(eq("/api/auth/login"), any(LoginRequest.class), eq(LoginResponse.class), eq(false)))
                .thenThrow(new ApiException(400, "Invalid Credentials"));

        ApiException ex = assertThrows(ApiException.class, () ->
                authService.login("unknown@hotel.com", "wrongpass")
        );

        assertEquals(400, ex.getStatusCode());
        assertEquals("Invalid Credentials", ex.getMessage());
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    @DisplayName("Register validation rejects invalid names, email, and short password")
    void testRegisterValidation() {
        assertThrows(ApiException.class, () -> authService.register("", "Doe", "test@test.com", "123456"));
        assertThrows(ApiException.class, () -> authService.register("John", "", "test@test.com", "123456"));
        assertThrows(ApiException.class, () -> authService.register("John", "Doe", "not-email", "123456"));
        assertThrows(ApiException.class, () -> authService.register("John", "Doe", "test@test.com", "12345")); // < 6 chars

        verifyNoInteractions(mockApiClient);
    }

    @Test
    @DisplayName("Register sends payload with strictly guest fields (no role escalation)")
    void testRegisterPayloadIntegrity() throws Exception {
        MessageResponse okResponse = new MessageResponse("User registered OK");
        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);

        when(mockApiClient.post(eq("/api/users/register"), captor.capture(), eq(MessageResponse.class), eq(false)))
                .thenReturn(okResponse);

        MessageResponse response = authService.register("Alice", "Guest", "alice@example.com", "securePass123");

        assertNotNull(response);
        assertEquals("User registered OK", response.getMessage());

        RegisterRequest sent = captor.getValue();
        assertEquals("Alice", sent.getFirstName());
        assertEquals("Guest", sent.getLastName());
        assertEquals("alice@example.com", sent.getEmail());
        assertEquals("securePass123", sent.getPassword());
    }

    @Test
    @DisplayName("Fetch current user requires active session")
    void testFetchCurrentUserUnauthenticated() {
        ApiException ex = assertThrows(ApiException.class, () -> authService.fetchCurrentUser());
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @DisplayName("Session validation returns true when backend confirms token")
    void testValidateSessionActive() throws Exception {
        sessionManager.setSession("dummy-token", new User());
        when(mockApiClient.get(eq("/api/auth/validate-token"), eq(ValidateTokenResponse.class), eq(true)))
                .thenReturn(new ValidateTokenResponse("1", UserRole.USER));

        assertTrue(authService.validateSession());
    }

    @Test
    @DisplayName("Session validation returns false and clears session on invalid token")
    void testValidateSessionExpired() throws Exception {
        sessionManager.setSession("expired-token", new User());
        when(mockApiClient.get(eq("/api/auth/validate-token"), eq(ValidateTokenResponse.class), eq(true)))
                .thenThrow(new ApiException(401, "Token expired"));

        assertFalse(authService.validateSession());
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    @DisplayName("Logout notifies backend and clears session")
    void testLogout() throws Exception {
        sessionManager.setSession("active-token", new User());
        assertTrue(sessionManager.isLoggedIn());

        authService.logout();

        assertFalse(sessionManager.isLoggedIn());
        verify(mockApiClient).post(eq("/api/auth/logout"), isNull(), eq(MessageResponse.class), eq(true));
    }
}

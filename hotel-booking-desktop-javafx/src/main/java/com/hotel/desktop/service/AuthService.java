package com.hotel.desktop.service;

import com.hotel.desktop.model.LoginRequest;
import com.hotel.desktop.model.LoginResponse;
import com.hotel.desktop.model.MessageResponse;
import com.hotel.desktop.model.RegisterRequest;
import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;
import com.hotel.desktop.model.ValidateTokenResponse;

import java.util.regex.Pattern;

/**
 * Service managing user authentication, registration, session state, and profile queries.
 */
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final ApiClient apiClient;
    private final SessionManager sessionManager;

    public AuthService() {
        this(new ApiClient(), SessionManager.getInstance());
    }

    public AuthService(ApiClient apiClient, SessionManager sessionManager) {
        this.apiClient = apiClient;
        this.sessionManager = sessionManager;
    }

    /**
     * Authenticates user against POST /api/auth/login and loads profile via GET /api/users/me.
     *
     * @param email User email address
     * @param password User plain password
     * @return Fully populated User object with resolved role
     * @throws ApiException on invalid credentials, network error, or unexpected HTTP status
     */
    public User login(String email, String password) throws ApiException {
        validateCredentialsInput(email, password);

        LoginRequest request = new LoginRequest(email.trim().toLowerCase(), password);
        LoginResponse response = apiClient.post("/api/auth/login", request, LoginResponse.class, false);

        if (response == null || response.getToken() == null || response.getToken().isBlank()) {
            throw new ApiException(500, "Authentication server did not return a valid session token.");
        }

        String token = response.getToken();

        // Temporary basic user from login summary before full profile fetch
        User user = new User();
        user.setId(response.getUserId());
        if (response.getUser() != null) {
            user.setEmail(response.getUser().getEmail());
            user.setFirstName(response.getUser().getFirstName());
            user.setLastName(response.getUser().getLastName());
        } else {
            user.setEmail(email.trim().toLowerCase());
        }

        // Establish session so subsequent authenticated GET /api/users/me works
        sessionManager.setSession(token, user);

        try {
            // Fetch complete profile with authority/role from Spring Boot
            User fullProfile = apiClient.get("/api/users/me", User.class, true);
            if (fullProfile != null) {
                user = fullProfile;
                sessionManager.updateCurrentUser(fullProfile);
            }
        } catch (ApiException e) {
            // If /api/users/me fails, fallback to validate-token to determine role
            try {
                ValidateTokenResponse validate = apiClient.get("/api/auth/validate-token", ValidateTokenResponse.class, true);
                if (validate != null && validate.getRole() != null) {
                    user.setRole(validate.getRole());
                    sessionManager.updateCurrentUser(user);
                }
            } catch (ApiException ignored) {
                // Keep default USER role if role lookup fails
            }
        }

        return user;
    }

    /**
     * Registers a new guest account via POST /api/users/register.
     * Always assigns standard USER role on the backend.
     */
    public MessageResponse register(String firstName, String lastName, String email, String password) throws ApiException {
        validateRegistrationInput(firstName, lastName, email, password);

        RegisterRequest request = new RegisterRequest(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                password
        );

        return apiClient.post("/api/users/register", request, MessageResponse.class, false);
    }

    /**
     * Retrieves the current authenticated user's profile from GET /api/users/me.
     */
    public User fetchCurrentUser() throws ApiException {
        if (!sessionManager.isLoggedIn()) {
            throw new ApiException(401, "No active user session.");
        }

        User profile = apiClient.get("/api/users/me", User.class, true);
        if (profile != null) {
            sessionManager.updateCurrentUser(profile);
        }
        return profile;
    }

    /**
     * Validates current session token with the backend.
     */
    public boolean validateSession() {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }

        try {
            ValidateTokenResponse response = apiClient.get("/api/auth/validate-token", ValidateTokenResponse.class, true);
            return response != null && response.getUserId() != null;
        } catch (ApiException e) {
            sessionManager.clearSession();
            return false;
        }
    }

    /**
     * Clears local session and notifies the backend logout endpoint.
     */
    public void logout() {
        try {
            if (sessionManager.isLoggedIn()) {
                apiClient.post("/api/auth/logout", null, MessageResponse.class, true);
            }
        } catch (Exception ignored) {
            // Ignore network errors during logout
        } finally {
            sessionManager.clearSession();
        }
    }

    private void validateCredentialsInput(String email, String password) throws ApiException {
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException(400, "Please enter your email address.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ApiException(400, "Please enter a valid email address (e.g. user@example.com).");
        }
        if (password == null || password.isEmpty()) {
            throw new ApiException(400, "Please enter your password.");
        }
    }

    private void validateRegistrationInput(String firstName, String lastName, String email, String password) throws ApiException {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ApiException(400, "First name is required.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ApiException(400, "Last name is required.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException(400, "Email address is required.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ApiException(400, "Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new ApiException(400, "Password must be at least 6 characters long.");
        }
    }
}


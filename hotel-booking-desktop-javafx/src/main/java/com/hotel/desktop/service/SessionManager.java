package com.hotel.desktop.service;

import com.hotel.desktop.model.User;
import com.hotel.desktop.model.UserRole;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe in-memory session manager.
 * Stores JWT authentication token and current user context.
 * Passwords are never stored or logged, and tokens are kept strictly in memory.
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private final AtomicReference<String> tokenRef = new AtomicReference<>(null);
    private final AtomicReference<User> userRef = new AtomicReference<>(null);

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void setSession(String token, User user) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Session token cannot be empty");
        }
        this.tokenRef.set(token);
        this.userRef.set(user);
    }

    public synchronized void updateCurrentUser(User user) {
        this.userRef.set(user);
    }

    public synchronized void clearSession() {
        this.tokenRef.set(null);
        this.userRef.set(null);
    }

    public boolean isLoggedIn() {
        return tokenRef.get() != null && !tokenRef.get().isBlank();
    }

    public String getToken() {
        return tokenRef.get();
    }

    public User getCurrentUser() {
        return userRef.get();
    }

    public String getUserId() {
        User u = userRef.get();
        return u != null ? u.getId() : null;
    }

    public String getEmail() {
        User u = userRef.get();
        return u != null ? u.getEmail() : null;
    }

    public String getDisplayName() {
        User u = userRef.get();
        return u != null ? u.getFullName() : "Guest";
    }

    public UserRole getRole() {
        User u = userRef.get();
        return u != null && u.getRole() != null ? u.getRole() : UserRole.USER;
    }

    public boolean isAdmin() {
        return getRole() == UserRole.ADMIN;
    }

    public boolean isHotelOwner() {
        return getRole() == UserRole.HOTEL_OWNER;
    }

    public boolean isGuest() {
        return getRole() == UserRole.USER;
    }
}


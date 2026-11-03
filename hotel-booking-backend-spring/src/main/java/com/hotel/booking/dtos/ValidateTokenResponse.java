package com.hotel.booking.dtos;

import com.hotel.booking.entities.enums.UserRole;

public class ValidateTokenResponse {

    private String userId;
    private UserRole role;

    public ValidateTokenResponse() {
    }

    public ValidateTokenResponse(String userId) {
        this.userId = userId;
    }

    public ValidateTokenResponse(String userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}


package com.hotel.booking.dtos;

import com.hotel.booking.entities.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public class UpdateRoleRequest {

    @NotNull(message = "role is required")
    private UserRole role;

    public UpdateRoleRequest() {
    }

    public UpdateRoleRequest(UserRole role) {
        this.role = role;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}


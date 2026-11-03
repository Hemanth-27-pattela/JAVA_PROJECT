package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.enums.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

public class UserDto {

    @JsonProperty("_id")
    private String id;

    private String email;
    private String firstName;
    private String lastName;
    private String image;
    private UserRole role;
    private String phone;
    private AddressDto address = new AddressDto();
    private UserPreferencesDto preferences = new UserPreferencesDto();
    private Integer totalBookings = 0;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private Instant lastLogin;
    private Boolean isActive = true;
    private Boolean emailVerified = false;
    private Instant createdAt;
    private Instant updatedAt;

    public UserDto() {
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    @JsonAlias("id")
    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address != null ? address : new AddressDto();
    }

    public UserPreferencesDto getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferencesDto preferences) {
        this.preferences = preferences != null ? preferences : new UserPreferencesDto();
    }

    public Integer getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static UserDto fromEntity(com.hotel.booking.entities.User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setImage(user.getImage());
        dto.setRole(user.getRole());
        dto.setPhone(user.getPhone());
        if (user.getAddress() != null) {
            dto.setAddress(new AddressDto(
                    user.getAddress().getStreet(),
                    user.getAddress().getCity(),
                    user.getAddress().getState(),
                    user.getAddress().getCountry(),
                    user.getAddress().getZipCode()
            ));
        }
        if (user.getPreferences() != null) {
            UserPreferencesDto prefsDto = new UserPreferencesDto();
            try {
                if (user.getPreferences().getPreferredDestinations() != null) {
                    prefsDto.setPreferredDestinations(new ArrayList<>(user.getPreferences().getPreferredDestinations()));
                }
            } catch (Exception ignored) {
                // Ignore if uninitialized in detached context
            }
            try {
                if (user.getPreferences().getPreferredHotelTypes() != null) {
                    prefsDto.setPreferredHotelTypes(new ArrayList<>(user.getPreferences().getPreferredHotelTypes()));
                }
            } catch (Exception ignored) {
                // Ignore if uninitialized in detached context
            }
            if (user.getPreferences().getBudgetRange() != null) {
                prefsDto.setBudgetRange(new BudgetRangeDto(
                        user.getPreferences().getBudgetRange().getMin(),
                        user.getPreferences().getBudgetRange().getMax()
                ));
            }
            dto.setPreferences(prefsDto);
        }
        dto.setTotalBookings(user.getTotalBookings());
        dto.setTotalSpent(user.getTotalSpent());
        dto.setLastLogin(user.getLastLogin());
        dto.setIsActive(user.getIsActive());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}


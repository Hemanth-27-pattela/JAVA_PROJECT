package com.hotel.booking.dtos.ownerrequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateOwnerRequestDto {

    @NotBlank(message = "Business or Hotel name is required")
    @Size(min = 2, max = 255, message = "Business name must be between 2 and 255 characters")
    private String businessName;

    @NotBlank(message = "Business contact email is required")
    @Email(message = "Must be a valid email address")
    private String businessEmail;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String businessPhone;

    @Size(max = 1000, message = "Address cannot exceed 1000 characters")
    private String businessAddress;

    @Size(max = 2000, message = "Justification cannot exceed 2000 characters")
    private String justification;

    public CreateOwnerRequestDto() {
    }

    public CreateOwnerRequestDto(String businessName, String businessEmail, String businessPhone,
                                 String businessAddress, String justification) {
        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.businessPhone = businessPhone;
        this.businessAddress = businessAddress;
        this.justification = justification;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}


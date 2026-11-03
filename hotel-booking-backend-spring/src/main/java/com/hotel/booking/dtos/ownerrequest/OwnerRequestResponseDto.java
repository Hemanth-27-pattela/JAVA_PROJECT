package com.hotel.booking.dtos.ownerrequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.HotelOwnerRequest;
import com.hotel.booking.entities.enums.RequestStatus;
import java.time.Instant;

public class OwnerRequestResponseDto {

    @JsonProperty("_id")
    private String id;

    private String userId;
    private String userEmail;
    private String userName;

    private String businessName;
    private String businessEmail;
    private String businessPhone;
    private String businessAddress;
    private String justification;

    private RequestStatus status;
    private String adminNotes;
    private String rejectionReason;

    private String reviewedById;
    private String reviewedByEmail;
    private Instant reviewedAt;

    private Instant createdAt;
    private Instant updatedAt;

    public OwnerRequestResponseDto() {
    }

    public static OwnerRequestResponseDto fromEntity(HotelOwnerRequest entity) {
        if (entity == null) {
            return null;
        }
        OwnerRequestResponseDto dto = new OwnerRequestResponseDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        if (entity.getUser() != null) {
            dto.setUserEmail(entity.getUser().getEmail());
            String name = (entity.getUser().getFirstName() + " " + entity.getUser().getLastName()).trim();
            dto.setUserName(name);
        }
        dto.setBusinessName(entity.getBusinessName());
        dto.setBusinessEmail(entity.getBusinessEmail());
        dto.setBusinessPhone(entity.getBusinessPhone());
        dto.setBusinessAddress(entity.getBusinessAddress());
        dto.setJustification(entity.getJustification());
        dto.setStatus(entity.getStatus());
        dto.setAdminNotes(entity.getAdminNotes());
        dto.setRejectionReason(entity.getRejectionReason());

        if (entity.getReviewedBy() != null) {
            dto.setReviewedById(entity.getReviewedBy().getId());
            dto.setReviewedByEmail(entity.getReviewedBy().getEmail());
        } else {
            dto.setReviewedById(entity.getReviewedById());
        }
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(String reviewedById) {
        this.reviewedById = reviewedById;
    }

    public String getReviewedByEmail() {
        return reviewedByEmail;
    }

    public void setReviewedByEmail(String reviewedByEmail) {
        this.reviewedByEmail = reviewedByEmail;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
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
}


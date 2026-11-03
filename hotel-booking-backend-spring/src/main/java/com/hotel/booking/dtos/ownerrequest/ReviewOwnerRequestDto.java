package com.hotel.booking.dtos.ownerrequest;

import jakarta.validation.constraints.Size;

public class ReviewOwnerRequestDto {

    @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters")
    private String adminNotes;

    @Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
    private String rejectionReason;

    public ReviewOwnerRequestDto() {
    }

    public ReviewOwnerRequestDto(String adminNotes, String rejectionReason) {
        this.adminNotes = adminNotes;
        this.rejectionReason = rejectionReason;
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
}


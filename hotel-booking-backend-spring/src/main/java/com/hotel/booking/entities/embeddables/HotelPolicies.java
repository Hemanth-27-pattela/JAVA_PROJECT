package com.hotel.booking.entities.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class HotelPolicies {

    @Column(name = "policy_check_in_time")
    private String checkInTime;

    @Column(name = "policy_check_out_time")
    private String checkOutTime;

    @Column(name = "policy_cancellation", columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Column(name = "policy_pet")
    private String petPolicy;

    @Column(name = "policy_smoking")
    private String smokingPolicy;

    public HotelPolicies() {
    }

    public HotelPolicies(String checkInTime, String checkOutTime, String cancellationPolicy, String petPolicy, String smokingPolicy) {
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.cancellationPolicy = cancellationPolicy;
        this.petPolicy = petPolicy;
        this.smokingPolicy = smokingPolicy;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(String cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    public String getPetPolicy() {
        return petPolicy;
    }

    public void setPetPolicy(String petPolicy) {
        this.petPolicy = petPolicy;
    }

    public String getSmokingPolicy() {
        return smokingPolicy;
    }

    public void setSmokingPolicy(String smokingPolicy) {
        this.smokingPolicy = smokingPolicy;
    }
}


package com.hotel.booking.dtos;

public class HotelPoliciesDto {
    private String checkInTime;
    private String checkOutTime;
    private String cancellationPolicy;
    private String petPolicy;
    private String smokingPolicy;

    public HotelPoliciesDto() {
    }

    public HotelPoliciesDto(String checkInTime, String checkOutTime, String cancellationPolicy, String petPolicy, String smokingPolicy) {
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


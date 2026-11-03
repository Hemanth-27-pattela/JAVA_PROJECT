package com.hotel.booking.dtos.businessinsights;

public class GuestMixDto {
    private Long adults = 0L;
    private Long children = 0L;

    public GuestMixDto() {
    }

    public GuestMixDto(Long adults, Long children) {
        this.adults = adults;
        this.children = children;
    }

    public Long getAdults() { return adults; }
    public void setAdults(Long adults) { this.adults = adults; }

    public Long getChildren() { return children; }
    public void setChildren(Long children) { this.children = children; }
}


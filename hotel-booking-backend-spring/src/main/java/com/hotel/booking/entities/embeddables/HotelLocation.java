package com.hotel.booking.entities.embeddables;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class HotelLocation {

    @Column(name = "loc_latitude")
    private Double latitude;

    @Column(name = "loc_longitude")
    private Double longitude;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "loc_street")),
        @AttributeOverride(name = "city", column = @Column(name = "loc_city")),
        @AttributeOverride(name = "state", column = @Column(name = "loc_state")),
        @AttributeOverride(name = "country", column = @Column(name = "loc_country")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "loc_zip_code"))
    })
    private Address address;

    public HotelLocation() {
    }

    public HotelLocation(Double latitude, Double longitude, Address address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}


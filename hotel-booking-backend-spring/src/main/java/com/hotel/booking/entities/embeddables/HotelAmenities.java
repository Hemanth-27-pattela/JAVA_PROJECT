package com.hotel.booking.entities.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class HotelAmenities {

    @Column(name = "amenity_parking")
    private Boolean parking = false;

    @Column(name = "amenity_wifi")
    private Boolean wifi = false;

    @Column(name = "amenity_pool")
    private Boolean pool = false;

    @Column(name = "amenity_gym")
    private Boolean gym = false;

    @Column(name = "amenity_spa")
    private Boolean spa = false;

    @Column(name = "amenity_restaurant")
    private Boolean restaurant = false;

    @Column(name = "amenity_bar")
    private Boolean bar = false;

    @Column(name = "amenity_airport_shuttle")
    private Boolean airportShuttle = false;

    @Column(name = "amenity_business_center")
    private Boolean businessCenter = false;

    public HotelAmenities() {
    }

    public Boolean getParking() {
        return parking;
    }

    public void setParking(Boolean parking) {
        this.parking = parking;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    public Boolean getPool() {
        return pool;
    }

    public void setPool(Boolean pool) {
        this.pool = pool;
    }

    public Boolean getGym() {
        return gym;
    }

    public void setGym(Boolean gym) {
        this.gym = gym;
    }

    public Boolean getSpa() {
        return spa;
    }

    public void setSpa(Boolean spa) {
        this.spa = spa;
    }

    public Boolean getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Boolean restaurant) {
        this.restaurant = restaurant;
    }

    public Boolean getBar() {
        return bar;
    }

    public void setBar(Boolean bar) {
        this.bar = bar;
    }

    public Boolean getAirportShuttle() {
        return airportShuttle;
    }

    public void setAirportShuttle(Boolean airportShuttle) {
        this.airportShuttle = airportShuttle;
    }

    public Boolean getBusinessCenter() {
        return businessCenter;
    }

    public void setBusinessCenter(Boolean businessCenter) {
        this.businessCenter = businessCenter;
    }
}


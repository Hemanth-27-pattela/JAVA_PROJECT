package com.hotel.booking.dtos;

public class HotelAmenitiesDto {
    private Boolean parking = false;
    private Boolean wifi = false;
    private Boolean pool = false;
    private Boolean gym = false;
    private Boolean spa = false;
    private Boolean restaurant = false;
    private Boolean bar = false;
    private Boolean airportShuttle = false;
    private Boolean businessCenter = false;

    public HotelAmenitiesDto() {
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


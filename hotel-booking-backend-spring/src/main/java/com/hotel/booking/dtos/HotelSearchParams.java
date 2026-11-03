package com.hotel.booking.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HotelSearchParams {

    private String destination;
    private String checkIn;
    private String checkOut;
    private Integer adultCount;
    private Integer childCount;
    private Integer page = 1;
    private List<String> facilities = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<Integer> stars = new ArrayList<>();
    private BigDecimal maxPrice;
    private String sortOption;

    public HotelSearchParams() {
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public Integer getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }

    public Integer getChildCount() {
        return childCount;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }

    public Integer getPage() {
        return page != null && page > 0 ? page : 1;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types != null ? types : new ArrayList<>();
    }

    public List<Integer> getStars() {
        return stars;
    }

    public void setStars(List<Integer> stars) {
        this.stars = stars != null ? stars : new ArrayList<>();
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortOption() {
        return sortOption;
    }

    public void setSortOption(String sortOption) {
        this.sortOption = sortOption;
    }
}


package com.hotel.desktop.model;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates search and filter criteria for querying hotels.
 * Serializes parameters to standard URL query strings matching Spring Boot /api/hotels/search.
 */
public class HotelSearchParams {

    private String destination;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer adultCount;
    private Integer childCount;
    private Integer page;
    private BigDecimal maxPrice;
    private String sortOption = "";
    private List<String> facilities = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<Integer> stars = new ArrayList<>();

    public HotelSearchParams() {
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
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

    public List<String> getFacilities() {
        return facilities != null ? facilities : new ArrayList<>();
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    public List<String> getTypes() {
        return types != null ? types : new ArrayList<>();
    }

    public void setTypes(List<String> types) {
        this.types = types != null ? types : new ArrayList<>();
    }

    public List<Integer> getStars() {
        return stars != null ? stars : new ArrayList<>();
    }

    public void setStars(List<Integer> stars) {
        this.stars = stars != null ? stars : new ArrayList<>();
    }

    /**
     * Builds standard URL query parameters (e.g. ?destination=London&page=1&stars=5&stars=4)
     */
    public String toQueryString() {
        List<String> queryParts = new ArrayList<>();

        if (destination != null && !destination.trim().isEmpty()) {
            queryParts.add("destination=" + urlEncode(destination.trim()));
        }
        if (checkIn != null) {
            queryParts.add("checkIn=" + urlEncode(checkIn.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        }
        if (checkOut != null) {
            queryParts.add("checkOut=" + urlEncode(checkOut.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        }
        if (adultCount != null && adultCount > 0) {
            queryParts.add("adultCount=" + adultCount);
        }
        if (childCount != null && childCount > 0) {
            queryParts.add("childCount=" + childCount);
        }
        if (page != null && page > 1) {
            queryParts.add("page=" + page);
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            queryParts.add("maxPrice=" + maxPrice.toPlainString());
        }
        if (sortOption != null && !sortOption.trim().isEmpty()) {
            queryParts.add("sortOption=" + urlEncode(sortOption.trim()));
        }

        if (stars != null) {
            for (Integer star : stars) {
                if (star != null) {
                    queryParts.add("stars=" + star);
                }
            }
        }
        if (types != null) {
            for (String t : types) {
                if (t != null && !t.trim().isEmpty()) {
                    queryParts.add("types=" + urlEncode(t.trim()));
                }
            }
        }
        if (facilities != null) {
            for (String f : facilities) {
                if (f != null && !f.trim().isEmpty()) {
                    queryParts.add("facilities=" + urlEncode(f.trim()));
                }
            }
        }

        if (queryParts.isEmpty()) {
            return "";
        }
        return "?" + String.join("&", queryParts);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

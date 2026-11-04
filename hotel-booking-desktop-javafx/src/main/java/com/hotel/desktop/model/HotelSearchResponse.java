package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Envelope response returned by GET /api/hotels/search
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelSearchResponse {

    private List<Hotel> data = new ArrayList<>();
    private Pagination pagination = new Pagination();

    public HotelSearchResponse() {
    }

    public HotelSearchResponse(List<Hotel> data, Pagination pagination) {
        this.data = data != null ? data : new ArrayList<>();
        this.pagination = pagination != null ? pagination : new Pagination();
    }

    public List<Hotel> getData() {
        return data != null ? data : new ArrayList<>();
    }

    public void setData(List<Hotel> data) {
        this.data = data != null ? data : new ArrayList<>();
    }

    public Pagination getPagination() {
        return pagination != null ? pagination : new Pagination();
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination != null ? pagination : new Pagination();
    }
}

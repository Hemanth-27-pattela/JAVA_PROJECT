package com.hotel.booking.dtos;

import java.util.ArrayList;
import java.util.List;

public class HotelSearchResponse {
    private List<HotelDto> data = new ArrayList<>();
    private PaginationDto pagination = new PaginationDto();

    public HotelSearchResponse() {
    }

    public HotelSearchResponse(List<HotelDto> data, PaginationDto pagination) {
        this.data = data != null ? data : new ArrayList<>();
        this.pagination = pagination != null ? pagination : new PaginationDto();
    }

    public List<HotelDto> getData() {
        return data;
    }

    public void setData(List<HotelDto> data) {
        this.data = data != null ? data : new ArrayList<>();
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination != null ? pagination : new PaginationDto();
    }
}


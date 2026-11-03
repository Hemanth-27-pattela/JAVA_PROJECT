package com.hotel.booking.dtos;

public class HotelLocationDto {
    private Double latitude;
    private Double longitude;
    private AddressDto address = new AddressDto();

    public HotelLocationDto() {
    }

    public HotelLocationDto(Double latitude, Double longitude, AddressDto address) {
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

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address != null ? address : new AddressDto();
    }
}


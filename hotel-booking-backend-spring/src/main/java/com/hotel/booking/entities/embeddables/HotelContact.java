package com.hotel.booking.entities.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class HotelContact {

    @Column(name = "contact_phone")
    private String phone;

    @Column(name = "contact_email")
    private String email;

    @Column(name = "contact_website")
    private String website;

    public HotelContact() {
    }

    public HotelContact(String phone, String email, String website) {
        this.phone = phone;
        this.email = email;
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}


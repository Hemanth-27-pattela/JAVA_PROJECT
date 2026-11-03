package com.hotel.booking.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class BookingDto {

    @JsonProperty("_id")
    private String id;

    private String userId;
    private String hotelId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer adultCount = 1;
    private Integer childCount = 0;
    private Instant checkIn;
    private Instant checkOut;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BookingStatus status = BookingStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentMethod;
    private String specialRequests;
    private String cancellationReason;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private String stripePaymentIntentId;
    private Instant createdAt;
    private Instant updatedAt;

    public BookingDto() {
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    @JsonAlias("id")
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Instant getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Instant checkIn) {
        this.checkIn = checkIn;
    }

    public Instant getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Instant checkOut) {
        this.checkOut = checkOut;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static BookingDto fromEntity(com.hotel.booking.entities.Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser() != null ? booking.getUser().getId() : booking.getUserId());
        dto.setHotelId(booking.getHotel() != null ? booking.getHotel().getId() : booking.getHotelId());
        dto.setFirstName(booking.getFirstName());
        dto.setLastName(booking.getLastName());
        dto.setEmail(booking.getEmail());
        dto.setPhone(booking.getPhone());
        dto.setAdultCount(booking.getAdultCount());
        dto.setChildCount(booking.getChildCount());
        dto.setCheckIn(booking.getCheckIn());
        dto.setCheckOut(booking.getCheckOut());
        dto.setTotalCost(booking.getTotalCost());
        dto.setStatus(booking.getStatus());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setPaymentMethod(booking.getPaymentMethod());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setCancellationReason(booking.getCancellationReason());
        dto.setRefundAmount(booking.getRefundAmount());
        dto.setStripePaymentIntentId(booking.getStripePaymentIntentId());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }
}


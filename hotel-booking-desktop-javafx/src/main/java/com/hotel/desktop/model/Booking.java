package com.hotel.desktop.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model representing an individual guest booking matching Spring Boot BookingDto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    @JsonProperty("_id")
    private String id;

    private String userId;
    private String hotelId;
    private String hotelName;
    private String hotelLocation;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private Integer adultCount = 1;
    private Integer childCount = 0;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant checkIn;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant checkOut;

    private BigDecimal totalCost = BigDecimal.ZERO;
    private BookingStatus status = BookingStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String paymentMethod;
    private String specialRequests;
    private String cancellationReason;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private String stripePaymentIntentId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    public Booking() {
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

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelLocation() {
        return hotelLocation;
    }

    public void setHotelLocation(String hotelLocation) {
        this.hotelLocation = hotelLocation;
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

    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
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
        return adultCount != null ? adultCount : 1;
    }

    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }

    public Integer getChildCount() {
        return childCount != null ? childCount : 0;
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
        return totalCost != null ? totalCost : BigDecimal.ZERO;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BookingStatus getStatus() {
        return status != null ? status : BookingStatus.PENDING;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
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
        return refundAmount != null ? refundAmount : BigDecimal.ZERO;
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

    // === Convenience Presentation Methods ===

    public long getNightsCount() {
        if (checkIn == null || checkOut == null) {
            return 1;
        }
        LocalDate in = checkIn.atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate out = checkOut.atZone(ZoneId.of("UTC")).toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(in, out);
        return Math.max(1, days);
    }

    public String getFormattedTotalCost() {
        return "£" + String.format(Locale.UK, "%.2f", getTotalCost());
    }

    public String getFormattedCheckIn() {
        return checkIn != null ? DATE_FORMATTER.format(checkIn) : "N/A";
    }

    public String getFormattedCheckOut() {
        return checkOut != null ? DATE_FORMATTER.format(checkOut) : "N/A";
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? DATE_TIME_FORMATTER.format(createdAt) : "N/A";
    }

    public String getGuestsSummary() {
        int adults = getAdultCount();
        int children = getChildCount();
        String adultsStr = adults + " " + (adults == 1 ? "Adult" : "Adults");
        if (children > 0) {
            return adultsStr + ", " + children + " " + (children == 1 ? "Child" : "Children");
        }
        return adultsStr;
    }

    /**
     * Determines whether the current booking can be cancelled:
     * Allowed only if status is PENDING or CONFIRMED and check-in date is in the future.
     */
    public boolean isCancellable() {
        BookingStatus st = getStatus();
        boolean validStatus = (st == BookingStatus.PENDING || st == BookingStatus.CONFIRMED);
        boolean futureCheckIn = checkIn != null && checkIn.isAfter(Instant.now());
        return validStatus && futureCheckIn;
    }

    public boolean isPastOrCompleted() {
        return getStatus() == BookingStatus.COMPLETED ||
                (checkOut != null && checkOut.isBefore(Instant.now()) && getStatus() != BookingStatus.CANCELLED);
    }
}

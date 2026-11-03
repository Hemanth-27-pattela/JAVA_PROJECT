package com.hotel.booking.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.embeddables.ReviewCategories;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_hotel_rating", columnList = "hotel_id, rating"),
    @Index(name = "idx_reviews_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_reviews_hotel_created", columnList = "hotel_id, created_at"),
    @Index(name = "idx_reviews_booking_id", columnList = "booking_id", unique = true)
})
public class Review {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    @JsonProperty("_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    @Column(name = "hotel_id", insertable = false, updatable = false)
    private String hotelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @JsonIgnore
    private Booking booking;

    @Column(name = "booking_id", insertable = false, updatable = false)
    private String bookingId;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating = 5;

    @NotBlank
    @Lob
    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Embedded
    private ReviewCategories categories = new ReviewCategories();

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Review() {
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.isVerified == null) {
            this.isVerified = false;
        }
        if (this.helpfulCount == null) {
            this.helpfulCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    @JsonProperty("userId")
    public String getUserId() {
        return user != null ? user.getId() : this.userId;
    }

    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
        if (this.user == null && userId != null) {
            User u = new User();
            u.setId(userId);
            this.user = u;
        }
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        if (hotel != null) {
            this.hotelId = hotel.getId();
        }
    }

    @JsonProperty("hotelId")
    public String getHotelId() {
        return hotel != null ? hotel.getId() : this.hotelId;
    }

    @JsonProperty("hotelId")
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
        if (this.hotel == null && hotelId != null) {
            Hotel h = new Hotel();
            h.setId(hotelId);
            this.hotel = h;
        }
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
        if (booking != null) {
            this.bookingId = booking.getId();
        }
    }

    @JsonProperty("bookingId")
    public String getBookingId() {
        return booking != null ? booking.getId() : this.bookingId;
    }

    @JsonProperty("bookingId")
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
        if (this.booking == null && bookingId != null) {
            Booking b = new Booking();
            b.setId(bookingId);
            this.booking = b;
        }
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReviewCategories getCategories() {
        return categories;
    }

    public void setCategories(ReviewCategories categories) {
        this.categories = categories != null ? categories : new ReviewCategories();
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean verified) {
        isVerified = verified;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
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
}


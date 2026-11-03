package com.hotel.booking.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.embeddables.HotelAmenities;
import com.hotel.booking.entities.embeddables.HotelContact;
import com.hotel.booking.entities.embeddables.HotelLocation;
import com.hotel.booking.entities.embeddables.HotelPolicies;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hotels", indexes = {
    @Index(name = "idx_hotels_user_id", columnList = "user_id"),
    @Index(name = "idx_hotels_city_star", columnList = "city, star_rating"),
    @Index(name = "idx_hotels_price_star", columnList = "price_per_night, star_rating"),
    @Index(name = "idx_hotels_user_created", columnList = "user_id, created_at")
})
public class Hotel {

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

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank
    @Column(name = "country", nullable = false)
    private String country;

    @NotBlank
    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "hotel_types", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "type", nullable = false)
    private List<String> type = new ArrayList<>();

    @NotNull
    @Min(0)
    @Column(name = "adult_count", nullable = false)
    private Integer adultCount = 1;

    @NotNull
    @Min(0)
    @Column(name = "child_count", nullable = false)
    private Integer childCount = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "hotel_facilities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "facility", nullable = false)
    private List<String> facilities = new ArrayList<>();

    @NotNull
    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight = BigDecimal.ZERO;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "star_rating", nullable = false)
    private Integer starRating = 1;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "hotel_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url", nullable = false, length = 1024)
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Embedded
    private HotelLocation location = new HotelLocation();

    @Embedded
    private HotelContact contact = new HotelContact();

    @Embedded
    private HotelPolicies policies = new HotelPolicies();

    @Embedded
    private HotelAmenities amenities = new HotelAmenities();

    @Column(name = "total_bookings")
    private Integer totalBookings = 0;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "occupancy_rate")
    private Double occupancyRate = 0.0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Hotel() {
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
        if (this.lastUpdated == null) {
            this.lastUpdated = now;
        }
        if (this.totalBookings == null) {
            this.totalBookings = 0;
        }
        if (this.totalRevenue == null) {
            this.totalRevenue = BigDecimal.ZERO;
        }
        if (this.averageRating == null) {
            this.averageRating = 0.0;
        }
        if (this.reviewCount == null) {
            this.reviewCount = 0;
        }
        if (this.occupancyRate == null) {
            this.occupancyRate = 0.0;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isFeatured == null) {
            this.isFeatured = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        this.lastUpdated = Instant.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type != null ? type : new ArrayList<>();
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

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getStarRating() {
        return starRating;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public HotelLocation getLocation() {
        return location;
    }

    public void setLocation(HotelLocation location) {
        this.location = location != null ? location : new HotelLocation();
    }

    public HotelContact getContact() {
        return contact;
    }

    public void setContact(HotelContact contact) {
        this.contact = contact != null ? contact : new HotelContact();
    }

    public HotelPolicies getPolicies() {
        return policies;
    }

    public void setPolicies(HotelPolicies policies) {
        this.policies = policies != null ? policies : new HotelPolicies();
    }

    public HotelAmenities getAmenities() {
        return amenities;
    }

    public void setAmenities(HotelAmenities amenities) {
        this.amenities = amenities != null ? amenities : new HotelAmenities();
    }

    public Integer getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(Double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean featured) {
        isFeatured = featured;
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

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}


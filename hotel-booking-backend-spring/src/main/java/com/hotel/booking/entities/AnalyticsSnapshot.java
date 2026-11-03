package com.hotel.booking.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotel.booking.entities.converters.AnalyticsBreakdownConverter;
import com.hotel.booking.entities.embeddables.AnalyticsBreakdown;
import com.hotel.booking.entities.embeddables.AnalyticsMetrics;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analytics_snapshots", indexes = {
    @Index(name = "idx_analytics_date", columnList = "date"),
    @Index(name = "idx_analytics_revenue", columnList = "total_revenue")
})
public class AnalyticsSnapshot {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    @JsonProperty("_id")
    private String id;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @Embedded
    private AnalyticsMetrics metrics = new AnalyticsMetrics();

    @Lob
    @Convert(converter = AnalyticsBreakdownConverter.class)
    @Column(name = "breakdown_json", columnDefinition = "LONGTEXT")
    private AnalyticsBreakdown breakdown = new AnalyticsBreakdown();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AnalyticsSnapshot() {
    }

    public AnalyticsSnapshot(Instant date, AnalyticsMetrics metrics, AnalyticsBreakdown breakdown) {
        this.date = date;
        this.metrics = metrics != null ? metrics : new AnalyticsMetrics();
        this.breakdown = breakdown != null ? breakdown : new AnalyticsBreakdown();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        if (this.date == null) {
            this.date = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.metrics == null) {
            this.metrics = new AnalyticsMetrics();
        }
        if (this.breakdown == null) {
            this.breakdown = new AnalyticsBreakdown();
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

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public AnalyticsMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(AnalyticsMetrics metrics) {
        this.metrics = metrics != null ? metrics : new AnalyticsMetrics();
    }

    public AnalyticsBreakdown getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(AnalyticsBreakdown breakdown) {
        this.breakdown = breakdown != null ? breakdown : new AnalyticsBreakdown();
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


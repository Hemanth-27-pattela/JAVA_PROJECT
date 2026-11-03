package com.hotel.booking.entities.embeddables;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class UserPreferences {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_destinations", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "destination")
    private List<String> preferredDestinations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_hotel_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "hotel_type")
    private List<String> preferredHotelTypes = new ArrayList<>();

    @Embedded
    private BudgetRange budgetRange = new BudgetRange();

    public UserPreferences() {
    }

    public List<String> getPreferredDestinations() {
        return preferredDestinations;
    }

    public void setPreferredDestinations(List<String> preferredDestinations) {
        this.preferredDestinations = preferredDestinations != null ? preferredDestinations : new ArrayList<>();
    }

    public List<String> getPreferredHotelTypes() {
        return preferredHotelTypes;
    }

    public void setPreferredHotelTypes(List<String> preferredHotelTypes) {
        this.preferredHotelTypes = preferredHotelTypes != null ? preferredHotelTypes : new ArrayList<>();
    }

    public BudgetRange getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(BudgetRange budgetRange) {
        this.budgetRange = budgetRange != null ? budgetRange : new BudgetRange();
    }
}


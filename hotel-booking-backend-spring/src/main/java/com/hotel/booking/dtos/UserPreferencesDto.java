package com.hotel.booking.dtos;

import java.util.ArrayList;
import java.util.List;

public class UserPreferencesDto {
    private List<String> preferredDestinations = new ArrayList<>();
    private List<String> preferredHotelTypes = new ArrayList<>();
    private BudgetRangeDto budgetRange = new BudgetRangeDto();

    public UserPreferencesDto() {
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

    public BudgetRangeDto getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(BudgetRangeDto budgetRange) {
        this.budgetRange = budgetRange != null ? budgetRange : new BudgetRangeDto();
    }
}


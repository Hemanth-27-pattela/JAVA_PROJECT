package com.hotel.booking.entities.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class BudgetRange {

    @Column(name = "budget_min", precision = 10, scale = 2)
    private BigDecimal min;

    @Column(name = "budget_max", precision = 10, scale = 2)
    private BigDecimal max;

    public BudgetRange() {
    }

    public BudgetRange(BigDecimal min, BigDecimal max) {
        this.min = min;
        this.max = max;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }
}


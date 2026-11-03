package com.hotel.booking.dtos;

import java.math.BigDecimal;

public class BudgetRangeDto {
    private BigDecimal min;
    private BigDecimal max;

    public BudgetRangeDto() {
    }

    public BudgetRangeDto(BigDecimal min, BigDecimal max) {
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


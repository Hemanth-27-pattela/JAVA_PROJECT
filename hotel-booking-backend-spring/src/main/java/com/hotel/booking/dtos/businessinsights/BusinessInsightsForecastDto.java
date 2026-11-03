package com.hotel.booking.dtos.businessinsights;

import java.util.ArrayList;
import java.util.List;

public class BusinessInsightsForecastDto {
    private List<WeeklyHistoricalItemDto> historical = new ArrayList<>();
    private List<WeeklyForecastItemDto> forecasts = new ArrayList<>();
    private Double seasonalGrowth = 0.0;
    private TrendSummaryDto trends = new TrendSummaryDto();
    private String lastUpdated;

    public BusinessInsightsForecastDto() {
    }

    public List<WeeklyHistoricalItemDto> getHistorical() { return historical; }
    public void setHistorical(List<WeeklyHistoricalItemDto> historical) { this.historical = historical; }

    public List<WeeklyForecastItemDto> getForecasts() { return forecasts; }
    public void setForecasts(List<WeeklyForecastItemDto> forecasts) { this.forecasts = forecasts; }

    public Double getSeasonalGrowth() { return seasonalGrowth; }
    public void setSeasonalGrowth(Double seasonalGrowth) { this.seasonalGrowth = seasonalGrowth; }

    public TrendSummaryDto getTrends() { return trends; }
    public void setTrends(TrendSummaryDto trends) { this.trends = trends; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}


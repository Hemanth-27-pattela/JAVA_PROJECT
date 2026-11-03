package com.hotel.booking.services;

import com.hotel.booking.dtos.AnalyticsBreakdownDto;
import com.hotel.booking.dtos.AnalyticsMetricsDto;
import com.hotel.booking.dtos.AnalyticsSnapshotDto;
import com.hotel.booking.dtos.businessinsights.*;
import com.hotel.booking.entities.AnalyticsSnapshot;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Review;
import com.hotel.booking.entities.embeddables.AnalyticsBreakdown;
import com.hotel.booking.entities.embeddables.AnalyticsMetrics;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.repositories.AnalyticsSnapshotRepository;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusinessInsightsService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final AnalyticsSnapshotRepository analyticsSnapshotRepository;
    private final long appStartTime = System.currentTimeMillis();

    public BusinessInsightsService(HotelRepository hotelRepository,
                                   UserRepository userRepository,
                                   BookingRepository bookingRepository,
                                   ReviewRepository reviewRepository,
                                   AnalyticsSnapshotRepository analyticsSnapshotRepository) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.analyticsSnapshotRepository = analyticsSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public BusinessInsightsDashboardDto getDashboardData() {
        return getDashboardData(null, true);
    }

    @Transactional(readOnly = true)
    public BusinessInsightsDashboardDto getDashboardData(String userId, boolean isAdmin) {
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        List<Hotel> allHotels;
        List<Booking> allBookings;
        List<Review> allReviews;
        long totalUsers = 0;

        if (isAdmin) {
            allHotels = hotelRepository.findAll();
            allBookings = bookingRepository.findAll();
            allReviews = reviewRepository.findAll();
            totalUsers = userRepository.count();
        } else {
            allHotels = (userId != null) ? hotelRepository.findByUserIdOrderByCreatedAtDesc(userId) : Collections.emptyList();
            if (allHotels.isEmpty()) {
                return createEmptyDashboard(now.toString());
            }
            List<String> hotelIds = allHotels.stream().map(Hotel::getId).collect(Collectors.toList());
            allBookings = bookingRepository.findByHotelIdIn(hotelIds);
            allReviews = reviewRepository.findByHotelIdIn(hotelIds);
            totalUsers = allBookings.stream().map(Booking::getUserId).filter(Objects::nonNull).distinct().count();
        }

        long totalHotels = allHotels.size();
        long totalBookings = allBookings.size();

        long recentBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(thirtyDaysAgo))
                .count();

        double totalRevenue = allBookings.stream()
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                .sum();

        double recentRevenue = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(thirtyDaysAgo))
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                .sum();

        // Month-over-month calculation
        LocalDate todayDate = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate firstOfCurrentMonth = todayDate.withDayOfMonth(1);
        LocalDate firstOfPrevMonth = firstOfCurrentMonth.minusMonths(1);
        Instant currentMonthStart = firstOfCurrentMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant prevMonthStart = firstOfPrevMonth.atStartOfDay(ZoneOffset.UTC).toInstant();

        double currentMonthRevenue = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(currentMonthStart))
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                .sum();

        double prevMonthRevenue = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(prevMonthStart) && b.getCreatedAt().isBefore(currentMonthStart))
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                .sum();

        double revenueGrowth = 0.0;
        if (prevMonthRevenue > 0) {
            revenueGrowth = ((currentMonthRevenue - prevMonthRevenue) / prevMonthRevenue) * 100.0;
        }

        // Status counts
        long cancelledBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        long confirmedBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED).count();
        long pendingBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long refundedBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.REFUNDED || b.getPaymentStatus() == PaymentStatus.REFUNDED).count();

        double totalRefundAmount = allBookings.stream()
                .mapToDouble(b -> b.getRefundAmount() != null ? b.getRefundAmount().doubleValue() : 0.0)
                .sum();

        double cancellationRate = totalBookings > 0
                ? Math.round(((double) cancelledBookings / totalBookings) * 10000.0) / 100.0
                : 0.0;

        // Reviews
        long totalReviews = allReviews.size();
        long verifiedReviewCount = allReviews.stream().filter(r -> Boolean.TRUE.equals(r.getIsVerified())).count();
        double avgReviewRating = totalReviews > 0
                ? round2(allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0))
                : 0.0;

        double cleanlinessAvg = totalReviews > 0
                ? round2(allReviews.stream().filter(r -> r.getCategories() != null).mapToInt(r -> r.getCategories().getCleanliness()).average().orElse(0.0))
                : 0.0;
        double serviceAvg = totalReviews > 0
                ? round2(allReviews.stream().filter(r -> r.getCategories() != null).mapToInt(r -> r.getCategories().getService()).average().orElse(0.0))
                : 0.0;
        double locationAvg = totalReviews > 0
                ? round2(allReviews.stream().filter(r -> r.getCategories() != null).mapToInt(r -> r.getCategories().getLocation()).average().orElse(0.0))
                : 0.0;
        double valueAvg = totalReviews > 0
                ? round2(allReviews.stream().filter(r -> r.getCategories() != null).mapToInt(r -> r.getCategories().getValue()).average().orElse(0.0))
                : 0.0;
        double amenitiesAvg = totalReviews > 0
                ? round2(allReviews.stream().filter(r -> r.getCategories() != null).mapToInt(r -> r.getCategories().getAmenities()).average().orElse(0.0))
                : 0.0;

        ReviewCategoryAveragesDto catAverages = new ReviewCategoryAveragesDto(
                cleanlinessAvg, serviceAvg, locationAvg, valueAvg, amenitiesAvg
        );

        // LOS / ADR / Party Mix
        long totalNights = 0L;
        long totalAdults = 0L;
        long totalChildren = 0L;

        for (Booking b : allBookings) {
            long nights = 1;
            if (b.getCheckIn() != null && b.getCheckOut() != null && b.getCheckOut().isAfter(b.getCheckIn())) {
                nights = Math.max(1, Duration.between(b.getCheckIn(), b.getCheckOut()).toDays());
            }
            totalNights += nights;
            totalAdults += (b.getAdultCount() != null ? b.getAdultCount() : 1);
            totalChildren += (b.getChildCount() != null ? b.getChildCount() : 0);
        }

        double avgLos = totalBookings > 0 ? round2((double) totalNights / totalBookings) : 0.0;
        double adr = totalNights > 0 ? round2(totalRevenue / totalNights) : 0.0;
        double avgPartySize = totalBookings > 0 ? round2((double) (totalAdults + totalChildren) / totalBookings) : 0.0;
        GuestMixDto guestMix = new GuestMixDto(totalAdults, totalChildren);

        OverviewMetricsDto overview = new OverviewMetricsDto();
        overview.setTotalHotels(totalHotels);
        overview.setTotalUsers(totalUsers);
        overview.setTotalBookings(totalBookings);
        overview.setRecentBookings(recentBookings);
        overview.setTotalRevenue(round2(totalRevenue));
        overview.setRecentRevenue(round2(recentRevenue));
        overview.setRevenueGrowth(round2(revenueGrowth));
        overview.setCancelledBookings(cancelledBookings);
        overview.setConfirmedBookings(confirmedBookings);
        overview.setPendingBookings(pendingBookings);
        overview.setRefundedBookings(refundedBookings);
        overview.setTotalRefundAmount(round2(totalRefundAmount));
        overview.setCancellationRate(cancellationRate);
        overview.setTotalReviews(totalReviews);
        overview.setAvgReviewRating(avgReviewRating);
        overview.setAvgLos(avgLos);
        overview.setAdr(adr);
        overview.setAvgPartySize(avgPartySize);
        overview.setVerifiedReviewCount(verifiedReviewCount);

        // Map hotel lookup for fast destination / performance mapping
        Map<String, Hotel> hotelMap = allHotels.stream().collect(Collectors.toMap(Hotel::getId, h -> h, (h1, h2) -> h1));

        // Popular destinations
        Map<String, List<Booking>> bookingsByCity = new HashMap<>();
        for (Booking b : allBookings) {
            Hotel h = hotelMap.get(b.getHotelId());
            if (h != null && h.getCity() != null) {
                bookingsByCity.computeIfAbsent(h.getCity(), k -> new ArrayList<>()).add(b);
            }
        }

        List<PopularDestinationDto> popularDestinations = new ArrayList<>();
        for (Map.Entry<String, List<Booking>> entry : bookingsByCity.entrySet()) {
            String city = entry.getKey();
            List<Booking> cityBookings = entry.getValue();
            long count = cityBookings.size();
            double cityRevenue = cityBookings.stream()
                    .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                    .sum();
            double avgPrice = allHotels.stream()
                    .filter(h -> city.equalsIgnoreCase(h.getCity()) && h.getPricePerNight() != null)
                    .mapToDouble(h -> h.getPricePerNight().doubleValue())
                    .average().orElse(0.0);
            popularDestinations.add(new PopularDestinationDto(city, count, round2(cityRevenue), round2(avgPrice)));
        }
        popularDestinations.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        if (popularDestinations.size() > 5) {
            popularDestinations = popularDestinations.subList(0, 5);
        }

        // Fallback popular destinations if bookings are empty
        if (popularDestinations.isEmpty() && !allHotels.isEmpty()) {
            Map<String, List<Hotel>> hotelsByCity = allHotels.stream()
                    .filter(h -> h.getCity() != null)
                    .collect(Collectors.groupingBy(Hotel::getCity));
            for (Map.Entry<String, List<Hotel>> entry : hotelsByCity.entrySet()) {
                double avgPrice = entry.getValue().stream()
                        .filter(h -> h.getPricePerNight() != null)
                        .mapToDouble(h -> h.getPricePerNight().doubleValue())
                        .average().orElse(0.0);
                popularDestinations.add(new PopularDestinationDto(entry.getKey(), 1L, 0.0, round2(avgPrice)));
            }
            if (popularDestinations.size() > 5) {
                popularDestinations = popularDestinations.subList(0, 5);
            }
        }

        // Daily bookings (last 7 recorded days)
        Map<String, Integer> dailyCount = new TreeMap<>();
        for (Booking b : allBookings) {
            if (b.getCreatedAt() != null) {
                String dateKey = LocalDate.ofInstant(b.getCreatedAt(), ZoneOffset.UTC).toString();
                dailyCount.put(dateKey, dailyCount.getOrDefault(dateKey, 0) + 1);
            }
        }
        List<DailyBookingDto> dailyBookings = dailyCount.entrySet().stream()
                .map(e -> new DailyBookingDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        if (dailyBookings.size() > 7) {
            dailyBookings = dailyBookings.subList(dailyBookings.size() - 7, dailyBookings.size());
        }

        // Hotel performance
        Map<String, List<Booking>> bookingsByHotel = allBookings.stream()
                .filter(b -> b.getHotelId() != null)
                .collect(Collectors.groupingBy(Booking::getHotelId));

        List<HotelPerformanceDto> hotelPerformance = new ArrayList<>();
        for (Map.Entry<String, List<Booking>> entry : bookingsByHotel.entrySet()) {
            Hotel h = hotelMap.get(entry.getKey());
            if (h != null) {
                long bCount = entry.getValue().size();
                double bRev = entry.getValue().stream()
                        .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                        .sum();
                hotelPerformance.add(new HotelPerformanceDto(
                        h.getId(),
                        h.getName(),
                        h.getCity(),
                        h.getStarRating(),
                        h.getPricePerNight() != null ? h.getPricePerNight().doubleValue() : 0.0,
                        bCount,
                        round2(bRev)
                ));
            }
        }
        hotelPerformance.sort((a, b) -> Long.compare(b.getBookingCount(), a.getBookingCount()));
        if (hotelPerformance.size() > 10) {
            hotelPerformance = hotelPerformance.subList(0, 10);
        }

        // Fallback hotel performance if bookings are empty
        if (hotelPerformance.isEmpty() && !allHotels.isEmpty()) {
            for (Hotel h : allHotels) {
                hotelPerformance.add(new HotelPerformanceDto(
                        h.getId(),
                        h.getName(),
                        h.getCity(),
                        h.getStarRating(),
                        h.getPricePerNight() != null ? h.getPricePerNight().doubleValue() : 0.0,
                        0L,
                        0.0
                ));
            }
            if (hotelPerformance.size() > 10) {
                hotelPerformance = hotelPerformance.subList(0, 10);
            }
        }

        // Status breakdown
        Map<String, Long> statusMap = new HashMap<>();
        Map<String, Long> payStatusMap = new HashMap<>();
        for (Booking b : allBookings) {
            String s = b.getStatus() != null ? b.getStatus().name().toLowerCase() : "pending";
            statusMap.put(s, statusMap.getOrDefault(s, 0L) + 1);

            String p = b.getPaymentStatus() != null ? b.getPaymentStatus().name().toLowerCase() : "pending";
            payStatusMap.put(p, payStatusMap.getOrDefault(p, 0L) + 1);
        }

        List<StatusCountDto> bookingStatusBreakdown = statusMap.entrySet().stream()
                .map(e -> new StatusCountDto(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        List<StatusCountDto> paymentStatusBreakdown = payStatusMap.entrySet().stream()
                .map(e -> new StatusCountDto(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        // Hotels by star rating
        Map<Integer, Long> starMap = new TreeMap<>();
        for (Hotel h : allHotels) {
            int stars = h.getStarRating() != null ? h.getStarRating() : 0;
            starMap.put(stars, starMap.getOrDefault(stars, 0L) + 1);
        }
        List<HotelsByStarDto> hotelsByStar = starMap.entrySet().stream()
                .map(e -> new HotelsByStarDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        BusinessInsightsDashboardDto dashboard = new BusinessInsightsDashboardDto();
        dashboard.setOverview(overview);
        dashboard.setPopularDestinations(popularDestinations);
        dashboard.setDailyBookings(dailyBookings);
        dashboard.setHotelPerformance(hotelPerformance);
        dashboard.setBookingStatusBreakdown(bookingStatusBreakdown);
        dashboard.setPaymentStatusBreakdown(paymentStatusBreakdown);
        dashboard.setGuestMix(guestMix);
        dashboard.setReviewCategoryAverages(catAverages);
        dashboard.setHotelsByStar(hotelsByStar);
        dashboard.setLastUpdated(now.toString());

        return dashboard;
    }

    private BusinessInsightsDashboardDto createEmptyDashboard(String lastUpdated) {
        OverviewMetricsDto overview = new OverviewMetricsDto();
        overview.setTotalHotels(0L);
        overview.setTotalUsers(0L);
        overview.setTotalBookings(0L);
        overview.setRecentBookings(0L);
        overview.setTotalRevenue(0.0);
        overview.setRecentRevenue(0.0);
        overview.setRevenueGrowth(0.0);
        overview.setCancelledBookings(0L);
        overview.setConfirmedBookings(0L);
        overview.setPendingBookings(0L);
        overview.setRefundedBookings(0L);
        overview.setTotalRefundAmount(0.0);
        overview.setCancellationRate(0.0);
        overview.setTotalReviews(0L);
        overview.setAvgReviewRating(0.0);
        overview.setAvgLos(0.0);
        overview.setAdr(0.0);
        overview.setAvgPartySize(0.0);
        overview.setVerifiedReviewCount(0L);

        ReviewCategoryAveragesDto catAverages = new ReviewCategoryAveragesDto(0.0, 0.0, 0.0, 0.0, 0.0);
        GuestMixDto guestMix = new GuestMixDto(0L, 0L);

        BusinessInsightsDashboardDto dashboard = new BusinessInsightsDashboardDto();
        dashboard.setOverview(overview);
        dashboard.setPopularDestinations(Collections.emptyList());
        dashboard.setDailyBookings(Collections.emptyList());
        dashboard.setHotelPerformance(Collections.emptyList());
        dashboard.setBookingStatusBreakdown(Collections.emptyList());
        dashboard.setPaymentStatusBreakdown(Collections.emptyList());
        dashboard.setGuestMix(guestMix);
        dashboard.setReviewCategoryAverages(catAverages);
        dashboard.setHotelsByStar(Collections.emptyList());
        dashboard.setLastUpdated(lastUpdated);

        return dashboard;
    }

    @Transactional(readOnly = true)
    public BusinessInsightsForecastDto getForecastData() {
        return getForecastData(null, true);
    }

    @Transactional(readOnly = true)
    public BusinessInsightsForecastDto getForecastData(String userId, boolean isAdmin) {
        Instant now = Instant.now();
        Instant twoMonthsAgo = now.minus(60, ChronoUnit.DAYS);

        List<Booking> allBookings;
        if (isAdmin) {
            allBookings = bookingRepository.findAll();
        } else {
            List<Hotel> ownerHotels = (userId != null) ? hotelRepository.findByUserIdOrderByCreatedAtDesc(userId) : Collections.emptyList();
            if (ownerHotels.isEmpty()) {
                BusinessInsightsForecastDto emptyDto = new BusinessInsightsForecastDto();
                emptyDto.setHistorical(Collections.emptyList());
                emptyDto.setForecasts(Collections.emptyList());
                emptyDto.setSeasonalGrowth(0.0);
                emptyDto.setTrends(new TrendSummaryDto("stable", "stable"));
                emptyDto.setLastUpdated(now.toString());
                return emptyDto;
            }
            List<String> hotelIds = ownerHotels.stream().map(Hotel::getId).collect(Collectors.toList());
            allBookings = bookingRepository.findByHotelIdIn(hotelIds);
        }

        List<Booking> historicalBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(twoMonthsAgo))
                .collect(Collectors.toList());

        // Group into weekly buckets starting on Sunday
        Map<String, double[]> weekGroups = new TreeMap<>(); // key -> [count, revenue]

        for (Booking b : historicalBookings) {
            LocalDate bDate = LocalDate.ofInstant(b.getCreatedAt(), ZoneOffset.UTC);
            LocalDate sundayStart = bDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            String weekKey = sundayStart.toString();

            double rev = b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0;
            double[] acc = weekGroups.computeIfAbsent(weekKey, k -> new double[]{0.0, 0.0});
            acc[0] += 1.0;
            acc[1] += rev;
        }

        List<WeeklyHistoricalItemDto> historical = new ArrayList<>();
        for (Map.Entry<String, double[]> e : weekGroups.entrySet()) {
            historical.add(new WeeklyHistoricalItemDto(
                    e.getKey(),
                    (int) Math.round(e.getValue()[0]),
                    round2(e.getValue()[1])
            ));
        }

        // Linear regression trend calculation
        int n = historical.size();
        double[] bookingTrendLine = calculateTrend(historical.stream().mapToDouble(WeeklyHistoricalItemDto::getBookings).toArray());
        double[] revenueTrendLine = calculateTrend(historical.stream().mapToDouble(WeeklyHistoricalItemDto::getRevenue).toArray());

        double bookingSlope = bookingTrendLine[0];
        double bookingIntercept = bookingTrendLine[1];
        double revenueSlope = revenueTrendLine[0];
        double revenueIntercept = revenueTrendLine[1];

        List<WeeklyForecastItemDto> forecasts = new ArrayList<>();
        LocalDate todayDate = LocalDate.ofInstant(now, ZoneOffset.UTC);

        for (int i = 1; i <= 4; i++) {
            int weekIndex = historical.size() + i - 1;
            int forecastedBookings = 0;
            double forecastedRevenue = 0.0;

            if (historical.size() > 1) {
                forecastedBookings = Math.max(0, (int) Math.round(bookingSlope * weekIndex + bookingIntercept));
                forecastedRevenue = Math.max(0.0, revenueSlope * weekIndex + revenueIntercept);
            } else if (historical.size() == 1) {
                WeeklyHistoricalItemDto baseWeek = historical.get(0);
                forecastedBookings = Math.max(1, (int) Math.round(baseWeek.getBookings() * (0.9 + i * 0.1)));
                forecastedRevenue = Math.max(100.0, baseWeek.getRevenue() * (0.9 + i * 0.1));
            }

            LocalDate forecastDate = todayDate.plusDays((long) i * 7);
            double confidence = Math.max(0.6, 1.0 - i * 0.1);

            forecasts.add(new WeeklyForecastItemDto(
                    forecastDate.toString(),
                    forecastedBookings,
                    round2(forecastedRevenue),
                    round2(confidence)
            ));
        }

        // Seasonal growth: current month vs last month bookings count
        LocalDate firstOfCurrentMonth = todayDate.withDayOfMonth(1);
        LocalDate firstOfPrevMonth = firstOfCurrentMonth.minusMonths(1);
        Instant currentMonthStart = firstOfCurrentMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant prevMonthStart = firstOfPrevMonth.atStartOfDay(ZoneOffset.UTC).toInstant();

        long currentMonthBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(currentMonthStart))
                .count();

        long lastMonthBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(prevMonthStart) && b.getCreatedAt().isBefore(currentMonthStart))
                .count();

        double seasonalGrowth = 0.0;
        if (lastMonthBookings > 0) {
            seasonalGrowth = ((double) (currentMonthBookings - lastMonthBookings) / lastMonthBookings) * 100.0;
        }

        String bookingTrend = historical.size() > 1 ? (bookingSlope > 0 ? "increasing" : "decreasing") : "stable";
        String revenueTrend = historical.size() > 1 ? (revenueSlope > 0 ? "increasing" : "decreasing") : "stable";

        BusinessInsightsForecastDto dto = new BusinessInsightsForecastDto();
        dto.setHistorical(historical);
        dto.setForecasts(forecasts);
        dto.setSeasonalGrowth(round2(seasonalGrowth));
        dto.setTrends(new TrendSummaryDto(bookingTrend, revenueTrend));
        dto.setLastUpdated(now.toString());

        return dto;
    }

    private double[] calculateTrend(double[] data) {
        int n = data.length;
        if (n <= 1) {
            return new double[]{0.0, n == 1 ? data[0] : 0.0};
        }
        double sumX = (double) (n * (n - 1)) / 2.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            sumY += data[i];
            sumXY += data[i] * i;
        }
        double sumXX = (double) (n * (n - 1) * (2 * n - 1)) / 6.0;
        double denominator = n * sumXX - sumX * sumX;
        double slope = denominator != 0 ? (n * sumXY - sumX * sumY) / denominator : 0.0;
        double intercept = (sumY - slope * sumX) / n;
        return new double[]{slope, intercept};
    }

    @Transactional(readOnly = true)
    public SystemStatsDto getSystemStats(boolean includeSystemTelemetry) {
        return getSystemStats(null, true, includeSystemTelemetry);
    }

    @Transactional(readOnly = true)
    public SystemStatsDto getSystemStats(String userId, boolean isAdmin) {
        return getSystemStats(userId, isAdmin, isAdmin);
    }

    @Transactional(readOnly = true)
    public SystemStatsDto getSystemStats(String userId, boolean isAdmin, boolean includeSystemTelemetry) {
        long totalHotels;
        List<Booking> allBookings;

        if (isAdmin) {
            totalHotels = hotelRepository.count();
            allBookings = bookingRepository.findAll();
        } else {
            List<Hotel> ownerHotels = (userId != null) ? hotelRepository.findByUserIdOrderByCreatedAtDesc(userId) : Collections.emptyList();
            totalHotels = ownerHotels.size();
            if (ownerHotels.isEmpty()) {
                allBookings = Collections.emptyList();
            } else {
                List<String> hotelIds = ownerHotels.stream().map(Hotel::getId).collect(Collectors.toList());
                allBookings = bookingRepository.findByHotelIdIn(hotelIds);
            }
        }

        long totalBookings = allBookings.size();
        double totalRevenue = allBookings.stream()
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                .sum();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant todayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant weekAgo = today.minusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();

        long todayBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(todayStart))
                .count();

        long thisWeekBookings = allBookings.stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(weekAgo))
                .count();

        SystemStatsDto dto = new SystemStatsDto();
        dto.setDatabase(new SystemStatsDto.DatabaseStatsDto(totalHotels, totalBookings, round2(totalRevenue)));
        dto.setApplication(new SystemStatsDto.ApplicationStatsDto(
                75L,
                35L,
                0.01,
                "99.9%",
                todayBookings,
                thisWeekBookings
        ));
        dto.setLastUpdated(Instant.now().toString());

        if (includeSystemTelemetry) {
            Runtime rt = Runtime.getRuntime();
            long heapUsed = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long heapTotal = rt.totalMemory() / (1024 * 1024);
            int percentage = heapTotal > 0 ? (int) Math.round(((double) heapUsed / heapTotal) * 100.0) : 0;
            long uptimeSeconds = (System.currentTimeMillis() - appStartTime) / 1000;

            dto.setSystem(new SystemStatsDto.SystemMetricsDto(
                    new SystemStatsDto.MemoryDto(heapUsed, heapTotal, percentage),
                    uptimeSeconds
            ));
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsSnapshotDto> getRollups(Integer limit) {
        int effectiveLimit = (limit != null && limit > 0) ? Math.min(limit, 50) : 20;
        Pageable pageable = PageRequest.of(0, effectiveLimit);
        return analyticsSnapshotRepository.findAllByOrderByDateDesc(pageable).stream()
                .map(AnalyticsSnapshotDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnalyticsSnapshotDto createRollup() {
        long totalHotels = hotelRepository.count();
        long totalUsers = userRepository.count();
        List<Booking> allBookings = bookingRepository.findAll();
        List<Hotel> allHotels = hotelRepository.findAll();
        List<Review> allReviews = reviewRepository.findAll();

        long totalBookings = allBookings.size();
        double totalRevenue = allBookings.stream()
                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0)
                .sum();

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("pending", 0L);
        byStatus.put("confirmed", 0L);
        byStatus.put("cancelled", 0L);
        byStatus.put("completed", 0L);
        byStatus.put("refunded", 0L);

        Map<String, Long> byPaymentStatus = new HashMap<>();
        byPaymentStatus.put("pending", 0L);
        byPaymentStatus.put("paid", 0L);
        byPaymentStatus.put("failed", 0L);
        byPaymentStatus.put("refunded", 0L);

        for (Booking b : allBookings) {
            String s = b.getStatus() != null ? b.getStatus().name().toLowerCase() : "pending";
            if (byStatus.containsKey(s)) byStatus.put(s, byStatus.get(s) + 1);

            String p = b.getPaymentStatus() != null ? b.getPaymentStatus().name().toLowerCase() : "pending";
            if (byPaymentStatus.containsKey(p)) byPaymentStatus.put(p, byPaymentStatus.get(p) + 1);
        }

        long cancelledCount = byStatus.get("cancelled") + byStatus.get("refunded");
        double cancellationRate = totalBookings > 0 ? ((double) cancelledCount / totalBookings) * 100.0 : 0.0;
        double avgBookingValue = totalBookings > 0 ? totalRevenue / totalBookings : 0.0;
        double avgRating = allReviews.size() > 0
                ? round2(allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0))
                : 0.0;

        AnalyticsMetrics metrics = new AnalyticsMetrics(
                totalBookings,
                BigDecimal.valueOf(totalRevenue).setScale(2, RoundingMode.HALF_UP),
                totalUsers,
                totalHotels,
                BigDecimal.valueOf(avgBookingValue).setScale(2, RoundingMode.HALF_UP),
                0.0,
                round2(cancellationRate),
                avgRating
        );

        // Destination breakdown
        Map<String, Hotel> hotelMap = allHotels.stream().collect(Collectors.toMap(Hotel::getId, h -> h, (h1, h2) -> h1));
        Map<String, double[]> destMap = new HashMap<>();
        for (Booking b : allBookings) {
            Hotel h = hotelMap.get(b.getHotelId());
            if (h != null && h.getCity() != null) {
                double[] acc = destMap.computeIfAbsent(h.getCity(), k -> new double[]{0.0, 0.0});
                acc[0] += 1.0;
                acc[1] += (b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0);
            }
        }

        List<AnalyticsBreakdown.DestinationBreakdown> destList = destMap.entrySet().stream()
                .map(e -> new AnalyticsBreakdown.DestinationBreakdown(e.getKey(), (long) e.getValue()[0], round2(e.getValue()[1])))
                .sorted((a, b) -> Long.compare(b.getBookings(), a.getBookings()))
                .limit(10)
                .collect(Collectors.toList());

        // Hotel type breakdown
        Map<String, double[]> typeMap = new HashMap<>();
        for (Hotel h : allHotels) {
            if (h.getType() != null) {
                for (String t : h.getType()) {
                    if (t != null && !t.isBlank()) {
                        double[] acc = typeMap.computeIfAbsent(t, k -> new double[]{0.0, 0.0});
                        // Attribute bookings of this hotel
                        long bCount = allBookings.stream().filter(b -> h.getId().equals(b.getHotelId())).count();
                        double bRev = allBookings.stream().filter(b -> h.getId().equals(b.getHotelId()))
                                .mapToDouble(b -> b.getTotalCost() != null ? b.getTotalCost().doubleValue() : 0.0).sum();
                        acc[0] += bCount;
                        acc[1] += bRev;
                    }
                }
            }
        }

        List<AnalyticsBreakdown.HotelTypeBreakdown> typeList = typeMap.entrySet().stream()
                .map(e -> new AnalyticsBreakdown.HotelTypeBreakdown(e.getKey(), (long) e.getValue()[0], round2(e.getValue()[1])))
                .sorted((a, b) -> Long.compare(b.getBookings(), a.getBookings()))
                .limit(10)
                .collect(Collectors.toList());

        AnalyticsBreakdown breakdown = new AnalyticsBreakdown();
        breakdown.setByStatus(byStatus);
        breakdown.setByPaymentStatus(byPaymentStatus);
        breakdown.setByDestination(destList);
        breakdown.setByHotelType(typeList);

        AnalyticsSnapshot snapshot = new AnalyticsSnapshot(Instant.now(), metrics, breakdown);
        AnalyticsSnapshot saved = analyticsSnapshotRepository.save(snapshot);

        return AnalyticsSnapshotDto.fromEntity(saved);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}


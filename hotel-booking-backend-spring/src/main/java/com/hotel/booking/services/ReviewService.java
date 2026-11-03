package com.hotel.booking.services;

import com.hotel.booking.dtos.CreateReviewRequest;
import com.hotel.booking.dtos.ReviewDto;
import com.hotel.booking.dtos.ReviewSummaryDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Review;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.embeddables.ReviewCategories;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         HotelRepository hotelRepository,
                         BookingRepository bookingRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewDto createReview(CreateReviewRequest request, String userId) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking not found for this user/hotel"));

        if (!booking.getUserId().equals(userId) || !booking.getHotelId().equals(request.getHotelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking not found for this user/hotel");
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this booking");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = new Review();
        review.setUser(user);
        review.setHotel(hotel);
        review.setBooking(booking);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        if (request.getCategories() != null) {
            ReviewCategories categories = new ReviewCategories(
                    request.getCategories().getCleanliness(),
                    request.getCategories().getService(),
                    request.getCategories().getLocation(),
                    request.getCategories().getValue(),
                    request.getCategories().getAmenities()
            );
            review.setCategories(categories);
        }

        boolean isVerified = booking.getPaymentStatus() == PaymentStatus.PAID;
        review.setIsVerified(isVerified);

        try {
            review = reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this booking", ex);
        }

        // Recalculate hotel average rating and review count
        Double avg = reviewRepository.calculateAverageRatingByHotelId(hotel.getId());
        long count = reviewRepository.countByHotelId(hotel.getId());
        double roundedAvg = avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        hotel.setAverageRating(roundedAvg);
        hotel.setReviewCount((int) count);
        hotelRepository.save(hotel);

        return ReviewDto.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByHotelId(String hotelId) {
        Pageable pageable = PageRequest.of(0, 100);
        return reviewRepository.findByHotelIdOrderByCreatedAtDesc(hotelId, pageable).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDto getReviewSummary(String hotelId) {
        Double avg = reviewRepository.calculateAverageRatingByHotelId(hotelId);
        long count = reviewRepository.countByHotelId(hotelId);
        double roundedAvg = avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        return new ReviewSummaryDto(hotelId, roundedAvg, (int) count);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews(Integer limit) {
        int effectiveLimit = (limit != null && limit > 0) ? Math.min(limit, 200) : 100;
        Pageable pageable = PageRequest.of(0, effectiveLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findAll(pageable).getContent().stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }
}


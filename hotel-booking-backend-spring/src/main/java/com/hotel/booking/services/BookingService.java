package com.hotel.booking.services;

import com.hotel.booking.dtos.BookingDto;
import com.hotel.booking.dtos.CancelBookingRequest;
import com.hotel.booking.dtos.CancelBookingResponse;
import com.hotel.booking.dtos.CreateBookingRequest;
import com.hotel.booking.dtos.HotelWithBookingsDto;
import com.hotel.booking.dtos.PaymentIntentRequest;
import com.hotel.booking.dtos.PaymentIntentResponse;
import com.hotel.booking.dtos.UpdateBookingPaymentRequest;
import com.hotel.booking.dtos.UpdateBookingStatusRequest;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.services.payment.PaymentGatewayService;
import com.hotel.booking.services.payment.PaymentIntentResult;
import com.hotel.booking.services.payment.PaymentVerificationResult;
import com.hotel.booking.services.payment.RefundResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;

    public BookingService(
            BookingRepository bookingRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository,
            PaymentGatewayService paymentGatewayService
    ) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.paymentGatewayService = paymentGatewayService;
    }

    @Transactional
    public BookingDto createBooking(String hotelId, CreateBookingRequest request, String userId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        if (Boolean.FALSE.equals(hotel.getIsActive())) {
            throw new IllegalArgumentException("Hotel is currently inactive");
        }

        if (request.getCheckIn() == null || request.getCheckOut() == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }

        if (!request.getCheckIn().isBefore(request.getCheckOut())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.getAdultCount() == null || request.getAdultCount() < 1) {
            throw new IllegalArgumentException("At least 1 adult is required");
        }

        if (request.getChildCount() != null && request.getChildCount() < 0) {
            throw new IllegalArgumentException("Child count cannot be negative");
        }

        if (hotel.getAdultCount() != null && request.getAdultCount() > hotel.getAdultCount()) {
            throw new IllegalArgumentException("Adult count exceeds hotel capacity");
        }

        // Overlapping reservation check: user cannot double-book same hotel for overlapping dates
        boolean hasOverlap = bookingRepository.existsOverlappingBookingForUser(
                userId,
                hotelId,
                request.getCheckIn(),
                request.getCheckOut(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING)
        );
        if (hasOverlap) {
            throw new IllegalArgumentException("You already have an active reservation for this hotel during the selected dates");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long nights = Math.max(1, Duration.between(request.getCheckIn(), request.getCheckOut()).toDays());
        BigDecimal calculatedCost = hotel.getPricePerNight().multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        // If client provided a totalCost, verify it matches calculated total cost
        if (request.getTotalCost() != null && request.getTotalCost().compareTo(calculatedCost) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total cost does not match calculated price for booking duration");
        }

        String paymentIntentId = request.getPaymentIntentId();
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment intent ID is required");
        }

        // Server-side payment verification (against Stripe or verified simulation store)
        PaymentVerificationResult verification = paymentGatewayService.verifyPayment(
                paymentIntentId,
                hotelId,
                userId,
                calculatedCost,
                "gbp"
        );

        if (!verification.isVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, verification.getFailureReason());
        }

        BigDecimal verifiedCost = verification.getAmount() != null ? verification.getAmount() : calculatedCost;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setUserId(userId);
        booking.setHotel(hotel);
        booking.setHotelId(hotelId);
        booking.setFirstName(request.getFirstName());
        booking.setLastName(request.getLastName());
        booking.setEmail(request.getEmail());
        booking.setPhone(request.getPhone());
        booking.setAdultCount(request.getAdultCount());
        booking.setChildCount(request.getChildCount() != null ? request.getChildCount() : 0);
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setTotalCost(verifiedCost);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentMethod("card");
        // Always store the verified PaymentIntent ID from the server-side verification result
        booking.setStripePaymentIntentId(verification.getPaymentIntentId());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Update hotel analytics
        hotel.setTotalBookings((hotel.getTotalBookings() != null ? hotel.getTotalBookings() : 0) + 1);
        BigDecimal currentHotelRevenue = hotel.getTotalRevenue() != null ? hotel.getTotalRevenue() : BigDecimal.ZERO;
        hotel.setTotalRevenue(currentHotelRevenue.add(verifiedCost));
        hotelRepository.save(hotel);

        // Update user analytics
        user.setTotalBookings((user.getTotalBookings() != null ? user.getTotalBookings() : 0) + 1);
        BigDecimal currentUserSpent = user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO;
        user.setTotalSpent(currentUserSpent.add(verifiedCost));
        userRepository.save(user);

        return BookingDto.fromEntity(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<HotelWithBookingsDto> getMyBookings(String userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<HotelWithBookingsDto> result = new ArrayList<>();

        for (Booking booking : bookings) {
            String hId = booking.getHotel() != null ? booking.getHotel().getId() : booking.getHotelId();
            if (hId == null) continue;

            Hotel hotel = hotelRepository.findById(hId).orElse(null);
            if (hotel == null) continue;

            HotelWithBookingsDto dto = new HotelWithBookingsDto();
            // Copy hotel properties
            com.hotel.booking.dtos.HotelDto hotelDto = com.hotel.booking.dtos.HotelDto.fromEntity(hotel);
            if (hotelDto != null) {
                dto.setId(hotelDto.getId());
                dto.setUserId(hotelDto.getUserId());
                dto.setName(hotelDto.getName());
                dto.setCity(hotelDto.getCity());
                dto.setCountry(hotelDto.getCountry());
                dto.setDescription(hotelDto.getDescription());
                dto.setType(hotelDto.getType());
                dto.setAdultCount(hotelDto.getAdultCount());
                dto.setChildCount(hotelDto.getChildCount());
                dto.setFacilities(hotelDto.getFacilities());
                dto.setPricePerNight(hotelDto.getPricePerNight());
                dto.setStarRating(hotelDto.getStarRating());
                dto.setImageUrls(hotelDto.getImageUrls());
                dto.setLastUpdated(hotelDto.getLastUpdated());
                dto.setLocation(hotelDto.getLocation());
                dto.setContact(hotelDto.getContact());
                dto.setPolicies(hotelDto.getPolicies());
                dto.setAmenities(hotelDto.getAmenities());
                dto.setTotalBookings(hotelDto.getTotalBookings());
                dto.setTotalRevenue(hotelDto.getTotalRevenue());
                dto.setAverageRating(hotelDto.getAverageRating());
                dto.setReviewCount(hotelDto.getReviewCount());
                dto.setIsActive(hotelDto.getIsActive());
                dto.setIsFeatured(hotelDto.getIsFeatured());
                dto.setCreatedAt(hotelDto.getCreatedAt());
                dto.setUpdatedAt(hotelDto.getUpdatedAt());
            }
            dto.setBookings(List.of(BookingDto.fromEntity(booking)));
            result.add(dto);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BookingDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByHotelId(String hotelId, String userId, boolean isAdmin) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        if (!isAdmin && (hotel.getUser() == null || !hotel.getUser().getId().equals(userId))) {
            throw new AccessDeniedException("Access denied");
        }

        return bookingRepository.findByHotelIdOrderByCreatedAtDesc(hotelId).stream()
                .map(BookingDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingDto getBookingById(String id, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        assertCanViewOrCancel(booking, userId, isAdmin);
        return BookingDto.fromEntity(booking);
    }

    @Transactional
    public CancelBookingResponse cancelBooking(String id, CancelBookingRequest request, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        assertCanViewOrCancel(booking, userId, isAdmin);

        // Check if cancellable (upcoming pending or confirmed)
        boolean isCancellableStatus = booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED;
        boolean isUpcoming = booking.getCheckIn() != null && booking.getCheckIn().isAfter(Instant.now());

        if (!isCancellableStatus || !isUpcoming) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking cannot be cancelled (must be upcoming pending/confirmed)");
        }

        boolean wasPaid = booking.getPaymentStatus() == PaymentStatus.PAID;
        double refundAmount = 0.0;
        String refundSkipped = null;

        if (wasPaid && booking.getStripePaymentIntentId() != null && !booking.getStripePaymentIntentId().isBlank()) {
            String idempotencyKey = "refund_booking_" + booking.getId() + "_" + booking.getStripePaymentIntentId();
            RefundResult refundResult = paymentGatewayService.processRefund(
                    booking.getStripePaymentIntentId(),
                    booking.getTotalCost(),
                    idempotencyKey
            );

            if (refundResult == null || !refundResult.isSuccess()) {
                String errorMsg = (refundResult != null && refundResult.getFailureReason() != null)
                        ? refundResult.getFailureReason()
                        : "Stripe refund failed";
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, errorMsg);
            }

            refundAmount = refundResult.getRefundAmount() != null
                    ? refundResult.getRefundAmount().doubleValue()
                    : (booking.getTotalCost() != null ? booking.getTotalCost().doubleValue() : 0.0);
            refundSkipped = refundResult.getRefundSkipped();

            if (refundAmount > 0) {
                booking.setPaymentStatus(PaymentStatus.REFUNDED);
                booking.setRefundAmount(BigDecimal.valueOf(refundAmount));
            }
        } else if (wasPaid) {
            // Legacy bookings created before PI persistence - cancel without fake refund
            refundSkipped = "Cancelled without Stripe refund (no payment intent on file)";
            booking.setRefundAmount(BigDecimal.ZERO);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (request != null && request.getCancellationReason() != null && !request.getCancellationReason().isBlank()) {
            booking.setCancellationReason(request.getCancellationReason().trim());
        }
        booking.setUpdatedAt(Instant.now());

        bookingRepository.save(booking);

        // Adjust stats if was paid
        if (wasPaid) {
            String bHotelId = booking.getHotelId();
            if (bHotelId != null) {
                Hotel hotel = hotelRepository.findById(bHotelId).orElse(null);
                if (hotel != null) {
                    int tb = hotel.getTotalBookings() != null ? hotel.getTotalBookings() : 1;
                    hotel.setTotalBookings(Math.max(0, tb - 1));
                    BigDecimal tr = hotel.getTotalRevenue() != null ? hotel.getTotalRevenue() : BigDecimal.ZERO;
                    hotel.setTotalRevenue(tr.subtract(booking.getTotalCost() != null ? booking.getTotalCost() : BigDecimal.ZERO));
                    hotelRepository.save(hotel);
                }
            }

            String bUserId = booking.getUserId();
            if (bUserId != null) {
                User user = userRepository.findById(bUserId).orElse(null);
                if (user != null) {
                    int ub = user.getTotalBookings() != null ? user.getTotalBookings() : 1;
                    user.setTotalBookings(Math.max(0, ub - 1));
                    BigDecimal us = user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO;
                    user.setTotalSpent(us.subtract(booking.getTotalCost() != null ? booking.getTotalCost() : BigDecimal.ZERO));
                    userRepository.save(user);
                }
            }
        }

        return new CancelBookingResponse(BookingDto.fromEntity(booking), refundAmount, refundSkipped);
    }

    @Transactional
    public BookingDto updateBookingStatus(String id, UpdateBookingStatusRequest request, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        assertOwnerOrAdmin(booking, userId, isAdmin);

        String newStatusStr = request.getStatus().trim().toLowerCase();
        if ("cancelled".equals(newStatusStr) || "refunded".equals(newStatusStr)) {
            throw new IllegalArgumentException("Use POST /api/bookings/:id/cancel for cancel/refund");
        }

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status");
        }

        booking.setStatus(newStatus);
        if (request.getCancellationReason() != null) {
            booking.setCancellationReason(request.getCancellationReason());
        }
        booking.setUpdatedAt(Instant.now());

        Booking saved = bookingRepository.save(booking);
        return BookingDto.fromEntity(saved);
    }

    @Transactional
    public BookingDto updateBookingPayment(String id, UpdateBookingPaymentRequest request, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        assertOwnerOrAdmin(booking, userId, isAdmin);

        PaymentStatus newPaymentStatus;
        try {
            newPaymentStatus = PaymentStatus.valueOf(request.getPaymentStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status");
        }

        booking.setPaymentStatus(newPaymentStatus);
        if (request.getPaymentMethod() != null) {
            booking.setPaymentMethod(request.getPaymentMethod());
        }
        booking.setUpdatedAt(Instant.now());

        Booking saved = bookingRepository.save(booking);
        return BookingDto.fromEntity(saved);
    }

    @Transactional
    public void deleteBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        boolean alreadyAdjusted = booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.REFUNDED
                || booking.getPaymentStatus() == PaymentStatus.REFUNDED;

        if (!alreadyAdjusted && booking.getPaymentStatus() == PaymentStatus.PAID) {
            String bHotelId = booking.getHotelId();
            if (bHotelId != null) {
                Hotel hotel = hotelRepository.findById(bHotelId).orElse(null);
                if (hotel != null) {
                    int tb = hotel.getTotalBookings() != null ? hotel.getTotalBookings() : 1;
                    hotel.setTotalBookings(Math.max(0, tb - 1));
                    BigDecimal tr = hotel.getTotalRevenue() != null ? hotel.getTotalRevenue() : BigDecimal.ZERO;
                    hotel.setTotalRevenue(tr.subtract(booking.getTotalCost() != null ? booking.getTotalCost() : BigDecimal.ZERO));
                    hotelRepository.save(hotel);
                }
            }

            String bUserId = booking.getUserId();
            if (bUserId != null) {
                User user = userRepository.findById(bUserId).orElse(null);
                if (user != null) {
                    int ub = user.getTotalBookings() != null ? user.getTotalBookings() : 1;
                    user.setTotalBookings(Math.max(0, ub - 1));
                    BigDecimal us = user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO;
                    user.setTotalSpent(us.subtract(booking.getTotalCost() != null ? booking.getTotalCost() : BigDecimal.ZERO));
                    userRepository.save(user);
                }
            }
        }

        bookingRepository.delete(booking);
    }

    @Transactional(readOnly = true)
    public PaymentIntentResponse createPaymentIntent(String hotelId, PaymentIntentRequest request, String userId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        int nights = request != null && request.getNumberOfNights() != null ? request.getNumberOfNights() : 1;
        BigDecimal totalCost = hotel.getPricePerNight().multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        PaymentIntentResult piResult = paymentGatewayService.createPaymentIntent(hotelId, userId, totalCost, "gbp");

        return new PaymentIntentResponse(piResult.getId(), piResult.getClientSecret(), totalCost);
    }

    private void assertCanViewOrCancel(Booking booking, String userId, boolean isAdmin) {
        if (isAdmin) return;

        String guestUserId = booking.getUserId();
        if (guestUserId != null && guestUserId.equals(userId)) return;

        String bHotelId = booking.getHotelId();
        if (bHotelId != null) {
            Hotel hotel = hotelRepository.findById(bHotelId).orElse(null);
            if (hotel != null) {
                String ownerUserId = hotel.getUser() != null ? hotel.getUser().getId() : hotel.getUserId();
                if (ownerUserId != null && ownerUserId.equals(userId)) return;
            }
        }

        throw new AccessDeniedException("Access denied");
    }

    private void assertOwnerOrAdmin(Booking booking, String userId, boolean isAdmin) {
        if (isAdmin) return;

        String bHotelId = booking.getHotelId();
        if (bHotelId != null) {
            Hotel hotel = hotelRepository.findById(bHotelId).orElse(null);
            if (hotel != null) {
                String ownerUserId = hotel.getUser() != null ? hotel.getUser().getId() : hotel.getUserId();
                if (ownerUserId != null && ownerUserId.equals(userId)) return;
            }
        }

        throw new AccessDeniedException("Access denied");
    }
}


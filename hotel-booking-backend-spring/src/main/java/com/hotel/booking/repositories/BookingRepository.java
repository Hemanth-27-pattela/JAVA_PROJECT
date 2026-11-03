package com.hotel.booking.repositories;

import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Booking> findByHotelIdOrderByCreatedAtDesc(String hotelId);

    List<Booking> findByHotelIdIn(java.util.Collection<String> hotelIds);

    List<Booking> findByHotelIdAndStatus(String hotelId, BookingStatus status);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);

    Optional<Booking> findByIdAndUserId(String id, String userId);

    Optional<Booking> findByStripePaymentIntentId(String stripePaymentIntentId);

    long countByHotelId(String hotelId);

    long countByUserId(String userId);

    List<Booking> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.user.id = :userId AND b.hotel.id = :hotelId AND b.status IN :activeStatuses AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    boolean existsOverlappingBookingForUser(
            @Param("userId") String userId,
            @Param("hotelId") String hotelId,
            @Param("checkIn") Instant checkIn,
            @Param("checkOut") Instant checkOut,
            @Param("activeStatuses") Collection<BookingStatus> activeStatuses
    );
}


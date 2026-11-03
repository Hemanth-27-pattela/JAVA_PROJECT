package com.hotel.booking.repositories;

import com.hotel.booking.entities.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByHotelIdOrderByCreatedAtDesc(String hotelId);

    List<Review> findByHotelIdOrderByCreatedAtDesc(String hotelId, Pageable pageable);

    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Review> findByBookingId(String bookingId);

    List<Review> findByHotelIdIn(Collection<String> hotelIds);

    boolean existsByBookingId(String bookingId);

    long countByHotelId(String hotelId);

    @Query("SELECT AVG(CAST(r.rating as double)) FROM Review r WHERE r.hotel.id = :hotelId")
    Double calculateAverageRatingByHotelId(@Param("hotelId") String hotelId);

    @Query("SELECT r.hotel.id, AVG(CAST(r.rating as double)), COUNT(r.id) FROM Review r WHERE r.hotel.id IN :hotelIds GROUP BY r.hotel.id")
    List<Object[]> getReviewStatsByHotelIds(@Param("hotelIds") Collection<String> hotelIds);
}


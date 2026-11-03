package com.hotel.booking.repositories;

import com.hotel.booking.entities.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, String>, JpaSpecificationExecutor<Hotel> {

    List<Hotel> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Hotel> findByCityIgnoreCase(String city, Pageable pageable);

    Page<Hotel> findByCityIgnoreCaseAndStarRating(String city, Integer starRating, Pageable pageable);

    Page<Hotel> findByStarRating(Integer starRating, Pageable pageable);

    long countByUserId(String userId);
}


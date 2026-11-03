package com.hotel.booking.repositories;

import com.hotel.booking.entities.HotelOwnerRequest;
import com.hotel.booking.entities.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelOwnerRequestRepository extends JpaRepository<HotelOwnerRequest, String> {

    List<HotelOwnerRequest> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<HotelOwnerRequest> findFirstByUserIdAndStatus(String userId, RequestStatus status);

    boolean existsByUserIdAndStatus(String userId, RequestStatus status);

    List<HotelOwnerRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    List<HotelOwnerRequest> findAllByOrderByCreatedAtDesc();

    long countByStatus(RequestStatus status);

    @Query("SELECT r FROM HotelOwnerRequest r JOIN FETCH r.user WHERE r.id = :id")
    Optional<HotelOwnerRequest> findByIdWithUser(@Param("id") String id);

    @Query("SELECT r FROM HotelOwnerRequest r JOIN FETCH r.user ORDER BY r.createdAt DESC")
    List<HotelOwnerRequest> findAllWithUser();

    @Query("SELECT r FROM HotelOwnerRequest r JOIN FETCH r.user WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<HotelOwnerRequest> findByStatusWithUser(@Param("status") RequestStatus status);
}


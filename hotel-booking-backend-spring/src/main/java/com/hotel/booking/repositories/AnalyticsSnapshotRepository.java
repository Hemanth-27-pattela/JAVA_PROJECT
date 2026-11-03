package com.hotel.booking.repositories;

import com.hotel.booking.entities.AnalyticsSnapshot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, String> {

    List<AnalyticsSnapshot> findAllByOrderByDateDesc(Pageable pageable);

    Optional<AnalyticsSnapshot> findFirstByOrderByDateDesc();
}


package com.hotel.booking.services;

import com.hotel.booking.dtos.ownerrequest.CreateOwnerRequestDto;
import com.hotel.booking.dtos.ownerrequest.OwnerRequestResponseDto;
import com.hotel.booking.entities.HotelOwnerRequest;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.RequestStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelOwnerRequestRepository;
import com.hotel.booking.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HotelOwnerRequestService {

    private static final Logger log = LoggerFactory.getLogger(HotelOwnerRequestService.class);

    private final HotelOwnerRequestRepository requestRepository;
    private final UserRepository userRepository;

    public HotelOwnerRequestService(HotelOwnerRequestRepository requestRepository,
                                    UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submit a new hotel owner access request for the authenticated user.
     * Prevents duplicate pending requests both at the service layer and via database-level unique constraint.
     */
    @Transactional
    public OwnerRequestResponseDto submitRequest(String userId, CreateOwnerRequestDto dto) {
        // Pessimistic write lock on the user row ensures concurrent submissions are serialized
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == UserRole.HOTEL_OWNER || user.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User already has Hotel Owner or Administrator permissions");
        }

        if (requestRepository.existsByUserIdAndStatus(userId, RequestStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have a pending hotel owner request under review");
        }

        HotelOwnerRequest request = new HotelOwnerRequest();
        request.setUser(user);
        request.setBusinessName(dto.getBusinessName().trim());
        request.setBusinessEmail(dto.getBusinessEmail().trim().toLowerCase());
        request.setBusinessPhone(dto.getBusinessPhone() != null ? dto.getBusinessPhone().trim() : null);
        request.setBusinessAddress(dto.getBusinessAddress() != null ? dto.getBusinessAddress().trim() : null);
        request.setJustification(dto.getJustification() != null ? dto.getJustification().trim() : null);
        request.setStatus(RequestStatus.PENDING);

        try {
            HotelOwnerRequest saved = requestRepository.saveAndFlush(request);
            log.info("Hotel owner request created: id={} for user={}", saved.getId(), userId);
            return OwnerRequestResponseDto.fromEntity(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Concurrent duplicate request detected for user: {}", userId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A pending request already exists or was submitted concurrently");
        }
    }

    /**
     * Get all requests submitted by the calling user.
     */
    @Transactional(readOnly = true)
    public List<OwnerRequestResponseDto> getMyRequests(String userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OwnerRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get the calling user's current pending request if one exists.
     */
    @Transactional(readOnly = true)
    public Optional<OwnerRequestResponseDto> getMyPendingRequest(String userId) {
        return requestRepository.findFirstByUserIdAndStatus(userId, RequestStatus.PENDING)
                .map(OwnerRequestResponseDto::fromEntity);
    }

    /**
     * Admin: List all requests, optionally filtered by status.
     */
    @Transactional(readOnly = true)
    public List<OwnerRequestResponseDto> getAllRequests(RequestStatus status) {
        List<HotelOwnerRequest> requests;
        if (status != null) {
            requests = requestRepository.findByStatusWithUser(status);
        } else {
            requests = requestRepository.findAllWithUser();
        }
        return requests.stream()
                .map(OwnerRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Admin: Approve request. Promotes user to HOTEL_OWNER in an atomic transaction.
     */
    @Transactional
    public OwnerRequestResponseDto approveRequest(String requestId, String adminId, String adminNotes) {
        HotelOwnerRequest request = requestRepository.findByIdWithUser(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot approve a request that is already " + request.getStatus().name());
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

        request.setStatus(RequestStatus.APPROVED);
        request.setAdminNotes(adminNotes != null ? adminNotes.trim() : null);
        request.setReviewedBy(admin);
        request.setReviewedAt(Instant.now());

        // Promote the target user to HOTEL_OWNER
        User targetUser = request.getUser();
        targetUser.setRole(UserRole.HOTEL_OWNER);
        targetUser.setUpdatedAt(Instant.now());
        userRepository.save(targetUser);

        HotelOwnerRequest saved = requestRepository.save(request);
        log.info("Hotel owner request approved: id={} by admin={}. User {} promoted to HOTEL_OWNER.",
                requestId, adminId, targetUser.getId());

        return OwnerRequestResponseDto.fromEntity(saved);
    }

    /**
     * Admin: Reject request with reason. User remains USER role.
     */
    @Transactional
    public OwnerRequestResponseDto rejectRequest(String requestId, String adminId, String rejectionReason, String adminNotes) {
        HotelOwnerRequest request = requestRepository.findByIdWithUser(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot reject a request that is already " + request.getStatus().name());
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason != null && !rejectionReason.isBlank()
                ? rejectionReason.trim()
                : "Application does not meet requirements at this time");
        request.setAdminNotes(adminNotes != null ? adminNotes.trim() : null);
        request.setReviewedBy(admin);
        request.setReviewedAt(Instant.now());

        HotelOwnerRequest saved = requestRepository.save(request);
        log.info("Hotel owner request rejected: id={} by admin={}. Reason: {}",
                requestId, adminId, request.getRejectionReason());

        return OwnerRequestResponseDto.fromEntity(saved);
    }
}

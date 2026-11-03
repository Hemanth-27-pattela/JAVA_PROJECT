package com.hotel.booking.services;

import com.hotel.booking.dtos.HotelDto;
import com.hotel.booking.dtos.HotelSearchParams;
import com.hotel.booking.dtos.HotelSearchResponse;
import com.hotel.booking.dtos.PaginationDto;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import com.hotel.booking.specifications.HotelSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private static final int PAGE_SIZE = 5;

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ImageUploadService imageUploadService;

    public HotelService(
            HotelRepository hotelRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            ImageUploadService imageUploadService
    ) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.imageUploadService = imageUploadService;
    }

    @Transactional(readOnly = true)
    public HotelSearchResponse searchHotels(HotelSearchParams params) {
        Sort sort;
        String sortOption = params.getSortOption() != null ? params.getSortOption() : "";
        switch (sortOption) {
            case "starRating":
                sort = Sort.by(Sort.Direction.DESC, "starRating");
                break;
            case "pricePerNightAsc":
                sort = Sort.by(Sort.Direction.ASC, "pricePerNight");
                break;
            case "pricePerNightDesc":
                sort = Sort.by(Sort.Direction.DESC, "pricePerNight");
                break;
            default:
                sort = Sort.by(Sort.Direction.DESC, "lastUpdated");
                break;
        }

        int pageIndex = Math.max(0, params.getPage() - 1);
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE, sort);

        Page<Hotel> page = hotelRepository.findAll(HotelSpecifications.buildSearchSpecification(params), pageable);

        List<HotelDto> dtoList = page.getContent().stream()
                .map(HotelDto::fromEntity)
                .collect(Collectors.toList());

        PaginationDto pagination = new PaginationDto(
                page.getTotalElements(),
                params.getPage(),
                page.getTotalPages()
        );

        return new HotelSearchResponse(dtoList, pagination);
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated")).stream()
                .map(HotelDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelDto getHotelById(String id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
        return HotelDto.fromEntity(hotel);
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getMyHotels(String userId) {
        List<Hotel> hotels = hotelRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (hotels.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> hotelIds = hotels.stream().map(Hotel::getId).collect(Collectors.toList());

        // Fetch bookings for these hotels
        List<Booking> allBookings = bookingRepository.findByHotelIdIn(hotelIds);
        Map<String, List<Booking>> bookingsByHotel = new HashMap<>();
        for (Booking b : allBookings) {
            String hId = b.getHotel() != null ? b.getHotel().getId() : b.getHotelId();
            if (hId != null) {
                bookingsByHotel.computeIfAbsent(hId, k -> new ArrayList<>()).add(b);
            }
        }

        // Fetch reviews stats for these hotels
        List<Object[]> reviewStats = reviewRepository.getReviewStatsByHotelIds(hotelIds);
        Map<String, double[]> reviewStatsByHotel = new HashMap<>();
        for (Object[] row : reviewStats) {
            String hId = (String) row[0];
            Double avg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            reviewStatsByHotel.put(hId, new double[]{avg, count.doubleValue()});
        }

        Instant now = Instant.now();
        List<HotelDto> enrichedHotels = new ArrayList<>();

        for (Hotel hotel : hotels) {
            HotelDto dto = HotelDto.fromEntity(hotel);
            List<Booking> bookings = bookingsByHotel.getOrDefault(hotel.getId(), Collections.emptyList());

            int upcoming = 0;
            int completed = 0;
            int cancelled = 0;

            for (Booking b : bookings) {
                if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REFUNDED) {
                    cancelled++;
                } else if (b.getStatus() == BookingStatus.COMPLETED || (b.getCheckOut() != null && b.getCheckOut().isBefore(now))) {
                    completed++;
                } else {
                    upcoming++;
                }
            }

            dto.setUpcomingBookings(upcoming);
            dto.setCompletedBookings(completed);
            dto.setCancelledBookings(cancelled);

            if (reviewStatsByHotel.containsKey(hotel.getId())) {
                double[] stats = reviewStatsByHotel.get(hotel.getId());
                double roundedAvg = BigDecimal.valueOf(stats[0])
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
                dto.setAverageRating(roundedAvg);
                dto.setReviewCount((int) stats[1]);
            } else if (dto.getAverageRating() == null || dto.getAverageRating() == 0.0) {
                dto.setAverageRating(dto.getStarRating() != null ? dto.getStarRating().doubleValue() : 0.0);
            }

            enrichedHotels.add(dto);
        }

        return enrichedHotels;
    }

    @Transactional(readOnly = true)
    public HotelDto getMyHotelById(String hotelId, String userId, boolean isAdmin) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        if (!isAdmin && (hotel.getUser() == null || !hotel.getUser().getId().equals(userId))) {
            throw new AccessDeniedException("Access denied");
        }

        return HotelDto.fromEntity(hotel);
    }

    @Transactional
    public HotelDto createHotel(Hotel hotel, List<MultipartFile> imageFiles, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        hotel.setUser(user);
        hotel.setUserId(userId);
        hotel.setLastUpdated(Instant.now());

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> uploadedUrls = imageUploadService.uploadImages(imageFiles);
            hotel.setImageUrls(uploadedUrls);
        }

        Hotel saved = hotelRepository.save(hotel);
        return HotelDto.fromEntity(saved);
    }

    @Transactional
    public HotelDto updateHotel(
            String hotelId,
            Hotel updateData,
            List<String> keptImageUrls,
            List<MultipartFile> newImageFiles,
            String userId,
            boolean isAdmin
    ) {
        Hotel existingHotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        if (!isAdmin && (existingHotel.getUser() == null || !existingHotel.getUser().getId().equals(userId))) {
            throw new AccessDeniedException("Access denied");
        }

        existingHotel.setName(updateData.getName());
        existingHotel.setCity(updateData.getCity());
        existingHotel.setCountry(updateData.getCountry());
        existingHotel.setDescription(updateData.getDescription());
        if (updateData.getType() != null && !updateData.getType().isEmpty()) {
            existingHotel.setType(new ArrayList<>(updateData.getType()));
        }
        existingHotel.setPricePerNight(updateData.getPricePerNight());
        existingHotel.setStarRating(updateData.getStarRating());
        existingHotel.setAdultCount(updateData.getAdultCount());
        existingHotel.setChildCount(updateData.getChildCount());
        if (updateData.getFacilities() != null && !updateData.getFacilities().isEmpty()) {
            existingHotel.setFacilities(new ArrayList<>(updateData.getFacilities()));
        }
        if (updateData.getContact() != null) {
            existingHotel.setContact(updateData.getContact());
        }
        if (updateData.getPolicies() != null) {
            existingHotel.setPolicies(updateData.getPolicies());
        }
        existingHotel.setLastUpdated(Instant.now());

        // Handle images
        List<String> finalUrls = new ArrayList<>();
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            List<String> newUploaded = imageUploadService.uploadImages(newImageFiles);
            finalUrls.addAll(newUploaded);
        }
        if (keptImageUrls != null) {
            finalUrls.addAll(keptImageUrls);
        } else if (newImageFiles == null || newImageFiles.isEmpty()) {
            // Keep existing images if none specified
            finalUrls.addAll(existingHotel.getImageUrls());
        }
        existingHotel.setImageUrls(finalUrls);

        Hotel saved = hotelRepository.save(existingHotel);
        return HotelDto.fromEntity(saved);
    }

    @Transactional
    public HotelDto updateHotelActiveStatus(String hotelId, boolean isActive, String userId, boolean isAdmin) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        if (!isAdmin && (hotel.getUser() == null || !hotel.getUser().getId().equals(userId))) {
            throw new AccessDeniedException("Access denied");
        }

        hotel.setIsActive(isActive);
        hotel.setLastUpdated(Instant.now());

        Hotel saved = hotelRepository.save(hotel);
        return HotelDto.fromEntity(saved);
    }
}


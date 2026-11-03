package com.hotel.booking.controllers;

import com.hotel.booking.dtos.HotelActiveStatusRequest;
import com.hotel.booking.dtos.HotelDto;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.security.UserPrincipal;
import com.hotel.booking.services.HotelService;
import com.hotel.booking.util.HotelMultipartParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/my-hotels")
@PreAuthorize("hasRole('HOTEL_OWNER') or hasRole('ADMIN')")
public class MyHotelController {

    private final HotelService hotelService;

    public MyHotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HotelDto> addMyHotel(
            HttpServletRequest request,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Hotel hotel = new Hotel();
        HotelMultipartParser.populateHotelFromRequest(hotel, request);

        validateHotelData(hotel);

        HotelDto created = hotelService.createHotel(hotel, imageFiles, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getMyHotels(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(hotelService.getMyHotels(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getMyHotelById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(hotelService.getMyHotelById(id, principal.getId(), isAdmin));
    }

    @PutMapping(value = "/{hotelId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HotelDto> updateMyHotel(
            @PathVariable String hotelId,
            HttpServletRequest request,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Hotel updateData = new Hotel();
        HotelMultipartParser.populateHotelFromRequest(updateData, request);

        validateHotelData(updateData);

        List<String> keptImageUrls = HotelMultipartParser.extractIndexedOrDirectList(request, "imageUrls");

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        HotelDto updated = hotelService.updateHotel(
                hotelId,
                updateData,
                keptImageUrls,
                imageFiles,
                principal.getId(),
                isAdmin
        );
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<HotelDto> updateMyHotelActive(
            @PathVariable String id,
            @Valid @RequestBody HotelActiveStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        HotelDto updated = hotelService.updateHotelActiveStatus(id, request.getIsActive(), principal.getId(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    private void validateHotelData(Hotel hotel) {
        if (hotel.getName() == null || hotel.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (hotel.getCity() == null || hotel.getCity().isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (hotel.getCountry() == null || hotel.getCountry().isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
        if (hotel.getDescription() == null || hotel.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (hotel.getType() == null || hotel.getType().isEmpty()) {
            throw new IllegalArgumentException("Select at least one hotel type");
        }
        if (hotel.getPricePerNight() == null || hotel.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per night is required and must be a number");
        }
        if (hotel.getFacilities() == null || hotel.getFacilities().isEmpty()) {
            throw new IllegalArgumentException("Facilities are required");
        }
    }
}


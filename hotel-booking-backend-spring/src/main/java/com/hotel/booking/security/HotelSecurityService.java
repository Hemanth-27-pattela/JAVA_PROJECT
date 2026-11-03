package com.hotel.booking.security;

import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("hotelSecurity")
public class HotelSecurityService {

    private final HotelRepository hotelRepository;

    public HotelSecurityService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public boolean isOwner(String hotelId, Authentication authentication) {
        if (hotelId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        // Admins can manage any hotel
        if (userPrincipal.getRole() == UserRole.ADMIN) {
            return true;
        }

        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        return hotel.isPresent() && userPrincipal.getId().equals(hotel.get().getUserId());
    }
}


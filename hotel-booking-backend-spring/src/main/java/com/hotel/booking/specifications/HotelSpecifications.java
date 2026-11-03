package com.hotel.booking.specifications;

import com.hotel.booking.dtos.HotelSearchParams;
import com.hotel.booking.entities.Hotel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecifications {

    public static Specification<Hotel> buildSearchSpecification(HotelSearchParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Destination (city or country case-insensitive match)
            if (params.getDestination() != null && !params.getDestination().trim().isEmpty()) {
                String pattern = "%" + params.getDestination().trim().toLowerCase() + "%";
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), pattern);
                Predicate countryMatch = cb.like(cb.lower(root.get("country")), pattern);
                predicates.add(cb.or(cityMatch, countryMatch));
            }

            // Adult count (adultCount >= params.adultCount)
            if (params.getAdultCount() != null && params.getAdultCount() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("adultCount"), params.getAdultCount()));
            }

            // Child count (childCount >= params.childCount)
            if (params.getChildCount() != null && params.getChildCount() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("childCount"), params.getChildCount()));
            }

            // Max price (pricePerNight <= params.maxPrice)
            if (params.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), params.getMaxPrice()));
            }

            // Star ratings
            if (params.getStars() != null && !params.getStars().isEmpty()) {
                predicates.add(root.get("starRating").in(params.getStars()));
            }

            // Types ($in match: any of the requested types)
            if (params.getTypes() != null && !params.getTypes().isEmpty()) {
                Join<Hotel, String> typeJoin = root.join("type", JoinType.INNER);
                predicates.add(typeJoin.in(params.getTypes()));
                query.distinct(true);
            }

            // Facilities ($all match: hotel must contain all requested facilities)
            if (params.getFacilities() != null && !params.getFacilities().isEmpty()) {
                for (String facility : params.getFacilities()) {
                    predicates.add(cb.isMember(facility, root.get("facilities")));
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


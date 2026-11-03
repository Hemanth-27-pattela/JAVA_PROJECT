package com.hotel.booking.util;

import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.embeddables.HotelContact;
import com.hotel.booking.entities.embeddables.HotelPolicies;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HotelMultipartParser {

    private static final Pattern INDEXED_PATTERN = Pattern.compile("^(.+)\\[(\\d+)\\]$");

    public static List<String> extractIndexedOrDirectList(HttpServletRequest request, String fieldName) {
        Map<String, String[]> paramMap = request.getParameterMap();
        TreeMap<Integer, String> indexedMap = new TreeMap<>();
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Matcher matcher = INDEXED_PATTERN.matcher(key);
            if (matcher.matches() && matcher.group(1).equals(fieldName)) {
                int index = Integer.parseInt(matcher.group(2));
                if (entry.getValue() != null && entry.getValue().length > 0) {
                    indexedMap.put(index, entry.getValue()[0]);
                }
            }
        }

        if (!indexedMap.isEmpty()) {
            result.addAll(indexedMap.values());
            return result;
        }

        // Fallback to direct key
        String[] values = request.getParameterValues(fieldName);
        if (values != null) {
            for (String val : values) {
                if (val != null && !val.isBlank()) {
                    if (val.contains(",") && !fieldName.equals("description")) {
                        for (String part : val.split(",")) {
                            if (!part.trim().isEmpty()) {
                                result.add(part.trim());
                            }
                        }
                    } else {
                        result.add(val.trim());
                    }
                }
            }
        }
        return result;
    }

    public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String val = request.getParameter(name);
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }

    public static Integer getIntegerParameter(HttpServletRequest request, String name, Integer defaultValue) {
        String val = request.getParameter(name);
        if (val == null || val.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static BigDecimal getBigDecimalParameter(HttpServletRequest request, String name, BigDecimal defaultValue) {
        String val = request.getParameter(name);
        if (val == null || val.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void populateHotelFromRequest(Hotel hotel, HttpServletRequest request) {
        String name = getParameter(request, "name", null);
        if (name != null) hotel.setName(name);

        String city = getParameter(request, "city", null);
        if (city != null) hotel.setCity(city);

        String country = getParameter(request, "country", null);
        if (country != null) hotel.setCountry(country);

        String description = getParameter(request, "description", null);
        if (description != null) hotel.setDescription(description);

        Integer adultCount = getIntegerParameter(request, "adultCount", null);
        if (adultCount != null) hotel.setAdultCount(adultCount);

        Integer childCount = getIntegerParameter(request, "childCount", null);
        if (childCount != null) hotel.setChildCount(childCount);

        Integer starRating = getIntegerParameter(request, "starRating", null);
        if (starRating != null) hotel.setStarRating(starRating);

        BigDecimal pricePerNight = getBigDecimalParameter(request, "pricePerNight", null);
        if (pricePerNight != null) hotel.setPricePerNight(pricePerNight);

        List<String> types = extractIndexedOrDirectList(request, "type");
        if (!types.isEmpty()) {
            hotel.setType(types);
        }

        List<String> facilities = extractIndexedOrDirectList(request, "facilities");
        if (!facilities.isEmpty()) {
            hotel.setFacilities(facilities);
        }

        // Contact information
        HotelContact contact = hotel.getContact() != null ? hotel.getContact() : new HotelContact();
        String phone = getParameter(request, "contact.phone", null);
        if (phone != null) contact.setPhone(phone);
        String email = getParameter(request, "contact.email", null);
        if (email != null) contact.setEmail(email);
        String website = getParameter(request, "contact.website", null);
        if (website != null) contact.setWebsite(website);
        hotel.setContact(contact);

        // Policies
        HotelPolicies policies = hotel.getPolicies() != null ? hotel.getPolicies() : new HotelPolicies();
        String checkInTime = getParameter(request, "policies.checkInTime", null);
        if (checkInTime != null) policies.setCheckInTime(checkInTime);
        String checkOutTime = getParameter(request, "policies.checkOutTime", null);
        if (checkOutTime != null) policies.setCheckOutTime(checkOutTime);
        String cancellationPolicy = getParameter(request, "policies.cancellationPolicy", null);
        if (cancellationPolicy != null) policies.setCancellationPolicy(cancellationPolicy);
        String petPolicy = getParameter(request, "policies.petPolicy", null);
        if (petPolicy != null) policies.setPetPolicy(petPolicy);
        String smokingPolicy = getParameter(request, "policies.smokingPolicy", null);
        if (smokingPolicy != null) policies.setSmokingPolicy(smokingPolicy);
        hotel.setPolicies(policies);
    }
}


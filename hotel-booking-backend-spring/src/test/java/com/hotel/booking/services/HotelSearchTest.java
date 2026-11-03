package com.hotel.booking.services;

import com.hotel.booking.dtos.HotelDto;
import com.hotel.booking.dtos.HotelSearchParams;
import com.hotel.booking.dtos.HotelSearchResponse;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class HotelSearchTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ImageUploadService imageUploadService;

    private User testOwner;
    private Hotel grandPalace;
    private Hotel seasideResort;
    private Hotel alpineChalet;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        testOwner = new User();
        testOwner.setEmail("owner_search@test.com");
        testOwner.setPassword("hashedpassword123");
        testOwner.setFirstName("Hotel");
        testOwner.setLastName("Owner");
        testOwner.setRole(UserRole.HOTEL_OWNER);
        testOwner = userRepository.save(testOwner);

        Instant now = Instant.now();

        // Hotel 1: Paris, France - Luxury - 5 stars, $350
        grandPalace = new Hotel();
        grandPalace.setUser(testOwner);
        grandPalace.setName("The Grand Palace");
        grandPalace.setCity("Paris");
        grandPalace.setCountry("France");
        grandPalace.setDescription("Luxury hotel in central Paris.");
        grandPalace.setType(Arrays.asList("Luxury", "Boutique"));
        grandPalace.setAdultCount(2);
        grandPalace.setChildCount(1);
        grandPalace.setFacilities(Arrays.asList("Free WiFi", "Spa", "Fitness Center"));
        grandPalace.setPricePerNight(new BigDecimal("350.00"));
        grandPalace.setStarRating(5);
        grandPalace.setImageUrls(List.of("https://res.cloudinary.com/demo/image/upload/sample1.jpg"));
        grandPalace.setLastUpdated(now.minus(2, ChronoUnit.HOURS));
        grandPalace = hotelRepository.save(grandPalace);

        // Hotel 2: Nice, France - Beach Resort - 4 stars, $200
        seasideResort = new Hotel();
        seasideResort.setUser(testOwner);
        seasideResort.setName("Nice Seaside Resort");
        seasideResort.setCity("Nice");
        seasideResort.setCountry("France");
        seasideResort.setDescription("Mediterranean beachfront resort.");
        seasideResort.setType(Arrays.asList("Beach Resort", "Family"));
        seasideResort.setAdultCount(4);
        seasideResort.setChildCount(2);
        seasideResort.setFacilities(Arrays.asList("Free WiFi", "Outdoor Pool", "Parking"));
        seasideResort.setPricePerNight(new BigDecimal("200.00"));
        seasideResort.setStarRating(4);
        seasideResort.setImageUrls(List.of("https://res.cloudinary.com/demo/image/upload/sample2.jpg"));
        seasideResort.setLastUpdated(now.minus(1, ChronoUnit.HOURS));
        seasideResort = hotelRepository.save(seasideResort);

        // Hotel 3: Zermatt, Switzerland - Ski Resort - 3 stars, $120
        alpineChalet = new Hotel();
        alpineChalet.setUser(testOwner);
        alpineChalet.setName("Alpine Ski Chalet");
        alpineChalet.setCity("Zermatt");
        alpineChalet.setCountry("Switzerland");
        alpineChalet.setDescription("Cozy ski resort in the Alps.");
        alpineChalet.setType(Arrays.asList("Ski Resort", "Budget"));
        alpineChalet.setAdultCount(2);
        alpineChalet.setChildCount(0);
        alpineChalet.setFacilities(Arrays.asList("Free WiFi", "Parking"));
        alpineChalet.setPricePerNight(new BigDecimal("120.00"));
        alpineChalet.setStarRating(3);
        alpineChalet.setImageUrls(List.of("https://res.cloudinary.com/demo/image/upload/sample3.jpg"));
        alpineChalet.setLastUpdated(now);
        alpineChalet = hotelRepository.save(alpineChalet);
    }

    @Test
    @DisplayName("Search by destination matching city case-insensitively")
    void testSearchByDestinationCity() {
        HotelSearchParams params = new HotelSearchParams();
        params.setDestination("paris");

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("The Grand Palace", response.getData().get(0).getName());
        assertEquals(1, response.getPagination().getTotal());
    }

    @Test
    @DisplayName("Search by destination matching country")
    void testSearchByDestinationCountry() {
        HotelSearchParams params = new HotelSearchParams();
        params.setDestination("France");

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertNotNull(response);
        assertEquals(2, response.getData().size());
        assertEquals(2, response.getPagination().getTotal());
    }

    @Test
    @DisplayName("Search by guest capacity (adults and children)")
    void testSearchByGuestCounts() {
        HotelSearchParams params = new HotelSearchParams();
        params.setAdultCount(3);
        params.setChildCount(2);

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertEquals(1, response.getData().size());
        assertEquals("Nice Seaside Resort", response.getData().get(0).getName());
    }

    @Test
    @DisplayName("Search with maxPrice filter")
    void testSearchByMaxPrice() {
        HotelSearchParams params = new HotelSearchParams();
        params.setMaxPrice(new BigDecimal("210.00"));

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertEquals(2, response.getData().size());
        for (HotelDto h : response.getData()) {
            assertTrue(h.getPricePerNight().compareTo(new BigDecimal("210.00")) <= 0);
        }
    }

    @Test
    @DisplayName("Search with star rating filter")
    void testSearchByStarRating() {
        HotelSearchParams params = new HotelSearchParams();
        params.setStars(Arrays.asList(5, 4));

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertEquals(2, response.getData().size());
        for (HotelDto h : response.getData()) {
            assertTrue(h.getStarRating() == 5 || h.getStarRating() == 4);
        }
    }

    @Test
    @DisplayName("Search with facilities ($all filter)")
    void testSearchByFacilities() {
        HotelSearchParams params = new HotelSearchParams();
        params.setFacilities(Arrays.asList("Free WiFi", "Spa"));

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertEquals(1, response.getData().size());
        assertEquals("The Grand Palace", response.getData().get(0).getName());
    }

    @Test
    @DisplayName("Search with sorting: pricePerNightAsc")
    void testSearchSortingPriceAsc() {
        HotelSearchParams params = new HotelSearchParams();
        params.setSortOption("pricePerNightAsc");

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertEquals(3, response.getData().size());
        assertEquals("Alpine Ski Chalet", response.getData().get(0).getName()); // $120
        assertEquals("Nice Seaside Resort", response.getData().get(1).getName()); // $200
        assertEquals("The Grand Palace", response.getData().get(2).getName()); // $350
    }

    @Test
    @DisplayName("Search with sorting: starRating DESC")
    void testSearchSortingStarRating() {
        HotelSearchParams params = new HotelSearchParams();
        params.setSortOption("starRating");

        HotelSearchResponse response = hotelService.searchHotels(params);
        assertEquals(3, response.getData().size());
        assertEquals(5, response.getData().get(0).getStarRating());
        assertEquals(4, response.getData().get(1).getStarRating());
        assertEquals(3, response.getData().get(2).getStarRating());
    }
}


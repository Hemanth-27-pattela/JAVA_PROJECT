package com.hotel.booking.repositories;

import com.hotel.booking.entities.AnalyticsSnapshot;
import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Review;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.embeddables.Address;
import com.hotel.booking.entities.embeddables.AnalyticsBreakdown;
import com.hotel.booking.entities.embeddables.AnalyticsMetrics;
import com.hotel.booking.entities.embeddables.HotelAmenities;
import com.hotel.booking.entities.embeddables.HotelContact;
import com.hotel.booking.entities.embeddables.HotelLocation;
import com.hotel.booking.entities.embeddables.HotelPolicies;
import com.hotel.booking.entities.embeddables.ReviewCategories;
import com.hotel.booking.entities.embeddables.UserPreferences;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EntityPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AnalyticsSnapshotRepository analyticsSnapshotRepository;

    @Test
    @DisplayName("User persistence generates UUID, timestamps, and queries by email and role")
    void testUserPersistence() {
        User user = new User("sarah@example.com", "hashedSecretPass123", "Sarah", "Connor");
        user.setRole(UserRole.HOTEL_OWNER);
        user.setAddress(new Address("456 Ocean Ave", "Miami", "FL", "USA", "33139"));

        UserPreferences prefs = new UserPreferences();
        prefs.setPreferredDestinations(List.of("Miami", "Paris"));
        prefs.setPreferredHotelTypes(List.of("Resort", "Luxury"));
        user.setPreferences(prefs);

        User saved = userRepository.saveAndFlush(user);

        assertNotNull(saved.getId(), "UUID id must be auto-generated");
        assertEquals(36, saved.getId().length(), "UUID must be 36 characters");
        assertNotNull(saved.getCreatedAt(), "createdAt must be auto-set");
        assertNotNull(saved.getUpdatedAt(), "updatedAt must be auto-set");
        assertEquals(UserRole.HOTEL_OWNER, saved.getRole());

        // Test queries
        Optional<User> found = userRepository.findByEmail("sarah@example.com");
        assertTrue(found.isPresent());
        assertEquals("Sarah", found.get().getFirstName());

        assertTrue(userRepository.existsByEmail("sarah@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));

        List<User> owners = userRepository.findByRole(UserRole.HOTEL_OWNER);
        assertFalse(owners.isEmpty());
        assertEquals(1, userRepository.countByRole(UserRole.HOTEL_OWNER));
    }

    @Test
    @DisplayName("Hotel relationship: One user owns multiple hotels, collections and embeddables persist")
    void testHotelRelationshipAndPersistence() {
        User owner = new User("owner1@test.com", "pass", "Elena", "Rostova");
        owner.setRole(UserRole.HOTEL_OWNER);
        owner = userRepository.saveAndFlush(owner);

        // Hotel 1
        Hotel hotel1 = new Hotel();
        hotel1.setUser(owner);
        hotel1.setName("Seaside Resort & Spa");
        hotel1.setCity("Nice");
        hotel1.setCountry("France");
        hotel1.setDescription("Beautiful seaside resort on Promenade des Anglais.");
        hotel1.setType(List.of("Resort", "Luxury"));
        hotel1.setAdultCount(2);
        hotel1.setChildCount(2);
        hotel1.setFacilities(List.of("Free WiFi", "Swimming Pool", "Spa & Wellness Centre", "Beachfront"));
        hotel1.setPricePerNight(new BigDecimal("185.50"));
        hotel1.setStarRating(4);
        hotel1.setImageUrls(List.of("https://resort.com/img1.jpg", "https://resort.com/img2.jpg"));
        hotel1.setLastUpdated(Instant.now());

        HotelAmenities amenities = new HotelAmenities();
        amenities.setWifi(true);
        amenities.setPool(true);
        amenities.setSpa(true);
        hotel1.setAmenities(amenities);

        HotelContact contact = new HotelContact("+33493000000", "info@seaside-nice.fr", "https://seaside-nice.fr");
        hotel1.setContact(contact);

        HotelPolicies policies = new HotelPolicies("15:00", "11:00", "Free cancellation up to 48h before check-in", "Pets allowed", "Non-smoking throughout");
        hotel1.setPolicies(policies);

        hotel1.setLocation(new HotelLocation(43.696, 7.271, new Address("10 Promenade des Anglais", "Nice", "PACA", "France", "06000")));

        // Hotel 2 for the same owner
        Hotel hotel2 = new Hotel();
        hotel2.setUser(owner);
        hotel2.setName("Alpine Lodge");
        hotel2.setCity("Chamonix");
        hotel2.setCountry("France");
        hotel2.setDescription("Cozy alpine lodge near Mont Blanc.");
        hotel2.setType(List.of("Lodge", "Ski Resort"));
        hotel2.setAdultCount(4);
        hotel2.setChildCount(1);
        hotel2.setFacilities(List.of("Free WiFi", "Ski Storage", "Sauna"));
        hotel2.setPricePerNight(new BigDecimal("220.00"));
        hotel2.setStarRating(4);
        hotel2.setImageUrls(List.of("https://lodge.com/ski.jpg"));
        hotel2.setLastUpdated(Instant.now());

        hotelRepository.saveAndFlush(hotel1);
        hotelRepository.saveAndFlush(hotel2);

        // Verify owner has 2 hotels
        List<Hotel> ownerHotels = hotelRepository.findByUserIdOrderByCreatedAtDesc(owner.getId());
        assertEquals(2, ownerHotels.size(), "Owner must have 2 hotels");
        assertEquals(2, hotelRepository.countByUserId(owner.getId()));

        // Query by city
        Page<Hotel> niceHotels = hotelRepository.findByCityIgnoreCase("nice", PageRequest.of(0, 10));
        assertEquals(1, niceHotels.getTotalElements());
        assertEquals("Seaside Resort & Spa", niceHotels.getContent().get(0).getName());
        assertTrue(niceHotels.getContent().get(0).getAmenities().getWifi());
        assertEquals(4, niceHotels.getContent().get(0).getFacilities().size());
    }

    @Test
    @DisplayName("Booking relationships: Users and Hotels have multiple bookings with monetary precision")
    void testBookingPersistenceAndRelationships() {
        User guest = userRepository.saveAndFlush(new User("guest@example.com", "pass", "Mark", "Twain"));
        User owner = userRepository.saveAndFlush(new User("owner2@example.com", "pass", "Jane", "Hotelier"));

        Hotel hotel = new Hotel();
        hotel.setUser(owner);
        hotel.setName("Downtown Boutique");
        hotel.setCity("London");
        hotel.setCountry("UK");
        hotel.setDescription("Stylish hotel in Soho.");
        hotel.setType(List.of("Boutique"));
        hotel.setFacilities(List.of("Free WiFi"));
        hotel.setPricePerNight(new BigDecimal("150.00"));
        hotel.setStarRating(4);
        hotel.setImageUrls(List.of("https://example.com/soho.jpg"));
        hotel.setLastUpdated(Instant.now());
        hotel = hotelRepository.saveAndFlush(hotel);

        // Create booking
        Booking booking = new Booking();
        booking.setUser(guest);
        booking.setHotel(hotel);
        booking.setFirstName("Mark");
        booking.setLastName("Twain");
        booking.setEmail("guest@example.com");
        booking.setPhone("+447123456789");
        booking.setAdultCount(2);
        booking.setChildCount(0);
        booking.setCheckIn(Instant.now().plusSeconds(86400));
        booking.setCheckOut(Instant.now().plusSeconds(86400 * 4));
        booking.setTotalCost(new BigDecimal("450.00"));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaymentMethod("card");
        booking.setStripePaymentIntentId("pi_3MtwBwLkdIwHu7ix28a3tqPa");
        booking.setSpecialRequests("Quiet room on high floor");

        Booking saved = bookingRepository.saveAndFlush(booking);

        assertNotNull(saved.getId());
        assertEquals("Mark", saved.getFirstName());
        assertEquals(new BigDecimal("450.00"), saved.getTotalCost());
        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());

        // Query by user
        List<Booking> userBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(guest.getId());
        assertEquals(1, userBookings.size());
        assertEquals(hotel.getId(), userBookings.get(0).getHotelId());

        // Query by hotel
        List<Booking> hotelBookings = bookingRepository.findByHotelIdOrderByCreatedAtDesc(hotel.getId());
        assertEquals(1, hotelBookings.size());

        // Query by status
        List<Booking> confirmedBookings = bookingRepository.findByHotelIdAndStatus(hotel.getId(), BookingStatus.CONFIRMED);
        assertEquals(1, confirmedBookings.size());

        // Query by stripe intent
        Optional<Booking> byStripe = bookingRepository.findByStripePaymentIntentId("pi_3MtwBwLkdIwHu7ix28a3tqPa");
        assertTrue(byStripe.isPresent());
    }

    @Test
    @DisplayName("Review relationship: User and Hotel linked to completed Booking with category ratings")
    void testReviewPersistenceAndAverageCalculation() {
        User guest = userRepository.saveAndFlush(new User("reviewer@example.com", "pass", "Lucy", "Pevensie"));
        User owner = userRepository.saveAndFlush(new User("owner3@example.com", "pass", "Peter", "Pan"));

        Hotel hotel = new Hotel();
        hotel.setUser(owner);
        hotel.setName("Cotswold Manor");
        hotel.setCity("Oxford");
        hotel.setCountry("UK");
        hotel.setDescription("Historic countryside manor.");
        hotel.setType(List.of("Historic", "Manor"));
        hotel.setFacilities(List.of("Free WiFi", "Garden"));
        hotel.setPricePerNight(new BigDecimal("210.00"));
        hotel.setStarRating(5);
        hotel.setImageUrls(List.of("https://example.com/manor.jpg"));
        hotel.setLastUpdated(Instant.now());
        hotel = hotelRepository.saveAndFlush(hotel);

        Booking booking = new Booking();
        booking.setUser(guest);
        booking.setHotel(hotel);
        booking.setFirstName("Lucy");
        booking.setLastName("Pevensie");
        booking.setEmail("reviewer@example.com");
        booking.setAdultCount(1);
        booking.setChildCount(0);
        booking.setCheckIn(Instant.now().minusSeconds(86400 * 5));
        booking.setCheckOut(Instant.now().minusSeconds(86400 * 2));
        booking.setTotalCost(new BigDecimal("630.00"));
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking = bookingRepository.saveAndFlush(booking);

        Review review = new Review();
        review.setUser(guest);
        review.setHotel(hotel);
        review.setBooking(booking);
        review.setRating(5);
        review.setComment("Exceptional hospitality, stunning gardens, and outstanding breakfast!");
        review.setCategories(new ReviewCategories(5, 5, 4, 5, 4));
        review.setIsVerified(true);
        review.setHelpfulCount(3);

        Review saved = reviewRepository.saveAndFlush(review);

        assertNotNull(saved.getId());
        assertEquals(5, saved.getRating());
        assertTrue(saved.getIsVerified());
        assertEquals(booking.getId(), saved.getBookingId());

        // Check relationship queries
        assertTrue(reviewRepository.existsByBookingId(booking.getId()));
        assertEquals(1, reviewRepository.countByHotelId(hotel.getId()));

        Double avgRating = reviewRepository.calculateAverageRatingByHotelId(hotel.getId());
        assertNotNull(avgRating);
        assertEquals(5.0, avgRating, 0.01);
    }

    @Test
    @DisplayName("AnalyticsSnapshot stores metrics and breakdown JSON with proper serialization")
    void testAnalyticsSnapshotPersistence() {
        AnalyticsMetrics metrics = new AnalyticsMetrics(
                125L,
                new BigDecimal("48750.00"),
                450L,
                32L,
                new BigDecimal("390.00"),
                14.5,
                3.2,
                4.8
        );

        AnalyticsBreakdown breakdown = new AnalyticsBreakdown();
        breakdown.getByStatus().put("confirmed", 85L);
        breakdown.getByStatus().put("completed", 35L);
        breakdown.getByStatus().put("cancelled", 5L);

        breakdown.setByDestination(List.of(
                new AnalyticsBreakdown.DestinationBreakdown("Paris", 50L, 21500.0),
                new AnalyticsBreakdown.DestinationBreakdown("London", 40L, 16800.0)
        ));

        breakdown.setByHotelType(List.of(
                new AnalyticsBreakdown.HotelTypeBreakdown("Luxury", 70L, 32000.0),
                new AnalyticsBreakdown.HotelTypeBreakdown("Boutique", 55L, 16750.0)
        ));

        AnalyticsSnapshot snapshot = new AnalyticsSnapshot(Instant.now(), metrics, breakdown);
        AnalyticsSnapshot saved = analyticsSnapshotRepository.saveAndFlush(snapshot);

        assertNotNull(saved.getId());
        assertEquals(new BigDecimal("48750.00"), saved.getMetrics().getTotalRevenue());
        assertEquals(125L, saved.getMetrics().getTotalBookings());
        assertNotNull(saved.getBreakdown());
        assertEquals(85L, saved.getBreakdown().getByStatus().get("confirmed"));
        assertEquals(2, saved.getBreakdown().getByDestination().size());
        assertEquals("Paris", saved.getBreakdown().getByDestination().get(0).getCity());

        Optional<AnalyticsSnapshot> latest = analyticsSnapshotRepository.findFirstByOrderByDateDesc();
        assertTrue(latest.isPresent());
        assertEquals(saved.getId(), latest.get().getId());
    }
}


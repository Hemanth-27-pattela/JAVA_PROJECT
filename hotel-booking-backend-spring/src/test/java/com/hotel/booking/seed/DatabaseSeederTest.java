package com.hotel.booking.seed;

import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Review;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelOwnerRequestRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "app.seeder.enabled=true")
public class DatabaseSeederTest {

    @Autowired
    private DatabaseSeeder databaseSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private HotelOwnerRequestRepository requestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        requestRepository.deleteAll();
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Seeder populates an empty database with hotels, users, and sample bookings")
    void testSeederPopulatesEmptyDatabase() {
        assertEquals(0, hotelRepository.count());
        assertEquals(0, userRepository.count());

        boolean seeded = databaseSeeder.seedDemoDataIfEmpty();
        assertTrue(seeded, "Seeder should return true when populating empty database");

        // Verify hotel count
        assertEquals(8, hotelRepository.count(), "Should seed exactly 8 hotels");

        // Verify demo users count
        assertEquals(3, userRepository.count(), "Should seed exactly 3 demo accounts");

        // Verify sample bookings and reviews
        assertEquals(2, bookingRepository.count(), "Should seed 2 sample bookings");
        assertEquals(1, reviewRepository.count(), "Should seed 1 sample review");
    }

    @Test
    @DisplayName("Seeder is idempotent and does not duplicate records on repeated execution")
    void testSeederIsIdempotentOnRepeatedStartup() {
        // First run
        boolean firstRun = databaseSeeder.seedDemoDataIfEmpty();
        assertTrue(firstRun);
        assertEquals(8, hotelRepository.count());
        assertEquals(3, userRepository.count());

        // Second run
        boolean secondRun = databaseSeeder.seedDemoDataIfEmpty();
        assertFalse(secondRun, "Seeder should return false and skip when database is not empty");

        // Verify no duplicate records were created
        assertEquals(8, hotelRepository.count(), "Hotel count must remain 8");
        assertEquals(3, userRepository.count(), "User count must remain 3");
        assertEquals(2, bookingRepository.count(), "Booking count must remain 2");
        assertEquals(1, reviewRepository.count(), "Review count must remain 1");
    }

    @Test
    @DisplayName("Existing hotel records are preserved and prevent overwriting")
    void testExistingHotelRecordsArePreserved() {
        // Pre-create an existing custom user and hotel
        User customOwner = new User("custom.owner@example.com", passwordEncoder.encode("secret"), "Custom", "Owner");
        customOwner.setRole(UserRole.HOTEL_OWNER);
        customOwner = userRepository.save(customOwner);

        Hotel customHotel = new Hotel();
        customHotel.setUser(customOwner);
        customHotel.setName("Custom Boutique Inn");
        customHotel.setCity("Oxford");
        customHotel.setCountry("United Kingdom");
        customHotel.setDescription("A custom hotel added prior to seeder execution.");
        customHotel.setPricePerNight(BigDecimal.valueOf(180));
        customHotel.setStarRating(4);
        customHotel.setAdultCount(2);
        customHotel.setChildCount(0);
        customHotel.setLastUpdated(Instant.now());
        hotelRepository.save(customHotel);

        assertEquals(1, hotelRepository.count());

        // Attempt running seeder
        boolean seeded = databaseSeeder.seedDemoDataIfEmpty();
        assertFalse(seeded, "Seeder must skip when hotels already exist");

        // Verify custom hotel was preserved and no demo hotels were injected
        assertEquals(1, hotelRepository.count());
        Optional<Hotel> retrieved = hotelRepository.findAll().stream().findFirst();
        assertTrue(retrieved.isPresent());
        assertEquals("Custom Boutique Inn", retrieved.get().getName());
        assertEquals("Oxford", retrieved.get().getCity());
    }

    @Test
    @DisplayName("Passwords for all demonstration accounts are BCrypt hashed")
    void testPasswordsAreBcryptHashed() {
        databaseSeeder.seedDemoDataIfEmpty();

        List<String> emails = List.of("admin@hotel.com", "owner@hotel.com", "guest@hotel.com");

        for (String email : emails) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            assertTrue(userOpt.isPresent(), "User " + email + " must exist");
            User user = userOpt.get();

            // Passwords must not be plain text
            assertNotEquals("12345678", user.getPassword(), "Password must not be stored in plain text");

            // Must start with BCrypt signature ($2a$ or $2b$)
            assertTrue(user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"),
                    "Password hash must follow BCrypt format");

            // Must match demo password "12345678"
            assertTrue(passwordEncoder.matches("12345678", user.getPassword()),
                    "BCrypt hash must successfully match demo password '12345678'");
        }
    }

    @Test
    @DisplayName("Seeded account roles and permissions are correct")
    void testSeededAccountRolesAreCorrect() {
        databaseSeeder.seedDemoDataIfEmpty();

        User admin = userRepository.findByEmail("admin@hotel.com").orElseThrow();
        assertEquals(UserRole.ADMIN, admin.getRole(), "admin@hotel.com must have ADMIN role");
        assertTrue(admin.getIsActive());
        assertTrue(admin.getEmailVerified());

        User owner = userRepository.findByEmail("owner@hotel.com").orElseThrow();
        assertEquals(UserRole.HOTEL_OWNER, owner.getRole(), "owner@hotel.com must have HOTEL_OWNER role");
        assertTrue(owner.getIsActive());
        assertTrue(owner.getEmailVerified());

        User guest = userRepository.findByEmail("guest@hotel.com").orElseThrow();
        assertEquals(UserRole.USER, guest.getRole(), "guest@hotel.com must have USER role");
        assertTrue(guest.getIsActive());
        assertTrue(guest.getEmailVerified());
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    @DisplayName("Hotel-owner relationships and data integrity are correct")
    void testHotelOwnerRelationshipIsCorrect() {
        databaseSeeder.seedDemoDataIfEmpty();

        User owner = userRepository.findByEmail("owner@hotel.com").orElseThrow();
        List<Hotel> ownerHotels = hotelRepository.findByUserIdOrderByCreatedAtDesc(owner.getId());

        assertEquals(8, ownerHotels.size(), "All 8 seeded hotels must belong to the seeded hotel owner");

        for (Hotel hotel : ownerHotels) {
            assertNotNull(hotel.getUser());
            assertEquals(owner.getId(), hotel.getUser().getId());
            assertNotNull(hotel.getName());
            assertNotNull(hotel.getCity());
            assertNotNull(hotel.getCountry());
            assertNotNull(hotel.getPricePerNight());
            assertTrue(hotel.getPricePerNight().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(hotel.getStarRating() >= 1 && hotel.getStarRating() <= 5);
            assertFalse(hotel.getType().isEmpty(), "Hotel must have at least one type");
            assertFalse(hotel.getFacilities().isEmpty(), "Hotel must have at least one facility");
            assertFalse(hotel.getImageUrls().isEmpty(), "Hotel must have at least one image URL");
        }
    }

    @Test
    @DisplayName("Bookings and reviews maintain referential integrity and update hotel metrics")
    void testBookingsAndReviewIntegrity() {
        databaseSeeder.seedDemoDataIfEmpty();

        User guest = userRepository.findByEmail("guest@hotel.com").orElseThrow();
        List<Booking> guestBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(guest.getId());
        assertEquals(2, guestBookings.size(), "Guest should have 2 bookings");

        // Verify completed booking has review
        Booking completedBooking = guestBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .findFirst()
                .orElseThrow();

        Optional<Review> reviewOpt = reviewRepository.findByBookingId(completedBooking.getId());
        assertTrue(reviewOpt.isPresent(), "Completed booking should have an associated review");
        Review review = reviewOpt.get();
        assertEquals(5, review.getRating());
        assertTrue(review.getIsVerified());
        assertTrue(review.getComment().contains("[DEMO]"));

        // Verify hotel statistics
        Hotel thamesHotel = hotelRepository.findById(completedBooking.getHotelId()).orElseThrow();
        assertEquals(2, thamesHotel.getTotalBookings());
        assertEquals(5.0, thamesHotel.getAverageRating());
        assertEquals(1, thamesHotel.getReviewCount());
        assertTrue(thamesHotel.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0);
    }
}

package com.hotel.booking.seed;

import com.hotel.booking.entities.Booking;
import com.hotel.booking.entities.Hotel;
import com.hotel.booking.entities.Review;
import com.hotel.booking.entities.User;
import com.hotel.booking.entities.embeddables.Address;
import com.hotel.booking.entities.embeddables.HotelAmenities;
import com.hotel.booking.entities.embeddables.HotelContact;
import com.hotel.booking.entities.embeddables.HotelLocation;
import com.hotel.booking.entities.embeddables.HotelPolicies;
import com.hotel.booking.entities.embeddables.ReviewCategories;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import com.hotel.booking.repositories.BookingRepository;
import com.hotel.booking.repositories.HotelRepository;
import com.hotel.booking.repositories.ReviewRepository;
import com.hotel.booking.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Safe, idempotent database seeder for demonstration and integration testing.
 * Runs on startup only when the hotels table is completely empty.
 * Never creates duplicate records, never overwrites existing records,
 * and maintains full JPA referential integrity across all entities.
 */
@Component
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = false)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            UserRepository userRepository,
            HotelRepository hotelRepository,
            BookingRepository bookingRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedDemoDataIfEmpty();
    }

    /**
     * Seeds initial demonstration records if the hotels repository is empty.
     *
     * @return true if demo data was seeded; false if skipped because hotels already exist.
     */
    @Transactional
    public boolean seedDemoDataIfEmpty() {
        long currentHotelCount = hotelRepository.count();
        if (currentHotelCount > 0) {
            log.info("DatabaseSeeder: Hotel table already contains {} records. Demo seeding skipped to preserve existing data.", currentHotelCount);
            return false;
        }

        log.info("DatabaseSeeder: Hotels table is empty. Beginning initial demo data seeding...");

        // 1. Seed or retrieve the three standard demonstration accounts
        User admin = getOrCreateUser(
                "admin@hotel.com",
                "12345678",
                "System",
                "Admin",
                UserRole.ADMIN,
                "+44 20 7946 0001",
                new Address("10 Downing Street", "London", "England", "United Kingdom", "SW1A 2AA")
        );

        User owner = getOrCreateUser(
                "owner@hotel.com",
                "12345678",
                "Hotel",
                "Owner",
                UserRole.HOTEL_OWNER,
                "+44 131 000 0002",
                new Address("42 Castle Wynd", "Edinburgh", "Scotland", "United Kingdom", "EH1 2NE")
        );

        User guest = getOrCreateUser(
                "guest@hotel.com",
                "12345678",
                "Guest",
                "Traveler",
                UserRole.USER,
                "+44 7700 900123",
                new Address("88 Quayside", "Liverpool", "Merseyside", "United Kingdom", "L3 4FP")
        );

        // 2. Seed 8 realistic demonstration hotels across major global destinations
        List<Hotel> seededHotels = new ArrayList<>();

        // Hotel 1: London, UK
        Hotel h1 = buildHotel(
                "Thames View Boutique",
                "London",
                "United Kingdom",
                "A refined riverside boutique hotel with contemporary rooms, picturesque water views, and easy access to central London landmarks.",
                List.of("Boutique", "Luxury"),
                BigDecimal.valueOf(165.00),
                4,
                2,
                1,
                List.of("Free WiFi", "Non-Smoking Rooms", "Family Rooms", "Fitness Center"),
                List.of(
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                        "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
                        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800"
                ),
                new HotelLocation(51.5074, -0.1278, new Address("25 Embankment", "London", "England", "United Kingdom", "WC2N 6PB")),
                new HotelContact("+44 20 7123 4567", "thames.view@hotel.com", "https://thamesview.hotel.example.com"),
                new HotelPolicies("15:00", "11:00", "Free cancellation up to 48 hours before check-in.", "No pets allowed.", "100% smoke free."),
                true,
                owner
        );
        seededHotels.add(hotelRepository.save(h1));

        // Hotel 2: Edinburgh, UK
        Hotel h2 = buildHotel(
                "Edinburgh Castle Inn",
                "Edinburgh",
                "United Kingdom",
                "Historic comfort situated near the world-famous Royal Mile and Edinburgh Castle. Ideal for leisure getaways and family city breaks.",
                List.of("Budget", "Family"),
                BigDecimal.valueOf(95.00),
                3,
                4,
                2,
                List.of("Free WiFi", "Family Rooms", "Parking"),
                List.of(
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800",
                        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800"
                ),
                new HotelLocation(55.9486, -3.1999, new Address("12 Royal Mile", "Edinburgh", "Scotland", "United Kingdom", "EH1 2PB")),
                new HotelContact("+44 131 555 0199", "castle.inn@hotel.com", "https://castleinn.hotel.example.com"),
                new HotelPolicies("14:00", "10:30", "Free cancellation up to 24 hours before check-in.", "Pets allowed on request.", "Non-smoking rooms."),
                false,
                owner
        );
        seededHotels.add(hotelRepository.save(h2));

        // Hotel 3: Liverpool, UK
        Hotel h3 = buildHotel(
                "Quiet Quayside Suites",
                "Liverpool",
                "United Kingdom",
                "Modern dockside suites designed for extended stays, business trips, and leisure travelers exploring Liverpool's cultural quarters.",
                List.of("Business", "Boutique"),
                BigDecimal.valueOf(120.00),
                4,
                2,
                1,
                List.of("Free WiFi", "Fitness Center", "Parking", "Non-Smoking Rooms"),
                List.of(
                        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800",
                        "https://images.unsplash.com/photo-1591088398332-8a7791972843?w=800"
                ),
                new HotelLocation(53.4084, -2.9916, new Address("5 Albert Dock", "Liverpool", "Merseyside", "United Kingdom", "L3 4AA")),
                new HotelContact("+44 151 709 8811", "quayside@hotel.com", "https://quayside.hotel.example.com"),
                new HotelPolicies("15:00", "11:00", "Standard cancellation policy applies.", "Pets allowed with deposit.", "Non-smoking property."),
                false,
                owner
        );
        seededHotels.add(hotelRepository.save(h3));

        // Hotel 4: Cotswolds, UK
        Hotel h4 = buildHotel(
                "Cotswold Meadow Manor",
                "Cotswolds",
                "United Kingdom",
                "An idyllic honey-stone country manor surrounded by private gardens, luxury spa facilities, and award-winning dining.",
                List.of("Luxury", "Romantic"),
                BigDecimal.valueOf(240.00),
                5,
                2,
                0,
                List.of("Free WiFi", "Spa", "Outdoor Pool", "Fitness Center", "Parking"),
                List.of(
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800"
                ),
                new HotelLocation(51.9294, -1.7246, new Address("High Street", "Bourton-on-the-Water", "Gloucestershire", "United Kingdom", "GL54 2AN")),
                new HotelContact("+44 1451 820200", "cotswold.manor@hotel.com", "https://cotswoldmanor.hotel.example.com"),
                new HotelPolicies("15:00", "12:00", "Free cancellation up to 7 days before arrival.", "Pet-friendly rooms available.", "Non-smoking property."),
                true,
                owner
        );
        seededHotels.add(hotelRepository.save(h4));

        // Hotel 5: Inverness, UK
        Hotel h5 = buildHotel(
                "Highland Pine Lodge",
                "Inverness",
                "United Kingdom",
                "A cozy alpine-inspired mountain lodge on the edge of the Scottish Highlands, surrounded by forest trails and tranquil lochs.",
                List.of("Cabin", "Hiking Resort"),
                BigDecimal.valueOf(135.00),
                4,
                4,
                2,
                List.of("Free WiFi", "Parking", "Family Rooms", "Non-Smoking Rooms"),
                List.of(
                        "https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=800",
                        "https://images.unsplash.com/photo-1510798831971-661eb04b3739?w=800"
                ),
                new HotelLocation(57.4778, -4.2247, new Address("Glenurquhart Road", "Inverness", "Highlands", "United Kingdom", "IV3 5TD")),
                new HotelContact("+44 1463 234567", "highland.pine@hotel.com", "https://highlandpine.hotel.example.com"),
                new HotelPolicies("16:00", "10:00", "Flexible cancellation up to 48 hours.", "Dogs welcome.", "Non-smoking cabins."),
                false,
                owner
        );
        seededHotels.add(hotelRepository.save(h5));

        // Hotel 6: Dublin, Ireland
        Hotel h6 = buildHotel(
                "Grand Central Dublin Hotel",
                "Dublin",
                "Ireland",
                "Contemporary elegance overlooking the River Liffey, conveniently located near Temple Bar, Trinity College, and the business quarter.",
                List.of("Business", "Family"),
                BigDecimal.valueOf(185.00),
                4,
                3,
                1,
                List.of("Free WiFi", "Airport Shuttle", "Fitness Center", "Non-Smoking Rooms"),
                List.of(
                        "https://images.unsplash.com/photo-1568495248636-6432b97bd949?w=800",
                        "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800"
                ),
                new HotelLocation(53.3498, -6.2603, new Address("14 O'Connell Street", "Dublin", "Leinster", "Ireland", "D01 A1B2")),
                new HotelContact("+353 1 874 5555", "dublin.central@hotel.com", "https://grandcentraldublin.hotel.example.com"),
                new HotelPolicies("15:00", "11:00", "Free cancellation up to 24 hours prior to arrival.", "No pets allowed.", "100% non-smoking."),
                true,
                owner
        );
        seededHotels.add(hotelRepository.save(h6));

        // Hotel 7: Paris, France
        Hotel h7 = buildHotel(
                "Eiffel Vista Suites",
                "Paris",
                "France",
                "Chic Parisian hotel suites featuring private balconies with panoramic vistas of the Eiffel Tower, the Seine, and Parisian rooftops.",
                List.of("Romantic", "Luxury"),
                BigDecimal.valueOf(220.00),
                5,
                2,
                1,
                List.of("Free WiFi", "Airport Shuttle", "Spa", "Non-Smoking Rooms"),
                List.of(
                        "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800",
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"
                ),
                new HotelLocation(48.8584, 2.2945, new Address("8 Avenue Montaigne", "Paris", "Île-de-France", "France", "75008")),
                new HotelContact("+33 1 42 68 55 00", "eiffel.vista@hotel.com", "https://eiffelvista.hotel.example.com"),
                new HotelPolicies("15:00", "12:00", "Free cancellation up to 3 days before check-in.", "Small pets allowed.", "Non-smoking property."),
                true,
                owner
        );
        seededHotels.add(hotelRepository.save(h7));

        // Hotel 8: Tokyo, Japan
        Hotel h8 = buildHotel(
                "Shinjuku Garden Haven",
                "Tokyo",
                "Japan",
                "Serene Japanese hospitality harmonizing minimalist aesthetic with modern smart amenities, steps away from Shinjuku Gyoen National Garden.",
                List.of("Boutique", "Business"),
                BigDecimal.valueOf(150.00),
                4,
                2,
                0,
                List.of("Free WiFi", "Spa", "Fitness Center", "Non-Smoking Rooms"),
                List.of(
                        "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800",
                        "https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800"
                ),
                new HotelLocation(35.6895, 139.7005, new Address("3-1-1 Shinjuku", "Tokyo", "Kanto", "Japan", "160-0022")),
                new HotelContact("+81 3 3344 0111", "shinjuku.haven@hotel.com", "https://shinjukuhaven.hotel.example.com"),
                new HotelPolicies("15:00", "11:00", "Free cancellation up to 24 hours before check-in.", "No pets permitted.", "Non-smoking rooms."),
                false,
                owner
        );
        seededHotels.add(hotelRepository.save(h8));

        // 3. Seed sample demonstration bookings and reviews for Thames View Boutique
        Hotel primaryHotel = seededHotels.get(0);
        seedDemoBookingsAndReviews(primaryHotel, guest);

        log.info("DatabaseSeeder: Successfully seeded {} hotels, 3 demo user accounts, and initial demo bookings/reviews.", seededHotels.size());
        return true;
    }

    /**
     * Seeds two demonstration bookings (one completed with verified review, one upcoming confirmed)
     * clearly marked as demo records.
     */
    private void seedDemoBookingsAndReviews(Hotel hotel, User guest) {
        Instant now = Instant.now();

        // 1. Completed past booking
        Instant pastCheckIn = now.minus(10, ChronoUnit.DAYS);
        Instant pastCheckOut = now.minus(7, ChronoUnit.DAYS);
        BigDecimal pastCost = hotel.getPricePerNight().multiply(BigDecimal.valueOf(3));

        Booking completedBooking = new Booking();
        completedBooking.setUser(guest);
        completedBooking.setUserId(guest.getId());
        completedBooking.setHotel(hotel);
        completedBooking.setHotelId(hotel.getId());
        completedBooking.setFirstName(guest.getFirstName());
        completedBooking.setLastName(guest.getLastName());
        completedBooking.setEmail(guest.getEmail());
        completedBooking.setPhone(guest.getPhone());
        completedBooking.setAdultCount(2);
        completedBooking.setChildCount(0);
        completedBooking.setCheckIn(pastCheckIn);
        completedBooking.setCheckOut(pastCheckOut);
        completedBooking.setTotalCost(pastCost);
        completedBooking.setStatus(BookingStatus.COMPLETED);
        completedBooking.setPaymentStatus(PaymentStatus.PAID);
        completedBooking.setPaymentMethod("card");
        completedBooking.setStripePaymentIntentId("pi_demo_seed_thames_past");
        completedBooking.setSpecialRequests("[DEMO] High floor requested with river view");
        completedBooking.setCreatedAt(pastCheckIn.minus(5, ChronoUnit.DAYS));
        completedBooking.setUpdatedAt(pastCheckOut);

        Booking savedCompletedBooking = bookingRepository.save(completedBooking);

        // Verified Review for completed stay
        Review review = new Review();
        review.setUser(guest);
        review.setUserId(guest.getId());
        review.setHotel(hotel);
        review.setHotelId(hotel.getId());
        review.setBooking(savedCompletedBooking);
        review.setBookingId(savedCompletedBooking.getId());
        review.setRating(5);
        review.setComment("[DEMO] Outstanding stay along the River Thames! The views were breathtaking and the staff was exceptionally welcoming.");
        review.setCategories(new ReviewCategories(5, 5, 5, 5, 5));
        review.setIsVerified(true);
        review.setCreatedAt(pastCheckOut.plus(1, ChronoUnit.DAYS));
        review.setUpdatedAt(pastCheckOut.plus(1, ChronoUnit.DAYS));
        reviewRepository.save(review);

        // 2. Upcoming confirmed booking
        Instant futureCheckIn = now.plus(14, ChronoUnit.DAYS);
        Instant futureCheckOut = now.plus(16, ChronoUnit.DAYS);
        BigDecimal futureCost = hotel.getPricePerNight().multiply(BigDecimal.valueOf(2));

        Booking upcomingBooking = new Booking();
        upcomingBooking.setUser(guest);
        upcomingBooking.setUserId(guest.getId());
        upcomingBooking.setHotel(hotel);
        upcomingBooking.setHotelId(hotel.getId());
        upcomingBooking.setFirstName(guest.getFirstName());
        upcomingBooking.setLastName(guest.getLastName());
        upcomingBooking.setEmail(guest.getEmail());
        upcomingBooking.setPhone(guest.getPhone());
        upcomingBooking.setAdultCount(2);
        upcomingBooking.setChildCount(0);
        upcomingBooking.setCheckIn(futureCheckIn);
        upcomingBooking.setCheckOut(futureCheckOut);
        upcomingBooking.setTotalCost(futureCost);
        upcomingBooking.setStatus(BookingStatus.CONFIRMED);
        upcomingBooking.setPaymentStatus(PaymentStatus.PAID);
        upcomingBooking.setPaymentMethod("card");
        upcomingBooking.setStripePaymentIntentId("pi_demo_seed_thames_upcoming");
        upcomingBooking.setSpecialRequests("[DEMO] Late arrival around 8:00 PM");
        upcomingBooking.setCreatedAt(now.minus(1, ChronoUnit.DAYS));
        upcomingBooking.setUpdatedAt(now.minus(1, ChronoUnit.DAYS));

        bookingRepository.save(upcomingBooking);

        // Update hotel statistics
        hotel.setTotalBookings(2);
        hotel.setTotalRevenue(pastCost.add(futureCost));
        hotel.setAverageRating(5.0);
        hotel.setReviewCount(1);
        hotelRepository.save(hotel);

        // Update guest statistics
        guest.setTotalBookings(2);
        guest.setTotalSpent(pastCost.add(futureCost));
        userRepository.save(guest);
    }

    private User getOrCreateUser(
            String email,
            String rawPassword,
            String firstName,
            String lastName,
            UserRole role,
            String phone,
            Address address
    ) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(email, passwordEncoder.encode(rawPassword), firstName, lastName);
            user.setRole(role);
            user.setIsActive(true);
            user.setEmailVerified(true);
            user.setPhone(phone);
            user.setAddress(address);
            User saved = userRepository.save(user);
            log.info("DatabaseSeeder: Created demo account {} with role {}", email, role);
            return saved;
        });
    }

    private Hotel buildHotel(
            String name,
            String city,
            String country,
            String description,
            List<String> types,
            BigDecimal pricePerNight,
            int starRating,
            int adultCount,
            int childCount,
            List<String> facilities,
            List<String> imageUrls,
            HotelLocation location,
            HotelContact contact,
            HotelPolicies policies,
            boolean isFeatured,
            User owner
    ) {
        Hotel hotel = new Hotel();
        hotel.setUser(owner);
        hotel.setUserId(owner.getId());
        hotel.setName(name);
        hotel.setCity(city);
        hotel.setCountry(country);
        hotel.setDescription(description);
        hotel.setType(new ArrayList<>(types));
        hotel.setPricePerNight(pricePerNight);
        hotel.setStarRating(starRating);
        hotel.setAdultCount(adultCount);
        hotel.setChildCount(childCount);
        hotel.setFacilities(new ArrayList<>(facilities));
        hotel.setImageUrls(new ArrayList<>(imageUrls));
        hotel.setLocation(location);
        hotel.setContact(contact);
        hotel.setPolicies(policies);
        hotel.setIsActive(true);
        hotel.setIsFeatured(isFeatured);
        hotel.setLastUpdated(Instant.now());
        hotel.setTotalBookings(0);
        hotel.setTotalRevenue(BigDecimal.ZERO);
        hotel.setAverageRating(Double.valueOf(starRating));
        hotel.setReviewCount(0);
        hotel.setOccupancyRate(0.0);

        // Amenities flags
        HotelAmenities amenities = new HotelAmenities();
        amenities.setWifi(facilities.contains("Free WiFi"));
        amenities.setParking(facilities.contains("Parking"));
        amenities.setGym(facilities.contains("Fitness Center"));
        amenities.setSpa(facilities.contains("Spa"));
        amenities.setPool(facilities.contains("Outdoor Pool"));
        amenities.setAirportShuttle(facilities.contains("Airport Shuttle"));
        hotel.setAmenities(amenities);

        return hotel;
    }
}


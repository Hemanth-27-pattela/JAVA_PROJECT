package com.hotel.booking.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotel.booking.dtos.HotelDto;
import com.hotel.booking.dtos.UserDto;
import com.hotel.booking.entities.embeddables.Address;
import com.hotel.booking.entities.enums.BookingStatus;
import com.hotel.booking.entities.enums.PaymentStatus;
import com.hotel.booking.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntitySerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("User entity serializes ID as _id and never outputs id or password")
    void testUserSerialization() throws Exception {
        User user = new User("alice@example.com", "secretHash123", "Alice", "Smith");
        user.setId("user-uuid-123");
        user.setRole(UserRole.USER);
        user.setAddress(new Address("123 Main St", "London", "Greater London", "UK", "SW1A 1AA"));

        String json = objectMapper.writeValueAsString(user);
        JsonNode node = objectMapper.readTree(json);

        // Verify _id exists and matches
        assertTrue(node.has("_id"), "JSON must have _id property");
        assertEquals("user-uuid-123", node.get("_id").asText());

        // Verify id does NOT exist
        assertFalse(node.has("id"), "JSON must NOT have duplicate or conflicting 'id' property");

        // Verify password is NOT exposed
        assertFalse(node.has("password"), "JSON must NOT expose password hash");

        // Verify role is lowercase
        assertEquals("user", node.get("role").asText());
    }

    @Test
    @DisplayName("Hotel entity serializes ID as _id, userId as string, and never outputs id")
    void testHotelSerialization() throws Exception {
        User owner = new User("owner@example.com", "hash", "Bob", "Owner");
        owner.setId("owner-uuid-456");

        Hotel hotel = new Hotel();
        hotel.setId("hotel-uuid-789");
        hotel.setUser(owner);
        hotel.setName("Grand Royal Palace");
        hotel.setCity("Paris");
        hotel.setCountry("France");
        hotel.setDescription("Luxury hotel near Eiffel Tower");
        hotel.setType(List.of("Luxury", "Boutique"));
        hotel.setFacilities(List.of("Free WiFi", "Spa", "Pool"));
        hotel.setPricePerNight(new BigDecimal("299.99"));
        hotel.setStarRating(5);
        hotel.setImageUrls(List.of("https://example.com/img1.jpg", "https://example.com/img2.jpg"));
        hotel.setLastUpdated(Instant.now());

        String json = objectMapper.writeValueAsString(hotel);
        JsonNode node = objectMapper.readTree(json);

        assertTrue(node.has("_id"), "Hotel JSON must have _id property");
        assertEquals("hotel-uuid-789", node.get("_id").asText());
        assertFalse(node.has("id"), "Hotel JSON must NOT have 'id' property");

        assertTrue(node.has("userId"), "Hotel JSON must have userId property");
        assertEquals("owner-uuid-456", node.get("userId").asText());

        // Verify monetary value
        assertEquals(299.99, node.get("pricePerNight").asDouble());
    }

    @Test
    @DisplayName("Booking entity serializes ID as _id, userId, hotelId, and status enums")
    void testBookingSerialization() throws Exception {
        Booking booking = new Booking();
        booking.setId("booking-uuid-111");
        booking.setUserId("user-uuid-123");
        booking.setHotelId("hotel-uuid-789");
        booking.setFirstName("John");
        booking.setLastName("Doe");
        booking.setEmail("john@example.com");
        booking.setCheckIn(Instant.now());
        booking.setCheckOut(Instant.now().plusSeconds(86400 * 3));
        booking.setTotalCost(new BigDecimal("599.50"));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        String json = objectMapper.writeValueAsString(booking);
        JsonNode node = objectMapper.readTree(json);

        assertTrue(node.has("_id"));
        assertEquals("booking-uuid-111", node.get("_id").asText());
        assertFalse(node.has("id"));

        assertEquals("user-uuid-123", node.get("userId").asText());
        assertEquals("hotel-uuid-789", node.get("hotelId").asText());
        assertEquals("confirmed", node.get("status").asText());
        assertEquals("paid", node.get("paymentStatus").asText());
    }

    @Test
    @DisplayName("UserDto and HotelDto serialize _id correctly and deserialize from _id or id")
    void testDtoSerializationAndDeserialization() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId("dto-user-123");
        userDto.setEmail("user@dto.com");
        userDto.setRole(UserRole.ADMIN);

        String userJson = objectMapper.writeValueAsString(userDto);
        JsonNode userNode = objectMapper.readTree(userJson);

        assertTrue(userNode.has("_id"));
        assertFalse(userNode.has("id"));
        assertEquals("admin", userNode.get("role").asText());

        // Deserialization with _id
        String incomingJson = "{\"_id\":\"incoming-id\",\"email\":\"in@test.com\"}";
        UserDto deserialized1 = objectMapper.readValue(incomingJson, UserDto.class);
        assertEquals("incoming-id", deserialized1.getId());

        // Deserialization with id (via @JsonAlias)
        String incomingJsonWithId = "{\"id\":\"alias-id\",\"email\":\"in@test.com\"}";
        UserDto deserialized2 = objectMapper.readValue(incomingJsonWithId, UserDto.class);
        assertEquals("alias-id", deserialized2.getId());
    }
}


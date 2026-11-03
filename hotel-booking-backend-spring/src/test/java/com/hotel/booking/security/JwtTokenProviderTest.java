package com.hotel.booking.security;

import com.hotel.booking.entities.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String validSecret = "testSecretKeyForIsolatedUnitTestingPurposesOnlyMustBeAtLeast256BitsLong12345";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000L); // 1 hour
        tokenProvider.init();
    }

    @Test
    @DisplayName("Generate and validate valid JWT token")
    void testGenerateAndValidateToken() {
        String token = tokenProvider.generateToken("user-123", "alice@example.com", UserRole.USER);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("user-123", tokenProvider.getUserIdFromToken(token));
        assertEquals("alice@example.com", tokenProvider.getEmailFromToken(token));
        assertEquals("user", tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Reject expired JWT token")
    void testExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationMs", -1000L); // Already expired
        shortLivedProvider.init();

        String expiredToken = shortLivedProvider.generateToken("user-exp", "exp@example.com", UserRole.USER);
        assertFalse(tokenProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Reject token signed with different secret key")
    void testInvalidSignature() {
        SecretKey otherKey = Keys.hmacShaKeyFor("differentKeyThatIsAlso256BitsLongForTestingSignatures12345678".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = Jwts.builder()
                .setSubject("user-tampered")
                .claim("userId", "user-tampered")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(tokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Reject malformed or empty token")
    void testMalformedToken() {
        assertFalse(tokenProvider.validateToken("not-a-valid-jwt-token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Fail securely at startup if secret is missing or under 256 bits")
    void testStartupFailsIfSecretMissingOrTooShort() {
        JwtTokenProvider emptySecretProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(emptySecretProvider, "jwtSecret", "");
        assertThrows(IllegalStateException.class, emptySecretProvider::init);

        JwtTokenProvider shortSecretProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortSecretProvider, "jwtSecret", "too-short");
        assertThrows(IllegalStateException.class, shortSecretProvider::init);
    }
}


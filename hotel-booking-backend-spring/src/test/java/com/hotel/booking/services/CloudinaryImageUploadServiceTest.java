package com.hotel.booking.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloudinaryImageUploadServiceTest {

    @Test
    @DisplayName("Upload with null or empty list returns empty list without error")
    void testUploadEmptyList() {
        CloudinaryImageUploadService service = new CloudinaryImageUploadService("", "", "");
        List<String> resultNull = service.uploadImages(null);
        List<String> resultEmpty = service.uploadImages(Collections.emptyList());

        assertTrue(resultNull.isEmpty());
        assertTrue(resultEmpty.isEmpty());
    }

    @Test
    @DisplayName("Upload without credentials throws IllegalStateException")
    void testUploadUnconfiguredThrows() {
        CloudinaryImageUploadService service = new CloudinaryImageUploadService("", "", "");
        assertFalse(service.isConfigured());

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "some image bytes".getBytes()
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.uploadImages(List.of(file));
        });
        assertTrue(ex.getMessage().contains("Cloudinary credentials are not configured"));
    }

    @Test
    @DisplayName("Upload file exceeding 5MB limit throws IllegalArgumentException")
    void testUploadFileTooLargeThrows() {
        CloudinaryImageUploadService service = new CloudinaryImageUploadService("cloud", "key", "secret");
        assertTrue(service.isConfigured());

        // 6MB dummy byte array
        byte[] largeBytes = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "huge.jpg",
                "image/jpeg",
                largeBytes
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.uploadImages(List.of(largeFile));
        });
        assertTrue(ex.getMessage().contains("exceeds the 5MB size limit"));
    }

    @Test
    @DisplayName("Upload non-image file throws IllegalArgumentException")
    void testUploadNonImageFileThrows() {
        CloudinaryImageUploadService service = new CloudinaryImageUploadService("cloud", "key", "secret");
        assertTrue(service.isConfigured());

        MockMultipartFile textFile = new MockMultipartFile(
                "image",
                "malicious.txt",
                "text/plain",
                "some text".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.uploadImages(List.of(textFile));
        });
        assertTrue(ex.getMessage().contains("must be an image"));
    }
}


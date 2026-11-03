package com.hotel.booking.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryImageUploadService implements ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryImageUploadService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private Cloudinary cloudinary;

    public CloudinaryImageUploadService(
            @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}") String cloudName,
            @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}") String apiKey,
            @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}") String apiSecret
    ) {
        this.cloudName = cloudName != null ? cloudName.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiSecret = apiSecret != null ? apiSecret.trim() : "";

        if (isConfigured()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", this.cloudName,
                    "api_key", this.apiKey,
                    "api_secret", this.apiSecret,
                    "secure", true
            ));
            log.info("Cloudinary configured successfully for cloud: {}", this.cloudName);
        } else {
            log.warn("Cloudinary credentials not fully configured. Image uploads will require configuration.");
        }
    }

    public boolean isConfigured() {
        return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }

        if (!isConfigured() && cloudinary == null) {
            throw new IllegalStateException(
                    "Cloudinary credentials are not configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET."
            );
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            // Validate file size (max 5MB)
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " exceeds the 5MB size limit");
            }

            // Validate file content type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File " + file.getOriginalFilename() + " must be an image");
            }

            try {
                @SuppressWarnings("rawtypes")
                Map uploadParams = ObjectUtils.asMap(
                        "secure", true,
                        "transformation", new Transformation().width(800).height(600).crop("fill").quality("auto")
                );

                @SuppressWarnings("unchecked")
                Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                String secureUrl = (String) result.get("secure_url");
                if (secureUrl == null || secureUrl.isBlank()) {
                    secureUrl = (String) result.get("url");
                }
                if (secureUrl != null) {
                    imageUrls.add(secureUrl);
                }
            } catch (IOException e) {
                log.error("Failed to upload image file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
            }
        }

        return imageUrls;
    }
}


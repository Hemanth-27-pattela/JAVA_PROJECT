package com.hotel.booking.services;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImageUploadService {
    List<String> uploadImages(List<MultipartFile> imageFiles);
}


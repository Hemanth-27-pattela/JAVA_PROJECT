package com.hotel.booking.entities.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.entities.embeddables.AnalyticsBreakdown;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class AnalyticsBreakdownConverter implements AttributeConverter<AnalyticsBreakdown, String> {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsBreakdownConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AnalyticsBreakdown attribute) {
        if (attribute == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert AnalyticsBreakdown to JSON string", e);
            return "{}";
        }
    }

    @Override
    public AnalyticsBreakdown convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new AnalyticsBreakdown();
        }
        try {
            return objectMapper.readValue(dbData, AnalyticsBreakdown.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON string to AnalyticsBreakdown: {}", dbData, e);
            return new AnalyticsBreakdown();
        }
    }
}


package com.hotel.booking.entities.converters;

import com.hotel.booking.entities.enums.BookingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus attribute) {
        if (attribute == null) {
            return BookingStatus.PENDING.getValue();
        }
        return attribute.getValue();
    }

    @Override
    public BookingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return BookingStatus.PENDING;
        }
        return BookingStatus.fromValue(dbData);
    }
}


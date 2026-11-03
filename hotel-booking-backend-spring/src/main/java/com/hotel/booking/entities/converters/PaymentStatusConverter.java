package com.hotel.booking.entities.converters;

import com.hotel.booking.entities.enums.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStatus attribute) {
        if (attribute == null) {
            return PaymentStatus.PENDING.getValue();
        }
        return attribute.getValue();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return PaymentStatus.PENDING;
        }
        return PaymentStatus.fromValue(dbData);
    }
}


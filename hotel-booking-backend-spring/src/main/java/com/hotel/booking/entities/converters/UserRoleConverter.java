package com.hotel.booking.entities.converters;

import com.hotel.booking.entities.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) {
            return UserRole.USER.getValue();
        }
        return attribute.getValue();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return UserRole.USER;
        }
        return UserRole.fromValue(dbData);
    }
}


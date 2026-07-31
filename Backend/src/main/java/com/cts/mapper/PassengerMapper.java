package com.cts.mapper;

import com.cts.dto.PassengerProfileDTO;
import com.cts.entity.PassengerProfile;
import com.cts.entity.User;
import com.cts.enumeration.IdProofType;
import org.springframework.stereotype.Component;

@Component
public class PassengerMapper {

    public PassengerProfile toEntity(PassengerProfileDTO dto, User customer) {
        if (dto == null) return null;
        IdProofType type = parseType(dto.getIdProofType());
        com.cts.util.IdProofValidator.validate(type, dto.getIdProofNumber());
        return PassengerProfile.builder()
                .customer(customer)
                .name(dto.getName())
                .age(dto.getAge())
                .gender(dto.getGender())
                .idProofType(type)
                .idProofNumber(com.cts.util.IdProofValidator.normalize(dto.getIdProofNumber()))
                .build();
    }

    public PassengerProfileDTO toDTO(PassengerProfile entity) {
        if (entity == null) return null;
        PassengerProfileDTO dto = new PassengerProfileDTO();
        dto.setProfileId(entity.getProfileId());
        dto.setName(entity.getName());
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());
        dto.setIdProofType(entity.getIdProofType().name());
        dto.setIdProofNumber(entity.getIdProofNumber());
        return dto;
    }

    public void updateEntityFromDTO(PassengerProfileDTO dto, PassengerProfile entity) {
        if (dto == null || entity == null) return;
        if (dto.getName() != null)   entity.setName(dto.getName());
        if (dto.getAge() != null)    entity.setAge(dto.getAge());
        if (dto.getGender() != null) entity.setGender(dto.getGender());
        if (dto.getIdProofType() != null) {
            IdProofType type = parseType(dto.getIdProofType());
            com.cts.util.IdProofValidator.validate(type, dto.getIdProofNumber());
            entity.setIdProofType(type);
            entity.setIdProofNumber(com.cts.util.IdProofValidator.normalize(dto.getIdProofNumber()));
        }
    }

    private IdProofType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new com.cts.exception.InvalidTimelineException("ID proof type is required.");
        }
        try {
            return IdProofType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new com.cts.exception.InvalidTimelineException(
                    "Unknown ID proof type '" + raw + "'. Allowed: PAN, AADHAAR, DRIVING_LICENSE, PASSPORT.");
        }
    }
}
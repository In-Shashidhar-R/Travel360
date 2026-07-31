package com.cts.mapper;

import com.cts.dto.PartnerRequestDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.dto.UpdatePartnerRequestDTO;
import com.cts.entity.Partner;
import com.cts.enumeration.Status;
import org.springframework.stereotype.Component;

@Component
public class PartnerMapper {

    public Partner toEntity(PartnerRequestDTO dto) {
        if (dto == null) return null;
        return Partner.builder()
                .name(dto.getName())
                .type(dto.getType())
                .email(dto.getEmail())
                .contactNumber(dto.getContactNumber())
                .status(Status.ACTIVE) // Default state upon provisioning
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .gender(dto.getGender())
                .gstNumber(dto.getGstNumber())
                .commissionRate(dto.getCommissionRate())
                .build();
    }

    public PartnerResponseDTO toResponseDTO(Partner entity) {
        if (entity == null) return null;
        PartnerResponseDTO dto = new PartnerResponseDTO();
        dto.setPartnerId(entity.getPartnerId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setEmail(entity.getEmail());
        dto.setContactNumber(entity.getContactNumber());
        dto.setStatus(entity.getStatus().name());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setGender(entity.getGender());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setGstNumber(entity.getGstNumber());
        dto.setCommissionRate(entity.getCommissionRate());
        if (entity.getUser() != null) {
            dto.setLoginUserId(entity.getUser().getUserId());
        }
        return dto;
    }
    
    public void updateEntityFromDTO(UpdatePartnerRequestDTO dto, Partner entity) {

        if (dto == null || entity == null) return;

        

        entity.setName(dto.getName());

        entity.setType(dto.getType());

        entity.setContactNumber(dto.getContactNumber());

        entity.setAddress(dto.getAddress());

        entity.setCity(dto.getCity());

        entity.setState(dto.getState());

        entity.setCountry(dto.getCountry());
        
        entity.setDateOfBirth(dto.getDateOfBirth());

        entity.setGender(dto.getGender());

        entity.setGstNumber(dto.getGstNumber());

        entity.setCommissionRate(dto.getCommissionRate());

    }
}
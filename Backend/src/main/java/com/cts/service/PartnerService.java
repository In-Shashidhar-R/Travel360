package com.cts.service;

import com.cts.dto.PageResponse;
import com.cts.dto.PartnerRequestDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.dto.UpdatePartnerRequestDTO;

import org.springframework.data.domain.Pageable;

public interface PartnerService {
    PartnerResponseDTO registerPartner(PartnerRequestDTO request);
    PageResponse<PartnerResponseDTO> getAllPartners(Pageable pageable);
    PartnerResponseDTO updatePartnerProfile(Long partnerId, UpdatePartnerRequestDTO dto, String authenticatedEmail);
}

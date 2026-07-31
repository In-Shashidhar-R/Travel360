package com.cts.serviceimpl;

import com.cts.dto.PageResponse;
import com.cts.dto.PartnerRequestDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.dto.UpdatePartnerRequestDTO;
import com.cts.entity.Partner;
import com.cts.entity.User;
import com.cts.enumeration.Role;
import com.cts.exception.IdentityConflictException;
import com.cts.mapper.PartnerMapper;
import com.cts.repository.PartnerRepository;
import com.cts.repository.UserRepository;
import com.cts.service.PartnerService;
import com.cts.util.CoreTransactionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;
    private final CoreTransactionalUtil transUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PartnerResponseDTO registerPartner(PartnerRequestDTO request) {
        transUtil.validateNewPartnerEmail(request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IdentityConflictException(
                    "A user account with this email already exists; choose a different email for the partner.");
        }

        User loginAccount = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PARTNER)
                .phone(request.getContactNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .state(request.getState())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .build();
        User savedUser = userRepository.save(loginAccount);

        Partner partner = partnerMapper.toEntity(request);
        partner.setUser(savedUser);

        return partnerMapper.toResponseDTO(transUtil.commitPartner(partner));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PartnerResponseDTO> getAllPartners(Pageable pageable) {
        return PageResponse.from(partnerRepository.findAll(pageable).map(partnerMapper::toResponseDTO));
    }
    
    @Override
    @Transactional
    public PartnerResponseDTO updatePartnerProfile(Long partnerId, UpdatePartnerRequestDTO dto, String authenticatedEmail) {
        // 1. Locate the target partner workspace and current executing identification entity
        Partner targetPartner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Partner profile workspace registration not found."));
        
        User currentUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Authenticated tracking user context missing."));

        // 2. Validate RBAC updates: Self-update or global operational admin clearance
        boolean isSelfUpdate = targetPartner.getUser() != null && 
                targetPartner.getUser().getEmail().equalsIgnoreCase(authenticatedEmail);
        
        boolean isAdminAlteringTarget = currentUser.getRole() == Role.ADMIN;

        if (!isSelfUpdate && !isAdminAlteringTarget) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access violation: You do not possess structural clearance to modify this merchant partner distribution workspace.");
        }

        // 3. Delegate field mutations directly to your mapper component
        partnerMapper.updateEntityFromDTO(dto, targetPartner);

     // 4. Synchronize core metadata fields down into the associated user login profile
       if (targetPartner.getUser() != null) {
         User associatedLogin = targetPartner.getUser();
         associatedLogin.setName(dto.getName());
         associatedLogin.setPhone(dto.getContactNumber());
         associatedLogin.setAddress(dto.getAddress());
         associatedLogin.setCity(dto.getCity());
         associatedLogin.setState(dto.getState());
         associatedLogin.setCountry(dto.getCountry());
         associatedLogin.setGender(dto.getGender());
         associatedLogin.setDateOfBirth(dto.getDateOfBirth()); 
         
         userRepository.save(associatedLogin);
     }

     Partner savedPartner = partnerRepository.save(targetPartner);
     return partnerMapper.toResponseDTO(savedPartner);
    }
}

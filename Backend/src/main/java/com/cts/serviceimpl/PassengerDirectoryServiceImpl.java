package com.cts.serviceimpl;

import com.cts.dto.PassengerProfileDTO;
import com.cts.entity.PassengerProfile;
import com.cts.entity.User;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.PassengerMapper;
import com.cts.repository.PassengerProfileRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.PassengerDirectoryService;
import com.cts.util.UserSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PassengerDirectoryServiceImpl implements PassengerDirectoryService {

    private final PassengerProfileRepository profileRepository;
    private final PassengerMapper passengerMapper;
    private final UserSecurityUtil securityUtil;

    @Override
    @Transactional
    public void savePassengerProfile(Long customerId, PassengerProfileDTO dto) {
        User customer = securityUtil.fetchUser(customerId);
        PassengerProfile profile = passengerMapper.toEntity(dto, customer);
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void updatePassengerProfile(Long profileId, PassengerProfileDTO dto) {
        PassengerProfile existingRecord = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Target passenger directory data row index missing"));
        SecurityUtil.assertSelfOrAdmin(existingRecord.getCustomer().getUserId());
        passengerMapper.updateEntityFromDTO(dto, existingRecord);
        profileRepository.save(existingRecord);
    }

    @Override
    @Transactional
    public void removePassengerProfile(Long profileId) {
        PassengerProfile record = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Target catalog item data profile index not found"));
        SecurityUtil.assertSelfOrAdmin(record.getCustomer().getUserId());
        profileRepository.delete(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerProfileDTO> getCustomerDirectoryPool(Long customerId) {
        User customer = securityUtil.fetchUser(customerId);
        return profileRepository.findByCustomer(customer).stream()
                .map(passengerMapper::toDTO)
                .toList();
    }
}
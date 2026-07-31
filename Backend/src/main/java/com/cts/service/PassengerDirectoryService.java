package com.cts.service;

import com.cts.dto.PassengerProfileDTO;
import java.util.List;

public interface PassengerDirectoryService {
    void savePassengerProfile(Long customerId, PassengerProfileDTO dto);
    void updatePassengerProfile(Long profileId, PassengerProfileDTO dto);
    void removePassengerProfile(Long profileId);
    List<PassengerProfileDTO> getCustomerDirectoryPool(Long customerId);
}
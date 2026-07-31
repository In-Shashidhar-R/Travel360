package com.cts.service;

import com.cts.dto.ItineraryEntryDTO;

import java.util.List;

public interface ItineraryService {

    List<ItineraryEntryDTO> getMyUpcomingTrips();
    List<ItineraryEntryDTO> getMyPastTrips();
    List<ItineraryEntryDTO> getTripsForCustomer(Long customerId);
}

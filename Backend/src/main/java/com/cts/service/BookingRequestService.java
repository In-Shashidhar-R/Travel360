package com.cts.service;

import com.cts.dto.*;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface BookingRequestService {

    BookingRequestResponseDTO createRequest(BookingRequestCreateDTO req);

    BookingRequestResponseDTO acceptRequest(Long requestId, BookingRequestDecisionDTO decision);

    BookingRequestResponseDTO rejectRequest(Long requestId, BookingRequestDecisionDTO decision);

    BookingRequestResponseDTO completeRequestByBooking(Long requestId, TourBookingRequestDTO bookingPayload);

    BookingRequestResponseDTO getRequestById(Long requestId);
    
    List<BookingRequestResponseDTO> getall();

    PageResponse<BookingRequestResponseDTO> listMyCustomerRequests(Pageable pageable);

    PageResponse<BookingRequestResponseDTO> listAssignedRequests(Pageable pageable);
}

package com.cts.service;

import com.cts.dto.BusBookingRequestDTO;
import com.cts.dto.BusBookingResponseDTO;
import com.cts.dto.CabBookingRequestDTO;
import com.cts.dto.CabBookingResponseDTO;
import com.cts.dto.FlightBookingRequestDTO;
import com.cts.dto.FlightBookingResponseDTO;
import com.cts.dto.HotelBookingRequestDTO;
import com.cts.dto.HotelBookingResponseDTO;
import com.cts.dto.InvoiceCancelResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.dto.PartialCancelRequestDTO;
import com.cts.dto.TourBookingRequestDTO;
import com.cts.dto.TourBookingResponseDTO;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    FlightBookingResponseDTO bookFlight(FlightBookingRequestDTO request);

    HotelBookingResponseDTO bookHotel(HotelBookingRequestDTO request);

    BusBookingResponseDTO bookBus(BusBookingRequestDTO request);

    CabBookingResponseDTO bookCab(CabBookingRequestDTO request);

    TourBookingResponseDTO bookTour(TourBookingRequestDTO request);

    InvoiceCancelResponseDTO cancelBooking(Long bookingId, PartialCancelRequestDTO request);

    InvoiceCancelResponseDTO cancelEntireBooking(Long bookingId, Long customerId, String cancellationRemarks);

    Object getBookingById(Long bookingId);

    PageResponse<Object> getAllBookings(Pageable pageable);

    PageResponse<Object> getCustomerBookings(Long customerId, Pageable pageable);
}

package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.BookingService;
import com.cts.service.ComplaintService;
import com.cts.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private final FlightBookingUtil flightUtil;
    private final HotelBookingUtil hotelUtil;
    private final TransitBookingUtil transitUtil;
    private final UserSecurityUtil securityUtil;
    private final CoreTransactionalUtil coreUtil;
    private final ComplaintService complaintService;

    @Override
    @Transactional
    public FlightBookingResponseDTO bookFlight(FlightBookingRequestDTO req) {
        User user = securityUtil.fetchUser(req.getCustomerId());
        FlightInventory flight = flightUtil.fetchFlight(req.getInventoryId());
        List<PassengerProfile> passengers = securityUtil.fetchAndValidateProfiles(req.getPassengerProfileIds(), user.getUserId());

        double finalCost = flightUtil.calculateFlightCost(flight, req.getChosenSeatType(), passengers.size(), req.getTargetTravelDate());

        Booking booking = bookingMapper.toFlightEntity(user, flight, passengers.size(), req.getTargetTravelDate(), finalCost, passengers);
        booking.setChosenSeatType(req.getChosenSeatType().name());

        return bookingMapper.toFlightBookingResponse(coreUtil.executeBookingPipeline(booking, finalCost, req.getTargetTravelDate(), "FLIGHT"));
    }

    @Override
    @Transactional
    public HotelBookingResponseDTO bookHotel(HotelBookingRequestDTO req) {
        User user = securityUtil.fetchUser(req.getCustomerId());
        HotelInventory hotel = hotelUtil.fetchHotel(req.getInventoryId());
        List<PassengerProfile> passengers = securityUtil.fetchOptionalProfiles(req.getPassengerProfileIds(), user.getUserId());

        double finalCost = hotelUtil.calculateHotelCost(hotel, req.getCheckInDate(), req.getCheckOutDate(), req.getRequestedRooms());

        Booking booking = bookingMapper.toHotelEntity(user, hotel, req.getRequestedRooms(), req.getCheckInDate(), req.getCheckOutDate(), finalCost, passengers);
        return bookingMapper.toHotelBookingResponse(coreUtil.executeBookingPipeline(booking, finalCost, req.getCheckInDate(), "HOTEL"));
    }

    @Override
    @Transactional
    public BusBookingResponseDTO bookBus(BusBookingRequestDTO req) {
        User user = securityUtil.fetchUser(req.getCustomerId());
        BusInventory bus = transitUtil.fetchBus(req.getInventoryId());
        List<PassengerProfile> passengers = securityUtil.fetchAndValidateProfiles(req.getPassengerProfileIds(), user.getUserId());

        double finalCost = transitUtil.calculateBusCost(bus, req.getChosenSeatType(), passengers.size(), req.getTargetTravelDate());

        Booking booking = bookingMapper.toBusEntity(user, bus, passengers.size(), req.getTargetTravelDate(), req.getPickupLocation(), req.getDropoffLocation(), finalCost, passengers);
        booking.setChosenSeatType(req.getChosenSeatType().name());

        return bookingMapper.toBusBookingResponse(coreUtil.executeBookingPipeline(booking, finalCost, req.getTargetTravelDate(), "BUS"));
    }

    @Override
    @Transactional
    public CabBookingResponseDTO bookCab(CabBookingRequestDTO req) {
        User user = securityUtil.fetchUser(req.getCustomerId());
        CabInventory cab = transitUtil.fetchAndValidateCab(req.getInventoryId(), req.getDistrict(), req.getState());
        List<PassengerProfile> passengers = securityUtil.fetchAndValidateProfiles(req.getPassengerProfileIds(), user.getUserId());

        double finalCost = transitUtil.calculateCabCost(cab, req.getTargetTravelDate());

        Booking booking = bookingMapper.toCabEntity(user, cab, req.getTargetTravelDate(), req.getPickupLocation(), req.getDropoffLocation(), finalCost, passengers);
        return bookingMapper.toCabBookingResponse(coreUtil.executeBookingPipeline(booking, finalCost, req.getTargetTravelDate(), "CAB"));
    }

    @Override
    @Transactional
    public TourBookingResponseDTO bookTour(TourBookingRequestDTO req) {
        User user = securityUtil.fetchUser(req.getCustomerId());
        TourPackageInventory tour = transitUtil.fetchTour(req.getInventoryId());
        List<PassengerProfile> passengers = securityUtil.fetchAndValidateProfiles(req.getPassengerProfileIds(), user.getUserId());

        double finalCost = transitUtil.calculateTourCost(tour, req.getNumberOfPersons(), passengers.size(), req.getTargetTravelDate());

        Booking booking = bookingMapper.toTourEntity(user, tour, req.getNumberOfPersons(), req.getTargetTravelDate(), finalCost, passengers);
        return bookingMapper.toTourBookingResponse(coreUtil.executeBookingPipeline(booking, finalCost, req.getTargetTravelDate(), "TOUR_PACKAGE"));
    }

    @Override
    @Transactional
    public InvoiceCancelResponseDTO cancelBooking(Long bookingId, PartialCancelRequestDTO req) {
        Booking booking = coreUtil.fetchBooking(bookingId);
        InvoiceCancelResponseDTO response =
                coreUtil.executeCancellationPipeline(booking, req.getCustomerId(), req.getPassengerProfileIdsToCancel());
        recordCancellationRemarks(bookingId, req.getCancellationRemarks());
        return response;
    }

    @Override
    @Transactional
    public InvoiceCancelResponseDTO cancelEntireBooking(Long bookingId, Long customerId, String cancellationRemarks) {
        Booking booking = coreUtil.fetchBooking(bookingId);
        InvoiceCancelResponseDTO response = coreUtil.executeFullCancellationPipeline(booking, customerId);
        recordCancellationRemarks(bookingId, cancellationRemarks);
        return response;
    }

    private void recordCancellationRemarks(Long bookingId, String remarks) {
        if (remarks == null || remarks.isBlank()) {
            return;
        }
        ComplaintCreateDTO complaint = new ComplaintCreateDTO();
        complaint.setSubject("Cancellation remarks for booking #" + bookingId);
        complaint.setDescription(remarks.trim());
        complaint.setRelatedBookingId(bookingId);
        complaintService.raiseComplaint(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getBookingById(Long bookingId) {
        Booking booking = coreUtil.fetchBooking(bookingId);
        SecurityUtil.assertSelfOrAdmin(booking.getCustomer().getUserId());
        return bookingMapper.toGenericBookingResponseDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Object> getAllBookings(Pageable pageable) {
        Page<Object> page = bookingRepository.findAll(pageable).map(bookingMapper::toGenericBookingResponseDTO);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Object> getCustomerBookings(Long customerId, Pageable pageable) {
        User customer = securityUtil.fetchUser(customerId);
        Page<Object> page = bookingRepository.findByCustomer(customer, pageable).map(bookingMapper::toGenericBookingResponseDTO);
        return PageResponse.from(page);
    }
}

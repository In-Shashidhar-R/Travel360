package com.cts.mapper;

import com.cts.dto.BookingResponseDTO;
import com.cts.dto.BusBookingResponseDTO;
import com.cts.dto.CabBookingResponseDTO;
import com.cts.dto.FlightBookingResponseDTO;
import com.cts.dto.HotelBookingResponseDTO;
import com.cts.dto.InvoiceResponseDTO;
import com.cts.dto.TourBookingResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Inventory;
import com.cts.entity.Invoice;
import com.cts.entity.Itinerary;
import com.cts.entity.KPIReport;
import com.cts.entity.Notification;
import com.cts.entity.PassengerProfile;
import com.cts.entity.Reservation;
import com.cts.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final BookingResponseMapper responseMapper;
    private final BookingPipelineMapper pipelineMapper;

    public Object toGenericBookingResponseDTO(Booking booking) {
        return responseMapper.toGenericBookingResponseDTO(booking);
    }

    public FlightBookingResponseDTO toFlightBookingResponse(Booking booking) {
        return responseMapper.toFlightBookingResponse(booking);
    }

    public HotelBookingResponseDTO toHotelBookingResponse(Booking booking) {
        return responseMapper.toHotelBookingResponse(booking);
    }

    public BusBookingResponseDTO toBusBookingResponse(Booking booking) {
        return responseMapper.toBusBookingResponse(booking);
    }

    public CabBookingResponseDTO toCabBookingResponse(Booking booking) {
        return responseMapper.toCabBookingResponse(booking);
    }

    public TourBookingResponseDTO toTourBookingResponse(Booking booking) {
        return responseMapper.toTourBookingResponse(booking);
    }

    public BookingResponseDTO toResponseDTO(Booking booking) {
        return responseMapper.toResponseDTO(booking);
    }

    public InvoiceResponseDTO toInvoiceResponseDTO(Invoice invoice) {
        return responseMapper.toInvoiceResponseDTO(invoice);
    }

    public Booking toFlightEntity(User customer, Inventory inventory, int seats, LocalDate travelDate,
                                  double cost, List<PassengerProfile> passengers) {
        return pipelineMapper.toFlightEntity(customer, inventory, seats, travelDate, cost, passengers);
    }

    public Booking toHotelEntity(User customer, Inventory inventory, int rooms, LocalDate checkIn,
                                 LocalDate checkOut, double cost, List<PassengerProfile> passengers) {
        return pipelineMapper.toHotelEntity(customer, inventory, rooms, checkIn, checkOut, cost, passengers);
    }

    public Booking toBusEntity(User customer, Inventory inventory, int seats, LocalDate travelDate,
                               String pickup, String dropoff, double cost, List<PassengerProfile> passengers) {
        return pipelineMapper.toBusEntity(customer, inventory, seats, travelDate, pickup, dropoff, cost, passengers);
    }

    public Booking toCabEntity(User customer, Inventory inventory, LocalDate travelDate,
                               String pickup, String dropoff, double cost, List<PassengerProfile> passengers) {
        return pipelineMapper.toCabEntity(customer, inventory, travelDate, pickup, dropoff, cost, passengers);
    }

    public Booking toTourEntity(User customer, Inventory inventory, int persons, LocalDate travelDate,
                                double cost, List<PassengerProfile> passengers) {
        return pipelineMapper.toTourEntity(customer, inventory, persons, travelDate, cost, passengers);
    }

    public Invoice toInvoiceEntity(Booking booking, double cost) {
        return pipelineMapper.toInvoiceEntity(booking, cost);
    }

    public Itinerary toNewItineraryEntity(User customer) {
        return pipelineMapper.toNewItineraryEntity(customer);
    }

    public Reservation toReservationEntity(Booking booking, String label, LocalDate start, LocalDate end) {
        return pipelineMapper.toReservationEntity(booking, label, start, end);
    }

    public Notification toNotificationEntity(User user, Long bookingId) {
        return pipelineMapper.toNotificationEntity(user, bookingId);
    }

    public KPIReport toKPIReportEntity(String label, double cost) {
        return pipelineMapper.toKPIReportEntity(label, cost);
    }
}

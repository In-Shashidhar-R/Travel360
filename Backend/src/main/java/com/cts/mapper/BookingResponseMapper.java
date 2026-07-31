package com.cts.mapper;

import com.cts.dto.BaseBookingResponseDTO;
import com.cts.dto.BookingResponseDTO;
import com.cts.dto.BusBookingResponseDTO;
import com.cts.dto.CabBookingResponseDTO;
import com.cts.dto.FlightBookingResponseDTO;
import com.cts.dto.HotelBookingResponseDTO;
import com.cts.dto.InvoiceResponseDTO;
import com.cts.dto.PassengerSnapshotDTO;
import com.cts.dto.TourBookingResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingResponseMapper {

    public Object toGenericBookingResponseDTO(Booking entity) {
        if (entity == null) {
            return null;
        }
        return switch (entity.getInventory().getItemType()) {
            case FLIGHT -> toFlightBookingResponse(entity);
            case HOTEL -> toHotelBookingResponse(entity);
            case BUS -> toBusBookingResponse(entity);
            case CAB -> toCabBookingResponse(entity);
            case TOUR_PACKAGE -> toTourBookingResponse(entity);
        };
    }

    public FlightBookingResponseDTO toFlightBookingResponse(Booking entity) {
        FlightBookingResponseDTO dto = new FlightBookingResponseDTO();
        mapCommonBaselines(entity, dto);
        dto.setRequestedSeats(entity.getRequestedSeats());
        dto.setTargetTravelDate(entity.getTargetTravelDate());
        dto.setPickupLocation(entity.getPickupLocation());
        dto.setDropoffLocation(entity.getDropoffLocation());
        dto.setChosenSeatType(entity.getChosenSeatType());
        return dto;
    }

    public HotelBookingResponseDTO toHotelBookingResponse(Booking entity) {
        HotelBookingResponseDTO dto = new HotelBookingResponseDTO();
        mapCommonBaselines(entity, dto);
        dto.setRequestedRooms(entity.getRequestedSeats());
        dto.setCheckInDate(entity.getCheckInDate());
        dto.setCheckOutDate(entity.getCheckOutDate());
        return dto;
    }

    public BusBookingResponseDTO toBusBookingResponse(Booking entity) {
        BusBookingResponseDTO dto = new BusBookingResponseDTO();
        mapCommonBaselines(entity, dto);
        dto.setRequestedSeats(entity.getRequestedSeats());
        dto.setTargetTravelDate(entity.getTargetTravelDate());
        dto.setPickupLocation(entity.getPickupLocation());
        dto.setDropoffLocation(entity.getDropoffLocation());
        dto.setChosenSeatType(entity.getChosenSeatType());
        return dto;
    }

    public CabBookingResponseDTO toCabBookingResponse(Booking entity) {
        CabBookingResponseDTO dto = new CabBookingResponseDTO();
        mapCommonBaselines(entity, dto);
        dto.setTargetTravelDate(entity.getTargetTravelDate());
        dto.setPickupLocation(entity.getPickupLocation());
        dto.setDropoffLocation(entity.getDropoffLocation());
        return dto;
    }

    public TourBookingResponseDTO toTourBookingResponse(Booking entity) {
        TourBookingResponseDTO dto = new TourBookingResponseDTO();
        mapCommonBaselines(entity, dto);
        dto.setNumberOfPersons(entity.getNumberOfPersons());
        dto.setTargetTravelDate(entity.getTargetTravelDate());
        return dto;
    }

    public BookingResponseDTO toResponseDTO(Booking entity) {
        if (entity == null) {
            return null;
        }
        BookingResponseDTO dto = new BookingResponseDTO();
        mapCommonBaselines(entity, dto);
        dto.setRequestedSeats(entity.getRequestedSeats());
        dto.setPickupLocation(entity.getPickupLocation());
        dto.setDropoffLocation(entity.getDropoffLocation());
        dto.setNumberOfPersons(entity.getNumberOfPersons());
        dto.setCheckInDate(entity.getCheckInDate());
        dto.setCheckOutDate(entity.getCheckOutDate());
        dto.setChosenSeatType(entity.getChosenSeatType());
        return dto;
    }

    public InvoiceResponseDTO toInvoiceResponseDTO(Invoice entity) {
        if (entity == null) {
            return null;
        }
        return InvoiceResponseDTO.builder()
                .invoiceId(entity.getInvoiceId())
                .bookingId(entity.getBooking().getBookingId())
                .customerName(entity.getBooking().getCustomer().getName())
                .amount(entity.getAmount())
                .generatedDate(entity.getGeneratedDate())
                .status(entity.getStatus().name())
                .build();
    }

    private void mapCommonBaselines(Booking source, BaseBookingResponseDTO target) {
        target.setBookingId(source.getBookingId());
        target.setCustomerId(source.getCustomer().getUserId());
        target.setCustomerName(source.getCustomer().getName());
        target.setPartnerId(source.getPartner().getPartnerId());
        target.setPartnerName(source.getPartner().getName());
        target.setInventoryId(source.getInventory().getInventoryId());
        target.setItemType(source.getInventory().getItemType().name());
        target.setBookingDate(source.getBookingDate());
        target.setStatus(source.getStatus().name());
        target.setTotalAmount(source.getTotalAmount());
        target.setPassengers(extractPassengerSnapshots(source));
    }

    private List<PassengerSnapshotDTO> extractPassengerSnapshots(Booking entity) {
        if (entity.getPassengerProfiles() == null) {
            return Collections.emptyList();
        }
        return entity.getPassengerProfiles().stream().map(p -> {
            PassengerSnapshotDTO s = new PassengerSnapshotDTO();
            s.setName(p.getName());
            s.setAge(p.getAge());
            s.setGender(p.getGender());
            s.setIdProofType(p.getIdProofType() != null ? p.getIdProofType().name() : "UNKNOWN");
            s.setIdProofNumber(p.getIdProofNumber());
            return s;
        }).collect(Collectors.toList());
    }
}

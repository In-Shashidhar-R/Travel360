package com.cts.mapper;

import com.cts.entity.Booking;
import com.cts.entity.Inventory;
import com.cts.entity.Invoice;
import com.cts.entity.Itinerary;
import com.cts.entity.KPIReport;
import com.cts.entity.Notification;
import com.cts.entity.PassengerProfile;
import com.cts.entity.Reservation;
import com.cts.entity.User;
import com.cts.enumeration.NotificationCategory;
import com.cts.enumeration.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookingPipelineMapper {

    public Booking toFlightEntity(User customer, Inventory inv, int seats, LocalDate date,
                                  double cost, List<PassengerProfile> profiles) {
        Booking booking = buildBase(customer, inv, seats, date, cost);
        addProfiles(booking, profiles);
        return booking;
    }

    public Booking toHotelEntity(User customer, Inventory inv, int rooms, LocalDate checkIn,
                                 LocalDate checkOut, double cost, List<PassengerProfile> profiles) {
        Booking booking = buildBase(customer, inv, rooms, checkIn, cost);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        addProfiles(booking, profiles);
        return booking;
    }

    public Booking toBusEntity(User customer, Inventory inv, int seats, LocalDate date,
                               String pickup, String dropoff, double cost, List<PassengerProfile> profiles) {
        Booking booking = buildBase(customer, inv, seats, date, cost);
        booking.setPickupLocation(pickup);
        booking.setDropoffLocation(dropoff);
        addProfiles(booking, profiles);
        return booking;
    }

    public Booking toCabEntity(User customer, Inventory inv, LocalDate date,
                               String pickup, String dropoff, double cost, List<PassengerProfile> profiles) {
        Booking booking = buildBase(customer, inv, 1, date, cost);
        booking.setPickupLocation(pickup);
        booking.setDropoffLocation(dropoff);
        addProfiles(booking, profiles);
        return booking;
    }

    public Booking toTourEntity(User customer, Inventory inv, int persons, LocalDate date,
                                double cost, List<PassengerProfile> profiles) {
        Booking booking = buildBase(customer, inv, persons, date, cost);
        booking.setNumberOfPersons(persons);
        addProfiles(booking, profiles);
        return booking;
    }

    public Invoice toInvoiceEntity(Booking booking, double cost) {
        return Invoice.builder()
                .booking(booking).amount(cost).generatedDate(LocalDate.now()).status(Status.UNPAID).build();
    }

    public Itinerary toNewItineraryEntity(User customer) {
        return Itinerary.builder()
                .customer(customer).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(2))
                .status(Status.ACTIVE).build();
    }

    public Reservation toReservationEntity(Booking booking, String label, LocalDate start, LocalDate end) {
        return Reservation.builder()
                .booking(booking).details("Automated hold registered: " + label)
                .startDate(start).endDate(end).status(Status.PENDING).build();
    }

    public Notification toNotificationEntity(User user, Long bookingId) {
        return Notification.builder()
                .user(user).message("Booking pipeline initialized successfully. ID #" + bookingId)
                .category(NotificationCategory.BOOKING_ALERT).status(Status.ACTIVE)
                .createdDate(LocalDateTime.now()).build();
    }

    public KPIReport toKPIReportEntity(String label, double cost) {
        return KPIReport.builder()
                .scope("SALES_VOLUME_METRIC").metrics("Processed " + label + " index: " + cost)
                .generatedDate(LocalDate.now()).build();
    }

    private Booking buildBase(User customer, Inventory inv, int seats, LocalDate date, double cost) {
        return Booking.builder()
                .customer(customer).partner(inv.getPartner()).inventory(inv).requestedSeats(seats)
                .bookingDate(LocalDate.now()).targetTravelDate(date).status(Status.PENDING).totalAmount(cost)
                .passengerProfiles(new ArrayList<>()).build();
    }

    private void addProfiles(Booking booking, List<PassengerProfile> profiles) {
        if (profiles != null && !profiles.isEmpty()) {
            booking.getPassengerProfiles().addAll(profiles);
        }
    }
}

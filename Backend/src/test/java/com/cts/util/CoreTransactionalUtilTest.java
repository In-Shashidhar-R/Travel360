package com.cts.util;

import com.cts.dto.InvoiceCancelResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.FlightInventory;
import com.cts.entity.Invoice;
import com.cts.entity.PassengerProfile;
import com.cts.entity.Payment;
import com.cts.entity.Reservation;
import com.cts.entity.User;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.PaymentMethod;
import com.cts.enumeration.PaymentType;
import com.cts.enumeration.Role;
import com.cts.enumeration.Status;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.InvalidTimelineException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.ItineraryRepository;
import com.cts.repository.KPIReportRepository;
import com.cts.repository.NotificationRepository;
import com.cts.repository.PartnerRepository;
import com.cts.repository.PaymentRepository;
import com.cts.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreTransactionalUtilTest {

    @Mock private PartnerRepository partnerRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ItineraryRepository itineraryRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private KPIReportRepository kpiReportRepository;
    @Mock private BookingMapper bookingMapper;

    @InjectMocks
    private CoreTransactionalUtil coreUtil;

    private User customer;
    private FlightInventory flight;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .userId(1L).name("Cust").role(Role.CUSTOMER)
                .email("c@c.com").password("Secret1!").build();
        flight = FlightInventory.builder()
                .flightNumber("AI-1").airlineName("TestAir")
                .departureAirport("MAA").arrivalAirport("DEL")
                .isConnecting(false).startTime("10:00").endTime("12:00").numberOfHours(2)
                .seatTiers(List.of()).inventoryId(1L)
                .itemType(InventoryType.FLIGHT).basePricePerUnit(1000.0).status(Status.ACTIVE)
                .build();

        // Common stubs (lenient so partial/full tests only touch what they use).
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(a -> a.getArgument(0));
        lenient().when(invoiceRepository.save(any(Invoice.class))).thenAnswer(a -> {
            Invoice i = a.getArgument(0);
            if (i.getInvoiceId() == null) {
                i.setInvoiceId(99L);
            }
            return i;
        });
        lenient().when(bookingMapper.toKPIReportEntity(anyString(), anyDouble())).thenReturn(null);
    }

    private PassengerProfile passenger(long id) {
        return PassengerProfile.builder().profileId(id).customer(customer)
                .name("P" + id).age(30).gender("M").idProofNumber("ID" + id).build();
    }

    private Booking confirmedBooking(double total, int gapDays, int passengers) {
        List<PassengerProfile> profiles = new ArrayList<>();
        for (int i = 1; i <= passengers; i++) {
            profiles.add(passenger(i));
        }
        return Booking.builder()
                .bookingId(50L).customer(customer).partner(null).inventory(flight)
                .status(Status.CONFIRMED).totalAmount(total)
                .targetTravelDate(LocalDate.now().plusDays(gapDays))
                .requestedSeats(passengers)
                .passengerProfiles(profiles)
                .build();
    }

    private Invoice paidInvoice(Booking booking, double amount) {
        return Invoice.builder().invoiceId(10L).booking(booking).amount(amount)
                .generatedDate(LocalDate.now()).status(Status.PAID).build();
    }

    private Payment payment(Invoice inv, double amount) {
        return Payment.builder().paymentId(1L).invoice(inv).amount(amount)
                .paymentDate(LocalDate.now()).method(PaymentMethod.CREDIT_CARD)
                .paymentType(PaymentType.CREDIT).status(Status.PAID).build();
    }

    @Test
    @DisplayName("Partial cancellation: fee = cancelledValue * farRate, refund = remainder, total reduced")
    void partialCancellationFeeMath() {
        Booking booking = confirmedBooking(4000.0, 20, 4); 
        Invoice paid = paidInvoice(booking, 4000.0);
        when(invoiceRepository.findByBookingAndStatus(booking, Status.PAID)).thenReturn(Optional.of(paid));
        when(paymentRepository.findByInvoice(paid)).thenReturn(List.of(payment(paid, 4000.0)));

        InvoiceCancelResponseDTO result =
                coreUtil.executeCancellationPipeline(booking, 1L, List.of(1L));
        assertEquals(1, result.getPassengersCancelledCount());
        assertEquals(200.0, result.getCancellationFeeApplied(), 0.001);
        assertEquals(800.0, result.getRefundAmount(), 0.001);
        assertEquals(3000.0, result.getUpdatedNewBookingTotal(), 0.001);
        assertEquals("REFUNDED", result.getStatus());
        assertEquals(3, booking.getPassengerProfiles().size());
        assertEquals(3, booking.getRequestedSeats());
    }

    @Test
    @DisplayName("Full cancellation: entire value refunded minus fee, booking marked CANCELLED, total zeroed")
    void fullCancellationFeeMath() {
        Booking booking = confirmedBooking(4000.0, 20, 4); // gap 20 => FAR rate 0.20
        Invoice paid = paidInvoice(booking, 4000.0);
        when(invoiceRepository.findByBookingAndStatus(booking, Status.PAID)).thenReturn(Optional.of(paid));
        when(paymentRepository.findByInvoice(paid)).thenReturn(List.of(payment(paid, 4000.0)));
        Reservation res = Reservation.builder().reservationId(5L).booking(booking)
                .details("hold").startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1))
                .status(Status.PENDING).build();
        when(reservationRepository.findByBooking(booking)).thenReturn(List.of(res));

        InvoiceCancelResponseDTO result = coreUtil.executeFullCancellationPipeline(booking, 1L);

        assertEquals(4, result.getPassengersCancelledCount());
        assertEquals(800.0, result.getCancellationFeeApplied(), 0.001);
        assertEquals(3200.0, result.getRefundAmount(), 0.001);
        assertEquals(0.0, result.getUpdatedNewBookingTotal(), 0.001);
        assertEquals(Status.CANCELLED, booking.getStatus());
        assertEquals(0.0, booking.getTotalAmount(), 0.001);
        assertEquals(0, booking.getRequestedSeats());
        assertEquals(Status.CANCELLED, res.getStatus());
    }

    @Test
    @DisplayName("Cancellation by a non-owner is rejected")
    void cancellationByNonOwnerRejected() {
        Booking booking = confirmedBooking(4000.0, 20, 4);
        assertThrows(DataIsolationViolationException.class,
                () -> coreUtil.executeCancellationPipeline(booking, 999L, List.of(1L)));
    }

    @Test
    @DisplayName("Cancellation of a non-CONFIRMED booking is rejected")
    void cancellationOfNonConfirmedRejected() {
        Booking booking = confirmedBooking(4000.0, 20, 4);
        booking.setStatus(Status.PENDING);
        assertThrows(InvalidTimelineException.class,
                () -> coreUtil.executeFullCancellationPipeline(booking, 1L));
    }
}

package com.cts.util;

import com.cts.dto.PaymentResponseDTO;
import com.cts.entity.*;
import com.cts.enumeration.PaymentMethod;
import com.cts.enumeration.PaymentType;
import com.cts.enumeration.Status;
import com.cts.exception.IdentityConflictException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.*;
import com.cts.service.AuditLogWorker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentProcessingUtilTest {

    @Mock InvoiceRepository invoiceRepository;
    @Mock BookingRepository bookingRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock ComplianceReportRepository complianceReportRepository;
    @Mock AuditLogWorker auditLogWorker;
    @InjectMocks PaymentProcessingUtil util;

    @Test
    void fetchUnpaidInvoice_missing_throws() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> util.fetchUnpaidInvoice(1L));
    }

    @Test
    void fetchUnpaidInvoice_alreadyPaid_throwsConflict() {
        Invoice inv = Invoice.builder().invoiceId(1L).status(Status.PAID).build();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));
        assertThrows(IdentityConflictException.class, () -> util.fetchUnpaidInvoice(1L));
    }

    @Test
    void fetchUnpaidInvoice_unpaid_returns() {
        Invoice inv = Invoice.builder().invoiceId(1L).status(Status.UNPAID).build();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));
        assertSame(inv, util.fetchUnpaidInvoice(1L));
    }

    // ----- synchronizePaymentState -----------------------------------------
    @Test
    void synchronizePaymentState_marksAllConfirmedAndSaves() {
        User customer = User.builder().userId(1L).build();
        Booking booking = Booking.builder().bookingId(10L).customer(customer).status(Status.PENDING).build();
        Invoice invoice = Invoice.builder().invoiceId(5L).booking(booking).status(Status.UNPAID).build();
        Reservation r1 = Reservation.builder().booking(booking).status(Status.PENDING).build();
        Reservation r2 = Reservation.builder().booking(booking).status(Status.PENDING).build();
        when(reservationRepository.findByBooking(booking)).thenReturn(List.of(r1, r2));

        util.synchronizePaymentState(invoice);

        assertEquals(Status.PAID, invoice.getStatus());
        assertEquals(Status.CONFIRMED, booking.getStatus());
        assertEquals(Status.CONFIRMED, r1.getStatus());
        assertEquals(Status.CONFIRMED, r2.getStatus());
        verify(invoiceRepository).save(invoice);
        verify(bookingRepository).save(booking);
        verify(reservationRepository, times(2)).save(any(Reservation.class));
    }

    // ----- processFinalSettlement ------------------------------------------
    @Test
    void processFinalSettlement_buildsPaymentNotificationComplianceAndAudit() {
        User customer = User.builder().userId(1L).build();
        Booking booking = Booking.builder().bookingId(10L).customer(customer).build();
        Invoice invoice = Invoice.builder().invoiceId(5L).booking(booking).amount(500.0).build();

        Payment savedPayment = Payment.builder()
                .paymentId(100L).invoice(invoice).amount(500.0)
                .method(PaymentMethod.UPI).status(Status.PAID).build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        util.processFinalSettlement(invoice, "upi");

        verify(paymentRepository).save(any(Payment.class));
        verify(notificationRepository).save(any(Notification.class));
        verify(complianceReportRepository).save(any(ComplianceReport.class));
        verify(auditLogWorker).logAsyncAction(
                eq(customer), eq("INVOICE_SETTLEMENT_CLEARANCE_SUCCESS"),
                eq("PAYMENT"), any(), anyString());
    }

    // ----- toPaymentResponseDTO --------------------------------------------
    @Test
    void toPaymentResponseDTO_copiesAllFields() {
        Invoice invoice = Invoice.builder().invoiceId(5L).build();
        Payment payment = Payment.builder()
                .paymentId(1L).invoice(invoice).amount(750.0)
                .paymentDate(LocalDate.of(2026, 6, 1))
                .paymentType(PaymentType.CREDIT).method(PaymentMethod.CREDIT_CARD).status(Status.PAID).build();

        PaymentResponseDTO dto = util.toPaymentResponseDTO(payment);
        assertEquals(1L, dto.getPaymentId());
        assertEquals(5L, dto.getInvoiceId());
        assertEquals(750.0, dto.getAmount());
        assertEquals("CREDIT_CARD", dto.getMethod());
        assertEquals("PAID", dto.getStatus());
    }
}

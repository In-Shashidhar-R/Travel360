package com.cts.util;

import com.cts.dto.PaymentResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.ComplianceReport;
import com.cts.entity.Invoice;
import com.cts.entity.Notification;
import com.cts.entity.Payment;
import com.cts.entity.Reservation;
import com.cts.enumeration.NotificationCategory;
import com.cts.enumeration.PaymentMethod;
import com.cts.enumeration.PaymentType;
import com.cts.enumeration.Status;
import com.cts.exception.IdentityConflictException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.ComplianceReportRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.NotificationRepository;
import com.cts.repository.PaymentRepository;
import com.cts.repository.ReservationRepository;
import com.cts.service.AuditLogWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentProcessingUtil {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final ComplianceReportRepository complianceReportRepository;
    private final AuditLogWorker auditLogWorker;

    public Invoice fetchUnpaidInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Target Invoice accounting entry not found with ID: " + id));
        if (Status.PAID.equals(invoice.getStatus())) {
            throw new IdentityConflictException("Idempotency violation protection: Invoice balance already cleared.");
        }
        return invoice;
    }

    public void synchronizePaymentState(Invoice invoice) {
        invoice.setStatus(Status.PAID);
        invoiceRepository.save(invoice);

        Booking booking = invoice.getBooking();
        booking.setStatus(Status.CONFIRMED);
        bookingRepository.save(booking);

        List<Reservation> linkedReservations = reservationRepository.findByBooking(booking);
        for (Reservation res : linkedReservations) {
            res.setStatus(Status.CONFIRMED);
            reservationRepository.save(res);
        }
    }

    public void processFinalSettlement(Invoice invoice, String rawMethod) {
        Booking booking = invoice.getBooking();
        PaymentMethod method = PaymentMethod.valueOf(rawMethod.toUpperCase());

        Payment payment = paymentRepository.save(Payment.builder()
                .invoice(invoice)
                .amount(invoice.getAmount()) 
                .paymentDate(LocalDate.now())
                .paymentType(PaymentType.CREDIT)
                .method(method)
                .status(Status.PAID)
                .build());

        notificationRepository.save(Notification.builder()
                .user(booking.getCustomer())
                .message("Financial ledger confirmation complete. Your reservations are now secured.")
                .category(NotificationCategory.PAYMENT_ALERT)
                .status(Status.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build());

        complianceReportRepository.save(ComplianceReport.builder()
                .scope("GDPR_FINANCIAL_SETTLEMENT_RECORD")
                .metrics("Financial clearance confirmed for customer tracking key index ID: " + booking.getCustomer().getUserId())
                .generatedDate(LocalDate.now())
                .build());
        
        auditLogWorker.logAsyncAction( 
                booking.getCustomer(),
                "INVOICE_SETTLEMENT_CLEARANCE_SUCCESS",
                "PAYMENT",
                payment.getPaymentId(),
                String.format("Balance settlement of %.2f cleared dynamically via %s interface mapping strategy for Invoice ID #%d.", 
                        invoice.getAmount(), payment.getMethod().name(), invoice.getInvoiceId())
        );
    }

    public PaymentResponseDTO toPaymentResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .invoiceId(payment.getInvoice().getInvoiceId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .paymentDate(payment.getPaymentDate())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .build();
    }
}
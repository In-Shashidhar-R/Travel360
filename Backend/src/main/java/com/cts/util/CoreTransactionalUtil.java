package com.cts.util;

import com.cts.dto.InvoiceCancelResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.entity.Itinerary;
import com.cts.entity.Notification;
import com.cts.entity.Partner;
import com.cts.entity.Payment;
import com.cts.enumeration.NotificationCategory;
import com.cts.enumeration.PaymentType;
import com.cts.enumeration.Status;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.IdentityConflictException;
import com.cts.exception.InvalidTimelineException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.ItineraryRepository;
import com.cts.repository.KPIReportRepository;
import com.cts.repository.NotificationRepository;
import com.cts.repository.PartnerRepository;
import com.cts.repository.PaymentRepository;
import com.cts.repository.BookingRepository;
import com.cts.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreTransactionalUtil {

    private final PartnerRepository partnerRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ItineraryRepository itineraryRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final KPIReportRepository kpiReportRepository;
    private final BookingMapper bookingMapper;

    // --- Lookups ---

    public Partner fetchPartner(Long partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + partnerId));
    }

    public Invoice fetchInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
    }

    public Booking fetchBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }

    // --- Booking creation pipeline ---

    public Booking executeBookingPipeline(Booking booking, double cost, LocalDate targetDate, String label) {
        Booking saved = bookingRepository.save(booking);
        invoiceRepository.save(bookingMapper.toInvoiceEntity(saved, cost));

        Itinerary itinerary = itineraryRepository.findByCustomer(saved.getCustomer())
                .orElseGet(() -> bookingMapper.toNewItineraryEntity(saved.getCustomer()));
        itinerary.getBookings().add(saved);
        itineraryRepository.save(itinerary);

        reservationRepository.save(bookingMapper.toReservationEntity(saved, label, targetDate, targetDate.plusDays(1)));
        notificationRepository.save(bookingMapper.toNotificationEntity(saved.getCustomer(), saved.getBookingId()));
        kpiReportRepository.save(bookingMapper.toKPIReportEntity(label, cost));

        log.info("Booking #{} ({}) created for customer #{} at amount {}",
                saved.getBookingId(), label, saved.getCustomer().getUserId(), cost);
        return saved;
    }

    // --- Partial cancellation ---

    public InvoiceCancelResponseDTO executeCancellationPipeline(Booking booking, Long customerId, List<Long> cancelIds) {
        validateCancellable(booking, customerId);

        int total = booking.getPassengerProfiles().size();
        int cancelCount = cancelIds.size();
        if (cancelCount <= 0) {
            throw new InvalidTimelineException("At least one passenger must be selected for cancellation.");
        }
        if (cancelCount >= total) {
            throw new InvalidTimelineException("To cancel every passenger use the full-cancellation endpoint.");
        }

        double cancelledValue = (booking.getTotalAmount() / total) * cancelCount;
        double penaltyRate = penaltyRateFor(booking.getTargetTravelDate());
        double fee = cancelledValue * penaltyRate;
        double refund = Math.max(0.0, cancelledValue - fee);

        booking.getPassengerProfiles().removeIf(p -> cancelIds.contains(p.getProfileId()));
        booking.setRequestedSeats(booking.getRequestedSeats() - cancelCount);
        booking.setTotalAmount(booking.getTotalAmount() - cancelledValue);
        bookingRepository.save(booking);

        Invoice refundInvoice = persistRefund(booking, refund, "Partial cancellation");
        log.info("Partial cancellation on booking #{}: {} passenger(s), refund {}",
                booking.getBookingId(), cancelCount, refund);

        return buildCancelResponse(refundInvoice, booking, cancelCount, fee, refund, booking.getTotalAmount());
    }

    // --- Full cancellation ---

    public InvoiceCancelResponseDTO executeFullCancellationPipeline(Booking booking, Long customerId) {
        validateCancellable(booking, customerId);

        int cancelledCount = booking.getPassengerProfiles().size();
        double cancelledValue = booking.getTotalAmount();
        double penaltyRate = penaltyRateFor(booking.getTargetTravelDate());
        double fee = cancelledValue * penaltyRate;
        double refund = Math.max(0.0, cancelledValue - fee);

        booking.setStatus(Status.CANCELLED);
        booking.setRequestedSeats(0);
        booking.setTotalAmount(0.0);
        bookingRepository.save(booking);

        markReservationsCancelled(booking);

        Invoice refundInvoice = persistRefund(booking, refund, "Full cancellation");
        log.info("Full cancellation on booking #{}: refund {}", booking.getBookingId(), refund);

        return buildCancelResponse(refundInvoice, booking, cancelledCount, fee, refund, 0.0);
    }

    // --- Partner provisioning ---

    public void validateNewPartnerEmail(String email) {
        if (partnerRepository.existsByEmail(email)) {
            throw new IdentityConflictException("A partner with this email already exists.");
        }
    }

    public Partner commitPartner(Partner partner) {
        return partnerRepository.save(partner);
    }

    // --- Internal helpers ---

    private void validateCancellable(Booking booking, Long customerId) {
        if (!booking.getCustomer().getUserId().equals(customerId)) {
            throw new DataIsolationViolationException("This booking does not belong to the requesting customer.");
        }
        if (booking.getStatus() != Status.CONFIRMED) {
            throw new InvalidTimelineException("Only CONFIRMED bookings can be cancelled.");
        }
        if (booking.getTargetTravelDate() != null && LocalDate.now().isAfter(booking.getTargetTravelDate())) {
            throw new InvalidTimelineException("The travel window for this booking has already closed.");
        }
    }

    private double penaltyRateFor(LocalDate targetTravelDate) {
        long gap = ChronoUnit.DAYS.between(LocalDate.now(), targetTravelDate);
        if (gap < AppConstants.PENALTY_NEAR_DAYS) {
            return AppConstants.PENALTY_RATE_NEAR;
        }
        if (gap < AppConstants.PENALTY_MID_DAYS) {
            return AppConstants.PENALTY_RATE_MID;
        }
        return AppConstants.PENALTY_RATE_FAR;
    }

    private Invoice persistRefund(Booking booking, double refund, String reason) {
        Invoice paidInvoice = invoiceRepository.findByBookingAndStatus(booking, Status.PAID)
                .orElseThrow(() -> new ResourceNotFoundException("No settled invoice found for this booking."));

        Invoice refundInvoice = invoiceRepository.save(Invoice.builder()
                .booking(booking)
                .amount(refund)
                .generatedDate(LocalDate.now())
                .status(Status.REFUNDED)
                .build());

        if (refund > 0) {
            Payment originalPayment = paymentRepository.findByInvoice(paidInvoice).stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Original payment record not found."));
            paymentRepository.save(Payment.builder()
                    .invoice(refundInvoice)
                    .amount(refund)
                    .paymentDate(LocalDate.now())
                    .method(originalPayment.getMethod())
                    .paymentType(PaymentType.DEBIT)
                    .status(Status.PAID)
                    .build());
        }

        notificationRepository.save(Notification.builder()
                .user(booking.getCustomer())
                .message(reason + " complete. Refund of " + refund + " has been processed.")
                .category(NotificationCategory.BOOKING_ALERT)
                .status(Status.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build());

        kpiReportRepository.save(bookingMapper.toKPIReportEntity(
                "CANCEL_" + booking.getInventory().getItemType(), -refund));

        return refundInvoice;
    }

    private void markReservationsCancelled(Booking booking) {
        reservationRepository.findByBooking(booking).forEach(reservation -> {
            reservation.setStatus(Status.CANCELLED);
            reservationRepository.save(reservation);
        });
    }

    private InvoiceCancelResponseDTO buildCancelResponse(Invoice refundInvoice, Booking booking, int cancelledCount,
                                                         double fee, double refund, double updatedTotal) {
        return InvoiceCancelResponseDTO.builder()
                .invoiceId(refundInvoice.getInvoiceId())
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomer().getUserId())
                .customerName(booking.getCustomer().getName())
                .passengersCancelledCount(cancelledCount)
                .cancellationFeeApplied(fee)
                .refundAmount(refund)
                .updatedNewBookingTotal(updatedTotal)
                .generatedDate(refundInvoice.getGeneratedDate())
                .status(refundInvoice.getStatus().name())
                .build();
    }
}

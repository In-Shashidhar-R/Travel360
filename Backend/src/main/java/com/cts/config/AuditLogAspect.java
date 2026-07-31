package com.cts.config;

import com.cts.dto.BaseInventoryResponseDTO;
import com.cts.dto.BusBookingResponseDTO;
import com.cts.dto.CabBookingResponseDTO;
import com.cts.dto.FlightBookingResponseDTO;
import com.cts.dto.HotelBookingResponseDTO;
import com.cts.dto.InvoiceCancelResponseDTO;
import com.cts.dto.PaymentRequestDTO;
import com.cts.dto.TourBookingResponseDTO;
import com.cts.entity.AuditLog;
import com.cts.entity.Invoice;
import com.cts.entity.User;
import com.cts.enumeration.EventLevel;
import com.cts.repository.AuditLogRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.UserRepository;
import com.cts.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    @Pointcut("execution(* com.cts.serviceimpl.BookingServiceImpl.book*(..))")
    public void bookingMethods() { }

    @Pointcut("execution(* com.cts.serviceimpl.BookingServiceImpl.cancel*(..))")
    public void cancellationMethod() { }

    @Pointcut("execution(* com.cts.serviceimpl.PaymentServiceImpl.executePayment(..))")
    public void paymentExecutionMethod() { }

    /** Inventory provisioning methods (provisionFlight / provisionHotel / ...). */
    @Pointcut("execution(* com.cts.serviceimpl.InventoryServiceImpl.provision*(..))")
    public void inventoryProvisionMethods() { }

    // BOOKING SUCCESS
    @AfterReturning(pointcut = "bookingMethods()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditSuccessfulBookings(Object result) {
        if (result == null) {
            return;
        }

        Long customerId = null;
        Long bookingId = null;
        String type = "UNKNOWN";
        double cost = 0.0;
        String metadata = "";

        if (result instanceof FlightBookingResponseDTO flight) {
            customerId = flight.getCustomerId();
            bookingId = flight.getBookingId();
            type = "FLIGHT";
            cost = flight.getTotalAmount();
            metadata = String.format("Seats reserved: %d | Route: %s to %s",
                    flight.getRequestedSeats(), flight.getPickupLocation(), flight.getDropoffLocation());
        } else if (result instanceof HotelBookingResponseDTO hotel) {
            customerId = hotel.getCustomerId();
            bookingId = hotel.getBookingId();
            type = "HOTEL";
            cost = hotel.getTotalAmount();
            metadata = String.format("Rooms reserved: %d | Window: %s to %s",
                    hotel.getRequestedRooms(), hotel.getCheckInDate(), hotel.getCheckOutDate());
        } else if (result instanceof BusBookingResponseDTO bus) {
            customerId = bus.getCustomerId();
            bookingId = bus.getBookingId();
            type = "BUS";
            cost = bus.getTotalAmount();
            metadata = String.format("Seats reserved: %d | Route: %s to %s",
                    bus.getRequestedSeats(), bus.getPickupLocation(), bus.getDropoffLocation());
        } else if (result instanceof CabBookingResponseDTO cab) {
            customerId = cab.getCustomerId();
            bookingId = cab.getBookingId();
            type = "CAB";
            cost = cab.getTotalAmount();
            metadata = String.format("Transfer route: %s to %s", cab.getPickupLocation(), cab.getDropoffLocation());
        } else if (result instanceof TourBookingResponseDTO tour) {
            customerId = tour.getCustomerId();
            bookingId = tour.getBookingId();
            type = "TOUR_PACKAGE";
            cost = tour.getTotalAmount();
            metadata = String.format("Group size: %d travellers", tour.getNumberOfPersons());
        }

        if (customerId != null) {
            User customer = userRepository.findById(customerId).orElse(null);
            saveLog(customer, "BOOKING_CREATION_" + type, "BOOKING", bookingId,
                    String.format("Processed transaction for amount: %.2f. Details: %s", cost, metadata),
                    EventLevel.INFO);
            saveLog(customer, "INVOICE_GENERATION_UNPAID", "INVOICE", bookingId,
                    String.format("Auto-generated unpaid invoice for value: %.2f", cost),
                    EventLevel.INFO);
            log.info("Audit recorded for {} booking #{} (customer #{})", type, bookingId, customerId);
        }
    }

    @AfterThrowing(pointcut = "bookingMethods()", throwing = "ex")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditBookingFailure(JoinPoint joinPoint, Throwable ex) {
        User caller = currentUserOrNull();
        saveLog(caller, "BOOKING_CREATION_FAILED", "BOOKING", null,
                String.format("%s threw %s: %s", joinPoint.getSignature().toShortString(),
                              ex.getClass().getSimpleName(), ex.getMessage()),
                EventLevel.EXCEPTION);
    }

    // CANCELLATION SUCCESS
    @AfterReturning(pointcut = "cancellationMethod()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditSuccessfulCancellations(Object result) {
        if (!(result instanceof InvoiceCancelResponseDTO cancellation)) {
            return;
        }
        User customer = cancellation.getCustomerId() != null
                ? userRepository.findById(cancellation.getCustomerId()).orElse(null)
                : null;

        saveLog(customer, "CANCELLATION_REFUND_PROCESSED", "BOOKING", cancellation.getBookingId(),
                String.format("Cancelled %d passenger(s). Refund issued: %.2f (fee applied: %.2f). Updated booking total: %.2f.",
                        cancellation.getPassengersCancelledCount(), cancellation.getRefundAmount(),
                        cancellation.getCancellationFeeApplied(), cancellation.getUpdatedNewBookingTotal()),
                EventLevel.INFO);
    }

    @AfterThrowing(pointcut = "cancellationMethod()", throwing = "ex")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditCancellationFailure(JoinPoint joinPoint, Throwable ex) {
        saveLog(currentUserOrNull(), "CANCELLATION_FAILED", "BOOKING", null,
                String.format("%s threw %s: %s", joinPoint.getSignature().toShortString(),
                              ex.getClass().getSimpleName(), ex.getMessage()),
                EventLevel.EXCEPTION);
    }

    // PAYMENT SUCCESS
    @AfterReturning(pointcut = "paymentExecutionMethod()")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditSuccessfulPayments(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || !(args[0] instanceof PaymentRequestDTO request)) {
            return;
        }
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId()).orElse(null);
        if (invoice != null && invoice.getBooking() != null) {
            User customer = invoice.getBooking().getCustomer();
            saveLog(customer, "INVOICE_SETTLEMENT_CLEARANCE_SUCCESS", "PAYMENT", invoice.getInvoiceId(),
                    String.format("Balance of %.2f cleared via %s. Booking #%d is now CONFIRMED.",
                            invoice.getAmount(), request.getMethod().toUpperCase(), invoice.getBooking().getBookingId()),
                    EventLevel.INFO);
        }
    }

    @AfterThrowing(pointcut = "paymentExecutionMethod()", throwing = "ex")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditPaymentFailure(JoinPoint joinPoint, Throwable ex) {
        saveLog(currentUserOrNull(), "PAYMENT_FAILED", "PAYMENT", null,
                String.format("%s threw %s: %s", joinPoint.getSignature().toShortString(),
                              ex.getClass().getSimpleName(), ex.getMessage()),
                EventLevel.EXCEPTION);
    }

    // INVENTORY CREATION
    @AfterReturning(pointcut = "inventoryProvisionMethods()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditInventoryProvisioned(JoinPoint joinPoint, Object result) {
        if (!(result instanceof BaseInventoryResponseDTO base)) {
            return;
        }
        User actor = currentUserOrNull();
        saveLog(actor, "INVENTORY_CREATED_" + safe(base.getItemType()),
                "INVENTORY", base.getInventoryId(),
                String.format("Provisioned new %s inventory (partner #%s, base price %.2f)",
                        safe(base.getItemType()),
                        String.valueOf(base.getPartnerId()),
                        base.getBasePricePerSeat()),
                EventLevel.INFO);
        log.info("Audit recorded for inventory creation #{} ({})", base.getInventoryId(), base.getItemType());
    }

    @AfterThrowing(pointcut = "inventoryProvisionMethods()", throwing = "ex")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditInventoryProvisionFailure(JoinPoint joinPoint, Throwable ex) {
        saveLog(currentUserOrNull(), "INVENTORY_CREATION_FAILED", "INVENTORY", null,
                String.format("%s threw %s: %s", joinPoint.getSignature().toShortString(),
                              ex.getClass().getSimpleName(), ex.getMessage()),
                EventLevel.EXCEPTION);
    }

    private void saveLog(User user, String action, String resourceType, Long resourceId,
                         String details, EventLevel level) {
        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .eventLevel(level)
                .timestamp(LocalDateTime.now())
                .build());
    }


    private User currentUserOrNull() {
        try {
            Long id = SecurityUtil.getCurrentUserId();
            return (id == null) ? null : userRepository.findById(id).orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String safe(String s) { return s == null ? "UNKNOWN" : s; }
}

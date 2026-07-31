package com.cts.serviceimpl;

import com.cts.dto.AnalyticsDashboardDTO;
import com.cts.entity.*;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Role;
import com.cts.enumeration.Status;
import com.cts.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PartnerRepository partnerRepository;
    @Mock InventoryRepository inventoryRepository;
    @Mock BookingRepository bookingRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock PaymentRepository paymentRepository;
    @InjectMocks AnalyticsServiceImpl service;

    @Test
    void getDashboard_emptyData_returnsZeros() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(bookingRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(paymentRepository.findAll()).thenReturn(List.of());
        when(partnerRepository.count()).thenReturn(0L);
        when(inventoryRepository.count()).thenReturn(0L);

        AnalyticsDashboardDTO dto = service.getDashboard();
        assertEquals(0L, dto.getTotalUsers());
        assertEquals(0L, dto.getTotalCustomers());
        assertEquals(0L, dto.getTotalBookings());
        assertEquals(0.0, dto.getTotalRevenueCollected());
        assertTrue(dto.getBookingCountByInventoryType().isEmpty());
    }

    @Test
    void getDashboard_realData_aggregatesEverythingCorrectly() {
        User customer = User.builder().userId(1L).role(Role.CUSTOMER).build();
        User agent    = User.builder().userId(2L).role(Role.TRAVEL_AGENT).build();
        User admin    = User.builder().userId(3L).role(Role.ADMIN).build();
        when(userRepository.findAll()).thenReturn(List.of(customer, agent, admin));

        Booking b1 = Booking.builder().bookingId(1L).status(Status.CONFIRMED).totalAmount(500.0)
                .inventory(inv(InventoryType.FLIGHT)).build();
        Booking b2 = Booking.builder().bookingId(2L).status(Status.CONFIRMED).totalAmount(700.0)
                .inventory(inv(InventoryType.FLIGHT)).build();
        Booking b3 = Booking.builder().bookingId(3L).status(Status.CANCELLED).totalAmount(200.0)
                .inventory(inv(InventoryType.HOTEL)).build();
        Booking b4 = Booking.builder().bookingId(4L).status(Status.PENDING).totalAmount(150.0)
                .inventory(inv(InventoryType.BUS)).build();
        when(bookingRepository.findAll()).thenReturn(List.of(b1, b2, b3, b4));

        when(invoiceRepository.findAll()).thenReturn(List.of(
            invoice(Status.PAID, 500.0),
            invoice(Status.PAID, 700.0),
            invoice(Status.UNPAID, 150.0),
            invoice(Status.REFUNDED, 200.0)
        ));

        when(paymentRepository.findAll()).thenReturn(List.of(
            payment(Status.PAID, 500.0),
            payment(Status.PAID, 700.0),
            payment(Status.REFUNDED, 200.0)
        ));

        when(partnerRepository.count()).thenReturn(5L);
        when(inventoryRepository.count()).thenReturn(20L);

        AnalyticsDashboardDTO dto = service.getDashboard();

        assertEquals(3L, dto.getTotalUsers());
        assertEquals(1L, dto.getTotalCustomers());
        assertEquals(1L, dto.getTotalTravelAgents());
        assertEquals(5L, dto.getTotalPartners());
        assertEquals(20L, dto.getTotalInventoryItems());
        assertEquals(4L, dto.getTotalBookings());
        assertEquals(2L, dto.getConfirmedBookings());
        assertEquals(1L, dto.getCancelledBookings());
        assertEquals(1L, dto.getPendingBookings());
        assertEquals(4L, dto.getTotalInvoices());
        assertEquals(2L, dto.getPaidInvoices());
        assertEquals(1L, dto.getUnpaidInvoices());
        assertEquals(1L, dto.getRefundedInvoices());
        assertEquals(1200.0, dto.getTotalRevenueCollected());
        assertEquals(200.0, dto.getTotalRefundsIssued());
        assertEquals(2L, dto.getBookingCountByInventoryType().get("FLIGHT"));
        assertEquals(1L, dto.getBookingCountByInventoryType().get("HOTEL"));
        assertEquals(1L, dto.getBookingCountByInventoryType().get("BUS"));
    }

    @Test
    void getDashboard_bookingWithNullInventory_isSkippedInTypeMap() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(bookingRepository.findAll()).thenReturn(List.of(
            Booking.builder().bookingId(1L).status(Status.CONFIRMED).build()  // no inventory
        ));
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(paymentRepository.findAll()).thenReturn(List.of());

        AnalyticsDashboardDTO dto = service.getDashboard();
        assertEquals(1L, dto.getTotalBookings());
        assertTrue(dto.getBookingCountByInventoryType().isEmpty());
    }

    private Inventory inv(InventoryType type) {
        FlightInventory i = FlightInventory.builder().build();
        i.setItemType(type);
        return i;
    }

    private Invoice invoice(Status status, double amount) {
        return Invoice.builder().status(status).amount(amount).build();
    }

    private Payment payment(Status status, double amount) {
        return Payment.builder().status(status).amount(amount).build();
    }
}

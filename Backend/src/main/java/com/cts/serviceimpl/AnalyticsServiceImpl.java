package com.cts.serviceimpl;

import com.cts.dto.AnalyticsDashboardDTO;
import com.cts.entity.*;
import com.cts.enumeration.Role;
import com.cts.enumeration.Status;
import com.cts.repository.*;
import com.cts.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDashboardDTO getDashboard() {
        List<User> users = userRepository.findAll();
        long customers = users.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
        long agents    = users.stream().filter(u -> u.getRole() == Role.TRAVEL_AGENT).count();

        List<Booking> bookings = bookingRepository.findAll();
        long confirmed = bookings.stream().filter(b -> b.getStatus() == Status.CONFIRMED).count();
        long cancelled = bookings.stream().filter(b -> b.getStatus() == Status.CANCELLED).count();
        long pending   = bookings.stream().filter(b -> b.getStatus() == Status.PENDING).count();

        Map<String, Long> byType = bookings.stream()
                .filter(b -> b.getInventory() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getInventory().getItemType().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Invoice> invoices = invoiceRepository.findAll();
        long paid     = invoices.stream().filter(i -> i.getStatus() == Status.PAID).count();
        long unpaid   = invoices.stream().filter(i -> i.getStatus() == Status.UNPAID).count();
        long refunded = invoices.stream().filter(i -> i.getStatus() == Status.REFUNDED).count();

        double revenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Status.PAID)
                .mapToDouble(Payment::getAmount)
                .sum();

        double refundsIssued = invoices.stream()
                .filter(i -> i.getStatus() == Status.REFUNDED)
                .mapToDouble(Invoice::getAmount)
                .sum();

        return AnalyticsDashboardDTO.builder()
                .totalUsers(users.size())
                .totalCustomers(customers)
                .totalTravelAgents(agents)
                .totalPartners(partnerRepository.count())
                .totalInventoryItems(inventoryRepository.count())
                .totalBookings(bookings.size())
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .pendingBookings(pending)
                .totalInvoices(invoices.size())
                .paidInvoices(paid)
                .unpaidInvoices(unpaid)
                .refundedInvoices(refunded)
                .totalRevenueCollected(revenue)
                .totalRefundsIssued(refundsIssued)
                .bookingCountByInventoryType(byType)
                .build();
    }
}

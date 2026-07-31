package com.cts.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;


@Data
@Builder
public class AnalyticsDashboardDTO {

    private long totalUsers;
    private long totalCustomers;
    private long totalTravelAgents;
    private long totalPartners;
    private long totalInventoryItems;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long pendingBookings;
    private long totalInvoices;
    private long paidInvoices;
    private long unpaidInvoices;
    private long refundedInvoices;
    private double totalRevenueCollected;     
    private double totalRefundsIssued;        
    private Map<String, Long> bookingCountByInventoryType;
}

package com.cts.entity;

import com.cts.enumeration.BookingRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private User customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_user_id", nullable = false)
    private User assignedAgent;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String customerRequirements;

    @Column(columnDefinition = "TEXT")
    private String agentNotes;

    @Column(nullable = false)
    private LocalDateTime requestedDate;

    private LocalDateTime updatedDate;

    private Long resultingBookingId;
}

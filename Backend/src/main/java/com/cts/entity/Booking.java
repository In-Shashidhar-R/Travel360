package com.cts.entity;

import com.cts.enumeration.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(nullable = false)
    private Integer requestedSeats;
    
    @Column(name = "chosen_seat_type")
    private String chosenSeatType;

    @Column(nullable = false)
    private LocalDate bookingDate;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private String pickupLocation;
    private String dropoffLocation;
    
    private LocalDate targetTravelDate;
    private Integer numberOfPersons;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private double totalAmount;
    
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
        name = "booking_to_passenger_profiles",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private List<PassengerProfile> passengerProfiles = new ArrayList<>();
}
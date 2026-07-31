package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FlightInventory extends Inventory {
    @Column(nullable = false)
    private String flightNumber;
    
    @Column(nullable = false)
    private String airlineName;

    @Column(nullable = false)
    private String departureAirport;

    @Column(nullable = false)
    private String arrivalAirport;

    @Column(nullable = false)
    private boolean isConnecting;

    private String layoverDetails;
    
    @Column(nullable = false)
    private String startTime; 

    @Column(nullable = false)
    private String endTime;   

    @Column(nullable = false)
    private double numberOfHours; 

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flight_seat_tiers", joinColumns = @JoinColumn(name = "inventory_id"))
    @Builder.Default
    private List<SeatTierCapacity> seatTiers = new ArrayList<>();

    public int getTotalSeats() {
        if (seatTiers == null) return 0;
        return seatTiers.stream().mapToInt(SeatTierCapacity::getTotalSeatsAllocated).sum();
    }
}
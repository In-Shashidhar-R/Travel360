package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bus_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BusInventory extends Inventory {
    private String busNumberPlate;
    private String operatorName;
    private String routeFrom;
    private String routeTo;
    private String startTime;
    private String endTime;
    private double numberOfHours;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bus_seat_tiers", joinColumns = @JoinColumn(name = "inventory_id"))
    @Builder.Default
    private List<SeatTierCapacity> seatTiers = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "busInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusStopDetail> routeStops = new ArrayList<>();
    
    public int getTotalSeats() {
        if (seatTiers == null) return 0;
        return seatTiers.stream().mapToInt(SeatTierCapacity::getTotalSeatsAllocated).sum();
    }
}
package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bus_stop_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusStopDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private BusInventory busInventory;

    @Column(nullable = false)
    private String stopName;

    @Column(nullable = false)
    private String stopType; 

    @Column(nullable = false)
    private String scheduledTime;
}
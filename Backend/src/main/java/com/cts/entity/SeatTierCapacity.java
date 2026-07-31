package com.cts.entity;

import com.cts.enumeration.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatTierCapacity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private Integer totalSeatsAllocated;

    @Column(nullable = false)
    private double priceMultiplier; 
}
package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tour_package_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TourPackageInventory extends Inventory {

    @Column(nullable = false)
    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String fullItineraryDetails;

    @Column(nullable = false)
    private Integer durationDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_agent_user_id")
    private User travelAgent;
}

package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hotel_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HotelInventory extends Inventory {

    @Column(nullable = false)
    private String hotelName;

    @Column(nullable = false)
    private String roomType;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer starRating;

    private String addressLocation;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String country;
}

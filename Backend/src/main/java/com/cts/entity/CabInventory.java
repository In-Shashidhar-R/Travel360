package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cab_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CabInventory extends Inventory {
    
    @Column(nullable = false)
    private String vehicleRegistrationNumber;

    @Column(nullable = false)
    private String carModel;

    @Column(nullable = false)
    private String fuelType; 

    @Column(nullable = false)
    private Integer seaterCount; 
    
    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String state;
    
}
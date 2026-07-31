package com.cts.entity;

import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "inventories")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryType itemType; 

    @Column(nullable = false)
    private double basePricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
}
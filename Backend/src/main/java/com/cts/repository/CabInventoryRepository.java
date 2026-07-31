package com.cts.repository;

import com.cts.entity.CabInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CabInventoryRepository extends JpaRepository<CabInventory, Long> {
    Optional<CabInventory> findByVehicleRegistrationNumber(String vehicleRegistrationNumber);
}
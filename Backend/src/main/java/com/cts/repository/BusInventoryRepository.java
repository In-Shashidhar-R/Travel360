package com.cts.repository;

import com.cts.entity.BusInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BusInventoryRepository extends JpaRepository<BusInventory, Long> {
    Optional<BusInventory> findByBusNumberPlate(String busNumberPlate);
}
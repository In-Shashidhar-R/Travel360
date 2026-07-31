package com.cts.repository;

import com.cts.entity.FlightInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FlightInventoryRepository extends JpaRepository<FlightInventory, Long> {
    Optional<FlightInventory> findByFlightNumber(String flightNumber);
}
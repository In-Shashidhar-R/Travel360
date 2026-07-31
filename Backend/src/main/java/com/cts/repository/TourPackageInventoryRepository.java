package com.cts.repository;

import com.cts.entity.TourPackageInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TourPackageInventoryRepository extends JpaRepository<TourPackageInventory, Long> {
    Optional<TourPackageInventory> findByPackageName(String packageName);
}
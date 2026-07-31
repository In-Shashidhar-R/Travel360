package com.cts.repository;

import com.cts.entity.HotelInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HotelInventoryRepository extends JpaRepository<HotelInventory, Long> {
	Optional<HotelInventory> findByHotelNameAndRoomType(String hotelName, String roomType);
	}

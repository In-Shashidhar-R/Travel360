package com.cts.repository;
 
import com.cts.entity.Booking;
import com.cts.entity.Inventory;
import com.cts.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.time.LocalDate;
import java.util.List;
 
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
 
    Page<Booking> findByCustomer(User customer, Pageable pageable);
 
    List<Booking> findByCustomer(User customer);
 
    long countByInventory(Inventory inventory);
 
    @Query("SELECT COALESCE(SUM(b.requestedSeats), 0) FROM Booking b "
            + "WHERE b.inventory = :inventory "
            + "AND b.targetTravelDate = :travelDate "
            + "AND b.chosenSeatType = :seatType "
            + "AND b.status <> 'CANCELLED'")
    int getFilledSeatsCountForDateAndSeatType(
            @Param("inventory") Inventory inventory,
            @Param("travelDate") LocalDate travelDate,
            @Param("seatType") String seatType);
 
    @Query("SELECT COALESCE(SUM(b.requestedSeats), 0) FROM Booking b "
            + "WHERE b.inventory = :inventory "
            + "AND b.targetTravelDate = :travelDate "
            + "AND b.status <> 'CANCELLED'")
    int getFilledSeatsCountForDate(
            @Param("inventory") Inventory inventory,
            @Param("travelDate") LocalDate travelDate);
 
    @Query("SELECT b FROM Booking b "
            + "WHERE b.inventory = :inventory "
            + "AND b.status <> 'CANCELLED' "
            + "AND b.checkInDate IS NOT NULL AND b.checkOutDate IS NOT NULL "
            + "AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    List<Booking> findActiveHotelBookingsOverlapping(
            @Param("inventory") Inventory inventory,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
 
    //
    @Query("SELECT COALESCE(SUM(b.requestedSeats), 0) FROM Booking b "
            + "WHERE b.inventory = :inventory "
            + "AND b.status <> 'CANCELLED' "
            + "AND :targetDate >= b.checkInDate AND :targetDate < b.checkOutDate")
    int getFilledRoomsCountForDate(
            @Param("inventory") Inventory inventory,
            @Param("targetDate") LocalDate targetDate);
}
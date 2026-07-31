package com.cts.repository;

import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.entity.User;
import com.cts.enumeration.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE i.booking.customer = :customer")
    List<Invoice> findByCustomer(@Param("customer") User customer);

    @Query("SELECT i FROM Invoice i WHERE i.booking.customer = :customer")
    Page<Invoice> findByCustomer(@Param("customer") User customer, Pageable pageable);

    Optional<Invoice> findByBookingAndStatus(Booking booking, Status status);
}

package com.cts.repository;

import com.cts.entity.BookingRequest;
import com.cts.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

    Page<BookingRequest> findByCustomer(User customer, Pageable pageable);

    Page<BookingRequest> findByAssignedAgent(User agent, Pageable pageable);
}

package com.cts.repository;

import com.cts.entity.PassengerProfile;
import com.cts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PassengerProfileRepository extends JpaRepository<PassengerProfile, Long> {
    List<PassengerProfile> findByCustomer(User customer);
}

package com.cts.repository;
 
import com.cts.entity.Invoice;
import com.cts.entity.Payment;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByInvoice(Invoice invoice);
}
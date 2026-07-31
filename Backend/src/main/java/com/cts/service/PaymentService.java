package com.cts.service;

import com.cts.dto.PageResponse;
import com.cts.dto.PaymentRequestDTO;
import com.cts.dto.PaymentResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    void executePayment(PaymentRequestDTO request);
    PageResponse<PaymentResponseDTO> getAllPayments(Pageable pageable);
    PaymentResponseDTO getPaymentById(Long paymentId);
    List<PaymentResponseDTO> getPaymentsByInvoice(Long invoiceId);
}

package com.cts.serviceimpl;

import com.cts.dto.PageResponse;
import com.cts.dto.PaymentRequestDTO;
import com.cts.dto.PaymentResponseDTO;
import com.cts.entity.Invoice;
import com.cts.entity.Payment;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.PaymentRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.PaymentService;
import com.cts.util.CoreTransactionalUtil;
import com.cts.util.PaymentProcessingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CoreTransactionalUtil transUtil;
    private final PaymentProcessingUtil paymentUtil;

    @Override
    @Transactional
    public void executePayment(PaymentRequestDTO request) {
        Invoice invoice = paymentUtil.fetchUnpaidInvoice(request.getInvoiceId());
        paymentUtil.synchronizePaymentState(invoice);
        paymentUtil.processFinalSettlement(invoice, request.getMethod());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponseDTO> getAllPayments(Pageable pageable) {
        return PageResponse.from(paymentRepository.findAll(pageable).map(paymentUtil::toPaymentResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment ledger entry not found with ID: " + paymentId));
        SecurityUtil.assertSelfOrAdmin(payment.getInvoice().getBooking().getCustomer().getUserId());
        return paymentUtil.toPaymentResponseDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByInvoice(Long invoiceId) {
        Invoice invoice = transUtil.fetchInvoice(invoiceId);
        SecurityUtil.assertSelfOrAdmin(invoice.getBooking().getCustomer().getUserId());
        return paymentRepository.findByInvoice(invoice).stream()
                .map(paymentUtil::toPaymentResponseDTO)
                .toList();
    }
}

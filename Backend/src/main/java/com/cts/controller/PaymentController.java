package com.cts.controller;

import com.cts.dto.PageResponse;
import com.cts.dto.MessageResponse;
import com.cts.dto.PaymentRequestDTO;
import com.cts.dto.PaymentResponseDTO;
import com.cts.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.cts.util.AppConstants;
import com.cts.util.PaginationUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "07. Payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')") // SECURED: Restricts payment clearance to transaction owners
    @Operation(summary = "Clears the outstanding balance on an invoice")
    public ResponseEntity<MessageResponse> executePayment(@Valid @RequestBody PaymentRequestDTO request) {
        paymentService.executePayment(request);
        return ResponseEntity.ok(
                MessageResponse.of("PAYMENT_CLEARED", "Payment transaction cleared successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER')")
    @Operation(summary = "Paginated administrative listing of all payment ledger entries")
    public ResponseEntity<PageResponse<PaymentResponseDTO>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "paymentId") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Fetches the full payment history (charges and refunds) for an invoice")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoice(invoiceId));
    }
}
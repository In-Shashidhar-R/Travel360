package com.cts.service;

import com.cts.dto.InvoiceResponseDTO;
import com.cts.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
    PageResponse<InvoiceResponseDTO> getAllInvoices(Pageable pageable);
    PageResponse<InvoiceResponseDTO> getInvoicesByCustomer(Long customerId, Pageable pageable);
    InvoiceResponseDTO getInvoiceById(Long invoiceId);
}

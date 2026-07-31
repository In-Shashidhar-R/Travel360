package com.cts.serviceimpl;

import com.cts.dto.InvoiceResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.entity.User;
import com.cts.mapper.BookingMapper;
import com.cts.repository.InvoiceRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.InvoiceService;
import com.cts.util.CoreTransactionalUtil;
import com.cts.util.UserSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingMapper bookingMapper;
    private final UserSecurityUtil securityUtil;
    private final CoreTransactionalUtil transUtil;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponseDTO> getAllInvoices(Pageable pageable) {
        return PageResponse.from(invoiceRepository.findAll(pageable).map(bookingMapper::toInvoiceResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponseDTO> getInvoicesByCustomer(Long customerId, Pageable pageable) {
        User customer = securityUtil.fetchUser(customerId);
        return PageResponse.from(invoiceRepository.findByCustomer(customer, pageable).map(bookingMapper::toInvoiceResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(Long invoiceId) {
        var invoice = transUtil.fetchInvoice(invoiceId);
        SecurityUtil.assertSelfOrAdmin(invoice.getBooking().getCustomer().getUserId());
        return bookingMapper.toInvoiceResponseDTO(invoice);
    }
}

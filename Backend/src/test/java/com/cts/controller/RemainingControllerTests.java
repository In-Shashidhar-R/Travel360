package com.cts.controller;

import com.cts.dto.*;
import com.cts.exception.GlobalExceptionHandler;
import com.cts.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RemainingControllerTests {

    private static MockMvc standalone(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(ControllerTestSupport.NOOP_VALIDATOR)
                .build();
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class InvoiceControllerTest {
        @Mock InvoiceService invoiceService;
        @InjectMocks InvoiceController controller;
        MockMvc mockMvc;

        @BeforeEach void s() { mockMvc = standalone(controller); ControllerTestSupport.loginAsAdmin(); }
        @AfterEach void c() { ControllerTestSupport.clear(); }

        @Test void getAll() throws Exception {
            when(invoiceService.getAllInvoices(any(Pageable.class)))
                    .thenReturn(PageResponse.from(new PageImpl<>(List.of(new InvoiceResponseDTO()))));
            mockMvc.perform(get("/api/v1/invoices")).andExpect(status().isOk());
        }
        @Test void getById() throws Exception {
            when(invoiceService.getInvoiceById(1L)).thenReturn(new InvoiceResponseDTO());
            mockMvc.perform(get("/api/v1/invoices/1")).andExpect(status().isOk());
        }
        @Test void getByCustomer() throws Exception {
            when(invoiceService.getInvoicesByCustomer(eq(1L), any(Pageable.class)))
                    .thenReturn(PageResponse.from(new PageImpl<>(List.of(new InvoiceResponseDTO()))));
            mockMvc.perform(get("/api/v1/invoices/customer/1")).andExpect(status().isOk());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class PaymentControllerTest {
        @Mock PaymentService paymentService;
        @InjectMocks PaymentController controller;
        MockMvc mockMvc;

        @BeforeEach void s() { mockMvc = standalone(controller); }

        @Test void executePayment() throws Exception {
            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());
        }
        @Test void getAll() throws Exception {
            when(paymentService.getAllPayments(any(Pageable.class)))
                    .thenReturn(PageResponse.from(new PageImpl<>(List.of(new PaymentResponseDTO()))));
            mockMvc.perform(get("/api/v1/payments")).andExpect(status().isOk());
        }
        @Test void getById() throws Exception {
            when(paymentService.getPaymentById(1L)).thenReturn(new PaymentResponseDTO());
            mockMvc.perform(get("/api/v1/payments/1")).andExpect(status().isOk());
        }
        @Test void getByInvoice() throws Exception {
            when(paymentService.getPaymentsByInvoice(1L)).thenReturn(List.of(new PaymentResponseDTO()));
            mockMvc.perform(get("/api/v1/payments/invoice/1")).andExpect(status().isOk());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class NotificationControllerTest {
        @Mock NotificationService notificationService;
        @InjectMocks NotificationController controller;
        MockMvc mockMvc;

        @BeforeEach void s() { mockMvc = standalone(controller); ControllerTestSupport.loginAsAdmin(); }
        @AfterEach void c() { ControllerTestSupport.clear(); }

        @Test void getUserNotifications() throws Exception {
            when(notificationService.getUserNotifications(eq(1L), anyBoolean(), any(Pageable.class)))
                    .thenReturn(PageResponse.from(new PageImpl<>(List.of(new NotificationResponseDTO()))));
            mockMvc.perform(get("/api/v1/notifications/user/1")).andExpect(status().isOk());
        }
        @Test void markAsRead() throws Exception {
            when(notificationService.markAsRead(1L, 1L)).thenReturn(new NotificationResponseDTO());
            mockMvc.perform(put("/api/v1/notifications/1/read").param("userId", "1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class PartnerControllerTest {
        @Mock PartnerService partnerService;
        @InjectMocks PartnerController controller;
        MockMvc mockMvc;

        @BeforeEach void s() { mockMvc = standalone(controller); }

        @Test void register() throws Exception {
            when(partnerService.registerPartner(any())).thenReturn(new PartnerResponseDTO());
            mockMvc.perform(post("/api/v1/partners")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isCreated());
        }
        @Test void getAll() throws Exception {
            when(partnerService.getAllPartners(any(Pageable.class)))
                    .thenReturn(PageResponse.from(new PageImpl<>(List.of(new PartnerResponseDTO()))));
            mockMvc.perform(get("/api/v1/partners")).andExpect(status().isOk());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class PassengerDirectoryControllerTest {
        @Mock PassengerDirectoryService directoryService;
        @InjectMocks PassengerDirectoryController controller;
        MockMvc mockMvc;

        @BeforeEach void s() { mockMvc = standalone(controller); ControllerTestSupport.loginAsAdmin(); }
        @AfterEach void c() { ControllerTestSupport.clear(); }

        @Test void save() throws Exception {
            mockMvc.perform(post("/api/v1/passengers")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isCreated());
        }
        @Test 
        void update() throws Exception {
            mockMvc.perform(patch("/api/v1/passengers/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
        @Test void remove() throws Exception {
            mockMvc.perform(delete("/api/v1/passengers/2")).andExpect(status().isOk());
        }
        @Test void getPool() throws Exception {
            when(directoryService.getCustomerDirectoryPool(1L)).thenReturn(List.of(new PassengerProfileDTO()));
            mockMvc.perform(get("/api/v1/passengers")).andExpect(status().isOk());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class AuditLogControllerTest {
        @Mock AuditLogQueryService auditLogQueryService;
        @InjectMocks AuditLogController controller;
        MockMvc mockMvc;

        @BeforeEach void s() { mockMvc = standalone(controller); }

        @Test 
        void getLogs_withLocalDateParam_returns200() throws Exception {
            when(auditLogQueryService.getLogs(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(PageResponse.from(new PageImpl<>(List.of(new AuditLogResponseDTO()))));
            
            mockMvc.perform(get("/api/v1/audit-logs")
                            .param("date", "2026-07-01"))
                    .andExpect(status().isOk());
        }

        @Test 
        void complianceReport_withLocalDateRanges_returns200() throws Exception {
            when(auditLogQueryService.buildComplianceReport(any(), any()))
                    .thenReturn(com.cts.dto.ComplianceReportDTO.builder().totalAuditEvents(5).build());
            
            mockMvc.perform(get("/api/v1/audit-logs/compliance-report")
                            .param("from", "2026-06-01")
                            .param("to", "2026-07-01")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAuditEvents").value(5));
        }
    }
}

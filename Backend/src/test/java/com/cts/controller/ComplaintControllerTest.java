package com.cts.controller;

import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.exception.GlobalExceptionHandler;
import com.cts.service.ComplaintService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplaintControllerTest {

    @Mock ComplaintService complaintService;
    @InjectMocks ComplaintController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(ControllerTestSupport.NOOP_VALIDATOR)
                .build();
        ControllerTestSupport.loginAsAdmin();
    }

    @AfterEach
    void tearDown() { ControllerTestSupport.clear(); }

    @Test
    void raise_returns201() throws Exception {
        when(complaintService.raiseComplaint(any()))
                .thenReturn(ComplaintResponseDTO.builder().complaintId(1L).build());
        mockMvc.perform(post("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaintId").value(1));
    }

    @Test
    void getComplaints_withQueryParameters_returns200() throws Exception {
        when(complaintService.getComplaints(any(), any(), any(), any(Pageable.class)))
                .thenReturn(PageResponse.<ComplaintResponseDTO>builder().content(java.util.List.of()).build());
        
        mockMvc.perform(get("/api/v1/complaints")
                        .param("userId", "7")
                        .param("status", "OPEN")
                        .param("bookingId", "50")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getOne_returns200() throws Exception {
        when(complaintService.getComplaintById(1L))
                .thenReturn(ComplaintResponseDTO.builder().complaintId(1L).build());
        mockMvc.perform(get("/api/v1/complaints/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void inProgress_returns200() throws Exception {
        when(complaintService.markInProgress(eq(1L), any()))
                .thenReturn(ComplaintResponseDTO.builder().complaintId(1L).status("IN_PROGRESS").build());
                
        mockMvc.perform(patch("/api/v1/complaints/1/in-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"working\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void resolve_returns200() throws Exception {
        when(complaintService.resolveComplaint(eq(1L), any()))
                .thenReturn(ComplaintResponseDTO.builder().complaintId(1L).status("RESOLVED").build());
                
        mockMvc.perform(patch("/api/v1/complaints/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"done\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
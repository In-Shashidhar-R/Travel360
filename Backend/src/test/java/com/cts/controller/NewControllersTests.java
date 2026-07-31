package com.cts.controller;

import com.cts.dto.*;
import com.cts.exception.GlobalExceptionHandler;
import com.cts.service.AnalyticsService;
import com.cts.service.BookingRequestService;
import com.cts.service.ItineraryService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NewControllersTests {

    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    @Nested
    class BookingRequestControllerTest {

        @Mock BookingRequestService bookingRequestService;
        @InjectMocks BookingRequestController controller;
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
        void create_returns201() throws Exception {
            when(bookingRequestService.createRequest(any()))
                    .thenReturn(BookingRequestResponseDTO.builder().requestId(11L).build());
            mockMvc.perform(post("/api/v1/booking-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.requestId").value(11));
        }

        @Test
        void mine_returns200() throws Exception {
            when(bookingRequestService.listMyCustomerRequests(any(Pageable.class)))
                    .thenReturn(PageResponse.<BookingRequestResponseDTO>builder()
                            .content(List.of()).page(0).size(10).build());
            mockMvc.perform(get("/api/v1/booking-requests/mine").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void assigned_returns200() throws Exception {
            when(bookingRequestService.listAssignedRequests(any(Pageable.class)))
                    .thenReturn(PageResponse.<BookingRequestResponseDTO>builder()
                            .content(List.of()).page(0).size(10).build());
            mockMvc.perform(get("/api/v1/booking-requests/assigned").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void accept_returns200() throws Exception {
            when(bookingRequestService.acceptRequest(any(), any()))
                    .thenReturn(BookingRequestResponseDTO.builder().requestId(5L).build());
            mockMvc.perform(put("/api/v1/booking-requests/5/accept")
                            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                            .content("{\"agentNotes\":\"ok\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.requestId").value(5));
        }

        @Test
        void accept_withoutBody_stillWorks() throws Exception {
            when(bookingRequestService.acceptRequest(any(), any()))
                    .thenReturn(BookingRequestResponseDTO.builder().requestId(5L).build());
            mockMvc.perform(put("/api/v1/booking-requests/5/accept").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void reject_returns200() throws Exception {
            when(bookingRequestService.rejectRequest(any(), any()))
                    .thenReturn(BookingRequestResponseDTO.builder().requestId(5L).build());
            mockMvc.perform(put("/api/v1/booking-requests/5/reject")
                            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                            .content("{\"agentNotes\":\"no\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        void book_returns200() throws Exception {
            when(bookingRequestService.completeRequestByBooking(any(), any()))
                    .thenReturn(BookingRequestResponseDTO.builder().requestId(5L).resultingBookingId(77L).build());
            mockMvc.perform(post("/api/v1/booking-requests/5/book")
                            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultingBookingId").value(77));
        }

        @Test
        void getOne_returns200() throws Exception {
            when(bookingRequestService.getRequestById(5L))
                    .thenReturn(BookingRequestResponseDTO.builder().requestId(5L).build());
            mockMvc.perform(get("/api/v1/booking-requests/5").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    @Nested
    class ItineraryControllerTest {

        @Mock ItineraryService itineraryService;
        @InjectMocks ItineraryController controller;
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
        void upcoming_returns200() throws Exception {
            when(itineraryService.getMyUpcomingTrips()).thenReturn(List.of());
            mockMvc.perform(get("/api/v1/itineraries/upcoming").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void past_returns200() throws Exception {
            when(itineraryService.getMyPastTrips()).thenReturn(List.of());
            mockMvc.perform(get("/api/v1/itineraries/past").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        void forCustomer_returns200() throws Exception {
            when(itineraryService.getTripsForCustomer(7L)).thenReturn(List.of());
            mockMvc.perform(get("/api/v1/itineraries/customer/7").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    @Nested
    class AnalyticsControllerTest {

        @Mock AnalyticsService analyticsService;
        @InjectMocks AnalyticsController controller;
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
        void dashboard_returns200() throws Exception {
            when(analyticsService.getDashboard())
                    .thenReturn(AnalyticsDashboardDTO.builder()
                            .totalUsers(5).totalBookings(10)
                            .totalRevenueCollected(1500.0).build());
            mockMvc.perform(get("/api/v1/analytics/dashboard").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers").value(5))
                    .andExpect(jsonPath("$.totalBookings").value(10));
        }
    }
}

package com.cts.controller;

import com.cts.dto.*;
import com.cts.exception.GlobalExceptionHandler;
import com.cts.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingControllerTest {

    @Mock private BookingService bookingService;
    @InjectMocks private BookingController controller;
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
    void clear() {
        ControllerTestSupport.clear();
    }

    private void postCreated(String path) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void bookFlight_returns201() throws Exception {
        when(bookingService.bookFlight(any())).thenReturn(new FlightBookingResponseDTO());
        postCreated("/api/v1/bookings/flight");
    }

    @Test
    void bookHotel_returns201() throws Exception {
        when(bookingService.bookHotel(any())).thenReturn(new HotelBookingResponseDTO());
        postCreated("/api/v1/bookings/hotel");
    }

    @Test
    void bookBus_returns201() throws Exception {
        when(bookingService.bookBus(any())).thenReturn(new BusBookingResponseDTO());
        postCreated("/api/v1/bookings/bus");
    }

    @Test
    void bookCab_returns201() throws Exception {
        when(bookingService.bookCab(any())).thenReturn(new CabBookingResponseDTO());
        postCreated("/api/v1/bookings/cab");
    }

    @Test
    void bookTour_returns201() throws Exception {
        when(bookingService.bookTour(any())).thenReturn(new TourBookingResponseDTO());
        postCreated("/api/v1/bookings/tour-package");
    }

    @Test
    void getAllBookings_returns200() throws Exception {
        when(bookingService.getAllBookings(any(Pageable.class)))
                .thenReturn(PageResponse.from(new PageImpl<>(List.of(new FlightBookingResponseDTO()))));
        mockMvc.perform(get("/api/v1/bookings")).andExpect(status().isOk());
    }

    @Test
    void getBookingById_returns200() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(new FlightBookingResponseDTO());
        mockMvc.perform(get("/api/v1/bookings/1")).andExpect(status().isOk());
    }

    @Test
    void getCustomerBookings_returns200() throws Exception {
        when(bookingService.getCustomerBookings(eq(1L), any(Pageable.class)))
                .thenReturn(PageResponse.from(new PageImpl<>(List.of(new FlightBookingResponseDTO()))));
        mockMvc.perform(get("/api/v1/bookings/customer/1")).andExpect(status().isOk());
    }

    @Test
    void cancelBooking_returns200() throws Exception {
        when(bookingService.cancelBooking(eq(1L), any())).thenReturn(InvoiceCancelResponseDTO.builder().build());
        mockMvc.perform(put("/api/v1/bookings/cancel/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelEntireBooking_returns200() throws Exception {
        when(bookingService.cancelEntireBooking(eq(1L), any(), any())).thenReturn(InvoiceCancelResponseDTO.builder().build());
        mockMvc.perform(put("/api/v1/bookings/cancel-all/1")).andExpect(status().isOk());
    }
}

package com.cts.controller;

import com.cts.dto.*;
import com.cts.exception.GlobalExceptionHandler;
import com.cts.exception.InventoryTypeMismatchException;
import com.cts.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryControllerTest {

    @Mock private InventoryService inventoryService;
    @InjectMocks private InventoryController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(ControllerTestSupport.NOOP_VALIDATOR)
                .build();
    }

    private void postCreated(String path) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    private void putOk(String path) throws Exception {
        mockMvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    // ---- create ----
    @Test void provisionFlight() throws Exception {
        when(inventoryService.provisionFlight(any())).thenReturn(new FlightInventoryResponseDTO());
        postCreated("/api/v1/inventories/flight");
    }
    @Test void provisionHotel() throws Exception {
        when(inventoryService.provisionHotel(any())).thenReturn(new HotelInventoryResponseDTO());
        postCreated("/api/v1/inventories/hotel");
    }
    @Test void provisionBus() throws Exception {
        when(inventoryService.provisionBus(any())).thenReturn(new BusInventoryResponseDTO());
        postCreated("/api/v1/inventories/bus");
    }
    @Test void provisionCab() throws Exception {
        when(inventoryService.provisionCab(any())).thenReturn(new CabInventoryResponseDTO());
        postCreated("/api/v1/inventories/cab");
    }
    @Test void provisionTour() throws Exception {
        when(inventoryService.provisionTour(any())).thenReturn(new TourInventoryResponseDTO());
        postCreated("/api/v1/inventories/tour-package");
    }

    // ---- update ----
    @Test void updateFlight() throws Exception {
        when(inventoryService.updateFlight(eq(1L), any())).thenReturn(new FlightInventoryResponseDTO());
        putOk("/api/v1/inventories/flight/1");
    }
    @Test void updateHotel() throws Exception {
        when(inventoryService.updateHotel(eq(1L), any())).thenReturn(new HotelInventoryResponseDTO());
        putOk("/api/v1/inventories/hotel/1");
    }
    @Test void updateBus() throws Exception {
        when(inventoryService.updateBus(eq(1L), any())).thenReturn(new BusInventoryResponseDTO());
        putOk("/api/v1/inventories/bus/1");
    }
    @Test void updateCab() throws Exception {
        when(inventoryService.updateCab(eq(1L), any())).thenReturn(new CabInventoryResponseDTO());
        putOk("/api/v1/inventories/cab/1");
    }
    @Test void updateTour() throws Exception {
        when(inventoryService.updateTour(eq(1L), any())).thenReturn(new TourInventoryResponseDTO());
        putOk("/api/v1/inventories/tour-package/1");
    }

    // ---- delete ----
    @Test void deleteFlight() throws Exception {
        mockMvc.perform(delete("/api/v1/inventories/flight/1")).andExpect(status().isOk());
    }
    @Test void deleteHotel() throws Exception {
        mockMvc.perform(delete("/api/v1/inventories/hotel/1")).andExpect(status().isOk());
    }
    @Test void deleteBus() throws Exception {
        mockMvc.perform(delete("/api/v1/inventories/bus/1")).andExpect(status().isOk());
    }
    @Test void deleteCab() throws Exception {
        mockMvc.perform(delete("/api/v1/inventories/cab/1")).andExpect(status().isOk());
    }
    @Test void deleteTour() throws Exception {
        mockMvc.perform(delete("/api/v1/inventories/tour-package/1")).andExpect(status().isOk());
    }

    // ---- lifecycle ----
    @Test void deactivate() throws Exception {
        when(inventoryService.deactivateInventory(1L)).thenReturn(new FlightInventoryResponseDTO());
        putOk("/api/v1/inventories/1/deactivate");
    }
    @Test void activate() throws Exception {
        when(inventoryService.activateInventory(1L)).thenReturn(new FlightInventoryResponseDTO());
        putOk("/api/v1/inventories/1/activate");
    }

    // ---- read ----
    @Test void getById() throws Exception {
        when(inventoryService.getInventoryById(eq(1L), any())).thenReturn(new FlightInventoryResponseDTO());
        mockMvc.perform(get("/api/v1/inventories/1")).andExpect(status().isOk());
    }
    @Test void search() throws Exception {
        when(inventoryService.searchByRoute(eq("A"), eq("B"), nullable(LocalDate.class)))
                .thenReturn(List.of(new FlightInventoryResponseDTO()));
        mockMvc.perform(get("/api/v1/inventories/search").param("source", "A").param("destination", "B"))
                .andExpect(status().isOk());
    }
    @Test void getAll() throws Exception {
        when(inventoryService.getAllInventories(nullable(LocalDate.class)))
                .thenReturn(List.of(new FlightInventoryResponseDTO()));
        mockMvc.perform(get("/api/v1/inventories")).andExpect(status().isOk());
    }
    @Test void filter() throws Exception {
        when(inventoryService.filterInventories(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(new FlightInventoryResponseDTO()));
        mockMvc.perform(get("/api/v1/inventories/filter")).andExpect(status().isOk());
    }

    @Test
    void update_typeMismatch_returns400() throws Exception {
        when(inventoryService.updateFlight(eq(1L), any()))
                .thenThrow(new InventoryTypeMismatchException("Inventory ID 1 is a HOTEL, not a FLIGHT."));
        mockMvc.perform(put("/api/v1/inventories/flight/1")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyInventories_returns200() throws Exception {
        when(inventoryService.getMyInventories(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(PageResponse.<Object>builder()
                        .content(List.of()).page(0).size(10).build());
        mockMvc.perform(get("/api/v1/inventories/mine").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateInventory_returns200() throws Exception {
        when(inventoryService.deactivateInventory(7L)).thenReturn(new FlightInventoryResponseDTO());
        mockMvc.perform(put("/api/v1/inventories/7/deactivate")).andExpect(status().isOk());
    }

    @Test
    void activateInventory_returns200() throws Exception {
        when(inventoryService.activateInventory(7L)).thenReturn(new FlightInventoryResponseDTO());
        mockMvc.perform(put("/api/v1/inventories/7/activate")).andExpect(status().isOk());
    }
}

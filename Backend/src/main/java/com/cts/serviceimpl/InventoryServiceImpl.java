package com.cts.serviceimpl;
 
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Role;
import com.cts.enumeration.Status;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.InventoryTypeMismatchException;
import com.cts.mapper.InventoryMapper;
import com.cts.repository.InventoryRepository;
import com.cts.repository.UserRepository;
import com.cts.service.InventoryService;
import com.cts.util.CoreTransactionalUtil;
import com.cts.util.InventoryManagementUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDate;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
 
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final CoreTransactionalUtil transUtil;
    private final InventoryManagementUtil inventoryUtil;
    private final UserRepository userRepository;
 
    @Override
    @Transactional
    public HotelInventoryResponseDTO provisionHotel(HotelInventoryRequestDTO req) {
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.HOTEL);
        HotelInventory entity = inventoryMapper.toHotelEntity(req, partner);
        return inventoryMapper.toHotelResponseDTO(inventoryUtil.commitInventory(entity), null);
    }
 
    @Override
    @Transactional
    public FlightInventoryResponseDTO provisionFlight(FlightInventoryRequestDTO req) {
        inventoryUtil.assertDistinctRoute(req.getDepartureAirport(), req.getArrivalAirport());
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.FLIGHT);
        FlightInventory entity = inventoryMapper.toFlightEntity(req, partner);
        return inventoryMapper.toFlightResponseDTO(inventoryUtil.commitInventory(entity), null);
    }
 
    @Override
    @Transactional
    public BusInventoryResponseDTO provisionBus(BusInventoryRequestDTO req) {
        inventoryUtil.assertDistinctRoute(req.getRouteFrom(), req.getRouteTo());
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.BUS);
        BusInventory entity = inventoryMapper.toBusEntity(req, partner);
        return inventoryMapper.toBusResponseDTO(inventoryUtil.commitInventory(entity), null);
    }
 
    @Override
    @Transactional
    public CabInventoryResponseDTO provisionCab(CabInventoryRequestDTO req) {
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.CAB);
        CabInventory entity = inventoryMapper.toCabEntity(req, partner);
        return inventoryMapper.toCabResponseDTO(inventoryUtil.commitInventory(entity), null);
    }
 
    @Override
    @Transactional
    public TourInventoryResponseDTO provisionTour(TourInventoryRequestDTO req) {
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.TOUR_PACKAGE);
        TourPackageInventory entity = inventoryMapper.toTourEntity(req, partner);
        applyTravelAgent(entity, req.getTravelAgentId());
        return inventoryMapper.toTourResponseDTO(inventoryUtil.commitInventory(entity), null);
    }
 
    @Override
    @Transactional
    public FlightInventoryResponseDTO updateFlight(Long inventoryId, FlightInventoryRequestDTO req) {
        FlightInventory existing = (FlightInventory) inventoryUtil.fetchInventoryOfType(inventoryId, InventoryType.FLIGHT);
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.FLIGHT);
        inventoryMapper.updateFlightEntity(existing, req, partner);
        return inventoryMapper.toFlightResponseDTO(inventoryUtil.commitInventory(existing), null);
    }
 
    @Override
    @Transactional
    public HotelInventoryResponseDTO updateHotel(Long inventoryId, HotelInventoryRequestDTO req) {
        HotelInventory existing = (HotelInventory) inventoryUtil.fetchInventoryOfType(inventoryId, InventoryType.HOTEL);
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.HOTEL);
        inventoryMapper.updateHotelEntity(existing, req, partner);
        return inventoryMapper.toHotelResponseDTO(inventoryUtil.commitInventory(existing), null);
    }
 
    @Override
    @Transactional
    public BusInventoryResponseDTO updateBus(Long inventoryId, BusInventoryRequestDTO req) {
        BusInventory existing = (BusInventory) inventoryUtil.fetchInventoryOfType(inventoryId, InventoryType.BUS);
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.BUS);
        inventoryMapper.updateBusEntity(existing, req, partner);
        return inventoryMapper.toBusResponseDTO(inventoryUtil.commitInventory(existing), null);
    }
 
    @Override
    @Transactional
    public CabInventoryResponseDTO updateCab(Long inventoryId, CabInventoryRequestDTO req) {
        CabInventory existing = (CabInventory) inventoryUtil.fetchInventoryOfType(inventoryId, InventoryType.CAB);
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.CAB);
        inventoryMapper.updateCabEntity(existing, req, partner);
        return inventoryMapper.toCabResponseDTO(inventoryUtil.commitInventory(existing), null);
    }
 
    @Override
    @Transactional
    public TourInventoryResponseDTO updateTour(Long inventoryId, TourInventoryRequestDTO req) {
        TourPackageInventory existing = (TourPackageInventory) inventoryUtil.fetchInventoryOfType(inventoryId, InventoryType.TOUR_PACKAGE);
        Partner partner = fetchPartnerOfType(req.getPartnerId(), InventoryType.TOUR_PACKAGE);
        inventoryMapper.updateTourEntity(existing, req, partner);
        applyTravelAgent(existing, req.getTravelAgentId());
        return inventoryMapper.toTourResponseDTO(inventoryUtil.commitInventory(existing), null);
    }
 
    private Partner fetchPartnerOfType(Long partnerId, InventoryType expectedType) {
        Partner partner = transUtil.fetchPartner(partnerId);
        if (partner.getType() != expectedType) {
            throw new InventoryTypeMismatchException(
                    "Partner #" + partnerId + " is a " + partner.getType()
                    + " provider and cannot hold " + expectedType + " inventory.");
        }
        return partner;
    }
 
    private void applyTravelAgent(TourPackageInventory tour, Long travelAgentId) {
        if (travelAgentId == null) {
            tour.setTravelAgent(null);
            return;
        }
        User agent = userRepository.findById(travelAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Travel agent not found with ID: " + travelAgentId));
        if (agent.getRole() != Role.TRAVEL_AGENT) {
            throw new IllegalArgumentException("User #" + travelAgentId + " is not a TRAVEL_AGENT");
        }
        tour.setTravelAgent(agent);
    }
 
    @Override
    @Transactional
    public void deleteInventory(Long inventoryId, InventoryType expectedType) {
        inventoryUtil.deleteInventoryOfType(inventoryId, expectedType);
    }
 
    @Override
    @Transactional
    public Object deactivateInventory(Long inventoryId) {
        Inventory inv = inventoryUtil.fetchInventory(inventoryId);
        assertCanChangeInventoryStatus(inv);
        inv.setStatus(Status.INACTIVE);
        return inventoryMapper.toGenericResponseDTO(inventoryUtil.commitInventory(inv), null);
    }
 
    @Override
    @Transactional
    public Object activateInventory(Long inventoryId) {
        Inventory inv = inventoryUtil.fetchInventory(inventoryId);
        assertCanChangeInventoryStatus(inv);
        inv.setStatus(Status.ACTIVE);
        return inventoryMapper.toGenericResponseDTO(inventoryUtil.commitInventory(inv), null);
    }
 
    private void assertCanChangeInventoryStatus(Inventory inv) {
        if (com.cts.security.SecurityUtil.isAdmin()) {
            return;
        }
        Long currentUserId = com.cts.security.SecurityUtil.getCurrentUserId();
        if (inv.getPartner() == null
                || inv.getPartner().getUser() == null
                || !inv.getPartner().getUser().getUserId().equals(currentUserId)) {
            throw new com.cts.exception.DataIsolationViolationException(
                    "You can only change status on inventories owned by your partner account.");
        }
    }
 
    @Override
    @Transactional(readOnly = true)
    public PageResponse<Object> getMyInventories(org.springframework.data.domain.Pageable pageable) {
        Long currentUserId = com.cts.security.SecurityUtil.getCurrentUserId();
        return PageResponse.from(
                inventoryRepository.findByPartner_User_UserId(currentUserId, pageable)
                        .map(inv -> (Object) inventoryMapper.toGenericResponseDTO(inv, null)));
    }
 
    @Override
    @Transactional(readOnly = true)
    public Object getInventoryById(Long inventoryId, LocalDate targetDate) {
        LocalDate activeDate = (targetDate != null) ? targetDate : LocalDate.now();

        Inventory inv = inventoryUtil.fetchInventory(inventoryId);
        inventoryUtil.applyLiveDynamicPricing(inv, activeDate);
        return inventoryMapper.toGenericResponseDTO(inv, activeDate);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<Object> getAllInventories(LocalDate targetDate) {
        return inventoryUtil.streamActiveInventories()
                .peek(inv -> inventoryUtil.applyLiveDynamicPricing(inv, targetDate))
                .map(inv -> inventoryMapper.toGenericResponseDTO(inv, targetDate))
                .toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<Object> searchByRoute(String source, String destination, LocalDate targetDate) {
        return inventoryUtil.streamActiveInventories()
                .filter(inv -> inventoryUtil.matchesRoute(inv, source, destination))
                .peek(inv -> inventoryUtil.applyLiveDynamicPricing(inv, targetDate))
                .map(inv -> inventoryMapper.toGenericResponseDTO(inv, targetDate))
                .toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<Object> filterInventories(InventoryType type, String state, String district, String city,
                                           String source, String destination, Integer capacity,
                                           Integer days, Double maxPrice, LocalDate targetDate) {
        return inventoryUtil.streamActiveInventories()
                .filter(inv -> type == null || inv.getItemType() == type)
                .peek(inv -> inventoryUtil.applyLiveDynamicPricing(inv, targetDate))
                .filter(inv -> maxPrice == null || inv.getBasePricePerUnit() <= maxPrice)
                .filter(inv -> inventoryUtil.matchesAdvancedFilters(inv, state, district, city, source, destination, capacity, days))
                .map(inv -> inventoryMapper.toGenericResponseDTO(inv, targetDate))
                .toList();
    }
}
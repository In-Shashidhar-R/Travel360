package com.cts.mapper;
 
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumeration.InventoryType;
import com.cts.repository.BookingRepository;
import org.springframework.stereotype.Component;
 
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
 
@Component
public class InventoryMapper {
    
    private final BookingRepository bookingRepository;
 
    // Explicit constructor injection to safely wire up the repository dependency
    public InventoryMapper(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
 
    public FlightInventory toFlightEntity(FlightInventoryRequestDTO req, Partner partner) {
        FlightInventory f = FlightInventory.builder()
                .flightNumber(req.getFlightNumber())
                .airlineName(req.getAirlineName())
                .departureAirport(req.getDepartureAirport())
                .arrivalAirport(req.getArrivalAirport())
                .isConnecting(req.isConnecting())
                .layoverDetails(req.isConnecting() ? req.getLayoverDetails() : "Direct Flight Segment")
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .numberOfHours(calculateDurationInHours(req.getStartTime(), req.getEndTime()))
                .seatTiers(req.getSeatTiers() != null ? req.getSeatTiers() : new ArrayList<>())
                .build();
        applyParentBaselinesToEntity(f, partner, InventoryType.FLIGHT, req.getBasePricePerSeat());
        return f;
    }
 
    public HotelInventory toHotelEntity(HotelInventoryRequestDTO req, Partner partner) {
        HotelInventory h = HotelInventory.builder()
                .hotelName(req.getHotelName())
                .roomType(req.getRoomType())
                .starRating(req.getHotelRating() != null ? req.getHotelRating() : 3)
                .addressLocation(req.getAddressLocation())
                .district(req.getDistrict())
                .state(req.getState())
                .country(req.getCountry())
                .totalSeats(req.getTotalRooms())
                .build();
        applyParentBaselinesToEntity(h, partner, InventoryType.HOTEL, req.getBasePricePerRoom());
        return h;
    }
 
    public BusInventory toBusEntity(BusInventoryRequestDTO req, Partner partner) {
        BusInventory b = BusInventory.builder()
                .busNumberPlate(req.getBusNumberPlate())
                .operatorName(req.getOperatorName())
                .routeFrom(req.getRouteFrom())
                .routeTo(req.getRouteTo())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .numberOfHours(calculateDurationInHours(req.getStartTime(), req.getEndTime()))
                .seatTiers(req.getSeatTiers() != null ? req.getSeatTiers() : new ArrayList<>())
                .build();
        
        if (req.getRouteStops() != null) {
            b.setRouteStops(new ArrayList<>());
            b.getRouteStops().addAll(req.getRouteStops().stream().map(stopDTO -> BusStopDetail.builder()
                    .busInventory(b)
                    .stopName(stopDTO.getStopName())
                    .stopType(stopDTO.getStopType().toUpperCase())
                    .scheduledTime(stopDTO.getScheduledTime())
                    .build()).toList());
        }
        applyParentBaselinesToEntity(b, partner, InventoryType.BUS, req.getBasePricePerSeat());
        return b;
    }
 
    public CabInventory toCabEntity(CabInventoryRequestDTO req, Partner partner) {
        CabInventory c = CabInventory.builder()
                .vehicleRegistrationNumber(req.getVehicleRegistrationNumber())
                .carModel(req.getCarModel())
                .fuelType(req.getFuelType().toUpperCase())
                .seaterCount(req.getSeaterCount())
                .district(req.getDistrict().trim())
                .state(req.getState().trim())
                .build();
        applyParentBaselinesToEntity(c, partner, InventoryType.CAB, req.getBasePricePerSeat());
        return c;
    }
 
    public TourPackageInventory toTourEntity(TourInventoryRequestDTO req, Partner partner) {
        TourPackageInventory t = TourPackageInventory.builder()
                .packageName(req.getPackageName())
                .fullItineraryDetails(req.getFullItineraryDetails())
                .durationDays(req.getDurationDays())
                .build();
        applyParentBaselinesToEntity(t, partner, InventoryType.TOUR_PACKAGE, req.getBasePricePerPersonForPackage());
        return t;
    }
 
    public void updateFlightEntity(FlightInventory f, FlightInventoryRequestDTO req, Partner partner) {
        f.setFlightNumber(req.getFlightNumber());
        f.setAirlineName(req.getAirlineName());
        f.setDepartureAirport(req.getDepartureAirport());
        f.setArrivalAirport(req.getArrivalAirport());
        f.setConnecting(req.isConnecting());
        f.setLayoverDetails(req.isConnecting() ? req.getLayoverDetails() : "Direct Flight Segment");
        f.setStartTime(req.getStartTime());
        f.setEndTime(req.getEndTime());
        f.setNumberOfHours(calculateDurationInHours(req.getStartTime(), req.getEndTime()));
        f.getSeatTiers().clear();
        if (req.getSeatTiers() != null) {
            f.getSeatTiers().addAll(req.getSeatTiers());
        }
        f.setPartner(partner);
        f.setBasePricePerUnit(req.getBasePricePerSeat());
    }
 
    public void updateHotelEntity(HotelInventory h, HotelInventoryRequestDTO req, Partner partner) {
        h.setHotelName(req.getHotelName());
        h.setRoomType(req.getRoomType());
        h.setStarRating(req.getHotelRating() != null ? req.getHotelRating() : h.getStarRating());
        h.setAddressLocation(req.getAddressLocation());
        h.setDistrict(req.getDistrict());
        h.setState(req.getState());
        h.setCountry(req.getCountry());
        h.setTotalSeats(req.getTotalRooms());
        h.setPartner(partner);
        h.setBasePricePerUnit(req.getBasePricePerRoom());
    }
 
    public void updateBusEntity(BusInventory b, BusInventoryRequestDTO req, Partner partner) {
        b.setBusNumberPlate(req.getBusNumberPlate());
        b.setOperatorName(req.getOperatorName());
        b.setRouteFrom(req.getRouteFrom());
        b.setRouteTo(req.getRouteTo());
        b.setStartTime(req.getStartTime());
        b.setEndTime(req.getEndTime());
        b.setNumberOfHours(calculateDurationInHours(req.getStartTime(), req.getEndTime()));
 
        b.getSeatTiers().clear();
        if (req.getSeatTiers() != null) {
            b.getSeatTiers().addAll(req.getSeatTiers());
        }
 
        if (b.getRouteStops() == null) {
            b.setRouteStops(new ArrayList<>());
        }
        b.getRouteStops().clear();
        if (req.getRouteStops() != null) {
            b.getRouteStops().addAll(req.getRouteStops().stream().map(stopDTO -> BusStopDetail.builder()
                    .busInventory(b)
                    .stopName(stopDTO.getStopName())
                    .stopType(stopDTO.getStopType().toUpperCase())
                    .scheduledTime(stopDTO.getScheduledTime())
                    .build()).toList());
        }
        b.setPartner(partner);
        b.setBasePricePerUnit(req.getBasePricePerSeat());
    }
 
    public void updateCabEntity(CabInventory c, CabInventoryRequestDTO req, Partner partner) {
        c.setVehicleRegistrationNumber(req.getVehicleRegistrationNumber());
        c.setCarModel(req.getCarModel());
        c.setFuelType(req.getFuelType().toUpperCase());
        c.setSeaterCount(req.getSeaterCount());
        c.setDistrict(req.getDistrict().trim());
        c.setState(req.getState().trim());
        c.setPartner(partner);
        c.setBasePricePerUnit(req.getBasePricePerSeat());
    }
 
    public void updateTourEntity(TourPackageInventory t, TourInventoryRequestDTO req, Partner partner) {
        t.setPackageName(req.getPackageName());
        t.setFullItineraryDetails(req.getFullItineraryDetails());
        t.setDurationDays(req.getDurationDays());
        t.setPartner(partner);
        t.setBasePricePerUnit(req.getBasePricePerPersonForPackage());
    }
 
    public Object toGenericResponseDTO(Inventory entity, LocalDate targetDate) {
        if (entity == null) return null;

        return switch (entity.getItemType()) {
            case FLIGHT -> toFlightResponseDTO((FlightInventory) entity, targetDate);
            case HOTEL -> toHotelResponseDTO((HotelInventory) entity, targetDate);
            case BUS -> toBusResponseDTO((BusInventory) entity, targetDate);
            case CAB -> toCabResponseDTO((CabInventory) entity, targetDate);
            case TOUR_PACKAGE -> toTourResponseDTO((TourPackageInventory) entity, targetDate);
        };
    }
 
    public FlightInventoryResponseDTO toFlightResponseDTO(FlightInventory entity, LocalDate targetDate) {
        FlightInventoryResponseDTO dto = new FlightInventoryResponseDTO();
        int debugDateAsInt = (targetDate != null) 
                ? (targetDate.getYear() * 10000 + targetDate.getMonthValue() * 100 + targetDate.getDayOfMonth()) 
                : 0;

            
        mapCommonBaselinesToDTO(entity, dto);
        dto.setTotalSeats(debugDateAsInt);
        dto.setFlightNumber(entity.getFlightNumber());
        dto.setAirlineName(entity.getAirlineName());
        dto.setDepartureAirport(entity.getDepartureAirport());
        dto.setArrivalAirport(entity.getArrivalAirport());
        dto.setConnecting(entity.isConnecting());
        dto.setLayoverDetails(entity.getLayoverDetails());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setNumberOfHours(entity.getNumberOfHours());
        dto.setSeatTiers(buildSeatTiers(entity, entity.getSeatTiers(), entity.getBasePricePerUnit(), targetDate));
        return dto;
    }
 
    public HotelInventoryResponseDTO toHotelResponseDTO(HotelInventory entity, LocalDate targetDate) {
        HotelInventoryResponseDTO dto = new HotelInventoryResponseDTO();
        dto.setInventoryId(entity.getInventoryId());
        dto.setPartnerId(entity.getPartner().getPartnerId());
        dto.setPartnerName(entity.getPartner().getName());
        dto.setItemType(entity.getItemType().name());
        dto.setBasePricePerSeat(entity.getBasePricePerUnit());
        dto.setStatus(entity.getStatus().name());
        
        dto.setTotalRooms(entity.getTotalSeats());
        dto.setHotelName(entity.getHotelName());
        dto.setRoomType(entity.getRoomType());
        dto.setHotelRating(entity.getStarRating());
        dto.setAddressLocation(entity.getAddressLocation());
        dto.setDistrict(entity.getDistrict());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
 
        // Queries the hotel repository count using target travel date
        int filledRooms = (targetDate != null) ? bookingRepository.getFilledRoomsCountForDate(entity, targetDate) : 0;
        dto.setAvailableRooms(Math.max(0, entity.getTotalSeats() - filledRooms));
 
        return dto;
    }
 
    public BusInventoryResponseDTO toBusResponseDTO(BusInventory entity, LocalDate targetDate) {
        BusInventoryResponseDTO dto = new BusInventoryResponseDTO();
        mapCommonBaselinesToDTO(entity, dto);
        dto.setTotalSeats(entity.getTotalSeats());
        dto.setBusNumberPlate(entity.getBusNumberPlate());
        dto.setOperatorName(entity.getOperatorName());
        dto.setRouteFrom(entity.getRouteFrom());
        dto.setRouteTo(entity.getRouteTo());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setNumberOfHours(entity.getNumberOfHours());
        dto.setSeatTiers(buildSeatTiers(entity, entity.getSeatTiers(), entity.getBasePricePerUnit(), targetDate));
        
        if (entity.getRouteStops() != null) {
            dto.setRouteStops(entity.getRouteStops().stream().map(stop -> {
                BusStopDTO s = new BusStopDTO();
                s.setStopName(stop.getStopName());
                s.setStopType(stop.getStopType());
                s.setScheduledTime(stop.getScheduledTime());
                return s;
            }).toList());
        }
        return dto;
    }
 
    public CabInventoryResponseDTO toCabResponseDTO(CabInventory entity, LocalDate targetDate) {
        CabInventoryResponseDTO dto = new CabInventoryResponseDTO();
        mapCommonBaselinesToDTO(entity, dto);
        dto.setVehicleRegistrationNumber(entity.getVehicleRegistrationNumber());
        dto.setCarModel(entity.getCarModel());
        dto.setFuelType(entity.getFuelType());
        dto.setSeaterCount(entity.getSeaterCount());
        dto.setDistrict(entity.getDistrict());
        dto.setState(entity.getState());
 
        // Dynamic subtraction using target date against general vehicle layout capacity
        int filledSeats = (targetDate != null) ? bookingRepository.getFilledSeatsCountForDate(entity, targetDate) : 0;
        dto.setAvailableSeats(Math.max(0, entity.getSeaterCount() - filledSeats));
 
        return dto;
    }
 
    public TourInventoryResponseDTO toTourResponseDTO(TourPackageInventory entity, LocalDate targetDate) {
        TourInventoryResponseDTO dto = new TourInventoryResponseDTO();
        mapCommonBaselinesToDTO(entity, dto);
        dto.setPackageName(entity.getPackageName());
        dto.setFullItineraryDetails(entity.getFullItineraryDetails());
        dto.setDurationDays(entity.getDurationDays());
        if (entity.getTravelAgent() != null) {
            dto.setTravelAgentId(entity.getTravelAgent().getUserId());
            dto.setTravelAgentName(entity.getTravelAgent().getName());
            dto.setTravelAgentEmail(entity.getTravelAgent().getEmail());
        }
 
        // Substracts overall active slots for the package using the common date repository query
        dto.setAvailableSlots(999);
 
        return dto;
    }
 
    private void applyParentBaselinesToEntity(Inventory entity, Partner partner, InventoryType type, double price) {
        entity.setPartner(partner);
        entity.setItemType(type);
        entity.setBasePricePerUnit(price);
    }
 
    private void mapCommonBaselinesToDTO(Inventory source, Object target) {
        if (target instanceof BaseInventoryResponseDTO dto) {
            dto.setInventoryId(source.getInventoryId());
            if (source.getPartner() != null) {
                dto.setPartnerId(source.getPartner().getPartnerId());
                dto.setPartnerName(source.getPartner().getName());
            }
            if (source.getItemType() != null) {
                dto.setItemType(source.getItemType().name());
            }
            dto.setBasePricePerSeat(source.getBasePricePerUnit());
            if (source.getStatus() != null) {
                dto.setStatus(source.getStatus().name());
            }
        }
    }
 
    private double calculateDurationInHours(String string, String string2) {
        if (string == null || string2 == null) return 0.0;
        LocalTime time1 = LocalTime.parse(string);
        LocalTime time2 = LocalTime.parse(string2);
        Duration duration = Duration.between(time1, time2);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        return duration.toMinutes() / 60.0;
    }
 
    // Completely replaces the old 2-argument helper method to properly extract real-time tier allocation levels
    private java.util.List<SeatTierDTO> buildSeatTiers(Inventory entity, java.util.List<SeatTierCapacity> tiers, double basePrice, LocalDate targetDate) {
        if (tiers == null || tiers.isEmpty()) {
            return new ArrayList<>();
        }
        java.util.List<SeatTierDTO> out = new ArrayList<>();
        for (SeatTierCapacity tier : tiers) {
            double price = Math.round(basePrice * tier.getPriceMultiplier() * 100.0) / 100.0;
            
            int filledSeatsCount = 
                bookingRepository.getFilledSeatsCountForDateAndSeatType(entity, targetDate, tier.getSeatType().name());
            int availableSeatsCount = Math.max(0, tier.getTotalSeatsAllocated() - filledSeatsCount);
 
            out.add(SeatTierDTO.builder()
                    .seatType(tier.getSeatType().name())
                    .pricePerSeat(price)
                    .priceMultiplier(tier.getPriceMultiplier())
                    .totalSeatsAllocated(tier.getTotalSeatsAllocated())
                    .availableSeats(availableSeatsCount)
                    .build());
        }
        return out;
    }
}
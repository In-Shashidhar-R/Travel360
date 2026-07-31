package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumeration.BookingRequestStatus;
import com.cts.enumeration.InventoryType;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRequestRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.BookingRequestService;
import com.cts.service.BookingService;
import com.cts.util.InventoryManagementUtil;
import com.cts.util.UserSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingRequestServiceImpl implements BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final UserSecurityUtil userSecurityUtil;
    private final InventoryManagementUtil inventoryUtil;
    private final BookingService bookingService;

    @Override
    @Transactional
    public BookingRequestResponseDTO createRequest(BookingRequestCreateDTO req) {
        Long customerId = SecurityUtil.getCurrentUserId();
        User customer = userSecurityUtil.fetchUser(customerId);

        Inventory inventory = inventoryUtil.fetchInventory(req.getInventoryId());
        if (inventory.getItemType() != InventoryType.TOUR_PACKAGE) {
            throw new IllegalArgumentException(
                    "Booking requests are only supported on tour packages (inventory #"
                    + req.getInventoryId() + " is " + inventory.getItemType() + ")");
        }
        TourPackageInventory pkg = (TourPackageInventory) inventory;
        if (pkg.getTravelAgent() == null) {
            throw new IllegalStateException(
                    "Package #" + req.getInventoryId() + " has no travel agent assigned; "
                  + "book it directly via /api/v1/bookings/tour-package instead.");
        }

        BookingRequest entity = BookingRequest.builder()
                .customer(customer)
                .assignedAgent(pkg.getTravelAgent())
                .inventory(pkg)
                .status(BookingRequestStatus.PENDING)
                .customerRequirements(req.getCustomerRequirements())
                .requestedDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        return toDTO(bookingRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO acceptRequest(Long requestId, BookingRequestDecisionDTO decision) {
        BookingRequest r = fetchAsAssignedAgent(requestId);
        requireStatus(r, BookingRequestStatus.PENDING);
        r.setStatus(BookingRequestStatus.APPROVED);
        r.setAgentNotes(decision.getAgentNotes());
        r.setUpdatedDate(LocalDateTime.now());
        return toDTO(bookingRequestRepository.save(r));
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO rejectRequest(Long requestId, BookingRequestDecisionDTO decision) {
        BookingRequest r = fetchAsAssignedAgent(requestId);
        requireStatus(r, BookingRequestStatus.PENDING);
        r.setStatus(BookingRequestStatus.REJECTED);
        r.setAgentNotes(decision.getAgentNotes());
        r.setUpdatedDate(LocalDateTime.now());
        return toDTO(bookingRequestRepository.save(r));
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO completeRequestByBooking(Long requestId,
                                                              TourBookingRequestDTO bookingPayload) {
        BookingRequest r = fetchAsAssignedAgent(requestId);
        requireStatus(r, BookingRequestStatus.APPROVED);
        bookingPayload.setCustomerId(r.getCustomer().getUserId());
        bookingPayload.setInventoryId(r.getInventory().getInventoryId());
        TourBookingResponseDTO bookingDto = bookingService.bookTour(bookingPayload);

        r.setStatus(BookingRequestStatus.COMPLETED);
        r.setResultingBookingId(bookingDto.getBookingId());
        r.setUpdatedDate(LocalDateTime.now());
        return toDTO(bookingRequestRepository.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingRequestResponseDTO getRequestById(Long requestId) {
        BookingRequest r = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking request not found with ID: " + requestId));
        Long me = SecurityUtil.getCurrentUserId();
        if (!SecurityUtil.isAdmin()
                && !r.getCustomer().getUserId().equals(me)
                && !r.getAssignedAgent().getUserId().equals(me)) {
            throw new DataIsolationViolationException("You do not own booking request #" + requestId);
        }
        return toDTO(r);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingRequestResponseDTO> getall() {
        List<BookingRequest> requests = bookingRequestRepository.findAll();
        return requests.stream()
                .map(this::toDTO)
                .toList(); 
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingRequestResponseDTO> listMyCustomerRequests(Pageable pageable) {
        User me = userSecurityUtil.fetchUser(SecurityUtil.getCurrentUserId());
        Page<BookingRequest> page = bookingRequestRepository.findByCustomer(me, pageable);
        return PageResponse.from(page.map(this::toDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingRequestResponseDTO> listAssignedRequests(Pageable pageable) {
        User me = userSecurityUtil.fetchUser(SecurityUtil.getCurrentUserId());
        Page<BookingRequest> page = bookingRequestRepository.findByAssignedAgent(me, pageable);
        return PageResponse.from(page.map(this::toDTO));
    }

    private BookingRequest fetchAsAssignedAgent(Long requestId) {
        BookingRequest r = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking request not found with ID: " + requestId));
        Long me = SecurityUtil.getCurrentUserId();
        if (!SecurityUtil.isAdmin() && !r.getAssignedAgent().getUserId().equals(me)) {
            throw new DataIsolationViolationException(
                    "Only the assigned agent (or an admin) can act on booking request #" + requestId);
        }
        return r;
    }

    private void requireStatus(BookingRequest r, BookingRequestStatus expected) {
        if (r.getStatus() != expected) {
            throw new IllegalStateException(
                    "Booking request #" + r.getRequestId() + " is " + r.getStatus()
                    + "; expected " + expected);
        }
    }

    private BookingRequestResponseDTO toDTO(BookingRequest r) {
        String pkgName = null;
        if (r.getInventory() instanceof TourPackageInventory tp) {
            pkgName = tp.getPackageName();
        }
        return BookingRequestResponseDTO.builder()
                .requestId(r.getRequestId())
                .status(r.getStatus())
                .customerId(r.getCustomer().getUserId())
                .customerName(r.getCustomer().getName())
                .assignedAgentId(r.getAssignedAgent().getUserId())
                .assignedAgentName(r.getAssignedAgent().getName())
                .assignedAgentEmail(r.getAssignedAgent().getEmail())
                .inventoryId(r.getInventory().getInventoryId())
                .inventoryItemType(r.getInventory().getItemType().name())
                .packageName(pkgName)
                .customerRequirements(r.getCustomerRequirements())
                .agentNotes(r.getAgentNotes())
                .requestedDate(r.getRequestedDate())
                .updatedDate(r.getUpdatedDate())
                .resultingBookingId(r.getResultingBookingId())
                .build();
    }
}

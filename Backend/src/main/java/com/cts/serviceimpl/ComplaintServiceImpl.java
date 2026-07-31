package com.cts.serviceimpl;

import com.cts.dto.ComplaintCreateDTO;
import com.cts.dto.ComplaintResolveDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.entity.Booking;
import com.cts.entity.Complaint;
import com.cts.entity.User;
import com.cts.enumeration.ComplaintStatus;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.ComplaintRepository;
import com.cts.repository.UserRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.ComplaintService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ComplaintResponseDTO raiseComplaint(ComplaintCreateDTO request) {
        User raisedBy = currentUser();

        Booking relatedBooking = null;
        if (request.getRelatedBookingId() != null) {
            relatedBooking = bookingRepository.findById(request.getRelatedBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Booking not found with ID: " + request.getRelatedBookingId()));
            
            if (!relatedBooking.getCustomer().getUserId().equals(raisedBy.getUserId())) {
                throw new DataIsolationViolationException(
                        "You can only raise complaints against your own bookings.");
            }
        }

        Complaint complaint = Complaint.builder()
                .raisedBy(raisedBy)
                .relatedBooking(relatedBooking)
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(ComplaintStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return toDto(complaintRepository.save(complaint));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponseDTO> getComplaints(Long userId, ComplaintStatus status, Long bookingId, Pageable pageable) {
        Long targetUserId = userId;

        // Security Lockdown: Force customer to only see their own complaints
        boolean isStaff = SecurityUtil.isAdmin() || SecurityUtil.hasRole("COMPLIANCE_OFFICER");
        if (!isStaff) {
            targetUserId = SecurityUtil.getCurrentUserId();
        }

        final Long finalUserId = targetUserId;

        // Construct dynamic predicates matching provided filter criteria
        Specification<Complaint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (finalUserId != null) {
                predicates.add(cb.equal(root.get("raisedBy").get("userId"), finalUserId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (bookingId != null) {
                predicates.add(cb.equal(root.get("relatedBooking").get("bookingId"), bookingId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return PageResponse.from(complaintRepository.findAll(spec, pageable).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponseDTO getComplaintById(Long complaintId) {
        Complaint complaint = fetch(complaintId);
        Long me = SecurityUtil.getCurrentUserId();
        if (!SecurityUtil.isAdmin()
                && !SecurityUtil.hasRole("COMPLIANCE_OFFICER")
                && !complaint.getRaisedBy().getUserId().equals(me)) {
            throw new DataIsolationViolationException("You are not allowed to view this complaint.");
        }
        return toDto(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponseDTO markInProgress(Long complaintId, ComplaintResolveDTO request) {
        Complaint complaint = fetch(complaintId);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        
        if (request != null && request.getResolutionNote() != null) {
            complaint.setResolutionNote(request.getResolutionNote());
        }
        
        complaint.setUpdatedAt(LocalDateTime.now());
        return toDto(complaintRepository.save(complaint));
    }

    @Override
    @Transactional
    public ComplaintResponseDTO resolveComplaint(Long complaintId, ComplaintResolveDTO request) {
        Complaint complaint = fetch(complaintId); 
        complaint.setStatus(ComplaintStatus.RESOLVED);
        
        if (request != null && request.getResolutionNote() != null) {
            complaint.setResolutionNote(request.getResolutionNote());
        }
        
        complaint.setUpdatedAt(LocalDateTime.now());
        return toDto(complaintRepository.save(complaint));
    }

    private User currentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + userId));
    }

    private Complaint fetch(Long complaintId) {
        return complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));
    }

    private ComplaintResponseDTO toDto(Complaint c) {
        return ComplaintResponseDTO.builder()
                .complaintId(c.getComplaintId())
                .raisedByUserId(c.getRaisedBy().getUserId())
                .raisedByName(c.getRaisedBy().getName())
                .relatedBookingId(c.getRelatedBooking() != null ? c.getRelatedBooking().getBookingId() : null)
                .subject(c.getSubject())
                .description(c.getDescription())
                .status(c.getStatus().name())
                .resolutionNote(c.getResolutionNote())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
package com.cts.service;

import com.cts.dto.ComplaintCreateDTO;
import com.cts.dto.ComplaintResolveDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.enumeration.ComplaintStatus;
import org.springframework.data.domain.Pageable;

public interface ComplaintService {

    ComplaintResponseDTO raiseComplaint(ComplaintCreateDTO request);

    PageResponse<ComplaintResponseDTO> getComplaints(Long userId, ComplaintStatus status, Long bookingId, Pageable pageable);

    ComplaintResponseDTO getComplaintById(Long complaintId);

    ComplaintResponseDTO markInProgress(Long complaintId, ComplaintResolveDTO request);

    ComplaintResponseDTO resolveComplaint(Long complaintId, ComplaintResolveDTO request);
}
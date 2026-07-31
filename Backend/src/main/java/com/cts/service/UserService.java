package com.cts.service;


import com.cts.dto.AgentRequestDTO;
import com.cts.dto.ChangePasswordRequestDTO;
import com.cts.dto.ForgotPasswordRequestDTO;
import com.cts.dto.LoginRequestDTO;
import com.cts.dto.LoginResponseDTO;
import com.cts.dto.ResetPasswordDTO;
import com.cts.dto.UpdateUserRequestDTO;
import com.cts.dto.UserRequestDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDTO registerCustomer(UserRequestDTO request);
    UserResponseDTO addTravelAgent(AgentRequestDTO request, Long adminId);
    
    LoginResponseDTO authenticateUser(LoginRequestDTO request);
    String initiatePasswordRecovery(ForgotPasswordRequestDTO request);
    String finalizePasswordReset(ResetPasswordDTO request);
    
    PageResponse<UserResponseDTO> getAllUsers(String roleFilter, Pageable pageable);
    UserResponseDTO getUserById(Long userId);
    
    UserResponseDTO updateUserProfile(Long userId, UpdateUserRequestDTO dto, String authenticatedEmail);
    String changePassword(ChangePasswordRequestDTO dto, String authenticatedEmail);
}
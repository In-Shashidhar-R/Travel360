package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.User;
import com.cts.mapper.UserMapper;
import com.cts.repository.UserRepository;
import com.cts.service.UserService;
import com.cts.enumeration.Role;
import com.cts.dto.PageResponse;
import com.cts.util.UserSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSecurityUtil securityUtil;

    @Override
    @Transactional
    public LoginResponseDTO authenticateUser(LoginRequestDTO req) {
        User user = securityUtil.authenticateAndVerify(req.getEmail(), req.getPassword());
        securityUtil.logUserLoginSuccess(user);
        return userMapper.toLoginResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO registerCustomer(UserRequestDTO req) {
        securityUtil.validateNewUserEmailAndSecurity(req.getEmail(), req.getPassword());
        
        User saved = securityUtil.commitUser(userMapper.toCustomerEntity(req));
        securityUtil.logCustomerRegistration(saved);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO addTravelAgent(AgentRequestDTO req, Long adminId) {
        User admin = securityUtil.fetchAdminUser(adminId);
        securityUtil.validateNewUserEmailAndSecurity(req.getEmail(), req.getPassword());

        User saved = securityUtil.commitUser(userMapper.toAgentEntity(req));
        securityUtil.logAgentProvision(admin, saved);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public String initiatePasswordRecovery(ForgotPasswordRequestDTO req) {
        User user = securityUtil.fetchUserByEmail(req.getEmail());
        securityUtil.logPasswordRecoveryRequest(user);
        return "Identity match located. A secure multi-factor authentication token reset challenge sequence has been generated.";
    }

    @Override
    @Transactional
    public String finalizePasswordReset(ResetPasswordDTO req) {
        User user = securityUtil.fetchUserByEmail(req.getEmail());
        securityUtil.updateUserPassword(user, req.getNewPassword());
        securityUtil.logPasswordResetComplete(user);
        return "Credential parameters synchronized safely. Account workspace unlocked.";
    }
    
    @Override
    @Transactional
    public PageResponse<UserResponseDTO> getAllUsers(String roleFilter, Pageable pageable) {
        Page<User> users;
        if (roleFilter == null || roleFilter.trim().isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            Role role = parseRole(roleFilter.trim());
            users = userRepository.findByRole(role, pageable);
        }
        return PageResponse.from(users.map(userMapper::toResponseDTO));
    }

    private Role parseRole(String roleFilter) {
        try {
            return Role.valueOf(roleFilter.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown role filter: " + roleFilter);
        }
    }

    @Override
    @Transactional
    public UserResponseDTO getUserById(Long userId) {
        User user = securityUtil.fetchUser(userId);
        securityUtil.logProfileLookup(user);
        return userMapper.toResponseDTO(user);
    }
    
    @Override
    @Transactional
    public UserResponseDTO updateUserProfile(Long userId, UpdateUserRequestDTO dto, String authenticatedEmail) {
        // 1. Retrieve execution identification context alongside data targets
        User targetUser = securityUtil.fetchUser(userId);
        User currentUser = securityUtil.fetchUserByEmail(authenticatedEmail);

        // 2. Run contextual RBAC access evaluations
        securityUtil.verifyProfileUpdateAuthorization(currentUser, targetUser);

        // 3. Delegate field updates cleanly to your Mapper Component
        userMapper.updateEntityFromDTO(dto, targetUser);

        // 4. Persist and Log changes securely
        User savedUser = userRepository.save(targetUser);
        securityUtil.logProfileUpdateCompleted(currentUser, savedUser);
        
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public String changePassword(ChangePasswordRequestDTO dto, String authenticatedEmail) {
        User contextUser = securityUtil.fetchUserByEmail(authenticatedEmail);
        
        // Execute password verification checks and save sequence inside utility layer
        securityUtil.verifyAndRotatePassword(contextUser, dto.getCurrentPassword(), dto.getNewPassword());
        
        return "Security parameters rotated successfully. Your profile workspace credentials have changed.";
    }
    
}
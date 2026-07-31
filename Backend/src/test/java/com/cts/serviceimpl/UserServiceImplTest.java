package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.User;
import com.cts.enumeration.Role;
import com.cts.mapper.UserMapper;
import com.cts.repository.UserRepository;
import com.cts.util.UserSecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserSecurityUtil securityUtil;

    @InjectMocks private UserServiceImpl service;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void authenticateUser_returnsMappedDto() {
        LoginRequestDTO req = mock(LoginRequestDTO.class);
        when(req.getEmail()).thenReturn("a@a.com");
        when(req.getPassword()).thenReturn("pw");
        User user = mock(User.class);
        
        LoginResponseDTO dto = new LoginResponseDTO(); 
        
        when(securityUtil.authenticateAndVerify("a@a.com", "pw")).thenReturn(user);
        
        when(userMapper.toLoginResponseDTO(user)).thenReturn(dto); 

        assertSame(dto, service.authenticateUser(req));
        verify(securityUtil).logUserLoginSuccess(user);
    }

    @Test
    void registerCustomer_validatesCommitsAndMaps() {
        UserRequestDTO req = mock(UserRequestDTO.class);
        when(req.getEmail()).thenReturn("c@c.com");
        when(req.getPassword()).thenReturn("pw");
        User entity = mock(User.class);
        User saved = mock(User.class);
        UserResponseDTO dto = new UserResponseDTO();
        when(userMapper.toCustomerEntity(req)).thenReturn(entity);
        when(securityUtil.commitUser(entity)).thenReturn(saved);
        when(userMapper.toResponseDTO(saved)).thenReturn(dto);

        assertSame(dto, service.registerCustomer(req));
        verify(securityUtil).validateNewUserEmailAndSecurity("c@c.com", "pw");
        verify(securityUtil).logCustomerRegistration(saved);
    }

    @Test
    void addTravelAgent_fetchesAdminAndProvisions() {
        AgentRequestDTO req = mock(AgentRequestDTO.class);
        when(req.getEmail()).thenReturn("ag@a.com");
        when(req.getPassword()).thenReturn("pw");
        User admin = mock(User.class);
        User entity = mock(User.class);
        User saved = mock(User.class);
        UserResponseDTO dto = new UserResponseDTO();
        when(securityUtil.fetchAdminUser(7L)).thenReturn(admin);
        when(userMapper.toAgentEntity(req)).thenReturn(entity);
        when(securityUtil.commitUser(entity)).thenReturn(saved);
        when(userMapper.toResponseDTO(saved)).thenReturn(dto);

        assertSame(dto, service.addTravelAgent(req, 7L));
        verify(securityUtil).logAgentProvision(admin, saved);
    }

    @Test
    void initiatePasswordRecovery_returnsMessage() {
        ForgotPasswordRequestDTO req = mock(ForgotPasswordRequestDTO.class);
        when(req.getEmail()).thenReturn("x@x.com");
        User user = mock(User.class);
        when(securityUtil.fetchUserByEmail("x@x.com")).thenReturn(user);

        assertNotNull(service.initiatePasswordRecovery(req));
        verify(securityUtil).logPasswordRecoveryRequest(user);
    }

    @Test
    void finalizePasswordReset_updatesPassword() {
        ResetPasswordDTO req = mock(ResetPasswordDTO.class);
        when(req.getEmail()).thenReturn("x@x.com");
        when(req.getNewPassword()).thenReturn("newpw");
        User user = mock(User.class);
        when(securityUtil.fetchUserByEmail("x@x.com")).thenReturn(user);

        assertNotNull(service.finalizePasswordReset(req));
        verify(securityUtil).updateUserPassword(user, "newpw");
        verify(securityUtil).logPasswordResetComplete(user);
    }

    @Test
    void getAllUsers_noFilter_usesFindAll() {
        Page<User> page = new PageImpl<>(List.of(mock(User.class)));
        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toResponseDTO(any())).thenReturn(new UserResponseDTO());

        PageResponse<UserResponseDTO> result = service.getAllUsers(null, pageable);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getAllUsers_withFilter_usesFindByRole() {
        Page<User> page = new PageImpl<>(List.of(mock(User.class)));
        when(userRepository.findByRole(eq(Role.CUSTOMER), eq(pageable))).thenReturn(page);
        when(userMapper.toResponseDTO(any())).thenReturn(new UserResponseDTO());

        PageResponse<UserResponseDTO> result = service.getAllUsers("customer", pageable);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findByRole(Role.CUSTOMER, pageable);
    }

    @Test
    void getAllUsers_invalidFilter_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getAllUsers("NOPE", pageable));
    }

    @Test
    void getUserById_returnsMappedDto() {
        User user = mock(User.class);
        UserResponseDTO dto = new UserResponseDTO();
        when(securityUtil.fetchUser(3L)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(dto);

        assertSame(dto, service.getUserById(3L));
        verify(securityUtil).logProfileLookup(user);
    }
}

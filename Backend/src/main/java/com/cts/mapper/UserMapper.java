package com.cts.mapper;

import com.cts.dto.AgentRequestDTO;
import com.cts.dto.LoginResponseDTO;
import com.cts.dto.UpdateUserRequestDTO;
import com.cts.dto.UserRequestDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.User;
import com.cts.enumeration.Role;
import com.cts.util.JWTUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public User toCustomerEntity(UserRequestDTO dto) {
        if (dto == null) return null;
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.CUSTOMER)
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .build();
    }

    public User toAgentEntity(AgentRequestDTO dto) {
        if (dto == null) return null;
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.TRAVEL_AGENT)
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .state(dto.getState())
                .country(dto.getCountry())
                .agentBio(dto.getAgentBio())
                .agentExperienceYears(dto.getAgentExperienceYears())
                .build();
    }

    public UserResponseDTO toResponseDTO(User entity) {
        if (entity == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole().name());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setAgentBio(entity.getAgentBio());
        dto.setAgentExperienceYears(entity.getAgentExperienceYears());
        return dto;
    }
    
    public LoginResponseDTO toLoginResponseDTO(User entity) {
        if (entity == null) return null;
        String token = jwtUtil.generateToken(entity.getEmail(), entity.getUserId(), entity.getRole().name());
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole().name());
        dto.setToken(token);
        return dto;
    }
    
    

    public User toCustomAdminEntity(String name, String email, String rawPassword, String phone) {
        return User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .phone(phone)
                .build();
    }

    public User toCustomSeedUser(String name, String email, String rawPassword,
                                 String phone, Role role) {
        return User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .phone(phone)
                .build();
    }
    
    public void updateEntityFromDTO(UpdateUserRequestDTO dto, User entity) {
        if (dto == null || entity == null) return;
        
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setGender(dto.getGender());
        entity.setDateOfBirth(dto.getDateOfBirth());
        if (entity.getRole() == Role.TRAVEL_AGENT) {
            entity.setAgentBio(dto.getAgentBio());
            entity.setAgentExperienceYears(dto.getAgentExperienceYears());
        }
    }
}
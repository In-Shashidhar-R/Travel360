package com.cts.controller;

import com.cts.dto.LoginResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.dto.UserResponseDTO;
import com.cts.exception.GlobalExceptionHandler;
import com.cts.exception.ResourceNotFoundException;
import com.cts.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(ControllerTestSupport.NOOP_VALIDATOR)
                .build();
    }

    private UserResponseDTO sampleUser() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(1L);
        dto.setEmail("a@a.com");
        return dto;
    }
    
    private LoginResponseDTO loginUser() {
    	LoginResponseDTO dto = new LoginResponseDTO();
        dto.setUserId(1L);
        dto.setEmail("a@a.com");
        return dto;
    }

    @Test
    void register_returns201() throws Exception {
        when(userService.registerCustomer(any())).thenReturn(sampleUser());
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON).content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void addAgent_returns201() throws Exception {
        when(userService.addTravelAgent(any(), eq(5L))).thenReturn(sampleUser());
        mockMvc.perform(post("/api/v1/users/agent").param("adminId", "5")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void login_returns200() throws Exception {
        when(userService.authenticateUser(any())).thenReturn(loginUser());
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }


    @Test
    void resetPassword_returns200() throws Exception {
        when(userService.finalizePasswordReset(any())).thenReturn("done");
        mockMvc.perform(post("/api/v1/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_returns200() throws Exception {
        when(userService.getAllUsers(any(), any(Pageable.class)))
                .thenReturn(PageResponse.from(new PageImpl<>(List.of(sampleUser()))));
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isOk());
    }

    @Test
    void getUserById_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser());
        mockMvc.perform(get("/api/v1/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(9L)).thenThrow(new ResourceNotFoundException("User account not found with ID: 9"));
        mockMvc.perform(get("/api/v1/users/9").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}

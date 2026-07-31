package com.cts.dto;

import java.time.LocalDate;

import com.cts.enumeration.InventoryType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerResponseDTO {
    private Long partnerId;
    private String name;
    private InventoryType type;
    private String email;
    private String contactNumber;
    private String status;

    private Long loginUserId;
    private LocalDate dateOfBirth;
    private String address;
    private String gender;
    private String city;
    private String state;
    private String country;
    private String gstNumber;
    private Double commissionRate;
}
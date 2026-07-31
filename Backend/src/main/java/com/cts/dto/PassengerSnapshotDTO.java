package com.cts.dto;

import lombok.Data;

@Data
public class PassengerSnapshotDTO {
    private String name;
    private Integer age;
    private String gender;
    private String idProofType;
    private String idProofNumber;
}
package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "kpi_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KPIReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private String scope;
    private String metrics;
    private LocalDate generatedDate;
}
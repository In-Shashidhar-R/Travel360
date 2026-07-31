package com.cts.repository;

import com.cts.entity.KPIReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KPIReportRepository extends JpaRepository<KPIReport, Long> {
}
package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for shift definitions (name + daily time window).
 */
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /** List all shifts that belong to a company. */
    List<Shift> findAllByCompany_Id(Long companyId);

    /** Optional: deterministic ordering for nicer UI (morning before evening). */
    List<Shift> findAllByCompany_IdOrderByStartTimeAsc(Long companyId);

    /** Company guard helper — validate a shift belongs to the manager's company. */
    boolean existsByIdAndCompany_Id(Long id, Long companyId);
}

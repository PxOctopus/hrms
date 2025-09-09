package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for dated employee shift assignments.
 * We do NOT use @Where on the entity, so add "active = true" in queries where needed.
 */
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    // This returns both active and inactive since we did not put @Where on the entity.
    List<EmployeeShift> findAllByEmployee_Id(Long employeeId);

    // Same here; both active and inactive.
    List<EmployeeShift> findAllByShift_Id(Long shiftId);

    // Duplicate check finder that sees ANY row (active or inactive).
    // Service will enforce the correct behavior (error if another ACTIVE row exists, or reactivate if only inactive).
    Optional<EmployeeShift> findByEmployee_IdAndShiftDate(Long employeeId, LocalDate shiftDate);

    // Range listing for UI and 40h chip — only ACTIVE rows
    @Query("""
        select es
        from EmployeeShift es
        join fetch es.shift s
        where es.employee.id = :employeeId
          and es.active = true
          and es.shiftDate between :from and :to
        order by es.shiftDate asc, s.startTime asc
    """)
    List<EmployeeShift> findActiveByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to);

    // Company-scoped variant — only ACTIVE rows
    @Query("""
        select es
        from EmployeeShift es
        join fetch es.shift s
        where es.employee.id = :employeeId
          and es.employee.company.id = :companyId
          and es.active = true
          and es.shiftDate between :from and :to
        order by es.shiftDate asc, s.startTime asc
    """)
    List<EmployeeShift> findActiveByEmployeeAndRangeInCompany(Long employeeId, Long companyId, LocalDate from, LocalDate to);
}

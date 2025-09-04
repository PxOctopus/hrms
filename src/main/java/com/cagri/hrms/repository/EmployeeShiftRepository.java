package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for dated employee shift assignments.
 * Includes helpers for:
 *  - duplicate guard (same employee + same day)
 *  - weekly/range listing with FETCH JOIN to avoid N+1 on shift times
 *  - (optional) company-scoped range listing
 */
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    /** Find all assignments for an employee (rarely used directly; prefer ranged methods). */
    List<EmployeeShift> findAllByEmployee_Id(Long employeeId);

    /** Find all assignments that reference a given shift definition. */
    List<EmployeeShift> findAllByShift_Id(Long shiftId);

    /** Duplicate guard: prevent multiple assignments on the same date for the same employee. */
    Optional<EmployeeShift> findByEmployee_IdAndShiftDate(Long employeeId, LocalDate shiftDate);

    /**
     * Weekly / ranged listing with shift eagerly loaded to expose start/end times.
     * NOTE: This method is not company-scoped; enforce company checks in the service layer,
     * or use the company-scoped variant below.
     */
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

    /**
     * Company-scoped variant to further reduce data-leak risks at the query level.
     * Use this if you want an extra defense-in-depth in addition to service-layer checks.
     */
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

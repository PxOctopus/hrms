package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.Leave;
import com.cagri.hrms.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // Find all leaves of a given employee
    List<Leave> findByEmployee(Employee employee);

    // Find all leaves of an employee with specific status
    List<Leave> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

    // Find all leaves with a specific status (e.g., APPROVED, PENDING)
    List<Leave> findAllByStatus(LeaveStatus status);

    // Find leaves by type (if needed for reports, limits, etc.)
    List<Leave> findByLeaveDefinition(LeaveDefinition definition);

    List<Leave> findAllByCreatedBy_Id(Long managerId);

    List<Leave> findByStatusAndManager_Id(LeaveStatus status, Long managerId);

    List<Leave> findByEmployee_Company_IdAndStatus(Long companyId, LeaveStatus status);

    // --- NEW METHODS ---

    // --- NATIVE QUERIES (PostgreSQL) ---

    // Overlap check: a leave overlaps if (newStart <= existingEnd) AND (newEnd >= existingStart)
    @Query(value = // language=PostgreSQL
            """
            SELECT EXISTS (
              SELECT 1
                FROM leaves l
               WHERE l.employee_id = :employeeId
                 AND l.status      IN (:statuses)
                 AND l.start_date  <= :endDate
                 AND l.end_date    >= :startDate
            )
            """,
            nativeQuery = true)
    boolean existsOverlappingLeave(@Param("employeeId") Long employeeId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   // Pass enum names, e.g. ["PENDING","APPROVED"]
                                   @Param("statuses") List<String> statuses);

    // Simple yearly usage: count inclusive days for leaves whose START date falls in :year
    @Query(value = // language=PostgreSQL
            """
            SELECT COALESCE(SUM((l.end_date - l.start_date) + 1), 0)
              FROM leaves l
             WHERE l.employee_id = :employeeId
               AND l.leave_definition_id = :definitionId
               AND l.status IN (:statuses)
               AND EXTRACT(YEAR FROM l.start_date) = :year
            """,
            nativeQuery = true)
    Integer getUsedLeaveDays(@Param("employeeId") Long employeeId,
                             @Param("definitionId") Long definitionId,
                             @Param("statuses") List<String> statuses,
                             @Param("year") int year);

    // Window-clipped usage: correctly handles cross-year spans by clipping to [windowStart, windowEnd]
    @Query(value = // language=PostgreSQL
            """
            SELECT COALESCE(SUM(
                     (LEAST(l.end_date, :windowEnd) - GREATEST(l.start_date, :windowStart) + 1)
                   ), 0)
              FROM leaves l
             WHERE l.employee_id = :employeeId
               AND l.leave_definition_id = :definitionId
               AND l.status IN (:statuses)              -- pass enum names (e.g. ["PENDING","APPROVED"])
               AND l.end_date   >= :windowStart         -- must overlap the window
               AND l.start_date <= :windowEnd
            """,
            nativeQuery = true)
    Integer getUsedLeaveDaysInWindow(@Param("employeeId") Long employeeId,
                                     @Param("definitionId") Long definitionId,
                                     @Param("statuses") List<String> statuses,
                                     @Param("windowStart") LocalDate windowStart,
                                     @Param("windowEnd") LocalDate windowEnd);
}

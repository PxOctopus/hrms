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

    // ----- Standard query methods -----

    List<Leave> findByEmployee(Employee employee);

    List<Leave> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

    List<Leave> findAllByStatus(LeaveStatus status);

    List<Leave> findByLeaveDefinition(LeaveDefinition definition);

    List<Leave> findAllByCreatedBy_Id(Long managerId);

    List<Leave> findByStatusAndManager_Id(LeaveStatus status, Long managerId);

    List<Leave> findByEmployee_Company_IdAndStatus(Long companyId, LeaveStatus status);


    // ----- NEW: Native PostgreSQL queries for overlap and allowance -----

    /**
     * Overlap check (inclusive): a leave overlaps if (newStart <= existingEnd) AND (newEnd >= existingStart).
     * Pass enum names as statuses, e.g. ["PENDING","APPROVED"].
     */
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
                                   @Param("statuses") List<String> statuses);

    /**
     * Overlap check excluding the given leave id. Use this during approval so the row does not collide with itself.
     */
    @Query(value = // language=PostgreSQL
            """
            SELECT EXISTS (
              SELECT 1
                FROM leaves l
               WHERE l.employee_id = :employeeId
                 AND l.id <> :leaveId
                 AND l.status      IN (:statuses)
                 AND l.start_date  <= :endDate
                 AND l.end_date    >= :startDate
            )
            """,
            nativeQuery = true)
    boolean existsOverlappingLeaveExcept(@Param("employeeId") Long employeeId,
                                         @Param("leaveId") Long leaveId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("statuses") List<String> statuses);

    /**
     * Simple yearly usage: counts inclusive days for leaves whose START date falls in :year.
     * Useful for quick reports; for cross-year correctness prefer the window-based method below.
     */
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

    /**
     * Window-clipped usage: correctly sums days within [windowStart, windowEnd] (inclusive),
     * handling cross-year spans by clipping to the window bounds.
     */
    @Query(value = // language=PostgreSQL
            """
            SELECT COALESCE(SUM(
                     (LEAST(l.end_date, :windowEnd) - GREATEST(l.start_date, :windowStart) + 1)
                   ), 0)
              FROM leaves l
             WHERE l.employee_id = :employeeId
               AND l.leave_definition_id = :definitionId
               AND l.status IN (:statuses)          -- pass enum names (e.g. ["PENDING","APPROVED"])
               AND l.end_date   >= :windowStart     -- must overlap the window
               AND l.start_date <= :windowEnd
            """,
            nativeQuery = true)
    Integer getUsedLeaveDaysInWindow(@Param("employeeId") Long employeeId,
                                     @Param("definitionId") Long definitionId,
                                     @Param("statuses") List<String> statuses,
                                     @Param("windowStart") LocalDate windowStart,
                                     @Param("windowEnd") LocalDate windowEnd);


    // =========================
    // NEW: Assignment guard helper
    // =========================

    /**
     * NEW: Check if the employee has an APPROVED leave that covers the target date (inclusive).
     * This is used by the shift-assignment service to block assignments on leave days.
     * (JPQL version; portable across databases.)
     */
    @Query("""
      select (count(l) > 0) from Leave l
      where l.employee.id = :employeeId
        and l.status = :status
        and :date between l.startDate and l.endDate
    """)
    boolean existsApprovedOn(@Param("employeeId") Long employeeId,
                             @Param("date") LocalDate date,
                             @Param("status") LeaveStatus status);
}

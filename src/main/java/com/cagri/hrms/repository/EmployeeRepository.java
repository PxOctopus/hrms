package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// NEW: Added import for EntityGraph to prefetch associations when needed.
import org.springframework.data.jpa.repository.EntityGraph; // NEW: used by findWithUserAndCompanyById

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);

    Optional<Employee> findByEmail(String email);

    int countByCompanyId(Long companyId);

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByUser(User user);

    List<Employee> findByCompanyIdAndIsPendingApprovalByManagerTrue(Long companyId);

    // =========================
    // NEW: Additions for security & performance
    // =========================

    /**
     * NEW: Company guard helper.
     * Quick existence check to ensure the (employeeId, companyId) pair is valid,
     * allowing the service layer to block cross-company access without loading the entity.
     */
    boolean existsByIdAndCompany_Id(Long id, Long companyId); // NEW

    /**
     * NEW: Prefetch 'user' and 'company' to avoid LazyInitializationException/N+1
     * when mapping DTO fields like employee.user.fullName in the service layer.
     */
    @EntityGraph(attributePaths = {"user", "company"}) // NEW
    Optional<Employee> findWithUserAndCompanyById(Long id); // NEW
}

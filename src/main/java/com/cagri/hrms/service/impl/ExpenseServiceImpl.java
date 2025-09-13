package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.Expense;
import com.cagri.hrms.entity.expense.Project;
import com.cagri.hrms.entity.expense.ProjectAssignment;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.mapper.ExpenseMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.ExpenseRepository;
import com.cagri.hrms.repository.ProjectAssignmentRepository;
import com.cagri.hrms.repository.ProjectRepository;
import com.cagri.hrms.service.ExpenseService;
import com.cagri.hrms.service.PayrollService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final ProjectRepository projectRepo;
    private final ProjectAssignmentRepository assignmentRepo;
    private final ExpenseMapper mapper;
    private final PayrollService payrollService;

    @Override
    public ExpenseResponseDTO create(ExpenseCreateDTO dto, Employee employee) {
        Expense e = mapper.toEntity(dto);
        e.setEmployee(employee);

        // Resolve project: default to company-wide if null
        Project p = resolveProjectOrCompanyWide(dto.getProjectId(), employee.getCompany());
        e.setProject(p);

        // Compute net (simple policy; adjust as needed)
        BigDecimal tip = Optional.ofNullable(e.getTipAmount()).orElse(BigDecimal.ZERO);
        BigDecimal vat = Optional.ofNullable(e.getVatAmount()).orElse(BigDecimal.ZERO);
        e.setNetAmount(e.getGrossAmount().add(tip).add(vat));

        // Initial state
        e.setStatus(ExpenseStatus.DRAFT);
        expenseRepo.save(e);

        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, employee, false);
    }

    @Override
    public ExpenseResponseDTO update(Long id, ExpenseUpdateDTO dto, Employee employee) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        // Security: only owner can update in DRAFT/REJECTED
        if (!e.getEmployee().getId().equals(employee.getId()) ||
                !(e.getStatus() == ExpenseStatus.DRAFT || e.getStatus() == ExpenseStatus.REJECTED)) {
            throw new IllegalStateException("Not allowed to update this expense");
        }

        mapper.updateEntity(e, dto);

        Project p = resolveProjectOrCompanyWide(dto.getProjectId(), employee.getCompany());
        e.setProject(p);

        BigDecimal tip = Optional.ofNullable(e.getTipAmount()).orElse(BigDecimal.ZERO);
        BigDecimal vat = Optional.ofNullable(e.getVatAmount()).orElse(BigDecimal.ZERO);
        e.setNetAmount(e.getGrossAmount().add(tip).add(vat));

        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, employee, false);
    }

    @Override
    public ExpenseResponseDTO getById(Long id, Employee requester) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        boolean isManager = requester.getUser().getRole().getName().equals("MANAGER");
        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, requester, isManager);
    }

    @Override
    public Page<ExpenseResponseDTO> listMy(Employee employee, Pageable pageable) {
        return expenseRepo.findByEmployee(employee, pageable)
                .map(mapper::toDTO)
                .map(dto -> withAllowedActions(dto, employee, false));
    }

    @Override
    public ExpenseResponseDTO submit(Long id, Employee employee) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!e.getEmployee().getId().equals(employee.getId()) || e.getStatus() != ExpenseStatus.DRAFT && e.getStatus() != ExpenseStatus.REJECTED) {
            throw new IllegalStateException("Only draft/rejected expenses can be submitted by owner");
        }
        // Policy checks: backdate, receipt required, project assignment (if needed)
        validatePolicyOnSubmit(e);

        e.setStatus(ExpenseStatus.SUBMITTED);
        e.setSubmittedAt(java.time.LocalDateTime.now());

        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, employee, false);
    }

    @Override
    public Page<ExpenseResponseDTO> listSubmittedForCompany(Long companyId, Pageable pageable) {
        return expenseRepo.findByProjectCompanyIdAndStatus(companyId, ExpenseStatus.SUBMITTED, pageable)
                .map(mapper::toDTO);
    }

    @Override
    public ExpenseResponseDTO approve(Long id, Long managerUserId) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (e.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED expenses can be approved");
        }
        e.setStatus(ExpenseStatus.APPROVED);
        e.setManagerReviewerId(managerUserId);
        e.setManagerReviewedAt(java.time.LocalDateTime.now());

        // Create payroll adjustment (effective date = next payroll; replace with your logic)
        LocalDate effectiveDate = nextPayrollCutoff();
        payrollService.createReimbursementForExpense(
                e.getId(),
                e.getEmployee(),
                e.getNetAmount(), // assume TRY; convert if needed
                e.getCurrency(),
                effectiveDate
        );

        ExpenseResponseDTO out = mapper.toDTO(e);
        return out;
    }

    @Override
    public ExpenseResponseDTO reject(Long id, String reason, Long managerUserId) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (e.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED expenses can be rejected");
        }
        e.setStatus(ExpenseStatus.REJECTED);
        e.setManagerReviewerId(managerUserId);
        e.setManagerReviewedAt(java.time.LocalDateTime.now());
        e.setManagerDecisionNote(reason);

        return mapper.toDTO(e);
    }

    @Override
    public ExpenseResponseDTO withAllowedActions(ExpenseResponseDTO dto, Employee requester, boolean isManager) {
        // TODO: Build allowed actions array based on state + role
        // e.g., DRAFT -> ["EDIT","SUBMIT","DELETE"], SUBMITTED (manager) -> ["APPROVE","REJECT"]
        return dto;
    }

    // --- helpers ---

    private Project resolveProjectOrCompanyWide(Long projectId, Company company) {
        if (projectId != null) {
            return projectRepo.findById(projectId).orElseThrow(EntityNotFoundException::new);
        }
        return projectRepo.findByCompanyAndIsGenericTrue(company)
                .orElseThrow(() -> new IllegalStateException("Company-wide project missing for company"));
    }

    private void validatePolicyOnSubmit(Expense e) {
        // TODO: receipt required, backdate limit, category caps, assignment check if project.requiresAssignment
        if (e.getProject() != null && !e.getProject().isGeneric() && e.getProject().isRequiresAssignment()) {
            ProjectAssignment pa = assignmentRepo.findByProjectAndEmployee(e.getProject(), e.getEmployee())
                    .orElseThrow(() -> new IllegalStateException("Project assignment not found"));
            if (!pa.isAccepted()) throw new IllegalStateException("Project assignment not accepted");
        }
    }

    private LocalDate nextPayrollCutoff() {
        // TODO: replace with real logic; for now return end-of-month
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(today.lengthOfMonth());
    }
}

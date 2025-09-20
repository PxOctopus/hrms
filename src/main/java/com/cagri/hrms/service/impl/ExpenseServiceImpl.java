package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.Expense;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import com.cagri.hrms.enums.expense.PayrollAdjustmentType;
import com.cagri.hrms.mapper.ExpenseMapper;
import com.cagri.hrms.repository.ExpenseRepository;
import com.cagri.hrms.repository.PayrollAdjustmentRepository;
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
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cagri.hrms.enums.expense.ExpenseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final ExpenseMapper mapper;
    private final PayrollService payrollService;
    private final PayrollAdjustmentRepository payrollAdjustmentRepo;


    // -------------------------
    // Employee-scope operations
    // -------------------------

    @Override
    public ExpenseResponseDTO create(ExpenseCreateDTO dto, Employee employee) {
        // Map basic fields; owner is set here (never from client)
        Expense e = mapper.toEntity(dto);
        e.setEmployee(employee);

        // Compute net amount (service rule for MVP)
        e.setNetAmount(computeNet(e));

        // Status starts at DRAFT (enforced by mapper constant as well)
        e.setStatus(DRAFT);

        expenseRepo.save(e);

        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, employee, false);
    }

    @Override
    public ExpenseResponseDTO update(Long id, ExpenseUpdateDTO dto, Employee employee) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);

        // Only owner, only when DRAFT/REJECTED
        if (!e.getEmployee().getId().equals(employee.getId()) ||
                !(e.getStatus() == DRAFT || e.getStatus() == REJECTED)) {
            throw new IllegalStateException("Not allowed to update this expense");
        }

        // Apply partial updates; mapper ignores nulls
        mapper.updateEntity(e, dto);

        // Recompute net after updates
        e.setNetAmount(computeNet(e));

        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, employee, false);
    }

    @Override
    public ExpenseResponseDTO getById(Long id, Employee requester) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);

        // Simple role check based on requester's role name
        boolean isManager = "MANAGER".equalsIgnoreCase(requester.getUser().getRole().getName());

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

        // Only owner; must be DRAFT or REJECTED
        if (!e.getEmployee().getId().equals(employee.getId()) ||
                (e.getStatus() != DRAFT && e.getStatus() != REJECTED)) {
            throw new IllegalStateException("Only draft/rejected expenses can be submitted by owner");
        }

        // Move to SUBMITTED and stamp time
        e.submit();

        ExpenseResponseDTO out = mapper.toDTO(e);
        return withAllowedActions(out, employee, false);
    }

    @Override
    public void delete(Long id, Employee employee) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);

        // Only owner; only when DRAFT/REJECTED
        if (!e.getEmployee().getId().equals(employee.getId()) ||
                !(e.getStatus() == DRAFT || e.getStatus() == REJECTED)) {
            throw new IllegalStateException("Not allowed to delete this expense");
        }
        expenseRepo.delete(e);
    }

    @Override
    public ExpenseResponseDTO withdraw(Long id, Employee employee) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);

        // Only owner; only when SUBMITTED
        if (!e.getEmployee().getId().equals(employee.getId()) || e.getStatus() != SUBMITTED) {
            throw new IllegalStateException("Only submitted expenses can be withdrawn by owner");
        }

        // Revert to DRAFT (clear submit time)
        e.setStatus(DRAFT);
        e.setSubmittedAt(null);

        return mapper.toDTO(e);
    }

    // -------------------------
    // Manager-scope operations
    // -------------------------

    @Override
    public Page<ExpenseResponseDTO> listSubmittedForCompany(Long companyId, Pageable pageable) {
        // Query by employee.company.id (Project is not used in MVP)
        return expenseRepo.findByEmployeeCompanyIdAndStatus(companyId, SUBMITTED, pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public ExpenseResponseDTO approve(Long expenseId, Long managerUserId) {
        Expense e = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

        if (e.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED can be approved");
        }

        e.approve(managerUserId, "Approved");
        expenseRepo.save(e);

        // Create QUEUED adjustment once (if not exists)
        if (!payrollAdjustmentRepo.existsByExpenseId(e.getId())) {
            PayrollAdjustment adj = PayrollAdjustment.builder()
                    .employee(e.getEmployee())
                    .type(PayrollAdjustmentType.REIMBURSEMENT)
                    .amount(e.getNetAmount() != null ? e.getNetAmount() : e.getGrossAmount())
                    .currency(e.getCurrency())
                    .description("Expense #" + e.getId() + " - " + e.getCategory())
                    .effectiveDate(endOfMonth(e.getExpenseDate()))
                    .processed(false)
                    .expenseId(e.getId())
                    .build();
            payrollAdjustmentRepo.save(adj);
        }

        return mapper.toDTO(e);
    }

    @Override
    public ExpenseResponseDTO reject(Long id, String reason, Long managerUserId) {
        Expense e = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);

        if (e.getStatus() != SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED expenses can be rejected");
        }

        // Transition and stamp reviewer/note
        e.reject(managerUserId, reason);

        return mapper.toDTO(e);
    }

    // -------------------------
    // UI permissions composer
    // -------------------------

    @Override
    public ExpenseResponseDTO withAllowedActions(ExpenseResponseDTO dto, Employee requester, boolean isManager) {
        List<String> actions = new ArrayList<>();

        if (isManager) {
            if (dto.getStatus() == SUBMITTED) {
                actions.add("APPROVE");
                actions.add("REJECT");
            }
        } else {
            if (dto.getStatus() == DRAFT || dto.getStatus() == REJECTED) {
                actions.add("EDIT");
                actions.add("SUBMIT");
                actions.add("DELETE");
            }
            if (dto.getStatus() == SUBMITTED) {
                actions.add("WITHDRAW");
            }
        }

        if (dto.getReceiptFiles() != null && !dto.getReceiptFiles().isEmpty()) {
            actions.add("DOWNLOAD_RECEIPTS");
        }

        dto.setAllowedActions(actions);
        return dto;
    }

    // -------------------------
    // Helpers
    // -------------------------

    /** MVP rule: net = gross + (vat or 0) + (tip or 0). */
    private BigDecimal computeNet(Expense e) {
        BigDecimal tip = Optional.ofNullable(e.getTipAmount()).orElse(BigDecimal.ZERO);
        BigDecimal vat = Optional.ofNullable(e.getVatAmount()).orElse(BigDecimal.ZERO);
        return Optional.ofNullable(e.getGrossAmount()).orElse(BigDecimal.ZERO)
                .add(tip)
                .add(vat);
    }

    /** Simple cutoff: last day of current month. Adapt if you have real payroll cycles. */
    private LocalDate nextPayrollCutoff() {
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(today.lengthOfMonth());
    }



        @Override
        public Page<ExpenseResponseDTO> findCompanyExpenses(Long companyId,
                ExpenseStatus status,
                Pageable pageable) {
            Page<Expense> page = (status == null)
                    ? expenseRepo.findByEmployeeCompanyId(companyId, pageable)             // ALL
                    : expenseRepo.findByEmployeeCompanyIdAndStatus(companyId, status, pageable);

            return page.map(mapper::toDTO);
        }

    @Transactional
    public ExpenseResponseDTO markPaid(Long expenseId, Long financeUserId) {
        Expense e = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

        if (e.getStatus() != ExpenseStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED expenses can be marked paid");
        }

        if (e.getPaidAt() == null) {
            e.setPaidAt(LocalDateTime.now());
            expenseRepo.save(e);
        }

        PayrollAdjustment adj =  payrollAdjustmentRepo.findByExpenseId(expenseId).orElseGet(() ->
                // Edge-case
                PayrollAdjustment.builder()
                        .employee(e.getEmployee())
                        .type(PayrollAdjustmentType.REIMBURSEMENT)
                        .amount(e.getNetAmount() != null ? e.getNetAmount() : e.getGrossAmount())
                        .currency(e.getCurrency())
                        .description("Expense #" + e.getId() + " - " + e.getCategory())
                        .effectiveDate(endOfMonth(e.getExpenseDate()))
                        .expenseId(e.getId())
                        .processed(false)
                        .build()
        );
        adj.setProcessed(true);            // Paid
        payrollAdjustmentRepo.save(adj);

        return mapper.toDTO(e);
    }

    private LocalDate endOfMonth(LocalDate d) {
        return d.with(TemporalAdjusters.lastDayOfMonth());
    }

    @Override
    public Page<ExpenseResponseDTO> findCompanyExpenses(Long companyId,
                                                        ExpenseStatus status,
                                                        Boolean paidOnly,
                                                        Pageable pageable) {
        Page<Expense> page;

        boolean onlyPaid = Boolean.TRUE.equals(paidOnly);

        if (status == null) {
            page = onlyPaid
                    ? expenseRepo.findByEmployeeCompanyIdAndPaidAtIsNotNull(companyId, pageable)
                    : expenseRepo.findByEmployeeCompanyId(companyId, pageable);
        } else {
            page = onlyPaid
                    ? expenseRepo.findByEmployeeCompanyIdAndStatusAndPaidAtIsNotNull(companyId, status, pageable)
                    : expenseRepo.findByEmployeeCompanyIdAndStatus(companyId, status, pageable);
        }

        return page.map(mapper::toDTO);
    }
    }


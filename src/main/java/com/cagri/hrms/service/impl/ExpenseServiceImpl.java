package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.response.employee.ExpenseResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.Expense;
import com.cagri.hrms.enums.ExpenseStatus;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.mapper.ExpenseMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.ExpenseRepository;
import com.cagri.hrms.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto, User currentUser) {
        // Only the employee can create their own expense
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Employee not found"));

        if (!isSelf(currentUser, employee)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only create expenses for yourself.");
        }

        Expense expense = expenseMapper.toEntity(dto);
        expense.setEmployee(employee);
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setActive(true);

        expense = expenseRepository.save(expense);

        return expenseMapper.toDto(expense);
    }

    @Override
    public ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO dto, User currentUser) {
        // Only the employee can update their own expense, and only if it's still pending
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Expense not found"));

        if (!isSelf(currentUser, expense.getEmployee())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only update your own expenses.");
        }
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "You can only update expenses that are still pending.");
        }

        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setFileUrl(dto.getFileUrl());
        // Do not change status or employee here

        expense = expenseRepository.save(expense);

        return expenseMapper.toDto(expense);
    }

    @Override
    public void deleteExpense(Long id, User currentUser) {
        // Only the employee can delete their own expense, and only if it's still pending
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Expense not found"));

        if (!isSelf(currentUser, expense.getEmployee())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only delete your own expenses.");
        }
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "You can only delete expenses that are still pending.");
        }

        expense.setActive(false); // Soft delete
        expenseRepository.save(expense);
    }

    @Override
    public ExpenseResponseDTO getExpenseById(Long id, User currentUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Expense not found"));

        if (isSelf(currentUser, expense.getEmployee()) || isManagerOf(currentUser, expense.getEmployee())) {
            return expenseMapper.toDto(expense);
        }
        throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You are not authorized to view this expense.");
    }

    @Override
    public List<ExpenseResponseDTO> getExpensesByEmployeeId(Long employeeId, User currentUser) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Employee not found"));

        if (isSelf(currentUser, employee) || isManagerOf(currentUser, employee)) {
            return expenseRepository.findAllByEmployee_Id(employeeId)
                    .stream()
                    .map(expenseMapper::toDto)
                    .collect(Collectors.toList());
        }
        throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You are not authorized to view this employee's expenses.");
    }

    @Override
    public List<ExpenseResponseDTO> getAllExpenses(User currentUser) {
        // Only MANAGER can see all company expenses
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can view all expenses.");
        }
        // Get all employees in the same company
        return expenseRepository.findAll().stream()
                .filter(expense -> expense.getEmployee().getCompany().getId().equals(currentUser.getCompany().getId()))
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void approveExpense(Long id, String managerNote, User currentUser) {
        // Only MANAGER can approve expenses in their own company
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Expense not found"));
        if (!isManagerOf(currentUser, expense.getEmployee())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You are not authorized to approve this expense.");
        }
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setManagerNote(managerNote);
        expenseRepository.save(expense);
    }

    @Override
    public void rejectExpense(Long id, String managerNote, User currentUser) {
        // Only MANAGER can reject expenses in their own company
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Expense not found"));
        if (!isManagerOf(currentUser, expense.getEmployee())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You are not authorized to reject this expense.");
        }
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setManagerNote(managerNote);
        expenseRepository.save(expense);
    }


    // Returns true if currentUser is the employee (self)
    private boolean isSelf(User currentUser, Employee employee) {
        return employee.getUser() != null && currentUser.getId().equals(employee.getUser().getId());
    }

    // Returns true if currentUser is MANAGER of employee's company
    private boolean isManagerOf(User currentUser, Employee employee) {
        return isManager(currentUser)
                && employee.getCompany() != null
                && currentUser.getCompany() != null
                && employee.getCompany().getId().equals(currentUser.getCompany().getId());
    }

    // Returns true if user has MANAGER role
    private boolean isManager(User currentUser) {
        return currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getName());
    }
}

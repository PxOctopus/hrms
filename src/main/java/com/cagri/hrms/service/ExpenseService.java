package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.response.employee.ExpenseResponseDTO;
import com.cagri.hrms.entity.core.User;

import java.util.List;

public interface ExpenseService {
    ExpenseResponseDTO createExpense(ExpenseRequestDTO dto, User currentUser);
    ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO dto, User currentUser);
    void deleteExpense(Long id, User currentUser);
    ExpenseResponseDTO getExpenseById(Long id, User currentUser);
    List<ExpenseResponseDTO> getExpensesByEmployeeId(Long employeeId, User currentUser);
    List<ExpenseResponseDTO> getAllExpenses(User currentUser);
    void approveExpense(Long id, String managerNote, User currentUser);
    void rejectExpense(Long id, String managerNote, User currentUser);
}

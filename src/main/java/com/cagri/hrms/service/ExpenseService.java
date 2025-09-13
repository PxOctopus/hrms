package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExpenseService {

    ExpenseResponseDTO create(ExpenseCreateDTO dto, Employee employee);

    ExpenseResponseDTO update(Long id, ExpenseUpdateDTO dto, Employee employee);

    ExpenseResponseDTO getById(Long id, Employee requester);

    Page<ExpenseResponseDTO> listMy(Employee employee, Pageable pageable);

    ExpenseResponseDTO submit(Long id, Employee employee);

    // Manager scope (company)
    Page<ExpenseResponseDTO> listSubmittedForCompany(Long companyId, Pageable pageable);

    ExpenseResponseDTO approve(Long id, Long managerUserId);

    ExpenseResponseDTO reject(Long id, String reason, Long managerUserId);

    // Utility to compute allowed actions for UI
    ExpenseResponseDTO withAllowedActions(ExpenseResponseDTO dto, Employee requester, boolean isManager);
}

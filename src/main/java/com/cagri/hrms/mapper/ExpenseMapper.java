package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.expense.Expense;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.User;
import org.mapstruct.*;

// Use IGNORE to avoid warnings if DTOs don't carry all fields
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExpenseMapper {

    // CreateDTO -> Entity (only safe fields; employee set in service from auth)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true) // set from auth context in service
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "managerReviewedAt", ignore = true)
    @Mapping(target = "managerReviewerId", ignore = true)
    @Mapping(target = "managerDecisionNote", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
//  @Mapping(target = "financeUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(ExpenseCreateDTO dto);

    // UpdateDTO -> existing Entity (editable only in DRAFT/REJECTED; enforce in service)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "managerReviewedAt", ignore = true)
    @Mapping(target = "managerReviewerId", ignore = true)
    @Mapping(target = "managerDecisionNote", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
//  @Mapping(target = "financeUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Expense entity, ExpenseUpdateDTO dto);

    // Entity -> ResponseDTO
    // `allowedActions` is typically filled in the service based on status/ownership
    @Mapping(target = "allowedActions", ignore = true)

    // Manager list/detail
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeEmail", source = "employee.email")
    @Mapping(target = "employeeName", expression = "java(resolveEmployeeName(entity))")
    ExpenseResponseDTO toDTO(Expense entity);

    // ---- Helpers ----

    /** Build a friendly employee display name.
     *  Priority: employee.user.fullName -> employee.email -> null
     */
    default String resolveEmployeeName(Expense e) {
        if (e == null) return null;
        Employee emp = e.getEmployee();
        if (emp == null) return null;

        // If User is present and has a non-blank fullName, prefer it
        User u = emp.getUser();
        if (u != null) {
            String full = u.getFullName();
            if (full != null && !full.isBlank()) {
                return full;
            }
        }

        // fallback to employee email
        return emp.getEmail();
    }
}

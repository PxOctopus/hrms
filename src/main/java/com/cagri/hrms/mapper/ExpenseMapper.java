package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.response.employee.ExpenseResponseDTO;
import com.cagri.hrms.entity.employee.Expense;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    Expense toEntity(ExpenseRequestDTO dto);
    ExpenseResponseDTO toDto(Expense expense);
}

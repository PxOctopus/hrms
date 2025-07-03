package com.cagri.hrms.dto.response.employee;

import com.cagri.hrms.enums.ContractType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private String fullName;         // user.getFullName()
    private String email;            // user.getEmail()
    private String companyName;      // company.getCompanyName()
    private LocalDate birthDate;
    private LocalDate hireDate;
    private LocalDate endDate;
    private String position;
    private ContractType contractType;
    private String phoneNumber;
    private String address;
    private BigDecimal salary;
    private Integer annualLeave;
    private boolean isActive;
    private Long createdAt;
    private Long updatedAt;
}

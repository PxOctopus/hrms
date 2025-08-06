package com.cagri.hrms.dto.request.employee;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeUpdateProfileRequestDTO {
    private String phoneNumber;
    private String address;
    private LocalDate birthDate;
}

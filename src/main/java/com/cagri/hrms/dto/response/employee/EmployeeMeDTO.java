package com.cagri.hrms.dto.response.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeMeDTO {
    private Boolean pendingApprovalByManager;
}

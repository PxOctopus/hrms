package com.cagri.hrms.dto.response.employee;

import com.cagri.hrms.enums.AssetStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeAssetResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String employeeFullName;

    // NEW:
    private String serialNumber;
    private AssetStatus status;
    private boolean confirmed;
    private LocalDate assignedDate;
}

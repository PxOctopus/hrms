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


    private String serialNumber;
    private AssetStatus status;
    private boolean confirmed;
    private LocalDate assignedDate;

    // NEW: show "Undo Issue" only if MAINTENANCE/LOST and manager has NOT confirmed yet
    private boolean issueUndoable;
}

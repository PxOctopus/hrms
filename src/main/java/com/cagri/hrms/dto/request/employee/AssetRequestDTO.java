package com.cagri.hrms.dto.request.employee;

import lombok.Data;

@Data
public class AssetRequestDTO {
    private String name;
    private String description;
    private Long employeeId;
}

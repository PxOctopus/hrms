package com.cagri.hrms.dto.response.employee;

import lombok.Data;

@Data
public class AssetResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String employeeFullName;
}

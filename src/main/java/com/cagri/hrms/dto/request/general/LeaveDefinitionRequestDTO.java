package com.cagri.hrms.dto.request.general;

import lombok.Data;

@Data
public class LeaveDefinitionRequestDTO {
    private String name;
    private Integer maxDays;
    private boolean active;
}

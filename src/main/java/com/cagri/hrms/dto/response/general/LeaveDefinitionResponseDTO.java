package com.cagri.hrms.dto.response.general;

import lombok.Data;

@Data
public class LeaveDefinitionResponseDTO {
    private Long id;
    private String name;
    private Integer maxDays;
    private boolean active;
}

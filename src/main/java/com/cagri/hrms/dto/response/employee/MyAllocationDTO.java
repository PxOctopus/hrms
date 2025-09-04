package com.cagri.hrms.dto.response.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAllocationDTO {
    private Long leaveDefinitionId;
    private Integer totalDays; // annual leaves assigned by manager
    private Integer usedDays;
}

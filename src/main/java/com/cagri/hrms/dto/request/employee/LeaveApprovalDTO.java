package com.cagri.hrms.dto.request.employee;

import lombok.Data;

@Data
public class LeaveApprovalDTO {
    private Long leaveId;
    private boolean approved;
}

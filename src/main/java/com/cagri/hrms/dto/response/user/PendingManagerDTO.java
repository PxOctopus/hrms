package com.cagri.hrms.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingManagerDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String pendingCompanyName;
}

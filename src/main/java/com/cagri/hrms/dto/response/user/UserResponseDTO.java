package com.cagri.hrms.dto.response.user;

import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private final Long id;
    private final String fullName;
    private final String email;
    private String role;
    private String pendingCompanyName;
    private CompanyResponseDTO company;
    private boolean companyApproved;
    private boolean mustChangePassword;

    private String phoneNumber;
    private String address;
}

package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.exception.BusinessException;
import com.cagri.hrms.exception.ResourceNotFoundException;
import com.cagri.hrms.mapper.CompanyMapper;
import com.cagri.hrms.mapper.UserMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.cagri.hrms.enums.EmployeeLimitLevel;
import com.cagri.hrms.enums.SubscriptionPlan;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        // Get the currently authenticated user from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("No authenticated user found.");
        }

        Object principal = auth.getPrincipal();
        User manager;

        if (principal instanceof com.cagri.hrms.security.CustomUserDetails customUserDetails) {
            manager = customUserDetails.getUser();
        } else {
            throw new BusinessException("Unsupported principal type: " + principal.getClass().getName());
        }

        // Prevent duplicate company creation by name (case-insensitive)
        if (companyRepository.existsByCompanyNameIgnoreCase(dto.getCompanyName())) {
            throw new BusinessException("Company already exists with name: " + dto.getCompanyName());
        }

        // Map DTO to Company entity using mapper
        Company company = companyMapper.toEntity(dto, manager);

        // Set audit fields (since updateAt was ignored in mapping)
        company.setUpdateAt(System.currentTimeMillis());

        // Save and return the company
        Company saved = companyRepository.save(company);
        return companyMapper.toDTO(saved);
    }

    @Override
    public CompanyResponseDTO approvePendingCompany(Long userId) {
        // 1. Retrieve the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 2. Check if the user is already associated with a company
        if (user.getCompany() != null) {
            throw new BusinessException("User is already associated with a company.");
        }

        // 3. Check if the user has a pending company name
        if (user.getPendingCompanyName() == null || user.getPendingCompanyName().isBlank()) {
            throw new BusinessException("User has no pending company name.");
        }

        // 4. Prepare a DTO to create a new company
        CompanyRequestDTO companyRequest = new CompanyRequestDTO();
        companyRequest.setCompanyName(user.getPendingCompanyName());
        companyRequest.setEmployeeLimitLevel(EmployeeLimitLevel.SMALL); // or dynamically determine
        companyRequest.setEmployeeNumberLimit(10);                         // default limit
        companyRequest.setSubscriptionPlan(SubscriptionPlan.FREE);       // default plan

        // 5. Convert the DTO to a Company entity using the mapper
        Company newCompany = companyMapper.toEntity(companyRequest, user);

        // 6. Set the company email if required
        newCompany.setCompanyEmail(user.getEmail());

        // 7. Save the new company
        Company savedCompany = companyRepository.save(newCompany);

        // 8. Update the user to link the new company and clear the pending company name
        user.setCompany(savedCompany);
        user.setPendingCompanyName(null);
        userRepository.save(user);

        // 9. Return the saved company as a response DTO
        return companyMapper.toDTO(savedCompany);
    }

    @Override
    public List<UserResponseDTO> getPendingCompanyManagers() {
        return userRepository.findByRoleNameAndPendingCompanyNameNotNull("MANAGER")
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyResponseDTO> getAllCompanies() {
        // Return all companies as DTOs
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CompanyResponseDTO getCompanyById(Long id) {
        // Find company by ID or throw exception
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        return companyMapper.toDTO(company);
    }

    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
        // Find company and update its fields
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        company.setCompanyName(dto.getCompanyName());
        company.setEmployeeLimitLevel(dto.getEmployeeLimitLevel());
        company.setEmployeeNumberLimit(dto.getEmployeeNumberLimit());
        company.setSubscriptionPlan(dto.getSubscriptionPlan());
        company.setUpdateAt(System.currentTimeMillis());

        Company updated = companyRepository.save(company);
        return companyMapper.toDTO(updated);
    }

    @Override
    public void deleteCompany(Long id) {
        // Delete company if exists
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        companyRepository.delete(company);
    }
}

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

//    @Override
//    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
//        // Get the currently authenticated user from the security context
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !auth.isAuthenticated()) {
//            throw new BusinessException("No authenticated user found.");
//        }
//
//        Object principal = auth.getPrincipal();
//        User manager;
//
//        if (principal instanceof com.cagri.hrms.security.CustomUserDetails customUserDetails) {
//            manager = customUserDetails.getUser();
//        } else {
//            throw new BusinessException("Unsupported principal type: " + principal.getClass().getName());
//        }
//
//        // Prevent duplicate company creation by name (case-insensitive)
//        if (companyRepository.existsByCompanyNameIgnoreCase(dto.getCompanyName())) {
//            throw new BusinessException("Company already exists with name: " + dto.getCompanyName());
//        }
//
//        // Map DTO to Company entity using mapper
//        Company company = companyMapper.toEntity(dto, manager);
//
//        // Set audit fields (since updateAt was ignored in mapping)
//        company.setUpdateAt(System.currentTimeMillis());
//
//        // Save and return the company
//        Company saved = companyRepository.save(company);
//        return companyMapper.toDTO(saved);
//    }

    @Override
    public CompanyResponseDTO approvePendingCompany(Long userId) {
        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + userId));

        if (!"MANAGER".equalsIgnoreCase(manager.getRole().getName())) {
            throw new BusinessException("Only managers can be approved for company creation.");
        }

        if (manager.getCompany() != null) {
            throw new BusinessException("This manager already has a company.");
        }

        if (manager.getPendingCompanyName() == null || manager.getPendingCompanyName().isBlank()) {
            throw new BusinessException("No pending company name found for this manager.");
        }

        // Create new Company entity
        Company company = new Company();
        company.setCompanyName(manager.getPendingCompanyName());
        company.setCompanyEmail(manager.getEmail());
        company.setCompanyManager(manager);
        company.setUpdateAt(System.currentTimeMillis());

        Company savedCompany = companyRepository.save(company);

        // Assign company to manager and activate account
        manager.setCompany(savedCompany);
        manager.setPendingCompanyName(null);
        manager.setEnabled(true);
        manager.setEmailVerified(true);
        userRepository.save(manager);

        return companyMapper.toDTO(savedCompany);
    }

    @Override
    public List<UserResponseDTO> getPendingCompanyManagers() {
        List<User> pendingManagers = userRepository.findByPendingCompanyNameIsNotNullAndEnabledFalse();
        return pendingManagers.stream()
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

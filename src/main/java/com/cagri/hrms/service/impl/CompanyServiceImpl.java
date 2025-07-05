package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.entity.Company;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.exception.BusinessException;
import com.cagri.hrms.exception.ResourceNotFoundException;
import com.cagri.hrms.mapper.CompanyMapper;
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

    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        // Get the currently authenticated user from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User manager = (User) auth.getPrincipal();

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

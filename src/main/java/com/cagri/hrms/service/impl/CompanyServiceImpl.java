package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.entity.Company;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.exception.ResourceNotFoundException;
import com.cagri.hrms.mapper.CompanyMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;

    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto, Long managerUserId) {
        User manager = userRepository.findById(managerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + managerUserId));

        Company company = companyMapper.toEntity(dto, manager);
        Company saved = companyRepository.save(company);
        return companyMapper.toDTO(saved);
    }

    @Override
    public List<CompanyResponseDTO> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CompanyResponseDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        return companyMapper.toDTO(company);
    }

    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
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
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        companyRepository.delete(company);
    }
}

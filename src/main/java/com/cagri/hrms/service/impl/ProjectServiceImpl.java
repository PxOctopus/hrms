package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.expense.ProjectCreateDTO;
import com.cagri.hrms.dto.response.expense.ProjectResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.expense.Project;
import com.cagri.hrms.mapper.ProjectMapper;
import com.cagri.hrms.repository.ProjectRepository;
import com.cagri.hrms.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repo;
    private final ProjectMapper mapper;

    @Override
    public ProjectResponseDTO createProject(ProjectCreateDTO dto, Company company) {
        Project p = mapper.toEntity(dto, company);
        repo.save(p);
        return mapper.toDTO(p);
    }

    @Override
    public ProjectResponseDTO getById(Long id, Company company) {
        Project p = repo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!p.getCompany().getId().equals(company.getId())) {
            throw new IllegalStateException("Project outside company scope");
        }
        return mapper.toDTO(p);
    }

    @Override
    public Page<ProjectResponseDTO> listActive(Company company, Pageable pageable) {
        return repo.findByCompanyAndActiveTrue(company, pageable)
                .map(mapper::toDTO);
    }

    @Override
    public Long getOrCreateCompanyWideProjectId(Company company) {
        return repo.findByCompanyAndIsGenericTrue(company)
                .orElseGet(() -> {
                    Project p = Project.builder()
                            .company(company)
                            .name("Company-wide")
                            .isGeneric(true)
                            .requiresAssignment(false)
                            .active(true)
                            .build();
                    repo.save(p);
                    return p;
                }).getId();
    }
}

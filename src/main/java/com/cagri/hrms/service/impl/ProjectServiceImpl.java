//package com.cagri.hrms.service.impl;
//
//import com.cagri.hrms.dto.request.expense.ProjectCreateDTO;
//import com.cagri.hrms.dto.response.expense.ProjectResponseDTO;
//import com.cagri.hrms.entity.core.Company;
//import com.cagri.hrms.entity.expense.Project;
//import com.cagri.hrms.mapper.ProjectMapper;
//import com.cagri.hrms.repository.ProjectRepository;
//import com.cagri.hrms.service.ProjectService;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class ProjectServiceImpl implements ProjectService {
//
//    private final ProjectRepository repo;
//    private final ProjectMapper mapper;
//
//    @Override
//    public ProjectResponseDTO createProject(ProjectCreateDTO dto, Company company) {
//        // Validate & normalize project name
//        final String name = dto.getName() == null ? "" : dto.getName().trim();
//        if (name.isEmpty()) {
//            // Prefer a domain exception if you have one (e.g., HrmsException with VALIDATION_ERROR)
//            throw new IllegalArgumentException("Project name is required");
//        }
//        // Prevent duplicate names within the same company (case-insensitive)
//        if (repo.existsByCompanyAndNameIgnoreCase(company, name)) {
//            throw new IllegalStateException("A project with the same name already exists in this company");
//        }
//
//        // Map DTO to entity and persist
//        Project p = mapper.toEntity(dto, company);
//        // Ensure normalized name is stored
//        p.setName(name);
//        repo.save(p);
//
//        return mapper.toDTO(p);
//    }
//
//    @Override
//    @Transactional(Transactional.TxType.SUPPORTS) // read-only intent
//    public ProjectResponseDTO getById(Long id, Company company) {
//        // Scope-safe single fetch (avoid cross-company access)
//        Project p = repo.findByIdAndCompanyId(id, company.getId())
//                .orElseThrow(EntityNotFoundException::new);
//        return mapper.toDTO(p);
//    }
//
//    @Override
//    @Transactional(Transactional.TxType.SUPPORTS) // read-only intent
//    public Page<ProjectResponseDTO> listActive(Company company, Pageable pageable) {
//        return repo.findByCompanyAndActiveTrue(company, pageable)
//                .map(mapper::toDTO);
//    }
//
//    // NOTE: getOrCreateCompanyWideProjectId(...) removed intentionally.
//    // We no longer auto-create a "Company-wide" project; project selection is mandatory now.
//}

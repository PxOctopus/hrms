//package com.cagri.hrms.service;
//
//import com.cagri.hrms.dto.request.expense.ProjectCreateDTO;
//import com.cagri.hrms.dto.response.expense.ProjectResponseDTO;
//import com.cagri.hrms.entity.core.Company;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//public interface ProjectService {
//
//    ProjectResponseDTO createProject(ProjectCreateDTO dto, Company company);
//
//    ProjectResponseDTO getById(Long id, Company company);
//
//    Page<ProjectResponseDTO> listActive(Company company, Pageable pageable);
//
////    Long getOrCreateCompanyWideProjectId(Company company); // returns generic project id
//}

//package com.cagri.hrms.service.impl;
//
//import com.cagri.hrms.dto.response.expense.ProjectAssignmentResponseDTO;
//import com.cagri.hrms.entity.core.User;
//import com.cagri.hrms.entity.employee.Employee;
//import com.cagri.hrms.entity.expense.Project;
//import com.cagri.hrms.entity.expense.ProjectAssignment;
//import com.cagri.hrms.mapper.ProjectAssignmentMapper;
//import com.cagri.hrms.repository.ProjectAssignmentRepository;
//import com.cagri.hrms.repository.ProjectRepository;
//import com.cagri.hrms.repository.UserRepository;
//import com.cagri.hrms.service.EmployeeService;
//import com.cagri.hrms.service.ProjectAssignmentService;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class ProjectAssignmentServiceImpl implements ProjectAssignmentService {
//
//    private final ProjectRepository projectRepo;
//    private final ProjectAssignmentRepository assignmentRepo;
//    private final ProjectAssignmentMapper mapper;
//    private final EmployeeService employeeService;
//    private final UserRepository userRepo;
//
//    @Override
//    public ProjectAssignmentResponseDTO assignEmployee(Long projectId, Long employeeId, Long managerUserId) {
//        // Load project by id or throw if not found
//        Project project = projectRepo.findById(projectId)
//                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
//
//        // Load employee by id or throw if not found
//        Employee employee = employeeService.findById(employeeId)
//                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
//
//        // Load manager as User entity or throw if not found
//        User manager = userRepo.findById(managerUserId)
//                .orElseThrow(() -> new EntityNotFoundException("Manager user not found"));
//
//        // Use mapper to create assignment entity (default accepted=false, assignedAt=now)
//        ProjectAssignment assignment = mapper.create(project, employee);
//        assignment.setAssignedBy(manager); // set manager as the assigning user
//        assignmentRepo.save(assignment);
//
//        return mapper.toDTO(assignment);
//    }
//
//    @Override
//    public ProjectAssignmentResponseDTO respondAssignment(Long assignmentId, boolean accepted, Long employeeUserId) {
//        ProjectAssignment pa = assignmentRepo.findById(assignmentId)
//                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
//
//
//        if (!pa.getEmployee().getUser().getId().equals(employeeUserId)) {
//            throw new IllegalStateException("You cannot respond to another employee's assignment");
//        }
//
//        pa.setAccepted(accepted);
//        pa.setRespondedAt(LocalDateTime.now());
//        assignmentRepo.save(pa);
//
//        return mapper.toDTO(pa);
//    }
//
//    @Override
//    public List<ProjectAssignmentResponseDTO> listForEmployee(Employee employee) {
//        return assignmentRepo.findByEmployee(employee).stream()
//                .map(mapper::toDTO)
//                .toList();
//    }
//}

package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;

import java.util.List;

public interface LeaveService {

    // Submit a new leave request (usually initiated by employee or manager)
    void requestLeave(LeaveRequestDTO dto);

    // Approve or reject a leave request
    void approveOrRejectLeave(LeaveApprovalDTO dto);

    // Retrieve all leaves in the system (admin or manager use only)
    List<LeaveResponseDTO> getAllLeaves();

    // Retrieve all leaves submitted by a specific employee (manager uses)
    List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId);

    // Retrieve leaves of the currently logged-in employee
    List<LeaveResponseDTO> getLeavesOfCurrentEmployee();

    // Retrieve all approved leaves
    List<LeaveResponseDTO> getApprovedLeaves();

    // Retrieve all pending leave requests
    List<LeaveResponseDTO> getPendingLeaves();

    // Retrieve all rejected leave requests
    List<LeaveResponseDTO> getRejectedLeaves();
}

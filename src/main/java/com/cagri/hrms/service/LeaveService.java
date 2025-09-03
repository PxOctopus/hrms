package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveCheckDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;

import java.util.List;
import java.util.Map;

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

    List<LeaveResponseDTO> getLeavesAssignedByManager();

    List<LeaveResponseDTO> getLeavesWaitingForMyApproval();

    List<LeaveResponseDTO> getLeavesApprovedByManager();

//    // --- NEW METHODS ---
//
//    // Check if employee has overlapping leave for the requested date range
//    boolean hasOverlappingLeave(Long employeeId, LeaveRequestDTO dto);
//
//    // Check if employee has enough remaining annual leave days
//    boolean hasRemainingAnnualLeave(Long employeeId, LeaveRequestDTO dto);

    // Lightweight pre-checks used by live UI badges
    boolean checkOverlap(LeaveCheckDTO dto);
    Map<String,Object> checkAnnualQuota(LeaveCheckDTO dto); // {ok, remainingDays?}
}

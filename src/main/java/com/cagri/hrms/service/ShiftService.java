package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.ShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.ShiftResponseDTO;
import com.cagri.hrms.entity.core.User;

import java.util.List;


/**
 * Service boundary for Shift (definition: name + daily time window).
 *
 * Rules:
 *  - Company derives from auth: write operations MUST bind to currentUser.getCompany().getId()
 *    and ignore DTO.companyId to prevent cross-company data leaks.
 *  - Overnight shifts ARE allowed (endTime <= startTime). Any business logic that depends on
 *    overnight handling is applied in the assignment service (EmployeeShiftService).
 *  - Company guard MUST be enforced on read/update/delete (only same-company access).
 *  - No global/admin listing: managers operate strictly within their own company scope.
 */
public interface ShiftService {

    /** Create a shift for the authenticated user's company (ignore DTO.companyId on write). */
    ShiftResponseDTO createShift(ShiftRequestDTO dto, User currentUser);

    /** Update a shift that belongs to the authenticated user's company. */
    ShiftResponseDTO updateShift(Long id, ShiftRequestDTO dto, User currentUser);

    /** Delete a shift that belongs to the authenticated user's company. */
    void deleteShift(Long id, User currentUser);

    /** Read a single shift by id (must belong to the authenticated user's company). */
    ShiftResponseDTO getShiftById(Long id, User currentUser);

    /** List all shifts for the authenticated user's company (manager-scoped). */
    List<ShiftResponseDTO> getMyCompanyShifts(User currentUser);
}

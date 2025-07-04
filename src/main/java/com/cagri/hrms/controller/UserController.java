package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.user.ChangeEmailRequestDTO;
import com.cagri.hrms.dto.request.user.ChangePasswordRequestDTO;
import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.general.MessageResponseDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.security.CustomUserDetails;
import com.cagri.hrms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the profile of the currently authenticated user.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        UserResponseDTO user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    /**
     * Updates the profile information of the currently authenticated user.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                         @RequestBody UserRequestDTO dto) {
        UserResponseDTO updated = userService.updateUser(currentUser.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Changes the email address of the currently authenticated user.
     * Usually triggers email verification for the new address.
     */
    @PutMapping("/change-email")
    public ResponseEntity<MessageResponseDTO> changeEmail(@AuthenticationPrincipal CustomUserDetails currentUser,
                                         @RequestBody ChangeEmailRequestDTO dto) {
        userService.changeEmail(currentUser.getId(), dto);
        return ResponseEntity.ok(new MessageResponseDTO("Email updated. Verification email sent."));
    }

    /**
     * Changes the password of the currently authenticated user.
     * Requires the current password for validation.
     */
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponseDTO> changePassword(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                                    @RequestBody ChangePasswordRequestDTO dto) {
        userService.changePassword(currentUser.getId(), dto);
        return ResponseEntity.ok(new MessageResponseDTO("Password changed successfully."));
    }

    /**
     * Deactivates the account of the currently authenticated user.
     * Prevents login until reactivation or support intervention.
     */
    @PutMapping("/deactivate")
    public ResponseEntity<MessageResponseDTO> deactivateAccount(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        userService.deactivateUser(currentUser.getId());
        return ResponseEntity.ok(new MessageResponseDTO("Account deactivated."));
    }
}

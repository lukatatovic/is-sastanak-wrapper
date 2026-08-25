package auth.controller;

import auth.dto.TemporaryRoleAssignmentDto;
import auth.dto.TemporaryRoleAssignmentRequest;
import auth.security.AppUserPrincipal;
import auth.service.TemporaryRoleAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/temporary-roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class TemporaryRoleAssignmentController {

    private final TemporaryRoleAssignmentService temporaryRoleAssignmentService;

    @PostMapping
    public ResponseEntity<TemporaryRoleAssignmentDto> assign(Authentication auth, @Valid @RequestBody TemporaryRoleAssignmentRequest request){
        Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        return ResponseEntity.ok(temporaryRoleAssignmentService.assign(adminId,request));
    }
}

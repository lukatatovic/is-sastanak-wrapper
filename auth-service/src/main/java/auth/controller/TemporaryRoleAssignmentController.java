package auth.controller;

import auth.dto.TemporaryRoleAssignmentDto;
import auth.dto.TemporaryRoleAssignmentRequest;
import auth.security.AppUserPrincipal;
import auth.service.TemporaryRoleAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temporary-roles")
@RequiredArgsConstructor
public class TemporaryRoleAssignmentController {

    private final TemporaryRoleAssignmentService temporaryRoleAssignmentService;

    @Value("${app.internal-api-key}")
    private String internalApiKey;

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<TemporaryRoleAssignmentDto> assign(Authentication auth, @Valid @RequestBody TemporaryRoleAssignmentRequest request){
        Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        return ResponseEntity.ok(temporaryRoleAssignmentService.assign(adminId,request));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TemporaryRoleAssignmentDto>> findByUser(@PathVariable Long userId){
        return ResponseEntity.ok(temporaryRoleAssignmentService.findByUser(userId));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id){
        temporaryRoleAssignmentService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/internal")
    public ResponseEntity<Void> assignInternal(@RequestBody TemporaryRoleAssignmentRequest request,
                                               @RequestHeader("X-Internal-Api-Key") String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            System.out.println(internalApiKey + " " +apiKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        temporaryRoleAssignmentService.assignInternal(request);
        return ResponseEntity.noContent().build();
    }

}

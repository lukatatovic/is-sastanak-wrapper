package auth.service;

import auth.dto.TemporaryRoleAssignmentDto;
import auth.dto.TemporaryRoleAssignmentRequest;
import auth.exception.ResourceNotFoundReception;
import auth.model.OrganizationalUnit;
import auth.model.TemporaryRoleAssigment;
import auth.model.User;
import auth.repository.OrganizationalUnitRepository;
import auth.repository.TemporaryRoleAssignmentRepository;
import auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TemporaryRoleAssignmentService {

    private final TemporaryRoleAssignmentRepository temporaryRoleAssignmentRepository;
    private final UserRepository userRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    public Set<String> effectiveRoles(Long id, Long meetingId, Long organizationalUnitId) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundReception("Korisnik ne postoji"));

        Set<String> roles = new HashSet<>();
        roles.add(user.getPrimaryRole().name());

        temporaryRoleAssignmentRepository.findByUserIdAndRevokedFalse(id).stream()
                .filter(TemporaryRoleAssigment::isActive)
                .filter(a -> matchContext(a,meetingId,organizationalUnitId))
                .forEach(a -> roles.add(a.getRole().name()));

        return roles;
    }

    private boolean matchContext(TemporaryRoleAssigment a, Long meetingId, Long organizationalUnitId) {

        if (a.getMeetingId() != null && meetingId != null){
            return meetingId != null && a.getMeetingId().equals(meetingId);
        }

        if (a.getOrganizationalUnitId() != null && organizationalUnitId != null){
            return organizationalUnitId != null && a.getOrganizationalUnitId().equals(organizationalUnitId);
        }

        return false;
    }
    @Transactional
    public TemporaryRoleAssignmentDto assign(Long adminId, TemporaryRoleAssignmentRequest request) {

        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundReception("Korisnik ne postoju"));
        User admin = userRepository.findById(adminId).orElseThrow(() -> new ResourceNotFoundReception("Administrator ne postoju"));
        OrganizationalUnit ou = organizationalUnitRepository.findById(request.getOrganizationalUnitId()).orElseThrow(() -> new ResourceNotFoundReception("Organizaciona jedinica ne postoji"));

        TemporaryRoleAssigment assigment = TemporaryRoleAssigment.builder()
                .user(user)
                .role(request.getRole())
                .meetingId(request.getMeetingId())
                .organizationalUnitId(request.getOrganizationalUnitId())
                .note(request.getNote())
                .assignedByAdmin(admin)
                .validUntil(request.getValidUntil())
                .revoked(false)
                .build();

        return toDto(temporaryRoleAssignmentRepository.save(assigment));
    }

    private TemporaryRoleAssignmentDto toDto(TemporaryRoleAssigment a) {
        return new TemporaryRoleAssignmentDto(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getFirstName() +" "+ a.getUser().getLastName(),
                a.getRole().name(),
                a.getMeetingId(),
                a.getOrganizationalUnitId(),
                a.getNote(),
                a.getAssignedByAdmin().getFirstName()+" "+a.getAssignedByAdmin().getLastName(),
                a.getAssignedAt(),
                a.getValidUntil(),
                a.isRevoked(),
                a.isActive()
        );
    }
}

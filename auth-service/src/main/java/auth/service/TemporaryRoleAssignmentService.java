package auth.service;

import auth.exception.ResourceNotFoundReception;
import auth.model.TemporaryRoleAssigment;
import auth.model.User;
import auth.repository.TemporaryRoleAssignmentRepository;
import auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TemporaryRoleAssignmentService {

    private final TemporaryRoleAssignmentRepository temporaryRoleAssignmentRepository;
    private final UserRepository userRepository;
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
}

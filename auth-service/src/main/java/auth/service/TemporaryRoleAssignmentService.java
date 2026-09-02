package auth.service;

import auth.client.MeetingsServiceClient;
import auth.dto.TemporaryRoleAssignmentDto;
import auth.dto.TemporaryRoleAssignmentRequest;
import auth.exception.ResourceNotFoundReception;
import auth.model.OrganizationalUnit;
import auth.model.Role;
import auth.model.TemporaryRoleAssigment;
import auth.model.User;
import auth.repository.OrganizationalUnitRepository;
import auth.repository.TemporaryRoleAssignmentRepository;
import auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemporaryRoleAssignmentService {

    private final TemporaryRoleAssignmentRepository temporaryRoleAssignmentRepository;
    private final UserRepository userRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final MeetingsServiceClient meetingsServiceClient;
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
        boolean hasOrgUnit = request.getOrganizationalUnitId() != null;

        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundReception("Korisnik ne postoju"));
        User admin = userRepository.findById(adminId).orElseThrow(() -> new ResourceNotFoundReception("Administrator ne postoju"));
        String ouName = "";
        if(hasOrgUnit) {
            OrganizationalUnit ou = organizationalUnitRepository.findById(request.getOrganizationalUnitId()).orElseThrow(() -> new ResourceNotFoundReception("Organizaciona jedinica ne postoji"));
            ouName = ou.getName();
        }

        Optional<TemporaryRoleAssigment> existingAssigment = request.getMeetingId() != null ? temporaryRoleAssignmentRepository.findByUserIdAndMeetingIdAndRevokedFalse(request.getUserId(), request.getMeetingId()) : Optional.empty();

        TemporaryRoleAssigment assigment;

        if(existingAssigment.isPresent()){
            assigment = existingAssigment.get();

            assigment.setRole(request.getRole());
            assigment.setOrganizationalUnitId(request.getOrganizationalUnitId());
            assigment.setNote(request.getNote());
            assigment.setAssignedByAdmin(admin);
            assigment.setValidUntil(request.getValidUntil());
            assigment.setRevoked(false);
        }else{
            assigment = TemporaryRoleAssigment.builder()
                    .user(user)
                    .role(request.getRole())
                    .meetingId(request.getMeetingId())
                    .organizationalUnitId(request.getOrganizationalUnitId())
                    .note(request.getNote())
                    .assignedByAdmin(admin)
                    .validUntil(request.getValidUntil())
                    .revoked(false)
                    .build();
        }

        if(hasOrgUnit) {
            meetingsServiceClient.notify(user.getId(), "Dodeljena vam je privremena uloga " + request.getRole() + " za organizacionu jedinicu: "+ ouName +", " + request.getNote(), "INFO");
        }
        if(existingAssigment.isPresent() && request.getMeetingId() != null){
            meetingsServiceClient.notify(user.getId(), "Dodeljena vam je privremena uloga " + request.getRole() + " za sastanak: "+ request.getMeetingId() +", " + request.getNote(), "INFP");
        }
        return toDto(temporaryRoleAssignmentRepository.save(assigment));
    }

    private TemporaryRoleAssignmentDto toDto(TemporaryRoleAssigment a) {
        String adminName = null;
        if(a.getAssignedByAdmin() != null){
            adminName = a.getAssignedByAdmin().getFirstName()+" "+a.getAssignedByAdmin().getLastName();
        }

        return new TemporaryRoleAssignmentDto(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getFirstName() +" "+ a.getUser().getLastName(),
                a.getRole().name(),
                a.getMeetingId(),
                a.getOrganizationalUnitId(),
                a.getNote(),
                adminName,
                a.getAssignedAt(),
                a.getValidUntil(),
                a.isRevoked(),
                a.isActive()
        );
    }

    @Transactional(readOnly = true)
    public List<TemporaryRoleAssignmentDto> findByUser(Long userId) {
        return temporaryRoleAssignmentRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void revoke(Long id) {
        TemporaryRoleAssigment assigment = temporaryRoleAssignmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundReception("Privremena uloga ne postoju"));
        assigment.setRevoked(true);
    }

    public void assignInternal(TemporaryRoleAssignmentRequest request) {
        boolean hasOrgUnit = request.getOrganizationalUnitId() != null;

        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundReception("Korisnik ne postoju"));

        if(hasOrgUnit) {
            OrganizationalUnit ou = organizationalUnitRepository.findById(request.getOrganizationalUnitId()).orElseThrow(() -> new ResourceNotFoundReception("Organizaciona jedinica ne postoji"));
        }

        Optional<TemporaryRoleAssigment> existingAssigment = request.getMeetingId() != null ? temporaryRoleAssignmentRepository.findByUserIdAndMeetingIdAndRevokedFalse(request.getUserId(), request.getMeetingId()) : Optional.empty();

        TemporaryRoleAssigment assigment;

        if(existingAssigment.isPresent()){
            assigment = existingAssigment.get();
            assigment.setRole(request.getRole());
            assigment.setOrganizationalUnitId(request.getOrganizationalUnitId());
            assigment.setNote(request.getNote());
            assigment.setValidUntil(request.getValidUntil());
            assigment.setRevoked(false);
        }else{
            assigment = TemporaryRoleAssigment.builder()
                    .user(user)
                    .role(request.getRole())
                    .meetingId(request.getMeetingId())
                    .organizationalUnitId(request.getOrganizationalUnitId())
                    .note(request.getNote())
                    .validUntil(request.getValidUntil())
                    .revoked(false)
                    .build();
        }

        temporaryRoleAssignmentRepository.save(assigment);

        if(existingAssigment.isPresent() && request.getMeetingId() != null){
            meetingsServiceClient.notify(user.getId(), "Dodeljena vam je privremena uloga " + request.getRole() + " za sastanak: "+ request.getMeetingId() +", " + request.getNote(), "INFP");
        }

        return;
    }

    public List<Long> findUserIdsByMeeting(Long meetingId) {
        return temporaryRoleAssignmentRepository.findByMeetingIdAndRevokedFalse(meetingId).stream()
                .filter(TemporaryRoleAssigment::isActive)
                .map(a -> a.getUser().getId())
                .collect(Collectors.toList());
    }
}

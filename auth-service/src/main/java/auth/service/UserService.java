package auth.service;

import auth.dto.UserCreateRequest;
import auth.dto.UserDto;
import auth.exception.BusinessRuleException;
import auth.exception.ResourceNotFoundReception;
import auth.model.OrganizationalUnit;
import auth.model.Role;
import auth.model.User;
import auth.repository.OrganizationalUnitRepository;
import auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        if(userRepository.existsByUsername(request.getUsername())){
            throw new BusinessRuleException("Korisnicko ime je zauzeto");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new BusinessRuleException("Email je zauzet");
        }
        if(userRepository.existsByJmbg(request.getJmbg())){
            throw new BusinessRuleException("JMBG je vec registrovan");
        }
        if(userRepository.existsByOfficePhone(request.getOfficePhone())){
            throw new BusinessRuleException("Poslovni broj je zauzet");
        }
        if(userRepository.existsByMobilePhone(request.getMobilePhone())){
            throw new BusinessRuleException("Broj mobilnog telefona je zauzet");
        }

        OrganizationalUnit ou = null;
        if(request.getOrganizationalUnitId() != null){
            ou = organizationalUnitRepository.findById(request.getOrganizationalUnitId()).orElseThrow(() -> new ResourceNotFoundReception("Organizaciona jedinica ne postoju"));
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .fatherName(request.getFatherName())
                .lastName(request.getLastName())
                .jmbg(request.getJmbg())
                .jobTitle(request.getJobTitle())
                .officePhone(request.getOfficePhone())
                .mobilePhone(request.getMobilePhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .organizationalUnit(ou)
                .primaryRole(request.getPrimaryRole() == null ? Role.UCESNIK : request.getPrimaryRole())
                .build();

        return toDto(userRepository.save(user));
    }



    private UserDto toDto(User u ){
        return new UserDto(u.getId(), u.getFirstName(), u.getLastName(),u.getJobTitle(),u.getEmail(),u.getUsername(),
                u.getOrganizationalUnit() !=null ? u.getOrganizationalUnit().getId() :null,
                u.getOrganizationalUnit() !=null ? u.getOrganizationalUnit().getName() :null,
                u.getPrimaryRole().name()
                );
    }
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
}

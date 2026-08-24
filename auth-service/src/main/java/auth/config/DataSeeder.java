package auth.config;

import auth.model.OrganizationalUnit;
import auth.model.Role;
import auth.model.TemporaryRoleAssigment;
import auth.model.User;
import auth.repository.OrganizationalUnitRepository;
import auth.repository.TemporaryRoleAssignmentRepository;
import auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {


    private final UserRepository userRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final TemporaryRoleAssignmentRepository temporaryRoleAssignmentRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args){
        if(userRepository.count() > 0) return;

        OrganizationalUnit ou = organizationalUnitRepository.save(OrganizationalUnit.builder().name("IT sektor").build());

        User admin = User.builder().firstName("Admin").lastName("Adminic").jmbg("1234567891234").email("admin@gmail.com")
                .username("admin").passwordHash(passwordEncoder.encode("admin123")).organizationalUnit(ou)
                .primaryRole(Role.ADMINISTRATOR).build();
        userRepository.save(admin);

        User pera = User.builder().firstName("Pera").lastName("Peric").jmbg("1234567891235").email("pera@gmail.com")
                .username("pera").passwordHash(passwordEncoder.encode("pera123")).organizationalUnit(ou)
                .primaryRole(Role.RUKOVODILAC).build();
        userRepository.save(pera);

        User zika = User.builder().firstName("Zika").lastName("Zikic").jmbg("1234567891236").email("zika@gmail.com")
                .username("zika").passwordHash(passwordEncoder.encode("zika123")).organizationalUnit(ou)
                .primaryRole(Role.ZAPISNICAR).build();
        userRepository.save(zika);

        User mika = User.builder().firstName("Mika").lastName("Mikic").jmbg("1234567891237").email("mika@gmail.com")
                .username("mika").passwordHash(passwordEncoder.encode("mika123")).organizationalUnit(ou)
                .primaryRole(Role.UCESNIK).build();
        userRepository.save(mika);

        temporaryRoleAssignmentRepository.save(
            TemporaryRoleAssigment.builder().user(zika).role(Role.ZAPISNICAR).meetingId(1L)
                    .note("Zamena zbog zdravstvenih probelma").assignedByAdmin(admin).revoked(false).build()
        );

        System.out.println("Uspesno seed-ovani podaci u bazu podataka");

    }
}

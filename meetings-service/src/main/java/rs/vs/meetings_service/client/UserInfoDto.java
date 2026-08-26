package rs.vs.meetings_service.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

        private Long id;

        private String firstName;

        private String lastName;

        private Long organizationalUnitId;

        private String organizationalUnitName;

        private String primaryRole;

        private Set<String> effectiveRoles;

        public  String fullName() { return firstName + " "+ lastName;}

        public boolean hasEffectiveRole(String role) { return  effectiveRoles != null && effectiveRoles.contains(role);}

}

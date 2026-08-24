package auth.security;

import auth.model.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class AppUserPrincipal extends User {

    private final Long id;

    public AppUserPrincipal(Long id, String username, String passwordHash, Role primaryRole) {
        super(username,passwordHash,mapAuthorities(primaryRole));
        this.id = id;
    }

    private static List<GrantedAuthority> mapAuthorities(Role primaryRole){
        return List.of(new SimpleGrantedAuthority("ROLE_" + primaryRole.name()));
    }
}

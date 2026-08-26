package rs.vs.meetings_service.security;

import lombok.Getter;

@Getter
public class AppUserPrincipal {

    private final Long id;

    private final String username;

    public AppUserPrincipal(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}

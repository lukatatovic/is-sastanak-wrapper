package auth.security;

import auth.model.User;
import auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow( () -> new UsernameNotFoundException("Korisnik "+ username+ " ne postoji"));

        if(!user.isEnabled()){
            throw new UsernameNotFoundException("Nije moguc pristup nalogu");
        }
        return new AppUserPrincipal(user.getId(),user.getUsername(),user.getPasswordHash(),user.getPrimaryRole());
    }
}

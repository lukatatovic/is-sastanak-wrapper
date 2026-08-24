package auth.service;

import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import auth.exception.ResourceNotFoundReception;
import auth.model.User;
import auth.repository.UserRepository;
import auth.security.AppUserPrincipal;
import auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));

        AppUserPrincipal principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new ResourceNotFoundReception("Korisnik ne postoji"));

        String token = jwtService.generateToken( principal, user.getId());

        return new LoginResponse(token, user.getId(), user.getFirstName() + " " + user.getLastName(), java.util.Set.of(user.getPrimaryRole().name()));

    }
}

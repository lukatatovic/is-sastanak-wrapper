package auth.controller;

import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import auth.dto.UserCreateRequest;
import auth.repository.OrganizationalUnitRepository;
import auth.service.AuthService;
import auth.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gui")
@RequiredArgsConstructor
public class GuiController {

    private final AuthService authService;
    private final UserService userService;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String guiLogin(@ModelAttribute LoginRequest loginRequest, HttpServletResponse response, Model model){
        try {
            LoginResponse result = authService.login(loginRequest);
            Cookie cookie = new Cookie("jwt", result.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(8*60*60);
            response.addCookie(cookie);
            return "redirect:/gui/users";
        }catch (Exception e){
            model.addAttribute("error", "Pogrešno korisničko ime ili lozinka.");
            return "login";
        }
    }


    @GetMapping("/users")
    public String usersPage(Model model){
        model.addAttribute("users",userService.findAll());
        model.addAttribute("units",organizationalUnitRepository.findAll());
        model.addAttribute("newUser",new UserCreateRequest());
        return "users";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute UserCreateRequest newUser){
        userService.createUser(newUser);
        return "redirect:/gui/users";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response){
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/gui/login";
    }
}

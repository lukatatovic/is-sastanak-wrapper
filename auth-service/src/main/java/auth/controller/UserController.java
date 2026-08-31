package auth.controller;

import auth.dto.UserCreateRequest;
import auth.dto.UserDto;
import auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest request){
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR','RUKOVODILAC')")
    @GetMapping
    public ResponseEntity<List<UserDto>> findAll(){return ResponseEntity.ok(userService.findAll());}
}

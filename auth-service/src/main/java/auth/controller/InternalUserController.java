package auth.controller;

import auth.dto.UserInternalDto;
import auth.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @Value("${app.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/{id}")
    public ResponseEntity<UserInternalDto> getUser(@PathVariable Long id,
                                                   @RequestParam(required = false) Long meetingId,
                                                   @RequestParam(required = false) Long organizationalUnitId,
                                                   @RequestHeader("X-Internal-Api-Key") String apiKey){
            if(!internalApiKey.equals(apiKey)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(userService.getInternal(id,meetingId,organizationalUnitId));
    }
}

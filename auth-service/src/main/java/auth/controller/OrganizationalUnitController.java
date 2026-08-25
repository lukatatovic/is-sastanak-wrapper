package auth.controller;

import auth.model.OrganizationalUnit;
import auth.repository.OrganizationalUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org-units")
@RequiredArgsConstructor
public class OrganizationalUnitController {

    private final OrganizationalUnitRepository organizationalUnitRepository;

    @GetMapping
    public ResponseEntity<List<OrganizationalUnit>> findAll() {return ResponseEntity.ok(organizationalUnitRepository.findAll());}

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<OrganizationalUnit> create(@RequestBody OrganizationalUnit ou){
        return ResponseEntity.ok(organizationalUnitRepository.save(ou));
    }
}

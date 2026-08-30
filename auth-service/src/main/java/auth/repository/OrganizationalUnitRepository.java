package auth.repository;

import auth.model.OrganizationalUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit,Long> {

    boolean existsByName(String name);
}

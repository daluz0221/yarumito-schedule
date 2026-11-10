package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Permite consultar y persistir usuarios autenticables.
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    // Busca un usuario por el correo que envía el formulario de login.
    Optional<Usuario> findByCorreo(String correo);

    // Evita crear más de una vez el usuario semilla.
    boolean existsByCorreo(String correo);
}

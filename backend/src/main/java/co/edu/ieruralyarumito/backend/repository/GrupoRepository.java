package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Gestiona la persistencia de los grupos escolares.
public interface GrupoRepository extends JpaRepository<Grupo, UUID> {

    // Verifica si ya existe el mismo código de grupo
    // dentro del año escolar indicado.
    boolean existsByAnioEscolar_IdAndCodigo(
            UUID anioEscolarId,
            String codigo);

    // Verifica duplicidad del código al actualizar,
    // excluyendo el grupo que se está modificando.
    boolean existsByAnioEscolar_IdAndCodigoAndIdNot(
            UUID anioEscolarId,
            String codigo,
            UUID id);

    // Verifica si un aula está o ha quedado referenciada
    // actualmente como aula fija de algún grupo.
    boolean existsByAulaFija_Id(
            UUID aulaId);
}

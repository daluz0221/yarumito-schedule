package co.edu.ieruralyarumito.backend.repository;

import co.edu.ieruralyarumito.backend.entity.ParametroInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Permite gestionar los parámetros institucionales asociados a cada año escolar.
public interface ParametroInstitucionalRepository
        extends JpaRepository<ParametroInstitucional, UUID> {

    // Verifica si ya existe la misma clave para el año escolar indicado.
    boolean existsByAnioEscolar_IdAndClave(
            UUID anioEscolarId,
            String clave);

    // Verifica duplicidad de la clave al actualizar,
    // excluyendo el parámetro actual.
    boolean existsByAnioEscolar_IdAndClaveAndIdNot(
            UUID anioEscolarId,
            String clave,
            UUID id);

    // Consulta un parámetro específico por año escolar y clave.
    Optional<ParametroInstitucional> findByAnioEscolar_IdAndClave(
            UUID anioEscolarId,
            String clave);

    // Lista todos los parámetros institucionales de un año escolar.
    List<ParametroInstitucional> findByAnioEscolar_Id(
            UUID anioEscolarId);
}

package co.edu.ieruralyarumito.backend.repository;

import java.util.UUID;
import java.util.List;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import org.springframework.data.repository.Repository;


public interface AsignacionAcademicaRepository extends Repository<AsignacionAcademica, UUID> {

    //Filtrar por anio escolar y grupo
    List<AsignacionAcademica> findByAnioEscolarAndGrupo(UUID anioEscolarId, UUID grupoId);


}

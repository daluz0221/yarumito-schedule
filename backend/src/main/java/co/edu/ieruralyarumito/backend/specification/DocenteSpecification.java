package co.edu.ieruralyarumito.backend.specification;

import co.edu.ieruralyarumito.backend.entity.Docente;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

public class DocenteSpecification {

    // Construye la búsqueda por nombre, apellido o número de documento.
    public static Specification<Docente> buscarPorTexto(String texto) {

        return (root, query, criteriaBuilder) -> {

            // Si no se envía texto, no aplica ninguna restricción.
            if (texto == null || texto.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String patron = "%" + texto.trim().toLowerCase() + "%";

            // Busca coincidencias parciales en los tres campos permitidos.
            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("nombres")), patron),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("apellidos")), patron),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("numeroDocumento")), patron)
            );
        };
    }

    // Filtra los docentes por su área de nombramiento.
    public static Specification<Docente> porArea(UUID areaId) {

        return (root, query, criteriaBuilder) -> {

            // Si no se envía un área, no aplica ninguna restricción.
            if (areaId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("areaNombramiento").get("id"),
                    areaId
            );
        };
    }

    // Filtra los docentes por su estado.
    public static Specification<Docente> porEstado(EstadoDocente estado) {

        return (root, query, criteriaBuilder) -> {

            // Si no se envía un estado, no aplica ninguna restricción.
            if (estado == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("estado"),
                    estado
            );
        };
    }

    // Filtra los docentes por su tipo de vinculación.
    public static Specification<Docente> porTipoVinculacion(TipoVinculacion tipoVinculacion) {

        return (root, query, criteriaBuilder) -> {

            // Si no se envía un tipo de vinculación, no aplica ninguna restricción.
            if (tipoVinculacion == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("tipoVinculacion"),
                    tipoVinculacion
            );
        };
    }
}


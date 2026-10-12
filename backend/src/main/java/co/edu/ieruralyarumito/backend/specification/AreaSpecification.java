package co.edu.ieruralyarumito.backend.specification;

import co.edu.ieruralyarumito.backend.entity.Area;
import org.springframework.data.jpa.domain.Specification;

// Construye los filtros dinámicos utilizados al consultar áreas.
public class AreaSpecification {

    // Busca coincidencias parciales por nombre o código.
    public static Specification<Area> buscarPorTexto(String texto) {

        return (root, query, criteriaBuilder) -> {

            // Si no se envía texto, no aplica ninguna restricción.
            if (texto == null || texto.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String patron = "%" + texto.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("nombre")),
                            patron),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("codigo")),
                            patron)
            );
        };
    }

    // Filtra las áreas según su estado activo o inactivo.
    public static Specification<Area> porEstado(Boolean activa) {

        return (root, query, criteriaBuilder) -> {

            // Si no se envía estado, devuelve áreas activas e inactivas.
            if (activa == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("activa"),
                    activa
            );
        };
    }
}

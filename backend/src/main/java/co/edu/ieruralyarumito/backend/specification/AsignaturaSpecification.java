package co.edu.ieruralyarumito.backend.specification;

import co.edu.ieruralyarumito.backend.entity.Asignatura;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class AsignaturaSpecification {

    private AsignaturaSpecification() {
    }

    public static Specification<Asignatura> buscarPorTexto(String texto) {
        return (root, query, criteriaBuilder) -> {

            if (texto == null || texto.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String patron = "%" + texto.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("nombre")),
                            patron
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("codigo")),
                            patron
                    )
            );
        };
    }

    public static Specification<Asignatura> porEstado(Boolean activa) {
        return (root, query, criteriaBuilder) -> {

            if (activa == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("activa"), activa);
        };
    }

    public static Specification<Asignatura> porArea(UUID areaId) {
        return (root, query, criteriaBuilder) -> {

            if (areaId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("area").get("id"),
                    areaId
            );
        };
    }
}

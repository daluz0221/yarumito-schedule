
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.specification.DocenteSpecification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HU-04 / SCRUM-31.
 *
 * Construye el informe de planta docente y capacidad
 * ordinaria contratada.
 *
 * No modifica docentes ni asignaciones académicas.
 * No calcula horas efectivamente disponibles.
 */
@Service
public class InformePlantaDocenteService {

    private final DocenteRepository docenteRepository;

    public InformePlantaDocenteService(
            DocenteRepository docenteRepository) {

        this.docenteRepository = docenteRepository;
    }

    /**
     * Consulta todos los docentes que satisfacen los
     * filtros y genera un resultado consolidado.
     *
     * Los filtros nulos significan "Todos".
     */
    @Transactional(readOnly = true)
    public InformePlantaDocenteResponse generarInforme(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion) {

        Specification<Docente> filtros =
                DocenteSpecification.porArea(areaNombramientoId)
                        .and(DocenteSpecification.porEstado(estado))
                        .and(DocenteSpecification
                                .porTipoVinculacion(tipoVinculacion));

        List<Docente> encontrados =
                docenteRepository.findAll(filtros);

        /*
         * Cada docente se identifica por UUID.
         * Evitamos contabilizarlo más de una vez.
         */
        Map<UUID, Docente> docentesUnicos =
                encontrados.stream()
                        .collect(Collectors.toMap(
                                Docente::getId,
                                Function.identity(),
                                (primero, repetido) -> primero
                        ));

        List<InformePlantaDocenteDetalleResponse> detalle =
                docentesUnicos.values().stream()
                        .map(this::convertirADetalle)
                        .sorted(Comparator
                                .comparing(
                                        InformePlantaDocenteDetalleResponse
                                                ::nombreCompleto,
                                        String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(
                                        InformePlantaDocenteDetalleResponse
                                                ::docenteId))
                        .toList();

        /*
         * Sumamos las horas registradas de los
         * docentes únicos del conjunto filtrado.
         *
         * No multiplicamos automáticamente por 22.
         */
        long totalHoras = 0L;

        for (InformePlantaDocenteDetalleResponse fila : detalle) {
            totalHoras = Math.addExact(
                    totalHoras,
                    fila.horasSemanalesContratadas()
            );
        }

        return new InformePlantaDocenteResponse(
                detalle.size(),
                totalHoras,
                detalle
        );
    }

    /**
     * Construye exclusivamente los datos necesarios
     * para una fila del informe.
     */
    private InformePlantaDocenteDetalleResponse convertirADetalle(
            Docente docente) {

        String nombreCompleto =
                docente.getNombres() + " "
                        + docente.getApellidos();

        String nombreArea =
                docente.getAreaNombramiento().getNombre();

        return new InformePlantaDocenteDetalleResponse(
                docente.getId(),
                nombreCompleto.trim(),
                nombreArea,
                docente.getEstado(),
                docente.getTipoVinculacion(),
                docente.getHorasSemanalesContratadas()
        );
    }
}

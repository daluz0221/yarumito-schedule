package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;

import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Docente;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;

import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;

import co.edu.ieruralyarumito.backend.specification.DocenteSpecification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HU-04 / SCRUM-31.
 *
 * Informe de Planta Docente y Carga Academica.
 *
 * Permite consultar:
 * - Toda la planta docente.
 * - Un docente especifico.
 * - Carga academica por año escolar.
 *
 * Conserva los filtros originales.
 *
 * Todas las operaciones son de lectura.
 */
@Service
public class InformePlantaDocenteService {

    private final DocenteRepository docenteRepository;

    private final AsignacionAcademicaRepository
            asignacionAcademicaRepository;

    private final AnioEscolarRepository anioEscolarRepository;

    public InformePlantaDocenteService(
            DocenteRepository docenteRepository,
            AsignacionAcademicaRepository
                    asignacionAcademicaRepository,
            AnioEscolarRepository anioEscolarRepository) {

        this.docenteRepository = docenteRepository;

        this.asignacionAcademicaRepository =
                asignacionAcademicaRepository;

        this.anioEscolarRepository = anioEscolarRepository;
    }

    /**
     * Consulta original.
     *
     * Sin año escolar ni docente especifico.
     */
    @Transactional(readOnly = true)
    public InformePlantaDocenteResponse generarInforme(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion) {

        return construirInforme(
                areaNombramientoId,
                estado,
                tipoVinculacion,
                null,
                null
        );
    }

    /**
     * Consulta con año escolar.
     *
     * Conserva compatibilidad con el codigo existente.
     */
    @Transactional(readOnly = true)
    public InformePlantaDocenteResponse generarInforme(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            UUID anioEscolarId) {

        if (anioEscolarId == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un año escolar "
                            + "para consultar la carga docente."
            );
        }

        return generarInforme(
                areaNombramientoId,
                estado,
                tipoVinculacion,
                anioEscolarId,
                null
        );
    }

    /**
     * Consulta ampliada.
     *
     * docenteId == null:
     *     consulta todos los docentes.
     *
     * docenteId != null:
     *     consulta exclusivamente ese docente.
     *
     * anioEscolarId == null:
     *     conserva la presentacion original.
     *
     * anioEscolarId != null:
     *     incorpora horas asignadas y extras.
     */
    @Transactional(readOnly = true)
    public InformePlantaDocenteResponse generarInforme(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            UUID anioEscolarId,
            UUID docenteId) {

        AnioEscolar anioEscolar = null;

        if (anioEscolarId != null) {

            anioEscolar = anioEscolarRepository
                    .findById(anioEscolarId)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "El año escolar no existe."
                            )
                    );
        }

        return construirInforme(
                areaNombramientoId,
                estado,
                tipoVinculacion,
                anioEscolar,
                docenteId
        );
    }

    /**
     * Construye el informe aplicando los filtros
     * existentes y el nuevo filtro por docente.
     */
    private InformePlantaDocenteResponse construirInforme(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            AnioEscolar anioEscolar,
            UUID docenteId) {

        Specification<Docente> filtros =
                DocenteSpecification
                        .porArea(areaNombramientoId)
                        .and(
                                DocenteSpecification
                                        .porEstado(estado)
                        )
                        .and(
                                DocenteSpecification
                                        .porTipoVinculacion(
                                                tipoVinculacion
                                        )
                        );

        /*
         * Filtro opcional por docente.
         *
         * Se aplica en la consulta a la base
         * de datos, antes de generar el informe.
         */
        if (docenteId != null) {

            filtros = filtros.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("id"),
                                    docenteId
                            )
            );
        }

        List<Docente> encontrados =
                docenteRepository.findAll(filtros);

        Map<UUID, Docente> docentesUnicos =
                encontrados.stream()
                        .collect(Collectors.toMap(
                                Docente::getId,
                                Function.identity(),
                                (primero, repetido) -> primero
                        ));

        List<InformePlantaDocenteDetalleResponse> detalle =
                docentesUnicos.values()
                        .stream()
                        .map(docente ->
                                convertirADetalle(
                                        docente,
                                        anioEscolar
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                InformePlantaDocenteDetalleResponse
                                                        ::nombreCompleto,
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                        .thenComparing(
                                                InformePlantaDocenteDetalleResponse
                                                        ::docenteId
                                        )
                        )
                        .toList();

        long totalContratadas = 0L;
        long totalAsignadas = 0L;
        long totalExtras = 0L;

        for (InformePlantaDocenteDetalleResponse fila : detalle) {

            totalContratadas = Math.addExact(
                    totalContratadas,
                    fila.horasSemanalesContratadas()
            );

            totalAsignadas = Math.addExact(
                    totalAsignadas,
                    fila.horasSemanalesAsignadas()
            );

            totalExtras = Math.addExact(
                    totalExtras,
                    fila.horasSemanalesExtras()
            );
        }

        return new InformePlantaDocenteResponse(
                detalle.size(),
                totalContratadas,
                detalle,
                anioEscolar == null
                        ? null
                        : anioEscolar.getId(),
                anioEscolar == null
                        ? null
                        : anioEscolar.getAnio(),
                totalAsignadas,
                totalExtras
        );
    }

    /**
     * Construye el detalle individual.
     *
     * Solo considera asignaciones del año
     * escolar seleccionado.
     *
     * Incluye BORRADOR y CONFIRMADA.
     */
    private InformePlantaDocenteDetalleResponse convertirADetalle(
            Docente docente,
            AnioEscolar anioEscolar) {

        String nombreCompleto =
                docente.getNombres() + " "
                        + docente.getApellidos();

        String nombreArea =
                docente.getAreaNombramiento().getNombre();

        int horasContratadas =
                docente.getHorasSemanalesContratadas();

        long horasAsignadas = 0L;
        long horasExtras = 0L;

        if (anioEscolar != null) {

            List<AsignacionAcademica> asignaciones =
                    asignacionAcademicaRepository
                            .findByDocente_Id(
                                    docente.getId()
                            );

            horasAsignadas = asignaciones.stream()
                    .filter(Objects::nonNull)
                    .filter(asignacion ->
                            asignacion.getAnioEscolar() != null
                    )
                    .filter(asignacion ->
                            anioEscolar.getId().equals(
                                    asignacion
                                            .getAnioEscolar()
                                            .getId()
                            )
                    )
                    .filter(asignacion ->
                            asignacion.getEstado()
                                    == EstadoAsignacionAcademica.BORRADOR
                                    || asignacion.getEstado()
                                    == EstadoAsignacionAcademica.CONFIRMADA
                    )
                    .mapToLong(
                            AsignacionAcademica::getHorasSemanales
                    )
                    .sum();

            /*
             * No duplicar horas extras.
             *
             * Las horas asignadas ya representan
             * la carga academica total.
             */
            horasExtras = Math.max(
                    0L,
                    horasAsignadas - horasContratadas
            );
        }

        return new InformePlantaDocenteDetalleResponse(
                docente.getId(),
                nombreCompleto.trim(),
                nombreArea,
                docente.getEstado(),
                docente.getTipoVinculacion(),
                horasContratadas,
                horasAsignadas,
                horasExtras
        );
    }
}

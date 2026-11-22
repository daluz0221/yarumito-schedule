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
 * Conserva los datos originales de planta docente
 * y permite consultar las horas asignadas y extras
 * para un año escolar especifico.
 *
 * Todas las operaciones son de lectura.
 *
 * No modifica docentes, asignaciones, horarios
 * ni registros de la base de datos.
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
     * Conserva la consulta original de Planta Docente.
     *
     * Al no seleccionar un año escolar, no se calculan
     * las horas correspondientes a las asignaciones.
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
                null
        );
    }

    /**
     * Nueva consulta de Planta Docente con carga
     * academica del año escolar seleccionado.
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

        AnioEscolar anioEscolar =
                anioEscolarRepository
                        .findById(anioEscolarId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "El año escolar no existe."
                                )
                        );

        return construirInforme(
                areaNombramientoId,
                estado,
                tipoVinculacion,
                anioEscolar
        );
    }

    /**
     * Construye el informe respetando los filtros
     * originales y evitando duplicar docentes.
     */
    private InformePlantaDocenteResponse construirInforme(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            AnioEscolar anioEscolar) {

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
     * Construye el detalle de un docente.
     *
     * Las horas asignadas se calculan exclusivamente
     * con asignaciones del año escolar seleccionado.
     *
     * Incluye estados BORRADOR y CONFIRMADA para
     * reflejar la planificacion academica en curso.
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
             * Las horasSemanales ya representan
             * la carga total de cada asignacion.
             *
             * Las extras son el exceso sobre
             * la carga ordinaria contratada.
             *
             * No se suma nuevamente horasExtras
             * para evitar doble contabilizacion.
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

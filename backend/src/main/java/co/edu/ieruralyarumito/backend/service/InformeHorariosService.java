package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCargaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HU-04 / SCRUM-31.
 *
 * Construye el Informe de Horarios Academicos
 * a partir de las clases recibidas del Gestor
 * de Horarios.
 *
 * No almacena resultados ni modifica PostgreSQL.
 *
 * Regla institucional:
 * Una clase programada de 55 minutos equivale
 * a una hora academica para su contabilizacion.
 */
@Service
public class InformeHorariosService {

    public InformeHorariosResponse construirInforme(
            UUID anioEscolarId,
            String nombreAnioEscolar,
            String nombreInstitucion,
            String nombreSede,
            InformeHorariosFiltroRequest filtros,
            List<InformeHorarioClaseDetalleResponse> clases) {

        if (filtros == null) {
            throw new IllegalArgumentException(
                    "Los filtros del informe son obligatorios."
            );
        }

        if (!filtros.tieneRangoFechasValido()) {
            throw new IllegalArgumentException(
                    "El rango de fechas del informe no es valido."
            );
        }

        List<InformeHorarioClaseDetalleResponse> detalle =
                new ArrayList<>(
                        clases == null ? List.of() : clases
                );

        // Filtrar las clases del grupo solicitado.
        // Si no se selecciona grupo, conservar todas.
        if (filtros.grupoId() != null) {

            detalle.removeIf(
                    clase -> !filtros.grupoId()
                            .equals(clase.grupoId())
            );
        }

        // Filtrar por día de la semana.
        if (filtros.dia() != null && !filtros.dia().isBlank()) {

            String diaSolicitado = filtros.dia().trim();

            detalle.removeIf(
                    clase -> clase.diaSemana() == null
                            || !diaSolicitado.equalsIgnoreCase(
                            clase.diaSemana().trim()
                    )
            );
        }


        // Filtrar por jornada cuando se indique.
        // TODAS significa que no se aplica restricción.
        if (filtros.jornada() != null
                && !filtros.jornada().isBlank()
                && !"TODAS".equalsIgnoreCase(
                filtros.jornada().trim())) {

            String jornadaSolicitada = filtros.jornada().trim();

            detalle.removeIf(
                    clase -> clase.jornada() == null
                            || !jornadaSolicitada.equalsIgnoreCase(
                            clase.jornada().trim()
                    )
            );
        }


        // Filtrar por asignatura cuando se seleccione una.
        if (filtros.asignaturaId() != null) {

            detalle.removeIf(
                    clase -> !filtros.asignaturaId()
                            .equals(clase.asignaturaId())
            );
        }
        // Filtrar por estado del horario cuando se indique.
        if (filtros.estado() != null
                && !filtros.estado().isBlank()) {

            String estadoSolicitado = filtros.estado().trim();

            detalle.removeIf(
                    clase -> clase.estadoHorario() == null
                            || !estadoSolicitado.equalsIgnoreCase(
                            clase.estadoHorario().trim()
                    )
            );
        }

        // Filtrar por docente cuando se seleccione uno.
        if (filtros.docenteId() != null) {

            detalle.removeIf(
                    clase -> !filtros.docenteId()
                            .equals(clase.docenteId())
            );
        }

        /*
         * Orden cronologico de los dias de clase.
         * No se ordenan alfabeticamente.
         */

        detalle.sort(
                Comparator.comparingInt(
                        (InformeHorarioClaseDetalleResponse clase) ->
                                ordenDia(clase.diaSemana())
                ).thenComparing(
                        InformeHorarioClaseDetalleResponse::horaInicio,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                ).thenComparing(
                        InformeHorarioClaseDetalleResponse::nombreDocente,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
        );

        Set<UUID> docentes = new HashSet<>();
        Set<UUID> grupos = new HashSet<>();
        Set<UUID> asignaturas = new HashSet<>();

        long horasRegulares = 0L;
        long horasContrajornada = 0L;

        Map<ClaveCarga, AcumuladorCarga> cargas =
                new LinkedHashMap<>();

        for (InformeHorarioClaseDetalleResponse clase : detalle) {

            if (clase.docenteId() != null) {
                docentes.add(clase.docenteId());
            }

            if (clase.grupoId() != null) {
                grupos.add(clase.grupoId());
            }

            if (clase.asignaturaId() != null) {
                asignaturas.add(clase.asignaturaId());
            }

            if ("CONTRAJORNADA".equalsIgnoreCase(clase.jornada())) {
                horasContrajornada++;
            } else {
                horasRegulares++;
            }

            ClaveCarga clave = new ClaveCarga(
                    clase.docenteId(),
                    clase.grupoId(),
                    clase.asignaturaId()
            );

            AcumuladorCarga carga = cargas.computeIfAbsent(
                    clave,
                    ignorada -> new AcumuladorCarga(clase)
            );

            carga.agregarClase(clase);
        }

        List<InformeHorarioCargaResponse> resumen =
                cargas.values().stream()
                        .map(AcumuladorCarga::aResponse)
                        .toList();

        return new InformeHorariosResponse(
                anioEscolarId,
                nombreAnioEscolar,
                filtros.fechaInicio(),
                filtros.fechaFin(),
                nombreInstitucion,
                nombreSede,
                filtros,
                detalle,
                resumen,
                detalle.size(),
                docentes.size(),
                grupos.size(),
                asignaturas.size(),
                (long) detalle.size(),
                horasRegulares,
                horasContrajornada
        );
    }

    /**
     * Ordena los dias de lunes a viernes.
     * Los valores desconocidos se ubican al final.
     */
    private int ordenDia(String dia) {

        if (dia == null) {
            return 99;
        }

        return switch (dia.toUpperCase()) {
            case "LUNES" -> DayOfWeek.MONDAY.getValue();
            case "MARTES" -> DayOfWeek.TUESDAY.getValue();
            case "MIERCOLES", "MIÉRCOLES" ->
                    DayOfWeek.WEDNESDAY.getValue();
            case "JUEVES" -> DayOfWeek.THURSDAY.getValue();
            case "VIERNES" -> DayOfWeek.FRIDAY.getValue();
            default -> 99;
        };
    }

    private record ClaveCarga(
            UUID docenteId,
            UUID grupoId,
            UUID asignaturaId
    ) {
    }

    /**
     * Acumula la carga horaria por docente,
     * grupo y asignatura.
     *
     * Las horas extras se identifican exclusivamente
     * mediante el indicador esHoraExtra.
     */
    private static class AcumuladorCarga {

        private final InformeHorarioClaseDetalleResponse base;

        private long horasOrdinarias;
        private long horasExtras;
        private long jornadaRegular;
        private long contrajornada;

        private AcumuladorCarga(
                InformeHorarioClaseDetalleResponse base) {
            this.base = base;
        }

        private void agregarClase(
                InformeHorarioClaseDetalleResponse clase) {

            if (clase.esHoraExtra()) {
                horasExtras++;
            } else {
                horasOrdinarias++;
            }

            if ("CONTRAJORNADA".equalsIgnoreCase(clase.jornada())) {
                contrajornada++;
            } else {
                jornadaRegular++;
            }
        }

        private InformeHorarioCargaResponse aResponse() {

            return new InformeHorarioCargaResponse(
                    base.docenteId(),
                    base.nombreDocente(),
                    base.grupoId(),
                    base.nombreGrupo(),
                    base.asignaturaId(),
                    base.nombreAsignatura(),
                    horasOrdinarias,
                    horasExtras,
                    jornadaRegular,
                    contrajornada,
                    horasOrdinarias + horasExtras
            );
        }
    }
}

package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeHorarioCeldaSemanalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * HU-04 / SCRUM-31.
 *
 * Organiza las clases programadas en celdas semanales
 * por jornada, franja horaria y dia.
 *
 * No consulta PostgreSQL ni genera persistencia.
 * No modifica la programacion academica.
 *
 * Recibe clases previamente consultadas y filtradas
 * por el modulo de Gestion de Horarios.
 */
@Service
public class InformeHorarioSemanalService {

    private static final List<String> DIAS = List.of(
            "LUNES",
            "MARTES",
            "MIERCOLES",
            "JUEVES",
            "VIERNES"
    );

    /**
     * Construye las celdas correspondientes a las
     * clases recibidas.
     *
     * No inventa franjas vacias: para representar
     * espacios sin programacion se necesitaran las
     * franjas oficiales del turno institucional.
     */
    public List<InformeHorarioCeldaSemanalResponse>
    construirCuadricula(
            List<InformeHorarioClaseDetalleResponse> clases) {

        if (clases == null || clases.isEmpty()) {
            return List.of();
        }

        Map<ClaveCelda, List<InformeHorarioClaseDetalleResponse>>
                agrupadas = new LinkedHashMap<>();

        for (InformeHorarioClaseDetalleResponse clase : clases) {

            Objects.requireNonNull(
                    clase,
                    "No se permiten clases nulas."
            );

            String dia = normalizarDia(clase.diaSemana());
            String jornada = normalizarJornada(clase.jornada());

            validarHoras(
                    clase.horaInicio(),
                    clase.horaFin()
            );

            ClaveCelda clave = new ClaveCelda(
                    jornada,
                    dia,
                    clase.horaInicio(),
                    clase.horaFin()
            );

            agrupadas.computeIfAbsent(
                    clave,
                    ignorada -> new ArrayList<>()
            ).add(clase);
        }

        return agrupadas.entrySet().stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (Map.Entry<ClaveCelda,
                                                List<InformeHorarioClaseDetalleResponse>>
                                                 entrada) ->
                                                ordenJornada(
                                                        entrada.getKey().jornada()
                                                )
                                )
                                .thenComparing(
                                        entrada ->
                                                entrada.getKey().horaInicio()
                                )
                                .thenComparing(
                                        entrada ->
                                                entrada.getKey().horaFin()
                                )
                                .thenComparingInt(
                                        entrada ->
                                                DIAS.indexOf(
                                                        entrada.getKey().dia()
                                                )
                                )
                )
                .map(entrada ->
                        new InformeHorarioCeldaSemanalResponse(
                                entrada.getKey().dia(),
                                entrada.getKey().horaInicio(),
                                entrada.getKey().horaFin(),
                                entrada.getKey().jornada(),
                                entrada.getValue()
                        )
                )
                .toList();
    }

    /**
     * Solo acepta los dias definidos actualmente
     * para la programacion academica institucional.
     */
    private String normalizarDia(String dia) {

        if (dia == null || dia.isBlank()) {
            throw new IllegalArgumentException(
                    "El dia de la clase es obligatorio."
            );
        }

        String normalizado = dia.trim()
                .toUpperCase(Locale.ROOT)
                .replace('É', 'E');

        if (!DIAS.contains(normalizado)) {
            throw new IllegalArgumentException(
                    "Dia de clase no reconocido: " + dia
            );
        }

        return normalizado;
    }

    /**
     * Mantiene separadas la jornada regular
     * y la contrajornada.
     *
     * Un valor desconocido no debe clasificarse
     * automaticamente como jornada regular.
     */
    private String normalizarJornada(String jornada) {

        if (jornada == null || jornada.isBlank()) {
            throw new IllegalArgumentException(
                    "La jornada es obligatoria."
            );
        }

        String normalizada = jornada.trim()
                .toUpperCase(Locale.ROOT);

        if (!normalizada.equals("REGULAR")
                && !normalizada.equals("CONTRAJORNADA")) {

            throw new IllegalArgumentException(
                    "Jornada no reconocida: " + jornada
            );
        }

        return normalizada;
    }

    private void validarHoras(
            LocalTime inicio,
            LocalTime fin) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "Las horas de inicio y fin son obligatorias."
            );
        }

        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior "
                            + "a la hora de finalizacion."
            );
        }
    }

    private int ordenJornada(String jornada) {

        return jornada.equals("REGULAR") ? 0 : 1;
    }

    private record ClaveCelda(
            String jornada,
            String dia,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
    }
}

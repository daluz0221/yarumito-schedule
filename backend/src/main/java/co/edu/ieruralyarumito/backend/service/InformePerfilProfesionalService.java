
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeIdoneidadDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeTituloProfesionalDetalleResponse;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HU-04 / SCRUM-31.
 *
 * Construye el informe de perfiles profesionales
 * e idoneidades utilizando datos ya registrados.
 *
 * No modifica entidades ni persiste informes.
 */
@Service
public class InformePerfilProfesionalService {

    private final DocenteRepository docenteRepository;
    private final TituloProfesionalRepository tituloRepository;
    private final IdoneidadRepository idoneidadRepository;

    public InformePerfilProfesionalService(
            DocenteRepository docenteRepository,
            TituloProfesionalRepository tituloRepository,
            IdoneidadRepository idoneidadRepository) {

        this.docenteRepository = docenteRepository;
        this.tituloRepository = tituloRepository;
        this.idoneidadRepository = idoneidadRepository;
    }

    /**
     * Filtros opcionales:
     * docenteId: docente específico.
     * areaId: área cubierta por la idoneidad.
     * tipo: tipo de idoneidad.
     *
     * Los filtros nulos representan "Todos".
     */
    @Transactional(readOnly = true)
    public InformePerfilProfesionalResponse generarInforme(
            UUID docenteId,
            UUID areaId,
            TipoIdoneidad tipo) {

        LocalDate fechaReferencia = LocalDate.now();

        /*
         * Para esta primera versión usamos los
         * repositorios existentes, sin consultas
         * individuales por cada docente.
         */
        List<Docente> docentesConsultados =
                docenteId == null
                        ? docenteRepository.findAll()
                        : docenteRepository.findById(docenteId)
                        .map(List::of)
                        .orElseGet(List::of);

        Map<UUID, Docente> docentes = new LinkedHashMap<>();

        for (Docente docente : docentesConsultados) {
            docentes.putIfAbsent(docente.getId(), docente);
        }

        if (docentes.isEmpty()) {
            return new InformePerfilProfesionalResponse(
                    fechaReferencia, 0, 0, 0, List.of()
            );
        }

        /*
         * Conservamos títulos e idoneidades
         * en conjuntos separados.
         */
        Map<UUID, Map<UUID, TituloProfesional>> titulos =
                new HashMap<>();

        for (TituloProfesional titulo : tituloRepository.findAll()) {

            UUID idDocente = titulo.getDocente().getId();

            if (docentes.containsKey(idDocente)) {
                titulos.computeIfAbsent(
                                idDocente,
                                id -> new LinkedHashMap<>()
                        )
                        .putIfAbsent(titulo.getId(), titulo);
            }
        }

        Map<UUID, Map<UUID, Idoneidad>> idoneidades =
                new HashMap<>();

        for (Idoneidad idoneidad : idoneidadRepository.findAll()) {

            UUID idDocente = idoneidad.getDocente().getId();

            if (!docentes.containsKey(idDocente)) {
                continue;
            }

            if (areaId != null &&
                    !areaId.equals(idoneidad.getArea().getId())) {
                continue;
            }

            if (tipo != null && tipo != idoneidad.getTipo()) {
                continue;
            }

            idoneidades.computeIfAbsent(
                            idDocente,
                            id -> new LinkedHashMap<>()
                    )
                    .putIfAbsent(idoneidad.getId(), idoneidad);
        }

        List<InformePerfilProfesionalResponse.DetalleDocente> detalle =
                new ArrayList<>();

        long totalTitulos = 0;
        long totalIdoneidades = 0;

        for (Docente docente : docentes.values()) {

            UUID id = docente.getId();

            Map<UUID, Idoneidad> habilitaciones =
                    idoneidades.getOrDefault(id, Map.of());

            /*
             * Si se filtra por área o tipo,
             * mostramos únicamente docentes que
             * tienen idoneidades coincidentes.
             */
            if ((areaId != null || tipo != null)
                    && habilitaciones.isEmpty()) {
                continue;
            }

            List<InformeTituloProfesionalDetalleResponse> listaTitulos =
                    titulos.getOrDefault(id, Map.of())
                            .values().stream()
                            .map(this::convertirTitulo)
                            .sorted(Comparator.comparing(
                                    InformeTituloProfesionalDetalleResponse
                                            ::nombreTitulo,
                                    String.CASE_INSENSITIVE_ORDER
                            ).thenComparing(
                                    InformeTituloProfesionalDetalleResponse
                                            ::tituloId
                            ))
                            .toList();

            List<InformeIdoneidadDetalleResponse> listaIdoneidades =
                    habilitaciones.values().stream()
                            .map(i -> convertirIdoneidad(
                                    i, fechaReferencia))
                            .sorted(Comparator.comparing(
                                    InformeIdoneidadDetalleResponse
                                            ::idoneidadId
                            ))
                            .toList();

            totalTitulos = Math.addExact(
                    totalTitulos, listaTitulos.size()
            );

            totalIdoneidades = Math.addExact(
                    totalIdoneidades, listaIdoneidades.size()
            );

            detalle.add(
                    new InformePerfilProfesionalResponse.DetalleDocente(
                            id,
                            (docente.getNombres() + " "
                                    + docente.getApellidos()).trim(),
                            docente.getAreaNombramiento().getNombre(),
                            listaTitulos,
                            listaIdoneidades
                    )
            );
        }

        detalle.sort(Comparator
                .comparing(
                        InformePerfilProfesionalResponse.DetalleDocente
                                ::nombreCompleto,
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(
                        InformePerfilProfesionalResponse.DetalleDocente
                                ::docenteId
                ));

        return new InformePerfilProfesionalResponse(
                fechaReferencia,
                detalle.size(),
                totalTitulos,
                totalIdoneidades,
                detalle
        );
    }

    private InformeTituloProfesionalDetalleResponse convertirTitulo(
            TituloProfesional titulo) {

        return new InformeTituloProfesionalDetalleResponse(
                titulo.getId(),
                titulo.getNombreTitulo(),
                titulo.getNivel(),
                titulo.getInstitucion(),
                titulo.getAnioGraduacion()
        );
    }

    private InformeIdoneidadDetalleResponse convertirIdoneidad(
            Idoneidad idoneidad,
            LocalDate fechaReferencia) {

        LocalDate desde = idoneidad.getVigenteDesde();
        LocalDate hasta = idoneidad.getVigenteHasta();

        boolean vigente =
                desde != null
                        && !desde.isAfter(fechaReferencia)
                        && (hasta == null ||
                        !hasta.isBefore(fechaReferencia));

        UUID asignaturaId =
                idoneidad.getAsignatura() == null
                        ? null
                        : idoneidad.getAsignatura().getId();

        String nombreAsignatura =
                idoneidad.getAsignatura() == null
                        ? null
                        : idoneidad.getAsignatura().getNombre();

        UUID tituloSoporteId =
                idoneidad.getTituloSoporte() == null
                        ? null
                        : idoneidad.getTituloSoporte().getId();

        return new InformeIdoneidadDetalleResponse(
                idoneidad.getId(),
                idoneidad.getArea().getId(),
                idoneidad.getArea().getNombre(),
                asignaturaId,
                nombreAsignatura,
                idoneidad.getTipo(),
                tituloSoporteId,
                desde,
                hasta,
                vigente
        );
    }
}

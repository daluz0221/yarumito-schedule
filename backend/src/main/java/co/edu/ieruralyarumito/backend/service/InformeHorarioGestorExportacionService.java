package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * HU-04 / SCRUM-31.
 *
 * Exporta los horarios institucionales obtenidos
 * directamente desde la integración oficial
 * con el Gestor de Horarios.
 *
 * No modifica PostgreSQL ni duplica lógica
 * del Gestor.
 */
@Service
public class InformeHorarioGestorExportacionService {

    private final InformeHorarioGestorIntegracionService integracion;
    private final InformeHorariosPdfService pdfService;
    private final InformeHorariosExcelService excelService;

    public InformeHorarioGestorExportacionService(
            InformeHorarioGestorIntegracionService integracion,
            InformeHorariosPdfService pdfService,
            InformeHorariosExcelService excelService) {

        this.integracion = Objects.requireNonNull(integracion);
        this.pdfService = Objects.requireNonNull(pdfService);
        this.excelService = Objects.requireNonNull(excelService);
    }

    /**
     * Exporta el horario institucional de un grupo en PDF.
     */
    public byte[] exportarPdfInstitucionalPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                integracion.generarInstitucional(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                integracion.obtenerDescansosOficiales(
                        grupoId
                );

        return pdfService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }

    /**
     * Exporta el horario institucional de un grupo en Excel.
     */
    public byte[] exportarExcelInstitucionalPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                integracion.generarInstitucional(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                integracion.obtenerDescansosOficiales(
                        grupoId
                );

        return excelService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }

    /**
     * Exporta exclusivamente un horario PUBLICADO en PDF.
     */
    public byte[] exportarPdfPublicadoPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                integracion.generarPublicados(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                integracion.obtenerDescansosOficiales(
                        grupoId
                );

        return pdfService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }

    /**
     * Exporta exclusivamente un horario PUBLICADO en Excel.
     */
    public byte[] exportarExcelPublicadoPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                integracion.generarPublicados(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                integracion.obtenerDescansosOficiales(
                        grupoId
                );

        return excelService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }
}

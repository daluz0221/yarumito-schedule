
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
 * Conecta los horarios obtenidos del Gestor
 * con los exportadores existentes de informes.
 *
 * Los descansos oficiales se obtienen mediante
 * el puente de integracion con Juan Camilo.
 *
 * No modifica PostgreSQL ni genera persistencia.
 */
@Service
public class InformeHorarioGestorExportacionService {

    private final InformeHorarioJuanCamiloBridgeService puente;
    private final InformeHorariosPdfService pdfService;
    private final InformeHorariosExcelService excelService;

    public InformeHorarioGestorExportacionService(
            InformeHorarioJuanCamiloBridgeService puente,
            InformeHorariosPdfService pdfService,
            InformeHorariosExcelService excelService) {

        this.puente = Objects.requireNonNull(puente);
        this.pdfService = Objects.requireNonNull(pdfService);
        this.excelService = Objects.requireNonNull(excelService);
    }

    /**
     * Exporta el horario institucional de un grupo en PDF.
     *
     * Integra los descansos oficiales proporcionados
     * por el Gestor de Horarios.
     *
     * No calcula ni inventa descansos.
     */
    public byte[] exportarPdfInstitucionalPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                puente.generarInstitucional(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                puente.obtenerDescansosOficiales(grupoId);

        return pdfService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }

    /**
     * Exporta el horario institucional de un grupo en Excel.
     *
     * Integra las franjas oficiales de descanso
     * proporcionadas por el Gestor de Horarios.
     *
     * No calcula ni inventa descansos.
     */
    public byte[] exportarExcelInstitucionalPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                puente.generarInstitucional(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                puente.obtenerDescansosOficiales(grupoId);

        return excelService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }

    /**
     * Exporta el horario PUBLICADO de un grupo en PDF.
     *
     * No admite horarios en BORRADOR ni confirmaciones
     * de asignaciones academicas como sustituto
     * del estado PUBLICADO.
     *
     * Primero valida la publicacion mediante el puente.
     * Despues obtiene los descansos oficiales.
     *
     * No modifica PostgreSQL.
     */
    public byte[] exportarPdfPublicadoPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                puente.generarPublicados(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                puente.obtenerDescansosOficiales(grupoId);

        return pdfService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }

    /**
     * Exporta el horario PUBLICADO de un grupo en Excel.
     *
     * Primero valida la publicacion mediante el puente.
     * Despues obtiene los descansos oficiales.
     *
     * No modifica el estado del horario ni
     * crea registros en la base de datos.
     */
    public byte[] exportarExcelPublicadoPorGrupo(
            UUID grupoId,
            String nombreInstitucion,
            InformeHorariosFiltroRequest filtros) {

        InformeHorariosResponse informe =
                puente.generarPublicados(
                        List.of(grupoId),
                        nombreInstitucion,
                        filtros
                );

        List<FranjaHorariaResponse> descansos =
                puente.obtenerDescansosOficiales(grupoId);

        return excelService.exportarHorarioSemanalPorGrupo(
                informe,
                descansos
        );
    }
}

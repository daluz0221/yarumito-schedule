package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;

import co.edu.ieruralyarumito.backend.service.InformeAreasAsignaturasPdfService;
import co.edu.ieruralyarumito.backend.service.InformeAreasAsignaturasService;
import co.edu.ieruralyarumito.backend.service.InformeExcelService;
import co.edu.ieruralyarumito.backend.service.InformePdfService;
import co.edu.ieruralyarumito.backend.service.InformePerfilProfesionalPdfService;
import co.edu.ieruralyarumito.backend.service.InformePerfilProfesionalService;
import co.edu.ieruralyarumito.backend.service.InformePlantaDocenteService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * HU-04 / SCRUM-31.
 *
 * Exportaciones PDF y Excel de los informes institucionales.
 *
 * Planta Docente permite:
 * - Consultar toda la planta docente.
 * - Filtrar por un docente especifico.
 * - Consultar carga academica por año escolar.
 *
 * Conserva las exportaciones de los demas informes.
 *
 * Todas las operaciones son de lectura.
 */
@RestController
@RequestMapping("/api/v1/informes")
public class InformeExportacionController {

    private static final MediaType EXCEL =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument."
                            + "spreadsheetml.sheet"
            );

    private final InformePlantaDocenteService plantaService;
    private final InformeAreasAsignaturasService areasService;
    private final InformePerfilProfesionalService perfilesService;

    private final InformePdfService plantaPdfService;
    private final InformeAreasAsignaturasPdfService areasPdfService;
    private final InformePerfilProfesionalPdfService perfilesPdfService;
    private final InformeExcelService excelService;

    public InformeExportacionController(
            InformePlantaDocenteService plantaService,
            InformeAreasAsignaturasService areasService,
            InformePerfilProfesionalService perfilesService,
            InformePdfService plantaPdfService,
            InformeAreasAsignaturasPdfService areasPdfService,
            InformePerfilProfesionalPdfService perfilesPdfService,
            InformeExcelService excelService) {

        this.plantaService = plantaService;
        this.areasService = areasService;
        this.perfilesService = perfilesService;

        this.plantaPdfService = plantaPdfService;
        this.areasPdfService = areasPdfService;
        this.perfilesPdfService = perfilesPdfService;
        this.excelService = excelService;
    }

    // =====================================================
    // INFORME 1 - PLANTA DOCENTE
    // =====================================================

    /**
     * Exporta Planta Docente en PDF.
     *
     * docenteId es opcional:
     * - null: todos los docentes.
     * - informado: docente especifico.
     *
     * anioEscolarId es opcional:
     * - null: informe original.
     * - informado: carga academica y horas extras.
     */
    @GetMapping("/planta-docente/pdf")
    public ResponseEntity<byte[]> plantaDocentePdf(
            @RequestParam(required = false) UUID areaNombramientoId,
            @RequestParam(required = false) EstadoDocente estado,
            @RequestParam(required = false) TipoVinculacion tipoVinculacion,
            @RequestParam(required = false) UUID anioEscolarId,
            @RequestParam(required = false) UUID docenteId,
            Authentication authentication) {

        exigirRector(authentication);

        InformePlantaDocenteResponse informe =
                consultarPlantaDocente(
                        areaNombramientoId,
                        estado,
                        tipoVinculacion,
                        anioEscolarId,
                        docenteId
                );

        byte[] archivo =
                plantaPdfService.exportarPlantaDocente(
                        informe,
                        null
                );

        return descargar(
                archivo,
                MediaType.APPLICATION_PDF,
                "planta-docente.pdf"
        );
    }

    /**
     * Exporta Planta Docente en Excel.
     *
     * Utiliza los mismos filtros y datos
     * que la consulta JSON y la exportacion PDF.
     */
    @GetMapping("/planta-docente/excel")
    public ResponseEntity<byte[]> plantaDocenteExcel(
            @RequestParam(required = false) UUID areaNombramientoId,
            @RequestParam(required = false) EstadoDocente estado,
            @RequestParam(required = false) TipoVinculacion tipoVinculacion,
            @RequestParam(required = false) UUID anioEscolarId,
            @RequestParam(required = false) UUID docenteId,
            Authentication authentication) {

        exigirRector(authentication);

        InformePlantaDocenteResponse informe =
                consultarPlantaDocente(
                        areaNombramientoId,
                        estado,
                        tipoVinculacion,
                        anioEscolarId,
                        docenteId
                );

        byte[] archivo =
                excelService.exportarPlantaDocente(
                        informe,
                        null
                );

        return descargar(
                archivo,
                EXCEL,
                "planta-docente.xlsx"
        );
    }

    /**
     * Consulta centralizada de Planta Docente.
     *
     * Conserva los filtros originales.
     * Agrega el filtro opcional por docente.
     *
     * PDF y Excel reciben exactamente
     * los mismos datos.
     */
    private InformePlantaDocenteResponse consultarPlantaDocente(
            UUID areaNombramientoId,
            EstadoDocente estado,
            TipoVinculacion tipoVinculacion,
            UUID anioEscolarId,
            UUID docenteId) {

        return plantaService.generarInforme(
                areaNombramientoId,
                estado,
                tipoVinculacion,
                anioEscolarId,
                docenteId
        );
    }

    // =====================================================
    // INFORME 2 - AREAS Y ASIGNATURAS
    // =====================================================

    @GetMapping("/areas-asignaturas/pdf")
    public ResponseEntity<byte[]> areasAsignaturasPdf(
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) Boolean activa,
            Authentication authentication) {

        exigirRector(authentication);

        InformeAreasAsignaturasResponse informe =
                areasService.generarInforme(
                        areaId,
                        activa
                );

        byte[] archivo =
                areasPdfService.exportarAreasAsignaturas(
                        informe,
                        null
                );

        return descargar(
                archivo,
                MediaType.APPLICATION_PDF,
                "areas-asignaturas.pdf"
        );
    }

    @GetMapping("/areas-asignaturas/excel")
    public ResponseEntity<byte[]> areasAsignaturasExcel(
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) Boolean activa,
            Authentication authentication) {

        exigirRector(authentication);

        InformeAreasAsignaturasResponse informe =
                areasService.generarInforme(
                        areaId,
                        activa
                );

        byte[] archivo =
                excelService.exportarAreasAsignaturas(
                        informe,
                        null
                );

        return descargar(
                archivo,
                EXCEL,
                "areas-asignaturas.xlsx"
        );
    }

    // =====================================================
    // INFORME 3 - PERFILES PROFESIONALES
    // =====================================================

    @GetMapping("/perfiles-profesionales/pdf")
    public ResponseEntity<byte[]> perfilesProfesionalesPdf(
            @RequestParam(required = false) UUID docenteId,
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) TipoIdoneidad tipo,
            Authentication authentication) {

        exigirRector(authentication);

        InformePerfilProfesionalResponse informe =
                perfilesService.generarInforme(
                        docenteId,
                        areaId,
                        tipo
                );

        byte[] archivo =
                perfilesPdfService.exportarPerfilProfesional(
                        informe,
                        null
                );

        return descargar(
                archivo,
                MediaType.APPLICATION_PDF,
                "perfiles-profesionales.pdf"
        );
    }

    @GetMapping("/perfiles-profesionales/excel")
    public ResponseEntity<byte[]> perfilesProfesionalesExcel(
            @RequestParam(required = false) UUID docenteId,
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) TipoIdoneidad tipo,
            Authentication authentication) {

        exigirRector(authentication);

        InformePerfilProfesionalResponse informe =
                perfilesService.generarInforme(
                        docenteId,
                        areaId,
                        tipo
                );

        byte[] archivo =
                excelService.exportarPerfilProfesional(
                        informe,
                        null
                );

        return descargar(
                archivo,
                EXCEL,
                "perfiles-profesionales.xlsx"
        );
    }

    // =====================================================
    // DESCARGA DE ARCHIVOS
    // =====================================================

    private ResponseEntity<byte[]> descargar(
            byte[] archivo,
            MediaType tipo,
            String nombreArchivo) {

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentLength(archivo.length)
                .body(archivo);
    }

    // =====================================================
    // CONTROL DE ACCESO
    // =====================================================

    private void exigirRector(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    UNAUTHORIZED,
                    "Se requiere autenticacion."
            );
        }

        boolean rector = authentication.getAuthorities()
                .stream()
                .anyMatch(autoridad -> {
                    String nombre = autoridad.getAuthority();

                    return "RECTOR".equals(nombre)
                            || "ROLE_RECTOR".equals(nombre);
                });

        if (!rector) {

            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Acceso exclusivo del rector."
            );
        }
    }
}

package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosFiltroRequest;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.service.InformeAreasAsignaturasService;
import co.edu.ieruralyarumito.backend.service.InformeHorarioGestorExportacionService;
import co.edu.ieruralyarumito.backend.service.InformeHorarioGestorIntegracionService;
import co.edu.ieruralyarumito.backend.service.InformePerfilProfesionalService;
import co.edu.ieruralyarumito.backend.service.InformePlantaDocenteService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Controller
@RequestMapping("/api/v1/informes")
public class InformeController {

    private static final String INSTITUCION =
            "I.E. Rural Yarumito";

    private static final MediaType EXCEL =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument."
                            + "spreadsheetml.sheet"
            );

    private final InformePlantaDocenteService plantaService;
    private final InformeAreasAsignaturasService areasService;
    private final InformePerfilProfesionalService perfilesService;
    private final InformeHorarioGestorIntegracionService horariosService;
    private final InformeHorarioGestorExportacionService exportacionHorarios;

    public InformeController(
            InformePlantaDocenteService plantaService,
            InformeAreasAsignaturasService areasService,
            InformePerfilProfesionalService perfilesService,
            InformeHorarioGestorIntegracionService horariosService,
            InformeHorarioGestorExportacionService exportacionHorarios) {

        this.plantaService = plantaService;
        this.areasService = areasService;
        this.perfilesService = perfilesService;
        this.horariosService = horariosService;
        this.exportacionHorarios = exportacionHorarios;
    }

    // INFORME 1: PLANTA DOCENTE

    @GetMapping("/planta-docente")
    @ResponseBody
    public InformePlantaDocenteResponse plantaDocente(
            @RequestParam(required = false) UUID areaNombramientoId,
            @RequestParam(required = false) EstadoDocente estado,
            @RequestParam(required = false) TipoVinculacion tipoVinculacion,
            Authentication authentication) {

        exigirRector(authentication);

        return plantaService.generarInforme(
                areaNombramientoId,
                estado,
                tipoVinculacion
        );
    }

    // INFORME 2: AREAS Y ASIGNATURAS

    @GetMapping("/areas-asignaturas")
    @ResponseBody
    public InformeAreasAsignaturasResponse areasAsignaturas(
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) Boolean activa,
            Authentication authentication) {

        exigirRector(authentication);

        return areasService.generarInforme(
                areaId,
                activa
        );
    }

    // INFORME 3: PERFILES PROFESIONALES

    @GetMapping("/perfiles-profesionales")
    @ResponseBody
    public InformePerfilProfesionalResponse perfilesProfesionales(
            @RequestParam(required = false) UUID docenteId,
            @RequestParam(required = false) UUID areaId,
            @RequestParam(required = false) TipoIdoneidad tipo,
            Authentication authentication) {

        exigirRector(authentication);

        return perfilesService.generarInforme(
                docenteId,
                areaId,
                tipo
        );
    }

    // INFORME 4: HORARIOS ACADEMICOS

    @PostMapping("/horarios/grupos/{grupoId}")
    @ResponseBody
    public InformeHorariosResponse horarioInstitucional(
            @PathVariable UUID grupoId,
            @RequestBody InformeHorariosFiltroRequest filtros,
            Authentication authentication) {

        exigirRector(authentication);

        return horariosService.generarInstitucional(
                List.of(grupoId),
                INSTITUCION,
                filtros
        );
    }

    @PostMapping("/horarios/grupos/{grupoId}/publicado")
    @ResponseBody
    public InformeHorariosResponse horarioPublicado(
            @PathVariable UUID grupoId,
            @RequestBody InformeHorariosFiltroRequest filtros,
            Authentication authentication) {

        exigirRector(authentication);

        return horariosService.generarPublicados(
                List.of(grupoId),
                INSTITUCION,
                filtros
        );
    }

    // EXPORTACIONES DE HORARIOS

    @PostMapping("/horarios/grupos/{grupoId}/pdf")
    public ResponseEntity<byte[]> horarioPdf(
            @PathVariable UUID grupoId,
            @RequestBody InformeHorariosFiltroRequest filtros,
            Authentication authentication) {

        exigirRector(authentication);

        byte[] contenido =
                exportacionHorarios.exportarPdfInstitucionalPorGrupo(
                        grupoId,
                        INSTITUCION,
                        filtros
                );

        return descargar(
                contenido,
                MediaType.APPLICATION_PDF,
                "horario-institucional.pdf"
        );
    }

    @PostMapping("/horarios/grupos/{grupoId}/excel")
    public ResponseEntity<byte[]> horarioExcel(
            @PathVariable UUID grupoId,
            @RequestBody InformeHorariosFiltroRequest filtros,
            Authentication authentication) {

        exigirRector(authentication);

        byte[] contenido =
                exportacionHorarios.exportarExcelInstitucionalPorGrupo(
                        grupoId,
                        INSTITUCION,
                        filtros
                );

        return descargar(
                contenido,
                EXCEL,
                "horario-institucional.xlsx"
        );
    }

    @PostMapping("/horarios/grupos/{grupoId}/publicado/pdf")
    public ResponseEntity<byte[]> horarioPublicadoPdf(
            @PathVariable UUID grupoId,
            @RequestBody InformeHorariosFiltroRequest filtros,
            Authentication authentication) {

        exigirRector(authentication);

        byte[] contenido =
                exportacionHorarios.exportarPdfPublicadoPorGrupo(
                        grupoId,
                        INSTITUCION,
                        filtros
                );

        return descargar(
                contenido,
                MediaType.APPLICATION_PDF,
                "horario-publicado.pdf"
        );
    }

    @PostMapping("/horarios/grupos/{grupoId}/publicado/excel")
    public ResponseEntity<byte[]> horarioPublicadoExcel(
            @PathVariable UUID grupoId,
            @RequestBody InformeHorariosFiltroRequest filtros,
            Authentication authentication) {

        exigirRector(authentication);

        byte[] contenido =
                exportacionHorarios.exportarExcelPublicadoPorGrupo(
                        grupoId,
                        INSTITUCION,
                        filtros
                );

        return descargar(
                contenido,
                EXCEL,
                "horario-publicado.xlsx"
        );
    }

    private ResponseEntity<byte[]> descargar(
            byte[] contenido,
            MediaType tipo,
            String nombreArchivo) {

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .body(contenido);
    }

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

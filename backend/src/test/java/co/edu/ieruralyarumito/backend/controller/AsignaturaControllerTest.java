package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignaturaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignaturaRequest;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.service.AsignaturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas unitarias de los endpoints de AsignaturaController.
@ExtendWith(MockitoExtension.class)
public class AsignaturaControllerTest {

    @Mock
    private AsignaturaService asignaturaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {

        AsignaturaController asignaturaController =
                new AsignaturaController(asignaturaService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(asignaturaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void consultarAsignatura_debeRetornarOk() throws Exception {

        UUID asignaturaId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        AsignaturaResponse response =
                new AsignaturaResponse();

        response.setId(asignaturaId);
        response.setAreaId(areaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT-01");
        response.setAbreviatura("MAT");
        response.setTipoAulaRequerida(
                TipoAulaRequerida.AULA
        );
        response.setMaxClasesConsecutivas(2);
        response.setActiva(true);

        when(asignaturaService.consultarAsignatura(asignaturaId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/asignaturas/{id}", asignaturaId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(asignaturaId.toString()))
                .andExpect(jsonPath("$.areaId")
                        .value(areaId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Matemáticas"))
                .andExpect(jsonPath("$.codigo")
                        .value("MAT-01"));
    }

    @Test
    void listarAsignaturas_debeRetornarOk() throws Exception {

        AsignaturaResponse asignatura =
                new AsignaturaResponse();

        asignatura.setNombre("Matemáticas");
        asignatura.setCodigo("MAT-01");
        asignatura.setActiva(true);

        PageImpl<AsignaturaResponse> pagina =
                new PageImpl<>(
                        List.of(asignatura),
                        PageRequest.of(0, 20),
                        1
                );

        when(asignaturaService.listarAsignaturas(
                isNull(),
                isNull(),
                isNull(),
                any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre")
                        .value("Matemáticas"))
                .andExpect(jsonPath("$.content[0].codigo")
                        .value("MAT-01"));
    }

    @Test
    void registrarAsignatura_debeRetornarCreated()
            throws Exception {

        UUID asignaturaId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        AsignaturaResponse response =
                new AsignaturaResponse();

        response.setId(asignaturaId);
        response.setAreaId(areaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT-01");
        response.setAbreviatura("MAT");
        response.setExigeIdoneidadEstricta(true);
        response.setEsMediaTecnica(false);
        response.setRequiereDocenteExclusivo(false);
        response.setTipoAulaRequerida(
                TipoAulaRequerida.AULA
        );
        response.setMaxClasesConsecutivas(2);
        response.setActiva(true);

        when(asignaturaService.registrarAsignatura(
                any(CrearAsignaturaRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "areaId": "%s",
                  "nombre": "Matemáticas",
                  "codigo": "MAT-01",
                  "abreviatura": "MAT",
                  "colorUi": "#336699",
                  "exigeIdoneidadEstricta": true,
                  "esMediaTecnica": false,
                  "requiereDocenteExclusivo": false,
                  "tipoAulaRequerida": "AULA",
                  "maxClasesConsecutivas": 2,
                  "activa": true
                }
                """.formatted(areaId);

        mockMvc.perform(
                        post("/api/v1/asignaturas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(asignaturaId.toString()))
                .andExpect(jsonPath("$.areaId")
                        .value(areaId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Matemáticas"))
                .andExpect(jsonPath("$.codigo")
                        .value("MAT-01"));
    }

    @Test
    void actualizarAsignatura_debeRetornarOk()
            throws Exception {

        UUID asignaturaId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        AsignaturaResponse response =
                new AsignaturaResponse();

        response.setId(asignaturaId);
        response.setAreaId(areaId);
        response.setNombre("Ciencias Naturales");
        response.setCodigo("CN-01");
        response.setTipoAulaRequerida(
                TipoAulaRequerida.LABORATORIO
        );
        response.setMaxClasesConsecutivas(2);
        response.setActiva(true);

        when(asignaturaService.actualizarAsignatura(
                eq(asignaturaId),
                any(ActualizarAsignaturaRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "areaId": "%s",
                  "nombre": "Ciencias Naturales",
                  "codigo": "CN-01",
                  "abreviatura": "CN",
                  "colorUi": "#228833",
                  "exigeIdoneidadEstricta": true,
                  "esMediaTecnica": false,
                  "requiereDocenteExclusivo": false,
                  "tipoAulaRequerida": "LABORATORIO",
                  "maxClasesConsecutivas": 2,
                  "activa": true
                }
                """.formatted(areaId);

        mockMvc.perform(
                        put("/api/v1/asignaturas/{id}", asignaturaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(asignaturaId.toString()))
                .andExpect(jsonPath("$.areaId")
                        .value(areaId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Ciencias Naturales"))
                .andExpect(jsonPath("$.codigo")
                        .value("CN-01"));
    }

    @Test
    void cambiarEstado_debeRetornarOk()
            throws Exception {

        UUID asignaturaId = UUID.randomUUID();

        AsignaturaResponse response =
                new AsignaturaResponse();

        response.setId(asignaturaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT-01");
        response.setActiva(false);

        when(asignaturaService.cambiarEstado(
                asignaturaId,
                false))
                .thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/asignaturas/{id}/estado",
                                asignaturaId
                        )
                                .param("activa", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(asignaturaId.toString()))
                .andExpect(jsonPath("$.activa")
                        .value(false));
    }

    @Test
    void registrarAsignatura_debeRetornarBadRequestConDatosInvalidos()
            throws Exception {

        String json = """
                {
                  "nombre": "Matemáticas"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/asignaturas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(asignaturaService);
    }

    @Test
    void consultarAsignatura_debeRetornarNotFound()
            throws Exception {

        UUID asignaturaId = UUID.randomUUID();

        when(asignaturaService.consultarAsignatura(asignaturaId))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "La asignatura no existe")
                );

        mockMvc.perform(
                        get(
                                "/api/v1/asignaturas/{id}",
                                asignaturaId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje")
                        .value("La asignatura no existe"));
    }
}

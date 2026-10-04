package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.AulaResponse;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.exception.CambioAulaInvalidoException;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.service.AulaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AulaControllerTest {

    @Mock
    private AulaService aulaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        AulaController controller =
                new AulaController(aulaService);

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void registrarAula_debeRetornarCreated()
            throws Exception {

        UUID sedeId = UUID.randomUUID();

        AulaResponse response =
                crearResponse(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        when(aulaService.registrarAula(any()))
                .thenReturn(response);

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "Aula 101",
                  "tipo": "AULA",
                  "capacidad": 30
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sedeId")
                        .value(sedeId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Aula 101"))
                .andExpect(jsonPath("$.tipo")
                        .value("AULA"))
                .andExpect(jsonPath("$.capacidad")
                        .value(30))
                .andExpect(jsonPath("$.activa")
                        .value(true));
    }

    @Test
    void registrarAula_debePermitirCapacidadNula()
            throws Exception {

        UUID sedeId = UUID.randomUUID();

        AulaResponse response =
                crearResponse(
                        sedeId,
                        "Laboratorio 1",
                        TipoAulaRequerida.LABORATORIO,
                        null,
                        true);

        when(aulaService.registrarAula(any()))
                .thenReturn(response);

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "Laboratorio 1",
                  "tipo": "LABORATORIO",
                  "capacidad": null
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacidad")
                        .doesNotExist());
    }

    @Test
    void registrarAula_debeRetornarBadRequestSinSede()
            throws Exception {

        String json = """
                {
                  "nombre": "Aula 101",
                  "tipo": "AULA",
                  "capacidad": 30
                }
                """;

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarAula_debeRetornarBadRequestConNombreVacio()
            throws Exception {

        UUID sedeId = UUID.randomUUID();

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "   ",
                  "tipo": "AULA",
                  "capacidad": 30
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarAula_debeRetornarBadRequestSinTipo()
            throws Exception {

        UUID sedeId = UUID.randomUUID();

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "Aula 101",
                  "capacidad": 30
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarAula_debeRetornarOk()
            throws Exception {

        UUID id = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        AulaResponse response =
                crearResponse(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        response.setId(id);

        when(aulaService.consultarAula(id))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/aulas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.sedeId")
                        .value(sedeId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Aula 101"));
    }

    @Test
    void listarPorSede_debeRetornarOk()
            throws Exception {

        UUID sedeId = UUID.randomUUID();

        AulaResponse aula1 =
                crearResponse(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        AulaResponse aula2 =
                crearResponse(
                        sedeId,
                        "Laboratorio 1",
                        TipoAulaRequerida.LABORATORIO,
                        null,
                        true);

        when(aulaService.listarPorSede(sedeId))
                .thenReturn(
                        List.of(
                                aula1,
                                aula2));

        mockMvc.perform(
                        get("/api/v1/aulas")
                                .param(
                                        "sedeId",
                                        sedeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].nombre")
                        .value("Aula 101"))
                .andExpect(jsonPath("$[1].nombre")
                        .value("Laboratorio 1"));
    }

    @Test
    void listarPorSede_debeRetornarBadRequestSinSedeId()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/aulas"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarAula_debeRetornarOk()
            throws Exception {

        UUID id = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        AulaResponse response =
                crearResponse(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        40,
                        true);

        response.setId(id);

        when(aulaService.actualizarAula(
                eq(id),
                any()))
                .thenReturn(response);

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "Aula 101",
                  "tipo": "AULA",
                  "capacidad": 40,
                  "activa": true
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.capacidad")
                        .value(40))
                .andExpect(jsonPath("$.activa")
                        .value(true));
    }

    @Test
    void actualizarAula_debeRetornarBadRequestSinEstado()
            throws Exception {

        UUID id = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "Aula 101",
                  "tipo": "AULA",
                  "capacidad": 30
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarCambioReal_debeRetornarCreated()
            throws Exception {

        UUID aulaAnteriorId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        AulaResponse response =
                crearResponse(
                        sedeId,
                        "Aula 102",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        when(aulaService.registrarCambioReal(
                eq(aulaAnteriorId),
                any()))
                .thenReturn(response);

        String json = """
                {
                  "sedeId": "%s",
                  "nombre": "Aula 102",
                  "tipo": "AULA",
                  "capacidad": 30
                }
                """.formatted(sedeId);

        mockMvc.perform(
                        post(
                                "/api/v1/aulas/{id}/nueva-version",
                                aulaAnteriorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sedeId")
                        .value(sedeId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Aula 102"))
                .andExpect(jsonPath("$.tipo")
                        .value("AULA"))
                .andExpect(jsonPath("$.capacidad")
                        .value(30))
                .andExpect(jsonPath("$.activa")
                        .value(true));
    }

    @Test
    void eliminarAula_debeRetornarConflictCuandoNoSePuedeGarantizarAusenciaDeUsoHistorico()
            throws Exception {

        UUID aulaId =
                UUID.randomUUID();

        doThrow(
                new CambioAulaInvalidoException(
                        "No se puede eliminar físicamente el aula porque el sistema no puede garantizar que nunca haya sido utilizada"))
                .when(aulaService)
                .eliminarAula(aulaId);

        mockMvc.perform(
                        delete(
                                "/api/v1/aulas/{id}",
                                aulaId))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "No se puede eliminar físicamente el aula porque el sistema no puede garantizar que nunca haya sido utilizada"));
    }

    private AulaResponse crearResponse(
            UUID sedeId,
            String nombre,
            TipoAulaRequerida tipo,
            Integer capacidad,
            boolean activa) {

        AulaResponse response =
                new AulaResponse();

        response.setId(UUID.randomUUID());
        response.setSedeId(sedeId);
        response.setSedeNombre("SEDE PRINCIPAL");
        response.setNombre(nombre);
        response.setTipo(tipo);
        response.setCapacidad(capacidad);
        response.setActiva(activa);

        return response;
    }
}

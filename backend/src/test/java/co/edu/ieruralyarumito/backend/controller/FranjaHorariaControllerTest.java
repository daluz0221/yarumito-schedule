package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.service.FranjaHorariaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FranjaHorariaControllerTest {

    @Mock
    private FranjaHorariaService franjaHorariaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        FranjaHorariaController controller =
                new FranjaHorariaController(
                        franjaHorariaService);

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void registrarFranjaHoraria_debeRetornarCreated()
            throws Exception {

        UUID turnoId = UUID.randomUUID();

        FranjaHorariaResponse response =
                crearResponse(turnoId);

        when(franjaHorariaService
                .registrarFranjaHoraria(any()))
                .thenReturn(response);

        String json = """
                {
                  "turnoId": "%s",
                  "numero": 1,
                  "horaInicio": "06:30:00",
                  "horaFin": "07:25:00",
                  "esDescanso": false,
                  "horasAcademicasEquivalentes": 1.00,
                  "orden": 1
                }
                """.formatted(turnoId);

        mockMvc.perform(
                        post("/api/v1/franjas-horarias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.turnoId")
                        .value(turnoId.toString()))
                .andExpect(jsonPath("$.numero")
                        .value(1))
                .andExpect(jsonPath("$.esDescanso")
                        .value(false))
                .andExpect(jsonPath("$.orden")
                        .value(1));
    }

    @Test
    void registrarFranjaHoraria_debeRetornarBadRequestConTurnoNulo()
            throws Exception {

        String json = """
                {
                  "numero": 1,
                  "horaInicio": "06:30:00",
                  "horaFin": "07:25:00",
                  "esDescanso": false,
                  "horasAcademicasEquivalentes": 1.00,
                  "orden": 1
                }
                """;

        mockMvc.perform(
                        post("/api/v1/franjas-horarias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarFranjaHoraria_debeRetornarBadRequestConOrdenCero()
            throws Exception {

        UUID turnoId = UUID.randomUUID();

        String json = """
                {
                  "turnoId": "%s",
                  "numero": 1,
                  "horaInicio": "06:30:00",
                  "horaFin": "07:25:00",
                  "esDescanso": false,
                  "horasAcademicasEquivalentes": 1.00,
                  "orden": 0
                }
                """.formatted(turnoId);

        mockMvc.perform(
                        post("/api/v1/franjas-horarias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarFranjaHoraria_debeRetornarOk()
            throws Exception {

        UUID id = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        FranjaHorariaResponse response =
                crearResponse(turnoId);

        response.setId(id);

        when(franjaHorariaService
                .consultarFranjaHoraria(id))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/franjas-horarias/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.turnoId")
                        .value(turnoId.toString()));
    }

    @Test
    void listarPorTurno_debeRetornarOk()
            throws Exception {

        UUID turnoId = UUID.randomUUID();

        FranjaHorariaResponse response =
                crearResponse(turnoId);

        when(franjaHorariaService
                .listarPorTurno(turnoId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/franjas-horarias")
                                .param(
                                        "turnoId",
                                        turnoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turnoId")
                        .value(turnoId.toString()))
                .andExpect(jsonPath("$[0].numero")
                        .value(1));
    }

    @Test
    void listarPorTurno_debeRetornarBadRequestSinTurnoId()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/franjas-horarias"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarFranjaHoraria_debeRetornarOk()
            throws Exception {

        UUID id = UUID.randomUUID();
        UUID turnoId = UUID.randomUUID();

        FranjaHorariaResponse response =
                crearResponse(turnoId);

        response.setId(id);

        when(franjaHorariaService
                .actualizarFranjaHoraria(
                        eq(id),
                        any()))
                .thenReturn(response);

        String json = """
                {
                  "turnoId": "%s",
                  "numero": 1,
                  "horaInicio": "06:30:00",
                  "horaFin": "07:25:00",
                  "esDescanso": false,
                  "horasAcademicasEquivalentes": 1.00,
                  "orden": 1
                }
                """.formatted(turnoId);

        mockMvc.perform(
                        put("/api/v1/franjas-horarias/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.turnoId")
                        .value(turnoId.toString()));
    }

    private FranjaHorariaResponse crearResponse(
            UUID turnoId) {

        FranjaHorariaResponse response =
                new FranjaHorariaResponse();

        response.setId(UUID.randomUUID());
        response.setTurnoId(turnoId);
        response.setTurnoNombre("Mañana");
        response.setNumero(1);
        response.setHoraInicio(
                LocalTime.of(6, 30));
        response.setHoraFin(
                LocalTime.of(7, 25));
        response.setEsDescanso(false);
        response.setHorasAcademicasEquivalentes(
                new BigDecimal("1.00"));
        response.setOrden(1);

        return response;
    }
}

package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTurnoRequest;
import co.edu.ieruralyarumito.backend.dto.TurnoResponse;
import co.edu.ieruralyarumito.backend.service.TurnoService;
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
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;


// Pruebas unitarias de los endpoints de TurnoController.
@ExtendWith(MockitoExtension.class)
public class TurnoControllerTest {

    @Mock
    private TurnoService turnoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {

        TurnoController turnoController =
                new TurnoController(turnoService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(turnoController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void registrarTurno_debeRetornarCreated() throws Exception {

        UUID turnoId = UUID.randomUUID();

        TurnoResponse response = crearResponse(turnoId);

        when(turnoService.registrarTurno(
                any(CrearTurnoRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "nombre": "Mañana",
                  "horaInicio": "06:30:00",
                  "horaFin": "12:30:00",
                  "duracionClaseMinutos": 55,
                  "clasesPorDia": 6,
                  "clasesAntesDeDescanso": 3,
                  "duracionDescansoMinutos": 30
                }
                """;

        mockMvc.perform(post("/api/v1/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(turnoId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Mañana"))
                .andExpect(jsonPath("$.duracionClaseMinutos")
                        .value(55));
    }

    @Test
    void registrarTurno_debeRetornarBadRequestConNombreVacio()
            throws Exception {

        String json = """
                {
                  "nombre": "   ",
                  "horaInicio": "06:30:00",
                  "horaFin": "12:30:00",
                  "duracionClaseMinutos": 55,
                  "clasesPorDia": 6,
                  "clasesAntesDeDescanso": 3,
                  "duracionDescansoMinutos": 30
                }
                """;

        mockMvc.perform(post("/api/v1/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarTurno_debeRetornarOk()
            throws Exception {

        UUID turnoId = UUID.randomUUID();

        when(turnoService.consultarTurno(turnoId))
                .thenReturn(crearResponse(turnoId));

        mockMvc.perform(
                        get("/api/v1/turnos/{id}", turnoId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(turnoId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Mañana"))
                .andExpect(jsonPath("$.horaInicio")
                        .value("06:30:00"))
                .andExpect(jsonPath("$.horaFin")
                        .value("12:30:00"));
    }

    @Test
    void listarTurnos_debeRetornarOk()
            throws Exception {

        TurnoResponse turno =
                crearResponse(UUID.randomUUID());

        PageImpl<TurnoResponse> pagina =
                new PageImpl<>(
                        List.of(turno),
                        PageRequest.of(0, 20),
                        1
                );

        when(turnoService.listarTurnos(any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/turnos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre")
                        .value("Mañana"))
                .andExpect(jsonPath("$.content[0].clasesPorDia")
                        .value(6));
    }

    @Test
    void actualizarTurno_debeRetornarOk()
            throws Exception {

        UUID turnoId = UUID.randomUUID();

        TurnoResponse response =
                crearResponse(turnoId);

        response.setNombre("Jornada Única");

        when(turnoService.actualizarTurno(
                eq(turnoId),
                any(ActualizarTurnoRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "nombre": "Jornada Única",
                  "horaInicio": "06:30:00",
                  "horaFin": "12:30:00",
                  "duracionClaseMinutos": 55,
                  "clasesPorDia": 6,
                  "clasesAntesDeDescanso": 3,
                  "duracionDescansoMinutos": 30
                }
                """;

        mockMvc.perform(
                        put("/api/v1/turnos/{id}", turnoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(turnoId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Jornada Única"));
    }

    @Test
    void registrarTurno_debeRetornarBadRequestCuandoServicioLanzaIllegalArgumentException()
            throws Exception {

        when(turnoService.registrarTurno(
                any(CrearTurnoRequest.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "La hora de inicio debe ser anterior a la hora de fin"));

        String json = """
            {
              "nombre": "Mañana",
              "horaInicio": "12:30:00",
              "horaFin": "06:30:00",
              "duracionClaseMinutos": 55,
              "clasesPorDia": 6,
              "clasesAntesDeDescanso": 3,
              "duracionDescansoMinutos": 30
            }
            """;

        mockMvc.perform(
                        post("/api/v1/turnos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value(
                                "La hora de inicio debe ser anterior a la hora de fin"));
    }


    private TurnoResponse crearResponse(UUID id) {

        TurnoResponse response =
                new TurnoResponse();

        response.setId(id);
        response.setNombre("Mañana");
        response.setHoraInicio(LocalTime.of(6, 30));
        response.setHoraFin(LocalTime.of(12, 30));
        response.setDuracionClaseMinutos(55);
        response.setClasesPorDia(6);
        response.setClasesAntesDeDescanso(3);
        response.setDuracionDescansoMinutos(30);

        return response;
    }
}

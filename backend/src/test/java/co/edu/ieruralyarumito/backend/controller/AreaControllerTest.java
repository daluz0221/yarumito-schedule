package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarAreaRequest;
import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAreaRequest;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.service.AreaService;
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

// Pruebas unitarias de los endpoints de AreaController.
@ExtendWith(MockitoExtension.class)
public class AreaControllerTest {

    @Mock
    private AreaService areaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {

        AreaController areaController =
                new AreaController(areaService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(areaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void consultarArea_debeRetornarOk() throws Exception {

        UUID areaId = UUID.randomUUID();

        AreaResponse response = new AreaResponse();
        response.setId(areaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT");
        response.setObligatoria(true);
        response.setSoloMedia(false);
        response.setActiva(true);

        when(areaService.consultarArea(areaId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/areas/{id}", areaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(areaId.toString()))
                .andExpect(jsonPath("$.nombre").value("Matemáticas"))
                .andExpect(jsonPath("$.codigo").value("MAT"))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    void listarAreas_debeRetornarOk() throws Exception {

        AreaResponse area = new AreaResponse();
        area.setNombre("Matemáticas");
        area.setCodigo("MAT");
        area.setActiva(true);

        PageImpl<AreaResponse> pagina =
                new PageImpl<>(
                        List.of(area),
                        PageRequest.of(0, 20),
                        1
                );

        when(areaService.listarAreas(
                isNull(),
                isNull(),
                any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre")
                        .value("Matemáticas"))
                .andExpect(jsonPath("$.content[0].codigo")
                        .value("MAT"));
    }

    @Test
    void registrarArea_debeRetornarCreated() throws Exception {

        UUID areaId = UUID.randomUUID();

        AreaResponse response = new AreaResponse();
        response.setId(areaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT");
        response.setObligatoria(true);
        response.setSoloMedia(false);
        response.setActiva(true);

        when(areaService.registrarArea(
                any(CrearAreaRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "nombre": "Matemáticas",
                  "codigo": "MAT",
                  "obligatoria": true,
                  "soloMedia": false,
                  "activa": true
                }
                """;

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(areaId.toString()))
                .andExpect(jsonPath("$.nombre").value("Matemáticas"))
                .andExpect(jsonPath("$.codigo").value("MAT"));
    }

    @Test
    void actualizarArea_debeRetornarOk() throws Exception {

        UUID areaId = UUID.randomUUID();

        AreaResponse response = new AreaResponse();
        response.setId(areaId);
        response.setNombre("Ciencias Naturales");
        response.setCodigo("CN");
        response.setObligatoria(true);
        response.setSoloMedia(false);
        response.setActiva(true);

        when(areaService.actualizarArea(
                eq(areaId),
                any(ActualizarAreaRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "nombre": "Ciencias Naturales",
                  "codigo": "CN",
                  "obligatoria": true,
                  "soloMedia": false,
                  "activa": true
                }
                """;

        mockMvc.perform(put("/api/v1/areas/{id}", areaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(areaId.toString()))
                .andExpect(jsonPath("$.nombre")
                        .value("Ciencias Naturales"))
                .andExpect(jsonPath("$.codigo").value("CN"));
    }

    @Test
    void cambiarEstado_debeRetornarOk() throws Exception {

        UUID areaId = UUID.randomUUID();

        AreaResponse response = new AreaResponse();
        response.setId(areaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT");
        response.setActiva(false);

        when(areaService.cambiarEstado(areaId, false))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/areas/{id}/estado", areaId)
                                .param("activa", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(areaId.toString()))
                .andExpect(jsonPath("$.activa")
                        .value(false));
    }

    @Test
    void registrarArea_debeRetornarBadRequestConDatosInvalidos()
            throws Exception {

        String json = """
                {
                  "nombre": "Matemáticas"
                }
                """;

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(areaService);
    }

    @Test
    void consultarArea_debeRetornarNotFound() throws Exception {

        UUID areaId = UUID.randomUUID();

        when(areaService.consultarArea(areaId))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "El área no existe")
                );

        mockMvc.perform(get("/api/v1/areas/{id}", areaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje")
                        .value("El área no existe"));
    }
}

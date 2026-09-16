package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.service.DocenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import co.edu.ieruralyarumito.backend.dto.DocenteResponse;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.domain.PageRequest;
import co.edu.ieruralyarumito.backend.dto.CrearDocenteRequest;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import co.edu.ieruralyarumito.backend.dto.ActualizarDocenteRequest;
import static org.mockito.Mockito.verifyNoInteractions;

// Pruebas unitarias de los endpoints de DocenteController.
@ExtendWith(MockitoExtension.class)
public class DocenteControllerTest {

    // Simula la lógica de negocio utilizada por el Controller.
    @Mock
    private DocenteService docenteService;

    // Permite simular peticiones HTTP contra el Controller.
    private MockMvc mockMvc;

    // Prepara el Controller antes de ejecutar cada prueba.
    @BeforeEach
    void configurar() {

        DocenteController docenteController =
                new DocenteController(docenteService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(docenteController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }
    // Verifica que GET /api/v1/docentes/{id} retorne un docente existente.
    @Test
    void consultarDocente_debeRetornarOk() throws Exception {

        // Identificador del docente solicitado.
        UUID docenteId = UUID.randomUUID();

        // Respuesta simulada del Service.
        DocenteResponse response = new DocenteResponse();
        response.setId(docenteId);
        response.setNombres("Ana");
        response.setApellidos("Gómez");

        // Simula la respuesta del Service.
        when(docenteService.consultarDocente(docenteId))
                .thenReturn(response);

        // Ejecuta la petición y verifica la respuesta HTTP.
        mockMvc.perform(get("/api/v1/docentes/{id}", docenteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docenteId.toString()))
                .andExpect(jsonPath("$.nombres").value("Ana"))
                .andExpect(jsonPath("$.apellidos").value("Gómez"));
    }
    // Verifica que GET /api/v1/docentes retorne correctamente la lista de docentes.
    @Test
    void listarDocentes_debeRetornarOk() throws Exception {

        // Respuesta simulada del Service.
        DocenteResponse docente = new DocenteResponse();
        docente.setNombres("Ana");
        docente.setApellidos("Gómez");

        PageImpl<DocenteResponse> pagina =
                new PageImpl<>(
                        List.of(docente),
                        PageRequest.of(0, 20),
                        1
                );

        // Simula el listado sin filtros.
        when(docenteService.listarDocentes(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()))
                .thenReturn(pagina);

        // Ejecuta la petición y verifica la respuesta.
        mockMvc.perform(get("/api/v1/docentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombres").value("Ana"))
                .andExpect(jsonPath("$.content[0].apellidos").value("Gómez"));
    }
    // Verifica que POST /api/v1/docentes registre correctamente un docente.
    @Test
    void registrarDocente_debeRetornarCreated() throws Exception {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        // Respuesta simulada del Service.
        DocenteResponse response = new DocenteResponse();
        response.setId(docenteId);
        response.setNombres("Carlos");
        response.setApellidos("Bermúdez");
        response.setNumeroDocumento("999888777");

        when(docenteService.registrarDocente(
                any(CrearDocenteRequest.class)))
                .thenReturn(response);

        // Datos válidos enviados en la petición.
        String json = """
            {
              "nombres": "Carlos",
              "apellidos": "Bermúdez",
              "tipoDocumento": "CC",
              "numeroDocumento": "999888777",
              "tipoVinculacion": "PLANTA",
              "areaNombramientoId": "%s",
              "estado": "ACTIVO"
            }
            """.formatted(areaId);

        mockMvc.perform(post("/api/v1/docentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(docenteId.toString()))
                .andExpect(jsonPath("$.nombres").value("Carlos"))
                .andExpect(jsonPath("$.apellidos").value("Bermúdez"))
                .andExpect(jsonPath("$.numeroDocumento").value("999888777"));
    }
    // Verifica que PUT /api/v1/docentes/{id} actualice correctamente un docente.
    @Test
    void actualizarDocente_debeRetornarOk() throws Exception {

        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        // Respuesta simulada del Service.
        DocenteResponse response = new DocenteResponse();
        response.setId(docenteId);
        response.setNombres("Carlos");
        response.setApellidos("Bermúdez");
        response.setNumeroDocumento("555666777");

        when(docenteService.actualizarDocente(
                org.mockito.ArgumentMatchers.eq(docenteId),
                any(ActualizarDocenteRequest.class)))
                .thenReturn(response);

        // Datos válidos enviados para actualizar.
        String json = """
            {
              "nombres": "Carlos",
              "apellidos": "Bermúdez",
              "tipoDocumento": "CC",
              "numeroDocumento": "555666777",
              "tipoVinculacion": "PLANTA",
              "areaNombramientoId": "%s"
            }
            """.formatted(areaId);

        mockMvc.perform(put("/api/v1/docentes/{id}", docenteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docenteId.toString()))
                .andExpect(jsonPath("$.nombres").value("Carlos"))
                .andExpect(jsonPath("$.apellidos").value("Bermúdez"))
                .andExpect(jsonPath("$.numeroDocumento").value("555666777"));
    }
    // Verifica que POST /api/v1/docentes rechace datos obligatorios faltantes.
    @Test
    void registrarDocente_debeRetornarBadRequestConDatosInvalidos() throws Exception {

        // Petición incompleta: solo contiene el nombre.
        String json = """
            {
              "nombres": "Carlos"
            }
            """;

        // Ejecuta la petición inválida.
        mockMvc.perform(post("/api/v1/docentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // La validación debe detener la petición antes de llegar al Service.
        verifyNoInteractions(docenteService);
    }
}
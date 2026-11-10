package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.CrearIdoneidadRequest;
import co.edu.ieruralyarumito.backend.dto.IdoneidadResponse;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import co.edu.ieruralyarumito.backend.service.IdoneidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;
import co.edu.ieruralyarumito.backend.dto.ActualizarIdoneidadRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import co.edu.ieruralyarumito.backend.dto.FinalizarVigenciaIdoneidadRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;


// Pruebas unitarias de los endpoints de IdoneidadController.
@ExtendWith(MockitoExtension.class)
public class IdoneidadControllerTest {

    // Simula la lógica de negocio utilizada por el Controller.
    @Mock
    private IdoneidadService idoneidadService;

    // Permite simular peticiones HTTP contra el Controller.
    private MockMvc mockMvc;

    // Prepara el Controller antes de ejecutar cada prueba.
    @BeforeEach
    void configurar() {

        IdoneidadController idoneidadController =
                new IdoneidadController(idoneidadService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(idoneidadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Verifica que POST /api/v1/idoneidades registre correctamente una idoneidad.
    @Test
    void registrarIdoneidad_debeRetornarCreated() throws Exception {

        UUID idoneidadId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        // Respuesta simulada del Service.
        IdoneidadResponse response = new IdoneidadResponse();
        response.setId(idoneidadId);
        response.setDocenteId(docenteId);
        response.setAreaId(areaId);
        response.setTipo(TipoIdoneidad.PRINCIPAL);
        response.setVigenteDesde(LocalDate.of(2026, 9, 17));

        when(idoneidadService.registrarIdoneidad(
                any(CrearIdoneidadRequest.class)))
                .thenReturn(response);

        // Datos válidos enviados en la petición.
        String json = """
                {
                  "docenteId": "%s",
                  "areaId": "%s",
                  "tipo": "PRINCIPAL",
                  "vigenteDesde": "2026-09-17"
                }
                """.formatted(docenteId, areaId);

        mockMvc.perform(post("/api/v1/idoneidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(idoneidadId.toString()))
                .andExpect(jsonPath("$.docenteId").value(docenteId.toString()))
                .andExpect(jsonPath("$.areaId").value(areaId.toString()))
                .andExpect(jsonPath("$.tipo").value("PRINCIPAL"))
                .andExpect(jsonPath("$.vigenteDesde").value("2026-09-17"));
    }

    // Verifica que GET /api/v1/idoneidades/{id} retorne una idoneidad existente.
    @Test
    void consultarIdoneidad_debeRetornarOk() throws Exception {

        UUID idoneidadId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        // Respuesta simulada del Service.
        IdoneidadResponse response = new IdoneidadResponse();
        response.setId(idoneidadId);
        response.setDocenteId(docenteId);
        response.setAreaId(areaId);
        response.setTipo(TipoIdoneidad.PRINCIPAL);
        response.setVigenteDesde(LocalDate.of(2026, 9, 17));

        when(idoneidadService.consultarIdoneidad(idoneidadId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/idoneidades/{id}", idoneidadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idoneidadId.toString()))
                .andExpect(jsonPath("$.docenteId").value(docenteId.toString()))
                .andExpect(jsonPath("$.areaId").value(areaId.toString()))
                .andExpect(jsonPath("$.tipo").value("PRINCIPAL"))
                .andExpect(jsonPath("$.vigenteDesde").value("2026-09-17"));
    }

    // Verifica que GET /api/v1/idoneidades retorne la lista de idoneidades.
    @Test
    void listarIdoneidades_debeRetornarOk() throws Exception {

        UUID idoneidadId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        // Respuesta simulada del Service.
        IdoneidadResponse response = new IdoneidadResponse();
        response.setId(idoneidadId);
        response.setDocenteId(docenteId);
        response.setAreaId(areaId);
        response.setTipo(TipoIdoneidad.PRINCIPAL);
        response.setVigenteDesde(LocalDate.of(2026, 9, 17));

        when(idoneidadService.listarIdoneidades())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/idoneidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idoneidadId.toString()))
                .andExpect(jsonPath("$[0].docenteId").value(docenteId.toString()))
                .andExpect(jsonPath("$[0].areaId").value(areaId.toString()))
                .andExpect(jsonPath("$[0].tipo").value("PRINCIPAL"))
                .andExpect(jsonPath("$[0].vigenteDesde").value("2026-09-17"));
    }

    // Verifica que PUT /api/v1/idoneidades/{id}
// acepte todos los datos editables.
    @Test
    void actualizarIdoneidad_debeRetornarOk() throws Exception {

        UUID idoneidadId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID asignaturaId = UUID.randomUUID();
        UUID tituloSoporteId = UUID.randomUUID();

        // Respuesta simulada del Service.
        IdoneidadResponse response = new IdoneidadResponse();
        response.setId(idoneidadId);
        response.setDocenteId(docenteId);
        response.setAreaId(areaId);
        response.setAsignaturaId(asignaturaId);
        response.setTipo(TipoIdoneidad.EXCEPCIONAL);
        response.setTituloSoporteId(tituloSoporteId);
        response.setJustificacion(
                "Actualización del soporte académico"
        );
        response.setVigenteDesde(LocalDate.of(2026, 9, 18));
        response.setVigenteHasta(LocalDate.of(2026, 12, 15));

        when(idoneidadService.actualizarIdoneidad(
                org.mockito.ArgumentMatchers.eq(idoneidadId),
                any(ActualizarIdoneidadRequest.class)))
                .thenReturn(response);

        // Envía la representación completa de los campos editables.
        String json = """
            {
              "areaId": "%s",
              "asignaturaId": "%s",
              "tipo": "EXCEPCIONAL",
              "tituloSoporteId": "%s",
              "justificacion": "Actualización del soporte académico",
              "vigenteDesde": "2026-09-18",
              "vigenteHasta": "2026-12-15"
            }
            """.formatted(areaId, asignaturaId, tituloSoporteId);

        mockMvc.perform(
                        put("/api/v1/idoneidades/{id}", idoneidadId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(idoneidadId.toString()))
                .andExpect(jsonPath("$.docenteId")
                        .value(docenteId.toString()))
                .andExpect(jsonPath("$.areaId")
                        .value(areaId.toString()))
                .andExpect(jsonPath("$.asignaturaId")
                        .value(asignaturaId.toString()))
                .andExpect(jsonPath("$.tipo")
                        .value("EXCEPCIONAL"))
                .andExpect(jsonPath("$.tituloSoporteId")
                        .value(tituloSoporteId.toString()))
                .andExpect(jsonPath("$.justificacion")
                        .value("Actualización del soporte académico"))
                .andExpect(jsonPath("$.vigenteDesde")
                        .value("2026-09-18"))
                .andExpect(jsonPath("$.vigenteHasta")
                        .value("2026-12-15"));
    }

    // Verifica que PATCH /api/v1/idoneidades/{id}/vigencia finalice la vigencia.
    @Test
    void finalizarVigencia_debeRetornarOk() throws Exception {

        UUID idoneidadId = UUID.randomUUID();
        LocalDate vigenteHasta = LocalDate.of(2026, 9, 18);

        // Respuesta simulada del Service.
        IdoneidadResponse response = new IdoneidadResponse();
        response.setId(idoneidadId);
        response.setVigenteHasta(vigenteHasta);

        when(idoneidadService.finalizarVigencia(
                org.mockito.ArgumentMatchers.eq(idoneidadId),
                any(FinalizarVigenciaIdoneidadRequest.class)))
                .thenReturn(response);

        // Fecha enviada para finalizar la vigencia.
        String json = """
        {
          "vigenteHasta": "2026-09-18"
        }
        """;

        mockMvc.perform(patch("/api/v1/idoneidades/{id}/vigencia", idoneidadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idoneidadId.toString()))
                .andExpect(jsonPath("$.vigenteHasta").value("2026-09-18"));
    }

    // Verifica que GET /api/v1/idoneidades/docente/{docenteId}
// retorne las idoneidades asociadas al docente.
    @Test
    void listarIdoneidadesPorDocente_debeRetornarOk() throws Exception {

        UUID idoneidadId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        IdoneidadResponse response = new IdoneidadResponse();
        response.setId(idoneidadId);
        response.setDocenteId(docenteId);
        response.setAreaId(areaId);
        response.setTipo(TipoIdoneidad.PRINCIPAL);
        response.setVigenteDesde(LocalDate.of(2026, 9, 18));

        when(idoneidadService.listarIdoneidadesPorDocente(docenteId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/idoneidades/docente/{docenteId}", docenteId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idoneidadId.toString()))
                .andExpect(jsonPath("$[0].docenteId").value(docenteId.toString()))
                .andExpect(jsonPath("$[0].areaId").value(areaId.toString()))
                .andExpect(jsonPath("$[0].tipo").value("PRINCIPAL"))
                .andExpect(jsonPath("$[0].vigenteDesde").value("2026-09-18"));
    }

    // Verifica que GET /api/v1/idoneidades/{id}
// retorne 404 cuando la idoneidad no existe.
    @Test
    void consultarIdoneidad_debeRetornarNotFound() throws Exception {

        UUID idoneidadId = UUID.randomUUID();

        when(idoneidadService.consultarIdoneidad(idoneidadId))
                .thenThrow(new RecursoNoEncontradoException(
                        "La idoneidad no existe"));

        mockMvc.perform(
                        get("/api/v1/idoneidades/{id}", idoneidadId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje")
                        .value("La idoneidad no existe"));
    }
}

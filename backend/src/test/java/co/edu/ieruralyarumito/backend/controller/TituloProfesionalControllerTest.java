package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;
import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import co.edu.ieruralyarumito.backend.exception.GlobalExceptionHandler;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.service.TituloProfesionalService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas unitarias de los endpoints de TituloProfesionalController.
@ExtendWith(MockitoExtension.class)
public class TituloProfesionalControllerTest {

    // Simula la lógica de negocio utilizada por el Controller.
    @Mock
    private TituloProfesionalService tituloProfesionalService;

    // Permite simular peticiones HTTP contra el Controller.
    private MockMvc mockMvc;

    // Prepara el Controller antes de ejecutar cada prueba.
    @BeforeEach
    void configurar() {

        TituloProfesionalController tituloProfesionalController =
                new TituloProfesionalController(tituloProfesionalService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(tituloProfesionalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Verifica que POST registre correctamente un título profesional.
    @Test
    void registrarTituloProfesional_debeRetornarCreated() throws Exception {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(tituloId);
        response.setDocenteId(docenteId);
        response.setNivel(NivelTituloProfesional.LICENCIATURA);
        response.setNombreTitulo("Licenciatura en Matemáticas");
        response.setInstitucion("Universidad de Antioquia");
        response.setAnioGraduacion(2020);
        response.setArchivoSoporte("titulo-matematicas.pdf");

        when(tituloProfesionalService.registrarTituloProfesional(
                any(CrearTituloProfesionalRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "docenteId": "%s",
                  "nivel": "LICENCIATURA",
                  "nombreTitulo": "Licenciatura en Matemáticas",
                  "institucion": "Universidad de Antioquia",
                  "anioGraduacion": 2020,
                  "archivoSoporte": "titulo-matematicas.pdf"
                }
                """.formatted(docenteId);

        mockMvc.perform(
                        post("/api/v1/titulos-profesionales")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(tituloId.toString()))
                .andExpect(jsonPath("$.docenteId")
                        .value(docenteId.toString()))
                .andExpect(jsonPath("$.nivel")
                        .value("LICENCIATURA"))
                .andExpect(jsonPath("$.nombreTitulo")
                        .value("Licenciatura en Matemáticas"))
                .andExpect(jsonPath("$.institucion")
                        .value("Universidad de Antioquia"))
                .andExpect(jsonPath("$.anioGraduacion")
                        .value(2020))
                .andExpect(jsonPath("$.archivoSoporte")
                        .value("titulo-matematicas.pdf"));
    }

    // Verifica Bean Validation al registrar un título inválido.
    @Test
    void registrarTituloProfesional_debeRetornarBadRequestCuandoEsInvalido()
            throws Exception {

        UUID docenteId = UUID.randomUUID();

        // nombreTitulo vacío viola @NotBlank.
        String json = """
                {
                  "docenteId": "%s",
                  "nivel": "PROFESIONAL",
                  "nombreTitulo": "   "
                }
                """.formatted(docenteId);

        mockMvc.perform(
                        post("/api/v1/titulos-profesionales")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        // Una petición inválida no debe llegar al Service.
        verify(
                tituloProfesionalService,
                never()
        ).registrarTituloProfesional(
                any(CrearTituloProfesionalRequest.class)
        );
    }

    // Verifica que GET retorne un título profesional existente.
    @Test
    void consultarTituloProfesional_debeRetornarOk() throws Exception {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(tituloId);
        response.setDocenteId(docenteId);
        response.setNivel(NivelTituloProfesional.MAESTRIA);
        response.setNombreTitulo("Maestría en Educación");

        when(tituloProfesionalService
                .consultarTituloProfesional(tituloId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/titulos-profesionales/{id}",
                                tituloId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(tituloId.toString()))
                .andExpect(jsonPath("$.docenteId")
                        .value(docenteId.toString()))
                .andExpect(jsonPath("$.nivel")
                        .value("MAESTRIA"))
                .andExpect(jsonPath("$.nombreTitulo")
                        .value("Maestría en Educación"));
    }

    // Verifica que GET retorne 404 cuando el título no existe.
    @Test
    void consultarTituloProfesional_debeRetornarNotFound()
            throws Exception {

        UUID tituloId = UUID.randomUUID();

        when(tituloProfesionalService
                .consultarTituloProfesional(tituloId))
                .thenThrow(
                        new RecursoNoEncontradoException(
                                "El título profesional no existe"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/titulos-profesionales/{id}",
                                tituloId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje")
                        .value("El título profesional no existe"));
    }

    // Verifica que se listen los títulos asociados a un docente.
    @Test
    void listarTitulosPorDocente_debeRetornarOk() throws Exception {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(tituloId);
        response.setDocenteId(docenteId);
        response.setNivel(
                NivelTituloProfesional.ESPECIALIZACION
        );
        response.setNombreTitulo(
                "Especialización en Pedagogía"
        );

        when(tituloProfesionalService
                .listarTitulosPorDocente(docenteId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get(
                                "/api/v1/docentes/{docenteId}/titulos-profesionales",
                                docenteId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(tituloId.toString()))
                .andExpect(jsonPath("$[0].docenteId")
                        .value(docenteId.toString()))
                .andExpect(jsonPath("$[0].nivel")
                        .value("ESPECIALIZACION"))
                .andExpect(jsonPath("$[0].nombreTitulo")
                        .value("Especialización en Pedagogía"));
    }

    // Verifica que PUT actualice correctamente los campos permitidos.
    @Test
    void actualizarTituloProfesional_debeRetornarOk()
            throws Exception {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        TituloProfesionalResponse response =
                new TituloProfesionalResponse();

        response.setId(tituloId);
        response.setDocenteId(docenteId);
        response.setNivel(NivelTituloProfesional.DOCTORADO);
        response.setNombreTitulo(
                "Doctorado en Educación"
        );
        response.setInstitucion(
                "Universidad Nacional"
        );
        response.setAnioGraduacion(2026);
        response.setArchivoSoporte(
                "doctorado.pdf"
        );

        when(tituloProfesionalService
                .actualizarTituloProfesional(
                        eq(tituloId),
                        any(ActualizarTituloProfesionalRequest.class)
                ))
                .thenReturn(response);

        String json = """
                {
                  "nivel": "DOCTORADO",
                  "nombreTitulo": "Doctorado en Educación",
                  "institucion": "Universidad Nacional",
                  "anioGraduacion": 2026,
                  "archivoSoporte": "doctorado.pdf"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/v1/titulos-profesionales/{id}",
                                tituloId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(tituloId.toString()))
                .andExpect(jsonPath("$.docenteId")
                        .value(docenteId.toString()))
                .andExpect(jsonPath("$.nivel")
                        .value("DOCTORADO"))
                .andExpect(jsonPath("$.nombreTitulo")
                        .value("Doctorado en Educación"))
                .andExpect(jsonPath("$.institucion")
                        .value("Universidad Nacional"))
                .andExpect(jsonPath("$.anioGraduacion")
                        .value(2026))
                .andExpect(jsonPath("$.archivoSoporte")
                        .value("doctorado.pdf"));
    }

    // Verifica Bean Validation al actualizar un título inválido.
    @Test
    void actualizarTituloProfesional_debeRetornarBadRequestCuandoEsInvalido()
            throws Exception {

        UUID tituloId = UUID.randomUUID();

        // nivel es obligatorio y nombreTitulo no puede estar vacío.
        String json = """
                {
                  "nombreTitulo": ""
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/v1/titulos-profesionales/{id}",
                                tituloId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        // La petición inválida no debe llegar al Service.
        verify(
                tituloProfesionalService,
                never()
        ).actualizarTituloProfesional(
                eq(tituloId),
                any(ActualizarTituloProfesionalRequest.class)
        );
    }
}

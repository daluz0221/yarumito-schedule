package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.CrearTituloProfesionalRequest;
import co.edu.ieruralyarumito.backend.dto.TituloProfesionalResponse;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

// Pruebas unitarias de la lógica de negocio de TituloProfesionalService.
@ExtendWith(MockitoExtension.class)
public class TituloProfesionalServiceTest {

    @Mock
    private TituloProfesionalRepository tituloProfesionalRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @InjectMocks
    private TituloProfesionalService tituloProfesionalService;

    // Verifica que se pueda registrar correctamente un título profesional.
    @Test
    void registrarTituloProfesional_debeRegistrarCorrectamente() {

        UUID docenteId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();

        CrearTituloProfesionalRequest request =
                new CrearTituloProfesionalRequest();

        request.setDocenteId(docenteId);
        request.setNivel(NivelTituloProfesional.LICENCIATURA);
        request.setNombreTitulo("Licenciatura en Matemáticas");
        request.setInstitucion("Universidad de Antioquia");
        request.setAnioGraduacion(2020);
        request.setArchivoSoporte("titulo-matematicas.pdf");

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(tituloProfesionalRepository.save(any(TituloProfesional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TituloProfesionalResponse response =
                tituloProfesionalService.registrarTituloProfesional(request);

        assertEquals(docenteId, response.getDocenteId());
        assertEquals(
                NivelTituloProfesional.LICENCIATURA,
                response.getNivel()
        );
        assertEquals(
                "Licenciatura en Matemáticas",
                response.getNombreTitulo()
        );
        assertEquals(
                "Universidad de Antioquia",
                response.getInstitucion()
        );
        assertEquals(2020, response.getAnioGraduacion());
        assertEquals(
                "titulo-matematicas.pdf",
                response.getArchivoSoporte()
        );
    }

    // Verifica que no se pueda registrar un título para un docente inexistente.
    @Test
    void registrarTituloProfesional_debeRechazarDocenteInexistente() {

        UUID docenteId = UUID.randomUUID();

        CrearTituloProfesionalRequest request =
                new CrearTituloProfesionalRequest();

        request.setDocenteId(docenteId);
        request.setNivel(NivelTituloProfesional.PROFESIONAL);
        request.setNombreTitulo("Ingeniería de Sistemas");

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> tituloProfesionalService
                        .registrarTituloProfesional(request)
        );
    }

    // Verifica que se pueda consultar un título profesional existente.
    @Test
    void consultarTituloProfesional_debeRetornarTituloExistente() {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();

        TituloProfesional tituloProfesional =
                spy(new TituloProfesional());

        doReturn(tituloId).when(tituloProfesional).getId();

        tituloProfesional.setDocente(docente);
        tituloProfesional.setNivel(
                NivelTituloProfesional.MAESTRIA
        );
        tituloProfesional.setNombreTitulo(
                "Maestría en Educación"
        );

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.of(tituloProfesional));

        TituloProfesionalResponse response =
                tituloProfesionalService
                        .consultarTituloProfesional(tituloId);

        assertEquals(tituloId, response.getId());
        assertEquals(docenteId, response.getDocenteId());
        assertEquals(
                NivelTituloProfesional.MAESTRIA,
                response.getNivel()
        );
        assertEquals(
                "Maestría en Educación",
                response.getNombreTitulo()
        );
    }

    // Verifica que consultar un título inexistente informe recurso no encontrado.
    @Test
    void consultarTituloProfesional_debeRechazarIdInexistente() {

        UUID tituloId = UUID.randomUUID();

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> tituloProfesionalService
                        .consultarTituloProfesional(tituloId)
        );
    }

    // Verifica que se listen los títulos profesionales de un docente existente.
    @Test
    void listarTitulosPorDocente_debeRetornarTitulosDelDocente() {

        UUID docenteId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();

        TituloProfesional tituloProfesional =
                new TituloProfesional();

        tituloProfesional.setDocente(docente);
        tituloProfesional.setNivel(
                NivelTituloProfesional.ESPECIALIZACION
        );
        tituloProfesional.setNombreTitulo(
                "Especialización en Pedagogía"
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(tituloProfesionalRepository
                .findByDocente_Id(docenteId))
                .thenReturn(List.of(tituloProfesional));

        List<TituloProfesionalResponse> response =
                tituloProfesionalService
                        .listarTitulosPorDocente(docenteId);

        assertEquals(1, response.size());
        assertEquals(
                docenteId,
                response.get(0).getDocenteId()
        );
        assertEquals(
                "Especialización en Pedagogía",
                response.get(0).getNombreTitulo()
        );
    }

    // Verifica que no se listen títulos para un docente inexistente.
    @Test
    void listarTitulosPorDocente_debeRechazarDocenteInexistente() {

        UUID docenteId = UUID.randomUUID();

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> tituloProfesionalService
                        .listarTitulosPorDocente(docenteId)
        );
    }

    // Verifica que un título profesional pueda actualizarse correctamente.
    @Test
    void actualizarTituloProfesional_debeActualizarCorrectamente() {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();

        TituloProfesional tituloProfesional =
                spy(new TituloProfesional());

        doReturn(tituloId).when(tituloProfesional).getId();

        tituloProfesional.setDocente(docente);
        tituloProfesional.setNivel(
                NivelTituloProfesional.LICENCIATURA
        );
        tituloProfesional.setNombreTitulo(
                "Licenciatura anterior"
        );

        ActualizarTituloProfesionalRequest request =
                new ActualizarTituloProfesionalRequest();

        request.setNivel(NivelTituloProfesional.MAESTRIA);
        request.setNombreTitulo(
                "Maestría en Educación"
        );
        request.setInstitucion(
                "Universidad de Antioquia"
        );
        request.setAnioGraduacion(2025);
        request.setArchivoSoporte(
                "maestria.pdf"
        );

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.of(tituloProfesional));

        when(tituloProfesionalRepository.save(
                any(TituloProfesional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TituloProfesionalResponse response =
                tituloProfesionalService
                        .actualizarTituloProfesional(
                                tituloId,
                                request
                        );

        assertEquals(
                NivelTituloProfesional.MAESTRIA,
                response.getNivel()
        );
        assertEquals(
                "Maestría en Educación",
                response.getNombreTitulo()
        );
        assertEquals(
                "Universidad de Antioquia",
                response.getInstitucion()
        );
        assertEquals(2025, response.getAnioGraduacion());
        assertEquals(
                "maestria.pdf",
                response.getArchivoSoporte()
        );
    }

    // Verifica que actualizar un título conserve el docente propietario.
    @Test
    void actualizarTituloProfesional_debeConservarDocentePropietario() {

        UUID tituloId = UUID.randomUUID();
        UUID docenteId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();

        TituloProfesional tituloProfesional =
                spy(new TituloProfesional());

        doReturn(tituloId).when(tituloProfesional).getId();

        tituloProfesional.setDocente(docente);
        tituloProfesional.setNivel(
                NivelTituloProfesional.PROFESIONAL
        );
        tituloProfesional.setNombreTitulo(
                "Título inicial"
        );

        ActualizarTituloProfesionalRequest request =
                new ActualizarTituloProfesionalRequest();

        request.setNivel(
                NivelTituloProfesional.ESPECIALIZACION
        );
        request.setNombreTitulo(
                "Especialización en Educación"
        );

        when(tituloProfesionalRepository.findById(tituloId))
                .thenReturn(Optional.of(tituloProfesional));

        when(tituloProfesionalRepository.save(
                any(TituloProfesional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TituloProfesionalResponse response =
                tituloProfesionalService
                        .actualizarTituloProfesional(
                                tituloId,
                                request
                        );

        assertSame(
                docente,
                tituloProfesional.getDocente()
        );

        assertEquals(
                docenteId,
                response.getDocenteId()
        );
    }

    // Verifica que los campos opcionales puedan permanecer nulos.
    @Test
    void registrarTituloProfesional_debePermitirCamposOpcionalesNulos() {

        UUID docenteId = UUID.randomUUID();

        Docente docente = spy(new Docente());
        doReturn(docenteId).when(docente).getId();

        CrearTituloProfesionalRequest request =
                new CrearTituloProfesionalRequest();

        request.setDocenteId(docenteId);
        request.setNivel(
                NivelTituloProfesional.NORMALISTA
        );
        request.setNombreTitulo(
                "Normalista Superior"
        );

        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        when(tituloProfesionalRepository.save(
                any(TituloProfesional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TituloProfesionalResponse response =
                tituloProfesionalService
                        .registrarTituloProfesional(request);

        assertEquals(
                docenteId,
                response.getDocenteId()
        );

        assertNull(response.getInstitucion());
        assertNull(response.getAnioGraduacion());
        assertNull(response.getArchivoSoporte());
    }
}

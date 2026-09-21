package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.ieruralyarumito.backend.dto.CrearDocenteRequest;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import java.util.Optional;
import java.util.UUID;
import co.edu.ieruralyarumito.backend.dto.DocenteResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import co.edu.ieruralyarumito.backend.dto.ActualizarDocenteRequest;

// Pruebas unitarias de la lógica de negocio de DocenteService.
@ExtendWith(MockitoExtension.class)
public class DocenteServiceTest {

    // Simula el acceso a datos de docentes.
    @Mock
    private DocenteRepository docenteRepository;

    // Simula el acceso a datos de áreas.
    @Mock
    private AreaRepository areaRepository;

    // Crea DocenteService e inyecta automáticamente los mocks anteriores.
    @InjectMocks
    private DocenteService docenteService;
    // Verifica que no se permita registrar un documento ya existente.
    @Test
    void registrarDocente_debeRechazarDocumentoDuplicado() {

        // Prepara una solicitud con un número de documento existente.
        CrearDocenteRequest request = new CrearDocenteRequest();
        request.setNumeroDocumento("123456789");

        // Simula que el documento ya existe en la base de datos.
        when(docenteRepository.existsByNumeroDocumento("123456789"))
                .thenReturn(true);

        // Verifica que el servicio rechace el registro.
        assertThrows(
                RecursoDuplicadoException.class,
                () -> docenteService.registrarDocente(request)
        );
    }
    // Verifica que no se permita registrar un docente con un área inexistente.
    @Test
    void registrarDocente_debeRechazarAreaInexistente() {

        // Identificador de un área que no existe.
        UUID areaId = UUID.randomUUID();

        CrearDocenteRequest request = new CrearDocenteRequest();
        request.setNumeroDocumento("987654321");
        request.setAreaNombramientoId(areaId);

        // Simula que el área indicada no existe en la base de datos.
        when(areaRepository.findById(areaId))
                .thenReturn(Optional.empty());

        // Verifica que el servicio rechace el registro.
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> docenteService.registrarDocente(request)
        );
    }
    // Verifica que se rechace la consulta de un docente inexistente.
    @Test
    void consultarDocente_debeRechazarDocenteInexistente() {

        // Identificador de un docente que no existe.
        UUID docenteId = UUID.randomUUID();

        // Simula que el repositorio no encuentra el docente.
        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.empty());

        // Verifica que el servicio informe que el docente no existe.
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> docenteService.consultarDocente(docenteId)
        );
    }
    // Verifica que se registre correctamente un docente con datos válidos.
    @Test
    void registrarDocente_debeRegistrarCorrectamente() {

        // Identificador del área válida.
        UUID areaId = UUID.randomUUID();

        // Simula un área existente.
        Area area = new Area();

        CrearDocenteRequest request = new CrearDocenteRequest();
        request.setNombres("Carlos");
        request.setApellidos("Bermúdez");
        request.setNumeroDocumento("111222333");
        request.setAreaNombramientoId(areaId);

        // Simula que el documento no está registrado.
        when(docenteRepository.existsByNumeroDocumento("111222333"))
                .thenReturn(false);

        // Simula que el área sí existe.
        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        // Devuelve la misma entidad que el servicio intenta guardar.
        when(docenteRepository.save(any(Docente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuta el registro.
        DocenteResponse response =
                docenteService.registrarDocente(request);

        // Verifica los datos principales de la respuesta.
        assertEquals("Carlos", response.getNombres());
        assertEquals("Bermúdez", response.getApellidos());
        assertEquals("111222333", response.getNumeroDocumento());
    }
    // Verifica que no se permita actualizar un docente con el documento de otro docente.
    @Test
    void actualizarDocente_debeRechazarDocumentoDuplicado() {

        // Identificador del docente que se intenta actualizar.
        UUID docenteId = UUID.randomUUID();

        // Simula que el docente existe.
        Docente docenteExistente = new Docente();

        ActualizarDocenteRequest request = new ActualizarDocenteRequest();
        request.setNumeroDocumento("444555666");

        // Simula que el docente a actualizar sí existe.
        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docenteExistente));

        // Simula que el documento pertenece a otro docente.
        when(docenteRepository.existsByNumeroDocumentoAndIdNot(
                "444555666", docenteId))
                .thenReturn(true);

        // Verifica que el servicio rechace la actualización.
        assertThrows(
                RecursoDuplicadoException.class,
                () -> docenteService.actualizarDocente(docenteId, request)
        );
    }
    // Verifica que se actualice correctamente un docente con datos válidos.
    @Test
    void actualizarDocente_debeActualizarCorrectamente() {

        // Identificadores del docente y del área válida.
        UUID docenteId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();

        // Simula un docente y un área existentes.
        Docente docenteExistente = new Docente();
        Area area = new Area();

        ActualizarDocenteRequest request = new ActualizarDocenteRequest();
        request.setNombres("Carlos");
        request.setApellidos("Bermúdez");
        request.setNumeroDocumento("777888999");
        request.setAreaNombramientoId(areaId);

        // Simula que el docente existe.
        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docenteExistente));

        // Simula que el documento no pertenece a otro docente.
        when(docenteRepository.existsByNumeroDocumentoAndIdNot(
                "777888999", docenteId))
                .thenReturn(false);

        // Simula que el área indicada existe.
        when(areaRepository.findById(areaId))
                .thenReturn(Optional.of(area));

        // Devuelve la misma entidad que el servicio actualiza.
        when(docenteRepository.save(any(Docente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecuta la actualización.
        DocenteResponse response =
                docenteService.actualizarDocente(docenteId, request);

        // Verifica que los datos hayan sido actualizados.
        assertEquals("Carlos", response.getNombres());
        assertEquals("Bermúdez", response.getApellidos());
        assertEquals("777888999", response.getNumeroDocumento());
    }
    // Verifica que se consulte correctamente un docente existente.
    @Test
    void consultarDocente_debeRetornarDocenteExistente() {

        // Identificador del docente consultado.
        UUID docenteId = UUID.randomUUID();

        // Simula un docente existente.
        Docente docente = new Docente();
        docente.setNombres("Ana");
        docente.setApellidos("Gómez");
        docente.setNumeroDocumento("123123123");
        docente.setAreaNombramiento(new Area());

        // Simula que el repositorio encuentra el docente.
        when(docenteRepository.findById(docenteId))
                .thenReturn(Optional.of(docente));

        // Ejecuta la consulta.
        DocenteResponse response =
                docenteService.consultarDocente(docenteId);

        // Verifica los datos principales devueltos.
        assertEquals("Ana", response.getNombres());
        assertEquals("Gómez", response.getApellidos());
        assertEquals("123123123", response.getNumeroDocumento());
    }
}

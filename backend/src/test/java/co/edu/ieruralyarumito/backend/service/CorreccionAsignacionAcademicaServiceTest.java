package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.RolUsuario;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionAcademicaMapper;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorreccionAsignacionAcademicaServiceTest {

    private static final String CORREO =
            "rector@yarumito.edu.co";

    @Mock
    private AsignacionAcademicaRepository asignacionRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AsignacionAcademicaMapper mapper;

    @Mock
    private RegistroAuditoriaService auditoria;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> conteoQuery;

    @InjectMocks
    private CorreccionAsignacionAcademicaService service;

    private final UUID asignacionId = UUID.randomUUID();
    private final UUID anioId = UUID.randomUUID();
    private final UUID grupoId = UUID.randomUUID();
    private final UUID sedeId = UUID.randomUUID();
    private final UUID asignaturaId = UUID.randomUUID();

    private Usuario rector() {

        Usuario usuario = new Usuario();
        usuario.setRol(RolUsuario.RECTOR);
        usuario.setActivo(true);

        return usuario;
    }

    private void autenticarRector(Usuario usuario) {

        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));
    }

    private AsignacionAcademica asignacion(
            EstadoAsignacionAcademica estado) {

        AnioEscolar anio = new AnioEscolar();
        ReflectionTestUtils.setField(anio, "id", anioId);

        Sede sede = new Sede();
        ReflectionTestUtils.setField(sede, "id", sedeId);

        Grupo grupo = new Grupo();
        ReflectionTestUtils.setField(grupo, "id", grupoId);
        grupo.setAnioEscolar(anio);
        grupo.setSede(sede);

        Asignatura asignatura = new Asignatura();
        ReflectionTestUtils.setField(
                asignatura, "id", asignaturaId);

        Docente docente = docente(UUID.randomUUID());

        AsignacionAcademica asignacion =
                new AsignacionAcademica();

        ReflectionTestUtils.setField(
                asignacion, "id", asignacionId);

        asignacion.setAnioEscolar(anio);
        asignacion.setGrupo(grupo);
        asignacion.setAsignatura(asignatura);
        asignacion.setDocente(docente);
        asignacion.setEstado(estado);
        asignacion.setHorasSemanales(4);

        return asignacion;
    }

    private Docente docente(UUID id) {

        Docente docente = new Docente();
        ReflectionTestUtils.setField(docente, "id", id);

        docente.setEstado(EstadoDocente.ACTIVO);
        docente.setHorasSemanalesContratadas(22);
        docente.setMaxHorasExtra(10);

        return docente;
    }

    // Simula las dos consultas de protección:
    // clases asociadas y horarios publicados.
    private void sinClasesNiPublicacion() {

        when(entityManager.createQuery(
                anyString(), eq(Long.class)))
                .thenReturn(conteoQuery);

        when(conteoQuery.setParameter(
                anyString(), any()))
                .thenReturn(conteoQuery);

        when(conteoQuery.getSingleResult())
                .thenReturn(0L, 0L);
    }

    private ActualizarAsignacionAcademicaRequest request(
            UUID docenteId,
            int horas) {

        ActualizarAsignacionAcademicaRequest request =
                new ActualizarAsignacionAcademicaRequest();

        request.setDocenteId(docenteId);
        request.setHorasSemanales(horas);
        request.setObservacion("Corrección autorizada por rectoría");

        return request;
    }

    private void prepararDocenteIdoneo(
            Docente docente,
            Asignatura asignatura) {

        Idoneidad idoneidad = new Idoneidad();
        idoneidad.setAsignatura(asignatura);
        idoneidad.setVigenteDesde(
                LocalDate.now().minusDays(1));

        when(docenteRepository.findById(docente.getId()))
                .thenReturn(Optional.of(docente));

        when(idoneidadRepository.findByDocente_Id(
                docente.getId()))
                .thenReturn(List.of(idoneidad));
    }

    @Test
    void H04_docenteNoPuedeEditar() {

        Usuario docente = new Usuario();
        docente.setActivo(true);
        docente.setRol(RolUsuario.DOCENTE);

        autenticarRector(docente);

        var request = request(UUID.randomUUID(), 4);

        assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(
                        asignacionId, request, CORREO));

        verifyNoInteractions(asignacionRepository);
    }

    @Test
    void H04_noPermiteModificarConfirmada() {

        Usuario rector = rector();
        autenticarRector(rector);

        AsignacionAcademica confirmada =
                asignacion(EstadoAsignacionAcademica.CONFIRMADA);

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(confirmada));

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> service.actualizar(
                        asignacionId,
                        request(UUID.randomUUID(), 4),
                        CORREO));

        verify(asignacionRepository, never())
                .save(any());
    }

    @Test
    void H04_noPermiteRetirarSiTieneClases() {

        Usuario rector = rector();
        autenticarRector(rector);

        AsignacionAcademica borrador =
                asignacion(EstadoAsignacionAcademica.BORRADOR);

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(borrador));

        when(entityManager.createQuery(
                anyString(), eq(Long.class)))
                .thenReturn(conteoQuery);

        when(conteoQuery.setParameter(
                anyString(), any()))
                .thenReturn(conteoQuery);

        when(conteoQuery.getSingleResult())
                .thenReturn(1L);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> service.retirar(asignacionId, CORREO));

        verify(asignacionRepository, never())
                .delete(any());
    }

    @Test
    void H04_noPermiteRetirarConHorarioPublicado() {

        Usuario rector = rector();
        autenticarRector(rector);

        AsignacionAcademica borrador =
                asignacion(EstadoAsignacionAcademica.BORRADOR);

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(borrador));

        when(entityManager.createQuery(
                anyString(), eq(Long.class)))
                .thenReturn(conteoQuery);

        when(conteoQuery.setParameter(
                anyString(), any()))
                .thenReturn(conteoQuery);

        when(conteoQuery.getSingleResult())
                .thenReturn(0L, 1L);

        assertThrows(
                TransicionEstadoNoPermitidaException.class,
                () -> service.retirar(asignacionId, CORREO));

        verify(asignacionRepository, never())
                .delete(any());
    }

    @Test
    void H04_permiteRetirarBorradorSinClases() {

        Usuario rector = rector();
        autenticarRector(rector);

        AsignacionAcademica borrador =
                asignacion(EstadoAsignacionAcademica.BORRADOR);

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(borrador));

        sinClasesNiPublicacion();

        assertDoesNotThrow(
                () -> service.retirar(asignacionId, CORREO));

        verify(asignacionRepository).delete(borrador);

        verify(auditoria).registrar(
                eq(rector),
                eq(RegistroAuditoriaService.ACCION_DELETE),
                eq("AsignacionAcademica"),
                eq(asignacionId),
                anyMap(),
                isNull(),
                isNull());
    }

    @Test
    void H04_permiteCorregirDocenteYHorasEnBorrador() {

        Usuario rector = rector();
        autenticarRector(rector);

        AsignacionAcademica borrador =
                asignacion(EstadoAsignacionAcademica.BORRADOR);

        Docente nuevoDocente = docente(UUID.randomUUID());

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(borrador));

        sinClasesNiPublicacion();

        prepararDocenteIdoneo(
                nuevoDocente, borrador.getAsignatura());

        when(asignacionRepository.findByDocente_Id(
                nuevoDocente.getId()))
                .thenReturn(List.of());

        when(asignacionRepository.save(borrador))
                .thenReturn(borrador);

        when(mapper.toResponse(borrador))
                .thenReturn(new AsignacionAcademicaResponse());

        assertDoesNotThrow(
                () -> service.actualizar(
                        asignacionId,
                        request(nuevoDocente.getId(), 5),
                        CORREO));

        assertEquals(
                nuevoDocente.getId(),
                borrador.getDocente().getId());

        assertEquals(5, borrador.getHorasSemanales());

        verify(auditoria).registrar(
                eq(rector),
                eq(RegistroAuditoriaService.ACCION_UPDATE),
                eq("AsignacionAcademica"),
                eq(asignacionId),
                anyMap(),
                anyMap(),
                isNull());
    }

    @Test
    void H04_noPermiteSuperarCargaDocente() {

        Usuario rector = rector();
        autenticarRector(rector);

        AsignacionAcademica borrador =
                asignacion(EstadoAsignacionAcademica.BORRADOR);

        Docente nuevoDocente = docente(UUID.randomUUID());

        when(asignacionRepository.findById(asignacionId))
                .thenReturn(Optional.of(borrador));

        sinClasesNiPublicacion();

        prepararDocenteIdoneo(
                nuevoDocente, borrador.getAsignatura());

        AsignacionAcademica existente =
                new AsignacionAcademica();

        ReflectionTestUtils.setField(
                existente, "id", UUID.randomUUID());

        existente.setAnioEscolar(borrador.getAnioEscolar());
        existente.setHorasSemanales(30);

        when(asignacionRepository.findByDocente_Id(
                nuevoDocente.getId()))
                .thenReturn(List.of(existente));

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> service.actualizar(
                        asignacionId,
                        request(nuevoDocente.getId(), 3),
                        CORREO));

        verify(asignacionRepository, never())
                .save(any());
    }
}

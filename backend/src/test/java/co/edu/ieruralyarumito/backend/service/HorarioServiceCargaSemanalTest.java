package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ClaseProgramadaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearClaseProgramadaRequest;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.ClaseProgramada;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.FranjaHoraria;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.mapper.HorarioMapper;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.ClaseProgramadaRepository;
import co.edu.ieruralyarumito.backend.repository.FranjaHorariaRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.HorarioRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.RestriccionDocenteRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioServiceCargaSemanalTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private AsignacionAcademicaRepository asignacionAcademicaRepository;

    @Mock
    private ClaseProgramadaRepository claseProgramadaRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PlanEstudiosRepository planEstudiosRepository;

    @Mock
    private HorarioMapper horarioMapper;

    @Mock
    private ConflictoHorarioRegistroService conflictoHorarioRegistroService;

    @Mock
    private RestriccionDocenteRepository restriccionDocenteRepository;

    @InjectMocks
    private HorarioService horarioService;

    private UUID anioId;
    private UUID sedeId;
    private UUID grupoId;
    private UUID horarioId;
    private UUID asignacionId;
    private UUID asignaturaId;
    private UUID docenteId;
    private UUID franjaId;
    private UUID aulaId;

    private AnioEscolar anio;
    private Sede sede;
    private Grupo grupo;
    private Horario horario;
    private AsignacionAcademica asignacion;
    private Asignatura asignatura;
    private Docente docente;
    private FranjaHoraria franja;
    private Aula aula;

    @BeforeEach
    void prepararDatos() {

        anioId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        grupoId = UUID.randomUUID();
        horarioId = UUID.randomUUID();
        asignacionId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();
        docenteId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        aulaId = UUID.randomUUID();

        anio = mock(AnioEscolar.class);
        sede = mock(Sede.class);
        grupo = mock(Grupo.class);
        horario = mock(Horario.class);
        asignacion = mock(AsignacionAcademica.class);
        asignatura = mock(Asignatura.class);
        docente = mock(Docente.class);
        franja = mock(FranjaHoraria.class);
        aula = mock(Aula.class);
    }

    @Test
    void H07_debeClasificarHora22ComoOrdinaria() {

        ClaseProgramada nueva =
                crearClaseConCargaPrevia(21);

        assertFalse(nueva.isEsHoraExtra());
    }

    @Test
    void H07_debeClasificarHora23ComoExtra() {

        ClaseProgramada nueva =
                crearClaseConCargaPrevia(22);

        assertTrue(nueva.isEsHoraExtra());
    }

    @Test
    void H07_debePermitirHora32ComoExtra() {

        ClaseProgramada nueva =
                crearClaseConCargaPrevia(31);

        assertTrue(nueva.isEsHoraExtra());
    }

    @Test
    void H07_debeRechazarHora33() {

        prepararCreacion();

        List<ClaseProgramada> existentes =
                crearClasesExistentes(32);

        when(claseProgramadaRepository
                .findByDocenteAndAnioEscolar(
                        anioId, docenteId))
                .thenReturn(existentes);

        assertThrows(
                RelacionAcademicaInvalidaException.class,
                () -> horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co"));

        verify(claseProgramadaRepository, never())
                .save(any(ClaseProgramada.class));
    }

    @Test
    void H07_debeReclasificarHorasExtrasDespuesDeEliminar() {

        prepararDatosCarga();

        UUID claseEliminarId = UUID.randomUUID();

        ClaseProgramada eliminar =
                mock(ClaseProgramada.class);

        when(eliminar.getHorarioId())
                .thenReturn(horario);

        when(eliminar.getAsignacionAcademicaId())
                .thenReturn(asignacion);

        when(claseProgramadaRepository
                .findById(claseEliminarId))
                .thenReturn(Optional.of(eliminar));

        // Después de eliminar quedan 22 clases.
        List<ClaseProgramada> restantes =
                crearClasesExistentes(22);

        ClaseProgramada reclasificar = restantes.get(0);

        when(reclasificar.isEsHoraExtra())
                .thenReturn(true);

        when(claseProgramadaRepository
                .findByDocenteAndAnioEscolar(
                        anioId, docenteId))
                .thenReturn(restantes);

        horarioService.eliminarClaseProgramada(
                claseEliminarId);

        verify(claseProgramadaRepository)
                .delete(eliminar);

        verify(reclasificar)
                .setEsHoraExtra(false);

        verify(claseProgramadaRepository)
                .save(reclasificar);
    }

    private ClaseProgramada crearClaseConCargaPrevia(
            int cantidadAnterior) {

        prepararCreacion();

        List<ClaseProgramada> anteriores =
                crearClasesExistentes(cantidadAnterior);

        AtomicReference<ClaseProgramada> nuevaReferencia =
                new AtomicReference<>();

        // H-07: capturar solo la clase recién creada.
        // Las llamadas posteriores a save() corresponden
        // al recálculo de horas extra y no deben sustituirla.
        when(claseProgramadaRepository
                .save(any(ClaseProgramada.class)))
                .thenAnswer(invocation -> {

                    ClaseProgramada guardada =
                            invocation.getArgument(0);

                    nuevaReferencia.compareAndSet(
                            null,
                            guardada);

                    return guardada;
                });

        // Antes de guardar: carga anterior.
        // Después de guardar: carga con la nueva clase.
        when(claseProgramadaRepository
                .findByDocenteAndAnioEscolar(
                        anioId, docenteId))
                .thenAnswer(invocation -> {

                    if (nuevaReferencia.get() == null) {
                        return anteriores;
                    }

                    List<ClaseProgramada> resultado =
                            new ArrayList<>(anteriores);

                    resultado.add(nuevaReferencia.get());

                    return resultado;
                });

        ClaseProgramadaResponse respuesta =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(
                any(ClaseProgramada.class)))
                .thenReturn(respuesta);

        ClaseProgramadaResponse resultado =
                horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co");

        assertSame(respuesta, resultado);

        ClaseProgramada nueva =
                nuevaReferencia.get();

        assertNotNull(nueva);

        verify(claseProgramadaRepository, atLeastOnce())
                .findByDocenteAndAnioEscolar(
                        anioId, docenteId);

        return nueva;
    }

    private void prepararCreacion() {

        prepararDatosCarga();

        when(asignacionAcademicaRepository
                .findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(grupo.getAnioEscolar())
                .thenReturn(anio);
    }

    private void prepararDatosCarga() {

        lenient().when(asignacion.getGrupo())
                .thenReturn(grupo);

        lenient().when(asignacion.getDocente())
                .thenReturn(docente);

        lenient().when(asignacion.getAsignatura())
                .thenReturn(asignatura);

        lenient().when(asignatura.getId())
                .thenReturn(asignaturaId);

        lenient().when(grupo.getId())
                .thenReturn(grupoId);

        lenient().when(grupo.getSede())
                .thenReturn(sede);

        lenient().when(anio.getId())
                .thenReturn(anioId);

        lenient().when(sede.getId())
                .thenReturn(sedeId);

        lenient().when(docente.getId())
                .thenReturn(docenteId);

        lenient().when(horario.getId())
                .thenReturn(horarioId);

        lenient().when(horario.getAnioEscolarId())
                .thenReturn(anio);

        lenient().when(horario.getSedeId())
                .thenReturn(sede);

        lenient().when(horarioRepository
                        .findFirstByAnioEscolarId_IdAndSedeId_IdOrderByVersionDesc(
                                anioId, sedeId))
                .thenReturn(Optional.of(horario));

        lenient().when(franjaHorariaRepository
                        .findById(franjaId))
                .thenReturn(Optional.of(franja));

        lenient().when(franja.getId())
                .thenReturn(franjaId);

        lenient().when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        lenient().when(aula.isActiva())
                .thenReturn(true);

        lenient().when(aula.getSede())
                .thenReturn(sede);
    }

    private List<ClaseProgramada> crearClasesExistentes(
            int cantidad) {

        List<ClaseProgramada> clases =
                new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {

            ClaseProgramada clase =
                    mock(ClaseProgramada.class);

            lenient().when(clase.getId())
                    .thenReturn(UUID.randomUUID());

            lenient().when(clase.getHorarioId())
                    .thenReturn(horario);

            lenient().when(clase.getFranjaHorariaId())
                    .thenReturn(franja);

            lenient().when(clase.getDia())
                    .thenReturn(DiasSemana.LUNES);

            clases.add(clase);
        }

        return clases;
    }

    private CrearClaseProgramadaRequest crearRequest() {

        CrearClaseProgramadaRequest request =
                new CrearClaseProgramadaRequest();

        request.setAsignacionAcademicaId(asignacionId);
        request.setDia(DiasSemana.MARTES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        return request;
    }
}

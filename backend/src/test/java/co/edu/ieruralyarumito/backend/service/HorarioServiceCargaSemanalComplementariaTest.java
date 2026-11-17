package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarClaseProgramadaRequest;
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
class HorarioServiceCargaSemanalComplementariaTest {

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

        configurarDatosComunes();
    }

    @Test
    void H07_noDebeSumarBloquesDeVersionesHistoricas() {

        // Horario anterior de la misma sede.
        Horario horarioHistorico = mock(Horario.class);

        lenient().when(horarioHistorico.getId())
                .thenReturn(UUID.randomUUID());

        lenient().when(horarioHistorico.getSedeId())
                .thenReturn(sede);

        // 32 clases en una versión histórica.
        List<ClaseProgramada> historicas =
                crearClasesExistentes(
                        32,
                        horarioHistorico,
                        DiasSemana.LUNES);

        // Solo 21 clases en el horario vigente.
        List<ClaseProgramada> vigentes =
                crearClasesExistentes(
                        21,
                        horario,
                        DiasSemana.LUNES);

        List<ClaseProgramada> todas =
                new ArrayList<>(historicas);

        todas.addAll(vigentes);

        AtomicReference<ClaseProgramada> nuevaReferencia =
                new AtomicReference<>();

        when(asignacionAcademicaRepository.findById(asignacionId))
                .thenReturn(Optional.of(asignacion));

        when(grupo.getAnioEscolar())
                .thenReturn(anio);

        when(claseProgramadaRepository.save(
                any(ClaseProgramada.class)))
                .thenAnswer(invocation -> {

                    ClaseProgramada guardada =
                            invocation.getArgument(0);

                    nuevaReferencia.compareAndSet(null, guardada);

                    return guardada;
                });

        when(claseProgramadaRepository
                .findByDocenteAndAnioEscolar(anioId, docenteId))
                .thenAnswer(invocation -> {

                    List<ClaseProgramada> resultado =
                            new ArrayList<>(todas);

                    if (nuevaReferencia.get() != null) {
                        resultado.add(nuevaReferencia.get());
                    }

                    return resultado;
                });

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(
                any(ClaseProgramada.class)))
                .thenReturn(response);

        ClaseProgramadaResponse resultado =
                horarioService.crearClaseProgramada(
                        crearRequest(),
                        "rector@yarumito.edu.co");

        assertSame(response, resultado);

        assertNotNull(nuevaReferencia.get());

        // Es la hora 22 vigente, por tanto es ordinaria.
        assertFalse(nuevaReferencia.get().isEsHoraExtra());

        verify(claseProgramadaRepository, atLeastOnce())
                .findByDocenteAndAnioEscolar(
                        anioId,
                        docenteId);
    }

    @Test
    void H07_debeRecalcularHoraExtraAlMoverClase() {

        UUID claseId = UUID.randomUUID();

        ClaseProgramada clase =
                mock(ClaseProgramada.class);

        // Ya existen 22 clases ordinarias los lunes.
        List<ClaseProgramada> existentes =
                crearClasesExistentes(
                        22,
                        horario,
                        DiasSemana.LUNES);

        when(claseProgramadaRepository.findById(claseId))
                .thenReturn(Optional.of(clase));

        when(clase.getId()).thenReturn(claseId);
        when(clase.getHorarioId()).thenReturn(horario);

        when(clase.getAsignacionAcademicaId())
                .thenReturn(asignacion);

        // Se representa el estado después del movimiento.
        lenient().when(clase.getDia())
                .thenReturn(DiasSemana.MARTES);

        lenient().when(clase.getFranjaHorariaId())
                .thenReturn(franja);

        when(claseProgramadaRepository.save(clase))
                .thenReturn(clase);

        List<ClaseProgramada> total =
                new ArrayList<>(existentes);

        total.add(clase);

        when(claseProgramadaRepository
                .findByDocenteAndAnioEscolar(anioId, docenteId))
                .thenReturn(total);

        ClaseProgramadaResponse response =
                new ClaseProgramadaResponse();

        when(horarioMapper.toClaseResponse(clase))
                .thenReturn(response);

        ClaseProgramadaResponse resultado =
                horarioService.actualizarClaseProgramada(
                        claseId,
                        actualizarRequest());

        assertSame(response, resultado);

        // Los 22 bloques anteriores son ordinarios.
        // La clase movida queda clasificada como extra.
        verify(clase).setEsHoraExtra(true);

        verify(claseProgramadaRepository, times(2))
                .save(clase);

        verify(claseProgramadaRepository)
                .findByDocenteAndAnioEscolar(
                        anioId,
                        docenteId);
    }

    private void configurarDatosComunes() {

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
                                anioId,
                                sedeId))
                .thenReturn(Optional.of(horario));

        lenient().when(franjaHorariaRepository.findById(franjaId))
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
            int cantidad,
            Horario horarioClase,
            DiasSemana dia) {

        List<ClaseProgramada> clases = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {

            ClaseProgramada clase =
                    mock(ClaseProgramada.class);

            lenient().when(clase.getId())
                    .thenReturn(UUID.randomUUID());

            lenient().when(clase.getHorarioId())
                    .thenReturn(horarioClase);

            lenient().when(clase.getFranjaHorariaId())
                    .thenReturn(franja);

            lenient().when(clase.getDia())
                    .thenReturn(dia);

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

    private ActualizarClaseProgramadaRequest actualizarRequest() {

        ActualizarClaseProgramadaRequest request =
                new ActualizarClaseProgramadaRequest();

        request.setDia(DiasSemana.MARTES);
        request.setFranjaHorariaId(franjaId);
        request.setAulaId(aulaId);

        return request;
    }
}

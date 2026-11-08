package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignacionAcademicaRequest;
import co.edu.ieruralyarumito.backend.dto.ResumenAsignacionAcademicaResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.AsignacionAcademica;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;
import co.edu.ieruralyarumito.backend.exception.TransicionEstadoNoPermitidaException;
import co.edu.ieruralyarumito.backend.mapper.AsignacionAcademicaMapper;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio de la asignación académica.
@Service
public class AsignacionAcademicaService {

    private final AsignacionAcademicaRepository asignacionAcademicaRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final DocenteRepository docenteRepository;
    private final GrupoRepository grupoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdoneidadRepository idoneidadRepository;
    private final PlanEstudiosRepository planEstudiosRepository;
    private final AsignacionAcademicaMapper asignacionAcademicaMapper;

    public AsignacionAcademicaService(
            AsignacionAcademicaRepository asignacionAcademicaRepository,
            AnioEscolarRepository anioEscolarRepository,
            DocenteRepository docenteRepository,
            GrupoRepository grupoRepository,
            AsignaturaRepository asignaturaRepository,
            UsuarioRepository usuarioRepository,
            IdoneidadRepository idoneidadRepository,
            PlanEstudiosRepository planEstudiosRepository,
            AsignacionAcademicaMapper asignacionAcademicaMapper) {

        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
        this.anioEscolarRepository = anioEscolarRepository;
        this.docenteRepository = docenteRepository;
        this.grupoRepository = grupoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.idoneidadRepository = idoneidadRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.asignacionAcademicaMapper = asignacionAcademicaMapper;
    }

    // Registra una nueva asignación académica en estado BORRADOR.
    @Transactional
    public AsignacionAcademicaResponse crearAsignacionAcademica(
            CrearAsignacionAcademicaRequest request,
            String correoUsuario) {

        AnioEscolar anioEscolar = anioEscolarRepository
                .findById(request.getAnioEscolarId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El año escolar no existe"));

        Docente docente = docenteRepository
                .findById(request.getDocenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El docente no existe"));

        Grupo grupo = grupoRepository
                .findById(request.getGrupoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El grupo no existe"));

        Asignatura asignatura = asignaturaRepository
                .findById(request.getAsignaturaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La asignatura no existe"));

        Usuario asignadaPor = usuarioRepository
                .findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no existe"));

        // Reglas de negocio HU-06, aplicadas antes de persistir.
        validarUnicidad(anioEscolar, grupo, asignatura);
        validarContinuidadDocente(anioEscolar, grupo, asignatura, docente);
        validarIdoneidadDocente(docente, asignatura);

        AsignacionAcademica asignacion =
                asignacionAcademicaMapper.toEntity(
                        request,
                        anioEscolar,
                        docente,
                        grupo,
                        asignatura,
                        asignadaPor);

        AsignacionAcademica guardada =
                asignacionAcademicaRepository.save(asignacion);

        return asignacionAcademicaMapper.toResponse(guardada);
    }

    // Consulta una asignación académica por su identificador.
    @Transactional(readOnly = true)
    public AsignacionAcademicaResponse consultarAsignacionAcademica(
            UUID id) {

        return asignacionAcademicaMapper.toResponse(
                obtenerAsignacion(id));
    }

    // Lista las asignaciones que se encuentran en estado BORRADOR.
    @Transactional(readOnly = true)
    public List<AsignacionAcademicaResponse> listarBorradores() {

        return asignacionAcademicaRepository
                .findByEstado(EstadoAsignacionAcademica.BORRADOR)
                .stream()
                .map(asignacionAcademicaMapper::toResponse)
                .toList();
    }

    // Lista las asignaciones de un docente.
    @Transactional(readOnly = true)
    public List<AsignacionAcademicaResponse> listarPorDocente(
            UUID docenteId) {

        if (!docenteRepository.existsById(docenteId)) {
            throw new RecursoNoEncontradoException(
                    "El docente no existe");
        }

        return asignacionAcademicaRepository
                .findByDocente_Id(docenteId)
                .stream()
                .map(asignacionAcademicaMapper::toResponse)
                .toList();
    }

    // Lista las asignaciones de un grupo.
    @Transactional(readOnly = true)
    public List<AsignacionAcademicaResponse> listarPorGrupo(
            UUID grupoId) {

        if (!grupoRepository.existsById(grupoId)) {
            throw new RecursoNoEncontradoException(
                    "El grupo no existe");
        }

        return asignacionAcademicaRepository
                .findByGrupo_Id(grupoId)
                .stream()
                .map(asignacionAcademicaMapper::toResponse)
                .toList();
    }

    // Confirma una asignación que se encuentra en estado BORRADOR.
    @Transactional
    public AsignacionAcademicaResponse confirmarAsignacionAcademica(
            UUID id) {

        AsignacionAcademica asignacion = obtenerAsignacion(id);

        if (asignacion.getEstado() != EstadoAsignacionAcademica.BORRADOR) {
            throw new TransicionEstadoNoPermitidaException(
                    "Solo las asignaciones en estado BORRADOR pueden confirmarse");
        }

        asignacion.setEstado(EstadoAsignacionAcademica.CONFIRMADA);

        return asignacionAcademicaMapper.toResponse(
                asignacionAcademicaRepository.save(asignacion));
    }

    // CA-12: impide duplicar la misma asignatura para el mismo año escolar,
    // grupo y asignatura.
    private void validarUnicidad(
            AnioEscolar anioEscolar,
            Grupo grupo,
            Asignatura asignatura) {

        boolean existeAsignacion =
                asignacionAcademicaRepository
                        .existsByAnioEscolar_IdAndGrupo_IdAndAsignatura_Id(
                                anioEscolar.getId(),
                                grupo.getId(),
                                asignatura.getId());

        if (existeAsignacion) {
            throw new RecursoDuplicadoException(
                    "Ya existe una asignación académica para el año escolar, "
                            + "grupo y asignatura indicados");
        }
    }

    // CA-09: una asignatura de un grado debe ser dictada por un único docente
    // en todos los grupos de ese grado durante el mismo año escolar.
    private void validarContinuidadDocente(
            AnioEscolar anioEscolar,
            Grupo grupo,
            Asignatura asignatura,
            Docente docente) {

        List<AsignacionAcademica> asignacionesExistentes =
                asignacionAcademicaRepository
                        .findByAnioEscolar_IdAndGrupo_Grado_IdAndAsignatura_Id(
                                anioEscolar.getId(),
                                grupo.getGrado().getId(),
                                asignatura.getId());

        boolean asignadaAOtroDocente = asignacionesExistentes.stream()
                .anyMatch(asignacion -> !asignacion.getDocente().getId()
                        .equals(docente.getId()));

        if (asignadaAOtroDocente) {
            throw new RelacionAcademicaInvalidaException(
                    "La asignatura ya está asignada a otro docente en el grado "
                            + grupo.getGrado().getNombre()
                            + "; debe ser dictada por el mismo docente en todos "
                            + "los grupos del grado");
        }
    }

    // CA-07: el docente debe tener una idoneidad vigente para la asignatura.
    // Sólo se consulta la idoneidad; no se crea ni se modifica.
    private void validarIdoneidadDocente(
            Docente docente,
            Asignatura asignatura) {

        LocalDate hoy = LocalDate.now();

        List<Idoneidad> idoneidades =
                idoneidadRepository.findByDocente_Id(docente.getId());

        boolean habilitado = idoneidades.stream()
                .anyMatch(idoneidad ->
                        cubreAsignatura(idoneidad, asignatura)
                                && estaVigente(idoneidad, hoy));

        if (!habilitado) {
            throw new RelacionAcademicaInvalidaException(
                    "El docente no cuenta con una idoneidad vigente "
                            + "para la asignatura indicada");
        }
    }

    // Una idoneidad cubre la asignatura de forma específica
    // o por el área a la que pertenece la asignatura.
    private boolean cubreAsignatura(
            Idoneidad idoneidad,
            Asignatura asignatura) {

        if (idoneidad.getAsignatura() != null) {
            return idoneidad.getAsignatura().getId()
                    .equals(asignatura.getId());
        }

        return idoneidad.getArea() != null
                && asignatura.getArea() != null
                && idoneidad.getArea().getId()
                        .equals(asignatura.getArea().getId());
    }

    // Verifica que la idoneidad esté vigente en la fecha indicada.
    private boolean estaVigente(
            Idoneidad idoneidad,
            LocalDate fecha) {

        boolean yaInicio = !idoneidad.getVigenteDesde().isAfter(fecha);
        boolean noHaFinalizado = idoneidad.getVigenteHasta() == null
                || !idoneidad.getVigenteHasta().isBefore(fecha);

        return yaInicio && noHaFinalizado;
    }

    // Obtiene una asignación existente o lanza el error de no encontrado.
    private AsignacionAcademica obtenerAsignacion(UUID id) {

        return asignacionAcademicaRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La asignación académica no existe"));
    }

    // Genera el resumen de horas requeridas y asignadas para un grupo.
    @Transactional(readOnly = true)
    public ResumenAsignacionAcademicaResponse generateResumenAsignacionAcademica(
            UUID anioEscolarId,
            UUID grupoId) {

        AnioEscolar anioEscolar = anioEscolarRepository
                .findById(anioEscolarId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Año escolar no encontrado"));

        Grupo grupo = grupoRepository
                .findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Grupo no encontrado"));

        Grado grado = grupo.getGrado();

        List<PlanEstudios> planEstudiosList = planEstudiosRepository
                .findByAnioEscolarAndGrado(
                        anioEscolar.getId(),
                        grado.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontraron planes de estudio para el año escolar "
                                + "y grupo especificados"));

        int horasRequeridas = planEstudiosList.stream()
                .mapToInt(PlanEstudios::getHorasSemanales)
                .sum();

        EstadoAsignacionAcademica estado;
        if (horasRequeridas < grado.getHorasSemanalesEsperadas()) {
            estado = EstadoAsignacionAcademica.BORRADOR;
        } else {
            estado = EstadoAsignacionAcademica.CONFIRMADA;
        }

        return new ResumenAsignacionAcademicaResponse(
                grupo.getCodigo(),
                grado.getNombre(),
                horasRequeridas,
                grado.getHorasSemanalesEsperadas(),
                estado);
    }
}

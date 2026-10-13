package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.CrearGrupoRequest;
import co.edu.ieruralyarumito.backend.dto.GrupoResponse;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.GradoRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Contiene la lógica de negocio para la gestión de grupos escolares.
@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final GradoRepository gradoRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final SedeRepository sedeRepository;
    private final DocenteRepository docenteRepository;

    // Inyección de dependencias mediante constructor.
    public GrupoService(
            GrupoRepository grupoRepository,
            GradoRepository gradoRepository,
            AnioEscolarRepository anioEscolarRepository,
            SedeRepository sedeRepository,
            DocenteRepository docenteRepository) {

        this.grupoRepository = grupoRepository;
        this.gradoRepository = gradoRepository;
        this.anioEscolarRepository = anioEscolarRepository;
        this.sedeRepository = sedeRepository;
        this.docenteRepository = docenteRepository;
    }

    // Elimina espacios externos del código del grupo.
    private String normalizarCodigo(String codigo) {
        return codigo.trim();
    }

    // Obtiene un grado existente.
    private Grado obtenerGrado(UUID gradoId) {

        return gradoRepository.findById(gradoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grado no existe"));
    }

    // Obtiene un año escolar existente.
    private AnioEscolar obtenerAnioEscolar(UUID anioEscolarId) {

        return anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El año escolar no existe"));
    }

    // Obtiene una sede existente.
    private Sede obtenerSede(UUID sedeId) {

        return sedeRepository.findById(sedeId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La sede no existe"));
    }

    // Obtiene el director de grupo cuando fue informado.
    private Docente obtenerDirectorGrupo(UUID directorGrupoId) {

        if (directorGrupoId == null) {
            return null;
        }

        return docenteRepository.findById(directorGrupoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El docente director de grupo no existe"));
    }

    // Obtiene un grupo existente.
    private Grupo obtenerGrupo(UUID id) {

        return grupoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El grupo no existe"));
    }

    // Valida la unicidad del código dentro del mismo año escolar.
    private void validarCodigoDuplicado(
            UUID anioEscolarId,
            String codigo) {

        if (grupoRepository.existsByAnioEscolar_IdAndCodigo(
                anioEscolarId,
                codigo)) {

            throw new RecursoDuplicadoException(
                    "Ya existe un grupo con ese código en el año escolar indicado");
        }
    }

    // Valida duplicidad al actualizar excluyendo el grupo actual.
    private void validarCodigoDuplicadoAlActualizar(
            UUID anioEscolarId,
            String codigo,
            UUID id) {

        if (grupoRepository.existsByAnioEscolar_IdAndCodigoAndIdNot(
                anioEscolarId,
                codigo,
                id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otro grupo con ese código en el año escolar indicado");
        }
    }

    // Convierte la entidad Grupo en el DTO devuelto por la API.
    private GrupoResponse convertirAResponse(Grupo grupo) {

        GrupoResponse response = new GrupoResponse();

        response.setId(grupo.getId());
        response.setCodigo(grupo.getCodigo());

        response.setGradoId(grupo.getGrado().getId());
        response.setGradoNivel(grupo.getGrado().getNivel());
        response.setGradoNombre(grupo.getGrado().getNombre());

        response.setAnioEscolarId(
                grupo.getAnioEscolar().getId());
        response.setAnioEscolar(
                grupo.getAnioEscolar().getAnio());

        response.setSedeId(grupo.getSede().getId());
        response.setSedeNombre(grupo.getSede().getNombre());

        if (grupo.getDirectorGrupo() != null) {

            Docente director = grupo.getDirectorGrupo();

            response.setDirectorGrupoId(director.getId());
            response.setDirectorGrupoNombre(
                    director.getNombres()
                            + " "
                            + director.getApellidos());
        }

        response.setCantidadEstudiantes(
                grupo.getCantidadEstudiantes());

        response.setAulaFijaId(
                grupo.getAulaFijaId());

        response.setActivo(
                grupo.isActivo());

        return response;
    }

    // Registra un nuevo grupo escolar.
    @Transactional
    public GrupoResponse registrarGrupo(
            CrearGrupoRequest request) {

        String codigoNormalizado =
                normalizarCodigo(request.getCodigo());

        validarCodigoDuplicado(
                request.getAnioEscolarId(),
                codigoNormalizado);

        Grado grado =
                obtenerGrado(request.getGradoId());

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(request.getAnioEscolarId());

        Sede sede =
                obtenerSede(request.getSedeId());

        Docente directorGrupo =
                obtenerDirectorGrupo(request.getDirectorGrupoId());

        Grupo grupo = new Grupo();

        grupo.setCodigo(codigoNormalizado);
        grupo.setGrado(grado);
        grupo.setAnioEscolar(anioEscolar);
        grupo.setSede(sede);
        grupo.setDirectorGrupo(directorGrupo);
        grupo.setCantidadEstudiantes(
                request.getCantidadEstudiantes());
        grupo.setAulaFijaId(
                request.getAulaFijaId());
        grupo.setActivo(
                request.getActivo());

        Grupo grupoGuardado =
                grupoRepository.save(grupo);

        return convertirAResponse(grupoGuardado);
    }

    // Consulta un grupo por su identificador.
    @Transactional(readOnly = true)
    public GrupoResponse consultarGrupo(UUID id) {

        return convertirAResponse(
                obtenerGrupo(id));
    }

    // Lista los grupos registrados con paginación.
    @Transactional(readOnly = true)
    public Page<GrupoResponse> listarGrupos(
            Pageable pageable) {

        return grupoRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza un grupo existente.
    @Transactional
    public GrupoResponse actualizarGrupo(
            UUID id,
            ActualizarGrupoRequest request) {

        Grupo grupo =
                obtenerGrupo(id);

        String codigoNormalizado =
                normalizarCodigo(request.getCodigo());

        validarCodigoDuplicadoAlActualizar(
                request.getAnioEscolarId(),
                codigoNormalizado,
                id);

        Grado grado =
                obtenerGrado(request.getGradoId());

        AnioEscolar anioEscolar =
                obtenerAnioEscolar(request.getAnioEscolarId());

        Sede sede =
                obtenerSede(request.getSedeId());

        Docente directorGrupo =
                obtenerDirectorGrupo(request.getDirectorGrupoId());

        grupo.setCodigo(codigoNormalizado);
        grupo.setGrado(grado);
        grupo.setAnioEscolar(anioEscolar);
        grupo.setSede(sede);
        grupo.setDirectorGrupo(directorGrupo);
        grupo.setCantidadEstudiantes(
                request.getCantidadEstudiantes());
        grupo.setAulaFijaId(
                request.getAulaFijaId());
        grupo.setActivo(
                request.getActivo());

        Grupo grupoActualizado =
                grupoRepository.save(grupo);

        return convertirAResponse(grupoActualizado);
    }
}

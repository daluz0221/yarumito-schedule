package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAulaRequest;
import co.edu.ieruralyarumito.backend.dto.AulaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAulaRequest;

import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;

import co.edu.ieruralyarumito.backend.exception.CambioAulaInvalidoException;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.exception.RelacionAcademicaInvalidaException;

import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// Gestión de aulas.
// H-16: validación de capacidad y auditoría.
@Service
public class AulaService {

    private final AulaRepository aulaRepository;
    private final SedeRepository sedeRepository;

    private UsuarioRepository usuarioRepository;
    private RegistroAuditoriaService registroAuditoriaService;

    // Conserva el constructor original.
    public AulaService(
            AulaRepository aulaRepository,
            SedeRepository sedeRepository) {

        this.aulaRepository = aulaRepository;
        this.sedeRepository = sedeRepository;
    }

    // H-16: integración con Control y Auditoría.
    @Autowired
    public void configurarAuditoria(
            UsuarioRepository usuarioRepository,
            RegistroAuditoriaService registroAuditoriaService) {

        this.usuarioRepository =
                Objects.requireNonNull(usuarioRepository);

        this.registroAuditoriaService =
                Objects.requireNonNull(registroAuditoriaService);
    }

    private Sede obtenerSede(UUID sedeId) {

        return sedeRepository.findById(sedeId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La sede no existe"));
    }

    private Aula obtenerAula(UUID aulaId) {

        return aulaRepository.findById(aulaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El aula no existe"));
    }

    // H-16: capacidad positiva o desconocida.
    // null significa que no hay capacidad formal registrada.
    private void validarCapacidad(Integer capacidad) {

        if (capacidad != null && capacidad <= 0) {

            throw new RelacionAcademicaInvalidaException(
                    "La capacidad del aula debe ser mayor "
                            + "que cero o permanecer sin definir");
        }
    }

    private String normalizarNombre(String nombre) {

        return nombre.trim().replaceAll("\\s+", " ");
    }

    // Rechaza aulas activas duplicadas dentro de una sede.
    private void validarDuplicadoActivoAlCrear(
            UUID sedeId,
            String nombre) {

        if (aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
                        sedeId, nombre)) {

            throw new RecursoDuplicadoException(
                    "Ya existe un aula activa con ese nombre "
                            + "en la sede indicada");
        }
    }

    private void validarDuplicadoActivoAlActualizar(
            UUID sedeId,
            String nombre,
            UUID aulaId) {

        if (aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId, nombre, aulaId)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otra aula activa con ese nombre "
                            + "en la sede indicada");
        }
    }

    // Conserva la distinción entre actualización ordinaria
    // y cambio real de identidad del aula.
    private boolean existeCambioReal(
            Aula aulaActual,
            UUID nuevaSedeId,
            String nuevoNombre,
            TipoAulaRequerida nuevoTipo) {

        boolean cambioNombre =
                !aulaActual.getNombre()
                        .equalsIgnoreCase(nuevoNombre);

        boolean cambioTipo =
                aulaActual.getTipo() != nuevoTipo;

        boolean cambioSede =
                !aulaActual.getSede().getId()
                        .equals(nuevaSedeId);

        return cambioNombre || cambioTipo || cambioSede;
    }

    private AulaResponse convertirAResponse(
            Aula aula,
            UUID sedeId) {

        AulaResponse response = new AulaResponse();

        response.setId(aula.getId());
        response.setSedeId(sedeId);
        response.setSedeNombre(aula.getSede().getNombre());
        response.setNombre(aula.getNombre());
        response.setTipo(aula.getTipo());
        response.setCapacidad(aula.getCapacidad());
        response.setActiva(aula.isActiva());

        return response;
    }

    // Crea un aula activa.
    @Transactional
    public AulaResponse registrarAula(CrearAulaRequest request) {

        Sede sede = obtenerSede(request.getSedeId());

        String nombreNormalizado =
                normalizarNombre(request.getNombre());

        validarDuplicadoActivoAlCrear(
                request.getSedeId(),
                nombreNormalizado);

        // H-16: impide capacidad cero o negativa.
        validarCapacidad(request.getCapacidad());

        Aula aula = new Aula();

        aula.setSede(sede);
        aula.setNombre(nombreNormalizado);
        aula.setTipo(request.getTipo());
        aula.setCapacidad(request.getCapacidad());
        aula.setActiva(true);

        Aula guardada = aulaRepository.save(aula);

        // H-16: auditoría de creación.
        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_CREATE,
                    guardada,
                    null,
                    datosAula(guardada));
        }

        return convertirAResponse(
                guardada,
                request.getSedeId());
    }

    @Transactional(readOnly = true)
    public AulaResponse consultarAula(UUID id) {

        Aula aula = obtenerAula(id);

        return convertirAResponse(
                aula,
                aula.getSede().getId());
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarPorSede(UUID sedeId) {

        obtenerSede(sedeId);

        return aulaRepository.findBySede_Id(sedeId)
                .stream()
                .map(aula -> convertirAResponse(aula, sedeId))
                .toList();
    }

    // Actualiza el mismo registro del aula.
    // La capacidad no genera una nueva versión.
    @Transactional
    public AulaResponse actualizarAula(
            UUID id,
            ActualizarAulaRequest request) {

        Aula aula = obtenerAula(id);

        Sede sede = obtenerSede(request.getSedeId());

        String nombreNormalizado =
                normalizarNombre(request.getNombre());

        if (request.getActiva()) {

            validarDuplicadoActivoAlActualizar(
                    request.getSedeId(),
                    nombreNormalizado,
                    id);
        }

        validarCapacidad(request.getCapacidad());

        // H-16: datos originales antes del cambio.
        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null
                        : datosAula(aula);

        aula.setSede(sede);
        aula.setNombre(nombreNormalizado);
        aula.setTipo(request.getTipo());
        aula.setCapacidad(request.getCapacidad());
        aula.setActiva(request.getActiva());

        Aula actualizada = aulaRepository.save(aula);

        // H-16: trazabilidad de actualización.
        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    actualizada,
                    antes,
                    datosAula(actualizada));
        }

        return convertirAResponse(
                actualizada,
                request.getSedeId());
    }

    // Conserva el versionado histórico existente.
    @Transactional
    public AulaResponse registrarCambioReal(
            UUID aulaAnteriorId,
            CrearAulaRequest request) {

        Aula aulaAnterior = obtenerAula(aulaAnteriorId);

        Sede nuevaSede = obtenerSede(request.getSedeId());

        String nuevoNombre =
                normalizarNombre(request.getNombre());

        if (!existeCambioReal(
                aulaAnterior,
                request.getSedeId(),
                nuevoNombre,
                request.getTipo())) {

            throw new CambioAulaInvalidoException(
                    "El cambio indicado no requiere "
                            + "una nueva versión del aula");
        }

        validarDuplicadoActivoAlActualizar(
                request.getSedeId(),
                nuevoNombre,
                aulaAnteriorId);

        validarCapacidad(request.getCapacidad());

        // H-16: estado de la versión histórica anterior.
        Map<String, Object> antes =
                registroAuditoriaService == null
                        ? null
                        : datosAula(aulaAnterior);

        // La versión anterior permanece en PostgreSQL.
        aulaAnterior.setActiva(false);

        Aula anteriorActualizada =
                aulaRepository.save(aulaAnterior);

        // Auditoría de la inactivación de la versión anterior.
        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_UPDATE,
                    anteriorActualizada,
                    antes,
                    datosAula(anteriorActualizada));
        }

        // Nueva versión con identificador propio.
        Aula nuevaAula = new Aula();

        nuevaAula.setSede(nuevaSede);
        nuevaAula.setNombre(nuevoNombre);
        nuevaAula.setTipo(request.getTipo());
        nuevaAula.setCapacidad(request.getCapacidad());
        nuevaAula.setActiva(true);

        Aula nuevaGuardada = aulaRepository.save(nuevaAula);

        // Auditoría de la nueva versión.
        if (registroAuditoriaService != null) {

            auditar(
                    RegistroAuditoriaService.ACCION_CREATE,
                    nuevaGuardada,
                    null,
                    datosAula(nuevaGuardada));
        }

        return convertirAResponse(
                nuevaGuardada,
                request.getSedeId());
    }

    // El borrado físico continúa bloqueado.
    // No se genera auditoría para una operación rechazada.
    @Transactional
    public void eliminarAula(UUID aulaId) {

        obtenerAula(aulaId);

        throw new CambioAulaInvalidoException(
                "No se puede eliminar físicamente el aula "
                        + "porque el sistema no puede garantizar "
                        + "que nunca haya sido utilizada");
    }

    // H-16: datos de negocio para el historial.
    private Map<String, Object> datosAula(Aula aula) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("id", aula.getId());

        datos.put("sedeId",
                aula.getSede() == null
                        ? null
                        : aula.getSede().getId());

        datos.put("nombre", aula.getNombre());
        datos.put("tipo", aula.getTipo());
        datos.put("capacidad", aula.getCapacidad());
        datos.put("activa", aula.isActiva());

        return datos;
    }

    // H-16: registra las operaciones exitosas
    // en la misma transacción del servicio.
    private void auditar(
            String accion,
            Aula aula,
            Object antes,
            Object despues) {

        if (registroAuditoriaService == null) {
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No hay usuario autenticado para auditar");
        }

        Usuario usuario = usuarioRepository
                .findByCorreo(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Usuario autenticado no identificado "
                                        + "para auditoría"));

        registroAuditoriaService.registrar(
                usuario,
                accion,
                "Aula",
                aula.getId(),
                antes,
                despues,
                null);
    }
}

package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAulaRequest;
import co.edu.ieruralyarumito.backend.dto.AulaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAulaRequest;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.exception.CambioAulaInvalidoException;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de aulas.
@Service
public class AulaService {

    private final AulaRepository aulaRepository;
    private final SedeRepository sedeRepository;

    public AulaService(
            AulaRepository aulaRepository,
            SedeRepository sedeRepository){


        this.aulaRepository = aulaRepository;
        this.sedeRepository = sedeRepository;

    }

    // Obtiene una sede existente.
    private Sede obtenerSede(
            UUID sedeId) {

        return sedeRepository.findById(sedeId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La sede no existe"));
    }

    // Obtiene un aula existente.
    private Aula obtenerAula(
            UUID aulaId) {

        return aulaRepository.findById(aulaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El aula no existe"));
    }

    // Normaliza el nombre del aula eliminando espacios externos
    // y reduciendo secuencias internas de espacios.
    private String normalizarNombre(
            String nombre) {

        return nombre
                .trim()
                .replaceAll("\\s+", " ");
    }

    // Valida que no exista otra aula activa con el mismo nombre
    // dentro de la misma sede.
    private void validarDuplicadoActivoAlCrear(
            UUID sedeId,
            String nombre) {

        if (aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
                        sedeId,
                        nombre)) {

            throw new RecursoDuplicadoException(
                    "Ya existe un aula activa con ese nombre en la sede indicada");
        }
    }

    // Valida duplicidad de aula activa al actualizar,
    // excluyendo el registro actual.
    private void validarDuplicadoActivoAlActualizar(
            UUID sedeId,
            String nombre,
            UUID aulaId) {

        if (aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        nombre,
                        aulaId)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otra aula activa con ese nombre en la sede indicada");
        }
    }

    // Determina si el cambio representa una modificación real
    // de identidad física/operativa del espacio.
    //
    // La capacidad no genera una nueva versión histórica.
    private boolean existeCambioReal(
            Aula aulaActual,
            UUID nuevaSedeId,
            String nuevoNombre,
            TipoAulaRequerida nuevoTipo) {

        boolean cambioNombre =
                !aulaActual
                        .getNombre()
                        .equalsIgnoreCase(
                                nuevoNombre);

        boolean cambioTipo =
                aulaActual.getTipo()
                        != nuevoTipo;

        boolean cambioSede =
                !aulaActual
                        .getSede()
                        .getId()
                        .equals(
                                nuevaSedeId);

        return cambioNombre
                || cambioTipo
                || cambioSede;
    }

    // Convierte la entidad Aula en el DTO devuelto por la API.
    private AulaResponse convertirAResponse(
            Aula aula,
            UUID sedeId) {

        AulaResponse response =
                new AulaResponse();

        response.setId(
                aula.getId());

        response.setSedeId(
                sedeId);

        response.setSedeNombre(
                aula.getSede().getNombre());

        response.setNombre(
                aula.getNombre());

        response.setTipo(
                aula.getTipo());

        response.setCapacidad(
                aula.getCapacidad());

        response.setActiva(
                aula.isActiva());

        return response;
    }

    // Registra un aula nueva.
    // Toda aula nueva inicia activa.
    @Transactional
    public AulaResponse registrarAula(
            CrearAulaRequest request) {

        Sede sede =
                obtenerSede(
                        request.getSedeId());

        String nombreNormalizado =
                normalizarNombre(
                        request.getNombre());

        validarDuplicadoActivoAlCrear(
                request.getSedeId(),
                nombreNormalizado);

        Aula aula =
                new Aula();

        aula.setSede(
                sede);

        aula.setNombre(
                nombreNormalizado);

        aula.setTipo(
                request.getTipo());

        aula.setCapacidad(
                request.getCapacidad());

        aula.setActiva(
                true);

        Aula aulaGuardada =
                aulaRepository.save(
                        aula);

        return convertirAResponse(
                aulaGuardada,
                request.getSedeId());
    }

    // Consulta un aula por identificador.
    @Transactional(readOnly = true)
    public AulaResponse consultarAula(
            UUID id) {

        Aula aula =
                obtenerAula(id);

        return convertirAResponse(
                aula,
                aula.getSede().getId());
    }

    // Lista las aulas pertenecientes a una sede.
    @Transactional(readOnly = true)
    public List<AulaResponse> listarPorSede(
            UUID sedeId) {

        obtenerSede(
                sedeId);

        return aulaRepository
                .findBySede_Id(sedeId)
                .stream()
                .map(aula ->
                        convertirAResponse(
                                aula,
                                sedeId))
                .toList();
    }

    // Actualiza el mismo registro del aula.
    //
    // Este método se utiliza para:
    // - corrección de errores de digitación;
    // - actualización de capacidad;
    // - activación o inactivación temporal;
    // - corrección de sede cuando el dato original fue erróneo.
    //
    // Un cambio real de identidad del espacio debe utilizar
    // registrarCambioReal().
    @Transactional
    public AulaResponse actualizarAula(
            UUID id,
            ActualizarAulaRequest request) {

        Aula aula =
                obtenerAula(id);

        Sede sede =
                obtenerSede(
                        request.getSedeId());

        String nombreNormalizado =
                normalizarNombre(
                        request.getNombre());

        if (request.getActiva()) {

            validarDuplicadoActivoAlActualizar(
                    request.getSedeId(),
                    nombreNormalizado,
                    id);
        }

        aula.setSede(
                sede);

        aula.setNombre(
                nombreNormalizado);

        aula.setTipo(
                request.getTipo());

        aula.setCapacidad(
                request.getCapacidad());

        aula.setActiva(
                request.getActiva());

        Aula aulaActualizada =
                aulaRepository.save(
                        aula);

        return convertirAResponse(
                aulaActualizada,
                request.getSedeId());
    }

    // Registra una nueva versión cuando existe un cambio real
    // en la identidad operativa del espacio.
    //
    // La versión anterior se conserva inactiva.
    // La nueva versión se registra activa y obtiene un UUID propio
    // mediante JPA al persistirse.
    @Transactional
    public AulaResponse registrarCambioReal(
            UUID aulaAnteriorId,
            CrearAulaRequest request) {

        Aula aulaAnterior =
                obtenerAula(
                        aulaAnteriorId);

        Sede nuevaSede =
                obtenerSede(
                        request.getSedeId());

        String nuevoNombre =
                normalizarNombre(
                        request.getNombre());

        if (!existeCambioReal(
                aulaAnterior,
                request.getSedeId(),
                nuevoNombre,
                request.getTipo())) {

            throw new CambioAulaInvalidoException(
                    "El cambio indicado no requiere una nueva versión del aula");
        }

        validarDuplicadoActivoAlActualizar(
                request.getSedeId(),
                nuevoNombre,
                aulaAnteriorId);

        // La versión anterior queda preservada como histórica.
        aulaAnterior.setActiva(
                false);

        aulaRepository.save(
                aulaAnterior);

        // Se crea una nueva entidad para que JPA genere
        // un identificador diferente.
        Aula nuevaAula =
                new Aula();

        nuevaAula.setSede(
                nuevaSede);

        nuevaAula.setNombre(
                nuevoNombre);

        nuevaAula.setTipo(
                request.getTipo());

        nuevaAula.setCapacidad(
                request.getCapacidad());

        nuevaAula.setActiva(
                true);

        Aula nuevaAulaGuardada =
                aulaRepository.save(
                        nuevaAula);

        return convertirAResponse(
                nuevaAulaGuardada,
                request.getSedeId());
    }

    // El borrado físico de Aula se mantiene bloqueado de forma conservadora.
    //
    // La regla de negocio permite eliminar únicamente un aula creada por error
    // y que nunca haya sido utilizada.
    //
    // Con el modelo actual no existe trazabilidad suficiente para demostrar
    // de forma completa la ausencia de uso histórico.
    //
    // Mientras esa condición no pueda acreditarse, el registro se preserva.

    @Transactional
    public void eliminarAula(
            UUID aulaId) {

        obtenerAula(
                aulaId);

        throw new CambioAulaInvalidoException(
                "No se puede eliminar físicamente el aula porque el sistema no puede garantizar que nunca haya sido utilizada");

    }
}

package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarSedeRequest;
import co.edu.ieruralyarumito.backend.dto.CrearSedeRequest;
import co.edu.ieruralyarumito.backend.dto.SedeResponse;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

// Contiene la lógica de negocio para la gestión de sedes.
@Service
public class SedeService {

    private final SedeRepository sedeRepository;

    // Inyección de dependencias mediante constructor.
    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    // Normaliza textos de catálogo eliminando espacios externos
    // y convirtiéndolos a mayúsculas de forma consistente.
    private String normalizarMayusculas(String texto) {
        return texto.trim().toUpperCase(Locale.ROOT);
    }

    // Normaliza la dirección sin alterar su combinación de mayúsculas y minúsculas.
    private String normalizarDireccion(String direccion) {

        if (direccion == null) {
            return null;
        }

        String direccionNormalizada = direccion.trim();

        return direccionNormalizada.isEmpty()
                ? null
                : direccionNormalizada;
    }

    // RN-12.S01: valida que el código de la sede sea único.
    private void validarCodigoDuplicado(String codigo) {

        if (sedeRepository.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException(
                    "Ya existe una sede con ese código");
        }
    }

    // RN-12.S01: valida duplicidad al actualizar excluyendo la sede actual.
    private void validarCodigoDuplicadoAlActualizar(
            String codigo,
            UUID id) {

        if (sedeRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new RecursoDuplicadoException(
                    "Ya existe otra sede con ese código");
        }
    }

    // RN-12.S02: solo puede existir una sede principal.
    private void validarUnicaSedePrincipal(boolean esPrincipal) {

        if (esPrincipal && sedeRepository.existsByEsPrincipalTrue()) {
            throw new RecursoDuplicadoException(
                    "Ya existe una sede principal registrada");
        }
    }

    // RN-12.S02: valida la sede principal al actualizar,
    // excluyendo la sede que se está modificando.
    private void validarUnicaSedePrincipalAlActualizar(
            boolean esPrincipal,
            UUID id) {

        if (esPrincipal
                && sedeRepository.existsByEsPrincipalTrueAndIdNot(id)) {

            throw new RecursoDuplicadoException(
                    "Ya existe otra sede principal registrada");
        }
    }

    // Convierte la entidad Sede en el DTO devuelto por la API.
    private SedeResponse convertirAResponse(Sede sede) {

        SedeResponse response = new SedeResponse();

        response.setId(sede.getId());
        response.setNombre(sede.getNombre());
        response.setCodigo(sede.getCodigo());
        response.setDireccion(sede.getDireccion());
        response.setEsPrincipal(sede.isEsPrincipal());

        return response;
    }

    // Registra una nueva sede.
    @Transactional
    public SedeResponse registrarSede(CrearSedeRequest request) {

        String nombreNormalizado =
                normalizarMayusculas(request.getNombre());

        String codigoNormalizado =
                normalizarMayusculas(request.getCodigo());

        String direccionNormalizada =
                normalizarDireccion(request.getDireccion());

        validarCodigoDuplicado(codigoNormalizado);
        validarUnicaSedePrincipal(request.getEsPrincipal());

        Sede sede = new Sede();

        sede.setNombre(nombreNormalizado);
        sede.setCodigo(codigoNormalizado);
        sede.setDireccion(direccionNormalizada);
        sede.setEsPrincipal(request.getEsPrincipal());

        Sede sedeGuardada = sedeRepository.save(sede);

        return convertirAResponse(sedeGuardada);
    }

    // Consulta una sede por su identificador.
    @Transactional(readOnly = true)
    public SedeResponse consultarSede(UUID id) {

        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La sede no existe"));

        return convertirAResponse(sede);
    }

    // Lista las sedes con paginación.
    @Transactional(readOnly = true)
    public Page<SedeResponse> listarSedes(Pageable pageable) {

        return sedeRepository.findAll(pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza una sede existente.
    @Transactional
    public SedeResponse actualizarSede(
            UUID id,
            ActualizarSedeRequest request) {

        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La sede no existe"));

        String nombreNormalizado =
                normalizarMayusculas(request.getNombre());

        String codigoNormalizado =
                normalizarMayusculas(request.getCodigo());

        String direccionNormalizada =
                normalizarDireccion(request.getDireccion());

        validarCodigoDuplicadoAlActualizar(
                codigoNormalizado,
                id);

        validarUnicaSedePrincipalAlActualizar(
                request.getEsPrincipal(),
                id);

        sede.setNombre(nombreNormalizado);
        sede.setCodigo(codigoNormalizado);
        sede.setDireccion(direccionNormalizada);
        sede.setEsPrincipal(request.getEsPrincipal());

        Sede sedeActualizada = sedeRepository.save(sede);

        return convertirAResponse(sedeActualizada);
    }

}

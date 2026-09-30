package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAreaRequest;
import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAreaRequest;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.specification.AreaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Contiene la lógica de negocio para la gestión de áreas.
@Service
public class AreaService {

    private final AreaRepository areaRepository;

    // Inyección de dependencias mediante constructor.
    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    // Valida que el código no esté registrado por otra área.
    private void validarCodigoDuplicado(String codigo) {

        if (areaRepository.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException(
                    "Ya existe un área con ese código");
        }
    }

    // Valida duplicidad de código al actualizar, excluyendo el área actual.
    private void validarCodigoDuplicadoAlActualizar(
            String codigo,
            UUID id) {

        if (areaRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new RecursoDuplicadoException(
                    "Ya existe otra área con ese código");
        }
    }

    // Convierte la entidad Area en el DTO devuelto por la API.
    private AreaResponse convertirAResponse(Area area) {

        AreaResponse response = new AreaResponse();

        response.setId(area.getId());
        response.setNombre(area.getNombre());
        response.setCodigo(area.getCodigo());
        response.setObligatoria(area.isObligatoria());
        response.setSoloMedia(area.isSoloMedia());
        response.setActiva(area.isActiva());

        return response;
    }

    // Registra una nueva área.
    @Transactional
    public AreaResponse registrarArea(CrearAreaRequest request) {

        validarCodigoDuplicado(request.getCodigo());

        Area area = new Area();

        area.setNombre(request.getNombre());
        area.setCodigo(request.getCodigo());
        area.setObligatoria(request.getObligatoria());
        area.setSoloMedia(request.getSoloMedia());
        area.setActiva(request.getActiva());

        Area areaGuardada = areaRepository.save(area);

        return convertirAResponse(areaGuardada);
    }

    // Consulta un área por su identificador.
    @Transactional(readOnly = true)
    public AreaResponse consultarArea(UUID id) {

        Area area = areaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área no existe"));

        return convertirAResponse(area);
    }

    // Lista áreas aplicando búsqueda, filtro por estado y paginación.
    @Transactional(readOnly = true)
    public Page<AreaResponse> listarAreas(
            String texto,
            Boolean activa,
            Pageable pageable) {

        Specification<Area> specification =
                AreaSpecification.buscarPorTexto(texto)
                        .and(AreaSpecification.porEstado(activa));

        return areaRepository.findAll(specification, pageable)
                .map(this::convertirAResponse);
    }

    // Actualiza los datos de un área existente.
    @Transactional
    public AreaResponse actualizarArea(
            UUID id,
            ActualizarAreaRequest request) {

        Area area = areaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área no existe"));

        validarCodigoDuplicadoAlActualizar(
                request.getCodigo(),
                id);

        area.setNombre(request.getNombre());
        area.setCodigo(request.getCodigo());
        area.setObligatoria(request.getObligatoria());
        area.setSoloMedia(request.getSoloMedia());
        area.setActiva(request.getActiva());

        Area areaActualizada = areaRepository.save(area);

        return convertirAResponse(areaActualizada);
    }

    // Cambia únicamente el estado activo/inactivo de un área.
    @Transactional
    public AreaResponse cambiarEstado(
            UUID id,
            boolean activa) {

        Area area = areaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El área no existe"));

        area.setActiva(activa);

        Area areaActualizada = areaRepository.save(area);

        return convertirAResponse(areaActualizada);
    }
}
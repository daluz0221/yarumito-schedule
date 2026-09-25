package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAsignaturaRequest;
import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAsignaturaRequest;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.specification.AsignaturaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final AreaRepository areaRepository;

    public AsignaturaService(
            AsignaturaRepository asignaturaRepository,
            AreaRepository areaRepository) {

        this.asignaturaRepository = asignaturaRepository;
        this.areaRepository = areaRepository;
    }

    private Area obtenerArea(UUID areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("El área no existe"));
    }

    private Asignatura obtenerAsignatura(UUID id) {
        return asignaturaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("La asignatura no existe"));
    }

    private void validarCodigoDuplicado(String codigo) {
        if (asignaturaRepository.existsByCodigo(codigo)) {
            throw new RecursoDuplicadoException(
                    "Ya existe una asignatura con ese código");
        }
    }

    private void validarCodigoDuplicadoAlActualizar(
            String codigo,
            UUID id) {

        if (asignaturaRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new RecursoDuplicadoException(
                    "Ya existe otra asignatura con ese código");
        }
    }

    private AsignaturaResponse convertirAResponse(Asignatura asignatura) {

        AsignaturaResponse response = new AsignaturaResponse();

        response.setId(asignatura.getId());
        response.setAreaId(asignatura.getArea().getId());
        response.setNombre(asignatura.getNombre());
        response.setCodigo(asignatura.getCodigo());
        response.setAbreviatura(asignatura.getAbreviatura());
        response.setColorUi(asignatura.getColorUi());
        response.setExigeIdoneidadEstricta(
                asignatura.isExigeIdoneidadEstricta());
        response.setEsMediaTecnica(
                asignatura.isEsMediaTecnica());
        response.setRequiereDocenteExclusivo(
                asignatura.isRequiereDocenteExclusivo());
        response.setTipoAulaRequerida(
                asignatura.getTipoAulaRequerida());
        response.setMaxClasesConsecutivas(
                asignatura.getMaxClasesConsecutivas());
        response.setActiva(
                asignatura.isActiva());

        return response;
    }

    @Transactional
    public AsignaturaResponse registrarAsignatura(
            CrearAsignaturaRequest request) {

        validarCodigoDuplicado(request.getCodigo());

        Area area = obtenerArea(request.getAreaId());

        Asignatura asignatura = new Asignatura();

        asignatura.setArea(area);
        asignatura.setNombre(request.getNombre());
        asignatura.setCodigo(request.getCodigo());
        asignatura.setAbreviatura(request.getAbreviatura());
        asignatura.setColorUi(request.getColorUi());
        asignatura.setExigeIdoneidadEstricta(
                request.getExigeIdoneidadEstricta());
        asignatura.setEsMediaTecnica(
                request.getEsMediaTecnica());
        asignatura.setRequiereDocenteExclusivo(
                request.getRequiereDocenteExclusivo());
        asignatura.setTipoAulaRequerida(
                request.getTipoAulaRequerida());
        asignatura.setMaxClasesConsecutivas(
                request.getMaxClasesConsecutivas());
        asignatura.setActiva(
                request.getActiva());

        return convertirAResponse(
                asignaturaRepository.save(asignatura));
    }

    @Transactional(readOnly = true)
    public AsignaturaResponse consultarAsignatura(UUID id) {

        return convertirAResponse(
                obtenerAsignatura(id));
    }

    @Transactional(readOnly = true)
    public Page<AsignaturaResponse> listarAsignaturas(
            String texto,
            Boolean activa,
            UUID areaId,
            Pageable pageable) {

        Specification<Asignatura> specification =
                AsignaturaSpecification.buscarPorTexto(texto)
                        .and(AsignaturaSpecification.porEstado(activa))
                        .and(AsignaturaSpecification.porArea(areaId));

        return asignaturaRepository
                .findAll(specification, pageable)
                .map(this::convertirAResponse);
    }

    @Transactional
    public AsignaturaResponse actualizarAsignatura(
            UUID id,
            ActualizarAsignaturaRequest request) {

        Asignatura asignatura = obtenerAsignatura(id);

        validarCodigoDuplicadoAlActualizar(
                request.getCodigo(),
                id);

        Area area = obtenerArea(request.getAreaId());

        asignatura.setArea(area);
        asignatura.setNombre(request.getNombre());
        asignatura.setCodigo(request.getCodigo());
        asignatura.setAbreviatura(request.getAbreviatura());
        asignatura.setColorUi(request.getColorUi());
        asignatura.setExigeIdoneidadEstricta(
                request.getExigeIdoneidadEstricta());
        asignatura.setEsMediaTecnica(
                request.getEsMediaTecnica());
        asignatura.setRequiereDocenteExclusivo(
                request.getRequiereDocenteExclusivo());
        asignatura.setTipoAulaRequerida(
                request.getTipoAulaRequerida());
        asignatura.setMaxClasesConsecutivas(
                request.getMaxClasesConsecutivas());
        asignatura.setActiva(
                request.getActiva());

        return convertirAResponse(
                asignaturaRepository.save(asignatura));
    }

    @Transactional
    public AsignaturaResponse cambiarEstado(
            UUID id,
            boolean activa) {

        Asignatura asignatura = obtenerAsignatura(id);

        asignatura.setActiva(activa);

        return convertirAResponse(
                asignaturaRepository.save(asignatura));
    }
}

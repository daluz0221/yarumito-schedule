package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AsignaturaResponse;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Consulta las asignaturas de un área académica.
@Service
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository) {
        this.asignaturaRepository = asignaturaRepository;
    }

    @Transactional(readOnly = true)
    public List<AsignaturaResponse> listarPorArea(UUID areaId) {
        return asignaturaRepository.findByAreaIdOrderByNombreAsc(areaId).stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private AsignaturaResponse convertirAResponse(Asignatura asignatura) {
        AsignaturaResponse response = new AsignaturaResponse();
        response.setId(asignatura.getId());
        response.setAreaId(asignatura.getArea().getId());
        response.setNombre(asignatura.getNombre());
        response.setCodigo(asignatura.getCodigo());
        response.setAbreviatura(asignatura.getAbreviatura());
        response.setColorUi(asignatura.getColorUi());
        response.setExigeIdoneidadEstricta(asignatura.isExigeIdoneidadEstricta());
        response.setEsMediaTecnica(asignatura.isEsMediaTecnica());
        response.setRequiereDocenteExclusivo(asignatura.isRequiereDocenteExclusivo());
        response.setTipoAulaRequerida(
                asignatura.getTipoAulaRequerida() == null
                        ? null
                        : asignatura.getTipoAulaRequerida().name());
        response.setMaxClasesConsecutivas(asignatura.getMaxClasesConsecutivas());
        response.setActiva(asignatura.isActiva());
        return response;
    }
}

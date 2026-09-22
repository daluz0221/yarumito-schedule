package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Consulta el catálogo de áreas académicas.
@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Transactional(readOnly = true)
    public List<AreaResponse> listarAreas() {
        return areaRepository.findByActivaTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    private AreaResponse convertirAResponse(Area area) {
        AreaResponse response = new AreaResponse();
        response.setId(area.getId());
        response.setNombre(area.getNombre());
        response.setCodigo(area.getCodigo());
        return response;
    }
}

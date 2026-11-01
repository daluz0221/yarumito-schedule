package co.edu.ieruralyarumito.backend.service;

import java.util.List;
import java.util.UUID;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;
import co.edu.ieruralyarumito.backend.repository.PlanEstudiosRepository;
import co.edu.ieruralyarumito.backend.entity.AnioEscolar;
import co.edu.ieruralyarumito.backend.entity.Grado;
import co.edu.ieruralyarumito.backend.entity.Grupo;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import co.edu.ieruralyarumito.backend.dto.ResumenAsignacionAcademicaResponse;
import org.springframework.stereotype.Service;

@Service
public class AsignacionAcademicaService {

    private final AnioEscolarRepository anioEscolarRepository;
    private final GrupoRepository grupoRepository;
    private final PlanEstudiosRepository planEstudiosRepository;

    public AsignacionAcademicaService(AnioEscolarRepository anioEscolarRepository, 
                                      GrupoRepository grupoRepository,
                                      PlanEstudiosRepository planEstudiosRepository ) {
        this.anioEscolarRepository = anioEscolarRepository;
        this.grupoRepository = grupoRepository;
        this.planEstudiosRepository = planEstudiosRepository;
    }

    public ResumenAsignacionAcademicaResponse generateResumenAsignacionAcademica(UUID anioEscolarId, UUID grupoId) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() -> new IllegalArgumentException("Año escolar no encontrado"));
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));

        Grado grado = grupo.getGrado();

        List<PlanEstudios> planEstudiosList = planEstudiosRepository.findByAnioEscolarAndGrado(anioEscolar.getId(), grado.getId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron planes de estudio para el año escolar y grupo especificados"));

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
                estado
        );
    }
}

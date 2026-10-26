package co.edu.ieruralyarumito.backend.service;

import java.util.List;
import co.edu.ieruralyarumito.backend.repository.AsignacionAcademicaRepository;
import co.edu.ieruralyarumito.backend.entity.PlanEstudios;
import co.edu.ieruralyarumito.backend.repository.AnioEscolarRepository;
import co.edu.ieruralyarumito.backend.repository.GrupoRepository;



@Service
public class AsignacionAcademicaService {

    private final AsignacionAcademicaRepository asignacionAcademicaRepository;
    private final AnioEscolarRepository anioEscolarRepository;
    private final GrupoRepository grupoRepository;

    public AsignacionAcademicaService(AsignacionAcademicaRepository asignacionAcademicaRepository, 
                                      AnioEscolarRepository anioEscolarRepository, GrupoRepository grupoRepository) {
        this.asignacionAcademicaRepository = asignacionAcademicaRepository;
        this.anioEscolarRepository = anioEscolarRepository;
        this.grupoRepository = grupoRepository;
    }

    public ResumenAsignacionAcademicaResponse generateResumenAsignacionAcademica(UUID anioEscolarId, UUID grupoId) {
        AnioEscolar anioEscolar = anioEscolarRepository.findById(anioEscolarId)
                .orElseThrow(() -> new IllegalArgumentException("Año escolar no encontrado"));
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));

        List<PlanEstudios> planEstudiosList = asignacionAcademicaRepository.findByAnioEscolarAndGrupo(anioEscolar, grupo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron planes de estudio para el año escolar y grupo especificados"));

        int horasRequeridas = planEstudiosList.stream()
                .mapToInt(PlanEstudios::getHorasSemanales)
                .sum();



        return new ResumenAsignacionAcademicaResponse(
                grupo.getNombre(),
                grupo.getGrado(),
                horasRequeridas,
                grupo.getHorasAsignadas(),
                grupo.getEstado()
        );
    }
}

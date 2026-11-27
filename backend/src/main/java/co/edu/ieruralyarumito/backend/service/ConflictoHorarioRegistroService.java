package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.dto.DetalleConflicto;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import org.springframework.stereotype.Service;

@Service
public class ConflictoHorarioRegistroService {

    private final ConflictoService conflictoService;

    public ConflictoHorarioRegistroService(
            ConflictoService conflictoService) {

        this.conflictoService = conflictoService;
    }

    public ConflictoResponse registrarDocenteOcupado(
            Horario horario,
            String mensaje,
            DetalleConflicto detalle) {

        return conflictoService.registrarConflicto(
                horario,
                TipoConflicto.DOCENTE_OCUPADO,
                mensaje,
                detalle
        );
    }

    public ConflictoResponse registrarAulaOcupada(
            Horario horario,
            String mensaje,
            DetalleConflicto detalle) {

        return conflictoService.registrarConflicto(
                horario,
                TipoConflicto.AULA_OCUPADA,
                mensaje,
                detalle
        );
    }

    public ConflictoResponse registrarCapacidadAula(
            Horario horario,
            String mensaje,
            DetalleConflicto detalle) {

        return conflictoService.registrarConflicto(
                horario,
                TipoConflicto.CAPACIDAD_AULA,
                mensaje,
                detalle
        );
    }
}

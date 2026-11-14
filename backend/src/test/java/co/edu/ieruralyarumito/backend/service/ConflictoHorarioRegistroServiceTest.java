package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConflictoHorarioRegistroServiceTest {

    @Mock
    private ConflictoService conflictoService;

    private ConflictoHorarioRegistroService registroService;

    @BeforeEach
    void setUp() {
        registroService =
                new ConflictoHorarioRegistroService(
                        conflictoService
                );
    }

    @Test
    void registrarDocenteOcupado_debeDelegarEnConflictoService() {

        Horario horario = new Horario();
        String mensaje = "Docente ocupado";

        registroService.registrarDocenteOcupado(
                horario,
                mensaje
        );

        verify(conflictoService)
                .registrarConflicto(
                        horario,
                        TipoConflicto.DOCENTE_OCUPADO,
                        mensaje
                );
    }

    @Test
    void registrarAulaOcupada_debeDelegarEnConflictoService() {

        Horario horario = new Horario();
        String mensaje = "Aula ocupada";

        registroService.registrarAulaOcupada(
                horario,
                mensaje
        );

        verify(conflictoService)
                .registrarConflicto(
                        horario,
                        TipoConflicto.AULA_OCUPADA,
                        mensaje
                );
    }

    @Test
    void registrarCapacidadAula_debeDelegarEnConflictoService() {

        Horario horario = new Horario();
        String mensaje = "Capacidad del aula superada";

        registroService.registrarCapacidadAula(
                horario,
                mensaje
        );

        verify(conflictoService)
                .registrarConflicto(
                        horario,
                        TipoConflicto.CAPACIDAD_AULA,
                        mensaje
                );
    }
}

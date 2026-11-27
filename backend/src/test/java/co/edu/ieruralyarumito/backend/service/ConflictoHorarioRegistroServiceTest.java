package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ConflictoResponse;
import co.edu.ieruralyarumito.backend.entity.Horario;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
                mensaje,
                null
        );

        verify(conflictoService)
                .registrarConflicto(
                        eq(horario),
                        eq(TipoConflicto.DOCENTE_OCUPADO),
                        eq(mensaje),
                        isNull()
                );
    }

    @Test
    void registrarAulaOcupada_debeDelegarEnConflictoService() {

        Horario horario = new Horario();
        String mensaje = "Aula ocupada";

        registroService.registrarAulaOcupada(
                horario,
                mensaje,
                null
        );

        verify(conflictoService)
                .registrarConflicto(
                        eq(horario),
                        eq(TipoConflicto.AULA_OCUPADA),
                        eq(mensaje),
                        isNull()
                );
    }

    @Test
    void registrarCapacidadAula_debeDelegarEnConflictoService() {

        Horario horario = new Horario();
        String mensaje = "Capacidad del aula superada";

        registroService.registrarCapacidadAula(
                horario,
                mensaje,
                null
        );

        verify(conflictoService)
                .registrarConflicto(
                        eq(horario),
                        eq(TipoConflicto.CAPACIDAD_AULA),
                        eq(mensaje),
                        isNull()
                );
    }
}

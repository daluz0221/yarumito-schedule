package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.ActualizarAulaRequest;
import co.edu.ieruralyarumito.backend.dto.AulaResponse;
import co.edu.ieruralyarumito.backend.dto.CrearAulaRequest;
import co.edu.ieruralyarumito.backend.entity.Aula;
import co.edu.ieruralyarumito.backend.entity.Sede;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.exception.CambioAulaInvalidoException;
import co.edu.ieruralyarumito.backend.exception.RecursoDuplicadoException;
import co.edu.ieruralyarumito.backend.exception.RecursoNoEncontradoException;
import co.edu.ieruralyarumito.backend.repository.AulaRepository;
import co.edu.ieruralyarumito.backend.repository.SedeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private SedeRepository sedeRepository;

    private AulaService aulaService;

    @BeforeEach
    void setUp() {

        aulaService =
                new AulaService(
                        aulaRepository,
                        sedeRepository);
    }

    @Test
    void debeRegistrarAulaActivaPorDefecto() {

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
                        sedeId,
                        "Aula 101"))
                .thenReturn(false);

        when(aulaRepository.save(any(Aula.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AulaResponse response =
                aulaService.registrarAula(request);

        assertTrue(response.isActiva());

        assertEquals(
                sedeId,
                response.getSedeId());

        assertEquals(
                "Aula 101",
                response.getNombre());

        assertEquals(
                TipoAulaRequerida.AULA,
                response.getTipo());

        assertEquals(
                30,
                response.getCapacidad());

        ArgumentCaptor<Aula> captor =
                ArgumentCaptor.forClass(Aula.class);

        verify(aulaRepository)
                .save(captor.capture());

        Aula aulaGuardada =
                captor.getValue();

        assertTrue(
                aulaGuardada.isActiva());
    }

    @Test
    void debePermitirCapacidadNula() {

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Laboratorio 1",
                        TipoAulaRequerida.LABORATORIO,
                        null);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
                        sedeId,
                        "Laboratorio 1"))
                .thenReturn(false);

        when(aulaRepository.save(any(Aula.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AulaResponse response =
                aulaService.registrarAula(request);

        assertNull(
                response.getCapacidad());
    }

    @Test
    void debeRechazarSedeInexistente() {

        UUID sedeId =
                UUID.randomUUID();

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        aulaService
                                .registrarAula(request));

        verify(aulaRepository, never())
                .save(any());
    }

    @Test
    void debeRechazarAulaActivaDuplicadaEnMismaSede() {

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
                        sedeId,
                        "Aula 101"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        aulaService
                                .registrarAula(request));

        verify(aulaRepository, never())
                .save(any());
    }

    @Test
    void debeNormalizarNombreAlCrear() {

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "   Aula    101   ",
                        TipoAulaRequerida.AULA,
                        30);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrue(
                        sedeId,
                        "Aula 101"))
                .thenReturn(false);

        when(aulaRepository.save(any(Aula.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AulaResponse response =
                aulaService.registrarAula(request);

        assertEquals(
                "Aula 101",
                response.getNombre());
    }

    @Test
    void debeCambiarDeActivaAInactiva() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        false);

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        AulaResponse response =
                aulaService.actualizarAula(
                        aulaId,
                        request);

        assertFalse(
                response.isActiva());
    }

    @Test
    void debeCambiarDeInactivaAActiva() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        false);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Aula 101",
                        aulaId))
                .thenReturn(false);

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        AulaResponse response =
                aulaService.actualizarAula(
                        aulaId,
                        request);

        assertTrue(
                response.isActiva());
    }

    @Test
    void debePermitirActivaAActiva() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        35,
                        true);

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Aula 101",
                        aulaId))
                .thenReturn(false);

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        AulaResponse response =
                aulaService.actualizarAula(
                        aulaId,
                        request);

        assertTrue(
                response.isActiva());

        assertEquals(
                35,
                response.getCapacidad());
    }

    @Test
    void debePermitirInactivaAInactiva() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        false);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        false);

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        AulaResponse response =
                aulaService.actualizarAula(
                        aulaId,
                        request);

        assertFalse(
                response.isActiva());
    }

    @Test
    void debeRechazarReactivacionCuandoExisteOtraAulaActivaEquivalente() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        false);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Aula 101",
                        aulaId))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () ->
                        aulaService
                                .actualizarAula(
                                        aulaId,
                                        request));

        verify(aulaRepository, never())
                .save(any());
    }

    @Test
    void debeActualizarCapacidadSobreElMismoRegistro() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        25,
                        true);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        40,
                        true);

        when(aulaRepository.findById(aulaId))
                .thenReturn(Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Aula 101",
                        aulaId))
                .thenReturn(false);

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        AulaResponse response =
                aulaService.actualizarAula(
                        aulaId,
                        request);

        assertEquals(
                40,
                response.getCapacidad());

        verify(aulaRepository)
                .save(aula);
    }

    @Test
    void debeListarAulasPorSede() {

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula1 =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        Aula aula2 =
                crearAula(
                        sede,
                        "Laboratorio 1",
                        TipoAulaRequerida.LABORATORIO,
                        null,
                        true);

        when(sedeRepository.findById(sedeId))
                .thenReturn(Optional.of(sede));

        when(aulaRepository.findBySede_Id(sedeId))
                .thenReturn(
                        List.of(
                                aula1,
                                aula2));

        List<AulaResponse> resultado =
                aulaService.listarPorSede(
                        sedeId);

        assertEquals(
                2,
                resultado.size());

        assertEquals(
                "Aula 101",
                resultado.get(0).getNombre());

        assertEquals(
                "Laboratorio 1",
                resultado.get(1).getNombre());
    }

    @Test
    void debeCrearNuevaVersionCuandoExisteCambioReal() {

        UUID aulaAnteriorId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSedeConId(
                        sedeId);

        Aula aulaAnterior =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Aula 102",
                        TipoAulaRequerida.AULA,
                        30);

        when(aulaRepository.findById(aulaAnteriorId))
                .thenReturn(
                        Optional.of(aulaAnterior));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Aula 102",
                        aulaAnteriorId))
                .thenReturn(false);

        when(aulaRepository.save(any(Aula.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AulaResponse response =
                aulaService.registrarCambioReal(
                        aulaAnteriorId,
                        request);

        ArgumentCaptor<Aula> captor =
                ArgumentCaptor.forClass(Aula.class);

        verify(aulaRepository, times(2))
                .save(captor.capture());

        List<Aula> aulasGuardadas =
                captor.getAllValues();

        Aula registroAnterior =
                aulasGuardadas.get(0);

        Aula registroNuevo =
                aulasGuardadas.get(1);

        assertFalse(
                registroAnterior.isActiva());

        assertTrue(
                registroNuevo.isActiva());

        assertNotSame(
                registroAnterior,
                registroNuevo);

        assertEquals(
                "Aula 102",
                registroNuevo.getNombre());

        assertEquals(
                "Aula 102",
                response.getNombre());

        assertTrue(
                response.isActiva());
    }

    @Test
    void debeCrearNuevaVersionCuandoCambiaElTipoDeAula() {

        UUID aulaAnteriorId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSedeConId(
                        sedeId);

        Aula aulaAnterior =
                crearAula(
                        sede,
                        "Espacio 1",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Espacio 1",
                        TipoAulaRequerida.LABORATORIO,
                        30);

        when(aulaRepository.findById(aulaAnteriorId))
                .thenReturn(
                        Optional.of(aulaAnterior));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Espacio 1",
                        aulaAnteriorId))
                .thenReturn(false);

        when(aulaRepository.save(any(Aula.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AulaResponse response =
                aulaService.registrarCambioReal(
                        aulaAnteriorId,
                        request);

        assertFalse(
                aulaAnterior.isActiva());

        assertEquals(
                TipoAulaRequerida.LABORATORIO,
                response.getTipo());

        assertTrue(
                response.isActiva());

        verify(aulaRepository, times(2))
                .save(any(Aula.class));
    }

    @Test
    void noDebeCrearNuevaVersionSiSoloCambiaLaCapacidad() {

        UUID aulaAnteriorId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSedeConId(
                        sedeId);

        Aula aulaAnterior =
                crearAula(
                        sede,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        CrearAulaRequest request =
                crearRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        40);

        when(aulaRepository.findById(aulaAnteriorId))
                .thenReturn(
                        Optional.of(aulaAnterior));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        assertThrows(
                CambioAulaInvalidoException.class,
                () ->
                        aulaService.registrarCambioReal(
                                aulaAnteriorId,
                                request));

        assertTrue(
                aulaAnterior.isActiva());

        verify(aulaRepository, never())
                .save(any(Aula.class));
    }

    @Test
    void debeBloquearEliminacionFisicaDeAulaExistente() {

        UUID aulaId =
                UUID.randomUUID();

        Aula aula =
                new Aula();

        when(aulaRepository.findById(aulaId))
                .thenReturn(
                        Optional.of(aula));

        CambioAulaInvalidoException exception =
                assertThrows(
                        CambioAulaInvalidoException.class,
                        () ->
                                aulaService.eliminarAula(
                                        aulaId));

        assertEquals(
                "No se puede eliminar físicamente el aula porque el sistema no puede garantizar que nunca haya sido utilizada",
                exception.getMessage());

        verify(aulaRepository, never())
                .delete(any(Aula.class));
    }

    @Test
    void debeRechazarEliminarAulaInexistente() {

        UUID aulaId =
                UUID.randomUUID();

        when(aulaRepository.findById(aulaId))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        aulaService.eliminarAula(
                                aulaId));

        verify(aulaRepository, never())
                .delete(any(Aula.class));
    }

    @Test
    void debeCorregirDatosSobreElMismoRegistroSinCrearNuevaVersion() {

        UUID aulaId =
                UUID.randomUUID();

        UUID sedeId =
                UUID.randomUUID();

        Sede sede =
                crearSede("SEDE PRINCIPAL");

        Aula aula =
                crearAula(
                        sede,
                        "Aula 10I",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        ActualizarAulaRequest request =
                actualizarRequest(
                        sedeId,
                        "Aula 101",
                        TipoAulaRequerida.AULA,
                        30,
                        true);

        when(aulaRepository.findById(aulaId))
                .thenReturn(
                        Optional.of(aula));

        when(sedeRepository.findById(sedeId))
                .thenReturn(
                        Optional.of(sede));

        when(aulaRepository
                .existsBySede_IdAndNombreIgnoreCaseAndActivaTrueAndIdNot(
                        sedeId,
                        "Aula 101",
                        aulaId))
                .thenReturn(false);

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        AulaResponse response =
                aulaService.actualizarAula(
                        aulaId,
                        request);

        assertEquals(
                "Aula 101",
                response.getNombre());

        verify(aulaRepository, times(1))
                .save(aula);
    }

    private CrearAulaRequest crearRequest(
            UUID sedeId,
            String nombre,
            TipoAulaRequerida tipo,
            Integer capacidad) {

        CrearAulaRequest request =
                new CrearAulaRequest();

        request.setSedeId(sedeId);
        request.setNombre(nombre);
        request.setTipo(tipo);
        request.setCapacidad(capacidad);

        return request;
    }

    private ActualizarAulaRequest actualizarRequest(
            UUID sedeId,
            String nombre,
            TipoAulaRequerida tipo,
            Integer capacidad,
            boolean activa) {

        ActualizarAulaRequest request =
                new ActualizarAulaRequest();

        request.setSedeId(sedeId);
        request.setNombre(nombre);
        request.setTipo(tipo);
        request.setCapacidad(capacidad);
        request.setActiva(activa);

        return request;
    }

    private Sede crearSede(
            String nombre) {

        Sede sede =
                new Sede();

        sede.setNombre(nombre);
        sede.setCodigo("PRINCIPAL");
        sede.setDireccion("Dirección");
        sede.setEsPrincipal(true);

        return sede;
    }

    private Sede crearSedeConId(
            UUID sedeId) {

        Sede sede =
                mock(Sede.class);

        when(sede.getId())
                .thenReturn(sedeId);

        return sede;
    }

    private Aula crearAula(
            Sede sede,
            String nombre,
            TipoAulaRequerida tipo,
            Integer capacidad,
            boolean activa) {

        Aula aula =
                new Aula();

        aula.setSede(sede);
        aula.setNombre(nombre);
        aula.setTipo(tipo);
        aula.setCapacidad(capacidad);
        aula.setActiva(activa);

        return aula;
    }
}

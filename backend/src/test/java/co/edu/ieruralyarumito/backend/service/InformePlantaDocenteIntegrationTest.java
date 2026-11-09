
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas de integración del informe de planta docente.
 *
 * Utiliza PostgreSQL 16 mediante Testcontainers.
 * Las operaciones de prueba se revierten al finalizar
 * cada método mediante @Transactional.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InformePlantaDocenteIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private InformePlantaDocenteService informeService;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void debeCalcularTotalesConDatosReales() {

        Area matematicas = crearArea("Matematicas");
        Area ciencias = crearArea("Ciencias");

        crearDocente(
                "Carlos", matematicas,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 22);

        crearDocente(
                "Ana", matematicas,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 22);

        crearDocente(
                "Luis", matematicas,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 20);

        crearDocente(
                "Marta", ciencias,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 18);

        prepararConsultaReal();

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        matematicas.getId(),
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PLANTA
                );

        assertEquals(3, resultado.cantidadDocentes());
        assertEquals(
                64,
                resultado.totalHorasSemanalesContratadas()
        );
        assertEquals(3, resultado.docentes().size());

        assertTrue(resultado.docentes().stream()
                .allMatch(d ->
                        d.areaNombramiento().equals("Matematicas")));
    }

    @Test
    void debeFiltrarCorrectamentePorEstado() {

        Area area = crearArea("Lenguaje");

        crearDocente(
                "Carlos", area,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 22);

        crearDocente(
                "Ana", area,
                EstadoDocente.LICENCIA,
                TipoVinculacion.PLANTA, 20);

        crearDocente(
                "Luis", area,
                EstadoDocente.RETIRADO,
                TipoVinculacion.PLANTA, 18);

        prepararConsultaReal();

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        area.getId(),
                        EstadoDocente.ACTIVO,
                        null
                );

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(
                22,
                resultado.totalHorasSemanalesContratadas()
        );

        assertEquals(
                EstadoDocente.ACTIVO,
                resultado.docentes().get(0).estado()
        );
    }

    @Test
    void debeFiltrarPorTipoDeVinculacion() {

        Area area = crearArea("Sociales");

        crearDocente(
                "Carlos", area,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 22);

        crearDocente(
                "Ana", area,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PROVISIONAL, 20);

        crearDocente(
                "Luis", area,
                EstadoDocente.ACTIVO,
                TipoVinculacion.CONTRATO, 18);

        prepararConsultaReal();

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        area.getId(),
                        null,
                        TipoVinculacion.PROVISIONAL
                );

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(
                20,
                resultado.totalHorasSemanalesContratadas()
        );
    }

    @Test
    void debeInterpretarFiltrosNulosComoTodos() {

        Area matematicas = crearArea("Matematicas");
        Area ciencias = crearArea("Ciencias");

        crearDocente(
                "Carlos", matematicas,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 22);

        crearDocente(
                "Ana", ciencias,
                EstadoDocente.LICENCIA,
                TipoVinculacion.PROVISIONAL, 20);

        prepararConsultaReal();

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        null, null, null
                );

        assertEquals(2, resultado.cantidadDocentes());
        assertEquals(
                42,
                resultado.totalHorasSemanalesContratadas()
        );
    }

    @Test
    void debeRetornarCerosSinCoincidencias() {

        Area matematicas = crearArea("Matematicas");
        Area ciencias = crearArea("Ciencias");

        crearDocente(
                "Carlos", matematicas,
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA, 22);

        prepararConsultaReal();

        InformePlantaDocenteResponse resultado =
                informeService.generarInforme(
                        ciencias.getId(),
                        null,
                        null
                );

        assertEquals(0, resultado.cantidadDocentes());
        assertEquals(
                0,
                resultado.totalHorasSemanalesContratadas()
        );
        assertTrue(resultado.docentes().isEmpty());
    }

    /**
     * Persiste un área válida para las pruebas.
     */
    private Area crearArea(String nombre) {

        Area area = new Area();

        area.setNombre(nombre);
        area.setCodigo(
                "T-" + UUID.randomUUID()
                        .toString().substring(0, 8)
        );
        area.setObligatoria(true);
        area.setSoloMedia(false);
        area.setActiva(true);

        return areaRepository.saveAndFlush(area);
    }

    /**
     * Persiste un docente de prueba asociado a un área.
     */
    private Docente crearDocente(
            String nombre,
            Area area,
            EstadoDocente estado,
            TipoVinculacion vinculacion,
            int horas) {

        Docente docente = new Docente();

        docente.setNombres(nombre);
        docente.setApellidos("Prueba");

        docente.setTipoDocumento("CC");
        docente.setNumeroDocumento(
                UUID.randomUUID().toString()
        );

        docente.setAreaNombramiento(area);
        docente.setEstado(estado);
        docente.setTipoVinculacion(vinculacion);
        docente.setHorasSemanalesContratadas(horas);

        return docenteRepository.saveAndFlush(docente);
    }

    /**
     * Fuerza a JPA a recuperar nuevamente los
     * registros desde PostgreSQL durante la consulta.
     */
    private void prepararConsultaReal() {

        entityManager.flush();
        entityManager.clear();
    }
}

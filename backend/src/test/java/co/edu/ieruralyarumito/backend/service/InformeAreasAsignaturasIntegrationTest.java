
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.annotation.DirtiesContext;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas de integración para el informe de áreas
 * y asignaturas institucionales.
 *
 * Utiliza PostgreSQL temporal mediante Testcontainers.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InformeAreasAsignaturasIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private InformeAreasAsignaturasService informeService;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private AsignaturaRepository asignaturaRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * La migración V31 carga el catálogo institucional (8 áreas y 24
     * asignaturas). Estas pruebas validan el informe con datos propios,
     * así que se parte de un catálogo vacío. El borrado ocurre dentro de
     * la transacción de la prueba y se revierte al finalizar.
     */
    @BeforeEach
    void limpiarCatalogoAcademico() {

        asignaturaRepository.deleteAllInBatch();
        areaRepository.deleteAllInBatch();
    }

    @Test
    void debeContarAreasYAsignaturasReales() {

        Area matematicas = crearArea("Matematicas", true);
        Area ciencias = crearArea("Ciencias", true);

        crearAsignatura("Algebra", matematicas, true);
        crearAsignatura("Geometria", matematicas, true);
        crearAsignatura("Estadistica", matematicas, true);

        crearAsignatura("Biologia", ciencias, true);
        crearAsignatura("Quimica", ciencias, true);

        prepararConsultaReal();

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, null);

        assertEquals(2, resultado.cantidadAreas());
        assertEquals(5, resultado.cantidadAsignaturas());

        assertEquals(2, resultado.areas().size());
    }

    @Test
    void debeIncluirAreasSinAsignaturas() {

        crearArea("Filosofia", true);

        prepararConsultaReal();

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, null);

        assertEquals(1, resultado.cantidadAreas());
        assertEquals(0, resultado.cantidadAsignaturas());

        assertEquals(
                "Filosofia",
                resultado.areas().get(0).nombre()
        );

        assertTrue(
                resultado.areas().get(0).asignaturas().isEmpty()
        );
    }

    @Test
    void debeFiltrarPorEstadoDelArea() {

        Area activa = crearArea("Matematicas", true);
        Area inactiva = crearArea("Area Historica", false);

        crearAsignatura("Algebra", activa, true);
        crearAsignatura("Asignatura Historica", inactiva, false);

        prepararConsultaReal();

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(null, true);

        assertEquals(1, resultado.cantidadAreas());
        assertEquals(1, resultado.cantidadAsignaturas());

        assertEquals(
                activa.getId(),
                resultado.areas().get(0).areaId()
        );

        assertTrue(resultado.areas().get(0).activa());
    }

    @Test
    void debeFiltrarPorAreaEspecifica() {

        Area matematicas = crearArea("Matematicas", true);
        Area ciencias = crearArea("Ciencias", true);

        crearAsignatura("Algebra", matematicas, true);
        crearAsignatura("Geometria", matematicas, true);
        crearAsignatura("Biologia", ciencias, true);

        prepararConsultaReal();

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(
                        ciencias.getId(),
                        null
                );

        assertEquals(1, resultado.cantidadAreas());
        assertEquals(1, resultado.cantidadAsignaturas());

        assertEquals(
                "Ciencias",
                resultado.areas().get(0).nombre()
        );

        assertEquals(
                "Biologia",
                resultado.areas().get(0)
                        .asignaturas().get(0).nombre()
        );
    }

    @Test
    void debeRetornarCerosCuandoFiltrosNoCoinciden() {

        Area matematicas = crearArea("Matematicas", true);

        crearAsignatura("Algebra", matematicas, true);

        prepararConsultaReal();

        InformeAreasAsignaturasResponse resultado =
                informeService.generarInforme(
                        matematicas.getId(),
                        false
                );

        assertEquals(0, resultado.cantidadAreas());
        assertEquals(0, resultado.cantidadAsignaturas());
        assertTrue(resultado.areas().isEmpty());
    }

    /**
     * Registra un área exclusivamente para la prueba.
     */
    private Area crearArea(String nombre, boolean activa) {

        Area area = new Area();

        area.setNombre(nombre);
        area.setCodigo("T-" + UUID.randomUUID());
        area.setActiva(activa);
        area.setObligatoria(true);
        area.setSoloMedia(false);

        return areaRepository.saveAndFlush(area);
    }

    /**
     * Registra una asignatura asociada a un área.
     */
    private Asignatura crearAsignatura(
            String nombre,
            Area area,
            boolean activa) {

        Asignatura asignatura = new Asignatura();

        asignatura.setNombre(nombre);
        asignatura.setCodigo("T-" + UUID.randomUUID());
        asignatura.setArea(area);
        asignatura.setActiva(activa);

        asignatura.setExigeIdoneidadEstricta(false);
        asignatura.setEsMediaTecnica(false);
        asignatura.setRequiereDocenteExclusivo(false);
        asignatura.setMaxClasesConsecutivas(2);

        return asignaturaRepository.saveAndFlush(asignatura);
    }

    /**
     * Vacía el contexto de persistencia para obligar
     * a que las consultas recuperen datos de PostgreSQL.
     */
    private void prepararConsultaReal() {

        entityManager.flush();
        entityManager.clear();
    }
}

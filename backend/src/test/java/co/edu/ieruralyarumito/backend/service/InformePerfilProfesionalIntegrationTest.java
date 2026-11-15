
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.Docente;
import co.edu.ieruralyarumito.backend.entity.Idoneidad;
import co.edu.ieruralyarumito.backend.entity.TituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente;
import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.annotation.DirtiesContext;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas de integración del informe de perfiles
 * profesionales e idoneidades docentes.
 *
 * PostgreSQL 16 temporal mediante Testcontainers.
 * Cada prueba se revierte mediante @Transactional.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InformePerfilProfesionalIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private InformePerfilProfesionalService informeService;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private AsignaturaRepository asignaturaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private TituloProfesionalRepository tituloRepository;

    @Autowired
    private IdoneidadRepository idoneidadRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void debeContarTitulosEIdoneidadesSinMultiplicarlos() {

        Area humanidades = crearArea("Humanidades");
        Area sociales = crearArea("Sociales");

        Docente docente = crearDocente("Juan", humanidades);

        TituloProfesional licenciatura = crearTitulo(
                docente,
                "Licenciatura en Literatura",
                NivelTituloProfesional.LICENCIATURA
        );

        crearTitulo(
                docente,
                "Especializacion en Educacion",
                NivelTituloProfesional.ESPECIALIZACION
        );

        crearTitulo(
                docente,
                "Maestria en Educacion",
                NivelTituloProfesional.MAESTRIA
        );

        crearIdoneidad(
                docente, humanidades,
                TipoIdoneidad.PRINCIPAL, licenciatura,
                null, LocalDate.now().minusYears(2), null
        );

        crearIdoneidad(
                docente, sociales,
                TipoIdoneidad.AUTORIZADA, licenciatura,
                null, LocalDate.now().minusYears(1), null
        );

        crearIdoneidad(
                docente, sociales,
                TipoIdoneidad.EXCEPCIONAL, null,
                null, LocalDate.now().minusMonths(3), null
        );

        crearIdoneidad(
                docente, humanidades,
                TipoIdoneidad.AUTORIZADA, licenciatura,
                null, LocalDate.now().minusMonths(1), null
        );

        prepararConsultaReal();

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(3, resultado.cantidadTitulos());
        assertEquals(4, resultado.cantidadIdoneidades());

        assertEquals(
                3, resultado.docentes().get(0).titulos().size()
        );

        assertEquals(
                4, resultado.docentes().get(0).idoneidades().size()
        );
    }

    @Test
    void debeFiltrarPorAreaDeIdoneidadYTipo() {

        Area humanidades = crearArea("Humanidades");
        Area sociales = crearArea("Sociales");

        Docente docente = crearDocente("Ana", humanidades);

        crearIdoneidad(
                docente, humanidades,
                TipoIdoneidad.PRINCIPAL, null,
                null, LocalDate.now().minusYears(1), null
        );

        crearIdoneidad(
                docente, sociales,
                TipoIdoneidad.AUTORIZADA, null,
                null, LocalDate.now().minusMonths(6), null
        );

        crearIdoneidad(
                docente, sociales,
                TipoIdoneidad.EXCEPCIONAL, null,
                null, LocalDate.now().minusMonths(2), null
        );

        prepararConsultaReal();

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(
                        null,
                        sociales.getId(),
                        TipoIdoneidad.AUTORIZADA
                );

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(1, resultado.cantidadIdoneidades());

        var detalle = resultado.docentes().get(0);

        assertEquals(
                humanidades.getNombre(),
                detalle.areaNombramiento()
        );

        assertEquals(
                sociales.getId(),
                detalle.idoneidades().get(0).areaId()
        );

        assertEquals(
                TipoIdoneidad.AUTORIZADA,
                detalle.idoneidades().get(0).tipo()
        );
    }

    @Test
    void debeFiltrarPorDocenteEspecifico() {

        Area matematicas = crearArea("Matematicas");

        Docente carlos = crearDocente("Carlos", matematicas);
        Docente ana = crearDocente("Ana", matematicas);

        crearTitulo(
                carlos,
                "Licenciatura en Matematicas",
                NivelTituloProfesional.LICENCIATURA
        );

        crearTitulo(
                ana,
                "Maestria en Educacion",
                NivelTituloProfesional.MAESTRIA
        );

        crearIdoneidad(
                carlos, matematicas,
                TipoIdoneidad.PRINCIPAL, null,
                null, LocalDate.now().minusYears(1), null
        );

        crearIdoneidad(
                ana, matematicas,
                TipoIdoneidad.PRINCIPAL, null,
                null, LocalDate.now().minusYears(1), null
        );

        prepararConsultaReal();

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(
                        carlos.getId(),
                        null,
                        null
                );

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(1, resultado.cantidadTitulos());
        assertEquals(1, resultado.cantidadIdoneidades());

        assertEquals(
                carlos.getId(),
                resultado.docentes().get(0).docenteId()
        );
    }

    @Test
    void debeDiferenciarVigentesFuturasYVencidas() {

        Area area = crearArea("Ciencias");

        Docente docente = crearDocente("Marta", area);

        LocalDate hoy = LocalDate.now();

        crearIdoneidad(
                docente, area,
                TipoIdoneidad.PRINCIPAL, null,
                null, hoy.minusDays(30), null
        );

        crearIdoneidad(
                docente, area,
                TipoIdoneidad.AUTORIZADA, null,
                null, hoy.plusDays(10), null
        );

        crearIdoneidad(
                docente, area,
                TipoIdoneidad.EXCEPCIONAL, null,
                null, hoy.minusDays(30), hoy.minusDays(1)
        );

        prepararConsultaReal();

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(3, resultado.cantidadIdoneidades());

        long vigentes = resultado.docentes().get(0)
                .idoneidades().stream()
                .filter(i -> i.vigenteEnFecha())
                .count();

        assertEquals(1, vigentes);
        assertEquals(hoy, resultado.fechaReferencia());
    }

    @Test
    void debeAdmitirAsignaturaYTituloSoporteOpcionales() {

        Area ciencias = crearArea("Ciencias");

        Docente docente = crearDocente("Luis", ciencias);

        Asignatura biologia = crearAsignatura(
                "Biologia", ciencias
        );

        TituloProfesional titulo = crearTitulo(
                docente,
                "Licenciatura en Biologia",
                NivelTituloProfesional.LICENCIATURA
        );

        Idoneidad especifica = crearIdoneidad(
                docente, ciencias,
                TipoIdoneidad.PRINCIPAL,
                titulo,
                biologia,
                LocalDate.now().minusYears(1),
                null
        );

        Idoneidad general = crearIdoneidad(
                docente, ciencias,
                TipoIdoneidad.AUTORIZADA,
                null,
                null,
                LocalDate.now().minusMonths(1),
                null
        );

        prepararConsultaReal();

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(1, resultado.cantidadTitulos());
        assertEquals(2, resultado.cantidadIdoneidades());

        var detalles = resultado.docentes().get(0).idoneidades();

        var detalleEspecifico = detalles.stream()
                .filter(i -> i.idoneidadId().equals(especifica.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(
                biologia.getId(),
                detalleEspecifico.asignaturaId()
        );

        assertEquals(
                "Biologia",
                detalleEspecifico.nombreAsignatura()
        );

        assertEquals(
                titulo.getId(),
                detalleEspecifico.tituloSoporteId()
        );

        var detalleGeneral = detalles.stream()
                .filter(i -> i.idoneidadId().equals(general.getId()))
                .findFirst()
                .orElseThrow();

        assertNull(detalleGeneral.asignaturaId());
        assertNull(detalleGeneral.nombreAsignatura());
        assertNull(detalleGeneral.tituloSoporteId());
    }

    @Test
    void debeConservarDocentesSinIdoneidadesYSinTitulos() {

        Area area = crearArea("Filosofia");

        Docente docente = crearDocente("Pedro", area);

        prepararConsultaReal();

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(0, resultado.cantidadTitulos());
        assertEquals(0, resultado.cantidadIdoneidades());

        assertEquals(
                docente.getId(),
                resultado.docentes().get(0).docenteId()
        );

        assertTrue(
                resultado.docentes().get(0).titulos().isEmpty()
        );

        assertTrue(
                resultado.docentes().get(0).idoneidades().isEmpty()
        );
    }

    private Area crearArea(String nombre) {

        Area area = new Area();

        area.setNombre(nombre);
        area.setCodigo(
                "T-" + UUID.randomUUID().toString().substring(0, 8)
        );
        area.setObligatoria(true);
        area.setSoloMedia(false);
        area.setActiva(true);

        return areaRepository.saveAndFlush(area);
    }

    private Docente crearDocente(String nombre, Area area) {

        Docente docente = new Docente();

        docente.setNombres(nombre);
        docente.setApellidos("Prueba");
        docente.setTipoDocumento("CC");
        docente.setNumeroDocumento(
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 20)
        );

        docente.setAreaNombramiento(area);
        docente.setEstado(EstadoDocente.ACTIVO);
        docente.setTipoVinculacion(TipoVinculacion.PLANTA);
        docente.setHorasSemanalesContratadas(22);

        return docenteRepository.saveAndFlush(docente);
    }

    private TituloProfesional crearTitulo(
            Docente docente,
            String nombre,
            NivelTituloProfesional nivel) {

        TituloProfesional titulo = new TituloProfesional();

        titulo.setDocente(docente);
        titulo.setNombreTitulo(nombre);
        titulo.setNivel(nivel);
        titulo.setInstitucion("Institucion de Prueba");
        titulo.setAnioGraduacion(2020);

        return tituloRepository.saveAndFlush(titulo);
    }

    private Asignatura crearAsignatura(
            String nombre,
            Area area) {

        Asignatura asignatura = new Asignatura();

        asignatura.setNombre(nombre);
        asignatura.setCodigo(
                "T-" + UUID.randomUUID().toString().substring(0, 8)
        );
        asignatura.setArea(area);
        asignatura.setActiva(true);
        asignatura.setExigeIdoneidadEstricta(false);
        asignatura.setEsMediaTecnica(false);
        asignatura.setRequiereDocenteExclusivo(false);
        asignatura.setMaxClasesConsecutivas(2);

        return asignaturaRepository.saveAndFlush(asignatura);
    }

    private Idoneidad crearIdoneidad(
            Docente docente,
            Area area,
            TipoIdoneidad tipo,
            TituloProfesional tituloSoporte,
            Asignatura asignatura,
            LocalDate desde,
            LocalDate hasta) {

        Idoneidad idoneidad = new Idoneidad();

        idoneidad.setDocente(docente);
        idoneidad.setArea(area);
        idoneidad.setTipo(tipo);
        idoneidad.setTituloSoporte(tituloSoporte);
        idoneidad.setAsignatura(asignatura);
        idoneidad.setVigenteDesde(desde);
        idoneidad.setVigenteHasta(hasta);

        return idoneidadRepository.saveAndFlush(idoneidad);
    }

    /**
     * Obliga a recuperar las entidades de PostgreSQL,
     * en vez de depender del contexto JPA anterior.
     */
    private void prepararConsultaReal() {

        entityManager.flush();
        entityManager.clear();
    }
}

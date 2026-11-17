
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
import co.edu.ieruralyarumito.backend.repository.DocenteRepository;
import co.edu.ieruralyarumito.backend.repository.IdoneidadRepository;
import co.edu.ieruralyarumito.backend.repository.TituloProfesionalRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU-04 / SCRUM-31.
 *
 * Pruebas unitarias del informe de perfiles
 * profesionales e idoneidades docentes.
 *
 * Sin base de datos y sin modificar entidades.
 */
@ExtendWith(MockitoExtension.class)
class InformePerfilProfesionalServiceTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private TituloProfesionalRepository tituloRepository;

    @Mock
    private IdoneidadRepository idoneidadRepository;

    @InjectMocks
    private InformePerfilProfesionalService informeService;

    private Docente docente;
    private Area areaNombramiento;
    private Area areaIdoneidad;

    @BeforeEach
    void preparar() {
        areaNombramiento = crearArea("Humanidades");
        areaIdoneidad = crearArea("Ciencias Sociales");
        docente = crearDocente("Juan", "Perez", areaNombramiento);
    }

    @Test
    void debeContarTitulosEIdoneidadesSinMultiplicarlos() {

        TituloProfesional titulo1 = crearTitulo(
                "Licenciatura en Literatura",
                NivelTituloProfesional.LICENCIATURA);

        TituloProfesional titulo2 = crearTitulo(
                "Especializacion en Educacion",
                NivelTituloProfesional.ESPECIALIZACION);

        TituloProfesional titulo3 = crearTitulo(
                "Maestria en Educacion",
                NivelTituloProfesional.MAESTRIA);

        Idoneidad idoneidad1 = crearIdoneidad(
                areaNombramiento, TipoIdoneidad.PRINCIPAL);

        Idoneidad idoneidad2 = crearIdoneidad(
                areaIdoneidad, TipoIdoneidad.AUTORIZADA);

        Idoneidad idoneidad3 = crearIdoneidad(
                areaIdoneidad, TipoIdoneidad.EXCEPCIONAL);

        Idoneidad idoneidad4 = crearIdoneidad(
                areaNombramiento, TipoIdoneidad.AUTORIZADA);

        prepararRepositorios(
                List.of(docente),
                List.of(titulo1, titulo2, titulo3),
                List.of(idoneidad1, idoneidad2,
                        idoneidad3, idoneidad4)
        );

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(3, resultado.cantidadTitulos());
        assertEquals(4, resultado.cantidadIdoneidades());

        assertEquals(3,
                resultado.docentes().get(0).titulos().size());

        assertEquals(4,
                resultado.docentes().get(0).idoneidades().size());
    }

    @Test
    void debeEvitarRegistrosDuplicados() {

        TituloProfesional titulo = crearTitulo(
                "Licenciatura en Literatura",
                NivelTituloProfesional.LICENCIATURA);

        Idoneidad idoneidad = crearIdoneidad(
                areaNombramiento,
                TipoIdoneidad.PRINCIPAL);

        prepararRepositorios(
                List.of(docente, docente),
                List.of(titulo, titulo),
                List.of(idoneidad, idoneidad)
        );

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(1, resultado.cantidadTitulos());
        assertEquals(1, resultado.cantidadIdoneidades());
    }

    @Test
    void debeFiltrarPorAreaDeIdoneidad() {

        Idoneidad principal = crearIdoneidad(
                areaNombramiento,
                TipoIdoneidad.PRINCIPAL);

        Idoneidad autorizada = crearIdoneidad(
                areaIdoneidad,
                TipoIdoneidad.AUTORIZADA);

        prepararRepositorios(
                List.of(docente),
                List.of(),
                List.of(principal, autorizada)
        );

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(
                        null,
                        areaIdoneidad.getId(),
                        null
                );

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(1, resultado.cantidadIdoneidades());

        assertEquals(
                areaIdoneidad.getId(),
                resultado.docentes().get(0)
                        .idoneidades().get(0).areaId()
        );
    }

    @Test
    void debeFiltrarPorTipoDeIdoneidad() {

        Idoneidad principal = crearIdoneidad(
                areaNombramiento,
                TipoIdoneidad.PRINCIPAL);

        Idoneidad autorizada = crearIdoneidad(
                areaIdoneidad,
                TipoIdoneidad.AUTORIZADA);

        prepararRepositorios(
                List.of(docente),
                List.of(),
                List.of(principal, autorizada)
        );

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(
                        null,
                        null,
                        TipoIdoneidad.AUTORIZADA
                );

        assertEquals(1, resultado.cantidadIdoneidades());

        assertEquals(
                TipoIdoneidad.AUTORIZADA,
                resultado.docentes().get(0)
                        .idoneidades().get(0).tipo()
        );
    }

    @Test
    void debeEvaluarCorrectamenteLasVigencias() {

        LocalDate hoy = LocalDate.now();

        Idoneidad vigente = crearIdoneidad(
                areaNombramiento,
                TipoIdoneidad.PRINCIPAL);

        Idoneidad futura = crearIdoneidad(
                areaIdoneidad,
                TipoIdoneidad.AUTORIZADA);

        Idoneidad vencida = crearIdoneidad(
                areaIdoneidad,
                TipoIdoneidad.EXCEPCIONAL);

        when(vigente.getVigenteDesde())
                .thenReturn(hoy.minusDays(10));
        when(vigente.getVigenteHasta())
                .thenReturn(null);

        when(futura.getVigenteDesde())
                .thenReturn(hoy.plusDays(10));
        when(futura.getVigenteHasta())
                .thenReturn(null);

        when(vencida.getVigenteDesde())
                .thenReturn(hoy.minusDays(30));
        when(vencida.getVigenteHasta())
                .thenReturn(hoy.minusDays(1));

        prepararRepositorios(
                List.of(docente),
                List.of(),
                List.of(vigente, futura, vencida)
        );

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(3, resultado.cantidadIdoneidades());

        long vigentes = resultado.docentes().get(0)
                .idoneidades().stream()
                .filter(i -> i.vigenteEnFecha())
                .count();

        assertEquals(1, vigentes);
    }

    @Test
    void debeConservarDocentesSinIdoneidades() {

        TituloProfesional titulo = crearTitulo(
                "Licenciatura en Literatura",
                NivelTituloProfesional.LICENCIATURA);

        prepararRepositorios(
                List.of(docente),
                List.of(titulo),
                List.of()
        );

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(1, resultado.cantidadDocentes());
        assertEquals(1, resultado.cantidadTitulos());
        assertEquals(0, resultado.cantidadIdoneidades());

        assertTrue(resultado.docentes().get(0)
                .idoneidades().isEmpty());
    }

    @Test
    void debeRetornarCerosSinDocentes() {

        when(docenteRepository.findAll()).thenReturn(List.of());

        InformePerfilProfesionalResponse resultado =
                informeService.generarInforme(null, null, null);

        assertEquals(0, resultado.cantidadDocentes());
        assertEquals(0, resultado.cantidadTitulos());
        assertEquals(0, resultado.cantidadIdoneidades());

        assertTrue(resultado.docentes().isEmpty());

        verifyNoInteractions(
                tituloRepository, idoneidadRepository);
    }

    private void prepararRepositorios(
            List<Docente> docentes,
            List<TituloProfesional> titulos,
            List<Idoneidad> idoneidades) {

        when(docenteRepository.findAll()).thenReturn(docentes);

        if (!docentes.isEmpty()) {
            when(tituloRepository.findAll()).thenReturn(titulos);
            when(idoneidadRepository.findAll()).thenReturn(idoneidades);
        }
    }

    private Docente crearDocente(
            String nombres,
            String apellidos,
            Area area) {

        Docente resultado = mock(Docente.class);

        lenient().when(resultado.getId())
                .thenReturn(UUID.randomUUID());
        lenient().when(resultado.getNombres())
                .thenReturn(nombres);
        lenient().when(resultado.getApellidos())
                .thenReturn(apellidos);
        lenient().when(resultado.getAreaNombramiento())
                .thenReturn(area);

        return resultado;
    }

    private Area crearArea(String nombre) {

        Area resultado = mock(Area.class);

        lenient().when(resultado.getId())
                .thenReturn(UUID.randomUUID());
        lenient().when(resultado.getNombre())
                .thenReturn(nombre);

        return resultado;
    }

    private TituloProfesional crearTitulo(
            String nombre,
            NivelTituloProfesional nivel) {

        TituloProfesional resultado =
                mock(TituloProfesional.class);

        lenient().when(resultado.getId())
                .thenReturn(UUID.randomUUID());
        lenient().when(resultado.getDocente())
                .thenReturn(docente);
        lenient().when(resultado.getNombreTitulo())
                .thenReturn(nombre);
        lenient().when(resultado.getNivel())
                .thenReturn(nivel);

        return resultado;
    }

    private Idoneidad crearIdoneidad(
            Area area,
            TipoIdoneidad tipo) {

        Idoneidad resultado = mock(Idoneidad.class);

        lenient().when(resultado.getId())
                .thenReturn(UUID.randomUUID());
        lenient().when(resultado.getDocente())
                .thenReturn(docente);
        lenient().when(resultado.getArea())
                .thenReturn(area);
        lenient().when(resultado.getTipo())
                .thenReturn(tipo);

        return resultado;
    }
}

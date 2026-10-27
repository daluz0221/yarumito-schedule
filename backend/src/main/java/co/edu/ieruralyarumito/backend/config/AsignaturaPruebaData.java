package co.edu.ieruralyarumito.backend.config;

import co.edu.ieruralyarumito.backend.entity.Area;
import co.edu.ieruralyarumito.backend.entity.Asignatura;
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import co.edu.ieruralyarumito.backend.repository.AreaRepository;
import co.edu.ieruralyarumito.backend.repository.AsignaturaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Crea tres asignaturas de Matemáticas si todavía no existen.
@Component
@Order(3)
public class AsignaturaPruebaData implements ApplicationRunner {

    private final AreaRepository areaRepository;
    private final AsignaturaRepository asignaturaRepository;

    public AsignaturaPruebaData(
            AreaRepository areaRepository,
            AsignaturaRepository asignaturaRepository) {
        this.areaRepository = areaRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        areaRepository.findByCodigo("MAT").ifPresent(this::asegurarAsignaturas);
    }

    private void asegurarAsignaturas(Area matematicas) {
        crear(matematicas, "ALG", "Álgebra", "ALG");
        crear(matematicas, "TRIG", "Trigonometría", "TRIG");
        crear(matematicas, "GEO", "Geometría", "GEO");
    }

    private void crear(Area area, String codigo, String nombre, String abreviatura) {
        if (asignaturaRepository.existsByCodigo(codigo)) {
            return;
        }

        Asignatura asignatura = new Asignatura();
        asignatura.setArea(area);
        asignatura.setCodigo(codigo);
        asignatura.setNombre(nombre);
        asignatura.setAbreviatura(abreviatura);
        asignatura.setExigeIdoneidadEstricta(true);
        asignatura.setEsMediaTecnica(false);
        asignatura.setRequiereDocenteExclusivo(false);
        asignatura.setTipoAulaRequerida(TipoAulaRequerida.AULA);
        asignatura.setMaxClasesConsecutivas(2);
        asignatura.setActiva(true);
        asignaturaRepository.save(asignatura);
    }
}

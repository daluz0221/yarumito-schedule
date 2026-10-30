package co.edu.ieruralyarumito.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoAsignaturaController {

    public record OpcionAsignatura(
            String codigo,
            String nombre,
            String abreviatura,
            String codigoArea) {}

    private static final List<OpcionAsignatura> ASIGNATURAS = List.of(
        new OpcionAsignatura("NAT-BIO", "Biología / Ciencias Naturales", "BIOL", "NAT"),
        new OpcionAsignatura("NAT-QUI", "Química", "QUIM", "NAT"),
        new OpcionAsignatura("NAT-FIS", "Física", "FISI", "NAT"),
        new OpcionAsignatura("NAT-AMB", "Educación Ambiental y Ecológica", "AMBI", "NAT"),

        new OpcionAsignatura("SOC-SOC", "Ciencias Sociales", "SOCI", "SOC"),
        new OpcionAsignatura("SOC-POL", "Ciencias Económicas y Políticas", "POLI", "SOC"),
        new OpcionAsignatura("SOC-CIV", "Competencias Ciudadanas / Cátedra de la Paz", "CIVI", "SOC"),

        new OpcionAsignatura("HUM-CAS", "Lengua Castellana y Literatura", "CAST", "HUM"),
        new OpcionAsignatura("HUM-ING", "Inglés", "INGL", "HUM"),
        new OpcionAsignatura("HUM-LEC", "Comprensión Lectora", "LECT", "HUM"),

        new OpcionAsignatura("MAT-MAT", "Matemáticas Generales", "MATE", "MAT"),
        new OpcionAsignatura("MAT-GEO", "Geometría", "GEOM", "MAT"),
        new OpcionAsignatura("MAT-EST", "Estadística", "ESTAD", "MAT"),

        new OpcionAsignatura("TEC-INF", "Informática", "INFO", "TEC"),
        new OpcionAsignatura("TEC-EMP", "Emprendimiento", "EMPRE", "TEC"),

        new OpcionAsignatura("ETI-VAL", "Ética y Valores", "ETIC", "ETI"),
        new OpcionAsignatura("ETI-REL", "Educación Religiosa", "RELI", "ETI"),
        new OpcionAsignatura("ETI-PHI", "Filosofía", "FILO", "ETI"),

        new OpcionAsignatura("WEL-ART", "Educación Artística y Cultural", "ARTE", "WEL"),
        new OpcionAsignatura("WEL-EFI", "Educación Física, Recreación y Deportes", "EDFI", "WEL"),

        new OpcionAsignatura("AGR-AGR", "Especialidad Agropecuaria General", "AGRO", "AGR"),
        new OpcionAsignatura("AGR-VEG", "Producción Vegetal y Cultivos", "VEGE", "AGR"),
        new OpcionAsignatura("AGR-TRA", "Transformación y Aprovechamiento", "TRAN", "AGR"),
        new OpcionAsignatura("AGR-TEC", "Tecnología e Innovación Agrícola", "TECA", "AGR")
    );

    @GetMapping("/asignaturas")
    public List<OpcionAsignatura> listarAsignaturas(
            @RequestParam(required = false) String area) {

        if (area == null || area.isBlank()) {
            return ASIGNATURAS;
        }

        return ASIGNATURAS.stream()
                .filter(asignatura ->
                        asignatura.codigoArea().equalsIgnoreCase(area.trim()))
                .toList();
    }
}

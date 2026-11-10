package co.edu.ieruralyarumito.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoEscalafonController {

    public record OpcionRegimen(
            String codigo,
            String nombre) {}

    public record OpcionEscalafon(
            String codigo,
            String nombre) {}

    @GetMapping("/regimenes-docentes")
    public List<OpcionRegimen> listarRegimenes() {
        return List.of(
            new OpcionRegimen("2277", "Decreto Ley 2277 de 1979"),
            new OpcionRegimen("1278", "Decreto Ley 1278 de 2002"),
            new OpcionRegimen("SEIP", "Régimen especial indígena - SEIP"),
            new OpcionRegimen("ETNO_AFRO",
                    "Etnoeducador afrocolombiano, raizal o palenquero"),
            new OpcionRegimen("NO_APLICA", "Sin escalafón / No aplica")
        );
    }

    @GetMapping("/escalafones")
    public List<OpcionEscalafon> listarEscalafones(
            @RequestParam String regimen) {

        List<OpcionEscalafon> opciones = new ArrayList<>();

        switch (regimen.trim().toUpperCase()) {
            case "2277" -> {
                for (int grado = 1; grado <= 14; grado++) {
                    opciones.add(new OpcionEscalafon(
                            String.valueOf(grado),
                            "Grado " + grado));
                }
            }
            case "1278" -> {
                for (int grado = 1; grado <= 3; grado++) {
                    for (String nivel : List.of("A", "B", "C", "D")) {
                        String codigo = grado + nivel;
                        opciones.add(new OpcionEscalafon(
                                codigo, "Grado " + grado + " - Nivel " + nivel));
                    }
                }
            }
            case "SEIP" -> {
                for (String nivel : List.of(
                        "I", "II", "III", "IV", "V", "VI")) {
                    opciones.add(new OpcionEscalafon(
                            nivel, "Nivel " + nivel));
                }
            }
            case "ETNO_AFRO", "NO_APLICA" -> {
                // No se asigna automáticamente una categoría.
                // Se conserva el valor registrado conforme
                // al régimen aplicable del docente.
            }
            default -> {
                // Régimen no reconocido: lista vacía.
            }
        }

        return opciones;
    }
}

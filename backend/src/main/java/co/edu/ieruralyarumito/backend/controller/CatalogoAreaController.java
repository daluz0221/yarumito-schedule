package co.edu.ieruralyarumito.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoAreaController {

    public record OpcionArea(String codigo, String nombre) {}

    @GetMapping("/areas")
    public List<OpcionArea> listarAreasPredefinidas() {
        return List.of(
            new OpcionArea("NAT", "Ciencias Naturales y Educación Ambiental"),
            new OpcionArea("SOC", "Ciencias Sociales, Historia, Geografía y Democracia"),
            new OpcionArea("HUM", "Humanidades"),
            new OpcionArea("MAT", "Matemáticas"),
            new OpcionArea("TEC", "Tecnología, Informática y Emprendimiento"),
            new OpcionArea("ETI", "Educación Ética y Religiosa"),
            new OpcionArea("WEL", "Áreas del Bienestar"),
            new OpcionArea("AGR", "Media Técnica Agropecuaria")
        );
    }
}

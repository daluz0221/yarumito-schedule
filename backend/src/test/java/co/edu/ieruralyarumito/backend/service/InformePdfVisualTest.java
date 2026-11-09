package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.*;
import co.edu.ieruralyarumito.backend.entity.enums.*;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InformePdfVisualTest {

    private static final Path CARPETA =
            Path.of("target", "informes-pdf-visual");

    @Test
    void generarTresPdfParaRevisionVisual() throws Exception {

        Files.createDirectories(CARPETA);

        generarPlantaDocente();
        generarAreasAsignaturas();
        generarPerfilProfesional();

        assertTrue(Files.size(
                CARPETA.resolve("01-planta-docente.pdf")) > 0);

        assertTrue(Files.size(
                CARPETA.resolve("02-areas-asignaturas.pdf")) > 0);

        assertTrue(Files.size(
                CARPETA.resolve("03-perfil-profesional.pdf")) > 0);
    }

    private void generarPlantaDocente() throws Exception {

        var docente = new InformePlantaDocenteDetalleResponse(
                UUID.randomUUID(),
                "María Muñoz",
                "Matemáticas y Educación",
                EstadoDocente.ACTIVO,
                TipoVinculacion.PLANTA,
                22
        );

        var informe = new InformePlantaDocenteResponse(
                1, 22, List.of(docente)
        );

        byte[] pdf = new InformePdfService()
                .exportarPlantaDocente(
                        informe,
                        "Relación de planta docente"
                );

        Files.write(
                CARPETA.resolve("01-planta-docente.pdf"),
                pdf
        );
    }

    private void generarAreasAsignaturas() throws Exception {

        var asignatura = new InformeAsignaturaDetalleResponse(
                UUID.randomUUID(),
                "Geometría",
                "GEO",
                true,
                false,
                true,
                false
        );

        var area = new InformeAreaDetalleResponse(
                UUID.randomUUID(),
                "Matemáticas",
                "MAT",
                true,
                true,
                false,
                1,
                List.of(asignatura)
        );

        var informe = new InformeAreasAsignaturasResponse(
                1, 1, List.of(area)
        );

        byte[] pdf = new InformeAreasAsignaturasPdfService()
                .exportarAreasAsignaturas(
                        informe,
                        "Relación de áreas y asignaturas"
                );

        Files.write(
                CARPETA.resolve("02-areas-asignaturas.pdf"),
                pdf
        );
    }

    private void generarPerfilProfesional() throws Exception {

        UUID tituloId = UUID.randomUUID();

        var titulo = new InformeTituloProfesionalDetalleResponse(
                tituloId,
                "Licenciatura en Educación",
                NivelTituloProfesional.LICENCIATURA,
                "Universidad de Antioquia",
                2024
        );

        var idoneidad = new InformeIdoneidadDetalleResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Ciencias Sociales",
                UUID.randomUUID(),
                "Geografía",
                TipoIdoneidad.PRINCIPAL,
                tituloId,
                LocalDate.of(2025, 1, 1),
                null,
                true
        );

        var docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        UUID.randomUUID(),
                        "María Muñoz",
                        "Educación Artística",
                        List.of(titulo),
                        List.of(idoneidad)
                );

        var informe = new InformePerfilProfesionalResponse(
                LocalDate.of(2026, 10, 12),
                1, 1, 1,
                List.of(docente)
        );

        byte[] pdf = new InformePerfilProfesionalPdfService()
                .exportarPerfilProfesional(
                        informe,
                        "Relación de perfiles e idoneidades"
                );

        Files.write(
                CARPETA.resolve("03-perfil-profesional.pdf"),
                pdf
        );
    }
}

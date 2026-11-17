package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.*;
import co.edu.ieruralyarumito.backend.entity.enums.*;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class InformePdfConcurrenciaTest {

    private final InformePdfService planta =
            new InformePdfService();

    private final InformeAreasAsignaturasPdfService areas =
            new InformeAreasAsignaturasPdfService();

    private final InformePerfilProfesionalPdfService perfiles =
            new InformePerfilProfesionalPdfService();

    @Test
    void debeGenerarPdfSimultaneosSinMezclarInformacion()
            throws Exception {

        ExecutorService ejecutor = Executors.newFixedThreadPool(6);

        try {
            int cantidad = 18;

            CountDownLatch inicio = new CountDownLatch(1);

            List<Future<Boolean>> resultados =
                    new java.util.ArrayList<>();

            for (int i = 0; i < cantidad; i++) {

                final int numero = i;

                resultados.add(ejecutor.submit(() -> {

                    inicio.await();

                    String identificador = String.format(
                            "PRUEBACONCURRENCIA%03d", numero
                    );

                    byte[] pdf;

                    if (numero % 3 == 0) {
                        pdf = generarPlanta(identificador);
                    } else if (numero % 3 == 1) {
                        pdf = generarAreas(identificador);
                    } else {
                        pdf = generarPerfil(identificador);
                    }

                    try (PDDocument documento =
                                 Loader.loadPDF(pdf)) {

                        String texto =
                                new PDFTextStripper().getText(documento);

                        boolean contieneSuIdentificador =
                                texto.contains(identificador);

                        boolean contieneIdentificadorAjeno = false;

                        for (int otro = 0; otro < cantidad; otro++) {

                            if (otro == numero) {
                                continue;
                            }

                            String identificadorAjeno = String.format(
                                    "PRUEBACONCURRENCIA%03d", otro
                            );

                            if (texto.contains(identificadorAjeno)) {
                                contieneIdentificadorAjeno = true;
                                break;
                            }
                        }

                        return documento.getNumberOfPages() >= 1
                                && contieneSuIdentificador
                                && !contieneIdentificadorAjeno;
                    }
                }));
            }

            inicio.countDown();

            for (Future<Boolean> resultado : resultados) {
                assertTrue(
                        resultado.get(30, TimeUnit.SECONDS),
                        "Una exportacion concurrente no fue valida"
                );
            }

        } finally {
            ejecutor.shutdownNow();
        }
    }

    private byte[] generarPlanta(String identificador) {

        var docente =
                new InformePlantaDocenteDetalleResponse(
                        UUID.randomUUID(),
                        identificador,
                        "Matemáticas",
                        EstadoDocente.ACTIVO,
                        TipoVinculacion.PLANTA,
                        22
                );

        return planta.exportarPlantaDocente(
                new InformePlantaDocenteResponse(
                        1, 22, List.of(docente)
                ),
                "Informe de planta docente"
        );
    }

    private byte[] generarAreas(String identificador) {

        var asignatura =
                new InformeAsignaturaDetalleResponse(
                        UUID.randomUUID(),
                        identificador,
                        "COD",
                        true,
                        false,
                        true,
                        false
                );

        var area =
                new InformeAreaDetalleResponse(
                        UUID.randomUUID(),
                        "Matemáticas",
                        "MAT",
                        true,
                        true,
                        false,
                        1,
                        List.of(asignatura)
                );

        return areas.exportarAreasAsignaturas(
                new InformeAreasAsignaturasResponse(
                        1, 1, List.of(area)
                ),
                "Informe de áreas y asignaturas"
        );
    }

    private byte[] generarPerfil(String identificador) {

        var titulo =
                new InformeTituloProfesionalDetalleResponse(
                        UUID.randomUUID(),
                        identificador,
                        NivelTituloProfesional.LICENCIATURA,
                        "Universidad de Antioquia",
                        2024
                );

        var docente =
                new InformePerfilProfesionalResponse.DetalleDocente(
                        UUID.randomUUID(),
                        "María Muñoz",
                        "Humanidades",
                        List.of(titulo),
                        List.of()
                );

        return perfiles.exportarPerfilProfesional(
                new InformePerfilProfesionalResponse(
                        LocalDate.of(2026, 10, 12),
                        1, 1, 0,
                        List.of(docente)
                ),
                "Informe de perfiles profesionales"
        );
    }
}

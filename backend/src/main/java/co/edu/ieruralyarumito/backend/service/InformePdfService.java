package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HU-04 / SCRUM-31.
 *
 * Exportacion PDF de informes institucionales.
 *
 * Planta docente y capacidad academica.
 *
 * No consulta repositorios ni persiste archivos.
 * Los metadatos proceden de una fuente externa
 * y no se inventan dentro del exportador.
 */
@Service
public class InformePdfService {

    private static final String TITULO_PREDETERMINADO =
            "Informe de planta docente y capacidad academica";

    private static final int MAX_LONGITUD_TITULO = 100;

    private static final PDFont FUENTE =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDFont FUENTE_NEGRITA =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    private static final PDRectangle TAMANO_PAGINA =
            new PDRectangle(
                    PDRectangle.A4.getHeight(),
                    PDRectangle.A4.getWidth()
            );

    private static final float MARGEN = 42f;
    private static final float LIMITE_INFERIOR = 65f;
    private static final float ALTO_LINEA = 11f;
    private static final float RELLENO = 5f;

    private static final float[] ANCHOS = {
            196f, 156f, 83f, 125f, 114f
    };

    private static final String[] ENCABEZADOS = {
            "Docente",
            "Area de nombramiento",
            "Estado",
            "Vinculacion",
            "Horas contratadas"
    };

    /**
     * Conserva la firma utilizada por las pruebas
     * y por los consumidores existentes del servicio.
     */
    public byte[] exportarPlantaDocente(
            InformePlantaDocenteResponse informe,
            String tituloPersonalizado) {

        return exportarPlantaDocente(
                informe,
                tituloPersonalizado,
                null
        );
    }

    /**
     * Exportacion con metadatos institucionales.
     *
     * No obtiene ni inventa datos del Rector,
     * de la institucion o de la sede.
     */
    public byte[] exportarPlantaDocente(
            InformePlantaDocenteResponse informe,
            String tituloPersonalizado,
            MetadatosInformeResponse metadatos) {

        Objects.requireNonNull(
                informe,
                "El informe de planta docente es obligatorio"
        );

        String titulo = resolverTitulo(tituloPersonalizado);

        try (PDDocument documento = new PDDocument()) {

            PaginaActual actual = crearPagina(
                    documento,
                    titulo,
                    informe,
                    true
            );

            for (InformePlantaDocenteDetalleResponse docente
                    : informe.docentes()) {

                List<List<String>> celdas = List.of(
                        envolver(
                                docente.nombreCompleto(),
                                ANCHOS[0]
                        ),
                        envolver(
                                docente.areaNombramiento(),
                                ANCHOS[1]
                        ),
                        envolver(
                                docente.estado().name(),
                                ANCHOS[2]
                        ),
                        envolver(
                                docente.tipoVinculacion().name(),
                                ANCHOS[3]
                        ),
                        envolver(
                                String.valueOf(
                                        docente.horasSemanalesContratadas()
                                ),
                                ANCHOS[4]
                        )
                );

                int maximoLineas = celdas.stream()
                        .mapToInt(List::size)
                        .max()
                        .orElse(1);

                float altura = Math.max(
                        24f,
                        maximoLineas * ALTO_LINEA
                                + 2f * RELLENO
                );

                if (actual.y - altura < LIMITE_INFERIOR) {

                    actual.contenido.close();

                    actual = crearPagina(
                            documento,
                            titulo,
                            informe,
                            false
                    );
                }

                dibujarFila(
                        actual.contenido,
                        actual.y,
                        altura,
                        celdas,
                        false
                );

                actual.y -= altura;
            }

            if (informe.docentes().isEmpty()) {

                escribirTexto(
                        actual.contenido,
                        "No se encontraron docentes para los filtros aplicados.",
                        MARGEN,
                        actual.y - 15f,
                        FUENTE,
                        10f
                );

                actual.y -= 30f;
            }

            // Metadatos al final del mismo documento.
            actual = escribirMetadatos(
                    documento,
                    actual,
                    titulo,
                    metadatos
            );

            actual.contenido.close();

            agregarPaginacion(documento);

            try (ByteArrayOutputStream salida =
                         new ByteArrayOutputStream()) {

                documento.save(salida);

                return salida.toByteArray();
            }

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible generar el informe PDF",
                    e
            );
        }
    }

    /**
     * Escribe los metadatos al final del informe,
     * alineados a la izquierda y en letra pequena.
     *
     * Si no hay espacio, continua en otra pagina
     * del mismo documento, sin agregar otra tabla.
     */
    private PaginaActual escribirMetadatos(
            PDDocument documento,
            PaginaActual actual,
            String titulo,
            MetadatosInformeResponse metadatos)
            throws IOException {

        if (metadatos == null) {
            return actual;
        }

        List<String> lineas = lineasMetadatos(metadatos);

        if (lineas.isEmpty()) {
            return actual;
        }

        // Separacion visual respecto de la tabla.
        actual.y -= 17f;

        for (String texto : lineas) {

            List<String> fragmentos = envolver(
                    texto,
                    TAMANO_PAGINA.getWidth() - 2f * MARGEN,
                    FUENTE,
                    8f
            );

            for (String fragmento : fragmentos) {

                if (actual.y - 15f < LIMITE_INFERIOR) {

                    actual.contenido.close();

                    actual = crearPaginaMetadatos(
                            documento,
                            titulo
                    );
                }

                escribirTexto(
                        actual.contenido,
                        fragmento,
                        MARGEN,
                        actual.y,
                        FUENTE,
                        8f
                );

                actual.y -= 15f;
            }
        }

        return actual;
    }

    /**
     * Solo incorpora campos disponibles y verificables.
     * Nunca muestra valores ficticios.
     */
    private List<String> lineasMetadatos(
            MetadatosInformeResponse metadatos) {

        List<String> lineas = new ArrayList<>();

        anadirCampo(
                lineas,
                "Institución",
                metadatos.nombreInstitucion()
        );

        anadirCampo(
                lineas,
                "Sede",
                metadatos.nombreSede()
        );

        anadirCampo(
                lineas,
                "Código de sede",
                metadatos.codigoSede()
        );

        if (metadatos.filtrosAplicados() != null) {

            metadatos.filtrosAplicados()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(filtro ->
                            anadirCampo(
                                    lineas,
                                    "Filtro " + filtro.getKey(),
                                    filtro.getValue()
                            )
                    );
        }

        if (metadatos.fechaHoraGeneracion() != null) {

            lineas.add(
                    "Fecha de generación: "
                            + metadatos.fechaHoraGeneracion()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd HH:mm:ss"
                                    )
                            )
            );
        }

        anadirCampo(
                lineas,
                "Generado por",
                metadatos.generadoPor()
        );

        return lineas;
    }

    private void anadirCampo(
            List<String> lineas,
            String etiqueta,
            String valor) {

        if (valor != null && !valor.isBlank()) {

            lineas.add(
                    etiqueta + ": " + valor.trim()
            );
        }
    }

    /**
     * Pagina adicional solo para continuar la nota
     * de metadatos cuando no cabe al final.
     */
    private PaginaActual crearPaginaMetadatos(
            PDDocument documento,
            String titulo) throws IOException {

        PDPage pagina = new PDPage(TAMANO_PAGINA);
        documento.addPage(pagina);

        PDPageContentStream contenido =
                new PDPageContentStream(documento, pagina);

        float y = TAMANO_PAGINA.getHeight() - MARGEN;

        List<String> lineasTitulo = envolver(
                titulo,
                560f,
                FUENTE_NEGRITA,
                15f
        );

        for (String linea : lineasTitulo) {

            escribirTexto(
                    contenido,
                    linea,
                    MARGEN,
                    y,
                    FUENTE_NEGRITA,
                    15f
            );

            y -= 20f;
        }

        y -= 20f;

        escribirTexto(
                contenido,
                "Información del informe (continuación)",
                MARGEN,
                y,
                FUENTE,
                9f
        );

        y -= 25f;

        return new PaginaActual(contenido, y);
    }

    /**
     * Crea la primera pagina y las continuaciones
     * de la tabla de planta docente.
     */
    private PaginaActual crearPagina(
            PDDocument documento,
            String titulo,
            InformePlantaDocenteResponse informe,
            boolean primeraPagina) throws IOException {

        PDPage pagina = new PDPage(TAMANO_PAGINA);
        documento.addPage(pagina);

        PDPageContentStream contenido =
                new PDPageContentStream(documento, pagina);

        float y = TAMANO_PAGINA.getHeight() - MARGEN;

        List<String> lineasTitulo =
                envolver(
                        titulo,
                        560f,
                        FUENTE_NEGRITA,
                        15f
                );

        for (String linea : lineasTitulo) {

            escribirTexto(
                    contenido,
                    linea,
                    MARGEN,
                    y,
                    FUENTE_NEGRITA,
                    15f
            );

            y -= 20f;
        }

        y -= 12f;

        if (primeraPagina) {

            escribirTexto(
                    contenido,
                    "Cantidad de docentes: "
                            + informe.cantidadDocentes(),
                    MARGEN,
                    y,
                    FUENTE_NEGRITA,
                    10f
            );

            y -= 17f;

            escribirTexto(
                    contenido,
                    "Horas semanales contratadas: "
                            + informe.totalHorasSemanalesContratadas(),
                    MARGEN,
                    y,
                    FUENTE_NEGRITA,
                    10f
            );

            y -= 26f;

        } else {

            escribirTexto(
                    contenido,
                    "Continuacion del informe",
                    MARGEN,
                    y,
                    FUENTE,
                    9f
            );

            y -= 25f;
        }

        List<List<String>> encabezados =
                new ArrayList<>();

        for (int i = 0; i < ENCABEZADOS.length; i++) {

            encabezados.add(
                    envolver(
                            ENCABEZADOS[i],
                            ANCHOS[i],
                            FUENTE_NEGRITA,
                            9f
                    )
            );
        }

        dibujarFila(
                contenido,
                y,
                30f,
                encabezados,
                true
        );

        y -= 30f;

        return new PaginaActual(contenido, y);
    }

    /**
     * Dibuja una fila de tabla
     * con varias lineas por celda.
     */
    private void dibujarFila(
            PDPageContentStream contenido,
            float bordeSuperior,
            float altura,
            List<List<String>> celdas,
            boolean encabezado) throws IOException {

        float x = MARGEN;
        float bordeInferior = bordeSuperior - altura;

        contenido.setStrokingColor(
                160f / 255f,
                160f / 255f,
                160f / 255f
        );

        contenido.setLineWidth(0.5f);

        for (int columna = 0;
             columna < ANCHOS.length;
             columna++) {

            float ancho = ANCHOS[columna];

            contenido.addRect(
                    x,
                    bordeInferior,
                    ancho,
                    altura
            );

            contenido.stroke();

            List<String> lineas = celdas.get(columna);

            float yTexto =
                    bordeSuperior - RELLENO - 9f;

            for (String linea : lineas) {

                escribirTexto(
                        contenido,
                        linea,
                        x + RELLENO,
                        yTexto,
                        encabezado ? FUENTE_NEGRITA : FUENTE,
                        9f
                );

                yTexto -= ALTO_LINEA;
            }

            x += ancho;
        }
    }

    /**
     * Divide el texto para respetar
     * el ancho disponible.
     */
    private List<String> envolver(
            String texto,
            float anchoCelda) throws IOException {

        return envolver(
                texto,
                anchoCelda,
                FUENTE,
                9f
        );
    }

    private List<String> envolver(
            String texto,
            float anchoCelda,
            PDFont fuente,
            float tamanoFuente) throws IOException {

        String limpio = normalizarTexto(texto);

        float disponible =
                anchoCelda - 2f * RELLENO;

        List<String> resultado = new ArrayList<>();

        if (limpio.isBlank()) {

            resultado.add("");
            return resultado;
        }

        StringBuilder linea = new StringBuilder();

        for (String palabra : limpio.split("\\s+")) {

            String candidata = linea.isEmpty()
                    ? palabra
                    : linea + " " + palabra;

            if (medir(
                    candidata,
                    fuente,
                    tamanoFuente
            ) <= disponible) {

                linea = new StringBuilder(candidata);
                continue;
            }

            if (!linea.isEmpty()) {

                resultado.add(linea.toString());
                linea.setLength(0);
            }

            for (int i = 0; i < palabra.length(); i++) {

                String candidataCorta =
                        linea.toString() + palabra.charAt(i);

                if (!linea.isEmpty()
                        && medir(
                        candidataCorta,
                        fuente,
                        tamanoFuente
                ) > disponible) {

                    resultado.add(linea.toString());
                    linea.setLength(0);
                }

                linea.append(palabra.charAt(i));
            }
        }

        if (!linea.isEmpty()) {
            resultado.add(linea.toString());
        }

        return resultado;
    }

    private float medir(
            String texto,
            PDFont fuente,
            float tamanoFuente) throws IOException {

        String textoPreparado =
                PdfTextoEspanol.preparar(
                        texto,
                        fuente
                );

        return fuente.getStringWidth(textoPreparado)
                * tamanoFuente / 1000f;
    }

    private void escribirTexto(
            PDPageContentStream contenido,
            String texto,
            float x,
            float y,
            PDFont fuente,
            float tamanoFuente) throws IOException {

        String textoPreparado =
                PdfTextoEspanol.preparar(
                        texto,
                        fuente
                );

        contenido.beginText();

        contenido.setFont(
                fuente,
                tamanoFuente
        );

        contenido.newLineAtOffset(x, y);
        contenido.showText(textoPreparado);

        contenido.endText();
    }

    private String normalizarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(
                texto,
                Normalizer.Form.NFC
        );
    }

    /**
     * Agrega la numeracion cuando ya se conoce
     * el total definitivo de paginas.
     */
    private void agregarPaginacion(
            PDDocument documento) throws IOException {

        int total = documento.getNumberOfPages();

        for (int i = 0; i < total; i++) {

            PDPage pagina = documento.getPage(i);

            try (PDPageContentStream contenido =
                         new PDPageContentStream(
                                 documento,
                                 pagina,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                String pie =
                        "Pagina " + (i + 1)
                                + " de " + total;

                escribirTexto(
                        contenido,
                        pie,
                        TAMANO_PAGINA.getWidth() - 130f,
                        30f,
                        FUENTE,
                        9f
                );
            }
        }
    }

    private String resolverTitulo(
            String personalizado) {

        if (personalizado == null
                || personalizado.isBlank()) {

            return TITULO_PREDETERMINADO;
        }

        String titulo = personalizado.trim();

        if (titulo.length() > MAX_LONGITUD_TITULO) {

            throw new IllegalArgumentException(
                    "El titulo no puede superar los 100 caracteres"
            );
        }

        return titulo;
    }

    private static class PaginaActual {

        private final PDPageContentStream contenido;
        private float y;

        private PaginaActual(
                PDPageContentStream contenido,
                float y) {

            this.contenido = contenido;
            this.y = y;
        }
    }
}

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
 * Exportacion PDF del Informe de Planta Docente.
 *
 * Conserva la presentacion original y permite
 * incluir horas asignadas y extras cuando existe
 * un año escolar seleccionado.
 *
 * No consulta repositorios ni modifica datos.
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

    /**
     * Distribucion original de cinco columnas.
     */
    private static final float[] ANCHOS_ORIGINALES = {
            196f, 156f, 83f, 125f, 114f
    };

    private static final String[] ENCABEZADOS_ORIGINALES = {
            "Docente",
            "Area de nombramiento",
            "Estado",
            "Vinculacion",
            "Horas contratadas"
    };

    /**
     * Distribucion de siete columnas.
     *
     * Total: 752 puntos.
     * Se mantiene dentro de A4 horizontal.
     */
    private static final float[] ANCHOS_CARGA = {
            172f, 138f, 75f, 112f,
            95f, 80f, 80f
    };

    private static final String[] ENCABEZADOS_CARGA = {
            "Docente",
            "Area de nombramiento",
            "Estado",
            "Vinculacion",
            "Horas contratadas",
            "Horas asignadas",
            "Horas extras"
    };

    /**
     * Mantiene la firma utilizada anteriormente.
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
     * Exporta el informe de Planta Docente.
     *
     * Sin año escolar: cinco columnas originales.
     * Con año escolar: siete columnas y resumen
     * de carga academica.
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

        boolean mostrarCarga =
                informe.anioEscolarId() != null;

        float[] anchos = mostrarCarga
                ? ANCHOS_CARGA
                : ANCHOS_ORIGINALES;

        String[] encabezados = mostrarCarga
                ? ENCABEZADOS_CARGA
                : ENCABEZADOS_ORIGINALES;

        try (PDDocument documento = new PDDocument()) {

            PaginaActual actual = crearPagina(
                    documento,
                    titulo,
                    informe,
                    true,
                    anchos,
                    encabezados
            );

            for (InformePlantaDocenteDetalleResponse docente
                    : informe.docentes()) {

                List<List<String>> celdas =
                        new ArrayList<>();

                celdas.add(
                        envolver(
                                docente.nombreCompleto(),
                                anchos[0]
                        )
                );

                celdas.add(
                        envolver(
                                docente.areaNombramiento(),
                                anchos[1]
                        )
                );

                celdas.add(
                        envolver(
                                docente.estado().name(),
                                anchos[2]
                        )
                );

                celdas.add(
                        envolver(
                                docente.tipoVinculacion().name(),
                                anchos[3]
                        )
                );

                celdas.add(
                        envolver(
                                String.valueOf(
                                        docente.horasSemanalesContratadas()
                                ),
                                anchos[4]
                        )
                );

                if (mostrarCarga) {

                    celdas.add(
                            envolver(
                                    String.valueOf(
                                            docente.horasSemanalesAsignadas()
                                    ),
                                    anchos[5]
                            )
                    );

                    celdas.add(
                            envolver(
                                    String.valueOf(
                                            docente.horasSemanalesExtras()
                                    ),
                                    anchos[6]
                            )
                    );
                }

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
                            false,
                            anchos,
                            encabezados
                    );
                }

                dibujarFila(
                        actual.contenido,
                        actual.y,
                        altura,
                        celdas,
                        false,
                        anchos
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
     * Incorpora los metadatos al final del documento.
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
     * Solo incorpora metadatos disponibles.
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
     * Pagina adicional para continuar metadatos.
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
     * del Informe de Planta Docente.
     */
    private PaginaActual crearPagina(
            PDDocument documento,
            String titulo,
            InformePlantaDocenteResponse informe,
            boolean primeraPagina,
            float[] anchos,
            String[] encabezados) throws IOException {

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

        boolean mostrarCarga =
                informe.anioEscolarId() != null;

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

            y -= 17f;

            if (mostrarCarga) {

                escribirTexto(
                        contenido,
                        "Año escolar: "
                                + informe.anioEscolar(),
                        MARGEN,
                        y,
                        FUENTE_NEGRITA,
                        10f
                );

                y -= 17f;

                escribirTexto(
                        contenido,
                        "Horas semanales asignadas: "
                                + informe.totalHorasSemanalesAsignadas(),
                        MARGEN,
                        y,
                        FUENTE_NEGRITA,
                        10f
                );

                y -= 17f;

                escribirTexto(
                        contenido,
                        "Horas semanales extras: "
                                + informe.totalHorasSemanalesExtras(),
                        MARGEN,
                        y,
                        FUENTE_NEGRITA,
                        10f
                );

                y -= 17f;
            }

            y -= 9f;

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

        List<List<String>> celdasEncabezado =
                new ArrayList<>();

        int maximoLineas = 1;

        for (int i = 0; i < encabezados.length; i++) {

            List<String> lineas = envolver(
                    encabezados[i],
                    anchos[i],
                    FUENTE_NEGRITA,
                    9f
            );

            celdasEncabezado.add(lineas);

            maximoLineas = Math.max(
                    maximoLineas,
                    lineas.size()
            );
        }

        float alturaEncabezado = Math.max(
                30f,
                maximoLineas * ALTO_LINEA
                        + 2f * RELLENO
        );

        dibujarFila(
                contenido,
                y,
                alturaEncabezado,
                celdasEncabezado,
                true,
                anchos
        );

        y -= alturaEncabezado;

        return new PaginaActual(contenido, y);
    }

    /**
     * Dibuja una fila con ancho variable,
     * compatible con cinco o siete columnas.
     */
    private void dibujarFila(
            PDPageContentStream contenido,
            float bordeSuperior,
            float altura,
            List<List<String>> celdas,
            boolean encabezado,
            float[] anchos) throws IOException {

        float x = MARGEN;
        float bordeInferior = bordeSuperior - altura;

        contenido.setStrokingColor(
                160f / 255f,
                160f / 255f,
                160f / 255f
        );

        contenido.setLineWidth(0.5f);

        for (int columna = 0;
             columna < anchos.length;
             columna++) {

            float ancho = anchos[columna];

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
     * Ajusta el texto a cada celda.
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
     * Pagina el documento una vez generado.
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

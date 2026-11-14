package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeAreaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAsignaturaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;

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
 * Exporta el informe de areas y asignaturas a PDF.
 *
 * No consulta repositorios ni modifica registros.
 *
 * Los recursos PDF son locales a cada exportacion.
 * No conserva flujos entre solicitudes.
 *
 * A2:
 * Los metadatos se presentan al final del informe
 * y se reciben desde una fuente externa.
 */
@Service
public class InformeAreasAsignaturasPdfService {

    private static final String TITULO_PREDETERMINADO =
            "Informe de areas y asignaturas institucionales";

    private static final int MAX_TITULO = 100;

    private static final PDRectangle PAGINA =
            new PDRectangle(
                    PDRectangle.A4.getHeight(),
                    PDRectangle.A4.getWidth()
            );

    private static final PDFont NORMAL = new PDType1Font(
            Standard14Fonts.FontName.HELVETICA
    );

    private static final PDFont NEGRITA = new PDType1Font(
            Standard14Fonts.FontName.HELVETICA_BOLD
    );

    private static final float MARGEN = 42f;
    private static final float INTERLINEADO = 12f;
    private static final float RELLENO = 5f;
    private static final float LIMITE_INFERIOR = 60f;

    private static final float[] ANCHOS = {
            185f, 85f, 75f, 115f, 140f, 140f
    };

    private static final String[] COLUMNAS = {
            "Asignatura",
            "Codigo",
            "Estado",
            "Media tecnica",
            "Idoneidad estricta",
            "Docente exclusivo"
    };

    /**
     * Conserva el contrato original de dos parametros.
     */
    public byte[] exportarAreasAsignaturas(
            InformeAreasAsignaturasResponse informe,
            String tituloPersonalizado) {

        return exportarAreasAsignaturas(
                informe,
                tituloPersonalizado,
                null
        );
    }

    /**
     * Exportacion con metadatos institucionales.
     */
    public byte[] exportarAreasAsignaturas(
            InformeAreasAsignaturasResponse informe,
            String tituloPersonalizado,
            MetadatosInformeResponse metadatos) {

        Objects.requireNonNull(
                informe,
                "El informe de areas y asignaturas es obligatorio"
        );

        String titulo = resolverTitulo(tituloPersonalizado);

        try (PDDocument documento = new PDDocument()) {

            EstadoPagina pagina = nuevaPagina(
                    documento, titulo, true, null
            );

            escribir(
                    pagina.stream,
                    "Cantidad de areas: "
                            + informe.cantidadAreas(),
                    MARGEN,
                    pagina.y,
                    NEGRITA,
                    10f
            );

            pagina.y -= 18f;

            escribir(
                    pagina.stream,
                    "Cantidad de asignaturas: "
                            + informe.cantidadAsignaturas(),
                    MARGEN,
                    pagina.y,
                    NEGRITA,
                    10f
            );

            pagina.y -= 32f;

            if (informe.areas().isEmpty()) {

                escribir(
                        pagina.stream,
                        "No se encontraron areas para los filtros aplicados.",
                        MARGEN,
                        pagina.y,
                        NORMAL,
                        10f
                );

                pagina.y -= 25f;

            } else {

                for (InformeAreaDetalleResponse area
                        : informe.areas()) {

                    pagina = iniciarArea(
                            documento,
                            pagina,
                            titulo,
                            area
                    );

                    if (area.asignaturas().isEmpty()) {

                        escribir(
                                pagina.stream,
                                "Sin asignaturas registradas.",
                                MARGEN + 5f,
                                pagina.y - 14f,
                                NORMAL,
                                9f
                        );

                        pagina.y -= 34f;
                        continue;
                    }

                    for (InformeAsignaturaDetalleResponse asignatura
                            : area.asignaturas()) {

                        List<List<String>> celdas =
                                celdasAsignatura(asignatura);

                        int lineas = celdas.stream()
                                .mapToInt(List::size)
                                .max()
                                .orElse(1);

                        float alto = Math.max(
                                25f,
                                lineas * INTERLINEADO
                                        + 2f * RELLENO
                        );

                        if (pagina.y - alto < LIMITE_INFERIOR) {

                            pagina = nuevaPagina(
                                    documento,
                                    titulo,
                                    false,
                                    pagina
                            );

                            pagina = iniciarArea(
                                    documento,
                                    pagina,
                                    titulo,
                                    area
                            );
                        }

                        dibujarFila(
                                pagina.stream,
                                pagina.y,
                                alto,
                                celdas,
                                false
                        );

                        pagina.y -= alto;
                    }

                    pagina.y -= 18f;
                }
            }

            pagina = escribirMetadatos(
                    documento,
                    pagina,
                    titulo,
                    metadatos
            );

            pagina.stream.close();

            agregarNumeracion(documento);

            try (ByteArrayOutputStream salida =
                         new ByteArrayOutputStream()) {

                documento.save(salida);
                return salida.toByteArray();
            }

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible generar el PDF de areas y asignaturas",
                    e
            );
        }
    }

    /**
     * A2:
     * Escribe los metadatos al final del documento,
     * en letra pequena y alineados a la izquierda.
     */
    private EstadoPagina escribirMetadatos(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            MetadatosInformeResponse metadatos)
            throws IOException {

        if (metadatos == null) {
            return pagina;
        }

        List<String> lineas = lineasMetadatos(metadatos);

        if (lineas.isEmpty()) {
            return pagina;
        }

        pagina.y -= 17f;

        for (String texto : lineas) {

            List<String> fragmentos = envolver(
                    texto,
                    PAGINA.getWidth() - 2f * MARGEN,
                    NORMAL,
                    8f
            );

            for (String fragmento : fragmentos) {

                if (pagina.y - 15f < LIMITE_INFERIOR) {

                    pagina = nuevaPagina(
                            documento,
                            titulo,
                            false,
                            pagina
                    );
                }

                escribir(
                        pagina.stream,
                        fragmento,
                        MARGEN,
                        pagina.y,
                        NORMAL,
                        8f
                );

                pagina.y -= 15f;
            }
        }

        return pagina;
    }

    /**
     * Incluye solo los datos recibidos.
     * No inventa institucion, sede ni usuario.
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
     * Encabezado de cada area y su tabla.
     */
    private EstadoPagina iniciarArea(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            InformeAreaDetalleResponse area)
            throws IOException {

        String encabezado =
                "Area: " + area.nombre()
                        + " | Codigo: " + area.codigo();

        List<String> lineasArea =
                envolver(
                        encabezado,
                        730f,
                        NEGRITA,
                        11f
                );

        float espacio =
                lineasArea.size() * 15f + 70f;

        if (pagina.y - espacio < LIMITE_INFERIOR) {

            pagina = nuevaPagina(
                    documento,
                    titulo,
                    false,
                    pagina
            );
        }

        for (String linea : lineasArea) {

            escribir(
                    pagina.stream,
                    linea,
                    MARGEN,
                    pagina.y,
                    NEGRITA,
                    11f
            );

            pagina.y -= 15f;
        }

        escribir(
                pagina.stream,
                "Estado: " + estado(area.activa())
                        + "  |  Obligatoria: "
                        + siNo(area.obligatoria())
                        + "  |  Solo media: "
                        + siNo(area.soloMedia())
                        + "  |  Asignaturas: "
                        + area.cantidadAsignaturas(),
                MARGEN,
                pagina.y,
                NORMAL,
                9f
        );

        pagina.y -= 18f;

        List<List<String>> columnas =
                new ArrayList<>();

        for (int i = 0; i < COLUMNAS.length; i++) {

            columnas.add(
                    envolver(
                            COLUMNAS[i],
                            ANCHOS[i],
                            NEGRITA,
                            9f
                    )
            );
        }

        dibujarFila(
                pagina.stream,
                pagina.y,
                35f,
                columnas,
                true
        );

        pagina.y -= 35f;

        return pagina;
    }

    private List<List<String>> celdasAsignatura(
            InformeAsignaturaDetalleResponse asignatura)
            throws IOException {

        return List.of(
                envolver(
                        asignatura.nombre(),
                        ANCHOS[0],
                        NORMAL,
                        9f
                ),
                envolver(
                        asignatura.codigo(),
                        ANCHOS[1],
                        NORMAL,
                        9f
                ),
                envolver(
                        estado(asignatura.activa()),
                        ANCHOS[2],
                        NORMAL,
                        9f
                ),
                envolver(
                        siNo(asignatura.esMediaTecnica()),
                        ANCHOS[3],
                        NORMAL,
                        9f
                ),
                envolver(
                        siNo(asignatura.exigeIdoneidadEstricta()),
                        ANCHOS[4],
                        NORMAL,
                        9f
                ),
                envolver(
                        siNo(asignatura.requiereDocenteExclusivo()),
                        ANCHOS[5],
                        NORMAL,
                        9f
                )
        );
    }

    /**
     * Abre una pagina nueva.
     *
     * Si existe una pagina anterior del mismo
     * documento, cierra exclusivamente su flujo.
     *
     * No utiliza listas ni variables de instancia.
     */
    private EstadoPagina nuevaPagina(
            PDDocument documento,
            String titulo,
            boolean primera,
            EstadoPagina anterior) throws IOException {

        if (anterior != null) {
            anterior.stream.close();
        }

        PDPage pagina = new PDPage(PAGINA);
        documento.addPage(pagina);

        PDPageContentStream stream =
                new PDPageContentStream(documento, pagina);

        float y = PAGINA.getHeight() - MARGEN;

        List<String> lineasTitulo =
                envolver(
                        titulo,
                        730f,
                        NEGRITA,
                        15f
                );

        for (String linea : lineasTitulo) {

            escribir(
                    stream,
                    linea,
                    MARGEN,
                    y,
                    NEGRITA,
                    15f
            );

            y -= 20f;
        }

        y -= 12f;

        if (!primera) {

            escribir(
                    stream,
                    "Continuacion del informe",
                    MARGEN,
                    y,
                    NORMAL,
                    9f
            );

            y -= 24f;
        }

        return new EstadoPagina(stream, y);
    }

    private void dibujarFila(
            PDPageContentStream stream,
            float superior,
            float alto,
            List<List<String>> celdas,
            boolean encabezado) throws IOException {

        float x = MARGEN;

        stream.setStrokingColor(
                160f / 255f,
                160f / 255f,
                160f / 255f
        );

        stream.setLineWidth(0.5f);

        for (int columna = 0;
             columna < ANCHOS.length;
             columna++) {

            stream.addRect(
                    x,
                    superior - alto,
                    ANCHOS[columna],
                    alto
            );

            stream.stroke();

            float yTexto =
                    superior - RELLENO - 9f;

            for (String linea : celdas.get(columna)) {

                escribir(
                        stream,
                        linea,
                        x + RELLENO,
                        yTexto,
                        encabezado ? NEGRITA : NORMAL,
                        9f
                );

                yTexto -= INTERLINEADO;
            }

            x += ANCHOS[columna];
        }
    }

    private List<String> envolver(
            String texto,
            float ancho,
            PDFont fuente,
            float tamano) throws IOException {

        String limpio = normalizar(texto);

        float disponible =
                ancho - 2f * RELLENO;

        List<String> resultado = new ArrayList<>();

        if (limpio.isBlank()) {
            return List.of("");
        }

        StringBuilder linea = new StringBuilder();

        for (String palabra : limpio.split("\\s+")) {

            String propuesta = linea.isEmpty()
                    ? palabra
                    : linea + " " + palabra;

            if (medir(propuesta, fuente, tamano)
                    <= disponible) {

                linea = new StringBuilder(propuesta);
                continue;
            }

            if (!linea.isEmpty()) {

                resultado.add(linea.toString());
                linea.setLength(0);
            }

            for (int i = 0; i < palabra.length(); i++) {

                String candidato =
                        linea.toString() + palabra.charAt(i);

                if (!linea.isEmpty()
                        && medir(candidato, fuente, tamano)
                        > disponible) {

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
            float tamano) throws IOException {

        String preparado =
                PdfTextoEspanol.preparar(
                        texto,
                        fuente
                );

        return fuente.getStringWidth(preparado)
                * tamano / 1000f;
    }

    private void escribir(
            PDPageContentStream stream,
            String texto,
            float x,
            float y,
            PDFont fuente,
            float tamano) throws IOException {

        String preparado =
                PdfTextoEspanol.preparar(
                        texto,
                        fuente
                );

        stream.beginText();
        stream.setFont(fuente, tamano);
        stream.newLineAtOffset(x, y);
        stream.showText(preparado);
        stream.endText();
    }

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(
                texto,
                Normalizer.Form.NFC
        ).trim();
    }

    private void agregarNumeracion(
            PDDocument documento) throws IOException {

        int total = documento.getNumberOfPages();

        for (int i = 0; i < total; i++) {

            try (PDPageContentStream stream =
                         new PDPageContentStream(
                                 documento,
                                 documento.getPage(i),
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                escribir(
                        stream,
                        "Pagina " + (i + 1)
                                + " de " + total,
                        PAGINA.getWidth() - 130f,
                        30f,
                        NORMAL,
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

        if (titulo.length() > MAX_TITULO) {

            throw new IllegalArgumentException(
                    "El titulo no puede superar los 100 caracteres"
            );
        }

        return titulo;
    }

    private String estado(boolean activo) {
        return activo ? "ACTIVA" : "INACTIVA";
    }

    private String siNo(boolean valor) {
        return valor ? "Si" : "No";
    }

    private static final class EstadoPagina {

        private final PDPageContentStream stream;
        private float y;

        private EstadoPagina(
                PDPageContentStream stream,
                float y) {

            this.stream = stream;
            this.y = y;
        }
    }
}

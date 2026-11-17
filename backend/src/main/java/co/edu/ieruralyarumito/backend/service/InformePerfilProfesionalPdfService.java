package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.InformeIdoneidadDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeTituloProfesionalDetalleResponse;
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
 * PDF de perfiles profesionales e idoneidades.
 *
 * Primera parte: informe administrativo.
 * Segunda parte: anexo técnico de trazabilidad.
 *
 * Los metadatos A2 se muestran al final del documento.
 *
 * No consulta ni modifica la base de datos.
 * Cada exportación administra sus propios recursos.
 */
@Service
public class InformePerfilProfesionalPdfService {

    private static final String TITULO_PREDETERMINADO =
            "Informe de perfiles profesionales e idoneidades docentes";

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
    private static final float LIMITE = 65f;
    private static final float RELLENO = 5f;
    private static final float INTERLINEADO = 11f;

    private static final float ANCHO_UTIL =
            PAGINA.getWidth() - 2f * MARGEN;

    private static final float[] ANCHOS_TITULOS = {
            245f, 145f, 255f, 95f
    };

    private static final String[] COLUMNAS_TITULOS = {
            "Nombre del título",
            "Nivel",
            "Institución",
            "Año"
    };

    private static final float[] ANCHOS_IDONEIDADES = {
            170f, 180f, 115f, 105f, 105f, 65f
    };

    private static final String[] COLUMNAS_IDONEIDADES = {
            "Área",
            "Asignatura",
            "Tipo",
            "Desde",
            "Hasta",
            "Vigente"
    };

    /**
     * Conserva la firma original para los consumidores
     * y las pruebas existentes.
     */
    public byte[] exportarPerfilProfesional(
            InformePerfilProfesionalResponse informe,
            String tituloPersonalizado) {

        return exportarPerfilProfesional(
                informe,
                tituloPersonalizado,
                null
        );
    }

    /**
     * Exportación que admite metadatos A2.
     *
     * Los datos institucionales y del usuario
     * se reciben externamente, sin inventarlos.
     */
    public byte[] exportarPerfilProfesional(
            InformePerfilProfesionalResponse informe,
            String tituloPersonalizado,
            MetadatosInformeResponse metadatos) {

        Objects.requireNonNull(
                informe,
                "El informe de perfil profesional es obligatorio"
        );

        String titulo = resolverTitulo(tituloPersonalizado);

        try (PDDocument documento = new PDDocument()) {

            EstadoPagina pagina = nuevaPagina(
                    documento, titulo, null
            );

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    "Fecha de referencia: "
                            + informe.fechaReferencia(),
                    NORMAL,
                    10f,
                    19f
            );

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    "Cantidad de docentes: "
                            + informe.cantidadDocentes(),
                    NEGRITA,
                    10f,
                    18f
            );

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    "Cantidad de títulos: "
                            + informe.cantidadTitulos(),
                    NEGRITA,
                    10f,
                    18f
            );

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    "Cantidad de idoneidades: "
                            + informe.cantidadIdoneidades(),
                    NEGRITA,
                    10f,
                    35f
            );

            if (informe.docentes().isEmpty()) {

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "No se encontraron docentes para los filtros aplicados.",
                        NORMAL,
                        10f,
                        15f
                );

            } else {

                // Parte administrativa: sin identificadores UUID.
                for (InformePerfilProfesionalResponse.DetalleDocente docente
                        : informe.docentes()) {

                    pagina = iniciarDocente(
                            documento,
                            pagina,
                            titulo,
                            docente
                    );

                    pagina = escribirTitulos(
                            documento,
                            pagina,
                            titulo,
                            docente
                    );

                    pagina = escribirIdoneidades(
                            documento,
                            pagina,
                            titulo,
                            docente
                    );

                    pagina.y -= 15f;
                }

                // Anexo técnico en una página independiente.
                pagina = nuevaPagina(
                        documento,
                        titulo,
                        pagina
                );

                pagina = escribirAnexo(
                        documento,
                        pagina,
                        titulo,
                        informe
                );
            }

            // A2: nota final, después de todo el informe
            // y del anexo técnico, cuando corresponda.
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
                    "No fue posible generar el PDF de perfiles profesionales",
                    e
            );
        }
    }

    /**
     * A2: presenta los metadatos al final del documento,
     * en letra pequeña y alineación izquierda.
     *
     * Si no caben, crea espacio mediante la
     * paginación existente.
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
                    ANCHO_UTIL,
                    NORMAL,
                    8f
            );

            for (String fragmento : fragmentos) {

                pagina = asegurarEspacio(
                        documento,
                        pagina,
                        titulo,
                        28f
                );

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
     * Solo muestra los metadatos que realmente
     * se han proporcionado.
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

    private EstadoPagina iniciarDocente(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            InformePerfilProfesionalResponse.DetalleDocente docente)
            throws IOException {

        pagina = asegurarEspacio(
                documento, pagina, titulo, 95f
        );

        pagina = textoEnvuelto(
                documento,
                pagina,
                titulo,
                "Docente: " + docente.nombreCompleto(),
                NEGRITA,
                12f,
                17f
        );

        pagina = textoEnvuelto(
                documento,
                pagina,
                titulo,
                "Área de nombramiento: "
                        + docente.areaNombramiento(),
                NORMAL,
                9f,
                14f
        );

        pagina.y -= 12f;

        return pagina;
    }

    private EstadoPagina escribirTitulos(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            InformePerfilProfesionalResponse.DetalleDocente docente)
            throws IOException {

        if (docente.titulos().isEmpty()) {

            return linea(
                    documento,
                    pagina,
                    titulo,
                    "Títulos profesionales: sin registros.",
                    NORMAL,
                    9f,
                    29f
            );
        }

        pagina = encabezadoSeccion(
                documento,
                pagina,
                titulo,
                "Títulos profesionales",
                COLUMNAS_TITULOS,
                ANCHOS_TITULOS
        );

        for (InformeTituloProfesionalDetalleResponse registro
                : docente.titulos()) {

            List<List<String>> celdas = List.of(
                    envolver(
                            registro.nombreTitulo(),
                            ANCHOS_TITULOS[0],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.nivel() == null
                                    ? ""
                                    : registro.nivel().name(),
                            ANCHOS_TITULOS[1],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.institucion(),
                            ANCHOS_TITULOS[2],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.anioGraduacion() == null
                                    ? ""
                                    : registro.anioGraduacion().toString(),
                            ANCHOS_TITULOS[3],
                            NORMAL,
                            8f
                    )
            );

            float alto = alturaFila(celdas);

            if (pagina.y - alto < LIMITE) {

                pagina = nuevaPagina(
                        documento,
                        titulo,
                        pagina
                );

                // M1: repetir nombre y área del docente
                // en las continuaciones de títulos.
                pagina = iniciarDocente(
                        documento,
                        pagina,
                        titulo,
                        docente
                );

                pagina = encabezadoSeccion(
                        documento,
                        pagina,
                        titulo,
                        "Títulos profesionales (continuación)",
                        COLUMNAS_TITULOS,
                        ANCHOS_TITULOS
                );
            }

            dibujarFila(
                    pagina.stream,
                    pagina.y,
                    alto,
                    celdas,
                    ANCHOS_TITULOS,
                    false
            );

            pagina.y -= alto;
        }

        pagina.y -= 20f;

        return pagina;
    }

    private EstadoPagina escribirIdoneidades(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            InformePerfilProfesionalResponse.DetalleDocente docente)
            throws IOException {

        if (docente.idoneidades().isEmpty()) {

            return linea(
                    documento,
                    pagina,
                    titulo,
                    "Idoneidades: sin registros.",
                    NORMAL,
                    9f,
                    25f
            );
        }

        pagina = encabezadoSeccion(
                documento,
                pagina,
                titulo,
                "Idoneidades",
                COLUMNAS_IDONEIDADES,
                ANCHOS_IDONEIDADES
        );

        for (InformeIdoneidadDetalleResponse registro
                : docente.idoneidades()) {

            List<List<String>> celdas = List.of(
                    envolver(
                            registro.nombreArea(),
                            ANCHOS_IDONEIDADES[0],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.nombreAsignatura() == null
                                    ? "No aplica"
                                    : registro.nombreAsignatura(),
                            ANCHOS_IDONEIDADES[1],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.tipo() == null
                                    ? ""
                                    : registro.tipo().name(),
                            ANCHOS_IDONEIDADES[2],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.vigenteDesde() == null
                                    ? ""
                                    : registro.vigenteDesde().toString(),
                            ANCHOS_IDONEIDADES[3],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.vigenteHasta() == null
                                    ? ""
                                    : registro.vigenteHasta().toString(),
                            ANCHOS_IDONEIDADES[4],
                            NORMAL,
                            8f
                    ),
                    envolver(
                            registro.vigenteEnFecha()
                                    ? "Sí"
                                    : "No",
                            ANCHOS_IDONEIDADES[5],
                            NORMAL,
                            8f
                    )
            );

            float alto = alturaFila(celdas);

            if (pagina.y - alto < LIMITE) {

                pagina = nuevaPagina(
                        documento,
                        titulo,
                        pagina
                );

                // M1: repetir nombre y área del docente
                // en las continuaciones de idoneidades.
                pagina = iniciarDocente(
                        documento,
                        pagina,
                        titulo,
                        docente
                );

                pagina = encabezadoSeccion(
                        documento,
                        pagina,
                        titulo,
                        "Idoneidades (continuación)",
                        COLUMNAS_IDONEIDADES,
                        ANCHOS_IDONEIDADES
                );
            }

            dibujarFila(
                    pagina.stream,
                    pagina.y,
                    alto,
                    celdas,
                    ANCHOS_IDONEIDADES,
                    false
            );

            pagina.y -= alto;
        }

        pagina.y -= 15f;

        return pagina;
    }

    /**
     * Anexo separado del informe administrativo.
     *
     * Conserva los UUID y sus relaciones para
     * la trazabilidad técnica.
     */
    private EstadoPagina escribirAnexo(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            InformePerfilProfesionalResponse informe)
            throws IOException {

        pagina = linea(
                documento,
                pagina,
                titulo,
                "ANEXO TÉCNICO - IDENTIFICADORES DE REGISTROS",
                NEGRITA,
                12f,
                25f
        );

        pagina = textoEnvuelto(
                documento,
                pagina,
                titulo,
                "Los siguientes identificadores permiten "
                        + "relacionar los registros del informe "
                        + "con el sistema de información.",
                NORMAL,
                9f,
                14f
        );

        pagina.y -= 16f;

        int numeroDocente = 0;

        for (InformePerfilProfesionalResponse.DetalleDocente docente
                : informe.docentes()) {

            numeroDocente++;

            pagina = asegurarEspacio(
                    documento,
                    pagina,
                    titulo,
                    90f
            );

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    "Registro de docente #" + numeroDocente,
                    NEGRITA,
                    10f,
                    18f
            );

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    "Docente ID: " + docente.docenteId(),
                    NORMAL,
                    8f,
                    16f
            );

            pagina.y -= 5f;

            int numeroTitulo = 0;

            for (InformeTituloProfesionalDetalleResponse registro
                    : docente.titulos()) {

                numeroTitulo++;

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Título #" + numeroTitulo
                                + " - Título ID: "
                                + registro.tituloId(),
                        NORMAL,
                        8f,
                        15f
                );
            }

            if (docente.titulos().isEmpty()) {

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Sin identificadores de títulos.",
                        NORMAL,
                        8f,
                        15f
                );
            }

            pagina.y -= 5f;

            int numeroIdoneidad = 0;

            for (InformeIdoneidadDetalleResponse registro
                    : docente.idoneidades()) {

                numeroIdoneidad++;

                pagina = asegurarEspacio(
                        documento,
                        pagina,
                        titulo,
                        90f
                );

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Idoneidad #" + numeroIdoneidad,
                        NEGRITA,
                        9f,
                        15f
                );

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Idoneidad ID: "
                                + registro.idoneidadId(),
                        NORMAL,
                        8f,
                        13f
                );

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Área ID: "
                                + registro.areaId(),
                        NORMAL,
                        8f,
                        13f
                );

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Asignatura ID: "
                                + (registro.asignaturaId() == null
                                ? "No aplica"
                                : registro.asignaturaId()),
                        NORMAL,
                        8f,
                        13f
                );

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Título soporte ID: "
                                + (registro.tituloSoporteId() == null
                                ? "No aplica"
                                : registro.tituloSoporteId()),
                        NORMAL,
                        8f,
                        16f
                );

                pagina.y -= 5f;
            }

            if (docente.idoneidades().isEmpty()) {

                pagina = linea(
                        documento,
                        pagina,
                        titulo,
                        "Sin identificadores de idoneidades.",
                        NORMAL,
                        8f,
                        15f
                );
            }

            pagina.y -= 20f;
        }

        return pagina;
    }

    private EstadoPagina linea(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            String texto,
            PDFont fuente,
            float tamano,
            float avance)
            throws IOException {

        pagina = asegurarEspacio(
                documento,
                pagina,
                titulo,
                avance
        );

        escribir(
                pagina.stream,
                texto,
                MARGEN,
                pagina.y,
                fuente,
                tamano
        );

        pagina.y -= avance;

        return pagina;
    }

    private EstadoPagina textoEnvuelto(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            String texto,
            PDFont fuente,
            float tamano,
            float avance)
            throws IOException {

        List<String> lineas = envolver(
                texto,
                ANCHO_UTIL,
                fuente,
                tamano
        );

        for (String fragmento : lineas) {

            pagina = linea(
                    documento,
                    pagina,
                    titulo,
                    fragmento,
                    fuente,
                    tamano,
                    avance
            );
        }

        return pagina;
    }

    private EstadoPagina encabezadoSeccion(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            String nombre,
            String[] columnas,
            float[] anchos)
            throws IOException {

        List<List<String>> encabezados =
                new ArrayList<>();

        for (int i = 0; i < columnas.length; i++) {

            encabezados.add(
                    envolver(
                            columnas[i],
                            anchos[i],
                            NEGRITA,
                            8f
                    )
            );
        }

        float alto = Math.max(
                27f,
                alturaFila(encabezados)
        );

        pagina = asegurarEspacio(
                documento,
                pagina,
                titulo,
                alto + 50f
        );

        escribir(
                pagina.stream,
                nombre,
                MARGEN,
                pagina.y,
                NEGRITA,
                10f
        );

        pagina.y -= 18f;

        dibujarFila(
                pagina.stream,
                pagina.y,
                alto,
                encabezados,
                anchos,
                true
        );

        pagina.y -= alto;

        return pagina;
    }

    private EstadoPagina asegurarEspacio(
            PDDocument documento,
            EstadoPagina pagina,
            String titulo,
            float espacio)
            throws IOException {

        if (pagina.y - espacio < LIMITE) {

            return nuevaPagina(
                    documento,
                    titulo,
                    pagina
            );
        }

        return pagina;
    }

    /**
     * Una sola página activa por documento.
     * No utiliza estado compartido del servicio.
     */
    private EstadoPagina nuevaPagina(
            PDDocument documento,
            String titulo,
            EstadoPagina anterior)
            throws IOException {

        if (anterior != null) {
            anterior.stream.close();
        }

        PDPage hoja = new PDPage(PAGINA);
        documento.addPage(hoja);

        PDPageContentStream stream =
                new PDPageContentStream(
                        documento,
                        hoja
                );

        float y = PAGINA.getHeight() - MARGEN;

        List<String> lineasTitulo = envolver(
                titulo,
                ANCHO_UTIL,
                NEGRITA,
                15f
        );

        for (String fragmento : lineasTitulo) {

            escribir(
                    stream,
                    fragmento,
                    MARGEN,
                    y,
                    NEGRITA,
                    15f
            );

            y -= 20f;
        }

        y -= 17f;

        if (anterior != null) {

            escribir(
                    stream,
                    "Continuación del informe",
                    MARGEN,
                    y,
                    NORMAL,
                    9f
            );

            y -= 24f;
        }

        return new EstadoPagina(stream, y);
    }

    private float alturaFila(
            List<List<String>> celdas) {

        int cantidadLineas = celdas.stream()
                .mapToInt(List::size)
                .max()
                .orElse(1);

        return Math.max(
                25f,
                cantidadLineas * INTERLINEADO
                        + 2f * RELLENO
        );
    }

    private void dibujarFila(
            PDPageContentStream stream,
            float superior,
            float alto,
            List<List<String>> celdas,
            float[] anchos,
            boolean encabezado)
            throws IOException {

        float x = MARGEN;

        stream.setStrokingColor(
                160f / 255f,
                160f / 255f,
                160f / 255f
        );

        stream.setLineWidth(0.5f);

        for (int columna = 0;
             columna < anchos.length;
             columna++) {

            stream.addRect(
                    x,
                    superior - alto,
                    anchos[columna],
                    alto
            );

            stream.stroke();

            float yTexto =
                    superior - RELLENO - 8f;

            for (String fragmento : celdas.get(columna)) {

                escribir(
                        stream,
                        fragmento,
                        x + RELLENO,
                        yTexto,
                        encabezado ? NEGRITA : NORMAL,
                        8f
                );

                yTexto -= INTERLINEADO;
            }

            x += anchos[columna];
        }
    }

    private List<String> envolver(
            String texto,
            float ancho,
            PDFont fuente,
            float tamano)
            throws IOException {

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

            if (medir(
                    propuesta,
                    fuente,
                    tamano
            ) <= disponible) {

                linea = new StringBuilder(propuesta);
                continue;
            }

            if (!linea.isEmpty()) {

                resultado.add(linea.toString());
                linea.setLength(0);
            }

            for (int i = 0; i < palabra.length(); i++) {

                String candidato =
                        linea.toString()
                                + palabra.charAt(i);

                if (!linea.isEmpty()
                        && medir(
                        candidato,
                        fuente,
                        tamano
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
            float tamano)
            throws IOException {

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
            float tamano)
            throws IOException {

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
            PDDocument documento)
            throws IOException {

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
                        "Página " + (i + 1)
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
                    "El título no puede superar los 100 caracteres"
            );
        }

        return titulo;
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

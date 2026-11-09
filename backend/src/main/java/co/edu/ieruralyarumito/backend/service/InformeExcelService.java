
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;

import co.edu.ieruralyarumito.backend.dto.InformeAreaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAreasAsignaturasResponse;
import co.edu.ieruralyarumito.backend.dto.InformeAsignaturaDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeIdoneidadDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePerfilProfesionalResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformePlantaDocenteResponse;
import co.edu.ieruralyarumito.backend.dto.InformeTituloProfesionalDetalleResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * HU-04 / SCRUM-31.
 *
 * Exportacion de informes institucionales a XLSX.
 *
 * Los datos proceden de los servicios de informes.
 * No realiza consultas ni modifica la base de datos.
 */
@Service
public class InformeExcelService {

    private static final String TITULO_PLANTA =
            "Informe de planta docente y capacidad academica";

    private static final String TITULO_AREAS =
            "Informe de areas y asignaturas institucionales";

    private static final String TITULO_PERFIL =
            "Informe de perfiles profesionales e idoneidades docentes";

    private static final int MAX_LONGITUD_TITULO = 100;

    private static final int FILA_ENCABEZADO = 5;

    /**
     * Primer informe: planta docente.
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

    public byte[] exportarPlantaDocente(
            InformePlantaDocenteResponse informe,
            String tituloPersonalizado,
            MetadatosInformeResponse metadatos) {

        Objects.requireNonNull(
                informe,
                "El informe de planta docente es obligatorio"
        );

        String titulo = resolverTitulo(
                tituloPersonalizado,
                TITULO_PLANTA
        );

        try (XSSFWorkbook libro = new XSSFWorkbook()) {

            Sheet hoja = libro.createSheet("Planta docente");

            CellStyle estiloTitulo = crearEstiloTitulo(libro);
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
            CellStyle estiloNumero = crearEstiloNumero(libro);

            escribirTitulo(hoja, titulo, estiloTitulo);

            escribirIndicador(
                    hoja, 2,
                    "Cantidad de docentes",
                    informe.cantidadDocentes()
            );

            escribirIndicador(
                    hoja, 3,
                    "Horas semanales contratadas",
                    informe.totalHorasSemanalesContratadas()
            );

            String[] columnas = {
                    "Docente",
                    "Area de nombramiento",
                    "Estado",
                    "Tipo de vinculacion",
                    "Horas semanales contratadas"
            };

            crearEncabezado(
                    hoja, FILA_ENCABEZADO,
                    columnas, estiloEncabezado
            );

            int filaActual = FILA_ENCABEZADO + 1;

            for (InformePlantaDocenteDetalleResponse docente
                    : informe.docentes()) {

                Row fila = hoja.createRow(filaActual++);

                escribirTexto(
                        fila, 0,
                        docente.nombreCompleto()
                );
                escribirTexto(
                        fila, 1,
                        docente.areaNombramiento()
                );
                escribirTexto(
                        fila, 2,
                        docente.estado().name()
                );
                escribirTexto(
                        fila, 3,
                        docente.tipoVinculacion().name()
                );

                Cell horas = fila.createCell(4);

                horas.setCellValue(
                        docente.horasSemanalesContratadas()
                );

                horas.setCellStyle(estiloNumero);
            }

            int[] anchos = {32, 28, 18, 22, 30};

            configurarAnchos(hoja, anchos);

            configurarTabla(
                    hoja,
                    FILA_ENCABEZADO,
                    filaActual,
                    columnas.length
            );

            escribirMetadatos(
                    hoja,
                    filaActual,
                    columnas.length,
                    libro,
                    metadatos
            );

            return convertirEnBytes(libro);

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible generar el Excel de planta docente",
                    e
            );
        }
    }

    /**
     * Segundo informe: areas y asignaturas.
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

    public byte[] exportarAreasAsignaturas(
            InformeAreasAsignaturasResponse informe,
            String tituloPersonalizado,
            MetadatosInformeResponse metadatos) {

        Objects.requireNonNull(
                informe,
                "El informe de areas y asignaturas es obligatorio"
        );

        String titulo = resolverTitulo(
                tituloPersonalizado,
                TITULO_AREAS
        );

        try (XSSFWorkbook libro = new XSSFWorkbook()) {

            Sheet hoja = libro.createSheet("Areas y asignaturas");

            CellStyle estiloTitulo = crearEstiloTitulo(libro);
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
            CellStyle estiloNumero = crearEstiloNumero(libro);

            escribirTitulo(hoja, titulo, estiloTitulo);

            escribirIndicador(
                    hoja, 2,
                    "Cantidad de areas",
                    informe.cantidadAreas()
            );

            escribirIndicador(
                    hoja, 3,
                    "Cantidad de asignaturas",
                    informe.cantidadAsignaturas()
            );

            String[] columnas = {
                    "Area",
                    "Codigo de area",
                    "Estado del area",
                    "Obligatoria",
                    "Solo media",
                    "Asignaturas del area",
                    "Asignatura",
                    "Codigo de asignatura",
                    "Estado de asignatura",
                    "Media tecnica",
                    "Exige idoneidad estricta",
                    "Requiere docente exclusivo"
            };

            crearEncabezado(
                    hoja,
                    FILA_ENCABEZADO,
                    columnas,
                    estiloEncabezado
            );

            int filaActual = FILA_ENCABEZADO + 1;

            for (InformeAreaDetalleResponse area
                    : informe.areas()) {

                if (area.asignaturas().isEmpty()) {

                    Row fila = hoja.createRow(filaActual++);

                    escribirDatosArea(
                            fila,
                            area,
                            estiloNumero
                    );

                    continue;
                }

                for (InformeAsignaturaDetalleResponse asignatura
                        : area.asignaturas()) {

                    Row fila = hoja.createRow(filaActual++);

                    escribirDatosArea(
                            fila,
                            area,
                            estiloNumero
                    );

                    escribirTexto(
                            fila, 6,
                            asignatura.nombre()
                    );
                    escribirTexto(
                            fila, 7,
                            asignatura.codigo()
                    );
                    escribirTexto(
                            fila, 8,
                            estado(asignatura.activa())
                    );
                    escribirTexto(
                            fila, 9,
                            siNo(asignatura.esMediaTecnica())
                    );
                    escribirTexto(
                            fila, 10,
                            siNo(asignatura.exigeIdoneidadEstricta())
                    );
                    escribirTexto(
                            fila, 11,
                            siNo(asignatura.requiereDocenteExclusivo())
                    );
                }
            }

            int[] anchos = {
                    28, 19, 19, 17, 17, 22,
                    30, 23, 23, 19, 27, 29
            };

            configurarAnchos(hoja, anchos);

            configurarTabla(
                    hoja,
                    FILA_ENCABEZADO,
                    filaActual,
                    columnas.length
            );

            escribirMetadatos(
                    hoja,
                    filaActual,
                    columnas.length,
                    libro,
                    metadatos
            );

            return convertirEnBytes(libro);

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible generar el Excel de areas y asignaturas",
                    e
            );
        }
    }

    /**
     * Tercer informe: perfiles profesionales e idoneidades.
     *
     * Genera dos hojas independientes:
     * 1. Titulos profesionales.
     * 2. Idoneidades.
     *
     * No cruza las listas entre si.
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

    public byte[] exportarPerfilProfesional(
            InformePerfilProfesionalResponse informe,
            String tituloPersonalizado,
            MetadatosInformeResponse metadatos) {

        Objects.requireNonNull(
                informe,
                "El informe de perfil profesional es obligatorio"
        );

        String titulo = resolverTitulo(
                tituloPersonalizado,
                TITULO_PERFIL
        );

        try (XSSFWorkbook libro = new XSSFWorkbook()) {

            CellStyle estiloTitulo = crearEstiloTitulo(libro);
            CellStyle estiloEncabezado = crearEstiloEncabezado(libro);
            CellStyle estiloNumero = crearEstiloNumero(libro);

            crearHojaTitulos(
                    libro,
                    informe,
                    titulo,
                    estiloTitulo,
                    estiloEncabezado,
                    estiloNumero,
                    metadatos
            );

            crearHojaIdoneidades(
                    libro,
                    informe,
                    titulo,
                    estiloTitulo,
                    estiloEncabezado,
                    estiloNumero,
                    metadatos
            );

            return convertirEnBytes(libro);

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "No fue posible generar el Excel de perfiles e idoneidades",
                    e
            );
        }
    }

    private void crearHojaTitulos(
            Workbook libro,
            InformePerfilProfesionalResponse informe,
            String titulo,
            CellStyle estiloTitulo,
            CellStyle estiloEncabezado,
            CellStyle estiloNumero,
            MetadatosInformeResponse metadatos) {

        Sheet hoja = libro.createSheet("Titulos profesionales");

        escribirTitulo(
                hoja,
                titulo,
                estiloTitulo
        );

        escribirTexto(
                hoja.createRow(1),
                0,
                "Fecha de referencia: "
                        + informe.fechaReferencia()
        );

        escribirIndicador(
                hoja, 2,
                "Cantidad de docentes",
                informe.cantidadDocentes()
        );

        escribirIndicador(
                hoja, 3,
                "Cantidad de titulos",
                informe.cantidadTitulos()
        );

        escribirIndicador(
                hoja, 4,
                "Cantidad de idoneidades",
                informe.cantidadIdoneidades()
        );

        // Se reserva el indice 5 como separacion.
        int encabezado = 6;

        String[] columnas = {
                "Docente ID",
                "Docente",
                "Area de nombramiento",
                "Titulo ID",
                "Nombre del titulo",
                "Nivel academico",
                "Institucion",
                "Anio de graduacion"
        };

        crearEncabezado(
                hoja,
                encabezado,
                columnas,
                estiloEncabezado
        );

        int filaActual = encabezado + 1;

        for (InformePerfilProfesionalResponse.DetalleDocente docente
                : informe.docentes()) {

            if (docente.titulos().isEmpty()) {

                Row fila = hoja.createRow(filaActual++);

                escribirTexto(
                        fila, 0,
                        docente.docenteId().toString()
                );
                escribirTexto(
                        fila, 1,
                        docente.nombreCompleto()
                );
                escribirTexto(
                        fila, 2,
                        docente.areaNombramiento()
                );
            }

            for (InformeTituloProfesionalDetalleResponse tituloDocente
                    : docente.titulos()) {

                Row fila = hoja.createRow(filaActual++);

                escribirTexto(
                        fila, 0,
                        docente.docenteId().toString()
                );
                escribirTexto(
                        fila, 1,
                        docente.nombreCompleto()
                );
                escribirTexto(
                        fila, 2,
                        docente.areaNombramiento()
                );
                escribirTexto(
                        fila, 3,
                        tituloDocente.tituloId().toString()
                );
                escribirTexto(
                        fila, 4,
                        tituloDocente.nombreTitulo()
                );
                escribirTexto(
                        fila, 5,
                        tituloDocente.nivel() == null
                                ? null
                                : tituloDocente.nivel().name()
                );
                escribirTexto(
                        fila, 6,
                        tituloDocente.institucion()
                );

                if (tituloDocente.anioGraduacion() != null) {

                    Cell anio = fila.createCell(7);

                    anio.setCellValue(
                            tituloDocente.anioGraduacion()
                    );

                    anio.setCellStyle(estiloNumero);
                }
            }
        }

        configurarAnchos(
                hoja,
                new int[]{
                        38, 32, 28, 38,
                        42, 24, 35, 22
                }
        );

        configurarTabla(
                hoja,
                encabezado,
                filaActual,
                columnas.length
        );

        escribirMetadatos(
                hoja,
                filaActual,
                columnas.length,
                libro,
                metadatos
        );
    }

    private void crearHojaIdoneidades(
            Workbook libro,
            InformePerfilProfesionalResponse informe,
            String titulo,
            CellStyle estiloTitulo,
            CellStyle estiloEncabezado,
            CellStyle estiloNumero,
            MetadatosInformeResponse metadatos) {

        Sheet hoja = libro.createSheet("Idoneidades");

        escribirTitulo(
                hoja,
                titulo,
                estiloTitulo
        );

        escribirTexto(
                hoja.createRow(1),
                0,
                "Fecha de referencia: "
                        + informe.fechaReferencia()
        );

        escribirIndicador(
                hoja, 2,
                "Cantidad de docentes",
                informe.cantidadDocentes()
        );

        escribirIndicador(
                hoja, 3,
                "Cantidad de titulos",
                informe.cantidadTitulos()
        );

        escribirIndicador(
                hoja, 4,
                "Cantidad de idoneidades",
                informe.cantidadIdoneidades()
        );

        int encabezado = 6;

        String[] columnas = {
                "Docente ID",
                "Docente",
                "Area de nombramiento",
                "Idoneidad ID",
                "Area de idoneidad",
                "Asignatura",
                "Tipo de idoneidad",
                "Titulo soporte ID",
                "Vigente desde",
                "Vigente hasta",
                "Vigente en fecha",
                "Area ID",
                "Asignatura ID"
        };

        crearEncabezado(
                hoja,
                encabezado,
                columnas,
                estiloEncabezado
        );

        int filaActual = encabezado + 1;

        for (InformePerfilProfesionalResponse.DetalleDocente docente
                : informe.docentes()) {

            if (docente.idoneidades().isEmpty()) {

                Row fila = hoja.createRow(filaActual++);

                escribirTexto(
                        fila, 0,
                        docente.docenteId().toString()
                );
                escribirTexto(
                        fila, 1,
                        docente.nombreCompleto()
                );
                escribirTexto(
                        fila, 2,
                        docente.areaNombramiento()
                );
            }

            for (InformeIdoneidadDetalleResponse idoneidad
                    : docente.idoneidades()) {

                Row fila = hoja.createRow(filaActual++);

                escribirTexto(
                        fila, 0,
                        docente.docenteId().toString()
                );
                escribirTexto(
                        fila, 1,
                        docente.nombreCompleto()
                );
                escribirTexto(
                        fila, 2,
                        docente.areaNombramiento()
                );
                escribirTexto(
                        fila, 3,
                        idoneidad.idoneidadId().toString()
                );
                escribirTexto(
                        fila, 4,
                        idoneidad.nombreArea()
                );
                escribirTexto(
                        fila, 5,
                        idoneidad.nombreAsignatura()
                );
                escribirTexto(
                        fila, 6,
                        idoneidad.tipo() == null
                                ? null
                                : idoneidad.tipo().name()
                );
                escribirTexto(
                        fila, 7,
                        idoneidad.tituloSoporteId() == null
                                ? null
                                : idoneidad.tituloSoporteId().toString()
                );
                escribirFecha(
                        fila, 8,
                        idoneidad.vigenteDesde()
                );
                escribirFecha(
                        fila, 9,
                        idoneidad.vigenteHasta()
                );
                escribirTexto(
                        fila, 10,
                        siNo(idoneidad.vigenteEnFecha())
                );
                escribirTexto(
                        fila, 11,
                        idoneidad.areaId().toString()
                );
                escribirTexto(
                        fila, 12,
                        idoneidad.asignaturaId() == null
                                ? null
                                : idoneidad.asignaturaId().toString()
                );
            }
        }

        configurarAnchos(
                hoja,
                new int[]{
                        38, 32, 28, 38,
                        30, 32, 24, 38,
                        20, 20, 22, 38, 38
                }
        );

        configurarTabla(
                hoja,
                encabezado,
                filaActual,
                columnas.length
        );

        escribirMetadatos(
                hoja,
                filaActual,
                columnas.length,
                libro,
                metadatos
        );
    }

    /**
     * Nota final imprimible en la misma hoja,
     * sin alterar las filas de datos.
     */
    private void escribirMetadatos(
            Sheet hoja,
            int siguienteFila,
            int columnas,
            Workbook libro,
            MetadatosInformeResponse metadatos) {

        if (metadatos == null) {
            return;
        }

        Font fuente = libro.createFont();

        fuente.setFontHeightInPoints((short) 8);

        fuente.setColor(
                org.apache.poi.ss.usermodel.IndexedColors
                        .GREY_50_PERCENT.getIndex()
        );

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setWrapText(true);

        java.util.List<String> lineas =
                new java.util.ArrayList<>();

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

        int fila = siguienteFila + 1;

        for (String linea : lineas) {

            Row renglon = hoja.createRow(fila++);
            renglon.setHeightInPoints(18f);

            Cell celda = renglon.createCell(0);
            celda.setCellValue(linea);
            celda.setCellStyle(estilo);

            if (columnas > 1) {

                hoja.addMergedRegion(
                        new CellRangeAddress(
                                renglon.getRowNum(),
                                renglon.getRowNum(),
                                0,
                                columnas - 1
                        )
                );
            }
        }

        libro.setPrintArea(
                libro.getSheetIndex(hoja),
                0,
                columnas - 1,
                0,
                Math.max(0, fila - 1)
        );

        hoja.setFitToPage(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 0);
    }

    private void anadirCampo(
            java.util.List<String> lineas,
            String etiqueta,
            String valor) {

        if (valor != null && !valor.isBlank()) {

            lineas.add(
                    etiqueta + ": " + valor.trim()
            );
        }
    }

    private void escribirDatosArea(
            Row fila,
            InformeAreaDetalleResponse area,
            CellStyle estiloNumero) {

        escribirTexto(fila, 0, area.nombre());
        escribirTexto(fila, 1, area.codigo());
        escribirTexto(fila, 2, estado(area.activa()));
        escribirTexto(fila, 3, siNo(area.obligatoria()));
        escribirTexto(fila, 4, siNo(area.soloMedia()));

        Cell cantidad = fila.createCell(5);

        cantidad.setCellValue(
                area.cantidadAsignaturas()
        );

        cantidad.setCellStyle(estiloNumero);
    }

    private void escribirTitulo(
            Sheet hoja,
            String titulo,
            CellStyle estiloTitulo) {

        Row fila = hoja.createRow(0);
        Cell celda = fila.createCell(0);

        celda.setCellValue(titulo);
        celda.setCellStyle(estiloTitulo);
    }

    private void escribirIndicador(
            Sheet hoja,
            int indiceFila,
            String descripcion,
            long valor) {

        Row fila = hoja.createRow(indiceFila);

        escribirTexto(fila, 0, descripcion);

        fila.createCell(1).setCellValue(valor);
    }

    private void escribirTexto(
            Row fila,
            int columna,
            String valor) {

        if (valor != null) {

            fila.createCell(columna)
                    .setCellValue(valor);
        }
    }

    private void escribirFecha(
            Row fila,
            int columna,
            LocalDate fecha) {

        if (fecha != null) {

            escribirTexto(
                    fila,
                    columna,
                    fecha.toString()
            );
        }
    }

    private void crearEncabezado(
            Sheet hoja,
            int indiceFila,
            String[] columnas,
            CellStyle estilo) {

        Row fila = hoja.createRow(indiceFila);

        for (int i = 0; i < columnas.length; i++) {

            Cell celda = fila.createCell(i);

            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estilo);
        }
    }

    private void configurarAnchos(
            Sheet hoja,
            int[] anchos) {

        for (int i = 0; i < anchos.length; i++) {

            hoja.setColumnWidth(
                    i,
                    anchos[i] * 256
            );
        }
    }

    private void configurarTabla(
            Sheet hoja,
            int indiceEncabezado,
            int siguienteFila,
            int numeroColumnas) {

        hoja.createFreezePane(
                0,
                indiceEncabezado + 1
        );

        if (siguienteFila > indiceEncabezado + 1) {

            hoja.setAutoFilter(
                    new CellRangeAddress(
                            indiceEncabezado,
                            siguienteFila - 1,
                            0,
                            numeroColumnas - 1
                    )
            );
        }
    }

    private String resolverTitulo(
            String personalizado,
            String predeterminado) {

        if (personalizado == null
                || personalizado.isBlank()) {

            return predeterminado;
        }

        String titulo = personalizado.trim();

        if (titulo.length() > MAX_LONGITUD_TITULO) {

            throw new IllegalArgumentException(
                    "El titulo no puede superar los 100 caracteres"
            );
        }

        return titulo;
    }

    private String estado(boolean activo) {

        return activo
                ? "ACTIVA"
                : "INACTIVA";
    }

    private String siNo(boolean valor) {

        return valor
                ? "Si"
                : "No";
    }

    private CellStyle crearEstiloTitulo(
            Workbook libro) {

        Font fuente = libro.createFont();

        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);

        return estilo;
    }

    private CellStyle crearEstiloEncabezado(
            Workbook libro) {

        Font fuente = libro.createFont();
        fuente.setBold(true);

        CellStyle estilo = libro.createCellStyle();

        estilo.setFont(fuente);
        estilo.setWrapText(true);

        return estilo;
    }

    private CellStyle crearEstiloNumero(
            Workbook libro) {

        CellStyle estilo = libro.createCellStyle();

        estilo.setDataFormat(
                libro.createDataFormat()
                        .getFormat("0")
        );

        return estilo;
    }

    private byte[] convertirEnBytes(
            Workbook libro) throws IOException {

        try (ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            libro.write(salida);

            return salida.toByteArray();
        }
    }
}

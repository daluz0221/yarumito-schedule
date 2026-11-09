
package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCargaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCeldaSemanalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;
import co.edu.ieruralyarumito.backend.dto.MetadatosInformeResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * HU-04 / SCRUM-31.
 *
 * Exportacion Excel del Informe de Horarios Academicos.
 *
 * Hojas:
 * 1. Horario semanal.
 * 2. Detalle horarios.
 * 3. Carga academica.
 *
 * Los datos provienen del informe previamente construido.
 * No se crean ni modifican registros en PostgreSQL.
 *
 * Incluye trazabilidad al final de cada hoja.
 */
@Service
public class InformeHorariosExcelService {

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> DIAS = List.of(
            "LUNES",
            "MARTES",
            "MIERCOLES",
            "JUEVES",
            "VIERNES"
    );

    private final InformeHorarioSemanalService semanalService;

    public InformeHorariosExcelService() {
        this(new InformeHorarioSemanalService());
    }

    public InformeHorariosExcelService(
            InformeHorarioSemanalService semanalService) {

        this.semanalService =
                Objects.requireNonNull(semanalService);
    }

    /**
     * Exportacion institucional completa.
     *
     * Conserva las tres hojas originales.
     */
    public byte[] exportarHorarios(
            InformeHorariosResponse informe,
            MetadatosInformeResponse metadatos) {

        if (informe == null) {
            throw new IllegalArgumentException(
                    "El informe de horarios es obligatorio."
            );
        }

        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            CellStyle titulo = crearEstiloTitulo(libro);
            CellStyle encabezado = crearEstiloEncabezado(libro);
            CellStyle nota = crearEstiloNota(libro);
            CellStyle celdaSemanal = crearEstiloCeldaSemanal(libro);

            crearHojaSemanal(
                    libro,
                    informe,
                    metadatos,
                    titulo,
                    encabezado,
                    nota,
                    celdaSemanal
            );

            crearHojaDetalle(
                    libro,
                    informe,
                    metadatos,
                    titulo,
                    encabezado,
                    nota
            );

            crearHojaCarga(
                    libro,
                    informe,
                    metadatos,
                    titulo,
                    encabezado,
                    nota
            );

            libro.write(salida);

            return salida.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No fue posible generar el Excel de horarios.",
                    ex
            );
        }
    }

    /**
     * Exportacion semanal original.
     *
     * Se conserva para compatibilidad con los
     * consumidores y pruebas existentes.
     */
    public byte[] exportarHorarioSemanalPorGrupo(
            InformeHorariosResponse informe) {

        return exportarHorarioSemanalPorGrupo(
                informe,
                null
        );
    }

    /**
     * Exportacion semanal con descansos oficiales.
     *
     * Las franjas proceden del Gestor de Horarios.
     * Un descanso no representa una hora academica.
     *
     * null: compatibilidad con la exportacion anterior.
     * Lista vacia: el Gestor no informa descansos.
     */
    public byte[] exportarHorarioSemanalPorGrupo(
            InformeHorariosResponse informe,
            List<FranjaHorariaResponse> descansos) {

        if (informe == null) {
            throw new IllegalArgumentException(
                    "El informe es obligatorio."
            );
        }

        var grupos = new InformeHorarioPorGrupoService()
                .organizar(informe.clasesProgramadas());

        if (grupos.size() != 1) {
            throw new IllegalArgumentException(
                    "Se requiere exactamente un grupo "
                            + "y una version de horario."
            );
        }

        var grupo = grupos.get(0);

        if (grupo.tieneConflictos()) {
            throw new IllegalStateException(
                    "El horario contiene clases simultaneas "
                            + "que requieren revision."
            );
        }

        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            crearHojaSemanalPorGrupo(
                    libro,
                    grupo.clases(),
                    grupo.nombreGrupo() == null
                            ? "SIN IDENTIFICAR"
                            : grupo.nombreGrupo(),
                    descansos
            );

            libro.write(salida);
            return salida.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No fue posible exportar el horario semanal.",
                    ex
            );
        }
    }

    /**
     * Construye una parrilla de un grupo y una version.
     *
     * Cuando hay franjas oficiales, los descansos
     * se agregan como filas independientes.
     *
     * No se deducen descansos a partir de huecos
     * entre clases.
     */
    private void crearHojaSemanalPorGrupo(
            XSSFWorkbook libro,
            List<InformeHorarioClaseDetalleResponse> clases,
            String nombreGrupo,
            List<FranjaHorariaResponse> descansos) {

        Sheet hoja = libro.createSheet("Horario semanal");

        CellStyle titulo = crearEstiloTitulo(libro);
        CellStyle encabezado = crearEstiloEncabezado(libro);
        CellStyle estiloCelda = crearEstiloCeldaSemanal(libro);

        escribirTitulo(
                hoja,
                "I. E. RURAL YARUMITO - HORARIO SEMANAL"
                        + " - GRUPO " + nombreGrupo,
                7,
                titulo
        );

        escribirEncabezados(
                hoja,
                2,
                new String[]{
                        "Clase",
                        "Horario",
                        "Lunes",
                        "Martes",
                        "Miércoles",
                        "Jueves",
                        "Viernes"
                },
                encabezado
        );

        List<InformeHorarioCeldaSemanalResponse> celdas =
                semanalService.construirCuadricula(clases);

        Map<ClaveFranja, Map<String,
                List<InformeHorarioClaseDetalleResponse>>> matriz =
                new TreeMap<>(
                        Comparator
                                .comparingInt(
                                        (ClaveFranja f) ->
                                                ordenJornada(f.jornada())
                                )
                                .thenComparing(ClaveFranja::inicio)
                                .thenComparing(ClaveFranja::fin)
                );

        for (InformeHorarioCeldaSemanalResponse celda : celdas) {

            ClaveFranja franja = new ClaveFranja(
                    celda.jornada(),
                    celda.horaInicio(),
                    celda.horaFin()
            );

            matriz.computeIfAbsent(
                    franja,
                    ignorada -> new LinkedHashMap<>()
            ).put(celda.diaSemana(), celda.clases());
        }

        /*
         * Las franjas oficiales se agregan a la matriz,
         * aunque no haya clases inmediatamente antes
         * o despues del descanso.
         */
        Set<ClaveFranja> descansosOficiales =
                new HashSet<>();

        if (descansos != null) {

            Set<String> jornadasPresentes =
                    new HashSet<>();

            for (InformeHorarioClaseDetalleResponse clase
                    : clases) {

                if (clase.jornada() != null) {
                    jornadasPresentes.add(clase.jornada());
                }
            }

            for (FranjaHorariaResponse descanso : descansos) {

                if (descanso == null
                        || !descanso.isEsDescanso()) {
                    continue;
                }

                LocalTime inicio =
                        descanso.getHoraInicio();

                LocalTime fin =
                        descanso.getHoraFin();

                if (inicio == null
                        || fin == null
                        || !inicio.isBefore(fin)) {

                    throw new IllegalArgumentException(
                            "La franja oficial de descanso "
                                    + "tiene horas invalidas."
                    );
                }

                String jornada = jornadaDelDescanso(
                        descanso.getTurnoNombre(),
                        jornadasPresentes
                );

                if (!jornadasPresentes.contains(jornada)) {
                    continue;
                }

                ClaveFranja claveDescanso =
                        new ClaveFranja(
                                jornada,
                                inicio,
                                fin
                        );

                /*
                 * No se permite presentar como descanso
                 * una franja que contiene clases.
                 */
                for (ClaveFranja claseProgramada :
                        matriz.keySet()) {

                    if (descansosOficiales.contains(
                            claseProgramada)) {
                        continue;
                    }

                    if (jornada.equals(
                            claseProgramada.jornada())
                            && claseProgramada.inicio()
                            .isBefore(fin)
                            && inicio.isBefore(
                            claseProgramada.fin())) {

                        throw new IllegalStateException(
                                "Existe una clase programada "
                                        + "durante el descanso oficial."
                        );
                    }
                }

                descansosOficiales.add(
                        claveDescanso
                );

                matriz.putIfAbsent(
                        claveDescanso,
                        new LinkedHashMap<>()
                );
            }
        }

        int numeroFila = 3;
        int numeroClase = 0;

        ClaveFranja anterior = null;

        for (var entrada : matriz.entrySet()) {

            ClaveFranja franja =
                    entrada.getKey();

            /*
             * Descanso oficial: se dibuja como fila
             * independiente sin incrementar el numero
             * de horas academicas.
             */
            if (descansosOficiales.contains(franja)) {

                Row filaDescanso = crearFilaParrilla(
                        hoja,
                        numeroFila++,
                        estiloCelda
                );

                filaDescanso.getCell(0)
                        .setCellValue("—");

                filaDescanso.getCell(1)
                        .setCellValue(
                                hora(franja.inicio())
                                        + " - "
                                        + hora(franja.fin())
                        );

                for (int dia = 2; dia < 7; dia++) {
                    filaDescanso.getCell(dia)
                            .setCellValue("DESCANSO");
                }

                filaDescanso.setHeightInPoints(28f);

                continue;
            }

            /*
             * Compatibilidad exclusiva con la
             * exportacion historica.
             *
             * Solo se utiliza cuando el consumidor
             * llama al metodo original sin franjas.
             *
             * No se aplica cuando el Gestor proporciona
             * los datos oficiales, incluso si su lista
             * de descansos esta vacia.
             */
            if (descansos == null
                    && anterior != null
                    && Objects.equals(
                    anterior.jornada(),
                    franja.jornada())
                    && anterior.fin().equals(
                    LocalTime.of(9, 15))
                    && franja.inicio().equals(
                    LocalTime.of(9, 45))) {

                Row filaDescanso = crearFilaParrilla(
                        hoja,
                        numeroFila++,
                        estiloCelda
                );

                filaDescanso.getCell(0)
                        .setCellValue("—");

                filaDescanso.getCell(1)
                        .setCellValue("09:15 - 09:45");

                for (int dia = 2; dia < 7; dia++) {
                    filaDescanso.getCell(dia)
                            .setCellValue("DESCANSO");
                }

                filaDescanso.setHeightInPoints(28f);
            }

            numeroClase++;

            Row fila = crearFilaParrilla(
                    hoja,
                    numeroFila++,
                    estiloCelda
            );

            fila.getCell(0)
                    .setCellValue(numeroClase);

            fila.getCell(1).setCellValue(
                    hora(franja.inicio())
                            + " - "
                            + hora(franja.fin())
            );

            for (int dia = 0; dia < DIAS.size(); dia++) {

                List<InformeHorarioClaseDetalleResponse> clasesDia =
                        entrada.getValue().getOrDefault(
                                DIAS.get(dia),
                                List.of()
                        );

                if (clasesDia.size() > 1) {
                    throw new IllegalStateException(
                            "Varias clases en la misma celda "
                                    + "del horario semanal."
                    );
                }

                String asignatura = clasesDia.isEmpty()
                        ? ""
                        : valor(
                        clasesDia.get(0)
                                .nombreAsignatura()
                );

                fila.getCell(dia + 2)
                        .setCellValue(asignatura);
            }

            fila.setHeightInPoints(35f);

            anterior = franja;
        }

        hoja.setColumnWidth(0, 12 * 256);
        hoja.setColumnWidth(1, 19 * 256);

        for (int columna = 2; columna < 7; columna++) {
            hoja.setColumnWidth(columna, 30 * 256);
        }

        hoja.setAutobreaks(true);
        hoja.setFitToPage(true);

        hoja.getPrintSetup().setLandscape(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 1);
    }

    /**
     * Crea una fila con sus siete celdas y bordes.
     */
    private Row crearFilaParrilla(
            Sheet hoja,
            int numeroFila,
            CellStyle estilo) {

        Row fila = hoja.createRow(numeroFila);

        for (int columna = 0; columna < 7; columna++) {

            Cell celda = fila.createCell(columna);
            celda.setCellStyle(estilo);
        }

        return fila;
    }

    /**
     * Representacion visual del horario institucional.
     *
     * Se conserva el comportamiento original.
     */
    private void crearHojaSemanal(
            Workbook libro,
            InformeHorariosResponse informe,
            MetadatosInformeResponse metadatos,
            CellStyle titulo,
            CellStyle encabezado,
            CellStyle nota,
            CellStyle estiloCelda) {

        Sheet hoja = libro.createSheet("Horario semanal");

        escribirTitulo(
                hoja,
                "HORARIO SEMANAL - "
                        + valor(informe.nombreAnioEscolar()),
                7,
                titulo
        );

        String[] columnas = {
                "Jornada",
                "Horario",
                "Lunes",
                "Martes",
                "Miercoles",
                "Jueves",
                "Viernes"
        };

        escribirEncabezados(
                hoja,
                2,
                columnas,
                encabezado
        );

        List<InformeHorarioCeldaSemanalResponse> celdas =
                semanalService.construirCuadricula(
                        informe.clasesProgramadas()
                );

        Map<ClaveFranja, Map<String,
                List<InformeHorarioClaseDetalleResponse>>> matriz =
                new TreeMap<>(
                        Comparator
                                .comparingInt(
                                        (ClaveFranja clave) ->
                                                ordenJornada(
                                                        clave.jornada()
                                                )
                                )
                                .thenComparing(
                                        ClaveFranja::inicio
                                )
                                .thenComparing(
                                        ClaveFranja::fin
                                )
                );

        for (InformeHorarioCeldaSemanalResponse celda : celdas) {

            ClaveFranja clave = new ClaveFranja(
                    celda.jornada(),
                    celda.horaInicio(),
                    celda.horaFin()
            );

            matriz.computeIfAbsent(
                    clave,
                    ignorada -> new LinkedHashMap<>()
            ).put(
                    celda.diaSemana(),
                    celda.clases()
            );
        }

        int numeroFila = 3;

        for (Map.Entry<ClaveFranja,
                Map<String, List<InformeHorarioClaseDetalleResponse>>>
                entrada : matriz.entrySet()) {

            Row fila = hoja.createRow(numeroFila++);

            for (int columna = 0; columna < 7; columna++) {

                Cell celda = fila.createCell(columna);
                celda.setCellStyle(estiloCelda);
            }

            ClaveFranja franja = entrada.getKey();

            fila.getCell(0).setCellValue(
                    franja.jornada().equals("CONTRAJORNADA")
                            ? "Contrajornada"
                            : "Regular"
            );

            fila.getCell(1).setCellValue(
                    hora(franja.inicio())
                            + " - "
                            + hora(franja.fin())
            );

            int maximoLineas = 1;

            for (int indice = 0; indice < DIAS.size(); indice++) {

                List<InformeHorarioClaseDetalleResponse> clases =
                        entrada.getValue()
                                .getOrDefault(
                                        DIAS.get(indice),
                                        List.of()
                                );

                StringBuilder contenido =
                        new StringBuilder();

                for (InformeHorarioClaseDetalleResponse clase : clases) {

                    if (!contenido.isEmpty()) {
                        contenido.append("\n\n");
                    }

                    contenido.append(
                            valor(clase.nombreAsignatura())
                    );

                    contenido.append("\n");
                    contenido.append(
                            valor(clase.nombreDocente())
                    );

                    contenido.append("\nGrupo: ");
                    contenido.append(
                            valor(clase.nombreGrupo())
                    );

                    contenido.append("\nAula: ");
                    contenido.append(
                            valor(clase.nombreAula())
                    );

                    if (clase.esHoraExtra()) {
                        contenido.append("\nHora extra");
                    }

                    contenido.append("\n");
                    contenido.append(
                            valor(clase.estadoHorario())
                    );

                    contenido.append(" V");
                    contenido.append(
                            valor(clase.versionHorario())
                    );
                }

                String texto = contenido.toString();

                fila.getCell(indice + 2)
                        .setCellValue(texto);

                maximoLineas = Math.max(
                        maximoLineas,
                        texto.isEmpty()
                                ? 1
                                : texto.split("\n", -1).length
                );
            }

            fila.setHeightInPoints(
                    Math.min(
                            409f,
                            Math.max(
                                    30f,
                                    maximoLineas * 13f
                            )
                    )
            );
        }

        if (matriz.isEmpty()) {

            Row fila = hoja.createRow(numeroFila++);

            fila.createCell(0).setCellValue(
                    "No hay clases programadas "
                            + "para los filtros seleccionados."
            );
        }

        numeroFila++;

        Row advertencia = hoja.createRow(numeroFila++);

        advertencia.createCell(0).setCellValue(
                "Las celdas muestran clases programadas. "
                        + "Una celda vacia no certifica "
                        + "disponibilidad del docente."
        );

        escribirMetadatos(
                hoja,
                numeroFila,
                7,
                libro,
                metadatos,
                nota
        );

        hoja.setColumnWidth(0, 19 * 256);
        hoja.setColumnWidth(1, 17 * 256);

        for (int i = 2; i < 7; i++) {
            hoja.setColumnWidth(i, 35 * 256);
        }

        hoja.setAutobreaks(true);
        hoja.getPrintSetup().setLandscape(true);
    }

    /**
     * Hoja de detalle de clases programadas.
     */
    private void crearHojaDetalle(
            Workbook libro,
            InformeHorariosResponse informe,
            MetadatosInformeResponse metadatos,
            CellStyle titulo,
            CellStyle encabezado,
            CellStyle nota) {

        Sheet hoja = libro.createSheet("Detalle horarios");

        String[] columnas = {
                "Dia",
                "Hora inicio",
                "Hora fin",
                "Docente",
                "Grado",
                "Grupo",
                "Asignatura",
                "Jornada",
                "Aula",
                "Hora extra",
                "Estado",
                "Version"
        };

        escribirTitulo(
                hoja,
                "INFORME DE HORARIOS ACADEMICOS",
                columnas.length,
                titulo
        );

        escribirEncabezados(
                hoja,
                2,
                columnas,
                encabezado
        );

        int fila = 3;

        for (InformeHorarioClaseDetalleResponse clase :
                informe.clasesProgramadas()) {

            Row renglon = hoja.createRow(fila++);

            texto(renglon, 0, clase.diaSemana());
            texto(renglon, 1, hora(clase.horaInicio()));
            texto(renglon, 2, hora(clase.horaFin()));
            texto(renglon, 3, clase.nombreDocente());
            texto(renglon, 4, clase.grado());
            texto(renglon, 5, clase.nombreGrupo());
            texto(renglon, 6, clase.nombreAsignatura());
            texto(renglon, 7, clase.jornada());
            texto(renglon, 8, clase.nombreAula());

            texto(
                    renglon,
                    9,
                    clase.esHoraExtra() ? "Si" : "No"
            );

            texto(
                    renglon,
                    10,
                    clase.estadoHorario()
            );

            numero(
                    renglon,
                    11,
                    clase.versionHorario()
            );
        }

        fila++;

        Row resumen = hoja.createRow(fila++);

        texto(
                resumen,
                0,
                "Clases programadas: "
                        + valor(informe.totalClasesProgramadas())
        );

        Row regulares = hoja.createRow(fila++);

        texto(
                regulares,
                0,
                "Horas academicas jornada regular: "
                        + valor(informe.horasAcademicasJornadaRegular())
        );

        Row contrajornada = hoja.createRow(fila++);

        texto(
                contrajornada,
                0,
                "Horas academicas contrajornada: "
                        + valor(informe.horasAcademicasContrajornada())
        );

        Row total = hoja.createRow(fila++);

        texto(
                total,
                0,
                "Total horas academicas programadas: "
                        + valor(informe.horasAcademicasProgramadas())
        );

        escribirMetadatos(
                hoja,
                fila,
                columnas.length,
                libro,
                metadatos,
                nota
        );

        configurarHoja(
                hoja,
                columnas.length,
                20,
                30
        );
    }

    /**
     * Hoja de carga academica.
     *
     * Se conservan los totales calculados por HU-04.
     */
    private void crearHojaCarga(
            Workbook libro,
            InformeHorariosResponse informe,
            MetadatosInformeResponse metadatos,
            CellStyle titulo,
            CellStyle encabezado,
            CellStyle nota) {

        Sheet hoja = libro.createSheet("Carga academica");

        String[] columnas = {
                "Docente",
                "Grupo",
                "Asignatura",
                "Horas ordinarias",
                "Horas extras",
                "Jornada regular",
                "Contrajornada",
                "Total horas"
        };

        escribirTitulo(
                hoja,
                "RESUMEN DE CARGA ACADEMICA",
                columnas.length,
                titulo
        );

        escribirEncabezados(
                hoja,
                2,
                columnas,
                encabezado
        );

        int fila = 3;

        for (InformeHorarioCargaResponse carga :
                informe.resumenCargaHoraria()) {

            Row renglon = hoja.createRow(fila++);

            texto(renglon, 0, carga.nombreDocente());
            texto(renglon, 1, carga.nombreGrupo());
            texto(renglon, 2, carga.nombreAsignatura());

            numero(
                    renglon,
                    3,
                    carga.horasOrdinarias()
            );

            numero(
                    renglon,
                    4,
                    carga.horasExtras()
            );

            numero(
                    renglon,
                    5,
                    carga.horasJornadaRegular()
            );

            numero(
                    renglon,
                    6,
                    carga.horasContrajornada()
            );

            numero(
                    renglon,
                    7,
                    carga.totalHorasAcademicas()
            );
        }

        fila++;

        Row aclaracion = hoja.createRow(fila++);

        texto(
                aclaracion,
                0,
                "Las horas por jornada clasifican las mismas "
                        + "horas ordinarias y extras; "
                        + "no deben sumarse nuevamente."
        );

        escribirMetadatos(
                hoja,
                fila,
                columnas.length,
                libro,
                metadatos,
                nota
        );

        configurarHoja(
                hoja,
                columnas.length,
                22,
                30
        );
    }

    /**
     * Escribe el titulo principal de una hoja.
     */
    private void escribirTitulo(
            Sheet hoja,
            String contenido,
            int columnas,
            CellStyle estilo) {

        Row fila = hoja.createRow(0);
        fila.setHeightInPoints(28);

        Cell celda = fila.createCell(0);
        celda.setCellValue(contenido);
        celda.setCellStyle(estilo);

        hoja.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        columnas - 1
                )
        );
    }

    private void escribirEncabezados(
            Sheet hoja,
            int numeroFila,
            String[] nombres,
            CellStyle estilo) {

        Row fila = hoja.createRow(numeroFila);

        for (int i = 0; i < nombres.length; i++) {

            Cell celda = fila.createCell(i);
            celda.setCellValue(nombres[i]);
            celda.setCellStyle(estilo);
        }

        hoja.createFreezePane(
                0,
                numeroFila + 1
        );
    }

    /**
     * Metadatos institucionales y trazabilidad.
     */
    private void escribirMetadatos(
            Sheet hoja,
            int siguienteFila,
            int columnas,
            Workbook libro,
            MetadatosInformeResponse metadatos,
            CellStyle estilo) {

        if (metadatos == null) {

            ajustarAreaImpresion(
                    hoja,
                    columnas,
                    siguienteFila,
                    libro
            );

            return;
        }

        List<String> lineas = new ArrayList<>();

        anadirCampo(
                lineas,
                "Institucion",
                metadatos.nombreInstitucion()
        );

        anadirCampo(
                lineas,
                "Sede",
                metadatos.nombreSede()
        );

        anadirCampo(
                lineas,
                "Codigo de sede",
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
                    "Fecha de generacion: "
                            + metadatos.fechaHoraGeneracion()
                            .format(FORMATO_FECHA_HORA)
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

        ajustarAreaImpresion(
                hoja,
                columnas,
                fila,
                libro
        );
    }

    private void ajustarAreaImpresion(
            Sheet hoja,
            int columnas,
            int siguienteFila,
            Workbook libro) {

        libro.setPrintArea(
                libro.getSheetIndex(hoja),
                0,
                columnas - 1,
                0,
                Math.max(0, siguienteFila - 1)
        );

        hoja.setFitToPage(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 0);
    }

    private void configurarHoja(
            Sheet hoja,
            int columnas,
            int anchoNormal,
            int anchoPrincipal) {

        for (int i = 0; i < columnas; i++) {

            hoja.setColumnWidth(
                    i,
                    (i == 0
                            ? anchoPrincipal
                            : anchoNormal) * 256
            );
        }

        hoja.setAutobreaks(true);
        hoja.getPrintSetup().setLandscape(true);
    }

    private CellStyle crearEstiloTitulo(
            Workbook libro) {

        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 14);

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);

        return estilo;
    }

    private CellStyle crearEstiloEncabezado(
            Workbook libro) {

        Font fuente = libro.createFont();
        fuente.setBold(true);

        fuente.setColor(
                IndexedColors.WHITE.getIndex()
        );

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);

        estilo.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );

        estilo.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        estilo.setAlignment(
                HorizontalAlignment.CENTER
        );

        estilo.setWrapText(true);

        return estilo;
    }

    private CellStyle crearEstiloCeldaSemanal(
            Workbook libro) {

        CellStyle estilo = libro.createCellStyle();

        estilo.setWrapText(true);

        estilo.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        estilo.setAlignment(
                HorizontalAlignment.LEFT
        );

        estilo.setBorderBottom(
                org.apache.poi.ss.usermodel.BorderStyle.THIN
        );

        estilo.setBorderRight(
                org.apache.poi.ss.usermodel.BorderStyle.THIN
        );

        return estilo;
    }

    private CellStyle crearEstiloNota(
            Workbook libro) {

        Font fuente = libro.createFont();

        fuente.setFontHeightInPoints((short) 8);

        fuente.setColor(
                IndexedColors.GREY_50_PERCENT.getIndex()
        );

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);

        estilo.setAlignment(
                HorizontalAlignment.LEFT
        );

        estilo.setWrapText(true);

        return estilo;
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

    private void texto(
            Row fila,
            int columna,
            String valor) {

        fila.createCell(columna).setCellValue(
                valor == null ? "" : valor
        );
    }

    private void numero(
            Row fila,
            int columna,
            Number valor) {

        Cell celda = fila.createCell(columna);

        if (valor != null) {
            celda.setCellValue(valor.doubleValue());
        }
    }

    private String hora(LocalTime valor) {

        return valor == null
                ? ""
                : valor.format(FORMATO_HORA);
    }

    private String valor(Object dato) {

        return dato == null
                ? ""
                : dato.toString();
    }

    private int ordenJornada(
            String jornada) {

        return "REGULAR".equals(jornada)
                ? 0
                : 1;
    }

    private record ClaveFranja(
            String jornada,
            LocalTime inicio,
            LocalTime fin) {
    }

    /**
     * Determina la jornada institucional del descanso.
     *
     * Se utilizan los nombres del turno provenientes
     * del Gestor y los valores ya definidos en HU-04.
     */
    private String jornadaDelDescanso(
            String turnoNombre,
            Set<String> jornadasPresentes) {

        if (turnoNombre == null
                || turnoNombre.isBlank()) {

            if (jornadasPresentes.size() == 1) {
                return jornadasPresentes.iterator().next();
            }

            throw new IllegalArgumentException(
                    "No es posible asociar el descanso "
                            + "con una jornada: falta el turno."
            );
        }

        String nombre = java.text.Normalizer.normalize(
                turnoNombre.trim()
                        .toUpperCase(Locale.ROOT),
                java.text.Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return switch (nombre) {

            case "REGULAR", "MANANA", "TARDE" ->
                    "REGULAR";

            case "CONTRAJORNADA" ->
                    "CONTRAJORNADA";

            default -> throw new IllegalArgumentException(
                    "Turno de descanso no reconocido: "
                            + turnoNombre
            );
        };
    }
}

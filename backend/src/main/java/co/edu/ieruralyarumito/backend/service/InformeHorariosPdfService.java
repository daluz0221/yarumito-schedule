package co.edu.ieruralyarumito.backend.service;

import co.edu.ieruralyarumito.backend.dto.FranjaHorariaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCeldaSemanalResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioClaseDetalleResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorarioCargaResponse;
import co.edu.ieruralyarumito.backend.dto.InformeHorariosResponse;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * HU-04 / SCRUM-31.
 *
 * Genera el PDF del Informe de Horarios Academicos.
 *
 * Formato:
 * - Carta horizontal.
 * - Cuadricula semanal de lunes a viernes.
 * - Detalle cronologico de clases.
 * - Resumen de carga academica.
 * - Trazabilidad al final.
 * - Numeracion y continuidad entre paginas.
 *
 * No consulta ni modifica PostgreSQL.
 */
@Service
public class InformeHorariosPdfService {

    private static final PDRectangle PAGINA =
            new PDRectangle(
                    PDRectangle.LETTER.getHeight(),
                    PDRectangle.LETTER.getWidth()
            );

    private static final PDFont NORMAL =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDFont NEGRITA =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    private static final float MARGEN = 40f;
    private static final float LIMITE = 55f;
    private static final float ANCHO_UTIL =
            PAGINA.getWidth() - 2f * MARGEN;

    private static final String TITULO =
            "INFORME DE HORARIOS ACADEMICOS";

    private static final List<String> DIAS = List.of(
            "LUNES", "MARTES", "MIERCOLES",
            "JUEVES", "VIERNES"
    );

    /*
     * Ancho carta horizontal disponible: 712 puntos.
     *
     * Jornada + horas + cinco dias = 712.
     */
    private static final float[] ANCHOS_SEMANA = {
            85f, 75f, 110f, 110f, 110f, 110f, 112f
    };

    private static final String[] CABECERAS_SEMANA = {
            "Jornada", "Horario", "Lunes",
            "Martes", "Miercoles", "Jueves", "Viernes"
    };

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InformeHorarioSemanalService semanalService;

    private final InformeHorarioPorGrupoService grupoService =
            new InformeHorarioPorGrupoService();

    public InformeHorariosPdfService() {
        this(new InformeHorarioSemanalService());
    }

    public InformeHorariosPdfService(
            InformeHorarioSemanalService semanalService) {

        this.semanalService =
                Objects.requireNonNull(semanalService);
    }

    public byte[] exportarHorarios(
            InformeHorariosResponse informe) {

        return exportarHorarios(informe, null);
    }

    public byte[] exportarHorarios(
            InformeHorariosResponse informe,
            MetadatosInformeResponse metadatos) {

        if (informe == null) {
            throw new IllegalArgumentException(
                    "El informe de horarios es obligatorio."
            );
        }

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            EstadoPagina pagina = nuevaPagina(
                    documento, null, false
            );

            pagina = datosGenerales(
                    documento, pagina, informe
            );

            List<InformeHorarioPorGrupoService.HorarioGrupo> grupos =
                    grupoService.organizar(informe.clasesProgramadas());

            if (grupos.isEmpty()) {

                pagina = cuadriculaSemanal(
                        documento,
                        pagina,
                        List.of(),
                        "HORARIO SEMANAL"
                );

            } else {

                for (InformeHorarioPorGrupoService.HorarioGrupo grupo
                        : grupos) {

                    String nombreGrupo = grupo.nombreGrupo() == null
                            ? "SIN IDENTIFICAR"
                            : grupo.nombreGrupo();

                    String tituloGrupo = "HORARIO SEMANAL - GRADO "
                            + nombreGrupo
                            + " - VERSION "
                            + grupo.versionHorario();

                    pagina = cuadriculaSemanal(
                            documento,
                            pagina,
                            grupo.clases(),
                            tituloGrupo
                    );

                    if (grupo.tieneConflictos()) {

                        pagina = linea(
                                documento,
                                pagina,
                                "ADVERTENCIA: Se encontraron "
                                        + grupo.conflictos().size()
                                        + " franjas con varias clases "
                                        + "para este grupo. "
                                        + "La programacion requiere revision.",
                                8f
                        );
                    }
                }
            }

            pagina = detalleClases(
                    documento, pagina, informe
            );

            pagina = resumenCarga(
                    documento, pagina, informe
            );

            pagina = escribirMetadatos(
                    documento, pagina, metadatos
            );

            pagina.stream.close();

            numerarPaginas(documento);

            documento.save(salida);

            return salida.toByteArray();

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No fue posible generar el PDF de horarios.",
                    ex
            );
        }
    }

    /**
     * Mantiene la exportacion original para los consumidores existentes.
     * Solo esta entrada conserva el descanso historico de 09:15 a 09:45.
     */
    public byte[] exportarHorarioSemanalPorGrupo(
            InformeHorariosResponse informe) {

        return exportarHorarioSemanalPorGrupo(informe, null);
    }

    /**
     * Exporta la parrilla semanal con descansos oficiales del Gestor.
     * null mantiene compatibilidad; lista vacia significa sin descansos.
     */
    public byte[] exportarHorarioSemanalPorGrupo(
            InformeHorariosResponse informe,
            List<FranjaHorariaResponse> descansos) {

        if (informe == null) {
            throw new IllegalArgumentException(
                    "El informe de horarios es obligatorio."
            );
        }

        List<InformeHorarioPorGrupoService.HorarioGrupo> grupos =
                grupoService.organizar(informe.clasesProgramadas());

        if (grupos.size() != 1) {
            throw new IllegalArgumentException(
                    "La exportación semanal requiere exactamente "
                            + "un grupo y una versión de horario."
            );
        }

        InformeHorarioPorGrupoService.HorarioGrupo grupo =
                grupos.get(0);

        String nombreGrupo = grupo.nombreGrupo() == null
                ? "SIN IDENTIFICAR"
                : grupo.nombreGrupo();

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            EstadoPagina pagina = nuevaPagina(
                    documento,
                    null,
                    false,
                    "I. E. Rural Yarumito"
            );

            pagina = cuadriculaSemanal(
                    documento,
                    pagina,
                    grupo.clases(),
                    "HORARIO SEMANAL - GRUPO " + nombreGrupo,
                    true,
                    descansos
            );

            pagina.stream.close();

            numerarPaginas(documento);
            documento.save(salida);

            return salida.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No fue posible generar el horario semanal "
                            + "del grupo " + nombreGrupo,
                    ex
            );
        }
    }

    private EstadoPagina datosGenerales(
            PDDocument documento,
            EstadoPagina pagina,
            InformeHorariosResponse informe)
            throws IOException {

        pagina = linea(
                documento, pagina,
                "Institucion: "
                        + valor(informe.nombreInstitucion()),
                9f
        );

        pagina = linea(
                documento, pagina,
                "Sede: " + valor(informe.nombreSede()),
                9f
        );

        pagina = linea(
                documento, pagina,
                "Anio lectivo: "
                        + valor(informe.nombreAnioEscolar()),
                9f
        );

        pagina = linea(
                documento, pagina,
                "Docentes: " + valor(informe.totalDocentes())
                        + " | Grupos: "
                        + valor(informe.totalGrupos())
                        + " | Asignaturas: "
                        + valor(informe.totalAsignaturas()),
                9f
        );

        if (informe.fechaInicioConsulta() != null
                && informe.fechaFinConsulta() != null) {

            pagina = linea(
                    documento, pagina,
                    "Rango consultado: "
                            + informe.fechaInicioConsulta()
                            + " al "
                            + informe.fechaFinConsulta(),
                    9f
            );
        }

        pagina.y -= 8f;

        return pagina;
    }

    /**
     * Construye una cuadricula semanal a partir
     * de las celdas generadas por el servicio.
     *
     * No inventa franjas horarias ni clases.
     */
    private EstadoPagina cuadriculaSemanal(
            PDDocument documento,
            EstadoPagina pagina,
            List<InformeHorarioClaseDetalleResponse> clases,
            String tituloGrupo)
            throws IOException {

        return cuadriculaSemanal(
                documento, pagina, clases, tituloGrupo, false
        );
    }

    private EstadoPagina cuadriculaSemanal(
            PDDocument documento,
            EstadoPagina pagina,
            List<InformeHorarioClaseDetalleResponse> clases,
            String tituloGrupo,
            boolean soloAsignatura)
            throws IOException {

        return cuadriculaSemanal(
                documento, pagina, clases, tituloGrupo,
                soloAsignatura, null
        );
    }

    private EstadoPagina cuadriculaSemanal(
            PDDocument documento,
            EstadoPagina pagina,
            List<InformeHorarioClaseDetalleResponse> clases,
            String tituloGrupo,
            boolean soloAsignatura,
            List<FranjaHorariaResponse> descansos)
            throws IOException {

        pagina = seccion(
                documento,
                pagina,
                tituloGrupo
        );

        List<InformeHorarioCeldaSemanalResponse> celdas =
                semanalService.construirCuadricula(clases);

        if (celdas.isEmpty()) {

            return linea(
                    documento,
                    pagina,
                    "No hay clases programadas "
                            + "para representar en la semana.",
                    9f
            );
        }

        Map<ClaveFranja, Map<String,
                List<InformeHorarioClaseDetalleResponse>>>
                matriz = new TreeMap<>(
                Comparator
                        .comparingInt(
                                (ClaveFranja clave) ->
                                        ordenJornada(
                                                clave.jornada()
                                        )
                        )
                        .thenComparing(ClaveFranja::inicio)
                        .thenComparing(ClaveFranja::fin)
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

        /*
         * Para la parrilla de un solo grupo, las franjas del Gestor
         * se agregan a la matriz aunque no haya una clase antes
         * o despues del descanso. No representan horas academicas.
         */
        Set<ClaveFranja> descansosOficiales = new HashSet<>();

        if (soloAsignatura && descansos != null) {

            Set<String> jornadasPresentes = new HashSet<>();

            for (InformeHorarioClaseDetalleResponse clase : clases) {
                if (clase.jornada() != null) {
                    jornadasPresentes.add(clase.jornada());
                }
            }

            for (FranjaHorariaResponse descanso : descansos) {

                if (descanso == null || !descanso.isEsDescanso()) {
                    continue;
                }

                LocalTime inicio = descanso.getHoraInicio();
                LocalTime fin = descanso.getHoraFin();

                if (inicio == null || fin == null || !inicio.isBefore(fin)) {
                    throw new IllegalArgumentException(
                            "La franja oficial de descanso tiene horas invalidas."
                    );
                }

                String jornada = jornadaDelDescanso(
                        descanso.getTurnoNombre(), jornadasPresentes
                );

                if (!jornadasPresentes.contains(jornada)) {
                    continue;
                }

                ClaveFranja claveDescanso =
                        new ClaveFranja(jornada, inicio, fin);

                for (ClaveFranja claseProgramada : matriz.keySet()) {

                    if (descansosOficiales.contains(claseProgramada)) {
                        continue;
                    }

                    if (Objects.equals(jornada, claseProgramada.jornada())
                            && claseProgramada.inicio().isBefore(fin)
                            && inicio.isBefore(claseProgramada.fin())) {

                        throw new IllegalStateException(
                                "Existe una clase programada durante "
                                        + "el descanso oficial."
                        );
                    }
                }

                descansosOficiales.add(claveDescanso);
                matriz.putIfAbsent(
                        claveDescanso,
                        new LinkedHashMap<>()
                );
            }
        }

        pagina = encabezadoSemanal(
                documento, pagina, soloAsignatura
        );

        ClaveFranja franjaAnterior = null;
        int numeroHoraVisual = 0;

        for (Map.Entry<ClaveFranja,
                Map<String, List<InformeHorarioClaseDetalleResponse>>>
                entrada : matriz.entrySet()) {

            ClaveFranja franja = entrada.getKey();

            /*
             * Fila de descanso oficial: visible de lunes a viernes,
             * pero sin incrementar el numero de clases.
             */
            if (soloAsignatura && descansosOficiales.contains(franja)) {

                float alturaDescanso = 25f;

                if (pagina.y - alturaDescanso < LIMITE) {
                    pagina = nuevaPagina(
                            documento, pagina, true
                    );

                    pagina = encabezadoSemanal(
                            documento, pagina, soloAsignatura
                    );
                }

                pagina = dibujarFilaSemanal(
                        pagina,
                        List.of(
                                List.of("—"),
                                List.of(
                                        hora(franja.inicio())
                                                + " - "
                                                + hora(franja.fin())
                                ),
                                List.of("DESCANSO"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO")
                        ),
                        alturaDescanso
                );

                continue;
            }

            if (soloAsignatura) {
                numeroHoraVisual++;
            }

            /*
             * Compatibilidad con la parrilla anterior: el descanso
             * historico solo aplica cuando el consumidor no envia
             * la lista oficial. Una lista vacia no lo activa.
             */
            if (soloAsignatura
                    && descansos == null
                    && franjaAnterior != null
                    && Objects.equals(
                    franjaAnterior.jornada(),
                    franja.jornada())
                    && franjaAnterior.fin().equals(
                    LocalTime.of(9, 15))
                    && franja.inicio().equals(
                    LocalTime.of(9, 45))) {

                float alturaDescanso = 25f;

                if (pagina.y - alturaDescanso < LIMITE) {

                    pagina = nuevaPagina(
                            documento, pagina, true
                    );

                    pagina = encabezadoSemanal(
                            documento, pagina, soloAsignatura
                    );
                }

                pagina = dibujarFilaSemanal(
                        pagina,
                        List.of(
                                List.of("—"),
                                List.of("09:15 - 09:45"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO"),
                                List.of("DESCANSO")
                        ),
                        alturaDescanso
                );
            }

            franjaAnterior = franja;

            /*
             * Si hay varias clases simultaneas,
             * se presentan en filas adicionales.
             *
             * Asi evitamos acumular demasiadas clases
             * dentro de una unica celda.
             */
            int cantidadFilas = entrada.getValue()
                    .values()
                    .stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(1);

            for (int indice = 0; indice < cantidadFilas; indice++) {

                List<String> valores = new ArrayList<>();

                valores.add(
                        indice == 0
                                ? (soloAsignatura
                                ? String.valueOf(numeroHoraVisual)
                                : franja.jornada())
                                : ""
                );

                valores.add(
                        indice == 0
                                ? hora(franja.inicio())
                                + " - "
                                + hora(franja.fin())
                                : ""
                );

                for (String dia : DIAS) {

                    List<InformeHorarioClaseDetalleResponse> clasesDelDia =
                            entrada.getValue()
                                    .getOrDefault(
                                            dia, List.of()
                                    );

                    valores.add(
                            indice < clasesDelDia.size()
                                    ? (soloAsignatura
                                    ? valor(
                                    clasesDelDia.get(indice)
                                            .nombreAsignatura()
                            )
                                    : textoClaseSemanal(
                                    clasesDelDia.get(indice)
                            ))
                                    : ""
                    );
                }

                List<List<String>> columnas =
                        new ArrayList<>();

                int maxLineas = 1;

                for (int c = 0; c < valores.size(); c++) {

                    List<String> fragmentos = envolver(
                            valores.get(c),
                            ANCHOS_SEMANA[c] - 8f,
                            NORMAL,
                            6.7f
                    );

                    columnas.add(fragmentos);

                    maxLineas = Math.max(
                            maxLineas,
                            fragmentos.size()
                    );
                }

                float altura = Math.max(
                        25f,
                        maxLineas * 9f + 9f
                );

                if (pagina.y - altura < LIMITE) {

                    pagina = nuevaPagina(
                            documento, pagina, true
                    );

                    pagina = seccion(
                            documento,
                            pagina,
                            "HORARIO SEMANAL - CONTINUACION"
                    );

                    pagina = encabezadoSemanal(
                            documento, pagina, soloAsignatura
                    );
                }

                if (pagina.y - altura < LIMITE) {

                    throw new IllegalStateException(
                            "Una celda semanal contiene "
                                    + "mas texto del disponible "
                                    + "en una pagina carta."
                    );
                }

                pagina = dibujarFilaSemanal(
                        pagina, columnas, altura
                );
            }
        }

        pagina.y -= 12f;

        return pagina;
    }

    private String textoClaseSemanal(
            InformeHorarioClaseDetalleResponse clase) {

        StringBuilder texto = new StringBuilder();

        texto.append(
                valor(clase.nombreAsignatura())
        );

        texto.append("\n");
        texto.append(
                valor(clase.nombreDocente())
        );

        texto.append("\nGrupo: ");
        texto.append(
                valor(clase.nombreGrupo())
        );

        texto.append("\nAula: ");
        texto.append(
                valor(clase.nombreAula())
        );

        if (clase.esHoraExtra()) {
            texto.append("\nHora extra");
        }

        texto.append("\n");
        texto.append(
                valor(clase.estadoHorario())
        );

        texto.append(" V");
        texto.append(
                valor(clase.versionHorario())
        );

        return texto.toString();
    }

    private EstadoPagina encabezadoSemanal(
            PDDocument documento,
            EstadoPagina pagina,
            boolean soloAsignatura)
            throws IOException {

        pagina = asegurarEspacio(
                documento, pagina, 40f
        );

        float ySuperior = pagina.y;
        float alturaCabecera = 26f;
        float yInferior = ySuperior - alturaCabecera;

        // Bordes horizontales del encabezado.
        dibujarLinea(
                pagina.stream,
                MARGEN,
                ySuperior,
                MARGEN + ANCHO_UTIL,
                ySuperior
        );

        dibujarLinea(
                pagina.stream,
                MARGEN,
                yInferior,
                MARGEN + ANCHO_UTIL,
                yInferior
        );

        // Borde izquierdo.
        float x = MARGEN;

        dibujarLinea(
                pagina.stream,
                x,
                ySuperior,
                x,
                yInferior
        );

        // Encabezados y separadores verticales.
        for (int i = 0; i < CABECERAS_SEMANA.length; i++) {

            escribir(
                    pagina.stream,
                    soloAsignatura && i == 0
                            ? "Clase"
                            : CABECERAS_SEMANA[i],
                    x + 4f,
                    ySuperior - 16f,
                    NEGRITA,
                    7.5f
            );

            x += ANCHOS_SEMANA[i];

            dibujarLinea(
                    pagina.stream,
                    x,
                    ySuperior,
                    x,
                    yInferior
            );
        }

        // La primera fila empieza exactamente debajo
        // del encabezado, sin espacios entre bordes.
        pagina.y = yInferior;

        return pagina;
    }

    private EstadoPagina dibujarFilaSemanal(
            EstadoPagina pagina,
            List<List<String>> columnas,
            float altura)
            throws IOException {

        float x = MARGEN;

        for (int columna = 0;
             columna < columnas.size();
             columna++) {

            float yTexto = pagina.y - 11f;

            for (String fragmento : columnas.get(columna)) {

                escribir(
                        pagina.stream,
                        fragmento,
                        x + 4f,
                        yTexto,
                        NORMAL,
                        6.7f
                );

                yTexto -= 9f;
            }

            x += ANCHOS_SEMANA[columna];
        }

        float ySuperior = pagina.y;
        float yInferior = pagina.y - altura;

        // Borde horizontal superior e inferior.
        dibujarLinea(
                pagina.stream,
                MARGEN,
                ySuperior,
                MARGEN + ANCHO_UTIL,
                ySuperior
        );

        dibujarLinea(
                pagina.stream,
                MARGEN,
                yInferior,
                MARGEN + ANCHO_UTIL,
                yInferior
        );

        // Bordes verticales y separadores entre columnas.
        float posicionX = MARGEN;

        dibujarLinea(
                pagina.stream,
                posicionX,
                ySuperior,
                posicionX,
                yInferior
        );

        for (float anchoColumna : ANCHOS_SEMANA) {

            posicionX += anchoColumna;

            dibujarLinea(
                    pagina.stream,
                    posicionX,
                    ySuperior,
                    posicionX,
                    yInferior
            );
        }

        pagina.y -= altura;

        return pagina;
    }

    /**
     * Detalle completo de clases programadas.
     *
     * Mantiene cada registro identificable,
     * aunque el informe ocupe varias paginas.
     */
    private EstadoPagina detalleClases(
            PDDocument documento,
            EstadoPagina pagina,
            InformeHorariosResponse informe)
            throws IOException {

        pagina = seccion(
                documento,
                pagina,
                "DETALLE DE CLASES PROGRAMADAS"
        );

        if (informe.clasesProgramadas().isEmpty()) {

            return linea(
                    documento,
                    pagina,
                    "Sin clases programadas.",
                    9f
            );
        }

        for (InformeHorarioClaseDetalleResponse clase :
                informe.clasesProgramadas()) {

            String descripcion =
                    valor(clase.diaSemana())
                            + " | "
                            + hora(clase.horaInicio())
                            + " - "
                            + hora(clase.horaFin())
                            + " | Docente: "
                            + valor(clase.nombreDocente())
                            + " | Grado: "
                            + valor(clase.grado())
                            + " | Grupo: "
                            + valor(clase.nombreGrupo())
                            + " | Asignatura: "
                            + valor(clase.nombreAsignatura())
                            + " | Aula: "
                            + valor(clase.nombreAula())
                            + " | Jornada: "
                            + valor(clase.jornada())
                            + " | Extra: "
                            + (clase.esHoraExtra() ? "Si" : "No")
                            + " | Estado: "
                            + valor(clase.estadoHorario())
                            + " | Version: "
                            + valor(clase.versionHorario());

            pagina = linea(
                    documento,
                    pagina,
                    descripcion,
                    8f
            );
        }

        return pagina;
    }

    private EstadoPagina resumenCarga(
            PDDocument documento,
            EstadoPagina pagina,
            InformeHorariosResponse informe)
            throws IOException {

        pagina = seccion(
                documento,
                pagina,
                "RESUMEN DE CARGA ACADEMICA"
        );

        pagina = linea(
                documento,
                pagina,
                "Clases programadas: "
                        + valor(informe.totalClasesProgramadas()),
                9f
        );

        pagina = linea(
                documento,
                pagina,
                "Horas academicas programadas: "
                        + valor(informe.horasAcademicasProgramadas()),
                9f
        );

        pagina = linea(
                documento,
                pagina,
                "Jornada regular: "
                        + valor(informe.horasAcademicasJornadaRegular()),
                9f
        );

        pagina = linea(
                documento,
                pagina,
                "Contrajornada: "
                        + valor(informe.horasAcademicasContrajornada()),
                9f
        );

        pagina = linea(
                documento,
                pagina,
                "Las horas por jornada no deben "
                        + "sumarse nuevamente al total.",
                8f
        );

        for (InformeHorarioCargaResponse carga :
                informe.resumenCargaHoraria()) {

            String descripcion =
                    valor(carga.nombreDocente())
                            + " | Grupo: "
                            + valor(carga.nombreGrupo())
                            + " | Asignatura: "
                            + valor(carga.nombreAsignatura())
                            + " | Ordinarias: "
                            + valor(carga.horasOrdinarias())
                            + " | Extras: "
                            + valor(carga.horasExtras())
                            + " | Total: "
                            + valor(carga.totalHorasAcademicas());

            pagina = linea(
                    documento,
                    pagina,
                    descripcion,
                    8f
            );
        }

        return pagina;
    }

    /**
     * Trazabilidad final con el mismo criterio
     * utilizado en los informes anteriores.
     */
    private EstadoPagina escribirMetadatos(
            PDDocument documento,
            EstadoPagina pagina,
            MetadatosInformeResponse metadatos)
            throws IOException {

        if (metadatos == null) {
            return pagina;
        }

        pagina.y -= 10f;

        pagina = seccion(
                documento,
                pagina,
                "TRAZABILIDAD DEL INFORME"
        );

        List<String> campos = new ArrayList<>();

        anadir(
                campos,
                "Institucion",
                metadatos.nombreInstitucion()
        );

        anadir(
                campos,
                "Sede",
                metadatos.nombreSede()
        );

        anadir(
                campos,
                "Codigo de sede",
                metadatos.codigoSede()
        );

        if (metadatos.filtrosAplicados() != null) {

            metadatos.filtrosAplicados()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(filtro ->
                            anadir(
                                    campos,
                                    "Filtro " + filtro.getKey(),
                                    filtro.getValue()
                            )
                    );
        }

        if (metadatos.fechaHoraGeneracion() != null) {

            campos.add(
                    "Fecha de generacion: "
                            + metadatos.fechaHoraGeneracion()
                            .format(FORMATO_FECHA)
            );
        }

        anadir(
                campos,
                "Generado por",
                metadatos.generadoPor()
        );

        for (String campo : campos) {

            pagina = linea(
                    documento,
                    pagina,
                    campo,
                    8f
            );
        }

        return pagina;
    }

    private void anadir(
            List<String> campos,
            String etiqueta,
            String contenido) {

        if (contenido != null && !contenido.isBlank()) {

            campos.add(
                    etiqueta + ": " + contenido.trim()
            );
        }
    }

    private EstadoPagina seccion(
            PDDocument documento,
            EstadoPagina pagina,
            String texto)
            throws IOException {

        pagina = asegurarEspacio(
                documento, pagina, 34f
        );

        escribir(
                pagina.stream,
                texto,
                MARGEN,
                pagina.y,
                NEGRITA,
                10f
        );

        pagina.y -= 24f;

        return pagina;
    }

    /**
     * Escribe texto con ajuste de ancho.
     * Si no cabe, continua en la pagina siguiente.
     */
    private EstadoPagina linea(
            PDDocument documento,
            EstadoPagina pagina,
            String contenido,
            float tamano)
            throws IOException {

        List<String> fragmentos = envolver(
                contenido,
                ANCHO_UTIL,
                NORMAL,
                tamano
        );

        for (String fragmento : fragmentos) {

            pagina = asegurarEspacio(
                    documento, pagina, 18f
            );

            escribir(
                    pagina.stream,
                    fragmento,
                    MARGEN,
                    pagina.y,
                    NORMAL,
                    tamano
            );

            pagina.y -= 15f;
        }

        return pagina;
    }

    private EstadoPagina asegurarEspacio(
            PDDocument documento,
            EstadoPagina pagina,
            float necesario)
            throws IOException {

        if (pagina.y - necesario < LIMITE) {

            return nuevaPagina(
                    documento, pagina, true
            );
        }

        return pagina;
    }

    /**
     * Cierra el flujo anterior antes de abrir
     * una nueva pagina.
     */
    private EstadoPagina nuevaPagina(
            PDDocument documento,
            EstadoPagina anterior,
            boolean continuacion)
            throws IOException {

        return nuevaPagina(
                documento,
                anterior,
                continuacion,
                TITULO
        );
    }

    private EstadoPagina nuevaPagina(
            PDDocument documento,
            EstadoPagina anterior,
            boolean continuacion,
            String titulo)
            throws IOException {

        if (anterior != null) {
            anterior.stream.close();
        }

        PDPage hoja = new PDPage(PAGINA);
        documento.addPage(hoja);

        PDPageContentStream stream =
                new PDPageContentStream(documento, hoja);

        float y = PAGINA.getHeight() - MARGEN;

        escribir(
                stream,
                titulo,
                MARGEN,
                y,
                NEGRITA,
                14f
        );

        y -= 25f;

        if (continuacion) {

            String textoContinuacion = TITULO.equals(titulo)
                    ? "Continuacion del informe"
                    : "Continuación del horario";

            escribir(
                    stream,
                    textoContinuacion,
                    MARGEN,
                    y,
                    NORMAL,
                    9f
            );

            y -= 22f;
        }

        return new EstadoPagina(stream, y);
    }

    /**
     * Numera el documento una vez creadas
     * todas sus paginas.
     */
    private void numerarPaginas(
            PDDocument documento)
            throws IOException {

        int total = documento.getNumberOfPages();

        for (int i = 0; i < total; i++) {

            PDPage hoja = documento.getPage(i);

            try (PDPageContentStream stream =
                         new PDPageContentStream(
                                 documento,
                                 hoja,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                escribir(
                        stream,
                        "Pagina " + (i + 1)
                                + " de " + total,
                        PAGINA.getWidth()
                                - MARGEN - 90f,
                        28f,
                        NORMAL,
                        8f
                );
            }
        }
    }

    /**
     * Ajusta el texto a un ancho determinado.
     *
     * Divide palabras largas cuando es necesario.
     */
    private List<String> envolver(
            String texto,
            float ancho,
            PDFont fuente,
            float tamano)
            throws IOException {

        String contenido = valor(texto).trim();

        if (contenido.isEmpty()) {
            return List.of("");
        }

        List<String> resultado = new ArrayList<>();
        StringBuilder linea = new StringBuilder();

        for (String palabra : contenido.split("\\s+")) {

            String candidato = linea.isEmpty()
                    ? palabra
                    : linea + " " + palabra;

            if (medir(candidato, fuente, tamano) <= ancho) {

                linea.setLength(0);
                linea.append(candidato);

            } else {

                if (!linea.isEmpty()) {
                    resultado.add(linea.toString());
                    linea.setLength(0);
                }

                for (char caracter : palabra.toCharArray()) {

                    String intento = linea.toString() + caracter;

                    if (!linea.isEmpty()
                            && medir(intento, fuente, tamano) > ancho) {

                        resultado.add(linea.toString());
                        linea.setLength(0);
                    }

                    linea.append(caracter);
                }
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

        String preparado = PdfTextoEspanol.preparar(
                texto, fuente
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

        String preparado = PdfTextoEspanol.preparar(
                valor(texto), fuente
        );

        stream.beginText();
        stream.setFont(fuente, tamano);
        stream.newLineAtOffset(x, y);
        stream.showText(preparado);
        stream.endText();
    }

    private void dibujarLinea(
            PDPageContentStream stream,
            float x1,
            float y1,
            float x2,
            float y2)
            throws IOException {

        stream.setLineWidth(0.4f);
        stream.moveTo(x1, y1);
        stream.lineTo(x2, y2);
        stream.stroke();
    }

    private int ordenJornada(String jornada) {

        return "REGULAR".equals(jornada) ? 0 : 1;
    }

    private String hora(LocalTime valor) {

        return valor == null
                ? ""
                : valor.format(FORMATO_HORA);
    }

    private String valor(Object dato) {

        return dato == null ? "" : dato.toString();
    }

    private record ClaveFranja(
            String jornada,
            LocalTime inicio,
            LocalTime fin
    ) {
    }

    /**
     * Traduce el turno de la franja al valor de jornada de HU-04.
     */
    private String jornadaDelDescanso(
            String turnoNombre,
            Set<String> jornadasPresentes) {

        if (turnoNombre == null || turnoNombre.isBlank()) {

            if (jornadasPresentes.size() == 1) {
                return jornadasPresentes.iterator().next();
            }

            throw new IllegalArgumentException(
                    "No es posible asociar el descanso con una jornada: "
                            + "falta el turno."
            );
        }

        String nombre = java.text.Normalizer.normalize(
                turnoNombre.trim().toUpperCase(java.util.Locale.ROOT),
                java.text.Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return switch (nombre) {

            case "REGULAR", "MANANA", "TARDE" ->
                    "REGULAR";

            case "CONTRAJORNADA" ->
                    "CONTRAJORNADA";

            default -> throw new IllegalArgumentException(
                    "Turno de descanso no reconocido: " + turnoNombre
            );
        };
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

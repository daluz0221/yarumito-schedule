package co.edu.ieruralyarumito.backend.entity;

// Importa las anotaciones necesarias para mapear Docente con JPA/Hibernate.
import jakarta.persistence.Entity; // Marca una clase como entidad
import jakarta.persistence.Id; // Define la clave primaria
import jakarta.persistence.GeneratedValue; // Permite generar automáticamente el identificador
import jakarta.persistence.GenerationType; // Define la estrategia de generación
import jakarta.persistence.Column; // Configura las columnas de la tabla
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion; // Enum de vinculación docente
import jakarta.persistence.EnumType; // Define cómo se guarda el enum
import jakarta.persistence.Enumerated; // Permite mapear un enum con JPA
import jakarta.persistence.FetchType; // Define cuándo se carga la relación
import jakarta.persistence.JoinColumn; // Define la columna FK
import jakarta.persistence.ManyToOne; // Relación muchos Docentes a un Area
import java.time.LocalDate; // Tipo de fecha sin hora
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente; // Enum de estado del docente

// Importa UUID para utilizar identificadores únicos.
import java.util.UUID; // Tipo del identificador

// Representa el perfil laboral y académico del docente.
@Entity // Indica que Docente es una entidad persistente
public class Docente {

    // Identificador único del docente.
    @Id // Clave primaria
    @GeneratedValue(strategy = GenerationType.UUID) // Genera automáticamente un UUID
    private UUID id; // Identificador UUID del docente

    // Información personal básica del docente.
    @Column(nullable = false) // Campo obligatorio
    private String nombres; // Nombres del docente

    @Column(nullable = false) // Campo obligatorio
    private String apellidos; // Apellidos del docente

    @Column(name = "tipo_documento", nullable = false) // Campo obligatorio; mapea tipo_documento
    private String tipoDocumento; // Tipo de documento del docente

    @Column(name = "numero_documento", nullable = false, unique = true) // Campo obligatorio y único
    private String numeroDocumento; // Número de documento del docente

    private String telefono; // Teléfono de contacto del docente

    @Column(name = "correo_institucional") // Mapea la columna correo_institucional
    private String correoInstitucional; // Correo institucional de contacto

    // Información institucional del docente.
    @Enumerated(EnumType.STRING) // Guarda PLANTA, PROVISIONAL o CONTRATO como texto
    @Column(name = "tipo_vinculacion", nullable = false) // Campo obligatorio; mapea tipo_vinculacion
    private TipoVinculacion tipoVinculacion; // Tipo de vinculación del docente

    // Área de nombramiento obligatoria del docente.
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Muchos docentes pueden pertenecer a un área
    @JoinColumn(name = "area_nombramiento_id", nullable = false) // FK obligatoria hacia Area
    private Area areaNombramiento; // Área de nombramiento del docente

    @Column(name = "numero_decreto") // Mapea la columna numero_decreto
    private String numeroDecreto; // Número del decreto de vinculación

    @Column(name = "fecha_decreto") // Mapea la columna fecha_decreto
    private LocalDate fechaDecreto; // Fecha del decreto de vinculación

    private String escalafon; // Escalafón docente

    // Parámetros de carga laboral del docente.
    @Column(name = "horas_semanales_contratadas", nullable = false) // Campo obligatorio
    private int horasSemanalesContratadas = 22; // Carga semanal ordinaria por defecto

    @Column(name = "max_horas_extra", nullable = false) // Campo obligatorio
    private int maxHorasExtra = 10; // Máximo de horas extra permitidas por defecto

    @Column(name = "es_exclusivo_media_tecnica", nullable = false) // Campo obligatorio
    private boolean esExclusivoMediaTecnica = false; // Indica si el docente es exclusivo de media técnica

    // Estado administrativo actual del docente.
    @Enumerated(EnumType.STRING) // Guarda ACTIVO, LICENCIA o RETIRADO como texto
    @Column(nullable = false) // Campo obligatorio
    private EstadoDocente estado; // Estado actual del docente

    @Column(name = "fecha_vinculacion") // Mapea la columna fecha_vinculacion
    private LocalDate fechaVinculacion; // Fecha de inicio de vinculación del docente





}

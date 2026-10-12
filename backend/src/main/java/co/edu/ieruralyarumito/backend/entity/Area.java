package co.edu.ieruralyarumito.backend.entity;

// Importa las anotaciones necesarias para mapear Area con JPA/Hibernate.
import jakarta.persistence.Entity; // Marca una clase como entidad
import jakarta.persistence.Id; // Define la clave primaria
import jakarta.persistence.Column; // Configura las columnas de la tabla
import jakarta.persistence.GeneratedValue; // Permite generar automáticamente el identificador
import jakarta.persistence.GenerationType; // Define la estrategia de generación

// Importa UUID para utilizar identificadores únicos.
import java.util.UUID; // Tipo del identificador

// Representa el área académica definida en el Modelo ER
@Entity   // Indica que Area es una entidad persistente
public class Area {

    // Identificador único del área.
    @Id //clave primaria
    @GeneratedValue(strategy = GenerationType.UUID) // Genera automáticamente un UUID
    private UUID id; // Identificador UUID del Area

    // Información básica del área
    @Column(nullable = false)
    private String nombre; // Nombre del área

    @Column(nullable = false, unique = true)
    private String codigo; // Código del área

    // Define las características académicas del área.
    @Column(nullable = false)
    private boolean obligatoria; // Indica si el área es obligatoria

    @Column(name = "solo_media", nullable = false) // Mapea la columna solo_media
    private boolean soloMedia;  // Indica si aplica solo a educación media

    @Column(nullable = false)
    private boolean activa; // Indica si el área está activa

    // Permite consultar el identificador del área.
    public UUID getId() {
        return id; // Retorna el UUID del área
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isObligatoria() {
        return obligatoria;
    }

    public void setObligatoria(boolean obligatoria) {
        this.obligatoria = obligatoria;
    }

    public boolean isSoloMedia() {
        return soloMedia;
    }

    public void setSoloMedia(boolean soloMedia) {
        this.soloMedia = soloMedia;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}

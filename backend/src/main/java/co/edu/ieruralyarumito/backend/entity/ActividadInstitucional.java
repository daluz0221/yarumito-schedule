package co.edu.ieruralyarumito.backend.entity;


import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import co.edu.ieruralyarumito.backend.entity.enums.TipoActividadInstitucional;

@Entity 
@Table(name = "actividadinstitucional", schema = "asignacionyhorario")
public class ActividadInstitucional {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "anio_escolar_id")
    private AnioEscolar anioEscolarId;

    @Column (name = "nombre", nullable = false)
    private String nombre;

    @Column (name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoActividadInstitucional tipo;

    @Column (name = "descripcion")
    private String descripcion;

    @Column (name = "horas_semanales", nullable = false)
    private int horasSemanales;

    public ActividadInstitucional(){}

    public UUID getId() {
        return id;
    }

    public AnioEscolar getAnioEscolarId() {
        return anioEscolarId;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoActividadInstitucional getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setAnioEscolarId(AnioEscolar anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(TipoActividadInstitucional tipo) {
        this.tipo = tipo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales;
    }
}

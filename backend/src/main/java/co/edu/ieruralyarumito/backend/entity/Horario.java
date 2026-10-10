package co.edu.ieruralyarumito.backend.entity;

import java.util.UUID;
import java.time.LocalDateTime;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoHorario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;


@Entity 
public class Horario {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "anio_escolar_id")
    private AnioEscolar anioEscolarId;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "sede_id")
    private Sede sedeId;

    @Column (name = "version", nullable = false)
    private int version;

    @Column (name = "nombre", nullable = false)
    private String nombre;
    
    @Column (name = "estado", nullable = false)
    private EstadoHorario estado;

    @Column (name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "creado_por")
    private Usuario creadoPor;
    
    @Column (name = "notas")
    private String notas;
    
    public Horario(){}

    public UUID getId(){
        return this.id;
    }

    public int getVersion(){
        return this.version;
    }

    public void setVersion(int version){
        this.version = version;
    }

    public String getNombre(){
        return this.nombre;
    }    

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public EstadoHorario getEstado(){
        return this.estado;
    }

    public void setEstado(EstadoHorario estado){
        this.estado = estado;
    }

    public LocalDateTime getFechaPublicacion(){
        return this.fechaPublicacion;
    }

    public String getNotas(){
        return this.notas;
    }

    public void setNotas(String notas){
        this.notas = notas;
    }
}

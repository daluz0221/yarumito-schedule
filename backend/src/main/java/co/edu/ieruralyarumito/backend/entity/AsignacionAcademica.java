package co.edu.ieruralyarumito.backend.entity;

import java.util.UUID;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table (
    name = "asignacionAcademica",
    schema = "asignacionyhorario"
)
public class AsignacionAcademica {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "anio_escolar_id")
    private AnioEscolar anioEscolarId; 

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "docente_id")
    private Docente docenteId;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "grupo_id")
    private Grupo grupoId;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "asignatura_id")
    private Asignatura asignaturaId;

    @Column (name = "horas_semanales", nullable = false)
    private int horasSemanales;

    @Column (name = "horas_extras", nullable = false)
    private int horasExtras = 0;

    @Column (name = "estado", nullable = false)
    private EstadoAsignacionAcademica estado;

    @Column (name = "observacion", nullable = false)
    private  String observacion;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "asignada_por")
    private Usuario asignadaPor;

    public AsignacionAcademica(){}

    public int getHorasSemanales(){
        return this.horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales){
        this.horasSemanales = horasSemanales;
    }

    public int getHorasExtras(){
        return this.horasExtras;
    }

    public void setHorasExtras(int horasExtras){
        this.horasExtras = horasExtras;
    }

    public EstadoAsignacionAcademica getEstado(){
        return this.estado;
    }

    public String getObservacion(){
        return this.observacion;
    }

    public void setObservacion(String observacion){
        this.observacion = observacion;
    }
    
}

package co.edu.ieruralyarumito.backend.entity;

import java.util.UUID;

import co.edu.ieruralyarumito.backend.entity.enums.DiasSemana;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table (
    name = "claseprogramada",
    schema = "asignacionyhorario"    
)
public class ClaseProgramada {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "horario_id")
    private Horario horarioId;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "asignacion_academica_id")
    private AsignacionAcademica asignacionAcademicaId;
    
    @Column (name = "dia", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiasSemana dia;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "franja_horaria_id")
    private FranjaHoraria franjaHorariaId;

    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "aula_id")
    private Aula aulaId;

    @Column (name = "es_hora_extra", nullable = false)
    private boolean esHoraExtra;

    @Column (name = "bloqueada", nullable = false)
    private boolean bloqueada;

    public ClaseProgramada(){}

    public UUID getId() {
        return id;
    }

    public Horario getHorarioId() {
        return this.horarioId;
    }

    public void setHorarioId(Horario horarioId) {
        this.horarioId = horarioId;
    }

    public AsignacionAcademica getAsignacionAcademicaId() {
        return this.asignacionAcademicaId;
    }

    public void setAsignacionAcademicaId(AsignacionAcademica asignacionAcademicaId) {
        this.asignacionAcademicaId = asignacionAcademicaId;
    }

    public DiasSemana getDia() {
        return this.dia;
    }

    public void setDia(DiasSemana dia) {
        this.dia = dia;
    }

    public FranjaHoraria getFranjaHorariaId() {
        return this.franjaHorariaId;
    }

    public void setFranjaHorariaId(FranjaHoraria franjaHorariaId) {
        this.franjaHorariaId = franjaHorariaId;
    }

    public Aula getAulaId() {
        return this.aulaId;
    }

    public void setAulaId(Aula aulaId) {
        this.aulaId = aulaId;
    }

    public boolean isEsHoraExtra() {
        return this.esHoraExtra;
    }

    public void setEsHoraExtra(boolean esHoraExtra) {
        this.esHoraExtra = esHoraExtra;
    }

    public boolean isBloqueada() {
        return this.bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

}

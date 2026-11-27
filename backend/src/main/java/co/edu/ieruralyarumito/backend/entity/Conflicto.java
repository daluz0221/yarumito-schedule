package co.edu.ieruralyarumito.backend.entity;

import co.edu.ieruralyarumito.backend.entity.enums.SeveridadConflicto;
import co.edu.ieruralyarumito.backend.entity.enums.TipoConflicto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

// Representa un conflicto materializado detectado
// durante las validaciones del horario.
@Entity
@Table(name = "conflicto", schema = "controlyauditoria")
public class Conflicto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horario_id", nullable = false)
    private Horario horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConflicto tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeveridadConflicto severidad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private boolean resuelto = false;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDateTime fechaDeteccion;

    @Column(length = 20)
    private String dia;

    @Column(length = 40)
    private String franja;

    @Column(name = "elemento_afectado", length = 160)
    private String elementoAfectado;

    @Column(length = 160)
    private String asignatura;

    @Column(length = 160)
    private String docente;

    @Column(length = 160)
    private String aula;

    @Column(length = 40)
    private String grupo;

    public UUID getId() {
        return id;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public TipoConflicto getTipo() {
        return tipo;
    }

    public void setTipo(TipoConflicto tipo) {
        this.tipo = tipo;
    }

    public SeveridadConflicto getSeveridad() {
        return severidad;
    }

    public void setSeveridad(SeveridadConflicto severidad) {
        this.severidad = severidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isResuelto() {
        return resuelto;
    }

    public void setResuelto(boolean resuelto) {
        this.resuelto = resuelto;
    }

    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }

    public void setFechaDeteccion(LocalDateTime fechaDeteccion) {
        this.fechaDeteccion = fechaDeteccion;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getFranja() {
        return franja;
    }

    public void setFranja(String franja) {
        this.franja = franja;
    }

    public String getElementoAfectado() {
        return elementoAfectado;
    }

    public void setElementoAfectado(String elementoAfectado) {
        this.elementoAfectado = elementoAfectado;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}

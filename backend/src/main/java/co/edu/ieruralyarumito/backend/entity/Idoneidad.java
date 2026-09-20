package co.edu.ieruralyarumito.backend.entity;

import co.edu.ieruralyarumito.backend.entity.enums.TipoIdoneidad;
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
import java.time.LocalDate;
import java.util.UUID;

// Representa la habilitación de un docente para un área académica.
@Entity
public class Idoneidad {

    // Identificador único de la idoneidad.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Docente al que pertenece la habilitación.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    // Área académica cubierta por la idoneidad.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    // Asignatura específica cubierta por la idoneidad, cuando aplica.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;

    // Tipo de idoneidad asignada al docente.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoIdoneidad tipo;

    // Título profesional que respalda la idoneidad, cuando existe.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_soporte_id")
    private TituloProfesional tituloSoporte;

    // Justificación de la idoneidad, especialmente cuando es excepcional.
    @Column(columnDefinition = "TEXT")
    private String justificacion;

    // Fecha desde la cual la idoneidad entra en vigencia.
    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    // Fecha hasta la cual la idoneidad está vigente; null indica vigencia actual.
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    // Usuario que aprueba la idoneidad, cuando aplica.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobada_por")
    private Usuario aprobadaPor;

    public UUID getId() {
        return id;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
    }

    public TipoIdoneidad getTipo() {
        return tipo;
    }

    public void setTipo(TipoIdoneidad tipo) {
        this.tipo = tipo;
    }

    public TituloProfesional getTituloSoporte() {
        return tituloSoporte;
    }

    public void setTituloSoporte(TituloProfesional tituloSoporte) {
        this.tituloSoporte = tituloSoporte;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public void setVigenteDesde(LocalDate vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }

    public void setVigenteHasta(LocalDate vigenteHasta) {
        this.vigenteHasta = vigenteHasta;
    }

    public Usuario getAprobadaPor() {
        return aprobadaPor;
    }

    public void setAprobadaPor(Usuario aprobadaPor) {
        this.aprobadaPor = aprobadaPor;
    }
}

package co.edu.ieruralyarumito.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

// Representa la intensidad semanal esperada de una asignatura
// para un grado dentro de un año escolar.
@Entity
@Table(
        name = "plan_estudios",
        schema = "catalogoacademico",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_estudios_anio_grado_asignatura",
                        columnNames = {
                                "anio_escolar_id",
                                "grado_id",
                                "asignatura_id"
                        }
                )
        }
)
public class PlanEstudios {

    // Identificador único del registro del plan de estudios.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Año escolar al que pertenece la planificación.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anio_escolar_id")
    private AnioEscolar anioEscolar;

    // Grado al que aplica la asignatura.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grado_id")
    private Grado grado;

    // Asignatura incluida en el plan.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura_id")
    private Asignatura asignatura;

    // Intensidad semanal esperada de la asignatura.
    @Column(name = "horas_semanales", nullable = false)
    private int horasSemanales;

    // Turno esperado para la asignatura.
    // Se conserva temporalmente como UUID hasta implementar Turno.
    @Column(name = "turno_id")
    private UUID turnoId;

    // Observaciones adicionales del plan de estudios.
    @Column(columnDefinition = "TEXT")
    private String observacion;

    public UUID getId() {
        return id;
    }

    public AnioEscolar getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(AnioEscolar anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
    }

    public int getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(int horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public UUID getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(UUID turnoId) {
        this.turnoId = turnoId;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}

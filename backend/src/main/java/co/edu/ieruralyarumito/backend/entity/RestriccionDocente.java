package co.edu.ieruralyarumito.backend.entity;

import co.edu.ieruralyarumito.backend.entity.enums.DiaSemana;
import co.edu.ieruralyarumito.backend.entity.enums.TipoRestriccionDocente;

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

import java.util.UUID;

// Representa una condición de disponibilidad del docente
// para un día y una franja horaria específicos.
@Entity
@Table(name = "restriccion_docente", schema = "controlyauditoria")
public class RestriccionDocente {

    // Identificador único de la restricción.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Docente al que aplica la restricción.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    // Año escolar en el cual aplica la restricción.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    // Día lectivo en el cual aplica la restricción.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaSemana dia;

    // Franja horaria sobre la cual aplica la restricción.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "franja_horaria_id", nullable = false)
    private FranjaHoraria franjaHoraria;

    // Tipo de condición:
    // NO_DISPONIBLE o PREFERENTE.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRestriccionDocente tipo;

    // Justificación obligatoria de la restricción.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    // Documento soporte almacenado directamente
    // en PostgreSQL como bytea.
    @Column(name = "documento_soporte")
    private byte[] documentoSoporte;

    // Rector autenticado que incorpora o valida
    // la restricción para efectos de programación académica.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobada_por")
    private Usuario aprobadaPor;

    // H-04: permite retirar una restricción docente
    // sin eliminar físicamente su registro histórico.
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public UUID getId() {
        return id;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public AnioEscolar getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(AnioEscolar anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public DiaSemana getDia() {
        return dia;
    }

    public void setDia(DiaSemana dia) {
        this.dia = dia;
    }

    public FranjaHoraria getFranjaHoraria() {
        return franjaHoraria;
    }

    public void setFranjaHoraria(FranjaHoraria franjaHoraria) {
        this.franjaHoraria = franjaHoraria;
    }

    public TipoRestriccionDocente getTipo() {
        return tipo;
    }

    public void setTipo(TipoRestriccionDocente tipo) {
        this.tipo = tipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public byte[] getDocumentoSoporte() {
        return documentoSoporte;
    }

    public void setDocumentoSoporte(byte[] documentoSoporte) {
        this.documentoSoporte = documentoSoporte;
    }

    public Usuario getAprobadaPor() {
        return aprobadaPor;
    }

    public void setAprobadaPor(Usuario aprobadaPor) {
        this.aprobadaPor = aprobadaPor;
    }

    // H-04: consulta si la restricción está activa.
    public boolean isActivo() {
        return activo;
    }

    // H-04: permite retirar o reactivar la restricción
    // conservando su información histórica.
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

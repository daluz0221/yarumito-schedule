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

import java.util.UUID;

// Representa un parámetro institucional aplicable a un año escolar.
@Entity
@Table(name = "parametroInstitucional", schema = "controlyauditoria")
public class ParametroInstitucional {

    // Identificador único del parámetro institucional.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Año escolar al cual aplica el parámetro.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    // Clave estable utilizada internamente por el sistema.
    @Column(nullable = false)
    private String clave;

    // Valor almacenado de forma serializada.
    @Column(nullable = false)
    private String valor;

    // Tipo técnico utilizado para interpretar el valor:
    // INT, DECIMAL, BOOL o STRING.
    @Column(name = "tipo_dato", nullable = false)
    private String tipoDato;

    // Explicación funcional del parámetro.
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    public UUID getId() {
        return id;
    }

    public AnioEscolar getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(AnioEscolar anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

package co.edu.ieruralyarumito.backend.entity;

import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
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

// Representa un espacio físico disponible para la programación académica.
@Entity
@Table(name = "aula", schema = "catalogoacademico")
public class Aula {

    // Identificador único del aula.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Sede física a la cual pertenece el aula.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    // Nombre institucional del espacio.
    @Column(nullable = false)
    private String nombre;

    // Tipo oficial de espacio físico.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAulaRequerida tipo;

    // Capacidad máxima registrada del espacio.
    // Puede ser null cuando la institución no tenga aforo definido.
    @Column
    private Integer capacidad;

    // Indica si el espacio se encuentra disponible para nuevas asignaciones.
    @Column(nullable = false)
    private boolean activa;

    public UUID getId() {
        return id;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoAulaRequerida getTipo() {
        return tipo;
    }

    public void setTipo(TipoAulaRequerida tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}

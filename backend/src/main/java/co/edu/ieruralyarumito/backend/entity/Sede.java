package co.edu.ieruralyarumito.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

// Representa una sede de la institución educativa.
@Entity
@Table(name = "sede", schema = "catalogoacademico")
public class Sede {

    // Identificador único de la sede.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Nombre de la sede.
    @Column(nullable = false, length = 100)
    private String nombre;

    // Código único de la sede.
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    // Dirección de la sede.
    @Column(length = 100)
    private String direccion;

    // Indica si corresponde a la sede principal de la institución.
    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal;

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

}

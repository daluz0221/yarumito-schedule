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

import java.time.LocalDateTime;
import java.util.UUID;

// Representa un registro histórico de auditoría sobre cambios relevantes del sistema.
@Entity
@Table(name = "registro_auditoria", schema = "controlyauditoria")
public class RegistroAuditoria {

    // Identificador único del registro de auditoría.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Usuario autenticado que ejecutó la acción.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Acción realizada sobre la entidad.
    @Column(nullable = false)
    private String accion;

    // Nombre de la entidad afectada.
    @Column(nullable = false)
    private String entidad;

    // Identificador del registro afectado.
    @Column(name = "id_entidad", nullable = false)
    private UUID idEntidad;

    // Estado de la entidad antes del cambio.
    @Column(name = "datos_antes", columnDefinition = "jsonb")
    private String datosAntes;

    // Estado de la entidad después del cambio.
    @Column(name = "datos_despues", columnDefinition = "jsonb")
    private String datosDespues;

    // Momento en que se registró la acción.
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Dirección IP de origen, cuando esté disponible.
    @Column
    private String ip;

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public UUID getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(UUID idEntidad) {
        this.idEntidad = idEntidad;
    }

    public String getDatosAntes() {
        return datosAntes;
    }

    public void setDatosAntes(String datosAntes) {
        this.datosAntes = datosAntes;
    }

    public String getDatosDespues() {
        return datosDespues;
    }

    public void setDatosDespues(String datosDespues) {
        this.datosDespues = datosDespues;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}

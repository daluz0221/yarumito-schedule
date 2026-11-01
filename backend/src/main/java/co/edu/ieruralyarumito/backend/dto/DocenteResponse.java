package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente; // Enum de estado del docente
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion; // Enum de vinculación docente
import java.time.LocalDate; // Tipo de fecha sin hora
import java.util.UUID; // Tipo de identificadores

// Datos que la API devuelve al consultar un docente.
public class DocenteResponse {

    // Identificación del docente.
    private UUID id; // Identificador único del docente

    // Información personal básica.
    private String nombres; // Nombres del docente
    private String apellidos; // Apellidos del docente
    private String tipoDocumento; // Tipo de documento
    private String numeroDocumento; // Número de documento

    // Información de contacto.
    private String telefono; // Teléfono de contacto
    private String correoInstitucional; // Correo institucional de contacto

    // Información institucional.
    private TipoVinculacion tipoVinculacion; // Tipo de vinculación del docente
    private UUID areaNombramientoId; // Identificador del área de nombramiento

    private String numeroDecreto; // Número del decreto de vinculación
    private LocalDate fechaDecreto; // Fecha del decreto de vinculación
    private String escalafon; // Escalafón docente

    // Parámetros de carga laboral.
    private int horasSemanalesContratadas; // Carga semanal contratada
    private int maxHorasExtra; // Máximo de horas extra permitidas
    private boolean esExclusivoMediaTecnica; // Indica exclusividad para media técnica

    // Estado y vinculación.
    private EstadoDocente estado; // Estado actual del docente
    private LocalDate fechaVinculacion; // Fecha de inicio de vinculación

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public TipoVinculacion getTipoVinculacion() {
        return tipoVinculacion;
    }

    public void setTipoVinculacion(TipoVinculacion tipoVinculacion) {
        this.tipoVinculacion = tipoVinculacion;
    }

    public UUID getAreaNombramientoId() {
        return areaNombramientoId;
    }

    public void setAreaNombramientoId(UUID areaNombramientoId) {
        this.areaNombramientoId = areaNombramientoId;
    }

    public String getNumeroDecreto() {
        return numeroDecreto;
    }

    public void setNumeroDecreto(String numeroDecreto) {
        this.numeroDecreto = numeroDecreto;
    }

    public LocalDate getFechaDecreto() {
        return fechaDecreto;
    }

    public void setFechaDecreto(LocalDate fechaDecreto) {
        this.fechaDecreto = fechaDecreto;
    }

    public String getEscalafon() {
        return escalafon;
    }

    public void setEscalafon(String escalafon) {
        this.escalafon = escalafon;
    }

    public int getHorasSemanalesContratadas() {
        return horasSemanalesContratadas;
    }

    public void setHorasSemanalesContratadas(int horasSemanalesContratadas) {
        this.horasSemanalesContratadas = horasSemanalesContratadas;
    }

    public int getMaxHorasExtra() {
        return maxHorasExtra;
    }

    public void setMaxHorasExtra(int maxHorasExtra) {
        this.maxHorasExtra = maxHorasExtra;
    }

    public boolean isEsExclusivoMediaTecnica() {
        return esExclusivoMediaTecnica;
    }

    public void setEsExclusivoMediaTecnica(boolean esExclusivoMediaTecnica) {
        this.esExclusivoMediaTecnica = esExclusivoMediaTecnica;
    }

    public EstadoDocente getEstado() {
        return estado;
    }

    public void setEstado(EstadoDocente estado) {
        this.estado = estado;
    }

    public LocalDate getFechaVinculacion() {
        return fechaVinculacion;
    }

    public void setFechaVinculacion(LocalDate fechaVinculacion) {
        this.fechaVinculacion = fechaVinculacion;
    }
}


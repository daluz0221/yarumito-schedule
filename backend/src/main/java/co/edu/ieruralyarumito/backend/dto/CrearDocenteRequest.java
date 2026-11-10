package co.edu.ieruralyarumito.backend.dto;

// Importa validaciones para los campos obligatorios.
import jakarta.validation.constraints.NotBlank; // Valida textos obligatorios
import co.edu.ieruralyarumito.backend.entity.enums.TipoVinculacion; // Enum de vinculación docente
import jakarta.validation.constraints.NotNull; // Valida campos obligatorios no textuales
import java.util.UUID; // Tipo del identificador del área
import co.edu.ieruralyarumito.backend.entity.enums.EstadoDocente; // Enum de estado del docente
import java.time.LocalDate; // Tipo de fecha sin hora

// Datos necesarios para registrar un nuevo docente.
public class CrearDocenteRequest {

    // Información personal básica del docente.
    @NotBlank(message = "Los nombres son obligatorios") // No permite null, vacío ni solo espacios
    private String nombres; // Nombres del docente

    @NotBlank(message = "Los apellidos son obligatorios") // No permite null, vacío ni solo espacios
    private String apellidos; // Apellidos del docente

    @NotBlank(message = "El tipo de documento es obligatorio") // Campo obligatorio
    private String tipoDocumento; // Tipo de documento del docente

    @NotBlank(message = "El número de documento es obligatorio") // Campo obligatorio
    private String numeroDocumento; // Número de documento del docente

    private String telefono; // Teléfono de contacto del docente

    private String correoInstitucional; // Correo institucional de contacto

    @NotNull(message = "El tipo de vinculación es obligatorio") // Campo obligatorio
    private TipoVinculacion tipoVinculacion; // Tipo de vinculación del docente

    @NotNull(message = "El área de nombramiento es obligatoria") // Campo obligatorio
    private UUID areaNombramientoId; // Identificador del área de nombramiento

    @NotNull(message = "El estado es obligatorio") // Campo obligatorio
    private EstadoDocente estado; // Estado inicial del docente

    // Información institucional complementaria del docente.
    private String numeroDecreto; // Número del decreto de vinculación

    private LocalDate fechaDecreto; // Fecha del decreto de vinculación

    private String escalafon; // Escalafón docente

    private LocalDate fechaVinculacion; // Fecha de inicio de vinculación

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

    public EstadoDocente getEstado() {
        return estado;
    }

    public void setEstado(EstadoDocente estado) {
        this.estado = estado;
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

    public LocalDate getFechaVinculacion() {
        return fechaVinculacion;
    }

    public void setFechaVinculacion(LocalDate fechaVinculacion) {
        this.fechaVinculacion = fechaVinculacion;
    }
}





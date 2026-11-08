package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.EstadoAsignacionAcademica;

import java.util.List;
import java.util.UUID;

// Respuesta completa del tablero "Construcción de horario" para un grupo.
public class HorarioGrupoResponse {

    private UUID grupoId;
    private String grupoCodigo;

    private UUID gradoId;
    private String gradoNombre;

    private UUID anioEscolarId;
    private int anioEscolar;

    private UUID sedeId;
    private String sedeNombre;

    private UUID horarioId;
    private int versionHorario;

    private UUID turnoId;
    private String turnoNombre;

    private EstadoAsignacionAcademica estadoHorario;

    private ResumenHorarioResponse resumen;

    private List<FranjaHorariaResponse> franjas;
    private List<ClaseProgramadaResponse> clasesProgramadas;
    private List<AsignacionPendienteResponse> asignacionesPendientes;

    public UUID getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(UUID grupoId) {
        this.grupoId = grupoId;
    }

    public String getGrupoCodigo() {
        return grupoCodigo;
    }

    public void setGrupoCodigo(String grupoCodigo) {
        this.grupoCodigo = grupoCodigo;
    }

    public UUID getGradoId() {
        return gradoId;
    }

    public void setGradoId(UUID gradoId) {
        this.gradoId = gradoId;
    }

    public String getGradoNombre() {
        return gradoNombre;
    }

    public void setGradoNombre(String gradoNombre) {
        this.gradoNombre = gradoNombre;
    }

    public UUID getAnioEscolarId() {
        return anioEscolarId;
    }

    public void setAnioEscolarId(UUID anioEscolarId) {
        this.anioEscolarId = anioEscolarId;
    }

    public int getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(int anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public UUID getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(UUID horarioId) {
        this.horarioId = horarioId;
    }

    public int getVersionHorario() {
        return versionHorario;
    }

    public void setVersionHorario(int versionHorario) {
        this.versionHorario = versionHorario;
    }

    public UUID getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(UUID turnoId) {
        this.turnoId = turnoId;
    }

    public String getTurnoNombre() {
        return turnoNombre;
    }

    public void setTurnoNombre(String turnoNombre) {
        this.turnoNombre = turnoNombre;
    }

    public EstadoAsignacionAcademica getEstadoHorario() {
        return estadoHorario;
    }

    public void setEstadoHorario(EstadoAsignacionAcademica estadoHorario) {
        this.estadoHorario = estadoHorario;
    }

    public ResumenHorarioResponse getResumen() {
        return resumen;
    }

    public void setResumen(ResumenHorarioResponse resumen) {
        this.resumen = resumen;
    }

    public List<FranjaHorariaResponse> getFranjas() {
        return franjas;
    }

    public void setFranjas(List<FranjaHorariaResponse> franjas) {
        this.franjas = franjas;
    }

    public List<ClaseProgramadaResponse> getClasesProgramadas() {
        return clasesProgramadas;
    }

    public void setClasesProgramadas(
            List<ClaseProgramadaResponse> clasesProgramadas) {

        this.clasesProgramadas = clasesProgramadas;
    }

    public List<AsignacionPendienteResponse> getAsignacionesPendientes() {
        return asignacionesPendientes;
    }

    public void setAsignacionesPendientes(
            List<AsignacionPendienteResponse> asignacionesPendientes) {

        this.asignacionesPendientes = asignacionesPendientes;
    }
}

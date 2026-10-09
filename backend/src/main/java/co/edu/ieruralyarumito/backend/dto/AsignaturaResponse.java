package co.edu.ieruralyarumito.backend.dto;

import java.util.UUID;

// Datos públicos de una asignatura para el catálogo del área.
public class AsignaturaResponse {

    private UUID id;
    private UUID areaId;
    private String nombre;
    private String codigo;
    private String abreviatura;
    private String colorUi;
    private boolean exigeIdoneidadEstricta;
    private boolean esMediaTecnica;
    private boolean requiereDocenteExclusivo;
    private String tipoAulaRequerida;
    private int maxClasesConsecutivas;
    private boolean activa;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
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

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public String getColorUi() {
        return colorUi;
    }

    public void setColorUi(String colorUi) {
        this.colorUi = colorUi;
    }

    public boolean isExigeIdoneidadEstricta() {
        return exigeIdoneidadEstricta;
    }

    public void setExigeIdoneidadEstricta(boolean exigeIdoneidadEstricta) {
        this.exigeIdoneidadEstricta = exigeIdoneidadEstricta;
    }

    public boolean isEsMediaTecnica() {
        return esMediaTecnica;
    }

    public void setEsMediaTecnica(boolean esMediaTecnica) {
        this.esMediaTecnica = esMediaTecnica;
    }

    public boolean isRequiereDocenteExclusivo() {
        return requiereDocenteExclusivo;
    }

    public void setRequiereDocenteExclusivo(boolean requiereDocenteExclusivo) {
        this.requiereDocenteExclusivo = requiereDocenteExclusivo;
    }

    public String getTipoAulaRequerida() {
        return tipoAulaRequerida;
    }

    public void setTipoAulaRequerida(String tipoAulaRequerida) {
        this.tipoAulaRequerida = tipoAulaRequerida;
    }

    public int getMaxClasesConsecutivas() {
        return maxClasesConsecutivas;
    }

    public void setMaxClasesConsecutivas(int maxClasesConsecutivas) {
        this.maxClasesConsecutivas = maxClasesConsecutivas;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}

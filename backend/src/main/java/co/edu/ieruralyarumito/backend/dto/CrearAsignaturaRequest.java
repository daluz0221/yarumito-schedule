package co.edu.ieruralyarumito.backend.dto;

import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CrearAsignaturaRequest {

    @NotNull
    private UUID areaId;

    @NotBlank
    private String nombre;

    @NotBlank
    private String codigo;

    private String abreviatura;

    private String colorUi;

    @NotNull
    private Boolean exigeIdoneidadEstricta;

    @NotNull
    private Boolean esMediaTecnica;

    @NotNull
    private Boolean requiereDocenteExclusivo;

    private TipoAulaRequerida tipoAulaRequerida;

    @NotNull
    private Integer maxClasesConsecutivas = 2;

    @NotNull
    private Boolean activa;

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

    public Boolean getExigeIdoneidadEstricta() {
        return exigeIdoneidadEstricta;
    }

    public void setExigeIdoneidadEstricta(Boolean exigeIdoneidadEstricta) {
        this.exigeIdoneidadEstricta = exigeIdoneidadEstricta;
    }

    public Boolean getEsMediaTecnica() {
        return esMediaTecnica;
    }

    public void setEsMediaTecnica(Boolean esMediaTecnica) {
        this.esMediaTecnica = esMediaTecnica;
    }

    public Boolean getRequiereDocenteExclusivo() {
        return requiereDocenteExclusivo;
    }

    public void setRequiereDocenteExclusivo(Boolean requiereDocenteExclusivo) {
        this.requiereDocenteExclusivo = requiereDocenteExclusivo;
    }

    public TipoAulaRequerida getTipoAulaRequerida() {
        return tipoAulaRequerida;
    }

    public void setTipoAulaRequerida(TipoAulaRequerida tipoAulaRequerida) {
        this.tipoAulaRequerida = tipoAulaRequerida;
    }

    public Integer getMaxClasesConsecutivas() {
        return maxClasesConsecutivas;
    }

    public void setMaxClasesConsecutivas(Integer maxClasesConsecutivas) {
        this.maxClasesConsecutivas = maxClasesConsecutivas;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}

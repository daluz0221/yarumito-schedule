package co.edu.ieruralyarumito.backend.entity;

// Enum que representa el tipo de espacio físico requerido por la asignatura.
import co.edu.ieruralyarumito.backend.entity.enums.TipoAulaRequerida;

// Importaciones necesarias para el mapeo JPA/Hibernate.
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

import java.util.UUID;

// Representa una asignatura perteneciente a un área académica.
@Entity
public class Asignatura {

    // Identificador único de la asignatura.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Área académica a la cual pertenece la asignatura.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    // Nombre completo de la asignatura.
    @Column(nullable = false)
    private String nombre;

    // Código institucional único de la asignatura.
    @Column(nullable = false, unique = true)
    private String codigo;

    // Abreviatura utilizada para mostrar la asignatura en la interfaz.
    private String abreviatura;

    // Color opcional utilizado para representar la asignatura en la interfaz.
    @Column(name = "color_ui")
    private String colorUi;

    // Indica si la asignatura exige validación estricta de idoneidad docente.
    @Column(name = "exige_idoneidad_estricta", nullable = false)
    private boolean exigeIdoneidadEstricta;

    // Indica si la asignatura pertenece a la Media Técnica.
    @Column(name = "es_media_tecnica", nullable = false)
    private boolean esMediaTecnica;

    // Indica si la asignatura requiere un docente exclusivo.
    @Column(name = "requiere_docente_exclusivo", nullable = false)
    private boolean requiereDocenteExclusivo;

    // Tipo de espacio físico requerido para impartir la asignatura.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aula_requerida")
    private TipoAulaRequerida tipoAulaRequerida;

    // Número máximo de clases consecutivas permitidas para la asignatura.
    @Column(name = "max_clases_consecutivas", nullable = false)
    private int maxClasesConsecutivas = 2;

    // Indica si la asignatura se encuentra habilitada para su uso.
    @Column(nullable = false)
    private boolean activa;

    // Retorna el identificador único de la asignatura.
    public UUID getId() {
        return id;
    }

    // Retorna el área académica de la asignatura.
    public Area getArea() {
        return area;
    }

    // Permite establecer el área académica de la asignatura.
    public void setArea(Area area) {
        this.area = area;
    }

    // Retorna el nombre de la asignatura.
    public String getNombre() {
        return nombre;
    }

    // Permite establecer el nombre de la asignatura.
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Retorna el código institucional de la asignatura.
    public String getCodigo() {
        return codigo;
    }

    // Permite establecer el código institucional de la asignatura.
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    // Retorna la abreviatura de la asignatura.
    public String getAbreviatura() {
        return abreviatura;
    }

    // Permite establecer la abreviatura de la asignatura.
    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    // Retorna el color utilizado en la interfaz.
    public String getColorUi() {
        return colorUi;
    }

    // Permite establecer el color utilizado en la interfaz.
    public void setColorUi(String colorUi) {
        this.colorUi = colorUi;
    }

    // Indica si la asignatura exige idoneidad estricta.
    public boolean isExigeIdoneidadEstricta() {
        return exigeIdoneidadEstricta;
    }

    // Permite definir si la asignatura exige idoneidad estricta.
    public void setExigeIdoneidadEstricta(boolean exigeIdoneidadEstricta) {
        this.exigeIdoneidadEstricta = exigeIdoneidadEstricta;
    }

    // Indica si la asignatura pertenece a Media Técnica.
    public boolean isEsMediaTecnica() {
        return esMediaTecnica;
    }

    // Permite definir si la asignatura pertenece a Media Técnica.
    public void setEsMediaTecnica(boolean esMediaTecnica) {
        this.esMediaTecnica = esMediaTecnica;
    }

    // Indica si la asignatura requiere docente exclusivo.
    public boolean isRequiereDocenteExclusivo() {
        return requiereDocenteExclusivo;
    }

    // Permite definir si la asignatura requiere docente exclusivo.
    public void setRequiereDocenteExclusivo(boolean requiereDocenteExclusivo) {
        this.requiereDocenteExclusivo = requiereDocenteExclusivo;
    }

    // Retorna el tipo de aula requerido.
    public TipoAulaRequerida getTipoAulaRequerida() {
        return tipoAulaRequerida;
    }

    // Permite establecer el tipo de aula requerido.
    public void setTipoAulaRequerida(TipoAulaRequerida tipoAulaRequerida) {
        this.tipoAulaRequerida = tipoAulaRequerida;
    }

    // Retorna el máximo de clases consecutivas permitido.
    public int getMaxClasesConsecutivas() {
        return maxClasesConsecutivas;
    }

    // Permite establecer el máximo de clases consecutivas.
    public void setMaxClasesConsecutivas(int maxClasesConsecutivas) {
        this.maxClasesConsecutivas = maxClasesConsecutivas;
    }

    // Indica si la asignatura se encuentra activa.
    public boolean isActiva() {
        return activa;
    }

    // Permite cambiar el estado activo de la asignatura.
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}

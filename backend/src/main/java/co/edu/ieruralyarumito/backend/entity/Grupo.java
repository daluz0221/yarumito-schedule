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
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

// Representa un grupo real de estudiantes dentro de un año escolar.
@Entity
@Table(
        name = "grupo",
        schema = "catalogoacademico",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_grupo_anio_codigo",
                        columnNames = {
                                "anio_escolar_id",
                                "codigo"
                        }
                )
        }
)
public class Grupo {

    // Identificador único del grupo.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Código del grupo: 601, 602, 1001, 1101, etc.
    @Column(nullable = false)
    private String codigo;

    // Grado al que pertenece el grupo.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grado_id", nullable = false)
    private Grado grado;

    // Año escolar al que pertenece el grupo.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anio_escolar_id", nullable = false)
    private AnioEscolar anioEscolar;

    // Sede en la que funciona el grupo.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    // Docente director del grupo. Puede asignarse posteriormente.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_grupo_id")
    private Docente directorGrupo;

    // Cantidad actual de estudiantes del grupo.
    @Column(name = "cantidad_estudiantes")
    private Integer cantidadEstudiantes;

    // Aula habitual del grupo.
    // La relación es opcional: un grupo puede existir sin aula fija.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aula_fija_id")
    private Aula aulaFija;

    // Indica si el grupo se encuentra activo.
    @Column(nullable = false)
    private boolean activo;

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
    }

    public AnioEscolar getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(AnioEscolar anioEscolar) {
        this.anioEscolar = anioEscolar;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public Docente getDirectorGrupo() {
        return directorGrupo;
    }

    public void setDirectorGrupo(Docente directorGrupo) {
        this.directorGrupo = directorGrupo;
    }

    public Integer getCantidadEstudiantes() {
        return cantidadEstudiantes;
    }

    public void setCantidadEstudiantes(Integer cantidadEstudiantes) {
        this.cantidadEstudiantes = cantidadEstudiantes;
    }

    public Aula getAulaFija() {
        return aulaFija;
    }

    public void setAulaFija(Aula aulaFija) {
        this.aulaFija = aulaFija;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

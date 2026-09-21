package co.edu.ieruralyarumito.backend.entity;

import co.edu.ieruralyarumito.backend.entity.enums.NivelTituloProfesional;
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

// Representa un título académico asociado a un docente.
@Entity
public class TituloProfesional {

    // Identificador único del título profesional.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Docente titular del título.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    // Nivel académico del título profesional.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelTituloProfesional nivel;

    // Nombre oficial del título obtenido.
    @Column(name = "nombre_titulo", nullable = false)
    private String nombreTitulo;

    // Institución que otorgó el título.
    private String institucion;

    // Año en que el docente obtuvo el título.
    @Column(name = "anio_graduacion")
    private Integer anioGraduacion;

    // Ruta o referencia al archivo que soporta el título.
    @Column(name = "archivo_soporte")
    private String archivoSoporte;

    // Retorna el identificador único del título profesional.
    public UUID getId() {
        return id;
    }

    // Retorna el docente titular del título.
    public Docente getDocente() {
        return docente;
    }

    // Permite establecer el docente titular del título.
    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    // Retorna el nivel académico del título.
    public NivelTituloProfesional getNivel() {
        return nivel;
    }

    // Permite establecer el nivel académico del título.
    public void setNivel(NivelTituloProfesional nivel) {
        this.nivel = nivel;
    }

    public String getNombreTitulo() {
        return nombreTitulo;
    }

    public void setNombreTitulo(String nombreTitulo) {
        this.nombreTitulo = nombreTitulo;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public Integer getAnioGraduacion() {
        return anioGraduacion;
    }

    public void setAnioGraduacion(Integer anioGraduacion) {
        this.anioGraduacion = anioGraduacion;
    }

    public String getArchivoSoporte() {
        return archivoSoporte;
    }

    public void setArchivoSoporte(String archivoSoporte) {
        this.archivoSoporte = archivoSoporte;
    }
}

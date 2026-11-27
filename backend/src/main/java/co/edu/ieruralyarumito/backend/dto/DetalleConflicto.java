package co.edu.ieruralyarumito.backend.dto;

// Datos concretos del intento que produjo el conflicto.
// Los conflictos antiguos no los tienen y llegan en null.
public class DetalleConflicto {

    private final String dia;
    private final String franja;
    private final String elementoAfectado;
    private final String asignatura;
    private final String docente;
    private final String aula;
    private final String grupo;

    public DetalleConflicto(
            String dia,
            String franja,
            String elementoAfectado,
            String asignatura,
            String docente,
            String aula,
            String grupo) {

        this.dia = dia;
        this.franja = franja;
        this.elementoAfectado = elementoAfectado;
        this.asignatura = asignatura;
        this.docente = docente;
        this.aula = aula;
        this.grupo = grupo;
    }

    public String getDia() {
        return dia;
    }

    public String getFranja() {
        return franja;
    }

    public String getElementoAfectado() {
        return elementoAfectado;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public String getDocente() {
        return docente;
    }

    public String getAula() {
        return aula;
    }

    public String getGrupo() {
        return grupo;
    }
}

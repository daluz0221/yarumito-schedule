package co.edu.ieruralyarumito.backend.exception;

// Representa un conflicto de programación que impide guardar la clase.
// Incluye un código tipificado para que el frontend identifique el modal.
public class ConflictoHorarioException extends RuntimeException {

    private final String codigo;

    public ConflictoHorarioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
